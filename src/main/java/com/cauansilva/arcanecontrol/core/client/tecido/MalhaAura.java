package com.cauansilva.arcanecontrol.core.client.tecido;

/**
 * Tecido de energia para auras (position based, 20 Hz com 3 subpassos), sem Minecraft nem render.
 * <p>
 * Diferente de {@link MalhaTecido} (roupa presa pelo topo e puxada pelo peso), aqui a linha 0 e a BASE presa ao corpo
 * (as ancoras seguem o corpo em coordenadas do mundo, entao o resto da lingua arrasta atras quando ele corre) e o
 * tecido sobe por flutuacao, ondula por turbulencia e "respira": o comprimento de cada lingua pulsa como uma chama.
 * Coordenadas em blocos (metros); o chamador entrega as ancoras do mundo a cada tick.
 */
public final class MalhaAura {
    /** Parametros de uma aura (um JSON por personagem no jogo). */
    public record PerfilAura(double flutuacao, double arrasto, double turbulencia, double rigidez, double pulso, double frequencia) {
        public static final PerfilAura ELETRICA = new PerfilAura(14, 3.0, 10.0, .95, .25, 1.6);
        public PerfilAura {
            if (!(Double.isFinite(flutuacao + arrasto + turbulencia + rigidez + pulso + frequencia)) || rigidez <= 0 || rigidez > 1 || pulso < 0 || pulso >= 1)
                throw new IllegalArgumentException("Perfil de aura invalido");
        }
    }

    private final int colunas, linhas;
    private final double passoX, passoY;
    private final double[][] atual, verlet, anterior;
    private final int[][] ligA;
    private final double[] ligComp, ligK;
    private final boolean[] ligVertical;
    private final PerfilAura perfil;
    private final int semente;
    private double tempo, crescer = 1, abreX, abreZ, ex, ey, ez, eh, ev, impulso = 1, deforma, escala = 1;

    public MalhaAura(int colunas, int linhas, double passoX, double passoY, PerfilAura perfil, int semente) {
        if (colunas < 2 || colunas > 8 || linhas < 2 || linhas > 24 || !(passoX > 0) || !(passoY > 0)) throw new IllegalArgumentException("Dimensoes de aura invalidas");
        this.colunas = colunas; this.linhas = linhas; this.passoX = passoX; this.passoY = passoY;
        this.perfil = perfil == null ? PerfilAura.ELETRICA : perfil; this.semente = semente;
        int n = colunas * linhas;
        atual = new double[n][3]; verlet = new double[n][3]; anterior = new double[n][3];
        int m = 0;
        for (int y = 0; y < linhas; y++) for (int x = 0; x < colunas; x++) {
            if (x + 1 < colunas) m++;
            if (y + 1 < linhas) m++;
            if (x + 1 < colunas && y + 1 < linhas) m += 2;
            if (y + 2 < linhas) m++;
        }
        ligA = new int[m][2]; ligComp = new double[m]; ligK = new double[m]; ligVertical = new boolean[m];
        int k = 0;
        for (int y = 0; y < linhas; y++) for (int x = 0; x < colunas; x++) {
            if (x + 1 < colunas) k = ligar(k, x, y, x + 1, y, 1, false);
            if (y + 1 < linhas) k = ligar(k, x, y, x, y + 1, 1, true);
            if (x + 1 < colunas && y + 1 < linhas) { k = ligar(k, x, y, x + 1, y + 1, .2, true); k = ligar(k, x + 1, y, x, y + 1, .2, true); }
            if (y + 2 < linhas) k = ligar(k, x, y, x, y + 2, .1, true);
        }
    }
    private int ligar(int k, int x, int y, int xx, int yy, double r, boolean vertical) {
        ligA[k][0] = id(x, y); ligA[k][1] = id(xx, yy);
        ligComp[k] = Math.hypot((xx - x) * passoX, (yy - y) * passoY); ligK[k] = r; ligVertical[k] = vertical;
        return k + 1;
    }
    private int id(int x, int y) { return y * colunas + x; }
    public int colunas() { return colunas; }
    public int linhas() { return linhas; }

    /** Posiciona a lingua de pe, subindo das ancoras (direcao +y), parada. */
    public void reset(double[][] ancoras) {
        tempo = 0;
        for (int y = 0; y < linhas; y++) for (int x = 0; x < colunas; x++) {
            int i = id(x, y);
            atual[i][0] = ancoras[x][0]; atual[i][1] = ancoras[x][1] + y * passoY; atual[i][2] = ancoras[x][2];
            System.arraycopy(atual[i], 0, verlet[i], 0, 3); System.arraycopy(atual[i], 0, anterior[i], 0, 3);
        }
    }
    /** Forca horizontal que abre a lingua para fora do corpo (cresce para a ponta); direcao do mundo. */
    public void abertura(double x, double z) { abreX = x; abreZ = z; }
    /**
     * Envelope eliptico (centro do mundo, raio horizontal rh, vertical rv): particulas que saem dele sao trazidas
     * de volta para a superficie, entao as pontas curvam para dentro e o topo da aura fica arredondado. rv <= 0 desliga.
     */
    public void envelope(double cx, double cy, double cz, double rh, double rv) { ex = cx; ey = cy; ez = cz; eh = rh; ev = rv; }
    /**
     * Deformacao do envelope: lobos que giram e sobem com o tempo (amplitude 0..0,5) e escala propria desta lingua,
     * para a silhueta nao virar um ovo liso: algumas linguas furam o contorno, outras ficam por dentro.
     */
    public void deformacao(double amplitude, double escalaLingua) { deforma = Math.max(0, Math.min(.5, amplitude)); escala = Math.max(.5, Math.min(1.8, escalaLingua)); }
    /** Fator do raio do envelope na direcao (dx, dy, dz) normalizada pelo envelope. */
    public static double lobo(double dx, double dy, double dz, double tempo, double amp, int semente) {
        double az = Math.atan2(dz, dx);
        return 1 + amp * (.55 * Math.sin(az * 3 + tempo * 1.3 + semente) + .3 * Math.sin(az * 5 - tempo * 2.1 + dy * 3 + semente * .7) + .35 * Math.sin(dy * 4.5 - tempo * 1.7 + az * 2));
    }
    /** Multiplica a flutuacao (ex.: mais subida quando o dono corre, para a aura cobrir a cabeca). */
    public void impulso(double k) { impulso = Math.max(0, Math.min(3, k)); }
    /** Fator 0..1 do comprimento (entrada da forma: as linguas crescem do corpo). */
    public void crescer(double f) { crescer = Math.max(.02, Math.min(1, f)); }

