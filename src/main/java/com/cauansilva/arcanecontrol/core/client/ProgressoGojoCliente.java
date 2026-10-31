package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.progressao.ProgressoGojo;
import com.cauansilva.arcanecontrol.core.rede.PacoteProgressoGojo;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "arcanecontrol", value = Dist.CLIENT)
public final class ProgressoGojoCliente {
    public static ProgressoGojo progresso = new ProgressoGojo();
    public static int[] cooldowns = new int[5];
    public static boolean infinito;
    public static boolean recebido;

    public static void receber(PacoteProgressoGojo msg) {
        progresso = new ProgressoGojo(msg.xp(), msg.desbloqueios(), msg.usos());
        cooldowns = msg.cooldowns().clone();
        infinito = msg.infinito();
        recebido = true;
    }

    @SubscribeEvent
    public static void aoTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().isPaused()) return;
        for (int i = 0; i < 5; i++) if (cooldowns[i] > 0) cooldowns[i]--;
    }

    @SubscribeEvent
    public static void aoDesconectar(ClientPlayerNetworkEvent.LoggingOut event) {
        progresso = new ProgressoGojo();
        cooldowns = new int[5];
        infinito = false;
        recebido = false;
    }
}
