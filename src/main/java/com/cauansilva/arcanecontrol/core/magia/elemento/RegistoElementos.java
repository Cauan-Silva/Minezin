package com.cauansilva.arcanecontrol.core.magia.elemento;

import com.cauansilva.arcanecontrol.core.magia.elemento.elementos.ElementoGojo;
import java.util.HashMap;
import java.util.Map;

public class RegistoElementos {
    private static final Map<String, Elemento> elementos = new HashMap<>();

    public static void inicializar() {
        registar(new ElementoGojo());
    }

    private static void registar(Elemento elemento) {
        elementos.put(elemento.getNome().toLowerCase(), elemento);
    }

    public static Elemento obter(String nome) {
        return elementos.get(nome.toLowerCase());
    }
}
