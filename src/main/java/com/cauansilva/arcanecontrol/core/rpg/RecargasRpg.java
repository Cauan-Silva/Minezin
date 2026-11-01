package com.cauansilva.arcanecontrol.core.rpg;
import java.util.Arrays;

/** Recargas de Z/X/C/V/G separadas por kit; troca de forma nao reinicia G. */
public final class RecargasRpg {
    private final long[] prazos;
    private final int[] duracoes;
    public RecargasRpg(){this(new long[150],new int[150]);}
    public RecargasRpg(long[] prazos,int[] duracoes){
        this.prazos=new long[150];this.duracoes=new int[150];
        if(prazos.length==75){for(int k=0;k<15;k++)for(int s=0;s<5;s++){
            this.prazos[k*10+s]=prazos[k*5+s];if(k*5+s<duracoes.length)this.duracoes[k*10+s]=duracoes[k*5+s];
        }}else{System.arraycopy(prazos,0,this.prazos,0,Math.min(150,prazos.length));System.arraycopy(duracoes,0,this.duracoes,0,Math.min(150,duracoes.length));}
        for(int i=0;i<150;i++){this.prazos[i]=Math.max(0,this.prazos[i]);this.duracoes[i]=Math.max(0,Math.min(12000,this.duracoes[i]));}
    }
    private static int indice(PersonagemRpg p,int slot,boolean forma){if(p==null||slot<0||slot>4)throw new IllegalArgumentException("Kit/slot invalido");return (p.id-1)*10+(forma&&slot<4?5:0)+slot;}
    public int restante(PersonagemRpg p,int slot,long agora){return restante(p,slot,agora,false);}
    public int duracao(PersonagemRpg p,int slot,boolean forma){return duracoes[indice(p,slot,forma)];}
    public int restante(PersonagemRpg p,int slot,long agora,boolean forma){long fim=prazos[indice(p,slot,forma)];return (int)Math.max(0,Math.min(12000,fim-Math.max(0,agora)));}
    public boolean iniciar(PersonagemRpg p,int slot,long agora,int baseTicks,double multiplicador){
        return iniciar(p,slot,agora,baseTicks,multiplicador,false);
    }
    public boolean iniciar(PersonagemRpg p,int slot,long agora,int baseTicks,double multiplicador,boolean forma){
        int i=indice(p,slot,forma);if(agora<0||baseTicks<0||!Double.isFinite(multiplicador)||multiplicador<0||restante(p,slot,agora,forma)>0)return false;
        int duracao=(int)Math.min(12000,Math.ceil(baseTicks*multiplicador));
        prazos[i]=agora>Long.MAX_VALUE-duracao?Long.MAX_VALUE:agora+duracao;duracoes[i]=duracao;return true;
    }
    public double fracao(PersonagemRpg p,int slot,long agora){int i=indice(p,slot,false);return duracoes[i]==0?0:Math.min(1,restante(p,slot,agora)/(double)duracoes[i]);}
    public long[] prazos(){return prazos.clone();}
    public int[] duracoes(){return duracoes.clone();}
}
