package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class EscudoSubaruTest {
    static final double Q=60,SEM=Double.POSITIVE_INFINITY;
    static double dist(EscudoSubaru.P a,EscudoSubaru.P b){return Math.sqrt(Math.pow(a.x()-b.x(),2)+Math.pow(a.y()-b.y(),2)+Math.pow(a.z()-b.z(),2));}
    @Test void espiritosPousamNaBolha(){
        for(int k=0;k<EscudoSubaru.ESPIRITOS;k++){
            double ini=Math.abs(dist(EscudoSubaru.espirito(k,1,0),EscudoSubaru.CENTRO)-EscudoSubaru.RAIO);
            double fim=Math.abs(dist(EscudoSubaru.espirito(k,1,EscudoSubaru.INVOCA),EscudoSubaru.CENTRO)-EscudoSubaru.RAIO);
            assertTrue(fim<ini&&fim<.45,"espirito "+k+": "+ini+" -> "+fim);}
        assertEquals(0,EscudoSubaru.alphaEspiritos(EscudoSubaru.INVOCA+1),1e-9);
    }
    @Test void bolhaDuraEEstouraNoGolpe(){
        assertEquals(0,EscudoSubaru.escala(0,SEM),1e-9);assertEquals(1,EscudoSubaru.escala(30,SEM),1e-6);
        assertEquals(0,EscudoSubaru.escala(EscudoSubaru.DURACAO+8,SEM),1e-9,"some no fim da duracao");
        assertEquals(0,EscudoSubaru.escala(Q,Q),1e-9,"estoura no golpe");assertTrue(EscudoSubaru.tamanhoFragmento(0,Q+1,Q)>0);
        assertEquals(0,EscudoSubaru.tamanhoFragmento(0,Q+EscudoSubaru.ESTOURO,Q),1e-9);
        assertEquals(0,EscudoSubaru.alphaRunas(Q+6,Q),1e-9);
        // a casca amassa perto do ponto atingido durante a onda
        double minR=9;for(int i=0;i<=EscudoSubaru.LAT;i++)for(int j=0;j<EscudoSubaru.LON;j++){var p=EscudoSubaru.bolha(i,j,Q-3,Q);assertTrue(Double.isFinite(p.x()+p.y()+p.z()));minR=Math.min(minR,dist(p,EscudoSubaru.CENTRO));}
        assertTrue(minR<EscudoSubaru.RAIO*.93,"onda amassa a casca: "+minR);
    }
    @Test void animacaoDoEscudo()throws Exception{
        var j=com.google.gson.JsonParser.parseString(new String(getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/subaru.json").readAllBytes())).getAsJsonObject();
        assertTrue(j.getAsJsonObject("animations").has("subaru_escudo"));
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(double t=0;t<=Q+EscudoSubaru.ESTOURO+6;t+=.5){var m=new LinkedHashMap<String,Object>();
            var b=new ArrayList<Object>();for(int i=0;i<=EscudoSubaru.LAT;i++){var l=new ArrayList<Object>();for(int j=0;j<EscudoSubaru.LON;j++)l.add(EscudoSubaru.bolha(i,j,t,Q));b.add(l);}
            var n=new ArrayList<Object>();for(int i=0;i<=EscudoSubaru.LAT;i++){var l=new ArrayList<Object>();for(int j=0;j<EscudoSubaru.LON;j++)l.add(EscudoSubaru.normal(i,j));n.add(l);}
            var e=new ArrayList<Object>();for(int k=0;k<EscudoSubaru.ESPIRITOS;k++){var l=new ArrayList<Object>();for(int u=0;u<EscudoSubaru.PONTOS_ESPIRITO;u++)l.add(EscudoSubaru.espirito(k,u/(EscudoSubaru.PONTOS_ESPIRITO-1d),t));e.add(l);}
            var r=new ArrayList<Object>();for(int a=0;a<EscudoSubaru.RUNAS;a++){var l=new ArrayList<Object>();for(int i=0;i<EscudoSubaru.PONTOS_RUNA;i++)l.add(EscudoSubaru.runa(a,i,t));r.add(l);}
            var f=new ArrayList<Object>();for(int k=0;k<EscudoSubaru.FRAGMENTOS;k++)f.add(List.of(EscudoSubaru.fragmento(k,t,Q),EscudoSubaru.tamanhoFragmento(k,t,Q)));
            m.put("bolha",b);m.put("normais",n);m.put("alphaBolha",EscudoSubaru.alphaBolha(t,Q));m.put("clarao",EscudoSubaru.clarao(t,Q));
            m.put("espiritos",e);m.put("alphaEspiritos",EscudoSubaru.alphaEspiritos(t));m.put("runas",r);m.put("alphaRunas",EscudoSubaru.alphaRunas(t,Q));
            m.put("fragmentos",f);m.put("alphaOnda",EscudoSubaru.alphaOnda(t,Q));m.put("anguloOnda",EscudoSubaru.anguloOnda(t,Q));
            q.put(Double.toString(t),m);}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/subaru-escudo-samples.json"))){new Gson().toJson(Map.of("quadros",q,"invoca",EscudoSubaru.INVOCA,"quebra",Q,"toque",EscudoSubaru.toque(Q),"fim",Q+EscudoSubaru.ESTOURO+6,"golpe",EscudoSubaru.GOLPE,"centro",EscudoSubaru.CENTRO),w);}
    }
}
