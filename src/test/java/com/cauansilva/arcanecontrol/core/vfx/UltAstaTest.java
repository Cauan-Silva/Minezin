package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class UltAstaTest {
    private static final int D=UltAsta.DURACAO;
    private static double dist(UltAsta.P a,UltAsta.P b){return Math.sqrt(Math.pow(a.x()-b.x(),2)+Math.pow(a.y()-b.y(),2)+Math.pow(a.z()-b.z(),2));}
    @Test void cadaCorteFechaNoSeuTickECabeNaArea(){
        for(int j=0;j<UltAsta.CORTES;j++){
            int tc=UltAsta.tempoCorte(j);assertEquals(1,UltAsta.progresso(j,tc),1e-9);assertTrue(UltAsta.progresso(j,tc-1)<1);
            assertEquals(0,UltAsta.alphaCorte(j,tc-3.5),1e-9);assertEquals(0,UltAsta.alphaCorte(j,tc+7),1e-9);
            assertTrue(tc>UltAsta.SAIDA_ESPADA&&tc<UltAsta.FINAL);
            for(int i=0;i<=12;i++)for(int v=0;v<=2;v++)for(int lado=-1;lado<=1;lado+=2)
                assertTrue(dist(UltAsta.corte(j,i/12d,v/2d,lado,tc),UltAsta.centroCorte(j))<=3.0,"corte "+j);
        }
        assertEquals(1,UltAsta.progressoX(UltAsta.FINAL),1e-9);
        for(int k=0;k<2;k++)for(int i=0;i<=12;i++)for(int v=0;v<=2;v++)assertTrue(dist(UltAsta.cruz(k,i/12d,v/2d,1,UltAsta.FINAL),UltAsta.centroX())<=7.0);
    }
    @Test void cortesTravadosNoAlvoSaoAnunciadosAntesECabemNaArea(){
        // Locked on a target: the server announces each cut 3 ticks early (full sweep visible) and hits on its tick.
        var alvo=new UltAsta.P(3,1.1,9);
        for(int j=0;j<=UltAsta.CORTES;j++){int hit=j<UltAsta.CORTES?UltAsta.tempoCorte(j):UltAsta.FINAL;assertEquals(hit-3,UltAsta.anuncio(j));}
        for(int j=0;j<UltAsta.CORTES;j++)for(int i=0;i<=12;i++)for(int v=0;v<=2;v++)
            assertTrue(dist(UltAsta.corte(j,alvo,i/12d,v/2d,1,UltAsta.tempoCorte(j)),alvo)<=3.0);
        for(int i=0;i<=12;i++)assertTrue(dist(UltAsta.cruz(0,alvo,i/12d,1,1,UltAsta.FINAL),alvo)<=UltAsta.ALCANCE_X);
        assertTrue(UltAsta.PARADA<3,"the dash stops inside the cut radius");
    }
    @Test void cortesTemVolume(){
        // The two faces separate in the middle of the blade and meet at its edges: a solid, not a sheet.
        var a=UltAsta.corte(0,.5,.5,1,UltAsta.tempoCorte(0));var b=UltAsta.corte(0,.5,.5,-1,UltAsta.tempoCorte(0));
        assertTrue(dist(a,b)>=UltAsta.ESPESSURA_CORTE*.9);
        assertEquals(0,dist(UltAsta.corte(0,.5,0,1,UltAsta.tempoCorte(0)),UltAsta.corte(0,.5,0,-1,UltAsta.tempoCorte(0))),1e-9);
    }
    @Test void espadaSaiDoLivroAntesDosCortesEVolta(){
        assertEquals(0,UltAsta.espadaEsquerda(UltAsta.SAIDA_ESPADA-1),1e-9);assertEquals(1,UltAsta.emergir(UltAsta.SAIDA_ESPADA),1e-9);
        assertTrue(UltAsta.abrirLivro(10)>.99);assertEquals(0,UltAsta.espadaEsquerda(D),1e-9);assertEquals(0,UltAsta.abrirLivro(D),1e-9);
        assertEquals(0,UltAsta.alphaX(D),1e-9);
    }
    @Test void cameraNuncaEntraNoCorpo(){
        for(double t=0;t<=D;t+=.5){var q=UltAsta.quadro(t);var corpo=new UltAsta.P(0,1,UltAsta.avanco(t));assertTrue(dist(q.camera(),corpo)>1.5,"tick "+t);}
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(int n=0;n<=D*2;n++){
            double t=n/2.0;var m=new LinkedHashMap<String,Object>();
            var cortes=new ArrayList<Object>();
            for(int j=0;j<UltAsta.CORTES;j++){if(UltAsta.alphaCorte(j,t)<.01||UltAsta.progresso(j,t)<.01)continue;cortes.add(malha(j,t,false));}
            var xs=new ArrayList<Object>();if(UltAsta.alphaX(t)>.01&&UltAsta.progressoX(t)>.01)for(int k=0;k<2;k++)xs.add(malha(k,t,true));
            var linguas=new ArrayList<Object>();if(UltAsta.explosao(t)>.01)for(int k=0;k<UltAsta.LINGUAS;k++){var l=new ArrayList<Object>();for(int i=0;i<=8;i++)l.add(UltAsta.lingua(k,i/8d,t));linguas.add(l);}
            var cam=UltAsta.quadro(t);
            m.put("avanco",UltAsta.avanco(t));m.put("livro",UltAsta.abrirLivro(t));m.put("emergir",UltAsta.emergir(t));m.put("esquerda",UltAsta.espadaEsquerda(t));m.put("explosao",UltAsta.explosao(t));
            m.put("alphaX",UltAsta.alphaX(t));m.put("cortes",cortes);m.put("x",xs);m.put("linguas",linguas);m.put("camera",List.of(cam.camera(),cam.alvo(),cam.fov()));
            q.put(Double.toString(t),m);
        }
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/asta-ult-samples.json"))){new Gson().toJson(Map.of("quadros",q,"duracao",D,"livro",UltAsta.LIVRO,"final",UltAsta.FINAL),w);}
    }
    /** Grid [face][f][v] of one slash (or one arm of the X) plus its alpha. */
    private static Map<String,Object> malha(int j,double t,boolean x){
        var faces=new ArrayList<Object>();
        for(int lado=-1;lado<=1;lado+=2){var g=new ArrayList<Object>();for(int i=0;i<=16;i++){var l=new ArrayList<Object>();for(int v=0;v<=4;v++)l.add(x?UltAsta.cruz(j,i/16d,v/4d,lado,t):UltAsta.corte(j,i/16d,v/4d,lado,t));g.add(l);}faces.add(g);}
        return Map.of("faces",faces,"alpha",x?UltAsta.alphaX(t):UltAsta.alphaCorte(j,t));
    }
}
