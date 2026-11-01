package com.cauansilva.arcanecontrol.core.vfx;

/** Tempos e alcances compartilhados com a simulacao do servidor. */
public final class PerfilVfxGojo {
    public static final int VERMELHO_DISPARO=13, ROXO_DISPARO=30, DOMINIO_ABERTURA=35;
    public static final double ROXO_VELOCIDADE=1.6, ROXO_ORIGEM=3.2, DOMINIO_RAIO=26;
    public static int duracao(int tipo) {
        return switch(tipo) {case 1->60;case 2->28;case 4->85;case 5->400;default->0;};
    }
    public static double suave(double t) {
        t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);
    }
    public static double separacaoRoxo(double tick) {return 3.2*(1-suave(tick/ROXO_DISPARO));}
    public static double distanciaRoxo(double tick) {return ROXO_ORIGEM+Math.max(0,tick-ROXO_DISPARO)*ROXO_VELOCIDADE;}
    public static double raioDominio(double tick) {return DOMINIO_RAIO*suave(tick/DOMINIO_ABERTURA);}
    public static double presencaDominio(double tick,double distancia) {
        double raio=raioDominio(tick);
        return suave((raio-distancia)/2)*suave((400-tick)/18)*suave((tick-20)/15);
    }
    public static double opacidade(int tipo,double tick) {
        int fim=duracao(tipo);
        return fim==0 || tick<0?0:suave(tick/4)*suave((fim-tick)/8);
    }
}
