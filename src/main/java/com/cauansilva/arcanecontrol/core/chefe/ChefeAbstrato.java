package com.cauansilva.arcanecontrol.core.chefe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class ChefeAbstrato {
    protected final String idChefe;
    protected float vidaMaxima;
    protected float vidaAtual;

    public ChefeAbstrato(String idChefe, float vidaMaxima) {
        this.idChefe = idChefe;
        this.vidaMaxima = vidaMaxima;
        this.vidaAtual = vidaMaxima;
    }

    public void tickChefe(ServerLevel level, ServerPlayer alvoPrincipal) {
        verificarFasesDeBatalha();
    }

    protected abstract void verificarFasesDeBatalha();

    public String getIdChefe() {
        return idChefe;
    }

    public float getPorcentagemVida() {
        return (vidaAtual / vidaMaxima) * 100.0f;
    }
}
