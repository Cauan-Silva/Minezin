package com.cauansilva.arcanecontrol.core.magia.elementos.gojo;

import com.cauansilva.arcanecontrol.core.magia.MagiaAbstrata;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class InfinitoMagia extends MagiaAbstrata {
    public InfinitoMagia() {
        super("gojo_infinito", 60); // 3 segundos de barreira
    }

    @Override
    protected void executarLogicaFase(ServerPlayer jogador, Vec3 alvo) {
        Vec3 pos = jogador.position();
        jogador.serverLevel().sendParticles(
            ParticleTypes.END_ROD,
            pos.x, pos.y + 1.0, pos.z, 5, 0.6, 0.6, 0.6, 0.05
        );

        // Afasta projéteis e entidades que tentem aproximar-se
        jogador.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Entity.class,
            new AABB(pos.subtract(3, 3, 3), pos.add(3, 3, 3))
        ).forEach(entidade -> {
            if (entidade != jogador) {
                Vec3 afastamento = entidade.position().subtract(pos).normalize().scale(0.5);
                entidade.setDeltaMovement(afastamento);
                entidade.hurtMarked = true;
            }
        });
    }
}
