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
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) return;
        if(TeclasControlo.PODERES.consumeClick()) {
            minecraft.setScreen(new PoderesScreen());return;
        }
        if(TeclasControlo.ARVORE_RPG.consumeClick()||TeclasControlo.ARVORE.consumeClick()) {
            minecraft.setScreen(new ArvoreRpgScreen());return;
        }
        var kit=PoderesCliente.ativo();
        if(TeclasControlo.TECLA_G.consumeClick())RegistoRede.ENVIAR.sendToServer(new PacoteUsarMagia(kit.tecnica(4)));
        var teclas=new net.minecraft.client.KeyMapping[]{TeclasControlo.TECLA_Z,TeclasControlo.TECLA_X,
                TeclasControlo.TECLA_C,TeclasControlo.TECLA_V,TeclasControlo.TECLA_R};
        for(int i=0;i<teclas.length;i++) if(teclas[i].consumeClick()) {
            int id=kit.tecnica(i);if(id!=0)RegistoRede.ENVIAR.sendToServer(new PacoteUsarMagia(id));
        }

    }
}
