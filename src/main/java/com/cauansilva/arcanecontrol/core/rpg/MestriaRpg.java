package com.cauansilva.arcanecontrol.core.rpg;

/** Modelo de dominio independente do Minecraft: validacoes sempre executaveis no servidor. */
public final class MestriaRpg {
    public static final int NIVEL_MAXIMO=100,XP_POR_NIVEL=100,XP_MAXIMO=9900;
    private int xp,nos;
    private boolean despertar;
    public MestriaRpg(){this(0,0,false);}
    public MestriaRpg(int xp,int nos,boolean despertar){
        this.xp=Math.max(0,Math.min(XP_MAXIMO,xp));
        // NBT invalido nao pode tornar um no dependente acessivel sem os anteriores.
        for(var no:NoRpg.values())if((nos&no.bit())!=0&&(this.nos&no.requisitos)==no.requisitos&&nivel()>=no.nivel&&pontos()>=no.custo)this.nos|=no.bit();
        this.despertar=despertar&&tem(NoRpg.DESPERTAR);
    }
    public int xp(){return xp;}
    public int nivel(){return 1+xp/XP_POR_NIVEL;}
    public int nos(){return nos;}
    public boolean tem(NoRpg n){return n!=null&&(nos&n.bit())!=0;}
    public int pontos(){int gastos=0;for(var n:NoRpg.values())if(tem(n))gastos+=n.custo;return Math.max(0,nivel()-1-gastos);}
    public int ganharXp(int quantidade){int antes=nivel();if(quantidade>0)xp=(int)Math.min(XP_MAXIMO,(long)xp+quantidade);return nivel()-antes;}
    public boolean comprar(NoRpg no){
        if(no==null||tem(no)||nivel()<no.nivel||pontos()<no.custo||(nos&no.requisitos)!=no.requisitos)return false;
        nos|=no.bit();return true;
    }
    public boolean consumirAlma(PersonagemRpg ativo,PersonagemRpg alma){
        if(ativo==null||ativo!=alma||!tem(NoRpg.DESPERTAR)||despertar)return false;
        despertar=true;return true;
    }
    public boolean despertarLiberado(){return despertar;}
    public boolean podeUsarSlot(int slot){return switch(slot){case 0->true;case 1->tem(NoRpg.TECNICA_X);case 2->tem(NoRpg.TECNICA_C);case 3->tem(NoRpg.TECNICA_V);case 4->despertar;default->false;};}
    public double vidaAdicional(){return (nivel()-1)*2.0;}
    public double multiplicadorFisico(){return 1+(nivel()-1)*.015+(tem(NoRpg.DANO_FISICO)?.10:0);}
    public double multiplicadorMagico(){return 1+(nivel()-1)*.015+(tem(NoRpg.DANO_MAGICO)?.10:0);}
    public double multiplicadorRecarga(){return 1-(nivel()-1)*.005;}
    public double multiplicadorRegeneracao(){return tem(NoRpg.REGENERACAO_MANA)?1.25:1;}
    public double velocidadeAdicional(){return tem(NoRpg.MOVIMENTO)?.10:0;}
    /** Migra nivel/pontos anteriores sem conceder um despertar que ainda exige boss. */
    public static MestriaRpg migrar(int xpAntigo,boolean x,boolean c,boolean v){
        var m=new MestriaRpg(xpAntigo,0,false);
        if(x){m.xp=Math.max(m.xp,100);m.comprar(NoRpg.TECNICA_X);}
        if(c){m.xp=Math.max(m.xp,400);m.comprar(NoRpg.TECNICA_X);m.comprar(NoRpg.TECNICA_C);}
        if(v){m.xp=Math.max(m.xp,900);m.comprar(NoRpg.TECNICA_X);m.comprar(NoRpg.TECNICA_C);m.comprar(NoRpg.TECNICA_V);}
        return m;
    }
}
