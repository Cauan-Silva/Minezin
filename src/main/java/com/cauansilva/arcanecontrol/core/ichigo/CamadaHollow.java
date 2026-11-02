package com.cauansilva.arcanecontrol.core.ichigo;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Head-attached, hair-free mask. Follows the player's real head animation. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CamadaHollow extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>>{
    private static final ModelLayerLocation LAYER=new ModelLayerLocation(new ResourceLocation("arcanecontrol","hollow"),"main");
    private static final ResourceLocation TEXTURE=new ResourceLocation("arcanecontrol","textures/ichigo/mascara.png");
    private final ModelPart mascara,coat,rightTail,leftTail;
    public CamadaHollow(PlayerRenderer r,ModelPart m){super(r);mascara=m.getChild("mask");coat=m.getChild("coat");rightTail=m.getChild("rightTail");leftTail=m.getChild("leftTail");}
    @SubscribeEvent public static void definicoes(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,()->{
        var mesh=new MeshDefinition();var b=CubeListBuilder.create().texOffs(0,0).addBox(-4.25f,-8.1f,-4.7f,8.5f,7.7f,.65f);
        b.texOffs(0,16).addBox(-3.5f,-5.4f,-4.83f,2.4f,.65f,.2f).addBox(1.1f,-5.4f,-4.83f,2.4f,.65f,.2f);
        b.texOffs(12,16).addBox(-3.4f,-7.8f,-4.86f,.65f,2.2f,.2f).addBox(-2.7f,-3.9f,-4.86f,.65f,2.8f,.2f);
        b.texOffs(0,16).addBox(-1.6f,-2.2f,-4.83f,3.2f,.4f,.2f);
        mesh.getRoot().addOrReplaceChild("mask",b,PartPose.ZERO);
        // Coat meshes have a separate 64x64 texture, including a light sash.
        mesh.getRoot().addOrReplaceChild("coat",CubeListBuilder.create().texOffs(0,0).addBox(-4.4f,-.4f,-2.55f,8.8f,12.8f,5.1f).texOffs(0,40).addBox(-4.5f,10.5f,-2.65f,9,1.6f,5.3f),PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild("rightTail",CubeListBuilder.create().texOffs(0,0).addBox(-2.45f,0,-2.5f,4.7f,8.5f,5),PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild("leftTail",CubeListBuilder.create().texOffs(0,0).addBox(-2.25f,0,-2.5f,4.7f,8.5f,5),PartPose.ZERO);
        return LayerDefinition.create(mesh,64,64);
    });}
    @SubscribeEvent public static void camadas(EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins()){PlayerRenderer r=e.getSkin(skin);if(r!=null)r.addLayer(new CamadaHollow(r,e.getEntityModels().bakeLayer(LAYER)));}}
    @Override public void render(PoseStack s,MultiBufferSource buffer,int luz,AbstractClientPlayer p,float a,float b,float parcial,float idade,float yaw,float pitch){
        if(!EfeitosIchigo.bankai(p.getUUID())||p.isInvisible()||p.isSpectator())return;
        var vc=buffer.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("arcanecontrol","textures/ichigo/bankai_coat.png")));
        s.pushPose();try{getParentModel().body.translateAndRotate(s);coat.render(s,vc,luz,OverlayTexture.NO_OVERLAY);}finally{s.popPose();}
        s.pushPose();try{getParentModel().rightLeg.translateAndRotate(s);rightTail.render(s,vc,luz,OverlayTexture.NO_OVERLAY);}finally{s.popPose();}
        s.pushPose();try{getParentModel().leftLeg.translateAndRotate(s);leftTail.render(s,vc,luz,OverlayTexture.NO_OVERLAY);}finally{s.popPose();}
        if(!EfeitosIchigo.mascara(p.getUUID()))return;
        s.pushPose();try{getParentModel().head.translateAndRotate(s);mascara.render(s,buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),luz,OverlayTexture.NO_OVERLAY);}finally{s.popPose();}
    }
}
