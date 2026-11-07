package com.cauansilva.arcanecontrol.core.familiar;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Espirito desenhado por codigo: nucleo brilhante + halo girando (billboards aditivos) e particulas do tipo. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RenderFamiliar extends EntityRenderer<FamiliarEntity> {
    private static final ResourceLocation BRILHO=new ResourceLocation("arcanecontrol","textures/entity/familiar_brilho.png");
    public RenderFamiliar(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
    @SubscribeEvent public static void registrar(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(RegistroFamiliar.FAMILIAR.get(),RenderFamiliar::new);}
    @Override public ResourceLocation getTextureLocation(FamiliarEntity e){return BRILHO;}

    @Override public void render(FamiliarEntity e,float yaw,float parcial,PoseStack pose,MultiBufferSource buf,int luz){
        var mc=Minecraft.getInstance();
        // na primeira pessoa o proprio familiar fica discreto (nao tapa a visao)
        boolean meu=mc.player!=null&&mc.player.getUUID().equals(e.dono())&&mc.options.getCameraType().isFirstPerson();
        float t=e.tickCount+parcial;int cor=e.tipo().cor;float r=(cor>>16&255)/255f,g=(cor>>8&255)/255f,b=(cor&255)/255f;
        var vc=buf.getBuffer(RenderType.entityTranslucentEmissive(BRILHO));
        pose.pushPose();pose.translate(0,.25,0);pose.mulPose(entityRenderDispatcher.cameraOrientation());pose.mulPose(Axis.YP.rotationDegrees(180));
        float a=meu?.35f:1f;
        quad(pose,vc,.55f+.05f*(float)Math.sin(t*.25),r,g,b,.55f*a);                 // halo
        pose.mulPose(Axis.ZP.rotationDegrees(t*4));quad(pose,vc,.32f,1,1,1,.9f*a);    // nucleo
        pose.mulPose(Axis.ZP.rotationDegrees(-t*9));quad(pose,vc,.42f,r,g,b,.45f*a);
        pose.popPose();
        if(!mc.isPaused()&&e.tickCount%3==0&&!meu){
            ParticleOptions part=switch(e.tipo()){case RAIO->ParticleTypes.ELECTRIC_SPARK;case FONTE->ParticleTypes.HAPPY_VILLAGER;case GUARDIAO->ParticleTypes.END_ROD;};
            e.level().addParticle(part,e.getX()+(e.level().random.nextDouble()-.5)*.4,e.getY()+.25+(e.level().random.nextDouble()-.5)*.4,e.getZ()+(e.level().random.nextDouble()-.5)*.4,0,e.tipo()==RegrasFamiliar.Tipo.GUARDIAO?-.01:0,0);
        }
        super.render(e,yaw,parcial,pose,buf,luz);
    }
    private static void quad(PoseStack pose,VertexConsumer vc,float s,float r,float g,float b,float a){
        var m=pose.last().pose();var n=pose.last().normal();int full=LightTexture.FULL_BRIGHT;
        vc.vertex(m,-s,-s,0).color(r,g,b,a).uv(0,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(full).normal(n,0,1,0).endVertex();
        vc.vertex(m,s,-s,0).color(r,g,b,a).uv(1,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(full).normal(n,0,1,0).endVertex();
        vc.vertex(m,s,s,0).color(r,g,b,a).uv(1,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(full).normal(n,0,1,0).endVertex();
        vc.vertex(m,-s,s,0).color(r,g,b,a).uv(0,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(full).normal(n,0,1,0).endVertex();
    }
}
