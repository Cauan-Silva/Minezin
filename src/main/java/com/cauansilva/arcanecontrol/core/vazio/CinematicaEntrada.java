package com.cauansilva.arcanecontrol.core.vazio;

/**
 * Roteiro puro da cutscene de entrada no Vazio (sem Minecraft; testado em VazioTest). Tempo em ticks desde o teleporte.
 * <ol>
 *   <li>0-{@link #BLACKOUT}: tela preta; {@link #BLACKOUT}-{@link #CLAREIA}: clareia na arena com o HUD escondido.</li>
 *   <li>{@link #BLACKOUT}-{@link #REVELA}: camera solta da 1a pessoa e percorre um spline em volta da arena.</li>
 *   <li>{@link #REVELA}-{@link #FALA}: aproxima do chefe, que ruge ({@link #RUGIDO}).</li>
 *   <li>{@link #FALA}-{@link #VOLTA}: fala do chefe em close; {@link #VOLTA}-{@link #FIM}: volta rapida aos olhos do jogador.</li>
 * </ol>
 * Coordenadas relativas ao centro do chao da arena. O jogador olha para +x (o chefe fica em +x).
 */
public final class CinematicaEntrada {
    public static final int BLACKOUT = 40, CLAREIA = 60, REVELA = 140, RUGIDO = 150, FALA = 200, VOLTA = 220, FIM = 240;

    public record V(double x, double y, double z) {
        V mais(V o) { return new V(x + o.x, y + o.y, z + o.z); }
        V menos(V o) { return new V(x - o.x, y - o.y, z - o.z); }
        V vezes(double k) { return new V(x * k, y * k, z * k); }
        double tamanho() { return Math.sqrt(x * x + y * y + z * z); }
    }
    /** Camera: posicao e ponto para onde olha. */
    public record Quadro(V pos, V olha) {}

    static double suave(double u) { u = Math.max(0, Math.min(1, u)); return u * u * (3 - 2 * u); }
    /** Catmull-Rom entre p1 e p2. */
    static V catmull(V p0, V p1, V p2, V p3, double u) {
        double u2 = u * u, u3 = u2 * u;
        return p0.vezes(-.5 * u3 + u2 - .5 * u).mais(p1.vezes(1.5 * u3 - 2.5 * u2 + 1)).mais(p2.vezes(-1.5 * u3 + 2 * u2 + .5 * u)).mais(p3.vezes(.5 * u3 - .5 * u2));
    }
    /** Pontos de controle do passeio: do lado dos jogadores, alto, girando 200 graus ate atras do ombro do chefe. */
    static V[] passeio() {
        V[] p = new V[7];
        for (int i = 0; i < 7; i++) {
            double a = Math.PI + Math.toRadians(-200 * i / 6.0), r = 30 - 6 * i / 6.0, h = 16 - 6 * i / 6.0;
            p[i] = new V(Math.cos(a) * r, h, Math.sin(a) * r);
        }
        return p;
    }
    static V spline(V[] p, double u) {
        int n = p.length - 1; double s = Math.max(0, Math.min(1, u)) * n; int i = Math.min(n - 1, (int) s); double f = s - i;
        return catmull(p[Math.max(0, i - 1)], p[i], p[i + 1], p[Math.min(n, i + 2)], f);
    }

    /**
     * Quadro no tick t. jogador: olhos do jogador; chefe: pes do chefe; alturaChefe: altura do corpo. Antes de CLAREIA
     * e a partir de FIM a camera e a do proprio jogador (devolve o jogador olhando o chefe).
     */
    public static Quadro quadro(double t, V jogador, V chefe, double alturaChefe) {
        V cabeca = chefe.mais(new V(0, alturaChefe * .85, 0)), centro = new V(0, 2, 0);
        V doChefe = jogador.menos(chefe); doChefe = new V(doChefe.x, 0, doChefe.z); double dc = Math.max(1e-6, doChefe.tamanho()); doChefe = doChefe.vezes(1 / dc);
        V close = cabeca.mais(doChefe.vezes(4.5 + alturaChefe * .6)).mais(new V(0, -.3, 0)), perto = cabeca.mais(doChefe.vezes(10 + alturaChefe)).mais(new V(0, 3, 0));
        if (t < BLACKOUT || t >= FIM) return new Quadro(jogador, cabeca);   // o corte para o passeio acontece no preto
        V[] p = passeio();
        if (t < REVELA) { double u = suave((t - BLACKOUT) / (REVELA - BLACKOUT)); return new Quadro(spline(p, u), centro.mais(cabeca.menos(centro).vezes(u))); }
        if (t < FALA) {
            double u = suave((t - REVELA) / (double) (FALA - REVELA));
            V ini = p[p.length - 1], meio = perto;
            V pos = u < .5 ? ini.mais(meio.menos(ini).vezes(suave(u * 2))) : meio.mais(close.menos(meio).vezes(suave((u - .5) * 2)));
            return new Quadro(pos, cabeca);
        }
        if (t < VOLTA) return new Quadro(close, cabeca);
        double u = suave((t - VOLTA) / (double) (FIM - VOLTA));
        return new Quadro(close.mais(jogador.menos(close).vezes(u)), cabeca);
    }

    /** Fala de cada chefe (kit do desafio). */
    public static String fala(int kit) {
        return switch (kit) {
            case 1 -> "Feiticeiros... sempre confiando demais no próprio poder.";
            case 2 -> "As sombras me pertencem. Ajoelhe-se.";
            case 3 -> "Vocês aprenderam a matar com a minha magia. Vou cobrar por isso.";
            case 4 -> "Eu não preciso de magia para esmagar vocês.";
            case 6 -> "Se não consegue me dominar, eu tomo o seu lugar.";
            case 12 -> "Velocidade não salva ninguém do raio.";
            case 16 -> "Que preguiça... que deliciosa preguiça!";
            default -> "Vocês não deviam ter vindo.";
        };
    }
    private CinematicaEntrada() {}
}
