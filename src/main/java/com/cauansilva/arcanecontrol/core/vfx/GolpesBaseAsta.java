package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.asta.PerfilAsta;

/**
 * Golpes base do Asta (efeitos 300-303) na tinta antimagia aprovada no Black Divider: preto chapado com
 * contorno vermelho. Coordenadas do golpe: x lateral, y altura, z para frente. Cada corte so se completa
 * no tick de dano do servidor (PerfilAsta.impacto).
 */
public final class GolpesBaseAsta {
    public record P(double x,double y,double z){}
    public static final int DURACAO=32,GUARDA=PerfilAsta.BARREIRA,ESPINHOS=10;
    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    public static int impacto(int slot){return PerfilAsta.impacto(slot,false);}
    /** Varredura do corte: comeca 2 ticks antes e fecha 1 tick depois do dano. */
    public static double varredura(int slot,double t){double i=impacto(slot);return s((t-i+2)/3.0);}
    public static double alpha(int slot,double t){double i=impacto(slot);return t<i-2?0:1-s((t-i-3)/10.0);}

    /** Z: leque horizontal do lado plano, 120 graus (o cone do servidor), raio ate 5. u: -1..1 no leque, v: 0 interno..1 externo. */
    public static P leque(double u,double v){
        double a=u*Math.PI/3,r=3.0+v*1.9;return new P(Math.sin(a)*r,1.1+(1-Math.abs(u))*.15,Math.cos(a)*r);
    }
    /** X: meia-lua vertical no plano da mira, de cima para o chao a frente. u: 0 alto..1 baixo. */
    public static P corteVertical(double u,double v){
        // Ends at the ground in front instead of cutting below it.
        double a=u*2.3,r=2.3+v*1.9;return new P(0,1.25+Math.cos(a)*r,Math.sin(a)*r+.4);
    }
    /** Rasgo no chao deixado pelo X (2 a 6 blocos a frente). */
    public static double rasgo(double t){return t<impacto(1)?0:s((t-impacto(1))/3.0);}
    public static P bordaRasgo(int i){double u=i/11.0;return new P((hash(i)-.5)*.35,.04,2+u*4);}
    public static double larguraRasgo(int i){double u=i/11.0;return (.12+.25*hash(i+9))*Math.sin(Math.min(1,u*1.1)*Math.PI)+.04;}

    /** C: comprimento do rastro de tinta atras do Asta durante a investida (ticks 0..10). */
    public static double rastro(double t){return t>impacto(2)+2?0:Math.min(4,(t+1)*.55)*(1-s((t-impacto(2))/2.0));}
    /** C: espinho k do impacto em raio 3 (o raio de dano do servidor). */
    public static P espinho(int k,double t){
        double g=t<impacto(2)?0:s((t-impacto(2))/2.5),a=k*Math.PI*2/ESPINHOS+(hash(k)-.5)*.5,r=.6+2.3*g;
        return new P(Math.sin(a)*r,.3+(.9+hash(k+3)*1.1)*g,Math.cos(a)*r);
    }
    public static double alphaEspinho(double t){return t<impacto(2)?0:1-s((t-impacto(2)-4)/10.0);}

    /** V: guarda de tinta a frente; a = angulo no arco (-1..1), pulsa enquanto dura. */
    public static P guarda(double a,double v,double t){
        double ang=a*1.9,r=1.35+v*.35+.05*Math.sin(t*.4);return new P(Math.sin(ang)*r,1.1+Math.cos(ang)*r*.9,.95);
    }
    public static double alphaGuarda(double t){return s(t/4)*(1-s((t-(GUARDA-10))/10.0));}

    // --- Relampagos negros (pedido do usuario): cada golpe vira feixes em zigue-zague seguindo o mesmo gesto ---
    public static final int RAIOS=5,PONTOS=14;
    /** Ponto i (0..PONTOS) do raio j sobre o caminho base, com desvio em zigue-zague que troca a cada 2 ticks. */
    public static P relampago(java.util.function.BiFunction<Double,Double,P> caminho,int j,int i,double t,double amplitude){
        double u=i/(double)PONTOS,v=(j+.5)/RAIOS;var b=caminho.apply(u,v);
        if(i==0||i==PONTOS)return b;
        double q=Math.floor(t/2)*13+j*31+i*7;
        return new P(b.x()+(hash(q)-.5)*amplitude,b.y()+(hash(q+1)-.5)*amplitude,b.z()+(hash(q+2)-.5)*amplitude);
    }
    /** Ramo curto saindo do ponto i do raio j (so em pontos alternados). */
    public static P ramo(P origem,int j,int i,double t,double tamanho){
        double q=Math.floor(t/2)*17+j*29+i*11;
        return new P(origem.x()+(hash(q)-.5)*tamanho,origem.y()+(hash(q+1)-.3)*tamanho*.8,origem.z()+(hash(q+2)-.5)*tamanho);
    }
}
