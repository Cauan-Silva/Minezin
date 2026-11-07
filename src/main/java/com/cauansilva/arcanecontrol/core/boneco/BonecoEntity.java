package com.cauansilva.arcanecontrol.core.boneco;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Boneco de treino: nunca morre nem se mexe. Mede o dano final que recebe (depois de armadura, reacoes, execucoes...)
 * e mostra DPS/maior golpe/total no nome. Ao ficar ocioso manda o resumo a quem treinou. Agachado + clique direito
 * devolve o item.
 */
public final class BonecoEntity extends Mob {
    final SessaoDps sessao=new SessaoDps();
    private java.util.UUID treinador;

    public BonecoEntity(EntityType<? extends BonecoEntity> t,Level l){super(t,l);setNoAi(true);setPersistenceRequired();nomePadrao();}
    public static AttributeSupplier.Builder atributos(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,1000).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.MOVEMENT_SPEED,0);}
    private void nomePadrao(){setCustomName(Component.literal("Boneco de Treino").withStyle(ChatFormatting.GOLD));setCustomNameVisible(true);}

    /** Chamado pelo servidor com o dano final; o dano e anulado para o boneco nunca morrer. */
    void registrar(ServerPlayer p,float dano){
        treinador=p.getUUID();sessao.registrar(level().getGameTime(),dano);
        setCustomName(Component.literal(sessao.painel()).withStyle(ChatFormatting.YELLOW));
    }
    @Override public void tick(){
        super.tick();
        if(level().isClientSide)return;
        if(getHealth()<getMaxHealth())setHealth(getMaxHealth());
        if(sessao.ativa()&&sessao.expirou(level().getGameTime())){
            var p=treinador==null?null:level().getPlayerByUUID(treinador);
            if(p!=null)p.displayClientMessage(Component.literal("Treino: ").withStyle(ChatFormatting.GOLD).append(Component.literal(sessao.resumo()).withStyle(ChatFormatting.WHITE)),false);
            sessao.encerrar();nomePadrao();
        }
    }
    @Override protected InteractionResult mobInteract(Player p,InteractionHand mao){
        if(!p.isShiftKeyDown())return InteractionResult.PASS;
        if(!level().isClientSide){if(!p.getAbilities().instabuild)p.getInventory().placeItemBackInInventory(new ItemStack(RegistroBoneco.ITEM_BONECO.get()));discard();}
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override public boolean hurt(DamageSource s,float v){
        if(s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)){discard();return false;}
        return super.hurt(s,v);
    }
    @Override public boolean isPushable(){return false;}
    @Override public void push(double x,double y,double z){}
    @Override public void knockback(double f,double x,double z){}
    @Override public void setDeltaMovement(Vec3 v){super.setDeltaMovement(new Vec3(0,Math.min(0,v.y),0));}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override protected void dropCustomDeathLoot(DamageSource s,int l,boolean b){}
    @Override public boolean canBeLeashed(Player p){return false;}
}
