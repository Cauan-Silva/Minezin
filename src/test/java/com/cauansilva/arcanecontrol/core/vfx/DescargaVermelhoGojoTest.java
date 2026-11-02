package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class DescargaVermelhoGojoTest {
    @Test void descargaCompletaNoImpactoSemSimularProjetil(){
        assertEquals(13,PerfilVfxGojo.VERMELHO_DISPARO);assertEquals(28,PerfilVfxGojo.duracao(2));
        assertEquals(0,DescargaVermelhoGojo.impacto(12.99));assertEquals(1,DescargaVermelhoGojo.impacto(13));
        assertTrue(DescargaVermelhoGojo.impacto(20)<DescargaVermelhoGojo.impacto(14));
        for(int t=13;t<=28;t++)for(int j=0;j<12;j++){
            assertEquals(22,DescargaVermelhoGojo.descarga(j,12,1,t).frente()+1.8,1e-8);
            var a=DescargaVermelhoGojo.onda(j%3,0,t);var b=DescargaVermelhoGojo.onda(j%3,1,t);
            assertEquals(a.lateral(),b.lateral(),1e-8);assertEquals(a.vertical(),b.vertical(),1e-8);assertEquals(a.frente(),b.frente(),1e-8);
            for(int i=0;i<=24;i++){
                var p=DescargaVermelhoGojo.descarga(j,12,i/24d,t);
                assertTrue(Double.isFinite(p.lateral()+p.vertical()+p.frente()));assertTrue(p.frente()>=0&&p.frente()<=20.2);
            }
        }
    }
    @Test void exportaGeometriaParaPrevia()throws Exception{
        var samples=new LinkedHashMap<String,Object>();
        for(int t=0;t<=28;t++){
            var carga=new ArrayList<Object>();var descarga=new ArrayList<Object>();var ondas=new ArrayList<Object>();
            for(int j=0;j<6;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=32;i++)pontos.add(DescargaVermelhoGojo.carga(j,i/32d,t));carga.add(pontos);}
            for(int j=0;j<12;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=24;i++)pontos.add(DescargaVermelhoGojo.descarga(j,12,i/24d,t));descarga.add(pontos);}
            for(int j=0;j<3;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=64;i++)pontos.add(DescargaVermelhoGojo.onda(j,i/64d,t));ondas.add(pontos);}
            samples.put(Integer.toString(t),Map.of("raio",DescargaVermelhoGojo.raio(t),"alpha",PerfilVfxGojo.opacidade(2,t),"impacto",DescargaVermelhoGojo.impacto(t),"carga",carga,"descarga",descarga,"ondas",ondas));
        }
        Files.writeString(Path.of("build/gojo-vermelho-samples.json"),new Gson().toJson(samples));
    }
}
