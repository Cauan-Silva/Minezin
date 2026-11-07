package com.cauansilva.arcanecontrol.core.vazio;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Tela de MVP: placar da luta (dano, cura, recebido, quedas, maior golpe), destaques e Dicas de Sobrevivencia. */
public final class TelaMvp extends Screen {
    private final PacoteMvpVazio p;
    private static final int L = 340;
    public TelaMvp(PacoteMvpVazio p) { super(Component.literal(p.vitoria() ? "Vitória" : "Derrota")); this.p = p; }
    private int altura() { return 74 + p.linhas().size() * 14 + p.dicas().size() * 22 + 30; }
    @Override protected void init() {
        int y = (height - altura()) / 2 + altura() - 24;
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose()).bounds(width / 2 - 40, y, 80, 18).build());
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int x = (width - L) / 2, y = (height - altura()) / 2;
        g.fill(x - 2, y - 2, x + L + 2, y + altura() + 2, 0xFF6A5326);
        g.fill(x, y, x + L, y + altura(), 0xF0120E0B);
        g.drawCenteredString(font, p.vitoria() ? "§6§lDESAFIO VENCIDO" : "§c§lO GRUPO CAIU", width / 2, y + 8, 0xFFFFFF);
        g.drawCenteredString(font, "§7" + p.chefe() + " · " + p.segundos() / 60 + ":" + String.format("%02d", p.segundos() % 60), width / 2, y + 20, 0xFFFFFF);
        int ty = y + 42; int[] col = {x + 8, x + 110, x + 160, x + 205, x + 255, x + 300};
        String[] cab = {"Jogador", "Dano", "Cura", "Recebido", "Quedas", "Golpe"};
        for (int i = 0; i < cab.length; i++) g.drawString(font, "§8" + cab[i], col[i], ty, 0xFFFFFF, false);
        ty += 12;
        for (var l : p.linhas()) {
            boolean mvp = l.destaque().startsWith("MVP");
            g.drawString(font, (mvp ? "§6★ " : "§f") + l.nome(), col[0], ty, 0xFFFFFF, false);
            g.drawString(font, "§c" + l.dano(), col[1], ty, 0xFFFFFF, false);
            g.drawString(font, "§a" + l.cura(), col[2], ty, 0xFFFFFF, false);
            g.drawString(font, "§7" + l.recebido(), col[3], ty, 0xFFFFFF, false);
            g.drawString(font, (l.quedas() > 0 ? "§c" : "§7") + l.quedas(), col[4], ty, 0xFFFFFF, false);
            g.drawString(font, "§e" + l.maiorGolpe(), col[5], ty, 0xFFFFFF, false);
            if (!l.destaque().isEmpty() && mx >= col[0] && mx < col[1] && my >= ty && my < ty + 10) g.renderTooltip(font, Component.literal(l.destaque()), mx, my);
            ty += 14;
        }
        ty += 8; g.drawString(font, "§bDicas de Sobrevivência", x + 8, ty, 0xFFFFFF, false); ty += 12;
        for (var d : p.dicas()) { g.drawWordWrap(font, Component.literal("• " + d), x + 8, ty, L - 16, 0xFFCFC6B8); ty += 22; }
        // destaques em linha no rodape
        var dest = new StringBuilder();
        for (var l : p.linhas()) if (!l.destaque().isEmpty()) dest.append(dest.length() > 0 ? "   " : "").append("§6").append(l.destaque()).append("§7: ").append(l.nome());
        g.drawCenteredString(font, dest.toString(), width / 2, y + 28, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }
}
