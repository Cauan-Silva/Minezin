package com.cauansilva.arcanecontrol.core.equipamentos;
import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="arcanecontrol",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RegistroTrajes {
    public static final RegistryObject<Item> GOJO_CABECA=EspadaCid.ITEMS.register("gojo_cabeca",()->new TrajeAnime("gojo",ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> GOJO_PEITO=EspadaCid.ITEMS.register("gojo_peito",()->new TrajeAnime("gojo",ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> GOJO_PERNAS=EspadaCid.ITEMS.register("gojo_pernas",()->new TrajeAnime("gojo",ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> GOJO_BOTAS=EspadaCid.ITEMS.register("gojo_botas",()->new TrajeAnime("gojo",ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> FRI_CABECA=EspadaCid.ITEMS.register("frieren_cabeca",()->new TrajeAnime("frieren",ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> FRI_PEITO=EspadaCid.ITEMS.register("frieren_peito",()->new TrajeAnime("frieren",ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> FRI_PERNAS=EspadaCid.ITEMS.register("frieren_pernas",()->new TrajeAnime("frieren",ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> FRI_BOTAS=EspadaCid.ITEMS.register("frieren_botas",()->new TrajeAnime("frieren",ArmorItem.Type.BOOTS));
    public static void registrar(){}
    @SubscribeEvent public static void aba(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.COMBAT)EspadaCid.ITEMS.getEntries().forEach(i->e.accept(i.get()));}
}
