package com.cauansilva.arcanecontrol.core.familiar;

import org.junit.jupiter.api.Test;
import static com.cauansilva.arcanecontrol.core.familiar.RegrasFamiliar.*;
import static org.junit.jupiter.api.Assertions.*;

class RegrasFamiliarTest {
    @Test void stunAcumulaDecaiETemTeto(){
        double v=0;for(int i=0;i<5;i++)v=acumular(v,10,Tipo.RAIO.stun);
        assertTrue(atordoa(v),"5 golpes rapidos do Raio atordoam");
        double f=0;for(int i=0;i<5;i++)f=acumular(f,10,Tipo.FONTE.stun);assertFalse(atordoa(f));
        assertEquals(0,acumular(50,1000,0));assertEquals(STUN_MAX,acumular(99,0,50));
    }
    @Test void sacrificioSoNoGolpeFatal(){
        assertTrue(intercepta(20,10,true,100,100));assertFalse(intercepta(5,10,true,100,0),"nao fatal");
        assertFalse(intercepta(20,10,false,100,0),"ausente");assertFalse(intercepta(20,10,true,99,100),"recarga");
        assertTrue(Tipo.GUARDIAO.recargaSacrificio<Tipo.RAIO.recargaSacrificio&&Tipo.GUARDIAO.vidaSalva>Tipo.RAIO.vidaSalva);
    }
    @Test void posicaoNoOmbro(){
        double[] p=posicao(0,64,0,0,0);
        assertEquals(64+ALTURA,p[1],1e-9);assertTrue(Math.hypot(p[0],p[2])<1.2&&Math.hypot(p[0],p[2])>.5);
        assertTrue(p[2]<0,"fica atras quando o dono olha para +z");
    }
    @Test void tipos(){for(var t:Tipo.values()){assertFalse(t.efeito().isBlank());assertEquals(t,Tipo.porId(t.ordinal()));}assertNull(Tipo.porId(9));}
}
