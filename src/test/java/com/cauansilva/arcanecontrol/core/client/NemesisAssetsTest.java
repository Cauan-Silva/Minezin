package com.cauansilva.arcanecontrol.core.client;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
class NemesisAssetsTest {
    @Test void skinsEnviadasUsamUvVanilla64ESegundaCamadaSemFaceModelada()throws Exception{
        for(int kit:new int[]{1,2,3,4}){
            var geo=json("geo/nemesis/"+kit+".geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            assertEquals(64,geo.getAsJsonObject("description").get("texture_width").getAsInt());
            var bones=new HashMap<String,JsonObject>();for(var v:geo.getAsJsonArray("bones")){var b=v.getAsJsonObject();bones.put(b.get("name").getAsString(),b);}
            var head=bones.get("head").getAsJsonArray("cubes");
            assertEquals(JsonParser.parseString("[8,8]"),head.get(0).getAsJsonObject().getAsJsonObject("uv").getAsJsonObject("north").get("uv"));
            assertEquals(.5,head.get(1).getAsJsonObject().get("inflate").getAsDouble());
            for(String n:new String[]{"body","leftArm","rightArm","leftLeg","rightLeg"})assertEquals(2,bones.get(n).getAsJsonArray("cubes").size());
            String nome=kit==1?"toji":kit==2?"aurora":kit==3?"qual":"liebe";
            try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/textures/bosses/"+nome+"_skin.png")){
                assertNotNull(in);var image=javax.imageio.ImageIO.read(in);assertEquals(64,image.getWidth());assertEquals(64,image.getHeight());
            }
        }
    }
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

        for(int kit=1;kit<=4;kit++){
            var anim=json("animations/nemesis/"+kit+".animation.json").getAsJsonObject("animations");
            var g=json("geo/nemesis/"+kit+".geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();var bones=new HashMap<String,JsonObject>();int cubos=0;
            for(var v:g.getAsJsonArray("bones")){var b=v.getAsJsonObject();assertNull(bones.put(b.get("name").getAsString(),b));if(b.has("cubes"))cubos+=b.getAsJsonArray("cubes").size();}
            assertTrue(cubos>=12);assertTrue(bones.containsKey("root"));
            for(var b:bones.values()){var vistos=new HashSet<String>();while(b.has("parent")){assertTrue(vistos.add(b.get("name").getAsString()));b=bones.get(b.get("parent").getAsString());assertNotNull(b);}}
            for(var a:anim.entrySet())for(var n:a.getValue().getAsJsonObject().getAsJsonObject("bones").keySet())assertTrue(bones.containsKey(n),n);
        }
    }
}
