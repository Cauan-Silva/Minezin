package com.cauansilva.arcanecontrol;

import com.cauansilva.arcanecontrol.core.magia.elemento.RegistoElementos;
import com.cauansilva.arcanecontrol.core.rede.RegistoRede;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArcaneControl.MODID)
public class ArcaneControl {
    public static final String MODID = "arcanecontrol";

    public ArcaneControl() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RegistoRede.registarPacotes();
            RegistoElementos.inicializar();
        });
    }
}
