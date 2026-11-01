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
    @Test void dezSonsVorbisMonoExistemEUsamLegendasDosDoisIdiomas()throws Exception{
        var defs=json("/assets/arcanecontrol/sounds.json");assertEquals(10,defs.size());
        var pt=json("/assets/arcanecontrol/lang/pt_br.json");var en=json("/assets/arcanecontrol/lang/en_us.json");
        for(var entry:defs.entrySet()){
            var d=entry.getValue().getAsJsonObject();String subtitle=d.get("subtitle").getAsString();assertTrue(pt.has(subtitle)&&en.has(subtitle));
            var sounds=d.getAsJsonArray("sounds");assertEquals(1,sounds.size());var sound=sounds.get(0).getAsJsonObject();
            assertTrue(sound.get("attenuation_distance").getAsInt()<=64);
            String path="/assets/arcanecontrol/sounds/"+sound.get("name").getAsString().split(":")[1]+".ogg";
            try(var in=getClass().getResourceAsStream(path)){
                assertNotNull(in,path);byte[] bytes=in.readAllBytes();assertEquals("OggS",new String(bytes,0,4,StandardCharsets.US_ASCII));
                int pos=-1;for(int i=0;i<bytes.length-16;i++)if(bytes[i]==1&&new String(bytes,i+1,6,StandardCharsets.US_ASCII).equals("vorbis")){pos=i;break;}
                assertTrue(pos>=0);assertEquals(1,bytes[pos+11]);assertEquals(48000,ByteBuffer.wrap(bytes,pos+12,4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            }
        }
    }
    @Test void cuesSaoSincronizadosComImpactosERegistrados()throws Exception{
        var names=json("/assets/arcanecontrol/sounds.json").keySet();
        assertEquals(90,RoteiroAudio.atomic().get(1).atraso());assertEquals(18,RoteiroAudio.divider().get(1).atraso());assertEquals(220,RoteiroAudio.singularidade().get(1).atraso());
        for(var list:List.of(RoteiroAudio.atomic(),RoteiroAudio.divider(),RoteiroAudio.singularidade()))for(var cue:list){assertTrue(names.contains(cue.nome()));assertTrue(cue.atraso()>=0&&cue.vida()>0&&cue.volume()>0&&cue.volume()<=1);}
    }
    @Test void laminaTemOssoIndependenteDaGuarda(){
        assertDoesNotThrow(()->{
            var geo=json("/assets/arcanecontrol/geo/asta/matadora.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            var b=new HashMap<String,JsonObject>();for(var v:geo.getAsJsonArray("bones")){var o=v.getAsJsonObject();b.put(o.get("name").getAsString(),o);}
            assertEquals("root",b.get("blade").get("parent").getAsString());assertEquals("blade",b.get("energia_divider").get("parent").getAsString());
            assertEquals(7,b.get("root").getAsJsonArray("cubes").size());assertEquals(6.2,b.get("blade").getAsJsonArray("pivot").get(1).getAsDouble(),1e-9);
        });
    }
}
