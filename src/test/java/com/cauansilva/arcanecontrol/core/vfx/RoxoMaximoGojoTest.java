package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class RoxoMaximoGojoTest {
    @Test void cameraPermaneceForaDoNucleoEDentroDoDominio(){
        var casos=List.of(new RoxoMaximoGojo.V(0,-1.62,0),new RoxoMaximoGojo.V(0,-1.62,-1),new RoxoMaximoGojo.V(0,-1.62,-10),new RoxoMaximoGojo.V(0,-1.62,-24),new RoxoMaximoGojo.V(0,-24,0),new RoxoMaximoGojo.V(0,24,0),new RoxoMaximoGojo.V(15,-10,-15));
        for(var dominio:casos){
            RoxoMaximoGojo.V anterior=null;
            for(double t=0;t<=110;t+=.1){
                var q=RoxoMaximoGojo.quadro(t,dominio.add(new RoxoMaximoGojo.V(0,1.62,0)),dominio);
                assertTrue(Double.isFinite(q.camera().tamanho()));assertTrue(q.camera().tamanho()>=RoxoMaximoGojo.raio(t)+1.19);
                assertTrue(q.camera().sub(dominio).tamanho()<=24.8001);assertTrue(q.fov()>=.8&&q.fov()<=1.22);
                if(t>=90&&t<92)assertTrue(q.camera().tamanho()<30,"Climax must stay inside the existing impact-frame visibility range");
                if(anterior!=null)assertTrue(q.camera().sub(anterior).tamanho()<.7,"Camera path must not jump near the domain boundary");
                anterior=q.camera();
            }
        }
    }
    @Test void climaxSomETremorCoincidemComImpactoExistente(){
        assertEquals(90,RoxoMaximoGojo.COLAPSO);assertEquals(110,RoxoMaximoGojo.DURACAO);
        assertEquals(RoteiroUltimate.para(1,32,110).tick(),RoxoMaximoGojo.COLAPSO);
        var audio=com.cauansilva.arcanecontrol.core.audio.RoteiroAudio.roxoMaximo();assertEquals(2,audio.size());assertEquals(0,audio.get(0).atraso());assertEquals("purple_maximum_charge",audio.get(0).nome());assertEquals(90,audio.get(audio.size()-1).atraso());
        assertEquals(0,RoxoMaximoGojo.impacto(89));assertTrue(RoxoMaximoGojo.impacto(90)>.7);assertEquals(0,RoxoMaximoGojo.alpha(110));
        assertEquals(0,RoxoMaximoGojo.raioChoque(89));assertEquals(0,RoxoMaximoGojo.alphaChoque(110));
    }
    @Test void cinemaEExplosaoSaoLimitadosEEncerram(){
        assertEquals(0,RoxoMaximoGojo.pesoCinema(0));assertEquals(0,RoxoMaximoGojo.pesoCinema(110));
        assertEquals(0,RoxoMaximoGojo.raioExplosao(89));assertEquals(0,RoxoMaximoGojo.alphaExplosao(110));
        assertTrue(RoxoMaximoGojo.raioExplosao(98)>RoxoMaximoGojo.raio(98)+10);
        for(double t=0;t<=120;t+=.1){double p=RoxoMaximoGojo.pesoCinema(t);assertTrue(p>=0&&p<=1);assertTrue(RoxoMaximoGojo.alphaExplosao(t)>=0);}
    }
    @Test void exportaCameraMalhasERaiosParaPrevia()throws Exception{
        var samples=new LinkedHashMap<String,Object>();
        for(int t=0;t<=110;t++){
            var cam=RoxoMaximoGojo.quadro(t,new RoxoMaximoGojo.V(0,0,-10),new RoxoMaximoGojo.V(0,-1.62,-10));double r=RoxoMaximoGojo.raio(t);
            var coroas=new ArrayList<Object>();var raios=new ArrayList<Object>();var ramos=new ArrayList<Object>();var choque=new ArrayList<Object>();
            for(int j=0;j<3;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=48;i++)pontos.add(CoroaRoxoGojo.ponto(j,i/48d,t,r));coroas.add(pontos);}
            for(int j=0;j<8;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=8;i++)pontos.add(RaiosRoxo.ponto(j,i,t));raios.add(pontos);ramos.add(List.of(RaiosRoxo.ramo(j,3,t),RaiosRoxo.ramo(j,5,t)));}
            for(int j=0;j<3;j++){var pontos=new ArrayList<Object>();for(int i=0;i<=64;i++)pontos.add(RoxoMaximoGojo.anel(j,i/64d,t));choque.add(pontos);}
            samples.put(Integer.toString(t),Map.of("explosao",Map.of("raio",RoxoMaximoGojo.raioExplosao(t),"alpha",RoxoMaximoGojo.alphaExplosao(t),"pesoPost",RoxoMaximoGojo.pesoCinema(t)),"quadro",cam,"raio",r,"alpha",RoxoMaximoGojo.alpha(t),"coroas",coroas,"raios",raios,"ramos",ramos,"choque",choque,"alphaChoque",RoxoMaximoGojo.alphaChoque(t),"impacto",RoxoMaximoGojo.impacto(t)));
        }
        Files.writeString(Path.of("build/gojo-roxo-maximo-samples.json"),new Gson().toJson(Map.of("quadros",samples,"audio",com.cauansilva.arcanecontrol.core.audio.RoteiroAudio.roxoMaximo())));
    }
}
