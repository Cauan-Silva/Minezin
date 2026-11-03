package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Black Meteorite (efeito 304) conforme o anime: o Asta vira um feixe de antimagia preto de borda vermelha
 * que corta o ar (o corpo some), reaparece so para descer a espada sobre o alvo e o impacto abre um clarao em
 * estrela e uma cratera de tinta. Feixe 0..9 (o servidor move o dono), golpe vertical 9..12, impacto no tick do dano do servidor
 * (PerfilAsta.impacto(0,true) = 12) cobrindo o raio 4, tudo some ate 32.
 * Coordenadas no referencial do golpe: x lateral, y altura, z para frente. O feixe e o golpe ficam presos
 * ao dono, o rastro ao ponto de partida e a cratera ao ponto do impacto.
 */
public final class BlackMeteoriteAsta {
    public record P(double x,double y,double z){}
    public static final double COMPRIMENTO_FEIXE=3.4;
    public static final int IMPACTO=12,FIM=32,RAIO=4,REAPARECE=9,PONTOS_FEIXE=14,ESPIRAIS=3,ESTILHACOS=12,GOTAS_RASTRO=18,PONTOS_CRATERA=28,RACHADURAS=8,ESPINHOS=10,GOTAS=26;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    /** Arranque: respingo de tinta no ponto de partida quando o Asta dispara. */
    public static double arranque(double t){return t<0?0:s(t/2)*(1-s((t-3)/6));}
    public static P espinhoArranque(int k,double t){
        double a=k*Math.PI*2/ESPINHOS+hash(k)*.4,r=.3+1.5*s(t/2)*(.7+.3*hash(k+9));
        return new P(Math.cos(a)*r,.05+.5*hash(k+3)*s(t/2),Math.sin(a)*r-.4);
    }

    /** O corpo some no arranque e so volta para o corte: o Asta viaja como um feixe de antimagia. */
    public static boolean corpoVisivel(double t){return t<0||t>=REAPARECE;}
    public static double feixe(double t){return t<0?0:s(t/1.0)*(1-s((t-REAPARECE+.5)/1.0));}
    /** Largura do feixe no ponto s (0 = ponta da frente, 1 = fim do fuso atras do dono). */
    public static double larguraFeixe(double s){return .5*Math.pow(1-s,.7)*Math.min(1,s*5+.25);}
    public static P eixoFeixe(double s){return new P(0,1.0,.7-s*COMPRIMENTO_FEIXE);}
    /** Fio vermelho k enrolado no feixe (s = 0 na frente), fechado na ponta. */
    public static P espiral(int k,double s,double t){
        double a=k*Math.PI*2/ESPIRAIS+s*9-t*1.6,r=.1+.55*Math.sin(Math.PI*Math.min(1,s*1.6))*(1-s);
        return new P(Math.cos(a)*r,1.0+Math.sin(a)*r,.5-s*(COMPRIMENTO_FEIXE-.6));
    }
    /** Reaparicao: estilhacos de tinta convergem para o corpo antes do corte e estouram quando ele volta. */
    public static double reaparece(double t){return t<REAPARECE-1.5||t>REAPARECE+3?0:t<REAPARECE?s((t-REAPARECE+1.5)/1.5):1-s((t-REAPARECE)/3.0);}
    public static P estilhaco(int k,double t){
        double a=k*Math.PI*2/ESTILHACOS+hash(k+40)*.4,e=(hash(k+41)-.5)*2.2;
        double r=t<REAPARECE?.25+1.6*(1-s((t-REAPARECE+1.5)/1.5)):.25+2.2*s((t-REAPARECE)/3.0);
        return new P(Math.cos(a)*r,1.0+e*.5+Math.sin(a)*r*.35,Math.sin(a)*r*.6);
    }

