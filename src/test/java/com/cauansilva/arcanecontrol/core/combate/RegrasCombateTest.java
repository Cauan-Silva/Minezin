package com.cauansilva.arcanecontrol.core.combate;

import org.junit.jupiter.api.Test;
import static com.cauansilva.arcanecontrol.core.combate.RegrasCombate.*;
import static org.junit.jupiter.api.Assertions.*;

class RegrasCombateTest {
    @Test void impactoCresceComDanoECritico(){
        assertEquals(0,forca(0,20,false));
        assertTrue(forca(10,20,false)>forca(3,20,false));assertTrue(forca(5,20,true)>forca(5,20,false));
        assertTrue(forca(9999,20,true)<=1);
        assertEquals(0,hitstopMs(.2));assertEquals(35,hitstopMs(.25));assertEquals(110,hitstopMs(1));
        assertTrue(tremor(.5,true)>tremor(.5,false));
    }
    @Test void esquivaRecargaIframesEPerfeita(){
        assertTrue(podeEsquivar(100,0));assertFalse(podeEsquivar(110,100));assertTrue(podeEsquivar(100+RECARGA_ESQUIVA,100));
        assertTrue(invulneravel(100,100));assertTrue(invulneravel(100+IFRAMES-1,100));assertFalse(invulneravel(100+IFRAMES,100));
        assertTrue(perfeita(102,100));assertFalse(perfeita(100+JANELA_PERFEITA,100));assertFalse(perfeita(99,100));
        assertTrue(podeEsquivar(100+RECARGA_ESQUIVA/2,reembolso(100)),"perfeita devolve metade");
        assertTrue(JANELA_PERFEITA<IFRAMES);
    }
    @Test void lockOnPrefereMiraEDistancia(){
        assertTrue(pontuar(5,10)<pontuar(20,10));assertTrue(pontuar(5,5)<pontuar(5,20));
        assertEquals(Double.POSITIVE_INFINITY,pontuar(40,5));assertEquals(Double.POSITIVE_INFINITY,pontuar(5,30));
        assertEquals(190,aproximar(170,-170,1),1e-4);assertEquals(175,aproximar(170,-170,.25f),1e-4,"vira pelo lado curto");
        assertTrue(fov(3)>fov(30));
    }
}
