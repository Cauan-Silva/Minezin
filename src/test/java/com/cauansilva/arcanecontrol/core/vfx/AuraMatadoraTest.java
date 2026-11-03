package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AuraMatadoraTest {
    @Test void linguasNascemNaBordaDaLaminaEFicamPerto(){
        for(double t=0;t<=AuraMatadora.CICLO*2;t+=.5)for(int k=0;k<AuraMatadora.LINGUAS;k++){
            var b=AuraMatadora.lingua(k,0,t);
            if(AuraMatadora.vida(k,t)<=.55)assertEquals(AuraMatadora.meiaLargura(b.y()),Math.abs(b.x()),1e-9,"lingua "+k);
            assertTrue(b.y()>=AuraMatadora.BASE&&b.y()<=AuraMatadora.PONTA+3);
            for(int i=0;i<=AuraMatadora.PONTOS;i++){var p=AuraMatadora.lingua(k,i/(double)AuraMatadora.PONTOS,t);assertTrue(Math.abs(p.x())<14&&p.y()<52,"fora do alcance");}
        }
    }
    @Test void cicloSeRepeteSemSalto(){
        // A looping aura: same shape one cycle later, so the held sword never pops.
        for(int k=0;k<AuraMatadora.LINGUAS;k++)assertEquals(AuraMatadora.comprimento(k,3),AuraMatadora.comprimento(k,3+AuraMatadora.CICLO),1e-6);
        // Both edges are covered.
        long esquerda=java.util.stream.IntStream.range(0,AuraMatadora.LINGUAS).filter(k->AuraMatadora.lado(k)<0).count();
        assertEquals(AuraMatadora.LINGUAS/2,esquerda);
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(int n=0;n<=AuraMatadora.CICLO*4;n++){
            double t=n/2.0;var l=new ArrayList<Object>();
            for(int k=0;k<AuraMatadora.LINGUAS;k++){var pts=new ArrayList<Object>();for(int i=0;i<=AuraMatadora.PONTOS;i++){double s=i/(double)AuraMatadora.PONTOS;pts.add(List.of(AuraMatadora.lingua(k,s,t),AuraMatadora.largura(k,s,t)));}l.add(pts);}
            q.put(Double.toString(t),l);
        }
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/asta-aura-matadora-samples.json"))){new Gson().toJson(Map.of("quadros",q,"ciclo",AuraMatadora.CICLO),w);}
    }
}
