package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Liberacao do Bankai do Ichigo (G, efeito 200) em volume 3D: preparo com a Zangetsu estendida e o reiatsu vazando,
 * pilar preto de borda vermelha explodindo ao ceu no grito, poeira grossa cobrindo tudo, corte horizontal que abre a
 * poeira e revela o casaco do Bankai e a Tensa Zangetsu (troca visual no tick REVELA). Origem nos pes; x lateral,
 * y altura, z frente.
 */
public final class EntradaBankai {
    public record P(double x,double y,double z){}
    public static final int GRITO=14,REVELA=30,FIM=44,LINGUAS=18,ANEL=40,SECAO=8,COLUNA=16,VOLTAS=28,NUVENS=22,LAT=7,LON=12,ARCO=26,SECAO_CORTE=10;
    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    /** Fase legivel para a previa. */
    public static String fase(double t){return t<GRITO?"PREPARO":t<REVELA-6?"PILAR":t<REVELA?"POEIRA":t<REVELA+4?"REVELACAO":"AURA";}

    // Preparo: linguas de reiatsu vazando do chao em volta, mais fortes ate o grito.
    public static double vazamento(double t){return t>=GRITO+2?0:s(t/(GRITO*.9));}
    public static P lingua(int j,double u,double t){
        double a=j*Math.PI*2/LINGUAS+t*.05,r=(.7+.25*Math.sin(t*.7+j))*(1-u*.4),alt=(.9+1.4*vazamento(t))*(.8+.4*hash(j+Math.floor(t/2)));
        return new P(Math.cos(a)*r+Math.sin(u*6+t+j)*.1,u*alt,Math.sin(a)*r);
    }

    // Pilar: coluna em volume que sobe 24 blocos em 4 ticks no grito, gira e afina ate a revelacao.
    public static double alturaPilar(double t){return t<GRITO?0:24*s((t-GRITO)/4.0);}
    public static double raioPilar(double t){return t<GRITO?0:(1.1+.5*(1-s((t-GRITO)/3.0)))*(1-s((t-REVELA+4)/6.0))+.05;}
    public static double alphaPilar(double t){return t<GRITO?0:1-s((t-REVELA+2)/6.0);}
    /** Ponto (i ao longo da altura, j em volta) da superficie turbulenta do pilar. */
    public static P pilar(int i,int j,double t){
        double u=i/(COLUNA-1.0),a=j*Math.PI*2/VOLTAS+u*3+t*.35,turb=1+.22*Math.sin(a*3+u*11-t*.9)+.12*Math.sin(a*5-u*17+t*1.3);
        double r=raioPilar(t)*turb*(1-.35*u)*(u<.05?u/.05*.6+.4:1);return new P(Math.cos(a)*r,u*alturaPilar(t),Math.sin(a)*r);
    }

    // Onda de choque no chao no grito (anel em cunha preto e vermelho).
    public static double raioChoque(double t){return t<GRITO?0:10*s((t-GRITO)/6.0);}
    public static double alphaChoque(double t){return t<GRITO?0:1-s((t-GRITO-4)/8.0);}
    public static P choque(int i,int j,double t){
        double phi=i*Math.PI*2/ANEL,v=j*Math.PI*2/SECAO,R=raioChoque(t),h=.9*(1-s((t-GRITO-2)/8.0));
        return new P(Math.cos(phi)*(R+.35*Math.cos(v)),Math.max(0,h*(.5+.5*Math.sin(v))),Math.sin(phi)*(R+.35*Math.cos(v)));
    }

    // Poeira: nuvens que nascem no grito, cobrem o corpo e sao empurradas para fora pelo corte da revelacao.
    public static P nuvem(int k,double t){
        double a=k*2.39996,r0=.6+2.0*hash(k),idade=Math.max(0,t-GRITO-hash(k+1)*3),empurra=t<REVELA?0:(t-REVELA)*(.55+.3*hash(k+2));
        double r=r0+idade*.06+empurra,y=.3+2.4*hash(k+3)*s(idade/6.0)+idade*.02;return new P(Math.cos(a)*r,y,Math.sin(a)*r);
    }
    public static double raioNuvem(int k,double t){double idade=t-GRITO-hash(k+1)*3;return idade<0?0:(.8+.5*hash(k+4))*s(idade/5.0)*(1+Math.max(0,t-REVELA)*.03);}
    public static double alphaNuvem(int k,double t){double idade=t-GRITO-hash(k+1)*3;return idade<0?0:s(idade/2.0)*(1-s((t-REVELA)/(FIM-REVELA-6.0)));}
    public static P bolha(int k,int i,int j,double t){
        double la=-Math.PI/2+Math.PI*i/(LAT-1),lo=Math.PI*2*j/LON,R=raioNuvem(k,t),ond=1+.2*Math.sin(lo*3+la*2+k+t*.2)+.1*Math.sin(lo*5-la*3+k*2);var c=nuvem(k,t);
        return new P(c.x()+Math.cos(la)*Math.cos(lo)*R*ond,Math.max(0,c.y()+Math.sin(la)*R*ond*.85),c.z()+Math.cos(la)*Math.sin(lo)*R*ond);
    }

    // Corte da revelacao: arco horizontal em volume varrendo 200 graus a frente na altura do peito em 3 ticks.
    public static double pontaCorte(double t){return Math.toRadians(-100+200*s((t-REVELA+1)/3.0));}
    public static double alphaCorte(double t){return t<REVELA-1?0:1-s((t-REVELA-2)/6.0);}
    public static P corte(int i,int j,double t){
        double ponta=pontaCorte(t),cauda=Math.max(Math.toRadians(-100),ponta-Math.toRadians(120)),u=i/(ARCO-1.0),a=cauda+(ponta-cauda)*u;
        double v=j*Math.PI*2/SECAO_CORTE,k=Math.sin(Math.PI*Math.min(1,u*1.05)),larg=1.2*k,esp=.45*k,r=3.4-larg/2+larg/2*Math.cos(v);
        return new P(Math.sin(a)*r,1.25+esp*Math.sin(v)*(.35+.65*(1-Math.cos(v))/2),Math.cos(a)*r);
    }

    // Aura depois da revelacao: chamas pretas de borda vermelha tremulando e baixando ao nivel de repouso.
    public static double aura(double t){return t<REVELA?0:1-.6*s((t-REVELA)/(FIM-REVELA));}
    public static P chama(int j,double u,double t){
        double a=j*Math.PI*2/LINGUAS+hash(j)*.7,r=.55+.1*Math.sin(t*.9+j),alt=(1.6+1.1*hash(j+5+Math.floor(t/2)))*aura(t);
        return new P(Math.cos(a)*r*(1-u*.4)+Math.sin(u*6+t+j)*.09,u*alt,Math.sin(a)*r*(1-u*.4));
    }

    /** Giro do pulso (graus) que alinha a lamina com o braco estendido no preparo: o item em pe na mao apontaria para
     *  cima. Gira ate 90 quando o braco chega a frente (tick 8) e volta antes do corte (ticks 26-28). */
    public static double pulso(double t){return 90*s((t-5)/3.0)*(1-s((t-26)/2.0));}
    /** Troca de roupa e espada: visivel so a partir da revelacao. */
    public static boolean trajeVisivel(double t){return t>=REVELA;}
}
