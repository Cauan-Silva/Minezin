package com.cauansilva.arcanecontrol.core.vfx;

/** Visual-only compressed charge and instantaneous repulsion cone. Coordinates relative to focus. */
public final class DescargaVermelhoGojo {
    public record Ponto(double lateral,double vertical,double frente){}
    public static double raio(double tick){return tick<13?.1+.32*PerfilVfxGojo.suave(tick/13):.5;}
    public static double impacto(double tick){return tick<13?0:Math.exp(-(tick-13)/4);}
    public static Ponto carga(int faixa,double s,double tick){
        double a=s*Math.PI*1.65+faixa*2.399963+tick*.11;
        double r=raio(tick)*(1.35+.3*Math.sin(faixa*2.1));
        double incl=.4+faixa*.6,y=Math.sin(a)*r;
        return new Ponto(Math.cos(a)*r,y*Math.cos(incl),y*Math.sin(incl));
    }
    public static Ponto descarga(int faixa,int total,double s,double tick){
        double a=faixa*Math.PI*2/total;
        double d=1.8+s*20.2,idade=Math.max(0,tick-13);
        double r=(.2+d*.12)*(1+idade*.17)*PerfilVfxGojo.suave(s/.12);
        double torcao=Math.sin(s*18+faixa*2.7+tick*.39)*.13*Math.sin(s*Math.PI);
        return new Ponto(Math.cos(a+torcao)*r,Math.sin(a+torcao)*r,s*20.2);
    }
    public static Ponto onda(int faixa,double s,double tick){
        double a=s*Math.PI*2;
        double r=(1+faixa*1.3)*(1+Math.max(0,tick-13)*.17);
        r*=1+.025*Math.sin(a*9-tick*.5+faixa);
        return new Ponto(Math.cos(a)*r,Math.sin(a)*r,1+faixa*6+Math.sin(a*5+tick*.3)*.12);
    }
}
