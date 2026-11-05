package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MurakBeatriceTest {
    static final int F=MurakBeatrice.FIM;
    @Test void saltoAltoEQuedaLenta(){
        double topo=0;for(double t=0;t<F;t+=.25)topo=Math.max(topo,MurakBeatrice.altura(t));
        assertTrue(topo>=MurakBeatrice.ALTURA_SALTO,"salta 4,5 blocos");
        assertTrue(MurakBeatrice.DESCE>2*MurakBeatrice.SOBE,"desce bem mais devagar do que sobe");
        assertEquals(0,MurakBeatrice.altura(MurakBeatrice.POUSO),1e-9);
        double ultima=MurakBeatrice.altura(MurakBeatrice.POUSO-1);assertTrue(ultima<.05,"pouso macio, sem tranco");
        for(double t=0;t<F;t+=.25)assertTrue(MurakBeatrice.altura(t)>=0);
    }
    @Test void aneisAcompanhamOCorpo(){
        double t=MurakBeatrice.SALTO+MurakBeatrice.SOBE+2;var c=MurakBeatrice.corpo(t);
        for(int a=0;a<MurakBeatrice.ANEIS;a++){var p=MurakBeatrice.anel(a,5,t);
            assertEquals(c.y()+MurakBeatrice.ALTURA_ANEL[a],p.y(),.3,"anel na altura do corpo no ar");
            assertEquals(0,MurakBeatrice.aberturaAnel(a,MurakBeatrice.GOTA),1e-9,"abre so depois da gota");
            assertEquals(0,MurakBeatrice.alphaAnel(a,F),1e-9);}
        var g=MurakBeatrice.gota(MurakBeatrice.PONTOS_GOTA-1,MurakBeatrice.GOTA+1);assertEquals(1.1,g.y(),1e-6,"a gota chega no peito");
        assertEquals(160,MurakBeatrice.DURACAO);
    }
    @Test void animacoes()throws Exception{
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("subaru_murak"));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.25){var m=new LinkedHashMap<String,Object>();
            var an=new ArrayList<Object>();for(int a=0;a<MurakBeatrice.ANEIS;a++){var l=new ArrayList<Object>();for(int i=0;i<MurakBeatrice.PONTOS_ANEL;i++)l.add(MurakBeatrice.anel(a,i,t));
                an.add(Map.of("pontos",l,"alpha",MurakBeatrice.alphaAnel(a,t)));}
            var go=new ArrayList<Object>();for(int i=0;i<MurakBeatrice.PONTOS_GOTA;i++)go.add(MurakBeatrice.gota(i,t));
            var pe=new ArrayList<Object>();for(int k=0;k<MurakBeatrice.PEDRAS;k++)pe.add(List.of(MurakBeatrice.pedra(k,t),MurakBeatrice.tamanhoPedra(k,t)));
            var ra=new ArrayList<Object>();for(int k=1;k<MurakBeatrice.RASTRO;k++)ra.add(List.of(MurakBeatrice.rastro(k,t),MurakBeatrice.alphaRastro(k,t)));
            var on=new ArrayList<Object>();for(int i=0;i<MurakBeatrice.PONTOS_ONDA;i++)on.add(MurakBeatrice.onda(i,t));
            m.put("corpo",MurakBeatrice.corpo(t));m.put("aneis",an);m.put("gota",go);m.put("alphaGota",MurakBeatrice.alphaGota(t));
            m.put("pedras",pe);m.put("rastro",ra);m.put("onda",on);m.put("alphaOnda",MurakBeatrice.alphaOnda(t));q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-murak-samples.json"))){new Gson().toJson(Map.of("quadros",q,"fim",F,"salto",MurakBeatrice.SALTO,"pouso",MurakBeatrice.POUSO,"gota",MurakBeatrice.GOTA,"topo",MurakBeatrice.SALTO+MurakBeatrice.SOBE),w);}
    }
}
