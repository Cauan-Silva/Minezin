package com.cauansilva.arcanecontrol.core.reliquias;

import java.util.*;

/**
 * Regras puras das reliquias (sem Minecraft; testadas em ReliquiasTest): tiers, afixos com faixa de valor por tier,
 * nome gerado (prefixo/sufixo), bonus somados da Bolsa, set de chefe e o pity do bau.
 * <ul>
 *   <li>Tier 1 Comum (1 afixo), 2 Raro (2), 3 Epico (3), 4 Lendario (3, valores maiores), 5 Mitico (4).</li>
 *   <li>Set: 3 reliquias do mesmo chefe ativam a passiva do set; uma Mitica conta como 2.</li>
 *   <li>Pity: Mitico garantido na 11a abertura sem Mitico.</li>
 * </ul>
 */
public final class Reliquias {
    public static final int TIERS = 5, PITY = 11, PECAS_SET = 3, ITENS_BAU = 3;
    public static final String[] TIER_NOME = {"", "Comum", "Raro", "Épico", "Lendário", "Mítico"};
    /** Chance (soma 1) de cada tier no bau: Comum..Mitico. */
    public static final double[] CHANCE = {0, .45, .30, .16, .07, .02};

    /** Afixos: id, nome do bonus, prefixo/sufixo do nome e o valor (%) minimo/maximo no tier 1 e no tier 5. */
    public enum Afixo {
        DANO("Dano", "Feroz", "da Ruína", 3, 6, 12, 18),
        CRITICO("Chance de Crítico", "Preciso", "do Golpe Certo", 3, 5, 10, 15),
        RECARGA("Recarga reduzida", "Ágil", "do Tempo", 2, 4, 8, 12),
        MANA("Regeneração de Mana", "Arcano", "da Fonte", 5, 10, 20, 30),
        VIDA("Vida Máxima", "Vital", "do Coração", 2, 4, 8, 12),
        RESISTENCIA("Dano recebido reduzido", "Firme", "da Muralha", 2, 4, 8, 12),
        VELOCIDADE("Velocidade", "Veloz", "do Vento", 2, 4, 7, 10),
        ROUBO("Roubo de Vida", "Faminto", "do Sangue", 1, 2, 4, 6);
        public final String bonus, prefixo, sufixo; final double min1, max1, min5, max5;
        Afixo(String bonus, String prefixo, String sufixo, double min1, double max1, double min5, double max5) {
            this.bonus = bonus; this.prefixo = prefixo; this.sufixo = sufixo; this.min1 = min1; this.max1 = max1; this.min5 = min5; this.max5 = max5;
        }
        /** Faixa [min, max] do valor no tier. VIDA e em coracoes inteiros de vida extra (2 pontos), o resto em %. */
        public double[] faixa(int tier) { double u = (Math.max(1, Math.min(TIERS, tier)) - 1) / 4.0; return new double[]{min1 + (min5 - min1) * u, max1 + (max5 - max1) * u}; }
        public String formatar(double v) { return this == VIDA ? "+" + (int) v + " de " + bonus : (this == RESISTENCIA || this == RECARGA ? "-" : "+") + (int) Math.round(v) + "% " + bonus; }
    }
    public enum Tipo { ANEL("Anel"), COLAR("Colar"), AMULETO("Amuleto"), BRINCO("Brinco");
        public final String nome; Tipo(String n) { nome = n; } }

    public record Rolagem(Afixo afixo, double valor) {}
    /** Uma reliquia gerada: tipo, tier, chefe de origem (kit) e afixos. */
    public record Reliquia(Tipo tipo, int tier, int kit, List<Rolagem> afixos) {
        public boolean mitica() { return tier == TIERS; }
        /** "Anel Feroz da Ruina [Tier 4]": prefixo do 1o afixo, sufixo do 2o (ou do 1o se so houver um). */
        public String nome() {
            var a = afixos.get(0).afixo(); var b = afixos.size() > 1 ? afixos.get(1).afixo() : a;
            return tipo.nome + " " + a.prefixo + " " + b.sufixo + " [Tier " + tier + "]";
        }
    }

    public static int afixos(int tier) { return switch (tier) { case 1 -> 1; case 2 -> 2; case 3, 4 -> 3; default -> 4; }; }

    /** Sorteia o tier pela tabela CHANCE; forcaMitico (pity) devolve 5. */
    public static int tier(Random r, boolean forcaMitico) {
        if (forcaMitico) return TIERS;
        double x = r.nextDouble(), acc = 0;
        for (int t = 1; t <= TIERS; t++) { acc += CHANCE[t]; if (x < acc) return t; }
        return 1;
    }
    /** Gera uma reliquia: afixos distintos, valores na faixa do tier. */
    public static Reliquia gerar(Random r, Tipo tipo, int tier, int kit) {
        var lista = new ArrayList<>(List.of(Afixo.values())); Collections.shuffle(lista, r);
        var rol = new ArrayList<Rolagem>();
        for (int i = 0; i < afixos(tier); i++) {
            rol.add(rolar(r, lista.get(i), tier));
        }
        return new Reliquia(tipo, tier, kit, List.copyOf(rol));
    }
    /** Valor de um afixo sorteado na faixa do tier (VIDA inteiro, resto com 1 casa). */
    static Rolagem rolar(Random r, Afixo a, int tier) {
        double[] f = a.faixa(tier); double v = f[0] + (f[1] - f[0]) * r.nextDouble();
        return new Rolagem(a, a == Afixo.VIDA ? Math.round(v) : Math.round(v * 10) / 10.0);
    }

