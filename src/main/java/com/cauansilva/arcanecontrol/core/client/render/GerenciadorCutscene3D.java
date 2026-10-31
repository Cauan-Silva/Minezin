package com.cauansilva.arcanecontrol.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.client.GojoHudOverlay;
import com.cauansilva.arcanecontrol.core.client.ProgressoGojoCliente;
import com.cauansilva.arcanecontrol.core.progressao.TecnicaGojo;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "arcanecontrol", value = Dist.CLIENT)
public class GerenciadorCutscene3D {

    public static class Efeito3DAtivo {
        public final int tipo; // 1=Azul, 2=Vermelho, 4=VazioRoxo, 5=Dominio
        public final UUID playerId;
        public Vec3 origem;
        public Vec3 direcao;
        public int tickAtual = 0;
        public final int duracaoMax;

        public Efeito3DAtivo(int tipo, UUID playerId, Vec3 origem, Vec3 direcao, int duracaoMax) {
            this.tipo = tipo;
            this.playerId = playerId;
            this.origem = origem;
            this.direcao = direcao;
            this.duracaoMax = duracaoMax;
        }
    }

    private static final List<Efeito3DAtivo> EFEITOS_3D = new ArrayList<>();
    private static net.minecraft.client.multiplayer.ClientLevel mundoCutscene;

    // Estado da Cutscene Local (Para o jogador que ativou a Ultimate)
    public static int cutsceneTipo = 0; // 0=Nenhuma, 4=Vazio Roxo, 5=Expansão de Domínio
    public static int cutsceneTick = 0;
    public static int cutsceneDuracao = 0;
    private static CameraType cameraAnterior = null;
    private static Vec3 frenteCamera = new Vec3(0, 0, 1);
    private static Vec3 posicaoCameraInicial = Vec3.ZERO;

    public static void iniciarEfeitoCliente(int tipo, UUID playerId, double x, double y, double z, double dx, double dy, double dz) {
        Minecraft mc = Minecraft.getInstance();
        if (tipo == 6) {
            for (Efeito3DAtivo efeito : EFEITOS_3D) {
                if (efeito.tipo == 4 && efeito.playerId.equals(playerId)) {
                    efeito.origem = new Vec3(x, y, z);
                    efeito.direcao = new Vec3(dx, dy, dz);
                }
            }
            return;
        }
        if (EFEITOS_3D.size() >= 64) EFEITOS_3D.remove(0);
        if (tipo >= 11 && tipo <= 15) {
            com.cauansilva.arcanecontrol.core.cid.EfeitosCid.iniciar(tipo,playerId,new Vec3(x,y,z),new Vec3(dx,dy,dz));
            return;
        }
        TecnicaGojo tecnica = TecnicaGojo.porId(tipo);
        if (tecnica != null && mc.player != null && mc.player.getUUID().equals(playerId)) {
            GojoHudOverlay.notificarHabilidade(tecnica.nome, 0x55FFFF);
        }
        com.cauansilva.arcanecontrol.core.client.AnimacoesGojo.tocar(playerId, tipo);
        Vec3 origem = new Vec3(x, y, z);
        Vec3 direcao = new Vec3(dx, dy, dz);

        if (tipo == 1) {
            EFEITOS_3D.add(new Efeito3DAtivo(1, playerId, origem, direcao, 60));
        } else if (tipo == 2) {
            EFEITOS_3D.add(new Efeito3DAtivo(2, playerId, origem, direcao, 28));
        } else if (tipo == 3) {
            // O estado do Infinito local vem do snapshot validado pelo servidor.
        } else if (tipo == 4) {
            EFEITOS_3D.add(new Efeito3DAtivo(4, playerId, origem, direcao, 85));
            if (mc.player != null && mc.player.getUUID().equals(playerId)) {
                iniciarCutsceneLocal(4, 85);
            }
        } else if (tipo == 5) {
            EFEITOS_3D.add(new Efeito3DAtivo(5, playerId, origem, direcao, 160));
            if (mc.player != null && mc.player.getUUID().equals(playerId)) {
                iniciarCutsceneLocal(5, 160);
            }
        }
    }

