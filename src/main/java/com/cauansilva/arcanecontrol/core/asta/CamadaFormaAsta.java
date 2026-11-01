package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Attachments use the same head/arm transforms as the skin and attack animations. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CamadaFormaAsta extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public CamadaFormaAsta(PlayerRenderer renderer){super(renderer);}

    @SubscribeEvent public static void registrar(EntityRenderersEvent.AddLayers e){
        for(var skin:e.getSkins()){
            PlayerRenderer renderer=e.getSkin(skin);
            if(renderer!=null)renderer.addLayer(new CamadaFormaAsta(renderer));
        }
    }

    @Override public void render(PoseStack pose,MultiBufferSource buffer,int luz,AbstractClientPlayer p,
                                 float movimento,float intensidade,float parcial,float idade,float yaw,float pitch){
        var estado=EfeitosAsta.forma(p.getUUID(),parcial);
        if(estado==null||p.isInvisible()||p.isSpectator())return;
        if(buffer instanceof MultiBufferSource.BufferSource source)source.endBatch();
        if(!DesenhoMagia.iniciar(estado.tempo()))return;
        com.mojang.blaze3d.systems.RenderSystem.depthMask(estado.alpha()>.95f);
        try{
            parte(pose,getParentModel().body,"body",estado);
            parte(pose,getParentModel().head,"head",estado);
            parte(pose,getParentModel().rightArm,"right_arm",estado);
        }finally{DesenhoMagia.terminar();}
    }

    private static void parte(PoseStack pose,ModelPart parte,String nome,EfeitosAsta.EstadoVisual estado){
        pose.pushPose();
        try{parte.translateAndRotate(pose);ModeloFormaAsta.desenhar(pose,nome,estado.tempo(),estado.alpha(),estado.idade());}
        finally{pose.popPose();}
    }
}
