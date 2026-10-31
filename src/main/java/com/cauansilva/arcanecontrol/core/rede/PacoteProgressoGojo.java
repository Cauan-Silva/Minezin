package com.cauansilva.arcanecontrol.core.rede;

import com.cauansilva.arcanecontrol.core.client.ProgressoGojoCliente;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacoteProgressoGojo(int xp, int desbloqueios, int[] usos, int[] cooldowns, boolean infinito) {
    public static void encode(PacoteProgressoGojo msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.xp);
        buf.writeVarInt(msg.desbloqueios);
        for (int i = 0; i < 5; i++) buf.writeVarInt(msg.usos[i]);
        for (int i = 0; i < 5; i++) buf.writeVarInt(msg.cooldowns[i]);
        buf.writeBoolean(msg.infinito);
    }

    public static PacoteProgressoGojo decode(FriendlyByteBuf buf) {
        int xp = buf.readVarInt(), desbloqueios = buf.readVarInt();
        int[] usos = new int[5], cooldowns = new int[5];
        for (int i = 0; i < 5; i++) usos[i] = buf.readVarInt();
        for (int i = 0; i < 5; i++) cooldowns[i] = buf.readVarInt();
        return new PacoteProgressoGojo(xp, desbloqueios, usos, cooldowns, buf.readBoolean());
    }

    public static void handle(PacoteProgressoGojo msg, Supplier<NetworkEvent.Context> contexto) {
        NetworkEvent.Context ctx = contexto.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ProgressoGojoCliente.receber(msg)));
        ctx.setPacketHandled(true);
    }
}
