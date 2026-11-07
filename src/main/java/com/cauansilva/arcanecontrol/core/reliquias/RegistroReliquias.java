package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.Tipo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class RegistroReliquias {
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,"arcanecontrol");
    public static final RegistryObject<Item> ANEL=EspadaCid.ITEMS.register("reliquia_anel",()->new ItemReliquia(Tipo.ANEL));
    public static final RegistryObject<Item> COLAR=EspadaCid.ITEMS.register("reliquia_colar",()->new ItemReliquia(Tipo.COLAR));
    public static final RegistryObject<Item> AMULETO=EspadaCid.ITEMS.register("reliquia_amuleto",()->new ItemReliquia(Tipo.AMULETO));
    public static final RegistryObject<Item> BRINCO=EspadaCid.ITEMS.register("reliquia_brinco",()->new ItemReliquia(Tipo.BRINCO));
    public static final RegistryObject<MenuType<MenuBolsa>> BOLSA=MENUS.register("bolsa_reliquias",()->IForgeMenuType.create((id,inv,buf)->new MenuBolsa(id,inv)));
    public static void registrar(IEventBus bus){MENUS.register(bus);}
    private RegistroReliquias(){}
}
