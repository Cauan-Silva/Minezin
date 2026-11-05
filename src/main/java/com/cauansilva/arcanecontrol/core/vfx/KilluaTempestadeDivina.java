package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;

import java.util.ArrayList;
import java.util.List;

/**
 * Ultimate do Killua: Tempestade Divina (Kanmuru). G de novo durante o Godspeed: um raio prende o alvo no ar, Killua
 * teleporta GOLPES vezes em volta dele golpeando de todos os lados; cada golpe deixa uma pos-imagem parada e um raio
 * ate o golpe seguinte, fechando uma gaiola. Depois sobe ao ceu e desce como um relampago gigante (IMPACTO): explosao,
 * cratera, aneis, clarao. Encerra o Godspeed. Coordenadas locais: Killua comeca em (0,0,0), alvo em ALVO.
 */
public final class KilluaTempestadeDivina {
    public static final Interrupcao.Forca FORCA = Interrupcao.Forca.ABSOLUTA;
    public static final int PRENDER = 8, GOLPES = 12, INTERVALO = 3, FIM_GOLPES = PRENDER + GOLPES * INTERVALO;
    public static final int SUBIDA = 10, FIM_SUBIDA = FIM_GOLPES + SUBIDA, MERGULHO = 4, IMPACTO = FIM_SUBIDA + MERGULHO, FIM = IMPACTO + 46;
    public static final double ALVO = 4, ALTURA_PRESO = .7, CEU = 15, RAIO_GOLPE = 1.45, CRATERA = 3.2;
    public static final int COLUNA = 6, ESTOURO = 26, RACHAS = 12;

    private static double s(double t) { return PerfilVfxGojo.suave(t); }
    private static double lim(double v) { return Math.max(0, Math.min(1, v)); }
    private static double r(int a, int b, int c) { return KilluaBase.ruido(a, b, c); }

