package com.cauansilva.arcanecontrol.core.magia;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public abstract class MagiaAbstrata {
    protected final String id;
    protected final int duracaoMaximaTicks;
    protected int tickAtual;
    protected FaseMagia faseAtual;

    public enum FaseMagia {
        INICIALIZACAO,
        CANALIZACAO,
        EXECUCAO,
        FINALIZACAO,
        CONCLUIDA
    }

    public MagiaAbstrata(String id, int duracaoMaximaTicks) {
        this.id = id;
        this.duracaoMaximaTicks = duracaoMaximaTicks;
        this.tickAtual = 0;
        this.faseAtual = FaseMagia.INICIALIZACAO;
    }

    public void tick(ServerPlayer jogador, Vec3 alvo) {
        if (faseAtual == FaseMagia.CONCLUIDA) return;

        tickAtual++;
        executarLogicaFase(jogador, alvo);

        if (tickAtual >= duracaoMaximaTicks) {
            this.faseAtual = FaseMagia.CONCLUIDA;
        }
    }

    protected abstract void executarLogicaFase(ServerPlayer jogador, Vec3 alvo);

    public String getId() {
        return id;
    }

    public boolean estaConcluida() {
        return faseAtual == FaseMagia.CONCLUIDA;
    }
}
