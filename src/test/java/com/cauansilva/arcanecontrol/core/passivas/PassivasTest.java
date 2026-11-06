package com.cauansilva.arcanecontrol.core.passivas;

import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;
import org.junit.jupiter.api.Test;
import static com.cauansilva.arcanecontrol.core.passivas.Passivas.*;
import static org.junit.jupiter.api.Assertions.*;

/** Tabela de passivas aprovada em 06/11/2026: cada kit so altera o que a sua passiva promete. */
class PassivasTest {
    @Test void todoKitTemNome(){for(var k:PoderAnime.values())assertFalse(nome(k).isBlank(),k.name());}
    @Test void killuaReduzRaioEBloqueiaVeneno(){
        assertEquals(2.5f,danoRecebido(PoderAnime.KILLUA,Dano.RAIO,15,10),1e-4);
        assertEquals(10f,danoRecebido(PoderAnime.KILLUA,Dano.MAGIA,15,10),1e-4);
        assertTrue(bloqueiaEfeito(PoderAnime.KILLUA,"poison"));assertTrue(bloqueiaEfeito(PoderAnime.KILLUA,"wither"));
        assertFalse(bloqueiaEfeito(PoderAnime.KILLUA,"slowness"));
    }
    @Test void gojoDevolveMana(){assertEquals(3,reembolso(PoderAnime.GOJO,20),1e-9);assertEquals(0,reembolso(PoderAnime.CID,20),1e-9);}
    @Test void shadowSoNoEscuro(){
        assertEquals(11.5f,danoCausado(PoderAnime.CID,7,false,1,10),1e-4);assertEquals(10f,danoCausado(PoderAnime.CID,8,false,1,10),1e-4);
        assertEquals(8f,danoRecebido(PoderAnime.CID,Dano.OUTRO,0,10),1e-4);assertEquals(10f,danoRecebido(PoderAnime.CID,Dano.OUTRO,15,10),1e-4);
    }
    @Test void frierenAlvoDesatentoERegeneracao(){
        assertEquals(12f,danoCausado(PoderAnime.FRIEREN,15,true,1,10),1e-4);assertEquals(10f,danoCausado(PoderAnime.FRIEREN,15,false,1,10),1e-4);
        assertEquals(1.5,regeneracao(PoderAnime.FRIEREN));assertEquals(1,regeneracao(PoderAnime.GOJO));
    }
    @Test void astaAntimagia(){
        assertEquals(7f,danoRecebido(PoderAnime.ASTA,Dano.MAGIA,15,10),1e-4);assertEquals(10f,danoRecebido(PoderAnime.ASTA,Dano.OUTRO,15,10),1e-4);
        assertTrue(bloqueiaEfeito(PoderAnime.ASTA,"slowness"));
    }
    @Test void ichigoComPoucaVida(){
        assertEquals(12f,danoCausado(PoderAnime.ICHIGO,15,false,.3,10),1e-4);assertEquals(10f,danoCausado(PoderAnime.ICHIGO,15,false,.31,10),1e-4);
        assertEquals(50,recarga(PoderAnime.ICHIGO,0,.2,100));assertEquals(100,recarga(PoderAnime.ICHIGO,1,.2,100));assertEquals(100,recarga(PoderAnime.ICHIGO,0,.9,100));
    }
    @Test void subaruRetornoComRecarga(){
        assertTrue(retorno(PoderAnime.SUBARU,6,8,100,100));assertFalse(retorno(PoderAnime.SUBARU,6,8,99,100));
        assertFalse(retorno(PoderAnime.SUBARU,6,5,100,0));assertFalse(retorno(PoderAnime.GOJO,6,8,100,0));
    }
}
