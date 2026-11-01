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
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext contexto,PoseStack pose,MultiBufferSource buffer,int luz,int overlay){
        var m=(Modelo)getGeoModel();boolean anterior=m.bankai;m.bankai=false;var mc=Minecraft.getInstance();
        if(mc.level!=null&&(contexto.firstPerson()||contexto==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||contexto==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)){
            for(var p:mc.level.players())if(p.getMainHandItem()==stack&&(!contexto.firstPerson()||p==mc.player)){m.bankai=EfeitosIchigo.bankai(p.getUUID());break;}
        }
        try{super.renderByItem(stack,contexto,pose,buffer,luz,overlay);}finally{m.bankai=anterior;}
    }
}
