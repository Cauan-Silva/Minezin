package com.cauansilva.arcanecontrol.core.chefe.chefes;

import com.cauansilva.arcanecontrol.core.chefe.ChefeAbstrato;

public class CavaleiroDoVazioChefe extends ChefeAbstrato {

    public CavaleiroDoVazioChefe() {
        super("cavaleiro_do_vazio", 500.0f);
    }

    @Override
    protected void verificarFasesDeBatalha() {
        if (getPorcentagemVida() <= 50.0f) {
            // Lógica de transição de fase 2
        }
    }
}
