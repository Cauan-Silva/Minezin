package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MinyaBeatriceTest {
    static final int F=MinyaBeatrice.fim(),K=MinyaBeatrice.ESTACAS;
    static double d(MinyaBeatrice.P a,MinyaBeatrice.P b){return Math.sqrt(Math.pow(a.x()-b.x(),2)+Math.pow(a.y()-b.y(),2)+Math.pow(a.z()-b.z(),2));}
    @Test void disparaEmOrdemECravaNoLeque(){
        var vistos=new HashSet<Integer>();for(int k=0;k<K;k++){assertTrue(vistos.add(MinyaBeatrice.disparo(k)),"um disparo por tick");assertTrue(MinyaBeatrice.disparo(k)>=MinyaBeatrice.DISPARO);}
        assertEquals(MinyaBeatrice.DISPARO,MinyaBeatrice.disparo(K/2),"a do meio sai primeiro");
        for(int k=0;k<K;k++){var a=MinyaBeatrice.pontoAcerto(k);assertEquals(0,a.y(),1e-9);
            assertTrue(d(MinyaBeatrice.centro(k,MinyaBeatrice.acerto(k)),new MinyaBeatrice.P(a.x(),MinyaBeatrice.COMPRIMENTO*.25,a.z()))<1e-6,"crava no ponto");
            var dir=MinyaBeatrice.direcao(k,MinyaBeatrice.DISPARO);assertTrue(dir.z()>.5,"aponta para a frente ao disparar");
            assertEquals(0,MinyaBeatrice.escala(k,MinyaBeatrice.acerto(k)+MinyaBeatrice.CRISTAL),1e-9);}
        assertEquals(0,MinyaBeatrice.alphaCirculo(F),1e-9);
    }
    @Test void cristalECacos(){
        for(int k=0;k<K;k++){int a=MinyaBeatrice.acerto(k);assertEquals(0,MinyaBeatrice.tamanhoEspinho(k,0,a-1),1e-9);assertTrue(MinyaBeatrice.tamanhoEspinho(k,0,a+6)>.5);
            for(int j=0;j<MinyaBeatrice.ESPINHOS;j++)assertTrue(MinyaBeatrice.espinho(k,j).y()>.4,"espinho para cima");
            for(int c=0;c<MinyaBeatrice.CACOS;c++){assertEquals(0,MinyaBeatrice.tamanhoCaco(k,c,F),1e-9);assertTrue(MinyaBeatrice.caco(k,c,a+MinyaBeatrice.CRISTAL+5).y()>0);}}
    }
    @Test void animacoes()throws Exception{
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("subaru_minya"));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.25){var m=new LinkedHashMap<String,Object>();var es=new ArrayList<Object>();
            for(int k=0;k<K;k++){var e=new LinkedHashMap<String,Object>();
                e.put("centro",MinyaBeatrice.centro(k,t));e.put("dir",MinyaBeatrice.direcao(k,t));e.put("escala",MinyaBeatrice.escala(k,t));e.put("rastro",MinyaBeatrice.alphaRastro(k,t));
                e.put("acerto",MinyaBeatrice.pontoAcerto(k));e.put("impacto",MinyaBeatrice.impacto(k,t));
                var sp=new ArrayList<Object>();for(int j=0;j<MinyaBeatrice.ESPINHOS;j++)sp.add(List.of(MinyaBeatrice.espinho(k,j),MinyaBeatrice.tamanhoEspinho(k,j,t)));
                var cc=new ArrayList<Object>();for(int c=0;c<MinyaBeatrice.CACOS;c++)cc.add(List.of(MinyaBeatrice.caco(k,c,t),MinyaBeatrice.tamanhoCaco(k,c,t)));
                e.put("espinhos",sp);e.put("cacos",cc);es.add(e);}
            var ci=new ArrayList<Object>();for(int a=0;a<2;a++){var l=new ArrayList<Object>();for(int i=0;i<MinyaBeatrice.PONTOS_CIRCULO;i++)l.add(MinyaBeatrice.circulo(a,i,t));ci.add(l);}
            m.put("estacas",es);m.put("circulo",ci);m.put("alphaCirculo",MinyaBeatrice.alphaCirculo(t));q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-minya-samples.json"))){new Gson().toJson(Map.of("quadros",q,"fim",F,"disparo",MinyaBeatrice.DISPARO,"forma",MinyaBeatrice.FORMA,"comprimento",MinyaBeatrice.COMPRIMENTO,"raio",MinyaBeatrice.RAIO),w);}
    }
}
