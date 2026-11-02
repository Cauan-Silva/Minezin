package com.cauansilva.arcanecontrol.core.cid;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RoteiroTecidoCidTest {
    @Test void impulsosSaoFinitosLimitadosETerminam(){
        for(int tipo:new int[]{11,12,13,14,17}){
            for(int t=0;t<300;t++){var p=RoteiroTecidoCid.amostrar(tipo,t);assertTrue(Double.isFinite(p.frente()+p.lado()+p.giro()));assertTrue(Math.abs(p.frente())<=.3&&Math.abs(p.lado())<=.15&&Math.abs(p.giro())<=.2);}
            var fim=RoteiroTecidoCid.amostrar(tipo,300);assertEquals(0,fim.frente(),1e-12);assertEquals(0,fim.lado(),1e-12);assertEquals(0,fim.giro(),1e-12);
        }
        assertEquals(new RoteiroTecidoCid.Impulso(0,0,0),RoteiroTecidoCid.amostrar(14,Double.NaN));
    }
    @Test void ataquesTemAntecipacaoImpactoERetorno(){
        assertEquals(0,RoteiroTecidoCid.amostrar(11,0).frente(),1e-9);
        assertTrue(RoteiroTecidoCid.amostrar(11,6).giro()>.1);
        assertTrue(RoteiroTecidoCid.amostrar(12,2).frente()>0);
        assertTrue(RoteiroTecidoCid.amostrar(12,9).frente()<0);
        assertTrue(RoteiroTecidoCid.amostrar(14,92).frente()>.2);
        assertTrue(RoteiroTecidoCid.amostrar(17,8).frente()>.1);
    }
}
