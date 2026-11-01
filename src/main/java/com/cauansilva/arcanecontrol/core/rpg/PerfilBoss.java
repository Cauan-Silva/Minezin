package com.cauansilva.arcanecontrol.core.rpg;

/** Pure timing and distance rules, shared by rendering and regression tests. */
public final class PerfilBoss {
    private PerfilBoss(){}
    public static boolean efeito(int tipo){return tipo>=401&&tipo<=404||tipo>=411&&tipo<=414;}
    public static double trauma(double ticks,double distancia){
        if(ticks<0||ticks>=8||distancia<0||distancia>=24||!Double.isFinite(ticks)||!Double.isFinite(distancia))return 0;
        return .45*(1-ticks/8)*(1-distancia/24);
    }
}
