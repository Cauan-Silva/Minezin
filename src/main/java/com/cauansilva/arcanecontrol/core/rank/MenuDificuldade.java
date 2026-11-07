package com.cauansilva.arcanecontrol.core.rank;

import com.cauansilva.arcanecontrol.core.rank.RankCacador.*;
import com.cauansilva.arcanecontrol.core.vazio.VazioServidor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * Menu sem slots do altar: o cliente ve o proprio rank, o rank medio do grupo e o tamanho do grupo (DataSlots);
 * o botao (clickMenuButton, pacote vanilla) e a dificuldade. O servidor confere tudo de novo.
 */
public final class MenuDificuldade extends AbstractContainerMenu {
    private final BlockPos altar;
    private final ContainerData dados;
    public MenuDificuldade(int id,Inventory inv){super(RankServidor.MENU.get(),id);altar=BlockPos.ZERO;dados=new SimpleContainerData(3);addDataSlots(dados);}
    MenuDificuldade(int id,Inventory inv,BlockPos altar,ServerPlayer p){
        super(RankServidor.MENU.get(),id);this.altar=altar;
        var grupo=VazioServidor.grupo(p,altar);
        int pts=Math.min(32767,RankServidor.pontos(p)),media=RankCacador.media(grupo.stream().map(RankServidor::rank).toList()).ordinal(),n=grupo.size();
        dados=new ContainerData(){
            public int get(int i){return i==0?pts:i==1?media:n;}
            public void set(int i,int v){}
            public int getCount(){return 3;}
        };
        addDataSlots(dados);
    }
    public int pontos(){return dados.get(0);}
    public Rank media(){return Rank.values()[Math.max(0,Math.min(Rank.values().length-1,dados.get(1)))];}
    public int grupo(){return dados.get(2);}
    @Override public boolean clickMenuButton(Player p,int botao){
        if(!(p instanceof ServerPlayer sp)||botao<0||botao>=Dificuldade.values().length)return false;
        RankServidor.escolher(sp,altar,Dificuldade.porId(botao));return true;
    }
    @Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
    @Override public boolean stillValid(Player p){return altar==BlockPos.ZERO||p.distanceToSqr(altar.getX()+.5,altar.getY()+.5,altar.getZ()+.5)<=64;}
}
