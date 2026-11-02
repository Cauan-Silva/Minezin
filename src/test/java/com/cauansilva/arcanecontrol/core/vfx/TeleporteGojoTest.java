package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class TeleporteGojoTest {
    @Test void extremidadesCoincidemComServidorETeleporteEncerra(){
        assertTrue(TeleporteGojo.saida(0)>0&&TeleporteGojo.chegada(0)>0);
        assertEquals(0,TeleporteGojo.rastro(6));assertEquals(0,TeleporteGojo.saida(18));assertEquals(0,TeleporteGojo.chegada(18));
        for(var d:List.of(new double[]{0,10,0},new double[]{8,0,0},new double[]{0,-7,13},new double[]{30,0,-40}))for(int j=0;j<4;j++){
            var a=TeleporteGojo.trilha(j,0,2,d[0],d[1],d[2]);var b=TeleporteGojo.trilha(j,1,2,d[0],d[1],d[2]);
            assertEquals(0,a.x(),1e-8);assertEquals(0,a.z(),1e-8);assertEquals(d[0],b.x(),1e-8);assertEquals(d[2],b.z(),1e-8);assertEquals(d[1],b.y()-a.y(),1e-8);
            for(int i=0;i<=32;i++){var p=TeleporteGojo.trilha(j,i/32d,2,d[0],d[1],d[2]);assertTrue(Double.isFinite(p.x()+p.y()+p.z()));}
        }
    }
    @Test void exportaGeometriaParaPrevia()throws Exception{
        var samples=new LinkedHashMap<String,Object>();
        for(int t=0;t<=18;t++){
            var pontas=new ArrayList<Object>();
            for(boolean chegada:new boolean[]{false,true}){
                var arcos=new ArrayList<Object>();var particulas=new ArrayList<Object>();
                for(int j=0;j<3;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=32;i++)pontos.add(TeleporteGojo.arco(j,i/32d,t,chegada));arcos.add(pontos);}
                for(int j=0;j<16;j++)particulas.add(TeleporteGojo.fragmento(j,t,chegada));
                pontas.add(Map.of("arcos",arcos,"particulas",particulas,"alpha",chegada?TeleporteGojo.chegada(t):TeleporteGojo.saida(t)));
            }
            var rastros=new ArrayList<Object>();for(int j=0;j<4;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=32;i++)pontos.add(TeleporteGojo.trilha(j,i/32d,t,8,0,0));rastros.add(pontos);}
            samples.put(Integer.toString(t),Map.of("pontas",pontas,"rastros",rastros,"alphaRastro",TeleporteGojo.rastro(t)));
        }
        Files.writeString(Path.of("build/gojo-teleporte-samples.json"),new Gson().toJson(samples));
    }
}
