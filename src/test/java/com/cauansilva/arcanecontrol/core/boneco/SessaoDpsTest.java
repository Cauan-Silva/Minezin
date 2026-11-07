package com.cauansilva.arcanecontrol.core.boneco;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessaoDpsTest {
    @Test void calculaDps(){
        var s=new SessaoDps();assertEquals(0,s.dps());
        s.registrar(0,10);assertEquals(10,s.dps(),1e-9,"primeiro golpe conta como 1 s");
        s.registrar(40,10);s.registrar(80,20);
        assertEquals(40,s.total());assertEquals(10,s.dps(),1e-9);assertEquals(20,s.maior());assertEquals(3,s.golpes());
    }
    @Test void reiniciaDepoisDeOcioso(){
        var s=new SessaoDps();s.registrar(0,50);
        assertFalse(s.expirou(SessaoDps.OCIOSO));assertTrue(s.expirou(SessaoDps.OCIOSO+1));
        s.registrar(500,5);assertEquals(5,s.total());assertEquals(1,s.golpes());
        s.registrar(510,-3);assertEquals(1,s.golpes());
        s.encerrar();assertFalse(s.ativa());
    }
    @Test void textos(){var s=new SessaoDps();s.registrar(0,7.25);assertTrue(s.painel().contains("DPS 7.3")||s.painel().contains("DPS 7.2"));assertTrue(s.resumo().contains("1 golpes"));}
}
