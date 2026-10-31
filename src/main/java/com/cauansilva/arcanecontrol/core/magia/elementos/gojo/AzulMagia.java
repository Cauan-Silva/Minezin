package com.cauansilva.arcanecontrol.core.magia.elementos.gojo;

import com.cauansilva.arcanecontrol.core.magia.MagiaAbstrata;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AzulMagia extends MagiaAbstrata {
    public AzulMagia() {
        super("gojo_azul", 30);
    }

    @Override
    protected void executarLogicaFase(ServerPlayer jogador, Vec3 alvo) {
        Vec3 centroVacuo = jogador.position().add(alvo.scale(4.0));
        
        jogador.serverLevel().sendParticles(
            ParticleTypes.PORTAL,
            centroVacuo.x, centroVacuo.y, centroVacuo.z, 15, 0.5, 0.5, 0.5, 0.2
        );
        
        jogador.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
            new AABB(centroVacuo.subtract(4, 4, 4), centroVacuo.add(4, 4, 4))
        ).forEach(entidade -> {
            if (entidade != jogador) {
                Vec3 direcaoPuxao = centroVacuo.subtract(entidade.position()).normalize().scale(0.4);
                entidade.setDeltaMovement(direcaoPuxao);
                entidade.hurtMarked = true;
            }
        });
    }
}
