package com.cauansilva.arcanecontrol.core.rpg;

import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="arcanecontrol",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RegistroDespertar {
    public static final DeferredRegister<Block> BLOCOS=DeferredRegister.create(ForgeRegistries.BLOCKS,"arcanecontrol");
    public static final DeferredRegister<EntityType<?>> ENTIDADES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,"arcanecontrol");
    public static final RegistryObject<Block> ALTAR=BLOCOS.register("altar_despertar",()->new AltarDespertar(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).strength(8,1200).lightLevel(s->9)));
    public static final RegistryObject<Item> ITEM_ALTAR=EspadaCid.ITEMS.register("altar_despertar",()->new BlockItem(ALTAR.get(),new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ALMA=EspadaCid.ITEMS.register("alma_despertar",AlmaDespertar::new);
    public static final RegistryObject<EntityType<NemesisEntity>> NEMESIS=ENTIDADES.register("nemesis",()->EntityType.Builder.of(NemesisEntity::new,MobCategory.MONSTER).sized(.85f,2.2f).clientTrackingRange(12).updateInterval(2).fireImmune().build("arcanecontrol:nemesis"));
    public static void registrar(IEventBus bus){BLOCOS.register(bus);ENTIDADES.register(bus);}
    @SubscribeEvent public static void atributos(EntityAttributeCreationEvent e){e.put(NEMESIS.get(),NemesisEntity.atributos().build());}
}
