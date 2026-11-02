package com.cauansilva.arcanecontrol.core.rede;

import com.cauansilva.arcanecontrol.ArcaneControl;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;
import java.util.Optional;

public class RegistoRede {
    private static final String PROTOCOL_VERSION = "13";
    public static final SimpleChannel ENVIAR = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ArcaneControl.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registarPacotes() {
        int id = 0;
        ENVIAR.registerMessage(id++,PacoteEfeitoDespertar.class,PacoteEfeitoDespertar::encode,PacoteEfeitoDespertar::decode,PacoteEfeitoDespertar::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        ENVIAR.registerMessage(id++,PacoteEstadoRpg.class,PacoteEstadoRpg::encode,PacoteEstadoRpg::decode,PacoteEstadoRpg::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        ENVIAR.registerMessage(id++,PacoteAcaoRpg.class,PacoteAcaoRpg::encode,PacoteAcaoRpg::decode,PacoteAcaoRpg::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        ENVIAR.registerMessage(id++,PacoteAlvosCid.class,PacoteAlvosCid::encode,PacoteAlvosCid::decode,PacoteAlvosCid::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        ENVIAR.registerMessage(id++,PacoteAcaoPoder.class,PacoteAcaoPoder::encode,PacoteAcaoPoder::decode,PacoteAcaoPoder::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        ENVIAR.registerMessage(id++,PacoteEstadoPoderes.class,PacoteEstadoPoderes::encode,PacoteEstadoPoderes::decode,PacoteEstadoPoderes::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        ENVIAR.registerMessage(id++, PacoteUsarMagia.class,
                PacoteUsarMagia::encode,
                PacoteUsarMagia::decode,
                PacoteUsarMagia::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        ENVIAR.registerMessage(id++, PacoteSincronizarCutscene3D.class,
                PacoteSincronizarCutscene3D::encode,
                PacoteSincronizarCutscene3D::decode,
                PacoteSincronizarCutscene3D::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        ENVIAR.registerMessage(id++, PacoteDesbloquearTecnica.class,
                PacoteDesbloquearTecnica::encode, PacoteDesbloquearTecnica::decode,
                PacoteDesbloquearTecnica::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        ENVIAR.registerMessage(id++, PacoteProgressoGojo.class,
                PacoteProgressoGojo::encode, PacoteProgressoGojo::decode,
                PacoteProgressoGojo::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
