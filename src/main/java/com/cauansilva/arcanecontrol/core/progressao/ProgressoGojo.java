package com.cauansilva.arcanecontrol.core.progressao;

import java.util.Arrays;

/** Regras de progressão independentes de cliente, rede e Minecraft. */
public final class ProgressoGojo {
    public static final int XP_POR_NIVEL = 100;
    public static final int NIVEL_MAXIMO = 20;
    private int xp;
    private int desbloqueios;
    private final int[] usos;

    public ProgressoGojo() { this(0, 1, new int[5]); }

    public ProgressoGojo(int xp, int desbloqueios, int[] usos) {
        this.xp = Math.max(0, Math.min((NIVEL_MAXIMO - 1) * XP_POR_NIVEL, xp));
        this.desbloqueios = (desbloqueios & 31) | 1;
        this.usos = Arrays.copyOf(usos, 5);
        for (int i = 0; i < 5; i++) this.usos[i] = Math.max(0, Math.min(100, this.usos[i]));
    }

    public int xp() { return xp; }
    public int nivel() { return 1 + xp / XP_POR_NIVEL; }
    public int desbloqueios() { return desbloqueios; }
    public int[] usos() { return usos.clone(); }
    public boolean tem(TecnicaGojo tecnica) { return (desbloqueios & tecnica.bit()) != 0; }
    public int maestria(TecnicaGojo tecnica) { return usos[tecnica.id - 1] / 20; }
    public int cooldown(TecnicaGojo tecnica) {
        return tecnica.cooldown * (100 - maestria(tecnica) * 5) / 100;
    }

    public int pontos() {
        int gastos = 0;
        for (TecnicaGojo t : TecnicaGojo.values()) if (tem(t)) gastos += t.custo;
        return Math.max(0, nivel() - 1 - gastos);
    }

    public boolean podeDesbloquear(TecnicaGojo t) {
        return !tem(t) && nivel() >= t.nivel && pontos() >= t.custo
                && (desbloqueios & t.requisitos) == t.requisitos;
    }

    public boolean desbloquear(TecnicaGojo t) {
        if (!podeDesbloquear(t)) return false;
        desbloqueios |= t.bit();
        return true;
    }

    public void ganharXp(int valor) {
        if (valor > 0) xp = (int) Math.min((NIVEL_MAXIMO - 1L) * XP_POR_NIVEL, (long) xp + valor);
    }

    public void registrarUso(TecnicaGojo t) {
        if (!tem(t)) return;
        usos[t.id - 1] = Math.min(100, usos[t.id - 1] + 1);
        ganharXp(5);
    }
}
