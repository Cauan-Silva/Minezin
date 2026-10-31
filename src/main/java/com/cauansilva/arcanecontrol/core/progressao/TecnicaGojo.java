package com.cauansilva.arcanecontrol.core.progressao;

public enum TecnicaGojo {
    AZUL(1, "Azul", 1, 0, 200, 0),
    VERMELHO(2, "Vermelho", 2, 1, 300, 1),
    INFINITO(3, "Infinito", 3, 1, 100, 1),
    ROXO(4, "Vazio Roxo", 5, 2, 900, 3),
    DOMINIO(5, "Vazio Infinito", 8, 3, 1800, 12);

    public final int id;
    public final String nome;
    public final int nivel;
    public final int custo;
    public final int cooldown;
    public final int requisitos;

    TecnicaGojo(int id, String nome, int nivel, int custo, int cooldown, int requisitos) {
        this.id = id;
        this.nome = nome;
        this.nivel = nivel;
        this.custo = custo;
        this.cooldown = cooldown;
        this.requisitos = requisitos;
    }

    public int bit() { return 1 << (id - 1); }

    public static TecnicaGojo porId(int id) {
        return id >= 1 && id <= values().length ? values()[id - 1] : null;
    }
}