    private double ruido(double x, double y, double z) {
        // ruido suave barato: soma de senos com fases da semente (deterministico).
        double s = semente * .7311;
        return Math.sin(x * 1.7 + s) * Math.cos(y * 1.3 - s * .5) + .5 * Math.sin(z * 2.1 + y * .9 + s * 1.9);
    }

    /** Avanca 1 tick. ancoras = posicao do mundo da base de cada coluna neste tick (e o do tick anterior em antes). */
    public void tick(double[][] antes, double[][] ancoras) {
        for (double[] a : ancoras) if (!Double.isFinite(a[0] + a[1] + a[2])) { reset(ancoras); return; }
        for (int i = 0; i < atual.length; i++) System.arraycopy(atual[i], 0, anterior[i], 0, 3);
        double dt = 1d / 60, arrasto = Math.exp(-perfil.arrasto() * dt);
        for (int sub = 0; sub < 3; sub++) {
            double u = (sub + 1) / 3.0;
            for (int x = 0; x < colunas; x++) {
                int i = id(x, 0);
                for (int c = 0; c < 3; c++) atual[i][c] = antes[x][c] + (ancoras[x][c] - antes[x][c]) * u;
                System.arraycopy(atual[i], 0, verlet[i], 0, 3);
            }
            for (int i = colunas; i < atual.length; i++) {
                double[] p = atual[i], v = verlet[i];
                double alt = (i / colunas) / (double) (linhas - 1);
                double tx = ruido(p[1] * 1.3 + tempo * 2.2, p[2] * 1.1, tempo * 1.4) * perfil.turbulencia() * alt;
                double tz = ruido(p[0] * 1.2 - tempo * 1.9, p[1] * 1.4 + 3, tempo * 1.7) * perfil.turbulencia() * alt;
                double ty = perfil.flutuacao() * impulso * (.6 + .4 * alt);
                double dx = clamp((p[0] - v[0]) * arrasto + (tx + abreX * alt) * dt * dt, -.12, .12);
                double dy = clamp((p[1] - v[1]) * arrasto + ty * dt * dt, -.12, .12);
                double dz = clamp((p[2] - v[2]) * arrasto + (tz + abreZ * alt) * dt * dt, -.12, .12);
                System.arraycopy(p, 0, v, 0, 3); p[0] += dx; p[1] += dy; p[2] += dz;
            }
            tempo += dt;
            for (int it = 0; it < 6; it++) {
                for (int k = 0; k < ligA.length; k++) {
                    double[] a = atual[ligA[k][0]], b = atual[ligA[k][1]];
                    double x = b[0] - a[0], y = b[1] - a[1], z = b[2] - a[2], d = Math.sqrt(x * x + y * y + z * z);
                    if (d < 1e-10) continue;
                    double wa = ligA[k][0] < colunas ? 0 : 1, wb = ligA[k][1] < colunas ? 0 : 1;
                    if (wa + wb == 0) continue;
                    double alvo = ligComp[k];
                    if (ligVertical[k]) {
                        int coluna = ligA[k][0] % colunas;
                        alvo *= crescer * (1 - perfil.pulso() * .5 * (1 + Math.sin(tempo * perfil.frequencia() * 2 * Math.PI + semente * 1.37 + coluna * .8)));
                    }
                    double q = (d - alvo) / d * ligK[k] * perfil.rigidez() / (wa + wb);
                    a[0] += x * q * wa; a[1] += y * q * wa; a[2] += z * q * wa;
                    b[0] -= x * q * wb; b[1] -= y * q * wb; b[2] -= z * q * wb;
                }
                if (ev > 0) for (int i = colunas; i < atual.length; i++) {
                    double[] p = atual[i];
                    double dx = (p[0] - ex) / eh, dy = (p[1] - ey) / ev, dz = (p[2] - ez) / eh, d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    double lim = escala * lobo(dx, dy, dz, tempo, deforma, 0);
                    if (d > lim) { double k = 1 + (lim / d - 1) * .4; p[0] = ex + dx * eh * k; p[1] = ey + dy * ev * k; p[2] = ez + dz * eh * k; }
                }
            }
        }
    }
    public double[] ponto(int x, int y, double parcial) {
        int i = id(x, y);
        double t = Math.max(0, Math.min(1, parcial));
        return new double[]{anterior[i][0] + (atual[i][0] - anterior[i][0]) * t, anterior[i][1] + (atual[i][1] - anterior[i][1]) * t, anterior[i][2] + (atual[i][2] - anterior[i][2]) * t};
    }
    private static double clamp(double v, double a, double b) { return Math.max(a, Math.min(b, v)); }
}
