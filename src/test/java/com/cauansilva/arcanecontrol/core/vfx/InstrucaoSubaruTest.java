package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class InstrucaoSubaruTest {
    static final double[] ALIADOS={0,3,5.5};
    static final int F=70;
    @Test void pulsoAlcancaOsAliadosEmOrdem(){
        assertEquals(0,InstrucaoSubaru.raioPulso(InstrucaoSubaru.ORDEM),1e-9);assertEquals(InstrucaoSubaru.RAIO,InstrucaoSubaru.raioPulso(InstrucaoSubaru.ORDEM+InstrucaoSubaru.PULSO),1e-6);
        double ant=-1;for(double d:ALIADOS){double c=InstrucaoSubaru.chegada(d);assertTrue(c>ant);ant=c;assertEquals(d,InstrucaoSubaru.raioPulso(c),.01,"pulso no aliado quando acende");}
        assertEquals(0,InstrucaoSubaru.alphaPulso(InstrucaoSubaru.ORDEM+InstrucaoSubaru.PULSO+7),1e-9);
    }
    @Test void buffDuraCincoSegundos(){
        double c=InstrucaoSubaru.chegada(3);assertEquals(0,InstrucaoSubaru.alphaBuff(c-.1,c),1e-9);assertTrue(InstrucaoSubaru.alphaBuff(c+20,c)>.99);
        assertEquals(0,InstrucaoSubaru.alphaBuff(c+InstrucaoSubaru.DURACAO,c),1e-9);
        for(int n=0;n<InstrucaoSubaru.DIVISAS;n++)for(double t=0;t<100;t+=.5){double h=InstrucaoSubaru.alturaDivisa(n,t);assertTrue(h>=2.15&&h<=2.75);}
        for(int k=0;k<InstrucaoSubaru.ESPIRAIS;k++)assertTrue(InstrucaoSubaru.espiral(k,1,10).y()<2);
    }
    @Test void animacao()throws Exception{
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("subaru_instrucao"));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.5){var m=new LinkedHashMap<String,Object>();
            var an=new ArrayList<Object>();for(int i=0;i<InstrucaoSubaru.PONTOS_ANEL;i++)an.add(InstrucaoSubaru.anelPulso(i,t));
            var ra=new ArrayList<Object>();for(int k=0;k<InstrucaoSubaru.RAIOS;k++)ra.add(List.of(InstrucaoSubaru.raio(k,0,t),InstrucaoSubaru.raio(k,1,t)));
            var al=new ArrayList<Object>();
            for(double d:ALIADOS){double c=InstrucaoSubaru.chegada(d);var a=new LinkedHashMap<String,Object>();
                var pe=new ArrayList<Object>();for(int i=0;i<InstrucaoSubaru.PONTOS_PE;i++)pe.add(InstrucaoSubaru.pe(i,t,c));
                var es=new ArrayList<Object>();for(int k=0;k<InstrucaoSubaru.ESPIRAIS;k++){var l=new ArrayList<Object>();for(int u=0;u<InstrucaoSubaru.PONTOS_ESPIRAL;u++)l.add(InstrucaoSubaru.espiral(k,u/(InstrucaoSubaru.PONTOS_ESPIRAL-1d),t));es.add(List.of(l,InstrucaoSubaru.frenteEspiral(k,t)));}
                var dv=new ArrayList<Object>();for(int n=0;n<InstrucaoSubaru.DIVISAS;n++)dv.add(List.of(InstrucaoSubaru.alturaDivisa(n,t),InstrucaoSubaru.alphaDivisa(n,t)));
                a.put("pe",pe);a.put("espirais",es);a.put("divisas",dv);a.put("alpha",InstrucaoSubaru.alphaBuff(t,c));a.put("acende",InstrucaoSubaru.acende(t,c));al.add(a);}
            m.put("anel",an);m.put("raios",ra);m.put("alphaPulso",InstrucaoSubaru.alphaPulso(t));m.put("aliados",al);q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-instrucao-samples.json"))){new Gson().toJson(Map.of("quadros",q,"ordem",InstrucaoSubaru.ORDEM,"fim",F,"distancias",ALIADOS),w);}
    }
}
