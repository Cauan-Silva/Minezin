package com.cauansilva.arcanecontrol.core.vfx;

import java.util.ArrayList;
import java.util.List;

/**
 * Kit base do Killua Zoldyck (Hunter x Hunter), geometria pura sem Minecraft. Origem nos pes do Killua no inicio do
 * cast; x lateral, y para cima, z para frente; t em ticks a partir do cast.
 * <ul>
 * <li>Z Narukami: faiscas na palma, avanco curto e raio azul-branco da palma ao alvo; arcos de paralisia por 1 s.</li>
 * <li>X Rhythm Echo: balanco ritmado com pos-imagens e teleporte de 4 blocos para frente.</li>
 * <li>C Yo-yo de aco: os dois yo-yos saem, fazem um giro de 360 graus e voltam; mobs pequenos sao puxados.</li>
 * <li>V Garras de assassino: some na furtividade por 2 s, reaparece e corta o alvo com a mao em garra (critico +
 * sangramento).</li>
 * </ul>
 */
public final class KilluaBase {
    public record P(double x, double y, double z) {
        P mais(P o) { return new P(x + o.x, y + o.y, z + o.z); }
        P vezes(double k) { return new P(x * k, y * k, z * k); }
        static P lerp(P a, P b, double u) { return new P(a.x + (b.x - a.x) * u, a.y + (b.y - a.y) * u, a.z + (b.z - a.z) * u); }
    }

    /** Distancia do alvo de teste (o servidor usa a mira; o render recebe o ponto real). */
    public static final double ALVO = 6;
    public static final double ALTURA_PEITO = 1.25;

    private static double s(double t) { return PerfilVfxGojo.suave(t); }
    private static double lim(double v) { return Math.max(0, Math.min(1, v)); }
    /** Ruido deterministico em [-1, 1]. */
    static double ruido(int a, int b, int c) {
        long h = a * 73856093L ^ b * 19349663L ^ c * 83492791L;
        h = (h ^ (h >>> 13)) * 0x5bd1e995L;
        h ^= h >>> 15;
        return ((h & 0xFFFF) / 32767.5) - 1;
    }

    /** Raio quebrado entre a e b: segmentos com desvio que some nas pontas; muda de forma a cada 2 ticks. */
    public static List<P> raioQuebrado(P a, P b, int segmentos, double desvio, int semente, double t) {
        int q = (int) Math.floor(t / 2);
        List<P> l = new ArrayList<>();
        for (int i = 0; i <= segmentos; i++) {
            double u = i / (double) segmentos, env = Math.sin(Math.PI * u);
            P base = P.lerp(a, b, u);
            l.add(base.mais(new P(ruido(semente, i, q) * desvio * env, ruido(semente + 7, i, q) * desvio * env,
                    ruido(semente + 13, i, q) * desvio * .4 * env)));
        }
        return l;
    }

    /**
     * Particulas balisticas (faiscas, estilhacos, gotas): n particulas nascem em origem no tick t0 com velocidade vel
     * na direcao dir espalhada por espalha, caem com grav e vivem vida ticks. Cada item: {x,y,z, xAnterior,yAnterior,
     * zAnterior, alpha}; o anterior (1,5 tick antes) desenha o risco de movimento. Param no chao (y = 0).
     */
    public static List<double[]> particulas(int semente, int n, P origem, double t0, double t, double vel, double espalha,
                                            P dir, double grav, double vida) {
        List<double[]> l = new ArrayList<>();
        double a = t - t0;
        if (a < 0 || a > vida) return l;
        for (int k = 0; k < n; k++) {
            double vk = vel * (.45 + .55 * Math.abs(ruido(semente, k, 1)));
            double vx = dir.x() * vk + ruido(semente, k, 2) * espalha, vy = dir.y() * vk + ruido(semente, k, 3) * espalha,
                    vz = dir.z() * vk + ruido(semente, k, 4) * espalha;
            double vidaK = vida * (.55 + .45 * Math.abs(ruido(semente, k, 5)));
            if (a > vidaK) continue;
            double b = Math.max(0, a - 1.5);
            double[] q = new double[7];
            q[0] = origem.x() + vx * a; q[1] = Math.max(.02, origem.y() + vy * a - grav * a * a); q[2] = origem.z() + vz * a;
            q[3] = origem.x() + vx * b; q[4] = Math.max(.02, origem.y() + vy * b - grav * b * b); q[5] = origem.z() + vz * b;
            q[6] = 1 - a / vidaK;
            l.add(q);
        }
        return l;
    }
    /** Anel no plano (u, v) centrado em c. */
    public static List<P> anel(P c, P u, P v, double r, int pontos, double fase) {
        List<P> l = new ArrayList<>();
        for (int i = 0; i <= pontos; i++) {
            double a = 2 * Math.PI * i / pontos + fase;
            l.add(c.mais(u.vezes(Math.cos(a) * r)).mais(v.vezes(Math.sin(a) * r)));
        }
        return l;
    }
    static final P X = new P(1, 0, 0), Y = new P(0, 1, 0), Z = new P(0, 0, 1);
    /** Arco eletrico entre dois pontos aleatorios da superficie do corpo (cilindro de raio .38). */
    static List<P> arcoNoCorpo(P pes, int k, int semente, double t, double desvio) {
        int q = (int) Math.floor(t / 1.5);
        double h0 = .15 + 1.7 * Math.abs(ruido(semente, k, q)), a0 = Math.PI * ruido(semente + 1, k, q);
        double h1 = h0 + ruido(semente + 2, k, q) * .7, a1 = a0 + 1.2 + ruido(semente + 3, k, q);
        P a = pes.mais(new P(Math.cos(a0) * .38, h0, Math.sin(a0) * .3));
        P b = pes.mais(new P(Math.cos(a1) * .42, Math.max(.05, h1), Math.sin(a1) * .32));
        return raioQuebrado(a, b, 6, desvio, semente + k * 3, t * 1.3);
    }

