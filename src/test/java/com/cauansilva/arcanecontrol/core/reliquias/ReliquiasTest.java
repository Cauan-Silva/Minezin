package com.cauansilva.arcanecontrol.core.reliquias;

import org.junit.jupiter.api.Test;
import java.util.*;
import static com.cauansilva.arcanecontrol.core.reliquias.Reliquias.*;
import static org.junit.jupiter.api.Assertions.*;

class ReliquiasTest {
    @Test void chancesSomamUm(){double s=0;for(int t=1;t<=TIERS;t++)s+=CHANCE[t];assertEquals(1,s,1e-9);}
    @Test void geracaoRespeitaTierEFaixas(){
        var r=new Random(7);
        for(int i=0;i<2000;i++){int t=1+r.nextInt(TIERS);var rel=gerar(r,Tipo.ANEL,t,1);
            assertEquals(afixos(t),rel.afixos().size());
            assertEquals(rel.afixos().size(),rel.afixos().stream().map(Rolagem::afixo).distinct().count(),"afixos distintos");
            for(var a:rel.afixos()){double[] f=a.afixo().faixa(t);assertTrue(a.valor()>=f[0]-.06&&a.valor()<=f[1]+.06,a+" tier "+t);}}
        assertTrue(Afixo.DANO.faixa(5)[0]>Afixo.DANO.faixa(1)[1],"tier alto e sempre melhor");
    }
    @Test void nomeComPrefixoESufixo(){
        var rel=new Reliquia(Tipo.ANEL,4,1,List.of(new Rolagem(Afixo.DANO,15),new Rolagem(Afixo.CRITICO,8)));
        assertEquals("Anel Feroz do Golpe Certo [Tier 4]",rel.nome());
        assertEquals("+15% Dano",Afixo.DANO.formatar(15));assertEquals("-8% Recarga reduzida",Afixo.RECARGA.formatar(8));
    }
    @Test void somaComTeto(){
        var a=new Reliquia(Tipo.ANEL,5,1,List.of(new Rolagem(Afixo.RECARGA,30)));var b=new Reliquia(Tipo.COLAR,5,1,List.of(new Rolagem(Afixo.RECARGA,30)));
        assertEquals(teto(Afixo.RECARGA),somar(List.of(a,b)).get(Afixo.RECARGA));
    }
    @Test void setComTresPecasOuMitica(){
        Reliquia c=new Reliquia(Tipo.ANEL,1,2,List.of(new Rolagem(Afixo.DANO,3))),m=new Reliquia(Tipo.COLAR,5,2,List.of(new Rolagem(Afixo.DANO,15)));
        assertTrue(sets(List.of(c,c)).isEmpty());assertEquals(Set.of(2),sets(List.of(c,c,c)));assertEquals(Set.of(2),sets(List.of(c,m)));
        assertFalse(set(2).isBlank());assertFalse(bonusSet(2).isEmpty());
    }
    @Test void pityNaDecimaPrimeira(){
        int cont=0;for(int i=1;i<=10;i++){var p=pity(cont);assertEquals(0,p[0],"abertura "+i);cont=depois(p[1],false);}
        var p=pity(cont);assertEquals(1,p[0]);assertEquals(0,depois(p[1],true));
        var bau=bau(new Random(3),1,true);assertEquals(ITENS_BAU,bau.size());assertEquals(1,bau.stream().filter(Reliquia::mitica).count());
    }
    @Test void bauNuncaTemDuasMiticas(){var r=new Random(11);for(int i=0;i<500;i++)assertTrue(bau(r,1,i%2==0).stream().filter(Reliquia::mitica).count()<=1);}
}
