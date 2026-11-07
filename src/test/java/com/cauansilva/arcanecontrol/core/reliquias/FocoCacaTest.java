package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FocoCacaTest {
    @Test void primeiraSempreNoFoco(){
        var r=new Random(3);
        for(int k=0;k<200;k++){var b=FocoCaca.bau(r,1,false,Tipo.ANEL,Afixo.ROUBO);
            assertEquals(Reliquias.ITENS_BAU,b.size());assertEquals(Tipo.ANEL,b.get(0).tipo());assertEquals(Afixo.ROUBO,b.get(0).afixos().get(0).afixo());
            assertTrue(b.stream().filter(Reliquia::mitica).count()<=1);
            for(var x:b)assertEquals(x.afixos().size(),new HashSet<>(x.afixos().stream().map(Rolagem::afixo).toList()).size(),"sem afixo repetido");}
    }
    @Test void focoPuxaAsOutras(){
        var r=new Random(4);int aneis=0,total=0;
        for(int k=0;k<2000;k++){var b=FocoCaca.bau(r,1,false,Tipo.COLAR,null);for(int i=1;i<b.size();i++){total++;if(b.get(i).tipo()==Tipo.COLAR)aneis++;}}
        double f=aneis/(double)total;assertTrue(f>.55&&f<.70,"~50% foco + 12,5% natural: "+f);
    }
    @Test void custoEPity(){
        assertEquals(0,FocoCaca.custo(null,null));assertEquals(3,FocoCaca.custo(Tipo.ANEL,null));assertEquals(6,FocoCaca.custo(Tipo.ANEL,Afixo.DANO));
        assertTrue(FocoCaca.bau(new Random(1),2,true,null,Afixo.DANO).get(0).mitica());
        assertEquals(Tipo.BRINCO,FocoCaca.tipo("brinco"));assertEquals(Afixo.VIDA,FocoCaca.afixo("VIDA"));assertNull(FocoCaca.tipo("x"));
    }
}
