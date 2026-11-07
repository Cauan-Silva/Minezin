package com.cauansilva.arcanecontrol.core.familiar;

import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import com.cauansilva.arcanecontrol.core.familiar.RegrasFamiliar.Tipo;
import com.cauansilva.arcanecontrol.core.rpg.RegistroDespertar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import java.util.List;

/** Entidade do familiar e os 3 Selos (clique direito invoca/dispensa). */
public final class RegistroFamiliar {
    public static final RegistryObject<EntityType<FamiliarEntity>> FAMILIAR=RegistroDespertar.ENTIDADES.register("familiar",
        ()->EntityType.Builder.<FamiliarEntity>of(FamiliarEntity::new,MobCategory.MISC).sized(.5f,.5f).clientTrackingRange(8).updateInterval(1).fireImmune().noSave().build("arcanecontrol:familiar"));
    public static final RegistryObject<Item> SELO_RAIO=EspadaCid.ITEMS.register("selo_familiar_raio",()->new Selo(Tipo.RAIO));
    public static final RegistryObject<Item> SELO_FONTE=EspadaCid.ITEMS.register("selo_familiar_fonte",()->new Selo(Tipo.FONTE));
    public static final RegistryObject<Item> SELO_GUARDIAO=EspadaCid.ITEMS.register("selo_familiar_guardiao",()->new Selo(Tipo.GUARDIAO));
    /** Forca o carregamento da classe (os RegistryObjects entram nos DeferredRegisters antes do registro). */
    public static void carregar(){}

    public static final class Selo extends Item {
        final Tipo tipo;
        Selo(Tipo t){super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));tipo=t;}
        @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand mao){
            if(!l.isClientSide&&p instanceof ServerPlayer sp){FamiliarServidor.usarSelo(sp,tipo);p.getCooldowns().addCooldown(this,20);}
            return InteractionResultHolder.sidedSuccess(p.getItemInHand(mao),l.isClientSide);
        }
        @Override public boolean isFoil(ItemStack s){return true;}
        @Override public void appendHoverText(ItemStack s,Level l,List<Component> t,TooltipFlag f){
            t.add(Component.literal(tipo.nome).withStyle(x->x.withColor(tipo.cor)));
            t.add(Component.literal(tipo.efeito()).withStyle(ChatFormatting.GRAY));
            t.add(Component.literal("Todos: recolhem itens/XP, acumulam Stun e se sacrificam contra um golpe fatal.").withStyle(ChatFormatting.DARK_GRAY));
            t.add(Component.literal("Clique direito: invocar / dispensar").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    private RegistroFamiliar(){}
}
