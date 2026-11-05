package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class UlMinyaBeatriceTest {
    static final int F=UlMinyaBeatrice.FIM,K=UlMinyaBeatrice.ESTACAS;
    @Test void chuvaCaiNaAreaDentroDoTempo(){
        for(int k=0;k<K;k++){var a=UlMinyaBeatrice.pontoAcerto(k);
            assertTrue(UlMinyaBeatrice.naArea(a.x(),a.z()),"crava dentro da area");
            assertTrue(UlMinyaBeatrice.sai(k)>=UlMinyaBeatrice.ABRE&&UlMinyaBeatrice.acerto(k)<UlMinyaBeatrice.PRISAO,"toda a chuva antes da prisao");
            var c=UlMinyaBeatrice.centro(k,UlMinyaBeatrice.acerto(k));assertEquals(UlMinyaBeatrice.COMPRIMENTO*.25,c.y(),1e-9);
            assertTrue(UlMinyaBeatrice.direcao(k).y()<-.9,"cai quase na vertical");
            assertEquals(0,UlMinyaBeatrice.escala(k,UlMinyaBeatrice.QUEBRA),1e-9,"quebra tudo junto");}
    }
    @Test void prisaoEQuebra(){
        assertTrue(UlMinyaBeatrice.comprimentoGigante(0,UlMinyaBeatrice.QUEBRA-1)>6.5,"cristal do meio alto");
        assertTrue(UlMinyaBeatrice.RAIO_AREA>=12,"area de ultimate");
        for(int g=0;g<UlMinyaBeatrice.GIGANTES;g++){var b=UlMinyaBeatrice.baseGigante(g);assertTrue(UlMinyaBeatrice.naArea(b.x(),b.z()));assertTrue(UlMinyaBeatrice.comprimentoGigante(g,UlMinyaBeatrice.QUEBRA-1)>2.5,"todos crescidos antes da quebra");}
        for(int g=0;g<UlMinyaBeatrice.GIGANTES;g++){assertEquals(0,UlMinyaBeatrice.comprimentoGigante(g,UlMinyaBeatrice.PRISAO-.5),1e-9);assertTrue(UlMinyaBeatrice.eixoGigante(g).y()>.7);}
        assertTrue(UlMinyaBeatrice.clarao(UlMinyaBeatrice.QUEBRA)>.9);
        for(int k=0;k<K;k++)for(int c=0;c<UlMinyaBeatrice.CACOS;c++)assertEquals(0,UlMinyaBeatrice.tamanhoCaco(k,c,F),1e-9);
        assertEquals(0,UlMinyaBeatrice.alphaCirculo(F),1e-9);
    }
    @Test void animacoes()throws Exception{
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("subaru_ul_minya"));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.25){var m=new LinkedHashMap<String,Object>();
            var ci=new ArrayList<Object>();for(int a=0;a<UlMinyaBeatrice.ANEIS;a++){var l=new ArrayList<Object>();for(int i=0;i<UlMinyaBeatrice.PONTOS_CIRCULO;i++)l.add(UlMinyaBeatrice.circulo(a,i,t));ci.add(l);}
            var rc=new ArrayList<Object>();for(int k=0;k<UlMinyaBeatrice.RAIOS_CIRCULO;k++)rc.add(List.of(UlMinyaBeatrice.raioCirculo(k,0,t),UlMinyaBeatrice.raioCirculo(k,1,t)));
            var zo=new ArrayList<Object>();for(int i=0;i<UlMinyaBeatrice.PONTOS_ZONA;i++)zo.add(UlMinyaBeatrice.zona(i,t));
            var es=new ArrayList<Object>();
            for(int k=0;k<K;k++){var e=new LinkedHashMap<String,Object>();
                e.put("centro",UlMinyaBeatrice.centro(k,t));e.put("dir",UlMinyaBeatrice.direcao(k));e.put("escala",UlMinyaBeatrice.escala(k,t));e.put("rastro",UlMinyaBeatrice.alphaRastro(k,t));
                e.put("acerto",UlMinyaBeatrice.pontoAcerto(k));e.put("impacto",UlMinyaBeatrice.impacto(k,t));
                var sp=new ArrayList<Object>();for(int j=0;j<UlMinyaBeatrice.ESPINHOS;j++)sp.add(List.of(UlMinyaBeatrice.espinho(k,j),UlMinyaBeatrice.tamanhoEspinho(k,j,t)));
                var cc=new ArrayList<Object>();for(int c=0;c<UlMinyaBeatrice.CACOS;c++)cc.add(List.of(UlMinyaBeatrice.caco(k,c,t),UlMinyaBeatrice.tamanhoCaco(k,c,t)));
                e.put("espinhos",sp);e.put("cacos",cc);es.add(e);}
            var gi=new ArrayList<Object>();for(int g=0;g<UlMinyaBeatrice.GIGANTES;g++){var cc=new ArrayList<Object>();
                for(int c=0;c<UlMinyaBeatrice.CACOS_GIGANTE;c++)cc.add(List.of(UlMinyaBeatrice.cacoGigante(g,c,t),UlMinyaBeatrice.tamanhoCacoGigante(g,c,t)));
                gi.add(Map.of("base",UlMinyaBeatrice.baseGigante(g),"eixo",UlMinyaBeatrice.eixoGigante(g),"comprimento",UlMinyaBeatrice.comprimentoGigante(g,t),"raio",UlMinyaBeatrice.raioGigante(g),"cacos",cc));}
            m.put("circulo",ci);m.put("raios",rc);m.put("alphaCirculo",UlMinyaBeatrice.alphaCirculo(t));m.put("zona",zo);m.put("alphaZona",UlMinyaBeatrice.alphaZona(t));
            m.put("estacas",es);m.put("gigantes",gi);m.put("carga",UlMinyaBeatrice.carga(t));m.put("clarao",UlMinyaBeatrice.clarao(t));q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-ul-minya-samples.json"))){new Gson().toJson(Map.of("quadros",q,"fim",F,"abre",UlMinyaBeatrice.ABRE,"prisao",UlMinyaBeatrice.PRISAO,"quebra",UlMinyaBeatrice.QUEBRA,
            "comprimento",UlMinyaBeatrice.COMPRIMENTO,"raio",UlMinyaBeatrice.RAIO,"centroArea",UlMinyaBeatrice.CENTRO_AREA),w);}
    }
}
