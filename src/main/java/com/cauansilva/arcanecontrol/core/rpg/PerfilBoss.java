package com.cauansilva.arcanecontrol.core.rpg;

/** Pure timing and distance rules, shared by rendering and regression tests. */
public final class PerfilBoss {
    public static final float ALTURA_BASE=2f,QUAL_ESCALA_MINIMA=2.5f;
    public static float escala(int kit,double valor){return (float)Math.max(kit==3?QUAL_ESCALA_MINIMA:.5,Math.min(3,Double.isFinite(valor)?valor:kit==3?2.5:1));}
    private PerfilBoss(){}
    public static boolean efeito(int tipo){return tipo==406||tipo==416||tipo>=401&&tipo<=404||tipo>=411&&tipo<=414;}
    public static double trauma(double ticks,double distancia){
        if(ticks<0||ticks>=8||distancia<0||distancia>=24||!Double.isFinite(ticks)||!Double.isFinite(distancia))return 0;
        return .45*(1-ticks/8)*(1-distancia/24);
    }
}
