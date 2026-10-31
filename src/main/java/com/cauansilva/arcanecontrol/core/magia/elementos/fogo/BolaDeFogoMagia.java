package com.cauansilva.arcanecontrol.core.magia.elementos.fogo;

import com.cauansilva.arcanecontrol.core.magia.MagiaAbstrata;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class BolaDeFogoMagia extends MagiaAbstrata {

    public BolaDeFogoMagia() {
        super("bola_de_fogo", 40);
    }

    @Override
    protected void executarLogicaFase(ServerPlayer jogador, Vec3 alvo) {
        if (tickAtual == 10) {
            Vec3 posAlvo = jogador.position().add(alvo.scale(5.0));
            jogador.serverLevel().sendParticles(
                ParticleTypes.FLAME,
                posAlvo.x, posAlvo.y, posAlvo.z, 20, 0.5, 0.5, 0.5, 0.1
            );
        }
    }
}
