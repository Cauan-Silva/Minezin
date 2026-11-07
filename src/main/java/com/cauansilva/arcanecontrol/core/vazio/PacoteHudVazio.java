package com.cauansilva.arcanecontrol.core.vazio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.*;
import java.util.function.Supplier;

/**
 * Servidor -> cliente, a cada 5 ticks durante o combate no Vazio: barra do chefe e o Party HUD (vida/mana dos aliados).
 * chefe vazio = esconder o HUD.
 */
public record PacoteHudVazio(String chefe, float vida, float max, List<Membro> grupo) {
    public record Membro(UUID id, String nome, float vida, float max, float mana, boolean caiu) {}
    public static PacoteHudVazio limpar() { return new PacoteHudVazio("", 0, 1, List.of()); }
    public static void encode(PacoteHudVazio p, FriendlyByteBuf b) {
        b.writeUtf(p.chefe, 64); b.writeFloat(p.vida); b.writeFloat(p.max); b.writeVarInt(p.grupo.size());
        for (var m : p.grupo) { b.writeUUID(m.id); b.writeUtf(m.nome, 32); b.writeFloat(m.vida); b.writeFloat(m.max); b.writeFloat(m.mana); b.writeBoolean(m.caiu); }
    }
    public static PacoteHudVazio decode(FriendlyByteBuf b) {
        String c = b.readUtf(64); float v = b.readFloat(), mx = b.readFloat(); int n = Math.min(8, b.readVarInt());
        var g = new ArrayList<Membro>();
        for (int i = 0; i < n; i++) g.add(new Membro(b.readUUID(), b.readUtf(32), b.readFloat(), b.readFloat(), b.readFloat(), b.readBoolean()));
        return new PacoteHudVazio(c, v, mx, g);
    }
    public static void handle(PacoteHudVazio p, Supplier<NetworkEvent.Context> c) {
        c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HudVazio.receber(p)));
        c.get().setPacketHandled(true);
    }
}
