package com.cauansilva.arcanecontrol.core.rank;

import java.util.*;

/**
 * Rank de Cacador (regras puras, testadas em RankCacadorTest): F ao SSS por pontos ganhos vencendo desafios no Vazio.
 * Derrota rende 0 e nunca tira pontos. Dificuldades Mitica e Celestial exigem Rank medio minimo do grupo.
 */
public final class RankCacador {
    public enum Rank {
        F(0, 0xFF9A9A9A), E(100, 0xFFB5E3A0), D(300, 0xFF6FCF6F), C(700, 0xFF5AB8E8), B(1400, 0xFF4F7BE8),
        A(2600, 0xFFB05AE8), S(4500, 0xFFF2C14E), SS(7500, 0xFFF28A3C), SSS(12000, 0xFFE8433C);
        public final int minimo, cor;
        Rank(int minimo, int cor) { this.minimo = minimo; this.cor = cor; }
        public Rank proximo() { return this == SSS ? null : values()[ordinal() + 1]; }
    }
    public enum Dificuldade {
        NORMAL("Normal", Rank.F, 1.0, 1.0, 1.0),
        MITICA("Mítica", Rank.B, 1.8, 1.4, 2.5),
        CELESTIAL("Celestial", Rank.S, 3.0, 1.9, 5.0);
        public final String nome; public final Rank minimo; public final double vida, dano, pontos;
        Dificuldade(String nome, Rank minimo, double vida, double dano, double pontos) { this.nome = nome; this.minimo = minimo; this.vida = vida; this.dano = dano; this.pontos = pontos; }
        public static Dificuldade porId(int i) { return values()[Math.max(0, Math.min(values().length - 1, i))]; }
    }

    public static Rank rank(int pontos) { Rank r = Rank.F; for (var x : Rank.values()) if (pontos >= x.minimo) r = x; return r; }
    /** Progresso (0..1) ate o proximo rank; SSS = 1. */
    public static double progresso(int pontos) {
        var r = rank(pontos); var n = r.proximo(); if (n == null) return 1;
        return (pontos - r.minimo) / (double) (n.minimo - r.minimo);
    }
    /** Rank medio do grupo: media dos indices arredondada para baixo. */
    public static Rank media(Collection<Rank> grupo) {
        if (grupo.isEmpty()) return Rank.F;
        int soma = 0; for (var r : grupo) soma += r.ordinal();
        return Rank.values()[soma / grupo.size()];
    }
    public static boolean podeEntrar(Dificuldade d, Collection<Rank> grupo) { return media(grupo).ordinal() >= d.minimo.ordinal(); }

    /**
     * Pontos de um jogador ao fim do desafio: base 60, +30 sem cair, +20 se venceu em menos de 3 min,
     * +10 por aliado (ate 3), vezes o multiplicador da dificuldade. Derrota = 0.
     */
    public static int pontos(boolean vitoria, Dificuldade d, int segundos, boolean caiu, int grupo) {
        if (!vitoria) return 0;
        int base = 60 + (caiu ? 0 : 30) + (segundos < 180 ? 20 : 0) + 10 * Math.min(3, Math.max(0, grupo - 1));
        return (int) Math.round(base * d.pontos);
    }
    private RankCacador() {}
}
