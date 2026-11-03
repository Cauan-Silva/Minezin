package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MugetsuIchigoTest {
    private static final int G=MugetsuIchigo.GOLPE,F=MugetsuIchigo.FIM;
    @Test void fasesECustoDaGetsugaFinal(){
        assertTrue(MugetsuIchigo.FORMA<MugetsuIchigo.LAMINA&&MugetsuIchigo.LAMINA<G&&G<F);
        assertEquals(1200,MugetsuIchigo.BLOQUEIO,"1 minuto sem poderes");
        assertEquals(G+200,F,"destransforma 10 s depois do golpe");assertTrue(MugetsuIchigo.forma(G+199));assertEquals(0,MugetsuIchigo.aura(G+10),1e-9);
        assertFalse(MugetsuIchigo.forma(MugetsuIchigo.FORMA-1));assertTrue(MugetsuIchigo.forma(G));assertFalse(MugetsuIchigo.forma(F));
        assertEquals(0,MugetsuIchigo.pulso(0),1e-9);assertEquals(90,MugetsuIchigo.pulso(40),1e-9);assertEquals(0,MugetsuIchigo.pulso(F),1e-9);
        assertEquals(0,MugetsuIchigo.alphaLinha(F),1e-9);assertEquals(0,MugetsuIchigo.aura(F),1e-9);assertTrue(MugetsuIchigo.aura(MugetsuIchigo.FORMA)>.9);
        // The cut is a hair-thin straight line from the sky to the ground, inside the damage cone.
        assertEquals(0,MugetsuIchigo.topoLinha(G),1e-9);assertEquals(MugetsuIchigo.LINHA_ALTURA,MugetsuIchigo.topoLinha(G+3),1e-9);assertTrue(MugetsuIchigo.LINHA_ALTURA>=60&&MugetsuIchigo.espessuraLinha(G)<.2);
        assertEquals(MugetsuIchigo.ALCANCE,MugetsuIchigo.frenteLinha(G+6),1e-9);assertEquals(MugetsuIchigo.espessuraLinha(G),MugetsuIchigo.espessuraLinha(G+10),1e-9);assertTrue(MugetsuIchigo.atinge(0,0,MugetsuIchigo.LINHA_Z));
    }
    @Test void linhaFicaNoConeDeDano(){
        assertTrue(MugetsuIchigo.atinge(0,0,MugetsuIchigo.LINHA_Z)&&MugetsuIchigo.atinge(0,MugetsuIchigo.ALTURA_DANO,MugetsuIchigo.LINHA_Z));
        assertTrue(MugetsuIchigo.atinge(0,5,30));assertFalse(MugetsuIchigo.atinge(0,0,-1));assertFalse(MugetsuIchigo.atinge(20,0,5));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=F;t+=.5){var m=new LinkedHashMap<String,Object>();
            var la=new ArrayList<Object>();for(int i=0;i<MugetsuIchigo.COLUNA;i++){var l=new ArrayList<Object>();for(int j=0;j<MugetsuIchigo.VOLTAS;j++)l.add(MugetsuIchigo.lamina(i,j,t));la.add(l);}
            var li=new ArrayList<Object>();for(int k=0;k<MugetsuIchigo.LINGUAS;k++){var l=new ArrayList<Object>();for(int u=0;u<=5;u++)l.add(MugetsuIchigo.linguaLamina(k,u/5d,t));li.add(l);}
            m.put("fase",MugetsuIchigo.fase(t));m.put("forma",MugetsuIchigo.forma(t));m.put("pulso",MugetsuIchigo.pulso(t));
            var lb=new ArrayList<Object>();for(int k=0;k<MugetsuIchigo.LABAREDAS;k++){var g=new ArrayList<Object>();var e=new ArrayList<Object>();for(int i=0;i<MugetsuIchigo.ANEL_CHAMA;i++){var l=new ArrayList<Object>();for(int j=0;j<MugetsuIchigo.TUBO_CHAMA;j++)l.add(MugetsuIchigo.labareda(k,i,j,t));g.add(l);e.add(MugetsuIchigo.eixoLabareda(k,i/(MugetsuIchigo.ANEL_CHAMA-1d),t));}lb.add(List.of(g,e));}
            var fr=new ArrayList<Object>();for(int k=0;k<MugetsuIchigo.FRAGMENTOS;k++)fr.add(List.of(MugetsuIchigo.fragmento(k,t),MugetsuIchigo.tamanhoFragmento(k,t)));
            m.put("aura",MugetsuIchigo.aura(t));m.put("labaredas",lb);m.put("fragmentos",fr);m.put("alphaLinha",MugetsuIchigo.alphaLinha(t));m.put("topoLinha",MugetsuIchigo.topoLinha(t));m.put("frenteLinha",MugetsuIchigo.frenteLinha(t));m.put("espessura",MugetsuIchigo.espessuraLinha(t));m.put("halo",MugetsuIchigo.halo(t));m.put("alphaLamina",MugetsuIchigo.alphaLamina(t));m.put("comprimento",MugetsuIchigo.comprimento(t));m.put("lamina",la);m.put("linguas",li);
            q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/ichigo-mugetsu-samples.json"))){new Gson().toJson(Map.of("quadros",q,"golpe",G,"fim",F,"forma",MugetsuIchigo.FORMA,"linhaZ",MugetsuIchigo.LINHA_Z,"linhaAltura",MugetsuIchigo.LINHA_ALTURA),w);}
    }
}
