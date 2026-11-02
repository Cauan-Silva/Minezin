package com.cauansilva.arcanecontrol.core.cid;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class FontesStunAtomicTest {
    @Test void fimDeUmaAtomicNaoLiberaAlvoDeOutra(){var a=UUID.randomUUID();var b=UUID.randomUUID();var f=new FontesStunAtomic();f.renovar(a,100);f.renovar(b,100);f.remover(a);assertTrue(f.ativa(101));f.remover(b);assertFalse(f.ativa(101));}
    @Test void fonteSemRenovacaoExpiraSemCongelarEntidadeParaSempre(){var a=UUID.randomUUID();var f=new FontesStunAtomic();f.renovar(a,100);assertTrue(f.ativa(101));assertFalse(f.ativa(102));f.renovar(a,103);assertTrue(f.ativa(104));}
}
