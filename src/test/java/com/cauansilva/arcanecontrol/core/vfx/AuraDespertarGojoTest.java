package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AuraDespertarGojoTest {
    @Test void observadorTardioNaoRepeteEntradaEFormaEncerra(){
        assertEquals(0,AuraDespertarGojo.idade(400,0));assertEquals(225,AuraDespertarGojo.idade(180,5));
        assertTrue(AuraDespertarGojo.surto(AuraDespertarGojo.idade(180,5))<.001);
        assertEquals(0,AuraDespertarGojo.intensidade(0));assertEquals(0,AuraDespertarGojo.intensidade(400));
        assertEquals(0,AuraDespertarGojo.alphaOnda(56));assertEquals(0,AuraDespertarGojo.raioOnda(56));
    }
    @Test void geometriaPermaneceFinitaEProximaAoCorpo(){
        for(int t=0;t<=400;t++)for(int j=0;j<24;j++){
            double intensidade=AuraDespertarGojo.intensidade(t);assertTrue(intensidade>=0&&intensidade<=1);
            var mote=AuraDespertarGojo.particula(j,t);assertTrue(Double.isFinite(mote.altura()));assertTrue(Math.hypot(mote.lateral(),mote.frente())<1.5);
            for(int i=0;i<=24;i++){
                double u=i/24d;var p=AuraDespertarGojo.fio(j,u,t);
                assertTrue(Double.isFinite(p.altura()));assertTrue(Math.hypot(p.lateral(),p.frente())<1.4);
                assertTrue(p.altura()>=0&&p.altura()<2.6);double luz=AuraDespertarGojo.pulso(j,u,t);assertTrue(luz>=0&&luz<=1);
            }
        }
    }
    @Test void exportaPontosDaAuraParaPrevia()throws Exception{
        var samples=new LinkedHashMap<String,Object>();
        for(int t=0;t<=400;t+=2){
            var fios=new ArrayList<Object>();var pulsos=new ArrayList<Object>();var particulas=new ArrayList<Object>();var brilhos=new ArrayList<Double>();
            for(int j=0;j<12;j++){
                var pontos=new ArrayList<Object>();var luz=new ArrayList<Double>();
                for(int i=0;i<=24;i++){pontos.add(AuraDespertarGojo.fio(j,i/24d,t));luz.add(AuraDespertarGojo.pulso(j,i/24d,t));}
                fios.add(pontos);pulsos.add(luz);
            }
            for(int j=0;j<24;j++){particulas.add(AuraDespertarGojo.particula(j,t));brilhos.add(AuraDespertarGojo.brilhoParticula(j,t));}
            samples.put(Integer.toString(t),Map.of("intensidade",AuraDespertarGojo.intensidade(t),"fios",fios,"pulsos",pulsos,"particulas",particulas,"brilhos",brilhos,"raioOnda",AuraDespertarGojo.raioOnda(t),"alphaOnda",AuraDespertarGojo.alphaOnda(t)));
        }
        Files.writeString(Path.of("build/gojo-despertar-samples.json"),new Gson().toJson(samples));
    }
}
