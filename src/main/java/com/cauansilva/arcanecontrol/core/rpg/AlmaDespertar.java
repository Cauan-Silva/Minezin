package com.cauansilva.arcanecontrol.core.rpg;

import com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.List;
import javax.annotation.Nullable;

public final class AlmaDespertar extends Item {
    public AlmaDespertar(){super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());}
    public static ItemStack criar(int kit){var s=new ItemStack(RegistroDespertar.ALMA.get());s.getOrCreateTag().putInt("kit",kit);return s;}
    @Override public Component getName(ItemStack s){var k=PersonagemRpg.porId(s.hasTag()?s.getTag().getInt("kit"):0);return Component.literal("Alma do Despertar"+(k==null?"":" · "+k.nome));}
    @Override public void appendHoverText(ItemStack s,@Nullable Level l,List<Component> t,TooltipFlag f){
        t.add(Component.literal("Use com o kit correspondente ativo e o nó Despertar comprado em H."));
        t.add(Component.literal("Desbloqueio permanente. Ative a forma com G."));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player jogador,InteractionHand mao){
        var s=jogador.getItemInHand(mao);if(!(jogador instanceof ServerPlayer p))return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
        if(!p.isAlive()||p.isSpectator())return InteractionResultHolder.fail(s);
        var kit=ProgressaoPoderes.ler(p).ativo();var m=RpgServidor.mestria(p,kit.id);
        int alma=s.hasTag()?s.getTag().getInt("kit"):0;
        if(m.consumirAlma(PersonagemRpg.porId(kit.id),PersonagemRpg.porId(alma))){
            RpgServidor.salvarMestria(RpgServidor.dados(p),kit.id,m);if(!p.isCreative())s.shrink(1);
            p.displayClientMessage(Component.literal("Despertar de "+kit.nome+" liberado! Pressione G."),false);RpgServidor.sincronizar(p);
            return InteractionResultHolder.success(s);
        }
        p.displayClientMessage(Component.literal("A alma exige o kit correspondente, nível 50 e nó Despertar. Cada kit desperta uma única vez."),true);
        return InteractionResultHolder.fail(s);
    }
}
