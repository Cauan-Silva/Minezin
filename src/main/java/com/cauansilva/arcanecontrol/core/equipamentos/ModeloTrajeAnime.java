package com.cauansilva.arcanecontrol.core.equipamentos;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
public final class ModeloTrajeAnime extends GeoModel<TrajeAnime>{
    private final boolean inventario;
    public ModeloTrajeAnime(boolean inventario){this.inventario=inventario;}
    private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
    @Override public ResourceLocation getModelResource(TrajeAnime a){return id("geo/armaduras/"+a.estilo+(inventario?"_"+a.getType().name().toLowerCase(java.util.Locale.ROOT):"")+".geo.json");}
    @Override public ResourceLocation getTextureResource(TrajeAnime a){return id("textures/armor/anime_reference_atlas.png");}
    @Override public ResourceLocation getAnimationResource(TrajeAnime a){return id("animations/armaduras/"+a.estilo+".animation.json");}
}
