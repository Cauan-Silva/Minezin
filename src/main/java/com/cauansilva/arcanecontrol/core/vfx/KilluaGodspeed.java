package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;

import java.util.ArrayList;
import java.util.List;

/**
 * Godspeed / Kanmuru (G do Killua; referencia HxH 2011, ep. 116-118): ele concentra a eletricidade (arcos sobem das
 * maos pelos bracos e correm pelo chao ate os pes), estoura numa coluna de raios para cima e fica envolto numa aura
 * eletrica continua volumetrica (tres cascas que fervem, arcos no volume, aneis orbitando, descargas). Nao mexe no cabelo. Origem nos pes; x lateral, y para cima, z para
 * frente; t em ticks a partir do G. Funcoes de aura aceitam qualquer t (loop) enquanto a forma durar.
 */
public final class KilluaGodspeed {
    public static final int CARGA = 14, ATIVA = CARGA, ESTOURO = 10, ENTRADA = 30, DURACAO = 600;
    public static final int ARCOS_BRACO = 6, ARCOS_CHAO = 10, RAIOS_COLUNA = 9, RAIOS_ESTOURO = 28, ARCOS_CORPO = 12;

    private static double s(double t) { return PerfilVfxGojo.suave(t); }
    private static double lim(double v) { return Math.max(0, Math.min(1, v)); }
    private static double r(int a, int b, int c) { return KilluaBase.ruido(a, b, c); }

    // ------------------------------------------------------------------ entrada: carga
    public static double alphaCarga(double t) { return t < 0 || t > CARGA + 2 ? 0 : s(t / 5) * (1 - s((t - CARGA) / 2)); }
    /** Arcos que sobem das maos (ao lado do quadril) ate os ombros. lado = 1 direita, -1 esquerda. */
    public static List<P> arcoBraco(int k, int lado, double t) {
        double sobe = s(lim(t / CARGA)), q = Math.floor(t / 1.5);
        P mao = new P(lado * .55, .75, .05), ombro = new P(lado * .4, 1.45, 0);
        P a = P.lerp(mao, ombro, Math.abs(r(k, 600 + lado, (int) q)) * sobe * .6);
        P b = P.lerp(mao, ombro, Math.min(1, sobe * (.4 + .6 * Math.abs(r(k, 602 + lado, (int) q)))));
        return KilluaBase.raioQuebrado(a, b.mais(new P(lado * .1, 0, r(k, 604, (int) q) * .1)), 6, .09, 610 + k * 2 + lado, t * 1.4);
    }
    /** Arcos no chao convergindo para os pes. */
    public static List<P> arcoChao(int k, double t) {
        double u = s(lim(t / CARGA)), a = 2 * Math.PI * k / ARCOS_CHAO + r(k, 620, 0) * .3;
        double r0 = 3.2 - 2.6 * u, r1 = Math.max(.25, r0 - 1.1);
        return KilluaBase.raioQuebrado(new P(Math.cos(a) * r0, .03, Math.sin(a) * r0), new P(Math.cos(a) * r1, .03, Math.sin(a) * r1), 6, .15, 630 + k, t);
    }
    /** Faiscas que sobem do chao em volta dele na carga. */
    public static List<double[]> faiscasCarga(double t) {
        List<double[]> l = new ArrayList<>();
        for (int k = 0; k < 5; k++) l.addAll(KilluaBase.particulas(640 + k, 8, new P(r(k, 641, 0) * .8, .05, r(k, 642, 0) * .8), k * 2.5, t, .12, .05, new P(0, 1, 0), -.002, 12));
        return l;
    }

