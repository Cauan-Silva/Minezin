package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico;
import com.cauansilva.arcanecontrol.core.vfx.PerfilDivider;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import java.util.UUID;

/** The blade inherits the actual held-item transform in first and third person. While its owner is in the Devil
 * Union the sword turns with the power: the cracked black model (same grip and bones) with a full-bright glowmask. */
public final class RenderEspadaAsta extends GeoItemRenderer<EspadaAsta> {
    private static final ResourceLocation GLOW=new ResourceLocation("arcanecontrol","textures/armor/trajes/matadora_desperta_glowmask.png");
    /** Picks the normal or the awakened geometry/texture for the sword being drawn right now. */
    private static final class Modelo extends GeoModel<EspadaAsta>{
        boolean desperta;
        private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
        @Override public ResourceLocation getModelResource(EspadaAsta a){return id(desperta?"geo/asta/matadora_desperta.geo.json":"geo/asta/matadora.geo.json");}
        @Override public ResourceLocation getTextureResource(EspadaAsta a){return id(desperta?"textures/armor/trajes/matadora_desperta.png":"textures/armor/anime_reference_atlas.png");}
        @Override public ResourceLocation getAnimationResource(EspadaAsta a){return id("animations/asta/matadora.animation.json");}
    }
    private final Modelo modelo;
    private UUID dono;
    private boolean primeiraPessoa;
    public RenderEspadaAsta(){this(new Modelo());}
    private RenderEspadaAsta(Modelo modelo){super(modelo);this.modelo=modelo;addRenderLayer(new Brilho(this));}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext contexto,PoseStack pose,MultiBufferSource buffer,int luz,int overlay){
        UUID anterior=dono;boolean primeiraAnterior=primeiraPessoa,despertaAnterior=modelo.desperta;
        dono=null;primeiraPessoa=contexto.firstPerson();var mc=Minecraft.getInstance();
        if(primeiraPessoa&&mc.player!=null&&mc.player.getMainHandItem()==stack)dono=mc.player.getUUID();
        else if(mc.level!=null&&(contexto==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||contexto==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)){
            // Identity, not item equality: identical swords on other players must not inherit the cast.
            for(var p:mc.level.players())if(p.isAlive()&&p.getMainHandItem()==stack){dono=p.getUUID();break;}
        }
        var forma=dono==null?null:EfeitosAsta.forma(dono,mc.getFrameTime());
        modelo.desperta=forma!=null&&PerfilCinematico.espadaDesperta(forma.idade(),forma.alpha());
        try{super.renderByItem(stack,contexto,pose,buffer,luz,overlay);}finally{dono=anterior;primeiraPessoa=primeiraAnterior;modelo.desperta=despertaAnterior;}
    }
    @Override public void renderRecursively(PoseStack pose,EspadaAsta item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getScaleX(),y=bone.getScaleY(),z=bone.getScaleZ();boolean hidden=bone.isHidden();
        double t=dono==null?-1:EfeitosAsta.divider(dono,parcial);
        if(bone.getName().equals("blade")){bone.setScaleX(PerfilDivider.largura(t));bone.setScaleY(PerfilDivider.comprimento(t,primeiraPessoa));bone.setScaleZ(1);}
        if(bone.getName().equals("energia_divider"))bone.setHidden(PerfilDivider.intensidade(t)<.03);
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setScaleX(x);bone.setScaleY(y);bone.setScaleZ(z);bone.setHidden(hidden);}
    }
    private static final class Brilho extends GeoRenderLayer<EspadaAsta>{
        private final RenderEspadaAsta espada;
        Brilho(RenderEspadaAsta renderer){super(renderer);espada=renderer;}
        @Override public void render(PoseStack pose,EspadaAsta item,BakedGeoModel model,RenderType base,MultiBufferSource buffer,VertexConsumer consumer,float parcial,int luz,int overlay){
            if(!espada.modelo.desperta)return;var type=RenderType.eyes(GLOW);
            getRenderer().reRender(model,pose,buffer,item,type,buffer.getBuffer(type),parcial,LightTexture.FULL_BRIGHT,overlay,1,1,1,1);
        }
    }
}
