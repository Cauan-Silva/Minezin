package com.cauansilva.arcanecontrol.core.rpg;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DespertarTest {
    @Test void cadaFormaExpiraNoTickCorretoEExclusivoSoPodeSerUsadoUmaVez(){
        for(var kit:new PersonagemRpg[]{PersonagemRpg.GOJO,PersonagemRpg.SHADOW,PersonagemRpg.FRIEREN}){
            var f=new EstadoDespertar(kit,100);assertEquals(kit.duracaoSegundos*20,f.restante(100));assertTrue(f.ativa(100));
            assertTrue(f.consumirExclusivo(101));assertFalse(f.consumirExclusivo(102));assertTrue(f.exclusivoUsado());
            assertTrue(f.ativa(99+kit.duracaoSegundos*20));assertFalse(f.ativa(100+kit.duracaoSegundos*20));
            assertFalse(new EstadoDespertar(kit,0).consumirExclusivo(kit.duracaoSegundos*20));
        }
    }
    @Test void baseEFormaTemRecargasSeparadasMasCompartilhamG(){
        var cd=new RecargasRpg();var k=PersonagemRpg.FRIEREN;
        assertTrue(cd.iniciar(k,2,100,200,1,false));assertEquals(200,cd.restante(k,2,100,false));assertEquals(0,cd.restante(k,2,100,true));
        assertTrue(cd.iniciar(k,2,100,500,1,true));assertEquals(500,cd.restante(k,2,100,true));
        assertTrue(cd.iniciar(k,4,100,6000,1,true));assertEquals(6000,cd.restante(k,4,100,false));assertFalse(cd.iniciar(k,4,101,6000,1,false));
        var salvo=new RecargasRpg(cd.prazos(),cd.duracoes());assertEquals(190,salvo.restante(k,2,110,false));assertEquals(490,salvo.restante(k,2,110,true));assertEquals(5990,salvo.restante(k,4,110,true));
    }
    @Test void migracaoDe75PosicoesPreservaTodosOsKitsSemInventarRecargaDesperta(){
        long[] prazo=new long[75];int[] duracao=new int[75];for(int i=0;i<75;i++){prazo[i]=1000+i;duracao[i]=500+i;}
        var cd=new RecargasRpg(prazo,duracao);
        for(var kit:PersonagemRpg.values())for(int s=0;s<5;s++){
            int old=(kit.id-1)*5+s;assertEquals(900+old,cd.restante(kit,s,100,false));assertEquals(500+old,cd.duracao(kit,s,false));
            assertEquals(s==4?900+old:0,cd.restante(kit,s,100,true));
        }
        prazo[0]=0;assertEquals(900,cd.restante(PersonagemRpg.GOJO,0,100));
    }
    @Test void formaNaoDispensaAlmaENemPermiteCustoDuplicado(){
        var m=new MestriaRpg(4900,255,false);var mana=new RecursoMana(100);var cds=new RecargasRpg();
        assertEquals(ConjuracaoRpg.Resultado.BLOQUEADA,ConjuracaoRpg.tentar(m,mana,cds,PersonagemRpg.GOJO,4,100,7200,50,false,true,true,true));
        assertEquals(100,mana.atual());assertTrue(m.consumirAlma(PersonagemRpg.GOJO,PersonagemRpg.GOJO));
        assertEquals(ConjuracaoRpg.Resultado.OK,ConjuracaoRpg.tentar(m,mana,cds,PersonagemRpg.GOJO,4,100,7200,50,false,false,false,false));
        assertEquals(50,mana.atual());
        assertEquals(ConjuracaoRpg.Resultado.RECARGA,ConjuracaoRpg.tentar(m,mana,cds,PersonagemRpg.GOJO,4,101,7200,50,false,false,false,true));assertEquals(50,mana.atual());
    }
}
