package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class FluxoAzulGojoTest {
    @Test void fluxosConvergemEPulsosAvancamParaDentro(){
        for(int j=0;j<12;j++)for(int t=0;t<=60;t++){
            double anterior=Double.POSITIVE_INFINITY;
            for(int i=0;i<=32;i++){
                var p=FluxoAzulGojo.fluxo(j,i/32d,t);double r=Math.hypot(p.x(),p.z());
                assertTrue(Double.isFinite(p.y()));assertTrue(r<=anterior+1e-8);anterior=r;
                double pulso=FluxoAzulGojo.pulso(j,i/32d,t);assertTrue(pulso>=0&&pulso<=1);
            }
        }
        // Same travelling crest moves from s=.3 to s=.75 in ten ticks.
        assertTrue(FluxoAzulGojo.pulso(0,.3,.3/.045)>.99);
        assertTrue(FluxoAzulGojo.pulso(0,.75,.3/.045+10)>.99);
        assertEquals(60,PerfilVfxGojo.duracao(1));
    }
    @Test void exportaGeometriaDoRuntimeParaPrevia()throws Exception{
        var amostras=new LinkedHashMap<String,Object>();
        for(int t=0;t<=60;t++){
            var fluxos=new ArrayList<Object>();var pulsos=new ArrayList<Object>();var arcos=new ArrayList<Object>();
            for(int j=0;j<12;j++){
                var pontos=new ArrayList<Object>();var luz=new ArrayList<Double>();
                for(int i=0;i<=32;i++){pontos.add(FluxoAzulGojo.fluxo(j,i/32d,t));luz.add(FluxoAzulGojo.pulso(j,i/32d,t));}
                fluxos.add(pontos);pulsos.add(luz);
            }
            for(int j=0;j<3;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=48;i++)pontos.add(FluxoAzulGojo.arco(j,i/48d,t));arcos.add(pontos);}
            amostras.put(Integer.toString(t),Map.of("raio",FluxoAzulGojo.raio(t),"alpha",PerfilVfxGojo.opacidade(1,t),"fluxos",fluxos,"pulsos",pulsos,"arcos",arcos));
        }
        Files.writeString(Path.of("build/gojo-azul-samples.json"),new Gson().toJson(amostras));
    }
}
