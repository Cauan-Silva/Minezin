package com.cauansilva.arcanecontrol.core.vazio;

import com.cauansilva.arcanecontrol.core.vazio.RegrasNemesis.*;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class RegrasNemesisTest {
    @Test void aprendeContraOEstilo(){
        var r=new Random(1);
        assertEquals(Traco.COURACA,RegrasNemesis.aprender(0,100,10,r));
        assertEquals(Traco.ANTIMAGIA,RegrasNemesis.aprender(0,10,100,r));
        var t=RegrasNemesis.aprender(Traco.COURACA.bit(),100,0,r);assertNotNull(t);assertNotEquals(Traco.COURACA,t);
        assertNull(RegrasNemesis.aprender(31,1,1,r));
    }
    @Test void wipeSobeNivelComTeto(){
        var r=new Random(2);var e=new Estado(0,0);
        for(int i=0;i<8;i++)e=RegrasNemesis.wipe(e,50,10,r);
        assertEquals(RegrasNemesis.NIVEL_MAX,e.nivel());assertEquals(31,e.tracos(),"aprende todos com o tempo");
        assertEquals(1,RegrasNemesis.wipe(new Estado(0,0),0,0,r).lista().size());
    }
    @Test void escalas(){
        assertEquals(1,RegrasNemesis.vida(0));assertEquals(1.75,RegrasNemesis.vida(5),1e-9);
        assertEquals(1.2*1.2,RegrasNemesis.dano(2,Traco.FURIA.bit()),1e-9);
        assertEquals(.75,RegrasNemesis.recebido(Traco.COURACA.bit(),true),1e-9);assertEquals(1,RegrasNemesis.recebido(Traco.COURACA.bit(),false));
        assertEquals(6,RegrasNemesis.vinganca(3));
    }
    @Test void titulo(){
        assertEquals("Rei",RegrasNemesis.titulo("Rei",new Estado(0,0)));
        assertEquals("Rei Nv.2 (Couraça, Fúria)",RegrasNemesis.titulo("Rei",new Estado(2,Traco.COURACA.bit()|Traco.FURIA.bit())));
    }
}
