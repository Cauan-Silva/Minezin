package com.cauansilva.arcanecontrol.core.vfx;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.cauansilva.arcanecontrol.core.vfx.KilluaGodspeed.*;

class KilluaGodspeedTest {
    static final int F = 150;
    @Test void entradaEAura() {
        assertEquals(0, alphaAura(ATIVA - .5), 1e-9);
        assertTrue(alphaAura(ATIVA + 10) > .6, "aura acesa depois do estouro");
        assertTrue(alphaAura(300) > .6, "aura continua no meio da forma");
        assertEquals(0, alphaAura(DURACAO), 1e-6, "some no fim");
        assertEquals(0, alphaEstouro(ATIVA - .5), 1e-9);
        assertTrue(alphaEstouro(ATIVA + 2) > .9);
        assertTrue(alphaCarga(CARGA - 2) > .9);
        for (int c = 0; c < CAMADAS; c++) for (int i = 0; i <= LAT; i++) for (int j = 0; j < LON; j++) {
            var p = casca(c, i, j, 40); assertTrue(p.y() >= 0 && p.y() < 2.6 && Math.hypot(p.x(), p.z()) < 1.3, "volume em volta do corpo");
        }
        double alto = 0; for (int k = 0; k < RAIOS_COLUNA; k++) { var c = raioColuna(k, ATIVA + 4); alto = Math.max(alto, c.get(c.size() - 1).y()); }
        assertTrue(alto > 5, "coluna sobe");
    }
    @Test void animacao() throws Exception {
        var j = com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/killua.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("killua_godspeed"));
    }
    @Test void exportaParaPrevia() throws Exception {
        var q = new LinkedHashMap<String, Object>();
        for (double t = 0; t <= F; t += .5) {
            var m = new LinkedHashMap<String, Object>();
            var br = new ArrayList<Object>(); for (int k = 0; k < ARCOS_BRACO; k++) { br.add(arcoBraco(k, 1, t)); br.add(arcoBraco(k, -1, t)); }
            var ch = new ArrayList<Object>(); for (int k = 0; k < ARCOS_CHAO; k++) ch.add(arcoChao(k, t));
            var co = new ArrayList<Object>(); for (int k = 0; k < RAIOS_COLUNA; k++) co.add(raioColuna(k, t));
            var es = new ArrayList<Object>(); for (int k = 0; k < RAIOS_ESTOURO; k++) es.add(raioEstouro(k, t));
            var on = new ArrayList<Object>(); for (int k = 0; k < 3; k++) on.add(ondaChao(k, t));
            var ac = new ArrayList<Object>(); for (int k = 0; k < ARCOS_CORPO; k++) ac.add(arcoCorpo(k, t));
            var ca = new ArrayList<Object>();
            for (int c = 0; c < CAMADAS; c++) { var gr = new ArrayList<Object>(); for (int i = 0; i <= LAT; i++) { var lin = new ArrayList<Object>(); for (int j = 0; j < LON; j++) lin.add(casca(c, i, j, t)); gr.add(lin); } ca.add(gr); }
            var av = new ArrayList<Object>(); for (int k = 0; k < ARCOS_VOLUME; k++) av.add(arcoVolume(k, t));
            var te = new ArrayList<Object>(); for (int k = 0; k < TENTACULOS; k++) te.add(List.of(tentaculo(k, t), alphaTentaculo(k, t)));
            var an = new ArrayList<Object>(); for (int n = 0; n < ANEIS; n++) an.add(anelOrbita(n, t));
            m.put("alphaCarga", alphaCarga(t)); m.put("bracos", br); m.put("chao", ch); m.put("faiscasCarga", faiscasCarga(t));
            m.put("alphaEstouro", alphaEstouro(t)); m.put("clarao", clarao(t)); m.put("coluna", co); m.put("estouro", es);
            m.put("ondas", on); m.put("faiscasEstouro", faiscasEstouro(t));
            m.put("alphaAura", alphaAura(t)); m.put("corpo", ac); m.put("cascas", ca); m.put("volume", av); m.put("tentaculos", te); m.put("aneis", an);
            m.put("alphaDescarga", alphaDescarga(t)); m.put("descarga", descarga(t)); m.put("faiscasAura", faiscasAura(t));
            m.put("brilho", brilhoCorpo(t));
            q.put(Double.toString(t), m);
        }
        Files.createDirectories(Path.of("build"));
        try (var w = Files.newBufferedWriter(Path.of("build/killua-godspeed-samples.json"))) {
            new Gson().toJson(Map.of("quadros", q, "fim", F, "ativa", ATIVA, "carga", CARGA, "entrada", ENTRADA), w);
        }
    }
}
