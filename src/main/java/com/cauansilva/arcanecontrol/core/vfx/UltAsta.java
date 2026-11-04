package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Asta's ultimate in the Devil Union (V, efeito 307), from the user's "My second sword!" video: the grimoire opens,
 * he pulls the Demon-Dweller out of its red pages, crosses both swords in a burst of black aura, dashes forward with
 * a flurry of solid anti-magic slashes and ends with a giant X. Cuts are volumes (lens cross-section), not sheets.
 * Caster frame: x = his right, y up, z forward, origin at his feet when cast. Camera keyframes are local-only.
 */
public final class UltAsta {
    public record P(double x,double y,double z){}
    public record Quadro(P camera,P alvo,double fov){}
    public static final int DURACAO=80,SAIDA_ESPADA=18,CORTES=6,PRIMEIRO=32,PASSO=4,FINAL=60,AVANCO=6;
    public static final float DANO_CORTE=3,DANO_X=14;
    public static final double ALCANCE_X=7,RAIO_CORTE=2.6,ESPESSURA_CORTE=.35,RAIO_X=8,ARCO_X=.45,ESPESSURA_X=.6,ARCO=1.05,CURVA=.9;
    private static final double[] ROLAGEM={35,-35,80,-20,15,-60};
    // Tilt of each cut's plane away from facing the path (90 = horizontal): the blades wrap around the target in 3D.
    private static final double[] INCLINACAO={60,-50,20,75,-70,35};
    public static final double ARCO_CORTE=1.3;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    /** Forward dash 30..58, in blocks. */
    public static double avanco(double t){return AVANCO*s((t-30)/28.0);}

    /** Grimoire at his left: opens for the draw, closes, and comes back at the end to take the sword. */
    public static final P LIVRO=new P(-1.0,1.35,.6);
    public static double abrirLivro(double t){return s(t/6)*(1-s((t-22)/4))+s((t-66)/4)*(1-s((t-76)/4));}
    /** The Demon-Dweller rises out of the pages 8..18 (0..1). */
    public static double emergir(double t){return t>=SAIDA_ESPADA?1:s((t-8)/10.0);}
    /** Demon-Dweller drawn (in the left hand) from 18 until it sinks back into the book 68..74. */
    public static double espadaEsquerda(double t){return t<SAIDA_ESPADA?0:1-s((t-68)/6.0);}

    /** Crossing both swords: a burst of black flame tongues around the body, 20..32. */
    public static double explosao(double t){return t<20?0:s((t-20)/3)*(1-s((t-26)/6));}
    public static final int LINGUAS=16;
    public static P lingua(int k,double u,double t){
        double a=k*Math.PI*2/LINGUAS+hash(k)*.4,r=.5+u*(1.4+hash(k+1))*explosao(t),y=.3+hash(k+2)*1.6+u*(1.2+hash(k+3))*explosao(t);
        double onda=Math.sin(u*5-t*.7+k)*.25*u;
        return new P(Math.cos(a)*r+onda,y,Math.sin(a)*r+avanco(t));
    }

    public static int tempoCorte(int j){return PRIMEIRO+PASSO*j;}
    /** Sweep 0..1 of slash j: 3 ticks ending exactly on its hit tick, then the fade. */
    public static double progresso(int j,double t){return s((t-tempoCorte(j)+3)/3.0);}
    public static double alphaCorte(int j,double t){return t<tempoCorte(j)-3?0:1-s((t-tempoCorte(j)-2)/5.0);}
    public static P centroCorte(int j){return new P(0,1.3,avanco(tempoCorte(j))+1.6);}
    /** Point of slash j: f along the visible arc (0..1), v across it (0 inner, 1 outer), lado -1/+1 = which face. */
    public static P corte(int j,double f,double v,int lado,double t){return corte(j,centroCorte(j),f,v,lado,t);}
    /** Slash j around an explicit center (the locked target, sent by the server as effect 311). */
    public static P corte(int j,P centro,double f,double v,int lado,double t){
        return envolto(centro,Math.toRadians(ROLAGEM[j]),Math.toRadians(INCLINACAO[j]),RAIO_CORTE,ARCO_CORTE,ESPESSURA_CORTE,progresso(j,t),f,v,lado);
    }
    /**
     * A sword trail curving around c (its center of curvature), in a plane rolled by rho and tilted by theta, swept
     * over +-abertura; lens thickness along the plane normal. Seen in perspective it reads as part of an ellipse.
     */
    static P envolto(P c,double rho,double theta,double raio,double abertura,double espessura,double prog,double f,double v,int lado){
        double phi=-abertura+2*abertura*prog*f,g=Math.max(0,Math.min(1,prog*f));
        double esp=.32*raio*Math.pow(Math.sin(Math.PI*g),.8),r=raio-esp*(1-v);
        double th=lado*espessura*Math.pow(Math.sin(Math.PI*g),.6)*Math.sin(Math.PI*v)*.5;
        double ax=Math.sin(rho),ay=Math.cos(rho),bx=Math.cos(rho),by=-Math.sin(rho);// roll in the facing plane
        double ct=Math.cos(theta),st=Math.sin(theta);// tilt a toward the normal (0,0,1) around b
        double a2x=ax*ct,a2y=ay*ct,a2z=st,n2x=-ax*st,n2y=-ay*st,n2z=ct;
        double ca=Math.cos(phi)*r,sb=Math.sin(phi)*r;
        return new P(c.x()+a2x*ca+bx*sb+n2x*th,c.y()+a2y*ca+by*sb+n2y*th,c.z()+a2z*ca+n2z*th);
    }

