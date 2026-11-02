package com.cauansilva.arcanecontrol.core.client.tecido;

import java.util.*;

/**
 * Devil Union bat wings and tail, in the outfits' cloth style: the wings flap (fast downstroke, slower upstroke,
 * stronger when airborne or running) and every membrane strip/tail link chases the previous one through a damped
 * spring, so a wave runs from the leading edge to the scalloped hem like cloth. Fixed 20 Hz with three substeps; sampling never
 * advances the simulation. Pure Java; layout must match tools/armor_reference_shapes.py (ASA_* and CAUDA_*).
 * Poses are GeckoLib bone deltas in radians, added on top of the rest pose baked in the model.
 */
public final class DevilUnionTecido {
    public record Entrada(double frente,double lado,double vertical,double giro,boolean noChao){}
    public record Pose(double x,double y,double z){}
    /** Membrane strips per wing (inner to outer) and linked segments per strip; tail links. */
    public static final int ASA_FAIXAS=8,ASA_SEGMENTOS=3,CAUDA_SEGMENTOS=8;
    /** Idle flap cycle and full-effort cycle, in ticks. */
    public static final double CICLO_REPOUSO=44,CICLO_VOO=16;
    private static final Pose ZERO=new Pose(0,0,0);
    private record Osso(int tipo,int lado,int faixa,int segmento){}
    private static final Map<String,Osso> OSSOS_MAPA=new LinkedHashMap<>();
    public static final List<String> OSSOS;
    static{
        for(int lado=-1;lado<=1;lado+=2){
            String s=lado<0?"e":"d";OSSOS_MAPA.put("du_asa_"+s,new Osso(0,lado,-1,-1));
            for(int k=0;k<ASA_FAIXAS;k++)for(int j=0;j<ASA_SEGMENTOS;j++)OSSOS_MAPA.put("du_asa_"+s+"_"+k+"_"+j,new Osso(1,lado,k,j));
        }
        for(int i=0;i<CAUDA_SEGMENTOS;i++)OSSOS_MAPA.put("du_cauda_"+i,new Osso(2,0,-1,i));
        OSSOS=List.copyOf(OSSOS_MAPA.keySet());
    }

    /** A chain of world angles: link j springs toward link j-1 (root = driven input), so motion propagates with lag.
     * One-way coupling multiplies each link's resonance down the chain, so damping stays above ~0.55. */
    static final class Cadeia {
        final double[] w,v,anterior,rigidez,amortecimento;double raiz,raizAnterior;
        Cadeia(int n,double k0,double k1,double zeta){
            w=new double[n];v=new double[n];anterior=new double[n];rigidez=new double[n];amortecimento=new double[n];
            for(int j=0;j<n;j++){double t=n==1?0:j/(n-1d);rigidez[j]=k0+(k1-k0)*t;amortecimento[j]=2*Math.sqrt(rigidez[j])*zeta;}
        }
        void reset(){Arrays.fill(w,0);Arrays.fill(v,0);Arrays.fill(anterior,0);raiz=raizAnterior=0;}
        /** One 20 Hz tick toward root angle r with a per-link bias (drag) added to each target. */
        void tick(double r,double vies,double limite){
            System.arraycopy(w,0,anterior,0,w.length);raizAnterior=raiz;double dt=1/60d;
            for(int sub=0;sub<3;sub++){
                double alvo=raizAnterior+(r-raizAnterior)*(sub+1)/3d,vAlvo=(r-raizAnterior)*20;
                for(int j=0;j<w.length;j++){
                    double pai=j==0?alvo:w[j-1],vPai=j==0?vAlvo:v[j-1];
                    v[j]+=(rigidez[j]*(pai+vies-w[j])-amortecimento[j]*(v[j]-vPai))*dt;
                    w[j]+=v[j]*dt;
                    // Keep each joint within a believable bend of its parent.
                    double rel=w[j]-pai;if(Math.abs(rel)>limite){w[j]=pai+Math.copySign(limite,rel);v[j]=vPai;}
                }
            }
            raiz=r;
        }
        double raiz(double t){return raizAnterior+(raiz-raizAnterior)*t;}
        /** Parent-relative angle of link j (link 0 relative to the root input). */
        double local(int j,double t){
            double a=anterior[j]+(w[j]-anterior[j])*t,p=j==0?raizAnterior+(raiz-raizAnterior)*t:anterior[j-1]+(w[j-1]-anterior[j-1])*t;
            return a-p;
        }
    }

    // [lado][faixa]: front/back flutter and sideways sway chains per membrane strip; tail pitch and roll.
    private final Cadeia[][] fora=new Cadeia[2][ASA_FAIXAS],plano=new Cadeia[2][ASA_FAIXAS];
    private final Cadeia caudaPitch=new Cadeia(CAUDA_SEGMENTOS,150,45,.62),caudaRoll=new Cadeia(CAUDA_SEGMENTOS,130,40,.62);
    private double fase,faseAnterior,esforco,frente,lado,vertical,tempo;
    private final double[][] raiz=new double[2][2],raizAnterior=new double[2][2];

