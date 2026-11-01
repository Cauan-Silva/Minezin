package com.cauansilva.arcanecontrol.core.rpg;
import com.cauansilva.arcanecontrol.core.rede.*;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RpgRedeTest {
    @Test void efeitoDeChefeTransportaUuidEExtremidadesSemDependerDePlayer(){
        var pacote=new PacoteEfeitoDespertar(java.util.UUID.randomUUID(),103,new net.minecraft.world.phys.Vec3(1.25,64,-10),new net.minecraft.world.phys.Vec3(7,65,8),30,3);
        var buf=new FriendlyByteBuf(Unpooled.buffer());
        try{PacoteEfeitoDespertar.encode(pacote,buf);assertEquals(pacote,PacoteEfeitoDespertar.decode(buf));assertEquals(0,buf.readableBytes());}finally{buf.release();}
    }
    @Test void nbtMantemProgressaoManaECooldownsSeparados(){
        var raiz=new net.minecraft.nbt.CompoundTag();var gojo=MestriaRpg.migrar(900,true,true,true);var frieren=new MestriaRpg(4900,127,false);
        PerfilRpgNbt.salvar(raiz,1,gojo);PerfilRpgNbt.salvar(raiz,3,frieren);raiz.putDouble("mana",47.5);
        var cds=new RecargasRpg();cds.iniciar(PersonagemRpg.SHADOW,3,1000,1800,1);PerfilRpgNbt.salvarRecargas(raiz,cds);
        var buf=new FriendlyByteBuf(Unpooled.buffer());
        try{
            buf.writeNbt(raiz);var salvo=buf.readNbt();assertNotNull(salvo);
            assertEquals(900,PerfilRpgNbt.ler(salvo,1).xp());assertEquals(4900,PerfilRpgNbt.ler(salvo,3).xp());assertEquals(0,PerfilRpgNbt.ler(salvo,2).xp());
            assertTrue(PerfilRpgNbt.ler(salvo,1).podeUsarSlot(3));assertFalse(PerfilRpgNbt.ler(salvo,3).despertarLiberado());
            assertEquals(47.5,PerfilRpgNbt.mana(salvo).atual());assertEquals(1700,PerfilRpgNbt.recargas(salvo).restante(PersonagemRpg.SHADOW,3,1100));
            var copia=salvo.copy();PerfilRpgNbt.salvar(copia,1,new MestriaRpg());assertEquals(900,PerfilRpgNbt.ler(salvo,1).xp());
        }finally{buf.release();}
    }
    @Test void nbtDanificadoNaoConcedePoderesNemManaInfinita(){
        var raiz=new net.minecraft.nbt.CompoundTag();var kit=new net.minecraft.nbt.CompoundTag();kit.putInt("xp",0);kit.putInt("nos",255);kit.putBoolean("despertar",true);raiz.put("kit_1",kit);raiz.putDouble("mana",Double.NaN);
        assertEquals(0,PerfilRpgNbt.ler(raiz,1).nos());assertFalse(PerfilRpgNbt.ler(raiz,1).despertarLiberado());assertEquals(0,PerfilRpgNbt.mana(raiz).atual());
        assertThrows(IllegalArgumentException.class,()->PerfilRpgNbt.ler(raiz,99));
    }
    @Test void snapshotComPerfisECooldownsFazRoundTrip(){
        int[] xp=new int[15],nos=new int[15];boolean[] g=new boolean[15];xp[0]=900;xp[2]=4900;nos[2]=127;
        var pacote=new PacoteEstadoRpg(3,7,xp,nos,g,72,new int[]{10,20,30,40,0},new int[]{30,160,180,1200,0},false,true,true,350,true);
        xp[0]=0;assertEquals(900,pacote.xp()[0]);
        var buf=new FriendlyByteBuf(Unpooled.buffer());
        try{
            PacoteEstadoRpg.encode(pacote,buf);var lido=PacoteEstadoRpg.decode(buf);
            assertEquals(3,lido.ativo());assertEquals(7,lido.kits());assertArrayEquals(pacote.xp(),lido.xp());assertArrayEquals(pacote.nos(),lido.nos());
            assertArrayEquals(pacote.recargas(),lido.recargas());assertEquals(72,lido.mana());assertTrue(lido.dominioLegado());assertTrue(lido.conjuntoCompleto());assertEquals(350,lido.formaTicks());assertTrue(lido.exclusivoUsado());assertEquals(0,buf.readableBytes());
        }finally{buf.release();}
    }
    @Test void snapshotRejeitaTamanhosInvalidosECompraSoTransportaIds(){
        assertThrows(IllegalArgumentException.class,()->new PacoteEstadoRpg(1,1,new int[1000],new int[15],new boolean[15],100,new int[5],new int[5],false,false,false));
        var buf=new FriendlyByteBuf(Unpooled.buffer());
        try{var p=new PacoteAcaoRpg(1,3,4);PacoteAcaoRpg.encode(p,buf);assertEquals(p,PacoteAcaoRpg.decode(buf));assertEquals(0,buf.readableBytes());}finally{buf.release();}
    }
}
