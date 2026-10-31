package com.cauansilva.arcanecontrol.core.vfx;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PerfisAnimeTest {
    @Test void corteAtingeAlcanceCompletoNoTickDeDano(){
        assertEquals(5,PerfilCid.raioCorte(PerfilCid.CORTE_IMPACTO));
        assertEquals(1,PerfilCid.opacidadeCorte(PerfilCid.CORTE_IMPACTO));
        assertEquals(0,PerfilCid.opacidadeCorte(3));assertEquals(0,PerfilCid.opacidadeCorte(20));
    }
    @Test void singularidadeAbreColapsaEEncerraSemValoresInvalidos(){
        assertEquals(4.8,PerfilFrieren.nucleo(70),1e-6);assertEquals(0,PerfilFrieren.nucleo(234),1e-6);
        assertEquals(0,PerfilFrieren.opacidade(280));
        for(double t=0;t<=300;t+=.25){assertTrue(PerfilFrieren.nucleo(t)>=0);assertTrue(PerfilFrieren.opacidade(t)>=0&&PerfilFrieren.opacidade(t)<=1);}
    }
    @Test void raiosContinuosLimitadosEReproduziveis(){
        for(int r=0;r<8;r++)for(double t=0;t<160;t+=.5){
            assertTrue(RaiosRoxo.intensidade(r,t)>=.15&&RaiosRoxo.intensidade(r,t)<=1);
            for(int s=0;s<=8;s++){
                var p=RaiosRoxo.ponto(r,s,t);assertEquals(p,RaiosRoxo.ponto(r,s,t));
                assertTrue(Double.isFinite(p.frente()));assertTrue(Math.hypot(p.lateral(),p.vertical())<8.2);
                var next=RaiosRoxo.ponto(r,s,t+.001);assertTrue(Math.abs(next.lateral()-p.lateral())<.02);
            }
        }
    }
}
