package com.cauansilva.arcanecontrol.core.vfx;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class FormaFrierenTest {
    @Test void colunaDeManaExplodeEAssenta(){
        assertTrue(FormaFrieren.raioColuna(8)>FormaFrieren.raioColuna(200)+1,"surto mais largo que a coluna estavel");
        assertEquals(1.25,FormaFrieren.raioColuna(400),1e-3);
        assertTrue(FormaFrieren.alturaColuna(20)>30);
        assertTrue(FormaFrieren.alphaColuna(8,800)>FormaFrieren.alphaColuna(300,500));
        assertEquals(0,FormaFrieren.alphaColuna(890,0),1e-9);
        for(double idade=0;idade<=900;idade+=1.5)for(int i=0;i<FormaFrieren.PARTICULAS;i++){
            double a=FormaFrieren.alphaParticula(i,idade);assertTrue(a>=0&&a<=1);
            assertTrue(FormaFrieren.particula(i,idade).y()>=0);
        }
    }
    @Test void julgamentoFechaNoImpactoDoServidor(){
        // MovesetsServidor aplica o dano do Julgamento em t==25.
        assertEquals(25,FormaFrieren.QUEDA);
        assertEquals(0,FormaFrieren.fechamentoAviso(FormaFrieren.QUEDA),1e-9);
        assertEquals(0,FormaFrieren.alphaRaio(FormaFrieren.QUEDA-1),1e-9);assertEquals(1,FormaFrieren.alphaRaio(FormaFrieren.QUEDA),1e-9);
        assertEquals(0,FormaFrieren.alphaRaio(FormaFrieren.JULGAMENTO),1e-9);
        for(int j=0;j<FormaFrieren.RAIOS_JULGAMENTO;j++){
            var topo=FormaFrieren.raio(j,0,30,0);var chao=FormaFrieren.raio(j,FormaFrieren.PONTOS_RAIO-1,30,0);
            assertEquals(30,topo.y(),1e-9);assertEquals(0,Math.hypot(topo.x(),topo.z()),1e-9);assertEquals(0,chao.y(),1e-9);
            assertTrue(Math.hypot(chao.x(),chao.z())<=4,"raios terminam dentro da area de dano de raio 4");
        }
        assertNotEquals(FormaFrieren.raio(1,4,30,0),FormaFrieren.raio(1,4,30,1),"relampago muda de forma");
    }
    @Test void cupulaDePaineisSobreORaioProtegido(){
        var paineis=FormaFrieren.paineis();
        assertTrue(paineis.size()>60&&paineis.size()<260,"quantidade "+paineis.size());
        for(var p:paineis){
            var c=p.centro();assertEquals(FormaFrieren.RAIO_ESCUDO,Math.sqrt(c.x()*c.x()+c.y()*c.y()+c.z()*c.z()),1e-9);
            assertEquals(0,FormaFrieren.escalaPainel(p,FormaFrieren.ESCUDO),1e-9);
        }
        var topo=paineis.get(paineis.size()-1);var baixo=paineis.get(0);
        assertTrue(FormaFrieren.escalaPainel(topo,3)>FormaFrieren.escalaPainel(baixo,3),"acende de cima para baixo");
    }
    @Test void exportaGeometriaParaPrevia()throws Exception{
        var mana=new LinkedHashMap<String,Object>();
        for(int idade=0;idade<=80;idade++){
            var part=new ArrayList<Object>();for(int i=0;i<FormaFrieren.PARTICULAS;i++)part.add(List.of(FormaFrieren.particula(i,idade),FormaFrieren.alphaParticula(i,idade)));
            var estrias=new ArrayList<Object>();for(int j=0;j<FormaFrieren.ESTRIAS;j++)estrias.add(List.of(FormaFrieren.anguloEstria(j,idade),FormaFrieren.alphaEstria(j,idade)));
            mana.put(Integer.toString(idade),Map.of("raio",FormaFrieren.raioColuna(idade),"altura",FormaFrieren.alturaColuna(idade),"alpha",FormaFrieren.alphaColuna(idade,900-idade),"particulas",part,"estrias",estrias));
        }
        var julgamento=new LinkedHashMap<String,Object>();
        for(int t=0;t<=FormaFrieren.JULGAMENTO;t++){
            var raios=new ArrayList<Object>();double var=FormaFrieren.variacaoRaio(t);
            for(int j=0;j<FormaFrieren.RAIOS_JULGAMENTO;j++){var l=new ArrayList<Object>();for(int i=0;i<FormaFrieren.PONTOS_RAIO;i++)l.add(FormaFrieren.raio(j,i,30,var+j));raios.add(l);}
            var ramos=new ArrayList<Object>();for(int k=0;k<FormaFrieren.RAMOS;k++){var l=new ArrayList<Object>();for(int i=0;i<=6;i++)l.add(FormaFrieren.ramo(k,i,t));ramos.add(l);}
            julgamento.put(Integer.toString(t),Map.of("aviso",FormaFrieren.alphaAviso(t),"fecha",FormaFrieren.fechamentoAviso(t),"carga",FormaFrieren.cargaCeu(t),
                    "alphaRaio",FormaFrieren.alphaRaio(t),"alcance",FormaFrieren.alcanceRamo(t),"clarao",FormaFrieren.clarao(t),"raios",raios,"ramos",ramos));
        }
        var paineis=FormaFrieren.paineis();var escudo=new LinkedHashMap<String,Object>();
        for(int t=0;t<=FormaFrieren.ESCUDO;t+=2){
            var l=new ArrayList<Object>();for(var p:paineis)l.add(List.of(FormaFrieren.escalaPainel(p,t),FormaFrieren.brilhoPainel(p,t)));escudo.put(Integer.toString(t),l);
        }
        var raiz=new LinkedHashMap<String,Object>();
        raiz.put("mana",mana);raiz.put("julgamento",julgamento);raiz.put("paineis",paineis);raiz.put("escudo",escudo);
        raiz.put("queda",FormaFrieren.QUEDA);raiz.put("painel",FormaFrieren.PAINEL);
        Files.createDirectories(Path.of("build"));
        try(var writer=Files.newBufferedWriter(Path.of("build/frieren-forma-samples.json"))){new Gson().toJson(raiz,writer);}
    }
}
