package com.cauansilva.arcanecontrol.core.rpg;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.*;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.UUID;

/** Desafio vinculado ao UUID salvo no jogador, inclusive quando o chefe descarrega. */
public final class NemesisEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> KIT=SynchedEntityData.defineId(NemesisEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CONJURANDO=SynchedEntityData.defineId(NemesisEntity.class,EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent barra=new ServerBossEvent(Component.literal("Nêmesis"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
    private UUID dono;
    private Vec3 arena=Vec3.ZERO,mira=Vec3.ZERO;
    private int carga,recuperacao,proximo=70;
    private static final EntityDataAccessor<Float> ESCALA=SynchedEntityData.defineId(NemesisEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> LIBERANDO=SynchedEntityData.defineId(NemesisEntity.class,EntityDataSerializers.BOOLEAN);
    public float escala(){return entityData.get(ESCALA);}
    public void configurarTamanho(int kit){entityData.set(KIT,kit);entityData.set(ESCALA,PerfilBoss.escala(kit,com.cauansilva.arcanecontrol.ConfigServidor.escalaChefe(kit)));refreshDimensions();}
    @Override public EntityDimensions getDimensions(Pose pose){return EntityDimensions.scalable(.85f,PerfilBoss.ALTURA_BASE).scale(escala());}
    @Override protected float getStandingEyeHeight(Pose pose,EntityDimensions size){return size.height*.88f;}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(ESCALA.equals(key))refreshDimensions();}
    public NemesisEntity(EntityType<? extends Monster> tipo,Level level){super(tipo,level);xpReward=60;setPersistenceRequired();}
    public static AttributeSupplier.Builder atributos(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,360).add(Attributes.MOVEMENT_SPEED,.29).add(Attributes.ATTACK_DAMAGE,10).add(Attributes.ARMOR,10).add(Attributes.KNOCKBACK_RESISTANCE,.8).add(Attributes.FOLLOW_RANGE,64);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(KIT,1);entityData.define(CONJURANDO,false);entityData.define(ESCALA,1f);entityData.define(LIBERANDO,false);}
    public int kit(){return entityData.get(KIT);}
    public void preparar(ServerPlayer p,int kit,Vec3 centro){dono=p.getUUID();configurarTamanho(kit);arena=centro;getAttribute(Attributes.MAX_HEALTH).setBaseValue(kit==1?360:kit==2?420:kit==3?460:480);setHealth(getMaxHealth());setCustomName(Component.literal(PersonagemRpg.porId(kit).nemesis));setTarget(p);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1,true){@Override public boolean canUse(){return carga==0&&recuperacao==0&&super.canUse();}@Override public boolean canContinueToUse(){return carga==0&&recuperacao==0&&super.canContinueToUse();}});goalSelector.addGoal(4,new LookAtPlayerGoal(this,net.minecraft.world.entity.player.Player.class,32));}
    private boolean valido(ServerPlayer p){return p!=null&&p.isAlive()&&!p.isSpectator()&&p.serverLevel()==level()&&p.distanceToSqr(arena)<64*64&&RpgServidor.dados(p).hasUUID("desafio")&&getUUID().equals(RpgServidor.dados(p).getUUID("desafio"));}
    @Override public void tick(){
        super.tick();if(level().isClientSide||!isAlive())return;
        var p=dono==null?null:((ServerLevel)level()).getServer().getPlayerList().getPlayer(dono);
        if(!valido(p)){if(p!=null&&RpgServidor.dados(p).hasUUID("desafio")&&getUUID().equals(RpgServidor.dados(p).getUUID("desafio")))RpgServidor.dados(p).remove("desafio");discard();return;}
        barra.setName(getDisplayName());barra.setProgress(getHealth()/getMaxHealth());setTarget(p);
        if(distanceToSqr(arena)>48*48){setPos(arena.x,arena.y,arena.z);getNavigation().stop();}
        if(hasEffect(com.cauansilva.arcanecontrol.core.asta.RegistroAsta.SILENCIO.get())){carga=0;recuperacao=0;entityData.set(CONJURANDO,false);entityData.set(LIBERANDO,false);proximo=Math.max(proximo,20);return;}
        if(recuperacao>0){getNavigation().stop();if(--recuperacao==0)entityData.set(LIBERANDO,false);return;}
        if(carga>0){
            getNavigation().stop();setDeltaMovement(getDeltaMovement().multiply(0,1,0));getLookControl().setLookAt(p);
            if(--carga==0){entityData.set(CONJURANDO,false);entityData.set(LIBERANDO,true);recuperacao=8;impacto(p);proximo=getHealth()<getMaxHealth()/2?65:105;}
        }else if(--proximo<=0){
            mira=p.position();carga=kit()==1?18:kit()==2?26:kit()==4?22:30;entityData.set(CONJURANDO,true);
            EfeitosDespertar.enviar((ServerLevel)level(),getUUID(),400+kit(),position(),mira,carga,kit());
        }
    }
    private void impacto(ServerPlayer p){
        var l=(ServerLevel)level();
        if(kit()==1){
            var inicio=position();var d=mira.subtract(inicio).multiply(1,0,1);double comprimento=Math.min(12,d.length());var dir=d.normalize();var destino=inicio;
            for(double t=.25;t<=comprimento;t+=.25){var q=inicio.add(dir.scale(t));if(!l.hasChunkAt(net.minecraft.core.BlockPos.containing(q))||!l.getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(q))||!l.noCollision(this,getBoundingBox().move(q.subtract(inicio))))break;destino=q;}
            if(p.getBoundingBox().inflate(.7).clip(inicio.add(0,1,0),destino.add(0,1,0)).isPresent())p.hurt(damageSources().mobAttack(this),18);setPos(destino.x,destino.y,destino.z);
            EfeitosDespertar.enviar(l,getUUID(),411,inicio.add(0,1,0),destino.add(0,1,0),12,kit());
        }else if(kit()==2){
            for(int i=0;i<8;i++){double a=i*Math.PI/4;var c=mira.add(Math.cos(a)*3,0,Math.sin(a)*3);if(p.distanceToSqr(c)<5&&hasLineOfSight(p))p.hurt(damageSources().mobAttack(this),16);l.sendParticles(ParticleTypes.CRIMSON_SPORE,c.x,c.y+1,c.z,12,.4,1,.4,.04);}
            if(p.distanceToSqr(mira)<9&&hasLineOfSight(p))p.hurt(damageSources().mobAttack(this),16);
            EfeitosDespertar.enviar(l,getUUID(),412,mira,mira.add(0,5,0),18,kit());
        }else if(kit()==4){
            for(var v:l.getEntitiesOfClass(LivingEntity.class,new AABB(mira,mira).inflate(5),v->v.isAlive()&&v==p))if(hasLineOfSight(v)){
                v.hurt(damageSources().mobAttack(this),20);v.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.cauansilva.arcanecontrol.core.asta.RegistroAsta.SILENCIO.get(),60,0));
            }
            EfeitosDespertar.enviar(l,getUUID(),414,mira,new Vec3(0,1,0),24,4);
        }else{
            var inicio=getEyePosition();var fim=inicio.add(mira.add(0,1,0).subtract(inicio).normalize().scale(40));
            var bloco=l.clip(new net.minecraft.world.level.ClipContext(inicio,fim,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));fim=bloco.getLocation();
            if(p.getBoundingBox().inflate(.4).clip(inicio,fim).isPresent())p.hurt(damageSources().indirectMagic(this,this),22);
            EfeitosDespertar.enviar(l,getUUID(),413,inicio,fim,20,kit());
        }
    }
    @Override public boolean hurt(DamageSource s,float a){if(kit()==3&&entityData.get(CONJURANDO))a*=.5f;return super.hurt(s,a);}
    @Override public void die(DamageSource fonte){
        if(!level().isClientSide){var p=dono==null?null:((ServerLevel)level()).getServer().getPlayerList().getPlayer(dono);
            if(valido(p)){RpgServidor.dados(p).remove("desafio");var alma=AlmaDespertar.criar(kit());if(!p.getInventory().add(alma))p.drop(alma,false);p.displayClientMessage(Component.literal("Desafio vencido! Use a Alma do Despertar com o kit correspondente."),false);}
        }super.die(fonte);barra.removeAllPlayers();
    }
    @Override public void remove(RemovalReason r){barra.removeAllPlayers();super.remove(r);}
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);barra.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);barra.removePlayer(p);}
    @Override protected boolean shouldDespawnInPeaceful(){return false;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);if(dono!=null)t.putUUID("dono",dono);t.putInt("kit",kit());t.putDouble("ax",arena.x);t.putDouble("ay",arena.y);t.putDouble("az",arena.z);}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);dono=t.hasUUID("dono")?t.getUUID("dono"):null;configurarTamanho(Math.max(1,Math.min(4,t.getInt("kit"))));arena=new Vec3(t.getDouble("ax"),t.getDouble("ay"),t.getDouble("az"));}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar r){r.add(new AnimationController<>(this,"corpo",0,s->s.setAndContinue(entityData.get(LIBERANDO)?RawAnimation.begin().thenPlay("release"):entityData.get(CONJURANDO)?RawAnimation.begin().thenPlay("cast"):RawAnimation.begin().thenLoop(s.isMoving()?"walk":"idle"))));}
}