    // ===================================================================== Z — Narukami
    /**
     * Narukami (referencia: HxH 2011, Killua x Youpi): salto alto para frente, estrela de relampago na mao erguida, e
     * a descarga desce como uma coluna de raios atraves do braco ate o alvo, com explosao radial e paralisia de 1 s.
     */
    public static final class Narukami {
        public static final int SALTO = 10, DESCARGA = 15, IMPACTO = 16, PARALISIA = 20, POUSO = 32, FIM = 46;
        /** Compatibilidade com o roteiro antigo: a "carga" e a estrela na mao erguida. */
        public static final int CARGA = DESCARGA;
        public static final double ALTURA = 4.2, AVANCO = 4.4, POUSO_Z = ALVO - 1.2;
        public static final int FLARES = 8, RAIOS_ESTRELA = 7, ARCOS_AURA = 9, RAIOS_TRANCADOS = 3, RAIOS_CEU = 5,
                RISCOS_COLUNA = 72, RAIOS_EXPLOSAO = 32, FAISCAS_VOO = 40, ARCOS_GAIOLA = 5, GALHOS = 5;

        /** Corpo: sobe em arco ate ALTURA, paira durante a estrela e cai ate pousar na frente do alvo. */
        public static P corpo(double t) {
            if (t < SALTO) { double u = t / SALTO; return new P(0, ALTURA * Math.sin(Math.PI / 2 * u), AVANCO * s(u)); }
            if (t < DESCARGA + 2) return new P(0, ALTURA + .12 * Math.sin((t - SALTO) * .6), AVANCO);
            double u = lim((t - DESCARGA - 2) / (POUSO - DESCARGA - 2));
            return new P(0, ALTURA * (1 - u * u), AVANCO + (POUSO_Z - AVANCO) * s(u));
        }
        /** Mao direita: erguida acima da cabeca ate a descarga, depois desce apontando o alvo. */
        public static P mao(double t) {
            P alta = new P(-.35, 2.4, .1), baixa = new P(-.3, .55, .55);
            return corpo(t).mais(P.lerp(alta, baixa, s(lim((t - DESCARGA + 1) / 2))));
        }
        public static P alvo() { return new P(0, ALTURA_PEITO + .35, ALVO); }

        // ---- estrela na mao erguida ----
        public static double alphaEstrela(double t) { return t < 3 || t > DESCARGA + 2 ? 0 : s((t - 3) / 6) * (1 - s((t - DESCARGA) / 2)); }
        public static double alphaCarga(double t) { return alphaEstrela(t); }
        public static double raioEstrela(double t) { return .15 + .35 * s(lim((t - 3) / (DESCARGA - 3))) + .05 * Math.sin(t * 2.3); }
        /** Flare k da estrela: raios longos e curtos alternados, girando devagar no plano x-y. */
        public static P[] flare(int k, double t) {
            double a = 2 * Math.PI * k / FLARES + t * .04, r = raioEstrela(t) * (k % 2 == 0 ? 5.5 : 2.6);
            P c = mao(t);
            return new P[]{c, c.mais(new P(Math.cos(a) * r, Math.sin(a) * r, 0))};
        }
        /** Raios quebrados que saem da estrela para todos os lados. */
        public static List<P> raioEstrela(int k, double t) {
            P c = mao(t);
            int q = (int) (t / 2);
            double a = 2 * Math.PI * (k + .5) / RAIOS_ESTRELA + ruido(k, 500, q) * .4, r = 1.2 + 1.3 * Math.abs(ruido(k, 501, q));
            return raioQuebrado(c, c.mais(new P(Math.cos(a) * r, Math.sin(a) * r, ruido(k, 502, q) * .6)), 7, .18, 510 + k, t);
        }
        public static List<P> anelEstrela(double t) { return anel(mao(t), X, Y, raioEstrela(t) * 2.2, 40, t * .1); }
        /** Faiscas de compatibilidade (estalos curtos em volta da mao). */
        public static final int FAISCAS = 6;
        public static List<P> faisca(int k, double t) {
            P c = mao(t);
            double a = 2 * Math.PI * k / FAISCAS + ruido(k, 1, (int) t) * .8, r = .35 + .2 * Math.abs(ruido(k, 2, (int) t));
            return raioQuebrado(c, c.mais(new P(Math.cos(a) * r, Math.sin(a) * r, ruido(k, 3, (int) t) * .2)), 4, .08, 100 + k, t * 2);
        }

