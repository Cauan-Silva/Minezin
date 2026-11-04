package com.cauansilva.arcanecontrol.core.client.tecido;

import com.cauansilva.arcanecontrol.core.cid.MolaMantoCid;
import java.util.*;

/**
 * Ichigo's Bankai coat fully as cloth (tools/build_ichigo_traje): the back is one particle cloth of COLUNAS_COSTAS x
 * SEGMENTOS pinned along the waist (MalhaTecido); sides, corners and the open front strips follow its outer columns,
 * so the whole skirt of the coat is one connected cloth; both wide sleeves ride
 * damped angular springs (MolaMantoCid) sampled continuously along their SEGMENTOS, so long panels bend smoothly; the
 * side panels follow the back cloth's outer columns, so back and sides are one connected piece.
 * The sleeves also follow the arm swing; impulso() throws the whole coat outward (Bankai reveal) and it settles.
 * Bones: bk_capa_{costas|frente_esq|frente_dir|lado_esq|lado_dir|quina_esq|quina_dir}_{coluna}_{segmento}, bk_manga_{esq|dir}_{segmento}.
 */
public final class TecidoIchigo {
    public static final int SEGMENTOS=6,COLUNAS_COSTAS=6,COLUNAS_FRENTE=1,SEGMENTOS_MANGA=3,COLUNAS_FAIXA=2;
    public static final List<String> OSSOS;
    static{
        var nomes=new ArrayList<String>();
        for(var painel:new String[]{"costas","frente_esq","frente_dir","lado_esq","lado_dir","quina_esq","quina_dir","quinaf_esq","quinaf_dir"})
            for(int c=0;c<(painel.equals("costas")?COLUNAS_COSTAS:COLUNAS_FRENTE);c++)for(int s=0;s<SEGMENTOS;s++)nomes.add("bk_capa_"+painel+"_"+c+"_"+s);
        for(var lado:new String[]{"esq","dir"})for(int s=0;s<SEGMENTOS_MANGA;s++)nomes.add("bk_manga_"+lado+"_"+s);
        // Mugetsu form: loose ends of the face bandage (mg_faixa_{coluna}_{segmento}) and the closed skirt front.
        for(int c=0;c<COLUNAS_FAIXA;c++)for(int s=0;s<SEGMENTOS;s++)nomes.add("mg_faixa_"+c+"_"+s);
        for(int s=0;s<SEGMENTOS;s++)nomes.add("mg_saia_frente_meio_"+s);
        OSSOS=List.copyOf(nomes);
    }
    private static final MolaMantoCid.Pose ZERO=new MolaMantoCid.Pose(0,0,0);
    private final MalhaTecido costas;
    private final MolaMantoCid mangaEsq,mangaDir,cabelo;
    public TecidoIchigo(){this(PerfilTecido.PADRAO);}
    /** perfil: weight, stiffness, drag, inertia and spring response of the outfit (PerfilTecido). */
    public TecidoIchigo(PerfilTecido perfil){
        costas=new MalhaTecido(COLUNAS_COSTAS,SEGMENTOS+1,1.46/16,1.85/16,perfil);
        mangaEsq=new MolaMantoCid(1.5*perfil.mola());mangaDir=new MolaMantoCid(1.45*perfil.mola());cabelo=new MolaMantoCid(1.7*perfil.mola());
    }
    private double tempo,impulso,agitacao,anteriorAgitacao,subida;
    public void reset(){costas.reset();for(var m:molas())m.reset();tempo=impulso=agitacao=anteriorAgitacao=subida=0;}
    private MolaMantoCid[] molas(){return new MolaMantoCid[]{mangaEsq,mangaDir,cabelo};}
    /** Throws the coat outward for a few ticks (the Bankai reveal cut); decays by itself. */
    public void impulso(double forca){impulso=Math.max(impulso,forca);}
    public void tick(MolaMantoCid.Entrada e){tick(e,0,0);}
    /** braco*: change of each arm's swing angle this tick (radians), so the sleeves trail the arms. */
    public void tick(MolaMantoCid.Entrada e,double bracoEsq,double bracoDir){
        if(!Double.isFinite(e.frente()+e.lado()+e.vertical()+e.giro()+bracoEsq+bracoDir)){reset();return;}
        tempo++;var v=TecidoAsta.vento(tempo*1.2);double sopro=impulso*Math.sin(tempo*1.7);
        // Agitation follows speed (walking/running), turns, the reveal burst and falling; it drives the travelling ripple.
        double vel=Math.hypot(e.frente(),e.lado());anteriorAgitacao=agitacao;
        agitacao+=(Math.min(1.4,.12+vel*3.2+Math.abs(e.giro())*1.5+impulso+Math.max(0,-e.vertical())*2.5)-agitacao)*.25;
        subida+=(Math.max(-.2,Math.min(.6,-e.vertical()*2.2))-subida)*.3;
        double frente=e.frente()*1.6+v[0]*2.2-impulso*.6,lado=e.lado()*1.5+v[1]*2.2+sopro*.35,vertical=e.vertical()*1.4-impulso*.3;impulso*=.84;
        costas.tick(frente,lado,vertical,e.giro()+sopro*.2);
        // Bandage ends are light: they stream back hard and lift when falling.
        cabelo.tick(new MolaMantoCid.Entrada(frente*1.3-subida*.4,lado*1.2,vertical*1.3,e.giro()*1.2,e.noChao()));
        mangaEsq.tick(new MolaMantoCid.Entrada(frente*.7+bracoEsq*.22,lado*.6,vertical,e.giro()*.6,e.noChao()));
        mangaDir.tick(new MolaMantoCid.Entrada(frente*.7+bracoDir*.22,lado*.6,vertical,e.giro()*.6,e.noChao()));
    }
    /** Spring stage at a continuous position u in 0..3 (MolaMantoCid has 4 stages). */
    private static MolaMantoCid.Pose estagio(MolaMantoCid m,double u,double parcial){
        int i=(int)Math.floor(Math.min(2.999,Math.max(0,u)));double f=u-i;var a=m.amostrar(i,parcial);var b=m.amostrar(i+1,parcial);
        return new MolaMantoCid.Pose(a.x()+(b.x()-a.x())*f,a.y()+(b.y()-a.y())*f,a.z()+(b.z()-a.z())*f);
    }
    /** Parent-relative rotation delta (radians) for one coat bone; sampling never advances the physics. */
    public MolaMantoCid.Pose pose(String osso,double parcial){
        double ag=anteriorAgitacao+(agitacao-anteriorAgitacao)*parcial,tt=tempo+parcial;
        if(osso.startsWith("bk_manga_")){
            int s=Integer.parseInt(osso.substring(osso.lastIndexOf('_')+1));var m=osso.startsWith("bk_manga_esq_")?mangaEsq:mangaDir;int lado=m==mangaEsq?1:-1;
            var a=estagio(m,3.0*(s+1)/SEGMENTOS_MANGA,parcial);var b=estagio(m,3.0*s/SEGMENTOS_MANGA,parcial);double k=s==0?.8:1.4;
            double on=onda(s,0,tt,ag,SEGMENTOS_MANGA,lado>0?.3:1.9);
            return new MolaMantoCid.Pose(clamp(-(a.x()-b.x())*k+on,.6),clamp((a.y()-b.y())*k*.4,.2),clamp(((a.z()-b.z())*.6+Math.abs(a.x()-b.x())*.2)*k*lado+on*.4*lado,.35));
        }
        if(osso.startsWith("mg_faixa_")){
            String[] q=osso.substring(9).split("_");int c=Integer.parseInt(q[0]),s=Integer.parseInt(q[1]);
            var a=estagio(cabelo,3.0*(s+1)/SEGMENTOS,parcial);var b=estagio(cabelo,3.0*s/SEGMENTOS,parcial);double k=s==0?.9:1.5+.1*s;
            double on=onda(s,c+11,tt*1.4,ag*1.6+.25,SEGMENTOS,c*1.3),abre=c==0?-1:1;
            return new MolaMantoCid.Pose(clamp(-(s==0?a.x():a.x()-b.x())*k+on-subida*(s==0?.6:.15),.75),clamp((a.y()-b.y())*k*.5,.25),clamp(((a.z()-b.z())*.8)*k+abre*(.04+ag*.05)+on*.4*abre,.4));
        }
        if(osso.startsWith("mg_saia_frente_meio_")){
            // One piece with the sides (user): the closed front follows the mean of the back cloth's two outer columns,
            // the same ones the front strips and sides follow, so the whole skirt moves together.
            int s=Integer.parseInt(osso.substring(osso.lastIndexOf('_')+1));var r0=costas.rotacao(0,s,parcial);var r1=costas.rotacao(COLUNAS_COSTAS-1,s,parcial);
            double on=onda(s,3,tt,ag,SEGMENTOS,1.4);
            return new MolaMantoCid.Pose((r0.x()+r1.x())/2+on-subida*(s==0?.5:.12),-(r0.y()+r1.y())*.25,-(r0.z()+r1.z())*.3);
        }
        if(!osso.startsWith("bk_capa_"))return ZERO;
        String[] p=osso.substring(8).split("_");int s=Integer.parseInt(p[p.length-1]),c=Integer.parseInt(p[p.length-2]);
        if(osso.startsWith("bk_capa_costas_")){var r=costas.rotacao(c,s,parcial);double on=onda(s,c,tt,ag,SEGMENTOS,0);
            return new MolaMantoCid.Pose(r.x()+on-subida*(s==0?.5:.12),-r.y()+on*.15*(c-2.5)/2.5,-r.z()+onda(s,c,tt*1.3,ag,SEGMENTOS,2.1)*.35);}
        int lado=osso.contains("_esq_")?1:-1;
        // Side panels are one piece with the back (user): each follows the outer column of the back cloth, segment by
        // segment (left side = last column, x>0), so the corner moves as a single fabric.
        // Front strips, front corners, sides and back corners all follow the back cloth (user: everything joined).
        // Front strips and front corners take exactly the side's pose (user: the front must follow the side; every
        // limit or outward sweep of its own broke the sheet into flaps). The legs are kept out by the surface
        // collision (SuperficieTecido), which slides the cloth around their outer face.
        if(osso.startsWith("bk_capa_frente_")||osso.startsWith("bk_capa_quinaf_")||osso.startsWith("bk_capa_lado_")||osso.startsWith("bk_capa_quina"))return lateral(lado,s,parcial,tt,ag);
        return ZERO;
    }
    /** Side panel pose: follows the outer back column segment by segment (left side = last column). */
    private MolaMantoCid.Pose lateral(int lado,int s,double parcial,double tt,double ag){
        int col=lado>0?COLUNAS_COSTAS-1:0;var r=costas.rotacao(col,s,parcial);double on=onda(s,col,tt,ag,SEGMENTOS,0);
        return new MolaMantoCid.Pose(r.x()+on-subida*(s==0?.5:.12),-r.y()*.5,-r.z()*.6+lado*(Math.abs(r.x())*.2+subida*.15+on*.3));
    }
    /** Travelling ripple: runs from the waist to the hem (phase grows with the segment), each column offset, stronger
     *  on lower segments and with agitation; a slower swell plus a fast flutter so it never looks like one sine. */
    static double onda(int s,int c,double t,double agitacao,int segmentos,double fase){
        double u=(s+1.0)/segmentos,amp=agitacao*(.03+.09*u*u);
        return amp*(Math.sin(t*.55-s*.9+c*.7+fase)+.45*Math.sin(t*1.7-s*1.6+c*1.3+fase*2));
    }
    public double erroAlongamento(){return costas.erroAlongamento();}
    private static double clamp(double v,double max){return Math.max(-max,Math.min(max,v));}
}
