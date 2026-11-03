package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.asta.RoteiroTecidoAsta;
import com.cauansilva.arcanecontrol.core.cid.MolaMantoCid;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoAsta;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TecidoAstaTest {
    private static final MolaMantoCid.Entrada PARADO=new MolaMantoCid.Entrada(0,0,0,0,true);
    @Test void ossosDaCapaExistemEmCadeiaNoModelo()throws Exception{
        var geo=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/arcanecontrol/geo/armaduras/asta.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var ossos=new HashMap<String,JsonObject>();for(var v:geo.getAsJsonArray("bones"))ossos.put(v.getAsJsonObject().get("name").getAsString(),v.getAsJsonObject());
        assertEquals(14,TecidoAsta.OSSOS.size());
        for(String nome:TecidoAsta.OSSOS){
            assertTrue(ossos.containsKey(nome),nome);String pai=ossos.get(nome).get("parent").getAsString();
            assertEquals(nome.endsWith("_0")?"armorBody":nome.substring(0,nome.length()-1)+"0",pai,nome);
        }
        // The rigid robe pieces are gone; only the shoulder mantles stay rigid.
        assertFalse(ossos.containsKey("capa_touro_esq")||ossos.containsKey("manto_frente_esq")||ossos.containsKey("emblema_touro"));
        assertTrue(ossos.containsKey("manto_ombro_esq")&&ossos.containsKey("manto_ombro_dir"));
    }
    @Test void amostrarNaoAvancaFisicaEResetRemoveInercia(){
        var a=new TecidoAsta();var b=new TecidoAsta();
        for(int t=0;t<100;t++){var e=new MolaMantoCid.Entrada(.22,.12,-.5,.1,false);a.tick(e);b.tick(e);for(String o:TecidoAsta.OSSOS)for(int f=0;f<8;f++)a.pose(o,f/8d);}
        for(String o:TecidoAsta.OSSOS)assertEquals(a.pose(o,1),b.pose(o,1));
        a.tick(new MolaMantoCid.Entrada(Double.NaN,0,0,0,true));
        for(String o:TecidoAsta.OSSOS){var p=a.pose(o,1);assertEquals(0,p.x(),0.);assertEquals(0,p.y(),0.);assertEquals(0,p.z(),0.);}
    }
    @Test void movimentoCaoticoFicaEstavelELimitado(){
        var m=new TecidoAsta();
        for(int t=0;t<500;t++){
            m.tick(new MolaMantoCid.Entrada(Math.sin(t)*.3,Math.cos(t)*.3,t%30<20?-.8:.4,Math.sin(t*.3)*.3,false));
            for(String o:TecidoAsta.OSSOS){var p=m.pose(o,.5);assertTrue(Double.isFinite(p.x()+p.y()+p.z()),o);assertTrue(Math.abs(p.x())<=1.6&&Math.abs(p.y())<=1.6&&Math.abs(p.z())<=1.6,o);}
            assertTrue(m.erroAlongamento()<.06);
        }
        for(int t=0;t<300;t++)m.tick(PARADO);
        // Back at rest only the idle breeze is left: a small flutter, never a held swing.
        for(String o:TecidoAsta.OSSOS)if(o.contains("frente")){var p=m.pose(o,1);assertTrue(Math.abs(p.x())<.08,o);}
    }
    @Test void correrLevantaACapaDeTrasEAsAbasReagemAParada(){
        var corre=new TecidoAsta();var parado=new TecidoAsta();
        for(int t=0;t<40;t++){corre.tick(new MolaMantoCid.Entrada(.28,0,0,0,true));parado.tick(PARADO);}
        assertTrue(Math.abs(corre.pose("asta_capa_costas_1_0",1).x())>Math.abs(parado.pose("asta_capa_costas_1_0",1).x())+.02,"capa de tras sobe na corrida");
        double durante=corre.pose("asta_capa_frente_esq_0_0",1).x();
        for(int t=0;t<3;t++)corre.tick(PARADO);
        assertNotEquals(durante,corre.pose("asta_capa_frente_esq_0_0",1).x(),1e-4);
        // Hem bends relative to the top segment: cloth, not a rigid board.
        var g=new TecidoAsta();for(int t=0;t<30;t++)g.tick(new MolaMantoCid.Entrada(.28,.1,0,.08,true));
        assertTrue(Math.abs(g.pose("asta_capa_frente_dir_0_1",1).x())>1e-4);
    }
    @Test void ventoMexeACapaParadaSemRepetir(){
        var m=new TecidoAsta();double min=9,max=-9;
        for(int t=0;t<400;t++){m.tick(PARADO);if(t>40){double x=m.pose("asta_capa_costas_1_0",1).x();min=Math.min(min,x);max=Math.max(max,x);}}
        assertTrue(max-min>.01,"a brisa balanca a capa parada");
        assertTrue(Math.abs(TecidoAsta.vento(10)[0]-TecidoAsta.vento(10+2*Math.PI/.09)[0])>1e-3,"as ondas nao fecham um ciclo curto");
    }
    @Test void golpesBaseEmpurramACapaNoTempoDoGolpe(){
        assertEquals(0,RoteiroTecidoAsta.amostrar(301,30).frente(),1e-12);
        assertTrue(RoteiroTecidoAsta.amostrar(301,8).frente()>.2,"X chicoteia no acerto (tick 8)");
        assertTrue(RoteiroTecidoAsta.amostrar(300,6).giro()>.15,"Z gira a capa no acerto (tick 6)");
        assertTrue(RoteiroTecidoAsta.amostrar(302,6).frente()>RoteiroTecidoAsta.amostrar(302,16).frente());
        assertEquals(0,RoteiroTecidoAsta.amostrar(304,6).frente(),1e-12);// the form hides the outfit
        var x=new TecidoAsta();var parado=new TecidoAsta();
        for(int t=0;t<12;t++){var i=RoteiroTecidoAsta.amostrar(301,t);x.tick(new MolaMantoCid.Entrada(i.frente(),i.lado(),0,i.giro(),true));parado.tick(PARADO);}
        assertTrue(Math.abs(x.pose("asta_capa_costas_1_0",1).x())>Math.abs(parado.pose("asta_capa_costas_1_0",1).x())+.02);
    }
    @Test void exportaFisicaParaPrevia()throws Exception{
        var m=new TecidoAsta();var frames=new JsonArray();
        for(int t=0;t<400;t++){
            String fase=t<30?"REPOUSO":t<80?"CORRIDA":t<110?"PARADA BRUSCA":t<140?"PASSO LATERAL":t<160?"GIRO":t<170?"SALTO":t<195?"QUEDA":t<205?"POUSO":t<230?"VENTO PARADO":t<255?"GOLPE Z":t<280?"GOLPE X":t<300?"INVESTIDA C":t<330?"GUARDA V":t<350?"ATAQUES DE ESPADA":t<380?"AGACHADO":"RETORNO";
            double f=t>=30&&t<80?.28:0,l=t>=110&&t<140?.23:0,v=t>=160&&t<170?.32:t>=170&&t<195?-.6:0,g=t>=140&&t<160?.16:0;
            // Same inputs TecidoAstaCliente adds: strike pushes, sword swing flicks (every 6 ticks) and crouching.
            int[][] golpes={{300,230},{301,255},{302,280},{303,300}};
            for(var gp:golpes){var i=RoteiroTecidoAsta.amostrar(gp[0],t-gp[1]);if(t>=gp[1]&&t<gp[1]+30){f+=i.frente();l+=i.lado();g+=i.giro();}}
            if(t>=330&&t<350)g+=Math.sin(Math.PI*((t-330)%6)/6.0)*.12;
            if(t>=350&&t<380)f+=.07;
            m.tick(new MolaMantoCid.Entrada(f,l,v,g,t<160||t>=195));
            var fr=new JsonObject();fr.addProperty("fase",fase);fr.addProperty("frente",f);var ossos=new JsonObject();
            for(String o:TecidoAsta.OSSOS){var p=m.pose(o,1);var a=new JsonArray();a.add(p.x());a.add(p.y());a.add(p.z());ossos.add(o,a);}
            fr.add("ossos",ossos);frames.add(fr);
        }
        Files.createDirectories(Path.of("build"));Files.writeString(Path.of("build/asta-capa-samples.json"),frames.toString());
    }
}
