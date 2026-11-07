package com.cauansilva.arcanecontrol.core.vazio;

import java.util.*;

/**
 * Placar de uma instancia do Vazio (regras puras, testadas em PlacarVazioTest): dano no chefe, cura, dano recebido e
 * quedas de cada jogador; no fim gera as linhas da Tela de MVP, os destaques (Maior Dano, Maior Cura, Muralha) e as
 * Dicas de Sobrevivencia.
 */
public final class PlacarVazio<K> {
    public static final class Ficha { public double dano, cura, recebido; public int quedas, golpes;
        public double maiorGolpe; }
    public record Linha(String nome, int dano, int cura, int recebido, int quedas, int maiorGolpe, String destaque) {}

    private final Map<K, Ficha> fichas = new LinkedHashMap<>();
    private final Map<K, String> nomes = new HashMap<>();
    public void entrar(K id, String nome) { fichas.computeIfAbsent(id, k -> new Ficha()); nomes.put(id, nome); }
    public Ficha ficha(K id) { return fichas.computeIfAbsent(id, k -> new Ficha()); }
    public void dano(K id, double v) { if (v <= 0) return; var f = ficha(id); f.dano += v; f.golpes++; f.maiorGolpe = Math.max(f.maiorGolpe, v); }
    public void cura(K id, double v) { if (v > 0) ficha(id).cura += v; }
    public void recebido(K id, double v) { if (v > 0) ficha(id).recebido += v; }
    public void caiu(K id) { ficha(id).quedas++; }
    public double danoTotal() { return fichas.values().stream().mapToDouble(f -> f.dano).sum(); }

    /** Linhas ordenadas por dano; destaques so para quem tem valor > 0 (um destaque por jogador, o primeiro que couber). */
    public List<Linha> linhas() {
        var ids = new ArrayList<>(fichas.keySet());
        ids.sort(Comparator.comparingDouble((K k) -> -fichas.get(k).dano));
        K topDano = melhor(f -> f.dano), topCura = melhor(f -> f.cura), muralha = melhor(f -> f.quedas == 0 ? f.recebido : 0);
        var out = new ArrayList<Linha>();
        for (var id : ids) {
            var f = fichas.get(id);
            String d = id.equals(topDano) ? "MVP · Maior Dano" : id.equals(topCura) ? "Maior Cura" : id.equals(muralha) ? "Muralha" : "";
            out.add(new Linha(nomes.getOrDefault(id, "?"), (int) Math.round(f.dano), (int) Math.round(f.cura), (int) Math.round(f.recebido), f.quedas, (int) Math.round(f.maiorGolpe), d));
        }
        return out;
    }
    private K melhor(java.util.function.ToDoubleFunction<Ficha> valor) {
        K m = null; double v = 0;
        for (var e : fichas.entrySet()) { double x = valor.applyAsDouble(e.getValue()); if (x > v) { v = x; m = e.getKey(); } }
        return m;
    }

    /**
     * Dicas de Sobrevivencia (no maximo 3) a partir de como foi a luta: quedas, dano recebido em relacao a vida,
     * pouca cura, luta longa, golpe carregado do chefe e o que o grupo fez bem numa vitoria limpa.
     */
    public List<String> dicas(boolean vitoria, int segundos, double vidaMaxJogador) {
        var d = new ArrayList<String>();
        int quedas = fichas.values().stream().mapToInt(f -> f.quedas).sum();
        double recebido = fichas.values().stream().mapToDouble(f -> f.recebido).sum() / Math.max(1, fichas.size());
        double cura = fichas.values().stream().mapToDouble(f -> f.cura).sum();
        if (quedas > 0) d.add("Quando o chefe brilha e para, é o golpe carregado: saia do raio ou use guarda/esquiva.");
        if (recebido > vidaMaxJogador * 3) d.add("Você recebeu " + (int) Math.round(recebido / Math.max(1, vidaMaxJogador)) + "x a sua vida em dano: lute dos pilares e ataque nas janelas de recuperação.");
        if (cura < recebido * .25 && recebido > vidaMaxJogador) d.add("Pouca cura: leve poções/comida dourada ou relíquias com Roubo de Vida.");
        if (!vitoria && danoTotal() < 1) d.add("Ninguém acertou o chefe: aproxime-se depois do golpe dele, não antes.");
        if (segundos > 300) d.add("Luta longa (" + segundos / 60 + " min): relíquias de Dano e Recarga encurtam o desafio.");
        if (fichas.size() > 1 && quedas == 0 && vitoria) d.add("Vitória sem quedas: o grupo pode tentar um kit mais difícil.");
        if (d.isEmpty()) d.add(vitoria ? "Luta limpa. Use o Ferreiro do Vazio para melhorar as relíquias." : "Abaixo de 50% de vida o chefe ataca mais rápido: guarde a mana para essa fase.");
        return d.size() > 3 ? List.copyOf(d.subList(0, 3)) : List.copyOf(d);
    }
}
