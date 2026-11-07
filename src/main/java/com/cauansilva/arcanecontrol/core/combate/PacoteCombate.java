package com.cauansilva.arcanecontrol.core.combate;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * Servidor -> cliente: IMPACTO (hitstop + tremor na direcao dx,dz; apanhou = quem levou), PERFEITA (Perfect Dodge),
 * CONTRA (contra-ataque reforçado acertou).
 */
public record PacoteCombate(int tipo, float forca, float dx, float dz, boolean apanhou) {
    public static final int IMPACTO = 1, PERFEITA = 2, CONTRA = 3;
    public static void encode(PacoteCombate p, FriendlyByteBuf b) { b.writeVarInt(p.tipo); b.writeFloat(p.forca); b.writeFloat(p.dx); b.writeFloat(p.dz); b.writeBoolean(p.apanhou); }
    public static PacoteCombate decode(FriendlyByteBuf b) { return new PacoteCombate(b.readVarInt(), b.readFloat(), b.readFloat(), b.readFloat(), b.readBoolean()); }
    public static void handle(PacoteCombate p, Supplier<NetworkEvent.Context> c) {
        c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CombateCliente.receber(p)));
        c.get().setPacketHandled(true);
    }
}
