package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.equipamentos.TrajeAnime;
import com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Devil Union drawn like the outfits: one GeckoLib model (geo/armaduras/asta_forma) following the player's parts,
 * every bone visible, the red islands full-bright through a glowmask (Cid pattern). Each body part fades in on the
 * PerfilCinematico.revelarForma timeline; wings and tail bones take DevilUnionTecido deltas and restore afterwards.
 */
public final class RenderFormaAsta extends GeoArmorRenderer<TrajeAnime>{
    private static final ResourceLocation TEXTURA=id("textures/armor/trajes/asta_forma.png"),GLOW=id("textures/armor/trajes/asta_forma_glowmask.png");
    private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
    private static final class Modelo extends GeoModel<TrajeAnime>{
        @Override public ResourceLocation getModelResource(TrajeAnime a){return id("geo/armaduras/asta_forma.geo.json");}
        @Override public ResourceLocation getTextureResource(TrajeAnime a){return TEXTURA;}
        @Override public ResourceLocation getAnimationResource(TrajeAnime a){return id("animations/armaduras/asta_forma.animation.json");}
    }
    private double idade;
    public RenderFormaAsta(){super(new Modelo());addRenderLayer(new Brilho(this));}
    public void preparar(double idade){this.idade=idade;}
    @Override protected void applyBoneVisibilityBySlot(EquipmentSlot slot){setAllVisible(true);}
    @Override public RenderType getRenderType(TrajeAnime item,ResourceLocation textura,MultiBufferSource buffer,float parcial){return RenderType.entityTranslucent(textura);}
    private static String parte(String osso){
        return switch(osso){
            case "armorHead"->"head";case "armorRightArm"->"right_arm";case "armorLeftArm"->"left_arm";
            case "armorRightLeg","armorRightBoot"->"right_leg";case "armorLeftLeg","armorLeftBoot"->"left_leg";
            default->"body";
        };
    }
    @Override public void renderRecursively(PoseStack pose,TrajeAnime item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        // Top-level parts fade in one after another; children inherit it through the recursion. Colour is scaled too,
        // so the full-bright red of the glow pass fades with its part instead of popping in.
        if(bone.getParent()==null){float r=(float)PerfilCinematico.revelarForma(idade,parte(bone.getName()));alpha*=r;red*=r;green*=r;blue*=r;}
        if(alpha<=.004f)return;
        float x=bone.getRotX(),y=bone.getRotY(),z=bone.getRotZ();var entidade=getCurrentEntity();
        if(entidade!=null&&bone.getName().startsWith("du_")){
            var p=TecidoDevilUnionCliente.pose(entidade.getUUID(),bone.getName(),parcial,PerfilCinematico.abrirAsa(idade));
            bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());
        }
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setRotX(x);bone.setRotY(y);bone.setRotZ(z);}
    }
    private static final class Brilho extends GeoRenderLayer<TrajeAnime>{
        Brilho(RenderFormaAsta renderer){super(renderer);}
        @Override public void render(PoseStack pose,TrajeAnime item,BakedGeoModel model,RenderType base,MultiBufferSource buffer,VertexConsumer consumer,float parcial,int luz,int overlay){
            var type=RenderType.eyes(GLOW);
            getRenderer().reRender(model,pose,buffer,item,type,buffer.getBuffer(type),parcial,LightTexture.FULL_BRIGHT,overlay,1,1,1,1);
        }
    }
}
