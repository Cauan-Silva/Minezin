package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static com.cauansilva.arcanecontrol.core.vfx.KilluaPulsoEletromagnetico.*;
import static org.junit.jupiter.api.Assertions.*;

class KilluaPulsoEletromagneticoTest {
    @Test void esferaAlcancaOsAlvosEmOrdemEInterrompe() {
        assertEquals(0, raioPulso(PULSO - 1));
        assertEquals(RAIO, raioPulso(PULSO + EXPANSAO), 1e-6);
        assertTrue(toque(0) < toque(2) && toque(2) < toque(1), "mais perto e tocado antes");
        for (int i = 0; i < ALVOS.length; i++) assertTrue(raioPulso(toque(i)) >= ALVOS[i][1] - 1e-6);
        var s = new Interrupcao<String>();
        s.iniciar("mob", "carga", 0, 60, Interrupcao.Resistencia.FIRME, null);
        assertEquals(Interrupcao.Resultado.INTERROMPIDO, s.interromper("mob", "killua", FORCA, (long) toque(0)));
    }

    @Test void exportaParaPrevia() throws Exception {
        var aura = new AuraTecidoKillua();
        for (int i = 0; i < 40; i++) aura.tick(new P(0, 0, 0), 0, 1);
        var q = new LinkedHashMap<String, Object>();
        for (int t = 0; t <= FIM; t++) {
            aura.tick(new P(0, 0, 0), 0, 1);
            var m = new LinkedHashMap<String, Object>();
            m.put("corpo", new P(0, 0, 0)); m.put("giro", 0); m.put("alphaCorpo", 1); m.put("atacante", alvo(0));
            var li = new ArrayList<Object>(); var rb = new ArrayList<Object>(); var ex = new ArrayList<Object>();
            for (int n = 0; n < AuraTecidoKillua.LINGUAS; n++) { li.add(aura.lingua(n)); if (n % 3 == 0) rb.add(aura.raioBorda(n, 0, t)); }
            for (int k = 0; k < AuraTecidoKillua.EXPLOSIVOS; k++) if (AuraTecidoKillua.alphaExplosivo(k, t) > 0) ex.add(aura.raioExplosivo(k, t));
            m.put("linguas", li); m.put("bordas", rb); m.put("explosivos", ex); m.put("envelope", aura.envelope());
            var ac = new ArrayList<Object>(); for (int k = 0; k < KilluaGodspeed.ARCOS_CORPO; k++) ac.add(KilluaGodspeed.arcoCorpo(k, t));
            m.put("arcosCorpo", ac);
            m.put("alphaCarga", alphaCarga(t)); m.put("raioNucleo", raioNucleo(t)); m.put("centro", centro());
            var lc = new ArrayList<Object>(); for (int k = 0; k < LINHAS; k++) lc.add(linhaCampo(k, t));
            var an = new ArrayList<Object>(); for (int k = 0; k < 6; k++) an.add(arcoNucleo(k, t));
            m.put("linhas", lc); m.put("arcosNucleo", an);
            m.put("alphaPulso", alphaPulso(t)); m.put("raioPulso", raioPulso(t));
            var ca = new ArrayList<Object>(); for (int k = 0; k < ARCOS_CASCA; k++) ca.add(arcoCasca(k, t));
            m.put("casca", ca); m.put("ondas", List.of(onda(0, t), onda(1, t)));
            var alv = new ArrayList<Object>();
            for (int i = 0; i < ALVOS.length; i++) {
                var x = new LinkedHashMap<String, Object>();
                x.put("pos", alvo(i)); x.put("alpha", alphaAlvo(i, t)); x.put("raio", raioAlvo(i, t)); x.put("atordoado", alphaAtordoado(i, t));
                var aa = new ArrayList<Object>(); for (int k = 0; k < ARCOS_ALVO; k++) aa.add(arcoAlvo(i, k, t));
                x.put("arcos", aa); alv.add(x);
            }
            m.put("alvos", alv); m.put("faiscas", faiscas(t));
            q.put(Integer.toString(t), m);
        }
        Files.createDirectories(Path.of("build"));
        try (var w = Files.newBufferedWriter(Path.of("build/killua-pulso-samples.json"))) {
            new Gson().toJson(Map.of("quadros", q, "fim", FIM, "pulso", PULSO, "expansao", EXPANSAO), w);
        }
    }
}
