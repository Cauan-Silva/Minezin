package com.cauansilva.arcanecontrol.core.rank;

import org.junit.jupiter.api.Test;
import java.util.List;
import static com.cauansilva.arcanecontrol.core.rank.RankCacador.*;
import static org.junit.jupiter.api.Assertions.*;

class RankCacadorTest {
    @Test void faixas(){
        assertEquals(Rank.F,rank(0));assertEquals(Rank.F,rank(99));assertEquals(Rank.E,rank(100));
        assertEquals(Rank.SSS,rank(999999));assertEquals(1,progresso(20000));assertEquals(.5,progresso(50),1e-9);
        for(int i=1;i<Rank.values().length;i++)assertTrue(Rank.values()[i].minimo>Rank.values()[i-1].minimo);
    }
    @Test void mediaDoGrupoEPortao(){
        assertEquals(Rank.D,media(List.of(Rank.A,Rank.F)));
        assertTrue(podeEntrar(Dificuldade.NORMAL,List.of(Rank.F)));
        assertFalse(podeEntrar(Dificuldade.MITICA,List.of(Rank.A,Rank.F)));
        assertTrue(podeEntrar(Dificuldade.MITICA,List.of(Rank.A,Rank.C)));
        assertFalse(podeEntrar(Dificuldade.CELESTIAL,List.of(Rank.A)));assertTrue(podeEntrar(Dificuldade.CELESTIAL,List.of(Rank.SS,Rank.A)));
    }
    @Test void pontosPorLuta(){
        assertEquals(0,pontos(false,Dificuldade.CELESTIAL,60,false,4),"derrota nao rende nem tira");
        assertEquals(110,pontos(true,Dificuldade.NORMAL,100,false,1));
        assertEquals(60,pontos(true,Dificuldade.NORMAL,400,true,1));
        assertEquals(140*5,pontos(true,Dificuldade.CELESTIAL,100,false,9),"aliados com teto de 3");
        assertTrue(pontos(true,Dificuldade.MITICA,200,false,2)>pontos(true,Dificuldade.NORMAL,200,false,2));
    }
}
