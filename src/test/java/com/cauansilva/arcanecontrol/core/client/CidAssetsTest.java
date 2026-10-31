package com.cauansilva.arcanecontrol.core.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.cauansilva.arcanecontrol.core.client.render.RoteiroGojo;
class CidAssetsTest {
    @Test void gestosCarregamNoPlayerAnimator() throws Exception {
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/cid.json")){
            assertNotNull(in);var gestos=AnimationSerializing.deserializeAnimation(in);
            assertEquals(4,gestos.size());
            for(var g:gestos){assertFalse(g.isInfinite);assertTrue(g.endTick>0);}
        }
    }
    @Test void atomicTemImpactoEEncerramentoDefinidos(){
        assertEquals(0,RoteiroGojo.impacto(14,89));assertEquals(.65,RoteiroGojo.impacto(14,90));
        assertEquals(0,RoteiroGojo.impacto(14,102));assertEquals(1,RoteiroGojo.amostrar(14,140).fov());
        for(double t=0;t<=140;t+=.25){var q=RoteiroGojo.amostrar(14,t);assertTrue(Double.isFinite(q.x()));assertTrue(q.fov()>=.7&&q.fov()<=1.15);}
    }
    @Test void modelosTemHierarquiaValida() throws Exception {
        for(String modelo:new String[]{"lamina","traje"}){
            try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/geo/cid/"+modelo+".geo.json")){
                assertNotNull(in);
                var bones=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject()
                    .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
                var nomes=new java.util.HashSet<String>();
                for(var b:bones)assertTrue(nomes.add(b.getAsJsonObject().get("name").getAsString()));
                for(var b:bones){var o=b.getAsJsonObject();if(o.has("parent"))assertTrue(nomes.contains(o.get("parent").getAsString()));}
            }
        }
    }
}
