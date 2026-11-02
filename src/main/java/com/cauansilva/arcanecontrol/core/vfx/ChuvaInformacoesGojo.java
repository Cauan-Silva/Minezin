package com.cauansilva.arcanecontrol.core.vfx;

/** Presentation for accepted effect 40 / kit 1. Falling data remains inside the existing domain. */
public final class ChuvaInformacoesGojo {
    public static final int DURACAO=70,FAIXAS=48,PASSOS=12,FAIXAS_REDUZIDAS=20,PASSOS_REDUZIDOS=6;
    public record Ponto(double x,double y,double z){}
    public static Ponto coluna(int faixa,double s,double tick){
        double a=faixa*2.399963,r=Math.sqrt(frac(faixa*.61803398875+.13))*23;
        double teto=Math.sqrt(26*26-r*r)*.9,comprimento=Math.min(6,teto*.42);
        double cabeca=.35+(1-frac(faixa*.37+tick*.035))*(teto-comprimento-.35);
        double curva=Math.sin(s*3.8+tick*.05+faixa)*.12*Math.sin(s*Math.PI);
        return new Ponto(Math.cos(a)*r+Math.sin(a)*curva,cabeca+s*comprimento,Math.sin(a)*r-Math.cos(a)*curva);
    }
    public static double alpha(double tick){return PerfilVfxGojo.suave(tick/2)*PerfilVfxGojo.suave((DURACAO-tick)/10);}
    public static double presenca(int faixa,double tick){
        double fase=frac(faixa*.37+tick*.035);
        return PerfilVfxGojo.suave(fase/.08)*PerfilVfxGojo.suave((1-fase)/.1);
    }
    public static double luz(double s){return .2+.8*Math.pow(1-s,.7);}
    private static double frac(double x){return x-Math.floor(x);}
}
