package com.cauansilva.arcanecontrol.core.asta;
import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
public final class EspadaAsta extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public EspadaAsta(){super(Tiers.NETHERITE,6,-3.0f,new Item.Properties().rarity(Rarity.EPIC).fireResistant());}
    // A pose de arco ergue a lamina; o bloqueio e magico e validado pelo nosso evento,
    // sem herdar a imunidade fisica gratuita do escudo vanilla.
    @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.BOW;}
    @Override public int getUseDuration(ItemStack s){return 72000;}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand mao){
        var s=p.getItemInHand(mao);
        if(p instanceof ServerPlayer sp&&!com.cauansilva.arcanecontrol.core.rpg.RpgServidor.podeUsar(sp,com.cauansilva.arcanecontrol.core.poderes.PoderAnime.ASTA,0))return InteractionResultHolder.fail(s);
        p.startUsingItem(mao);return InteractionResultHolder.consume(s);
    }
    @Override public void appendHoverText(ItemStack s,Level l,java.util.List<net.minecraft.network.chat.Component> t,TooltipFlag f){
        t.add(net.minecraft.network.chat.Component.literal("Asta: segure o botão direito para aparar magia frontal."));
        t.add(net.minecraft.network.chat.Component.literal("Cada bloqueio/reflexão manual custa 2 de mana. Ataques físicos atravessam."));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar r){r.add(new AnimationController<>(this,"lamina",5,s->s.setAndContinue(RawAnimation.begin().thenLoop("animation.matadora.idle"))));}
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> c){
        c.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            private software.bernie.geckolib.renderer.GeoItemRenderer<EspadaAsta> renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){
                if(renderer==null)renderer=new software.bernie.geckolib.renderer.GeoItemRenderer<>(new software.bernie.geckolib.model.GeoModel<>(){
                    @Override public ResourceLocation getModelResource(EspadaAsta a){return new ResourceLocation("arcanecontrol","geo/asta/matadora.geo.json");}
                    @Override public ResourceLocation getTextureResource(EspadaAsta a){return new ResourceLocation("arcanecontrol","textures/armor/anime_reference_atlas.png");}
                    @Override public ResourceLocation getAnimationResource(EspadaAsta a){return new ResourceLocation("arcanecontrol","animations/asta/matadora.animation.json");}
                });return renderer;
            }
        });
    }
}
