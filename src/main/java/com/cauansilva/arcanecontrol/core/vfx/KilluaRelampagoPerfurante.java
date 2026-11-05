package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;

import java.util.ArrayList;
import java.util.List;

/**
 * G-Z Relampago Perfurante (modo Godspeed): Killua agacha e a eletricidade junta nos pes; vira um raio e atravessa o
 * alvo em linha reta (ALCANCE blocos em DISPARO ticks), deixando uma lanca de raios no caminho. O alvo e furado com
 * espinhos radiais e uma onda, e fica preso em arcos de paralisia; o rastro some com galhos e queimadura no chao.
 * Coordenadas locais: pes em (0,0,0) olhando para +z; o alvo fica em KilluaBase.ALVO.
 */
public final class KilluaRelampagoPerfurante {
    public static final int PREPARO = 7, DISPARO = 4, FIM_DISPARO = PREPARO + DISPARO, FIM = 40;
    public static final double ALCANCE = 9.5, ALTURA = 1.0;
    public static final int FIOS = 4, GALHOS = 10, ESPINHOS = 14, ARCOS_ALVO = 6, ARCOS_PES = 8;
    /** Tick em que o raio passa pelo alvo (golpe/dano). */
    public static final double GOLPE = PREPARO + DISPARO * KilluaBase.ALVO / ALCANCE;

    private static double s(double t) { return PerfilVfxGojo.suave(t); }
    private static double lim(double v) { return Math.max(0, Math.min(1, v)); }
    private static double r(int a, int b, int c) { return KilluaBase.ruido(a, b, c); }

    /** Pes do Killua (agacha no preparo, dispara, desliza e freia no fim). */
    public static P corpo(double t) {
        if (t < PREPARO) return new P(0, 0, -.25 * s(t / PREPARO));
        if (t < FIM_DISPARO) return new P(0, 0, -.25 + (ALCANCE + .25) * (t - PREPARO) / DISPARO);
        return new P(0, 0, ALCANCE + .8 * s((t - FIM_DISPARO) / 6));
    }
    /** 0 = corpo vira raio (invisivel), 1 = corpo normal. */
    public static double alphaCorpo(double t) { return t < PREPARO - 1 ? 1 : t < FIM_DISPARO ? .55 : .55 + .45 * lim((t - FIM_DISPARO) / 2); }

    // ---- preparo
    public static double alphaPreparo(double t) { return t > FIM_DISPARO ? 0 : s(t / 4); }
    /** Arco que corre do chao ate os pes/pernas, cada vez mais curto (eletricidade juntando). */
    public static List<P> arcoPes(int k, double t) {
        double u = s(t / PREPARO), a = 2 * Math.PI * (k + .3 * r(k, 1, (int) t)) / ARCOS_PES, rr = 1.6 - 1.1 * u;
        P c = corpo(t), de = c.mais(new P(Math.cos(a) * rr, .02, Math.sin(a) * rr));
        P ate = c.mais(new P(Math.cos(a) * .25, .2 + .7 * Math.abs(r(k, 2, (int) t)), Math.sin(a) * .2));
        return KilluaBase.raioQuebrado(de, ate, 6, .18, 2000 + k, t * 2);
    }
    /** Ponta do raio que se forma na frente da mao (cresce no preparo). */
    public static List<P> ponta(double t) {
        double u = s((t - 2) / (PREPARO - 2));
        P mao = corpo(t).mais(new P(.1, ALTURA + .15, .55));
        return KilluaBase.raioQuebrado(mao, mao.mais(new P(0, 0, .2 + 1.1 * u)), 4, .06, 2050, t * 2);
    }

    // ---- disparo: lanca de raios do ponto de partida ate a frente do corpo
    public static double frenteRaio(double t) { return t < PREPARO ? -.25 : Math.min(ALCANCE, corpo(t).z() + .6); }
    public static double alphaRastro(double t) { return t < PREPARO ? 0 : t < FIM_DISPARO + 3 ? 1 : Math.max(0, 1 - (t - FIM_DISPARO - 3) / 16); }
    /** Fio k da lanca (o 0 e o nucleo grosso); entre a partida e a frente atual; troca de forma a cada tick. */
    public static List<P> fio(int k, double t) {
        double f = frenteRaio(t), espalha = k == 0 ? .12 : .32 + .12 * k;
        double abre = t > FIM_DISPARO ? 1 + .6 * s((t - FIM_DISPARO) / 12) : 1;
        int n = Math.max(3, (int) Math.round((f + .25) / .45));
        List<P> l = new ArrayList<>();
        int q = (int) Math.floor(t);
        for (int i = 0; i <= n; i++) {
            double u = i / (double) n, env = Math.sin(Math.PI * u) * .7 + .3 * (i == 0 || i == n ? 0 : 1);
            l.add(new P(r(k, i, q + 3000) * espalha * abre * env, ALTURA + r(k + 9, i, q + 3000) * espalha * abre * env, -.25 + (f + .25) * u));
        }
        return l;
    }
    /** Galho k saindo do nucleo para fora (aparece depois da passagem). */
    public static List<P> galho(int k, double t) {
        int q = (int) Math.floor(t / 2);
        double z = .5 + (ALCANCE - 1) * Math.abs(r(k, 40, 7)), a = 2 * Math.PI * Math.abs(r(k, 41, q)), comp = .6 + .9 * Math.abs(r(k, 42, q));
        P de = new P(0, ALTURA, z), ate = de.mais(new P(Math.cos(a) * comp, Math.sin(a) * comp * .8, r(k, 43, q) * .5));
        if (ate.y() < .03) ate = new P(ate.x(), .03, ate.z());
        return KilluaBase.raioQuebrado(de, ate, 5, .14, 2100 + k * 3 + q, 0);
    }
    public static boolean galhoVisivel(int k, double t) { return frenteRaio(t) > .5 + (ALCANCE - 1) * Math.abs(r(k, 40, 7)); }
    /** Risco queimado no chao ao longo do caminho {de, ate} (some devagar). */
    public static double alphaQueimado(double t) { return t < PREPARO ? 0 : Math.max(0, 1 - Math.max(0, t - FIM_DISPARO - 6) / 24); }

