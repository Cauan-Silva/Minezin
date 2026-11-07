package com.cauansilva.arcanecontrol.core.vazio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/**
 * HUD do Vazio no cliente (so desenha o que o servidor manda):
 * barra do chefe estilo Souls no topo (vermelha + amarela de dano recente + numero do combo) e Party HUD a esquerda
 * com vida/mana dos aliados. Some sozinho se o servidor parar de mandar (saiu da instancia).
 */
@Mod.EventBusSubscriber(modid = "arcanecontrol", value = Dist.CLIENT)
public final class HudVazio {
    private static PacoteHudVazio atual;
    private static int semPacote = 999;
    private static final BarraChefe BARRA = new BarraChefe();

    static void receber(PacoteHudVazio p) {
        if (p.chefe().isEmpty()) { atual = null; BARRA.reiniciar(); return; }
        if (atual == null) BARRA.reiniciar();
        atual = p; semPacote = 0; BARRA.atualizar(p.vida() / Math.max(1, p.max()), p.max());
    }
    static void mvp(PacoteMvpVazio p) { atual = null; BARRA.reiniciar(); Minecraft.getInstance().setScreen(new TelaMvp(p)); }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || atual == null) return;
        BARRA.tick(); if (++semPacote > 60) { atual = null; BARRA.reiniciar(); }
    }

    @SubscribeEvent public static void desenhar(RenderGuiOverlayEvent.Post e) {
        if (atual == null || e.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        var mc = Minecraft.getInstance(); if (mc.options.hideGui || mc.player == null) return;
        var g = e.getGuiGraphics(); int w = e.getWindow().getGuiScaledWidth();
        chefe(g, mc, w);
        grupo(g, mc);
    }

    private static void chefe(GuiGraphics g, Minecraft mc, int w) {
        int largura = Math.min(320, (int) (w * .6)), x = (w - largura) / 2, y = 18, h = 6;
        g.drawString(mc.font, atual.chefe(), x, y - 11, 0xFFE8DCC8, true);
        int combo = BARRA.combo();
        if (combo > 0) { var t = String.valueOf(combo); g.drawString(mc.font, t, x + largura - mc.font.width(t), y - 11, 0xFFF2C14E, true); }
        g.fill(x - 1, y - 1, x + largura + 1, y + h + 1, 0xFF15100C);       // moldura
        g.fill(x, y, x + largura, y + h, 0xFF2A1F1A);                         // fundo
        g.fill(x, y, x + (int) (largura * BARRA.atraso()), y + h, 0xFFC9A227); // dano recente (amarelo)
        int v = (int) (largura * BARRA.vida());
        g.fill(x, y, x + v, y + h, 0xFF8E1A14);                               // vida
        g.fill(x, y, x + v, y + 1, 0xFFB8322A);                               // brilho de cima
        if (atual.vida() <= atual.max() / 2) g.fill(x + largura / 2, y - 1, x + largura / 2 + 1, y + h + 1, 0xFF15100C); // marca da fase 2
    }

    private static void grupo(GuiGraphics g, Minecraft mc) {
        var eu = mc.player.getUUID(); int x = 6, y = 40;
        for (var m : atual.grupo()) {
            if (m.id().equals(eu)) continue;
            int cor = m.caiu() ? 0xFF777777 : 0xFFFFFFFF;
            g.drawString(mc.font, m.caiu() ? m.nome() + " ✝" : m.nome(), x, y, cor, true);
            int bw = 70; float fv = m.caiu() ? 0 : m.vida() / Math.max(1, m.max());
            g.fill(x, y + 10, x + bw, y + 14, 0xC0201010); g.fill(x, y + 10, x + (int) (bw * fv), y + 14, fv < .3f ? 0xFFE0402A : 0xFFB8322A);
            g.fill(x, y + 15, x + bw, y + 17, 0xC0101020); g.fill(x, y + 15, x + (int) (bw * Math.min(1, m.mana() / 100f)), y + 17, 0xFF3D7BE0);
            g.drawString(mc.font, (int) Math.ceil(m.vida()) + "", x + bw + 4, y + 9, 0xFFD0C0C0, true);
            y += 24;
        }
    }
    private HudVazio() {}
}
