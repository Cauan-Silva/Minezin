package com.cauansilva.arcanecontrol.core.vfx;
import com.cauansilva.arcanecontrol.core.asta.PerfilAsta;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BlackDividerAstaTest {
    private static final int C=BlackDividerAsta.CORTE,F=BlackDividerAsta.FIM;
    @Test void corteAconteceNoTickDoDanoDoServidor(){
        assertEquals(PerfilAsta.impacto(2,true),C);
        assertEquals(0,BlackDividerAsta.fenda(C-1),1e-9);assertEquals(0,BlackDividerAsta.carga(C),1e-9);
        assertEquals(BlackDividerAsta.ALCANCE,BlackDividerAsta.fenda(C+4),1e-9);
        assertTrue(BlackDividerAsta.carga(C-1)>.99&&BlackDividerAsta.condensa(C-1)>.9);
        assertEquals(0,BlackDividerAsta.alphaFenda(F),1e-9);assertEquals(0,BlackDividerAsta.alphaMeiaLua(C+10),1e-9);
    }
    @Test void fendaFicaDentroDaFaixaDeDano(){
        // Server: 0..18 blocks forward, lateral distance below 2.2.
        for(int i=0;i<BlackDividerAsta.PONTOS_FENDA;i++){
            var b=BlackDividerAsta.borda(i);assertTrue(b.z()>=0&&b.z()<=18);
            assertTrue(Math.abs(b.x())+BlackDividerAsta.larguraFenda(i)<2.2,"ponto "+i);
        }
        for(double t=0;t<=F;t+=.5)for(int i=0;i<BlackDividerAsta.GOTAS;i++){
            var g=BlackDividerAsta.gota(i,t);double a=BlackDividerAsta.alphaGota(i,t);
            assertTrue(Math.abs(g.x())<2.2&&g.y()>=0&&a>=0&&a<=1);
        }
        for(int i=0;i<BlackDividerAsta.GOTAS;i++)assertEquals(0,BlackDividerAsta.alphaGota(i,F),1e-9);
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(int t=0;t<=F;t++){
            var fios=new ArrayList<Object>();for(int j=0;j<BlackDividerAsta.FIOS;j++){var l=new ArrayList<Object>();for(int i=0;i<=16;i++)l.add(List.of(BlackDividerAsta.fio(j,i/16d,t),BlackDividerAsta.alphaFio(j,i/16d,t)));fios.add(l);}
            var faiscas=new ArrayList<Object>();for(int k=0;k<BlackDividerAsta.FAISCAS;k++){var l=new ArrayList<Object>();for(int i=0;i<5;i++)l.add(BlackDividerAsta.faisca(k,i,t));faiscas.add(l);}
            var gotas=new ArrayList<Object>();for(int i=0;i<BlackDividerAsta.GOTAS;i++)gotas.add(List.of(BlackDividerAsta.gota(i,t),BlackDividerAsta.alphaGota(i,t)));
            q.put(Integer.toString(t),Map.of("carga",BlackDividerAsta.carga(t),"condensa",BlackDividerAsta.condensa(t),"varredura",BlackDividerAsta.varredura(t),"meia",BlackDividerAsta.alphaMeiaLua(t),
                    "fenda",BlackDividerAsta.fenda(t),"alphaFenda",BlackDividerAsta.alphaFenda(t),"fios",fios,"faiscas",faiscas,"gotas",gotas));
        }
        var borda=new ArrayList<Object>();for(int i=0;i<BlackDividerAsta.PONTOS_FENDA;i++)borda.add(List.of(BlackDividerAsta.borda(i),BlackDividerAsta.larguraFenda(i)));
        var lamina=new ArrayList<Object>();for(int i=0;i<=36;i++){double a=1.95*i/36;lamina.add(List.of(a,BlackDividerAsta.lamina(.25,a),BlackDividerAsta.lamina(1,a)));}
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/asta-divider-samples.json"))){new Gson().toJson(Map.of("quadros",q,"borda",borda,"lamina",lamina,"corte",C,"fim",F),w);}
    }
}
