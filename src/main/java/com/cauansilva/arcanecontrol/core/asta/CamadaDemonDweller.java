package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.vfx.UltAsta;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Demon-Dweller Sword during Asta's ultimate: it rises hilt first out of the open grimoire (ticks 8..18) and is then
 * held in the left hand until it sinks back into the book (UltAsta.espadaEsquerda). Drawn with the sword item's
 * animatable, the Demon-Dweller geometry and its glowing stars.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CamadaDemonDweller extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation GEO=id("geo/asta/demon_dweller.geo.json"),TEXTURA=id("textures/armor/trajes/demon_dweller.png"),GLOW=id("textures/armor/trajes/demon_dweller_glowmask.png");
    private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
    private static final class Modelo extends GeoModel<EspadaAsta>{
        @Override public ResourceLocation getModelResource(EspadaAsta a){return GEO;}
        @Override public ResourceLocation getTextureResource(EspadaAsta a){return TEXTURA;}
        @Override public ResourceLocation getAnimationResource(EspadaAsta a){return id("animations/asta/matadora.animation.json");}
    }
    private static final class Render extends GeoItemRenderer<EspadaAsta>{
        Render(){super(new Modelo());addRenderLayer(new GeoRenderLayer<>(this){
            @Override public void render(PoseStack pose,EspadaAsta item,BakedGeoModel model,RenderType base,MultiBufferSource buffer,com.mojang.blaze3d.vertex.VertexConsumer consumer,float parcial,int luz,int overlay){
                var type=RenderType.eyes(GLOW);getRenderer().reRender(model,pose,buffer,item,type,buffer.getBuffer(type),parcial,LightTexture.FULL_BRIGHT,overlay,1,1,1,1);
            }
        });}
    }
    private static Render RENDER;private static ItemStack PILHA;
    private static Render render(){if(RENDER==null){RENDER=new Render();PILHA=new ItemStack(RegistroAsta.ESPADA.get());}return RENDER;}

    public CamadaDemonDweller(PlayerRenderer renderer){super(renderer);}
    @SubscribeEvent public static void registrar(EntityRenderersEvent.AddLayers e){
        for(var skin:e.getSkins()){PlayerRenderer r=e.getSkin(skin);if(r!=null)r.addLayer(new CamadaDemonDweller(r));}
    }

    @Override public void render(PoseStack pose,MultiBufferSource buffer,int luz,AbstractClientPlayer p,float movimento,float intensidade,float parcial,float idade,float yaw,float pitch){
        double t=EfeitosAsta.ult(p.getUUID(),parcial);
        if(t<0||UltAsta.espadaEsquerda(t)<.5||p.isInvisible()||p.isSpectator())return;
        pose.pushPose();
        // Same steps as the vanilla held-item layer for the left arm, then the sword model's thirdperson_lefthand display.
        getParentModel().translateToHand(HumanoidArm.LEFT,pose);
        pose.mulPose(Axis.XP.rotationDegrees(-90));pose.mulPose(Axis.YP.rotationDegrees(180));pose.translate(-1/16f,.125f,-.625f);
        pose.mulPose(new Quaternionf().rotationXYZ(0,(float)Math.toRadians(-90),(float)Math.toRadians(10)));
        pose.scale(.8f,.8f,.8f);pose.translate(-.5,-.5,-.5);
        render().renderByItem(PILHA,ItemDisplayContext.THIRD_PERSON_LEFT_HAND,pose,buffer,luz,OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    /** The sword rising out of the grimoire before he grabs it (world space, every viewer). */
    @Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
    public static final class Saida{
        @SubscribeEvent public static void desenhar(RenderLevelStageEvent e){
            if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;var mc=Minecraft.getInstance();if(mc.level==null)return;
            var camera=e.getCamera().getPosition();var buffer=mc.renderBuffers().bufferSource();boolean desenhou=false;
            for(var p:mc.level.players()){
                float parcial=e.getPartialTick();double t=EfeitosAsta.ult(p.getUUID(),parcial);
                if(t<0||t>=UltAsta.SAIDA_ESPADA||UltAsta.emergir(t)<.01)continue;
                var o=EfeitosAsta.ultOrigem(p.getUUID());if(o==null)continue;
                var frente=EfeitosAsta.frenteHorizontal(o[1]);var c=o[0].add(EfeitosAsta.livro(frente,t));
                var pose=e.getPoseStack();pose.pushPose();
                // Hilt first: upside down, rising from under the open pages.
                pose.translate(c.x-camera.x,c.y-camera.y-.9+UltAsta.emergir(t)*1.25,c.z-camera.z);
                pose.mulPose(Axis.YP.rotation((float)Math.atan2(frente.x,frente.z)));pose.mulPose(Axis.ZP.rotationDegrees(180));
                pose.translate(-.5,-.5,-.5);
                render().renderByItem(PILHA,ItemDisplayContext.FIXED,pose,buffer,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                pose.popPose();desenhou=true;
            }
            if(desenhou)buffer.endBatch();
        }
    }
}
