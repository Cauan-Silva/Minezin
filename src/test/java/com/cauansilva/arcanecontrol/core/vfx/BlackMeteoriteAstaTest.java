package com.cauansilva.arcanecontrol.core.vfx;
import com.cauansilva.arcanecontrol.core.asta.PerfilAsta;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BlackMeteoriteAstaTest {
    private static final int I=BlackMeteoriteAsta.IMPACTO,F=BlackMeteoriteAsta.FIM;
    @Test void impactoAconteceNoTickDoDanoDoServidor(){
        assertEquals(PerfilAsta.impacto(0,true),I);
        for(double t=0;t<I;t+=.25){
            assertEquals(0,BlackMeteoriteAsta.cratera(t),1e-9);assertEquals(0,BlackMeteoriteAsta.estrela(t),1e-9);
            for(int k=0;k<BlackMeteoriteAsta.ESPINHOS;k++)assertEquals(0,BlackMeteoriteAsta.alturaEspinho(k,t),1e-9);
            for(int i=0;i<BlackMeteoriteAsta.GOTAS;i++)assertEquals(0,BlackMeteoriteAsta.alphaGota(i,t),1e-9);
        }
        assertEquals(1,BlackMeteoriteAsta.varredura(I),1e-9);assertEquals(0,BlackMeteoriteAsta.varredura(I-3),1e-9);
        // The body is a beam during the dash and comes back only for the overhead cut, before the swing starts.
        int r=BlackMeteoriteAsta.REAPARECE;assertTrue(r<=I-3);
        assertFalse(BlackMeteoriteAsta.corpoVisivel(0));assertFalse(BlackMeteoriteAsta.corpoVisivel(r-.01));assertTrue(BlackMeteoriteAsta.corpoVisivel(r));
        assertTrue(BlackMeteoriteAsta.feixe(1)>.99&&BlackMeteoriteAsta.feixe(r+.5)<1e-9&&BlackMeteoriteAsta.reaparece(r)>.99);
        assertEquals(BlackMeteoriteAsta.RAIO,BlackMeteoriteAsta.cratera(I+3),1e-9);
    }
    @Test void crateraCobreORaioDoDano(){
        // Server: damage radius 4 around the owner at the hit tick.
        double min=9,max=0;for(int i=0;i<BlackMeteoriteAsta.PONTOS_CRATERA;i++){double b=BlackMeteoriteAsta.borda(i);min=Math.min(min,b);max=Math.max(max,b);}
        assertTrue(min>=.8&&max<=1.1,"borda "+min+".."+max);
        for(int k=0;k<BlackMeteoriteAsta.ESPINHOS;k++){var b=BlackMeteoriteAsta.baseEspinho(k);assertTrue(Math.hypot(b.x(),b.z())<BlackMeteoriteAsta.RAIO);}
        for(double t=0;t<=F;t+=.25)for(int i=0;i<BlackMeteoriteAsta.GOTAS;i++){
            var g=BlackMeteoriteAsta.gota(i,t);assertTrue(Math.hypot(g.x(),g.z())<BlackMeteoriteAsta.RAIO&&g.y()>=0);
        }
    }
    @Test void tudoSomeAteOFim(){
        assertEquals(0,BlackMeteoriteAsta.alphaCratera(F),1e-9);assertEquals(0,BlackMeteoriteAsta.alphaRastro(F),1e-9);
        assertEquals(0,BlackMeteoriteAsta.alphaGolpe(F),1e-9);assertEquals(0,BlackMeteoriteAsta.arranque(F),1e-9);
        for(int k=0;k<BlackMeteoriteAsta.ESPINHOS;k++)assertEquals(0,BlackMeteoriteAsta.alturaEspinho(k,F),1e-9);
        for(int i=0;i<BlackMeteoriteAsta.GOTAS;i++)assertEquals(0,BlackMeteoriteAsta.alphaGota(i,F),1e-9);
        for(int i=0;i<BlackMeteoriteAsta.GOTAS_RASTRO;i++)assertEquals(0,BlackMeteoriteAsta.alphaGotaRastro(i,F),1e-9);
    }
    /** Server dash: up to 0.9 block per tick until the target, here 9.5 blocks ahead. */
    private static double avanco(double t){return Math.min(.9*Math.min(t,I),9.5);}
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(int n=0;n<=F*4;n++){
            double t=n/4.0,d=avanco(t),raio=BlackMeteoriteAsta.cratera(t);
            var espirais=new ArrayList<Object>();for(int k=0;k<BlackMeteoriteAsta.ESPIRAIS;k++){var l=new ArrayList<Object>();for(int i=0;i<=24;i++)l.add(BlackMeteoriteAsta.espiral(k,i/24d,t));espirais.add(l);}
            var estilhacos=new ArrayList<Object>();for(int k=0;k<BlackMeteoriteAsta.ESTILHACOS;k++)estilhacos.add(BlackMeteoriteAsta.estilhaco(k,t));
            var arranque=new ArrayList<Object>();for(int k=0;k<BlackMeteoriteAsta.ESPINHOS;k++)arranque.add(BlackMeteoriteAsta.espinhoArranque(k,t));
            var gotasRastro=new ArrayList<Object>();for(int i=0;i<BlackMeteoriteAsta.GOTAS_RASTRO;i++)gotasRastro.add(List.of(BlackMeteoriteAsta.gotaRastro(i,t,d),BlackMeteoriteAsta.alphaGotaRastro(i,t)));
            var borda=new ArrayList<Object>();for(int i=0;i<BlackMeteoriteAsta.PONTOS_CRATERA;i++)borda.add(BlackMeteoriteAsta.bordaCratera(i,raio));
            var rachaduras=new ArrayList<Object>();for(int k=0;k<BlackMeteoriteAsta.RACHADURAS;k++){var l=new ArrayList<Object>();for(int i=0;i<=5;i++)l.add(BlackMeteoriteAsta.rachadura(k,i,raio));rachaduras.add(l);}
            var espinhos=new ArrayList<Object>();for(int k=0;k<BlackMeteoriteAsta.ESPINHOS;k++)espinhos.add(List.of(BlackMeteoriteAsta.baseEspinho(k),BlackMeteoriteAsta.alturaEspinho(k,t)));
            var gotas=new ArrayList<Object>();for(int i=0;i<BlackMeteoriteAsta.GOTAS;i++)gotas.add(List.of(BlackMeteoriteAsta.gota(i,t),BlackMeteoriteAsta.alphaGota(i,t)));
            var m=new LinkedHashMap<String,Object>();
            m.put("avanco",d);m.put("arranque",BlackMeteoriteAsta.arranque(t));m.put("feixe",BlackMeteoriteAsta.feixe(t));m.put("visivel",BlackMeteoriteAsta.corpoVisivel(t));m.put("reaparece",BlackMeteoriteAsta.reaparece(t));m.put("cauda",BlackMeteoriteAsta.caudaRastro(t));m.put("rastro",BlackMeteoriteAsta.alphaRastro(t));
            m.put("varredura",BlackMeteoriteAsta.varredura(t));m.put("golpe",BlackMeteoriteAsta.alphaGolpe(t));m.put("estrela",BlackMeteoriteAsta.estrela(t));m.put("cratera",raio);m.put("alphaCratera",BlackMeteoriteAsta.alphaCratera(t));
            m.put("espirais",espirais);m.put("estilhacos",estilhacos);m.put("espinhosArranque",arranque);m.put("gotasRastro",gotasRastro);m.put("borda",borda);m.put("rachaduras",rachaduras);m.put("espinhos",espinhos);m.put("gotas",gotas);
            q.put(Double.toString(t),m);
        }
        var feixe=new ArrayList<Object>();for(int i=0;i<=BlackMeteoriteAsta.PONTOS_FEIXE;i++){double u=i/(double)BlackMeteoriteAsta.PONTOS_FEIXE;feixe.add(List.of(BlackMeteoriteAsta.eixoFeixe(u),BlackMeteoriteAsta.larguraFeixe(u)));}
        var rastro=new ArrayList<Object>();for(int i=0;i<=40;i++){double u=i/40d;rastro.add(List.of(u,BlackMeteoriteAsta.desvioRastro(u),BlackMeteoriteAsta.larguraRastro(u)));}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/asta-meteorite-samples.json"))){new Gson().toJson(Map.of("quadros",q,"feixe",feixe,"rastro",rastro,"impacto",I,"fim",F,"arco",BlackMeteoriteAsta.ARCO_GOLPE,"janela",BlackMeteoriteAsta.JANELA_GOLPE),w);}
    }
}
