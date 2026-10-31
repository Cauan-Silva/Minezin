package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.progressao.ProgressoGojo;
import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.progressao.TecnicaGojo;
import com.cauansilva.arcanecontrol.core.rede.PacoteDesbloquearTecnica;
import com.cauansilva.arcanecontrol.core.rede.RegistoRede;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ArvoreGojoScreen extends Screen {
    private final Button[] botoes = new Button[5];
    private int largura, esquerda, topo;

    public ArvoreGojoScreen() { super(Component.literal("Seis Olhos • Árvore de técnicas")); }

    @Override
    protected void init() {
        largura = Math.min(380, width - 16);
        esquerda = (width - largura) / 2;
        topo = Math.max(8, (height - 220) / 2);
        for (TecnicaGojo t : TecnicaGojo.values()) {
            Button botao = Button.builder(Component.literal(t.nome), b -> {
                b.active = false;
                RegistoRede.ENVIAR.sendToServer(new PacoteDesbloquearTecnica(t.id));
            }).bounds(esquerda + 8, topo + 64 + (t.id - 1) * 23, largura - 16, 20).build();
            StringBuilder requisitos = new StringBuilder();
            for (TecnicaGojo anterior : TecnicaGojo.values()) {
                if ((t.requisitos & anterior.bit()) != 0) requisitos.append(anterior.nome).append("; ");
            }
            botao.setTooltip(Tooltip.create(Component.literal("Nível " + t.nivel + " • " + t.custo
                    + " ponto(s)\nRequer: " + (requisitos.isEmpty() ? "nenhuma técnica" : requisitos)
                    + "\nCada 20 usos: +1 maestria (máx. 5). Cada grau reduz a recarga em 5%.")));
            botoes[t.id - 1] = addRenderableWidget(botao);
        }
        addRenderableWidget(Button.builder(textoCinematicas(), b -> {
            ConfigCliente.CINEMATICAS.set(!ConfigCliente.CINEMATICAS.get());
            ConfigCliente.SPEC.save();
            b.setMessage(textoCinematicas());
        }).bounds(esquerda + 8, topo + 188, largura - 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"), b -> onClose())
                .bounds(esquerda + largura - 84, topo + 188, 76, 20).build());
    }

    private Component textoCinematicas() {
        return Component.literal("Cinematics: " + (ConfigCliente.CINEMATICAS.get() ? "ON" : "OFF"));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.fill(esquerda, topo, esquerda + largura, topo + 218, 0xF00A1020);
        gui.renderOutline(esquerda, topo, largura, 218, 0xFF00C9E8);
        gui.drawCenteredString(font, title, width / 2, topo + 10, 0x55FFFF);
        ProgressoGojo p = ProgressoGojoCliente.progresso;
        gui.drawCenteredString(font, "Nível " + p.nivel() + " • " + p.pontos() + " ponto(s) • "
                + p.xp() + " XP", width / 2, topo + 27, 0xFFFFFF);
        int barra = largura - 24;
        gui.fill(esquerda + 12, topo + 42, esquerda + largura - 12, topo + 46, 0xFF25334A);
        int fracao = p.nivel() == ProgressoGojo.NIVEL_MAXIMO ? 100 : p.xp() % 100;
        gui.fill(esquerda + 12, topo + 42, esquerda + 12 + barra * fracao / 100, topo + 46, 0xFF00C9E8);
        gui.drawCenteredString(font, "Uso: +5 XP • Inimigo derrotado: +25 XP", width / 2, topo + 51, 0xAABBCD);
        String[] teclas = {"Z", "X", "C", "V", "R"};
        for (TecnicaGojo t : TecnicaGojo.values()) {
            Button botao = botoes[t.id - 1];
            boolean tem = p.tem(t);
            String estado = tem ? "M" + p.maestria(t) : "Nv." + t.nivel() + " / " + t.custo + " pt";
            int recarga = (ProgressoGojoCliente.cooldowns[t.id - 1] + 19) / 20;
            if (tem && recarga > 0) estado += " • " + recarga + "s";
            botao.setMessage(Component.literal("[" + teclas[t.id - 1] + "] " + t.nome + " • "
                    + (tem ? "Aprendida • " : "Desbloquear • ") + estado));
            botao.active = ProgressoGojoCliente.recebido && p.podeDesbloquear(t);
        }
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
