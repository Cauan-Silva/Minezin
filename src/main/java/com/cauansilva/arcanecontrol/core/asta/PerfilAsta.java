package com.cauansilva.arcanecontrol.core.asta;

/** Tempos em ticks compartilhados por colisao, avisos e animacoes. */
public final class PerfilAsta {
    public static final int DURACAO_FORMA=1200, ABERTURA=55, SILENCIO=80, BARREIRA=100;
    public static int recarga(int slot,boolean forma){
        if(slot<0||slot>3)throw new IllegalArgumentException("Slot invalido");
        return (forma?new int[]{60,200,300,400}:new int[]{80,160,240,500})[slot];
    }
    public static int impacto(int slot,boolean forma){return forma?new int[]{12,10,18,8}[slot]:new int[]{6,8,10,0}[slot];}
    public static boolean cone(double cosseno){return Double.isFinite(cosseno)&&cosseno>=.5;}
    public static boolean podeRefletir(int vezes,long agora,long bloqueadoAte){return vezes>=0&&vezes<2&&agora>=bloqueadoAte;}
    public static double alcanceOnda(int tick){return Math.max(0,Math.min(24,(tick-8)*1.2));}
}
