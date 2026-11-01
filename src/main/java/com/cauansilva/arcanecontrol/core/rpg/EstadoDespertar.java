package com.cauansilva.arcanecontrol.core.rpg;

/** Relogio absoluto: sair da forma nunca devolve a recarga de G. */
public final class EstadoDespertar {
    public final PersonagemRpg kit;
    private final long fim;
    private boolean exclusivo;
    public EstadoDespertar(PersonagemRpg kit,long inicio){
        if(kit==null||kit.duracaoSegundos<=0||inicio<0)throw new IllegalArgumentException("Forma invalida");
        this.kit=kit;fim=inicio+kit.duracaoSegundos*20L;
    }
    public int restante(long agora){return (int)Math.max(0,Math.min(kit.duracaoSegundos*20L,fim-agora));}
    public boolean ativa(long agora){return restante(agora)>0;}
    public boolean exclusivoUsado(){return exclusivo;}
    public boolean consumirExclusivo(long agora){if(!ativa(agora)||exclusivo)return false;exclusivo=true;return true;}
}
