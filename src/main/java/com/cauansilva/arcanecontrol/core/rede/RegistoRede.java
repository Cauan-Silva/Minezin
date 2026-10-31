package com.cauansilva.arcanecontrol.core.rede;

import com.cauansilva.arcanecontrol.ArcaneControl;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class RegistoRede {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ArcaneControl.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public static void registarPacotes() {
        INSTANCE.messageBuilder(EfeitoVisualPacote.class, packetId++)
            .encoder(EfeitoVisualPacote::encode)
            .decoder(EfeitoVisualPacote::decode)
            .consumerMainThread(EfeitoVisualPacote::handle)
            .add();
    }
}
