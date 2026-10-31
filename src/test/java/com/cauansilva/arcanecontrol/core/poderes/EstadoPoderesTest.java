package com.cauansilva.arcanecontrol.core.poderes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EstadoPoderesTest {
    @Test void pesquisaConsomeConhecimentoUmaVezEMantemProgressoSeparado(){
        var e=new EstadoPoderes();
        assertFalse(e.selecionar(PoderAnime.FRIEREN));
        assertFalse(e.desbloquear(PoderAnime.CID,false));
        e.ganharConhecimento(400);
        assertTrue(e.desbloquear(PoderAnime.CID,false));assertEquals(280,e.conhecimento());
        assertFalse(e.desbloquear(PoderAnime.CID,false));assertEquals(280,e.conhecimento());
        assertTrue(e.selecionar(PoderAnime.CID));e.ganharXp(PoderAnime.CID,400);
        assertTrue(e.podeUsar(PoderAnime.CID,3));
        assertTrue(e.desbloquear(PoderAnime.FRIEREN,false));assertEquals(40,e.conhecimento());
        assertTrue(e.selecionar(PoderAnime.FRIEREN));assertEquals(1,e.nivel(PoderAnime.FRIEREN));
        assertFalse(e.podeUsar(PoderAnime.FRIEREN,3));assertFalse(e.podeUsar(PoderAnime.CID,0));
        assertTrue(e.selecionar(PoderAnime.CID));assertEquals(5,e.nivel(PoderAnime.CID));
    }
    @Test void maestriaDependeDeUsosValidosEReduzRecargaNoMaximoVinteECincoPorCento(){
        var e=new EstadoPoderes();e.desbloquear(PoderAnime.FRIEREN,true);e.selecionar(PoderAnime.FRIEREN);
        for(int i=0;i<150;i++){e.registrarUso(PoderAnime.FRIEREN,0);e.registrarUso(PoderAnime.CID,0);}
        assertEquals(5,e.maestria(PoderAnime.FRIEREN,0));assertEquals(1150,e.xp(PoderAnime.FRIEREN));
        assertEquals(750,e.recarga(PoderAnime.FRIEREN,0,1000));assertEquals(0,e.maestria(PoderAnime.CID,0));
    }
    @Test void dadosPersistidosSaoLimitadosECopiados(){
        int[] xp={-1,Integer.MAX_VALUE,100},usos={999};
        var e=new EstadoPoderes(3,2,Integer.MAX_VALUE,xp,usos);xp[2]=999;usos[0]=0;
        assertEquals(PoderAnime.GOJO,e.ativo());assertEquals(100000,e.conhecimento());
        assertEquals(20,e.nivel(PoderAnime.CID));assertEquals(100,e.xp(PoderAnime.FRIEREN));
        var copy=e.usos();copy[0]=0;assertEquals(100,e.usos()[0]);
        e.ganharConhecimento(Integer.MAX_VALUE);e.ganharXp(PoderAnime.CID,Integer.MAX_VALUE);
        assertEquals(100000,e.conhecimento());assertEquals(1900,e.xp(PoderAnime.CID));
    }
    @Test void migracaoPreservaMovesetCidSemLiberarFrieren(){
        var e=new EstadoPoderes();e.migrarCid();e.migrarCid();assertTrue(e.selecionar(PoderAnime.CID));
        for(int i=0;i<4;i++)assertTrue(e.podeUsar(PoderAnime.CID,i));
        assertFalse(e.tem(PoderAnime.FRIEREN));assertFalse(e.podeUsar(PoderAnime.CID,4));
    }
}
