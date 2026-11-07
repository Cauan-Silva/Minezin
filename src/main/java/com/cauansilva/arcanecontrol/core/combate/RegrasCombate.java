package com.cauansilva.arcanecontrol.core.combate;

/**
 * Regras puras do polimento de combate (testadas em RegrasCombateTest).
 * <ul>
 *   <li>Impacto: forca 0..1 pelo dano em relacao a vida do alvo (critico soma), que define hitstop e tremor.</li>
 *   <li>Esquiva: dash com i-frames; acerto nos primeiros ticks = Perfect Dodge (camera lenta + contra-ataque).</li>
 *   <li>Lock-on: pontua alvos pelo angulo da mira e pela distancia.</li>
 * </ul>
 */
public final class RegrasCombate {
    // ---------------- impacto ----------------
    /** Forca do golpe: 4 de dano em alvo de 20 de vida ~ .35; critico +.25; teto 1. */
    public static double forca(double dano, double vidaMaxAlvo, boolean critico) {
        if (!(dano > 0)) return 0;
        double rel = dano / Math.max(10, Math.min(vidaMaxAlvo, 200));
        return Math.min(1, .15 + Math.sqrt(rel) * .55 + (critico ? .25 : 0));
    }
    /** Hitstop em ms: 0 abaixo de .25, de 35 a 110 ms. */
    public static int hitstopMs(double forca) { return forca < .25 ? 0 : (int) Math.round(35 + 75 * (forca - .25) / .75); }
    /** Amplitude do tremor (graus) para quem bate e (maior) para quem apanha. */
    public static double tremor(double forca, boolean apanhou) { return forca * (apanhou ? 2.4 : 1.1); }

    // ---------------- esquiva ----------------
    public static final int RECARGA_ESQUIVA = 26, IFRAMES = 7, JANELA_PERFEITA = 4, CONTRA_JANELA = 50;
    public static final double IMPULSO = 1.05, CONTRA_BONUS = .5;
    public static boolean podeEsquivar(long agora, long ultima) { return ultima <= 0 || agora - ultima >= RECARGA_ESQUIVA; }
    public static boolean invulneravel(long agora, long inicio) { return inicio > 0 && agora >= inicio && agora - inicio < IFRAMES; }
    public static boolean perfeita(long agora, long inicio) { return inicio > 0 && agora >= inicio && agora - inicio < JANELA_PERFEITA; }
    /** Perfect Dodge devolve metade da recarga (prazo novo da "ultima" esquiva). */
    public static long reembolso(long ultima) { return ultima - RECARGA_ESQUIVA / 2; }

    // ---------------- lock-on ----------------
    public static final double ALCANCE_LOCK = 24, SOLTA_LOCK = 32, CONE_LOCK = 35;
    /** Pontuacao do alvo (menor = melhor) ou infinito fora do cone/alcance. */
    public static double pontuar(double anguloGraus, double distancia) {
        if (anguloGraus > CONE_LOCK || distancia > ALCANCE_LOCK) return Double.POSITIVE_INFINITY;
        return anguloGraus * 1.5 + distancia;
    }
    /** Passo suave da camera ate o alvo: angulo (graus) com wrap em -180..180. */
    public static float aproximar(float atual, float alvo, float fator) {
        float d = ((alvo - atual) % 360 + 540) % 360 - 180;
        return atual + d * Math.max(0, Math.min(1, fator));
    }
    /** FOV do lock-on: alvo perto abre (1.06), longe fecha (0.92). */
    public static double fov(double distancia) { double t = Math.max(0, Math.min(1, (distancia - 3) / 18)); return 1.06 - .14 * t; }
    private RegrasCombate() {}
}
