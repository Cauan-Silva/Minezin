package com.cauansilva.arcanecontrol.core.vfx;

/** World-vertical information streaks descending into the confirmed target's head. */
public final class MorteCerebralGojo {
    public static final int EFEITO=42,DURACAO=6,FAIXAS=12,FAIXAS_REDUZIDAS=6;
    public record Ponto(double x,double y,double z){}
    public static double alpha(double tick){return tick<0?0:1-PerfilVfxGojo.suave(tick/DURACAO);}
    public static Ponto linha(int indice,double s,double tick){
        double a=indice*2.399963,queda=PerfilVfxGojo.suave(tick/3.5);
        double r=(.16+.26*frac(indice*.61803398875))*(1-.65*PerfilVfxGojo.suave(tick/6));
        double cabeca=.18+1.7*(1-queda)+.25*(indice%3)/2*(1-PerfilVfxGojo.suave(tick/6));
        double curva=Math.sin(s*Math.PI)*.025*Math.sin(indice*1.7);
        return new Ponto(Math.cos(a)*r*(1+s*.2)+curva,cabeca+s*(.8+(indice%3)*.15),Math.sin(a)*r*(1+s*.2));
    }
    private static double frac(double x){return x-Math.floor(x);}
}
