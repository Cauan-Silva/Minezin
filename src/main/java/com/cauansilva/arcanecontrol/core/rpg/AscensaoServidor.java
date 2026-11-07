package com.cauansilva.arcanecontrol.core.rpg;

import com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Ascensao: no nivel 100 o jogador renasce o kit ativo (nivel 1, arvore vazia) e ganha bonus permanentes. Comando
 * aberto a todos: {@code /ascender} mostra o que muda e {@code /ascender confirmar} (em ate 10 s) executa.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class AscensaoServidor {
    private static final String PEDIDO="asc_pedido";
    private static final int JANELA=200;

    @SubscribeEvent public static void comandos(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("ascender")
            .executes(c->{pedir(c.getSource().getPlayerOrException());return 1;})
            .then(Commands.literal("confirmar").executes(c->{confirmar(c.getSource().getPlayerOrException());return 1;})));
    }
    private static void pedir(ServerPlayer p){
        var kit=ProgressaoPoderes.ler(p).ativo();var m=RpgServidor.mestria(p,kit.id);
        if(m.ascensoes()>=MestriaRpg.ASC_MAX){p.sendSystemMessage(Component.literal("Ascensão máxima ("+MestriaRpg.ASC_MAX+") neste kit.").withStyle(ChatFormatting.GOLD));return;}
        if(!m.podeAscender()){p.sendSystemMessage(Component.literal("A Ascensão exige nível "+MestriaRpg.NIVEL_MAXIMO+" no kit ativo (você está no "+m.nivel()+"). Ascensões: "+estrelas(m.ascensoes())).withStyle(ChatFormatting.GRAY));return;}
        p.getPersistentData().putLong(PEDIDO,RpgServidor.agora(p));
        int n=m.ascensoes()+1;
        p.sendSystemMessage(Component.literal("Ascensão "+n+" de "+PersonagemRpg.porId(kit.id).name()+":").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
        p.sendSystemMessage(Component.literal(" • Volta ao nível 1 e a árvore é zerada (o Despertar volta sozinho ao recomprar o nó).").withStyle(ChatFormatting.GRAY));
        p.sendSystemMessage(Component.literal(" • Permanente: +"+n*4+"% dano, +"+n*3+" vida, -"+n*2+"% recarga, +"+n*10+"% XP.").withStyle(ChatFormatting.YELLOW));
        p.sendSystemMessage(Component.literal("[Clique para CONFIRMAR em 10 s]").withStyle(s->s.withColor(ChatFormatting.GREEN).withBold(true)
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/ascender confirmar"))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal("Renascer agora")))));
    }
    private static void confirmar(ServerPlayer p){
        long t=p.getPersistentData().getLong(PEDIDO);p.getPersistentData().remove(PEDIDO);
        if(t<=0||RpgServidor.agora(p)-t>JANELA){p.sendSystemMessage(Component.literal("Use /ascender primeiro (a confirmação vale por 10 s).").withStyle(ChatFormatting.RED));return;}
        var kit=ProgressaoPoderes.ler(p).ativo();var m=RpgServidor.mestria(p,kit.id);
        if(!m.ascender()){pedir(p);return;}
        RpgServidor.salvarMestria(RpgServidor.dados(p),kit.id,m);RpgServidor.aplicarAtributos(p);RpgServidor.sincronizar(p);
        var l=p.serverLevel();
        l.sendParticles(ParticleTypes.END_ROD,p.getX(),p.getY()+1,p.getZ(),120,.6,1.2,.6,.25);
        l.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,p.getX(),p.getY()+1,p.getZ(),80,.5,1,.5,.5);
        l.playSound(null,p.blockPosition(),SoundEvents.BEACON_POWER_SELECT,SoundSource.PLAYERS,1.2f,.8f);
        l.playSound(null,p.blockPosition(),SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,SoundSource.PLAYERS,1f,1f);
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("ASCENSÃO "+m.ascensoes()).withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD)));
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal(estrelas(m.ascensoes())).withStyle(ChatFormatting.YELLOW)));
        p.server.getPlayerList().broadcastSystemMessage(Component.literal(p.getGameProfile().getName()+" ascendeu "+PersonagemRpg.porId(kit.id).name()+" "+estrelas(m.ascensoes())).withStyle(ChatFormatting.GOLD),false);
    }
    static String estrelas(int n){return n<=0?"—":"★".repeat(n);}
    private AscensaoServidor(){}
}