    public static void iniciarCutsceneLocal(int tipo, int duracao) {
        if (!ConfigCliente.CINEMATICAS.get()) return;
        Minecraft mc = Minecraft.getInstance();
        finalizarCutscene();
        cutsceneTipo = tipo;
        cutsceneTick = 0;
        cutsceneDuracao = duracao;
        cameraAnterior = mc.options.getCameraType();
        mundoCutscene = mc.level;
        Vec3 olhar = mc.player.getLookAngle();
        frenteCamera = new Vec3(olhar.x, 0, olhar.z).normalize();
        if (frenteCamera.lengthSqr() < 1.0E-6) frenteCamera = new Vec3(0, 0, 1);
        posicaoCameraInicial = mc.gameRenderer.getMainCamera().getPosition();
        // Muda automaticamente para câmera cinemática em 3ª pessoa para ver o personagem e as esferas 3D!
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    public static void finalizarCutscene() {
        Minecraft mc = Minecraft.getInstance();
        if (cameraAnterior != null) mc.options.setCameraType(cameraAnterior);
        cameraAnterior = null;
        mundoCutscene = null;
        cutsceneTipo = 0;
        cutsceneTick = 0;
    }

    @SubscribeEvent
    public static void aoDesconectar(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        finalizarCutscene();
        EFEITOS_3D.clear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !mc.player.isAlive()) {
            finalizarCutscene();
            EFEITOS_3D.clear();
            return;
        }
        if (cutsceneTipo != 0 && (mundoCutscene != mc.level || !ConfigCliente.CINEMATICAS.get())) {
            finalizarCutscene();
            EFEITOS_3D.clear();
        }
        if (mc.isPaused()) return;

        Iterator<Efeito3DAtivo> it = EFEITOS_3D.iterator();
        while (it.hasNext()) {
            Efeito3DAtivo ef = it.next();
            ef.tickAtual++;
            if (ef.tickAtual >= ef.duracaoMax) {
                it.remove();
            }
        }

        if (cutsceneTipo != 0) {
            cutsceneTick++;
            if (cutsceneTick >= cutsceneDuracao) {
                finalizarCutscene();
            }
        }
    }

