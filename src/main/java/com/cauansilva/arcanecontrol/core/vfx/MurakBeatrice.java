package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Murak (G-C do Subaru, lancado pela Beatrice no contrato): magia Yin que tira o peso. Ela estala os dedos, uma gota
 * violeta voa da mao dela ate o Subaru e tres aneis de runas (tornozelos, cintura, peito) passam a orbitar o corpo
 * dele; pedrinhas do chao em volta flutuam como se a gravidade tivesse sumido. Com o efeito ele salta muito mais alto,
 * para um instante no topo e desce devagar deixando um rastro, pousando macio com uma onda no chao.
 * Tempo t a partir do cast. Referencial: origem nos pes do Subaru (no chao), x lateral (+x = lado da Beatrice),
 * y para cima, z para frente. Classe pura para a previa e os testes. No jogo: DURACAO ticks de salto alto e queda lenta
 * (os aneis acompanham o jogador); o salto de altura(t) existe so para a previa.
 */
public final class MurakBeatrice {
    public record P(double x,double y,double z){}
    /** Duracao do efeito no jogo (8 s), forca do salto e gravidade relativa na queda. */
    public static final int DURACAO=160;
    public static final double ALTURA_SALTO=4.5,GRAVIDADE_QUEDA=.3;
    public static final int GOTA=6,ANEIS_IN=10,SALTO=16,SOBE=10,TOPO=5,DESCE=26,POUSO=SALTO+SOBE+TOPO+DESCE,ONDA=10,FIM=POUSO+ONDA+6;
    public static final int ANEIS=3,PONTOS_ANEL=36,PEDRAS=14,RASTRO=8,PONTOS_ONDA=40,PONTOS_GOTA=10;
    public static final double[] ALTURA_ANEL={.15,.9,1.35},RAIO_ANEL={.5,.55,.48};
    public static final P MAOS=new P(MinyaBeatrice.MAOS.x(),MinyaBeatrice.MAOS.y(),MinyaBeatrice.MAOS.z());

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}

    // ---------- salto da previa ----------
    /** Altura dos pes do Subaru: sobe rapido desacelerando, paira no topo e desce devagar, sem tranco no pouso. */
    public static double altura(double t){
        double a=t-SALTO;if(a<=0)return 0;
        if(a<SOBE){double u=a/SOBE;return ALTURA_SALTO*(1-(1-u)*(1-u));}
        a-=SOBE;if(a<TOPO)return ALTURA_SALTO+.08*Math.sin(Math.PI*a/TOPO);
        a-=TOPO;if(a<DESCE)return ALTURA_SALTO*(1-s(a/DESCE));
        return 0;
    }
    /** Avanco para a frente durante o salto (o salto e para frente, nao no lugar). */
    public static double avanco(double t){return 2.2*s((t-SALTO)/(SOBE+TOPO+DESCE));}
    public static P corpo(double t){return new P(0,altura(t),avanco(t));}

    // ---------- gota da Beatrice ate o Subaru ----------
    /** Ponto i da gota (0 = cauda, 1 = cabeca) voando em arco da mao dela ao peito dele. */
    public static P gota(int i,double t){
        double u=lim((t-(double)i/PONTOS_GOTA*.9)/GOTA),arco=.6*Math.sin(Math.PI*u);var peito=new P(0,1.1,0);
        return new P(MAOS.x()+(peito.x()-MAOS.x())*u,MAOS.y()+(peito.y()-MAOS.y())*u+arco,MAOS.z()+(peito.z()-MAOS.z())*u);
    }
    public static double alphaGota(double t){return t<0||t>GOTA+1?0:1;}

    // ---------- aneis de runas no corpo ----------
    /** Abertura (0..1) do anel a: abrem um a um depois que a gota chega, com passada. */
    public static double aberturaAnel(int a,double t){
        double u=lim((t-GOTA-a*1.2)/(ANEIS_IN-GOTA));double x=u-1,c=1.2;return u<=0?0:1+(c+1)*x*x*x+c*x*x;
    }
    public static P anel(int a,int i,double t){
        var c=corpo(t);double g=2*Math.PI*i/PONTOS_ANEL+t*(.12+.04*a)*(a%2==0?1:-1),r=RAIO_ANEL[a]*aberturaAnel(a,t);
        // inclina um pouco conforme a velocidade vertical (sobe: anel de baixo fica para tras)
        double v=altura(t+.5)-altura(t-.5),incl=Math.max(-.25,Math.min(.25,v*.25))*(a-1)+.06*Math.sin(t*.2+a*2);
        return new P(c.x()+Math.cos(g)*r,c.y()+ALTURA_ANEL[a]+Math.sin(g)*r*incl,c.z()+Math.sin(g)*r);
    }
    public static double alphaAnel(int a,double t){return t>=FIM?0:Math.min(1,aberturaAnel(a,t))*(1-s((t-FIM+6)/6));}

    // ---------- pedrinhas flutuando ----------
    public static P pedra(int k,double t){
        double a=2*Math.PI*hash(k*3),r=.6+1.0*hash(k*5),sobe=s((t-GOTA-hash(k*7)*4)/14)*(.4+.9*hash(k*9));
        return new P(Math.cos(a+t*.01)*r,.05+sobe+.06*Math.sin(t*.15+k),Math.sin(a+t*.01)*r);
    }
    public static double tamanhoPedra(int k,double t){return t<GOTA?0:(.04+.04*hash(k*11))*(1-s((t-FIM+8)/8));}

    // ---------- rastro na queda ----------
    /** Copia k do rastro: o mesmo corpo k*1,5 ticks atras, so enquanto ele esta no ar. */
    public static P rastro(int k,double t){return corpo(t-k*1.5);}
    public static double alphaRastro(int k,double t){double tk=t-k*1.5;return altura(tk)<.05||altura(t)<.05?0:.5*(1-(double)k/RASTRO);}

    // ---------- pouso macio ----------
    public static P onda(int i,double t){
        double a=t-POUSO,r=.3+1.8*s(a/ONDA),g=2*Math.PI*i/PONTOS_ONDA;var c=corpo(POUSO);
        return new P(c.x()+Math.cos(g)*r,.03,c.z()+Math.sin(g)*r);
    }
    public static double alphaOnda(double t){double a=t-POUSO;return a<0||a>=ONDA?0:1-a/ONDA;}

    private MurakBeatrice(){}
}
