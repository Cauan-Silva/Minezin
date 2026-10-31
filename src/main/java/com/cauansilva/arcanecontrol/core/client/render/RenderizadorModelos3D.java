package com.cauansilva.arcanecontrol.core.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class RenderizadorModelos3D {

    /**
     * Renderiza uma Esfera 3D procedural suave com camadas de brilho (Núcleo + Aura Externa).
     */
    public static void desenharEsfera3D(PoseStack poseStack, float raio, int r, int g, int b, int a, int segmentos) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < segmentos; i++) {
            float lat0 = (float) (Math.PI * (-0.5 + (double) i / segmentos));
            float z0 = (float) Math.sin(lat0) * raio;
            float zr0 = (float) Math.cos(lat0) * raio;

            float lat1 = (float) (Math.PI * (-0.5 + (double) (i + 1) / segmentos));
            float z1 = (float) Math.sin(lat1) * raio;
            float zr1 = (float) Math.cos(lat1) * raio;

            buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int j = 0; j <= segmentos; j++) {
                float lng = (float) (2 * Math.PI * (double) j / segmentos);
                float x = (float) Math.cos(lng);
                float y = (float) Math.sin(lng);

                buffer.vertex(matrix, x * zr0, z0, y * zr0).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x * zr1, z1, y * zr1).color(r, g, b, a).endVertex();
            }
            tesselator.end();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Renderiza um Anel / Disco 3D de Distorção Espacial ao redor das esferas ou do buraco negro.
     */
    public static void desenharAnel3D(PoseStack poseStack, float raioInterno, float raioExterno, int r, int g, int b, int a, int lados) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= lados; i++) {
            double angulo = 2.0 * Math.PI * i / lados;
            float cos = (float) Math.cos(angulo);
            float sin = (float) Math.sin(angulo);

            buffer.vertex(matrix, cos * raioInterno, 0, sin * raioInterno).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, cos * raioExterno, 0, sin * raioExterno).color(r, g, b, 15).endVertex();
        }
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Renderiza um Cilindro / Feixe de Energia 3D Volumétrico (Estilo o laser/feixe do vídeo!).
     */
    public static void desenharFeixe3D(PoseStack poseStack, float raio, float comprimento, int r, int g, int b, int a, int lados) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= lados; i++) {
            double ang = 2.0 * Math.PI * i / lados;
            float x = (float) Math.cos(ang) * raio;
            float y = (float) Math.sin(ang) * raio;

            buffer.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x, y, comprimento).color(r, g, b, a).endVertex();
        }
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
