package com.cauansilva.arcanecontrol.core.vfx;
import com.cauansilva.arcanecontrol.core.ichigo.PerfilIchigo;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class KitBaseIchigoTest {
    @Test void ondaChegaAoRaioDeDanoESome(){
        assertEquals(8,KitBaseIchigo.RAIO_DANO,1e-9);assertEquals(8,KitBaseIchigo.raioOnda(5),1e-9);
        for(int i=0;i<KitBaseIchigo.ANEL;i++)for(int j=0;j<KitBaseIchigo.SECAO_ANEL;j++){var p=KitBaseIchigo.onda(i,j,5);assertTrue(Math.hypot(p.x(),p.z())<=8.5&&p.y()>=0);}
        assertEquals(0,KitBaseIchigo.alphaOnda(KitBaseIchigo.ONDA_FIM),1e-9);assertEquals(0,KitBaseIchigo.alphaRachadura(KitBaseIchigo.ONDA_FIM),1e-9);
        for(int k=0;k<KitBaseIchigo.DETRITOS;k++)assertEquals(0,KitBaseIchigo.tamanhoDetrito(k,KitBaseIchigo.ONDA_FIM),1e-9);
        for(int k=0;k<KitBaseIchigo.FUMACAS;k++)assertEquals(0,KitBaseIchigo.alphaFumaca(k,KitBaseIchigo.ONDA_FIM),1e-9);
    }
    @Test void corteFicaDentroDoAlcanceEmVolume(){
        double prof=0;
        for(double t=0;t<=KitBaseIchigo.CORTE_FIM;t+=.5)for(int i=0;i<KitBaseIchigo.ARCO;i++)for(int j=0;j<KitBaseIchigo.SECAO;j++){
            var p=KitBaseIchigo.corte(i,j,t);assertTrue(Math.hypot(p.y(),p.z())<=KitBaseIchigo.ALCANCE_CORTE);prof=Math.max(prof,Math.abs(p.x()));}
        assertTrue(prof>.1,"corte tem espessura lateral");
        assertEquals(Math.toRadians(85),KitBaseIchigo.pontaCorte(3),1e-9);assertEquals(0,KitBaseIchigo.alphaCorte(KitBaseIchigo.CORTE_FIM),1e-9);
        assertEquals(10,PerfilIchigo.impacto(3,false));assertEquals(12,PerfilIchigo.impacto(2,false));
    }
    @Test void lancamentoSome(){
        assertTrue(KitBaseIchigo.IMPULSO>=1.2);assertEquals(0,KitBaseIchigo.alphaEspiral(KitBaseIchigo.LANCA_FIM),1e-9);
        for(int k=0;k<KitBaseIchigo.ESTILHACOS;k++)assertEquals(0,KitBaseIchigo.tamanhoEstilhaco(k,12),1e-9);
        for(int k=0;k<KitBaseIchigo.ECOS;k++)for(int f=0;f<KitBaseIchigo.FLOCOS;f++)assertEquals(0,KitBaseIchigo.tamanhoFloco(k,f,KitBaseIchigo.SHUNPO_FIM+k),1e-9);
    }
    @Test void shunpoSome(){
        for(int k=0;k<KitBaseIchigo.ECOS;k++){assertTrue(KitBaseIchigo.ecoU(k)<1);assertTrue(KitBaseIchigo.alphaEco(k,KitBaseIchigo.SHUNPO_FIM)<.01);}
        assertEquals(0,KitBaseIchigo.alphaRisco(KitBaseIchigo.SHUNPO_FIM),1e-9);
    }
    @Test void exportaParaPrevia()throws Exception{
        var out=new LinkedHashMap<String,Object>();
        var sh=new LinkedHashMap<String,Object>();
        for(double t=0;t<=KitBaseIchigo.SHUNPO_FIM;t+=.5){var m=new LinkedHashMap<String,Object>();
            var ecos=new ArrayList<Object>();for(int k=0;k<KitBaseIchigo.ECOS;k++)ecos.add(List.of(KitBaseIchigo.ecoU(k),KitBaseIchigo.alphaEco(k,t)));
            var riscos=new ArrayList<Object>();for(int j=0;j<KitBaseIchigo.RISCOS;j++)riscos.add(List.of(KitBaseIchigo.riscoOffset(j),KitBaseIchigo.riscoU0(j,t),KitBaseIchigo.riscoU1(j,t)));
            var po=new ArrayList<Object>();for(int k=0;k<KitBaseIchigo.POEIRA;k++)po.add(List.of(KitBaseIchigo.poeira(k,t,false),KitBaseIchigo.poeira(k,t,true),KitBaseIchigo.tamanhoPoeira(k,t)));
            var aneis=new ArrayList<Object>();for(boolean ch:new boolean[]{false,true}){var g=new ArrayList<Object>();for(int i=0;i<=KitBaseIchigo.ANEL_SHUNPO;i++){var l=new ArrayList<Object>();for(int j=0;j<KitBaseIchigo.TUBO;j++)l.add(KitBaseIchigo.anelAr(i%KitBaseIchigo.ANEL_SHUNPO,j,t,ch));g.add(l);}aneis.add(List.of(g,KitBaseIchigo.alphaAnelAr(t,ch)));}
            var hel=new ArrayList<Object>();for(int h=0;h<KitBaseIchigo.HELICES;h++){var l=new ArrayList<Object>();double u0=KitBaseIchigo.riscoU0(h,t),u1=KitBaseIchigo.riscoU1(h,t);for(int i=0;i<=KitBaseIchigo.PONTOS_HELICE;i++){double u=u0+(u1-u0)*i/KitBaseIchigo.PONTOS_HELICE;l.add(List.of(u,KitBaseIchigo.helice(h,u)));}hel.add(l);}
            var fl=new ArrayList<Object>();for(int k=0;k<KitBaseIchigo.ECOS;k++)for(int f=0;f<KitBaseIchigo.FLOCOS;f++)fl.add(List.of(KitBaseIchigo.ecoU(k),KitBaseIchigo.floco(k,f,t),KitBaseIchigo.tamanhoFloco(k,f,t)));
            m.put("aneis",aneis);m.put("helices",hel);m.put("flocos",fl);
            m.put("ecos",ecos);m.put("riscos",riscos);m.put("alphaRisco",KitBaseIchigo.alphaRisco(t));m.put("poeira",po);sh.put(Double.toString(t),m);}
        var on=new LinkedHashMap<String,Object>();
        for(double t=0;t<=KitBaseIchigo.ONDA_FIM;t+=.5){var m=new LinkedHashMap<String,Object>();
            var g=new ArrayList<Object>();for(int i=0;i<=KitBaseIchigo.ANEL;i++){var l=new ArrayList<Object>();for(int j=0;j<KitBaseIchigo.SECAO_ANEL;j++)l.add(KitBaseIchigo.onda(i%KitBaseIchigo.ANEL,j,t));g.add(l);}
            var ra=new ArrayList<Object>();for(int j=0;j<KitBaseIchigo.RACHADURAS;j++){var l=new ArrayList<Object>();for(int i=0;i<KitBaseIchigo.PONTOS_RACHADURA;i++)l.add(List.of(KitBaseIchigo.rachadura(j,i,t),KitBaseIchigo.larguraRachadura(i)));ra.add(l);}
            var de=new ArrayList<Object>();for(int k=0;k<KitBaseIchigo.DETRITOS;k++)de.add(List.of(KitBaseIchigo.detrito(k,t),KitBaseIchigo.tamanhoDetrito(k,t)));
            m.put("onda",g);m.put("altura",KitBaseIchigo.alturaOnda(t));m.put("alpha",KitBaseIchigo.alphaOnda(t));m.put("rachaduras",ra);m.put("alphaRachadura",KitBaseIchigo.alphaRachadura(t));
            var fu=new ArrayList<Object>();for(int k=0;k<KitBaseIchigo.FUMACAS;k++){var gf=new ArrayList<Object>();for(int i=0;i<KitBaseIchigo.LAT;i++){var l=new ArrayList<Object>();for(int j=0;j<KitBaseIchigo.LON;j++)l.add(KitBaseIchigo.bolha(k,i,j,t));gf.add(l);}fu.add(List.of(gf,KitBaseIchigo.alphaFumaca(k,t),KitBaseIchigo.raioFumaca(k,t)));}
            m.put("fumaca",fu);m.put("detritos",de);m.put("clarao",KitBaseIchigo.clarao(t));m.put("alphaClarao",KitBaseIchigo.alphaClarao(t));on.put(Double.toString(t),m);}
        var co=new LinkedHashMap<String,Object>();
        for(double t=0;t<=KitBaseIchigo.CORTE_FIM;t+=.5){var m=new LinkedHashMap<String,Object>();var vols=new ArrayList<Object>();
            for(int k=0;k<=KitBaseIchigo.ECOS_CORTE;k++){double te=KitBaseIchigo.ecoCorte(k,t);if(te<0){vols.add(List.of());continue;}
                var g=new ArrayList<Object>();for(int i=0;i<KitBaseIchigo.ARCO;i++){var l=new ArrayList<Object>();for(int j=0;j<KitBaseIchigo.SECAO;j++)l.add(KitBaseIchigo.corte(i,j,te));g.add(l);}vols.add(g);}
            m.put("volumes",vols);m.put("alpha",KitBaseIchigo.alphaCorte(t));co.put(Double.toString(t),m);}
        var la=new LinkedHashMap<String,Object>();
        for(double t=0;t<=KitBaseIchigo.LANCA_FIM;t+=.5){var m=new LinkedHashMap<String,Object>();var es=new ArrayList<Object>();
            for(int e=0;e<KitBaseIchigo.ESPIRAIS;e++){var l=new ArrayList<Object>();for(int i=0;i<KitBaseIchigo.PONTOS_ESPIRAL;i++)l.add(KitBaseIchigo.espiral(e,i/(KitBaseIchigo.PONTOS_ESPIRAL-1d),t));es.add(l);}
            var et=new ArrayList<Object>();for(int k=0;k<KitBaseIchigo.ESTILHACOS;k++)et.add(List.of(KitBaseIchigo.estilhaco(k,t),KitBaseIchigo.tamanhoEstilhaco(k,t)));
            m.put("espirais",es);m.put("alpha",KitBaseIchigo.alphaEspiral(t));m.put("estilhacos",et);la.put(Double.toString(t),m);}
        out.put("lancamento",la);out.put("impulso",KitBaseIchigo.IMPULSO);
        out.put("shunpo",sh);out.put("onda",on);out.put("corte",co);
        out.put("impactoC",PerfilIchigo.impacto(2,false));out.put("impactoV",PerfilIchigo.impacto(3,false));out.put("investida",KitBaseIchigo.INVESTIDA);
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/ichigo-kit-base-samples.json"))){new Gson().toJson(out,w);}
    }
}
