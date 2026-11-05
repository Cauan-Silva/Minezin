package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.client.tecido.MalhaAura;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;

import java.util.ArrayList;
import java.util.List;

/**
 * Aura do Godspeed feita de tecido de energia (padrao de auras): LINGUAS faixas largas de {@link MalhaAura}
 * presas em volta do tronco, bracos e cintura, que sobem, ondulam e arrastam atras do Killua quando ele corre ou
 * salta. Por cima, raios correm pelas bordas das linguas. Um objeto por jogador; tick() a 20 Hz com a pose do mundo.
 * Nao mexe no cabelo.
 */
public final class AuraTecidoKillua {
    public static final int LINGUAS = 33, GRANDES = 18, COLUNAS = 3;
    public static final double PASSO_X = .28;
    /** Ancoras: {angulo (rad, 0 = frente), altura, raio horizontal, linhas}. */
    static final double[][] ANCORA = {
            {0.5, 1.5, .62, 15}, {1.6, 1.55, .66, 16}, {2.6, 1.5, .62, 15}, {3.6, 1.5, .62, 15}, {4.7, 1.55, .66, 16}, {5.7, 1.5, .62, 15},
            {0.0, 1.05, .6, 13}, {1.25, 1.0, .64, 14}, {2.5, 1.05, .6, 13}, {3.8, 1.05, .6, 13}, {5.0, 1.0, .64, 14},
            {0.6, .45, .62, 12}, {1.9, .4, .66, 12}, {3.14, .45, .62, 12}, {4.4, .4, .66, 12}, {5.6, .45, .62, 12},
            {1.0, .08, .75, 10}, {4.2, .08, .75, 10},
            {1.05, 1.3, .55, 8}, {2.1, 1.3, .55, 8}, {3.1, 1.35, .55, 9}, {4.15, 1.3, .55, 8}, {5.2, 1.3, .55, 8}, {0.0, 1.35, .55, 9},
            {0.6, .8, .55, 7}, {1.9, .75, .58, 7}, {3.15, .8, .55, 7}, {4.4, .75, .58, 7}, {5.6, .8, .55, 7},
            {0.8, 1.85, .4, 9}, {2.4, 1.9, .4, 9}, {3.9, 1.85, .4, 9}, {5.4, 1.9, .4, 9}};
    /** Envelope da aura em volta do corpo: centro 1,25 acima dos pes; topo arredondado em 1,25 + RV. */
    static final double RH = 1.3, RV = 1.6, CENTRO = 1.25;
    /** Amplitude dos lobos do envelope (silhueta irregular que muda o tempo todo). */
    public static final double DEFORMA = .32;
    /** Forca que abre as linguas para fora (aura larga em volta do corpo, nao colada). */
    static final double ABERTURA = 7;

    private final MalhaAura[] malhas = new MalhaAura[LINGUAS];
    private double[][][] antes;
    private boolean iniciada;
    private P pesAntes, pesAtual = new P(0, 0, 0);
    private double tempo;
    private double vx, vz, envX, envY, envZ, envRH = RH;

    public AuraTecidoKillua() {
        for (int n = 0; n < LINGUAS; n++) malhas[n] = new MalhaAura(COLUNAS, (int) ANCORA[n][3], n < GRANDES ? PASSO_X : PASSO_X * .6, n < GRANDES ? .17 : .14, MalhaAura.PerfilAura.ELETRICA, 31 + n * 7);
    }

    /** Bases das linguas no mundo para o corpo em pes (x, y, z) olhando para +z (yaw em radianos). */
    static double[][] ancoras(int n, P pes, double yaw) {
        double a = ANCORA[n][0], h = ANCORA[n][1], r = ANCORA[n][2];
        double[][] l = new double[COLUNAS][3];
        for (int c = 0; c < COLUNAS; c++) {
            double aa = a + (c - (COLUNAS - 1) / 2.0) * PASSO_X / r;
            double lx = Math.sin(aa) * r, lz = Math.cos(aa) * r * .8;
            double cy = Math.cos(yaw), sy = Math.sin(yaw);
            l[c][0] = pes.x() + lx * cy + lz * sy; l[c][1] = pes.y() + h; l[c][2] = pes.z() - lx * sy + lz * cy;
        }
        return l;
    }

