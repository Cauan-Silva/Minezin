package com.cauansilva.arcanecontrol.core.vfx;

/** Deterministic, continuous lightning paths in the projectile local frame. */
public final class RaiosRoxo {
    public record Ponto(double lateral,double vertical,double frente){}
    public static Ponto ponto(int raio,int segmento,double tick){
        double s=segmento/8.0;
        // Each bolt changes heading independently; the fan also tilts into depth.
        double a=raio*2.399963+Math.sin(tick*.105+raio*1.71)*1.15
                +Math.sin(tick*.047+raio*3.1)*.55;
        double curva=Math.sin(tick*.17+raio*2.3)*s*.65;
        double elevacao=Math.sin(tick*.083+raio*1.93)*.72;
        double r=3.9+s*(2.7+(Math.sin(raio*3+tick*.09)*.5+.5)*1.5);
        double desvio=Math.sin(raio*3+segmento*2.1+tick*.7)*.6*Math.sin(s*Math.PI);
        double radial=r*Math.cos(elevacao), angulo=a+curva;
        return new Ponto(Math.cos(angulo)*radial-Math.sin(angulo)*desvio,
                Math.sin(angulo)*radial+Math.cos(angulo)*desvio,
                r*Math.sin(elevacao)+Math.sin(raio*1.7+segmento*.9+tick*.23)*s*.65);
    }
    public static Ponto ramo(int raio,int segmento,double tick){
        var p=ponto(raio,segmento,tick);
        double a=raio*2.7+segmento+Math.sin(tick*.19+raio)*2.2;
        double z=Math.sin(tick*.13+segmento+raio)*.9;
        double comprimento=1.1+.6*(.5+.5*Math.sin(tick*.27+raio+segmento));
        return new Ponto(p.lateral()+Math.cos(a)*comprimento,
                p.vertical()+Math.sin(a)*comprimento,p.frente()+z);
    }
    public static double intensidade(int raio,double tick){return .65+.35*Math.pow(Math.max(0,Math.sin(tick*.22+raio*1.7)),4);}
}
