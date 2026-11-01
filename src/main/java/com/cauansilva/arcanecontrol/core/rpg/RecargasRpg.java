package com.cauansilva.arcanecontrol.core.rpg;
import java.util.Arrays;

/** Recargas de Z/X/C/V/G separadas por kit; troca de forma nao reinicia G. */
public final class RecargasRpg {
    private final long[] prazos;
    private final int[] duracoes;
    public RecargasRpg(){this(new long[75],new int[75]);}
    public RecargasRpg(long[] prazos,int[] duracoes){
        this.prazos=Arrays.copyOf(prazos,75);this.duracoes=Arrays.copyOf(duracoes,75);
        for(int i=0;i<75;i++){this.prazos[i]=Math.max(0,this.prazos[i]);this.duracoes[i]=Math.max(0,Math.min(12000,this.duracoes[i]));}
    }
    private static int indice(PersonagemRpg p,int slot){if(p==null||slot<0||slot>4)throw new IllegalArgumentException("Kit/slot invalido");return (p.id-1)*5+slot;}
    public int restante(PersonagemRpg p,int slot,long agora){long fim=prazos[indice(p,slot)];return (int)Math.max(0,Math.min(12000,fim-Math.max(0,agora)));}
    public boolean iniciar(PersonagemRpg p,int slot,long agora,int baseTicks,double multiplicador){
        int i=indice(p,slot);if(agora<0||baseTicks<0||!Double.isFinite(multiplicador)||multiplicador<0||restante(p,slot,agora)>0)return false;
        int duracao=(int)Math.min(12000,Math.ceil(baseTicks*multiplicador));
        prazos[i]=agora>Long.MAX_VALUE-duracao?Long.MAX_VALUE:agora+duracao;duracoes[i]=duracao;return true;
    }
    public double fracao(PersonagemRpg p,int slot,long agora){int i=indice(p,slot);return duracoes[i]==0?0:Math.min(1,restante(p,slot,agora)/(double)duracoes[i]);}
    public long[] prazos(){return prazos.clone();}
    public int[] duracoes(){return duracoes.clone();}
}
