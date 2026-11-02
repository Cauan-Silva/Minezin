package com.cauansilva.arcanecontrol.core.vfx;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import java.nio.file.*;
import java.util.*;
class ComposicaoCidTest {
    private static final ComposicaoCid.V CAMERA=new ComposicaoCid.V(8,5,-10);
    @Test void geometriasTemOrcamentoCoordenadasFinitasEEncerram(){
        for(int tipo:new int[]{11,12,14,17})for(int t=0;t<242;t++){
            var normal=ComposicaoCid.gerar(tipo,t,6,CAMERA,false);var reduzida=ComposicaoCid.gerar(tipo,t,6,CAMERA,true);
            int n=normal.stream().mapToInt(l->l.faces().size()).sum(),r=reduzida.stream().mapToInt(l->l.faces().size()).sum();assertTrue(n<=1800);assertTrue(r<=n);
            for(var l:normal){assertFalse(l.faces().isEmpty());}
            for(var l:normal)for(var q:l.faces()){
                assertTrue(q.alpha()>=0&&q.alpha()<=1);
                for(var v:List.of(q.a(),q.b(),q.c(),q.d()))assertTrue(Double.isFinite(v.x()+v.y()+v.z())&&v.len()<(tipo==14?80:25));
            }
        }
        for(int tipo:new int[]{11,12,14,17})assertTrue(ComposicaoCid.gerar(tipo,242,6,CAMERA,false).isEmpty());
        assertTrue(ComposicaoCid.gerar(11,Double.NaN,6,CAMERA,false).isEmpty());
    }
    @Test void exportaMalhasExatasParaRevisaoAnimada()throws Exception{
        var cenas=new LinkedHashMap<String,Object>();
        for(int tipo:new int[]{11,12,14,17}){
            var frames=new ArrayList<Object>();int duracao=tipo==14?242:tipo==17?34:26;
            for(int t=0;t<duracao;t++)frames.add(ComposicaoCid.gerar(tipo,t,6,CAMERA,false));cenas.put(Integer.toString(tipo),frames);
        }
        try(var writer=Files.newBufferedWriter(Path.of("build/cid-composition-samples.json"))){new Gson().toJson(cenas,writer);}
        assertEquals(ComposicaoCid.gerar(11,8,6,CAMERA,false),ComposicaoCid.gerar(11,8,6,CAMERA,false));
    }
}
