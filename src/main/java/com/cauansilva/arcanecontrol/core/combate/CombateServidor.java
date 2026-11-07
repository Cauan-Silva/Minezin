package com.cauansilva.arcanecontrol.core.combate;

import com.cauansilva.arcanecontrol.core.rede.RegistoRede;
import com.cauansilva.arcanecontrol.core.rpg.RpgServidor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Polimento de combate no servidor: i-frames da esquiva, Perfect Dodge com contra-ataque e o aviso de impacto
 * (hitstop/tremor) para quem bate e quem apanha. O movimento do dash e feito no cliente (resposta imediata).
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class CombateServidor {
    private static final String INICIO="esq_inicio",ULTIMA="esq_ultima",CONTRA="esq_contra",PERFEITA="esq_perfeita",CRIT="cmb_crit";

    private static void enviar(ServerPlayer p,PacoteCombate pac){RegistoRede.ENVIAR.send(PacketDistributor.PLAYER.with(()->p),pac);}

    /** Pedido de esquiva (PacoteAcaoRpg acao 11). */
    public static void esquivar(ServerPlayer p){
        if(!p.isAlive()||p.isSpectator())return;var t=p.getPersistentData();long agora=RpgServidor.agora(p);
        if(!RegrasCombate.podeEsquivar(agora,t.getLong(ULTIMA)))return;
        t.putLong(ULTIMA,agora);t.putLong(INICIO,agora);t.remove(PERFEITA);
        p.level().playSound(null,p.blockPosition(),SoundEvents.PHANTOM_FLAP,SoundSource.PLAYERS,.6f,1.6f);
        p.causeFoodExhaustion(.4f);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void atacado(LivingAttackEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        var t=p.getPersistentData();long agora=RpgServidor.agora(p),inicio=t.getLong(INICIO);
        if(!RegrasCombate.invulneravel(agora,inicio))return;
        e.setCanceled(true);
        if(RegrasCombate.perfeita(agora,inicio)&&!t.getBoolean(PERFEITA)&&e.getSource().getEntity()!=null){
            t.putBoolean(PERFEITA,true);t.putLong(CONTRA,agora+RegrasCombate.CONTRA_JANELA);t.putLong(ULTIMA,RegrasCombate.reembolso(t.getLong(ULTIMA)));
            p.serverLevel().sendParticles(ParticleTypes.END_ROD,p.getX(),p.getY()+1,p.getZ(),14,.4,.6,.4,.04);
            p.level().playSound(null,p.blockPosition(),SoundEvents.AMETHYST_BLOCK_RESONATE,SoundSource.PLAYERS,1.2f,1.4f);
            enviar(p,new PacoteCombate(PacoteCombate.PERFEITA,1,0,0,false));
        }
    }
    @SubscribeEvent public static void critico(CriticalHitEvent e){
        if(e.getEntity() instanceof ServerPlayer p&&(e.isVanillaCritical()||e.getResult()==Event.Result.ALLOW))p.getPersistentData().putLong(CRIT,RpgServidor.agora(p));
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void ferido(LivingHurtEvent e){
        var alvo=e.getEntity();if(alvo.level().isClientSide||e.getAmount()<=0)return;
        var autor=e.getSource().getEntity();
        if(autor instanceof ServerPlayer p&&p!=alvo){
            var t=p.getPersistentData();long agora=RpgServidor.agora(p);
            if(t.getLong(CONTRA)>=agora){t.remove(CONTRA);e.setAmount((float)(e.getAmount()*(1+RegrasCombate.CONTRA_BONUS)));
                p.level().playSound(null,alvo.blockPosition(),SoundEvents.PLAYER_ATTACK_CRIT,SoundSource.PLAYERS,1f,.7f);enviar(p,new PacoteCombate(PacoteCombate.CONTRA,1,0,0,false));}
            boolean crit=t.getLong(CRIT)==agora;
            double f=RegrasCombate.forca(e.getAmount(),alvo.getMaxHealth(),crit);
            Vec3 d=alvo.position().subtract(p.position());d=new Vec3(d.x,0,d.z);d=d.lengthSqr()<1e-6?Vec3.ZERO:d.normalize();
            if(f>0)enviar(p,new PacoteCombate(PacoteCombate.IMPACTO,(float)f,(float)d.x,(float)d.z,false));
        }
        if(alvo instanceof ServerPlayer v&&autor!=null&&autor!=v){
            double f=RegrasCombate.forca(e.getAmount(),v.getMaxHealth(),false);
            Vec3 d=v.position().subtract(autor.position());d=new Vec3(d.x,0,d.z);d=d.lengthSqr()<1e-6?Vec3.ZERO:d.normalize();
            if(f>0)enviar(v,new PacoteCombate(PacoteCombate.IMPACTO,(float)f,(float)d.x,(float)d.z,true));
        }
    }
    private CombateServidor(){}
}
