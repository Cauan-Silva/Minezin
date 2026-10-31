package com.cauansilva.arcanecontrol.core.chefe.chefes;

import com.cauansilva.arcanecontrol.core.chefe.ChefeAbstrato;

public class CavaleiroDoVazioChefe extends ChefeAbstrato {
    public CavaleiroDoVazioChefe() { super("cavaleiro_do_vazio", 500.0f); }
    @Override protected void verificarFasesDeBatalha() {}
}
