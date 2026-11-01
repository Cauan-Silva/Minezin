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
    public static Ponto campo(int indice,double s,double tick){
        double crescimento=com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo.suave((tick-8)/40);
        double a=indice*2.399963,r=3+(indice%7)*1.7;
        double h=s*(12+(indice%5)*1.8)*crescimento;
        double inclinar=1-s*s*.35;
        return new Ponto(Math.cos(a)*r*inclinar+Math.sin(s*9+indice)*.12,h,Math.sin(a)*r*inclinar);
    }
    public static Ponto coluna(double angulo,double s,double tick){
        double idade=Math.max(0,tick-90),fade=Math.max(0,1-idade/150);
        double base=(1.2+4.8*(1-Math.exp(-idade/9)))*fade;
        double r=base*(.82+.6*Math.sqrt(s))*(1+Math.sin(angulo*7+s*12-tick*.08)*.035);
        return new Ponto(Math.cos(angulo)*r,s*48,Math.sin(angulo)*r);
    }
    public record Detrito(Ponto ponto,double tamanho,double alpha,double giro){}
    public static Detrito detrito(int indice,double tick){
        double idade=tick-94-(indice%5)*1.5,vida=55+(indice%6)*7;
        if(!Double.isFinite(tick)||idade<0||idade>=vida)return new Detrito(new Ponto(0,0,0),0,0,0);
        double ang=indice*2.399963,vel=.08+(indice%7)*.018,r=2+(indice%9)*.55+idade*vel;
        double h=Math.max(.1,(.28+(indice%5)*.055)*idade-.0055*idade*idade);
        double fade=Math.min(1,idade/3)*Math.min(1,(vida-idade)/12);
        return new Detrito(new Ponto(Math.cos(ang)*r,h,Math.sin(ang)*r),.12+(indice%4)*.055,fade,idade*(.04+(indice%3)*.01));
    }
    public static double onda(double tick, int indice) {
        double idade=tick-90-indice*5;
        return idade<=0?0:18*(1-Math.exp(-idade/13));
    }
}