    public DevilUnionTecido(){
        for(int s=0;s<2;s++)for(int k=0;k<ASA_FAIXAS;k++){
            // Outer strips are softer: they trail the flap more, like loose membrane near the wing tip.
            double mole=k/(ASA_FAIXAS-1d);
            fora[s][k]=new Cadeia(ASA_SEGMENTOS,240-60*mole,95-30*mole,.55);
            plano[s][k]=new Cadeia(ASA_SEGMENTOS,300-60*mole,140-40*mole,.65);
        }
        reset();
    }
    public void reset(){
        for(var g:fora)for(var c:g)c.reset();for(var g:plano)for(var c:g)c.reset();caudaPitch.reset();caudaRoll.reset();
        fase=faseAnterior=esforco=frente=lado=vertical=tempo=0;for(int s=0;s<2;s++){raiz[s][0]=raiz[s][1]=raizAnterior[s][0]=raizAnterior[s][1]=0;}
    }

    /** Asymmetric stroke in [-1,1]: the downstroke (toward +1) takes 40% of the cycle, the recovery 60%. */
    static double batida(double fase){
        double u=fase/(2*Math.PI);u-=Math.floor(u);
        return u<.4?-Math.cos(Math.PI*u/.4):Math.cos(Math.PI*(u-.4)/.6);
    }

    public void tick(Entrada e){
        if(!Double.isFinite(e.frente()+e.lado()+e.vertical()+e.giro())){reset();return;}
        frente=clamp(e.frente(),-.6,.6);lado=clamp(e.lado(),-.6,.6);vertical=clamp(e.vertical(),-2,1.5);double giro=clamp(e.giro(),-.4,.4);
        // Effort: idle breathing flap on the ground, full flap airborne, in between when running.
        double alvo=e.noChao()?Math.min(.55,Math.abs(frente)*2.2+Math.abs(lado)*1.2):1;esforco+=(alvo-esforco)*.12;
        faseAnterior=fase;fase+=2*Math.PI/(CICLO_REPOUSO+(CICLO_VOO-CICLO_REPOUSO)*esforco);tempo++;
        double amp=.3+.7*esforco,b=batida(fase),fecha=batida(fase-.9);
        for(int s=0;s<2;s++){
            int ld=s==0?-1:1;raizAnterior[s][0]=raiz[s][0];raizAnterior[s][1]=raiz[s][1];
            // Root: forward sweep on the downstroke (minus a fold-back when running), and the wings close up and in
            // on the recovery; strafing and turning lean both wings the same way.
            double varre=amp*.42*b-frente*1.1,sobe=amp*.48*fecha+(vertical<0?vertical*.25:0)+ld*(lado*.9+giro*.6);
            raiz[s][0]=clamp(varre,-.9,.7);raiz[s][1]=clamp(sobe,-.8,.9);
            for(int k=0;k<ASA_FAIXAS;k++){
                // A strip further from the shoulder travels further when the root sweeps.
                double alcance=(k+.5)/ASA_FAIXAS*.8+.2;
                // Air drag blows the hem back while running.
                fora[s][k].tick(raiz[s][0]*alcance,-frente*.22,.75);
                plano[s][k].tick(raiz[s][1],(vertical<0?-vertical*.05:0),.6);
            }
        }
        // Tail: streams back when running, swings opposite to strafes and turns, and keeps a slow whip.
        caudaPitch.tick(clamp(-frente*2.6+(vertical<0?-vertical*.4:0)-.08*Math.sin(tempo*.09),-1.2,.6),-frente*.12,.7);
        // Entrada.lado > 0 is a step to the player's left (+x in the model): the tail trails to the right.
        caudaRoll.tick(clamp(-lado*2.2-giro*1.4+.18*Math.sin(tempo*.07),-1,1),0,.6);
    }

    /** Pose delta for a Devil Union bone; abertura (0 closed, 1 open) folds the wings during the entrance. */
    public Pose pose(String nome,double parcial,double abertura){
        var o=OSSOS_MAPA.get(nome);if(o==null)return ZERO;
        double t=Double.isFinite(parcial)?clamp(parcial,0,1):0,ab=Double.isFinite(abertura)?clamp(abertura,0,1):1;
        if(o.tipo==0){
            int s=o.lado<0?0:1;double varre=raizAnterior[s][0]+(raiz[s][0]-raizAnterior[s][0])*t,sobe=raizAnterior[s][1]+(raiz[s][1]-raizAnterior[s][1])*t;
            // Closed wings fold back against the spine and up; they open out over the entrance.
            varre-=(1-ab)*1.1;sobe+=(1-ab)*.9;
            // Cartesian sweep about Y (+ = toward the front for this side) and close about Z; GeckoLib negates Y.
            return new Pose(0,-(o.lado*varre),o.lado*sobe);
        }
        if(o.tipo==1){
            // Hanging strip: X swings the hem front/back (negative = back), Z sways it sideways with the wing.
            int s=o.lado<0?0:1;
            return new Pose(fora[s][o.faixa].local(o.segmento,t),0,o.lado*plano[s][o.faixa].local(o.segmento,t)*.6);
        }
        // The first link hangs from the body itself, so it carries the root swing too.
        int j=o.segmento;return new Pose(caudaPitch.local(j,t)+(j==0?caudaPitch.raiz(t):0),0,caudaRoll.local(j,t)+(j==0?caudaRoll.raiz(t):0));
    }
    public double esforco(){return esforco;}
    private static double clamp(double v,double a,double b){return Math.max(a,Math.min(b,v));}
}
