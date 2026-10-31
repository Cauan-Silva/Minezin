package com.cauansilva.arcanecontrol.core.client;
import com.google.gson.*;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
class AnimeAssetsTest {
    private JsonObject json(String path)throws Exception{
        try(var in=getClass().getResourceAsStream(path)){assertNotNull(in,path);return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}
    }
    @Test void frierenTemQuatroAnimacoesFinitasValidas()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/frieren.json")){
            assertNotNull(in);var animations=AnimationSerializing.deserializeAnimation(in);assertEquals(4,animations.size());
            for(var animation:animations){assertFalse(animation.isInfinite);assertTrue(animation.endTick>0);}
        }
    }
    @Test void armadurasPossuemHierarquiaTexturaEReceitasQuePreservamDados()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/textures/armor/anime_atlas.png")){
            assertNotNull(in);var image=javax.imageio.ImageIO.read(in);assertNotNull(image);assertEquals(1254,image.getWidth());assertEquals(1254,image.getHeight());
        }
        for(String style:new String[]{"gojo","frieren"}){
            var root=json("/assets/arcanecontrol/geo/armaduras/"+style+".geo.json");
            var geometry=root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            assertEquals(1254,geometry.getAsJsonObject("description").get("texture_width").getAsInt());
            var names=new HashSet<String>();var bones=geometry.getAsJsonArray("bones");int cubes=0;
            for(var element:bones){var b=element.getAsJsonObject();assertTrue(names.add(b.get("name").getAsString()));if(b.has("cubes"))cubes+=b.getAsJsonArray("cubes").size();}
            assertTrue(cubes>=60);
            for(var element:bones){var b=element.getAsJsonObject();if(b.has("parent"))assertTrue(names.contains(b.get("parent").getAsString()));}
            for(String piece:new String[]{"cabeca","peito","pernas","botas"}){
                var item=json("/assets/arcanecontrol/models/item/"+style+"_"+piece+".json");assertEquals("builtin/entity",item.get("parent").getAsString());
                var recipe=json("/data/arcanecontrol/recipes/"+style+"_"+piece+".json");assertEquals("minecraft:smithing_transform",recipe.get("type").getAsString());
                assertEquals("arcanecontrol:"+style+"_"+piece,recipe.getAsJsonObject("result").get("item").getAsString());
            }
        }
    }
}
