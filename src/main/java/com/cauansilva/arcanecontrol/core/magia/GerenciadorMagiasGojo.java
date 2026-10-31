package com.cauansilva.arcanecontrol.core.magia;

import com.cauansilva.arcanecontrol.core.rede.PacoteSincronizarCutscene3D;
import com.cauansilva.arcanecontrol.core.rede.RegistoRede;
import com.cauansilva.arcanecontrol.core.progressao.ProgressaoGojo;
import com.cauansilva.arcanecontrol.core.progressao.TecnicaGojo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.*;

public class GerenciadorMagiasGojo {

    private static final Set<UUID> INFINITO_ATIVO = new HashSet<>();
    private static final List<AnimacaoAtiva> ANIMACOES = new ArrayList<>();

    private static final DustParticleOptions PART_AZUL = new DustParticleOptions(new Vector3f(0.0f, 0.75f, 1.0f), 2.5f);
    private static final DustParticleOptions PART_VERMELHO = new DustParticleOptions(new Vector3f(1.0f, 0.1f, 0.15f), 2.5f);
    private static final DustParticleOptions PART_ROXO = new DustParticleOptions(new Vector3f(0.75f, 0.1f, 1.0f), 3.5f);
    private static final DustParticleOptions PART_BRANCO = new DustParticleOptions(new Vector3f(0.95f, 0.95f, 1.0f), 2.0f);

    public static void executarMagia(ServerPlayer player, int idMagia) {
        TecnicaGojo tecnica = TecnicaGojo.porId(idMagia);
        if (tecnica == null || !ProgressaoGojo.autorizar(player, tecnica,
                idMagia == 3 && infinitoAtivo(player))) return;
        ServerLevel level = player.serverLevel();
        Vec3 olho = player.getEyePosition();
        Vec3 dir = player.getLookAngle();

        // Envia pacote para todos os clientes ao redor renderizarem os Modelos 3D e Cutscenes!
        RegistoRede.ENVIAR.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new PacoteSincronizarCutscene3D(idMagia, player.getUUID(), olho.x, olho.y, olho.z, dir.x, dir.y, dir.z)
        );

