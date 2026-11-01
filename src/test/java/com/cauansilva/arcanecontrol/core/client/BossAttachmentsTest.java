package com.cauansilva.arcanecontrol.core.client;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.loading.json.raw.*;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BossAttachmentsTest {
    private Map<String,JsonObject> bones(int kit)throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/geo/nemesis/"+kit+".geo.json")){
            assertNotNull(in);var root=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();var out=new HashMap<String,JsonObject>();
            for(var v:root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")){var b=v.getAsJsonObject();out.put(b.get("name").getAsString(),b);}return out;
        }
    }
    private double[] vec(JsonObject c,String key){var a=c.getAsJsonArray(key);return new double[]{a.get(0).getAsDouble(),a.get(1).getAsDouble(),a.get(2).getAsDouble()};}
    private Vector4f nativePoint(JsonObject c,boolean tip){
        var o=vec(c,"origin");var pivot=vec(c,"pivot");var size=vec(c,"size");var rot=vec(c,"rotation");
        var raw=new Cube(0d,false,o,pivot,rot,size,new UVUnion(new double[]{0,0},null,true));
        var props=new ModelProperties(null,null,null,null,null,null,null,null,null,null,"test",null,64,64,null,null,null);
        var cube=new BakedModelFactory.Builtin().constructCube(raw,props,new GeoBone(null,"head",false,0d,false,false));
        var pose=new PoseStack();RenderUtils.translateToPivotPoint(pose,cube);RenderUtils.rotateMatrixAroundCube(pose,cube);RenderUtils.translateAwayFromPivotPoint(pose,cube);
        return new Vector4f((float)-pivot[0]/16,(float)(pivot[1]+(tip?size[1]:0))/16,(float)pivot[2]/16,1).mul(pose.last().pose());
    }
    @Test void chifresPermanecemConectadosDepoisDoConversorRealDoGeckoLib()throws Exception{
        for(int kit:new int[]{3,4}){
            var groups=new HashMap<Integer,List<JsonObject>>();
            for(var v:bones(kit).get("head").getAsJsonArray("cubes")){
                var c=v.getAsJsonObject();if(!c.has("pivot"))continue;var size=vec(c,"size");var pivot=vec(c,"pivot");
                if(pivot[1]<30||size[0]>1.71||Math.abs(size[0]-size[2])>1e-6)continue;
                groups.computeIfAbsent(pivot[0]<0?-1:1,k->new ArrayList<>()).add(c);
            }
            assertEquals(2,groups.size());
            for(var segments:groups.values()){
                assertEquals(4,segments.size());segments.sort(Comparator.comparingDouble(c->vec(c,"pivot")[1]));
                assertEquals(4,Math.abs(vec(segments.get(0),"pivot")[0]),1e-6);
                for(int i=0;i<3;i++){
                    var end=nativePoint(segments.get(i),true);var start=nativePoint(segments.get(i+1),false);
                    assertEquals(start.x,end.x,1e-5);assertEquals(start.y,end.y,1e-5);assertEquals(start.z,end.z,1e-5);
                }
            }
        }
    }
    @Test void caudaDoLiebeTemJuntasConectadas()throws Exception{
        var b=bones(4);
        for(int i=0;i<3;i++){
            var a=b.get("tail_"+i).getAsJsonArray("cubes").get(0).getAsJsonObject();var c=b.get("tail_"+(i+1)).getAsJsonArray("cubes").get(0).getAsJsonObject();
            var end=nativePoint(a,true);var start=nativePoint(c,false);
            assertEquals(start.x,end.x,1e-5);assertEquals(start.y,end.y,1e-5);assertEquals(start.z,end.z,1e-5);
            assertEquals("tail_"+i,b.get("tail_"+(i+1)).get("parent").getAsString());
        }
    }
    @Test void dedosDoQualCompartilhamARotacaoDoBraco()throws Exception{
        for(String n:new String[]{"lower_arm_-1","lower_arm_1"}){
            var b=bones(3).get(n);assertNotNull(b);assertTrue(b.has("rotation"));assertEquals(4,b.getAsJsonArray("cubes").size());
            for(var c:b.getAsJsonArray("cubes"))assertFalse(c.getAsJsonObject().has("rotation"),"A mao nao pode ficar fora da rotacao do braco");
        }
    }
    @Test void armaExclusivaDoTojiTemEmpunhaduraNaMaoELaminaAbaixoDoPunho()throws Exception{
        var weapon=bones(1).get("weapon");assertEquals("rightArm",weapon.get("parent").getAsString());
        var grip=weapon.getAsJsonArray("cubes").get(0).getAsJsonObject();var o=vec(grip,"origin");var size=vec(grip,"size");var p=vec(weapon,"pivot");
        for(int i=0;i<3;i++)assertTrue(p[i]>=o[i]&&p[i]<=o[i]+size[i]);
        assertTrue(o[1]<15&&o[1]+size[1]>12);assertTrue(o[2]<-2&&o[2]+size[2]>-2,"Cabo precisa tocar o volume da palma");
        var blade=weapon.getAsJsonArray("cubes").get(2).getAsJsonObject();assertTrue(vec(blade,"origin")[1]+vec(blade,"size")[1]<12);
    }
}
