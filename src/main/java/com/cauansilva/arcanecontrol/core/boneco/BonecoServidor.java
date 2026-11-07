package com.cauansilva.arcanecontrol.core.boneco;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Registra o dano FINAL (ultima prioridade, depois de todos os modificadores) e anula para o boneco nao morrer. */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class BonecoServidor {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void dano(LivingDamageEvent e){
        if(!(e.getEntity() instanceof BonecoEntity b))return;
        if(e.getSource().getEntity() instanceof ServerPlayer p)b.registrar(p,e.getAmount());
        e.setAmount(0);
    }
    private BonecoServidor(){}
}
