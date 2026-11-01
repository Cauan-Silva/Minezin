package com.cauansilva.arcanecontrol.core.vfx;
/** Visual clock only. The server's 50/90/170/240 attack landmarks are unchanged. */
public final class RoteiroCinemaAtomic {
    public enum Fase{INATIVA,CARGA,COMPRESSAO,NEGATIVO,PAPEL,INVERSAO,DUOTOM,TRACO,DESCARGA,RESCALDO}
    public static Fase fase(double t,boolean frames){
        if(!Double.isFinite(t)||t<0||t>=PerfilAtomic.FIM)return Fase.INATIVA;
        if(t<50)return Fase.CARGA;
        if(t<88)return Fase.COMPRESSAO;
        if(t<90&&!frames)return Fase.COMPRESSAO;
        if(frames){if(t<90)return Fase.NEGATIVO;if(t<91.25)return Fase.PAPEL;if(t<92.5)return Fase.INVERSAO;if(t<94)return Fase.DUOTOM;if(t<96)return Fase.TRACO;}
        return t<130?Fase.DESCARGA:Fase.RESCALDO;
    }
    public static double peso(double t,double distancia,boolean local){
        if(!Double.isFinite(t)||!Double.isFinite(distancia)||t<0||t>=240||distancia<0)return 0;
        double entrada=suave(t/12),saida=1-suave((t-170)/70);
        double alcance=local?1:1-suave((distancia-20)/60);
        return entrada*saida*alcance;
    }
    public static double suave(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    private RoteiroCinemaAtomic(){}
}
