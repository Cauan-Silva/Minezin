package com.cauansilva.arcanecontrol.core.rede;

import com.cauansilva.arcanecontrol.core.progressao.ProgressaoGojo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacoteDesbloquearTecnica(int id) {
    public static void encode(PacoteDesbloquearTecnica msg, FriendlyByteBuf buf) { buf.writeVarInt(msg.id); }
    public static PacoteDesbloquearTecnica decode(FriendlyByteBuf buf) { return new PacoteDesbloquearTecnica(buf.readVarInt()); }
    public static void handle(PacoteDesbloquearTecnica msg, Supplier<NetworkEvent.Context> contexto) {
        NetworkEvent.Context ctx = contexto.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) ProgressaoGojo.desbloquear(ctx.getSender(), msg.id);
        });
        ctx.setPacketHandled(true);
    }
}
