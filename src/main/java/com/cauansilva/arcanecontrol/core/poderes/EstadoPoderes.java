package com.cauansilva.arcanecontrol.core.poderes;

import java.util.Arrays;

/** Regras puras: rede e inventario nunca decidem desbloqueios no cliente. */
public final class EstadoPoderes {
    private PoderAnime ativo;
    private int desbloqueados,conhecimento;
    private final int[] xp,usos;
    public EstadoPoderes(){this(1,1,0,new int[3],new int[15]);}
    public EstadoPoderes(int ativo,int desbloqueados,int conhecimento,int[] xp,int[] usos){
        this.desbloqueados=(desbloqueados&7)|1;
        PoderAnime escolhido=PoderAnime.porId(ativo);
        this.ativo=escolhido!=null&&tem(escolhido)?escolhido:PoderAnime.GOJO;
        this.conhecimento=Math.max(0,Math.min(100000,conhecimento));
        this.xp=Arrays.copyOf(xp,3);this.usos=Arrays.copyOf(usos,15);
        for(int i=0;i<3;i++) this.xp[i]=Math.max(0,Math.min(1900,this.xp[i]));
        for(int i=0;i<15;i++) this.usos[i]=Math.max(0,Math.min(100,this.usos[i]));
    }
    public PoderAnime ativo(){return ativo;}
    public int desbloqueados(){return desbloqueados;}
    public int conhecimento(){return conhecimento;}
    public int[] xps(){return xp.clone();}
    public int[] usos(){return usos.clone();}
    public boolean tem(PoderAnime p){return p!=null&&(desbloqueados&p.bit())!=0;}
    public int xp(PoderAnime p){return xp[p.id-1];}
    public int nivel(PoderAnime p){return 1+xp(p)/100;}
    public boolean podeDesbloquear(PoderAnime p){return p!=null&&!tem(p)&&conhecimento>=p.custo;}
    public boolean desbloquear(PoderAnime p,boolean criativo){
        if(p==null||tem(p)||!criativo&&!podeDesbloquear(p)) return false;
        if(!criativo) conhecimento-=p.custo;
        desbloqueados|=p.bit();if(criativo) xp[p.id-1]=Math.max(400,xp[p.id-1]);return true;
    }
    public boolean selecionar(PoderAnime p){if(!tem(p))return false;ativo=p;return true;}
    public void migrarCid(){desbloqueados|=PoderAnime.CID.bit();xp[1]=Math.max(400,xp[1]);}
    public void ganharConhecimento(int valor){if(valor>0) conhecimento=(int)Math.min(100000L,(long)conhecimento+valor);}
    public void ganharXp(PoderAnime p,int valor){if(tem(p)&&valor>0)xp[p.id-1]=(int)Math.min(1900L,(long)xp[p.id-1]+valor);}
    public boolean podeUsar(PoderAnime p,int slot){
        return p!=null&&ativo==p&&tem(p)&&slot>=0&&slot<p.tecnicas.length
                &&(p==PoderAnime.GOJO||nivel(p)>=p.nivelTecnica(slot));
    }
    public int maestria(PoderAnime p,int slot){return slot<0||slot>=p.tecnicas.length?0:usos[(p.id-1)*5+slot]/20;}
    public int recarga(PoderAnime p,int slot,int base){return base*(100-maestria(p,slot)*5)/100;}
    public void registrarUso(PoderAnime p,int slot){
        if(!podeUsar(p,slot))return;
        int i=(p.id-1)*5+slot;usos[i]=Math.min(100,usos[i]+1);if(p!=PoderAnime.GOJO) ganharXp(p,5);
    }
}
