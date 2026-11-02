package com.cauansilva.arcanecontrol.core.ichigo;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
public final class EspadaZangetsu extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public EspadaZangetsu(){super(Tiers.NETHERITE,4,-2.6f,new Item.Properties().rarity(Rarity.EPIC).fireResistant());}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar r){r.add(new AnimationController<>(this,"lamina",0,s->s.setAndContinue(RawAnimation.begin().thenLoop("idle"))));}
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> c){
        c.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            private RenderZangetsu renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new RenderZangetsu();return renderer;}
        });
    }
}
