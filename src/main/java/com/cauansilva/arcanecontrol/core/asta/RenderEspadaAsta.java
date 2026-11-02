package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.vfx.PerfilDivider;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import java.util.UUID;

/** The blade inherits the actual held-item transform in first and third person. */
public final class RenderEspadaAsta extends GeoItemRenderer<EspadaAsta> {
    private UUID dono;
    private boolean primeiraPessoa;
    public RenderEspadaAsta(){super(new GeoModel<>(){
        private ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
        @Override public ResourceLocation getModelResource(EspadaAsta a){return id("geo/asta/matadora.geo.json");}
        @Override public ResourceLocation getTextureResource(EspadaAsta a){return id("textures/armor/anime_reference_atlas.png");}
        @Override public ResourceLocation getAnimationResource(EspadaAsta a){return id("animations/asta/matadora.animation.json");}
    });}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext contexto,PoseStack pose,MultiBufferSource buffer,int luz,int overlay){
        UUID anterior=dono;boolean primeiraAnterior=primeiraPessoa;
        dono=null;primeiraPessoa=contexto.firstPerson();var mc=Minecraft.getInstance();
        if(primeiraPessoa&&mc.player!=null&&mc.player.getMainHandItem()==stack)dono=mc.player.getUUID();
        else if(mc.level!=null&&(contexto==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||contexto==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)){
            // Identity, not item equality: identical swords on other players must not inherit the cast.
            for(var p:mc.level.players())if(p.isAlive()&&p.getMainHandItem()==stack){dono=p.getUUID();break;}
        }
        try{super.renderByItem(stack,contexto,pose,buffer,luz,overlay);}finally{dono=anterior;primeiraPessoa=primeiraAnterior;}
    }
    @Override public void renderRecursively(PoseStack pose,EspadaAsta item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getScaleX(),y=bone.getScaleY(),z=bone.getScaleZ();boolean hidden=bone.isHidden();
        double t=dono==null?-1:EfeitosAsta.divider(dono,parcial);
        if(bone.getName().equals("blade")){bone.setScaleX(PerfilDivider.largura(t));bone.setScaleY(PerfilDivider.comprimento(t,primeiraPessoa));bone.setScaleZ(1);}
        if(bone.getName().equals("energia_divider"))bone.setHidden(PerfilDivider.intensidade(t)<.03);
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setScaleX(x);bone.setScaleY(y);bone.setScaleZ(z);bone.setHidden(hidden);}
    }
}
