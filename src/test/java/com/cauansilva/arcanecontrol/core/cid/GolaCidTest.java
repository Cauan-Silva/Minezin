package com.cauansilva.arcanecontrol.core.cid;
import org.junit.jupiter.api.Test;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class GolaCidTest {
    @Test void detalhesTemMovimentoMenorQueACapaENaoAlteramPoseDeEntrada(){
        for(int i=0;i<500;i++){
            var entrada=new MolaMantoCid.Pose(Math.sin(i)*.35,Math.cos(i)*.12,Math.sin(i*.3)*.12);
            for(String n:DetalhesTrajeCid.OSSOS){var p=DetalhesTrajeCid.pose(n,entrada);double limite=n.startsWith("cid_collar_")?.045:.1;assertTrue(Math.abs(p.x())<=limite&&Math.abs(p.y())<=limite&&Math.abs(p.z())<=limite);}
        }
        var base=new MolaMantoCid.Pose(-.15,0,0);var l=DetalhesTrajeCid.pose("cid_collar_left",base);var r=DetalhesTrajeCid.pose("cid_collar_right",base);
        assertEquals(-l.z(),r.z(),1e-9);assertTrue(l.x()<0);assertTrue(DetalhesTrajeCid.pose("aba_esq",base).x()>0);
        assertEquals(new MolaMantoCid.Pose(0,0,0),DetalhesTrajeCid.pose("armorHead",base));
    }
    @Test void golaTemPonteTraseiraELateraisComSobreposicao()throws Exception{
        JsonObject geo;try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/geo/armaduras/shadow.geo.json")){geo=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();}
        var b=new HashMap<String,JsonObject>();for(var e:geo.getAsJsonArray("bones")){var x=e.getAsJsonObject();b.put(x.get("name").getAsString(),x);}
        assertEquals("armorBody",b.get("cid_yoke").get("parent").getAsString());assertEquals(3,b.get("cid_yoke").getAsJsonArray("cubes").size());
        for(String n:new String[]{"cid_collar_back","cid_collar_left","cid_collar_right"})assertEquals("cid_yoke",b.get(n).get("parent").getAsString());
        var ponte=b.get("cid_yoke").getAsJsonArray("cubes").get(0).getAsJsonObject();var traseira=b.get("cid_collar_back").getAsJsonArray("cubes").get(0).getAsJsonObject();
        assertTrue(ponte.getAsJsonArray("origin").get(2).getAsDouble()+ponte.getAsJsonArray("size").get(2).getAsDouble()>traseira.getAsJsonArray("origin").get(2).getAsDouble()+.25);
        assertTrue(ponte.getAsJsonArray("origin").get(1).getAsDouble()+ponte.getAsJsonArray("size").get(1).getAsDouble()>traseira.getAsJsonArray("origin").get(1).getAsDouble()+.25);
        assertTrue(ponte.getAsJsonArray("size").get(0).getAsDouble()<=10.3);
        assertTrue(ponte.getAsJsonArray("size").get(2).getAsDouble()<=2.6);
        assertTrue(traseira.getAsJsonArray("size").get(1).getAsDouble()<=1.9);
        assertEquals(0,b.get("armorHead").getAsJsonArray("cubes").size());
        assertEquals(20,b.keySet().stream().filter(n->n.startsWith("cid_cape_")).count());
    }
}
