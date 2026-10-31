package com.cauansilva.arcanecontrol.core.magia.elemento.elementos;

import com.cauansilva.arcanecontrol.core.magia.MagiaAbstrata;
import com.cauansilva.arcanecontrol.core.magia.elemento.Elemento;
import com.cauansilva.arcanecontrol.core.magia.elementos.fogo.BolaDeFogoMagia;

import java.util.List;

public class ElementoFogo implements Elemento {

    @Override
    public String getNome() {
        return "fogo";
    }

    @Override
    public int getCorHex() {
        return 0xFF4500;
    }

    @Override
    public List<MagiaAbstrata> getMagiasDisponiveis() {
        return List.of(
            new BolaDeFogoMagia()
        );
    }
}