    // ---------------- Essencia do Vazio e Ferreiro ----------------
    /** Essencia recebida ao desmanchar uma reliquia de cada tier. */
    public static final int[] DESMANCHE = {0, 2, 5, 12, 30, 80};
    /** Custo em Essencia para reforjar so os valores (mesmos afixos); reforjar tudo custa o dobro. */
    public static final int[] CUSTO_VALORES = {0, 3, 7, 16, 36, 90};
    public enum Reforja { VALORES, TUDO }
    public static int custo(int tier, Reforja modo) { int c = CUSTO_VALORES[Math.max(1, Math.min(TIERS, tier))]; return modo == Reforja.TUDO ? c * 2 : c; }
    /** Ferreiro: VALORES re-sorteia os valores dos mesmos afixos; TUDO sorteia afixos novos. Tier, tipo e chefe ficam. */
    public static Reliquia reforjar(Random r, Reliquia rel, Reforja modo) {
        if (modo == Reforja.TUDO) return gerar(r, rel.tipo(), rel.tier(), rel.kit());
        return new Reliquia(rel.tipo(), rel.tier(), rel.kit(), rel.afixos().stream().map(a -> rolar(r, a.afixo(), rel.tier())).toList());
    }
    /** Essencia extra no bau do chefe: 2 a 4. */
    public static int essenciaBau(Random r) { return 2 + r.nextInt(3); }

    /** Bonus somados de tudo que esta na Bolsa (valores em % ou coracoes, como nos afixos), com teto por afixo. */
    public static Map<Afixo, Double> somar(Collection<Reliquia> bolsa) {
        var m = new EnumMap<Afixo, Double>(Afixo.class);
        for (var r : bolsa) for (var a : r.afixos()) m.merge(a.afixo(), a.valor(), Double::sum);
        m.replaceAll((a, v) -> Math.min(v, teto(a)));
        return m;
    }
    /** Teto do total de cada afixo (evita builds quebradas). */
    public static double teto(Afixo a) {
        return switch (a) { case RECARGA, RESISTENCIA -> 40; case CRITICO -> 50; case ROUBO -> 15; case VIDA -> 20; case VELOCIDADE -> 30; default -> 80; };
    }
    /** Kits com set ativo (3 pecas do mesmo chefe; Mitica conta 2). */
    public static Set<Integer> sets(Collection<Reliquia> bolsa) {
        var conta = new HashMap<Integer, Integer>();
        for (var r : bolsa) conta.merge(r.kit(), r.mitica() ? 2 : 1, Integer::sum);
        var s = new TreeSet<Integer>(); conta.forEach((k, n) -> { if (n >= PECAS_SET) s.add(k); });
        return s;
    }
    /** Passiva do set de cada chefe (texto para tooltip). */
    public static String set(int kit) {
        return switch (kit) {
            case 1 -> "Set de Toji: +10% de dano e +10% de velocidade";
            case 2 -> "Set de Aurora: imune a Wither e +10% de dano";
            case 3 -> "Set de Qual: -15% de recarga";
            case 4 -> "Set de Liebe: -15% de dano mágico recebido e +10% de dano";
            case 6 -> "Set de Zangetsu: +5% de roubo de vida";
            case 12 -> "Set do Raio Absoluto: imune a lentidão e +10% de velocidade";
            case 16 -> "Set de Petelgeuse: +25% de regeneração de mana";
            default -> "Set do Vazio: +5% de dano";
        };
    }
    /** Bonus extra do set somado aos afixos. */
    public static Map<Afixo, Double> bonusSet(int kit) {
        var m = new EnumMap<Afixo, Double>(Afixo.class);
        switch (kit) {
            case 1 -> { m.put(Afixo.DANO, 10.0); m.put(Afixo.VELOCIDADE, 10.0); }
            case 2 -> m.put(Afixo.DANO, 10.0);
            case 3 -> m.put(Afixo.RECARGA, 15.0);
            case 4 -> m.put(Afixo.DANO, 10.0);
            case 6 -> m.put(Afixo.ROUBO, 5.0);
            case 12 -> m.put(Afixo.VELOCIDADE, 10.0);
            case 16 -> m.put(Afixo.MANA, 25.0);
            default -> m.put(Afixo.DANO, 5.0);
        }
        return m;
    }

    /** Pity: aberturas seguidas sem Mitico; devolve se esta abertura e forcada e o novo contador. */
    public static int[] pity(int semMitico) { boolean forca = semMitico + 1 >= PITY; return new int[]{forca ? 1 : 0, semMitico + 1}; }
    public static int depois(int contador, boolean saiuMitico) { return saiuMitico ? 0 : contador; }

    /** Itens do bau de um jogador: ITENS_BAU reliquias do chefe; no maximo uma Mitica (a do pity, se houver). */
    public static List<Reliquia> bau(Random r, int kit, boolean forcaMitico) {
        var out = new ArrayList<Reliquia>(); boolean mitico = false;
        for (int i = 0; i < ITENS_BAU; i++) {
            int t = tier(r, forcaMitico && i == 0); if (t == TIERS && mitico) t = TIERS - 1; mitico |= t == TIERS;
            out.add(gerar(r, Tipo.values()[r.nextInt(Tipo.values().length)], t, kit));
        }
        return out;
    }

    private Reliquias() {}
}
