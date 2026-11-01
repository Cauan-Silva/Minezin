package com.cauansilva.arcanecontrol.core.asta;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PivoFormaAstaTest {
    @Test void raizDoBracoECabecaCoincideComOPivoAnimado(){
        assertEquals(Vec3.ZERO,PivoFormaAsta.converter(new Vec3(-5,22,0),"right_arm"));
        assertEquals(Vec3.ZERO,PivoFormaAsta.converter(new Vec3(0,24,0),"head"));
        assertEquals(new Vec3(-.1875,.625,-.125),PivoFormaAsta.converter(new Vec3(-8,12,-2),"right_arm"));
        assertEquals(new Vec3(0,-.5,0),PivoFormaAsta.converter(new Vec3(0,32,0),"head"));
        assertThrows(IllegalArgumentException.class,()->PivoFormaAsta.converter(Vec3.ZERO,"unknown"));
    }
}
