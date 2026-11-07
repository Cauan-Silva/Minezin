package com.cauansilva.arcanecontrol.core.familiar;

import com.cauansilva.arcanecontrol.core.familiar.RegrasFamiliar.Tipo;
import com.cauansilva.arcanecontrol.core.rpg.RpgServidor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/**
 * Familiares no servidor: o tipo ativo fica no NBT do jogador ("fam_tipo"); a entidade e recriada quando falta
 * (login, troca de dimensao, Vazio). Coleta itens/XP perto dele, acumula Stun nos alvos do dono, aura de mana e
 * Sacrificio de Emergencia contra golpe fatal (o familiar some por um tempo).
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class FamiliarServidor {
    private static final String TIPO="fam_tipo",PRONTO="fam_pronto",AUSENTE="fam_ausente",STUN="stun_v",STUN_T="stun_t",STUN_IMUNE="stun_imune";
    private static final Map<UUID,UUID> ENTIDADES=new HashMap<>();

    public static Tipo tipo(ServerPlayer p){var t=RpgServidor.dados(p);return t.contains(TIPO)?Tipo.porId(t.getInt(TIPO)):null;}
    private static boolean presente(ServerPlayer p){return tipo(p)!=null&&RpgServidor.agora(p)>=RpgServidor.dados(p).getLong(AUSENTE);}

    /** Selo: invoca o tipo (ou dispensa, se ja for o ativo). */
    public static void usarSelo(ServerPlayer p,Tipo t){
        var d=RpgServidor.dados(p);
        if(tipo(p)==t){d.remove(TIPO);remover(p);p.displayClientMessage(Component.literal(t.nome+" dispensado.").withStyle(ChatFormatting.GRAY),true);return;}
        d.putInt(TIPO,t.ordinal());remover(p);
        p.displayClientMessage(Component.literal(t.nome+" invocado: "+t.efeito()).withStyle(s->s.withColor(t.cor)),true);
        p.level().playSound(null,p.blockPosition(),SoundEvents.ALLAY_AMBIENT_WITH_ITEM,SoundSource.PLAYERS,1f,1.2f);
    }
    private static FamiliarEntity entidade(ServerPlayer p){
        var id=ENTIDADES.get(p.getUUID());if(id==null)return null;
        var e=p.serverLevel().getEntity(id);return e instanceof FamiliarEntity f&&f.isAlive()?f:null;
    }
    private static void remover(ServerPlayer p){var id=ENTIDADES.remove(p.getUUID());if(id==null)return;
        for(var l:p.getServer().getAllLevels()){var e=l.getEntity(id);if(e!=null)e.discard();}}

    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||p.tickCount%5!=0)return;
        var t=tipo(p);var f=entidade(p);
        if(t==null||!presente(p)||p.isSpectator()||!p.isAlive()){if(f!=null)remover(p);return;}
        if(f==null||f.tipo()!=t){remover(p);f=RegistroFamiliar.FAMILIAR.get().create(p.serverLevel());if(f==null)return;
            f.configurar(p.getUUID(),t);double[] a=RegrasFamiliar.posicao(p.getX(),p.getY(),p.getZ(),p.getYRot(),0);f.setPos(a[0],a[1],a[2]);
            p.serverLevel().addFreshEntity(f);ENTIDADES.put(p.getUUID(),f.getUUID());}
        if(p.tickCount%10==0)coletar(p,f);
    }
    /** Auto-loot: itens e XP perto do familiar vao para o dono. */
    private static void coletar(ServerPlayer p,FamiliarEntity f){
        var caixa=f.getBoundingBox().inflate(RegrasFamiliar.RAIO_COLETA);
        for(var it:p.level().getEntitiesOfClass(ItemEntity.class,caixa,i->i.isAlive()&&!i.hasPickUpDelay()&&(i.getOwner()==null||i.getOwner()==p))){
            it.setPos(p.getX(),p.getY()+.3,p.getZ());it.setNoPickUpDelay();it.playerTouch(p);}
        for(var o:p.level().getEntitiesOfClass(ExperienceOrb.class,caixa,ExperienceOrb::isAlive))o.setPos(p.getX(),p.getY()+.3,p.getZ());
    }
    /** Aura de mana (RpgServidor multiplica a regeneracao). */
    public static double regeneracao(ServerPlayer p){var t=tipo(p);return t!=null&&presente(p)?t.mana:1;}

    /** Stun cumulativo: cada golpe do dono soma na barra do alvo; cheia = atordoado. */
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void golpe(LivingHurtEvent e){
        if(!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getEntity()==p||e.getEntity() instanceof net.minecraft.world.entity.player.Player)return;
        var t=tipo(p);if(t==null||!presente(p))return;
        var alvo=e.getEntity();var d=alvo.getPersistentData();long agora=RpgServidor.agora(p);
        if(d.getLong(STUN_IMUNE)>agora)return;
        double v=RegrasFamiliar.acumular(d.getDouble(STUN),agora-d.getLong(STUN_T),t.stun);d.putLong(STUN_T,agora);
        if(RegrasFamiliar.atordoa(v)){
            d.putDouble(STUN,0);d.putLong(STUN_IMUNE,agora+RegrasFamiliar.STUN_IMUNE);
            alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,RegrasFamiliar.STUN_DURACAO,9,false,true));
            alvo.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,RegrasFamiliar.STUN_DURACAO,2,false,true));
            if(alvo instanceof Mob m){m.getNavigation().stop();m.setTarget(null);}
            ((ServerLevel)alvo.level()).sendParticles(ParticleTypes.ELECTRIC_SPARK,alvo.getX(),alvo.getY()+alvo.getBbHeight(),alvo.getZ(),30,.4,.3,.4,.2);
            alvo.level().playSound(null,alvo.blockPosition(),SoundEvents.LIGHTNING_BOLT_IMPACT,SoundSource.PLAYERS,.5f,1.8f);
            p.displayClientMessage(Component.literal("Atordoado!").withStyle(ChatFormatting.AQUA),true);
        }else d.putDouble(STUN,v);
    }
    /** Sacrificio de Emergencia: o familiar intercepta o golpe fatal e some por um tempo. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void fatal(LivingDamageEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;var t=tipo(p);if(t==null)return;var d=RpgServidor.dados(p);long agora=RpgServidor.agora(p);
        if(!RegrasFamiliar.intercepta(e.getAmount(),p.getHealth(),presente(p),agora,d.getLong(PRONTO)))return;
        e.setAmount(0);p.setHealth(Math.max(p.getHealth(),t.vidaSalva));
        d.putLong(PRONTO,agora+t.recargaSacrificio);d.putLong(AUSENTE,agora+t.ausencia);
        var f=entidade(p);if(f!=null)p.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,f.getX(),f.getY(),f.getZ(),40,.3,.3,.3,.4);
        remover(p);
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,40,3,false,true));
        p.level().playSound(null,p.blockPosition(),SoundEvents.TOTEM_USE,SoundSource.PLAYERS,.8f,1.3f);
        p.displayClientMessage(Component.literal(t.nome+" se sacrificou por você! Volta em "+t.ausencia/20/60+" min.").withStyle(s->s.withColor(t.cor)),false);
    }
    @SubscribeEvent public static void dimensao(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)remover(p);}
    @SubscribeEvent public static void sair(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)remover(p);}
    private FamiliarServidor(){}
}