    /** Rastro do ponto de partida (u=0) ate o dono (u=1): a cauda recolhe depois que o Asta chega. */
    public static double caudaRastro(double t){return s((t-5)/9.0);}
    public static double alphaRastro(double t){return s(t/1.5)*(1-s((t-IMPACTO-2)/8.0));}
    public static double larguraRastro(double u){return .06+.5*Math.pow(u,1.3);}
    /** Ondulacao lateral do rastro (o risco do anime nao e uma reta), zero nas duas pontas. */
    public static P desvioRastro(double u){double e=Math.sin(u*Math.PI);return new P(Math.sin(u*7.5)*.35*e,Math.sin(u*5+1)*.25*e,0);}
    /** Gota preta i que se solta do rastro de comprimento d (blocos) e cai; z medido a partir da partida. */
    public static P gotaRastro(int i,double t,double d){
        double solta=hash(i)*(IMPACTO-1),idade=Math.max(0,t-solta),z=d*Math.min(1,solta/Math.max(1,Math.min(t,IMPACTO-1)));
        return new P((hash(i+2)-.5)*.6,Math.max(.1,1-idade*idade*.02),z);
    }
    public static double alphaGotaRastro(int i,double t){double idade=t-hash(i)*(IMPACTO-1);return idade<0?0:s(idade/1.5)*(1-s((idade-3)/4));}

    /** Golpe vertical da espada: meia-lua de cima para baixo que termina no tick do dano. */
    public static double varredura(double t){return t<IMPACTO-3?0:s((t-IMPACTO+3)/3.0);}
    public static double alphaGolpe(double t){return t<IMPACTO-3?0:1-s((t-IMPACTO-1)/6.0);}
    public static P lamina(double u,double a){double r=.7+u*1.9;return new P(0,1.5+Math.cos(a)*r,Math.sin(a)*r+.3);}
    public static final double ARCO_GOLPE=2.35,JANELA_GOLPE=1.6;
    /** Raio interno relativo (u) no ponto f (0..1) da janela visivel: meia-lua grossa no meio, afilada nas pontas. */
    public static double internoGolpe(double f){return 1-.65*Math.sin(Math.PI*Math.max(0,Math.min(1,f)));}

    /** Clarao em estrela de quatro pontas no impacto (o quadro preto com a estrela branca do anime). */
    public static double estrela(double t){return t<IMPACTO?0:s((t-IMPACTO)/1.0)*(1-s((t-IMPACTO-1)/3.0));}
    /** Raio da cratera: abre o raio 4 em 3 ticks a partir do impacto. */
    public static double cratera(double t){return t<IMPACTO?0:RAIO*s((t-IMPACTO)/3.0);}
    public static double alphaCratera(double t){return t<IMPACTO?0:1-s((t-IMPACTO-10)/(FIM-IMPACTO-10.0));}
    /** Fator do raio no ponto i da borda: rasgada, com pontas a cada tres pontos, nunca alem de 1.1. */
    public static double borda(int i){return (.8+.18*hash(i+11))*(i%3==0?1.1:1);}
    public static P bordaCratera(int i,double raio){double a=i*Math.PI*2/PONTOS_CRATERA;double r=raio*borda(i);return new P(Math.cos(a)*r,.04,Math.sin(a)*r);}
    /** Rachadura vermelha k em zigue-zague do centro ate alem da borda (i = 0..5). */
    public static P rachadura(int k,int i,double raio){
        double a=k*Math.PI*2/RACHADURAS+hash(k+20)*.5+(i>0?(hash(k*7+i)-.5)*.35:0),r=raio*1.12*i/5.0;
        return new P(Math.cos(a)*r,.06,Math.sin(a)*r);
    }
    /** Coroa de espinhos de tinta que sobe da borda e afunda de novo. */
    public static double alturaEspinho(int k,double t){
        double idade=t-IMPACTO-hash(k+30);return idade<0?0:(1.4+1.2*hash(k+31))*s(idade/2)*(1-s((idade-4)/10));
    }
    public static P baseEspinho(int k){double a=k*Math.PI*2/ESPINHOS+hash(k+32)*.4,r=2.2+1.3*hash(k+33);return new P(Math.cos(a)*r,0,Math.sin(a)*r);}
    /** Gota i atirada do centro da cratera em arco; cai dentro do raio 4. */
    public static P gota(int i,double t){
        double idade=Math.max(0,t-IMPACTO-hash(i)*2),a=hash(i+1)*Math.PI*2,vh=.1+.07*hash(i+2),vy=.3+.25*hash(i+3);
        double h=Math.max(0,.3+idade*vy-idade*idade*.045),r=Math.min(RAIO-.2,.4+idade*vh);
        return new P(Math.cos(a)*r,h,Math.sin(a)*r);
    }
    public static double alphaGota(int i,double t){double idade=t-IMPACTO-hash(i)*2;return idade<0?0:s(idade/1.5)*(1-s((idade-6)/10));}
}