    // ------------------------------------------------------------------ entrada: estouro
    public static double alphaEstouro(double t) { double a = t - ATIVA; return a < 0 || a > ESTOURO + 6 ? 0 : Math.min(1, a + .5) * (1 - s((a - 4) / (ESTOURO + 2))); }
    public static double clarao(double t) { double a = t - ATIVA; return a < 0 || a > 6 ? 0 : 1 - s(a / 6); }
    /** Coluna: raios que sobem do corpo ate o alto. */
    public static List<P> raioColuna(int k, double t) {
        double a = 2 * Math.PI * k / RAIOS_COLUNA, h = 5 + 3 * Math.abs(r(k, 650, 0)), sobe = s(lim((t - ATIVA) / 3));
        P base = new P(Math.cos(a) * .3, .2, Math.sin(a) * .3);
        P topo = new P(Math.cos(a) * (.6 + .8 * Math.abs(r(k, 651, 0))), .2 + h * sobe, Math.sin(a) * .6);
        return KilluaBase.raioQuebrado(base, topo, 14, .3, 660 + k, t);
    }
    /** Linhas radiais do estouro (esfera achatada). */
    public static P[] raioEstouro(int k, double t) {
        double a = t - ATIVA, g = 2.39996 * k, yv = 1 - 2 * (k + .5) / RAIOS_ESTOURO, rr = Math.sqrt(1 - yv * yv);
        P d = new P(Math.cos(g) * rr, yv * .7, Math.sin(g) * rr);
        double comp = (1.6 + 2.4 * Math.abs(r(k, 670, 0))) * (.3 + .7 * s(lim(a / 4)));
        P c = new P(0, 1.1, 0);
        return new P[]{c.mais(d.vezes(.5 + comp * .3)), c.mais(d.vezes(.5 + comp))};
    }
    public static List<P> ondaChao(int camada, double t) {
        double rr = .5 + (4.5 - camada) * s(lim((t - ATIVA) / (ESTOURO + 2)));
        return KilluaBase.anel(new P(0, .04 + camada * .02, 0), KilluaBase.X, KilluaBase.Z, rr, 56, camada);
    }
    public static List<double[]> faiscasEstouro(double t) {
        return KilluaBase.particulas(680, 44, new P(0, 1.1, 0), ATIVA, t, .5, .35, new P(0, .25, 0), .02, 16);
    }

