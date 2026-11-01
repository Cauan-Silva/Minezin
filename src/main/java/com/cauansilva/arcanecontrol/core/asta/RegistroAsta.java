package com.cauansilva.arcanecontrol.core.asta;
import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import com.cauansilva.arcanecontrol.core.equipamentos.TrajeAnime;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
public final class RegistroAsta {
    public static final DeferredRegister<MobEffect> EFEITOS=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,"arcanecontrol");
    public static final RegistryObject<MobEffect> SILENCIO=EFEITOS.register("silencio",()->new MobEffect(MobEffectCategory.HARMFUL,0x6B172A){});
    public static final RegistryObject<Item> ESPADA=EspadaCid.ITEMS.register("matadora_demonios",EspadaAsta::new);
    public static final RegistryObject<Item> CABECA=EspadaCid.ITEMS.register("asta_cabeca",()->new TrajeAnime("asta",ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> PEITO=EspadaCid.ITEMS.register("asta_peito",()->new TrajeAnime("asta",ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> PERNAS=EspadaCid.ITEMS.register("asta_pernas",()->new TrajeAnime("asta",ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> BOTAS=EspadaCid.ITEMS.register("asta_botas",()->new TrajeAnime("asta",ArmorItem.Type.BOOTS));
    public static Item[] conjunto(){return new Item[]{CABECA.get(),PEITO.get(),PERNAS.get(),BOTAS.get()};}
    public static void registrar(IEventBus bus){EFEITOS.register(bus);}
}
