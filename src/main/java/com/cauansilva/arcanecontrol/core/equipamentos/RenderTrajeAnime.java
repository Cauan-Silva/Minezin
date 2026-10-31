package com.cauansilva.arcanecontrol.core.equipamentos;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.cache.object.GeoBone;
public final class RenderTrajeAnime extends GeoArmorRenderer<TrajeAnime>{
    public RenderTrajeAnime(){super(new ModeloTrajeAnime(false));}
    @Override protected void setBoneVisible(GeoBone bone,boolean visivel){
        super.setBoneVisible(bone,visivel);if(bone!=null)bone.setChildrenHidden(!visivel);
    }
}
