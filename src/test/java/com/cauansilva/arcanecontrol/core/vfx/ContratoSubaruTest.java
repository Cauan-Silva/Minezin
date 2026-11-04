package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ContratoSubaruTest {
    static final int F=ContratoSubaru.FIM;
    static double d(ContratoSubaru.P a,ContratoSubaru.P b){return Math.sqrt(Math.pow(a.x()-b.x(),2)+Math.pow(a.y()-b.y(),2)+Math.pow(a.z()-b.z(),2));}
    @Test void sequencia(){
        assertTrue(0<ContratoSubaru.PORTA&&ContratoSubaru.PORTA<ContratoSubaru.ABRE&&ContratoSubaru.ABRE<ContratoSubaru.SAI&&ContratoSubaru.SAI<ContratoSubaru.FECHA&&ContratoSubaru.FECHA<F);
        assertEquals(1,ContratoSubaru.ergue(ContratoSubaru.PORTA),1e-9);assertEquals(0,ContratoSubaru.abertura(ContratoSubaru.PORTA),1e-9);assertEquals(1,ContratoSubaru.abertura(ContratoSubaru.ABRE),1e-9);
        assertEquals(0,ContratoSubaru.alphaPorta(ContratoSubaru.SAI+10),1e-9,"a porta some");
        assertEquals(0,ContratoSubaru.escalaBeatrice(ContratoSubaru.PORTA),1e-9);assertEquals(1,ContratoSubaru.escalaBeatrice(ContratoSubaru.ABRE+4),1e-9);
        assertTrue(d(ContratoSubaru.beatrice(ContratoSubaru.SAI),ContratoSubaru.LUGAR)<.05,"para ao lado do ombro");
        assertTrue(d(ContratoSubaru.fio(1,ContratoSubaru.FECHA),new ContratoSubaru.P(ContratoSubaru.beatrice(ContratoSubaru.FECHA).x()-.25,ContratoSubaru.beatrice(ContratoSubaru.FECHA).y()+.75,ContratoSubaru.beatrice(ContratoSubaru.FECHA).z()))<.05,"fio chega na mao dela");
        assertTrue(ContratoSubaru.clarao(ContratoSubaru.FECHA+4)>.99);assertTrue(ContratoSubaru.aura(F)>0,"a forma continua");
    }
    @Test void animacao()throws Exception{
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("subaru_contrato"));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.25){var m=new LinkedHashMap<String,Object>();
            var c=new ArrayList<Object>();for(int a=0;a<ContratoSubaru.ANEIS;a++){var l=new ArrayList<Object>();for(int i=0;i<ContratoSubaru.PONTOS_CIRCULO;i++)l.add(ContratoSubaru.circulo(a,i,t));c.add(l);}
            var e=new ArrayList<Object>();for(int k=0;k<ContratoSubaru.ESTRELAS;k++)e.add(ContratoSubaru.estrela(k,t));
            var r=new ArrayList<Object>();for(int k=0;k<ContratoSubaru.RUNAS;k++)r.add(List.of(ContratoSubaru.runa(k,t),ContratoSubaru.acendeRuna(k,t)));
            var fi=new ArrayList<Object>();for(int u=0;u<ContratoSubaru.PONTOS_FIO;u++)fi.add(ContratoSubaru.fio(u/(ContratoSubaru.PONTOS_FIO-1d)*ContratoSubaru.alcanceFio(t),t));
            m.put("circulo",c);m.put("estrela",e);m.put("runas",r);m.put("alphaCirculo",ContratoSubaru.alphaCirculo(t));m.put("clarao",ContratoSubaru.clarao(t));
            m.put("ergue",ContratoSubaru.ergue(t));m.put("abertura",ContratoSubaru.abertura(t));m.put("luzPorta",ContratoSubaru.luzPorta(t));m.put("alphaPorta",ContratoSubaru.alphaPorta(t));
            m.put("beatrice",ContratoSubaru.beatrice(t));m.put("escala",ContratoSubaru.escalaBeatrice(t));m.put("giro",ContratoSubaru.giroBeatrice(t));m.put("inclina",ContratoSubaru.inclinaBeatrice(t));
            m.put("fio",fi);m.put("alphaFio",ContratoSubaru.alphaFio(t));m.put("aura",ContratoSubaru.aura(t));q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-contrato-samples.json"))){new Gson().toJson(Map.of("quadros",q,"fim",F,"porta",ContratoSubaru.PORTA_POS,"fases",List.of(ContratoSubaru.PORTA,ContratoSubaru.ABRE,ContratoSubaru.SAI,ContratoSubaru.FECHA)),w);}
    }
}