        switch (idMagia) {
            case 1 -> iniciarAzul(player, level);
            case 2 -> iniciarVermelho(player, level);
            case 3 -> alternarInfinito(player, level);
            case 4 -> iniciarVazioRoxo(player, level);
            case 5 -> iniciarExpansaoDominio(player, level);
        }
        ProgressaoGojo.sincronizar(player);
    }

    public static boolean infinitoAtivo(ServerPlayer player) {
        return INFINITO_ATIVO.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void aoSair(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        INFINITO_ATIVO.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void aoMorrer(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) INFINITO_ATIVO.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void aoParar(net.minecraftforge.event.server.ServerStoppedEvent event) {
        INFINITO_ATIVO.clear();
        ANIMACOES.clear();
    }

    // =========================================================================
    // 1. TÉCNICA AZUL (AO) - Esfera 3D + Vórtice de Atração (60 ticks)
    // =========================================================================
    private static void iniciarAzul(ServerPlayer player, ServerLevel level) {
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 1.6f);

        ANIMACOES.add(new AnimacaoAtiva(60) {
            @Override
            void tick(int tickAtual) {
                if (!player.isAlive() || player.hasDisconnected() || player.serverLevel() != level) return;
                Vec3 centro = player.getEyePosition().add(player.getLookAngle().scale(7.5));

                level.sendParticles(PART_AZUL, centro.x, centro.y, centro.z, 8, 0.4, 0.4, 0.4, 0.02);

                AABB area = new AABB(centro, centro).inflate(14.0);
                List<Entity> entidades = level.getEntities(player, area, e -> e instanceof LivingEntity || e instanceof Projectile);
                for (Entity alvo : entidades) {
                    Vec3 direcaoPuxao = centro.subtract(alvo.position().add(0, alvo.getBbHeight() * 0.5, 0));
                    double distancia = Math.max(0.5, direcaoPuxao.length());
                    Vec3 velocidade = direcaoPuxao.normalize().scale(Math.min(1.2, 3.2 / distancia));
                    alvo.setDeltaMovement(velocidade);
                    alvo.hurtMarked = true;

                    if (distancia < 2.8 && alvo instanceof LivingEntity living && tickAtual % 5 == 0) {
                        living.hurt(level.damageSources().indirectMagic(player, player), 6.0f);
                        level.sendParticles(ParticleTypes.CRIT, alvo.getX(), alvo.getY() + 1, alvo.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
                    }
                }
            }
        });
    }

    // =========================================================================
    // 2. TÉCNICA VERMELHO (AKA) - Esfera 3D + Onda de Choque Repulsora (28 ticks)
    // =========================================================================
    private static void iniciarVermelho(ServerPlayer player, ServerLevel level) {
        level.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 1.5f, 1.9f);

        ANIMACOES.add(new AnimacaoAtiva(28) {
            @Override
            void tick(int tickAtual) {
                if (!player.isAlive() || player.hasDisconnected() || player.serverLevel() != level) return;
                Vec3 olho = player.getEyePosition();
                Vec3 dir = player.getLookAngle();
                Vec3 pontoDedo = olho.add(dir.scale(1.8));

                if (tickAtual == 13) {
                    level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.7f);
                    level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.8f, 1.2f);
                    level.sendParticles(ParticleTypes.FLASH, pontoDedo.x, pontoDedo.y, pontoDedo.z, 2, 0, 0, 0, 0);

                    for (int dist = 2; dist <= 22; dist += 2) {
                        Vec3 posOnda = olho.add(dir.scale(dist));
                        level.sendParticles(PART_VERMELHO, posOnda.x, posOnda.y, posOnda.z, 15, 0.6, 0.6, 0.6, 0.1);

                        AABB areaImpacto = new AABB(posOnda, posOnda).inflate(4.5);
                        for (Entity alvo : level.getEntities(player, areaImpacto)) {
                            Vec3 empurrao = dir.scale(4.2).add(0, 0.9, 0);
                            alvo.setDeltaMovement(empurrao);
                            alvo.hurtMarked = true;
                            if (alvo instanceof LivingEntity living) {
                                living.hurt(level.damageSources().playerAttack(player), 18.0f);
                            }
                        }
                    }
                }
            }
        });
    }

    // =========================================================================
    // 3. INFINITO (MUKAGEN)
    // =========================================================================
    private static void alternarInfinito(ServerPlayer player, ServerLevel level) {
        UUID id = player.getUUID();
        if (INFINITO_ATIVO.contains(id)) {
            INFINITO_ATIVO.remove(id);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
        } else {
            INFINITO_ATIVO.add(id);
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.5f, 1.8f);
        }
    }

    // =========================================================================
    // 4. ULTIMATE 1: VAZIO ROXO (MURASAKI) - Cutscene + Fusão 3D + Apagamento!
    // =========================================================================
    private static void iniciarVazioRoxo(ServerPlayer player, ServerLevel level) {
        level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 2.0f, 1.4f);

        // Levitação cinemática durante a Cutscene de fusão do Azul + Vermelho!
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 2, false, false));

        ANIMACOES.add(new AnimacaoAtiva(85) {
            Vec3 origemDisparo;
            Vec3 direcaoDisparo;

            @Override
            void tick(int tickAtual) {
                if (!player.isAlive() || player.hasDisconnected() || player.serverLevel() != level) return;
                Vec3 olho = player.getEyePosition();
                Vec3 dir = player.getLookAngle();

                if (tickAtual <= 30) {
                    // Mantém o jogador flutuando na pose da cutscene
                    player.setDeltaMovement(0, 0.03, 0);
                    player.hurtMarked = true;

                    if (tickAtual == 30) {
                        origemDisparo = olho.add(dir.scale(3.2));
                        direcaoDisparo = dir;
                        RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                                new PacoteSincronizarCutscene3D(6, player.getUUID(),
                                        olho.x, olho.y, olho.z, dir.x, dir.y, dir.z));
                        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0f, 0.5f);
                        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0f, 0.5f);
                        level.sendParticles(ParticleTypes.FLASH, origemDisparo.x, origemDisparo.y, origemDisparo.z, 5, 0.2, 0.2, 0.2, 0);
                    }
                } else {
                    int passo = tickAtual - 30;
                    Vec3 posEsfera = origemDisparo.add(direcaoDisparo.scale(passo * 1.6));

                    level.sendParticles(PART_ROXO, posEsfera.x, posEsfera.y, posEsfera.z, 25, 1.6, 1.6, 1.6, 0.05);
                    level.sendParticles(ParticleTypes.SONIC_BOOM, posEsfera.x, posEsfera.y, posEsfera.z, 1, 0, 0, 0, 0);

                    // Apaga blocos na esfera de raio 4 para abrir a cratera perfeita!
                    int raioBlocos = 4;
                    BlockPos centroPos = BlockPos.containing(posEsfera);
                    for (int x = -raioBlocos; x <= raioBlocos; x++) {
                        for (int y = -raioBlocos; y <= raioBlocos; y++) {
                            for (int z = -raioBlocos; z <= raioBlocos; z++) {
                                if (x * x + y * y + z * z <= raioBlocos * raioBlocos) {
                                    BlockPos bp = centroPos.offset(x, y, z);
                                    if (!level.getBlockState(bp).isAir() && level.getBlockState(bp).getDestroySpeed(level, bp) >= 0) {
                                        level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
                                    }
                                }
                            }
                        }
                    }

                    AABB areaRoxo = new AABB(posEsfera, posEsfera).inflate(5.0);
                    for (Entity alvo : level.getEntities(player, areaRoxo)) {
                        if (alvo instanceof LivingEntity living) {
                            living.hurt(new net.minecraft.world.damagesource.DamageSource(
                                    level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                                            .getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD), player), 60.0f);
                        } else {
                            alvo.discard();
                        }
                    }
                }
            }
        });
    }

    // =========================================================================
    // 5. ULTIMATE 2: EXPANSÃO DE DOMÍNIO - VAZIO INFINITO (MURYŌKŪSHO) (Tecla R)
    // =========================================================================
    private static void iniciarExpansaoDominio(ServerPlayer player, ServerLevel level) {
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 3.0f, 0.6f);
        level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 2.5f, 0.8f);
        Vec3 centroDominio = player.position();

        ANIMACOES.add(new AnimacaoAtiva(160) {
            @Override
            void tick(int tickAtual) {
                if (!player.isAlive() || player.hasDisconnected() || player.serverLevel() != level) return;

                // Partículas cósmicas dentro do Domo 3D
                if (tickAtual % 2 == 0) {
                    level.sendParticles(ParticleTypes.END_ROD,
                            centroDominio.x + (Math.random() - 0.5) * 36,
                            centroDominio.y + Math.random() * 18,
                            centroDominio.z + (Math.random() - 0.5) * 36,
                            8, 0.5, 0.5, 0.5, 0.01);
                }

                // Todos os inimigos dentro do Domo de 26 blocos têm a mente inundada por informação infinita (Congelamento Total!)
                AABB areaDominio = new AABB(centroDominio, centroDominio).inflate(26.0);
                for (Entity alvo : level.getEntities(player, areaDominio)) {
                    if (alvo instanceof LivingEntity living) {
                        living.setDeltaMovement(Vec3.ZERO);
                        living.hurtMarked = true;
                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 255, false, false));
                        living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 1, false, false));
                        living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 1, false, false));

                        if (tickAtual > 35 && tickAtual % 10 == 0) {
                            living.hurt(level.damageSources().indirectMagic(player, player), 8.0f);
                            level.sendParticles(ParticleTypes.ENCHANT, living.getX(), living.getY() + 1.5, living.getZ(), 15, 0.3, 0.3, 0.3, 0.2);
                        }
                    } else if (alvo instanceof Projectile proj) {
                        proj.setDeltaMovement(Vec3.ZERO);
                        proj.setNoGravity(true);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<AnimacaoAtiva> it = ANIMACOES.iterator();
        while (it.hasNext()) {
            AnimacaoAtiva anim = it.next();
            anim.tickAtual++;
            anim.tick(anim.tickAtual);
            if (anim.tickAtual >= anim.duracaoMax) {
                it.remove();
            }
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                if (INFINITO_ATIVO.contains(player.getUUID())) {
                    aplicarBarreiraInfinito(player, level);
                }
            }
        }
    }

    private static void aplicarBarreiraInfinito(ServerPlayer player, ServerLevel level) {
        if (player.tickCount % 4 == 0) {
            for (int i = 0; i < 6; i++) {
                double ang = (player.tickCount * 0.2) + (i * Math.PI / 3);
                level.sendParticles(PART_BRANCO,
                        player.getX() + Math.cos(ang) * 1.6,
                        player.getY() + 0.2,
                        player.getZ() + Math.sin(ang) * 1.6,
                        1, 0, 0, 0, 0);
            }
        }

        AABB bolhaInfinito = player.getBoundingBox().inflate(3.5);
        for (Entity entidade : level.getEntities(player, bolhaInfinito)) {
            Vec3 diff = entidade.position().subtract(player.position());
            double dist = Math.max(0.1, diff.length());

            if (entidade instanceof Projectile proj) {
                proj.setDeltaMovement(Vec3.ZERO);
                proj.setNoGravity(true);
                proj.hurtMarked = true;
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, proj.getX(), proj.getY(), proj.getZ(), 2, 0.05, 0.05, 0.05, 0);
            } else if (entidade instanceof LivingEntity) {
                Vec3 repulsaoSuave = diff.normalize().scale(0.25 / dist);
                entidade.setDeltaMovement(repulsaoSuave);
                entidade.hurtMarked = true;
            }
        }
    }

    private static abstract class AnimacaoAtiva {
        final int duracaoMax;
        int tickAtual = 0;

        AnimacaoAtiva(int duracaoMax) {
            this.duracaoMax = duracaoMax;
        }

        abstract void tick(int tickAtual);
    }
}
