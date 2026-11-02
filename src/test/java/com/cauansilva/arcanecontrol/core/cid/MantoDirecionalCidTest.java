package com.cauansilva.arcanecontrol.core.cid;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MantoDirecionalCidTest {
    private static MantoDirecionalCid estabilizar(double frente,double lado){var m=new MantoDirecionalCid();for(int i=0;i<160;i++)m.tick(new MolaMantoCid.Entrada(frente,lado,0,0,true));return m;}
    @Test void frenteReELateraisProduzemRespostasDiferentes(){
        var frente=estabilizar(.22,0);var re=estabilizar(-.22,0);var dir=estabilizar(0,.22);var esq=estabilizar(0,-.22);
        assertTrue(frente.amostrar(2,3,1).x()<-.1);assertTrue(re.amostrar(2,3,1).x()>0);
        assertEquals(0,dir.amostrar(2,3,1).x(),1e-8);
        assertTrue(dir.amostrar(2,3,1).z()<0);assertTrue(esq.amostrar(2,3,1).z()>0);
        assertEquals(-dir.amostrar(2,3,1).z(),esq.amostrar(2,3,1).z(),1e-8);
        assertTrue(dir.amostrar(2,3,1).y()<0);assertTrue(esq.amostrar(2,3,1).y()>0);
    }
    @Test void paineisTemRespostasIndependentesEReiniciamJuntos(){
        var m=new MantoDirecionalCid();for(int i=0;i<8;i++)m.tick(new MolaMantoCid.Entrada(.12,.2,0,.2,true));
        assertNotEquals(m.amostrar(0,3,1),m.amostrar(4,3,1));
        var p=m.detalhe("cid_collar_left",1);assertTrue(Math.abs(p.x())<=.045&&Math.abs(p.y())<=.045&&Math.abs(p.z())<=.045);
        m.reset();for(int i=0;i<5;i++)for(int s=0;s<4;s++)assertEquals(new MolaMantoCid.Pose(0,0,0),m.amostrar(i,s,1));
        for(var nome:DetalhesTrajeCid.OSSOS){var zero=m.detalhe(nome,1);assertEquals(0,zero.x(),1e-12);assertEquals(0,zero.y(),1e-12);assertEquals(0,zero.z(),1e-12);}
    }
    @Test void interpolacaoDeTodaACapaNaoAvancaOEstado(){
        var a=new MantoDirecionalCid();var b=new MantoDirecionalCid();
        for(int i=0;i<180;i++){var e=new MolaMantoCid.Entrada(Math.sin(i*.1)*.25,Math.cos(i*.13)*.18,-.1,Math.sin(i*.08)*.15,false);a.tick(e);b.tick(e);for(int f=0;f<12;f++)for(int panel=0;panel<5;panel++)a.amostrar(panel,3,f/12d);}
        for(int p=0;p<5;p++)for(int s=0;s<4;s++)assertEquals(a.amostrar(p,s,1),b.amostrar(p,s,1));
        assertThrows(IllegalArgumentException.class,()->a.amostrar(5,0,1));
    }
}
