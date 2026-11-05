package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;

import java.util.ArrayList;
import java.util.List;

/**
 * G-C Soco Relampago (Godspeed): o punho direito junta eletricidade (arcos que convergem e uma esfera que comprime),
 * Killua avanca AVANCO blocos e acerta um soco reto que INTERROMPE (forca FORTE no sistema de interrupcao). O golpe
 * solta um cone de raios para frente, aneis de choque, rachaduras eletricas no chao e arremessa o alvo para tras.
 * Coordenadas locais: Killua em (0,0,0) olhando +z; alvo em ALVO.
 */
public final class KilluaSocoRelampago {
    public static final Interrupcao.Forca FORCA = Interrupcao.Forca.FORTE;
    public static final int CARGA = 8, AVANCO_T = 3, IMPACTO = CARGA + AVANCO_T, FIM = 36;
    public static final double ALVO = 3.2, AVANCO = 2.1;
    public static final int ARCOS_PUNHO = 8, CONE = 12, RACHAS = 7, ARCOS_ALVO = 6;

    private static double s(double t) { return PerfilVfxGojo.suave(t); }
    private static double lim(double v) { return Math.max(0, Math.min(1, v)); }
    private static double r(int a, int b, int c) { return KilluaBase.ruido(a, b, c); }

    public static P corpo(double t) { return new P(0, 0, t < CARGA ? -.15 * s(t / CARGA) : -.15 + (AVANCO + .15) * s((t - CARGA) / AVANCO_T)); }
    /** Punho direito: puxado para tras na carga, estendido no impacto. */
    public static P punho(double t) {
        P c = corpo(t);
        if (t < CARGA) return c.mais(new P(-.38, 1.25, -.35 * s(t / 4)));
        double u = s((t - CARGA) / AVANCO_T);
        return c.mais(new P(-.38 + .3 * u, 1.25 + .1 * u, -.35 + 1.15 * u));
    }
    public static P alvo(double t) {
        if (t < IMPACTO) return new P(0, 0, ALVO);
        double u = lim((t - IMPACTO) / 10);
        return new P(0, 1.2 * Math.sin(Math.PI * u), ALVO + 5.5 * s(u));
    }

    // ---- carga
    public static double alphaCarga(double t) { return t > IMPACTO ? 0 : s(t / 3); }
    public static double raioEsfera(double t) { return t < CARGA ? .16 + .22 * s(t / CARGA) : .38 - .2 * s((t - CARGA) / AVANCO_T); }
    /** Arco k que vem de fora e converge no punho (fica mais curto ate o impacto). */
    public static List<P> arcoPunho(int k, double t) {
        int q = (int) Math.floor(t);
        double u = s(t / CARGA), comp = 1.3 - .9 * u;
        double th = 2 * Math.PI * Math.abs(r(k, 3500, q)), fi = r(k, 3501, q) * 1.2;
        P p = punho(t), de = p.mais(new P(Math.cos(th) * Math.cos(fi) * comp, Math.sin(fi) * comp, Math.sin(th) * Math.cos(fi) * comp));
        return KilluaBase.raioQuebrado(de, p, 5, .12, 3510 + k, t * 2);
    }
    /** Fio de raio que envolve o antebraco (espiral do cotovelo ao punho). */
    public static List<P> espiralBraco(double t) {
        P p = punho(t), cot = p.mais(new P(.05, .05, -.45));
        List<P> l = new ArrayList<>();
        for (int i = 0; i <= 12; i++) {
            double u = i / 12.0, a = u * 4 * Math.PI + t * 1.7;
            P b = P.lerp(cot, p, u);
            l.add(b.mais(new P(Math.cos(a) * .13, Math.sin(a) * .13, 0)));
        }
        return l;
    }

    // ---- impacto
    public static double alphaImpacto(double t) { double a = t - IMPACTO; return a < 0 || a > 12 ? 0 : Math.min(1, a + .5) * (1 - s((a - 2) / 10)); }
    public static P pontoImpacto() { return punho(IMPACTO).mais(new P(0, 0, .25)); }
    /** Raio k do cone que sai para frente do soco (abre 35 graus). */
    public static List<P> cone(int k, double t) {
        double a = t - IMPACTO, cresce = s(a / 2.5);
        double th = 2 * Math.PI * k / CONE + r(k, 3600, 0) * .3, ab = .35 + .3 * Math.abs(r(k, 3601, 0));
        double comp = (2.2 + 1.6 * Math.abs(r(k, 3602, 0))) * cresce;
        P c = pontoImpacto(), d = new P(Math.cos(th) * ab, Math.sin(th) * ab, 1);
        return KilluaBase.raioQuebrado(c, c.mais(d.vezes(comp)), 6, .22, 3610 + k, t);
    }
    /** Anel de choque no plano do soco (xy), andando para frente. */
    public static List<P> anelChoque(int camada, double t) {
        double a = t - IMPACTO - camada * 1.2;
        return KilluaBase.anel(pontoImpacto().mais(new P(0, 0, .6 * s(a / 6) + camada * .3)), KilluaBase.X, KilluaBase.Y, .25 + (1.3 + camada * .4) * s(a / 6), 30, t * .3);
    }
    public static double alphaAnel(int camada, double t) { double a = t - IMPACTO - camada * 1.2; return a < 0 || a > 8 ? 0 : 1 - s(a / 8); }
    /** Rachadura eletrica k no chao, do pe de apoio para frente/lados. */
    public static List<P> racha(int k, double t) {
        double a = t - IMPACTO, cresce = s(a / 3);
        double ang = -.9 + 1.8 * k / (RACHAS - 1) + r(k, 3700, 0) * .15, comp = (1.4 + 1.2 * Math.abs(r(k, 3701, 0))) * cresce;
        P de = new P(0, .03, AVANCO + .2);
        P ate = de.mais(new P(Math.sin(ang) * comp, 0, Math.cos(ang) * comp));
        List<P> l = KilluaBase.raioQuebrado(de, ate, 6, .18, 3710 + k, 0);
        List<P> chao = new ArrayList<>();
        for (P p : l) chao.add(new P(p.x(), .03, p.z()));
        return chao;
    }
    public static double alphaRacha(double t) { double a = t - IMPACTO; return a < 0 ? 0 : Math.max(0, 1 - Math.max(0, a - 6) / 18); }
    public static double alphaParalisia(double t) { double a = t - IMPACTO; return a < 0 || a > 20 ? 0 : 1 - s((a - 12) / 8); }
    public static List<P> arcoAlvo(int k, double t) { return KilluaBase.arcoNoCorpo(alvo(t), k, 3800, t, .15); }
    public static List<double[]> faiscasImpacto(double t) {
        return KilluaBase.particulas(3850, 44, pontoImpacto(), IMPACTO, t, .5, .32, new P(0, .25, .8), .012, 15);
    }
    public static List<double[]> poeira(double t) {
        return KilluaBase.particulas(3870, 18, new P(0, .05, AVANCO), IMPACTO, t, .12, .1, new P(0, .25, 0), .003, 18);
    }
}
