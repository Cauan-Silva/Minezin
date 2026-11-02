package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.client.tecido.TecidoFrieren;
import com.google.gson.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TecidoFrierenTest {
    private static final TecidoFrieren.Entrada PARADO=new TecidoFrieren.Entrada(0,0,0,0,true);
    @Test void geometriaTemOsOssosEncadeadosDoTecido()throws Exception{
        var geo=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/arcanecontrol/geo/armaduras/frieren.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var ossos=new java.util.HashMap<String,JsonObject>();
        for(var v:geo.getAsJsonArray("bones"))ossos.put(v.getAsJsonObject().get("name").getAsString(),v.getAsJsonObject());
        assertEquals(54,TecidoFrieren.OSSOS.size());
        for(String nome:TecidoFrieren.OSSOS){
            assertTrue(ossos.containsKey(nome),nome);String parent=ossos.get(nome).get("parent").getAsString();
            assertTrue(parent.equals("armorBody")||TecidoFrieren.OSSOS.contains(parent),nome);
        }
        // A saia saiu das pernas: nenhum osso de tecido pertence a armorLeftLeg/armorRightLeg.
        assertFalse(ossos.containsKey("saia_esq")||ossos.containsKey("saia_dir")||ossos.containsKey("manto_esq"));
    }
    @Test void amostrarNaoAvancaFisicaEResetRemoveInercia(){
        var a=new TecidoFrieren();var b=new TecidoFrieren();
        for(int t=0;t<100;t++){
            var e=new TecidoFrieren.Entrada(.22,.12,-.5,.1,false);a.tick(e);b.tick(e);
            for(String osso:TecidoFrieren.OSSOS)for(int f=0;f<8;f++)a.pose(osso,f/8d);
        }
        for(String osso:TecidoFrieren.OSSOS)assertEquals(a.pose(osso,1),b.pose(osso,1));
        a.tick(new TecidoFrieren.Entrada(Double.NaN,0,0,0,true));
        for(String osso:TecidoFrieren.OSSOS){var p=a.pose(osso,1);assertEquals(0,p.x(),0.);assertEquals(0,p.y(),0.);assertEquals(0,p.z(),0.);}
    }
    @Test void movimentoBruscoFicaDentroDosLimitesERetornaAoRepouso(){
        var m=new TecidoFrieren();
        for(int t=0;t<500;t++){
            m.tick(new TecidoFrieren.Entrada(Math.sin(t)*.3,Math.cos(t)*.3,t%30<20?-.8:.4,Math.sin(t*.3)*.3,false));
            for(String osso:TecidoFrieren.OSSOS){
                var p=m.pose(osso,.5);assertTrue(Double.isFinite(p.x()+p.y()+p.z()));
                assertTrue(Math.abs(p.x())<=.45&&Math.abs(p.y())<=.45&&Math.abs(p.z())<=.45,osso);
            }
            assertTrue(m.erroAlongamento()<.06);
        }
        for(int t=0;t<400;t++)m.tick(PARADO);
        for(String osso:TecidoFrieren.OSSOS){var p=m.pose(osso,1);assertTrue(Math.abs(p.x())<.05&&Math.abs(p.z())<.05,osso);}
    }
    @Test void saiaRespondeMaisQueACapinhaEAoSentidoDoPasso(){
        var frente=new TecidoFrieren();var tras=new TecidoFrieren();
        for(int t=0;t<40;t++){frente.tick(new TecidoFrieren.Entrada(.25,0,0,0,true));tras.tick(new TecidoFrieren.Entrada(-.25,0,0,0,true));}
        assertTrue(Math.abs(frente.pose("frieren_saia_back_1_0",1).x())>Math.abs(tras.pose("frieren_saia_back_1_0",1).x())+.005);
        assertTrue(Math.abs(tras.pose("frieren_saia_front_1_0",1).x())>Math.abs(frente.pose("frieren_saia_front_1_0",1).x())+.005);
        assertTrue(Math.abs(frente.pose("frieren_saia_back_1_0",1).x())>Math.abs(frente.pose("frieren_capa_back_1_0",1).x()),"saia mais solta que a capinha presa nos ombros");
    }
    @Test void exportaFisicaRealParaRevisaoVisual()throws Exception{
        var m=new TecidoFrieren();var frames=new JsonArray();
        for(int t=0;t<240;t++){
            String fase=t<30?"REPOUSO":t<70?"CORRIDA":t<100?"PASSO LATERAL":t<120?"GIRO":t<130?"SALTO":t<160?"QUEDA":t<170?"POUSO":"RETORNO AMORTECIDO";
            m.tick(new TecidoFrieren.Entrada(t>=30&&t<70?.25:0,t>=70&&t<100?.23:0,t>=120&&t<130?.3:t>=130&&t<160?-.65:0,t>=100&&t<120?.14:0,t<120||t>=160));
            for(int sub=0;sub<2;sub++){
                var frame=new JsonObject();frame.addProperty("fase",fase);var ossos=new JsonObject();frame.add("ossos",ossos);
                for(String nome:TecidoFrieren.OSSOS){var p=m.pose(nome,(sub+1)/2d);var xyz=new JsonArray();xyz.add(p.x());xyz.add(p.y());xyz.add(p.z());ossos.add(nome,xyz);}
                frames.add(frame);
            }
        }
        Files.createDirectories(Path.of("build"));try(var writer=Files.newBufferedWriter(Path.of("build/frieren-cloth-samples.json"))){new Gson().toJson(frames,writer);}
    }
}
