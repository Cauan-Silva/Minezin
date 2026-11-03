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
                // The ultimate (307) lands its strongest hit on the final X.
                int slot=(tipo-300)%4;double t=(tipo==307?com.cauansilva.arcanecontrol.core.vfx.UltAsta.FINAL:PerfilAsta.impacto(slot,tipo>=304))/20.0;
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
        // The ultimate's strongest hit (the final X) is its damage tick.
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.UltAsta.FINAL,PerfilAsta.impacto(3,true));assertEquals(0,RoteiroGojo.impacto(35,28));assertEquals(0,RoteiroGojo.impacto(35,46));assertTrue(RoteiroGojo.impacto(35,47)>0);assertEquals(1,RoteiroGojo.amostrar(35,55).fov());
        for(double t=0;t<55;t+=.1){var q=RoteiroGojo.amostrar(35,t);assertTrue(Double.isFinite(q.x())&&Double.isFinite(q.y())&&q.fov()>=.7&&q.fov()<=1.4);}
    }
    @Test void devilUnionColaNoCorpoDoJogador()throws Exception{
        // The outfit hides during the form, so the black coating (each part's largest cube) hugs the vanilla body.
        var limites=java.util.Map.of("armorBody",new double[]{-4,4,12,24,-2,2},"armorRightArm",new double[]{-8,-4,12,24,-2,2},"armorLeftArm",new double[]{4,8,12,24,-2,2},
                "armorRightLeg",new double[]{-4,0,0,12,-2,2},"armorLeftLeg",new double[]{0,4,0,12,-2,2});
        int revestimentos=0;
        for(var b:json("/assets/arcanecontrol/geo/armaduras/asta_forma.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")){
            var o=b.getAsJsonObject();var lim=limites.get(o.get("name").getAsString());if(lim==null)continue;
            JsonObject maior=null;double volume=0;
            for(var c:o.getAsJsonArray("cubes")){var s=c.getAsJsonObject().getAsJsonArray("size");double v=s.get(0).getAsDouble()*s.get(1).getAsDouble()*s.get(2).getAsDouble();if(v>volume){volume=v;maior=c.getAsJsonObject();}}
            assertNotNull(maior);revestimentos++;var or=maior.getAsJsonArray("origin");var sz=maior.getAsJsonArray("size");
            for(int i=0;i<3;i++){
                // Arms may cap the shoulder like the outfits do (outer sleeve layer ends at 24.25).
                double a=or.get(i).getAsDouble(),f=a+sz.get(i).getAsDouble(),folga=i==1?.55:.3;
                assertTrue(a>=lim[i*2]-folga&&f<=lim[i*2+1]+folga,o.get("name")+" eixo "+i+": "+a+".."+f);
            }
        }
        assertEquals(5,revestimentos,"um revestimento por parte do corpo");
    }
    @Test void matadoraDespertaMantemCaboEOssosDaNormal()throws Exception{
        // Same bones, pivots and grip: the held transform and the Black Divider scaling must not jump on the swap.
        var normal=new HashMap<String,JsonObject>();var desperta=new HashMap<String,JsonObject>();
        for(var b:json("/assets/arcanecontrol/geo/asta/matadora.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones"))normal.put(b.getAsJsonObject().get("name").getAsString(),b.getAsJsonObject());
        for(var b:json("/assets/arcanecontrol/geo/asta/matadora_desperta.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones"))desperta.put(b.getAsJsonObject().get("name").getAsString(),b.getAsJsonObject());
        assertEquals(normal.keySet(),desperta.keySet());
        for(String osso:normal.keySet())assertEquals(normal.get(osso).get("pivot"),desperta.get(osso).get("pivot"),osso);
        var caboNormal=normal.get("root").getAsJsonArray("cubes").get(0).getAsJsonObject();var caboDesperta=desperta.get("root").getAsJsonArray("cubes").get(0).getAsJsonObject();
        assertEquals(caboNormal.get("origin"),caboDesperta.get("origin"));assertEquals(caboNormal.get("size"),caboDesperta.get("size"));
        assertNotNull(getClass().getResourceAsStream("/assets/arcanecontrol/textures/armor/trajes/matadora_desperta_glowmask.png"));
    }
    private JsonObject json(String path)throws Exception{try(var in=getClass().getResourceAsStream(path)){assertNotNull(in,path);return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}}
    @Test void noveGestosCarregamNoPlayerAnimatorSemLoopPersistente()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/asta.json")){assertNotNull(in);var gestos=AnimationSerializing.deserializeAnimation(in);assertEquals(9,gestos.size());int longos=0;for(var g:gestos){assertFalse(g.isInfinite);assertTrue(g.endTick>0&&g.endTick<=com.cauansilva.arcanecontrol.core.vfx.UltAsta.DURACAO);if(g.endTick>55)longos++;}
            // Only the ultimate (V in the form) outlasts the 55-tick gesture budget.
            assertTrue(longos<=1);}
    }
    @Test void armaduraNaoImpõeCabeloEFormaTemAsaEChifreSeparados()throws Exception{
        var g=json("/assets/arcanecontrol/geo/armaduras/asta.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        for(var b:g.getAsJsonArray("bones")){var o=b.getAsJsonObject();assertFalse(o.get("name").getAsString().toLowerCase().contains("hair"));assertFalse(o.get("name").getAsString().toLowerCase().contains("cabelo"));if(o.get("name").getAsString().equals("armorHead"))for(var c:o.getAsJsonArray("cubes"))assertTrue(c.getAsJsonObject().getAsJsonArray("origin").get(1).getAsDouble()>=30,"A faixa deve ficar na testa e preservar os olhos da skin");}
        // Devil Union head: crown (band, 13 teeth, diamond) and two 6-piece horns, no hair; mirrored bat wings on the back.
        int asaEsq=0,asaDir=0,cabeca=0;
        for(var c:json("/assets/arcanecontrol/geo/armaduras/asta_forma.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")){
            var o=c.getAsJsonObject();String n=o.get("name").getAsString();
            if(n.startsWith("du_asa_e_"))asaEsq++;else if(n.startsWith("du_asa_d_"))asaDir++;else if(n.equals("armorHead"))cabeca=o.getAsJsonArray("cubes").size();
        }
        assertTrue(asaEsq>=20);assertEquals(asaEsq,asaDir);assertEquals(27,cabeca);
        var espada=json("/assets/arcanecontrol/models/item/matadora_demonios.json");assertEquals("builtin/entity",espada.get("parent").getAsString());assertTrue(espada.getAsJsonObject("display").has("firstperson_lefthand"));
        for(String item:new String[]{"asta_cabeca","asta_peito","asta_pernas","asta_botas","matadora_demonios"}){
            var recipe=json("/data/arcanecontrol/recipes/"+item+".json");assertEquals("minecraft:smithing_transform",recipe.get("type").getAsString());assertEquals("arcanecontrol:"+item,recipe.getAsJsonObject("result").get("item").getAsString());
        }
    }
}
