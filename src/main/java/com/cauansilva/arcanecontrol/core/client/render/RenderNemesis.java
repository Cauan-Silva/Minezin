package com.cauansilva.arcanecontrol.core.client.render;
import com.cauansilva.arcanecontrol.core.rpg.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RenderNemesis extends GeoEntityRenderer<NemesisEntity>{
    public RenderNemesis(EntityRendererProvider.Context c){super(c,new GeoModel<>(){
        @Override public ResourceLocation getModelResource(NemesisEntity e){return new ResourceLocation("arcanecontrol","geo/nemesis/"+e.kit()+".geo.json");}
        @Override public ResourceLocation getTextureResource(NemesisEntity e){return new ResourceLocation("arcanecontrol","textures/armor/anime_atlas.png");}
        @Override public ResourceLocation getAnimationResource(NemesisEntity e){return new ResourceLocation("arcanecontrol","animations/nemesis/"+e.kit()+".animation.json");}
    });shadowRadius=.55f;}
    @Override public void render(NemesisEntity e,float yaw,float parcial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light){withScale(e.escala());shadowRadius=.55f*e.escala();super.render(e,yaw,parcial,pose,buffers,light);}
    @SubscribeEvent public static void registrar(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(RegistroDespertar.NEMESIS.get(),RenderNemesis::new);}
}
