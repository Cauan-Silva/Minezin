package com.cauansilva.arcanecontrol.core.vazio;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Transicao do Void System no cliente (pacotes 800/801 de VazioServidor): tela preta que clareia na arena com o HUD
 * escondido durante a entrada travada; na saida, so o fade. Camera cinematica e fala do chefe ficam para o PR seguinte.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class VazioCliente {
    /** Entrada: preto cheio por SEGURA ticks, clareia em CLAREIA; HUD volta no fim da entrada travada. */
    static final int SEGURA=10,CLAREIA=30,SAIDA_SEGURA=6,SAIDA_CLAREIA=20;
    private static long inicio=-1000,fimHud=-1000;private static int segura,clareia;

    public static boolean trata(int tipo){return tipo==VazioServidor.ENTRADA||tipo==VazioServidor.SAIDA;}
    public static void iniciar(int tipo){
        var mc=Minecraft.getInstance();if(mc.level==null)return;long agora=mc.level.getGameTime();inicio=agora;
        if(tipo==VazioServidor.ENTRADA){segura=SEGURA;clareia=CLAREIA;fimHud=agora+EstadoInstancia.ENTRADA;}
        else{segura=SAIDA_SEGURA;clareia=SAIDA_CLAREIA;fimHud=agora;}
    }
    /** Opacidade do preto agora (0 = nada). */
    static float preto(float parcial){
        var mc=Minecraft.getInstance();if(mc.level==null)return 0;double t=mc.level.getGameTime()-inicio+parcial;
        if(t<0||t>segura+clareia)return 0;if(t<segura)return 1;double u=(t-segura)/clareia;return (float)(1-u*u*(3-2*u));
    }
    private static boolean hudEscondido(){var mc=Minecraft.getInstance();return mc.level!=null&&mc.level.getGameTime()<fimHud;}

    @SubscribeEvent public static void esconder(RenderGuiOverlayEvent.Pre e){if(hudEscondido())e.setCanceled(true);}
    @SubscribeEvent public static void fade(RenderGuiEvent.Post e){
        float a=preto(e.getPartialTick());if(a<=.003f)return;var g=e.getGuiGraphics();
        g.fill(0,0,g.guiWidth(),g.guiHeight(),((int)(a*255)<<24));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){inicio=-1000;fimHud=-1000;}
    private VazioCliente(){}
}
