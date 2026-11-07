package com.cauansilva.arcanecontrol.core.passivas;

import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rpg.RpgServidor;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/**
 * Ponte Forge das passivas ({@link Passivas}): so vale para o kit ativo do jogador. O aviso visual (particulas no
 * servidor) aparece no maximo uma vez por segundo por jogador.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class PassivasServidor {
    private static PoderAnime kit(ServerPlayer p){return ProgressaoPoderes.ler(p).ativo();}
    private static int luz(Entity e){return e.level().getMaxLocalRawBrightness(e.blockPosition());}
    private static double vida(LivingEntity e){return e.getHealth()/Math.max(1,e.getMaxHealth());}

    /** Aviso visual curto, no maximo 1 vez por segundo. */
    private static void aviso(ServerPlayer p,ParticleOptions part,int n){
        var t=RpgServidor.dados(p);long agora=RpgServidor.agora(p);if(agora-t.getLong("passiva_aviso")<20)return;t.putLong("passiva_aviso",agora);
        p.serverLevel().sendParticles(part,p.getX(),p.getY()+1,p.getZ(),n,.35,.55,.35,.05);
    }
    private static Passivas.Dano tipo(DamageSource s){
        if(s.is(DamageTypes.LIGHTNING_BOLT))return Passivas.Dano.RAIO;
        if(s.is(DamageTypeTags.WITCH_RESISTANT_TO))return Passivas.Dano.MAGIA;   // magia, magia indireta, espinhos, sopro do dragao
        return Passivas.Dano.OUTRO;
    }

    // ---------------- ganchos chamados pelo RPG ----------------
    /** Recarga base da tecnica com a passiva (Ichigo: Getsuga pela metade com pouca vida). */
    public static int recarga(ServerPlayer p,PoderAnime kit,int slot,int base){return kit==kit(p)?Passivas.recarga(kit,slot,vida(p),base):base;}
    /** Multiplicador da regeneracao de mana (Frieren). */
    public static double regeneracao(ServerPlayer p){return Passivas.regeneracao(kit(p));}
    /** Mana a devolver depois de uma tecnica (Gojo). */
    public static double reembolso(ServerPlayer p,PoderAnime kit,double custo){return kit==kit(p)?Passivas.reembolso(kit,custo):0;}

    // ---------------- dano ----------------
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void dano(LivingHurtEvent e){
        if(e.getEntity().level().isClientSide)return;
        // causado
        if(e.getSource().getEntity() instanceof ServerPlayer atq&&atq!=e.getEntity()){
            var k=kit(atq);boolean desatento=!(e.getEntity() instanceof Mob m)||m.getTarget()!=atq;
            float novo=Passivas.danoCausado(k,luz(atq),desatento,vida(atq),e.getAmount());
            if(novo>e.getAmount()){e.setAmount(novo);
                aviso(atq,k==PoderAnime.CID?ParticleTypes.SQUID_INK:k==PoderAnime.FRIEREN?ParticleTypes.ENCHANT:ParticleTypes.SOUL_FIRE_FLAME,8);}
        }
        // recebido
        if(e.getEntity() instanceof ServerPlayer p){
            var k=kit(p);float novo=Passivas.danoRecebido(k,tipo(e.getSource()),luz(p),e.getAmount());
            if(novo<e.getAmount()){e.setAmount(novo);
                aviso(p,k==PoderAnime.KILLUA?ParticleTypes.ELECTRIC_SPARK:k==PoderAnime.CID?ParticleTypes.SQUID_INK:ParticleTypes.CRIT,10);}
            retorno(e,p,k);
        }
    }
    /** Subaru: o golpe fatal deixa 1 coracao e 3 s de invulnerabilidade (recarga de 10 min). */
    private static void retorno(LivingHurtEvent e,ServerPlayer p,PoderAnime k){
        if(e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        var t=RpgServidor.dados(p);long agora=RpgServidor.agora(p);
        if(!Passivas.retorno(k,p.getHealth(),e.getAmount(),agora,t.getLong("passiva_retorno")))return;
        e.setAmount(Math.max(0,p.getHealth()-Passivas.VIDA_RETORNO));t.putLong("passiva_retorno",agora+Passivas.RECARGA_RETORNO);
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,Passivas.INVULNERAVEL_RETORNO,4,false,true));
        p.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,p.getX(),p.getY()+1,p.getZ(),60,.4,.8,.4,.08);
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),SoundEvents.TOTEM_USE,SoundSource.PLAYERS,.6f,.6f);
        p.displayClientMessage(Component.literal("Retorno pela Morte").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE),true);
    }
    /** Efeitos negativos bloqueados (Killua: veneno/wither; Asta: lentidao). */
    @SubscribeEvent public static void efeito(MobEffectEvent.Applicable e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var id=BuiltInRegistries.MOB_EFFECT.getKey(e.getEffectInstance().getEffect());if(id==null||!id.getNamespace().equals("minecraft"))return;
        var k=kit(p);if(!Passivas.bloqueiaEfeito(k,id.getPath()))return;
        e.setResult(Event.Result.DENY);aviso(p,k==PoderAnime.KILLUA?ParticleTypes.WITCH:ParticleTypes.CRIT,10);
    }
    /** Gojo: inimigos invisiveis por perto brilham (Seis Olhos). */
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||p.tickCount%20!=0||kit(p)!=PoderAnime.GOJO)return;
        for(var v:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(Passivas.RAIO_SEIS_OLHOS),v->v!=p&&v.isAlive()&&v.isInvisible()&&!v.isAlliedTo(p)))
            v.addEffect(new MobEffectInstance(MobEffects.GLOWING,30,0,false,false));
    }
    private PassivasServidor(){}
}
