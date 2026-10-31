package com.cauansilva.arcanecontrol.core.rede;

import com.cauansilva.arcanecontrol.ArcaneControl;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class RegistoRede {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel ENVIAR = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ArcaneControl.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registarPacotes() {
        int id = 0;
        ENVIAR.registerMessage(id++, PacoteUsarMagia.class,
                PacoteUsarMagia::encode,
                PacoteUsarMagia::decode,
                PacoteUsarMagia::handle);
        ENVIAR.registerMessage(id++, PacoteSincronizarCutscene3D.class,
                PacoteSincronizarCutscene3D::encode,
                PacoteSincronizarCutscene3D::decode,
                PacoteSincronizarCutscene3D::handle);
    }
}
