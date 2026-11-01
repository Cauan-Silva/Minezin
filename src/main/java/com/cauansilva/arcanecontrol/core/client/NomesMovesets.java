package com.cauansilva.arcanecontrol.core.client;
import com.cauansilva.arcanecontrol.core.rpg.*;
public final class NomesMovesets {
    private static final CatalogoMovesetsRpg CATALOGO=carregar();
    private static CatalogoMovesetsRpg carregar(){try{return CatalogoMovesetsRpg.padrao();}catch(java.io.IOException e){throw new IllegalStateException("Catalogo RPG ausente",e);}}
    public static String nome(int kit,int slot){var m=CATALOGO.get(PersonagemRpg.porId(kit));return slot==4?com.cauansilva.arcanecontrol.core.poderes.PoderAnime.porId(kit).tecnicas[4]:m.transformada().get(slot).nome();}
}
