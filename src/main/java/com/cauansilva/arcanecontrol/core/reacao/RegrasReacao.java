package com.cauansilva.arcanecontrol.core.reacao;

/**
 * Reacoes elementais (sem Minecraft): um golpe elemental deixa uma Aura no alvo; um segundo elemento diferente
 * consome a aura e dispara a reacao. Tempo em ticks.
 */
public final class RegrasReacao {
    public static final int AURA=160, IMBUIR=1200, CONGELA=60, FRAGIL=160;
    public static final double FRAGIL_MULT=1.25, SOBRECARGA_RAIO=3, ELETRO_RAIO=4, ELETRO_FRACAO=.4;
    public static final int ELETRO_MAX=5;

    public enum Elemento {
        FOGO("Fogo",0xFF6A1F), GELO("Gelo",0x8FE3FF), RAIO("Raio",0xC77DFF), AGUA("Água",0x2F7BFF);
        public final String nome; public final int cor;
        Elemento(String n,int c){nome=n;cor=c;}
        public static Elemento porId(int i){return i>=0&&i<values().length?values()[i]:null;}
    }
    public enum Reacao {
        DERRETER("Derreter",0xFFA040), VAPORIZAR("Vaporizar",0xC0D8FF), SOBRECARGA("Sobrecarga",0xFF4FA0),
        ELETROCUTAR("Eletrocutar",0x9F7BFF), CONGELAR("Congelar",0xBFF3FF), SUPERCONDUTOR("Supercondutor",0xB8A0FF);
        public final String nome; public final int cor;
        Reacao(String n,int c){nome=n;cor=c;}
    }
    private static boolean par(Elemento a,Elemento b,Elemento x,Elemento y){return a==x&&b==y||a==y&&b==x;}
    /** Reacao entre a aura presente e o elemento do golpe (null = nenhuma; o golpe so renova/troca a aura). */
    public static Reacao reagir(Elemento aura,Elemento golpe){
        if(aura==null||golpe==null||aura==golpe)return null;
        if(par(aura,golpe,Elemento.FOGO,Elemento.GELO))return Reacao.DERRETER;
        if(par(aura,golpe,Elemento.FOGO,Elemento.AGUA))return Reacao.VAPORIZAR;
        if(par(aura,golpe,Elemento.FOGO,Elemento.RAIO))return Reacao.SOBRECARGA;
        if(par(aura,golpe,Elemento.RAIO,Elemento.AGUA))return Reacao.ELETROCUTAR;
        if(par(aura,golpe,Elemento.GELO,Elemento.AGUA))return Reacao.CONGELAR;
        return Reacao.SUPERCONDUTOR; // raio + gelo
    }
    /** Multiplicador do proprio golpe: reacoes amplificadoras valem mais na ordem "forte". */
    public static double multiplicador(Reacao r,Elemento golpe){
        if(r==Reacao.DERRETER)return golpe==Elemento.FOGO?2:1.5;
        if(r==Reacao.VAPORIZAR)return golpe==Elemento.AGUA?2:1.5;
        return 1;
    }
    /** Dano em area da Sobrecarga. */
    public static double sobrecarga(double dano){return 4+Math.max(0,dano)*.5;}
    private RegrasReacao(){}
}
