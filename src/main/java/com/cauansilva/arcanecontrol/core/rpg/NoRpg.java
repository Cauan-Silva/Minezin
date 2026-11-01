package com.cauansilva.arcanecontrol.core.rpg;

/** Custos e niveis nao especificados no GDD ficam centralizados para balanceamento. */
public enum NoRpg {
    TECNICA_X(0,2,1,0),
    TECNICA_C(1,5,2,1),
    TECNICA_V(2,10,3,3),
    DANO_FISICO(3,5,2,0),
    DANO_MAGICO(4,5,2,0),
    REGENERACAO_MANA(5,10,3,0),
    MOVIMENTO(6,10,3,0),
    DESPERTAR(7,50,10,127);
    public final int indice,nivel,custo,requisitos;
    NoRpg(int indice,int nivel,int custo,int requisitos){this.indice=indice;this.nivel=nivel;this.custo=custo;this.requisitos=requisitos;}
    public int bit(){return 1<<indice;}
}