    /** Marca queimada irregular no chao (fixa): pontos {x, z, largura} ate a frente atual do raio. */
    public static List<double[]> marca(double t) {
        List<double[]> l = new ArrayList<>();
        double f = frenteRaio(t);
        for (int i = 0; i <= 40; i++) {
            double z = -.25 + (ALCANCE + .25) * i / 40.0;
            if (z > f) break;
            l.add(new double[]{r(i, 2500, 0) * .12 + .06 * Math.sin(z * 2.3), z, .08 + .1 * Math.abs(r(i, 2501, 0))});
        }
        return l;
    }
    /** Brilho da marca: azul quente logo apos a passagem, esfria para preto. */
    public static double calorMarca(double t) { return t < PREPARO ? 0 : Math.max(0, 1 - Math.max(0, t - FIM_DISPARO) / 10); }
    /** Fumaca fina subindo da marca. */
    public static List<double[]> fumacaMarca(double t) {
        List<double[]> l = new ArrayList<>();
        for (int k = 0; k < 6; k++) l.addAll(KilluaBase.particulas(2600 + k, 4, new P(0, .03, ALCANCE * (k + .5) / 6), FIM_DISPARO + k, t, .03, .02, new P(0, 1, 0), -.0005, 24));
        return l;
    }
    /** Brasas que estalam da marca enquanto ainda esta quente. */
    public static List<double[]> brasasMarca(double t) {
        List<double[]> l = new ArrayList<>();
        for (int k = 0; k < 5; k++) l.addAll(KilluaBase.particulas(2700 + k, 5, new P(0, .03, ALCANCE * (k + .3) / 5), PREPARO + 1 + k * 2, t, .18, .1, new P(0, 1, 0), .02, 9));
        return l;
    }

    // ---- impacto no alvo
    public static double alphaImpacto(double t) { double a = t - GOLPE; return a < 0 || a > 12 ? 0 : Math.min(1, a * 2 + .4) * (1 - s((a - 2) / 10)); }
    public static P centroAlvo() { return new P(0, ALTURA + .1, KilluaBase.ALVO); }
    /** Espinho k que estoura do alvo para fora (cresce e encolhe). */
    public static List<P> espinho(int k, double t) {
        double a = t - GOLPE, cresce = s(a / 2.5);
        double th = 2 * Math.PI * k / ESPINHOS + r(k, 60, 0) * .3, fi = r(k, 61, 0) * .9;
        double comp = (1.2 + 1.4 * Math.abs(r(k, 62, 0))) * cresce;
        P c = centroAlvo(), d = new P(Math.cos(th) * Math.cos(fi), Math.sin(fi) + .15, Math.sin(th) * Math.cos(fi) * .5);
        return KilluaBase.raioQuebrado(c, c.mais(d.vezes(comp)), 5, .2, 2200 + k, t);
    }
    /** Onda (anel) atravessada pelo raio, no plano xy, abrindo a partir do alvo; camada 0..1. */
    public static List<P> onda(int camada, double t) {
        double a = t - GOLPE - camada * 1.5;
        return KilluaBase.anel(centroAlvo().mais(new P(0, 0, camada * .4)), KilluaBase.X, KilluaBase.Y, .3 + 2.4 * s(a / 8), 32, t * .2);
    }
    public static double alphaOnda(int camada, double t) { double a = t - GOLPE - camada * 1.5; return a < 0 || a > 9 ? 0 : 1 - s(a / 9); }
    /** Paralisia no alvo: arcos no corpo dele por 1 s. */
    public static double alphaParalisia(double t) { double a = t - GOLPE; return a < 0 || a > 22 ? 0 : 1 - s((a - 14) / 8); }
    public static List<P> arcoAlvo(int k, double t) { return KilluaBase.arcoNoCorpo(new P(0, 0, KilluaBase.ALVO), k, 2300, t, .15); }
    public static List<double[]> faiscasImpacto(double t) {
        return KilluaBase.particulas(2400, 40, centroAlvo(), GOLPE, t, .45, .3, new P(0, .2, .6), .012, 16);
    }
    /** Freio no fim: faiscas e poeira saindo dos pes enquanto ele desliza. */
    public static List<double[]> faiscasFreio(double t) {
        return KilluaBase.particulas(2450, 26, new P(0, .05, ALCANCE), FIM_DISPARO, t, .3, .18, new P(0, .5, .3), .015, 12);
    }
}
