package com.cauansilva.arcanecontrol.core.postura;

import com.cauansilva.arcanecontrol.core.rpg.NemesisEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/**
 * Postura no servidor: o estado fica no persistentData do alvo. Golpes de jogador enchem a barra (mostrada na action
 * bar de quem bateu); cheia = Quebrada (lento, fraco, brilhando). O proximo golpe corpo a corpo de um jogador e a
 * Execucao: dano multiplicado, efeito forte e a barra zera com imunidade curta.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class PosturaServidor {
    private static final String V="pos_v",T="pos_t",QUEBRA="pos_quebra",IMUNE="pos_imune";

    static boolean chefe(LivingEntity e){return e instanceof NemesisEntity||e.getType().is(Tags.EntityTypes.BOSSES)||e.getMaxHealth()>=150;}

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void golpe(LivingHurtEvent e){
        if(!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getEntity() instanceof Player)return;
        var alvo=e.getEntity();if(!(alvo.level() instanceof ServerLevel sl))return;
        var d=alvo.getPersistentData();long agora=sl.getGameTime();boolean chefe=chefe(alvo);
        boolean corpo=e.getSource().getDirectEntity()==p;
        if(d.getLong(QUEBRA)>agora){
            if(!corpo)return;
            e.setAmount((float)RegrasPostura.execucao(e.getAmount(),alvo.getHealth(),alvo.getMaxHealth(),chefe));
            d.putLong(QUEBRA,0);d.putDouble(V,0);d.putLong(IMUNE,agora+RegrasPostura.IMUNE);
            alvo.removeEffect(MobEffects.GLOWING);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK,alvo.getX(),alvo.getY()+alvo.getBbHeight()*.5,alvo.getZ(),6,.5,.4,.5,0);
            sl.sendParticles(ParticleTypes.EXPLOSION,alvo.getX(),alvo.getY()+alvo.getBbHeight()*.5,alvo.getZ(),1,0,0,0,0);
            sl.sendParticles(ParticleTypes.CRIT,alvo.getX(),alvo.getY()+alvo.getBbHeight()*.5,alvo.getZ(),40,.4,.5,.4,.6);
            sl.playSound(null,alvo.blockPosition(),SoundEvents.PLAYER_ATTACK_CRIT,SoundSource.PLAYERS,1.2f,.6f);
            sl.playSound(null,alvo.blockPosition(),SoundEvents.ANVIL_LAND,SoundSource.PLAYERS,.6f,.7f);
            p.displayClientMessage(Component.literal("✦ EXECUÇÃO ✦").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD),true);
            return;
        }
        if(d.getLong(IMUNE)>agora)return;
        double max=RegrasPostura.maximo(alvo.getMaxHealth(),chefe);
        boolean critico=corpo&&p.fallDistance>0&&!p.onGround()&&!p.isSprinting();
        double v=RegrasPostura.recuperar(d.getDouble(V),agora-d.getLong(T),max)
            +RegrasPostura.golpe(e.getAmount(),corpo,corpo?p.getAttackStrengthScale(.5f):0,critico);
        d.putLong(T,agora);
        if(RegrasPostura.quebra(v,max)){
            int dur=RegrasPostura.duracao(chefe);d.putDouble(V,max);d.putLong(QUEBRA,agora+dur);
            alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,dur,9,false,false));
            alvo.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,dur,4,false,false));
            alvo.addEffect(new MobEffectInstance(MobEffects.GLOWING,dur,0,false,false));
            if(alvo instanceof Mob m){m.getNavigation().stop();m.setTarget(null);}
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT,alvo.getX(),alvo.getY()+alvo.getBbHeight(),alvo.getZ(),30,.4,.2,.4,.3);
            sl.playSound(null,alvo.blockPosition(),SoundEvents.SHIELD_BREAK,SoundSource.PLAYERS,1f,.8f);
            p.displayClientMessage(Component.literal("POSTURA QUEBRADA — golpeie para EXECUTAR!").withStyle(ChatFormatting.RED,ChatFormatting.BOLD),true);
        }else{
            d.putDouble(V,v);
            p.displayClientMessage(Component.literal("Postura ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(RegrasPostura.barra(v,max,12)).withStyle(v>=max*.75?ChatFormatting.RED:ChatFormatting.YELLOW)),true);
        }
    }
    private PosturaServidor(){}
}
