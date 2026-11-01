package com.cauansilva.arcanecontrol.core.rpg;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConjuracaoLayoutTest {
    @Test void falhaDeNivelOuManaNaoCobraNemIniciaRecarga(){
        var m=new MestriaRpg();var mana=new RecursoMana(5);var cd=new RecargasRpg();
        assertEquals(ConjuracaoRpg.Resultado.BLOQUEADA,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.GOJO,1,100,100,12,false,false,false));
        assertEquals(5,mana.atual());assertEquals(0,cd.restante(PersonagemRpg.GOJO,1,100));
        assertEquals(ConjuracaoRpg.Resultado.MANA,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.GOJO,0,100,100,8,false,false,false));
        assertEquals(0,cd.restante(PersonagemRpg.GOJO,0,100));assertEquals(5,mana.atual());
    }
    @Test void castValidoCobraUmaVezERepeticaoNaoBurlaRecarga(){
        var m=MestriaRpg.migrar(900,true,true,true);var mana=new RecursoMana(100);var cd=new RecargasRpg();
        assertEquals(ConjuracaoRpg.Resultado.OK,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.FRIEREN,3,100,1200,35,true,false,false));
        assertEquals(72,mana.atual(),1e-8);assertEquals(1146,cd.restante(PersonagemRpg.FRIEREN,3,100));
        assertEquals(ConjuracaoRpg.Resultado.RECARGA,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.FRIEREN,3,101,1200,35,true,false,false));
        assertEquals(72,mana.atual(),1e-8);
        assertEquals(ConjuracaoRpg.Resultado.OK,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.SHADOW,0,101,40,8,false,false,false));
        assertEquals(64,mana.atual(),1e-8);assertTrue(cd.restante(PersonagemRpg.FRIEREN,3,101)>0);
    }
    @Test void criativoNaoCobraManaENaoConcedeDespertar(){
        var m=new MestriaRpg();var mana=new RecursoMana(0);var cd=new RecargasRpg();
        assertEquals(ConjuracaoRpg.Resultado.OK,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.SHADOW,3,0,100,35,false,true,false));
        assertEquals(0,mana.atual());assertEquals(ConjuracaoRpg.Resultado.BLOQUEADA,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.SHADOW,4,0,100,50,false,true,false));
    }
    @Test void argumentosInvalidosNaoAlteramRecursos(){
        var m=new MestriaRpg();var mana=new RecursoMana(100);var cd=new RecargasRpg();
        for(int slot:new int[]{-1,5,999})assertEquals(ConjuracaoRpg.Resultado.INVALIDA,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.GOJO,slot,1,100,8,false,false,false));
        assertEquals(ConjuracaoRpg.Resultado.INVALIDA,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.GOJO,0,1,100,Double.NaN,false,false,false));
        assertEquals(100,mana.atual());assertEquals(0,cd.restante(PersonagemRpg.GOJO,0,1));
    }
    @Test void dominioLegadoNaoDispensaAlmaDoDespertar(){
        var m=new MestriaRpg();var mana=new RecursoMana(100);var cd=new RecargasRpg();
        assertEquals(ConjuracaoRpg.Resultado.BLOQUEADA,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.GOJO,4,1,100,50,false,false,false));
        assertEquals(ConjuracaoRpg.Resultado.BLOQUEADA,ConjuracaoRpg.tentar(m,mana,cd,PersonagemRpg.GOJO,4,1,100,50,false,false,true));
        assertEquals(100,mana.atual());
    }
    @Test void arvoreCabeEmTodasAsEscalasSemSobreporNos(){
        for(int[] tela:new int[][]{{320,240},{426,240},{640,360},{854,480},{1920,1080}}){
            var l=LayoutArvoreRpg.paraTela(tela[0],tela[1]);
            for(int i=0;i<8;i++){
                assertTrue(l.nodeX(i)>=l.x());assertTrue(l.nodeX(i)+l.nodeLargura()<=l.x()+l.largura());
                assertTrue(l.nodeY(i)>=l.y()+90);assertTrue(l.nodeY(i)+31<l.y()+l.altura()-48);
                for(int j=i+1;j<8;j++)assertTrue(l.nodeX(i)+l.nodeLargura()<=l.nodeX(j)||l.nodeX(j)+l.nodeLargura()<=l.nodeX(i)||l.nodeY(i)+18<=l.nodeY(j)||l.nodeY(j)+18<=l.nodeY(i));
            }
        }
    }
}
