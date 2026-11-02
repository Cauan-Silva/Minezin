package com.cauansilva.arcanecontrol.core.asta;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Devil Union model on top of the skin; it follows the same part transforms as armor (and attack animations). */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CamadaFormaAsta extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private static RenderFormaAsta RENDER;
    private static ItemStack PILHA;
    private static final ResourceLocation TEXTURA=new ResourceLocation("arcanecontrol","textures/armor/trajes/asta_forma.png");
    public CamadaFormaAsta(PlayerRenderer renderer){super(renderer);}

    @SubscribeEvent public static void registrar(EntityRenderersEvent.AddLayers e){
        for(var skin:e.getSkins()){
            PlayerRenderer renderer=e.getSkin(skin);
            if(renderer!=null)renderer.addLayer(new CamadaFormaAsta(renderer));
        }
    }

    @Override public void render(PoseStack pose,MultiBufferSource buffer,int luz,AbstractClientPlayer p,
                                 float movimento,float intensidade,float parcial,float idade,float yaw,float pitch){
        var estado=EfeitosAsta.forma(p.getUUID(),parcial);
        if(estado==null||p.isInvisible()||p.isSpectator())return;
        // Same path as an armor layer: the GeckoLib model copies the player's part transforms.
        if(RENDER==null){RENDER=new RenderFormaAsta();PILHA=new ItemStack(RegistroAsta.PEITO.get());}
        RENDER.preparar(estado.idade());
        RENDER.prepForRender(p,PILHA,EquipmentSlot.CHEST,getParentModel());
        RENDER.renderToBuffer(pose,buffer.getBuffer(RenderType.entityTranslucent(TEXTURA)),luz,OverlayTexture.NO_OVERLAY,1,1,1,estado.alpha());
    }
}
