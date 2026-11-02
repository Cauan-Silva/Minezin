package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class GolpesBaseAstaTest {
    @Test void cortesFechamNoTickDoDano(){
        for(int slot=0;slot<3;slot++){
            int i=GolpesBaseAsta.impacto(slot);
            assertEquals(0,GolpesBaseAsta.varredura(slot,i-2),1e-9);assertEquals(1,GolpesBaseAsta.varredura(slot,i+1),1e-9);
            assertEquals(0,GolpesBaseAsta.alpha(slot,GolpesBaseAsta.DURACAO),1e-9);
        }
        assertEquals(0,GolpesBaseAsta.rasgo(GolpesBaseAsta.impacto(1)-1),1e-9);
        assertEquals(0,GolpesBaseAsta.alphaEspinho(GolpesBaseAsta.impacto(2)-1),1e-9);
        assertEquals(0,GolpesBaseAsta.alphaGuarda(GolpesBaseAsta.GUARDA),1e-9);
    }
    @Test void formasFicamDentroDasAreasDoServidor(){
        for(double u=-1;u<=1;u+=.05)for(double v=0;v<=1;v+=.25){
            var p=GolpesBaseAsta.leque(u,v);double r=Math.hypot(p.x(),p.z());
            assertTrue(r<=5+1e-9);assertTrue(p.z()/r>=Math.cos(Math.PI/3)-1e-9,"cone de 120 graus");
        }
        for(int k=0;k<GolpesBaseAsta.ESPINHOS;k++)for(double t=0;t<=32;t++){var e=GolpesBaseAsta.espinho(k,t);assertTrue(Math.hypot(e.x(),e.z())<=3);}
        assertTrue(GolpesBaseAsta.rastro(GolpesBaseAsta.impacto(2)+3)==0);
    }
    @Test void exportaParaPrevia()throws Exception{
        var out=new LinkedHashMap<String,Object>();
        for(int t=0;t<=GolpesBaseAsta.DURACAO;t++){
            var esp=new ArrayList<Object>();for(int k=0;k<GolpesBaseAsta.ESPINHOS;k++)esp.add(GolpesBaseAsta.espinho(k,t));
            var lq=new ArrayList<Object>();for(int i=0;i<=20;i++){double u=-1+2*i/20d;lq.add(List.of(GolpesBaseAsta.leque(u,0),GolpesBaseAsta.leque(u,1)));}
            var cv=new ArrayList<Object>();for(int i=0;i<=18;i++){double u=i/18d;cv.add(List.of(GolpesBaseAsta.corteVertical(u,0),GolpesBaseAsta.corteVertical(u,1)));}
            var gd=new ArrayList<Object>();for(int i=0;i<=22;i++){double u=-1+2*i/22d;gd.add(List.of(GolpesBaseAsta.guarda(u,0,t),GolpesBaseAsta.guarda(u,1,t)));}
            var m=new LinkedHashMap<String,Object>();
            for(int s=0;s<3;s++){m.put("varre"+s,GolpesBaseAsta.varredura(s,t));m.put("alpha"+s,GolpesBaseAsta.alpha(s,t));}
            m.put("rasgo",GolpesBaseAsta.rasgo(t));m.put("rastro",GolpesBaseAsta.rastro(t));m.put("alphaEspinho",GolpesBaseAsta.alphaEspinho(t));m.put("guarda",GolpesBaseAsta.alphaGuarda(t));
            m.put("espinhos",esp);m.put("leque",lq);m.put("vertical",cv);m.put("arco",gd);
            out.put(Integer.toString(t),m);
        }
        var bordas=new ArrayList<Object>();for(int i=0;i<=11;i++)bordas.add(List.of(GolpesBaseAsta.bordaRasgo(i),GolpesBaseAsta.larguraRasgo(i)));
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/asta-base-samples.json"))){new Gson().toJson(Map.of("quadros",out,"rasgo",bordas),w);}
    }
}
