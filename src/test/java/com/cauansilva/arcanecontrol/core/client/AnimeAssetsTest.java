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
    @Test void modelosDeInventarioNaoIncluemOutrosSlots()throws Exception{
        String[] slots={"helmet","chestplate","leggings","boots"};
        var roots=java.util.List.of(Set.of("armorHead"),Set.of("armorBody","armorLeftArm","armorRightArm"),Set.of("armorLeftLeg","armorRightLeg"),Set.of("armorLeftBoot","armorRightBoot"));
        for(String style:new String[]{"gojo","frieren","shadow"})for(int i=0;i<slots.length;i++){
            var geometry=json("/assets/arcanecontrol/geo/armaduras/"+style+"_"+slots[i]+".geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            var bones=new HashMap<String,JsonObject>();
            for(var element:geometry.getAsJsonArray("bones")){var b=element.getAsJsonObject();bones.put(b.get("name").getAsString(),b);}
            int cubes=0;
            for(var b:bones.values())if(b.has("cubes")&&!b.getAsJsonArray("cubes").isEmpty()){
                cubes+=b.getAsJsonArray("cubes").size();var root=b;var visited=new HashSet<String>();
                while(root.has("parent")){assertTrue(visited.add(root.get("name").getAsString()));root=bones.get(root.get("parent").getAsString());assertNotNull(root);}
                assertTrue(roots.get(i).contains(root.get("name").getAsString()),"Outra peca vazou para o icone");
            }
            assertTrue(cubes>0);
        }
    }
    @Test void cajadoTemModeloCompletoAnimacaoCompativelEReceita()throws Exception{
        var geometry=json("/assets/arcanecontrol/geo/frieren/cajado.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var names=new HashSet<String>();double minY=100,maxY=-100;
        for(var element:geometry.getAsJsonArray("bones")){
            var b=element.getAsJsonObject();assertTrue(names.add(b.get("name").getAsString()));
            for(var cube:b.getAsJsonArray("cubes")){
                var c=cube.getAsJsonObject();double y=c.getAsJsonArray("origin").get(1).getAsDouble();
                minY=Math.min(minY,y);maxY=Math.max(maxY,y+c.getAsJsonArray("size").get(1).getAsDouble());
            }
        }
        assertTrue(maxY-minY>30&&maxY-minY<50);assertTrue(names.containsAll(Set.of("root","gema","fita_esq","fita_dir")));
        var animation=json("/assets/arcanecontrol/animations/frieren/cajado.animation.json").getAsJsonObject("animations").getAsJsonObject("animation.cajado.idle");
        for(String name:animation.getAsJsonObject("bones").keySet())assertTrue(names.contains(name));
        var item=json("/assets/arcanecontrol/models/item/cajado_frieren.json");assertEquals("builtin/entity",item.get("parent").getAsString());
        for(String view:new String[]{"gui","firstperson_righthand","firstperson_lefthand","thirdperson_righthand","thirdperson_lefthand"})assertTrue(item.getAsJsonObject("display").has(view));
        var recipe=json("/data/arcanecontrol/recipes/cajado_frieren.json");assertEquals("minecraft:crafting_shaped",recipe.get("type").getAsString());
        assertEquals("arcanecontrol:cajado_frieren",recipe.getAsJsonObject("result").get("item").getAsString());
        var advancement=json("/data/arcanecontrol/advancements/recipes/combat/cajado_frieren.json");
        assertEquals("arcanecontrol:cajado_frieren",advancement.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString());
    }
    @Test void armadurasPossuemHierarquiaTexturaEReceitasQuePreservamDados()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/textures/armor/anime_reference_atlas.png")){
            assertNotNull(in);var image=javax.imageio.ImageIO.read(in);assertNotNull(image);assertEquals(1254,image.getWidth());assertEquals(1254,image.getHeight());
            double variacao=0;int amostras=0;
            for(int y=20;y<280;y++)for(int x=20;x<280;x++){
                int a=image.getRGB(x,y)&255,b=image.getRGB(x+1,y)&255;variacao+=Math.abs(a-b);amostras++;
            }
            assertTrue(variacao/amostras<1.5,"Tecido deve ter cor limpa, sem ruido de alta frequencia");
        }
        for(String style:new String[]{"gojo","frieren","shadow"}){
            var root=json("/assets/arcanecontrol/geo/armaduras/"+style+".geo.json");
            var geometry=root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            assertEquals(1254,geometry.getAsJsonObject("description").get("texture_width").getAsInt());
            var names=new HashSet<String>();var bones=geometry.getAsJsonArray("bones");int cubes=0;
            for(var element:bones){var b=element.getAsJsonObject();assertTrue(names.add(b.get("name").getAsString()));if(b.has("cubes"))cubes+=b.getAsJsonArray("cubes").size();}
            assertTrue(cubes>=30&&cubes<100);
            for(var element:bones){var b=element.getAsJsonObject();if(!style.equals("shadow")&&b.get("name").getAsString().equals("armorHead")){
                for(var cube:b.getAsJsonArray("cubes")){var c=cube.getAsJsonObject();assertTrue(c.getAsJsonArray("origin").get(1).getAsDouble()+c.getAsJsonArray("size").get(1).getAsDouble()<=30.5,"Sem cabelo acima da testa");}
            }}
            for(var element:bones){var b=element.getAsJsonObject();if(b.has("parent"))assertTrue(names.contains(b.get("parent").getAsString()));}
            String[] pieces={"cabeca","peito","pernas","botas"},shadow={"capuz","manto","calcas","botas"};
            for(int i=0;i<pieces.length;i++){
                String itemId=style.equals("shadow")?shadow[i]+"_shadow":style+"_"+pieces[i];
                var item=json("/assets/arcanecontrol/models/item/"+itemId+".json");assertEquals("builtin/entity",item.get("parent").getAsString());
                var recipe=json("/data/arcanecontrol/recipes/"+itemId+".json");
                if(!style.equals("shadow"))assertEquals("minecraft:smithing_transform",recipe.get("type").getAsString());
                assertEquals("arcanecontrol:"+itemId,recipe.getAsJsonObject("result").get("item").getAsString());
            }
        }
    }
}
