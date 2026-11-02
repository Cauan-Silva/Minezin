package com.cauansilva.arcanecontrol.core.vfx;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SelosCidTest {
    @Test void selosConvergemAntesDoImpactoSemAlterarORaioDeCombate(){
        for(int i=0;i<3;i++){double antes=PerfilSelosCid.raio(0,i);for(double t=0;t<90;t+=.25){double r=PerfilSelosCid.raio(t,i);assertTrue(r>0&&r<=antes+1e-9);antes=r;}assertEquals(.2+i*.12,PerfilSelosCid.raio(88,i),1e-8);}
        assertEquals(0,PerfilSelosCid.elevar(50));assertEquals(1,PerfilSelosCid.elevar(88));assertEquals(90,PerfilAtomic.IMPACTO);
    }
}
