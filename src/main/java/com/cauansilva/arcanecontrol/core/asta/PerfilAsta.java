package com.cauansilva.arcanecontrol.core.asta;

/** Tempos em ticks compartilhados por colisao, avisos e animacoes. */
public final class PerfilAsta {
    public static final int DURACAO_FORMA=1200, ABERTURA=55, SILENCIO=80, BARREIRA=100;
    public static int recarga(int slot,boolean forma){
        if(slot<0||slot>3)throw new IllegalArgumentException("Slot invalido");
        return (forma?new int[]{80,200,280,480}:new int[]{80,160,240,440})[slot];
    }
    /** Damage tick per slot; for the ultimate (V in the form) it is the final X. */
    public static int impacto(int slot,boolean forma){return forma?new int[]{12,10,18,com.cauansilva.arcanecontrol.core.vfx.UltAsta.FINAL}[slot]:new int[]{6,8,10,0}[slot];}
    public static boolean cone(double cosseno){return Double.isFinite(cosseno)&&cosseno>=.5;}
    public static boolean podeRefletir(int vezes,long agora,long bloqueadoAte){return vezes>=0&&vezes<2&&agora>=bloqueadoAte;}
}
