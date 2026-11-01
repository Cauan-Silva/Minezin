package com.cauansilva.arcanecontrol.core.asta;
import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rpg.*;
import com.cauansilva.arcanecontrol.core.client.render.RoteiroGojo;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
class AstaTest {
    @Test void posesDeImpactoCoincidemComODanoESeguramDoisTicks()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/asta.json")){
            var a=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");
            for(int tipo:new int[]{300,301,302,304,305,306,307}){
                int slot=(tipo-300)%4;double t=PerfilAsta.impacto(slot,tipo>=304)/20.0;
                var frames=a.getAsJsonObject("asta_"+tipo).getAsJsonObject("bones").getAsJsonObject("right_arm").getAsJsonObject("rotation");
                assertEquals(frames.getAsJsonObject(Double.toString(t)).get("vector"),frames.getAsJsonObject(Double.toString(Math.round((t+.1)*100)/100.0)).get("vector"));
            }
        }
    }
    @Test void idsPersistidosAntigosNaoMudamEOQuartoKitTemCincoSlots(){
        assertEquals(1,PoderAnime.GOJO.id);assertEquals(2,PoderAnime.CID.id);assertEquals(3,PoderAnime.FRIEREN.id);assertEquals(4,PoderAnime.ASTA.id);
        for(int i=0;i<5;i++){assertEquals(31+i,PoderAnime.ASTA.tecnica(i));assertEquals(PoderAnime.ASTA,PoderAnime.daTecnica(31+i));}
        assertNull(PoderAnime.daTecnica(36));assertNull(PoderAnime.porId(5));
        var antigo=new EstadoPoderes(3,7,200,new int[]{100,200,300},new int[]{4,5,6});
        assertEquals(300,antigo.xp(PoderAnime.FRIEREN));assertFalse(antigo.tem(PoderAnime.ASTA));
        assertTrue(antigo.desbloquear(PoderAnime.ASTA,false));assertEquals(20,antigo.conhecimento());assertEquals(0,antigo.xp(PoderAnime.ASTA));
        assertTrue(antigo.selecionar(PoderAnime.ASTA));assertEquals(300,antigo.xp(PoderAnime.FRIEREN));assertEquals(4,antigo.usos()[0]);
    }
    @Test void reflexaoTemConeFrontalELimiteGlobalParaNaoOscilarEntreJogadores(){
        assertTrue(PerfilAsta.cone(.5));assertTrue(PerfilAsta.cone(1));assertFalse(PerfilAsta.cone(.499));assertFalse(PerfilAsta.cone(-1));assertFalse(PerfilAsta.cone(Double.NaN));
        assertTrue(PerfilAsta.podeRefletir(0,100,0));assertFalse(PerfilAsta.podeRefletir(1,109,110));assertTrue(PerfilAsta.podeRefletir(1,110,110));assertFalse(PerfilAsta.podeRefletir(2,200,110));
    }
    @Test void todasAsRecargasEImpactosSeguemOGddECinematicaTermina(){
        assertArrayEquals(new int[]{80,160,240,500},java.util.stream.IntStream.range(0,4).map(i->PerfilAsta.recarga(i,false)).toArray());
        assertArrayEquals(new int[]{60,200,300,400},java.util.stream.IntStream.range(0,4).map(i->PerfilAsta.recarga(i,true)).toArray());
        assertEquals(1200,new EstadoDespertar(PersonagemRpg.ASTA,0).restante(0));
        assertEquals(0,PerfilAsta.alcanceOnda(8));assertEquals(24,PerfilAsta.alcanceOnda(28));assertEquals(0,RoteiroGojo.impacto(35,28));assertEquals(0,RoteiroGojo.impacto(35,46));assertTrue(RoteiroGojo.impacto(35,47)>0);assertEquals(1,RoteiroGojo.amostrar(35,55).fov());
        for(double t=0;t<55;t+=.1){var q=RoteiroGojo.amostrar(35,t);assertTrue(Double.isFinite(q.x())&&Double.isFinite(q.y())&&q.fov()>=.7&&q.fov()<=1.4);}
    }
    private JsonObject json(String path)throws Exception{try(var in=getClass().getResourceAsStream(path)){assertNotNull(in,path);return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}}
    @Test void noveGestosCarregamNoPlayerAnimatorSemLoopPersistente()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/asta.json")){assertNotNull(in);var gestos=AnimationSerializing.deserializeAnimation(in);assertEquals(9,gestos.size());for(var g:gestos){assertFalse(g.isInfinite);assertTrue(g.endTick>0&&g.endTick<=55);}}
    }
    @Test void armaduraNaoImpõeCabeloEFormaTemAsaEChifreSeparados()throws Exception{
        var g=json("/assets/arcanecontrol/geo/armaduras/asta.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        for(var b:g.getAsJsonArray("bones")){var o=b.getAsJsonObject();assertFalse(o.get("name").getAsString().toLowerCase().contains("hair"));assertFalse(o.get("name").getAsString().toLowerCase().contains("cabelo"));if(o.get("name").getAsString().equals("armorHead"))for(var c:o.getAsJsonArray("cubes"))assertTrue(c.getAsJsonObject().getAsJsonArray("origin").get(1).getAsDouble()>=30,"A faixa deve ficar na testa e preservar os olhos da skin");}
        var forma=json("/assets/arcanecontrol/models/effects/asta_forma.json").getAsJsonArray("pieces");int asa=0,chifre=0;for(var c:forma){if(c.getAsJsonObject().get("attachment").getAsString().equals("body"))asa++;else if(c.getAsJsonObject().get("attachment").getAsString().equals("head"))chifre++;}assertTrue(asa>=12);assertEquals(6,chifre);
        var espada=json("/assets/arcanecontrol/models/item/matadora_demonios.json");assertEquals("builtin/entity",espada.get("parent").getAsString());assertTrue(espada.getAsJsonObject("display").has("firstperson_lefthand"));
        for(String item:new String[]{"asta_cabeca","asta_peito","asta_pernas","asta_botas","matadora_demonios"}){
            var recipe=json("/data/arcanecontrol/recipes/"+item+".json");assertEquals("minecraft:smithing_transform",recipe.get("type").getAsString());assertEquals("arcanecontrol:"+item,recipe.getAsJsonObject("result").get("item").getAsString());
        }
    }
}
