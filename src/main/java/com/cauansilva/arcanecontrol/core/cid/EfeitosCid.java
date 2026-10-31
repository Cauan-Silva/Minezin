package com.cauansilva.arcanecontrol.core.cid;

import com.cauansilva.arcanecontrol.core.client.render.*;
import com.cauansilva.arcanecontrol.core.client.AnimacoesGojo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;

@Mod.EventBusSubscriber(modid="arcanecontrol", value=Dist.CLIENT)
public final class EfeitosCid {
    private record Efeito(int tipo, UUID jogador, Vec3 origem, Vec3 direcao, long inicio, Object mundo) {}
    private static final List<Efeito> EFEITOS = new ArrayList<>();
    public static void iniciar(int tipo, UUID jogador, Vec3 origem, Vec3 direcao) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if(tipo==15){
            EFEITOS.removeIf(f->f.jogador.equals(jogador));
            if(mc.player!=null && mc.player.getUUID().equals(jogador)) GerenciadorCutscene3D.finalizarCutscene();
            return;
        }
        if (EFEITOS.size() >= 24) EFEITOS.remove(0);
        EFEITOS.add(new Efeito(tipo,jogador,origem,direcao,mc.level.getGameTime(),mc.level));
        AnimacoesGojo.tocarCid(jogador,tipo);
        if (tipo == 14 && mc.player != null && mc.player.getUUID().equals(jogador))
            GerenciadorCutscene3D.iniciarCutsceneLocal(14,140);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        var mc=Minecraft.getInstance();
        EFEITOS.removeIf(f -> mc.level == null || f.mundo != mc.level || mc.level.getGameTime()-f.inicio > (f.tipo==14?140:25)
                || mc.level.getPlayerByUUID(f.jogador)==null || !mc.level.getPlayerByUUID(f.jogador).isAlive());
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e) { EFEITOS.clear(); }
    private static void linha(PoseStack pose, Vec3 a, Vec3 b, float raio, int alpha) {
        Vec3 d=b.subtract(a);
        if(d.lengthSqr()<.00001) return;
        pose.pushPose();
        pose.translate(a.x,a.y,a.z);
        pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,0,1),new Vector3f((float)d.x,(float)d.y,(float)d.z).normalize()));
        RenderizadorModelos3D.desenharFeixe3D(pose,raio,(float)d.length(),150,70,255,alpha,8);
        pose.popPose();
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null) return;
        Vec3 camera=event.getCamera().getPosition();
        PoseStack pose=event.getPoseStack();
        for(Efeito f:EFEITOS) {
            if(f.mundo!=mc.level || f.origem.distanceToSqr(camera)>128*128) continue;
            double t=mc.level.getGameTime()-f.inicio+event.getPartialTick();
            pose.pushPose();
            Vec3 pos=f.origem.subtract(camera);pose.translate(pos.x,pos.y,pos.z);
            if(f.tipo==14) {
                double carga=Math.min(1,t/90), depois=Math.max(0,t-90), fade=1-Math.min(1,depois/50);
                float raio=(float)(t<90 ? 1.2+2*carga : 3+depois*.35);
                RenderizadorModelos3D.desenharAnel3D(pose,raio,raio+.12f,160,55,255,(int)(110*fade),64);
                for(int j=0;j<6;j++) {
                    Vec3 anterior=null;
                    for(int i=0;i<=10;i++) {
                        double h=i*(t<90?.45:1.5), ang=j*Math.PI/3+h*.8-t*.07;
                        double r=t<90 ? (1.8-.9*carga)*(1-h/8) : .6+depois*.025;
                        Vec3 atual=new Vec3(Math.cos(ang)*r,h,Math.sin(ang)*r);
                        if(anterior!=null) linha(pose,anterior,atual,(float)(.025+.04*carga),(int)(100*fade));
                        anterior=atual;
                    }
                }
                if(t>=90) {
                    linha(pose,Vec3.ZERO,new Vec3(0,30,0),(float)(1.8*fade),120);
                    for(int i=0;i<12;i++) {
                        double a=i*Math.PI/6;
                        linha(pose,new Vec3(Math.cos(a)*raio,0,Math.sin(a)*raio),
                                new Vec3(Math.cos(a)*raio,4*fade,Math.sin(a)*raio),.035f,(int)(110*fade));
                    }
                }
            } else if(f.tipo==12) {
                double fade=1-Math.min(1,t/25);
                var jogador=mc.level.getPlayerByUUID(f.jogador);
                Vec3 destino=jogador==null?f.direcao.scale(8):jogador.position().subtract(f.origem);
                for(int i=0;i<5;i++) {
                    double altura=.2+i*.35;
                    linha(pose,new Vec3(0,altura,0),destino.add(0,altura,0),(float)(.035*fade),(int)(100*fade));
                }
            } else if(f.tipo==13) {
                double fade=1-Math.min(1,t/25);
                for(int j=0;j<8;j++) {
                    double ang=j*Math.PI/4;
                    Vec3 anterior=new Vec3(0,1,0);
                    for(int i=1;i<=12;i++) {
                        double r=i*.7;
                        Vec3 atual=new Vec3(Math.cos(ang)*r,.5+Math.sin(i*.6-t*.2)*.3,Math.sin(ang)*r);
                        linha(pose,anterior,atual,(float)(.035*fade),(int)(150*fade));
                        anterior=atual;
                    }
                }
            } else {
                Vec3 frente=new Vec3(f.direcao.x,0,f.direcao.z).normalize(), lado=frente.cross(new Vec3(0,1,0));
                double progresso=Math.min(1,t/25), fade=1-progresso;
                Vec3 anterior=null;
                for(int i=0;i<=24;i++) {
                    double a=-1.2+i*.1;
                    Vec3 atual=frente.scale(Math.cos(a)*(2+progresso*3)).add(lado.scale(Math.sin(a)*(2+progresso*3))).add(0,1+Math.sin(a)*.8,0);
                    if(anterior!=null) linha(pose,anterior,atual,(float)(.09*fade),(int)(140*fade));
                    anterior=atual;
                }
            }
            pose.popPose();
        }
    }
    @SubscribeEvent public static void hud(RenderGuiOverlayEvent.Post e) {
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.options.hideGui || !e.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id())) return;
        if(GerenciadorCutscene3D.cutsceneTipo==14) {
            int t=GerenciadorCutscene3D.cutsceneTick;
            e.getGuiGraphics().drawCenteredString(mc.font,t<90?"I... AM...":"ATOMIC",e.getWindow().getGuiScaledWidth()/2,
                    e.getWindow().getGuiScaledHeight()-35,0xDDCCFF);
        } else if(mc.player.getMainHandItem().is(EspadaCid.ESPADA.get())) {
            e.getGuiGraphics().fill(8,8,285,47,0xDD100A1C);
            e.getGuiGraphics().drawString(mc.font,"SHADOW • Cid Kagenou",14,14,0xCC99FF);
            e.getGuiGraphics().drawString(mc.font,"[Z] Corte [X] Passo [C] Fios [V] Atomic",14,30,0xE0D8EE);
        }
    }
}
