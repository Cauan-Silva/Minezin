package com.cauansilva.arcanecontrol.core.reacao;

import com.cauansilva.arcanecontrol.core.reacao.RegrasReacao.*;
import org.junit.jupiter.api.Test;
import static com.cauansilva.arcanecontrol.core.reacao.RegrasReacao.Elemento.*;
import static org.junit.jupiter.api.Assertions.*;

class RegrasReacaoTest {
    @Test void tabelaSimetrica(){
        for(var a:Elemento.values())for(var b:Elemento.values()){
            assertEquals(RegrasReacao.reagir(a,b),RegrasReacao.reagir(b,a));
            if(a==b)assertNull(RegrasReacao.reagir(a,b));else assertNotNull(RegrasReacao.reagir(a,b));}
        assertNull(RegrasReacao.reagir(null,FOGO));
    }
    @Test void seisReacoesDistintas(){
        assertEquals(Reacao.DERRETER,RegrasReacao.reagir(GELO,FOGO));assertEquals(Reacao.VAPORIZAR,RegrasReacao.reagir(FOGO,AGUA));
        assertEquals(Reacao.SOBRECARGA,RegrasReacao.reagir(FOGO,RAIO));assertEquals(Reacao.ELETROCUTAR,RegrasReacao.reagir(AGUA,RAIO));
        assertEquals(Reacao.CONGELAR,RegrasReacao.reagir(AGUA,GELO));assertEquals(Reacao.SUPERCONDUTOR,RegrasReacao.reagir(GELO,RAIO));
    }
    @Test void amplificadoras(){
        assertEquals(2,RegrasReacao.multiplicador(Reacao.DERRETER,FOGO));assertEquals(1.5,RegrasReacao.multiplicador(Reacao.DERRETER,GELO));
        assertEquals(2,RegrasReacao.multiplicador(Reacao.VAPORIZAR,AGUA));assertEquals(1,RegrasReacao.multiplicador(Reacao.SOBRECARGA,RAIO));
        assertEquals(9,RegrasReacao.sobrecarga(10),1e-9);
    }
}
