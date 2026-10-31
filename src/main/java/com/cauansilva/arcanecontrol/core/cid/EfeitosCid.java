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
    private record Destino(Vec3 pos,long tick,Object mundo){}
    private static final Map<UUID,Destino> DESTINOS=new HashMap<>();
    private static final Map<UUID,List<UUID>> ALVOS=new HashMap<>();
    public static void vincularAlvos(UUID id,List<UUID> alvos){if(alvos.isEmpty())ALVOS.remove(id);else{if(ALVOS.size()>=64)ALVOS.clear();ALVOS.put(id,List.copyOf(alvos));}}
    public static void iniciar(int tipo, UUID jogador, Vec3 origem, Vec3 direcao) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if(tipo==16){if(DESTINOS.size()>=64)DESTINOS.clear();DESTINOS.put(jogador,new Destino(origem,mc.level.getGameTime(),mc.level));return;}
        if(tipo==15){
            PhotonAtomic.cancelar(jogador);
            AnimacoesGojo.cancelar(jogador);
            EFEITOS.removeIf(f->f.jogador.equals(jogador)&&f.tipo==14);
            if(mc.player!=null && mc.player.getUUID().equals(jogador)) GerenciadorCutscene3D.finalizarCutscene();
            return;
        }
        if (EFEITOS.size() >= 24) EFEITOS.remove(0);
        EFEITOS.add(new Efeito(tipo,jogador,origem,direcao,mc.level.getGameTime(),mc.level));
        if(tipo==14) PhotonAtomic.iniciar(jogador,origem);
        AnimacoesGojo.tocarCid(jogador,tipo);
        if (tipo == 14 && mc.player != null && mc.player.getUUID().equals(jogador))
            GerenciadorCutscene3D.iniciarCutsceneLocal(14,240);
    }
    public static Vec3 centroAtomic(UUID id){for(var f:EFEITOS)if(f.tipo==14&&f.jogador.equals(id))return f.origem;return null;}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        PhotonAtomic.tick();
        var mc=Minecraft.getInstance();
        EFEITOS.removeIf(f -> mc.level == null || f.mundo != mc.level || mc.level.getGameTime()-f.inicio > (f.tipo==14?240:f.tipo==13?48:25)
                || mc.level.getPlayerByUUID(f.jogador)==null || !mc.level.getPlayerByUUID(f.jogador).isAlive());
        if(mc.level == null) return;
        DESTINOS.entrySet().removeIf(a->a.getValue().mundo!=mc.level||mc.level.getGameTime()-a.getValue().tick>25);
        ALVOS.keySet().removeIf(id->EFEITOS.stream().noneMatch(f->f.tipo==13&&f.jogador.equals(id)));
        if(mc.isPaused())return;
        for(var f:EFEITOS) {
            long idade=mc.level.getGameTime()-f.inicio;
            if(f.tipo==11&&idade==6)com.cauansilva.arcanecontrol.core.client.render.EmissoresAnime.pulso(f.jogador,f.origem.add(f.direcao.scale(3)).add(0,1,0),0xB76BFF,16,3,.12,12);
            if(f.tipo==12&&idade==1){var d=DESTINOS.get(f.jogador);if(d!=null)com.cauansilva.arcanecontrol.core.client.render.EmissoresAnime.pulso(f.jogador,d.pos.add(0,1,0),0xB76BFF,16,2,.14,14);}
            if(f.tipo!=14 || mc.player==null || mc.player.distanceToSqr(f.origem)>96*96) continue;
            long t=mc.level.getGameTime()-f.inicio;
            if(t<90) {
                for(int i=0;i<6;i++) {
                    double ang=mc.level.random.nextDouble()*Math.PI*2, r=2+mc.level.random.nextDouble()*4;
                    Vec3 p=f.origem.add(Math.cos(ang)*r,mc.level.random.nextDouble()*5,Math.sin(ang)*r);
                    Vec3 v=f.origem.add(0,1.5,0).subtract(p).scale(.12);
                    mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,p.x,p.y,p.z,v.x,v.y,v.z);
                }
            } else if(t==90) {
                for(int i=0;i<160;i++) {
                    double ang=mc.level.random.nextDouble()*Math.PI*2, r=mc.level.random.nextDouble()*12;
                    Vec3 p=f.origem.add(Math.cos(ang)*r,.2,Math.sin(ang)*r);
                    mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,p.x,p.y,p.z,Math.cos(ang)*.6,.2+mc.level.random.nextDouble(),Math.sin(ang)*.6);
                    if(i%4==0) mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,p.x,p.y,p.z,0,.15,0);
                }
            }
        }
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e) { EFEITOS.clear();DESTINOS.clear();ALVOS.clear(); PhotonAtomic.limpar(); }
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
        int atomics=0;
        for(Efeito f:EFEITOS) {
            if(f.mundo!=mc.level || f.origem.distanceToSqr(camera)>128*128) continue;
            if(f.tipo==14 && atomics++>=4) continue;
            double t=mc.level.getGameTime()-f.inicio+event.getPartialTick();
            pose.pushPose();
            Vec3 pos=f.origem.subtract(camera);pose.translate(pos.x,pos.y,pos.z);
            Vec3 plano=f.direcao.multiply(1,0,1).normalize();
            Vec3 ancora=plano.scale(.5).add(plano.cross(new Vec3(0,1,0)).scale(.4)).add(0,1.2,0);
            var destinoDash=DESTINOS.get(f.jogador);
            if(f.tipo>=11&&f.tipo<=13&&RenderMovesetCid.desenhar(pose,f.tipo,f.jogador,f.origem,f.direcao,
                    destinoDash==null?null:destinoDash.pos,t,camera.subtract(f.origem),ALVOS.getOrDefault(f.jogador,List.of()))) {
            } else if(f.tipo==14 && RenderEnergiaAtomic.desenhar(pose,t,camera.subtract(f.origem),ancora)) {
                RenderMovesetCid.selosAtomic(pose,t,camera.subtract(f.origem));
                // O material procedural substitui as esferas/aneis lisos da Atomic.
            } else if(f.tipo==14) {
                double carga=Math.min(1,t/90), depois=Math.max(0,t-90), fade=1-Math.min(1,depois/150);
                // Contrarrotacao na carga; nucleo branco e casca violeta no impacto.
                if(t<90) {
                    for(int j=0;j<3;j++) {
                        pose.pushPose();
                        pose.translate(0,1.4+j*.3,0);
                        pose.mulPose(new Quaternionf().rotateX((float)(.5+j*.8)).rotateZ((float)(t*.025*(j%2==0?1:-1))));
                        float r=(float)(3.5-2.8*carga+j*.25);
                        RenderizadorModelos3D.desenharAnel3D(pose,r,r+.06f,190,90,255,130,48);
                        pose.popPose();
                    }
                } else {
                    float expansao=(float)(1-Math.exp(-depois/5));
                    pose.pushPose();pose.translate(0,1,0);
                    RenderizadorModelos3D.desenharEsfera3D(pose,(1+expansao*8)*(float)fade,120,35,255,(int)(100*fade),24);
                    RenderizadorModelos3D.desenharEsfera3D(pose,(1+expansao*5)*(float)fade,235,220,255,(int)(150*fade),20);
                    pose.popPose();
                    for(int j=0;j<3;j++) {
                        float onda=(float)Math.max(0,(depois-j*4)*.65);
                        if(onda<=0) continue;
                        pose.pushPose();pose.translate(0,.2+j*.4,0);
                        RenderizadorModelos3D.desenharAnel3D(pose,onda,onda+.35f,200,120,255,(int)(140*fade),64);
                        pose.popPose();
                    }
                }
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
                    pose.pushPose();
                    pose.mulPose(new Quaternionf().rotateX((float)(-Math.PI/2)));
                    RenderizadorModelos3D.desenharFeixe3D(pose,(float)(.85*fade),48,245,230,255,(int)(150*fade),24);
                    pose.popPose();
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
            if(com.cauansilva.arcanecontrol.ConfigCliente.CLARAO.get() && t>=90 && t<96) {
                int alpha=(int)(180*(1-(t-90+mc.getFrameTime())/6f));
                e.getGuiGraphics().fill(0,0,e.getWindow().getGuiScaledWidth(),e.getWindow().getGuiScaledHeight(),(Math.max(0,alpha)<<24)|0xEEDFFF);
            }
            e.getGuiGraphics().drawCenteredString(mc.font,t<90?"I... AM...":"ATOMIC",e.getWindow().getGuiScaledWidth()/2,
                    e.getWindow().getGuiScaledHeight()-35,0xDDCCFF);
        } else if(com.cauansilva.arcanecontrol.core.client.PoderesCliente.ativo()==com.cauansilva.arcanecontrol.core.poderes.PoderAnime.CID) {
            e.getGuiGraphics().fill(8,8,285,47,0xDD100A1C);
            e.getGuiGraphics().drawString(mc.font,"SHADOW • [P] Poderes",14,14,0xCC99FF);
            e.getGuiGraphics().drawString(mc.font,"[Z] Corte [X] Passo [C] Fios [V] Atomic",14,30,0xE0D8EE);
        }
    }
}
