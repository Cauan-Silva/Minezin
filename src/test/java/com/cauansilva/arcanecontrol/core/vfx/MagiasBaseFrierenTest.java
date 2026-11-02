package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MagiasBaseFrierenTest {
    @Test void feixeNasceInteiroNoTickDoDanoEApagaAntesDoCirculo(){
        assertEquals(1,MagiasBaseFrieren.larguraFeixe(0),1e-9);
        assertEquals(1,MagiasBaseFrieren.larguraFeixe(MagiasBaseFrieren.FEIXE_PLENO-1),1e-9);
        assertEquals(0,MagiasBaseFrieren.larguraFeixe(MagiasBaseFrieren.FEIXE_FIM),1e-9);
        assertTrue(MagiasBaseFrieren.alphaCirculo(MagiasBaseFrieren.FEIXE_FIM)>.5,"circulo permanece apos o feixe");
        assertEquals(0,MagiasBaseFrieren.alphaCirculo(MagiasBaseFrieren.ZOLTRAAK),1e-9);
        assertEquals(0,MagiasBaseFrieren.impacto(10),1e-9);
        for(double t=0;t<=MagiasBaseFrieren.ZOLTRAAK;t+=.5)for(int j=0;j<24;j++){double a=MagiasBaseFrieren.alphaFaisca(j,t);assertTrue(a>=0&&a<=1);}
    }
    @Test void flordeSeisPetalasFechadaESimetrica(){
        for(int k=0;k<MagiasBaseFrieren.PETALAS;k++){
            var a=MagiasBaseFrieren.petala(k,0,24);var b=MagiasBaseFrieren.petala(k,24,24);var ponta=MagiasBaseFrieren.petala(k,12,24);
            assertEquals(0,Math.hypot(a.x(),a.y()),1e-9);assertEquals(0,Math.hypot(b.x(),b.y()),1e-9);
            assertEquals(.8,Math.hypot(ponta.x(),ponta.y()),1e-9);
            var olho=MagiasBaseFrieren.olhoPetala(k);assertTrue(Math.hypot(olho.x(),olho.y())<.8);
            for(int v=0;v<3;v++){var q=MagiasBaseFrieren.triangulo(k,v);assertTrue(Math.hypot(q.x(),q.y())<.86,"triangulo dentro do aro interno");}
        }
        // Rotacionar a petala 0 em 60 graus produz a petala 1.
        var p0=MagiasBaseFrieren.petala(0,7,24);var p1=MagiasBaseFrieren.petala(1,7,24);
        double c=Math.cos(Math.PI/3),s=Math.sin(Math.PI/3);
        assertEquals(p1.x(),p0.x()*c-p0.y()*s,1e-9);assertEquals(p1.y(),p0.x()*s+p0.y()*c,1e-9);
    }
    @Test void favoDeSeteHexagonosEncaixados(){
        for(int c=1;c<MagiasBaseFrieren.CELULAS;c++){
            var o=MagiasBaseFrieren.celula(c);assertEquals(MagiasBaseFrieren.PASSO,Math.hypot(o.x(),o.y()),1e-9);
            // Celulas vizinhas compartilham uma aresta: dois vertices coincidem.
            int comuns=0;
            for(int v=0;v<6;v++)for(int w=0;w<6;w++){
                var a=MagiasBaseFrieren.vertice(0,v,1);var b=MagiasBaseFrieren.vertice(c,w,1);
                if(Math.hypot(a.x()-b.x(),a.y()-b.y())<1e-9)comuns++;
            }
            assertEquals(2,comuns);
        }
        assertTrue(MagiasBaseFrieren.escalaCelula(0,4)>MagiasBaseFrieren.escalaCelula(6,4),"acende do centro para fora");
        assertEquals(1,MagiasBaseFrieren.escalaCelula(6,12),1e-9);
        assertEquals(0,MagiasBaseFrieren.alphaBarreira(MagiasBaseFrieren.BARREIRA),1e-9);
    }
    @Test void cacosSeAfastamDoCentroESomem(){
        for(int c=0;c<MagiasBaseFrieren.CELULAS;c++)for(int v=0;v<6;v++){
            var o=MagiasBaseFrieren.celula(c);var a=MagiasBaseFrieren.vertice(c,v,1);var b=MagiasBaseFrieren.vertice(c,(v+1)%6,1);
            double cx=(o.x()+a.x()+b.x())/3,cy=(o.y()+a.y()+b.y())/3;
            var d=MagiasBaseFrieren.deslocamentoCaco(c,v,5);
            assertTrue((cx+d.x())*cx+(cy+d.y()+5*5*.002)*cy>cx*cx+cy*cy-1e-9,"caco avanca para fora");
        }
        assertEquals(0,MagiasBaseFrieren.alphaCaco(MagiasBaseFrieren.ESTILHACO),1e-9);
    }
    @Test void impulsoDiscretoEGaiolaDentroDoRaioDeAtracao(){
        assertEquals(0,MagiasBaseFrieren.alphaRajada(9),1e-9);assertEquals(0,MagiasBaseFrieren.alphaSubida(12),1e-9);
        for(int i=0;i<MagiasBaseFrieren.RASTRO;i++)assertEquals(0,MagiasBaseFrieren.alphaRastro(i,40),1e-9);
        // MovesetsServidor puxa inimigos num raio de 5 blocos durante a Gaiola.
        assertTrue(MagiasBaseFrieren.raioTornado(MagiasBaseFrieren.ALTURA_TORNADO)<=5);
        assertTrue(MagiasBaseFrieren.raioTornado(0)<MagiasBaseFrieren.raioTornado(6),"abre para cima");
        assertEquals(0,MagiasBaseFrieren.alphaTornado(MagiasBaseFrieren.GAIOLA),1e-9);
        assertEquals(0,MagiasBaseFrieren.faixaAngAltura(3,.5,0).y(),1e-9);assertTrue(MagiasBaseFrieren.faixaAngAltura(6,1,20).y()<=MagiasBaseFrieren.ALTURA_TORNADO);
        for(double t=0;t<=MagiasBaseFrieren.GAIOLA;t+=.5)for(int i=0;i<MagiasBaseFrieren.DETRITOS;i++){double y=MagiasBaseFrieren.alturaDetrito(i,t);assertTrue(y>=0&&y<=MagiasBaseFrieren.ALTURA_TORNADO);}
    }
    @Test void exportaGeometriaParaPrevia()throws Exception{
        var petalas=new ArrayList<Object>();var olhos=new ArrayList<Object>();var triangulos=new ArrayList<Object>();
        for(int k=0;k<MagiasBaseFrieren.PETALAS;k++){
            var l=new ArrayList<Object>();for(int i=0;i<=24;i++)l.add(MagiasBaseFrieren.petala(k,i,24));petalas.add(l);
            olhos.add(MagiasBaseFrieren.olhoPetala(k));triangulos.add(List.of(MagiasBaseFrieren.triangulo(k,0),MagiasBaseFrieren.triangulo(k,1),MagiasBaseFrieren.triangulo(k,2)));
        }
        var zoltraak=new LinkedHashMap<String,Object>();
        for(int t=0;t<=MagiasBaseFrieren.ZOLTRAAK;t++){
            var faiscas=new ArrayList<Object>();
            for(int j=0;j<24;j++)faiscas.add(List.of(MagiasBaseFrieren.posicaoFaisca(j),MagiasBaseFrieren.afastamentoFaisca(j,t),MagiasBaseFrieren.anguloFaisca(j),MagiasBaseFrieren.alphaFaisca(j,t)));
            zoltraak.put(Integer.toString(t),Map.of("largura",MagiasBaseFrieren.larguraFeixe(t),"alpha",MagiasBaseFrieren.alphaCirculo(t),"escala",MagiasBaseFrieren.escalaCirculo(t),
                    "giro",MagiasBaseFrieren.giroCirculo(t),"impacto",MagiasBaseFrieren.impacto(t),"faiscas",faiscas));
        }
        var barreira=new LinkedHashMap<String,Object>();
        for(int t=0;t<=MagiasBaseFrieren.BARREIRA;t++){
            var celulas=new ArrayList<Object>();
            for(int c=0;c<MagiasBaseFrieren.CELULAS;c++){
                double esc=MagiasBaseFrieren.escalaCelula(c,t);var v=new ArrayList<Object>();for(int k=0;k<6;k++)v.add(MagiasBaseFrieren.vertice(c,k,esc));
                celulas.add(Map.of("centro",MagiasBaseFrieren.celula(c),"escala",esc,"pulso",MagiasBaseFrieren.pulsoCelula(c,t),"vertices",v));
            }
            barreira.put(Integer.toString(t),Map.of("alpha",MagiasBaseFrieren.alphaBarreira(t),"celulas",celulas));
        }
        var cacos=new LinkedHashMap<String,Object>();
        for(int t=0;t<=MagiasBaseFrieren.ESTILHACO;t++){
            var l=new ArrayList<Object>();
            for(int c=0;c<MagiasBaseFrieren.CELULAS;c++)for(int v=0;v<6;v++)
                l.add(List.of(c,v,MagiasBaseFrieren.deslocamentoCaco(c,v,t),MagiasBaseFrieren.frenteCaco(c,v,t),MagiasBaseFrieren.giroCaco(c,v,t)));
            cacos.put(Integer.toString(t),Map.of("alpha",MagiasBaseFrieren.alphaCaco(t),"cacos",l));
        }
        var raiz=new LinkedHashMap<String,Object>();
        raiz.put("petalas",petalas);raiz.put("olhos",olhos);raiz.put("triangulos",triangulos);
        raiz.put("zoltraak",zoltraak);raiz.put("barreira",barreira);raiz.put("cacos",cacos);
        var hex=new ArrayList<Object>();for(int c=0;c<MagiasBaseFrieren.CELULAS;c++){var v=new ArrayList<Object>();for(int k=0;k<6;k++)v.add(MagiasBaseFrieren.vertice(c,k,1));hex.add(Map.of("centro",MagiasBaseFrieren.celula(c),"vertices",v));}
        raiz.put("favo",hex);
        var impulso=new LinkedHashMap<String,Object>();
        for(int t=0;t<=50;t++){
            var rastro=new ArrayList<Double>();for(int i=0;i<MagiasBaseFrieren.RASTRO;i++)rastro.add(MagiasBaseFrieren.alphaRastro(i,t));
            var angulos=new ArrayList<Double>();for(int j=0;j<MagiasBaseFrieren.RAJADAS;j++)angulos.add(MagiasBaseFrieren.anguloRajada(j));
            impulso.put(Integer.toString(t),Map.of("raio",MagiasBaseFrieren.raioRajada(t),"rajada",MagiasBaseFrieren.alphaRajada(t),"subida",MagiasBaseFrieren.alphaSubida(t),"rastro",rastro,"angulos",angulos));
        }
        var gaiola=new LinkedHashMap<String,Object>();
        for(int t=0;t<=MagiasBaseFrieren.GAIOLA;t++){
            var faixas=new ArrayList<Object>();
            for(int k=0;k<MagiasBaseFrieren.FAIXAS;k++){var l=new ArrayList<Object>();for(int i=0;i<=24;i++){var q=MagiasBaseFrieren.faixaAngAltura(k,i/24d,t);l.add(List.of(q.x(),q.y(),MagiasBaseFrieren.raioTornado(q.y())));}faixas.add(l);}
            var detritos=new ArrayList<Object>();
            for(int i=0;i<MagiasBaseFrieren.DETRITOS;i++){double y=MagiasBaseFrieren.alturaDetrito(i,t);detritos.add(List.of(MagiasBaseFrieren.anguloDetrito(i,t),y,MagiasBaseFrieren.raioTornado(y)));}
            gaiola.put(Integer.toString(t),Map.of("alpha",MagiasBaseFrieren.alphaTornado(t),"espalha",MagiasBaseFrieren.espalhamento(t),"faixas",faixas,"detritos",detritos));
        }
        raiz.put("impulso",impulso);raiz.put("gaiola",gaiola);
        Files.createDirectories(Path.of("build"));
        try(var writer=Files.newBufferedWriter(Path.of("build/frieren-base-samples.json"))){new Gson().toJson(raiz,writer);}
    }
}
