package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Geometria do Zoltraak e da Barreira da Frieren, conforme o anime: circulo com flor de seis
 * petalas e feixe branco; barreira em favo de sete hexagonos ciano que estilhaca ao esgotar.
 * Coordenadas 2D estao no plano do circulo/barreira (x lateral, y vertical), em unidades do raio.
 */
public final class MagiasBaseFrieren {
    public record P2(double x,double y){}
    public static final int ZOLTRAAK=30,BARREIRA=80,ESTILHACO=20,PETALAS=6,CELULAS=7;
    /** O dano do servidor e aplicado no tick do cast: o feixe ja nasce inteiro. */
    public static final int FEIXE_PLENO=6,FEIXE_FIM=14;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    // --- Zoltraak ---
    /** Escala do circulo: abre em 3 ticks com leve excesso e permanece ate apagar. */
    public static double escalaCirculo(double t){return t<0?0:.6+.4*s(t/3)+.06*Math.sin(Math.min(1,t/3)*Math.PI);}
    public static double alphaCirculo(double t){return t<0?0:s(t/2)*(1-s((t-12)/(ZOLTRAAK-12.0)));}
    public static double giroCirculo(double t){return t*.02;}
    /** Petala k: lente entre o centro e a ponta em .8, amostrada de i=0..n pela borda esquerda e volta pela direita. */
    public static P2 petala(int k,int i,int n){
        double ang=k*Math.PI/3,u=(double)i/n;
        double lado=u<=.5?1:-1,v=u<=.5?u*2:(1-u)*2;
        double r=.8*v,largura=Math.sin(v*Math.PI)*.22*lado;
        return new P2(Math.cos(ang)*r-Math.sin(ang)*largura,Math.sin(ang)*r+Math.cos(ang)*largura);
    }
    /** Centro do pequeno circulo concentrico dentro de cada petala. */
    public static P2 olhoPetala(int k){double a=k*Math.PI/3;return new P2(Math.cos(a)*.52,Math.sin(a)*.52);}
    /** Triangulo entre petalas, perto do aro: tres vertices. */
    public static P2 triangulo(int k,int v){
        double a=k*Math.PI/3+Math.PI/6;
        double[] ang={a,a-.16,a+.16},raio={.62,.8,.8};
        return new P2(Math.cos(ang[v])*raio[v],Math.sin(ang[v])*raio[v]);
    }
    public static double larguraFeixe(double t){return t<0?0:t<FEIXE_PLENO?1:1-s((t-FEIXE_PLENO)/(double)(FEIXE_FIM-FEIXE_PLENO));}
    /** Faisca j ao longo do feixe: fracao do comprimento e afastamento lateral crescente. */
    public static double posicaoFaisca(int j){return hash(j);}
    public static double afastamentoFaisca(int j,double t){return .15+t*(.03+hash(j+5)*.05);}
    public static double anguloFaisca(int j){return hash(j+9)*Math.PI*2;}
    public static double alphaFaisca(int j,double t){return t<0?0:(1-s(t/(10+hash(j+3)*10)))*(.5+.5*hash(j+7));}
    public static double impacto(double t){return t<0?0:1-s(t/10);}

    // --- Barreira ---
    public static final double RAIO_CELULA=.42,PASSO=RAIO_CELULA*Math.sqrt(3);
    /** Celula 0 no centro e seis ao redor (favo de mel). */
    public static P2 celula(int c){
        if(c==0)return new P2(0,0);double a=(c-1)*Math.PI/3+Math.PI/6;return new P2(Math.cos(a)*PASSO,Math.sin(a)*PASSO);
    }
    /** Vertice v do hexagono de topo reto, como no anime: vizinhos encaixam acima, abaixo e nas diagonais. */
    public static P2 vertice(int c,int v,double escala){
        var o=celula(c);double a=v*Math.PI/3;
        return new P2(o.x()+Math.cos(a)*RAIO_CELULA*escala,o.y()+Math.sin(a)*RAIO_CELULA*escala);
    }
    /** As celulas acendem do centro para fora nos primeiros ticks. */
    public static double escalaCelula(int c,double t){return s((t-(c==0?0:2+c*.5))/4.0);}
    public static double alphaBarreira(double t){return Math.min(1,Math.min(t/4,(BARREIRA-t)/10));}
    public static double pulsoCelula(int c,double t){return .75+.25*Math.pow(Math.max(0,Math.sin(t*.13+c*1.7)),4);}

    /** Caco: triangulo (centro, v, v+1) de cada celula voando para fora do centro do favo. */
    public static P2 deslocamentoCaco(int c,int v,double t){
        var o=celula(c);var a=vertice(c,v,1);var b=vertice(c,(v+1)%6,1);
        double cx=(o.x()+a.x()+b.x())/3,cy=(o.y()+a.y()+b.y())/3,n=Math.max(1e-6,Math.hypot(cx,cy));
        double vel=.08+hash(c*6+v)*.08,d=t*vel;
        return new P2(cx/n*d,cy/n*d-t*t*.002);
    }
    /** Deslocamento para fora do plano (em direcao a camera/frente). */
    public static double frenteCaco(int c,int v,double t){return t*(.02+hash(c*6+v+40)*.05);}
    public static double giroCaco(int c,int v,double t){return t*(.15+hash(c*6+v+80)*.3)*(hash(c+v)>.5?1:-1);}
    public static double alphaCaco(double t){return t<0?0:1-s(t/ESTILHACO);}

    // --- Impulso Arcano: a magia de voo do anime quase nao tem brilho; so vento e algumas particulas ---
    public static final int IMPULSO=100,RAJADAS=14,RASTRO=12;
    public static double raioRajada(double t){return .5+2.8*s(t/8);}
    public static double alphaRajada(double t){return t<0?0:1-s(t/9);}
    public static double anguloRajada(int j){return j*Math.PI*2/RAJADAS+(hash(j+90)-.5)*.3;}
    public static double alphaSubida(double t){return t<0?0:s(t/2)*(1-s(t/12));}
    public static double alphaRastro(int i,double t){
        double nasce=i*3;if(t<nasce)return 0;return (1-s((t-nasce)/18))*(1-s(t/40));
    }

    // --- Gaiola de Vento: tornado de poeira em faixas, como o Waldgose do anime ---
    public static final int GAIOLA=120,FAIXAS=7,DETRITOS=20;public static final double ALTURA_TORNADO=9;
    public static double crescimento(double t){return s(t/10);}
    public static double alphaTornado(double t){return t<0?0:s(t/4)*(1-s((t-(GAIOLA-15))/15.0));}
    public static double raioTornado(double y){return 1.4+Math.pow(Math.max(0,y)/ALTURA_TORNADO,1.3)*3.4;}
    /** Ponto u (0..1) da faixa k: helice subindo pouco, girando rapido, abrindo no fim. */
    public static P2 faixaAngAltura(int k,double u,double t){
        // Cada faixa sobe tres alturas de faixa numa volta e meia: diagonais longas e sobrepostas.
        double y=(k*.7+u*2.1)*ALTURA_TORNADO/FAIXAS*crescimento(t);
        double a=k*1.7+u*Math.PI*2.4+t*(.32-k*.015);return new P2(a,y);
    }
    public static double espalhamento(double t){return 1+.35*s((t-(GAIOLA-15))/15.0);}
    public static double alturaDetrito(int i,double t){return ((hash(i)*ALTURA_TORNADO+t*(.05+hash(i+1)*.06))%ALTURA_TORNADO)*crescimento(t);}
    public static double anguloDetrito(int i,double t){return hash(i+2)*Math.PI*2+t*(.35+hash(i+3)*.15);}
}
