package com.cauansilva.arcanecontrol.core.vazio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Nemesis que evolui (sem Minecraft): cada wipe sobe o nivel do Nemesis daquele jogador/kit e ele aprende um traco,
 * de preferencia o que contra-ataca o estilo de luta usado contra ele. Vencer zera a memoria e paga a vinganca.
 * Tracos sao uma mascara de bits.
 */
public final class RegrasNemesis {
    public static final int NIVEL_MAX=5;
    public static final double VIDA_POR_NIVEL=.15, DANO_POR_NIVEL=.10, RESISTENCIA=.25, FURIA=.20, CACADOR=.15, REGEN=.01;
    public static final int REGEN_ATRASO=100;

    public enum Traco {
        COURACA("Couraça","-25% dano corpo a corpo"), ANTIMAGIA("Antimagia","-25% dano de magia e projéteis"),
        FURIA("Fúria","+20% de dano"), CACADOR("Caçador","+15% de velocidade"), IMPLACAVEL("Implacável","regenera se ficar 5 s sem apanhar");
        public final String nome,efeito;
        Traco(String n,String e){nome=n;efeito=e;}
        public int bit(){return 1<<ordinal();}
        public boolean em(int mascara){return (mascara&bit())!=0;}
    }
    public record Estado(int nivel,int tracos){
        public List<Traco> lista(){var l=new ArrayList<Traco>();for(var t:Traco.values())if(t.em(tracos))l.add(t);return l;}
    }

    /** Traco que o Nemesis aprende: contra o estilo dominante se ainda nao tem; senao um dos que faltam. Null se ja tem todos. */
    public static Traco aprender(int tracos,double corpo,double magia,Random rng){
        if(corpo+magia>0){var c=corpo>=magia?Traco.COURACA:Traco.ANTIMAGIA;if(!c.em(tracos))return c;}
        var faltam=new ArrayList<Traco>();for(var t:Traco.values())if(!t.em(tracos))faltam.add(t);
        return faltam.isEmpty()?null:faltam.get(rng.nextInt(faltam.size()));
    }
    /** Estado depois de um wipe. No nivel maximo ainda pode completar tracos. */
    public static Estado wipe(Estado e,double corpo,double magia,Random rng){
        var t=aprender(e.tracos(),corpo,magia,rng);
        return new Estado(Math.min(NIVEL_MAX,e.nivel()+1),t==null?e.tracos():e.tracos()|t.bit());
    }
    public static double vida(int nivel){return 1+VIDA_POR_NIVEL*nivel;}
    public static double dano(int nivel,int tracos){return (1+DANO_POR_NIVEL*nivel)*(Traco.FURIA.em(tracos)?1+FURIA:1);}
    /** Multiplicador do dano recebido pelo Nemesis. */
    public static double recebido(int tracos,boolean corpo){
        if(corpo&&Traco.COURACA.em(tracos))return 1-RESISTENCIA;
        if(!corpo&&Traco.ANTIMAGIA.em(tracos))return 1-RESISTENCIA;
        return 1;
    }
    /** Essencias extras por vencer um Nemesis evoluido. */
    public static int vinganca(int nivel){return nivel*2;}
    public static String titulo(String base,Estado e){
        if(e.nivel()<=0)return base;
        var nomes=e.lista().stream().map(t->t.nome).toList();
        return base+" Nv."+e.nivel()+(nomes.isEmpty()?"":" ("+String.join(", ",nomes)+")");
    }
    private RegrasNemesis(){}
}
