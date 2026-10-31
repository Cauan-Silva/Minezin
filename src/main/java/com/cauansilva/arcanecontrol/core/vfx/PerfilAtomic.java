package com.cauansilva.arcanecontrol.core.vfx;
public final class PerfilAtomic {
    public static final int FEIXO_INICIO=50,IMPACTO=90,FEIXO_FIM=170,FIM=240;
    public static boolean cancelarPorMovimento(double t,double distanciaQuadrada){return t<IMPACTO&&distanciaQuadrada>9;}
    public static double baseFeixe(double t){return 48*(1-PerfilVfxGojo.suave((t-FEIXO_INICIO)/(IMPACTO-FEIXO_INICIO)));}
    public static double brilhoFeixe(double t){return t<FEIXO_INICIO||t>=FEIXO_FIM?0:PerfilVfxGojo.suave((t-FEIXO_INICIO)/12)*PerfilVfxGojo.suave((FEIXO_FIM-t)/35);}
}
