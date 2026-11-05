package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static com.cauansilva.arcanecontrol.core.vfx.KilluaWhirlpool.*;
import static org.junit.jupiter.api.Assertions.*;

class KilluaWhirlpoolTest {
    @Test void contraAtaqueSaiDentroDaJanelaDoSistema() {
        var s = new Interrupcao<String>();
        List<String> contra = new ArrayList<>();
        s.iniciar("atacante", "golpe", 0, 20, Interrupcao.Resistencia.FIRME, null);
        s.guardar("killua", "whirlpool", ENTRADA, JANELA, contra::add);
        assertEquals(Interrupcao.Defesa.CONTRA_ATAQUE, s.receberGolpe("killua", "atacante", ACERTO));
        assertEquals(List.of("atacante"), contra);
        assertFalse(s.canalizando("atacante", ACERTO + 1), "atacante interrompido");
        assertTrue(ACERTO >= ENTRADA && ACERTO < ENTRADA + JANELA);
        assertEquals(ATACANTE + ATRAS, corpo(REAPARECE).z(), 1e-9);
        assertTrue(atacante(GOLPE + 8).z() < atacante(GOLPE).z() - 1, "atacante arremessado");
        assertEquals(0, alphaGuarda(ACERTO + 1.5));
    }

    @Test void exportaParaPrevia() throws Exception {
        var aura = new AuraTecidoKillua();
        for (int i = 0; i < 40; i++) aura.tick(corpo(0), 0, 1);
        var q = new LinkedHashMap<String, Object>();
        for (int t = 0; t <= FIM; t++) {
            if (t == REAPARECE) aura = new AuraTecidoKillua();
            if (t == REAPARECE) for (int i = 0; i < 25; i++) aura.tick(corpo(t), Math.PI, 1);
            aura.tick(corpo(t), Math.toRadians(giro(t)), 1);
            var m = new LinkedHashMap<String, Object>();
            m.put("corpo", corpo(t)); m.put("giro", giro(t)); m.put("alphaCorpo", alphaCorpo(t)); m.put("atacante", atacante(t));
            var li = new ArrayList<Object>(); var rb = new ArrayList<Object>(); var ex = new ArrayList<Object>();
            for (int n = 0; n < AuraTecidoKillua.LINGUAS; n++) { li.add(aura.lingua(n)); if (n % 3 == 0) rb.add(aura.raioBorda(n, 0, t)); }
            for (int k = 0; k < AuraTecidoKillua.EXPLOSIVOS; k++) if (AuraTecidoKillua.alphaExplosivo(k, t) > 0) ex.add(aura.raioExplosivo(k, t));
            m.put("linguas", li); m.put("bordas", rb); m.put("explosivos", ex); m.put("envelope", aura.envelope());
            var ac = new ArrayList<Object>(); for (int k = 0; k < KilluaGodspeed.ARCOS_CORPO; k++) ac.add(KilluaGodspeed.arcoCorpo(k, t));
            m.put("arcosCorpo", ac);
            m.put("alphaGuarda", alphaGuarda(t));
            var cu = new ArrayList<Object>(); for (int k = 0; k < ARCOS_CUPULA; k++) cu.add(arcoCupula(k, t));
            m.put("cupula", cu); m.put("aneis", List.of(anelGuarda(0, t), anelGuarda(1, t)));
            m.put("alphaSumico", alphaSumico(t)); m.put("sumico", List.of(raioSumico(0, t), raioSumico(1, t))); m.put("faiscasSumico", faiscasSumico(t));
            m.put("alphaRedemoinho", alphaRedemoinho(t));
            var es = new ArrayList<Object>(); for (int k = 0; k < ESPIRAIS; k++) es.add(espiral(k, t));
            m.put("espirais", es); m.put("arcoChute", t >= REAPARECE && t <= FIM_CHUTE + 2 ? arcoChute(t) : List.of());
            m.put("alphaImpacto", alphaImpacto(t));
            var ri = new ArrayList<Object>(); for (int k = 0; k < 10; k++) ri.add(raioImpacto(k, t));
            m.put("raiosImpacto", ri); m.put("faiscasImpacto", faiscasImpacto(t));
            m.put("alphaParalisia", alphaParalisia(t));
            var aa = new ArrayList<Object>(); for (int k = 0; k < ARCOS_ALVO; k++) aa.add(arcoAlvo(k, t));
            m.put("arcosAlvo", aa);
            q.put(Integer.toString(t), m);
        }
        Files.createDirectories(Path.of("build"));
        try (var w = Files.newBufferedWriter(Path.of("build/killua-whirlpool-samples.json"))) {
            new Gson().toJson(Map.of("quadros", q, "fim", FIM, "entrada", ENTRADA, "acerto", ACERTO, "reaparece", REAPARECE, "golpe", GOLPE, "fimChute", FIM_CHUTE), w);
        }
    }
}