    /** Um tick: pes/yaw do corpo e o crescimento (0..1) da aura. */
    public void tick(P pes, double yaw, double crescer) {
        double[][][] agora = new double[LINGUAS][][];
        for (int n = 0; n < LINGUAS; n++) agora[n] = ancoras(n, pes, yaw);
        if (!iniciada) { for (int n = 0; n < LINGUAS; n++) malhas[n].reset(agora[n]); antes = agora; iniciada = true; }
        double cy = Math.cos(yaw), sy = Math.sin(yaw);
        // o envelope estica para tras com a velocidade: parado e arredondado, correndo vira uma gota que arrasta
        if (pesAntes != null) { vx += ((pes.x() - pesAntes.x()) - vx) * .35; vz += ((pes.z() - pesAntes.z()) - vz) * .35; }
        pesAntes = pes;
        double vel = Math.hypot(vx, vz);
        envX = pes.x() - vx * 1.3; envY = pes.y() + CENTRO + Math.min(.35, vel * .8); envZ = pes.z() - vz * 1.3; envRH = RH + vel * .8;
        pesAtual = pes; tempo += .05;
        for (int n = 0; n < LINGUAS; n++) {
            double lx = Math.sin(ANCORA[n][0]), lz = Math.cos(ANCORA[n][0]);
            malhas[n].abertura(ABERTURA * (lx * cy + lz * sy), ABERTURA * (-lx * sy + lz * cy));
            malhas[n].envelope(envX, envY, envZ, envRH, RV);
            malhas[n].deformacao(DEFORMA, .85 + .5 * Math.abs(KilluaBase.ruido(n, 1200, 0)));
            malhas[n].impulso(1 + Math.min(1.2, vel * 3));
            malhas[n].crescer(crescer); malhas[n].tick(antes[n], agora[n]);
        }
        antes = agora;
    }

    /**
     * Raio k que corre EM VOLTA da aura (referencia do anime): nasce num ponto da superficie deformada do envelope e
     * percorre 60 a 150 graus ao redor dela subindo ou descendo, com quinas fortes, colado a casca (0,92 a 1,08 do
     * raio). Troca de forma a cada 2 ticks e fica aceso so parte do ciclo (alphaExplosivo).
     */
    public List<P> raioExplosivo(int k, double t) {
        int q = (int) Math.floor((t + k * .7) / 2);
        double az0 = 2 * Math.PI * Math.abs(KilluaBase.ruido(k, 1100, q)), varre = (1.05 + 1.6 * Math.abs(KilluaBase.ruido(k, 1101, q))) * (KilluaBase.ruido(k, 1104, q) > 0 ? 1 : -1);
        double y0 = -.6 + 1.3 * Math.abs(KilluaBase.ruido(k, 1102, q)), dy = KilluaBase.ruido(k, 1103, q) * .9;
        List<P> l = new ArrayList<>();
        int n = 9;
        for (int i = 0; i <= n; i++) {
            double u = i / (double) n, az = az0 + varre * u, ey = Math.max(-.95, Math.min(.95, y0 + dy * u));
            double quina = i == 0 || i == n ? 0 : KilluaBase.ruido(k * 31 + i, 1105, q);
            ey = Math.max(-.97, Math.min(.97, ey + quina * .14));
            double horiz = Math.sqrt(1 - ey * ey), dx = Math.cos(az) * horiz, dz = Math.sin(az) * horiz;
            double r = MalhaAura.lobo(dx, ey, dz, tempo, DEFORMA, 0) * (1 + quina * .08);
            l.add(new P(envX + dx * envRH * r, envY + ey * RV * r, envZ + dz * envRH * r));
        }
        return l;
    }
    /** Galho do raio k: curto, sai de uma quina e segue a superficie por um trecho. */
    public List<P> galhoExplosivo(int k, double t) {
        List<P> r = raioExplosivo(k, t);
        int q = (int) Math.floor((t + k * .7) / 2);
        int i = 2 + Math.abs((int) (KilluaBase.ruido(k, 1120, q) * 5)) % 5;
        P a = r.get(i), b = r.get(Math.min(r.size() - 1, i + 2));
        P fim = b.mais(new P(KilluaBase.ruido(k, 1121, q) * .25, KilluaBase.ruido(k, 1122, q) * .35, KilluaBase.ruido(k, 1123, q) * .25));
        return KilluaBase.raioQuebrado(a, fim, 4, .1, 1130 + k * 7 + q, 0);
    }
    public static double alphaExplosivo(int k, double t) { double c = ((t + k * .7) % 2 + 2) % 2; return c < 1.4 ? 1 : 0; }
    public static final int EXPLOSIVOS = 12;

