package com.cauansilva.arcanecontrol.core.vfx;
import com.cauansilva.arcanecontrol.core.asta.PerfilAsta;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BlackHurricaneAstaTest {
    private static final int E=BlackHurricaneAsta.ESTOURO,F=BlackHurricaneAsta.FIM,R=BlackHurricaneAsta.RAIO;
    private static double plano(BlackHurricaneAsta.P p){return Math.hypot(p.x(),p.z());}
    @Test void fronteiraChegaAoRaioOitoNoTickDoDano(){
        assertEquals(PerfilAsta.impacto(1,true),E);
        assertEquals(R,BlackHurricaneAsta.fronteira(E),1e-9);
        for(double t=0;t<E;t+=.25)assertTrue(BlackHurricaneAsta.fronteira(t)<R);
        for(double t=0;t<E;t+=.25){assertEquals(0,BlackHurricaneAsta.anel(t),1e-9);for(int i=0;i<BlackHurricaneAsta.FRAGMENTOS;i++)assertEquals(0,BlackHurricaneAsta.alphaFragmento(i,t),1e-9);}
        // The spin keeps accelerating up to the burst and never stalls after it.
        for(double t=.25;t<=F;t+=.25)assertTrue(BlackHurricaneAsta.giro(t)>BlackHurricaneAsta.giro(t-.25));
    }
    @Test void nadaPassaDoRaioDoServidor(){
        for(double t=0;t<=F;t+=.25){
            for(int j=0;j<BlackHurricaneAsta.LAMINAS;j++)for(int i=0;i<=8;i++)for(int v=0;v<=1;v++)assertTrue(plano(BlackHurricaneAsta.lamina(j,i/8d,v,t))<=R+1e-9);
            for(int i=0;i<BlackHurricaneAsta.FRAGMENTOS;i++)assertTrue(plano(BlackHurricaneAsta.fragmento(i,t))<R);
            for(int k=0;k<BlackHurricaneAsta.FIOS;k++)assertTrue(plano(BlackHurricaneAsta.fio(k,0,t))<R);
        }
    }
    @Test void tudoSomeAteOFim(){
        assertEquals(0,BlackHurricaneAsta.succao(F),1e-9);assertEquals(0,BlackHurricaneAsta.alphaFunil(F),1e-9);
        assertEquals(0,BlackHurricaneAsta.alphaLaminas(F),1e-9);assertEquals(0,BlackHurricaneAsta.anel(F),1e-9);
        for(int i=0;i<BlackHurricaneAsta.FRAGMENTOS;i++)assertEquals(0,BlackHurricaneAsta.alphaFragmento(i,F),1e-9);
    }
    @Test void exportaParaPrevia()throws Exception{
        var q=new LinkedHashMap<String,Object>();
        for(int n=0;n<=F*4;n++){
            double t=n/4.0;
            var fios=new ArrayList<Object>();for(int k=0;k<BlackHurricaneAsta.FIOS;k++){var l=new ArrayList<Object>();for(int i=0;i<=20;i++)l.add(List.of(BlackHurricaneAsta.fio(k,i/20d,t),BlackHurricaneAsta.alphaFio(k,i/20d,t)));fios.add(l);}
            var bandas=new ArrayList<Object>();for(int b=0;b<BlackHurricaneAsta.BANDAS;b++){var l=new ArrayList<Object>();double u0=BlackHurricaneAsta.inicioRisco(b,t);
                for(int i=0;i<=12;i++){double f=i/12d,u=u0+f*BlackHurricaneAsta.COMPRIMENTO_RISCO;if(u<0||u>1)continue;l.add(List.of(BlackHurricaneAsta.banda(b,u,t),BlackHurricaneAsta.larguraRisco(u,f)));}bandas.add(l);}
            var laminas=new ArrayList<Object>();for(int j=0;j<BlackHurricaneAsta.LAMINAS;j++){var l=new ArrayList<Object>();for(int i=0;i<=12;i++)l.add(List.of(BlackHurricaneAsta.lamina(j,i/12d,0,t),BlackHurricaneAsta.lamina(j,i/12d,1,t)));laminas.add(l);}
            var frag=new ArrayList<Object>();for(int i=0;i<BlackHurricaneAsta.FRAGMENTOS;i++)frag.add(List.of(BlackHurricaneAsta.fragmento(i,t),BlackHurricaneAsta.alphaFragmento(i,t)));
            var m=new LinkedHashMap<String,Object>();
            m.put("succao",BlackHurricaneAsta.succao(t));m.put("funil",BlackHurricaneAsta.funil(t));m.put("alphaFunil",BlackHurricaneAsta.alphaFunil(t));m.put("fronteira",BlackHurricaneAsta.fronteira(t));
            m.put("alphaLaminas",BlackHurricaneAsta.alphaLaminas(t));m.put("anel",BlackHurricaneAsta.anel(t));m.put("giro",BlackHurricaneAsta.giro(t));
            m.put("fios",fios);m.put("bandas",bandas);m.put("laminas",laminas);m.put("fragmentos",frag);
            q.put(Double.toString(t),m);
        }
        Files.createDirectories(Path.of("build"));
        try(var w=Files.newBufferedWriter(Path.of("build/asta-hurricane-samples.json"))){new Gson().toJson(Map.of("quadros",q,"estouro",E,"fim",F,"raio",R),w);}
    }
}
