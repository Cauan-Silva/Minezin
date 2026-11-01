package com.cauansilva.arcanecontrol.core.frieren;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/** O clique usa a mesma tecnica e a mesma recarga das teclas, sempre no servidor. */
public final class CajadoFrieren extends Item implements GeoItem {
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public CajadoFrieren(){super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());}
    @Override public InteractionResultHolder<ItemStack> use(Level nivel,Player jogador,InteractionHand mao){
        if(jogador instanceof ServerPlayer p)com.cauansilva.arcanecontrol.core.rpg.MovesetsServidor.executar(p,21);
        return InteractionResultHolder.sidedSuccess(jogador.getItemInHand(mao),nivel.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack,Level nivel,java.util.List<net.minecraft.network.chat.Component> texto,TooltipFlag flag){
        texto.add(net.minecraft.network.chat.Component.translatable("item.arcanecontrol.cajado_frieren.uso").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar r){
        r.add(new AnimationController<>(this,"ornamento",5,s->s.setAndContinue(RawAnimation.begin().thenLoop("animation.cajado.idle"))));
    }
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            private software.bernie.geckolib.renderer.GeoItemRenderer<CajadoFrieren> renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){
                if(renderer==null)renderer=new software.bernie.geckolib.renderer.GeoItemRenderer<>(new software.bernie.geckolib.model.GeoModel<CajadoFrieren>(){
                    private ResourceLocation id(String path){return new ResourceLocation("arcanecontrol",path);}
                    @Override public ResourceLocation getModelResource(CajadoFrieren a){return id("geo/frieren/cajado.geo.json");}
                    @Override public ResourceLocation getTextureResource(CajadoFrieren a){return id("textures/armor/anime_reference_atlas.png");}
                    @Override public ResourceLocation getAnimationResource(CajadoFrieren a){return id("animations/frieren/cajado.animation.json");}
                });
                return renderer;
            }
        });
    }
}
