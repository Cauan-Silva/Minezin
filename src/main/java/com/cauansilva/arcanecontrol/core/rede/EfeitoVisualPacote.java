package com.cauansilva.arcanecontrol.core.rede;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EfeitoVisualPacote {
    private final String tipoEfeito;
    private final float intensidade;

    public EfeitoVisualPacote(String tipoEfeito, float intensidade) {
        this.tipoEfeito = tipoEfeito;
        this.intensidade = intensidade;
    }

    public static void encode(EfeitoVisualPacote msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.tipoEfeito);
        buf.writeFloat(msg.intensidade);
    }

    public static EfeitoVisualPacote decode(FriendlyByteBuf buf) {
        return new EfeitoVisualPacote(buf.readUtf(), buf.readFloat());
    }

    public static void handle(EfeitoVisualPacote msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Lógica no cliente para efeitos visuais/shaders
        });
        ctx.get().setPacketHandled(true);
    }
}
