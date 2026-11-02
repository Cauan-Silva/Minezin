package com.cauansilva.arcanecontrol.core.vfx;

/** Visual-only attraction paths, expressed in world-relative coordinates. */
public final class FluxoAzulGojo {
    public record Ponto(double x,double y,double z){}
    public static double raio(double tick){return .72*(.94+.06*Math.sin(tick*.3));}
    public static Ponto fluxo(int faixa,double s,double tick){
        double a=faixa*2.399963+s*5.6-tick*.035;
        double r=.75+4.5*Math.pow(1-s,1.45);
        double inclinacao=Math.sin(faixa*1.91)*.65;
        return new Ponto(Math.cos(a)*r,Math.sin(a+faixa)*r*inclinacao,Math.sin(a)*r);
    }
    public static double pulso(int faixa,double s,double tick){
        // Increasing s travels from the outer field towards the nucleus.
        double onda=.5+.5*Math.cos((s-tick*.045-faixa*.17)*Math.PI*2);
        return (.16+.84*Math.pow(onda,12))*PerfilVfxGojo.suave(s/.12);
    }
    public static Ponto arco(int faixa,double s,double tick){
        double a=(s*.82+faixa*.31)*Math.PI*2+tick*(faixa%2==0?.045:-.037);
        double r=raio(tick)*(1.3+faixa*.23),y=Math.sin(a)*r;
        double inclinacao=.45+faixa*.8;
        return new Ponto(Math.cos(a)*r,y*Math.cos(inclinacao),y*Math.sin(inclinacao));
    }
}
