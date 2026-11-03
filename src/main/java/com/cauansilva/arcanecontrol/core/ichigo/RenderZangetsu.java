package com.cauansilva.arcanecontrol.core.ichigo;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.model.GeoModel;
public final class RenderZangetsu extends GeoItemRenderer<EspadaZangetsu>{
    public RenderZangetsu(){super(new Modelo());}
    private static final class Modelo extends GeoModel<EspadaZangetsu>{
        boolean bankai;
        private ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
        @Override public ResourceLocation getModelResource(EspadaZangetsu a){return id("geo/ichigo/"+(bankai?"tensa_zangetsu":"zangetsu")+".geo.json");}
        @Override public ResourceLocation getTextureResource(EspadaZangetsu a){return id("textures/armor/anime_reference_atlas.png");}
        @Override public ResourceLocation getAnimationResource(EspadaZangetsu a){return id("animations/ichigo/zangetsu.animation.json");}
    }
    private java.util.UUID dono;
    @Override public void renderRecursively(PoseStack pose,EspadaZangetsu item,software.bernie.geckolib.cache.object.GeoBone bone,net.minecraft.client.renderer.RenderType type,MultiBufferSource buffer,com.mojang.blaze3d.vertex.VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        // Ribbon / chain bones take the cloth deltas of whoever holds this sword (TecidoEspadaIchigo); restored after.
        float x=bone.getRotX(),y=bone.getRotY(),z=bone.getRotZ();
        if(dono!=null&&TecidoEspadaIchigo.segmento(bone.getName())>=0){var p=TecidoEspadaIchigo.pose(dono,bone.getName(),parcial);bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());}
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setRotX(x);bone.setRotY(y);bone.setRotZ(z);}
    }
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext contexto,PoseStack pose,MultiBufferSource buffer,int luz,int overlay){
        var m=(Modelo)getGeoModel();boolean anterior=m.bankai;m.bankai=false;dono=null;var mc=Minecraft.getInstance();double pulso=0;
        if(mc.level!=null&&(contexto.firstPerson()||contexto==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||contexto==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)){
            for(var p:mc.level.players())if(p.getMainHandItem()==stack&&(!contexto.firstPerson()||p==mc.player)){dono=p.getUUID();m.bankai=EfeitosIchigo.bankaiVisual(p.getUUID());pulso=EfeitosIchigo.pulso(p.getUUID(),mc.getFrameTime());break;}
        }
        pose.pushPose();
        // Wrist turn around the grip (item X axis, same sign as tools/preview_ichigo_bankai): blade from upright to along the extended arm.
        if(pulso!=0){pose.translate(.5,.5,.5);pose.mulPose(com.mojang.math.Axis.XN.rotationDegrees((float)pulso));pose.mulPose(com.mojang.math.Axis.YN.rotationDegrees((float)pulso)); /* roll: broad face sideways, edge down */pose.translate(-.5,-.5,-.5);}
        try{super.renderByItem(stack,contexto,pose,buffer,luz,overlay);}finally{pose.popPose();m.bankai=anterior;}
    }
}
