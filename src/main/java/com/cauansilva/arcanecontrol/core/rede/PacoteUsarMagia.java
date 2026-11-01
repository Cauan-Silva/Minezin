package com.cauansilva.arcanecontrol.core.rede;

import com.cauansilva.arcanecontrol.core.magia.GerenciadorMagiasGojo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacoteUsarMagia {
    private final int idMagia;

    public PacoteUsarMagia(int idMagia) {
        this.idMagia = idMagia;
    }

    public static void encode(PacoteUsarMagia msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.idMagia);
    }

    public static PacoteUsarMagia decode(FriendlyByteBuf buf) {
        return new PacoteUsarMagia(buf.readInt());
    }

    public static void handle(PacoteUsarMagia msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                com.cauansilva.arcanecontrol.core.rpg.MovesetsServidor.executar(player,msg.idMagia);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
