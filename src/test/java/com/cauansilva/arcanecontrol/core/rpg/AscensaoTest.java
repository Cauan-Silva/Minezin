package com.cauansilva.arcanecontrol.core.rpg;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AscensaoTest {
    @Test void soNoNivelMaximo(){
        var m=new MestriaRpg();assertFalse(m.ascender());
        m.ganharXp(MestriaRpg.XP_MAXIMO);assertTrue(m.podeAscender());
    }
    @Test void renasceComBonusEMemoria(){
        var m=MestriaRpg.maximaParaTeste();double fis=new MestriaRpg().multiplicadorFisico();
        assertTrue(m.ascender());
        assertEquals(1,m.nivel());assertEquals(0,m.nos());assertFalse(m.despertarLiberado());assertEquals(1,m.ascensoes());
        assertEquals(fis+.04,m.multiplicadorFisico(),1e-9);assertEquals(3,m.vidaAdicional(),1e-9);
        m.ganharXp(100);assertEquals(110,m.xp(),"+10% XP");
    }
    @Test void despertarVoltaAoRecomprar(){
        var m=MestriaRpg.maximaParaTeste();m.ascender();m.ganharXp(MestriaRpg.XP_MAXIMO);
        for(var no:NoRpg.values())m.comprar(no);
        assertTrue(m.despertarLiberado());
    }
    @Test void nbtRoundTripETeto(){
        var m=MestriaRpg.maximaParaTeste();m.ascender();var t=new CompoundTag();PerfilRpgNbt.salvar(t,1,m);
        var l=PerfilRpgNbt.ler(t,1);assertEquals(1,l.ascensoes());assertTrue(l.memoriaDespertar());
        var x=new MestriaRpg(MestriaRpg.XP_MAXIMO,0,false,MestriaRpg.ASC_MAX,false);assertFalse(x.podeAscender());
        assertEquals(1,new MestriaRpg(0,0,false).multiplicadorRecarga(),1e-9);
    }
}
