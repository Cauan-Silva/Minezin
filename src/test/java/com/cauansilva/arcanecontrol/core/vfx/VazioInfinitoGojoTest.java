package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class VazioInfinitoGojoTest {
    @Test void geometriaFicaDentroDaBarreiraEAmbienteTermina(){
        assertEquals(26,PerfilVfxGojo.DOMINIO_RAIO);assertEquals(35,PerfilVfxGojo.DOMINIO_ABERTURA);assertEquals(400,PerfilVfxGojo.duracao(5));
        assertFalse(VazioInfinitoGojo.interior(27,26));assertFalse(VazioInfinitoGojo.interior(26,26));assertTrue(VazioInfinitoGojo.interior(10,26));
        assertEquals(0,PerfilVfxGojo.presencaDominio(400,0));assertEquals(0,PerfilVfxGojo.presencaDominio(80,27));
        assertTrue(VazioInfinitoGojo.pulso(0,.5,32)>VazioInfinitoGojo.pulso(0,.5,112)*4);
        for(int j=0;j<VazioInfinitoGojo.FAIXAS;j++)for(int t=35;t<400;t+=11)for(int i=0;i<=VazioInfinitoGojo.PASSOS;i++){
            double u=i/(double)VazioInfinitoGojo.PASSOS;var p=VazioInfinitoGojo.informacao(j,u,t);
            assertTrue(Double.isFinite(p.x()+p.y()+p.z()));assertTrue(p.x()*p.x()+p.y()*p.y()+p.z()*p.z()<26*26);
            double a=VazioInfinitoGojo.pulso(j,u,t);assertTrue(a>=0&&a<=1);
        }
    }
    @Test void exportaGeometriaParaPrevia()throws Exception{
        var samples=new LinkedHashMap<String,Object>();
        for(int t=0;t<=400;t+=2){
            var fluxos=new ArrayList<Object>();var pulsos=new ArrayList<Object>();var cores=new ArrayList<Object>();
            for(int j=0;j<VazioInfinitoGojo.FAIXAS;j++){
                var pontos=new ArrayList<Object>();var luz=new ArrayList<Double>();var cor=new ArrayList<Integer>();
                for(int i=0;i<=VazioInfinitoGojo.PASSOS;i++){pontos.add(VazioInfinitoGojo.informacao(j,i/(double)VazioInfinitoGojo.PASSOS,t));luz.add(VazioInfinitoGojo.pulso(j,i/(double)VazioInfinitoGojo.PASSOS,t));cor.add(VazioInfinitoGojo.cor(j,i/(double)VazioInfinitoGojo.PASSOS,t));}
                fluxos.add(pontos);pulsos.add(luz);cores.add(cor);
            }
            samples.put(Integer.toString(t),Map.of("passos",VazioInfinitoGojo.PASSOS,"raio",PerfilVfxGojo.raioDominio(t),"alpha",PerfilVfxGojo.opacidade(5,t),"presenca",PerfilVfxGojo.presencaDominio(t,Math.sqrt(15*15+1.7*1.7)),"fluxos",fluxos,"pulsos",pulsos,"cores",cores));
        }
        Files.writeString(Path.of("build/gojo-dominio-samples.json"),new Gson().toJson(samples));
    }
}
