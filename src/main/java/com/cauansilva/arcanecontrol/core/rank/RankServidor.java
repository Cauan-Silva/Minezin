package com.cauansilva.arcanecontrol.core.rank;

import com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes;
import com.cauansilva.arcanecontrol.core.rank.RankCacador.*;
import com.cauansilva.arcanecontrol.core.rpg.RpgServidor;
import com.cauansilva.arcanecontrol.core.vazio.VazioServidor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.*;

/** Rank de Cacador no servidor: pontos no NBT do jogador ("rank_pts"), premio da vitoria e o menu de dificuldade do altar. */
public final class RankServidor {
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,"arcanecontrol");
    public static final RegistryObject<MenuType<MenuDificuldade>> MENU=MENUS.register("dificuldade_vazio",()->IForgeMenuType.create((id,inv,buf)->new MenuDificuldade(id,inv)));

    public static int pontos(ServerPlayer p){return RpgServidor.dados(p).getInt("rank_pts");}
    public static Rank rank(ServerPlayer p){return RankCacador.rank(pontos(p));}
    public static void definir(ServerPlayer p,int pts){RpgServidor.dados(p).putInt("rank_pts",Math.max(0,pts));}
    public static Component texto(Rank r){return Component.literal("["+r.name()+"]").withStyle(s->s.withColor(r.cor&0xFFFFFF).withBold(true));}

    /** Vitoria no Vazio: soma os pontos e anuncia a subida de rank (titulo + som). */
    public static void premiar(ServerPlayer p,Dificuldade d,int segundos,boolean caiu,int grupo){
        int ganho=RankCacador.pontos(true,d,segundos,caiu,grupo);if(ganho<=0)return;
        var antes=rank(p);int total=pontos(p)+ganho;definir(p,total);var depois=RankCacador.rank(total);
        var prox=depois.proximo();
        p.displayClientMessage(Component.literal("+"+ganho+" pontos de Caçador ").withStyle(ChatFormatting.AQUA).append(texto(depois))
            .append(Component.literal(prox==null?" (máximo)":" "+total+"/"+prox.minimo).withStyle(ChatFormatting.GRAY)),false);
        if(depois!=antes){
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10,50,20));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Rank ").withStyle(ChatFormatting.WHITE).append(texto(depois))));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("Caçador promovido").withStyle(ChatFormatting.GRAY)));
            p.level().playSound(null,p.blockPosition(),SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,SoundSource.PLAYERS,1f,1f);
        }
    }

    /** Altar: abre a escolha de dificuldade (Normal / Mitica / Celestial). */
    public static void abrirMenu(ServerPlayer p,BlockPos altar){
        net.minecraftforge.network.NetworkHooks.openScreen(p,new SimpleMenuProvider((id,inv,pl)->new MenuDificuldade(id,inv,altar,p),Component.literal("Desafio do Vazio")));
    }
    /** Botao do menu: confere o rank medio do grupo e abre a instancia. */
    static boolean escolher(ServerPlayer p,BlockPos altar,Dificuldade d){
        if(p.distanceToSqr(altar.getX()+.5,altar.getY()+.5,altar.getZ()+.5)>64){p.displayClientMessage(Component.literal("Fique perto do altar.").withStyle(ChatFormatting.RED),true);return false;}
        var grupo=VazioServidor.grupo(p,altar);var media=RankCacador.media(grupo.stream().map(RankServidor::rank).toList());
        if(!RankCacador.podeEntrar(d,grupo.stream().map(RankServidor::rank).toList())){
            p.displayClientMessage(Component.literal(d.nome+" exige Rank médio "+d.minimo.name()+" (o grupo tem "+media.name()+").").withStyle(ChatFormatting.RED),false);return false;}
        p.closeContainer();
        return VazioServidor.iniciar(p,altar,ProgressaoPoderes.ler(p).ativo(),d);
    }
    private RankServidor(){}
}