    // ------------------------------------------------------------------ aura continua (volumetrica, sem cabelo)
    public static final int CAMADAS = 3, LAT = 9, LON = 16, ARCOS_VOLUME = 30, TENTACULOS = 14, ANEIS = 4;
    /** Intensidade da aura: nasce no estouro e fica (pulsa); some no fim da forma. */
    public static double alphaAura(double t) {
        if (t < ATIVA) return 0;
        return Math.min(1, (t - ATIVA) / 3) * (1 - s((t - DURACAO + 10) / 10)) * (.85 + .15 * Math.sin(t * .9));
    }
    /** Raio base da camada c (0 = colada ao corpo, 2 = a mais larga). */
    static double raioCamada(int c) { return .5 + .22 * c; }
    /**
     * Casca c da aura: elipsoide envolvendo o corpo (centro 1,0 m, semi-altura 1,15 + 0,1c), com a superficie
     * tremendo por ruido que muda a cada tick (a eletricidade "ferve"). i = latitude 0..LAT, j = longitude 0..LON-1.
     */
    public static P casca(int c, int i, int j, double t) {
        double th = Math.PI * i / LAT, ph = 2 * Math.PI * j / LON + t * (.05 + .03 * c) * (c % 2 == 0 ? 1 : -1);
        int q = (int) Math.floor(t);
        double ruido = .07 * r(c * 97 + i, j, q) + .05 * r(c * 31 + j, i, q / 2);
        double rr = raioCamada(c) * (1 + ruido) * (i == 0 || i == LAT ? .25 : Math.sin(th) * (.8 + .2 * Math.sin(th)));
        double h = 1.0 - Math.cos(th) * (1.15 + .1 * c) * (1 + ruido * .5);
        return new P(Math.cos(ph) * rr, Math.max(0, h), Math.sin(ph) * rr * .8);
    }
    /** Arcos dentro do volume: ligam dois pontos aleatorios das cascas (trocam a cada tick), com desvio forte. */
    public static List<P> arcoVolume(int k, double t) {
        int q = (int) Math.floor(t);
        int c0 = Math.abs((int) (r(k, 800, q) * 3)) % CAMADAS, c1 = Math.abs((int) (r(k, 801, q) * 3)) % CAMADAS;
        P a = casca(c0, 1 + Math.abs((int) (r(k, 802, q) * 7)) % (LAT - 1), Math.abs((int) (r(k, 803, q) * 16)) % LON, t);
        P b = casca(c1, 1 + Math.abs((int) (r(k, 804, q) * 7)) % (LAT - 1), Math.abs((int) (r(k, 805, q) * 16)) % LON, t);
        return KilluaBase.raioQuebrado(a, b, 10, .16, 810 + k, t * 2);
    }
    /** Arcos que correm pela superficie do corpo (mais densos que os do volume). */
    public static List<P> arcoCorpo(int k, double t) { return KilluaBase.arcoNoCorpo(new P(0, 0, 0), k, 700, t, .13); }
    /** Tentaculos: descargas que saltam do corpo para fora do volume e piscam (cada um vive 2 ticks). */
    public static double alphaTentaculo(int k, double t) { double c = ((t + k * 1.7) % 5 + 5) % 5; return c < 2 ? 1 - c / 2 : 0; }
    public static List<P> tentaculo(int k, double t) {
        int q = (int) Math.floor((t + k * 1.7) / 5);
        double a = 2 * Math.PI * Math.abs(r(k, 820, q)), h = .3 + 1.6 * Math.abs(r(k, 821, q)), rr = 1.1 + .6 * Math.abs(r(k, 822, q));
        P de = new P(Math.cos(a) * .35, h, Math.sin(a) * .28);
        P ate = new P(Math.cos(a + r(k, 823, q) * .6) * rr, h + r(k, 824, q) * .6, Math.sin(a) * rr * .8);
        return KilluaBase.raioQuebrado(de, ate, 8, .14, 830 + k, t * 2);
    }
    /** Aneis de relampago orbitando o corpo em alturas diferentes (cada um gira num sentido). */
    public static List<P> anelOrbita(int n, double t) {
        List<P> l = new ArrayList<>();
        double h = .35 + .5 * n, rr = .72 + .08 * Math.sin(t * .4 + n), gir = t * (.25 + .07 * n) * (n % 2 == 0 ? 1 : -1);
        int q = (int) Math.floor(t);
        double inc = .25 * Math.sin(n * 1.3 + t * .05);
        for (int i = 0; i <= 28; i++) {
            if (i > 18) break; // arco aberto (2/3 da volta), parece uma corrente eletrica correndo
            double a = gir + 2 * Math.PI * i / 28;
            double d = (i == 0 || i == 18) ? 0 : r(n * 40 + i, 840, q) * .09;
            l.add(new P(Math.cos(a) * (rr + d), h + Math.sin(a) * inc + r(n * 40 + i, 841, q) * .06, Math.sin(a) * (rr + d) * .8));
        }
        return l;
    }
    /** Descarga ocasional do corpo para o chao (uma a cada 8 ticks, dura 3). */
    public static double alphaDescarga(double t) { if (t < ATIVA + 4) return 0; double c = (t - ATIVA) % 8; return c < 3 ? 1 - c / 3 : 0; }
    public static List<P> descarga(double t) {
        int q = (int) ((t - ATIVA) / 8);
        double a = 2 * Math.PI * Math.abs(r(q, 730, 0)), rr = 1 + Math.abs(r(q, 731, 0));
        P de = new P(Math.cos(a) * .35, .9 + .6 * Math.abs(r(q, 732, 0)), Math.sin(a) * .3);
        return KilluaBase.raioQuebrado(de, new P(Math.cos(a) * rr, .02, Math.sin(a) * rr), 9, .22, 740 + q, t);
    }
    /** Faiscas que flutuam em volta dele enquanto a forma dura. */
    public static List<double[]> faiscasAura(double t) {
        List<double[]> l = new ArrayList<>();
        if (t < ATIVA) return l;
        int ciclo = (int) ((t - ATIVA) / 4);
        for (int c = Math.max(0, ciclo - 3); c <= ciclo; c++)
            l.addAll(KilluaBase.particulas(760 + c, 9, new P(r(c, 761, 0) * .5, .4 + 1.4 * Math.abs(r(c, 762, 0)), r(c, 763, 0) * .4), ATIVA + c * 4, t, .14, .12, new P(0, .4, 0), .002, 14));
        return l;
    }
    /** Brilho do corpo (o modelo fica branco-azulado): sobe no estouro e assenta em 0,35. */
    public static double brilhoCorpo(double t) {
        if (t < ATIVA - 2) return .25 * alphaCarga(t);
        double a = t - ATIVA;
        return a < 0 ? .3 : .35 + .65 * (1 - s(lim(a / 8)));
    }

    private KilluaGodspeed() {}
}
