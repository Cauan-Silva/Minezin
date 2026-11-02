package com.cauansilva.arcanecontrol.core.rpg;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

class RegrasRpgTest {
    @Test void perfilAdministrativoCompletoSobreviveReleituraSemMudarPerfilNormal(){
        var max=MestriaRpg.maximaParaTeste();
        var relido=new MestriaRpg(max.xp(),max.nos(),max.despertarLiberado());
        assertEquals(100,relido.nivel());
        for(var no:NoRpg.values())assertTrue(relido.tem(no));
        for(int slot=0;slot<5;slot++)assertTrue(relido.podeUsarSlot(slot));
        assertTrue(relido.despertarLiberado());assertTrue(relido.pontos()>=0);
        var normal=new MestriaRpg();assertEquals(1,normal.nivel());assertFalse(normal.podeUsarSlot(4));
    }
    @Test void mestriaTemCemNiveisEscalaLimitadaENaoEstouraInteiros(){
        var m=new MestriaRpg();assertEquals(1,m.nivel());assertEquals(0,m.vidaAdicional());
        assertEquals(99,m.ganharXp(Integer.MAX_VALUE));assertEquals(100,m.nivel());assertEquals(9900,m.xp());
        assertEquals(198,m.vidaAdicional());assertEquals(2.485,m.multiplicadorMagico(),1e-9);assertEquals(.505,m.multiplicadorRecarga(),1e-9);
        assertEquals(0,m.ganharXp(-10));assertEquals(99,m.pontos());
    }
    @Test void despertarExigeArvoreNivelEAlmaDoMesmoKit(){
        var m=new MestriaRpg();assertFalse(m.consumirAlma(PersonagemRpg.GOJO,PersonagemRpg.GOJO));
        m.ganharXp(4900);assertFalse(m.comprar(NoRpg.DESPERTAR));
        for(var no:NoRpg.values())assertTrue(m.comprar(no));
        int pontos=m.pontos();assertFalse(m.comprar(NoRpg.TECNICA_X));assertEquals(pontos,m.pontos());
        assertFalse(m.consumirAlma(PersonagemRpg.GOJO,PersonagemRpg.SHADOW));assertFalse(m.podeUsarSlot(4));
        assertTrue(m.consumirAlma(PersonagemRpg.GOJO,PersonagemRpg.GOJO));assertTrue(m.podeUsarSlot(4));
        assertFalse(m.consumirAlma(PersonagemRpg.GOJO,PersonagemRpg.GOJO));
        assertTrue(new MestriaRpg(m.xp(),m.nos(),true).despertarLiberado());
    }
    @Test void nbtInvalidoNaoLiberaNosEPerfilMigradoPreservaTecnicas(){
        var invalido=new MestriaRpg(0,255,true);assertEquals(0,invalido.nos());assertFalse(invalido.despertarLiberado());
        var legado=MestriaRpg.migrar(400,true,true,true);assertTrue(legado.podeUsarSlot(3));assertFalse(legado.podeUsarSlot(4));
        assertEquals(10,legado.nivel());assertEquals(3,legado.pontos());assertFalse(legado.podeUsarSlot(5));
    }
    @Test void manaAplicaBonusDeConjuntoUmaVezENaoAceitaNan(){
        var mana=new RecursoMana(100);assertTrue(mana.consumir(50,true));assertEquals(60,mana.atual());
        assertFalse(mana.consumir(80,false));assertEquals(60,mana.atual());
        assertThrows(IllegalArgumentException.class,()->mana.consumir(Double.NaN,false));
        mana.regenerar(20,5);assertEquals(65,mana.atual());mana.regenerar(Long.MAX_VALUE,5);assertEquals(100,mana.atual());
        assertEquals(0,new RecursoMana(Double.POSITIVE_INFINITY).atual());
    }
    @Test void trocarKitNaoResetaGEOsSnapshotsSaoCopiados(){
        var cds=new RecargasRpg();assertTrue(cds.iniciar(PersonagemRpg.SHADOW,4,100,6000,.505));
        assertEquals(3030,cds.restante(PersonagemRpg.SHADOW,4,100));assertEquals(0,cds.restante(PersonagemRpg.GOJO,4,100));
        assertFalse(cds.iniciar(PersonagemRpg.SHADOW,4,101,6000,1));assertEquals(1,cds.fracao(PersonagemRpg.SHADOW,4,100));
        long[] copy=cds.prazos();copy[9]=0;assertEquals(3030,cds.restante(PersonagemRpg.SHADOW,4,100));
        assertTrue(cds.iniciar(PersonagemRpg.SHADOW,4,3130,6000,1));
        assertThrows(IllegalArgumentException.class,()->cds.restante(PersonagemRpg.SHADOW,5,0));
    }
    @Test void catalogoPreservaTodosOsMovesetsAprovados()throws Exception{
        var catalogo=CatalogoMovesetsRpg.padrao();assertEquals(15,catalogo.tamanho());
        for(var p:PersonagemRpg.values()){assertEquals(4,catalogo.get(p).base().size());assertEquals(4,catalogo.get(p).transformada().size());}
        assertTrue(catalogo.get(PersonagemRpg.SHADOW).despertar().startsWith("I Am Atomic"));
        assertEquals("Teleporte Curto",catalogo.get(PersonagemRpg.GOJO).base().get(2).nome());
        assertEquals("Buraco Negro Mágico",catalogo.get(PersonagemRpg.FRIEREN).transformada().get(2).nome());
        assertEquals("Gancho DMT",catalogo.get(PersonagemRpg.EREN).base().get(1).nome());
        assertEquals(1,PersonagemRpg.GOJO.id);assertEquals(2,PersonagemRpg.SHADOW.id);assertEquals(3,PersonagemRpg.FRIEREN.id);
    }
    @Test void catalogoCorrompidoNaoECarregado(){
        assertThrows(IOException.class,()->CatalogoMovesetsRpg.ler(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8))));
        assertThrows(IOException.class,()->CatalogoMovesetsRpg.ler(new ByteArrayInputStream(new byte[131073])));
    }
}
