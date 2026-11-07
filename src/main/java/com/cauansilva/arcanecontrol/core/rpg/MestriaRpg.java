package com.cauansilva.arcanecontrol.core.rpg;

/** Modelo de dominio independente do Minecraft: validacoes sempre executaveis no servidor. */
public final class MestriaRpg {
    public static final int NIVEL_MAXIMO=100,XP_POR_NIVEL=100,XP_MAXIMO=9900;
    private int xp,nos,mut;
    private boolean despertar;
    /** Ascensao: renascimentos deste kit e se o Despertar ja foi conquistado antes (volta ao recomprar o no). */
    private int asc;private boolean despertarMemoria;
    public static final int ASC_MAX=10;
    public MestriaRpg(){this(0,0,false);}
    public MestriaRpg(int xp,int nos,boolean despertar,int asc,boolean memoria){
        this(xp,nos,despertar);this.asc=Math.max(0,Math.min(ASC_MAX,asc));despertarMemoria=memoria;
        if(memoria&&tem(NoRpg.DESPERTAR))this.despertar=true;
    }
    public MestriaRpg(int xp,int nos,boolean despertar){
        this.xp=Math.max(0,Math.min(XP_MAXIMO,xp));
        // NBT invalido nao pode tornar um no dependente acessivel sem os anteriores.
        for(var no:NoRpg.values())if((nos&no.bit())!=0&&(this.nos&no.requisitos)==no.requisitos&&nivel()>=no.nivel&&pontos()>=no.custo)this.nos|=no.bit();
        this.despertar=despertar&&tem(NoRpg.DESPERTAR);
        // mutacoes vem nos bits altos de "nos"; so ficam as que o nivel e o no permitem
        for(int s=1;s<=3;s++)if(podeMutar(s))mut=MutacaoRpg.gravar(mut,s,MutacaoRpg.porId(MutacaoRpg.ler(nos,s)).ordinal());
    }
    public int xp(){return xp;}
    public int nivel(){return 1+xp/XP_POR_NIVEL;}
    public int nos(){return nos;}
    /** Nos + mutacoes empacotados (formato salvo no NBT e enviado ao cliente). */
    public int nosComMutacoes(){return nos|mut;}
    public boolean podeMutar(int slot){var no=MutacaoRpg.no(slot);return no!=null&&tem(no)&&nivel()>=MutacaoRpg.NIVEL[slot];}
    public MutacaoRpg mutacao(int slot){return MutacaoRpg.porId(MutacaoRpg.ler(mut,slot));}
    /** Troca a mutacao da tecnica (gratis); false se o no nao foi comprado ou falta nivel. */
    public boolean mutar(int slot,MutacaoRpg m){if(!podeMutar(slot)||m==null)return false;mut=MutacaoRpg.gravar(mut,slot,m.ordinal());return true;}
    public boolean tem(NoRpg n){return n!=null&&(nos&n.bit())!=0;}
    public int pontos(){int gastos=0;for(var n:NoRpg.values())if(tem(n))gastos+=n.custo;return Math.max(0,nivel()-1-gastos);}
    public int ganharXp(int quantidade){int antes=nivel();if(quantidade>0)xp=(int)Math.min(XP_MAXIMO,(long)xp+Math.round(quantidade*bonusXp()));return nivel()-antes;}
    public boolean comprar(NoRpg no){
        if(no==null||tem(no)||nivel()<no.nivel||pontos()<no.custo||(nos&no.requisitos)!=no.requisitos)return false;
        nos|=no.bit();if(no==NoRpg.DESPERTAR&&despertarMemoria)despertar=true;return true;
    }
    public boolean consumirAlma(PersonagemRpg ativo,PersonagemRpg alma){
        if(ativo==null||ativo!=alma||!tem(NoRpg.DESPERTAR)||despertar)return false;
        despertar=true;return true;
    }
    public boolean despertarLiberado(){return despertar;}
    public int ascensoes(){return asc;}
    public boolean memoriaDespertar(){return despertarMemoria;}
    public boolean podeAscender(){return nivel()>=NIVEL_MAXIMO&&asc<ASC_MAX;}
    /** Renasce: volta ao nivel 1 sem nos, guardando a ascensao e a memoria do Despertar. */
    public boolean ascender(){
        if(!podeAscender())return false;
        despertarMemoria|=despertar;asc++;xp=0;nos=0;mut=0;despertar=false;return true;
    }
    /** Bonus permanentes por ascensao. */
    public double bonusDano(){return asc*.04;}
    public double bonusVida(){return asc*3.0;}
    public double bonusXp(){return 1+asc*.10;}
    public boolean podeUsarSlot(int slot){return switch(slot){case 0->true;case 1->tem(NoRpg.TECNICA_X);case 2->tem(NoRpg.TECNICA_C);case 3->tem(NoRpg.TECNICA_V);case 4->despertar;default->false;};}
    public double vidaAdicional(){return (nivel()-1)*2.0+bonusVida();}
    public double multiplicadorFisico(){return 1+bonusDano()+(nivel()-1)*.0075+(tem(NoRpg.DANO_FISICO)?.10:0);}
    public double multiplicadorMagico(){return 1+bonusDano()+(nivel()-1)*.0075+(tem(NoRpg.DANO_MAGICO)?.10:0);}
    public double multiplicadorRecarga(){return (1-(nivel()-1)*.003)*(1-asc*.02);}
    public double multiplicadorRegeneracao(){return tem(NoRpg.REGENERACAO_MANA)?1.25:1;}
    public double velocidadeAdicional(){return tem(NoRpg.MOVIMENTO)?.10:0;}
    /** Perfil administrativo completo, sem alterar os requisitos da progressão survival. */
    public static MestriaRpg maximaParaTeste(){
        var m=new MestriaRpg();m.ganharXp(XP_MAXIMO);
        for(var no:NoRpg.values())if(!m.comprar(no))throw new IllegalStateException("Árvore incompatível com maestria máxima: "+no);
        m.consumirAlma(PersonagemRpg.GOJO,PersonagemRpg.GOJO);
        return m;
    }
    /** Migra nivel/pontos anteriores sem conceder um despertar que ainda exige boss. */
    public static MestriaRpg migrar(int xpAntigo,boolean x,boolean c,boolean v){
        var m=new MestriaRpg(xpAntigo,0,false);
        if(x){m.xp=Math.max(m.xp,100);m.comprar(NoRpg.TECNICA_X);}
        if(c){m.xp=Math.max(m.xp,400);m.comprar(NoRpg.TECNICA_X);m.comprar(NoRpg.TECNICA_C);}
        if(v){m.xp=Math.max(m.xp,900);m.comprar(NoRpg.TECNICA_X);m.comprar(NoRpg.TECNICA_C);m.comprar(NoRpg.TECNICA_V);}
        return m;
    }
}
