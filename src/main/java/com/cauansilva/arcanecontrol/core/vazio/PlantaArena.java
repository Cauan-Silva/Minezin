package com.cauansilva.arcanecontrol.core.vazio;

/**
 * Regras puras do Void System (sem Minecraft; testadas em VazioTest): onde fica cada instancia na dimensao
 * arcanecontrol:boss_realm e que papel tem cada bloco da arena gerada por codigo.
 * <p>A arena e um disco de raio {@link #RAIO} no nivel {@link #CHAO}: chao com aneis e rachaduras, borda elevada,
 * {@link #PILARES} pilares de cobertura (para os QTEs de hit-kill futuros) e uma barreira cilindrica ate {@link #TETO}.
 */
public final class PlantaArena {
    /** Espacamento entre instancias (blocos) e quantas cabem por linha da grade. */
    public static final int ESPACO = 512, POR_LINHA = 64, ORIGEM = 10000;
    public static final int RAIO = 36, CHAO = 64, TETO = 40, PILARES = 8, RAIO_PILAR = 25, ALTURA_PILAR = 6, ALTURA_BORDA = 1;
    /** Onde os jogadores aparecem (distancia do centro) e onde o chefe aparece (do lado oposto). */
    public static final int RAIO_JOGADORES = 14, RAIO_CHEFE = 14;

    public enum Papel { VAZIO, CHAO, DETALHE, BORDA, PILAR, TOPO_PILAR, LUZ, BARREIRA }

    /** Centro (x, z) da instancia n (n >= 0), sem sobreposicao: cada uma fica a ESPACO blocos da vizinha. */
    public static int[] centro(long n) {
        if (n < 0) throw new IllegalArgumentException("instancia");
        return new int[]{ORIGEM + (int) (n % POR_LINHA) * ESPACO, ORIGEM + (int) (n / POR_LINHA) * ESPACO};
    }

    private static double ruido(int x, int z) {
        long h = x * 73856093L ^ z * 19349663L; h ^= h >>> 13; h *= 0x5bd1e995L; h ^= h >>> 15;
        return (h & 0xffff) / 65535.0;
    }
    /** Pilar k: centro (dx, dz). */
    public static int[] pilar(int k) {
        double a = 2 * Math.PI * (k + .5) / PILARES;
        return new int[]{(int) Math.round(Math.cos(a) * RAIO_PILAR), (int) Math.round(Math.sin(a) * RAIO_PILAR)};
    }
    private static boolean noPilar(int dx, int dz) {
        for (int k = 0; k < PILARES; k++) { int[] p = pilar(k); if (Math.abs(dx - p[0]) <= 1 && Math.abs(dz - p[1]) <= 1) return true; }
        return false;
    }

    /** Papel do bloco em (dx, dy, dz) relativo ao centro do chao (dy = 0 e o chao). */
    public static Papel papel(int dx, int dy, int dz) {
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d > RAIO + 2) return Papel.VAZIO;
        // barreira: casca cilindrica de 1,5 bloco logo fora da borda (sem frestas na diagonal), do chao ao teto, e a tampa
        if (d > RAIO + .5) return dy >= -1 && dy <= TETO ? Papel.BARREIRA : Papel.VAZIO;
        if (dy == TETO) return Papel.BARREIRA;
        if (dy == -1) return Papel.CHAO;                                         // base que impede quedas
        if (dy == 0) {
            if (d > RAIO - 2.5) return Papel.BORDA;
            if (Math.abs(d - 9) < .5 || Math.abs(d - 20) < .5) return Papel.DETALHE;   // aneis
            if (d < 2.5) return d < 1 ? Papel.LUZ : Papel.DETALHE;                     // selo no centro
            return ruido(dx, dz) < .08 ? Papel.DETALHE : Papel.CHAO;                   // rachaduras
        }
        if (dy >= 1 && dy <= ALTURA_BORDA && d > RAIO - 1.5) return Papel.BORDA;
        if (dy >= 1 && dy <= ALTURA_PILAR && noPilar(dx, dz)) return dy == ALTURA_PILAR ? Papel.TOPO_PILAR : Papel.PILAR;
        return Papel.VAZIO;
    }

    /** Ponto de entrada do jogador i (de n) e do chefe: (dx, dz) relativos ao centro. */
    public static double[] entradaJogador(int i, int n) {
        double a = Math.PI + (n <= 1 ? 0 : (i - (n - 1) / 2.0) * .35);
        return new double[]{Math.cos(a) * RAIO_JOGADORES, Math.sin(a) * RAIO_JOGADORES};
    }
    public static double[] entradaChefe() { return new double[]{RAIO_CHEFE, 0}; }

    private PlantaArena() {}
}
