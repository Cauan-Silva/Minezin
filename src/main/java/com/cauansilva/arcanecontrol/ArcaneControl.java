package com.cauansilva.arcanecontrol;

import com.cauansilva.arcanecontrol.core.client.TeclasControlo;
import com.cauansilva.arcanecontrol.core.magia.GerenciadorMagiasGojo;
import com.cauansilva.arcanecontrol.core.magia.elemento.RegistoElementos;
import com.cauansilva.arcanecontrol.core.rede.RegistoRede;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArcaneControl.MODID)
public class ArcaneControl {
    public static final String MODID = "arcanecontrol";

    public ArcaneControl() {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.CLIENT, ConfigCliente.SPEC);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(GerenciadorMagiasGojo.class);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RegistoRede.registarPacotes();
            RegistoElementos.inicializar();
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(TeclasControlo.TECLA_Z);
            event.register(TeclasControlo.TECLA_X);
            event.register(TeclasControlo.TECLA_C);
            event.register(TeclasControlo.TECLA_V);
            event.register(TeclasControlo.TECLA_R);
            event.register(TeclasControlo.ARVORE);
        }
    }
}
