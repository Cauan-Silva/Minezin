package com.cauansilva.arcanecontrol.core.vfx;
public final class PerfilFrieren {
    public static final int ABERTURA=60,COLAPSO=170,FIM=200;
    public static double nucleo(double tick){return (.15+3.05*PerfilVfxGojo.suave(tick/ABERTURA))
            *(tick<COLAPSO?1:1-PerfilVfxGojo.suave((tick-COLAPSO)/8));}
    public static double opacidade(double tick){return tick<0||tick>=FIM?0:tick<COLAPSO?Math.min(1,tick/10):Math.max(0,(FIM-tick)/30);}
}
