package com.cauansilva.arcanecontrol.core.vfx;
public final class PerfilCid {
    public static final int CORTE_IMPACTO=6,FIOS_IMPACTO=8,FIOS_DURACAO=48;
    public static final double CORTE_ANGULO=1.32;
    public static double opacidadeCorte(double t){return t<4||t>=20?0:Math.min(1,(t-4)/2)*Math.max(0,1-(t-6)/14);}
    public static double raioCorte(double t){return 2+Math.max(0,Math.min(1,(t-3)/3))*3;}
}
