package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Black Divider (efeito 306) conforme as referencias do anime: antimagia como tinta preta chapada
 * com contorno vermelho fino. Carga na mao, condensacao nas bordas da lamina, corte no tick do
 * dano do servidor (PerfilAsta.impacto(2,true) = 18) com fenda de 18 blocos, rastro ate 32.
 * Coordenadas no referencial do golpe: x lateral, y altura, z para frente.
 */
public final class BlackDividerAsta {
    public record P(double x,double y,double z){}
    public static final int CORTE=18,FIM=32,ALCANCE=18,PONTOS_FENDA=28,FIOS=5,GOTAS=26,FAISCAS=7;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    /** 0..1 enquanto a tinta se acumula na lamina (antes do corte). */
    public static double carga(double t){return t>=CORTE?0:s(t/14.0);}
    /** Aura irregular nas bordas da lamina nos ultimos ticks antes do corte. */
    public static double condensa(double t){return t>=CORTE?0:s((t-12)/6.0);}

    /** Fio de tinta j enrolando da mao (s=0) ate a ponta da lamina (s=1). */
    public static P fio(int j,double s,double t){
        double a=j*Math.PI*2/FIOS+s*7-t*.35,r=(1-s)*.9+.12;
        return new P(Math.cos(a)*r,Math.sin(a)*r,s*3.2);
    }
    public static double alphaFio(int j,double s,double t){
        double frente=((t*.09+j*.2)%1.0);return carga(t)*Math.exp(-Math.pow((s-frente)*3.5,2));
    }
    /** Faisca vermelha em zigue-zague perto da mao; muda a cada 2 ticks. */
    public static P faisca(int k,int i,double t){
        double v=Math.floor(t/2),a=hash(k+v*7)*Math.PI*2,r=.35+i*.18;
        return new P(Math.cos(a+i*.9)*r,Math.sin(a+i*.9)*r+(hash(k*3+i+v)-.5)*.3,.2+i*.12);
    }

    /** Comprimento aberto da fenda: rasga os 18 blocos em 4 ticks a partir do corte. */
    public static double fenda(double t){return t<CORTE?0:ALCANCE*s((t-CORTE)/4.0);}
    public static double alphaFenda(double t){return t<CORTE?0:1-s((t-CORTE-6)/(FIM-CORTE-6.0));}
    /** Ponto i da fenda no chao com deslocamento lateral irregular e largura variavel. */
    public static P borda(int i){
        double u=i/(double)(PONTOS_FENDA-1);
        return new P((hash(i)-.5)*.55*Math.sin(u*Math.PI),0,u*ALCANCE);
    }
    public static double larguraFenda(int i){
        double u=i/(double)(PONTOS_FENDA-1);return (.35+.75*hash(i+40))*Math.pow(Math.sin(Math.min(1,u*1.15)*Math.PI),.6)+.08;
    }

    /** Meia-lua preta varrendo para frente no corte: angulo de varredura 0..1 e alpha. */
    public static double varredura(double t){return t<CORTE-1?0:s((t-CORTE+1)/3.0);}
    public static double alphaMeiaLua(double t){return t<CORTE-1?0:1-s((t-CORTE-2)/8.0);}
    /** Ponto u (0 = base na mao, 1 = ponta) da lamina estendida em angulo a (vertical para frente). */
    public static P lamina(double u,double a){
        double r=1.2+u*7.5;return new P(0,1.3+Math.cos(a)*r,Math.sin(a)*r+.6);
    }

    /** Gota de tinta i: sobe da fenda e cai, presa a um ponto ao longo dela. */
    public static P gota(int i,double t){
        double idade=Math.max(0,t-CORTE-hash(i)*3),z=hash(i+1)*ALCANCE,vy=.12+hash(i+2)*.14;
        return new P((hash(i+3)-.5)*1.4+(hash(i+5)-.5)*idade*.03,Math.max(0,idade*vy-idade*idade*.006),z);
    }
    public static double alphaGota(int i,double t){
        double idade=t-CORTE-hash(i)*3;if(idade<0)return 0;// The latest drop starts 3 ticks after the cut, so every drop is gone by FIM.
        return s(idade/2)*(1-s((idade-3)/8.0));
    }
}
