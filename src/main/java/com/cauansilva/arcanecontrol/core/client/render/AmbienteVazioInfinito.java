package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Modifica apenas o frame atual. Sair da area ou encerrar a tecnica restaura o ambiente. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class AmbienteVazioInfinito {
    private record Dominio(double tempo,double presenca) {}
    private static Dominio consultar(Vec3 camera,float parcial) {
        var mc=Minecraft.getInstance();
        if(mc.level==null || !RenderGojo.ativo() || !ConfigCliente.AMBIENTE_DOMINIO.get()) return new Dominio(0,0);
        Dominio melhor=new Dominio(0,0);
        for(var ef:GerenciadorCutscene3D.efeitos()) {
            if(ef.tipo!=5 || ef.mundo!=mc.level) continue;
            double t=ef.tickAtual+parcial;
            double presenca=PerfilVfxGojo.presencaDominio(t,camera.distanceTo(ef.origem));
            if(presenca>melhor.presenca) melhor=new Dominio(t,presenca);
        }
        return melhor;
    }
    @SubscribeEvent public static void ceu(RenderLevelStageEvent e) {
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Dominio d=consultar(e.getCamera().getPosition(),e.getPartialTick());
        if(d.presenca>.001) RenderGojo.ceu(e.getPoseStack(),e.getCamera().getPosition(),d.tempo,(float)d.presenca);
    }
    @SubscribeEvent public static void cor(ViewportEvent.ComputeFogColor e) {
        if(e.getCamera().getFluidInCamera()!=net.minecraft.world.level.material.FogType.NONE) return;
        double p=consultar(e.getCamera().getPosition(),(float)e.getPartialTick()).presenca;
        e.setRed((float)(e.getRed()*(1-p)+.003*p));
        e.setGreen((float)(e.getGreen()*(1-p)+.007*p));
        e.setBlue((float)(e.getBlue()*(1-p)+.025*p));
    }
    @SubscribeEvent public static void neblina(ViewportEvent.RenderFog e) {
        if(e.getType()!=net.minecraft.world.level.material.FogType.NONE) return;
        double p=consultar(e.getCamera().getPosition(),(float)e.getPartialTick()).presenca;
        if(p<.001) return;
        e.setNearPlaneDistance((float)(e.getNearPlaneDistance()*(1-p)+2*p));
        e.setFarPlaneDistance((float)(e.getFarPlaneDistance()*(1-p)+22*p));
        e.setCanceled(true);
    }
}
