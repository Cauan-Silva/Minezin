package com.cauansilva.arcanecontrol.core.client;
import com.cauansilva.arcanecontrol.core.audio.RoteiroAudio;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class SfxAnimeTest {
    private JsonObject json(String path)throws Exception{
        try(var in=getClass().getResourceAsStream(path)){assertNotNull(in);return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}
    }
    @Test void sonsVorbisMonoExistemEUsamLegendasDosDoisIdiomas()throws Exception{
        var defs=json("/assets/arcanecontrol/sounds.json");var registrados=com.cauansilva.arcanecontrol.core.audio.CatalogoSons.NOMES;
        assertEquals(registrados.length,defs.size());for(String n:registrados)assertTrue(defs.has(n),n);
        var pt=json("/assets/arcanecontrol/lang/pt_br.json");var en=json("/assets/arcanecontrol/lang/en_us.json");
        for(var entry:defs.entrySet()){
            var d=entry.getValue().getAsJsonObject();String subtitle=d.get("subtitle").getAsString();assertTrue(pt.has(subtitle)&&en.has(subtitle));
            // One file, or up to 3 takes of the layered engine (tools/sfx_motor.py: Minecraft picks one at random).
            var sounds=d.getAsJsonArray("sounds");assertTrue(sounds.size()>=1&&sounds.size()<=3,entry.getKey());
            for(var el:sounds){var sound=el.getAsJsonObject();
            assertTrue(sound.get("attenuation_distance").getAsInt()<=64);
            String path="/assets/arcanecontrol/sounds/"+sound.get("name").getAsString().split(":")[1]+".ogg";
            try(var in=getClass().getResourceAsStream(path)){
                assertNotNull(in,path);byte[] bytes=in.readAllBytes();assertEquals("OggS",new String(bytes,0,4,StandardCharsets.US_ASCII));
                int pos=-1;for(int i=0;i<bytes.length-16;i++)if(bytes[i]==1&&new String(bytes,i+1,6,StandardCharsets.US_ASCII).equals("vorbis")){pos=i;break;}
                assertTrue(pos>=0);assertEquals(1,bytes[pos+11]);assertEquals(48000,ByteBuffer.wrap(bytes,pos+12,4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            }}
        }
    }
    @Test void cuesSaoSincronizadosComImpactosERegistrados()throws Exception{
        var names=json("/assets/arcanecontrol/sounds.json").keySet();
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.RelogioAtomic.IMPACTO,RoteiroAudio.atomic().get(1).atraso());assertEquals(18,RoteiroAudio.divider().get(1).atraso());assertEquals(0,RoteiroAudio.singularidade().get(0).atraso());
        // Black hole: formation until ABERTURA (70), pull until COLAPSO (220); Judradjim thunder on the damage tick (25).
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.PerfilFrieren.ABERTURA,RoteiroAudio.singularidade().get(1).atraso());assertEquals(com.cauansilva.arcanecontrol.core.vfx.PerfilFrieren.COLAPSO,RoteiroAudio.singularidade().get(2).atraso());
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.FormaFrieren.QUEDA,RoteiroAudio.julgamento().get(1).atraso());
        for(var list:List.of(RoteiroAudio.atomic(),RoteiroAudio.divider(),RoteiroAudio.singularidade(),RoteiroAudio.julgamento(),RoteiroAudio.roxoMaximo()))for(var cue:list){assertTrue(names.contains(cue.nome()));assertTrue(cue.atraso()>=0&&cue.vida()>0&&cue.volume()>0&&cue.volume()<=1);}
    }
    /** Asta's sounds land on the VFX ticks, are registered, and each lives at least as long as its moment. */
    @Test void sonsDoAstaNosTicksDoVfx()throws Exception{
        var names=json("/assets/arcanecontrol/sounds.json").keySet();
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.BlackMeteoriteAsta.IMPACTO,RoteiroAudio.asta(304).get(1).atraso());
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.BlackHurricaneAsta.ESTOURO,RoteiroAudio.asta(305).get(1).atraso());
        assertEquals(com.cauansilva.arcanecontrol.core.asta.PerfilAsta.impacto(0,false),RoteiroAudio.asta(300).get(1).atraso());
        var ult=RoteiroAudio.ultAsta();assertEquals(com.cauansilva.arcanecontrol.core.vfx.UltAsta.CORTES+2,ult.size());
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.UltAsta.FINAL,ult.get(ult.size()-1).atraso());
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico.FORMA_IMPACTO,RoteiroAudio.formaAsta().get(1).atraso());
        for(int t=300;t<=308;t++){var l=RoteiroAudio.asta(t);assertNotNull(l,""+t);for(var c:l){assertTrue(names.contains(c.nome()),c.nome());assertTrue(c.vida()>0&&c.atraso()>=0);}}
        assertNull(RoteiroAudio.asta(309));
    }
    /** Cid: hits on the VFX/server ticks, the Atomic release on IMPACTO lasting until FIM, all registered. */
    @Test void sonsDoCidNosTicksDoVfx()throws Exception{
        var names=json("/assets/arcanecontrol/sounds.json").keySet();
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.PerfilCid.CORTE_IMPACTO,RoteiroAudio.cid(11).get(1).atraso());
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.PerfilCid.FIOS_IMPACTO,RoteiroAudio.cid(13).get(1).atraso());
        var a=RoteiroAudio.atomic();assertEquals(com.cauansilva.arcanecontrol.core.vfx.RelogioAtomic.IMPACTO,a.get(1).atraso());
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.RelogioAtomic.FIM,a.get(1).atraso()+a.get(1).vida());
        assertEquals(11,RoteiroAudio.chuvaCid().size(),"lamina a cada 4 ticks de 24 a 60");
        for(int t:new int[]{11,12,13,16,17,31,34,35})for(var c:RoteiroAudio.cid(t))assertTrue(names.contains(c.nome()),c.nome());
        for(var c:a)assertTrue(names.contains(c.nome()));
    }
    /** Ichigo: Bankai shout/reveal and the Mugetsu cut on their VFX ticks; every effect with sound is registered. */
    @Test void sonsDoIchigoNosTicksDoVfx()throws Exception{
        var names=json("/assets/arcanecontrol/sounds.json").keySet();
        var b=RoteiroAudio.bankai();assertEquals(com.cauansilva.arcanecontrol.core.vfx.EntradaBankai.GRITO,b.get(1).atraso());assertEquals(com.cauansilva.arcanecontrol.core.vfx.EntradaBankai.REVELA,b.get(2).atraso());
        assertEquals(com.cauansilva.arcanecontrol.core.vfx.MugetsuIchigo.GOLPE,RoteiroAudio.mugetsu().get(1).atraso());
        for(int t:new int[]{500,501,502,503,510,511,512,513,520,521,522,523,524,525,526,527})for(var c:RoteiroAudio.ichigo(t))assertTrue(names.contains(c.nome()),c.nome());
        for(var c:b)assertTrue(names.contains(c.nome()));for(var c:RoteiroAudio.mugetsu())assertTrue(names.contains(c.nome()));
        assertNull(RoteiroAudio.ichigo(530));
    }
    @Test void laminaTemOssoIndependenteDaGuarda(){
        assertDoesNotThrow(()->{
            var geo=json("/assets/arcanecontrol/geo/asta/matadora.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            var b=new HashMap<String,JsonObject>();for(var v:geo.getAsJsonArray("bones")){var o=v.getAsJsonObject();b.put(o.get("name").getAsString(),o);}
            assertEquals("root",b.get("blade").get("parent").getAsString());assertEquals("blade",b.get("energia_divider").get("parent").getAsString());
            // Grip and guard stay in root (the reference guard has more pieces now); the blade, which the Divider
            // stretches, holds nothing below its pivot.
            assertTrue(b.get("root").getAsJsonArray("cubes").size()>=7);assertEquals(6.2,b.get("blade").getAsJsonArray("pivot").get(1).getAsDouble(),1e-9);
            for(var c:b.get("blade").getAsJsonArray("cubes"))assertTrue(c.getAsJsonObject().getAsJsonArray("origin").get(1).getAsDouble()>=6.2-1e-9);
        });
    }
}
