package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuraTecidoKilluaTest {
    static final int F = 150, CORRE = 70, SALTO = 100, PARA = 125;
    /** Roteiro da previa: parado na entrada, corre, salta correndo e para. */
    static P corpo(int t) {
        double z = 0, y = 0;
        for (int i = 0; i < t; i++) z += i < CORRE ? 0 : i < PARA - 6 ? .45 : i < PARA ? .45 * (PARA - i) / 6.0 : 0;
        if (t > SALTO && t < SALTO + 16) { double u = (t - SALTO) / 16.0; y = 6.4 * u * (1 - u); }
        return new P(0, y, z);
    }
    static double crescer(int t) { return PerfilVfxGojo.suave((t - KilluaGodspeed.ATIVA) / 8.0); }

    @Test void linguasSobemEArrastamAoCorrer() {
        var a = new AuraTecidoKillua();
        double[] tipParado = null, tipCorrendo = null;
        for (int t = KilluaGodspeed.ATIVA; t <= SALTO - 2; t++) {
            a.tick(corpo(t), 0, crescer(t));
            for (int n = 0; n < AuraTecidoKillua.LINGUAS; n++) for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++)
                assertTrue(Double.isFinite(a.pontoArr(n, x, y)[0]), "estavel");
            int topo = (int) AuraTecidoKillua.ANCORA[1][3] - 1;
            if (t == CORRE - 1) tipParado = a.pontoArr(1, 1, topo);
            if (t == SALTO - 2) tipCorrendo = a.pontoArr(1, 1, topo);
        }
        assertTrue(tipParado[1] > 1.5 + .6, "parado: lingua sobe acima do ombro");
        assertTrue(tipCorrendo[2] < corpo(SALTO - 2).z() - .3, "correndo: ponta fica para tras");
    }

    @Test void exportaParaPrevia() throws Exception {
        var a = new AuraTecidoKillua();
        var q = new LinkedHashMap<String, Object>();
        for (int t = 0; t <= F; t++) {
            var m = new LinkedHashMap<String, Object>();
            P c = corpo(t);
            m.put("corpo", c);
            if (t >= KilluaGodspeed.ATIVA) {
                a.tick(c, 0, crescer(t));
                var li = new ArrayList<Object>(); var rb = new ArrayList<Object>(); var rp = new ArrayList<Object>();
                for (int n = 0; n < AuraTecidoKillua.LINGUAS; n++) {
                    li.add(a.lingua(n));
                    for (int k = 0; k < 2; k++) rb.add(a.raioBorda(n, k, t));
                    if (n % 2 == 0) rp.add(a.raioPonte(n, t));
                }
                var ex = new ArrayList<Object>();
                for (int k = 0; k < AuraTecidoKillua.EXPLOSIVOS; k++) if (AuraTecidoKillua.alphaExplosivo(k, t) > 0) { ex.add(a.raioExplosivo(k, t)); ex.add(a.galhoExplosivo(k, t)); }
                m.put("linguas", li); m.put("envelope", a.envelope()); m.put("explosivos", ex); m.put("bordas", rb); m.put("pontes", rp);
            }
            m.put("crescer", t >= KilluaGodspeed.ATIVA ? crescer(t) : 0);
            q.put(Integer.toString(t), m);
        }
        Files.createDirectories(Path.of("build"));
        try (var w = Files.newBufferedWriter(Path.of("build/killua-aura-tecido-samples.json"))) {
            new Gson().toJson(Map.of("quadros", q, "fim", F, "corre", CORRE, "salto", SALTO, "para", PARA, "ativa", KilluaGodspeed.ATIVA), w);
        }
    }
}
