package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
class CoroaRoxoGojoTest {
    @Test void coroasSaoFinitasFechadasENaoAlteramVelocidadeDoProjetil()throws Exception{
        var samples=new LinkedHashMap<String,Object>();
        for(int t=0;t<=94;t++){
            double r=t<=30?CoroaRoxoGojo.formacao(t)*1.6:3.6;var faixas=new ArrayList<Object>();
            for(int f=0;f<3;f++){
                var pontos=new ArrayList<CoroaRoxoGojo.Ponto>();
                for(int i=0;i<=48;i++){var p=CoroaRoxoGojo.ponto(f,i/48d,t,r);assertTrue(Double.isFinite(p.lateral()+p.vertical()+p.frente()));assertTrue(Math.sqrt(p.lateral()*p.lateral()+p.vertical()*p.vertical()+p.frente()*p.frente())<=r*1.12+1e-8);pontos.add(p);}
                var a=pontos.get(0);var b=pontos.get(48);assertEquals(a.lateral(),b.lateral(),1e-8);assertEquals(a.vertical(),b.vertical(),1e-8);faixas.add(pontos);
            }
            var raios=new ArrayList<Object>();for(int f=0;f<8;f++){var pontos=new ArrayList<Object>();for(int i=0;i<=8;i++)pontos.add(RaiosRoxo.ponto(f,i,t));raios.add(pontos);}
            var pontes=new ArrayList<Object>();for(int f=0;f<4;f++){var pontos=new ArrayList<Object>();for(int i=0;i<=20;i++)pontos.add(CoroaRoxoGojo.ponte(f,i/20d,t));pontes.add(pontos);}
            var rastros=new ArrayList<Object>();for(int f=0;f<8;f++){var pontos=new ArrayList<Object>();for(int i=0;i<=24;i++)pontos.add(CoroaRoxoGojo.rastro(f,8,i/24d,t));rastros.add(pontos);}
            final int tickExport=t;
            samples.put(Integer.toString(t),Map.of("ramos",java.util.stream.IntStream.range(0,8).mapToObj(f->List.of(RaiosRoxo.ramo(f,3,tickExport),RaiosRoxo.ramo(f,5,tickExport))).toList(),"distancia",PerfilVfxGojo.distanciaRoxo(t),"pontes",pontes,"rastros",rastros,"coroas",faixas,"raios",raios,"raio",r,"separacao",PerfilVfxGojo.separacaoRoxo(t),"alpha",PerfilVfxGojo.opacidade(4,t)));
        }
        assertEquals(30,PerfilVfxGojo.ROXO_DISPARO);assertEquals(1.6,PerfilVfxGojo.ROXO_VELOCIDADE);assertEquals(0,CoroaRoxoGojo.formacao(21));assertEquals(1,CoroaRoxoGojo.formacao(30));
        Files.writeString(Path.of("build/gojo-roxo-samples.json"),new Gson().toJson(samples));
    }
}
