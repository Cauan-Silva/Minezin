package com.cauansilva.arcanecontrol.core.magia.elemento.elementos;

import com.cauansilva.arcanecontrol.core.magia.MagiaAbstrata;
import com.cauansilva.arcanecontrol.core.magia.elemento.Elemento;
import com.cauansilva.arcanecontrol.core.magia.elementos.gojo.AzulMagia;
import com.cauansilva.arcanecontrol.core.magia.elementos.gojo.VermelhoMagia;
import com.cauansilva.arcanecontrol.core.magia.elementos.gojo.InfinitoMagia;
import com.cauansilva.arcanecontrol.core.magia.elementos.gojo.RoxoMagia;

import java.util.List;

public class ElementoGojo implements Elemento {
    @Override
    public String getNome() { return "gojo"; }
    @Override
    public int getCorHex() { return 0x9400D3; }
    @Override
    public List<MagiaAbstrata> getMagiasDisponiveis() {
        return List.of(new AzulMagia(), new VermelhoMagia(), new InfinitoMagia(), new RoxoMagia());
    }
}
