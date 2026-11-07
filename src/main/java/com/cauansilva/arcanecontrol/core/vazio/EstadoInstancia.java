package com.cauansilva.arcanecontrol.core.vazio;

import java.util.*;

/**
 * Maquina de estados pura de uma instancia (sem Minecraft; testada em VazioTest). Tempo em ticks de jogo.
 * ENTRADA (fade + HUD travado) -> COMBATE -> VITORIA (chefe morto) ou WIPE (todos caidos) -> ENCERRADA.
 * Quem morre vira espectador ({@link #cair}); ninguem perde nada.
 */
public final class EstadoInstancia<K> {
    public static final int ENTRADA = CinematicaEntrada.FIM, SAIDA_VITORIA = 120, SAIDA_WIPE = 80, LIMITE = 20 * 60 * 15;
    public enum Fase { ENTRADA, COMBATE, VITORIA, WIPE, ENCERRADA }

    private final long inicio;
    private final Set<K> vivos = new LinkedHashSet<>(), caidos = new LinkedHashSet<>();
    private Fase fase = Fase.ENTRADA;
    private long desde;

    public EstadoInstancia(Collection<K> jogadores, long agora) {
        if (jogadores.isEmpty()) throw new IllegalArgumentException("sem jogadores");
        vivos.addAll(jogadores); inicio = agora; desde = agora;
    }
    public Fase fase() { return fase; }
    public Set<K> vivos() { return Collections.unmodifiableSet(vivos); }
    public Set<K> caidos() { return Collections.unmodifiableSet(caidos); }
    public Set<K> todos() { var t = new LinkedHashSet<>(vivos); t.addAll(caidos); return t; }
    /** Movimento e HUD travados (entrada). */
    public boolean travado(long agora) { return fase == Fase.ENTRADA; }

    private void ir(Fase f, long agora) { fase = f; desde = agora; }
    /** Avanca o relogio: fim da entrada, tempo limite (conta como wipe) e saida depois da vitoria/wipe. */
    public Fase tick(long agora) {
        if (fase == Fase.ENTRADA && agora - desde >= ENTRADA) ir(Fase.COMBATE, agora);
        if ((fase == Fase.ENTRADA || fase == Fase.COMBATE) && agora - inicio >= LIMITE) ir(Fase.WIPE, agora);
        if (fase == Fase.VITORIA && agora - desde >= SAIDA_VITORIA || fase == Fase.WIPE && agora - desde >= SAIDA_WIPE) ir(Fase.ENCERRADA, agora);
        return fase;
    }
    /** Jogador morreu (ou saiu): vira espectador; sem ninguem de pe durante a luta, e wipe. */
    public void cair(K k, long agora) {
        if (!vivos.remove(k)) return;
        caidos.add(k);
        if (vivos.isEmpty() && (fase == Fase.ENTRADA || fase == Fase.COMBATE)) ir(Fase.WIPE, agora);
    }
    /** Saiu de vez (desconectou): nao volta a contar. */
    public void remover(K k, long agora) { boolean estava = vivos.contains(k); vivos.remove(k); caidos.remove(k); if (estava && vivos.isEmpty() && (fase == Fase.ENTRADA || fase == Fase.COMBATE)) ir(Fase.WIPE, agora); }
    /** Chefe derrotado. */
    public void vencer(long agora) { if (fase == Fase.COMBATE || fase == Fase.ENTRADA) ir(Fase.VITORIA, agora); }
    public boolean participa(K k) { return vivos.contains(k) || caidos.contains(k); }
}
