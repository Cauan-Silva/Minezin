package com.cauansilva.arcanecontrol.core.combate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Regras de interrupcao comuns a todos os kits (sem Minecraft). Uma entidade (chave K, no jogo o UUID) pode ter uma
 * canalizacao (tecnica com preparo/carga/canal) e uma guarda de contra-ataque. Tempo em ticks de jogo.
 * <ul>
 *   <li>Uma canalizacao cai quando a forca do golpe vence a resistencia dela.</li>
 *   <li>Ao cair: o cancelamento do kit roda uma vez, a tecnica deve entrar em {@link #RECARGA_PENALIDADE} da recarga e o
 *   dono fica {@link #IMUNIDADE} ticks imune a nova interrupcao.</li>
 *   <li>Guarda (Whirlpool): golpe recebido dentro da janela e anulado, o atacante sofre interrupcao FORTE e o
 *   contra-ataque dispara.</li>
 * </ul>
 */
public final class Interrupcao<K> {
    public static final double RECARGA_PENALIDADE = .5;
    public static final int IMUNIDADE = 20;

    public enum Forca { LEVE, FORTE, ABSOLUTA }
    public enum Resistencia {
        NENHUMA, FIRME, SUPERARMADURA, IMUNE;
        boolean caiCom(Forca f) {
            return switch (this) { case NENHUMA -> true; case FIRME -> f != Forca.LEVE; case SUPERARMADURA -> f == Forca.ABSOLUTA; case IMUNE -> false; };
        }
    }
    public enum Resultado { SEM_CANALIZACAO, RESISTIU, IMUNE_RECENTE, INTERROMPIDO }
    /** O que o golpe recebido virou: dano normal, ou anulado e contra-atacado. */
    public enum Defesa { NORMAL, CONTRA_ATAQUE }

    /** Dados que o kit recebe ao ser interrompido (para desligar VFX/som/animacao e aplicar a recarga). */
    public record Quebra<K>(K dono, String tecnica, Object fonte, Forca forca, long tick) {}
    private record Canal<K>(String tecnica, long inicio, long fim, Resistencia resistencia, Consumer<Quebra<K>> cancelar) {}
    private record Guarda<K>(String tecnica, long fim, Consumer<K> contra) {}

    private final Map<K, Canal<K>> canais = new HashMap<>();
    private final Map<K, Guarda<K>> guardas = new HashMap<>();
    private final Map<K, Long> imuneAte = new HashMap<>();

    /** Registra (ou troca) a canalizacao do dono, de agora ate agora + duracao. */
    public void iniciar(K dono, String tecnica, long agora, int duracao, Resistencia r, Consumer<Quebra<K>> cancelar) {
        if (duracao <= 0) throw new IllegalArgumentException("duracao");
        canais.put(dono, new Canal<>(tecnica, agora, agora + duracao, r == null ? Resistencia.FIRME : r, cancelar));
    }
    /** Termino normal (a tecnica saiu); nao chama o cancelamento. */
    public void concluir(K dono, String tecnica) { var c = canais.get(dono); if (c != null && c.tecnica.equals(tecnica)) canais.remove(dono); }
    public boolean canalizando(K dono, long agora) { var c = canais.get(dono); return c != null && agora < c.fim; }
    public String tecnica(K dono) { var c = canais.get(dono); return c == null ? null : c.tecnica; }

    /** Tenta interromper o alvo com um golpe de forca f vindo de fonte. */
    public Resultado interromper(K alvo, Object fonte, Forca f, long agora) {
        var c = canais.get(alvo);
        if (c == null || agora >= c.fim) { canais.remove(alvo); return Resultado.SEM_CANALIZACAO; }
        if (f != Forca.ABSOLUTA && agora < imuneAte.getOrDefault(alvo, Long.MIN_VALUE)) return Resultado.IMUNE_RECENTE;
        if (!c.resistencia.caiCom(f)) return Resultado.RESISTIU;
        canais.remove(alvo);
        imuneAte.put(alvo, agora + IMUNIDADE);
        if (c.cancelar != null) c.cancelar.accept(new Quebra<>(alvo, c.tecnica, fonte, f, agora));
        return Resultado.INTERROMPIDO;
    }

    /** Abre a guarda de contra-ataque do dono por janela ticks. */
    public void guardar(K dono, String tecnica, long agora, int janela, Consumer<K> contra) {
        if (janela <= 0) throw new IllegalArgumentException("janela");
        guardas.put(dono, new Guarda<>(tecnica, agora + janela, contra));
    }
    public boolean guardando(K dono, long agora) { var g = guardas.get(dono); return g != null && agora < g.fim; }

    /**
     * Chamado quando o defensor recebe um golpe do atacante. Com guarda aberta: fecha a guarda, interrompe o atacante
     * (FORTE), chama o contra-ataque com o atacante e devolve CONTRA_ATAQUE (o chamador anula o dano).
     */
    public Defesa receberGolpe(K defensor, K atacante, long agora) {
        var g = guardas.get(defensor);
        if (g == null || agora >= g.fim) { guardas.remove(defensor); return Defesa.NORMAL; }
        guardas.remove(defensor);
        if (atacante != null) interromper(atacante, defensor, Forca.FORTE, agora);
        if (g.contra != null) g.contra.accept(atacante);
        return Defesa.CONTRA_ATAQUE;
    }

    /** Remove tudo do dono (morte, saida, troca de kit) sem chamar callbacks. */
    public void limpar(K dono) { canais.remove(dono); guardas.remove(dono); imuneAte.remove(dono); }
    /** Descarta entradas vencidas. */
    public void podar(long agora) {
        canais.values().removeIf(c -> agora >= c.fim);
        guardas.values().removeIf(g -> agora >= g.fim);
        imuneAte.values().removeIf(t -> agora >= t);
    }
    /** Recarga apos interrupcao (arredonda para cima). */
    public static int recargaPenalizada(int recargaCheia) { return (int) Math.ceil(Math.max(0, recargaCheia) * RECARGA_PENALIDADE); }
}
