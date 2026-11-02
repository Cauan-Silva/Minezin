package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.client.tecido.TecidoGojo;
import com.google.gson.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TecidoGojoTest {
    private static final TecidoGojo.Entrada PARADO=new TecidoGojo.Entrada(0,0,0,0,true);
    @Test void segmentacaoPreservaOmbrosMembrosEBotao()throws Exception{
        var base=JsonParser.parseString(Files.readString(Path.of("art/gojo/gojo-base.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var atual=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/arcanecontrol/geo/armaduras/gojo.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var originais=new java.util.HashMap<String,JsonObject>();var novos=new java.util.HashMap<String,JsonObject>();
        for(var v:base.getAsJsonArray("bones"))originais.put(v.getAsJsonObject().get("name").getAsString(),v.getAsJsonObject());
        for(var v:atual.getAsJsonArray("bones"))novos.put(v.getAsJsonObject().get("name").getAsString(),v.getAsJsonObject());
        for(var entry:originais.entrySet())if(!entry.getKey().equals("armorBody"))assertEquals(entry.getValue(),novos.get(entry.getKey()),entry.getKey());
        assertEquals(originais.get("armorBody").getAsJsonArray("cubes").get(0),novos.get("armorBody").getAsJsonArray("cubes").get(0));
        for(String nome:TecidoGojo.OSSOS){assertTrue(novos.containsKey(nome));String parent=novos.get(nome).get("parent").getAsString();assertTrue(parent.equals("armorBody")||TecidoGojo.OSSOS.contains(parent));}
        assertEquals(21,TecidoGojo.OSSOS.size());
    }
    @Test void amostrarNaoAvancaFisicaEResetRemoveInercia(){
        var a=new TecidoGojo();var b=new TecidoGojo();
        for(int t=0;t<100;t++){
            var e=new TecidoGojo.Entrada(.22,.12,-.5,.1,false);a.tick(e);b.tick(e);
            for(String osso:TecidoGojo.OSSOS)for(int f=0;f<8;f++)a.pose(osso,f/8d);
        }
        for(String osso:TecidoGojo.OSSOS)assertEquals(a.pose(osso,1),b.pose(osso,1));
        a.tick(new TecidoGojo.Entrada(Double.NaN,0,0,0,true));
        for(String osso:TecidoGojo.OSSOS){var p=a.pose(osso,1);assertEquals(0,p.x(),0.);assertEquals(0,p.y(),0.);assertEquals(0,p.z(),0.);}
    }
    @Test void quedasEGirosMantemBarraCurtaEGolaFirme(){
        var m=new TecidoGojo();
        for(int t=0;t<500;t++){
            m.tick(new TecidoGojo.Entrada(Math.sin(t)*.3,Math.cos(t)*.3,t%30<20?-.8:.4,Math.sin(t*.3)*.3,false));
            for(String osso:TecidoGojo.OSSOS){
                var p=m.pose(osso,.5);assertTrue(Double.isFinite(p.x()+p.y()+p.z()));
                double limite=osso.equals("gojo_collar")?.035:.25;
                assertTrue(Math.abs(p.x())<=limite&&Math.abs(p.y())<=limite&&Math.abs(p.z())<=limite,osso);
            }
            assertTrue(m.erroAlongamento()<.06);
        }
        for(int t=0;t<300;t++)m.tick(PARADO);
        var gola=m.pose("gojo_collar",1);assertEquals(0,gola.x(),1e-8);assertEquals(0,gola.y(),1e-8);
    }
    @Test void frenteECostasRespondemAoSentidoDoDeslocamento(){
        var frente=new TecidoGojo();var tras=new TecidoGojo();
        for(int t=0;t<40;t++){
            frente.tick(new TecidoGojo.Entrada(.25,0,0,0,true));
            tras.tick(new TecidoGojo.Entrada(-.25,0,0,0,true));
        }
        assertTrue(Math.abs(frente.pose("gojo_hem_back_1_0",1).x())>Math.abs(tras.pose("gojo_hem_back_1_0",1).x())+.005);
        assertTrue(Math.abs(tras.pose("gojo_hem_front_1_0",1).x())>Math.abs(frente.pose("gojo_hem_front_1_0",1).x())+.005);
    }
    @Test void exportaFisicaRealParaRevisaoVisual()throws Exception{
        var m=new TecidoGojo();var frames=new JsonArray();
        for(int t=0;t<240;t++){
            String fase=t<30?"REPOUSO":t<70?"CORRIDA":t<100?"PASSO LATERAL":t<120?"GIRO":t<130?"SALTO":t<160?"QUEDA":t<170?"POUSO":"RETORNO AMORTECIDO";
            m.tick(new TecidoGojo.Entrada(t>=30&&t<70?.25:0,t>=70&&t<100?.23:0,t>=120&&t<130?.3:t>=130&&t<160?-.65:0,t>=100&&t<120?.14:0,t<120||t>=160));
            for(int sub=0;sub<2;sub++){
                var frame=new JsonObject();frame.addProperty("fase",fase);var ossos=new JsonObject();frame.add("ossos",ossos);
                for(String nome:TecidoGojo.OSSOS){var p=m.pose(nome,(sub+1)/2d);var xyz=new JsonArray();xyz.add(p.x());xyz.add(p.y());xyz.add(p.z());ossos.add(nome,xyz);}
                frames.add(frame);
            }
        }
        Files.createDirectories(Path.of("build"));try(var writer=Files.newBufferedWriter(Path.of("build/gojo-cloth-samples.json"))){new Gson().toJson(frames,writer);}
    }
}
