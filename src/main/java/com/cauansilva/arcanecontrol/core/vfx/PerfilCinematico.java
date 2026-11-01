package com.cauansilva.arcanecontrol.core.vfx;

/** Pure choreography. Ages are game ticks, never wall-clock/render-frame counters. */
public final class PerfilCinematico {
    public static final int FORMA_IMPACTO=47;
    private PerfilCinematico() {}
    public record Ponto(double x,double y,double z) {}
    public static double limitar(double v){return Math.max(0,Math.min(1,v));}
    public static double impacto(double idade){return idade<0||idade>=24?0:Math.pow(1-idade/24,2);}
    public static double corte(double idade,int acerto){
        if(idade<acerto-2||idade>=acerto+12)return 0;
        return limitar((idade-acerto+2)/2)*Math.pow(limitar((acerto+12-idade)/12),1.5);
    }
    public static double varredura(double idade,int acerto){return limitar((idade-acerto+2)/3);}
    public static double trauma(double idade,double distancia,double alcance,double forca){
        if(!Double.isFinite(distancia)||alcance<=0)return 0;
        return limitar(forca)*impacto(idade)*limitar(1-distancia/alcance);
    }
    public static int fragmentos(boolean reduzido){return reduzido?6:18;}
    public static int raios(boolean reduzido){return reduzido?4:10;}
    public static double revelarForma(double idade,String attachment){
        return switch(attachment){
            case "right_arm"->PerfilVfxGojo.suave((idade-4)/14);
            case "head"->PerfilVfxGojo.suave((idade-18)/16);
            case "body"->PerfilVfxGojo.suave((idade-30)/17);
            default->0;
        };
    }
    public static double abrirAsa(double idade){return PerfilVfxGojo.suave((idade-26)/21);}
    public static Ponto orbita(int indice,double tick,double nucleo){
        // Each fragment spirals inward on a 64-tick cycle; fade its reset, not a teleporting bright rock.
        double fase=((tick+indice*7)%64+64)%64/64;
        double ang=indice*2.399963+tick*.035+fase*3;
        double r=nucleo+1+(1-fase)*(8+indice%4);
        return new Ponto(Math.cos(ang)*r,Math.sin(ang*.7)*r*.22,Math.sin(ang)*r);
    }
    public static double opacidadeFragmento(int indice,double tick){
        double fase=((tick+indice*7)%64+64)%64/64;
        return limitar(fase*8)*limitar((1-fase)*8);
    }
}
