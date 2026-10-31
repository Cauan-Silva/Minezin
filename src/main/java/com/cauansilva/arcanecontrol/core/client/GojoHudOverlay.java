package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.client.render.GerenciadorCutscene3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "arcanecontrol", value = Dist.CLIENT)
public class GojoHudOverlay {
    private static String ultimaTecnica = "§7Nenhuma técnica ativa";
    private static int corTecnica = 0xFFFFFF;
    private static long tempoNotificacao = 0;
    private static boolean infinitoAtivo = false;

    public static void notificarHabilidade(String nome, int cor) {
        ultimaTecnica = nome;
        corTecnica = cor;
        tempoNotificacao = System.currentTimeMillis();
    }

    public static void alternarInfinito() {
        infinitoAtivo = !infinitoAtivo;
        if (infinitoAtivo) {
            notificarHabilidade("§9∞ Infinito: ATIVO (Barreira 3D)", 0x55FFFF);
        } else {
            notificarHabilidade("§7∞ Infinito: DESATIVADO", 0xAAAAAA);
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics gui = event.getGuiGraphics();
        int sw = event.getWindow().getGuiScaledWidth();
        int sh = event.getWindow().getGuiScaledHeight();

        // =====================================================================
        // MODO CUTSCENE DE ULTIMATE (Barras de Cinema, Seis Olhos & Legendas)
        // =====================================================================
        if (GerenciadorCutscene3D.cutsceneTipo != 0) {
            int tick = GerenciadorCutscene3D.cutsceneTick;
            int tipo = GerenciadorCutscene3D.cutsceneTipo;

            // Barras pretas cinematográficas (Letterbox) no topo e rodapé
            int alturaBarra = Math.min(38, tick * 4);
            gui.fill(0, 0, sw, alturaBarra, 0xEE000000);
            gui.fill(0, sh - alturaBarra, sw, sh, 0xEE000000);

            if (tipo == 4) { // CUTSCENE VAZIO ROXO
                // Banner dos Seis Olhos (Ticks 4 a 28)
                if (tick >= 4 && tick <= 28) {
                    int yFaixa = sh / 4;
                    gui.fill(0, yFaixa - 14, sw, yFaixa + 18, 0xCC050515);
                    gui.fill(0, yFaixa - 14, sw, yFaixa - 12, 0xFF00D4FF);
                    gui.fill(0, yFaixa + 16, sw, yFaixa + 18, 0xFFFF2A55);

                    String txtOlhos = "§b§l◈ SEIS OLHOS: CONVERGÊNCIA + DIVERGÊNCIA ◈";
                    gui.drawCenteredString(mc.font, txtOlhos, sw / 2, yFaixa - 2, 0xFFFFFF);
                }

                // Flash de Impacto Branco/Roxo no momento exato da colisão (Ticks 29 a 33)
                if (tick >= 29 && tick <= 33) {
                    int alphaFlash = (34 - tick) * 45;
                    int corFlash = (alphaFlash << 24) | 0xE5AAFF;
                    gui.fill(0, 0, sw, sh, corFlash);
                }

                // Legenda estilo Anime na barra inferior (sem conflito de aspas!)
                String legenda = tick < 15
                        ? "§b« Técnica Amaldiçoada: Azul... » §7+ §c« Reversão: Vermelho... »"
                        : "§d§l« Técnica Imaginária: VAZIO ROXO (Murasaki)! »";
                gui.drawCenteredString(mc.font, legenda, sw / 2, sh - alturaBarra + 14, 0xFFFFFF);
            }
            else if (tipo == 5) { // CUTSCENE EXPANSÃO DE DOMÍNIO: VAZIO INFINITO
                if (tick >= 4 && tick <= 36) {
                    int yFaixa = sh / 4;
                    gui.fill(0, yFaixa - 14, sw, yFaixa + 18, 0xDD000814);
                    gui.fill(0, yFaixa - 14, sw, yFaixa - 12, 0xFF00FFFF);
                    gui.fill(0, yFaixa + 16, sw, yFaixa + 18, 0xFFFFFFFF);
                    gui.drawCenteredString(mc.font, "§b§l✦ RYŌIKI TENKAI: MURYŌKŪSHO ✦", sw / 2, yFaixa - 2, 0xFFFFFF);
                }
                if (tick >= 34 && tick <= 39) {
                    int alphaFlash = (40 - tick) * 40;
                    gui.fill(0, 0, sw, sh, (alphaFlash << 24) | 0xFFFFFF);
                }
                String legenda = tick < 35
                        ? "§b« Expansão de Domínio... »"
                        : "§f§l« VAZIO INFINITO (Unlimited Void) »";
                gui.drawCenteredString(mc.font, legenda, sw / 2, sh - alturaBarra + 14, 0xFFFFFF);
            }
            return;
        }

        // =====================================================================
        // HUD NORMAL NO CANTO SUPERIOR ESQUERDO
        // =====================================================================
        gui.fill(8, 8, 255, 62, 0x900A0A12);
        gui.renderOutline(8, 8, 247, 54, 0xFF00B4D8);

        gui.drawString(mc.font, "§b§lSEIS OLHOS & ILIMITADO (3D)", 14, 14, 0xFFFFFF, true);

        String statusInfinito = infinitoAtivo ? "§a§lATIVO (Anel 3D ON)" : "§c§lOFF (Pressione C)";
        gui.drawString(mc.font, "§fInfinito: " + statusInfinito, 14, 27, 0xFFFFFF, true);

        if (System.currentTimeMillis() - tempoNotificacao < 4000) {
            gui.drawString(mc.font, ultimaTecnica, 14, 42, corTecnica, true);
        } else {
            gui.drawString(mc.font, "§7[Z] Azul [X] Vermelho [C] Infinito [V] Roxo [R] Domínio", 14, 42, 0xAAAAAA, false);
        }
    }
}
