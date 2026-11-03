package com.cauansilva.arcanecontrol.core.vfx;
import com.cauansilva.arcanecontrol.core.ichigo.PerfilIchigo;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class GetsugaIchigoTest {
    private static final int V=GetsugaIchigo.VIAGEM,F=GetsugaIchigo.FIM;
    @Test void meiaLuaSegueAFrenteDeColisaoDoServidor(){
        for(int t=0;t<=V;t++)assertEquals(PerfilIchigo.alcance(t),GetsugaIchigo.distancia(t),1e-9);
        assertEquals(28,GetsugaIchigo.distancia(V),1e-9);assertEquals(V,GetsugaIchigo.parada(99),1e-9);assertEquals(5,GetsugaIchigo.parada(7),1e-9);
        assertEquals(1,GetsugaIchigo.escala(GetsugaIchigo.CRESCE),1e-9);
        assertEquals(0,GetsugaIchigo.alpha(F,V),1e-9);assertTrue(GetsugaIchigo.alpha(2,V)>.99);
        // Bankai Getsuga keeps a short wind-up so the swing reads before the release.
        assertTrue(PerfilIchigo.impacto(0,true)>=4);
        assertEquals(0,GetsugaIchigo.carga(PerfilIchigo.impacto(0,false)+1,PerfilIchigo.impacto(0,false)),1e-9);
    }
    @Test void formaFicaPresaAoCorteETerminaNoFim(){
        for(boolean b:new boolean[]{false,true})for(int i=0;i<GetsugaIchigo.PONTOS;i++){
            // Middle of the arc leads toward the target; tips trail behind it.
            var f=GetsugaIchigo.frente(i,b);assertTrue(Math.hypot(f.x(),f.y())<=GetsugaIchigo.raio(b)+1e-9&&f.z()<=1e-9);
            assertTrue(f.z()<=GetsugaIchigo.frente(GetsugaIchigo.PONTOS/2,b).z()+1e-9);
            for(double t=0;t<=F;t+=.5){var tr=GetsugaIchigo.tras(i,t,b);assertTrue(tr.z()<=f.z()+1e-9,"borda de tras atras da frente");}
        }
        // Volume: the wedge closes on the cutting edge and stays a thin solid (no flat card).
        for(boolean b:new boolean[]{false,true}){double prof=0;for(int j=0;j<GetsugaIchigo.SECAO;j++){var v=GetsugaIchigo.volume(GetsugaIchigo.PONTOS/2,j,5,b);var c=GetsugaIchigo.volume(GetsugaIchigo.PONTOS/2,0,5,b);var n=GetsugaIchigo.perpendicular();prof=Math.max(prof,Math.abs((v.x()-c.x())*n.x()+(v.y()-c.y())*n.y()));}assertTrue(prof>.4&&prof<2,"profundidade "+prof);}
        for(int k=1;k<=GetsugaIchigo.ECOS;k++)assertEquals(0,GetsugaIchigo.alphaEco(k,V,V),1e-9);
        for(double z=0;z<=28;z+=.5)assertEquals(0,GetsugaIchigo.alphaSulco(z,z/1.4+20),1e-9);
    }
    @Test void exportaParaPrevia()throws Exception{
        var out=new LinkedHashMap<String,Object>();
        for(boolean b:new boolean[]{false,true}){
            var q=new LinkedHashMap<String,Object>();int imp=PerfilIchigo.impacto(0,b);
            for(double t=-imp;t<=F;t+=.5){
                var frente=new ArrayList<Object>();var tras=new ArrayList<Object>();
                for(int i=0;i<GetsugaIchigo.PONTOS;i++){frente.add(GetsugaIchigo.frente(i,b));tras.add(GetsugaIchigo.tras(i,t,b));}
                var chamas=new ArrayList<Object>();for(int j=0;j<GetsugaIchigo.CHAMAS;j++){var l=new ArrayList<Object>();for(int u=0;u<=6;u++)l.add(GetsugaIchigo.chama(j,u/6d,t,b));chamas.add(l);}
                var lascas=new ArrayList<Object>();for(int k=0;k<GetsugaIchigo.LASCAS;k++)lascas.add(GetsugaIchigo.lasca(k,t,V,b));
                var linguas=new ArrayList<Object>();for(int j=0;j<GetsugaIchigo.LINGUAS;j++){var l=new ArrayList<Object>();for(int u=0;u<=6;u++)l.add(GetsugaIchigo.lingua(j,u/6d,t+imp,b));linguas.add(l);}
                var sulco=new ArrayList<Object>();for(int z=0;z<=56;z++)sulco.add(List.of(z*.5,GetsugaIchigo.alphaSulco(z*.5,t),GetsugaIchigo.larguraSulco(z*.5,b)));
                var ecos=new ArrayList<Object>();for(int k=1;k<=GetsugaIchigo.ECOS;k++)ecos.add(List.of(GetsugaIchigo.distanciaEco(k,t),GetsugaIchigo.alphaEco(k,t,V)));
                var m=new LinkedHashMap<String,Object>();
                m.put("d",GetsugaIchigo.distancia(t));m.put("escala",GetsugaIchigo.escala(t));m.put("alpha",GetsugaIchigo.alpha(t,V));m.put("dissolve",GetsugaIchigo.dissolve(t,V));
                m.put("carga",t<0?GetsugaIchigo.carga(t+imp,imp):0);m.put("frente",frente);m.put("tras",tras);m.put("chamas",chamas);m.put("lascas",lascas);
                var vol=new ArrayList<Object>();for(int i=0;i<GetsugaIchigo.PONTOS;i++){var l=new ArrayList<Object>();for(int j=0;j<GetsugaIchigo.SECAO;j++)l.add(List.of(GetsugaIchigo.volume(i,j,t,b),GetsugaIchigo.normal(i,j,t,b)));vol.add(l);}
                m.put("volume",vol);m.put("linguas",linguas);m.put("sulco",sulco);m.put("ecos",ecos);q.put(Double.toString(t),m);
            }
            out.put(b?"bankai":"shikai",Map.of("quadros",q,"impacto",imp));
        }
        out.put("luz",List.of(GetsugaIchigo.LX,GetsugaIchigo.LY,GetsugaIchigo.LZ));
        out.put("fim",F);out.put("viagem",V);out.put("inclinacao",GetsugaIchigo.INCLINACAO);
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/ichigo-getsuga-samples.json"))){new Gson().toJson(out,w);}
    }
}
