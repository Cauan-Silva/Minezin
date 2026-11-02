package com.cauansilva.arcanecontrol.core.vfx;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RelogioAtomicTest {
    @Test void relogioLongoTemImpactoUnicoEMapeamentoReversivel(){
        assertEquals(420,RelogioAtomic.IMPACTO);assertEquals(780,RelogioAtomic.FIM);assertEquals(90,RelogioAtomic.visual(420));assertEquals(240,RelogioAtomic.visual(780));
        double prev=-1;for(double t=0;t<=780;t+=.25){double v=RelogioAtomic.visual(t);assertTrue(v>=prev);assertEquals(t,RelogioAtomic.real(v),1e-8);prev=v;}
        assertTrue(RelogioAtomic.detonar(419,420));assertFalse(RelogioAtomic.detonar(420,421));
        assertTrue(PerfilAtomic.cancelarPorMovimento(RelogioAtomic.visual(300),10));assertFalse(PerfilAtomic.cancelarPorMovimento(RelogioAtomic.visual(420),10));
    }
    @Test void letrasSilencioEAudioConvergemNoMesmoImpacto(){
        assertEquals("I AM",RelogioAtomic.letreiro(RelogioAtomic.visual(320)));assertEquals("ATOMIC",RelogioAtomic.letreiro(87));assertEquals("",RelogioAtomic.letreiro(89));
        assertEquals(RelogioAtomic.IMPACTO,com.cauansilva.arcanecontrol.core.audio.RoteiroAudio.atomic().get(1).atraso());
        assertTrue(RelogioAtomic.OCUPADO>RelogioAtomic.IMPACTO);assertTrue(RelogioAtomic.FIM<800);
    }
    @Test void poseDaAtomicTerminaDepoisDoImpactoEAcompanhaORelogio()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/cid.json")){
            var d=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var a=d.getAsJsonObject("animations").getAsJsonObject("cid_4");assertEquals(RelogioAtomic.real(100)/20,a.get("animation_length").getAsDouble(),.0001);
            var keys=a.getAsJsonObject("bones").getAsJsonObject("right_arm").getAsJsonObject("rotation");assertTrue(keys.has("21.0"));
        }
    }
    @Test void exportaCameraDoRoteiroRealSemValoresInvalidos()throws Exception{
        var frames=new java.util.ArrayList<Object>();
        for(int t=0;t<RelogioAtomic.FIM;t++){
            var q=com.cauansilva.arcanecontrol.core.client.render.RoteiroGojo.amostrar(14,RelogioAtomic.visual(t));
            assertTrue(Double.isFinite(q.x()+q.y()+q.z()+q.alvoAltura()+q.fov()));assertTrue(q.fov()>.5&&q.fov()<1.5);frames.add(java.util.Map.of("camera",q,"visual",RelogioAtomic.visual(t),"peso",RoteiroCinemaAtomic.peso(RelogioAtomic.visual(t),0,true),"titulo",RelogioAtomic.letreiro(RelogioAtomic.visual(t))));
        }
        java.nio.file.Files.writeString(java.nio.file.Path.of("build/cid-reference-camera.json"),new com.google.gson.Gson().toJson(frames));
    }
}
