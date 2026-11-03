package com.cauansilva.arcanecontrol.core.vfx;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CeroIchigoTest {
    @Test void mascaraSeFormaESomeOsEstilhacos(){
        for(int k=0;k<CeroIchigo.ESTILHACOS;k++){assertEquals(0,CeroIchigo.tamanhoEstilhaco(k,CeroIchigo.FORMA),1e-9);var p=CeroIchigo.estilhaco(k,CeroIchigo.FORMA-2);assertTrue(Math.hypot(p.x(),p.y())<.6);}
        assertEquals(0,CeroIchigo.rachadura(0),1e-9);assertEquals(3,CeroIchigo.SHUNPOS);
    }
}