    /** Envelope atual {cx, cy, cz, raioHorizontal, raioVertical} (o render desenha o brilho interno nele). */
    public double[] envelope() { return new double[]{envX, envY, envZ, envRH, RV}; }

    /** Grade da lingua n: [linha][coluna] -> ponto do mundo. */
    public List<List<P>> lingua(int n) {
        List<List<P>> g = new ArrayList<>();
        MalhaAura m = malhas[n];
        for (int y = 0; y < m.linhas(); y++) {
            List<P> l = new ArrayList<>();
            for (int x = 0; x < m.colunas(); x++) { double[] p = m.ponto(x, y, 1); l.add(new P(p[0], p[1], p[2])); }
            g.add(l);
        }
        return g;
    }
    public double[] pontoArr(int n, int x, int y) { return malhas[n].ponto(x, y, 1); }

    /** Raio que corre pela borda (coluna 0 ou 2) da lingua n, de uma linha a outra mais acima; troca a cada tick. */
    public List<P> raioBorda(int n, int k, double t) {
        MalhaAura m = malhas[n];
        int q = (int) Math.floor(t);
        int borda = Math.abs((int) (KilluaBase.ruido(n, k, q) * 7)) % 2 == 0 ? 0 : COLUNAS - 1;
        int y0 = Math.abs((int) (KilluaBase.ruido(n, k + 50, q) * 9)) % Math.max(1, m.linhas() - 4);
        int y1 = Math.min(m.linhas() - 1, y0 + 3 + Math.abs((int) (KilluaBase.ruido(n, k + 90, q) * 4)) % 3);
        double[] a = m.ponto(borda, y0, 1), b = m.ponto(borda, y1, 1);
        return KilluaBase.raioQuebrado(new P(a[0], a[1], a[2]), new P(b[0], b[1], b[2]), 6, .07, 900 + n * 11 + k, t * 2);
    }
    /** Arco entre as pontas de duas linguas vizinhas (fecha o volume com eletricidade). */
    public List<P> raioPonte(int n, double t) {
        if (n >= GRANDES) n = n % GRANDES;
        int o = n < 6 ? (n + 1) % 6 : n < 11 ? 6 + (n - 5) % 5 : n < 16 ? 11 + (n - 10) % 5 : 16 + (n - 15) % 2;
        MalhaAura a = malhas[n], b = malhas[Math.min(LINGUAS - 1, o)];
        double[] p = a.ponto(1, a.linhas() * 2 / 3, 1), r = b.ponto(1, b.linhas() * 2 / 3, 1);
        return KilluaBase.raioQuebrado(new P(p[0], p[1], p[2]), new P(r[0], r[1], r[2]), 7, .12, 980 + n, t * 2);
    }
}
