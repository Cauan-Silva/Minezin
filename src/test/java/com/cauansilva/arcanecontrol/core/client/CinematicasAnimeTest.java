package com.cauansilva.arcanecontrol.core.client;

import com.google.gson.*;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class CinematicasAnimeTest {
    private JsonObject animations(String kit)throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/"+kit+".json")){
            assertNotNull(in);return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");
        }
    }
    @Test void bibliotecaRealCarregaDozeGestosDespertosSemPrenderPernas()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/despertos.json")){
            assertNotNull(in);var all=AnimationSerializing.deserializeAnimation(in);
            var nomes=all.stream().map(a->a.extraData.get("name").toString()).collect(Collectors.toSet());
            assertEquals(Set.of("desperto_1_32","desperto_1_40","desperto_1_41","desperto_2_33","desperto_2_34","desperto_2_35","desperto_2_36","desperto_3_30","desperto_3_37","desperto_3_38","desperto_3_39","desperto_3_200"),nomes);
            for(var a:all){
                assertFalse(a.isInfinite);assertTrue(a.endTick>0);
                for(String perna:new String[]{"rightLeg","leftLeg"}){var p=a.getPart(perna);assertTrue(p==null||!p.isEnabled());}
                assertNotNull(a.getPart("torso"));assertTrue(a.getPart("torso").isEnabled(),a.extraData.get("name").toString());
            }
        }
    }
    @Test void ultimatesSeguramPoseNoImpactoERetornamAoNeutro()throws Exception{
        for(var spec:List.of(new Object[]{"cid","cid_4",4.5,4.65},new Object[]{"frieren","frieren_4",3.5,3.65},new Object[]{"frieren","frieren_4",11d,11.15},new Object[]{"asta","asta_200",2.35,2.5})){
            var a=animations((String)spec[0]).getAsJsonObject((String)spec[1]);double fim=a.get("animation_length").getAsDouble();
            for(var b:a.getAsJsonObject("bones").entrySet()){
                var frames=b.getValue().getAsJsonObject().getAsJsonObject("rotation");
                var start=frame(frames,(double)spec[2]);var hold=frame(frames,(double)spec[3]);
                if(start!=null){assertNotNull(hold);assertEquals(start.getAsJsonArray("vector"),hold.getAsJsonArray("vector"));}
                var end=frame(frames,fim);assertNotNull(end);
                for(var v:end.getAsJsonArray("vector"))assertEquals(0,v.getAsDouble(),1e-9);
            }
        }
    }
    private JsonObject frame(JsonObject frames,double time){
        for(var e:frames.entrySet())if(Math.abs(Double.parseDouble(e.getKey())-time)<1e-6)return e.getValue().getAsJsonObject();return null;
    }
    @Test void chuvaELuzCelesteLiberamNaJanelaDoServidor()throws Exception{
        var all=animations("despertos");
        assertNotNull(frame(all.getAsJsonObject("desperto_2_34").getAsJsonObject("bones").getAsJsonObject("right_arm").getAsJsonObject("rotation"),24/20d));
        assertNotNull(frame(all.getAsJsonObject("desperto_3_38").getAsJsonObject("bones").getAsJsonObject("right_arm").getAsJsonObject("rotation"),25/20d));
        assertEquals(12,all.getAsJsonObject("desperto_2_33").get("animation_length").getAsDouble()*20,1e-9);
    }
}
