package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class RecuperacaoSubaruTest {
    static final int L=RecuperacaoSubaru.LIMPA,F=RecuperacaoSubaru.FIM;
    @Test void tempos(){
        assertEquals(40,RecuperacaoSubaru.CURA,"Regeneracao I por 2 s");
        assertEquals(0,RecuperacaoSubaru.alphaToxinas(L+1),1e-9);assertEquals(0,RecuperacaoSubaru.alphaCura(F),1e-9);assertTrue(RecuperacaoSubaru.alphaCura(L+10)>.99);
    }
    @Test void toxinasSaemDoCorpo(){
        for(int k=0;k<RecuperacaoSubaru.TOXINAS;k++){var a=RecuperacaoSubaru.toxina(k,1,RecuperacaoSubaru.INSPIRA);var b=RecuperacaoSubaru.toxina(k,1,L-2);
            assertTrue(Math.hypot(b.x(),b.z())>Math.hypot(a.x(),a.z())+.5,"toxina "+k+" sai para fora");}
    }
    @Test void curaFicaNoCorpo(){
        for(double t=L;t<F;t+=.5){for(int n=0;n<2;n++){double h=RecuperacaoSubaru.alturaAnel(n,t);assertTrue(h>=0&&h<=2.0001);}
            double c=RecuperacaoSubaru.coracao(t);assertTrue(c>.15&&c<.3);
            for(int k=0;k<RecuperacaoSubaru.PARTICULAS;k++){var p=RecuperacaoSubaru.particula(k,t);assertTrue(p.y()>=0&&p.y()<=2.3);}}
    }
    @Test void animacao()throws Exception{
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("subaru_recuperacao"));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.5){var m=new LinkedHashMap<String,Object>();
            var tx=new ArrayList<Object>();for(int k=0;k<RecuperacaoSubaru.TOXINAS;k++){var l=new ArrayList<Object>();for(int u=0;u<RecuperacaoSubaru.PONTOS_TOXINA;u++)l.add(RecuperacaoSubaru.toxina(k,u/(RecuperacaoSubaru.PONTOS_TOXINA-1d),t));tx.add(l);}
            var an=new ArrayList<Object>();for(int n=0;n<2;n++){var l=new ArrayList<Object>();for(int i=0;i<RecuperacaoSubaru.PONTOS_ANEL;i++)l.add(RecuperacaoSubaru.anel(n,i,t));an.add(List.of(l,RecuperacaoSubaru.alphaAnel(n,t)));}
            var pt=new ArrayList<Object>();for(int k=0;k<RecuperacaoSubaru.PARTICULAS;k++)pt.add(List.of(RecuperacaoSubaru.particula(k,t),RecuperacaoSubaru.tamanhoParticula(k,t)));
            var fu=new ArrayList<Object>();for(int k=0;k<RecuperacaoSubaru.TOXINAS;k++)for(int n=0;n<RecuperacaoSubaru.FUMACAS;n++)fu.add(List.of(RecuperacaoSubaru.fumaca(k,n,t),RecuperacaoSubaru.tamanhoFumaca(k,n,t),RecuperacaoSubaru.densidadeFumaca(k,n,t),k%3==0?1:0));
            m.put("fumacas",fu);m.put("toxinas",tx);m.put("alphaToxinas",RecuperacaoSubaru.alphaToxinas(t));m.put("raioPulso",RecuperacaoSubaru.raioPulso(t));m.put("alphaPulso",RecuperacaoSubaru.alphaPulso(t));
            m.put("alphaCura",RecuperacaoSubaru.alphaCura(t));m.put("coracao",RecuperacaoSubaru.coracao(t));m.put("aneis",an);m.put("particulas",pt);q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-recuperacao-samples.json"))){new Gson().toJson(Map.of("quadros",q,"inspira",RecuperacaoSubaru.INSPIRA,"limpa",L,"fim",F,"peito",RecuperacaoSubaru.PEITO),w);}
    }
}
