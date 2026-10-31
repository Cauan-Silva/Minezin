package com.cauansilva.arcanecontrol.core.cid;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
public class RenderTrajeCid extends GeoArmorRenderer<TrajeCid>{
    @Override protected void setBoneVisible(software.bernie.geckolib.cache.object.GeoBone bone,boolean visivel){
        super.setBoneVisible(bone,visivel);if(bone!=null)bone.setChildrenHidden(!visivel);
    }
    public RenderTrajeCid(){super(new GeoModel<>(){
        @Override public ResourceLocation getModelResource(TrajeCid a){return id("geo/cid/traje.geo.json");}
        @Override public ResourceLocation getTextureResource(TrajeCid a){return id("textures/cid/traje.png");}
        @Override public ResourceLocation getAnimationResource(TrajeCid a){return id("animations/cid/traje.animation.json");}
    });addRenderLayer(new AutoGlowingGeoLayer<>(this));}
    private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
}
