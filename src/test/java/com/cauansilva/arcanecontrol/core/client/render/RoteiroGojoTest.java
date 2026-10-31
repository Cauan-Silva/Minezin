package com.cauansilva.arcanecontrol.core.client.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoteiroGojoTest {
    @Test void trajetoriasSaoFinitasEContinuas() {
        for (int tipo : new int[]{4, 5}) {
            var anterior = RoteiroGojo.amostrar(tipo, 0);
            for (double tick = .1; tick <= 160; tick += .1) {
                var atual = RoteiroGojo.amostrar(tipo, tick);
                assertTrue(Double.isFinite(atual.x()) && Double.isFinite(atual.y()) && Double.isFinite(atual.z()));
                assertTrue(atual.fov() >= .75 && atual.fov() <= 1.1);
                assertTrue(Math.abs(atual.x() - anterior.x()) < .5);
                assertTrue(Math.abs(atual.z() - anterior.z()) < .5);
                anterior = atual;
            }
        }
    }

    @Test void tremorAconteceSomenteNaJanelaDoImpacto() {
        assertEquals(0, RoteiroGojo.impacto(4, 29));
        assertEquals(.65, RoteiroGojo.impacto(4, 30));
        assertEquals(0, RoteiroGojo.impacto(4, 42));
        assertEquals(0, RoteiroGojo.impacto(5, 34));
        assertEquals(.65, RoteiroGojo.impacto(5, 35));
        assertEquals(0, RoteiroGojo.impacto(5, 47));
    }

    @Test void fimRetornaFovNeutro() {
        assertEquals(1, RoteiroGojo.amostrar(4, 85).fov());
        assertEquals(1, RoteiroGojo.amostrar(5, 160).fov());
        assertEquals(0, RoteiroGojo.suave(-10));
        assertEquals(1, RoteiroGojo.suave(10));
    }
}
