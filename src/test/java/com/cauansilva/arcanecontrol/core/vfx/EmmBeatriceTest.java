package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class EmmBeatriceTest {
    static final int F=EmmBeatrice.FIM;
    static double d(EmmBeatrice.P a,EmmBeatrice.P b){return Math.sqrt(Math.pow(a.x()-b.x(),2)+Math.pow(a.y()-b.y(),2)+Math.pow(a.z()-b.z(),2));}
    @Test void invulneravelUmSegundoEMeio(){
        int n=0;for(int t=0;t<F;t++)if(EmmBeatrice.invulneravel(t))n++;
        assertEquals(31,n,"30 ticks fechada + 1 de margem no fechamento");
        assertFalse(EmmBeatrice.invulneravel(EmmBeatrice.ABRE));
    }
    @Test void cascaFechaEDescasca(){
        for(int i=0;i<=EmmBeatrice.LAT;i++){
            assertEquals(1,EmmBeatrice.cobertura(i,EmmBeatrice.FECHA),1e-9,"fechada no tick FECHA");
            assertEquals(0,EmmBeatrice.cobertura(i,EmmBeatrice.ABRE+EmmBeatrice.SOLTA),1e-9,"descascou");}
        assertTrue(EmmBeatrice.cobertura(0,3)>EmmBeatrice.cobertura(EmmBeatrice.LAT/2,3),"fecha das pontas para a cintura");
        assertTrue(EmmBeatrice.cobertura(EmmBeatrice.LAT,EmmBeatrice.ABRE+4)>EmmBeatrice.cobertura(1,EmmBeatrice.ABRE+4),"descasca de cima");
        assertEquals(0,EmmBeatrice.alphaCasca(F),1e-9);
    }
    @Test void tempoParaENadaEntra(){
        var a=EmmBeatrice.congelada(3,EmmBeatrice.FECHA+2);var b=EmmBeatrice.congelada(3,EmmBeatrice.ABRE-2);
        assertTrue(d(a,b)<.05,"particulas paradas com a casca fechada");
        for(int g=0;g<EmmBeatrice.GOLPES;g++){
            var p=EmmBeatrice.projetil(g,EmmBeatrice.CHEGADA[g]+3);var dentro=EmmBeatrice.naCasca(EmmBeatrice.direcaoGolpe(g),1);
            assertTrue(d(p,EmmBeatrice.CENTRO)>d(dentro,EmmBeatrice.CENTRO),"o projetil para fora da casca");
            assertTrue(EmmBeatrice.invulneravel(EmmBeatrice.CHEGADA[g]),"golpes da previa caem com a casca fechada");
            assertEquals(0,EmmBeatrice.alphaProjetil(g,EmmBeatrice.CHEGADA[g]+EmmBeatrice.CONGELA),1e-9);}
        for(int k=0;k<EmmBeatrice.POEIRA;k++)assertTrue(d(EmmBeatrice.poeira(k,EmmBeatrice.nasce(k)+EmmBeatrice.VOLTA),EmmBeatrice.MAOS)<1e-6,"a poeira volta para as maos");
    }
    @Test void cascaDoJogoIgualAPrevia(){
        for(double t:new double[]{3,EmmBeatrice.CHEGADA[0]+2,EmmBeatrice.ABRE-5})for(int i=0;i<=EmmBeatrice.LAT;i+=3)for(int j=0;j<EmmBeatrice.LON;j+=5){
            var n=EmmBeatrice.esfera(i,j);double k=EmmBeatrice.raioBase(i,j,t);for(int g=0;g<EmmBeatrice.GOLPES;g++)k-=EmmBeatrice.amassado(n,EmmBeatrice.direcaoGolpe(g),EmmBeatrice.afunda(g,t));
            assertTrue(d(EmmBeatrice.naCasca(n,k),EmmBeatrice.casca(i,j,t))<1e-12);}
        assertEquals(0,EmmBeatrice.afundaGolpe(-1),1e-12);assertTrue(EmmBeatrice.afundaGolpe(3)>.4);
    }
    @Test void animacoes()throws Exception{
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("subaru_emm"));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.25){var m=new LinkedHashMap<String,Object>();
            var cs=new ArrayList<Object>();var nr=new ArrayList<Object>();var cb=new ArrayList<Object>();
            for(int i=0;i<=EmmBeatrice.LAT;i++){var l=new ArrayList<Object>();var ln=new ArrayList<Object>();
                for(int j=0;j<EmmBeatrice.LON;j++){l.add(EmmBeatrice.casca(i,j,t));ln.add(EmmBeatrice.normal(i,j));}
                cs.add(l);nr.add(ln);cb.add(EmmBeatrice.cobertura(i,t));}
            var vs=new ArrayList<Object>();for(int k=0;k<EmmBeatrice.VEIAS;k++){var l=new ArrayList<Object>();
                for(int i=0;i<EmmBeatrice.PONTOS_VEIA;i++){double u=(double)i/(EmmBeatrice.PONTOS_VEIA-1);l.add(List.of(EmmBeatrice.veia(k,u,t),EmmBeatrice.brilhoVeia(k,u,t)));}vs.add(l);}
            var an=new ArrayList<Object>();for(int i=0;i<EmmBeatrice.PONTOS_ANEL;i++)an.add(EmmBeatrice.anel(i,t));
            var ci=new ArrayList<Object>();for(int a=0;a<2;a++){var l=new ArrayList<Object>();for(int i=0;i<EmmBeatrice.PONTOS_CIRCULO;i++)l.add(EmmBeatrice.circulo(a,i,t));ci.add(l);}
            var fi=new ArrayList<Object>();for(int i=0;i<EmmBeatrice.PONTOS_FIO;i++)fi.add(EmmBeatrice.fio(i,t));
            var co=new ArrayList<Object>();for(int k=0;k<EmmBeatrice.CONGELADAS;k++)co.add(EmmBeatrice.congelada(k,t));
            var pr=new ArrayList<Object>();for(int g=0;g<EmmBeatrice.GOLPES;g++){var es=new ArrayList<Object>();
                for(int e=0;e<EmmBeatrice.ESTILHACOS;e++)es.add(List.of(EmmBeatrice.estilhaco(g,e,t),EmmBeatrice.tamanhoEstilhaco(g,e,t)));
                pr.add(Map.of("pos",EmmBeatrice.projetil(g,t),"alpha",EmmBeatrice.alphaProjetil(g,t),"origem",EmmBeatrice.ORIGEM[g],"estilhacos",es));}
            var po=new ArrayList<Object>();for(int k=0;k<EmmBeatrice.POEIRA;k++)po.add(List.of(EmmBeatrice.poeira(k,t),EmmBeatrice.tamanhoPoeira(k,t)));
            m.put("casca",cs);m.put("normais",nr);m.put("cobertura",cb);m.put("alphaCasca",EmmBeatrice.alphaCasca(t));
            m.put("veias",vs);m.put("anel",an);m.put("alphaAnel",EmmBeatrice.alphaAnel(t));m.put("circulo",ci);m.put("alphaCirculo",EmmBeatrice.alphaCirculo(t));
            m.put("fio",fi);m.put("alcanceFio",EmmBeatrice.alcanceFio(t));m.put("alphaFio",EmmBeatrice.alphaFio(t));
            m.put("congeladas",co);m.put("alphaCongelada",EmmBeatrice.alphaCongelada(t));m.put("projeteis",pr);m.put("poeira",po);
            m.put("invulneravel",EmmBeatrice.invulneravel(t));q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-emm-samples.json"))){new Gson().toJson(Map.of("quadros",q,"fim",F,"fecha",EmmBeatrice.FECHA,"abre",EmmBeatrice.ABRE,"solta",EmmBeatrice.SOLTA,"chegada",EmmBeatrice.CHEGADA),w);}
    }
}
