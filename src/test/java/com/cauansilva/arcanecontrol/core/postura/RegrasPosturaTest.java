package com.cauansilva.arcanecontrol.core.postura;

import org.junit.jupiter.api.Test;
import static com.cauansilva.arcanecontrol.core.postura.RegrasPostura.*;
import static org.junit.jupiter.api.Assertions.*;

class RegrasPosturaTest {
    @Test void maximoRespeitaPiso(){assertEquals(12,maximo(4,false));assertEquals(16,maximo(20,false),1e-9);assertEquals(60,maximo(50,true));assertEquals(250,maximo(500,true));}
    @Test void golpeCarregadoValeMais(){
        assertTrue(golpe(10,true,1,false)>golpe(10,true,.3,false));assertEquals(15,golpe(10,true,1,true),1e-9);
        assertEquals(5,golpe(10,false,0,false),1e-9);assertEquals(0,golpe(-3,true,1,false));
    }
    @Test void regeneraSoDepoisDoAtraso(){
        assertEquals(10,recuperar(10,ATRASO_REGEN,20));assertTrue(recuperar(10,ATRASO_REGEN+20,20)<10);assertEquals(0,recuperar(10,10000,20));
    }
    @Test void execucaoMataOuMultiplica(){
        assertEquals(41,execucao(5,40,100,false),1e-9);
        assertEquals(20,execucao(5,80,100,false),1e-9);
        assertEquals(12.5,execucao(5,10,500,true),1e-9);
        assertTrue(quebra(16,16));assertFalse(quebra(15.9,16));assertTrue(duracao(true)>duracao(false));
    }
    @Test void barraDeTexto(){assertEquals("▮▮▯▯",RegrasPostura.barra(5,10,4));assertEquals("▮▮▮▮",RegrasPostura.barra(99,10,4));}
}
