package com.cauansilva.arcanecontrol.core.vazio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlacarVazioTest {
    @Test void ordenaPorDanoEDaDestaques(){
        var p=new PlacarVazio<String>();p.entrar("a","Ana");p.entrar("b","Beto");p.entrar("c","Caio");
        p.dano("a",50);p.dano("b",120);p.dano("b",30);p.cura("a",40);p.recebido("c",90);p.recebido("a",10);p.caiu("a");
        var l=p.linhas();
        assertEquals("Beto",l.get(0).nome());assertEquals(150,l.get(0).dano());assertEquals(120,l.get(0).maiorGolpe());
        assertEquals("MVP · Maior Dano",l.get(0).destaque());
        assertEquals("Maior Cura",l.stream().filter(x->x.nome().equals("Ana")).findFirst().get().destaque());
        assertEquals("Muralha",l.stream().filter(x->x.nome().equals("Caio")).findFirst().get().destaque());
    }
    @Test void semValoresSemDestaque(){var p=new PlacarVazio<String>();p.entrar("a","Ana");assertEquals("",p.linhas().get(0).destaque());}
    @Test void dicasPorSituacao(){
        var p=new PlacarVazio<String>();p.entrar("a","Ana");p.recebido("a",100);p.caiu("a");
        var d=p.dicas(false,30,20);
        assertTrue(d.size()<=3&&!d.isEmpty());assertTrue(d.get(0).contains("golpe carregado"));
        var limpo=new PlacarVazio<String>();limpo.entrar("a","Ana");limpo.dano("a",400);
        assertEquals(1,limpo.dicas(true,60,20).size());
        assertTrue(new PlacarVazio<String>().dicas(true,400,20).stream().anyMatch(s->s.contains("Luta longa")));
    }
    @Test void barraSoulsAtrasaEDesce(){
        var b=new BarraChefe();b.atualizar(.7,100);
        assertEquals(.7,b.vida(),1e-9);assertEquals(1,b.atraso(),1e-9);assertEquals(30,b.combo());
        for(int i=0;i<BarraChefe.ATRASO;i++)b.tick();
        assertEquals(1,b.atraso(),1e-9,"parada durante o atraso");
        b.atualizar(.6,100);assertEquals(40,b.combo(),"combo acumula");
        for(int i=0;i<BarraChefe.ATRASO+40;i++)b.tick();
        assertEquals(.6,b.atraso(),1e-9);assertEquals(0,b.combo());
    }
}
