package com.cauansilva.arcanecontrol.core.client.render;

/** Coordenadas relativas ao conjurador: direita, altura e frente (em blocos). */
public final class RoteiroGojo {
    public record Quadro(double x, double y, double z, double alvoFrente, double fov) {}
    private record Chave(double tick, Quadro quadro) {}
    private static final Chave[] ROXO = {
            new Chave(0, new Quadro(2.4, .4, 3.4, .2, .95)),
            new Chave(16, new Quadro(-2.8, .6, 3.8, 2.0, .8)),
            new Chave(28, new Quadro(-3.0, 1.0, 2.6, 3.2, .85)),
            new Chave(38, new Quadro(2.8, 1.5, -5.0, 12, 1.08)),
            new Chave(70, new Quadro(5.5, 3, -9, 24, 1.05)),
            new Chave(85, new Quadro(3, 1.4, -5, 10, 1))
    };
    private static final Chave[] DOMINIO = {
            new Chave(0, new Quadro(2, .3, 3, 0, .95)),
            new Chave(28, new Quadro(-1.8, .3, 2.6, 0, .75)),
            new Chave(40, new Quadro(-4.0, 2, 4, 0, .95)),
            new Chave(85, new Quadro(-9, 5, -4, 0, 1.1)),
            new Chave(140, new Quadro(7, 4, -9, 0, 1.05)),
            new Chave(160, new Quadro(3, 1.4, -5, 0, 1))
    };

    private static final Chave[] ATOMIC = {
        new Chave(0,new Quadro(2,.3,3,0,.95)),
        new Chave(35,new Quadro(-2,.4,3,0,.8)),
        new Chave(75,new Quadro(-3,1,2,0,.7)),
        new Chave(90,new Quadro(-5,2,-4,0,.9)),
        new Chave(110,new Quadro(-18,10,-20,0,1.15)),
        new Chave(140,new Quadro(-6,3,-7,0,1))
    };
    public static double suave(double k) {
        k = Math.max(0, Math.min(1, k));
        return k * k * (3 - 2 * k);
    }

    public static Quadro amostrar(int tipo, double tick) {
        Chave[] chaves = tipo == 14 ? ATOMIC : tipo == 5 ? DOMINIO : ROXO;
        if (tick <= 0) return chaves[0].quadro;
        for (int i = 1; i < chaves.length; i++) {
            Chave a = chaves[i - 1], b = chaves[i];
            if (tick <= b.tick) {
                double k = suave((tick - a.tick) / (b.tick - a.tick));
                return new Quadro(lerp(a.quadro.x, b.quadro.x, k), lerp(a.quadro.y, b.quadro.y, k),
                        lerp(a.quadro.z, b.quadro.z, k), lerp(a.quadro.alvoFrente, b.quadro.alvoFrente, k),
                        lerp(a.quadro.fov, b.quadro.fov, k));
            }
        }
        return chaves[chaves.length - 1].quadro;
    }

    public static double impacto(int tipo, double tick) {
        double desde = tick - (tipo == 14 ? 90 : tipo == 5 ? 35 : 30);
        return desde < 0 ? 0 : .65 * Math.max(0, 1 - desde / 12);
    }

    private static double lerp(double a, double b, double k) { return a + (b - a) * k; }
}
