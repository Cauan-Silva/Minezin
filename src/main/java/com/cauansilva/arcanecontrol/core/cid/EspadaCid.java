package com.cauansilva.arcanecontrol.core.cid;

import net.minecraft.world.item.*;
import net.minecraftforge.registries.*;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EspadaCid extends SwordItem implements GeoItem {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "arcanecontrol");
    public static final RegistryObject<Item> ESPADA = ITEMS.register("lamina_shadow", EspadaCid::new);
    public static final RegistryObject<Item> CAPUZ = ITEMS.register("capuz_shadow", () -> new TrajeCid(ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> MANTO = ITEMS.register("manto_shadow", () -> new TrajeCid(ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> CALCAS = ITEMS.register("calcas_shadow", () -> new TrajeCid(ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> BOTAS = ITEMS.register("botas_shadow", () -> new TrajeCid(ArmorItem.Type.BOOTS));
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public EspadaCid() { super(Tiers.NETHERITE, 3, -2.4f, new Item.Properties().rarity(Rarity.EPIC).fireResistant()); }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar r) {
        r.add(new AnimationController<>(this, "energia", 5, s -> s.setAndContinue(RawAnimation.begin().thenLoop("animation.shadow.idle"))));
    }
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private RenderEspadaCid renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new RenderEspadaCid();
                return renderer;
            }
        });
    }
}
