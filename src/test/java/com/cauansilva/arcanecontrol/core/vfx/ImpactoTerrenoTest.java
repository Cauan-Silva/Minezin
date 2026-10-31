package com.cauansilva.arcanecontrol.core.vfx;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
class ImpactoTerrenoTest {
    @Test void crateraTemFundoBordaEOrdemDeExpansao(){
        var plano=PerfilCratera.planejar(14,8);var unicos=new HashSet<>(plano);
        assertEquals(plano.size(),unicos.size());assertTrue(unicos.contains(new PerfilCratera.Bloco(0,-8,0)));
        assertFalse(unicos.contains(new PerfilCratera.Bloco(14,-1,0)));
        assertTrue(unicos.contains(new PerfilCratera.Bloco(14,0,0)));
        int ultimo=0;
        for(var b:plano){int r=b.x()*b.x()+b.z()*b.z();assertTrue(r>=ultimo);ultimo=r;assertTrue(b.y()>=-8&&b.y()<=2);}
        assertTrue(plano.size()>3000&&plano.size()<8000);
    }
    @Test void parametrosInvalidosNaoGeramPlanoIlimitado(){
        assertEquals(PerfilCratera.planejar(28,12),PerfilCratera.planejar(Integer.MAX_VALUE,Integer.MAX_VALUE));
        assertEquals(PerfilCratera.planejar(1,1),PerfilCratera.planejar(-10,-10));
    }
    @Test void crateraNaoCancelaAtomicDepoisDoImpacto(){
        assertTrue(PerfilAtomic.cancelarPorMovimento(89,16));
        assertFalse(PerfilAtomic.cancelarPorMovimento(90,64));
        assertFalse(PerfilAtomic.cancelarPorMovimento(150,100));
    }
    @Test void feixeDesceAntesDoDanoEPersisteDepois(){
        assertEquals(48,PerfilAtomic.baseFeixe(50));assertEquals(0,PerfilAtomic.baseFeixe(90));
        assertTrue(PerfilAtomic.baseFeixe(70)>0&&PerfilAtomic.baseFeixe(70)<48);
        assertEquals(1,PerfilAtomic.brilhoFeixe(120));assertEquals(0,PerfilAtomic.brilhoFeixe(170));
        assertEquals(0,PerfilAtomic.brilhoFeixe(49));
    }
}