        // ---- aura no corpo durante o salto ----
        public static double alphaAura(double t) { return t < 0 || t > POUSO ? 0 : Math.min(1, t / 3) * (1 - s((t - DESCARGA - 6) / 8)); }
        public static List<P> arcoAura(int k, double t) { return arcoNoCorpo(corpo(t), k, 300, t, .12); }

        // ---- descarga: coluna de raios ----
        public static double alphaColuna(double t) { double a = t - DESCARGA; return a < 0 || a > 10 ? 0 : Math.min(1, a / 1) * (1 - s((a - 4) / 6)); }
        public static final double COLUNA_Z = (AVANCO + ALVO) / 2, COLUNA_R = 2.1, COLUNA_TOPO = 13;
        /** Risco k da coluna: traco vertical caindo rapido (nasce no topo, morre no chao). */
        public static P[] riscoColuna(int k, double t) {
            double a = 2 * Math.PI * Math.abs(ruido(k, 520, 0)), r = COLUNA_R * Math.sqrt(Math.abs(ruido(k, 521, 0)));
            double len = 2 + 3 * Math.abs(ruido(k, 522, 0)), fase = Math.abs(ruido(k, 523, 0)) * 16;
            double y = COLUNA_TOPO - ((t * 2.2 + fase) % (COLUNA_TOPO + len));
            double x = Math.cos(a) * r, z = COLUNA_Z + Math.sin(a) * r * .8;
            return new P[]{new P(x, Math.max(0, y + len), z), new P(x, Math.max(0, y), z)};
        }
        /** Largura do risco k (alguns grossos, a maioria fina). */
        public static double larguraRisco(int k) { return .04 + .16 * Math.pow(Math.abs(ruido(k, 524, 0)), 3); }
        /** Raio principal: da mao ate o alvo, e os trancados em volta dele. */
        public static double alphaRaio(double t) { double a = t - DESCARGA; return a < 0 || a > 9 ? 0 : 1 - s((a - 3) / 6); }
        public static List<P> raio(double t) { return raioQuebrado(mao(t), alvo(), 16, .3, 1, t); }
        public static List<P> raioTrancado(int k, double t) { return raioQuebrado(mao(t), alvo(), 16, .25 + .18 * k, 10 + k * 17, t + k); }
        /** Raios do ceu: descem do alto da coluna, passando pelo Killua, ate o alvo. */
        public static List<P> raioCeu(int k, double t) {
            P topo = new P(ruido(k, 530, 0) * 1.2, COLUNA_TOPO, AVANCO - .6 + ruido(k, 531, 0) * .8);
            return raioQuebrado(topo, alvo().mais(new P(ruido(k, 532, 0) * .4, -.3, 0)), 22, .45, 540 + k, t);
        }
        /** Galhos que saem do raio principal. */
        public static List<P> galho(int k, double t) {
            List<P> r = raio(t);
            P a = r.get(2 + k * 3);
            P fim = a.mais(new P(ruido(k, 5, 0) * 1.5, ruido(k, 6, 0) * .8, ruido(k, 7, 0) * 1.0));
            return raioQuebrado(a, fim, 5, .18, 40 + k, t);
        }

