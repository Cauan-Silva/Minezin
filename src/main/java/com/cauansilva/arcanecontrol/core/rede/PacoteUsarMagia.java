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
                if (com.cauansilva.arcanecontrol.core.cid.PoderesCid.dados(player).getLong("cid_ocupado") > player.server.overworld().getGameTime()) return;
                if (msg.idMagia >= 11 && msg.idMagia <= 14) com.cauansilva.arcanecontrol.core.cid.PoderesCid.executar(player,msg.idMagia);
                else GerenciadorMagiasGojo.executarMagia(player, msg.idMagia);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
