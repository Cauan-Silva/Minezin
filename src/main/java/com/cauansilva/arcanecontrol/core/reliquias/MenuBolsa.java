package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.Tipo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * Bolsa de Reliquias: 5 slots (anel, colar, amuleto, brinco e um slot so para Mitica) + inventario do jogador.
 * O conteudo vive no NBT do jogador (ReliquiasServidor); ao fechar, salva e recalcula os bonus.
 */
public final class MenuBolsa extends AbstractContainerMenu {
    public static final int SLOTS=5;
    private final Container bolsa;
    /** Cliente: conteudo chega pela sincronizacao do menu. */
    public MenuBolsa(int id,Inventory inv){this(id,inv,new SimpleContainer(SLOTS));}
    public MenuBolsa(int id,Inventory inv,Container bolsa){
        super(RegistroReliquias.BOLSA.get(),id);this.bolsa=bolsa;bolsa.startOpen(inv.player);
        for(int i=0;i<SLOTS;i++){final int k=i;addSlot(new Slot(bolsa,i,44+i*18,20){
            @Override public boolean mayPlace(ItemStack s){return aceita(k,s);}
            @Override public int getMaxStackSize(){return 1;}
        });}
        for(int l=0;l<3;l++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+l*9+9,8+c*18,51+l*18));
        for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,109));
    }
    /** Slot k aceita o tipo dele (0-3) ou qualquer Mitica (4). */
    public static boolean aceita(int k,ItemStack s){
        var r=ItemReliquia.ler(s);if(r==null)return false;
        return k==4?r.mitica():r.tipo()==Tipo.values()[k];
    }
    @Override public boolean stillValid(Player p){return true;}
    @Override public ItemStack quickMoveStack(Player p,int indice){
        var slot=slots.get(indice);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copia=s.copy();
        if(indice<SLOTS){if(!moveItemStackTo(s,SLOTS,slots.size(),true))return ItemStack.EMPTY;}
        else{boolean foi=false;for(int k=0;k<SLOTS&&!foi;k++)if(aceita(k,s)&&!slots.get(k).hasItem()){slots.get(k).set(s.split(1));foi=true;}if(!foi)return ItemStack.EMPTY;}
        if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        return copia;
    }
    @Override public void removed(Player p){super.removed(p);bolsa.stopOpen(p);if(p instanceof ServerPlayer sp)ReliquiasServidor.fechou(sp,bolsa);}
}
