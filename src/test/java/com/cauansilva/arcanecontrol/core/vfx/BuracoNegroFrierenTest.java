package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BuracoNegroFrierenTest {
    private static final int A=PerfilFrieren.ABERTURA,C=PerfilFrieren.COLAPSO,F=PerfilFrieren.FIM;
    private static final BuracoNegroFrieren.Ponto MAO=new BuracoNegroFrieren.Ponto(-.8,-2.75,-11.2);
    private static double dist(BuracoNegroFrieren.Ponto p){return Math.sqrt(p.x()*p.x()+p.y()*p.y()+p.z()*p.z());}

    @Test void formaNaMaoEChegaAoCentroQuandoAAtracaoComeca(){
        assertEquals(MAO,BuracoNegroFrieren.posicao(MAO,0));assertEquals(MAO,BuracoNegroFrieren.posicao(MAO,BuracoNegroFrieren.LANCAMENTO));
        assertEquals(0,dist(BuracoNegroFrieren.posicao(MAO,A)),1e-9);
        double anterior=dist(MAO);
        for(double t=BuracoNegroFrieren.LANCAMENTO;t<=A;t+=.5){
            var p=BuracoNegroFrieren.posicao(MAO,t);double d=Math.hypot(p.x(),p.z());
            assertTrue(d<=anterior+1e-9,"o voo nao recua");anterior=d;
        }
        assertTrue(BuracoNegroFrieren.nucleo(A-10)<.6,"pequeno na mao como no anime");
        assertTrue(BuracoNegroFrieren.nucleo(A+12)>2,"abre no centro");
        assertTrue(BuracoNegroFrieren.emVoo(A-1)&&!BuracoNegroFrieren.emVoo(A));
    }
    @Test void cascaRachaNaTensaoEExplodeNoColapso(){
        assertEquals(0,BuracoNegroFrieren.presencaCasca(BuracoNegroFrieren.TENSAO),1e-9);
        assertTrue(BuracoNegroFrieren.presencaCasca(C-1)>.99);assertEquals(0,BuracoNegroFrieren.presencaCasca(C+3),1e-9);
        assertEquals(0,BuracoNegroFrieren.aberturaRachaduras(BuracoNegroFrieren.TENSAO+8),1e-9);
        assertEquals(BuracoNegroFrieren.PONTOS_RACHADURA-1,BuracoNegroFrieren.aberturaRachaduras(C),1e-9);
        for(int j=0;j<BuracoNegroFrieren.RACHADURAS;j++)for(int i=0;i<BuracoNegroFrieren.PONTOS_RACHADURA;i++){
            var p=BuracoNegroFrieren.rachadura(j,i);
            // Na superficie do elipsoide (x, 1.2y, z) de raio unitario.
            assertEquals(1,Math.sqrt(p.x()*p.x()+Math.pow(p.y()/1.2,2)+p.z()*p.z()),1e-9);
        }
        assertTrue(BuracoNegroFrieren.orbitaDetritos(C-1)<BuracoNegroFrieren.orbitaDetritos(150),"detritos compactam na casca");
        assertEquals(0,BuracoNegroFrieren.comprimentoEspinho(0,C-1));assertTrue(BuracoNegroFrieren.comprimentoEspinho(0,C+5)>=10);
        assertEquals(0,BuracoNegroFrieren.alcanceRelampago(C+3));assertTrue(BuracoNegroFrieren.alcanceRelampago(C+12)>7.9);
        assertEquals(0,BuracoNegroFrieren.estria(C),1e-9);
    }
    @Test void valoresLimitadosEEncerradosNoFim(){
        for(double t=0;t<=F;t+=.5){
            assertTrue(BuracoNegroFrieren.nucleo(t)>=0&&Double.isFinite(BuracoNegroFrieren.estria(t)));
            for(int k=0;k<3;k++){double a=BuracoNegroFrieren.alphaDistorcao(k,t);assertTrue(a>=0&&a<=1);}
            for(int j=0;j<BuracoNegroFrieren.LINHAS;j++)for(int i=0;i<=24;i++){double a=BuracoNegroFrieren.alphaVento(j,i/24d,t);assertTrue(a>=0&&a<=1);}
            for(int i=0;i<BuracoNegroFrieren.FAISCAS;i++){double a=BuracoNegroFrieren.alphaFaisca(i,t);assertTrue(a>=0&&a<=1);}
            for(int i=0;i<BuracoNegroFrieren.ESTILHACOS;i++){double a=BuracoNegroFrieren.alphaEstilhaco(i,t);assertTrue(a>=0&&a<=1);}
            assertTrue(BuracoNegroFrieren.alphaEspinho(t)>=0&&BuracoNegroFrieren.alphaRelampago(t)>=0);
        }
        for(int i=0;i<BuracoNegroFrieren.FAISCAS;i++)assertEquals(0,BuracoNegroFrieren.alphaFaisca(i,F),1e-9);
        for(int i=0;i<BuracoNegroFrieren.ESTILHACOS;i++)assertEquals(0,BuracoNegroFrieren.alphaEstilhaco(i,F),1e-9);
        assertEquals(0,BuracoNegroFrieren.alphaEspinho(C+20),1e-9);assertEquals(0,BuracoNegroFrieren.alphaRelampago(C+30),1e-9);
    }
    @Test void exportaGeometriaDoRuntimeParaPrevia()throws Exception{
        var quadros=new LinkedHashMap<String,Object>();
        var rachaduras=new ArrayList<Object>();
        for(int j=0;j<BuracoNegroFrieren.RACHADURAS;j++){var l=new ArrayList<Object>();for(int i=0;i<BuracoNegroFrieren.PONTOS_RACHADURA;i++)l.add(BuracoNegroFrieren.rachadura(j,i));rachaduras.add(l);}
        var relampagos=new ArrayList<Object>();
        for(int j=0;j<BuracoNegroFrieren.RELAMPAGOS;j++){var l=new ArrayList<Object>();for(int i=0;i<=8;i++)l.add(BuracoNegroFrieren.relampago(j,i));relampagos.add(l);}
        var espinhos=new ArrayList<Object>();for(int j=0;j<BuracoNegroFrieren.ESPINHOS;j++)espinhos.add(BuracoNegroFrieren.direcaoEspinho(j));
        for(int t=0;t<=F;t++){
            var m=new LinkedHashMap<String,Object>();
            var rastro=new ArrayList<Object>();for(int k=0;k<=8;k++)rastro.add(BuracoNegroFrieren.posicao(MAO,t-k*1.2));
            var ventos=new ArrayList<Object>();var ventosAlpha=new ArrayList<Object>();
            for(int j=0;j<BuracoNegroFrieren.LINHAS;j++){
                var pts=new ArrayList<Object>();var al=new ArrayList<Double>();
                for(int i=0;i<=24;i++){pts.add(BuracoNegroFrieren.linhaVento(j,i/24d,t));al.add(BuracoNegroFrieren.alphaVento(j,i/24d,t));}
                ventos.add(pts);ventosAlpha.add(al);
            }
            var distorcao=new ArrayList<Object>();for(int k=0;k<3;k++)distorcao.add(List.of(BuracoNegroFrieren.raioDistorcao(k,t),BuracoNegroFrieren.alphaDistorcao(k,t)));
            var comprimentos=new ArrayList<Double>();for(int j=0;j<BuracoNegroFrieren.ESPINHOS;j++)comprimentos.add(BuracoNegroFrieren.comprimentoEspinho(j,t));
            var faiscas=new ArrayList<Object>();for(int i=0;i<BuracoNegroFrieren.FAISCAS;i++)faiscas.add(List.of(BuracoNegroFrieren.faisca(i,t),BuracoNegroFrieren.alphaFaisca(i,t)));
            var estilhacos=new ArrayList<Object>();for(int i=0;i<BuracoNegroFrieren.ESTILHACOS;i++)estilhacos.add(List.of(BuracoNegroFrieren.estilhaco(i,t),BuracoNegroFrieren.alphaEstilhaco(i,t)));
            m.put("alpha",PerfilFrieren.opacidade(t));m.put("tensao",BuracoNegroFrieren.tensao(t));
            m.put("posicao",BuracoNegroFrieren.posicao(MAO,t));m.put("emVoo",BuracoNegroFrieren.emVoo(t));m.put("rastro",rastro);
            m.put("nucleo",BuracoNegroFrieren.nucleo(t));m.put("halo",BuracoNegroFrieren.halo(t));
            m.put("estria",BuracoNegroFrieren.estria(t));m.put("larguraEstria",BuracoNegroFrieren.larguraEstria(t));
            m.put("distorcao",distorcao);m.put("ventos",ventos);m.put("ventosAlpha",ventosAlpha);m.put("orbita",BuracoNegroFrieren.orbitaDetritos(t));
            m.put("casca",BuracoNegroFrieren.presencaCasca(t));m.put("rachaduras",BuracoNegroFrieren.aberturaRachaduras(t));
            m.put("espinhos",comprimentos);m.put("alphaEspinho",BuracoNegroFrieren.alphaEspinho(t));
            m.put("relampago",BuracoNegroFrieren.alcanceRelampago(t));m.put("alphaRelampago",BuracoNegroFrieren.alphaRelampago(t));
            m.put("faiscas",faiscas);m.put("estilhacos",estilhacos);
            quadros.put(Integer.toString(t),m);
        }
        var raiz=new LinkedHashMap<String,Object>();
        raiz.put("abertura",A);raiz.put("colapso",C);raiz.put("fim",F);raiz.put("lancamento",BuracoNegroFrieren.LANCAMENTO);raiz.put("mao",MAO);
        raiz.put("raioCasca",BuracoNegroFrieren.RAIO_CASCA);raiz.put("caminhosRachadura",rachaduras);raiz.put("caminhosRelampago",relampagos);raiz.put("direcoesEspinho",espinhos);
        raiz.put("quadros",quadros);
        Files.createDirectories(Path.of("build"));
        try(var writer=Files.newBufferedWriter(Path.of("build/frieren-buraco-samples.json"))){new Gson().toJson(raiz,writer);}
    }
}
