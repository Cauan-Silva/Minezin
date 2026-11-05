package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;

import java.util.ArrayList;
import java.util.List;

/**
 * G-X Whirlpool (contra-ataque do Godspeed): Killua entra em guarda e uma rede de arcos fecha uma cupula em volta dele
 * (janela de JANELA ticks no sistema de interrupcao). Se for atingido dentro dela, some num raio, reaparece atras do
 * atacante e gira num chute com um redemoinho de raios que sobe em espiral; o atacante e interrompido e arremessado.
 * Coordenadas locais da previa: Killua em (0,0,0) olhando +z, atacante em ATACANTE; golpe recebido em ACERTO.
 */
public final class KilluaWhirlpool {
    public static final int ENTRADA = 3, JANELA = 10, ACERTO = 7, SUMICO = 2, CHUTE = 8, FIM = 40;
    public static final int REAPARECE = ACERTO + SUMICO, FIM_CHUTE = REAPARECE + CHUTE, GOLPE = REAPARECE + 3;
    public static final double ATACANTE = 2.2, ATRAS = 1.1, RAIO_CUPULA = 1.25;
    public static final int ARCOS_CUPULA = 14, ESPIRAIS = 9, ARCOS_ALVO = 6;

    private static double s(double t) { return PerfilVfxGojo.suave(t); }
    private static double lim(double v) { return Math.max(0, Math.min(1, v)); }
    private static double r(int a, int b, int c) { return KilluaBase.ruido(a, b, c); }

    /** Posicao do Killua: parado em guarda; depois do acerto aparece atras do atacante. */
    public static P corpo(double t) { return t < REAPARECE ? new P(0, 0, 0) : new P(0, 0, ATACANTE + ATRAS); }
    /** Yaw do corpo (graus): olha +z; atras do atacante vira para -z e gira 360 no chute. */
    public static double giro(double t) {
        if (t < REAPARECE) return 0;
        return 180 + 360 * s((t - REAPARECE) / (CHUTE - 1.5));
    }
    public static double alphaCorpo(double t) { return t < ACERTO ? 1 : t < REAPARECE ? .15 : lim((t - REAPARECE) / 1.5 + .4); }

    /** Atacante: avanca e golpeia em ACERTO; depois e arremessado para tras e para cima (sentido -z). */
    public static P atacante(double t) {
        double z = ATACANTE - .5 * s(t / ACERTO);
        if (t < GOLPE) return new P(0, 0, z);
        double a = t - GOLPE, u = lim(a / 14);
        return new P(0, Math.max(0, 2.6 * Math.sin(Math.PI * u) - .0), z - 4.2 * s(u));
    }
    public static double alphaParalisia(double t) { double a = t - GOLPE; return a < 0 || a > 24 ? 0 : 1 - s((a - 16) / 8); }

