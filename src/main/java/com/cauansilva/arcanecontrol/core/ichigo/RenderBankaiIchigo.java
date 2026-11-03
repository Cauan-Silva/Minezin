package com.cauansilva.arcanecontrol.core.ichigo;

import com.cauansilva.arcanecontrol.core.client.tecido.TecidoIchigo;
import com.cauansilva.arcanecontrol.core.equipamentos.TrajeAnime;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/** Bankai coat / Mugetsu form drawn like RenderFormaAsta: one GeckoLib model (geo/armaduras/ichigo_bankai) following the player's
 *  parts; the tail bones take TecidoIchigo deltas and are restored after drawing. */
public final class RenderBankaiIchigo extends GeoArmorRenderer<TrajeAnime>{
    private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
    private static final class Modelo extends GeoModel<TrajeAnime>{
        private final String estilo;Modelo(String estilo){this.estilo=estilo;}
        @Override public ResourceLocation getModelResource(TrajeAnime a){return id("geo/armaduras/"+estilo+".geo.json");}
        @Override public ResourceLocation getTextureResource(TrajeAnime a){return id("textures/armor/trajes/"+estilo+".png");}
        @Override public ResourceLocation getAnimationResource(TrajeAnime a){return id("animations/armaduras/"+estilo+".animation.json");}
    }
    /** estilo: ichigo_bankai (coat) or ichigo_mugetsu (Mugetsu form); both use TecidoIchigo bones. */
    public RenderBankaiIchigo(String estilo){super(new Modelo(estilo));}
    @Override protected void applyBoneVisibilityBySlot(EquipmentSlot slot){setAllVisible(true);}
    @Override public void renderRecursively(PoseStack pose,TrajeAnime item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getRotX(),y=bone.getRotY(),z=bone.getRotZ();var entidade=getCurrentEntity();
        if(entidade!=null&&TecidoIchigo.OSSOS.contains(bone.getName())){
            var p=TecidoIchigoCliente.pose(entidade.getUUID(),bone.getName(),parcial);bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());
        }
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setRotX(x);bone.setRotY(y);bone.setRotZ(z);}
    }
}
