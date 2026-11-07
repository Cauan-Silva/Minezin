package com.cauansilva.arcanecontrol.core.rpg;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/**
 * Aplica a mutacao da tecnica no dano: depois de conjurar X/C/V, o dano que o jogador causa por
 * {@link MutacaoRpg#JANELA} ticks e da tecnica (dano, roubo de vida e ressonancia em area).
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class MutacoesServidor {
    private static final String MUT="mut_ativa",ATE="mut_ate";
    private static boolean espalhando;

    static void conjurou(ServerPlayer p,int slot,MutacaoRpg m){
        var t=p.getPersistentData();
        if(m==MutacaoRpg.NENHUMA){t.remove(MUT);return;}
        t.putInt(MUT,m.ordinal());t.putLong(ATE,RpgServidor.agora(p)+MutacaoRpg.JANELA);
    }
    private static MutacaoRpg ativa(ServerPlayer p){
        var t=p.getPersistentData();if(!t.contains(MUT)||RpgServidor.agora(p)>t.getLong(ATE))return MutacaoRpg.NENHUMA;
        return MutacaoRpg.porId(t.getInt(MUT));
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void dano(LivingHurtEvent e){
        if(espalhando||e.getEntity().level().isClientSide||!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getEntity()==p)return;
        var m=ativa(p);if(m==MutacaoRpg.NENHUMA)return;
        float v=(float)(e.getAmount()*m.dano);e.setAmount(v);
        if(m.roubo>0&&p.getHealth()<p.getMaxHealth()){p.heal(v*(float)m.roubo);p.serverLevel().sendParticles(ParticleTypes.HEART,p.getX(),p.getY()+1.8,p.getZ(),1,.2,.1,.2,0);}
        if(m.respingo>0){
            var alvo=e.getEntity();var fonte=p.damageSources().indirectMagic(p,p);float r=(float)(v*m.respingo);
            espalhando=true;
            try{
                for(var o:alvo.level().getEntitiesOfClass(LivingEntity.class,alvo.getBoundingBox().inflate(MutacaoRpg.RAIO_RESSONANCIA),x->x!=alvo&&x!=p&&x.isAlive()&&!(x instanceof Player pl&&!p.canHarmPlayer(pl))&&!x.isAlliedTo(p))){
                    if(o.hurt(fonte,r))p.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT,o.getX(),o.getY()+o.getBbHeight()/2,o.getZ(),6,.3,.3,.3,.1);
                }
            }finally{espalhando=false;}
        }
    }
    private MutacoesServidor(){}
}
