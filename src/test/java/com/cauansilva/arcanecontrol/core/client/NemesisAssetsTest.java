package com.cauansilva.arcanecontrol.core.client;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
class NemesisAssetsTest {
    @Test void altarPodeSerRecuperadoComPicaretaESuaReceitaEhDesbloqueavel()throws Exception{
        for(String tag:new String[]{"mineable/pickaxe","needs_iron_tool"})try(var in=getClass().getResourceAsStream("/data/minecraft/tags/blocks/"+tag+".json")){
            assertNotNull(in);var j=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();assertFalse(j.get("replace").getAsBoolean());assertTrue(j.getAsJsonArray("values").contains(new JsonPrimitive("arcanecontrol:altar_despertar")));
        }
        try(var in=getClass().getResourceAsStream("/data/arcanecontrol/loot_tables/blocks/altar_despertar.json")){
            assertNotNull(in);var j=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();assertEquals("arcanecontrol:altar_despertar",j.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
        }
    }
    private JsonObject json(String path)throws Exception{try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/"+path)){assertNotNull(in,path);return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}}
    @Test void tresChefesTemHierarquiaAnimavelCompletaSemCiclos()throws Exception{
        var anim=json("animations/nemesis.animation.json").getAsJsonObject("animations");
        for(int kit=1;kit<=3;kit++){
            var g=json("geo/nemesis/"+kit+".geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();var bones=new HashMap<String,JsonObject>();int cubos=0;
            for(var v:g.getAsJsonArray("bones")){var b=v.getAsJsonObject();assertNull(bones.put(b.get("name").getAsString(),b));if(b.has("cubes"))cubos+=b.getAsJsonArray("cubes").size();}
            assertTrue(cubos>=30);assertTrue(bones.containsKey("root"));
            for(var b:bones.values()){var vistos=new HashSet<String>();while(b.has("parent")){assertTrue(vistos.add(b.get("name").getAsString()));b=bones.get(b.get("parent").getAsString());assertNotNull(b);}}
            for(var a:anim.entrySet())for(var n:a.getValue().getAsJsonObject().getAsJsonObject("bones").keySet())assertTrue(bones.containsKey(n),n);
        }
    }
}
