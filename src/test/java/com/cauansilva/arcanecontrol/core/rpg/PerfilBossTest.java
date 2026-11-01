package com.cauansilva.arcanecontrol.core.rpg;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PerfilBossTest {
    @Test void efeitosDosQuatroChefesNaoCapturamMovesetsDoJogador(){
        for(int i=1;i<=4;i++){assertTrue(PerfilBoss.efeito(400+i));assertTrue(PerfilBoss.efeito(410+i));}
        for(int i:new int[]{0,30,101,104,111,113,114,200,300,304,310,405,410,415,1040})assertFalse(PerfilBoss.efeito(i));
    }
    @Test void tremorCaiComTempoEDistanciaERespeitaLimites(){
        assertEquals(.45,PerfilBoss.trauma(0,0),1e-9);
        assertEquals(.1125,PerfilBoss.trauma(4,12),1e-9);
        assertEquals(0,PerfilBoss.trauma(8,0));assertEquals(0,PerfilBoss.trauma(0,24));
        assertEquals(0,PerfilBoss.trauma(Double.NaN,0));assertEquals(0,PerfilBoss.trauma(0,Double.POSITIVE_INFINITY));
    }
}
