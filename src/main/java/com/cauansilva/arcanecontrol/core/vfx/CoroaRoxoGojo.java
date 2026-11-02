package com.cauansilva.arcanecontrol.core.vfx;
/** Broken, tilted energy sheets hugging Purple; never part of damage or collision geometry. */
public final class CoroaRoxoGojo {
    public record Ponto(double lateral,double vertical,double frente){}
    public static Ponto ponto(int faixa,double s,double tick,double raio){
        double a=s*Math.PI*2+tick*.045*(faixa%2==0?1:-1)+faixa*2.399963;
        double r=raio*(1.07+.035*Math.sin(a*5-tick*.13+faixa));
        double x=Math.cos(a)*r,y=Math.sin(a)*r,z=Math.sin(a*3+faixa)*raio*.045;
        double inclinacao=.55+faixa*.95,co=Math.cos(inclinacao),si=Math.sin(inclinacao);
        return new Ponto(x,y*co-z*si,y*si+z*co);
    }
    public static Ponto ponte(int faixa,double s,double tick){return new Ponto(PerfilVfxGojo.separacaoRoxo(tick)*(2*s-1),Math.sin(s*Math.PI)*Math.sin(tick*.12+faixa)*.6,0);}
    public static Ponto rastro(int faixa,int total,double s,double tick){double a=faixa*Math.PI*2/total+s*9-tick*.12;return new Ponto(Math.cos(a)*(3.7+s),Math.sin(a)*(3.7+s),-s*Math.min(12,Math.max(0,tick-30)*1.6));}
    public static double formacao(double tick){return PerfilVfxGojo.suave((tick-21)/9);}
    private CoroaRoxoGojo(){}
}
