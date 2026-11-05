package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static com.cauansilva.arcanecontrol.core.vfx.KilluaSocoRelampago.*;
import static org.junit.jupiter.api.Assertions.*;

class KilluaSocoRelampagoTest {
    @Test void socoInterrompeTecnicaFirme() {
        var s = new Interrupcao<String>();
        s.iniciar("alvo", "carga", 0, 40, Interrupcao.Resistencia.FIRME, null);
        assertEquals(Interrupcao.Resultado.INTERROMPIDO, s.interromper("alvo", "killua", FORCA, IMPACTO));
        s.iniciar("chefe", "ult", 0, 40, Interrupcao.Resistencia.SUPERARMADURA, null);
        assertEquals(Interrupcao.Resultado.RESISTIU, s.interromper("chefe", "killua", FORCA, IMPACTO));
        assertTrue(punho(IMPACTO).z() > ALVO - .6 && punho(IMPACTO).z() < ALVO, "punho chega no alvo no impacto");
        assertEquals(0, alphaImpacto(IMPACTO - .5));
        assertTrue(alvo(IMPACTO + 8).z() > ALVO + 3, "alvo arremessado");
    }

    @Test void exportaParaPrevia() throws Exception {
        var aura = new AuraTecidoKillua();
        for (int i = 0; i < 40; i++) aura.tick(corpo(0), 0, 1);
        var q = new LinkedHashMap<String, Object>();
        for (int t = 0; t <= FIM; t++) {
            aura.tick(corpo(t), 0, 1);
            var m = new LinkedHashMap<String, Object>();
            m.put("corpo", corpo(t)); m.put("giro", 0); m.put("alphaCorpo", 1); m.put("atacante", alvo(t));
            var li = new ArrayList<Object>(); var rb = new ArrayList<Object>(); var ex = new ArrayList<Object>();
            for (int n = 0; n < AuraTecidoKillua.LINGUAS; n++) { li.add(aura.lingua(n)); if (n % 3 == 0) rb.add(aura.raioBorda(n, 0, t)); }
            for (int k = 0; k < AuraTecidoKillua.EXPLOSIVOS; k++) if (AuraTecidoKillua.alphaExplosivo(k, t) > 0) ex.add(aura.raioExplosivo(k, t));
            m.put("linguas", li); m.put("bordas", rb); m.put("explosivos", ex); m.put("envelope", aura.envelope());
            var ac = new ArrayList<Object>(); for (int k = 0; k < KilluaGodspeed.ARCOS_CORPO; k++) ac.add(KilluaGodspeed.arcoCorpo(k, t));
            m.put("arcosCorpo", ac);
            m.put("alphaCarga", alphaCarga(t)); m.put("punho", punho(t)); m.put("raioEsfera", raioEsfera(t)); m.put("espiralBraco", espiralBraco(t));
            var ap = new ArrayList<Object>(); for (int k = 0; k < ARCOS_PUNHO; k++) ap.add(arcoPunho(k, t));
            m.put("arcosPunho", ap);
            m.put("alphaImpacto", alphaImpacto(t)); m.put("pontoImpacto", pontoImpacto());
            var co = new ArrayList<Object>(); for (int k = 0; k < CONE; k++) co.add(cone(k, t));
            m.put("cone", co);
            m.put("aneis", List.of(anelChoque(0, t), anelChoque(1, t))); m.put("alphaAneis", List.of(alphaAnel(0, t), alphaAnel(1, t)));
            var ra = new ArrayList<Object>(); for (int k = 0; k < RACHAS; k++) ra.add(racha(k, t));
            m.put("rachas", ra); m.put("alphaRacha", alphaRacha(t));
            m.put("alphaParalisia", alphaParalisia(t));
            var aa = new ArrayList<Object>(); for (int k = 0; k < ARCOS_ALVO; k++) aa.add(arcoAlvo(k, t));
            m.put("arcosAlvo", aa); m.put("faiscasImpacto", faiscasImpacto(t)); m.put("poeira", poeira(t));
            q.put(Integer.toString(t), m);
        }
        Files.createDirectories(Path.of("build"));
        try (var w = Files.newBufferedWriter(Path.of("build/killua-soco-relampago-samples.json"))) {
            new Gson().toJson(Map.of("quadros", q, "fim", FIM, "carga", CARGA, "impacto", IMPACTO), w);
        }
    }
}
