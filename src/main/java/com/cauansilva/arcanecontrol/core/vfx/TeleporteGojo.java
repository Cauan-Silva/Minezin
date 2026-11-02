package com.cauansilva.arcanecontrol.core.vfx;

/** Residual presentation at both server-confirmed teleport endpoints; never moves the player. */
public final class TeleporteGojo {
    public static final int DURACAO=18;
    public record Ponto(double x,double y,double z){}
    public static double saida(double t){return t<0?0:Math.pow(1-PerfilVfxGojo.suave(t/10),2);}
    public static double chegada(double t){return t<0?0:(1-PerfilVfxGojo.suave(t/18))*(.65+.35*Math.exp(-Math.pow((t-2)/3,2)));}
    public static double rastro(double t){return t<0?0:.7*(1-PerfilVfxGojo.suave(t/6));}
    public static Ponto arco(int faixa,double s,double t,boolean chegada){
        double a=s*Math.PI*1.8+faixa*2.399963+t*.07;
        double abertura=chegada?.5+.6*PerfilVfxGojo.suave(t/6):.9*(1-PerfilVfxGojo.suave(t/10));
        return new Ponto(Math.cos(a)*abertura,1.05+Math.sin(a)*1.05,Math.sin(a*3+faixa)*.08);
    }
    public static Ponto trilha(int faixa,double s,double t,double dx,double dy,double dz){
        double h=Math.hypot(dx,dz),x=h<1e-8?1:-dz/h,z=h<1e-8?0:dx/h;
        double curva=Math.sin(s*Math.PI)*Math.sin(s*6+faixa*2+t*.2)*(.18+faixa*.045);
        return new Ponto(dx*s+x*curva,dy*s+.9+faixa*.12+Math.sin(s*Math.PI)*.12,dz*s+z*curva);
    }
    public static Ponto fragmento(int i,double t,boolean chegada){
        double a=i*2.399963+t*.05,r=chegada?.15+.9*(1-PerfilVfxGojo.suave(t/18)):.3+t*.07;
        return new Ponto(Math.cos(a)*r,.2+(i%7)*.3+t*.025,Math.sin(a)*r);
    }
}
