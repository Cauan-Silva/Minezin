package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Chamado Espiritual de Emergencia (X do Subaru): pequenos espiritos de luz surgem em volta, espiralam e se fundem numa
 * bolha de mana que envolve o corpo, com um circulo de runas girando nos pes. O escudo dura ate DURACAO ou ate absorver
 * um golpe (ate 2 coracoes): o golpe faz uma onda na superficie a partir do ponto atingido e a bolha racha e estoura em
 * fragmentos. Tempo t a partir do cast; quebra = tick do golpe (Double.POSITIVE_INFINITY se nao houve).
 * Referencial: origem nos pes de quem lanca, x lateral, y para cima, z para frente. Classe pura para a previa e os testes.
 */
public final class EscudoSubaru {
    public record P(double x,double y,double z){}
    public static final int INVOCA=10,DURACAO=100,ESTOURO=12;
    public static final double RAIO=1.35,ABSORVE=4;
    public static final int LAT=16,LON=28,ESPIRITOS=12,PONTOS_ESPIRITO=8,PONTOS_RUNA=48,RUNAS=3,FRAGMENTOS=36;
    public static final P CENTRO=new P(0,1,0);
    /** Onde o golpe da previa acerta: de frente, na altura do peito. */
    public static final P GOLPE=new P(0,1.25,1);

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}
    /** Tick em que o escudo acaba: estoura no golpe ou se desfaz no fim da duracao. */
    public static double fim(double quebra){return Math.min(quebra,DURACAO);}

    // ---------- invocacao: espiritos ----------
    /** Ponto u (0 = cauda, 1 = cabeca) do espirito k: nasce longe, espirala e pousa na superficie da bolha. */
    public static P espirito(int k,double u,double t){
        double tau=lim((t-u*1.6)/INVOCA-.15*hash(k)),a0=2*Math.PI*k/ESPIRITOS+hash(k*3)*.7,el=(hash(k*5)-.5)*1.3+.2*Math.sin(t*.4+k)*(1-tau);
        double r=RAIO+(1.4+.6*hash(k*7))*(1-s(tau)),a=a0+3.2*s(tau)*(k%2==0?1:-1);
        return new P(CENTRO.x()+Math.cos(a)*r*Math.cos(el),Math.max(.05,CENTRO.y()+Math.sin(el)*r),CENTRO.z()+Math.sin(a)*r*Math.cos(el));
    }
    public static double alphaEspiritos(double t){return t<0?0:Math.min(1,(t+1)/2)*(1-s((t-INVOCA+2)/3));}

    // ---------- bolha ----------
    public static double escala(double t,double quebra){
        if(t<INVOCA-4)return 0;double e=s((t-INVOCA+4)/6);
        if(t>=DURACAO&&quebra>DURACAO)e*=1-s((t-DURACAO)/8);
        return t>=quebra?0:e;
    }
    public static P normal(int i,int j){double th=Math.PI*i/LAT,ph=2*Math.PI*j/LON;return new P(Math.sin(th)*Math.cos(ph),Math.cos(th),Math.sin(th)*Math.sin(ph));}
    /** Superficie: respira devagar; depois do golpe, uma onda anelar corre do ponto atingido amassando a casca. */
    public static P bolha(int i,int j,double t,double quebra){
        var n=normal(i,j);double gl=Math.sqrt(GOLPE.x()*GOLPE.x()+(GOLPE.y()-CENTRO.y())*(GOLPE.y()-CENTRO.y())+GOLPE.z()*GOLPE.z());
        double d=Math.acos(Math.max(-1,Math.min(1,(n.x()*GOLPE.x()+n.y()*(GOLPE.y()-CENTRO.y())+n.z()*GOLPE.z())/gl)));
        double amassa=-.14*Math.exp(-Math.pow(d-anguloOnda(t,quebra),2)/.04)*alphaOnda(t,quebra);
        double r=RAIO*escala(t,quebra)*(1+.015*Math.sin(t*.25)+.01*Math.sin(3*Math.PI*i/LAT+t*.5)+amassa);
        return new P(CENTRO.x()+n.x()*r,CENTRO.y()+n.y()*r,CENTRO.z()+n.z()*r);
    }
    public static double alphaBolha(double t,double quebra){return escala(t,quebra)*(t>=quebra-1?1:.85+.15*Math.sin(t*.3));}
    /** Brilho extra da casca durante o golpe (o escudo acende ao absorver). */
    public static double clarao(double t,double quebra){double a=t-quebra+4;return a<0||a>4?0:Math.sin(Math.PI*a/4);}

    /** Golpe da previa: chega no escudo 4 ticks antes de estourar (tempo para a onda de absorcao). */
    public static double toque(double quebra){return quebra-4;}
    /** Onda de absorcao na casca: angulo (rad) do anel em volta do ponto atingido. */
    public static double anguloOnda(double t,double quebra){return Math.PI*.9*s((t-toque(quebra))/4);}
    public static double alphaOnda(double t,double quebra){double a=t-toque(quebra);return a<0||a>4?0:1-.6*a/4;}

    // ---------- runas nos pes ----------
    public static P runa(int a,int i,double t){
        double ang=2*Math.PI*i/PONTOS_RUNA+t*(.05+.03*a)*(a%2==0?1:-1),r=RAIO*(.55+.22*a);
        return new P(Math.cos(ang)*r,.03+.01*a,Math.sin(ang)*r);
    }
    public static double alphaRunas(double t,double quebra){
        double f=fim(quebra);if(t<2||t>=f+6)return 0;return Math.min(1,(t-2)/6)*(t>f?1-(t-f)/6:1);
    }

    // ---------- estouro ----------
    /** Fragmento k da casca depois do estouro (pontos de Fibonacci na esfera voando para fora e caindo). */
    public static P fragmento(int k,double t,double quebra){
        double y=1-2*(k+.5)/FRAGMENTOS,rr=Math.sqrt(1-y*y),ph=k*2.399963,a=Math.max(0,t-quebra);
        double v=.09+.06*hash(k*11),r=RAIO+v*a,cai=.006*a*a;
        return new P(CENTRO.x()+rr*Math.cos(ph)*r,CENTRO.y()+y*r-cai,CENTRO.z()+rr*Math.sin(ph)*r);
    }
    public static double tamanhoFragmento(int k,double t,double quebra){
        double a=t-quebra;return a<0||a>=ESTOURO?0:(.1+.07*hash(k*13))*(1-a/ESTOURO);
    }

    private EscudoSubaru(){}
}
