package com.cauansilva.arcanecontrol.core.cid;

/** Geometria deterministica, sem depender do estado global do renderizador. */
public final class FormaEnergiaAtomic {
    public record Ponto(double x, double y, double z) {}
    public static Ponto filamento(int indice, double s, double tick) {
        double carga=Math.max(0,Math.min(1,tick/90));
        double r=(1-s)*(5.5-2*carga);
        double ang=indice*2.399963+s*5-tick*.035;
        double ondulacao=Math.sin(s*18+indice*3+tick*.09)*(1-s)*.22;
        return new Ponto(Math.cos(ang)*r+ondulacao,
                (1-s)*(1+Math.sin(indice*1.7)*2)+Math.sin(s*Math.PI)*.8,
                Math.sin(ang)*r-ondulacao);
    }
    public static double raio(double angulo, double latitude, double tick, double base) {
        double turbulencia=Math.sin(angulo*7+tick*.075+latitude*5)*.07
                +Math.sin(angulo*13-tick*.11+latitude*9)*.035;
        double atenuacao=Math.cos(latitude)*Math.cos(latitude);
        return base*(1+turbulencia*atenuacao);
    }
    public static double onda(double tick, int indice) {
        double idade=tick-90-indice*5;
        return idade<=0?0:18*(1-Math.exp(-idade/13));
    }
}
