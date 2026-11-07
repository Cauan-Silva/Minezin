package com.cauansilva.arcanecontrol.core.rede;

import com.cauansilva.arcanecontrol.ArcaneControl;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;
import java.util.Optional;

public class RegistoRede {
    private static final String PROTOCOL_VERSION = "22"; // 21: HUD do Vazio + MVP; 22: polimento de combate (PacoteCombate, acao 11); 20: reliquias (menu da Bolsa, acao 9); 19: Void System (efeitos 800-802); 18: kit 12 (Killua), efeitos 700-717; 17: kit 16 (Subaru), efeitos 600-605
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
            ENVIAR.registerMessage(id++,com.cauansilva.arcanecontrol.core.vazio.PacoteHudVazio.class,com.cauansilva.arcanecontrol.core.vazio.PacoteHudVazio::encode,com.cauansilva.arcanecontrol.core.vazio.PacoteHudVazio::decode,com.cauansilva.arcanecontrol.core.vazio.PacoteHudVazio::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        ENVIAR.registerMessage(id++,com.cauansilva.arcanecontrol.core.vazio.PacoteMvpVazio.class,com.cauansilva.arcanecontrol.core.vazio.PacoteMvpVazio::encode,com.cauansilva.arcanecontrol.core.vazio.PacoteMvpVazio::decode,com.cauansilva.arcanecontrol.core.vazio.PacoteMvpVazio::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        ENVIAR.registerMessage(id++,com.cauansilva.arcanecontrol.core.combate.PacoteCombate.class,com.cauansilva.arcanecontrol.core.combate.PacoteCombate::encode,com.cauansilva.arcanecontrol.core.combate.PacoteCombate::decode,com.cauansilva.arcanecontrol.core.combate.PacoteCombate::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
