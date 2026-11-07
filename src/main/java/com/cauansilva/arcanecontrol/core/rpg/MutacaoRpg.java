package com.cauansilva.arcanecontrol.core.rpg;

/**
 * Mutacoes da Skill Tree (regras puras, testadas em MutacaoRpgTest): cada tecnica X/C/V, depois de comprada e num nivel
 * minimo, ramifica numa de 4 mutacoes que mudam como ela funciona. Vale para qualquer kit: o servidor aplica no custo,
 * na recarga e no dano que sai da tecnica (janela de JANELA ticks depois de conjurar).
 */
public enum MutacaoRpg {
    NENHUMA("Base", "Sem mutação.", 1, 1, 1, 0, 0),
    DEVASTACAO("Devastação", "+30% de dano da técnica, mas +20% de recarga.", 1.30, 1.20, 1, 0, 0),
    RESSONANCIA("Ressonância", "Os golpes da técnica espalham 40% do dano em inimigos a até 3 blocos do alvo.", 1, 1, 1, 0, .40),
    VAMPIRICA("Vampírica", "20% do dano da técnica volta como vida.", 1, 1, 1, .20, 0),
    FLUXO("Fluxo", "-30% de recarga e -25% de mana, mas -15% de dano.", .85, .70, .75, 0, 0);

    public static final int JANELA = 50; public static final double RAIO_RESSONANCIA = 3;
    /** Nivel de maestria para mutar X, C e V (slots 1..3). */
    public static final int[] NIVEL = {0, 15, 25, 35};
    public final String nome, descricao; public final double dano, recarga, mana, roubo, respingo;
    MutacaoRpg(String nome, String descricao, double dano, double recarga, double mana, double roubo, double respingo) {
        this.nome = nome; this.descricao = descricao; this.dano = dano; this.recarga = recarga; this.mana = mana; this.roubo = roubo; this.respingo = respingo;
    }
    public static MutacaoRpg porId(int i) { return i >= 0 && i < values().length ? values()[i] : NENHUMA; }
    public MutacaoRpg proxima() { return values()[(ordinal() + 1) % values().length]; }
    public static NoRpg no(int slot) { return switch (slot) { case 1 -> NoRpg.TECNICA_X; case 2 -> NoRpg.TECNICA_C; case 3 -> NoRpg.TECNICA_V; default -> null; }; }

    /** 3 bits por slot (1..3) a partir do bit 16 do campo "nos" (os bits 0..7 sao os nos da arvore). */
    public static int ler(int empacotado, int slot) { return slot < 1 || slot > 3 ? 0 : (empacotado >>> (16 + (slot - 1) * 3)) & 7; }
    public static int gravar(int empacotado, int slot, int mut) {
        int d = 16 + (slot - 1) * 3; return (empacotado & ~(7 << d)) | ((mut & 7) << d);
    }
}