        // ---- impacto ----
        public static double clarao(double t) { double a = t - IMPACTO; return a < 0 || a > 6 ? 0 : 1 - s(a / 6); }
        public static double alphaExplosao(double t) { double a = t - IMPACTO; return a < 0 || a > 10 ? 0 : Math.min(1, a + .5) * (1 - s((a - 3) / 7)); }
        /** Raio k da explosao radial (linhas de velocidade do anime): direcoes numa esfera achatada. */
        public static P[] raioExplosao(int k, double t) {
            double a = t - IMPACTO, g = 2.39996 * k, yv = 1 - 2 * (k + .5) / RAIOS_EXPLOSAO, rr = Math.sqrt(1 - yv * yv);
            P d = new P(Math.cos(g) * rr, yv * .6, Math.sin(g) * rr);
            double comp = (1.5 + 2.5 * Math.abs(ruido(k, 550, 0))) * (.4 + .6 * s(lim(a / 4)));
            P c = alvo().mais(new P(0, -.2, -.3));
            return new P[]{c.mais(d.vezes(.3 + comp * .25)), c.mais(d.vezes(.3 + comp))};
        }
        public static double alphaOnda(double t) { double a = t - IMPACTO; return a < 0 || a > 9 ? 0 : 1 - s(a / 9); }
        public static List<P> ondaVertical(double t) { return anel(alvo().mais(new P(0, 0, -.3)), X, Y, .4 + 2.4 * s(lim((t - IMPACTO) / 9)), 44, 0); }
        public static List<P> ondaChao(double t) { return anel(new P(0, .04, ALVO), X, Z, .5 + 3.5 * s(lim((t - IMPACTO) / 9)), 56, 0); }
        public static List<double[]> faiscasVoo(double t) {
            return particulas(380, FAISCAS_VOO, alvo(), IMPACTO, t, .5, .35, new P(0, .2, 0), .02, 16);
        }

        // ---- paralisia (1 s) ----
        public static double alphaParalisia(double t) {
            double a = t - IMPACTO; return a < 0 || a > PARALISIA ? 0 : Math.min(1, a / 2) * (1 - s((a - PARALISIA + 5) / 5));
        }
        public static List<P> arcoParalisia(int k, double t) {
            int q = (int) Math.floor(t / 2);
            double h0 = .3 + 1.6 * Math.abs(ruido(k, 8, q)), a0 = Math.PI * 2 * ruido(k, 9, q);
            P a = new P(Math.cos(a0) * .4, h0, ALVO + Math.sin(a0) * .35);
            P b = new P(Math.cos(a0 + 1.6) * .4, h0 + ruido(k, 10, q) * .5, ALVO + Math.sin(a0 + 1.6) * .35);
            return raioQuebrado(a, b, 6, .14, 70 + k, t);
        }
        public static List<P> arcoGaiola(int k, double t) {
            double a = 2 * Math.PI * k / ARCOS_GAIOLA + t * .25;
            P base = new P(Math.cos(a) * .55, .05, ALVO + Math.sin(a) * .5);
            P topo = new P(Math.cos(a + .9) * .45, 2.0, ALVO + Math.sin(a + .9) * .4);
            return raioQuebrado(base, topo, 9, .14, 400 + k, t);
        }
        public static List<P> anelParalisia(double t) {
            List<P> l = anel(new P(0, .05, ALVO), X, Z, .75, 30, t * .3);
            int q = (int) (t / 2);
            for (int i = 0; i < l.size(); i++) l.set(i, l.get(i).mais(new P(0, Math.abs(ruido(i, 410, q)) * .12, 0)));
            return l;
        }
        /** Poeira e faiscas do pouso. */
        public static List<double[]> pouso(double t) {
            return particulas(590, 16, new P(0, .1, POUSO_Z), POUSO, t, .1, .08, new P(0, .5, 0), .004, 14);
        }
        private Narukami() {}
    }

    // ===================================================================== X — Rhythm Echo
    public static final class Echo {
        public static final int PREPARO = 4, RITMO = 10, TELEPORTE = PREPARO + RITMO, FIM = 32;
        public static final double DISTANCIA = 4, BALANCO = .9, VIDA_IMAGEM = 12;
        public static final int IMAGENS = 5;

