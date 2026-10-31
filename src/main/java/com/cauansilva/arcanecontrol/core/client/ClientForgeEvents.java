package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.rede.PacoteUsarMagia;
import com.cauansilva.arcanecontrol.core.rede.RegistoRede;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "arcanecontrol", value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        // Z - Azul (Ao) com Esfera 3D e Anéis de Acreção
        if (TeclasControlo.TECLA_Z.consumeClick()) {
            RegistoRede.ENVIAR.sendToServer(new PacoteUsarMagia(1));
            GojoHudOverlay.notificarHabilidade("§b✦ Azul (Ao) - Singularidade 3D", 0x00D4FF);
        }
        // X - Vermelho (Aka) com Núcleo 3D e Anéis de Choque
        if (TeclasControlo.TECLA_X.consumeClick()) {
            RegistoRede.ENVIAR.sendToServer(new PacoteUsarMagia(2));
            GojoHudOverlay.notificarHabilidade("§c✦ Vermelho (Aka) - Orbe 3D Repulsor", 0xFF2A2A);
        }
        // C - Infinito (Mukagen) com Aura Hexagonal/Esférica 3D
        if (TeclasControlo.TECLA_C.consumeClick()) {
            RegistoRede.ENVIAR.sendToServer(new PacoteUsarMagia(3));
            GojoHudOverlay.alternarInfinito();
        }
        // V - ULTIMATE 1: Vazio Roxo (Murasaki) com CUTSCENE + MODELOS 3D + FEIXE VOLUMÉTRICO
        if (TeclasControlo.TECLA_V.consumeClick()) {
            RegistoRede.ENVIAR.sendToServer(new PacoteUsarMagia(4));
            GojoHudOverlay.notificarHabilidade("§d✦ ULTIMATE: VAZIO ROXO (Murasaki) ✦", 0xB829FF);
        }
        // R - ULTIMATE 2: Expansão de Domínio (Vazio Infinito) com CUTSCENE + DOMO 3D + BURACO NEGRO
        if (TeclasControlo.TECLA_R.consumeClick()) {
            RegistoRede.ENVIAR.sendToServer(new PacoteUsarMagia(5));
            GojoHudOverlay.notificarHabilidade("§b§l✦ EXPANSÃO DE DOMÍNIO: VAZIO INFINITO ✦", 0x00FFFF);
        }
    }
}
