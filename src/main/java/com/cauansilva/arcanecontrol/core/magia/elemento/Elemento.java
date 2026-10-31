package com.cauansilva.arcanecontrol.core.magia.elemento;

import com.cauansilva.arcanecontrol.core.magia.MagiaAbstrata;
import java.util.List;

public interface Elemento {
    String getNome();
    int getCorHex();
    List<MagiaAbstrata> getMagiasDisponiveis();
}
