package com.cauansilva.arcanecontrol.core.progressao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgressoGojoTest {
    @Test void jogadorComecaSomenteComAzul() {
        ProgressoGojo p = new ProgressoGojo();
        assertTrue(p.tem(TecnicaGojo.AZUL));
        assertEquals(1, p.nivel());
        assertEquals(0, p.pontos());
        assertFalse(p.desbloquear(TecnicaGojo.VERMELHO));
        assertFalse(p.desbloquear(TecnicaGojo.DOMINIO));
    }

    @Test void requisitosBloqueiamUltimateMesmoComPontos() {
        ProgressoGojo p = new ProgressoGojo();
        p.ganharXp(700);
        assertFalse(p.desbloquear(TecnicaGojo.ROXO));
        assertFalse(p.desbloquear(TecnicaGojo.DOMINIO));
        assertTrue(p.desbloquear(TecnicaGojo.VERMELHO));
        assertTrue(p.desbloquear(TecnicaGojo.ROXO));
        assertFalse(p.desbloquear(TecnicaGojo.DOMINIO));
        assertTrue(p.desbloquear(TecnicaGojo.INFINITO));
        assertTrue(p.desbloquear(TecnicaGojo.DOMINIO));
        assertEquals(0, p.pontos());
        assertFalse(p.desbloquear(TecnicaGojo.DOMINIO));
    }

    @Test void pontosNaoPodemSerGastosDuasVezes() {
        ProgressoGojo p = new ProgressoGojo();
        p.ganharXp(100);
        assertTrue(p.desbloquear(TecnicaGojo.VERMELHO));
        assertEquals(0, p.pontos());
        assertFalse(p.desbloquear(TecnicaGojo.VERMELHO));
        assertFalse(p.desbloquear(TecnicaGojo.INFINITO));
    }

    @Test void maestriaReduzRecargaESaturaSemOverflow() {
        ProgressoGojo p = new ProgressoGojo();
        for (int i = 0; i < 19; i++) p.registrarUso(TecnicaGojo.AZUL);
        assertEquals(0, p.maestria(TecnicaGojo.AZUL));
        p.registrarUso(TecnicaGojo.AZUL);
        assertEquals(1, p.maestria(TecnicaGojo.AZUL));
        assertEquals(190, p.cooldown(TecnicaGojo.AZUL));
        for (int i = 0; i < 200; i++) p.registrarUso(TecnicaGojo.AZUL);
        assertEquals(5, p.maestria(TecnicaGojo.AZUL));
        assertEquals(150, p.cooldown(TecnicaGojo.AZUL));
        p.ganharXp(Integer.MAX_VALUE);
        assertEquals(20, p.nivel());
        assertEquals(1900, p.xp());
    }

    @Test void restauracaoPreservaProgressoEIgnoraValoresInvalidos() {
        ProgressoGojo original = new ProgressoGojo();
        for (int i = 0; i < 20; i++) original.registrarUso(TecnicaGojo.AZUL);
        original.desbloquear(TecnicaGojo.VERMELHO);
        ProgressoGojo restaurado = new ProgressoGojo(original.xp(), original.desbloqueios(), original.usos());
        assertEquals(original.pontos(), restaurado.pontos());
        assertTrue(restaurado.tem(TecnicaGojo.VERMELHO));
        assertEquals(1, restaurado.maestria(TecnicaGojo.AZUL));
        ProgressoGojo invalido = new ProgressoGojo(-100, 0, new int[]{-4, Integer.MAX_VALUE});
        assertEquals(1, invalido.nivel());
        assertTrue(invalido.tem(TecnicaGojo.AZUL));
        assertEquals(0, invalido.maestria(TecnicaGojo.AZUL));
        int[] copia = restaurado.usos();
        copia[0] = 999;
        assertEquals(1, restaurado.maestria(TecnicaGojo.AZUL));
    }
}
