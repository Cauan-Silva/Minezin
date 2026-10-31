package com.cauansilva.arcanecontrol.core.magia.elementos.gojo;

import com.cauansilva.arcanecontrol.core.magia.MagiaAbstrata;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class VermelhoMagia extends MagiaAbstrata {
    public VermelhoMagia() {
        super("gojo_vermelho", 20);
    }

    @Override
    protected void executarLogicaFase(ServerPlayer jogador, Vec3 alvo) {
        Vec3 pontoExplosao = jogador.position().add(alvo.scale(3.0));

        jogador.serverLevel().sendParticles(
            ParticleTypes.EXPLOSION_EMITTER,
            pontoExplosao.x, pontoExplosao.y, pontoExplosao.z, 3, 0.2, 0.2, 0.2, 0.1
        );

        jogador.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
            new AABB(pontoExplosao.subtract(3, 3, 3), pontoExplosao.add(3, 3, 3))
        ).forEach(entidade -> {
            if (entidade != jogador) {
                Vec3 direcaoRepulsao = entidade.position().subtract(pontoExplosao).normalize().scale(1.2).add(0, 0.4, 0);
                entidade.setDeltaMovement(direcaoRepulsao);
                entidade.hurt(jogador.damageSources().magic(), 12.0f);
                entidade.hurtMarked = true;
            }
        });
    }
}
