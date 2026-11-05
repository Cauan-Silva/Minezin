package com.cauansilva.arcanecontrol.core.subaru;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
/** A Beatrice do jogo usa os ossos do modelo aprovado e a animacao vazia que o GeckoLib exige. */
class BeatriceJogoTest {
    private static String ler(String p)throws Exception{try(var in=BeatriceJogoTest.class.getResourceAsStream(p)){assertNotNull(in,p);return new String(in.readAllBytes());}}
    @Test void poseUsaOssosDoModelo()throws Exception{
        var geo=JsonParser.parseString(ler("/assets/arcanecontrol/geo/subaru/beatrice.geo.json")).getAsJsonObject();
        var nomes=new HashSet<String>();for(var b:geo.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones"))nomes.add(b.getAsJsonObject().get("name").getAsString());
        var pose=new HashMap<String,float[]>();RenderBeatrice.poseOciosa(pose,123);
        assertTrue(pose.size()>25);
        for(var n:pose.keySet())assertTrue(nomes.contains(n),"osso inexistente: "+n);
        for(var r:pose.values())for(float v:r)assertTrue(Float.isFinite(v)&&Math.abs(v)<45);
    }
    @Test void poseDaMinyaUsaOssosDoModelo()throws Exception{
        var geo=JsonParser.parseString(ler("/assets/arcanecontrol/geo/subaru/beatrice.geo.json")).getAsJsonObject();
        var nomes=new HashSet<String>();for(var b:geo.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones"))nomes.add(b.getAsJsonObject().get("name").getAsString());
        var pose=new HashMap<String,float[]>();for(double t=0;t<60;t+=.5){RenderBeatrice.poseMinya(pose,t);for(var n:pose.keySet())assertTrue(nomes.contains(n),n);
            for(var r:pose.values())for(float v:r)assertTrue(Float.isFinite(v)&&Math.abs(v)<140);}
    }
    @Test void animacaoVaziaExiste()throws Exception{
        var a=JsonParser.parseString(ler("/assets/arcanecontrol/animations/subaru/beatrice.animation.json")).getAsJsonObject();
        assertTrue(a.has("animations"));
    }
}
