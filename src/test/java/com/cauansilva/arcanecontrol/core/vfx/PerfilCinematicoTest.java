package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.asta.PerfilAsta;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PerfilCinematicoTest {
    @Test void cortesNaoAntecipamAsJanelasDeAcertoDoServidor(){
        for(boolean forma:new boolean[]{false,true})for(int slot=0;slot<4;slot++){
            if(!forma&&slot==3)continue;
            int impacto=PerfilAsta.impacto(slot,forma);
            assertEquals(0,PerfilCinematico.corte(impacto-2.01,impacto));
            assertEquals(1,PerfilCinematico.corte(impacto,impacto),1e-9);
            assertEquals(0,PerfilCinematico.corte(impacto+12,impacto));
            assertEquals(0,PerfilCinematico.varredura(impacto-3,impacto));
            assertEquals(1,PerfilCinematico.varredura(impacto+1,impacto));
        }
    }
    @Test void impactoNaoExisteAntesDoDisparoENaoPersiste(){
        assertEquals(0,PerfilCinematico.impacto(-.01));
        assertEquals(1,PerfilCinematico.impacto(0));
        double anterior=1;
        for(double t=0;t<=30;t+=.25){double a=PerfilCinematico.impacto(t);assertTrue(a>=0&&a<=anterior);anterior=a;}
        assertEquals(0,PerfilCinematico.impacto(24));
    }
    @Test void cameraRespeitaDistanciaForcaELimiteTemporal(){
        assertEquals(0,PerfilCinematico.trauma(-1,0,40,1));
        assertEquals(1,PerfilCinematico.trauma(0,0,40,1));
        assertEquals(.5,PerfilCinematico.trauma(0,20,40,1),1e-9);
        assertEquals(0,PerfilCinematico.trauma(0,40,40,1));
        assertEquals(0,PerfilCinematico.trauma(24,0,40,1));
        assertEquals(0,PerfilCinematico.trauma(0,Double.NaN,40,1));
        assertEquals(0,PerfilCinematico.trauma(0,0,0,1));
        assertTrue(PerfilCinematico.trauma(12,0,40,1)<PerfilCinematico.trauma(0,0,40,1));
    }
    @Test void fragmentosConvergemESomemNaReinicializacaoDaOrbita(){
        for(int i=0;i<18;i++){
            double inicio=-i*7;
            assertEquals(0,PerfilCinematico.opacidadeFragmento(i,inicio));
            assertEquals(0,PerfilCinematico.opacidadeFragmento(i,inicio+64));
            double anterior=Double.POSITIVE_INFINITY;
            for(double t=inicio;t<inicio+64;t+=.5){
                var p=PerfilCinematico.orbita(i,t,4.8);double r=Math.hypot(p.x(),p.z());
                assertTrue(Double.isFinite(p.y())&&r<=anterior+1e-9&&r>4.8);
                double a=PerfilCinematico.opacidadeFragmento(i,t);assertTrue(a>=0&&a<=1);anterior=r;
            }
        }
    }
    @Test void qualidadeReduzidaTemOrcamentoMenor(){
        assertTrue(PerfilCinematico.fragmentos(true)<PerfilCinematico.fragmentos(false));
        assertTrue(PerfilCinematico.raios(true)<PerfilCinematico.raios(false));
        assertTrue(PerfilCinematico.fragmentos(false)<=18);
        assertTrue(PerfilCinematico.raios(false)<=10);
    }
    @Test void formaRevelaBracoChifreEAsaSemRepetirAberturaParaObservadorTardio(){
        assertEquals(0,PerfilCinematico.revelarForma(4,"right_arm"));
        assertEquals(1,PerfilCinematico.revelarForma(18,"right_arm"));
        assertEquals(0,PerfilCinematico.revelarForma(18,"head"));
        assertEquals(1,PerfilCinematico.revelarForma(34,"head"));
        assertEquals(0,PerfilCinematico.revelarForma(30,"body"));
        assertEquals(1,PerfilCinematico.revelarForma(PerfilCinematico.FORMA_IMPACTO,"body"));
        for(String parte:new String[]{"right_arm","head","body"}){
            double anterior=0;
            for(double t=0;t<1200;t+=.5){double v=PerfilCinematico.revelarForma(t,parte);assertTrue(v>=anterior&&v<=1);anterior=v;}
            assertEquals(1,PerfilCinematico.revelarForma(1000,parte));
        }
        assertEquals(0,PerfilCinematico.abrirAsa(26));
        assertEquals(1,PerfilCinematico.abrirAsa(PerfilCinematico.FORMA_IMPACTO));
    }
}
