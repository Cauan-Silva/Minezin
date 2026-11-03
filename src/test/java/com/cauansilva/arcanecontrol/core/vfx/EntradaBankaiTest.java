package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class EntradaBankaiTest {
    private static final int G=EntradaBankai.GRITO,R=EntradaBankai.REVELA,F=EntradaBankai.FIM;
    @Test void fasesEmOrdemETrocaDeRoupaNaRevelacao(){
        assertTrue(G<R&&R<F);assertFalse(EntradaBankai.trajeVisivel(R-1));assertTrue(EntradaBankai.trajeVisivel(R));
        assertEquals(0,EntradaBankai.alturaPilar(G),1e-9);assertEquals(24,EntradaBankai.alturaPilar(G+4),1e-9);
        assertEquals(0,EntradaBankai.alphaPilar(R+4),1e-9);assertEquals(0,EntradaBankai.alphaChoque(G+12),1e-9);
        for(int k=0;k<EntradaBankai.NUVENS;k++){assertEquals(0,EntradaBankai.alphaNuvem(k,F),1e-9);assertEquals(0,EntradaBankai.raioNuvem(k,G-1),1e-9);}
        assertEquals(0,EntradaBankai.pulso(0),1e-9);assertEquals(90,EntradaBankai.pulso(10),1e-9);assertEquals(0,EntradaBankai.pulso(R-2),1e-9);
        assertEquals(0,EntradaBankai.alphaCorte(R-2),1e-9);assertEquals(Math.toRadians(100),EntradaBankai.pontaCorte(R+2),1e-9);
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.5){var m=new LinkedHashMap<String,Object>();
            var li=new ArrayList<Object>();var ch=new ArrayList<Object>();
            for(int j=0;j<EntradaBankai.LINGUAS;j++){var a=new ArrayList<Object>();var c=new ArrayList<Object>();for(int u=0;u<=6;u++){a.add(EntradaBankai.lingua(j,u/6d,t));c.add(EntradaBankai.chama(j,u/6d,t));}li.add(a);ch.add(c);}
            var pi=new ArrayList<Object>();for(int i=0;i<EntradaBankai.COLUNA;i++){var l=new ArrayList<Object>();for(int j=0;j<EntradaBankai.VOLTAS;j++)l.add(EntradaBankai.pilar(i,j,t));pi.add(l);}
            var cq=new ArrayList<Object>();for(int i=0;i<=EntradaBankai.ANEL;i++){var l=new ArrayList<Object>();for(int j=0;j<EntradaBankai.SECAO;j++)l.add(EntradaBankai.choque(i%EntradaBankai.ANEL,j,t));cq.add(l);}
            var nu=new ArrayList<Object>();for(int k=0;k<EntradaBankai.NUVENS;k++){var gf=new ArrayList<Object>();for(int i=0;i<EntradaBankai.LAT;i++){var l=new ArrayList<Object>();for(int j=0;j<EntradaBankai.LON;j++)l.add(EntradaBankai.bolha(k,i,j,t));gf.add(l);}nu.add(List.of(gf,EntradaBankai.alphaNuvem(k,t),EntradaBankai.raioNuvem(k,t)));}
            var co=new ArrayList<Object>();for(int i=0;i<EntradaBankai.ARCO;i++){var l=new ArrayList<Object>();for(int j=0;j<EntradaBankai.SECAO_CORTE;j++)l.add(EntradaBankai.corte(i,j,t));co.add(l);}
            m.put("fase",EntradaBankai.fase(t));m.put("vazamento",EntradaBankai.vazamento(t));m.put("linguas",li);m.put("pilar",pi);m.put("alphaPilar",EntradaBankai.alphaPilar(t));m.put("altura",EntradaBankai.alturaPilar(t));
            m.put("choque",cq);m.put("alphaChoque",EntradaBankai.alphaChoque(t));m.put("raioChoque",EntradaBankai.raioChoque(t));m.put("nuvens",nu);m.put("corte",co);m.put("alphaCorte",EntradaBankai.alphaCorte(t));
            m.put("aura",EntradaBankai.aura(t));m.put("chamas",ch);m.put("traje",EntradaBankai.trajeVisivel(t));m.put("pulso",EntradaBankai.pulso(t));q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/ichigo-bankai-entrada-samples.json"))){new Gson().toJson(Map.of("quadros",q,"grito",G,"revela",R,"fim",F),w);}
    }
}
