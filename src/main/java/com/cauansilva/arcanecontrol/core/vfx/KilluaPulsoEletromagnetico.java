package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;

import java.util.ArrayList;
import java.util.List;

/**
 * G-V Pulso Eletromagnetico (Godspeed): Killua cruza os bracos e sobe um pouco; linhas de campo magnetico (lacos de
 * dipolo) apertam em volta dele e um nucleo cresce no peito. Em PULSO ele abre os bracos e solta uma esfera de raios
 * que cresce ate RAIO blocos, com onda no chao; cada inimigo alcancado leva um raio, e interrompido (FORTE) e fica
 * atordoado. Coordenadas locais: Killua em (0,0,0); ALVOS ao redor.
 */
public final class KilluaPulsoEletromagnetico {
    public static final Interrupcao.Forca FORCA = Interrupcao.Forca.FORTE;
    public static final int CARGA = 16, PULSO = CARGA, EXPANSAO = 9, FIM = 46;
    public static final double RAIO = 7, PEITO = 1.25;
    public static final int LINHAS = 10, ARCOS_CASCA = 18, ARCOS_ALVO = 5;
    /** Inimigos da previa: {angulo, distancia}. */
    public static final double[][] ALVOS = {{.3, 3.4}, {2.4, 4.7}, {4.3, 4.3}};

    private static double s(double t) { return PerfilVfxGojo.suave(t); }
    private static double lim(double v) { return Math.max(0, Math.min(1, v)); }
    private static double r(int a, int b, int c) { return KilluaBase.ruido(a, b, c); }

    public static P centro() { return new P(0, PEITO, 0); }
    /** Raio atual da esfera de pulso. */
    public static double raioPulso(double t) { return t < PULSO ? 0 : RAIO * s((t - PULSO) / EXPANSAO); }
    public static double alphaPulso(double t) { double a = t - PULSO; return a < 0 || a > EXPANSAO + 8 ? 0 : Math.min(1, a + .5) * (1 - s((a - EXPANSAO + 3) / 10)); }
    public static P alvo(int i) { return new P(Math.sin(ALVOS[i][0]) * ALVOS[i][1], 0, Math.cos(ALVOS[i][0]) * ALVOS[i][1]); }
    /** Tick em que a esfera alcanca o alvo i (golpe/interrupcao). */
    public static double toque(int i) {
        double alvo = ALVOS[i][1] / RAIO;
        for (double t = PULSO; t < PULSO + EXPANSAO + 1; t += .25) if (raioPulso(t) / RAIO >= alvo) return t;
        return PULSO + EXPANSAO;
    }

    // ---- carga
    public static double alphaCarga(double t) { return t > PULSO + 2 ? 0 : s(t / 4) * (t < PULSO ? 1 : 1 - (t - PULSO) / 2); }
    /** Linha de campo k: laco de dipolo r = L sen^2(th) num plano vertical girando; L encolhe na carga. */
    public static List<P> linhaCampo(int k, double t) {
        double u = s(t / CARGA), L = 3.0 - 1.8 * u, az = 2 * Math.PI * k / LINHAS + t * .12 * (k % 2 == 0 ? 1 : -1);
        List<P> l = new ArrayList<>();
        int q = (int) Math.floor(t);
        for (int i = 0; i <= 18; i++) {
            double th = .25 + (Math.PI - .5) * i / 18.0, rr = L * Math.sin(th) * Math.sin(th);
            double j = i == 0 || i == 18 ? 0 : r(k * 31 + i, 4000, q) * .05;
            l.add(new P(Math.cos(az) * rr * Math.sin(th) * (1 + j), PEITO + rr * Math.cos(th) * 1.1 + j, Math.sin(az) * rr * Math.sin(th) * (1 + j)));
        }
        return l;
    }
    /** Raio do nucleo no peito (cresce na carga, estoura no pulso). */
    public static double raioNucleo(double t) { return t < PULSO ? .08 + .3 * s(t / CARGA) : .38 * (1 - s((t - PULSO) / 3)); }
    /** Arcos pequenos que pulam entre o nucleo e as maos cruzadas. */
    public static List<P> arcoNucleo(int k, double t) {
        int q = (int) Math.floor(t);
        double a = 2 * Math.PI * Math.abs(r(k, 4050, q)), e = r(k, 4051, q) * .9, c = .45 + .3 * Math.abs(r(k, 4052, q));
        P de = centro(), ate = de.mais(new P(Math.cos(a) * Math.cos(e) * c, Math.sin(e) * c, Math.sin(a) * Math.cos(e) * c));
        return KilluaBase.raioQuebrado(de, ate, 4, .08, 4060 + k, t * 2);
    }

    // ---- pulso
    /** Arco k que corre na superficie da esfera de pulso (casca eletrica). */
    public static List<P> arcoCasca(int k, double t) {
        int q = (int) Math.floor(t);
        double R = raioPulso(t), az0 = 2 * Math.PI * Math.abs(r(k, 4100, q)), varre = .5 + .7 * Math.abs(r(k, 4101, q));
        double el0 = -.1 + 1.2 * Math.abs(r(k, 4102, q)), del = r(k, 4103, q) * .5;
        List<P> l = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            double u = i / 8.0, az = az0 + varre * u, el = Math.min(1.45, el0 + del * u), j = i == 0 || i == 8 ? 0 : r(k * 19 + i, 4104, q) * .05;
            double rr = R * (1 + j);
            l.add(new P(Math.cos(az) * Math.cos(el) * rr, Math.max(.03, PEITO + Math.sin(el) * rr * .85), Math.sin(az) * Math.cos(el) * rr));
        }
        return l;
    }
    /** Onda no chao (anel) acompanhando a esfera; camada 1 vem logo atras. */
    public static List<P> onda(int camada, double t) {
        double R = raioPulso(t - camada * 1.5);
        return KilluaBase.anel(new P(0, .03, 0), KilluaBase.X, KilluaBase.Z, Math.max(.1, R * (camada == 0 ? .98 : .9)), 56, t * .1);
    }
    /** Raio que salta da casca para o alvo i quando ela o alcanca. */
    public static double alphaAlvo(int i, double t) { double a = t - toque(i); return a < 0 || a > 6 ? 0 : 1 - a / 6; }
    public static List<P> raioAlvo(int i, double t) {
        P a = alvo(i), de = new P(a.x() * .7, PEITO + 1.6, a.z() * .7), ate = a.mais(new P(0, 1.1, 0));
        return KilluaBase.raioQuebrado(de, ate, 6, .25, 4200 + i * 5, t * 2);
    }
    public static double alphaAtordoado(int i, double t) { double a = t - toque(i); return a < 0 || a > 26 ? 0 : 1 - s((a - 18) / 8); }
    public static List<P> arcoAlvo(int i, int k, double t) { return KilluaBase.arcoNoCorpo(alvo(i), k, 4300 + i * 17, t, .14); }
    public static List<double[]> faiscas(double t) {
        List<double[]> l = new ArrayList<>(KilluaBase.particulas(4400, 50, centro(), PULSO, t, .55, .55, new P(0, .05, 0), .01, 16));
        for (int i = 0; i < ALVOS.length; i++) l.addAll(KilluaBase.particulas(4410 + i, 14, alvo(i).mais(new P(0, 1.1, 0)), toque(i), t, .25, .22, new P(0, .25, 0), .012, 12));
        return l;
    }
}
