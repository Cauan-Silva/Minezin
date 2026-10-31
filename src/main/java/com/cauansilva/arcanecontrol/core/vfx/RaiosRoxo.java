package com.cauansilva.arcanecontrol.core.vfx;
public final class RaiosRoxo {
    public record Ponto(double lateral,double vertical,double frente){}
    public static Ponto ponto(int raio,int segmento,double tick){
        double s=segmento/8.0,a=raio*Math.PI/4+tick*.008;
        double r=3.9+s*4.2;
        double desvio=Math.sin(raio*3+segmento*2.1+Math.floor(tick/3))*.6*Math.sin(s*Math.PI);
        return new Ponto(Math.cos(a)*r-Math.sin(a)*desvio,Math.sin(a)*r+Math.cos(a)*desvio,
                Math.sin(raio*1.7+segmento*.9)*s*1.2);
    }
    public static double intensidade(int raio,double tick){return .65+.35*Math.pow(Math.max(0,Math.sin(tick*.22+raio*1.7)),4);}
}
