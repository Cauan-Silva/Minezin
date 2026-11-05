package com.cauansilva.arcanecontrol.core.subaru;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/** Agasalho do Subaru (tools/build_subaru_traje.py): textura de 128 px no geo, icones, receitas e nomes das 4 pecas. */
class TrajeSubaruTest {
    private static String ler(String p)throws Exception{try(var in=TrajeSubaruTest.class.getResourceAsStream(p)){assertNotNull(in,p);return new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}}
    @Test void geoUsaTexturaDe128()throws Exception{
        // regressao: build_armor_models.document gravava o atlas antigo (1254 px) e o traje saia todo preto
        for(String s:new String[]{"subaru","subaru_helmet","subaru_chestplate","subaru_leggings","subaru_boots"}){
            var d=JsonParser.parseString(ler("/assets/arcanecontrol/geo/armaduras/"+s+".geo.json")).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonObject("description");
            assertEquals(128,d.get("texture_width").getAsInt(),s);assertEquals(128,d.get("texture_height").getAsInt(),s);}
        assertNotNull(TrajeSubaruTest.class.getResource("/assets/arcanecontrol/textures/armor/trajes/subaru.png"));
        assertNotNull(TrajeSubaruTest.class.getResource("/assets/arcanecontrol/animations/armaduras/subaru.animation.json"));
    }
    @Test void pecasTemIconeReceitaENome()throws Exception{
        var pt=JsonParser.parseString(ler("/assets/arcanecontrol/lang/pt_br.json")).getAsJsonObject();var en=JsonParser.parseString(ler("/assets/arcanecontrol/lang/en_us.json")).getAsJsonObject();
        for(String p:new String[]{"cabeca","peito","pernas","botas"}){
            assertNotNull(TrajeSubaruTest.class.getResource("/assets/arcanecontrol/models/item/subaru_"+p+".json"),p);
            var r=JsonParser.parseString(ler("/data/arcanecontrol/recipes/subaru_"+p+".json")).getAsJsonObject();
            assertEquals("arcanecontrol:subaru_"+p,r.getAsJsonObject("result").get("item").getAsString());
            assertTrue(pt.has("item.arcanecontrol.subaru_"+p)&&en.has("item.arcanecontrol.subaru_"+p),p);
        }
    }
}
