package com.cauansilva.arcanecontrol.core.reliquias;

import org.junit.jupiter.api.Test;
import java.util.*;
import static com.cauansilva.arcanecontrol.core.reliquias.Reliquias.*;
import static org.junit.jupiter.api.Assertions.*;

class EssenciaFerreiroTest {
    @Test void desmancheECustoCrescemComTier(){
        for(int t=2;t<=TIERS;t++){assertTrue(DESMANCHE[t]>DESMANCHE[t-1]);assertTrue(custo(t,Reforja.VALORES)>custo(t-1,Reforja.VALORES));}
        for(int t=1;t<=TIERS;t++){assertEquals(2*custo(t,Reforja.VALORES),custo(t,Reforja.TUDO));
            assertTrue(DESMANCHE[t]<custo(t,Reforja.VALORES),"desmanchar e reforjar o mesmo tier nao pode dar lucro");}
    }
    @Test void reforjarValoresMantemAfixos(){
        var r=new Random(3);
        for(int i=0;i<500;i++){int t=1+r.nextInt(TIERS);var a=gerar(r,Tipo.COLAR,t,4);var b=reforjar(r,a,Reforja.VALORES);
            assertEquals(a.afixos().stream().map(Rolagem::afixo).toList(),b.afixos().stream().map(Rolagem::afixo).toList());
            assertEquals(a.tier(),b.tier());assertEquals(a.kit(),b.kit());assertEquals(a.tipo(),b.tipo());
            for(var x:b.afixos()){double[] f=x.afixo().faixa(t);assertTrue(x.valor()>=f[0]-.06&&x.valor()<=f[1]+.06);}}
    }
    @Test void reforjarTudoMantemTierTipoEChefe(){
        var r=new Random(9);var a=gerar(r,Tipo.BRINCO,5,12);boolean mudou=false;
        for(int i=0;i<50;i++){var b=reforjar(r,a,Reforja.TUDO);assertEquals(5,b.tier());assertEquals(12,b.kit());assertEquals(Tipo.BRINCO,b.tipo());
            assertEquals(afixos(5),b.afixos().size());mudou|=!b.afixos().equals(a.afixos());}
        assertTrue(mudou);
    }
    @Test void essenciaDoBau(){var r=new Random(1);for(int i=0;i<200;i++){int e=essenciaBau(r);assertTrue(e>=2&&e<=4);}}
}
