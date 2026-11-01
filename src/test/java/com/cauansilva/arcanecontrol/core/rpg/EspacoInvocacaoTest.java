package com.cauansilva.arcanecontrol.core.rpg;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EspacoInvocacaoTest {
    private static boolean colide(AABB a, AABB b) {
        return Shapes.joinIsNotEmpty(Shapes.create(a), Shapes.create(b), BooleanOp.AND);
    }

    @Test void pisoSolidoNaoPodeSerConfundidoComObstrucao() {
        for (int y : new int[]{-32, 0, 64, 128}) {
            var corpo = new AABB(5.075, y, .075, 5.925, y + 2.2, .925);
            var piso = new AABB(3, y - 1, -2, 8, y, 3);
            assertTrue(colide(corpo.inflate(1, .1, 1), piso), "Reproduz a falha anterior");
            assertFalse(colide(EspacoInvocacao.volumeLivre(corpo), piso));
            assertEquals(corpo.minY, EspacoInvocacao.volumeLivre(corpo).minY);
        }
    }

    @Test void paredeOuTetoBaixoContinuamImpedindoInvocacao() {
        var corpo = new AABB(5.075, 64, .075, 5.925, 66.2, .925);
        var livre = EspacoInvocacao.volumeLivre(corpo);
        assertTrue(colide(livre, new AABB(6, 64, 0, 7, 67, 1)), "Parede dentro da folga lateral");
        assertTrue(colide(livre, new AABB(5, 66.25, 0, 6, 67, 1)), "Teto dentro da folga superior");
        assertFalse(colide(livre, new AABB(5, 67, 0, 6, 68, 1)), "Teto com altura suficiente");
    }

    @Test void folgaNaoAlteraDimensoesDaEntidadeNemPermitePisoDentroDoCorpo() {
        var corpo = new AABB(-5.925, 64, -.925, -5.075, 66.2, -.075);
        var livre = EspacoInvocacao.volumeLivre(corpo);
        assertEquals(64, corpo.minY);
        assertEquals(corpo.minX - 1, livre.minX);
        assertEquals(corpo.maxX + 1, livre.maxX);
        assertEquals(corpo.maxY + .1, livre.maxY);
        assertTrue(colide(livre, new AABB(-8, 63, -3, -3, 64.1, 2)), "Piso elevado invade os pes");
    }
}