        /** Corpo: balanca de um lado para o outro avancando 1 bloco, e no teleporte salta para 4 blocos. */
        public static P corpo(double t) {
            double r = lim((t - PREPARO) / RITMO);
            double x = BALANCO * Math.sin(r * Math.PI * 2.5) * (1 - r * .4);
            double z = t < TELEPORTE ? r : DISTANCIA;
            return t < TELEPORTE ? new P(x, 0, z) : new P(0, 0, z);
        }
        /** Tick em que nasce a pos-imagem n. */
        public static double nasce(int n) { return PREPARO + 1 + n * (RITMO - 2) / (double) (IMAGENS - 1); }
        public static P posImagem(int n) { return corpo(Math.min(nasce(n), TELEPORTE - .01)); }
        /** Pos-imagens ficam paradas e somem; todas tremem no teleporte (o alvo nao sabe qual e real). */
        public static double alphaImagem(int n, double t) {
            double a = t - nasce(n);
            if (a < 0) return 0;
            return .55 * (1 - s(a / VIDA_IMAGEM)) + (t >= TELEPORTE && t < TELEPORTE + 3 ? .15 : 0);
        }
        /** Riscos de velocidade entre a ultima posicao e o destino. */
        public static double alphaRiscos(double t) { double a = t - TELEPORTE; return a < 0 || a > 6 ? 0 : 1 - a / 6; }
        public static P[] risco(int k, double t) {
            double y = .2 + 1.6 * (k + .5) / 8, x = ruido(k, 11, 0) * .35;
            double cauda = 1 + 3 * s(lim((t - TELEPORTE) / 6));
            return new P[]{new P(x, y, Math.min(DISTANCIA - .3, cauda)), new P(x, y, DISTANCIA - .3)};
        }
        /** Corpo real visivel (some por 1 tick no teleporte). */
        public static double alphaCorpo(double t) { return t >= TELEPORTE - .5 && t < TELEPORTE + .5 ? 0 : 1; }
        // ---- camadas extras ----
        /** Ondas no chao onde cada pos-imagem pisou. */
        public static double alphaPasso(int n, double t) { double a = t - nasce(n); return a < 0 || a > 10 ? 0 : 1 - s(a / 10); }
        public static List<P> passo(int n, double t) {
            P p = posImagem(n);
            return anel(new P(p.x(), .04, p.z()), X, Z, .2 + .9 * s(lim((t - nasce(n)) / 10)), 28, 0);
        }
        /** Poeira levantada a cada passo. */
        public static List<double[]> poeira(int n, double t) {
            P p = posImagem(n);
            return particulas(500 + n, 8, new P(p.x(), .1, p.z()), nasce(n), t, .06, .06, new P(0, 1, 0), .002, 16);
        }
        /** Borrao entre pos-imagens vizinhas na altura do peito, da cabeca e das pernas (motion blur do ritmo). */
        public static double alphaBorrao(double t) { return t < PREPARO + 1 || t > TELEPORTE + 8 ? 0 : (1 - s((t - TELEPORTE) / 8)) * .8; }
        public static List<P> borrao(int altura, double t) {
            List<P> l = new ArrayList<>();
            double h = altura == 0 ? 1.0 : altura == 1 ? 1.5 : .5;
            double fimT = Math.min(t, TELEPORTE - .01);
            for (double u = PREPARO + 1; u <= fimT; u += .5) { P c = corpo(u); l.add(new P(c.x(), h, c.z())); }
            return l;
        }
        /** Estouro de deslocamento na saida e na chegada do teleporte. */
        public static double alphaEstouro(double t) { double a = t - TELEPORTE; return a < 0 || a > 9 ? 0 : 1 - s(a / 9); }
        public static List<P> estouro(boolean chegada, int camada, double t) {
            P c = chegada ? new P(0, .05 + camada * .55, DISTANCIA) : corpo(TELEPORTE - .01).mais(new P(0, .05 + camada * .55, 0));
            double r = (chegada ? .3 : .5) + (chegada ? 1.7 : 1.1) * s(lim((t - TELEPORTE) / 9)) * (1 - camada * .25);
            return anel(c, X, Z, r, 36, camada);
        }
        public static List<double[]> poeiraChegada(double t) {
            return particulas(560, 22, new P(0, .1, DISTANCIA), TELEPORTE, t, .14, .1, new P(0, .35, 0), .004, 20);
        }
        /** Linhas de vento convergindo para o ponto de chegada. */
        public static P[] vento(int k, double t) {
            double a = 2 * Math.PI * k / 10, u = s(lim((t - TELEPORTE + 1) / 5));
            double r0 = 2.2 - 1.6 * u, r1 = r0 + .8 * (1 - u);
            double h = .3 + 1.3 * Math.abs(ruido(k, 570, 0));
            return new P[]{new P(Math.cos(a) * r1, h, DISTANCIA + Math.sin(a) * r1), new P(Math.cos(a) * r0, h, DISTANCIA + Math.sin(a) * r0)};
        }
        private Echo() {}
    }

    // ===================================================================== C — Yo-yo de aco
    public static final class Yoyo {
        public static final int LANCA = 6, GIRO = 20, RECOLHE = 8, FIM = LANCA + GIRO + RECOLHE + 4;
        public static final double RAIO = 3.5, ALTURA = 1.0, PUXA_DE = 5, PUXA_ATE = 2.2;
        public static final int PONTOS_FIO = 12;

