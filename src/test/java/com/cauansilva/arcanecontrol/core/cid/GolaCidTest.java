package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoIchigo;
import org.junit.jupiter.api.Test;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
/** Shadow's outfit (tools/build_cid_traje.py): no hood, high collar with the straps hanging from it, coat as cloth. */
class GolaCidTest {
    private static Map<String,JsonObject> ossos()throws Exception{
        JsonObject geo;try(var in=GolaCidTest.class.getResourceAsStream("/assets/arcanecontrol/geo/armaduras/shadow.geo.json")){geo=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();}
        var b=new HashMap<String,JsonObject>();for(var e:geo.getAsJsonArray("bones")){var x=e.getAsJsonObject();b.put(x.get("name").getAsString(),x);}return b;
    }
    @Test void semCapuzGolaAltaComAlcasEmTecido()throws Exception{
        var b=ossos();
        assertEquals(0,b.get("armorHead").getAsJsonArray("cubes").size());
        assertEquals("armorBody",b.get("cid_gola").get("parent").getAsString());
        for(int c=0;c<TecidoIchigo.COLUNAS_FAIXA;c++)assertEquals("cid_gola",b.get("mg_faixa_"+c+"_0").get("parent").getAsString());
        assertTrue(b.containsKey("cid_glow_gema"));assertEquals(3,b.get("cid_gola").getAsJsonArray("cubes").size(),"paleta preto/roxo/ouro");
        assertFalse(b.keySet().stream().anyMatch(n->n.startsWith("cid_cape_")||n.startsWith("capa_")),"manto antigo removido");
    }
    @Test void caudaInteiraEmTecidoSemEsticarERepousa(){
        var t=new TecidoIchigo();
        for(int i=0;i<60;i++)t.tick(new MolaMantoCid.Entrada(.27,0,0,0,true));t.impulso(.9);
        for(int i=0;i<8;i++)t.tick(new MolaMantoCid.Entrada(.27,0,0,.1,true),.2,-.2);  // ability impulse while running (Passo das Sombras / golpes)
        assertTrue(t.erroAlongamento()<.08,"alongamento "+t.erroAlongamento());
        for(int i=0;i<200;i++)t.tick(new MolaMantoCid.Entrada(0,0,0,0,true));
        for(var o:TecidoIchigo.OSSOS){var q=t.pose(o,0);assertTrue(Math.abs(q.x())<.4&&Math.abs(q.y())<.4&&Math.abs(q.z())<.4,o);}
    }
    /** Same scenario as TecidoIchigoTest, plus a Passo das Sombras burst, for tools/preview_ichigo_traje.py --estilo shadow. */
    @Test void exportaParaPrevia()throws Exception{
        var t=new TecidoIchigo();var quadros=new ArrayList<Object>();double antes=0;
        for(int i=0;i<160;i++){
            double frente=i<20?0:i<80?.28:i<100?.05:0,giro=i>=100&&i<112?.18:0;if(i==20)t.impulso(.9);
            double braco=i>=20&&i<80?Math.sin(i*.55)*Math.toRadians(32):0,db=braco-antes;antes=braco;
            t.tick(new MolaMantoCid.Entrada(frente,0,0,giro,true),-db,db);var o=new LinkedHashMap<String,Object>();
            for(var n:TecidoIchigo.OSSOS){var q=t.pose(n,0);o.put(n,List.of(q.x(),q.y(),q.z()));}
            quadros.add(Map.of("fase",i<20?"PARADO":i<80?"CORRENDO":i<100?"PARANDO":i<112?"GIRANDO":"PARADO","ossos",o));
        }
        java.nio.file.Files.createDirectories(java.nio.file.Path.of("build"));
        try(var w=java.nio.file.Files.newBufferedWriter(java.nio.file.Path.of("build/cid-coat-samples.json"))){new Gson().toJson(quadros,w);}
    }
}
