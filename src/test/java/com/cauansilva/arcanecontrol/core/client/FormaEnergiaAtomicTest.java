package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.cid.FormaEnergiaAtomic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FormaEnergiaAtomicTest {
    @Test void filamentosConvergemNaAncoraSemSaltosOuValoresInvalidos() {
        for(int j=0;j<9;j++) for(double t=0;t<90;t+=.5) {
            var ponta=FormaEnergiaAtomic.filamento(j,1,t);
            assertEquals(0,ponta.x(),1e-8);assertEquals(0,ponta.y(),1e-8);assertEquals(0,ponta.z(),1e-8);
            for(double s=0;s<=1;s+=.02) {
                var p=FormaEnergiaAtomic.filamento(j,s,t);
                assertTrue(Double.isFinite(p.x()+p.y()+p.z()));
                assertTrue(Math.abs(p.x())<6 && Math.abs(p.z())<6 && Math.abs(p.y())<4);
                var q=FormaEnergiaAtomic.filamento(j,s,t+.01);
                assertTrue(Math.abs(q.x()-p.x())<.01 && Math.abs(q.z()-p.z())<.01);
            }
        }
    }
    @Test void ondasRespeitamImpactoEExpandemSemReverter() {
        for(int j=0;j<3;j++) {
            assertEquals(0,FormaEnergiaAtomic.onda(90+j*5,j));
            double anterior=0;
            for(double t=90;t<170;t+=.1) {
                double r=FormaEnergiaAtomic.onda(t,j);
                assertTrue(r>=anterior && r<18);anterior=r;
            }
        }
    }
    @Test void turbulenciaNaoInverteNemExplodeAMalha() {
        for(double t=0;t<170;t+=.5) for(double a=0;a<Math.PI*2;a+=.05) {
            double r=FormaEnergiaAtomic.raio(a,.3,t,10);
            assertTrue(r>=8.9 && r<=11.1);
        }
        for(double a=0;a<Math.PI*2;a+=.05)
            assertEquals(10,FormaEnergiaAtomic.raio(a,Math.PI/2,100,10),1e-8);
    }
}
