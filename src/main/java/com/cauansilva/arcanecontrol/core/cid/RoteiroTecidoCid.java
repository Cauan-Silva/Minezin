package com.cauansilva.arcanecontrol.core.cid;
/** Pure visual impulses in ticks; no movement, damage or network changes. */
public final class RoteiroTecidoCid {
    public record Impulso(double frente,double lado,double giro){}
    private static double pulso(double t,double centro,double largura){double u=Math.abs(t-centro)/largura;return u>=1?0:Math.pow(1-u*u,2);}
    public static Impulso amostrar(int tipo,double t){
        if(!Double.isFinite(t)||t<0)return new Impulso(0,0,0);
        return switch(tipo){
            case 11 -> new Impulso(.10*pulso(t,6,5),.13*pulso(t,6,5),.18*pulso(t,6,5));
            case 12 -> new Impulso(.24*pulso(t,2,3)-.06*pulso(t,9,5),0,0);
            case 13 -> new Impulso(.045*pulso(t,8,8),.025*Math.sin(t*.5)*pulso(t,20,25),0);
            case 14 -> new Impulso(.025*pulso(t,65,30)+.23*pulso(t,92,9),.018*Math.sin(t*.4)*pulso(t,65,30),.06*pulso(t,92,9));
            case 17 -> new Impulso(.18*pulso(t,8,8),.04*Math.sin(t*.5)*pulso(t,10,10),.07*pulso(t,8,8));
            default -> new Impulso(0,0,0);
        };
    }
    private RoteiroTecidoCid(){}
}
