package com.cauansilva.arcanecontrol.core.subaru;
import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rpg.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/** Kit 16 (Subaru): ids, vetores de estado com 16 kits e compatibilidade com saves de 15 kits. */
class KitSubaruTest {
    @Test void idsDoKit(){
        assertEquals(16,PoderAnime.SUBARU.id);assertEquals(PoderAnime.KITS,PoderAnime.SUBARU.id);
        assertEquals(151,PoderAnime.SUBARU.tecnica(0));assertEquals(PoderAnime.SUBARU,PoderAnime.daTecnica(155));
        assertEquals(PersonagemRpg.SUBARU,PersonagemRpg.porId(16));
        for(var k:PoderAnime.values())assertTrue(k.id<=PoderAnime.KITS,"todo kit cabe nos vetores");
    }
    @Test void saveAntigoDe15Kits(){
        var antigo=new EstadoPoderes(1,1|PoderAnime.SUBARU.bit(),0,new int[15],new int[75]);
        assertEquals(PoderAnime.KITS,antigo.xps().length);assertEquals(PoderAnime.KITS*5,antigo.usos().length);
        assertTrue(antigo.tem(PoderAnime.SUBARU));assertEquals(0,antigo.xp(PoderAnime.SUBARU));
        antigo.ganharXp(PoderAnime.SUBARU,50);assertEquals(50,antigo.xp(PoderAnime.SUBARU));
    }
    @Test void recargasDoKit16(){
        var r=new RecargasRpg(new long[150],new int[150]);
        assertTrue(r.iniciar(PersonagemRpg.SUBARU,3,100,500,1));assertEquals(500,r.restante(PersonagemRpg.SUBARU,3,100));
        assertEquals(0,r.restante(PersonagemRpg.EDWARD,3,100),"nao invade o kit 15");
        assertEquals(PoderAnime.KITS*10,r.prazos().length);
    }
}
