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
import com.cauansilva.arcanecontrol.core.vfx.UltAsta;
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
        LivingEntity alvo=forma&&slot==0?alvo(p,20):forma&&slot==3?alvo(p,UltAsta.ALCANCE_ALVO):null;
        // The ultimate locks on: its frame (and the client's) faces the target, so every cut lands on it.
        if(forma&&slot==3&&alvo!=null){Vec3 a=alvo.position().subtract(p.position()).multiply(1,0,1);if(a.lengthSqr()>1e-4)dir=a.normalize();}
        if(forma&&slot==0&&alvo==null){p.displayClientMessage(net.minecraft.network.chat.Component.literal("Mire em um inimigo a até vinte blocos para o Black Meteorite."),true);return;}
        if(!RpgServidor.conjurar(p,PoderAnime.ASTA,slot,PerfilAsta.recarga(slot,forma)))return;
        if(!forma&&slot==3)RpgServidor.dados(p).putLong("asta_barreira",RpgServidor.agora(p)+PerfilAsta.BARREIRA);
        else GOLPES.add(new Golpe(p,p.serverLevel(),p.position(),dir,RpgServidor.agora(p),slot,forma,alvo,new HashSet<>()));
        EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),300+(forma?4:0)+slot,p.position(),dir,!forma&&slot==3?100:forma&&slot==3?UltAsta.DURACAO:32,4);
        // Sound: the client plays Asta's own cues on the VFX ticks (RoteiroAudio.asta).
        if(forma||slot!=3)ProgressaoPoderes.bloquear(p,forma&&slot==3?UltAsta.DURACAO:32);
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
    private static Vec3 mundo(Vec3 origem,Vec3 frente,Vec3 lado,UltAsta.P q){return origem.add(lado.scale(q.x())).add(0,q.y(),0).add(frente.scale(q.z()));}
    /** Ultimate (V in the form), the same frame and ticks UltAsta draws: protected throughout, dash 30..58, a small hit per
     *  slash on its tick (i-frames cleared so every slash lands), then the final X: strong damage and Silence. */
    private static void ult(Golpe g,int t){
        var p=g.p;Vec3 f0=g.dir.multiply(1,0,1);final Vec3 frente=f0.lengthSqr()<1e-6?new Vec3(0,0,1):f0.normalize(),lado=frente.cross(new Vec3(0,1,0));
        RpgServidor.dados(p).putLong("asta_protegido",RpgServidor.agora(p)+2);
        LivingEntity alvo=g.alvo!=null&&g.alvo.isAlive()&&g.alvo.level()==g.mundo?g.alvo:null;
        if(t>30&&t<=58){
            // Locked: close in until PARADA blocks from the target; otherwise the fixed forward dash.
            if(alvo!=null){Vec3 d=alvo.position().subtract(p.position()).multiply(1,0,1);double dist=d.length();if(dist>UltAsta.PARADA+.05)mover(p,d.normalize().scale(Math.min(UltAsta.PASSO_AVANCO,dist-UltAsta.PARADA)));}
            else mover(p,frente.scale(UltAsta.avanco(t)-UltAsta.avanco(t-1)));
        }
        // Announce each cut (and the X) where it will land, 3 ticks early so every viewer draws the full sweep there.
        for(int j=0;j<=UltAsta.CORTES;j++)if(t==UltAsta.anuncio(j))EfeitosDespertar.enviar(g.mundo,p.getUUID(),311,centroUlt(g,alvo,frente,lado,j),new Vec3(j,0,0),12,4);
        for(int j=0;j<UltAsta.CORTES;j++)if(t==UltAsta.tempoCorte(j))for(var v:inimigos(p,centroUlt(g,alvo,frente,lado,j),3))if(p.hasLineOfSight(v)){v.invulnerableTime=0;v.hurt(p.damageSources().playerAttack(p),UltAsta.DANO_CORTE);}
        if(t==UltAsta.FINAL)for(var v:inimigos(p,centroUlt(g,alvo,frente,lado,UltAsta.CORTES),UltAsta.ALCANCE_X))if(p.hasLineOfSight(v)){
            v.invulnerableTime=0;v.hurt(p.damageSources().playerAttack(p),UltAsta.DANO_X);v.addEffect(new MobEffectInstance(RegistroAsta.SILENCIO.get(),PerfilAsta.SILENCIO,0));
        }
    }
    /** Center of cut j (j = CORTES: the X): on the locked target as it moves, or the fixed spot ahead without one. */
    private static Vec3 centroUlt(Golpe g,LivingEntity alvo,Vec3 frente,Vec3 lado,int j){
        if(alvo!=null)return alvo.position().add(0,j<UltAsta.CORTES?UltAsta.ALTURA_CORTE:UltAsta.ALTURA_X,0);
        return mundo(g.origem,frente,lado,j<UltAsta.CORTES?UltAsta.centroCorte(j):UltAsta.centroX());
    }
    private static void dano(Golpe g,LivingEntity v,float dano){if(g.atingidos.add(v.getUUID()))v.hurt(g.p.damageSources().playerAttack(g.p),dano);}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var it=GOLPES.iterator();while(it.hasNext()){
            var g=it.next();int t=(int)(RpgServidor.agora(g.p)-g.inicio);
            if(!valido(g)||t>(g.forma&&g.slot==3?UltAsta.DURACAO:32)){it.remove();continue;}
            var p=g.p;int impacto=PerfilAsta.impacto(g.slot,g.forma);
            if(!g.forma&&g.slot==0&&t>=3&&t<=10)refletirProximos(p,4,false);
            if(!g.forma&&g.slot==2&&t<=10){mover(p,g.dir.multiply(1,0,1).normalize().scale(.65));RpgServidor.dados(p).putLong("asta_protegido",RpgServidor.agora(p)+2);}
            if(g.forma&&g.slot==0&&t<12&&g.alvo!=null&&g.alvo.isAlive()&&g.alvo.level()==g.mundo){var d=g.alvo.position().subtract(p.position());mover(p,d.normalize().scale(Math.min(.9,d.length())));RpgServidor.dados(p).putLong("asta_protegido",RpgServidor.agora(p)+2);}
            if(g.forma&&g.slot==3){ult(g,t);continue;}
            if(t!=impacto)continue;
            if(!g.forma){
                for(var v:inimigos(p,p.position(),g.slot==0?5:g.slot==1?7:3)){
                    var d=v.getEyePosition().subtract(p.getEyePosition());
                    boolean atinge=g.slot==0?PerfilAsta.cone(d.normalize().dot(g.dir)):g.slot==1?d.normalize().dot(g.dir)>.75: true;
                    if(atinge&&p.hasLineOfSight(v))dano(g,v,g.slot==0?10:g.slot==1?12:8);
                }
            }else if(g.slot==0){for(var v:inimigos(p,p.position(),4))if(p.hasLineOfSight(v))dano(g,v,14);EfeitosDespertar.enviar(g.mundo,p.getUUID(),309,p.position(),Vec3.ZERO,20,4);}
            else if(g.slot==1){for(var v:inimigos(p,p.position(),8)){for(var efeito:List.copyOf(v.getActiveEffects()))if(efeito.getEffect().isBeneficial())v.removeEffect(efeito.getEffect());dano(g,v,6);}}
            else if(g.slot==2){
                Vec3 plano=g.dir.multiply(1,0,1).normalize();
                for(var v:inimigos(p,g.origem,20)){var d=v.position().subtract(g.origem);double frente=d.dot(plano),lado=d.subtract(plano.scale(frente)).length();if(frente>=0&&frente<=18&&lado<2.2&&p.hasLineOfSight(v))dano(g,v,20);}
                if(com.cauansilva.arcanecontrol.ConfigServidor.DESTRUIR.get())com.cauansilva.arcanecontrol.core.magia.DestruicaoAnime.fenda(p,g.origem,plano,18);
            }
        }
        for(var p:e.getServer().getPlayerList().getPlayers()){
            if(!p.isAlive()||ProgressaoPoderes.ler(p).ativo()!=PoderAnime.ASTA||p.hasEffect(RegistroAsta.SILENCIO.get()))continue;
            boolean barreira=RpgServidor.dados(p).getLong("asta_barreira")>RpgServidor.agora(p);
            if(barreira||defendendo(p))refletirProximos(p,barreira?4:2.8,!barreira);
            if(DespertaresServidor.ativo(p))campoAntimagia(p);
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
    /** Balance 3.11: anti-magic field of the Devil Union (user: nullify every skill that enters 5 blocks of him).
     *  Every enemy inside is silenced while it stays (cannot cast, its casting is broken, its magic projectiles
     *  fizzle), foreign projectiles inside are destroyed, and magic damage to Asta or allies inside is cancelled. */
    public static final double RAIO_ANTIMAGIA=5;
    static boolean dentroDoCampo(ServerPlayer asta,net.minecraft.world.entity.Entity e){return e.level()==asta.level()&&e.distanceToSqr(asta)<=RAIO_ANTIMAGIA*RAIO_ANTIMAGIA;}
    private static void campoAntimagia(ServerPlayer p){
        if(p.tickCount%5!=0)return;var caixa=p.getBoundingBox().inflate(RAIO_ANTIMAGIA);
        for(var v:p.serverLevel().getEntitiesOfClass(LivingEntity.class,caixa,v->v!=p&&v.isAlive()&&!v.isAlliedTo(p)&&dentroDoCampo(p,v)))
            v.addEffect(new MobEffectInstance(RegistroAsta.SILENCIO.get(),10,0,false,false));
        for(var proj:p.serverLevel().getEntitiesOfClass(Projectile.class,caixa,v->v.isAlive()&&dentroDoCampo(p,v)&&v.getOwner()!=p&&!(v.getOwner()!=null&&v.getOwner().isAlliedTo(p))))proj.discard();
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void anularMagia(LivingAttackEvent e){
        var fonte=e.getSource();if(!(fonte.is(DamageTypes.MAGIC)||fonte.is(DamageTypes.INDIRECT_MAGIC))||fonte.is(DamageTypeTags.BYPASSES_INVULNERABILITY)||e.getEntity().level().isClientSide)return;
        var alvo=e.getEntity();
        for(var p:alvo.getServer().getPlayerList().getPlayers())
            if(p.isAlive()&&ProgressaoPoderes.ler(p).ativo()==PoderAnime.ASTA&&DespertaresServidor.ativo(p)&&!p.hasEffect(RegistroAsta.SILENCIO.get())
                    &&(alvo==p||alvo.isAlliedTo(p))&&dentroDoCampo(p,alvo)&&fonte.getEntity()!=p&&!(fonte.getEntity()!=null&&fonte.getEntity().isAlliedTo(p))){e.setCanceled(true);return;}
    }
    /** Balance 3.11: Asta is the tank; in the Devil Union he takes REDUCAO_FORMA less damage. */
    public static final float REDUCAO_FORMA=.35f;
    @SubscribeEvent public static void protegido(LivingHurtEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        if(RpgServidor.dados(p).getLong("asta_protegido")>RpgServidor.agora(p))e.setAmount(e.getAmount()*.25f);
        else if(ProgressaoPoderes.ler(p).ativo()==PoderAnime.ASTA&&DespertaresServidor.ativo(p))e.setAmount(e.getAmount()*(1-REDUCAO_FORMA));
    }
    @SubscribeEvent public static void recuo(LivingKnockBackEvent e){if(e.getEntity() instanceof ServerPlayer p&&RpgServidor.dados(p).getLong("asta_protegido")>RpgServidor.agora(p))e.setCanceled(true);}
    public static void limpar(ServerPlayer p){RpgServidor.dados(p).remove("asta_barreira");RpgServidor.dados(p).remove("asta_protegido");if(!p.hasDisconnected())EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),0,p.position(),Vec3.ZERO,0,4);}
    @SubscribeEvent public static void sair(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)limpar(p);}
    @SubscribeEvent public static void dimensao(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)limpar(p);}
    @SubscribeEvent public static void morrer(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)limpar(p);}
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e){GOLPES.clear();}
}
