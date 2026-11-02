package com.cauansilva.arcanecontrol.core.vfx;
import com.cauansilva.arcanecontrol.core.cid.FormaEnergiaAtomic;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class CinemaAtomicTest {
    @Test void sequenciaTemQuadrosDistintosNoImpactoSemAlterarRelogioDoServidor(){
        assertEquals(50,PerfilAtomic.FEIXO_INICIO);assertEquals(90,PerfilAtomic.IMPACTO);assertEquals(170,PerfilAtomic.FEIXO_FIM);assertEquals(240,PerfilAtomic.FIM);
        assertEquals(RoteiroCinemaAtomic.Fase.NEGATIVO,RoteiroCinemaAtomic.fase(88.5,true));
        assertEquals(RoteiroCinemaAtomic.Fase.PAPEL,RoteiroCinemaAtomic.fase(90.5,true));
        assertEquals(RoteiroCinemaAtomic.Fase.INVERSAO,RoteiroCinemaAtomic.fase(91.5,true));
        assertEquals(RoteiroCinemaAtomic.Fase.DUOTOM,RoteiroCinemaAtomic.fase(93,true));
        assertEquals(RoteiroCinemaAtomic.Fase.TRACO,RoteiroCinemaAtomic.fase(95,true));
        assertEquals(RoteiroCinemaAtomic.Fase.DESCARGA,RoteiroCinemaAtomic.fase(100,true));
        assertEquals(RoteiroCinemaAtomic.Fase.INATIVA,RoteiroCinemaAtomic.fase(240,true));
    }
    @Test void desligarFramesRemoveTodosOsEstadosDeInversao(){
        var permitidas=Set.of(RoteiroCinemaAtomic.Fase.CARGA,RoteiroCinemaAtomic.Fase.COMPRESSAO,RoteiroCinemaAtomic.Fase.DESCARGA,RoteiroCinemaAtomic.Fase.RESCALDO);
        for(double t=0;t<240;t+=.25)assertTrue(permitidas.contains(RoteiroCinemaAtomic.fase(t,false)),"tick "+t);
        assertEquals(RoteiroCinemaAtomic.Fase.INATIVA,RoteiroCinemaAtomic.fase(Double.NaN,true));
    }
    @Test void pesoLimitaObservadoresEDissipaSemTransformacaoResidual(){
        assertEquals(0,RoteiroCinemaAtomic.peso(0,0,true));assertEquals(0,RoteiroCinemaAtomic.peso(240,0,true));
        assertEquals(0,RoteiroCinemaAtomic.peso(Double.NaN,0,true));assertEquals(0,RoteiroCinemaAtomic.peso(90,Double.POSITIVE_INFINITY,false));
        assertEquals(1,RoteiroCinemaAtomic.peso(90,12,true));assertEquals(0,RoteiroCinemaAtomic.peso(90,80,false));
        assertTrue(RoteiroCinemaAtomic.peso(90,30,false)>RoteiroCinemaAtomic.peso(90,60,false));
        for(double t=0;t<=240;t+=.5)for(int d=0;d<=100;d+=5){double p=RoteiroCinemaAtomic.peso(t,d,false);assertTrue(Double.isFinite(p)&&p>=0&&p<=1);}
        assertTrue(RoteiroCinemaAtomic.peso(239.99,0,true)<.000001);
    }
    @Test void fragmentosTemOrcamentoEspacialETemporalFinito(){
        for(int i=0;i<48;i++)for(double t=0;t<=240;t+=.5){var d=FormaEnergiaAtomic.detrito(i,t);var p=d.ponto();
            assertTrue(Double.isFinite(p.x()+p.y()+p.z()+d.alpha()+d.giro()));assertTrue(d.alpha()>=0&&d.alpha()<=1);
            assertTrue(p.x()*p.x()+p.z()*p.z()<=900);assertTrue(p.y()>=0&&p.y()<14);assertTrue(d.tamanho()>=0&&d.tamanho()<.3);
        }
        assertEquals(0,FormaEnergiaAtomic.detrito(0,90).alpha());assertEquals(0,FormaEnergiaAtomic.detrito(47,240).alpha());
        assertEquals(0,FormaEnergiaAtomic.detrito(0,Double.NaN).alpha());
    }
    private JsonObject json(String path)throws Exception{try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/"+path)){assertNotNull(in,path);return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}}
    @Test void cadeiaPrivadaEProgramasTemRecursosCompletos()throws Exception{
        var chain=json("shaders/post/atomic_cinema.json");assertEquals("atomic_swap",chain.getAsJsonArray("targets").get(0).getAsString());assertEquals(0,chain.getAsJsonArray("passes").size());
        var program=json("shaders/program/atomic_cinema.json");var names=new HashSet<String>();for(var u:program.getAsJsonArray("uniforms"))names.add(u.getAsJsonObject().get("name").getAsString());
        assertTrue(names.containsAll(List.of("AtomicTick","Strength","Frames","ScreenCenter","InSize","OutSize","ProjMat")));assertEquals(2,program.getAsJsonArray("samplers").size());
        for(String prefix:List.of("shaders/program/atomic_cinema","shaders/core/atomic_copy"))for(String suffix:List.of(".vsh",".fsh",".json"))try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/"+prefix+suffix)){assertNotNull(in,prefix+suffix);assertTrue(in.readAllBytes().length>30);}
    }
}
