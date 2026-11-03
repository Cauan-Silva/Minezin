package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Black Hurricane (efeito 305): o Asta puxa a antimagia em espiral, ela vira um funil de tinta girando em volta
 * dele e estoura em laminas que varrem 360 graus. A fronteira chega ao raio 8 do servidor exatamente no tick do
 * dano (PerfilAsta.impacto(1,true) = 10) e nunca passa dele; os fragmentos da magia arrancada sao puxados para o
 * funil e tudo some ate 32. Coordenadas em volta do dono: x/z no chao, y altura.
 */
public final class BlackHurricaneAsta {
    public record P(double x,double y,double z){}
    public static final int ESTOURO=10,FIM=32,RAIO=8,FIOS=8,BANDAS=14,LAMINAS=5,FRAGMENTOS=24;
    public static final double ALTURA_FUNIL=4.2,ARCO_LAMINA=1.15;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    /** Succao 0..6: fios de tinta em espiral do raio 5 ate o corpo. */
    public static double succao(double t){return t<0?0:s(t/2)*(1-s((t-5)/2));}
    public static P fio(int k,double u,double t){
        double a=k*Math.PI*2/FIOS+u*2.6+t*.25,r=.7+4.3*(1-u),y=.2+hash(k)*1.8+u*.4;
        return new P(Math.cos(a)*r,y,Math.sin(a)*r);
    }
    /** Trecho visivel do fio: a cabeca corre para dentro ao longo de u. */
    public static double alphaFio(int k,double u,double t){
        double frente=Math.min(1.2,t/5+hash(k+3)*.15);return succao(t)*Math.exp(-Math.pow((u-frente+.25)*3,2));
    }

    /** Abertura do funil (0..1) e giro acumulado: acelera ate o estouro e segue girando enquanto some. */
    public static double funil(double t){return s((t-3)/5.0);}
    public static double alphaFunil(double t){return t<3?0:1-s((t-ESTOURO-4)/10.0);}
    public static double giro(double t){
        double v0=.25,v1=.9;// rad/tick
        if(t<=ESTOURO)return v0*t+(v1-v0)*t*t/(2.0*ESTOURO);
        return v0*ESTOURO+(v1-v0)*ESTOURO/2.0+v1*(t-ESTOURO);
    }
    /** Risco de vento b do funil no ponto u (0 = chao, 1 = topo); cada risco tem raio e torcao proprios. */
    public static P banda(int b,double u,double t){
        double a=b*Math.PI*2/BANDAS+hash(b)*.6+u*(4.5+hash(b+1)*2)+giro(t),r=(.7+2.1*u)*(.8+.4*hash(b+2))*funil(t),y=u*ALTURA_FUNIL;
        return new P(Math.cos(a)*r,y,Math.sin(a)*r);
    }
    /** Trecho [ini, ini+COMPRIMENTO_RISCO] do risco b: sobe pelo funil e recomeca no chao. */
    public static double inicioRisco(int b,double t){double c=(t*.09+hash(b+3))%1.0;return c*(1+COMPRIMENTO_RISCO)-COMPRIMENTO_RISCO;}
    public static final double COMPRIMENTO_RISCO=.55;
    /** Meia-largura no ponto f (0..1) do trecho visivel: afilada nas duas pontas. */
    public static double larguraRisco(double u,double f){return (.05+.09*u)*Math.sin(Math.PI*Math.max(0,Math.min(1,f)));}

    /** Raio da frente das laminas: 0 ate o tick 6, chega a 8 exatamente no tick 10 e para ali. */
    public static double fronteira(double t){return t<6?0:RAIO*s((t-6)/(ESTOURO-6.0));}
    public static double alphaLaminas(double t){return t<6?0:1-s((t-ESTOURO-1)/8.0);}
    /** Lamina j: angulo do meio, girando com o funil. */
    public static double anguloLamina(int j,double t){return j*Math.PI*2/LAMINAS+giro(t)*.8;}
    /** Ponto f (0..1 ao longo do arco) e v (0 = borda interna, 1 = externa) da lamina j; externa = fronteira. */
    public static P lamina(int j,double f,double v,double t){
        double R=fronteira(t),esp=Math.min(1.4,R*.35)*Math.sin(Math.PI*Math.max(0,Math.min(1,f)));
        double a=anguloLamina(j,t)-ARCO_LAMINA*(f-.5),r=R-esp*(1-v),y=.7+.25*Math.sin(j*1.7);
        return new P(Math.cos(a)*r,y,Math.sin(a)*r);
    }
    /** Anel rasgado no chao marcando o raio 8 depois do estouro. */
    public static double anel(double t){return t<ESTOURO?0:s((t-ESTOURO)/1.5)*(1-s((t-ESTOURO-4)/12.0));}

    /** Fragmento i da magia arrancada: nasce dentro do raio 8 no estouro e e sugado em espiral para o funil. */
    public static P fragmento(int i,double t){
        double idade=Math.max(0,t-ESTOURO-hash(i)*2),k=s(idade/10.0),r0=2+5.8*hash(i+1),a=hash(i+2)*Math.PI*2+k*2.2;
        double r=r0*(1-k)+.9*k,y=.4+hash(i+3)*1.2+k*2.4;
        return new P(Math.cos(a)*r,y,Math.sin(a)*r);
    }
    public static double alphaFragmento(int i,double t){double idade=t-ESTOURO-hash(i)*2;return idade<0?0:s(idade/1.5)*(1-s((idade-7)/6.0));}
}
