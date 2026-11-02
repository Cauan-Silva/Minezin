package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ChuvaInformacoesGojoTest {
    @Test void chuvaDescePermaneceNoDomoEEncerra(){
        assertEquals(0,ChuvaInformacoesGojo.alpha(70));assertEquals(0,ChuvaInformacoesGojo.alpha(71));
        for(int j=0;j<ChuvaInformacoesGojo.FAIXAS;j++)for(double t=0;t<70;t+=.5)for(int i=0;i<=12;i++){
            var p=ChuvaInformacoesGojo.coluna(j,i/12d,t);assertTrue(Double.isFinite(p.x()+p.y()+p.z()));
            assertTrue(p.x()*p.x()+p.y()*p.y()+p.z()*p.z()<26*26);assertTrue(p.y()>=0);
        }
        // Before a new streak respawns above, its head travels downwards.
        assertTrue(ChuvaInformacoesGojo.coluna(0,0,1).y()<ChuvaInformacoesGojo.coluna(0,0,0).y());
    }
    @Test void exportaGeometriaParaPrevia()throws Exception{
        var samples=new LinkedHashMap<String,Object>();
        for(int t=0;t<=70;t+=2){
            var presencas=new ArrayList<Double>();var colunas=new ArrayList<Object>();var cores=new ArrayList<Integer>();var marcas=new ArrayList<Object>();
            for(int j=0;j<ChuvaInformacoesGojo.FAIXAS;j++){
                var pontos=new ArrayList<Object>();for(int i=0;i<=ChuvaInformacoesGojo.PASSOS;i++)pontos.add(ChuvaInformacoesGojo.coluna(j,i/(double)ChuvaInformacoesGojo.PASSOS,t));
                var simbolos=new ArrayList<Object>();for(int m=0;m<4;m++)simbolos.add(ChuvaInformacoesGojo.coluna(j,(m+.5)/4,t));
                presencas.add(ChuvaInformacoesGojo.presenca(j,t));colunas.add(pontos);cores.add(VazioInfinitoGojo.cor(j,.5,t));marcas.add(simbolos);
            }
            var luz=new ArrayList<Double>();for(int i=0;i<=ChuvaInformacoesGojo.PASSOS;i++)luz.add(ChuvaInformacoesGojo.luz(i/(double)ChuvaInformacoesGojo.PASSOS));
            samples.put(Integer.toString(t),Map.of("presencas",presencas,"alpha",ChuvaInformacoesGojo.alpha(t),"colunas",colunas,"cores",cores,"marcas",marcas,"luz",luz));
        }
        Files.writeString(Path.of("build/gojo-chuva-samples.json"),new Gson().toJson(samples));
    }
}
