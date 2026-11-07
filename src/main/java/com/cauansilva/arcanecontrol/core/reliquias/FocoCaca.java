package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.*;
import java.util.*;

/**
 * Foco de Caca (sem Minecraft): o jogador escolhe um Tipo e/ou um Afixo para o proximo bau do Vazio. A primeira
 * reliquia sempre respeita o foco; as outras com {@link #CHANCE_TIPO}/{@link #CHANCE_AFIXO}. Custa Essencia por bau.
 * Tier e pity nao mudam: o foco direciona, nao aumenta raridade.
 */
public final class FocoCaca {
    public static final double CHANCE_TIPO=.5, CHANCE_AFIXO=.35;
    public static final int CUSTO_UM=3, CUSTO_DOIS=6;

    public static int custo(Tipo t,Afixo a){int n=(t!=null?1:0)+(a!=null?1:0);return n==0?0:n==1?CUSTO_UM:CUSTO_DOIS;}

    /** Reliquia com afixo garantido (se dado): ele vira o primeiro, os demais sorteados sem repetir. */
    public static Reliquia gerar(Random r,Tipo tipo,int tier,int kit,Afixo garantido){
        if(garantido==null)return Reliquias.gerar(r,tipo,tier,kit);
        var lista=new ArrayList<>(List.of(Afixo.values()));lista.remove(garantido);Collections.shuffle(lista,r);lista.add(0,garantido);
        var rol=new ArrayList<Rolagem>();for(int i=0;i<Reliquias.afixos(tier);i++)rol.add(Reliquias.rolar(r,lista.get(i),tier));
        return new Reliquia(tipo,tier,kit,List.copyOf(rol));
    }
    /** Bau com foco: mesmos tiers do bau normal (no maximo uma Mitica), tipo/afixo puxados para o foco. */
    public static List<Reliquia> bau(Random r,int kit,boolean forcaMitico,Tipo foco,Afixo afixo){
        var out=new ArrayList<Reliquia>();boolean mitico=false;
        for(int i=0;i<Reliquias.ITENS_BAU;i++){
            int t=Reliquias.tier(r,forcaMitico&&i==0);if(t==Reliquias.TIERS&&mitico)t=Reliquias.TIERS-1;mitico|=t==Reliquias.TIERS;
            var tipo=foco!=null&&(i==0||r.nextDouble()<CHANCE_TIPO)?foco:Tipo.values()[r.nextInt(Tipo.values().length)];
            var a=afixo!=null&&(i==0||r.nextDouble()<CHANCE_AFIXO)?afixo:null;
            out.add(gerar(r,tipo,t,kit,a));
        }
        return out;
    }
    public static Tipo tipo(String s){for(var t:Tipo.values())if(t.name().equalsIgnoreCase(s))return t;return null;}
    public static Afixo afixo(String s){for(var a:Afixo.values())if(a.name().equalsIgnoreCase(s))return a;return null;}
    private FocoCaca(){}
}
