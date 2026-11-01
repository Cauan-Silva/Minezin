package com.cauansilva.arcanecontrol.core.rpg;

/** Valida antes de consumir recursos. Executar na thread do servidor. */
public final class ConjuracaoRpg {
    public enum Resultado{OK,BLOQUEADA,RECARGA,MANA,INVALIDA}
    public static Resultado tentar(MestriaRpg m,RecursoMana mana,RecargasRpg cds,PersonagemRpg kit,int slot,
                                   long agora,int baseTicks,double custo,boolean conjunto,boolean criativo,boolean legadoExtra){
        return tentar(m,mana,cds,kit,slot,agora,baseTicks,custo,conjunto,criativo,legadoExtra,false);
    }
    public static Resultado tentar(MestriaRpg m,RecursoMana mana,RecargasRpg cds,PersonagemRpg kit,int slot,
                                   long agora,int baseTicks,double custo,boolean conjunto,boolean criativo,boolean legadoExtra,boolean forma){
        if(kit==null||slot<0||slot>4||agora<0||baseTicks<0||!Double.isFinite(custo)||custo<0)return Resultado.INVALIDA;
        if(!m.podeUsarSlot(slot)&&!(criativo&&slot<4))return Resultado.BLOQUEADA;
        if(cds.restante(kit,slot,agora,forma)>0)return Resultado.RECARGA;
        double cobrado=criativo?0:RecursoMana.custo(custo,conjunto);
        if(mana.atual()+1e-9<cobrado)return Resultado.MANA;
        if(!cds.iniciar(kit,slot,agora,baseTicks,m.multiplicadorRecarga(),forma))return Resultado.INVALIDA;
        mana.consumir(cobrado,false);return Resultado.OK;
    }
    public static double custoBase(int slot){return switch(slot){case 0->8;case 1->12;case 2->20;case 3->35;case 4->50;default->0;};}
}
