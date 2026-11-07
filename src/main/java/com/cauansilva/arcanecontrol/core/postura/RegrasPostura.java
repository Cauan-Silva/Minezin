package com.cauansilva.arcanecontrol.core.postura;

/**
 * Postura (sem Minecraft): cada golpe enche a barra do alvo; cheia = Quebrada (atordoado e brilhando) e o proximo
 * golpe corpo a corpo vira Execucao. Sem golpes por um tempo, a postura se recupera. Tempo em ticks.
 */
public final class RegrasPostura {
    public static final int ATRASO_REGEN=60, QUEBRA_MOB=60, QUEBRA_CHEFE=80, IMUNE=100;
    public static final double REGEN_POR_SEG=.12, EXEC_MOB=4, EXEC_CHEFE=2.5, LIMIAR_FINAL=.4;

    public static double maximo(double vidaMax,boolean chefe){return chefe?Math.max(60,vidaMax*.5):Math.max(12,vidaMax*.8);}
    /** Postura causada por um golpe: corpo a corpo carregado conta inteiro, golpe fraco 40%, magia/projetil 50%; critico x1.5. */
    public static double golpe(double dano,boolean corpo,double carga,boolean critico){
        double m=corpo?(carga>=.9?1:.4):.5;return Math.max(0,dano)*m*(critico?1.5:1);
    }
    /** Valor atual depois de {@code parado} ticks sem golpes (regenera so depois do atraso). */
    public static double recuperar(double v,long parado,double max){
        if(parado<=ATRASO_REGEN)return v;return Math.max(0,v-max*REGEN_POR_SEG*(parado-ATRASO_REGEN)/20.0);
    }
    public static boolean quebra(double v,double max){return v>=max;}
    public static int duracao(boolean chefe){return chefe?QUEBRA_CHEFE:QUEBRA_MOB;}
    /** Dano da Execucao. Mob comum ja ferido (<=40%) morre na hora; chefe recebe multiplicador menor. */
    public static double execucao(double dano,double vida,double vidaMax,boolean chefe){
        if(!chefe&&vida<=vidaMax*LIMIAR_FINAL)return Math.max(dano*EXEC_MOB,vida+1);
        return dano*(chefe?EXEC_CHEFE:EXEC_MOB);
    }
    /** Barra de texto para a action bar. */
    public static String barra(double v,double max,int n){
        int c=(int)Math.round(Math.min(1,Math.max(0,v/max))*n);return "▮".repeat(c)+"▯".repeat(n-c);
    }
    private RegrasPostura(){}
}
