package com.cauansilva.arcanecontrol.core.rpg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MutacaoRpgTest {
    @Test void empacotamentoNaoMexeNosNos(){
        int n=NoRpg.TECNICA_X.bit()|NoRpg.TECNICA_C.bit();
        n=MutacaoRpg.gravar(n,1,MutacaoRpg.FLUXO.ordinal());n=MutacaoRpg.gravar(n,3,MutacaoRpg.VAMPIRICA.ordinal());
        assertEquals(MutacaoRpg.FLUXO.ordinal(),MutacaoRpg.ler(n,1));assertEquals(0,MutacaoRpg.ler(n,2));assertEquals(MutacaoRpg.VAMPIRICA.ordinal(),MutacaoRpg.ler(n,3));
        assertEquals(NoRpg.TECNICA_X.bit()|NoRpg.TECNICA_C.bit(),n&0xFF);
        n=MutacaoRpg.gravar(n,1,MutacaoRpg.RESSONANCIA.ordinal());assertEquals(MutacaoRpg.RESSONANCIA.ordinal(),MutacaoRpg.ler(n,1));
    }
    @Test void exigeNoENivel(){
        var m=new MestriaRpg();m.ganharXp(100*13);m.comprar(NoRpg.TECNICA_X);
        assertFalse(m.podeMutar(1),"nivel 14 < 15");assertFalse(m.mutar(1,MutacaoRpg.DEVASTACAO));
        m.ganharXp(100);assertTrue(m.mutar(1,MutacaoRpg.DEVASTACAO));assertEquals(MutacaoRpg.DEVASTACAO,m.mutacao(1));
        assertFalse(m.mutar(2,MutacaoRpg.FLUXO),"C nao comprada");
    }
    @Test void roundTripPeloCampoNos(){
        var m=MestriaRpg.maximaParaTeste();m.mutar(1,MutacaoRpg.FLUXO);m.mutar(2,MutacaoRpg.RESSONANCIA);m.mutar(3,MutacaoRpg.VAMPIRICA);
        var c=new MestriaRpg(m.xp(),m.nosComMutacoes(),m.despertarLiberado());
        assertEquals(m.nos(),c.nos());assertEquals(MutacaoRpg.FLUXO,c.mutacao(1));assertEquals(MutacaoRpg.RESSONANCIA,c.mutacao(2));assertEquals(MutacaoRpg.VAMPIRICA,c.mutacao(3));
    }
    @Test void nbtInvalidoNaoMuta(){var c=new MestriaRpg(0,MutacaoRpg.gravar(0,1,MutacaoRpg.FLUXO.ordinal()),false);assertEquals(MutacaoRpg.NENHUMA,c.mutacao(1));}
    @Test void cicloECompromissos(){
        assertEquals(MutacaoRpg.NENHUMA,MutacaoRpg.FLUXO.proxima());
        for(var m:MutacaoRpg.values())if(m!=MutacaoRpg.NENHUMA){
            boolean bonus=m.dano>1||m.recarga<1||m.roubo>0||m.respingo>0;assertTrue(bonus,m+" precisa dar algo");}
        assertTrue(MutacaoRpg.DEVASTACAO.recarga>1&&MutacaoRpg.FLUXO.dano<1,"mutacoes fortes tem custo");
    }
}
