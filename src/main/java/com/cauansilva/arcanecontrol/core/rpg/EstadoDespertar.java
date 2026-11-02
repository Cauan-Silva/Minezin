package com.cauansilva.arcanecontrol.core.rpg;

/** Relogio absoluto: sair da forma nunca devolve a recarga de G. */
public final class EstadoDespertar {
    public final PersonagemRpg kit;
    private long fim,inicioContagem;
    private boolean exclusivo;
    public EstadoDespertar(PersonagemRpg kit,long inicio){
        if(kit==null||(kit.duracaoSegundos<=0&&kit!=PersonagemRpg.ICHIGO)||inicio<0)throw new IllegalArgumentException("Forma invalida");
        this.kit=kit;inicioContagem=inicio+preparacao(kit);fim=kit==PersonagemRpg.ICHIGO?Long.MAX_VALUE:inicioContagem+kit.duracaoSegundos*20L;
    }
    public static int preparacao(PersonagemRpg kit){return kit==PersonagemRpg.SHADOW?com.cauansilva.arcanecontrol.core.vfx.RelogioAtomic.FIM:0;}
    public static int apresentacaoInicial(PersonagemRpg kit){return preparacao(kit)+kit.duracaoSegundos*20;}
    /** Includes the unconsumed introduction so clients can interpolate without a countdown/reset flicker. */
    public int prazoRestante(long agora){return kit==PersonagemRpg.ICHIGO?1200:(int)Math.max(0,Math.min(Integer.MAX_VALUE,fim-agora));}
    public static int interpolarPrazo(int kit,int prazo,long decorrido){
        int valor=(int)Math.max(0,prazo-Math.max(0,decorrido));return kit==PersonagemRpg.SHADOW.id?Math.min(PersonagemRpg.SHADOW.duracaoSegundos*20,valor):valor;
    }
    public boolean anteciparContagem(long agora){
        if(kit!=PersonagemRpg.SHADOW||agora<0||agora>=inicioContagem)return false;
        inicioContagem=agora;fim=agora+kit.duracaoSegundos*20L;return true;
    }
    public int restante(long agora){return (int)Math.max(0,Math.min(kit==PersonagemRpg.ICHIGO?1200:kit.duracaoSegundos*20L,fim-agora));}
    public boolean ativa(long agora){return restante(agora)>0;}
    public boolean exclusivoUsado(){return exclusivo;}
    public boolean consumirExclusivo(long agora){if(!ativa(agora)||exclusivo)return false;exclusivo=true;return true;}
}
