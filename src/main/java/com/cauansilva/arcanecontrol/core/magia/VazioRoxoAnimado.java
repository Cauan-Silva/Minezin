package com.cauansilva.arcanecontrol.core.magia;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class VazioRoxoAnimado {
    public static void disparar(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        Vec3 inicio = entity.getEyePosition();
        Vec3 direcao = entity.getLookAngle();

        // Simula o trajeto animado da esfera de energia (Vazio Roxo)
        for (int i = 1; i <= 20; i++) {
            Vec3 ponto = inicio.add(direcao.scale(i * 1.5));
            
            // Cria partículas roxas densas e clarões ao longo do caminho
            serverLevel.sendParticles(ParticleTypes.WITCH, ponto.x, ponto.y, ponto.z, 15, 0.2, 0.2, 0.2, 0.05);
            serverLevel.sendParticles(ParticleTypes.FLASH, ponto.x, ponto.y, ponto.z, 2, 0.1, 0.1, 0.1, 0.0);
        }
    }
}