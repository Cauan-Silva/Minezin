package com.cauansilva.arcanecontrol.core.frieren;
import com.cauansilva.arcanecontrol.core.client.render.*;
import com.cauansilva.arcanecontrol.core.client.*;
import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class EfeitosFrieren {
    public record Efeito(int tipo,UUID jogador,Vec3 origem,Vec3 alvo,long inicio,Object mundo){}
    private static final List<Efeito> EFEITOS=new ArrayList<>();
    public static void iniciar(int tipo,UUID id,Vec3 origem,Vec3 alvo){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(tipo==25||tipo==26){
            EFEITOS.removeIf(e->e.jogador.equals(id)&&(tipo==25||e.tipo==22));
            if(tipo==25&&mc.player!=null&&mc.player.getUUID().equals(id)&&GerenciadorCutscene3D.cutsceneTipo==24)GerenciadorCutscene3D.finalizarCutscene();
            if(tipo==26)EmissoresAnime.pulso(id,origem.add(0,1,0),0xD5EFFF,18,3,.1,18);
            return;
        }
        if(tipo<21||tipo>24)return;if(EFEITOS.size()>=32)EFEITOS.remove(0);
        EFEITOS.add(new Efeito(tipo,id,origem,alvo,mc.level.getGameTime(),mc.level));
        AnimacoesGojo.tocarFrieren(id,tipo);
        if(tipo==21)EmissoresAnime.pulso(id,alvo,0xDFEBFF,12,2,.1,12);
        if(tipo==24&&mc.player!=null&&mc.player.getUUID().equals(id))GerenciadorCutscene3D.iniciarCutsceneLocal(24,200);
    }
    public static Vec3 centroUltimate(UUID id){for(var e:EFEITOS)if(e.tipo==24&&e.jogador.equals(id))return e.origem;return null;}
    private static int duracao(int tipo){return switch(tipo){case 21->12;case 22->80;case 23->100;case 24->200;default->0;};}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        EFEITOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=duracao(f.tipo)
                ||mc.level.getPlayerByUUID(f.jogador)==null||!mc.level.getPlayerByUUID(f.jogador).isAlive());
        if(mc.level==null||mc.isPaused())return;
        for(var f:EFEITOS){long t=mc.level.getGameTime()-f.inicio;
            if(f.tipo==24&&t==60)for(int j=0;j<6;j++)EmissoresAnime.pulso(f.jogador,f.origem.add(Math.cos(j*Math.PI/3)*4,0,Math.sin(j*Math.PI/3)*4),0xE7D09C,6,1,.12,24);
            if(f.tipo==24&&t==170)EmissoresAnime.pulso(f.jogador,f.origem,0xF2DEB1,80,7,.18,24);
            if(f.tipo==23&&t<40&&t%8==0){var p=mc.level.getPlayerByUUID(f.jogador);EmissoresAnime.pulso(f.jogador,p.position(),0xA7DEFF,4,.3,.08,12);}
        }
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null)return;int n=0;Vec3 cam=e.getCamera().getPosition();
        for(var f:EFEITOS){if(f.mundo!=mc.level||f.origem.distanceToSqr(cam)>160*160||n++>=8)continue;
            var pose=e.getPoseStack();pose.pushPose();var p=f.origem.subtract(cam);pose.translate(p.x,p.y,p.z);
            RenderMagiasFrieren.desenhar(pose,f,mc.level.getGameTime()-f.inicio+e.getPartialTick(),cam.subtract(f.origem),e.getPartialTick());
            pose.popPose();
        }
    }
    @SubscribeEvent public static void hud(RenderGuiOverlayEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||!e.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id()))return;
        if(GerenciadorCutscene3D.cutsceneTipo==24){e.getGuiGraphics().drawCenteredString(mc.font,GerenciadorCutscene3D.cutsceneTick<60?"Singularidade...":"Buraco Negro",e.getWindow().getGuiScaledWidth()/2,e.getWindow().getGuiScaledHeight()-30,0xEEDDB7);return;}
        if(PoderesCliente.ativo()!=PoderAnime.FRIEREN)return;
        e.getGuiGraphics().fill(8,8,306,47,0xDC101321);
        e.getGuiGraphics().drawString(mc.font,"FRIEREN • [P] Poderes",14,14,0xE6D19C);
        e.getGuiGraphics().drawString(mc.font,"[Z] Raio [X] Barreira [C] Impulso [V] Buraco Negro",14,30,0xDDE6F2);
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){EFEITOS.clear();}
}
