package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static com.cauansilva.arcanecontrol.core.vfx.KilluaTempestadeDivina.*;
import static org.junit.jupiter.api.Assertions.*;

class KilluaTempestadeDivinaTest {
    @Test void roteiroDaUltimate() {
        var s = new Interrupcao<String>();
        s.iniciar("chefe", "ult", 0, 200, Interrupcao.Resistencia.SUPERARMADURA, null);
        assertEquals(Interrupcao.Resultado.INTERROMPIDO, s.interromper("chefe", "killua", FORCA, PRENDER), "absoluta derruba superarmadura");
        for (int i = 0; i < GOLPES; i++) {
            assertEquals(i, golpeAtual(tickGolpe(i)));
            P d = dirGolpe(i);
            assertEquals(1, Math.sqrt(d.x() * d.x() + d.y() * d.y() + d.z() * d.z()), 1e-9);
            assertTrue(corpo(tickGolpe(i)).y() > -1.0, "golpe acima do chao");
        }
        assertEquals(-1, golpeAtual(FIM_GOLPES));
        assertTrue(corpo(FIM_SUBIDA).y() > CEU - .1, "sobe ao ceu");
        assertTrue(corpo(IMPACTO).y() < .1, "chega ao chao no impacto");
        assertEquals(0, alphaImpacto(IMPACTO - .5));
        assertTrue(clarao(IMPACTO) > .9);
    }

    @Test void exportaParaPrevia() throws Exception {
        var aura = new AuraTecidoKillua();
        for (int i = 0; i < 40; i++) aura.tick(corpo(0), 0, 1);
        var q = new LinkedHashMap<String, Object>();
        int ultimo = -2;
        for (int t = 0; t <= FIM; t++) {
            int g = golpeAtual(t);
            boolean salto = (g >= 0 && g != ultimo) || t == IMPACTO;
            if (salto) { aura = new AuraTecidoKillua(); for (int i = 0; i < 8; i++) aura.tick(corpo(t), Math.toRadians(yaw(t)), 1); }
            if (g >= 0) ultimo = g;
            aura.tick(corpo(t), Math.toRadians(yaw(t)), 1);
            var m = new LinkedHashMap<String, Object>();
            m.put("corpo", corpo(t)); m.put("giro", yaw(t)); m.put("alphaCorpo", alphaCorpo(t)); m.put("golpe", g); m.put("tipo", g >= 0 ? tipoGolpe(g) : -1);
            m.put("alvo", alvoCentro(t));
            var li = new ArrayList<Object>(); var rb = new ArrayList<Object>(); var ex = new ArrayList<Object>();
            for (int n = 0; n < AuraTecidoKillua.LINGUAS; n++) { li.add(aura.lingua(n)); if (n % 3 == 0) rb.add(aura.raioBorda(n, 0, t)); }
            for (int k = 0; k < AuraTecidoKillua.EXPLOSIVOS; k++) if (AuraTecidoKillua.alphaExplosivo(k, t) > 0) ex.add(aura.raioExplosivo(k, t));
            m.put("linguas", li); m.put("bordas", rb); m.put("explosivos", ex); m.put("envelope", aura.envelope());
            var ac = new ArrayList<Object>(); for (int k = 0; k < KilluaGodspeed.ARCOS_CORPO; k++) ac.add(KilluaGodspeed.arcoCorpo(k, t));
            m.put("arcosCorpo", ac);
            m.put("alphaPrisao", alphaPrisao(t)); m.put("alphaRaioPrender", alphaRaioPrender(t)); m.put("raioPrender", raioPrender(t));
            m.put("aneisPrisao", List.of(anelPrisao(0, t), anelPrisao(1, t), anelPrisao(2, t)));
            var ga = new ArrayList<Object>(); var fa = new ArrayList<Object>(); var acs = new ArrayList<Object>();
            for (int i = 0; i < GOLPES; i++) {
                if (segmentoVisivel(i, t)) ga.add(segmentoGaiola(i, t));
                double af = alphaFantasma(i, t);
                if (af > 0) { P p = pontoGolpe(i); fa.add(Map.of("pos", new P(p.x(), p.y() - 1.2, p.z()), "yaw", yawGolpe(i), "tick", tickGolpe(i), "alpha", af)); }
                double aa = alphaAcerto(i, t);
                if (aa > 0) for (int k = 0; k < 6; k++) acs.add(Map.of("alpha", aa, "raio", raioAcerto(i, k, t)));
            }
            m.put("gaiola", ga); m.put("alphaGaiola", alphaGaiola(t)); m.put("fantasmas", fa); m.put("acertos", acs);
            m.put("alphaSubida", alphaSubida(t)); m.put("rastroSubida", alphaSubida(t) > 0 ? rastroSubida(t) : List.of());
            m.put("alphaCeu", alphaCeu(t)); m.put("ceu", new P(0, CEU + 2, ALVO));
            m.put("alphaColuna", alphaColuna(t));
            var co = new ArrayList<Object>(); if (alphaColuna(t) > 0) for (int k = 0; k < COLUNA; k++) co.add(fioColuna(k, t));
            m.put("coluna", co);
            m.put("alphaImpacto", alphaImpacto(t)); m.put("clarao", clarao(t)); m.put("raioExplosao", t >= IMPACTO ? raioExplosao(t) : 0); m.put("chao", chao());
            var es = new ArrayList<Object>(); if (alphaImpacto(t) > 0) for (int k = 0; k < ESTOURO; k++) es.add(raioEstouro(k, t));
            m.put("estouro", es);
            m.put("aneis", List.of(anelChoque(0, t), anelChoque(1, t), anelChoque(2, t))); m.put("alphaAneis", List.of(alphaAnel(0, t), alphaAnel(1, t), alphaAnel(2, t)));
            m.put("alphaCratera", alphaCratera(t)); m.put("calor", calorCratera(t));
            m.put("borda", t >= IMPACTO ? bordaCratera(t) : List.of());
            var ra = new ArrayList<Object>(); if (t >= IMPACTO) for (int k = 0; k < RACHAS; k++) ra.add(racha(k, t));
            m.put("rachas", ra); m.put("faiscas", faiscas(t)); m.put("poeira", poeira(t));
            m.put("alphaParalisia", alphaParalisia(t));
            var aa = new ArrayList<Object>(); for (int k = 0; k < 6; k++) aa.add(arcoAlvo(k, t));
            m.put("arcosAlvo", aa);
            q.put(Integer.toString(t), m);
        }
        Files.createDirectories(Path.of("build"));
        try (var w = Files.newBufferedWriter(Path.of("build/killua-tempestade-samples.json"))) {
            new Gson().toJson(Map.of("quadros", q, "fim", FIM, "prender", PRENDER, "fimGolpes", FIM_GOLPES, "fimSubida", FIM_SUBIDA, "impacto", IMPACTO, "golpes", GOLPES, "intervalo", INTERVALO), w);
        }
    }
}
