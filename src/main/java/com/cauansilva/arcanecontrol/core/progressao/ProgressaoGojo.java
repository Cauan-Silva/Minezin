package com.cauansilva.arcanecontrol.core.progressao;

import com.cauansilva.arcanecontrol.core.magia.GerenciadorMagiasGojo;
import com.cauansilva.arcanecontrol.core.rede.PacoteProgressoGojo;
import com.cauansilva.arcanecontrol.core.rede.RegistoRede;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = "arcanecontrol")
public final class ProgressaoGojo {
    private static final String CHAVE = "arcanecontrol_gojo";

    private static CompoundTag dados(ServerPlayer jogador) {
        CompoundTag raiz = jogador.getPersistentData();
        if (!raiz.contains(Player.PERSISTED_NBT_TAG)) raiz.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        CompoundTag persistido = raiz.getCompound(Player.PERSISTED_NBT_TAG);
        if (!persistido.contains(CHAVE)) persistido.put(CHAVE, new CompoundTag());
        return persistido.getCompound(CHAVE);
    }

    public static ProgressoGojo ler(ServerPlayer jogador) {
        CompoundTag tag = dados(jogador);
        return new ProgressoGojo(tag.getInt("xp"), tag.getInt("desbloqueios"), tag.getIntArray("usos"));
    }

    private static void salvar(ServerPlayer jogador, ProgressoGojo progresso) {
        CompoundTag tag = dados(jogador);
        tag.putInt("xp", progresso.xp());
        tag.putInt("desbloqueios", progresso.desbloqueios());
        tag.putIntArray("usos", progresso.usos());
    }

    private static long agora(ServerPlayer jogador) { return jogador.server.overworld().getGameTime(); }

    public static boolean autorizar(ServerPlayer jogador, TecnicaGojo tecnica, boolean desligandoInfinito) {
        if (!jogador.isAlive() || jogador.isSpectator()) return false;
        if (desligandoInfinito) return true;
        return com.cauansilva.arcanecontrol.core.rpg.RpgServidor.conjurar(jogador,
                com.cauansilva.arcanecontrol.core.poderes.PoderAnime.GOJO,tecnica.id-1,tecnica.cooldown);
    }

    public static void desbloquear(ServerPlayer jogador, int id) {
        if(id>=2&&id<=4)com.cauansilva.arcanecontrol.core.rpg.RpgServidor.comprar(jogador,1,id-2);
        else jogador.displayClientMessage(Component.literal("Abra a árvore com H."),true);
    }

    public static void sincronizar(ServerPlayer jogador) {
        ProgressoGojo p = ler(jogador);
        long[] prazos = Arrays.copyOf(dados(jogador).getLongArray("cooldowns"), 5);
        int[] restantes = new int[5];
        for (int i = 0; i < 5; i++) restantes[i] = (int) Math.max(0, Math.min(1800, prazos[i] - agora(jogador)));
        RegistoRede.ENVIAR.send(PacketDistributor.PLAYER.with(() -> jogador),
                new PacoteProgressoGojo(p.xp(), p.desbloqueios(), p.usos(), restantes,
                        GerenciadorMagiasGojo.infinitoAtivo(jogador)));
    }

    @SubscribeEvent
    public static void aoEntrar(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) sincronizar(jogador);
    }

    @SubscribeEvent
    public static void aoRenascer(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) sincronizar(jogador);
    }

    @SubscribeEvent
    public static void aoMudarDimensao(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) sincronizar(jogador);
    }

    @SubscribeEvent
    public static void aoClonar(PlayerEvent.Clone event) {
        CompoundTag origem = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag destino = event.getEntity().getPersistentData();
        if (!destino.contains(Player.PERSISTED_NBT_TAG)) destino.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        destino.getCompound(Player.PERSISTED_NBT_TAG).put(CHAVE, origem.getCompound(CHAVE).copy());
    }

    @SubscribeEvent
    public static void aoDerrotarInimigo(LivingDeathEvent event) {
        if (event.getEntity() instanceof Enemy && event.getSource().getEntity() instanceof ServerPlayer jogador) {
            if(com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes.ler(jogador).ativo()
                    !=com.cauansilva.arcanecontrol.core.poderes.PoderAnime.GOJO)return;
            ProgressoGojo progresso = ler(jogador);
            progresso.ganharXp(25);
            salvar(jogador, progresso);
            sincronizar(jogador);
        }
    }
}
