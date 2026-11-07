package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.Reforja;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import java.util.Random;

/**
 * Ferreiro: slot 0 reliquia, slot 1 Essencia do Vazio. Botoes (clickMenuButton, pacote vanilla):
 * 0 desmanchar (reliquia vira Essencia), 1 reforjar valores, 2 reforjar tudo. Tudo decidido no servidor.
 */
public final class MenuFerreiro extends AbstractContainerMenu {
    public static final int DESMANCHAR=0,VALORES=1,TUDO=2;
    private static final Random RNG=new Random();
    private final Container c=new SimpleContainer(2){@Override public void setChanged(){super.setChanged();slotsChanged(this);}};
    private final ContainerLevelAccess acesso;
    private final Inventory inv;
    public MenuFerreiro(int id,Inventory inv){this(id,inv,ContainerLevelAccess.NULL);}
    public MenuFerreiro(int id,Inventory inv,ContainerLevelAccess acesso){
        super(RegistroReliquias.MENU_FERREIRO.get(),id);this.acesso=acesso;this.inv=inv;
        addSlot(new Slot(c,0,44,24){@Override public boolean mayPlace(ItemStack s){return ItemReliquia.ler(s)!=null;}@Override public int getMaxStackSize(){return 1;}});
        addSlot(new Slot(c,1,116,24){@Override public boolean mayPlace(ItemStack s){return s.is(RegistroReliquias.ESSENCIA.get());}});
        for(int l=0;l<3;l++)for(int k=0;k<9;k++)addSlot(new Slot(inv,k+l*9+9,8+k*18,84+l*18));
        for(int k=0;k<9;k++)addSlot(new Slot(inv,k,8+k*18,142));
    }
    public ItemStack reliquia(){return slots.get(0).getItem();}
    /** Essencia disponivel: a do slot mais a do inventario (a reforja Mitica custa mais que um pack). */
    public int essencia(){return slots.get(1).getItem().getCount()+inv.countItem(RegistroReliquias.ESSENCIA.get());}
    /** Gasta primeiro do slot, depois do inventario. */
    private void gastar(int n){
        var e=c.getItem(1);int d=Math.min(n,e.getCount());e.shrink(d);n-=d;c.setChanged();
        for(int i=0;i<inv.getContainerSize()&&n>0;i++){var s=inv.getItem(i);if(s.is(RegistroReliquias.ESSENCIA.get())){int k=Math.min(n,s.getCount());s.shrink(k);n-=k;}}
    }

    @Override public boolean clickMenuButton(Player p,int botao){
        var s=c.getItem(0);var r=ItemReliquia.ler(s);if(r==null||!(p instanceof ServerPlayer sp))return false;
        if(botao==DESMANCHAR){
            int ganho=Reliquias.DESMANCHE[r.tier()];c.setItem(0,ItemStack.EMPTY);
            var e=c.getItem(1);
            if(e.isEmpty())c.setItem(1,new ItemStack(RegistroReliquias.ESSENCIA.get(),ganho));
            else{int cabe=Math.min(ganho,e.getMaxStackSize()-e.getCount());e.grow(cabe);c.setChanged();if(ganho>cabe)dar(sp,new ItemStack(RegistroReliquias.ESSENCIA.get(),ganho-cabe));}
            som(sp,SoundEvents.GRINDSTONE_USE);msg(sp,"Desmanchada: +"+ganho+" Essência do Vazio",ChatFormatting.LIGHT_PURPLE);
        }else if(botao==VALORES||botao==TUDO){
            var modo=botao==TUDO?Reforja.TUDO:Reforja.VALORES;int custo=Reliquias.custo(r.tier(),modo);
            if(essencia()<custo){msg(sp,"Essência insuficiente: precisa de "+custo+".",ChatFormatting.RED);return false;}
            gastar(custo);
            var nova=ItemReliquia.criar(Reliquias.reforjar(RNG,r,modo));
            if(ItemReliquia.soulbound(s))nova.getOrCreateTag().putBoolean(ItemReliquia.ALMA,true);
            c.setItem(0,nova);som(sp,SoundEvents.SMITHING_TABLE_USE);
            msg(sp,"Reforjada: "+ItemReliquia.ler(nova).nome(),ChatFormatting.GOLD);
        }else return false;
        broadcastChanges();return true;
    }
    private static void dar(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
    private static void som(ServerPlayer p,SoundEvent s){p.level().playSound(null,p.blockPosition(),s,SoundSource.BLOCKS,.9f,.8f);}
    private static void msg(ServerPlayer p,String t,ChatFormatting f){p.displayClientMessage(Component.literal(t).withStyle(f),true);}

    @Override public boolean stillValid(Player p){return acesso==ContainerLevelAccess.NULL||stillValid(acesso,p,RegistroReliquias.FERREIRO.get());}
    @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide)clearContainer(p,c);}
    @Override public ItemStack quickMoveStack(Player p,int i){
        var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copia=s.copy();
        if(i<2){if(!moveItemStackTo(s,2,slots.size(),true))return ItemStack.EMPTY;}
        else if(ItemReliquia.ler(s)!=null){if(slots.get(0).hasItem())return ItemStack.EMPTY;slots.get(0).set(s.split(1));}
        else if(s.is(RegistroReliquias.ESSENCIA.get())){if(!moveItemStackTo(s,1,2,false))return ItemStack.EMPTY;}
        else return ItemStack.EMPTY;
        if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        return copia;
    }
}
