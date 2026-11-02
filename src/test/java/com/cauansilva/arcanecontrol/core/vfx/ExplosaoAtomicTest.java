package com.cauansilva.arcanecontrol.core.vfx;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ExplosaoAtomicTest {
    @Test void explosaoCresceSustentaEDissipa(){
        assertEquals(0,ExplosaoAtomic.estado(89).coluna());
        assertTrue(ExplosaoAtomic.estado(100).raio()>6);assertTrue(ExplosaoAtomic.estado(115).altura()>60);
        assertTrue(ExplosaoAtomic.estado(150).coluna()>.9);assertTrue(ExplosaoAtomic.estado(220).coluna()<.2);
        assertEquals(0,ExplosaoAtomic.estado(240).coluna());assertEquals(0,ExplosaoAtomic.estado(Double.NaN).coluna());
    }
}