        public static double raio(double t) {
            if (t < LANCA) return RAIO * s(t / LANCA);
            if (t < LANCA + GIRO) return RAIO;
            return RAIO * (1 - s((t - LANCA - GIRO) / RECOLHE));
        }
        /** Angulo do yo-yo direito (0 = frente); o esquerdo fica do lado oposto. */
        public static double angulo(double t) { return 2 * Math.PI * s(lim((t - LANCA) / GIRO)); }
        /** Mao que segura o fio; gira junto com o corpo (o tronco da a volta inteira com os yo-yos). */
        public static P mao(int lado, double t) {
            double a = angulo(t), x = lado * .55, z = .3;
            return new P(x * Math.cos(a) + z * Math.sin(a), 1.3, -x * Math.sin(a) + z * Math.cos(a));
        }
        public static P yoyo(int lado, double t) {
            double a = angulo(t) + (lado > 0 ? 0 : Math.PI), r = raio(t);
            return new P(Math.sin(a) * r * lado, ALTURA + .1 * Math.sin(t * .8), Math.cos(a) * r);
        }
        /** Fio esticado com leve barriga para baixo. */
        public static List<P> fio(int lado, double t) {
            P a = mao(lado, t), b = yoyo(lado, t);
            List<P> l = new ArrayList<>();
            double barriga = .25 * (1 - lim(raio(t) / RAIO) * .7);
            for (int i = 0; i < PONTOS_FIO; i++) {
                double u = i / (PONTOS_FIO - 1d);
                l.add(P.lerp(a, b, u).mais(new P(0, -barriga * Math.sin(Math.PI * u), 0)));
            }
            return l;
        }
        /** Rotacao propria do yo-yo (radianos). */
        public static double giroProprio(double t) { return t * 1.7; }
        /** Rastro: arco que fica atras do yo-yo durante o giro. */
        public static double alphaRastro(double t) {
            return t < LANCA || t > LANCA + GIRO + 2 ? 0 : Math.min(1, (t - LANCA) / 2) * (1 - s((t - LANCA - GIRO + 2) / 4));
        }
        public static P rastro(int lado, double u, double t) {
            double a = angulo(t) - u * 1.4 + (lado > 0 ? 0 : Math.PI);
            return new P(Math.sin(a) * RAIO * lado, ALTURA, Math.cos(a) * RAIO);
        }
        /** Mob pequeno a frente: puxado para dentro do giro. */
        public static double puxao(double t) { return PUXA_DE + (PUXA_ATE - PUXA_DE) * s(lim((t - LANCA - 4) / 10)); }
        /** Acerto: o yo-yo passa pelo angulo do mob (frente, 0 e 2pi) enquanto gira. */
        public static double acerto(double t) {
            double best = 0;
            for (int lado : new int[]{1, -1}) {
                P y = yoyo(lado, t);
                double d = Math.hypot(y.x(), y.z() - Math.min(RAIO, puxao(t)));
                if (t > LANCA) best = Math.max(best, lim(1 - d / .9));
            }
            return best;
        }
        // ---- camadas extras ----
        /** Eixo do yo-yo: direcao radial horizontal (o disco fica de pe, alinhado ao movimento). */
        public static P eixo(int lado, double t) {
            P y = yoyo(lado, t);
            double r = Math.hypot(y.x(), y.z());
            return r < 1e-6 ? X : new P(y.x() / r, 0, y.z() / r);
        }
        /** Faixa larga de motion blur (borda de baixo 0, de cima 1) ao longo do rastro. */
        public static P faixa(int lado, double u, double t, int borda) {
            P p = rastro(lado, u, t);
            return p.mais(new P(0, (borda == 0 ? -1 : 1) * .2 * (1 - u * .6), 0));
        }
        /** Poeira levantada no chao sob o yo-yo. */
        public static List<double[]> poeira(int lado, double t) {
            List<double[]> l = new ArrayList<>();
            for (int k = 0; k < 6; k++) {
                double t0 = LANCA + k * 3.2;
                double a = angulo(t0) + (lado > 0 ? 0 : Math.PI);
                P o = new P(Math.sin(a) * RAIO * lado, .05, Math.cos(a) * RAIO);
                l.addAll(particulas(600 + k * 2 + (lado > 0 ? 0 : 1), 6, o, t0, t, .07, .05, new P(0, 1, 0), .002, 14));
            }
            return l;
        }
        /** Onda de vento cortante que sai no fim do giro. */
        public static double alphaOnda(double t) { double a = t - LANCA - GIRO + 3; return a < 0 || a > 10 ? 0 : 1 - s(a / 10); }
        public static List<P> onda(int camada, double t) {
            double r = RAIO + .3 + 2.2 * s(lim((t - LANCA - GIRO + 3) / 10));
            return anel(new P(0, ALTURA + (camada - 1) * .18, 0), X, Z, r - camada * .15, 64, 0);
        }
        /** Faiscas de metal no mob quando o yo-yo acerta. */
        public static List<double[]> faiscasAcerto(double t) {
            List<double[]> l = new ArrayList<>();
            for (double t0 : new double[]{LANCA + 2, LANCA + GIRO - 1}) l.addAll(particulas(650 + (int) t0, 16, new P(0, .7, Math.min(RAIO, puxao(t0)) - .2), t0, t, .35, .25, new P(0, .5, -.3), .02, 10));
            return l;
        }
        /** Linhas de puxao: tracos de vento do mob para o centro do giro. */
        public static double alphaPuxao(double t) { return t < LANCA + 3 || t > LANCA + 15 ? 0 : Math.sin(Math.PI * (t - LANCA - 3) / 12); }
        public static P[] puxaoLinha(int k, double t) {
            double m = puxao(t), x = (k - 2) * .18, ph = ((t * .12 + k * .23) % 1);
            return new P[]{new P(x, .3 + k * .25, m + .8 - ph * .6), new P(x, .3 + k * .25, m + .2 - ph * .6)};
        }
        private Yoyo() {}
    }

