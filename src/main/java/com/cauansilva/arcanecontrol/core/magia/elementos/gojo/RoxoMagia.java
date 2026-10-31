package com.cauansilva.arcanecontrol.core.magia.elementos.gojo;

import com.cauansilva.arcanecontrol.core.magia.MagiaAbstrata;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RoxoMagia extends MagiaAbstrata {
    public RoxoMagia() {
        super("gojo_roxo", 80); // 4 segundos de canalização e impacto cinematográfico
    }

    @Override
    protected void executarLogicaFase(ServerPlayer jogador, Vec3 alvo) {
        if (tickAtual < 40) {
            // Fase de Canalização: Acumulação de Energia Cinematográfica
            Vec3 maos = jogador.position().add(0, 1.2, 0).add(jogador.getLookAngle().scale(1.5));
            jogador.serverLevel().sendParticles(
                ParticleTypes.WITCH,
                maos.x, maos.y, maos.z, 25, 0.3, 0.3, 0.3, 0.1
            );
        } else {
            // Fase de Disparo do Vazio Roxo
            Vec3 posImpacto = jogador.position().add(jogador.getLookAngle().scale((tickAtual - 40) * 1.5));
            
            jogador.serverLevel().sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                posImpacto.x, posImpacto.y, posImpacto.z, 2, 1.0, 1.0, 1.0, 0.2
            );

            // Desintegração em área
            jogador.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                new AABB(posImpacto.subtract(5, 5, 5), posImpacto.add(5, 5, 5))
            ).forEach(entidade -> {
                if (entidade != jogador) {
                    entidade.hurt(jogador.damageSources().magic(), 50.0f); // Dano massivo mortal
                }
            });
        }
    }
}