    /** Centro (peito) do alvo: sobe preso no ar e cai no impacto. */
    public static P alvoCentro(double t) {
        double y = 1.1 + ALTURA_PRESO * (t < IMPACTO ? s(t / PRENDER) : 1 - s((t - IMPACTO) / 3));
        int g = golpeAtual(t);
        double tranco = g >= 0 && t - tickGolpe(g) < 2 ? .12 * (1 - (t - tickGolpe(g)) / 2) : 0;
        P d = g >= 0 ? dirGolpe(g) : new P(0, 0, 0);
        return new P(-d.x() * tranco, y - d.y() * tranco, ALVO - d.z() * tranco);
    }
    public static double tickGolpe(int i) { return PRENDER + i * INTERVALO + 1; }
    public static int golpeAtual(double t) { if (t < tickGolpe(0) - 1 || t >= FIM_GOLPES) return -1; return Math.min(GOLPES - 1, (int) ((t - PRENDER) / INTERVALO)); }
    /** Direcao (unitaria) do ponto de golpe i ao alvo, vinda de um angulo dourado e alturas alternadas. */
    public static P dirGolpe(int i) {
        double az = i * 2.39996 + .4, el = (i % 3 - 1) * .55 + r(i, 5000, 0) * .15;
        return new P(Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el));
    }
    /** Ponto (peito do Killua) do golpe i. */
    public static P pontoGolpe(int i) { P c = alvoCentro(PRENDER), d = dirGolpe(i); return new P(c.x() + d.x() * RAIO_GOLPE, c.y() + d.y() * RAIO_GOLPE, c.z() + d.z() * RAIO_GOLPE); }
    /** Tipo do golpe i (0 soco direito, 1 chute alto, 2 cotovelada, 3 joelhada): escolhe a pose. */
    public static int tipoGolpe(int i) { return i % 4; }
    /** Yaw (graus) para o Killua no ponto i olhar o alvo. */
    public static double yawGolpe(int i) { P d = dirGolpe(i); return Math.toDegrees(Math.atan2(-d.x(), -d.z())); }

    /** Pes do Killua. */
    public static P corpo(double t) {
        if (t < PRENDER) return new P(0, 0, -.2 * s(t / PRENDER));
        int g = golpeAtual(t);
        if (g >= 0) { P p = pontoGolpe(g); return new P(p.x(), p.y() - 1.2, p.z()); }
        if (t < FIM_SUBIDA) { P p = pontoGolpe(GOLPES - 1), de = new P(p.x(), p.y() - 1.2, p.z()); return P.lerp(de, new P(0, CEU, ALVO), s((t - FIM_GOLPES) / SUBIDA)); }
        if (t < IMPACTO) return new P(0, CEU - (CEU - .6) * Math.pow((t - FIM_SUBIDA) / MERGULHO, 2), ALVO);
        return new P(0, 0, ALVO - .9 * s((t - IMPACTO) / 4));
    }
    public static double yaw(double t) { int g = golpeAtual(t); return g >= 0 ? yawGolpe(g) : 0; }
    /** Corpo visivel so no instante do golpe; vira raio entre teleportes, na subida e no mergulho. */
    public static double alphaCorpo(double t) {
        if (t < PRENDER) return 1;
        int g = golpeAtual(t);
        if (g >= 0) { double a = t - tickGolpe(g); return a < -1 ? .1 : a < 1.5 ? 1 : .35; }
        if (t < IMPACTO) return .2;
        return lim((t - IMPACTO - 2) / 4);
    }

    // ---- prender
    public static double alphaPrisao(double t) { return t < 1 || t > IMPACTO ? 0 : s((t - 1) / 3); }
    /** Raio que prende o alvo: da mao do Killua ate o alvo no inicio, depois aneis eletricos em volta dele. */
    public static List<P> raioPrender(double t) {
        P de = corpo(t).mais(new P(-.3, 1.3, .4)), ate = alvoCentro(t);
        return KilluaBase.raioQuebrado(de, ate, 7, .2, 5010, t * 2);
    }
    public static double alphaRaioPrender(double t) { return t < 1 || t > PRENDER + 1 ? 0 : 1 - lim((t - PRENDER) / 1.5); }
    public static List<P> anelPrisao(int k, double t) {
        P c = alvoCentro(t);
        double inc = .5 + k * .9, f = t * (.35 + .1 * k);
        return KilluaBase.anel(c, new P(Math.cos(inc), 0, Math.sin(inc)), new P(0, 1, 0), .55 + .08 * k, 22, f);
    }

    // ---- gaiola
    /** Segmento i da gaiola (do golpe i-1 ao golpe i; o 0 vem do ponto de partida). Aparece quando o golpe i acontece. */
    public static boolean segmentoVisivel(int i, double t) { return t >= tickGolpe(i) - 1 && t < IMPACTO + 3; }
    public static List<P> segmentoGaiola(int i, double t) {
        P de = i == 0 ? new P(0, 1.2, 0) : pontoGolpe(i - 1);
        return KilluaBase.raioQuebrado(de, pontoGolpe(i), 7, .22, 5100 + i * 3, t);
    }
    public static double alphaGaiola(double t) { return t < IMPACTO - 2 ? 1 : Math.max(0, 1 - (t - IMPACTO + 2) / 5); }
    /** Pos-imagem i (parada no ponto do golpe): alpha. */
    public static double alphaFantasma(int i, double t) { double a = t - tickGolpe(i); return a < 1.5 || t > IMPACTO ? 0 : .2 * Math.max(.3, 1 - a / 18); }
    /** Estrela de impacto de cada golpe no alvo. */
    public static double alphaAcerto(int i, double t) { double a = t - tickGolpe(i); return a < 0 || a > 4 ? 0 : 1 - a / 4; }
    public static List<P> raioAcerto(int i, int k, double t) {
        P c = alvoCentro(tickGolpe(i)).mais(dirGolpe(i).vezes(.35));
        double th = 2 * Math.PI * k / 6 + r(i, k + 5200, 0), comp = .6 + .4 * Math.abs(r(i, k + 5201, 0));
        P d = dirGolpe(i).vezes(-.6).mais(new P(Math.cos(th) * .7, Math.sin(th) * .7, Math.sin(th + 1) * .4));
        return KilluaBase.raioQuebrado(c, c.mais(d.vezes(comp * s((t - tickGolpe(i)) / 1.5))), 3, .1, 5210 + i * 7 + k, t);
    }

    // ---- subida e mergulho
    public static double alphaSubida(double t) { return t < FIM_GOLPES || t > IMPACTO ? 0 : 1; }
    public static List<P> rastroSubida(double t) {
        P p = pontoGolpe(GOLPES - 1);
        return KilluaBase.raioQuebrado(p, corpo(t).mais(new P(0, 1, 0)), 9, .3, 5300, t * 2);
    }
    /** Nuvem/clarao no ceu que se forma antes do mergulho. */
    public static double alphaCeu(double t) { return t < FIM_GOLPES || t > IMPACTO + 10 ? 0 : s((t - FIM_GOLPES) / SUBIDA) * (t < IMPACTO ? 1 : 1 - (t - IMPACTO) / 10); }
    /** Fio k da coluna gigante (ceu -> alvo) no mergulho. */
    public static double alphaColuna(double t) { return t < FIM_SUBIDA - 1 ? 0 : t < IMPACTO + 3 ? 1 : Math.max(0, 1 - (t - IMPACTO - 3) / 9); }
    public static List<P> fioColuna(int k, double t) {
        double f = t < IMPACTO ? lim((t - FIM_SUBIDA + 1) / (MERGULHO + 1)) : 1, esp = k == 0 ? .25 : .7 + .25 * k;
        P de = new P(0, CEU + 2, ALVO), ate = new P(0, CEU + 2 - (CEU + 2) * f, ALVO);
        int q = (int) Math.floor(t);
        List<P> l = new ArrayList<>();
        for (int i = 0; i <= 14; i++) {
            double u = i / 14.0, env = i == 0 || i == 14 ? 0 : 1;
            P b = P.lerp(de, ate, u);
            l.add(b.mais(new P(r(k, i, q + 5400) * esp * env, 0, r(k + 9, i, q + 5400) * esp * env)));
        }
        return l;
    }

    // ---- impacto
    public static double alphaImpacto(double t) { double a = t - IMPACTO; return a < 0 || a > 16 ? 0 : Math.min(1, a + .5) * (1 - s((a - 3) / 13)); }
    public static double clarao(double t) { double a = t - IMPACTO; return a < 0 || a > 5 ? 0 : 1 - a / 5; }
    public static P chao() { return new P(0, .05, ALVO); }
    public static double raioExplosao(double t) { return 4.5 * s((t - IMPACTO) / 7); }
    public static List<P> raioEstouro(int k, double t) {
        double th = 2 * Math.PI * k / ESTOURO + r(k, 5500, 0) * .2, el = .1 + .9 * Math.abs(r(k, 5501, 0));
        double comp = (3 + 3 * Math.abs(r(k, 5502, 0))) * s((t - IMPACTO) / 3);
        P c = chao().mais(new P(0, .4, 0)), d = new P(Math.cos(th) * Math.cos(el), Math.sin(el), Math.sin(th) * Math.cos(el));
        return KilluaBase.raioQuebrado(c, c.mais(d.vezes(comp)), 6, .3, 5510 + k, t);
    }
    public static List<P> anelChoque(int camada, double t) {
        double a = t - IMPACTO - camada * 2;
        return KilluaBase.anel(chao(), KilluaBase.X, KilluaBase.Z, .5 + (6 + camada * 2) * s(a / 12), 60, t * .1);
    }
    public static double alphaAnel(int camada, double t) { double a = t - IMPACTO - camada * 2; return a < 0 || a > 12 ? 0 : 1 - s(a / 12); }
    /** Cratera: borda irregular (fica) e rachaduras eletricas que esfriam. */
    public static List<P> bordaCratera(double t) {
        List<P> l = new ArrayList<>();
        double cresce = s((t - IMPACTO) / 3);
        for (int i = 0; i <= 48; i++) {
            double a = 2 * Math.PI * i / 48, rr = CRATERA * cresce * (1 + .12 * r(i % 48, 5600, 0));
            l.add(chao().mais(new P(Math.cos(a) * rr, 0, Math.sin(a) * rr)));
        }
        return l;
    }
    public static List<P> racha(int k, double t) {
        double a = 2 * Math.PI * k / RACHAS + r(k, 5700, 0) * .25, comp = (CRATERA + 1.5 + 2.5 * Math.abs(r(k, 5701, 0))) * s((t - IMPACTO) / 4);
        List<P> l = KilluaBase.raioQuebrado(chao(), chao().mais(new P(Math.cos(a) * comp, 0, Math.sin(a) * comp)), 8, .35, 5710 + k, 0);
        List<P> c = new ArrayList<>();
        for (P p : l) c.add(new P(p.x(), .04, p.z()));
        return c;
    }
    public static double alphaCratera(double t) { return t < IMPACTO ? 0 : 1; }
    public static double calorCratera(double t) { return t < IMPACTO ? 0 : Math.max(0, 1 - (t - IMPACTO) / 30); }
    public static List<double[]> faiscas(double t) {
        List<double[]> l = new ArrayList<>(KilluaBase.particulas(5800, 80, chao().mais(new P(0, .4, 0)), IMPACTO, t, .75, .6, new P(0, .6, 0), .02, 26));
        for (int i = 0; i < GOLPES; i++) l.addAll(KilluaBase.particulas(5820 + i, 8, alvoCentro(tickGolpe(i)), tickGolpe(i), t, .25, .2, dirGolpe(i).vezes(-1), .01, 8));
        return l;
    }
    public static List<double[]> poeira(double t) { return KilluaBase.particulas(5900, 40, chao(), IMPACTO, t, .28, .25, new P(0, .35, 0), .002, 40); }
    public static double alphaParalisia(double t) { double a = t - IMPACTO; return a < 0 || a > 40 ? (t > PRENDER && t < IMPACTO ? .6 : 0) : 1 - s((a - 28) / 12); }
    public static List<P> arcoAlvo(int k, double t) { P c = alvoCentro(t); return KilluaBase.arcoNoCorpo(new P(c.x(), c.y() - 1.1, c.z()), k, 5950, t, .15); }
}
