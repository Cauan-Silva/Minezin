package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rpg.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class PoderesAsta {
    private record Golpe(ServerPlayer p,ServerLevel mundo,Vec3 origem,Vec3 dir,long inicio,int slot,boolean forma,LivingEntity alvo,Set<UUID> atingidos){}
    private static final List<Golpe> GOLPES=new ArrayList<>();
    public static boolean armado(ServerPlayer p){
        if(p.getMainHandItem().is(RegistroAsta.ESPADA.get()))return true;
        p.displayClientMessage(net.minecraft.network.chat.Component.literal("Segure a Espada Matadora de Demônios."),true);return false;
    }
    public static void executar(ServerPlayer p,int slot){
        if(slot<0||slot>3||!armado(p)||!RpgServidor.podeUsar(p,PoderAnime.ASTA,slot)||GOLPES.size()>=128)return;
        if(GOLPES.stream().anyMatch(g->g.p==p))return;
        boolean forma=DespertaresServidor.ativo(p);if(forma&&DespertaresServidor.restante(p)>PerfilAsta.DURACAO_FORMA-PerfilAsta.ABERTURA)return;Vec3 dir=p.getLookAngle();
        LivingEntity alvo=forma&&slot==0?alvo(p,20):null;
        if(forma&&slot==0&&alvo==null){p.displayClientMessage(net.minecraft.network.chat.Component.literal("Mire em um inimigo a até vinte blocos para o Black Meteorite."),true);return;}
        if(!RpgServidor.conjurar(p,PoderAnime.ASTA,slot,PerfilAsta.recarga(slot,forma)))return;
        if(!forma&&slot==3)RpgServidor.dados(p).putLong("asta_barreira",RpgServidor.agora(p)+PerfilAsta.BARREIRA);
        else GOLPES.add(new Golpe(p,p.serverLevel(),p.position(),dir,RpgServidor.agora(p),slot,forma,alvo,new HashSet<>()));
        EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),300+(forma?4:0)+slot,p.position(),dir,!forma&&slot==3?100:forma&&slot==3?30:32,4);
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,1.3f,.55f);
        if(forma||slot!=3)ProgressaoPoderes.bloquear(p,32);
        RpgServidor.uso(p,PoderAnime.ASTA);
    }
    private static LivingEntity alvo(ServerPlayer p,double r){
        return inimigos(p,p.position(),r).stream().filter(p::hasLineOfSight).filter(v->v.getEyePosition().subtract(p.getEyePosition()).normalize().dot(p.getLookAngle())>.75).min(Comparator.comparingDouble(v->v.distanceToSqr(p))).orElse(null);
    }
    private static List<LivingEntity> inimigos(ServerPlayer p,Vec3 c,double r){return p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(c,c).inflate(r),v->v!=p&&v.isAlive()&&!v.isAlliedTo(p)&&v.distanceToSqr(c)<=r*r).stream().limit(64).toList();}
    private static boolean valido(Golpe g){return g.p.isAlive()&&!g.p.hasDisconnected()&&g.p.serverLevel()==g.mundo&&ProgressaoPoderes.ler(g.p).ativo()==PoderAnime.ASTA&&(!g.forma||DespertaresServidor.ativo(g.p));}
    private static void mover(ServerPlayer p,Vec3 deslocamento){
        var q=p.position().add(deslocamento);var pos=BlockPos.containing(q);
        if(!p.serverLevel().hasChunkAt(pos)||!p.serverLevel().getWorldBorder().isWithinBounds(pos)||!p.serverLevel().noCollision(p,p.getBoundingBox().move(deslocamento)))return;
        p.connection.teleport(q.x,q.y,q.z,p.getYRot(),p.getXRot());p.fallDistance=0;
    }
    private static void dano(Golpe g,LivingEntity v,float dano){if(g.atingidos.add(v.getUUID()))v.hurt(g.p.damageSources().playerAttack(g.p),dano);}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var it=GOLPES.iterator();while(it.hasNext()){
            var g=it.next();int t=(int)(RpgServidor.agora(g.p)-g.inicio);
            if(!valido(g)||t>32){it.remove();continue;}
            var p=g.p;int impacto=PerfilAsta.impacto(g.slot,g.forma);
            if(!g.forma&&g.slot==0&&t>=3&&t<=10)refletirProximos(p,4,false);
            if(!g.forma&&g.slot==2&&t<=10){mover(p,g.dir.multiply(1,0,1).normalize().scale(.65));RpgServidor.dados(p).putLong("asta_protegido",RpgServidor.agora(p)+2);}
            if(g.forma&&g.slot==0&&t<12&&g.alvo!=null&&g.alvo.isAlive()&&g.alvo.level()==g.mundo){var d=g.alvo.position().subtract(p.position());mover(p,d.normalize().scale(Math.min(.9,d.length())));RpgServidor.dados(p).putLong("asta_protegido",RpgServidor.agora(p)+2);}
            if(g.forma&&g.slot==3&&t>=8&&t<=28){
                double distancia=PerfilAsta.alcanceOnda(t);Vec3 anterior=g.origem.add(0,1,0).add(g.dir.scale(Math.max(0,distancia-1.2))),c=g.origem.add(0,1,0).add(g.dir.scale(distancia));
                var colisao=g.mundo.clip(new net.minecraft.world.level.ClipContext(anterior,c,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
                if(colisao.getType()!=HitResult.Type.MISS){EfeitosDespertar.enviar(g.mundo,p.getUUID(),310,colisao.getLocation(),Vec3.ZERO,15,4);it.remove();continue;}
                for(var v:inimigos(p,c,3))if(p.hasLineOfSight(v)){dano(g,v,18);v.addEffect(new MobEffectInstance(RegistroAsta.SILENCIO.get(),PerfilAsta.SILENCIO,0));}
            }
            if(t!=impacto)continue;
            if(!g.forma){
                for(var v:inimigos(p,p.position(),g.slot==0?5:g.slot==1?7:3)){
                    var d=v.getEyePosition().subtract(p.getEyePosition());
                    boolean atinge=g.slot==0?PerfilAsta.cone(d.normalize().dot(g.dir)):g.slot==1?d.normalize().dot(g.dir)>.75: true;
                    if(atinge&&p.hasLineOfSight(v))dano(g,v,g.slot==0?12:g.slot==1?16:10);
                }
            }else if(g.slot==0){for(var v:inimigos(p,p.position(),4))if(p.hasLineOfSight(v))dano(g,v,22);EfeitosDespertar.enviar(g.mundo,p.getUUID(),309,p.position(),Vec3.ZERO,20,4);}
            else if(g.slot==1){for(var v:inimigos(p,p.position(),8)){for(var efeito:List.copyOf(v.getActiveEffects()))if(efeito.getEffect().isBeneficial())v.removeEffect(efeito.getEffect());dano(g,v,8);}}
            else if(g.slot==2){
                Vec3 plano=g.dir.multiply(1,0,1).normalize();
                for(var v:inimigos(p,g.origem,20)){var d=v.position().subtract(g.origem);double frente=d.dot(plano),lado=d.subtract(plano.scale(frente)).length();if(frente>=0&&frente<=18&&lado<2.2&&p.hasLineOfSight(v))dano(g,v,32);}
                if(com.cauansilva.arcanecontrol.ConfigServidor.DESTRUIR.get())com.cauansilva.arcanecontrol.core.magia.DestruicaoAnime.fenda(p,g.origem,plano,18);
            }
        }
        for(var p:e.getServer().getPlayerList().getPlayers()){
            if(!p.isAlive()||ProgressaoPoderes.ler(p).ativo()!=PoderAnime.ASTA||p.hasEffect(RegistroAsta.SILENCIO.get()))continue;
            boolean barreira=RpgServidor.dados(p).getLong("asta_barreira")>RpgServidor.agora(p);
            if(barreira||defendendo(p))refletirProximos(p,barreira?4:2.8,!barreira);
        }
    }
    private static boolean defendendo(ServerPlayer p){return p.isUsingItem()&&p.getUseItem().is(RegistroAsta.ESPADA.get());}
    private static boolean pagarDefesa(ServerPlayer p){return RpgServidor.consumirMana(p,PoderAnime.ASTA,2);}
    public static boolean refletir(ServerPlayer p,Projectile proj,boolean cobrar){
        if(proj.getOwner()==p)return false;var d=proj.position().subtract(p.getEyePosition());
        if(d.lengthSqr()>1e-6&&!PerfilAsta.cone(d.normalize().dot(p.getLookAngle())))return false;
        var tag=proj.getPersistentData();long agora=RpgServidor.agora(p);
        if(!PerfilAsta.podeRefletir(tag.getInt("asta_reflexoes"),agora,tag.getLong("asta_refletir_apos"))||cobrar&&!pagarDefesa(p))return false;
        tag.putInt("asta_reflexoes",tag.getInt("asta_reflexoes")+1);tag.putLong("asta_refletir_apos",agora+10);
        Vec3 dir=p.getLookAngle();double velocidade=Math.max(1.2,Math.min(3,proj.getDeltaMovement().length()));proj.setOwner(p);proj.setDeltaMovement(dir.scale(velocidade));
        if(proj instanceof AbstractHurtingProjectile bola){bola.xPower=dir.x*.1;bola.yPower=dir.y*.1;bola.zPower=dir.z*.1;}
        proj.hurtMarked=true;EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),308,proj.position(),dir,12,4);return true;
    }
    private static void refletirProximos(ServerPlayer p,double r,boolean cobrar){for(var v:p.serverLevel().getEntitiesOfClass(Projectile.class,p.getBoundingBox().inflate(r),v->v.isAlive()&&v.getOwner()!=p).stream().limit(16).toList())if(p.hasLineOfSight(v))refletir(p,v,cobrar);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void defesa(LivingAttackEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive()||ProgressaoPoderes.ler(p).ativo()!=PoderAnime.ASTA||p.hasEffect(RegistroAsta.SILENCIO.get())||e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        boolean barreira=RpgServidor.dados(p).getLong("asta_barreira")>RpgServidor.agora(p);if(!barreira&&!defendendo(p))return;
        var fonte=e.getSource();boolean magia=fonte.is(DamageTypes.MAGIC)||fonte.is(DamageTypes.INDIRECT_MAGIC)||fonte.getDirectEntity() instanceof Projectile;
        Vec3 origem=fonte.getSourcePosition();if(!magia||origem==null||!PerfilAsta.cone(origem.subtract(p.getEyePosition()).normalize().dot(p.getLookAngle())))return;
        if(fonte.getDirectEntity() instanceof Projectile proj&&refletir(p,proj,!barreira)){e.setCanceled(true);return;}
        if(barreira||pagarDefesa(p)){e.setCanceled(true);EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),308,p.getEyePosition(),p.getLookAngle(),12,4);}
    }
    @SubscribeEvent public static void conjuracaoMuda(net.minecraftforge.event.entity.EntityJoinLevelEvent e){
        if(e.getLevel().isClientSide)return;
        if(e.getEntity() instanceof Projectile p&&(p instanceof net.minecraft.world.entity.projectile.ThrownPotion||p instanceof AbstractHurtingProjectile||p instanceof ShulkerBullet)
                &&p.getOwner() instanceof LivingEntity dono&&dono.hasEffect(RegistroAsta.SILENCIO.get()))e.setCanceled(true);
        if(e.getEntity() instanceof net.minecraft.world.entity.projectile.EvokerFangs f&&f.getOwner()!=null&&f.getOwner().hasEffect(RegistroAsta.SILENCIO.get()))e.setCanceled(true);
    }
    @SubscribeEvent public static void protegido(LivingHurtEvent e){if(e.getEntity() instanceof ServerPlayer p&&RpgServidor.dados(p).getLong("asta_protegido")>RpgServidor.agora(p)&&!e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))e.setAmount(e.getAmount()*.25f);}
    @SubscribeEvent public static void recuo(LivingKnockBackEvent e){if(e.getEntity() instanceof ServerPlayer p&&RpgServidor.dados(p).getLong("asta_protegido")>RpgServidor.agora(p))e.setCanceled(true);}
    public static void limpar(ServerPlayer p){RpgServidor.dados(p).remove("asta_barreira");RpgServidor.dados(p).remove("asta_protegido");if(!p.hasDisconnected())EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),0,p.position(),Vec3.ZERO,0,4);}
    @SubscribeEvent public static void sair(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)limpar(p);}
    @SubscribeEvent public static void dimensao(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)limpar(p);}
    @SubscribeEvent public static void morrer(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)limpar(p);}
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e){GOLPES.clear();}
}
