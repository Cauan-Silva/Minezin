package com.cauansilva.arcanecontrol.core.vfx;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RaiosRoxoMovimentoTest {
    @Test void raiosMudamDirecaoNosTresEixosSemSaltos(){
        for(int r=0;r<8;r++){
            var inicio=RaiosRoxo.ponto(r,8,34);var depois=RaiosRoxo.ponto(r,8,48);
            double produto=inicio.lateral()*depois.lateral()+inicio.vertical()*depois.vertical()+inicio.frente()*depois.frente();
            double norma=Math.sqrt(norma2(inicio)*norma2(depois));
            assertTrue(produto/norma<.98,"Bolt must change heading, not only length");
            for(double t=0;t<100;t+=.37)for(int i=0;i<=8;i++){
                var p=RaiosRoxo.ponto(r,i,t);var q=RaiosRoxo.ponto(r,i,t+.001);
                assertTrue(Double.isFinite(norma2(p)));assertTrue(norma2(p)<100);
                assertTrue(distancia2(p,q)<.0001);
                var ramo=RaiosRoxo.ramo(r,i,t);assertTrue(distancia2(p,ramo)<4);
            }
        }
    }
    private static double norma2(RaiosRoxo.Ponto p){return p.lateral()*p.lateral()+p.vertical()*p.vertical()+p.frente()*p.frente();}
    private static double distancia2(RaiosRoxo.Ponto p,RaiosRoxo.Ponto q){return Math.pow(p.lateral()-q.lateral(),2)+Math.pow(p.vertical()-q.vertical(),2)+Math.pow(p.frente()-q.frente(),2);}
}
