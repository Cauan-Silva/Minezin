package com.cauansilva.arcanecontrol.core.rpg;

public final class RecursoMana {
    public static final double MAXIMO=100;
    private double atual;
    public RecursoMana(double atual){this.atual=Double.isFinite(atual)?Math.max(0,Math.min(MAXIMO,atual)):0;}
    public double atual(){return atual;}
    public static double custo(double base,boolean conjuntoCompleto){
        if(!Double.isFinite(base)||base<0)throw new IllegalArgumentException("Custo de mana invalido");
        return base*(conjuntoCompleto?.8:1);
    }
    public boolean consumir(double base,boolean conjuntoCompleto){double custo=custo(base,conjuntoCompleto);if(atual+1e-9<custo)return false;atual=Math.max(0,atual-custo);return true;}
    public void regenerar(long ticks,double porSegundo){
        if(ticks<=0||!Double.isFinite(porSegundo)||porSegundo<=0)return;
        atual=Math.min(MAXIMO,atual+Math.min(1200,ticks)/20.0*porSegundo);
    }
}
