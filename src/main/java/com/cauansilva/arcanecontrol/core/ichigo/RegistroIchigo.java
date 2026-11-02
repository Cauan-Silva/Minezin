package com.cauansilva.arcanecontrol.core.ichigo;
import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import com.cauansilva.arcanecontrol.core.equipamentos.TrajeAnime;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;
public final class RegistroIchigo {
    public static final RegistryObject<Item> ESPADA=EspadaCid.ITEMS.register("zangetsu",EspadaZangetsu::new);
    public static final RegistryObject<Item> CABECA=EspadaCid.ITEMS.register("ichigo_cabeca",()->new TrajeAnime("ichigo",ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> PEITO=EspadaCid.ITEMS.register("ichigo_peito",()->new TrajeAnime("ichigo",ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> PERNAS=EspadaCid.ITEMS.register("ichigo_pernas",()->new TrajeAnime("ichigo",ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> BOTAS=EspadaCid.ITEMS.register("ichigo_botas",()->new TrajeAnime("ichigo",ArmorItem.Type.BOOTS));
    public static Item[] conjunto(){return new Item[]{CABECA.get(),PEITO.get(),PERNAS.get(),BOTAS.get()};}
    public static void inicializar(){}
}
