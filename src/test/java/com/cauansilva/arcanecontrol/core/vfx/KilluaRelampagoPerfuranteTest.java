package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static com.cauansilva.arcanecontrol.core.vfx.KilluaRelampagoPerfurante.*;
import static org.junit.jupiter.api.Assertions.*;

class KilluaRelampagoPerfuranteTest {
    static final int AQUECE = 40;

    @Test void atravessaOAlvoNoTempoCerto() {
        assertTrue(GOLPE > PREPARO && GOLPE < FIM_DISPARO);
        assertTrue(corpo(GOLPE).z() > KilluaBase.ALVO - .5 && corpo(GOLPE).z() < KilluaBase.ALVO + .5);
        assertTrue(corpo(FIM).z() > KilluaBase.ALVO + 2, "termina atras do alvo");
        assertEquals(0, alphaImpacto(GOLPE - .5));
        assertTrue(alphaImpacto(GOLPE + 1) > .5);
        assertTrue(alphaCorpo(PREPARO + 1) < .7, "vira raio no disparo");
        List<P> f = fio(0, FIM_DISPARO);
        assertTrue(f.get(f.size() - 1).z() >= ALCANCE - .1, "lanca chega ao fim do caminho");
    }

    @Test void exportaParaPrevia() throws Exception {
        var aura = new AuraTecidoKillua();
        for (int i = 0; i < AQUECE; i++) aura.tick(corpo(0), 0, 1);
        var q = new LinkedHashMap<String, Object>();
        for (int t = 0; t <= FIM; t++) {
            aura.tick(corpo(t), 0, 1);
            var m = new LinkedHashMap<String, Object>();
            m.put("corpo", corpo(t)); m.put("alphaCorpo", alphaCorpo(t));
            var li = new ArrayList<Object>(); var rb = new ArrayList<Object>(); var ex = new ArrayList<Object>();
            for (int n = 0; n < AuraTecidoKillua.LINGUAS; n++) { li.add(aura.lingua(n)); if (n % 3 == 0) rb.add(aura.raioBorda(n, 0, t)); }
            for (int k = 0; k < AuraTecidoKillua.EXPLOSIVOS; k++) if (AuraTecidoKillua.alphaExplosivo(k, t) > 0) ex.add(aura.raioExplosivo(k, t));
            m.put("linguas", li); m.put("bordas", rb); m.put("explosivos", ex); m.put("envelope", aura.envelope());
            var ac = new ArrayList<Object>();
            for (int k = 0; k < KilluaGodspeed.ARCOS_CORPO; k++) ac.add(KilluaGodspeed.arcoCorpo(k, t));
            m.put("arcosCorpo", ac);
            m.put("alphaPreparo", alphaPreparo(t));
            var ap = new ArrayList<Object>(); for (int k = 0; k < ARCOS_PES; k++) ap.add(arcoPes(k, t));
            m.put("arcosPes", ap); m.put("ponta", ponta(t));
            m.put("alphaRastro", alphaRastro(t)); m.put("frente", frenteRaio(t)); m.put("alphaQueimado", alphaQueimado(t)); m.put("marca", marca(t)); m.put("calor", calorMarca(t)); m.put("fumaca", fumacaMarca(t)); m.put("brasas", brasasMarca(t));
            var fi = new ArrayList<Object>(); for (int k = 0; k < FIOS; k++) fi.add(fio(k, t));
            var ga = new ArrayList<Object>(); for (int k = 0; k < GALHOS; k++) if (galhoVisivel(k, t)) ga.add(galho(k, t));
            m.put("fios", fi); m.put("galhos", ga);
            m.put("alphaImpacto", alphaImpacto(t));
            var es = new ArrayList<Object>(); for (int k = 0; k < ESPINHOS; k++) es.add(espinho(k, t));
            m.put("espinhos", es);
            m.put("ondas", List.of(onda(0, t), onda(1, t))); m.put("alphaOndas", List.of(alphaOnda(0, t), alphaOnda(1, t)));
            m.put("alphaParalisia", alphaParalisia(t));
            var aa = new ArrayList<Object>(); for (int k = 0; k < ARCOS_ALVO; k++) aa.add(arcoAlvo(k, t));
            m.put("arcosAlvo", aa);
            m.put("faiscasImpacto", faiscasImpacto(t)); m.put("faiscasFreio", faiscasFreio(t));
            q.put(Integer.toString(t), m);
        }
        Files.createDirectories(Path.of("build"));
        try (var w = Files.newBufferedWriter(Path.of("build/killua-relampago-perfurante-samples.json"))) {
            new Gson().toJson(Map.of("quadros", q, "fim", FIM, "preparo", PREPARO, "disparo", FIM_DISPARO, "golpe", GOLPE, "alvo", KilluaBase.ALVO, "alcance", ALCANCE), w);
        }
    }
}