    // ---- guarda
    public static double alphaGuarda(double t) {
        if (t > ACERTO + 1) return 0;
        return s(t / ENTRADA) * (t < ACERTO ? 1 : 1 - (t - ACERTO));
    }
    /** Arco k da cupula: corre por um meridiano/paralelo da esfera em volta do corpo (rede eletrica). */
    public static List<P> arcoCupula(int k, double t) {
        int q = (int) Math.floor(t);
        double a0 = 2 * Math.PI * Math.abs(r(k, 3000, q)), y0 = .2 + 1.6 * Math.abs(r(k, 3001, q));
        double varre = (.8 + 1.2 * Math.abs(r(k, 3002, q))) * (r(k, 3003, q) > 0 ? 1 : -1), dy = r(k, 3004, q) * .9;
        double rr = RAIO_CUPULA * (.6 + .4 * s(t / ENTRADA));
        List<P> l = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            double u = i / 7.0, a = a0 + varre * u, y = Math.max(.05, Math.min(2.2, y0 + dy * u));
            double ey = (y - 1.0) / 1.3, h = Math.sqrt(Math.max(.05, 1 - ey * ey)), j = i == 0 || i == 7 ? 0 : r(k * 13 + i, 3005, q) * .1;
            l.add(new P(Math.cos(a) * rr * h * (1 + j), y + j, Math.sin(a) * rr * h * (1 + j)));
        }
        return l;
    }
    /** Anel de guarda no chao, girando. */
    public static List<P> anelGuarda(int camada, double t) {
        return KilluaBase.anel(new P(0, .03, 0), KilluaBase.X, KilluaBase.Z, RAIO_CUPULA * (1 + .18 * camada), 40, t * (.25 - camada * .5));
    }

    // ---- sumico (raio que liga a guarda ao ponto atras do atacante, por cima dele)
    public static double alphaSumico(double t) { double a = t - ACERTO; return a < 0 || a > SUMICO + 6 ? 0 : a < SUMICO ? 1 : 1 - (a - SUMICO) / 6; }
    public static List<P> raioSumico(int k, double t) {
        P a = new P(0, 1.0, 0), b = new P(0, 1.0, ATACANTE + ATRAS), m = new P(r(k, 3100, 0) * .4, 2.2 + .3 * k, ATACANTE * .55);
        List<P> l = new ArrayList<>(KilluaBase.raioQuebrado(a, m, 5, .2, 3110 + k, t));
        l.addAll(KilluaBase.raioQuebrado(m, b, 5, .2, 3120 + k, t).subList(1, 6));
        return l;
    }
    public static List<double[]> faiscasSumico(double t) {
        List<double[]> l = new ArrayList<>(KilluaBase.particulas(3150, 20, new P(0, 1, 0), ACERTO, t, .28, .25, new P(0, .3, 0), .012, 10));
        l.addAll(KilluaBase.particulas(3160, 20, new P(0, 1, ATACANTE + ATRAS), REAPARECE, t, .3, .25, new P(0, .3, 0), .012, 10));
        return l;
    }

    // ---- chute em redemoinho
    public static double alphaRedemoinho(double t) { double a = t - REAPARECE; return a < 0 || a > CHUTE + 10 ? 0 : Math.min(1, a / 1.5 + .3) * (1 - s((a - CHUTE + 2) / 10)); }
    /** Espiral k: raio que gira em volta do Killua subindo do pe que chuta ate acima da cabeca e abre com o tempo. */
    public static List<P> espiral(int k, double t) {
        double a = t - REAPARECE, abre = .55 + .9 * s(a / CHUTE), fase = 2 * Math.PI * k / ESPIRAIS + a * 1.1;
        int q = (int) Math.floor(t);
        P c = corpo(t);
        List<P> l = new ArrayList<>();
        for (int i = 0; i <= 12; i++) {
            double u = i / 12.0, ang = fase + u * 2.6, rr = abre * (.5 + .7 * u) * (1 + r(k * 17 + i, 3200, q) * .12);
            l.add(c.mais(new P(Math.cos(ang) * rr, .15 + 2.1 * u + r(k * 17 + i, 3201, q) * .08, Math.sin(ang) * rr)));
        }
        return l;
    }
    /** Arco do pe (rastro do chute): circulo inclinado na altura da cintura que o pe percorre. */
    public static List<P> arcoChute(double t) {
        double a = t - REAPARECE, u = s(a / (CHUTE - 1.5)), ate = 2 * Math.PI * u;
        P c = corpo(t).mais(new P(0, 1.05, 0));
        List<P> l = new ArrayList<>();
        for (int i = 0; i <= 24; i++) {
            double ang = ate - Math.PI * 1.2 * i / 24.0;
            l.add(c.mais(new P(Math.sin(ang) * 1.05, Math.sin(ang * 1) * .18, -Math.cos(ang) * 1.05)));
        }
        return l;
    }
    /** Impacto no atacante (no tick GOLPE): estrela de raios. */
    public static double alphaImpacto(double t) { double a = t - GOLPE; return a < 0 || a > 8 ? 0 : 1 - s(a / 8); }
    public static List<P> raioImpacto(int k, double t) {
        P c = atacante(GOLPE).mais(new P(0, 1.2, 0));
        double th = 2 * Math.PI * k / 10 + r(k, 3300, 0) * .3, comp = (.9 + .8 * Math.abs(r(k, 3301, 0))) * s((t - GOLPE) / 2);
        return KilluaBase.raioQuebrado(c, c.mais(new P(Math.cos(th) * comp, Math.sin(th) * comp, -.3 * comp)), 4, .15, 3310 + k, t);
    }
    public static List<P> arcoAlvo(int k, double t) { return KilluaBase.arcoNoCorpo(atacante(t), k, 3400, t, .14); }
    public static List<double[]> faiscasImpacto(double t) {
        return KilluaBase.particulas(3450, 34, atacante(GOLPE).mais(new P(0, 1.2, 0)), GOLPE, t, .42, .3, new P(0, .3, -.7), .012, 14);
    }
}
