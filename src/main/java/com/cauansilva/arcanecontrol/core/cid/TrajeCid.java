package com.cauansilva.arcanecontrol.core.cid;
import net.minecraft.world.item.*;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
public class TrajeCid extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public TrajeCid(Type tipo) { super(ArmorMaterials.NETHERITE,tipo,new Item.Properties().rarity(Rarity.EPIC).fireResistant()); }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar r){
        r.add(new AnimationController<>(this,"tecido",5,s->s.setAndContinue(RawAnimation.begin().thenLoop("animation.shadow.tecido"))));
    }
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            private RenderTrajeCid renderer;
            @Override public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(net.minecraft.world.entity.LivingEntity entity, ItemStack stack,net.minecraft.world.entity.EquipmentSlot slot,net.minecraft.client.model.HumanoidModel<?> original){
                if(renderer==null) renderer=new RenderTrajeCid();
                renderer.prepForRender(entity,stack,slot,original);return renderer;
            }
        });
    }
}
