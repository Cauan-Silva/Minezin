package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Presentation only. Scheduled from accepted server effects; never applies gameplay changes. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class ImpactosAnime {
    private record Impacto(UUID dono,Object mundo,Vec3 centro,long inicio,int cor,double forca,double raio,int kit){}
    private static final List<Impacto> ATIVOS=new ArrayList<>();
    private ImpactosAnime() {}
    public static void programar(UUID dono,Vec3 centro,int atraso,int cor,double forca,double raio,int kit){
        var mc=Minecraft.getInstance();if(mc.level==null||atraso<0||atraso>280||!Double.isFinite(centro.lengthSqr()))return;
        if(ATIVOS.size()>=48)ATIVOS.remove(0);
        ATIVOS.add(new Impacto(dono,mc.level,centro,mc.level.getGameTime()+atraso,cor,PerfilCinematico.limitar(forca),Math.max(1,Math.min(48,raio)),kit));
    }
    public static void cancelar(UUID dono){ATIVOS.removeIf(f->f.dono.equals(dono));}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        ATIVOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=24
                ||mc.level.getGameTime()<f.inicio&&(mc.level.getPlayerByUUID(f.dono)==null||!mc.level.getPlayerByUUID(f.dono).isAlive()));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ATIVOS.clear();}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.isPaused()||!ConfigCliente.CINEMATICAS.get())return;
        double trauma=0,t=mc.level.getGameTime()+e.getPartialTick();
        for(var f:ATIVOS)if(f.mundo==mc.level&&!(f.dono.equals(mc.player.getUUID())&&GerenciadorCutscene3D.cutsceneTipo!=0))
            trauma=Math.max(trauma,PerfilCinematico.trauma(t-f.inicio,mc.gameRenderer.getMainCamera().getPosition().distanceTo(f.centro),f.raio,f.forca));
        if(ConfigCliente.VFX_REDUZIDOS.get())trauma*=.35;
        double peso=trauma*trauma;
        e.setPitch(e.getPitch()+(float)(Math.sin(t*2.7)*peso*1.4));
        e.setYaw(e.getYaw()+(float)(Math.sin(t*2.1+.8)*peso*.8));
        e.setRoll(e.getRoll()+(float)(Math.cos(t*1.9)*peso*.65));
    }
    private static boolean visivel(Impacto f,Vec3 camera){
        var mc=Minecraft.getInstance();Vec3 dir=f.centro.subtract(camera);
        if(dir.lengthSqr()>Math.min(32,f.raio)*Math.min(32,f.raio))return false;
        if(dir.lengthSqr()>4&&dir.normalize().dot(new Vec3(mc.gameRenderer.getMainCamera().getLookVector()))<.2)return false;
        var hit=mc.level.clip(new net.minecraft.world.level.ClipContext(camera,f.centro,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,mc.player));
        return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS||hit.getLocation().distanceToSqr(f.centro)<2;
    }
    @SubscribeEvent public static void quadro(RenderGuiOverlayEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.options.hideGui||!ConfigCliente.IMPACT_FRAMES.get()||ConfigCliente.VFX_REDUZIDOS.get()
                ||!e.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id()))return;
        double agora=mc.level.getGameTime()+e.getPartialTick();Impacto escolhido=null;
        for(var f:ATIVOS)if(f.mundo==mc.level&&f.forca>=.8&&(f.kit!=2||ConfigCliente.CLARAO.get())&&(f.kit!=1||ConfigCliente.CLARAO_GOJO.get())&&agora>=f.inicio&&agora<f.inicio+2&&visivel(f,mc.gameRenderer.getMainCamera().getPosition()))
            if(escolhido==null||f.forca>escolhido.forca)escolhido=f;
        if(escolhido==null)return;
        var g=e.getGuiGraphics();int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();
        // One brief exposure, then graphic speed lines. Never freeze the simulation or player input.
        g.fill(0,0,w,h,agora-escolhido.inicio<.6?0x8CEEF1FF:0x88040812);
        var s=g.pose();s.pushPose();s.translate(w/2f,h/2f,0);
        try{for(int i=0;i<24;i++){
            s.pushPose();s.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(i*15f+(i%3)*3));
            g.fill(w/7+(i%4)*5,-1,w/2+(i%5)*13,i%3==0?2:1,0xCFFFFFFF);s.popPose();
        }}finally{s.popPose();}
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        double agora=mc.level.getGameTime()+e.getPartialTick();Vec3 camera=e.getCamera().getPosition();int count=0;
        for(var f:ATIVOS){double t=agora-f.inicio;if(f.mundo!=mc.level||t<0||t>=24||camera.distanceToSqr(f.centro)>96*96)continue;
            if(count++>=8)break;if(!DesenhoMagia.iniciar(agora))continue;
            var s=e.getPoseStack();s.pushPose();s.translate(f.centro.x-camera.x,f.centro.y-camera.y,f.centro.z-camera.z);
            try{DesenhoMagia.lote(s,1,true,()->{
                float alpha=(float)(PerfilCinematico.impacto(t)*f.forca);
                GeometriaCinematica.explosao(s,camera.subtract(f.centro),t,Math.min(22,f.raio*.55),f.cor,alpha);
                if(t<10)GeometriaCinematica.raios(s,camera.subtract(f.centro),new Vec3(0,.3,0),Math.min(14,f.raio*.4),t,f.cor,alpha);
            });}finally{s.popPose();DesenhoMagia.terminar();}
        }
    }
}
