package com.cauansilva.arcanecontrol.core.vfx;
/** Concentric, visual-only convergence; no change to the spell radius or its damage. */
public final class PerfilSelosCid {
    public static double raio(double t,int anel){return (6.5+anel*1.25)*(1-fase(t))+(.2+anel*.12)*fase(t);}
    public static double fase(double t){return PerfilVfxGojo.suave((t-12)/76);}
    public static double elevar(double t){return PerfilVfxGojo.suave((t-58)/30);}
    private PerfilSelosCid(){}
}