    // =========================================================================
    // CONTROLE CINEMATOGRÁFICO DE CÂMERA & SCREEN SHAKE NAS ULTIMATES
    // =========================================================================
    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (cutsceneTipo == 0 || mc.player == null || mc.level == null) return;
        float parcial = (float) event.getPartialTick();
        double tempo = cutsceneTick + parcial;
        RoteiroGojo.Quadro quadro = RoteiroGojo.amostrar(cutsceneTipo, tempo);
        Vec3 olho = mc.player.getEyePosition(parcial);
        Vec3 direita = frenteCamera.cross(new Vec3(0, 1, 0));
        Vec3 desejada = olho.add(direita.scale(quadro.x())).add(0, quadro.y(), 0)
                .add(frenteCamera.scale(quadro.z()));
        desejada = posicaoCameraInicial.lerp(desejada, RoteiroGojo.suave(tempo / 8));
        Vec3 alvo = olho.add(frenteCamera.scale(quadro.alvoFrente()));
        var colisao = mc.level.clip(new net.minecraft.world.level.ClipContext(olho, desejada,
                net.minecraft.world.level.ClipContext.Block.VISUAL,
                net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player));
        if (colisao.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            Vec3 trajeto = desejada.subtract(olho);
            double distancia = Math.max(.05, colisao.getLocation().distanceTo(olho) - .3);
            desejada = olho.add(trajeto.normalize().scale(distancia));
        }
        event.getCamera().setPosition(desejada);
        Vec3 direcao = alvo.subtract(desejada);
        double horizontal = Math.sqrt(direcao.x * direcao.x + direcao.z * direcao.z);
        double impacto = RoteiroGojo.impacto(cutsceneTipo, tempo);
        event.setYaw((float) Math.toDegrees(Math.atan2(-direcao.x, direcao.z))
                + (float) (Math.sin(tempo * 2.7) * impacto));
        event.setPitch((float) -Math.toDegrees(Math.atan2(direcao.y, horizontal))
                + (float) (Math.sin(tempo * 3.3) * impacto));
        event.setRoll((float) (Math.sin(tempo * 2.1) * impacto * .5));
    }

    @SubscribeEvent
    public static void onFovUpdate(ComputeFovModifierEvent event) {
        if (cutsceneTipo == 0) return;
        double tempo = cutsceneTick + Minecraft.getInstance().getFrameTime();
        event.setNewFovModifier((float) (event.getNewFovModifier()
                * RoteiroGojo.amostrar(cutsceneTipo, tempo).fov()));
    }

    // =========================================================================
    // RENDERIZAÇÃO DOS MODELOS 3D PROCEDURAIS NO MUNDO (ESFERAS, ANÉIS, FEIXES E DOMO)
    // =========================================================================
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        float partialTick = event.getPartialTick();

        // 1. Se o Infinito (C) estiver ativo, desenha anéis 3D translúcidos ao redor do Gojo
        if (ProgressoGojoCliente.infinito) {
            Player p = mc.player;
            double px = Mth.lerp(partialTick, p.xo, p.getX()) - camPos.x;
            double py = Mth.lerp(partialTick, p.yo, p.getY()) - camPos.y;
            double pz = Mth.lerp(partialTick, p.zo, p.getZ()) - camPos.z;

            poseStack.pushPose();
            poseStack.translate(px, py + 1.0, pz);
            float rot = (p.tickCount + partialTick) * 4.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rot));
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0f));
            RenderizadorModelos3D.desenharAnel3D(poseStack, 1.3f, 1.55f, 80, 220, 255, 95, 32);
            poseStack.popPose();
        }

        // 2. Renderiza cada Modelo 3D de Magia ativo no mundo
        for (Efeito3DAtivo ef : EFEITOS_3D) {
            float t = ef.tickAtual + partialTick;
            Player conjurador = mc.level.getPlayerByUUID(ef.playerId);

            // -----------------------------------------------------------------
            // MODELO 3D: TÉCNICA AZUL (AO) - Esfera 3D Azul + Disco Gravitacional
            // -----------------------------------------------------------------
            if (ef.tipo == 1) {
                Vec3 centro = conjurador != null
                        ? conjurador.getEyePosition(partialTick).add(conjurador.getViewVector(partialTick).scale(7.5))
                        : ef.origem.add(ef.direcao.scale(7.5));

                poseStack.pushPose();
                poseStack.translate(centro.x - camPos.x, centro.y - camPos.y, centro.z - camPos.z);

                float pulso = 1.0f + Mth.sin(t * 0.6f) * 0.15f;
                // Núcleo 3D Azul escuro/brilhante
                RenderizadorModelos3D.desenharEsfera3D(poseStack, 0.9f * pulso, 0, 180, 255, 220, 20);
                // Aura 3D Externa
                RenderizadorModelos3D.desenharEsfera3D(poseStack, 1.6f * pulso, 30, 100, 255, 90, 20);

                // Dois anéis 3D giratórios de colapso espacial
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(t * 14.0f));
                poseStack.mulPose(Axis.XP.rotationDegrees(35.0f));
                RenderizadorModelos3D.desenharAnel3D(poseStack, 1.8f, 3.4f, 0, 210, 255, 150, 32);
                poseStack.popPose();

                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(-t * 10.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(55.0f));
                RenderizadorModelos3D.desenharAnel3D(poseStack, 2.0f, 3.8f, 120, 230, 255, 110, 32);
                poseStack.popPose();

                poseStack.popPose();
            }

            // -----------------------------------------------------------------
            // MODELO 3D: TÉCNICA VERMELHO (AKA) - Esfera 3D Carmesim + Expansão
            // -----------------------------------------------------------------
            else if (ef.tipo == 2) {
                Vec3 olho = conjurador != null ? conjurador.getEyePosition(partialTick) : ef.origem;
                Vec3 dir = conjurador != null ? conjurador.getViewVector(partialTick) : ef.direcao;

                if (t <= 12) {
                    // Esfera 3D concentrando na ponta do dedo
                    Vec3 posDedo = olho.add(dir.scale(1.8));
                    float escala = (t / 12.0f) * 0.65f;
                    poseStack.pushPose();
                    poseStack.translate(posDedo.x - camPos.x, posDedo.y - camPos.y, posDedo.z - camPos.z);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, escala * 0.5f, 255, 230, 230, 240, 16);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, escala, 255, 20, 40, 180, 16);
                    poseStack.mulPose(Axis.YP.rotationDegrees(t * 25.0f));
                    RenderizadorModelos3D.desenharAnel3D(poseStack, escala * 1.1f, escala * 2.2f, 255, 50, 50, 160, 24);
                    poseStack.popPose();
                } else {
                    // Orbe 3D disparado em alta velocidade + Anel de Onda de Choque
                    float avanco = (t - 12.0f) * 2.2f;
                    Vec3 posOrbe = olho.add(dir.scale(1.8 + avanco));
                    poseStack.pushPose();
                    poseStack.translate(posOrbe.x - camPos.x, posOrbe.y - camPos.y, posOrbe.z - camPos.z);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 0.9f, 255, 255, 255, 240, 18);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 1.7f, 255, 15, 30, 160, 18);
                    poseStack.popPose();
                }
            }

            // -----------------------------------------------------------------
            // MODELO 3D + CUTSCENE: ULTIMATE VAZIO ROXO (MURASAKI)
            // -----------------------------------------------------------------
            else if (ef.tipo == 4) {
                Vec3 olho = (conjurador != null && t <= 30) ? conjurador.getEyePosition(partialTick) : ef.origem;
                Vec3 dir = (conjurador != null && t <= 30) ? conjurador.getViewVector(partialTick) : ef.direcao;
                Vec3 direita = dir.cross(new Vec3(0, 1, 0)).normalize();
                Vec3 centroFrente = olho.add(dir.scale(3.2));

                if (t <= 30) {
                    // FASE 1 DA CUTSCENE: Esfera 3D Azul e Esfera 3D Vermelha orbitando e colidindo!
                    float progresso = t / 30.0f;
                    double dist = 3.2 * (1.0 - progresso);

                    Vec3 posAzul = centroFrente.subtract(direita.scale(dist));
                    Vec3 posVermelho = centroFrente.add(direita.scale(dist));

                    // Renderiza Esfera 3D Azul
                    poseStack.pushPose();
                    poseStack.translate(posAzul.x - camPos.x, posAzul.y - camPos.y, posAzul.z - camPos.z);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 0.75f, 180, 240, 255, 240, 20);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 1.35f, 0, 150, 255, 150, 20);
                    poseStack.popPose();

                    // Renderiza Esfera 3D Vermelha
                    poseStack.pushPose();
                    poseStack.translate(posVermelho.x - camPos.x, posVermelho.y - camPos.y, posVermelho.z - camPos.z);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 0.75f, 255, 200, 200, 240, 20);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 1.35f, 255, 20, 40, 150, 20);
                    poseStack.popPose();

                    // Quando começam a se tocar (progresso > 0.65), nasce o núcleo 3D Roxo no meio!
                    if (progresso > 0.65f) {
                        float escalaRoxa = (progresso - 0.65f) / 0.35f * 2.2f;
                        poseStack.pushPose();
                        poseStack.translate(centroFrente.x - camPos.x, centroFrente.y - camPos.y, centroFrente.z - camPos.z);
                        RenderizadorModelos3D.desenharEsfera3D(poseStack, escalaRoxa * 0.6f, 255, 255, 255, 250, 24);
                        RenderizadorModelos3D.desenharEsfera3D(poseStack, escalaRoxa, 180, 20, 255, 190, 24);
                        poseStack.mulPose(Axis.YP.rotationDegrees(t * 20.0f));
                        RenderizadorModelos3D.desenharAnel3D(poseStack, escalaRoxa * 1.1f, escalaRoxa * 2.4f, 210, 80, 255, 180, 32);
                        poseStack.popPose();
                    }
                } else {
                    // FASE 2 DA CUTSCENE: Esfera 3D Gigante do Vazio Roxo avançando + Anéis de Distorção
                    float passo = t - 30.0f;
                    Vec3 posEsfera = centroFrente.add(dir.scale(passo * 1.6));

                    poseStack.pushPose();
                    poseStack.translate(posEsfera.x - camPos.x, posEsfera.y - camPos.y, posEsfera.z - camPos.z);

                    // Núcleo branco de singularidade + Esfera 3D Roxa Massiva
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 2.2f, 255, 240, 255, 245, 24);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 3.6f, 175, 10, 255, 175, 24);
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 4.4f, 90, 0, 180, 95, 24);

                    // Anéis 3D duplos girando em alta velocidade ao redor do Vazio Roxo
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.YP.rotationDegrees(t * 18.0f));
                    poseStack.mulPose(Axis.XP.rotationDegrees(45.0f));
                    RenderizadorModelos3D.desenharAnel3D(poseStack, 3.8f, 6.5f, 230, 120, 255, 180, 36);
                    poseStack.popPose();

                    poseStack.pushPose();
                    poseStack.mulPose(Axis.ZP.rotationDegrees(-t * 18.0f));
                    poseStack.mulPose(Axis.XP.rotationDegrees(-45.0f));
                    RenderizadorModelos3D.desenharAnel3D(poseStack, 4.0f, 7.0f, 140, 220, 255, 140, 36);
                    poseStack.popPose();

                    poseStack.popPose();
                }
            }

            // -----------------------------------------------------------------
            // MODELO 3D + CUTSCENE: EXPANSÃO DE DOMÍNIO - VAZIO INFINITO (R)
            // -----------------------------------------------------------------
            else if (ef.tipo == 5) {
                Vec3 centroDom = ef.origem;
                poseStack.pushPose();
                poseStack.translate(centroDom.x - camPos.x, centroDom.y - camPos.y, centroDom.z - camPos.z);

                // Domo 3D Cósmico se expandindo e trancando a área!
                float raioDomo = t < 35 ? (t / 35.0f) * 26.0f : 26.0f;
                RenderizadorModelos3D.desenharEsfera3D(poseStack, raioDomo, 5, 5, 18, 230, 32);

                // Buraco Negro 3D do Vazio Infinito pairando acima (exatamente como no vídeo 01:58!)
                if (t > 20) {
                    poseStack.pushPose();
                    poseStack.translate(0, 8.0, 0);
                    // Horizonte de eventos negro
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 3.5f, 0, 0, 0, 255, 28);
                    // Halo branco/azul brilhante ao redor do buraco negro
                    RenderizadorModelos3D.desenharEsfera3D(poseStack, 3.85f, 200, 240, 255, 130, 28);

                    // Disco de acreção cósmico girando
                    poseStack.mulPose(Axis.YP.rotationDegrees(t * 4.0f));
                    poseStack.mulPose(Axis.XP.rotationDegrees(20.0f));
                    RenderizadorModelos3D.desenharAnel3D(poseStack, 4.2f, 11.5f, 180, 235, 255, 190, 48);
                    poseStack.popPose();
                }

                poseStack.popPose();
            }
        }
    }
}
