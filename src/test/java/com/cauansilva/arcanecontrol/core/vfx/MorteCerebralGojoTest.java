package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MorteCerebralGojoTest {
    @Test void informacaoDesceNaVerticalConvergeNaCabecaEEncerra(){
        assertEquals(0,MorteCerebralGojo.alpha(6));assertEquals(0,MorteCerebralGojo.alpha(-1));
        for(int j=0;j<MorteCerebralGojo.FAIXAS;j++)for(int i=0;i<=12;i++){
            double u=i/12d;double anterior=Double.POSITIVE_INFINITY;
            for(double t=0;t<=6;t+=.1){
                var p=MorteCerebralGojo.linha(j,u,t);assertTrue(Double.isFinite(p.x()+p.y()+p.z()));
                assertTrue(p.y()<=anterior+1e-8);anterior=p.y();assertTrue(p.y()>=.179&&p.y()<3.3);assertTrue(Math.hypot(p.x(),p.z())<.55);
                double alpha=MorteCerebralGojo.alpha(t);assertTrue(alpha>=0&&alpha<=1);
            }
            assertTrue(MorteCerebralGojo.linha(j,0,5).y()<.25);
            assertTrue(MorteCerebralGojo.linha(j,0,3).y()<.45);assertTrue(MorteCerebralGojo.alpha(3)>.4);
        }
        assertFalse(com.cauansilva.arcanecontrol.core.rpg.PerfilBoss.efeito(MorteCerebralGojo.EFEITO));
    }
    @Test void exportaGeometriaParaPrevia()throws Exception{
        var samples=new LinkedHashMap<String,Object>();
        for(int t=0;t<=6;t++){
            var linhas=new ArrayList<Object>();var cores=new ArrayList<Integer>();
            for(int j=0;j<MorteCerebralGojo.FAIXAS;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=12;i++)pontos.add(MorteCerebralGojo.linha(j,i/12d,t));linhas.add(pontos);cores.add(VazioInfinitoGojo.cor(j,.5,t));}
            samples.put(Integer.toString(t),Map.of("alpha",MorteCerebralGojo.alpha(t),"linhas",linhas,"cores",cores));
        }
        Files.writeString(Path.of("build/gojo-olhar-samples.json"),new Gson().toJson(samples));
    }
}
