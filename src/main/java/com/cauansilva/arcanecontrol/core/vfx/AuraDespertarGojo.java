package com.cauansilva.arcanecontrol.core.vfx;

/** Body-relative presentation of the existing Gojo form, without changing its duration. */
public final class AuraDespertarGojo {
    public record Ponto(double lateral,double altura,double frente){}
    public static double idade(int restante,double decorrido){return Math.max(0,PerfilVfxGojo.duracao(5)-restante+decorrido);}
    public static double surto(double tick){return Math.exp(-Math.pow((tick-35)/7,2));}
    public static double intensidade(double tick){return PerfilVfxGojo.suave(tick/10)*PerfilVfxGojo.suave((400-tick)/12)*(.55+.45*surto(tick));}
    public static Ponto fio(int faixa,double s,double tick){
        double a=faixa*2.399963+s*2.4-tick*.04+Math.sin(tick*.06+faixa)*.3;
        double r=.68+.25*Math.sin(s*Math.PI)+surto(tick)*.35;
        return new Ponto(Math.cos(a)*r,.08+2.35*s+Math.sin(a*2+tick*.09)*.08,Math.sin(a)*r);
    }
    public static double pulso(int faixa,double s,double tick){
        double onda=.5+.5*Math.cos((s-tick*.045-faixa*.17)*Math.PI*2);
        return (.2+.8*Math.pow(onda,8))*Math.pow(Math.sin(s*Math.PI),.6);
    }
    public static Ponto particula(int indice,double tick){
        double fase=frac(tick*.014+indice*.61803398875),a=indice*2.399963+tick*.013;
        double r=1.05+.35*Math.sin(indice*1.9+tick*.025);
        return new Ponto(Math.cos(a)*r,.08+fase*2.9,Math.sin(a)*r);
    }
    public static double brilhoParticula(int indice,double tick){return Math.pow(Math.sin(frac(tick*.014+indice*.61803398875)*Math.PI),2);}
    public static double raioOnda(double tick){return tick<35||tick>55?0:.8+(tick-35)*.28;}
    public static double alphaOnda(double tick){return tick<35||tick>55?0:1-PerfilVfxGojo.suave((tick-35)/20);}
    private static double frac(double x){return x-Math.floor(x);}
}
