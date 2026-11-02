package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.equipamentos.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.*;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
/** Cid-only renderer. Spring offsets are applied identically in diffuse and emissive passes. */
public final class RenderTrajeCid extends GeoArmorRenderer<TrajeAnime>{
    private static final ResourceLocation GLOW=new ResourceLocation("arcanecontrol","textures/armor/trajes/shadow_glowmask.png");
    public RenderTrajeCid(){super(new ModeloTrajeAnime(false));addRenderLayer(new Emissao(this));}
    @Override protected void setBoneVisible(GeoBone bone,boolean visivel){super.setBoneVisible(bone,visivel);if(bone!=null)bone.setChildrenHidden(!visivel);}
    @Override public void renderRecursively(PoseStack pose,TrajeAnime item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getRotX(),y=bone.getRotY(),z=bone.getRotZ();
        var entity=getCurrentEntity();String name=bone.getName();
        if(entity!=null&&name.startsWith("cid_cape_")&&name.length()>11){
            int segment=Character.digit(name.charAt(name.length()-1),10);
            if(segment>=0&&segment<4){int painel=Character.digit(name.charAt(9),10);var p=painel>=0&&painel<5?MantoCidCliente.painel(entity.getUUID(),painel,segment,parcial):new MolaMantoCid.Pose(0,0,0);bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());}
        }
        if(entity!=null&&DetalhesTrajeCid.OSSOS.contains(name)){
            var p=MantoCidCliente.detalhe(entity.getUUID(),name,parcial);bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());
        }
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setRotX(x);bone.setRotY(y);bone.setRotZ(z);}
    }
    private static final class Emissao extends GeoRenderLayer<TrajeAnime>{
        Emissao(RenderTrajeCid renderer){super(renderer);}
        @Override public void render(PoseStack pose,TrajeAnime item,BakedGeoModel model,RenderType base,MultiBufferSource buffer,VertexConsumer consumer,float parcial,int luz,int overlay){
            if(!ConfigCliente.EMISSAO_CID.get())return;var type=RenderType.eyes(GLOW);
            getRenderer().reRender(model,pose,buffer,item,type,buffer.getBuffer(type),parcial,LightTexture.FULL_BRIGHT,overlay,1,1,1,1);
        }
    }
}