    /** The final X: two long, nearly straight crescents crossing through their middles, swept 57..60 (hit tick), held, gone by the end. */
    public static double progressoX(double t){return s((t-FINAL+3)/3.0);}
    public static double alphaX(double t){return t<FINAL-3?0:1-s((t-72)/8.0);}
    public static P centroX(){return new P(0,3.0,avanco(FINAL)+3.0);}// High enough that the lower arms clear the ground
    public static P cruz(int k,double f,double v,int lado,double t){return cruz(k,centroX(),f,v,lado,t);}
    public static P cruz(int k,P centro,double f,double v,int lado,double t){
        return arco(centro,Math.toRadians(k==0?45:-45),RAIO_X,ARCO_X,ESPESSURA_X,progressoX(t),f,v,lado);
    }
    // Locked target (server): range to pick it at cast, how close the dash stops, the per-tick dash cap, and where the
    // cuts and the X sit above its feet. Index of the X among the 311 centers is CORTES.
    public static final double ALCANCE_ALVO=24,PARADA=2.2,PASSO_AVANCO=.45,ALTURA_CORTE=1.1,ALTURA_X=2.8;
    /** Tick the server announces cut j (or the X, j = CORTES) so the client can draw the whole sweep. */
    public static int anuncio(int j){return (j<CORTES?tempoCorte(j):FINAL)-3;}

    /**
     * Crescent (half-angle abertura) standing across the path, bulging toward roll angle rho (0 = up), tips curling back toward the caster,
     * with a lens-shaped thickness (sharp at both edges, thickest mid-width) so it reads as a solid blade of ink.
     */
    static P arco(P c,double rho,double raio,double abertura,double espessura,double prog,double f,double v,int lado){
        double phi=-abertura+2*abertura*prog*f,g=prog*f;// g: position along the full arc for the taper
        double esp=Math.min(.32*raio,1.3)*Math.pow(Math.sin(Math.PI*Math.max(0,Math.min(1,g))),.8);
        double r=raio-esp*(1-v),a=Math.cos(phi)*r-raio,b=Math.sin(phi)*r;
        double th=lado*espessura*Math.pow(Math.sin(Math.PI*Math.max(0,Math.min(1,g))),.6)*Math.sin(Math.PI*v)*.5;
        double ci=Math.cos(rho),si=Math.sin(rho),curl=(1-Math.cos(phi))*CURVA*raio/RAIO_CORTE;
        return new P(c.x()+a*si+b*ci,c.y()+a*ci-b*si,c.z()-curl+th);
    }

    // Local cutscene camera keyframes: tick, camera xyz, target xyz, fov multiplier. Both follow the dash.
    private static final double[][] CHAVES={
        {0,2.8,1.8,3.2,-.4,1.4,0,.85},{14,-2.6,1.6,1.9,-.9,1.3,.5,.8},{24,0,1.5,3.6,0,1.4,0,.9},
        {32,3.2,2.8,8.2,0,1.3,2.0,1.0},{50,-3.2,3.0,8.4,0,1.3,2.0,1.0},{58,-1.2,2.0,-3.6,0,1.6,4,1.05},
        {64,-2.0,3.5,-7.0,0,2.4,3,1.18},{80,-1.5,2.6,-5.0,0,1.4,2,1.0}
    };
    public static Quadro quadro(double t){
        double[] a=CHAVES[0],b=CHAVES[0];double u=0;
        if(t>=CHAVES[CHAVES.length-1][0])a=b=CHAVES[CHAVES.length-1];
        else if(t>0)for(int i=1;i<CHAVES.length;i++)if(t<=CHAVES[i][0]){a=CHAVES[i-1];b=CHAVES[i];u=s((t-a[0])/(b[0]-a[0]));break;}
        double[] k=new double[8];for(int i=1;i<8;i++)k[i]=a[i]+(b[i]-a[i])*u;double z=avanco(t);
        return new Quadro(new P(k[1],k[2],k[3]+z),new P(k[4],k[5],k[6]+z),k[7]);
    }
}
