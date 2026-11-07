package com.cauansilva.arcanecontrol.core.passivas;

import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;

/**
 * Regras puras das passivas (sem Minecraft; testadas em PassivasTest). Cada kit tem uma passiva que so vale com o kit
 * ativo; a ponte Forge e PassivasServidor. Tabela aprovada em 06/11/2026 (vault: Sistemas/Passivas).
 * <ul>
 *   <li>Killua, Imunidade Zoldyck: imune a veneno e wither; -75% de dano de raio.</li>
 *   <li>Gojo, Seis Olhos: devolve 15% da mana de cada tecnica; inimigos invisiveis a ate 16 blocos brilham.</li>
 *   <li>Shadow, Mestre das Sombras: no escuro (luz &lt;= 7) +15% de dano causado e -20% recebido.</li>
 *   <li>Frieren, Supressao de Mana: +20% no golpe em quem nao a tem como alvo; mana regenera 50% mais rapido.</li>
 *   <li>Asta, Antimagia: -30% de dano magico recebido; imune a lentidao.</li>
 *   <li>Ichigo, Reiatsu Transbordante: abaixo de 30% de vida, +20% de dano e Getsuga com metade da recarga.</li>
 *   <li>Subaru, Retorno pela Morte: a cada 10 min, um golpe fatal deixa 1 coracao e 3 s de invulnerabilidade.</li>
 * </ul>
 */
public final class Passivas {
    public enum Dano { RAIO, MAGIA, OUTRO }

    public static final double REDUCAO_RAIO_KILLUA = .75, REEMBOLSO_GOJO = .15, RAIO_SEIS_OLHOS = 16;
    public static final int LUZ_ESCURO = 7;
    public static final double BONUS_SOMBRA = .15, REDUCAO_SOMBRA = .20, BONUS_FRIEREN = .20, REGEN_FRIEREN = 1.5;
    public static final double REDUCAO_MAGIA_ASTA = .30, VIDA_REIATSU = .30, BONUS_REIATSU = .20;
    public static final int RECARGA_RETORNO = 12000, INVULNERAVEL_RETORNO = 60;
    public static final float VIDA_RETORNO = 2;

    public static String nome(PoderAnime kit) {
        return switch (kit) {
            case GOJO -> "Seis Olhos"; case CID -> "Mestre das Sombras"; case FRIEREN -> "Supressão de Mana"; case ASTA -> "Antimagia";
            case ICHIGO -> "Reiatsu Transbordante"; case KILLUA -> "Imunidade Zoldyck"; case SUBARU -> "Retorno pela Morte";
        };
    }

    /** Dano recebido pelo dono do kit. */
    public static float danoRecebido(PoderAnime kit, Dano tipo, int luz, float valor) {
        if (valor <= 0) return valor;
        return switch (kit) {
            case KILLUA -> tipo == Dano.RAIO ? (float) (valor * (1 - REDUCAO_RAIO_KILLUA)) : valor;
            case CID -> luz <= LUZ_ESCURO ? (float) (valor * (1 - REDUCAO_SOMBRA)) : valor;
            case ASTA -> tipo == Dano.MAGIA ? (float) (valor * (1 - REDUCAO_MAGIA_ASTA)) : valor;
            default -> valor;
        };
    }

    /** Dano causado pelo dono do kit. desatento: o alvo nao tem o dono como alvo; vida: fracao da vida do dono. */
    public static float danoCausado(PoderAnime kit, int luz, boolean desatento, double vida, float valor) {
        if (valor <= 0) return valor;
        return switch (kit) {
            case CID -> luz <= LUZ_ESCURO ? (float) (valor * (1 + BONUS_SOMBRA)) : valor;
            case FRIEREN -> desatento ? (float) (valor * (1 + BONUS_FRIEREN)) : valor;
            case ICHIGO -> vida <= VIDA_REIATSU ? (float) (valor * (1 + BONUS_REIATSU)) : valor;
            default -> valor;
        };
    }

    /** Efeito bloqueado pela passiva (nome do registro vanilla, ex. "poison"). */
    public static boolean bloqueiaEfeito(PoderAnime kit, String efeito) {
        return switch (kit) {
            case KILLUA -> efeito.equals("poison") || efeito.equals("wither");
            case ASTA -> efeito.equals("slowness");
            default -> false;
        };
    }

    /** Multiplicador da regeneracao de mana. */
    public static double regeneracao(PoderAnime kit) { return kit == PoderAnime.FRIEREN ? REGEN_FRIEREN : 1; }

    /** Recarga base (ticks) da tecnica depois da passiva. */
    public static int recarga(PoderAnime kit, int slot, double vida, int base) {
        return kit == PoderAnime.ICHIGO && slot == 0 && vida <= VIDA_REIATSU ? Math.max(1, base / 2) : base;
    }

    /** Mana devolvida depois de uma tecnica que custou `custo`. */
    public static double reembolso(PoderAnime kit, double custo) { return kit == PoderAnime.GOJO ? custo * REEMBOLSO_GOJO : 0; }

    /** Subaru: o golpe e fatal e o Retorno esta pronto (agora >= prontoEm). */
    public static boolean retorno(PoderAnime kit, float vida, float dano, long agora, long prontoEm) {
        return kit == PoderAnime.SUBARU && dano >= vida && agora >= prontoEm;
    }

    private Passivas() {}
}
