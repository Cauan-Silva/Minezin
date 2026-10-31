package com.cauansilva.arcanecontrol.core.cid;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class RenderEspadaCid extends GeoItemRenderer<EspadaCid> {
    public RenderEspadaCid() {
        super(new GeoModel<>() {
            @Override public ResourceLocation getModelResource(EspadaCid a) { return id("geo/cid/lamina.geo.json"); }
            @Override public ResourceLocation getTextureResource(EspadaCid a) { return id("textures/cid/lamina.png"); }
            @Override public ResourceLocation getAnimationResource(EspadaCid a) { return id("animations/cid/lamina.animation.json"); }
        });
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
    private static ResourceLocation id(String path) { return new ResourceLocation("arcanecontrol", path); }
}
