package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class SamakSubaruTest {
    private static final int C=SamakSubaru.CARGA,I=SamakSubaru.IMPACTO,F=SamakSubaru.FIM;
    private static boolean finito(SamakSubaru.P p){return Double.isFinite(p.x()+p.y()+p.z());}
    @Test void temposEAlcance(){
        assertTrue(0<C&&C<I&&I<F);
        assertEquals(60,F-I,"cegueira e confusao por 3 s depois do acerto");
        assertEquals(SamakSubaru.ALCANCE,SamakSubaru.centro(I).z(),1e-9,"o orbe chega no alvo no tick do acerto");
        assertEquals(0,SamakSubaru.centro(I).x(),1e-9);assertEquals(0,SamakSubaru.centro(I).y(),1e-9);
        assertEquals(SamakSubaru.MAO.z(),SamakSubaru.centro(C).z(),1e-9,"sai da palma no disparo");
        for(int t=C;t<I;t++)assertTrue(SamakSubaru.centro(t+1).z()>SamakSubaru.centro(t).z(),"avanca sempre");
        // Accelerates: the last tick covers more ground than the first.
        assertTrue(SamakSubaru.centro(I).z()-SamakSubaru.centro(I-1).z()>SamakSubaru.centro(C+1).z()-SamakSubaru.centro(C).z());
    }
    @Test void orbeNasceNaPalmaESomeNoAcerto(){
        for(int t=1;t<C;t++)assertTrue(SamakSubaru.escalaOrbe(t)>=SamakSubaru.escalaOrbe(t-1),"cresce na carga");
        assertEquals(1,SamakSubaru.escalaOrbe(C+1),1e-9);assertEquals(0,SamakSubaru.escalaOrbe(I),1e-9);assertEquals(0,SamakSubaru.alphaOrbe(I),1e-9);
        for(int t=0;t<I;t++){var c=SamakSubaru.centro(t);
            for(int i=0;i<=SamakSubaru.LAT;i++)for(int j=0;j<SamakSubaru.LON;j++){var p=SamakSubaru.orbe(i,j,t);assertTrue(finito(p));
                double d=Math.sqrt(Math.pow(p.x()-c.x(),2)+Math.pow(p.y()-c.y(),2)+Math.pow(p.z()-c.z(),2));assertTrue(d<=SamakSubaru.RAIO_ORBE*2.5,"orbe pequeno: "+d);}}
    }
    @Test void nevoaFicaEmVoltaDoAlvoESome(){
        assertEquals(0,SamakSubaru.alphaNevoa(I-1),1e-9);assertTrue(SamakSubaru.alphaNevoa(I+10)>.9);assertEquals(0,SamakSubaru.alphaNevoa(F),1e-9);
        assertEquals(0,SamakSubaru.alphaAneis(F),1e-9);assertEquals(0,SamakSubaru.alphaOnda(F),1e-9);assertEquals(0,SamakSubaru.alphaRastro(I+2),1e-9);
        for(int t=I;t<=F;t++)for(int k=0;k<SamakSubaru.BOLHAS;k++){var p=SamakSubaru.bolha(k,t);assertTrue(finito(p));
            double d=Math.sqrt(Math.pow(p.x()-SamakSubaru.ALVO.x(),2)+Math.pow(p.y()-SamakSubaru.ALVO.y(),2)+Math.pow(p.z()-SamakSubaru.ALVO.z(),2));
            assertTrue(d<=SamakSubaru.RAIO_NEVOA*1.2,"bolha dentro da nevoa: "+d);assertTrue(SamakSubaru.tamanhoBolha(k,t)>=0);}
        for(int a=0;a<SamakSubaru.ANEIS;a++)for(int i=0;i<SamakSubaru.PONTOS_ANEL;i++)assertTrue(finito(SamakSubaru.anel(a,i,I+20)));
    }
    @Test void fiosConvergemNaPalma(){
        for(int k=0;k<SamakSubaru.FIOS;k++){
            double inicio=dist(SamakSubaru.fio(k,1,0),SamakSubaru.MAO),fim=dist(SamakSubaru.fio(k,1,C),SamakSubaru.MAO);
            assertTrue(fim<inicio&&fim<.2,"fio "+k+" chega na palma: "+inicio+" -> "+fim);
        }
        assertEquals(0,SamakSubaru.alphaFios(C+3),1e-9);
    }
    @Test void camadasDeDetalheFicamPresasAoOrbeEAoAlvo(){
        for(int t=0;t<I;t++){var c=SamakSubaru.centro(t);
            for(int i=0;i<=SamakSubaru.LAT;i++)for(int j=0;j<SamakSubaru.LON;j++){var p=SamakSubaru.casca(i,j,t);assertTrue(finito(p));assertTrue(dist(p,c)<=SamakSubaru.RAIO_ORBE*3*(1+SamakSubaru.estica(t)),"casca perto do nucleo");}
            for(int k=0;k<SamakSubaru.TENTACULOS;k++)for(int u=0;u<SamakSubaru.PONTOS_TENTACULO;u++){var p=SamakSubaru.tentaculo(k,u/(SamakSubaru.PONTOS_TENTACULO-1d),t);assertTrue(finito(p));assertTrue(dist(p,c)<2.2,"tentaculo preso ao orbe");}}
        assertEquals(0,SamakSubaru.alphaTentaculos(I),1e-9);
        for(int k=0;k<SamakSubaru.PARTICULAS;k++){assertTrue(finito(SamakSubaru.particula(k,3)));assertEquals(0,SamakSubaru.tamanhoParticula(k,I+4),1e-9);}
        for(int t=I;t<=F;t++)for(int k=0;k<SamakSubaru.ENLACES;k++)for(int i=0;i<SamakSubaru.PONTOS_ENLACE;i++)assertTrue(dist(SamakSubaru.enlace(k,i,t),SamakSubaru.ALVO)<1,"enlace na cabeca");
        assertEquals(0,SamakSubaru.alphaEnlaces(F),1e-9);assertEquals(0,SamakSubaru.alphaChao(F),1e-9);
    }
    private static double dist(SamakSubaru.P a,SamakSubaru.P b){return Math.sqrt(Math.pow(a.x()-b.x(),2)+Math.pow(a.y()-b.y(),2)+Math.pow(a.z()-b.z(),2));}
    /** subaru_shamak (player_animation/subaru.json) loads in the real Player Animator parser, ends after the orb lands
     *  and has the thrust keyed on the release tick. */
    @Test void animacaoDoCorpoPresaAoDisparo()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json")){
            assertNotNull(in);var l=dev.kosmx.playerAnim.core.data.gson.AnimationSerializing.deserializeAnimation(in);assertEquals(10,l.size());
            assertTrue(l.stream().allMatch(a->!a.isInfinite&&a.endTick<=Math.max(F,UlMinyaBeatrice.FIM)),"clipes finitos");assertTrue(l.stream().anyMatch(a->a.endTick>=I),"o Shamak termina depois do acerto");
        }
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        var braco=j.getAsJsonObject("animations").getAsJsonObject("subaru_shamak").getAsJsonObject("bones").getAsJsonObject("right_arm").getAsJsonObject("rotation");
        assertTrue(braco.has(String.valueOf(C/20.0)),"tranco do braco no tick do disparo");
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.5){var m=new LinkedHashMap<String,Object>();
            var orbe=new ArrayList<Object>();var norm=new ArrayList<Object>();
            for(int i=0;i<=SamakSubaru.LAT;i++){var l=new ArrayList<Object>();for(int j=0;j<SamakSubaru.LON;j++)l.add(SamakSubaru.orbe(i,j,t));orbe.add(l);}
            for(int i=0;i<=SamakSubaru.LAT;i++){var l=new ArrayList<Object>();for(int j=0;j<SamakSubaru.LON;j++)l.add(SamakSubaru.normal(i,j));norm.add(l);}
            var fios=new ArrayList<Object>();for(int k=0;k<SamakSubaru.FIOS;k++){var l=new ArrayList<Object>();for(int u=0;u<SamakSubaru.PONTOS_FIO;u++)l.add(SamakSubaru.fio(k,u/(SamakSubaru.PONTOS_FIO-1d),t));fios.add(l);}
            var rastros=new ArrayList<Object>();for(int k=0;k<SamakSubaru.RASTROS;k++){var l=new ArrayList<Object>();for(int u=0;u<SamakSubaru.PONTOS_RASTRO;u++)l.add(SamakSubaru.rastro(k,u/(SamakSubaru.PONTOS_RASTRO-1d),t));rastros.add(l);}
            var bolhas=new ArrayList<Object>();for(int k=0;k<SamakSubaru.BOLHAS;k++)bolhas.add(List.of(SamakSubaru.bolha(k,t),SamakSubaru.tamanhoBolha(k,t)));
            var aneis=new ArrayList<Object>();for(int a=0;a<SamakSubaru.ANEIS;a++){var l=new ArrayList<Object>();for(int i=0;i<SamakSubaru.PONTOS_ANEL;i++)l.add(SamakSubaru.anel(a,i,t));aneis.add(l);}
            m.put("orbe",orbe);m.put("normais",norm);m.put("alphaOrbe",SamakSubaru.alphaOrbe(t));m.put("fios",fios);m.put("alphaFios",SamakSubaru.alphaFios(t));
            m.put("rastros",rastros);m.put("alphaRastro",SamakSubaru.alphaRastro(t));m.put("bolhas",bolhas);m.put("alphaNevoa",SamakSubaru.alphaNevoa(t));
            m.put("raioOnda",SamakSubaru.raioOnda(t));m.put("alphaOnda",SamakSubaru.alphaOnda(t));m.put("aneis",aneis);m.put("alphaAneis",SamakSubaru.alphaAneis(t));
            var casca=new ArrayList<Object>();for(int i=0;i<=SamakSubaru.LAT;i++){var l=new ArrayList<Object>();for(int j=0;j<SamakSubaru.LON;j++)l.add(SamakSubaru.casca(i,j,t));casca.add(l);}
            var tent=new ArrayList<Object>();for(int k=0;k<SamakSubaru.TENTACULOS;k++){var l=new ArrayList<Object>();for(int u=0;u<SamakSubaru.PONTOS_TENTACULO;u++)l.add(SamakSubaru.tentaculo(k,u/(SamakSubaru.PONTOS_TENTACULO-1d),t));tent.add(l);}
            var part=new ArrayList<Object>();for(int k=0;k<SamakSubaru.PARTICULAS;k++)part.add(List.of(SamakSubaru.particula(k,t),SamakSubaru.tamanhoParticula(k,t)));
            var enl=new ArrayList<Object>();for(int k=0;k<SamakSubaru.ENLACES;k++){var l=new ArrayList<Object>();for(int i=0;i<SamakSubaru.PONTOS_ENLACE;i++)l.add(SamakSubaru.enlace(k,i,t));enl.add(l);}
            m.put("casca",casca);m.put("tentaculos",tent);m.put("alphaTentaculos",SamakSubaru.alphaTentaculos(t));m.put("particulas",part);
            m.put("enlaces",enl);m.put("alphaEnlaces",SamakSubaru.alphaEnlaces(t));m.put("raioChao",SamakSubaru.raioChao(t));m.put("alphaChao",SamakSubaru.alphaChao(t));
            q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-shamak-samples.json"))){new Gson().toJson(Map.of("quadros",q,"carga",C,"impacto",I,"fim",F,"alcance",SamakSubaru.ALCANCE,"mao",SamakSubaru.MAO,"alvo",SamakSubaru.ALVO),w);}
    }
}
