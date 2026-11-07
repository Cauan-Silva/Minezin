package com.cauansilva.arcanecontrol.core.vazio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.*;
import java.util.function.Supplier;

/** Servidor -> cliente no fim da luta (vitoria ou wipe): abre a Tela de MVP. */
public record PacoteMvpVazio(boolean vitoria, String chefe, int segundos, List<PlacarVazio.Linha> linhas, List<String> dicas) {
    public static void encode(PacoteMvpVazio p, FriendlyByteBuf b) {
        b.writeBoolean(p.vitoria); b.writeUtf(p.chefe, 64); b.writeVarInt(p.segundos); b.writeVarInt(p.linhas.size());
        for (var l : p.linhas) { b.writeUtf(l.nome(), 32); b.writeVarInt(l.dano()); b.writeVarInt(l.cura()); b.writeVarInt(l.recebido()); b.writeVarInt(l.quedas()); b.writeVarInt(l.maiorGolpe()); b.writeUtf(l.destaque(), 32); }
        b.writeVarInt(p.dicas.size()); for (var d : p.dicas) b.writeUtf(d, 256);
    }
    public static PacoteMvpVazio decode(FriendlyByteBuf b) {
        boolean v = b.readBoolean(); String c = b.readUtf(64); int s = b.readVarInt(); int n = Math.min(8, b.readVarInt());
        var l = new ArrayList<PlacarVazio.Linha>();
        for (int i = 0; i < n; i++) l.add(new PlacarVazio.Linha(b.readUtf(32), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readUtf(32)));
        int k = Math.min(6, b.readVarInt()); var d = new ArrayList<String>(); for (int i = 0; i < k; i++) d.add(b.readUtf(256));
        return new PacoteMvpVazio(v, c, s, l, d);
    }
    public static void handle(PacoteMvpVazio p, Supplier<NetworkEvent.Context> c) {
        c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HudVazio.mvp(p)));
        c.get().setPacketHandled(true);
    }
}
