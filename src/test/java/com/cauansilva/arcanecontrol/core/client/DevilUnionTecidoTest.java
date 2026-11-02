package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.client.tecido.DevilUnionTecido;
import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DevilUnionTecidoTest {
    private static final DevilUnionTecido.Entrada PARADO=new DevilUnionTecido.Entrada(0,0,0,0,true),VOO=new DevilUnionTecido.Entrada(0,0,-.05,0,false);
    private static double varredura(DevilUnionTecido m){return -m.pose("du_asa_d",1,1).y();}

    @Test void layoutBateComOModelo()throws Exception{
        // Every simulated bone exists in the generated GeckoLib model, chained to its parent.
        JsonObject geo;try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/geo/armaduras/asta_forma.geo.json")){assertNotNull(in);geo=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}
        var pais=new HashMap<String,String>();
        for(var b:geo.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")){var o=b.getAsJsonObject();pais.put(o.get("name").getAsString(),o.has("parent")?o.get("parent").getAsString():null);}
        for(String osso:DevilUnionTecido.OSSOS)assertTrue(pais.containsKey(osso),osso);
        assertEquals("du_asa_e_2_1",pais.get("du_asa_e_2_2"));assertEquals("du_asa_d",pais.get("du_asa_d_4_0"));
        for(int i=1;i<DevilUnionTecido.CAUDA_SEGMENTOS;i++)assertEquals("du_cauda_"+(i-1),pais.get("du_cauda_"+i),"cauda encadeada");
        assertEquals("armorBody",pais.get("du_cauda_0"));
        assertFalse(pais.keySet().stream().anyMatch(n->n.toLowerCase().contains("cabelo")||n.toLowerCase().contains("hair")),"so chifres, sem cabelo");
    }
    @Test void amostrarNaoAvancaEResetLimpa(){
        var a=new DevilUnionTecido();var b=new DevilUnionTecido();
        for(int t=0;t<80;t++){var e=new DevilUnionTecido.Entrada(.25,.1,-.4,.1,false);a.tick(e);b.tick(e);for(int f=0;f<6;f++){a.pose("du_asa_e_3_2",f/6d,1);a.pose("du_cauda_7",f/6d,1);}}
        assertEquals(a.pose("du_asa_d_4_2",1,1),b.pose("du_asa_d_4_2",1,1));assertEquals(a.pose("du_cauda_6",1,1),b.pose("du_cauda_6",1,1));
        a.tick(new DevilUnionTecido.Entrada(Double.NaN,0,0,0,true));
        for(String osso:DevilUnionTecido.OSSOS){var p=a.pose(osso,1,1);assertEquals(0,p.x(),1e-9,osso);assertEquals(0,p.y(),1e-9,osso);assertEquals(0,p.z(),1e-9,osso);}
        assertEquals(new DevilUnionTecido.Pose(0,0,0),a.pose("desconhecido",1,1));
    }
    @Test void asasBatemMaisRapidoEMaisForteNoAr(){
        var chao=new DevilUnionTecido();var ar=new DevilUnionTecido();double ampChao=0,ampAr=0;int viradasChao=0,viradasAr=0;double antChao=0,antAr=0;
        for(int t=0;t<220;t++){
            chao.tick(PARADO);ar.tick(VOO);double c=varredura(chao),a=varredura(ar);
            if(t>60){ampChao=Math.max(ampChao,Math.abs(c));ampAr=Math.max(ampAr,Math.abs(a));if(Math.signum(c)!=Math.signum(antChao))viradasChao++;if(Math.signum(a)!=Math.signum(antAr))viradasAr++;}
            antChao=c;antAr=a;
        }
        assertTrue(ampChao>.06,"parado ainda bate devagar: "+ampChao);
        assertTrue(ampAr>ampChao*2,"no ar a batida e mais forte");
        assertTrue(viradasAr>viradasChao*2,"no ar a batida e mais rapida: "+viradasAr+" x "+viradasChao);
    }
    @Test void membranaAtrasaComoTecido(){
        // Follow-through: at each end of the stroke the membrane keeps going past the root and whips back, so the
        // hem of an outer strip swings with the root sweep (x on a hanging strip).
        var m=new DevilUnionTecido();double soma=0,normaR=0,normaB=0;double maxPonta=0;
        for(int t=0;t<240;t++){
            m.tick(VOO);double r=varredura(m),dobra=0;
            for(int j=0;j<DevilUnionTecido.ASA_SEGMENTOS;j++)dobra+=m.pose("du_asa_d_6_"+j,1,1).x();
            if(t>40){soma+=r*dobra;normaR+=r*r;normaB+=dobra*dobra;maxPonta=Math.max(maxPonta,Math.abs(m.pose("du_asa_d_6_2",1,1).x())+Math.abs(m.pose("du_asa_d_6_2",1,1).z()));}
        }
        double correlacao=soma/Math.sqrt(normaR*normaB);
        assertTrue(correlacao>.3,"membrana passa do ponto e volta (follow-through): "+correlacao);
        assertTrue(maxPonta>.03&&maxPonta<1.6,"ponta ondula sem quebrar: "+maxPonta);
    }
    @Test void correrDobraAsAsasEEstendeACauda(){
        var m=new DevilUnionTecido();double parado=0,correndo=0,cauda=0;
        for(int t=0;t<88;t++){m.tick(PARADO);if(t>=44)parado+=varredura(m);}
        for(int t=0;t<88;t++){m.tick(new DevilUnionTecido.Entrada(.28,0,0,0,true));if(t>=44){correndo+=varredura(m);}}
        for(int i=0;i<DevilUnionTecido.CAUDA_SEGMENTOS;i++)cauda+=m.pose("du_cauda_"+i,1,1).x();
        assertTrue(correndo<parado-5,"asas dobram para tras na corrida");
        assertTrue(cauda<-.3,"cauda vai para tras na corrida: "+cauda);
        var lat=new DevilUnionTecido();for(int t=0;t<30;t++)lat.tick(new DevilUnionTecido.Entrada(0,.25,0,0,true));
        double roll=0;for(int i=0;i<DevilUnionTecido.CAUDA_SEGMENTOS;i++)roll+=lat.pose("du_cauda_"+i,1,1).z();
        assertTrue(roll<-.2,"passo para a esquerda joga a cauda para a direita: "+roll);
    }
    @Test void asasFechadasNaEntrada(){
        var m=new DevilUnionTecido();m.tick(PARADO);
        var aberta=m.pose("du_asa_d",1,1);var fechada=m.pose("du_asa_d",1,0);
        assertTrue(fechada.y()-aberta.y()>1,"fechada dobra para tras");assertTrue(fechada.z()-aberta.z()>.8,"fechada sobe e fecha");
        var esq=m.pose("du_asa_e",1,0);assertEquals(-fechada.y(),esq.y(),.3);assertEquals(-fechada.z(),esq.z(),.3);
    }
    @Test void limitesFinitosSobEntradaCaotica(){
        var m=new DevilUnionTecido();
        for(int t=0;t<600;t++){
            m.tick(new DevilUnionTecido.Entrada(Math.sin(t)*.5,Math.cos(t*1.3)*.5,t%30<20?-1.5:1,Math.sin(t*.3)*.4,t%50<25));
            for(String osso:DevilUnionTecido.OSSOS){var p=m.pose(osso,.5,1);assertTrue(Double.isFinite(p.x()+p.y()+p.z())&&Math.abs(p.x())<2&&Math.abs(p.y())<2&&Math.abs(p.z())<2,osso+" "+p);}
        }
    }
    @Test void exportaPoses()throws Exception{
        var m=new DevilUnionTecido();var frames=new JsonArray();
        for(int t=0;t<320;t++){
            // Entrance (wings unfold), rest, run, strafe, turn, jump, glide down, land, rest.
            String fase=t<30?"ABERTURA":t<90?"REPOUSO":t<130?"CORRIDA":t<160?"PASSO LATERAL":t<180?"GIRO":t<190?"SALTO":t<240?"VOO":t<250?"POUSO":"RETORNO";
            boolean chao=t<180||t>=240;
            m.tick(new DevilUnionTecido.Entrada(t>=90&&t<130?.25:t>=180&&t<240?.15:0,t>=130&&t<160?.23:0,t>=180&&t<190?.3:t>=190&&t<240?-.12:0,t>=160&&t<180?.14:0,chao));
            double u=Math.min(1,t/30d),abertura=u*u*(3-2*u);
            var f=new JsonObject();f.addProperty("fase",fase);f.addProperty("abertura",abertura);var ossos=new JsonObject();
            for(String osso:DevilUnionTecido.OSSOS){var p=m.pose(osso,1,abertura);var a=new JsonArray();a.add(p.x());a.add(p.y());a.add(p.z());ossos.add(osso,a);}
            f.add("ossos",ossos);frames.add(f);
        }
        Files.createDirectories(Path.of("build"));try(var w=Files.newBufferedWriter(Path.of("build/asta-devil-union-samples.json"))){new Gson().toJson(frames,w);}
    }
}
