package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Instrucao Tatica (C do Subaru): ele aponta e da a ordem; um pulso de comando corre pelo chao (anel com grade de raios
 * ate RAIO blocos) e, quando passa por um aliado (ou por ele mesmo, se estiver sozinho), acende o buff de 5 s:
 * circulo nos pes, espirais de vento subindo pelo corpo (velocidade) e tres divisas subindo acima da cabeca (recarga
 * do pulo). Tempo t a partir do cast. Funcoes do pulso: origem nos pes do Subaru. Funcoes do buff: origem nos pes de
 * quem recebe, com "chega" = tick em que o pulso o alcanca (chegada(distancia)). x lateral, y para cima, z para frente.
 */
public final class InstrucaoSubaru {
    public record P(double x,double y,double z){}
    public static final int ORDEM=6,PULSO=10,DURACAO=100;
    public static final double RAIO=6;
    public static final int PONTOS_ANEL=64,RAIOS=16,ESPIRAIS=3,PONTOS_ESPIRAL=18,DIVISAS=3,PONTOS_PE=32;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}

    // ---------- pulso de comando ----------
    public static double raioPulso(double t){return RAIO*s((t-ORDEM)/PULSO);}
    public static double alphaPulso(double t){double a=t-ORDEM;return a<0||a>PULSO+6?0:Math.min(1,a/2)*(1-s((a-PULSO+2)/8));}
    public static P anelPulso(int i,double t){double a=2*Math.PI*i/PONTOS_ANEL,r=raioPulso(t);return new P(Math.cos(a)*r,.04,Math.sin(a)*r);}
    /** Raio k da grade: do centro ate o anel, com o trecho aceso perto da frente da onda. */
    public static P raio(int k,double u,double t){double a=2*Math.PI*k/RAIOS,r=raioPulso(t)*(.25+.75*u);return new P(Math.cos(a)*r,.035,Math.sin(a)*r);}
    /** Tick em que o pulso alcanca quem esta a d blocos. */
    public static double chegada(double d){
        double u=lim(d/RAIO),lo=0,hi=1;for(int n=0;n<30;n++){double m=(lo+hi)/2;if(s(m)<u)lo=m;else hi=m;}
        return ORDEM+PULSO*(lo+hi)/2;
    }

    // ---------- buff em quem recebe ----------
    private static double idade(double t,double chega){return t-chega;}
    public static double alphaBuff(double t,double chega){double a=idade(t,chega);return a<0?0:Math.min(1,a/3)*(1-s((a-(DURACAO-12))/12));}
    /** Clarao de quando o buff acende (o corpo pisca). */
    public static double acende(double t,double chega){double a=idade(t,chega);return a<0||a>5?0:Math.sin(Math.PI*a/5);}
    public static P pe(int i,double t,double chega){
        double a=2*Math.PI*i/PONTOS_PE+t*.12,r=.7+.25*(1-s(idade(t,chega)/4));return new P(Math.cos(a)*r,.05,Math.sin(a)*r);
    }
    /** Espiral k de vento (u = 0 nos pes, 1 = cabeca): corre para cima sem parar enquanto o buff dura. */
    public static P espiral(int k,double u,double t){
        double a=2*Math.PI*k/ESPIRAIS+u*5.5-t*.55,r=.5+.08*Math.sin(u*Math.PI*3+t*.3);return new P(Math.cos(a)*r,u*1.9,Math.sin(a)*r);
    }
    /** Trecho aceso da espiral: um pulso que sobe em loop (posicao 0..1 ao longo dela). */
    public static double frenteEspiral(int k,double t){double v=(t*.06+k/(double)ESPIRAIS)%1;return v<0?v+1:v;}
    /** Altura da divisa n acima da cabeca (sobem em loop e somem no topo). */
    public static double alturaDivisa(int n,double t){double c=((t*.05+n/(double)DIVISAS)%1+1)%1;return 2.15+.6*c;}
    public static double alphaDivisa(int n,double t){double c=((t*.05+n/(double)DIVISAS)%1+1)%1;return Math.sin(Math.PI*c);}

    private InstrucaoSubaru(){}
}
