package com.cauansilva.arcanecontrol.core.vfx;
public final class PerfilFrieren {
    public static final int ABERTURA=70,COLAPSO=220,FIM=280;
    public static double nucleo(double tick){return (.15+4.65*PerfilVfxGojo.suave(tick/ABERTURA))
            *(tick<COLAPSO?1:1-PerfilVfxGojo.suave((tick-COLAPSO)/14));}
    public static double opacidade(double tick){return tick<0||tick>=FIM?0:tick<COLAPSO?Math.min(1,tick/10):Math.max(0,(FIM-tick)/60);}
}
