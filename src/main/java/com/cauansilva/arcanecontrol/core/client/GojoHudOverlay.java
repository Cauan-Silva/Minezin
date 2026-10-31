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
    public static void notificarHabilidade(String nome, int cor) {
        ultimaTecnica = nome;
        corTecnica = cor;
        tempoNotificacao = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id())) return;
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
            double entrada = com.cauansilva.arcanecontrol.core.client.render.RoteiroGojo.suave(tick / 8.0);
            double saida = com.cauansilva.arcanecontrol.core.client.render.RoteiroGojo.suave(
                    (GerenciadorCutscene3D.cutsceneDuracao - tick) / 12.0);
            int alturaBarra = (int) (Math.min(38, sh * .12) * Math.min(entrada, saida));
            gui.fill(0, 0, sw, alturaBarra, 0xEE000000);
            gui.fill(0, sh - alturaBarra, sw, sh, 0xEE000000);

            int legendaY=sh-Math.max(12,alturaBarra/2+4);
            boolean clarao=com.cauansilva.arcanecontrol.ConfigCliente.CLARAO_GOJO.get();
            if(tipo==4) {
                if(clarao && tick>=30 && tick<34) {
                    int a=(34-tick)*14;
                    gui.fill(0,0,sw,sh,(a<<24)|0xD8B9FF);
                }
                String legenda=tick<15?"Azul · Vermelho":tick<30?"Técnica Imaginária":"Roxo";
                gui.drawCenteredString(mc.font,legenda,sw/2,legendaY,0xE6CFFF);
            } else if(tipo==5) {
                if(clarao && tick>=35 && tick<39) {
                    int a=(39-tick)*14;
                    gui.fill(0,0,sw,sh,(a<<24)|0xE5F3FF);
                }
                gui.drawCenteredString(mc.font,tick<35?"Expansão de Domínio":"Vazio Infinito",
                        sw/2,legendaY,0xDDEEFF);
            }
            return;
        }

        // =====================================================================
        // HUD NORMAL NO CANTO SUPERIOR ESQUERDO
        // =====================================================================
        if(PoderesCliente.ativo()!=com.cauansilva.arcanecontrol.core.poderes.PoderAnime.GOJO)return;
        gui.fill(8, 8, 255, 62, 0x900A0A12);
        gui.renderOutline(8, 8, 247, 54, 0xFF00B4D8);

        gui.drawString(mc.font, "§b§lSEIS OLHOS · ILIMITADO", 14, 14, 0xFFFFFF, true);

        String statusInfinito = ProgressoGojoCliente.infinito ? "§a§lATIVO" : "§c§lOFF (Pressione C)";
        gui.drawString(mc.font, "§fInfinito: " + statusInfinito, 14, 27, 0xFFFFFF, true);
        gui.drawString(mc.font, "§b[K] Árvore §7• Nv. " + ProgressoGojoCliente.progresso.nivel()
                + " • " + ProgressoGojoCliente.progresso.pontos() + " ponto(s)", 14, 68, 0xFFFFFF, true);

        if (System.currentTimeMillis() - tempoNotificacao < 4000) {
            gui.drawString(mc.font, ultimaTecnica, 14, 42, corTecnica, true);
        } else {
            gui.drawString(mc.font, "§7[Z] Azul [X] Vermelho [C] Infinito [V] Roxo [R] Domínio", 14, 42, 0xAAAAAA, false);
        }
    }
}
