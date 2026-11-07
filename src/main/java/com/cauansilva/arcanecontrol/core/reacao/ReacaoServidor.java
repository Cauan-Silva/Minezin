package com.cauansilva.arcanecontrol.core.reacao;

import com.cauansilva.arcanecontrol.core.reacao.RegrasReacao.*;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/**
 * Reacoes no servidor. Elemento do golpe: arma imbuida com Frasco (so corpo a corpo) ou o tipo do dano (fogo,
 * congelamento, raio). Aura do alvo: a guardada no persistentData; sem ela, o estado natural (pegando fogo, molhado,
 * congelado). Reacao consome a aura; golpe sem reacao deixa a aura do seu elemento.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class ReacaoServidor {
    private static final String AURA="elem_a",AURA_T="elem_t",FRAGIL="elem_fragil",IMB="elem_imb",IMB_T="elem_imb_t";
    private static boolean emArea;

    public static void imbuir(ServerPlayer p,Elemento e){
        var d=p.getPersistentData();d.putInt(IMB,e.ordinal());d.putLong(IMB_T,p.serverLevel().getGameTime()+RegrasReacao.IMBUIR);
        p.displayClientMessage(Component.literal("Arma imbuída: "+e.nome+" (60 s)").withStyle(s->s.withColor(e.cor)),true);
        p.level().playSound(null,p.blockPosition(),SoundEvents.BREWING_STAND_BREW,SoundSource.PLAYERS,.8f,1.4f);
    }
    private static Elemento elementoDoGolpe(LivingHurtEvent e,ServerPlayer p,long agora){
        var s=e.getSource();
        if(s.getDirectEntity()==p){var d=p.getPersistentData();if(d.contains(IMB)&&d.getLong(IMB_T)>agora)return Elemento.porId(d.getInt(IMB));}
        if(s.is(DamageTypeTags.IS_LIGHTNING))return Elemento.RAIO;
        if(s.is(DamageTypeTags.IS_FREEZING))return Elemento.GELO;
        if(s.is(DamageTypeTags.IS_FIRE))return Elemento.FOGO;
        return null;
    }
    private static Elemento aura(LivingEntity a,long agora){
        var d=a.getPersistentData();if(d.contains(AURA)&&d.getLong(AURA_T)>agora)return Elemento.porId(d.getInt(AURA));
        if(a.isOnFire())return Elemento.FOGO;
        if(a.isInWaterRainOrBubble())return Elemento.AGUA;
        if(a.getTicksFrozen()>0)return Elemento.GELO;
        return null;
    }

    @SubscribeEvent(priority=EventPriority.NORMAL)
    public static void golpe(LivingHurtEvent e){
        if(emArea||!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getEntity() instanceof Player)return;
        var alvo=e.getEntity();if(!(alvo.level() instanceof ServerLevel sl))return;long agora=sl.getGameTime();var d=alvo.getPersistentData();
        if(d.getLong(FRAGIL)>agora)e.setAmount((float)(e.getAmount()*RegrasReacao.FRAGIL_MULT));
        var g=elementoDoGolpe(e,p,agora);if(g==null)return;
        var a=aura(alvo,agora);var r=RegrasReacao.reagir(a,g);
        if(r==null){d.putInt(AURA,g.ordinal());d.putLong(AURA_T,agora+RegrasReacao.AURA);
            sl.sendParticles(new DustParticleOptions(cor(g.cor),1.2f),alvo.getX(),alvo.getY()+alvo.getBbHeight()+.3,alvo.getZ(),8,.25,.1,.25,0);return;}
        d.remove(AURA);
        e.setAmount((float)(e.getAmount()*RegrasReacao.multiplicador(r,g)));
        double x=alvo.getX(),y=alvo.getY()+alvo.getBbHeight()*.5,z=alvo.getZ();
        switch(r){
            case DERRETER->{alvo.clearFire();alvo.setTicksFrozen(0);sl.sendParticles(ParticleTypes.FALLING_LAVA,x,y,z,20,.4,.4,.4,0);
                sl.playSound(null,alvo.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.PLAYERS,1f,.7f);}
            case VAPORIZAR->{alvo.clearFire();sl.sendParticles(ParticleTypes.CLOUD,x,y,z,30,.4,.5,.4,.08);
                sl.playSound(null,alvo.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.PLAYERS,1f,1.4f);}
            case SOBRECARGA->{sl.sendParticles(ParticleTypes.EXPLOSION,x,y,z,2,.3,.3,.3,0);sl.sendParticles(ParticleTypes.FLAME,x,y,z,40,.6,.6,.6,.15);
                sl.playSound(null,alvo.blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,.7f,1.3f);
                area(sl,p,alvo,RegrasReacao.SOBRECARGA_RAIO,(float)RegrasReacao.sobrecarga(e.getAmount()),Integer.MAX_VALUE,false,true);}
            case ELETROCUTAR->{sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,x,y,z,40,.5,.6,.5,.3);
                sl.playSound(null,alvo.blockPosition(),SoundEvents.TRIDENT_THUNDER,SoundSource.PLAYERS,.5f,1.6f);
                alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,9,false,false));
                area(sl,p,alvo,RegrasReacao.ELETRO_RAIO,(float)(e.getAmount()*RegrasReacao.ELETRO_FRACAO),RegrasReacao.ELETRO_MAX,true,false);}
            case CONGELAR->{alvo.setTicksFrozen(alvo.getTicksRequiredToFreeze()+RegrasReacao.CONGELA);
                alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,RegrasReacao.CONGELA,9,false,false));
                alvo.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,RegrasReacao.CONGELA,4,false,false));
                if(alvo instanceof Mob m)m.getNavigation().stop();
                sl.sendParticles(ParticleTypes.SNOWFLAKE,x,y,z,40,.4,.6,.4,.05);sl.playSound(null,alvo.blockPosition(),SoundEvents.GLASS_BREAK,SoundSource.PLAYERS,.8f,1.6f);}
            case SUPERCONDUTOR->{d.putLong(FRAGIL,agora+RegrasReacao.FRAGIL);
                sl.sendParticles(new DustParticleOptions(cor(r.cor),1.5f),x,y,z,30,.5,.6,.5,0);sl.playSound(null,alvo.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.PLAYERS,1.2f,.6f);}
        }
        p.displayClientMessage(Component.literal(r.nome+"!").withStyle(s->s.withColor(r.cor).withBold(true)),true);
    }
    /** Dano secundario das reacoes (sem disparar novas reacoes em cadeia). */
    private static void area(ServerLevel sl,ServerPlayer p,LivingEntity centro,double raio,float dano,int max,boolean soMolhados,boolean empurra){
        if(dano<=0)return;emArea=true;
        try{int n=0;
            for(var o:sl.getEntitiesOfClass(LivingEntity.class,centro.getBoundingBox().inflate(raio),o->o!=centro&&o!=p&&o.isAlive()&&!(o instanceof Player)&&!(o instanceof ArmorStand))){
                if(soMolhados&&!o.isInWaterRainOrBubble()&&aura(o,sl.getGameTime())!=Elemento.AGUA)continue;
                if(n++>=max)break;
                o.hurt(sl.damageSources().playerAttack(p),dano);
                if(empurra){var v=o.position().subtract(centro.position()).normalize().scale(.6);o.push(v.x,.3,v.z);}
                if(soMolhados)sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,o.getX(),o.getY()+o.getBbHeight()*.5,o.getZ(),12,.3,.4,.3,.2);
            }
        }finally{emArea=false;}
    }
    private static Vector3f cor(int c){return new Vector3f((c>>16&255)/255f,(c>>8&255)/255f,(c&255)/255f);}
    private ReacaoServidor(){}
}
