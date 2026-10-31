package com.cauansilva.arcanecontrol.core.rede;

import com.cauansilva.arcanecontrol.core.client.render.GerenciadorCutscene3D;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacoteSincronizarCutscene3D {
    private final int tipoEfeito; // 1=Azul3D, 2=Vermelho3D, 3=Infinito3D, 4=CutsceneVazioRoxo, 5=CutsceneDominio
    private final UUID playerId;
    private final double x, y, z;
    private final double dirX, dirY, dirZ;

    public PacoteSincronizarCutscene3D(int tipoEfeito, UUID playerId, double x, double y, double z, double dirX, double dirY, double dirZ) {
        this.tipoEfeito = tipoEfeito;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dirX = dirX;
        this.dirY = dirY;
        this.dirZ = dirZ;
    }

    public static void encode(PacoteSincronizarCutscene3D msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.tipoEfeito);
        buf.writeUUID(msg.playerId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.dirX);
        buf.writeDouble(msg.dirY);
        buf.writeDouble(msg.dirZ);
    }

    public static PacoteSincronizarCutscene3D decode(FriendlyByteBuf buf) {
        return new PacoteSincronizarCutscene3D(
                buf.readInt(),
                buf.readUUID(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble()
        );
    }

    public static void handle(PacoteSincronizarCutscene3D msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                GerenciadorCutscene3D.iniciarEfeitoCliente(
                        msg.tipoEfeito, msg.playerId,
                        msg.x, msg.y, msg.z,
                        msg.dirX, msg.dirY, msg.dirZ
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
