package com.cauansilva.arcanecontrol.core.ichigo;
/** Tick/geometry contract shared by server collision, poses and client presentation. */
public final class PerfilIchigo {
    public static final int ABERTURA=24,MASCARA=200;
    /** Balance 3.11 (user: Bankai drained too fast). Regeneration slows during the Bankai so the drain can drop
     *  while the form stays upkept by mana even with the full set (-20% cost) and the regen node (+25%):
     *  ~45 s from the 50 left after activation without them, ~4 min with everything. */
    public static final double MANA_BANKAI_POR_SEGUNDO=2.6,REGENERACAO_NO_BANKAI=.3;
    public static int recarga(int slot,boolean bankai){
        if(slot<0||slot>3)throw new IllegalArgumentException("Slot inválido");
        return (bankai?new int[]{30,40,240,800}:new int[]{80,140,280,400})[slot];
    }
    public static int impacto(int slot,boolean bankai){return bankai?new int[]{5,0,10,12}[slot]:new int[]{8,0,12,10}[slot];}
    public static double alcance(int idade){return Math.max(0,Math.min(28,idade*1.4));}
    public static double raio(boolean cruzado){return cruzado?2.1:1.25;}
    private PerfilIchigo(){}
}
