package com.cauansilva.arcanecontrol.core.reacao;

import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import com.cauansilva.arcanecontrol.core.reacao.RegrasReacao.Elemento;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import java.util.List;

/** Frascos elementais: clique direito imbui a arma (golpes corpo a corpo aplicam o elemento por 60 s). */
public final class RegistroReacao {
    public static final RegistryObject<Item> FRASCO_FOGO=EspadaCid.ITEMS.register("frasco_fogo",()->new Frasco(Elemento.FOGO));
    public static final RegistryObject<Item> FRASCO_GELO=EspadaCid.ITEMS.register("frasco_gelo",()->new Frasco(Elemento.GELO));
    public static final RegistryObject<Item> FRASCO_RAIO=EspadaCid.ITEMS.register("frasco_raio",()->new Frasco(Elemento.RAIO));
    public static final RegistryObject<Item> FRASCO_AGUA=EspadaCid.ITEMS.register("frasco_agua",()->new Frasco(Elemento.AGUA));
    /** Forca o carregamento da classe antes do registro. */
    public static void carregar(){}

    public static final class Frasco extends Item {
        final Elemento elemento;
        Frasco(Elemento e){super(new Item.Properties().stacksTo(16));elemento=e;}
        @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand mao){
            var s=p.getItemInHand(mao);
            if(!l.isClientSide&&p instanceof ServerPlayer sp){ReacaoServidor.imbuir(sp,elemento);if(!p.getAbilities().instabuild)s.shrink(1);p.getCooldowns().addCooldown(this,20);}
            return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
        }
        @Override public void appendHoverText(ItemStack s,Level l,List<Component> t,TooltipFlag f){
            t.add(Component.literal(elemento.nome).withStyle(x->x.withColor(elemento.cor)));
            t.add(Component.literal("Imbui a arma por 60 s. Combine elementos para Reações.").withStyle(ChatFormatting.GRAY));
        }
    }
    private RegistroReacao(){}
}