    // ===================================================================== V — Garras de assassino
    public static final class Garras {
        public static final int SOME = 8, FURTIVO = 40, GOLPE = SOME + FURTIVO, CORTE = 6, SANGRA = 30, FIM = GOLPE + SANGRA;
        public static final double PARADA = ALVO - 1.1;
        public static final int CORTES = 4, PASSOS = 5, UNHAS = 5, GOTAS = 14;

        /** Corpo: anda ate o alvo enquanto invisivel. */
        public static P corpo(double t) { return new P(.15, 0, PARADA * s(lim((t - SOME) / (FURTIVO - 4)))); }
        /** Visibilidade do corpo: some (0,08 = cintilacao quase invisivel) e reaparece 2 ticks antes do golpe. */
        public static double alphaCorpo(double t) {
            if (t < SOME) return 1 - .92 * s(t / SOME);
            if (t < GOLPE - 2) return .08 + .04 * Math.sin(t * 1.3);
            return Math.min(1, .08 + (t - GOLPE + 2) / 2);
        }
        /** Fumaca escura que sai quando ele some. */
        public static double alphaFumaca(double t) { return t < 0 || t > SOME + 8 ? 0 : Math.sin(Math.PI * t / (SOME + 8)); }
        public static P fumaca(int k, double t) {
            double a = k * 2.399, r = .3 + .5 * lim(t / (SOME + 8));
            return new P(Math.cos(a) * r, .3 + 1.5 * ((k * .37) % 1) + t * .03, Math.sin(a) * r);
        }
        /** Unhas da mao direita: alongam no reaparecimento. */
        public static double unha(double t) { return t < GOLPE - 4 ? 0 : .35 * s((t - GOLPE + 4) / 3) * (1 - s((t - GOLPE - 16) / 6)); }
        public static P mao(double t) {
            double sw = s(lim((t - GOLPE) / 3));
            return corpo(t).mais(new P(-.35 + .7 * sw, 1.7 - .8 * sw, .5));
        }
        /** Corte k: arco diagonal sobre o alvo, desenhado de cima-esquerda para baixo-direita. */
        public static double alphaCorte(double t) { double a = t - GOLPE; return a < 0 || a > CORTE + 8 ? 0 : 1 - s((a - CORTE) / 8); }
        public static P corte(int k, double u, double t) {
            double pr = lim((t - GOLPE) / 2.5), v = Math.min(u, pr);
            double off = (k - (CORTES - 1) / 2.0) * .16;
            return new P(-.55 + 1.1 * v + off, 1.85 - 1.2 * v + off * .4, ALVO - .45 - .15 * Math.sin(Math.PI * v));
        }
        public static double progressoCorte(double t) { return lim((t - GOLPE) / 2.5); }
        /** Gotas de sangue (sangramento): saem do corte e caem. */
        public static double alphaSangue(double t) { double a = t - GOLPE; return a < 0 || a > SANGRA ? 0 : 1 - s((a - SANGRA + 8) / 8); }
        public static P gota(int k, double t) {
            double a = t - GOLPE - (k % 5) * 3;
            if (a < 0) return null;
            a = a % 15;
            double u = (k + .5) / GOTAS;
            double x = -.4 + .8 * u + ruido(k, 20, 0) * .1, y0 = 1.7 - 1.0 * u;
            double vx = ruido(k, 21, 0) * .03, vz = -.04 - .02 * Math.abs(ruido(k, 22, 0));
            double y = y0 + .08 * a - .012 * a * a;
            return y < 0 ? null : new P(x + vx * a, y, ALVO - .45 + vz * a);
        }
        /** Estrela do critico sobre o alvo. */
        public static double critico(double t) { double a = t - GOLPE; return a < 0 || a > 10 ? 0 : Math.sin(Math.PI * a / 10); }
        // ---- camadas extras ----
        /** Fragmentos que se soltam do corpo e sobem enquanto ele some (dissolucao). */
        public static List<double[]> fragmentos(double t) {
            List<double[]> l = new ArrayList<>();
            for (int k = 0; k < 4; k++) l.addAll(particulas(700 + k, 10, corpo(k * 2.5).mais(new P(0, .4 + k * .4, 0)), k * 2.5, t, .05, .05, new P(0, 1, 0), -.001, 18));
            return l;
        }
        /** Aura assassina: fios escuros subindo em espiral enquanto furtivo. */
        public static double alphaAura(double t) { return t < SOME - 2 || t > GOLPE ? 0 : Math.min(1, (t - SOME + 2) / 4) * (1 - s((t - GOLPE + 6) / 6)); }
        public static List<P> fioAura(int k, double t) {
            P c = corpo(t);
            List<P> l = new ArrayList<>();
            double a0 = 2 * Math.PI * k / 6;
            for (int i = 0; i < 10; i++) {
                double u = i / 9.0, a = a0 + u * 2.2 + t * .15, r = .45 - .2 * u;
                l.add(c.mais(new P(Math.cos(a) * r, .1 + u * 2.1 + .15 * Math.sin(t * .3 + k), Math.sin(a) * r)));
            }
            return l;
        }
        /** Passos furtivos: ondinhas quase invisiveis no chao. */
        public static double alphaPassoFurtivo(int n, double t) { double t0 = SOME + 4 + n * 7, a = t - t0; return a < 0 || a > 9 ? 0 : (1 - a / 9) * .6; }
        public static List<P> passoFurtivo(int n, double t) {
            double t0 = SOME + 4 + n * 7;
            P c = corpo(t0);
            return anel(new P(c.x(), .04, c.z()), X, Z, .15 + .6 * s(lim((t - t0) / 9)), 24, 0);
        }
        /** Rastro do braco no golpe (arco que a mao desenha). */
        public static double alphaBraco(double t) { double a = t - GOLPE; return a < -1 || a > 7 ? 0 : 1 - s((a - 2) / 5); }
        public static List<P> rastroBraco(double t) {
            List<P> l = new ArrayList<>();
            double fim = Math.min(t, GOLPE + 3);
            for (double u = GOLPE - 1; u <= fim; u += .25) l.add(mao(u));
            return l;
        }
        /** Meia-lua de corte que atravessa o alvo e se afasta. */
        public static double alphaMeiaLua(double t) { double a = t - GOLPE - 1; return a < 0 || a > 9 ? 0 : 1 - s(a / 9); }
        public static List<P> meiaLua(double t) {
            double a = lim((t - GOLPE - 1) / 9), d = ALVO - .3 + 2.5 * s(a), r = .9 + .5 * a;
            List<P> l = new ArrayList<>();
            for (int i = 0; i <= 20; i++) { double v = -1.1 + 2.2 * i / 20.0; l.add(new P(Math.sin(v) * r, 1.15 - Math.cos(v) * r * .35 + v * .5, d + (1 - Math.cos(v)) * -.3)); }
            return l;
        }
        /** Jorro de sangue no corte e manchas no chao. */
        public static List<double[]> jorro(double t) {
            return particulas(760, 36, new P(0, 1.3, ALVO - .45), GOLPE + 1, t, .22, .12, new P(.3, .4, -.5), .016, 24);
        }
        public static double alphaMancha(double t) { double a = t - GOLPE - 6; return a < 0 ? 0 : Math.min(1, a / 3) * (1 - s((a - SANGRA + 10) / 8)); }
        public static List<double[]> manchas() {
            List<double[]> l = new ArrayList<>();
            for (int k = 0; k < 7; k++) l.add(new double[]{ruido(k, 780, 0) * .6 + .2, .03, ALVO - .9 - Math.abs(ruido(k, 781, 0)) * .8, .1 + .12 * Math.abs(ruido(k, 782, 0))});
            return l;
        }
        /** Raios de impacto (tracos radiais brancos e vermelhos) no instante do critico. */
        public static double alphaImpacto(double t) { double a = t - GOLPE; return a < 0 || a > 4 ? 0 : 1 - a / 4; }
        private Garras() {}
    }

    private KilluaBase() {}
}
