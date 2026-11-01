package com.cauansilva.arcanecontrol.core.ichigo;
/** Tick/geometry contract shared by server collision, poses and client presentation. */
public final class PerfilIchigo {
    public static final int ABERTURA=24,MASCARA=200,MANA_BANKAI_POR_SEGUNDO=8;
    public static int recarga(int slot,boolean bankai){
        if(slot<0||slot>3)throw new IllegalArgumentException("Slot inválido");
        return (bankai?new int[]{20,40,240,800}:new int[]{100,160,300,400})[slot];
    }
    public static int impacto(int slot,boolean bankai){return bankai?new int[]{0,0,10,12}[slot]:new int[]{8,0,12,10}[slot];}
    public static double alcance(int idade){return Math.max(0,Math.min(28,idade*1.4));}
    public static double raio(boolean cruzado){return cruzado?2.1:1.25;}
    private PerfilIchigo(){}
}
