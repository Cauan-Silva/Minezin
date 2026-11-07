package com.cauansilva.arcanecontrol.core.vazio;

/**
 * Barra de vida do chefe estilo Souls (pura, testada): a parte vermelha acompanha a vida na hora; a parte amarela
 * ("dano recente") fica parada ATRASO ticks depois do ultimo golpe e entao desce ate a vida. O numero do dano
 * acumulado no combo aparece enquanto a amarela estiver parada.
 */
public final class BarraChefe {
    public static final int ATRASO = 24; public static final double QUEDA = .02;
    private double vida = 1, atraso = 1, combo; private int parado;
    /** Novo valor (0..1) e vida maxima (para o numero do combo). */
    public void atualizar(double fracao, double max) {
        fracao = Math.max(0, Math.min(1, fracao));
        if (fracao < vida - 1e-6) { combo += (vida - fracao) * max; parado = ATRASO; }
        else if (fracao > vida) { atraso = Math.max(atraso, fracao); }
        vida = fracao;
    }
    public void tick() {
        if (parado > 0) { parado--; return; }
        combo = 0;
        if (atraso > vida) atraso = Math.max(vida, atraso - QUEDA); else atraso = vida;
    }
    public double vida() { return vida; }
    public double atraso() { return atraso; }
    public int combo() { return parado > 0 ? (int) Math.round(combo) : 0; }
    public void reiniciar() { vida = atraso = 1; combo = 0; parado = 0; }
}
