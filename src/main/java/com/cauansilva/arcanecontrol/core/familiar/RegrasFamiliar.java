package com.cauansilva.arcanecontrol.core.familiar;

/**
 * Familiares (regras puras, testadas em RegrasFamiliarTest): espiritos que seguem o jogador, recolhem itens do chao,
 * acumulam Stun nos alvos que o dono acerta, dao aura de mana e se sacrificam para salvar o dono de um golpe fatal.
 */
public final class RegrasFamiliar {
    public enum Tipo {
        RAIO("Espírito do Raio", 0x7FC8FF, 24, 1.0, 6000, 2400, 4),
        FONTE("Espírito da Fonte", 0x6FE3B0, 8, 1.35, 6000, 2400, 4),
        GUARDIAO("Espírito Guardião", 0xF2C14E, 8, 1.0, 3600, 1800, 10);
        public final String nome; public final int cor; public final double stun, mana; public final int recargaSacrificio, ausencia, vidaSalva;
        Tipo(String nome, int cor, double stun, double mana, int recargaSacrificio, int ausencia, int vidaSalva) {
            this.nome = nome; this.cor = cor; this.stun = stun; this.mana = mana; this.recargaSacrificio = recargaSacrificio; this.ausencia = ausencia; this.vidaSalva = vidaSalva;
        }
        public static Tipo porId(int i) { return i >= 0 && i < values().length ? values()[i] : null; }
        public String efeito() {
            return switch (this) {
                case RAIO -> "Golpes acumulam muito Stun elétrico no alvo.";
                case FONTE -> "Aura: +35% de regeneração de mana.";
                case GUARDIAO -> "Sacrifício volta mais rápido e salva com 5 corações.";
            };
        }
    }

    // ---------------- Stun (build-up) ----------------
    public static final double STUN_MAX = 100, STUN_DECAI_POR_TICK = .2; public static final int STUN_DURACAO = 30, STUN_IMUNE = 100;
    /** Barra de Stun depois de um golpe: decai com o tempo desde o ultimo, soma o do tipo. */
    public static double acumular(double atual, long ticksDesde, double ganho) {
        double v = Math.max(0, atual - Math.max(0, ticksDesde) * STUN_DECAI_POR_TICK);
        return Math.min(STUN_MAX, v + Math.max(0, ganho));
    }
    public static boolean atordoa(double valor) { return valor >= STUN_MAX; }

    // ---------------- sacrificio ----------------
    /** Intercepta o golpe se ele mataria o dono, o familiar esta presente e a recarga acabou. */
    public static boolean intercepta(double dano, double vida, boolean presente, long agora, long prontoEm) {
        return presente && dano >= vida && agora >= prontoEm;
    }

    // ---------------- seguir / coletar ----------------
    public static final double RAIO_COLETA = 6, TELEPORTA = 20, ALTURA = 1.9, LADO = .85;
    /** Posicao alvo do familiar: atras e ao lado do ombro do dono, com balanco suave. */
    public static double[] posicao(double x, double y, double z, float yawGraus, long tempo) {
        double r = Math.toRadians(yawGraus), bob = Math.sin(tempo * .12) * .12;
        double fx = -Math.sin(r), fz = Math.cos(r), lx = Math.cos(r), lz = Math.sin(r);
        return new double[]{x - fx * .55 + lx * LADO, y + ALTURA + bob, z - fz * .55 + lz * LADO};
    }
    private RegrasFamiliar() {}
}
