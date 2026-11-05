package com.cauansilva.arcanecontrol.core.combate;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InterrupcaoTest {
    @Test void forcaVenceResistencia() {
        for (Resistencia r : Resistencia.values()) for (Forca f : Forca.values()) {
            var s = new Interrupcao<String>();
            s.iniciar("a", "t", 0, 40, r, null);
            boolean esperado = switch (r) { case NENHUMA -> true; case FIRME -> f != Forca.LEVE; case SUPERARMADURA -> f == Forca.ABSOLUTA; case IMUNE -> false; };
            assertEquals(esperado ? Resultado.INTERROMPIDO : Resultado.RESISTIU, s.interromper("a", "b", f, 5), r + " x " + f);
        }
    }

    @Test void cancelaUmaVezEImunidadeDeUmSegundo() {
        var s = new Interrupcao<String>();
        List<Quebra<String>> q = new ArrayList<>();
        s.iniciar("a", "narukami", 0, 40, Resistencia.FIRME, q::add);
        assertEquals(Resultado.INTERROMPIDO, s.interromper("a", "killua", Forca.FORTE, 10));
        assertEquals(1, q.size());
        assertEquals("narukami", q.get(0).tecnica());
        assertFalse(s.canalizando("a", 11));
        s.iniciar("a", "outra", 12, 40, Resistencia.FIRME, q::add);
        assertEquals(Resultado.IMUNE_RECENTE, s.interromper("a", "killua", Forca.FORTE, 12 + 5));
        assertEquals(Resultado.INTERROMPIDO, s.interromper("a", "killua", Forca.ABSOLUTA, 17), "absoluta ignora a imunidade");
        s.iniciar("a", "x", 40, 40, Resistencia.FIRME, q::add);
        assertEquals(Resultado.INTERROMPIDO, s.interromper("a", "killua", Forca.FORTE, 17 + Interrupcao.IMUNIDADE));
        assertEquals(3, q.size());
    }

    @Test void concluirEVencerNaoChamamCancelar() {
        var s = new Interrupcao<String>();
        List<Quebra<String>> q = new ArrayList<>();
        s.iniciar("a", "t", 0, 10, Resistencia.NENHUMA, q::add);
        assertEquals(Resultado.SEM_CANALIZACAO, s.interromper("a", "b", Forca.LEVE, 10), "venceu no fim");
        s.iniciar("a", "t", 20, 10, Resistencia.NENHUMA, q::add);
        s.concluir("a", "t");
        assertEquals(Resultado.SEM_CANALIZACAO, s.interromper("a", "b", Forca.LEVE, 22));
        assertTrue(q.isEmpty());
    }

    @Test void guardaContraAtacaUmaVezDentroDaJanela() {
        var s = new Interrupcao<String>();
        List<String> contra = new ArrayList<>();
        List<Quebra<String>> q = new ArrayList<>();
        s.iniciar("inimigo", "soco", 0, 30, Resistencia.FIRME, q::add);
        s.guardar("killua", "whirlpool", 0, 10, contra::add);
        assertEquals(Defesa.CONTRA_ATAQUE, s.receberGolpe("killua", "inimigo", 9));
        assertEquals(List.of("inimigo"), contra);
        assertEquals(1, q.size(), "atacante interrompido");
        assertEquals(Defesa.NORMAL, s.receberGolpe("killua", "inimigo", 9), "guarda fecha apos o contra");
        s.guardar("killua", "whirlpool", 100, 10, contra::add);
        assertEquals(Defesa.NORMAL, s.receberGolpe("killua", "inimigo", 110), "janela vencida");
        assertEquals(1, contra.size());
    }

    @Test void recargaPenalizadaEMetade() {
        assertEquals(50, Interrupcao.recargaPenalizada(100));
        assertEquals(51, Interrupcao.recargaPenalizada(101));
        assertEquals(0, Interrupcao.recargaPenalizada(-3));
    }
}
