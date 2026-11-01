package com.cauansilva.arcanecontrol.core.vfx;

/** Visual blade extension only. Does not change reach, damage or collision. */
public final class PerfilDivider {
    private PerfilDivider(){}
    public static double intensidade(double tick){
        if(tick<0||tick>=32)return 0;
        return PerfilVfxGojo.suave(tick/16)*PerfilVfxGojo.suave((32-tick)/10);
    }
    public static float comprimento(double tick,boolean primeiraPessoa){return (float)(1+intensidade(tick)*(primeiraPessoa?1.2:2.6));}
    public static float largura(double tick){return (float)(1+intensidade(tick)*.55);}
}
