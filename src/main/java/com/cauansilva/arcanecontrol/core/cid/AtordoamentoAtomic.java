package com.cauansilva.arcanecontrol.core.cid;

import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Server-authoritative stun during Atomic charge; owned AI state is restored on every exit path. */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class AtordoamentoAtomic {
    public static final double RAIO=14;
    private static final String AI="minezin_atomic_ai_original";
    private static final UUID MOVIMENTO=UUID.fromString("8d8f4b9a-2f27-4e16-954b-882aeb1bb835");
    private record Alvo(LivingEntity entidade,ServerLevel mundo,Vec3 ancora,boolean noAi,FontesStunAtomic fontes){}
    private static final Map<UUID,Alvo> ALVOS=new HashMap<>();
    public static boolean ativo(LivingEntity e){var a=ALVOS.get(e.getUUID());return a!=null&&a.entidade==e&&e.level()==a.mundo&&a.fontes.ativa(a.mundo.getServer().overworld().getGameTime());}
    private static boolean inimigo(ServerPlayer dono,LivingEntity e){
        if(e==dono||!e.isAlive()||e.isAlliedTo(dono)||e.isSpectator())return false;
        if(e instanceof ServerPlayer p)return !p.isCreative()&&p.server.isPvpAllowed();
        return e instanceof Enemy||e instanceof Mob m&&m.getTarget()==dono;
    }
    public static void aplicar(ServerPlayer dono,Vec3 centro){
        var mundo=dono.serverLevel();long agora=mundo.getServer().overworld().getGameTime();
        for(var e:mundo.getEntitiesOfClass(LivingEntity.class,new AABB(centro,centro).inflate(RAIO),v->inimigo(dono,v)&&v.distanceToSqr(centro)<=RAIO*RAIO)){
            var a=ALVOS.get(e.getUUID());
            if(a!=null&&a.entidade!=e){restaurar(a);ALVOS.remove(e.getUUID());a=null;}
            if(a==null){
                boolean old=e instanceof Mob m&&m.isNoAi();a=new Alvo(e,mundo,e.position(),old,new FontesStunAtomic());ALVOS.put(e.getUUID(),a);
                if(e instanceof Mob m){m.getPersistentData().putBoolean(AI,old);m.setNoAi(true);m.getNavigation().stop();}
                for(var atributo:List.of(Attributes.MOVEMENT_SPEED,Attributes.FLYING_SPEED)){
                    var valor=e.getAttribute(atributo);if(valor!=null&&valor.getModifier(MOVIMENTO)==null)valor.addTransientModifier(new AttributeModifier(MOVIMENTO,"Atomic stun",-1,AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
            a.fontes.renovar(dono.getUUID(),agora);
        }
    }
    private static void restaurar(Alvo a){
        var e=a.entidade;if(e instanceof Mob m){m.setNoAi(a.noAi);m.getPersistentData().remove(AI);}
        for(var atributo:List.of(Attributes.MOVEMENT_SPEED,Attributes.FLYING_SPEED)){var valor=e.getAttribute(atributo);if(valor!=null)valor.removeModifier(MOVIMENTO);}
    }
    public static void liberar(UUID dono){
        var it=ALVOS.values().iterator();while(it.hasNext()){var a=it.next();a.fontes.remover(dono);if(!a.fontes.ativa(a.mundo.getServer().overworld().getGameTime())){restaurar(a);it.remove();}}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void tick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;long agora=event.getServer().overworld().getGameTime();
        var it=ALVOS.values().iterator();while(it.hasNext()){
            var a=it.next();var e=a.entidade;
            if(!e.isAlive()||e.isRemoved()||e.level()!=a.mundo||!a.fontes.ativa(agora)){restaurar(a);it.remove();continue;}
            e.stopUsingItem();e.setDeltaMovement(Vec3.ZERO);e.fallDistance=0;e.hurtMarked=true;
            if(e.position().distanceToSqr(a.ancora)>.0001){
                if(e instanceof ServerPlayer p)p.connection.teleport(a.ancora.x,a.ancora.y,a.ancora.z,p.getYRot(),p.getXRot());
                else e.setPos(a.ancora.x,a.ancora.y,a.ancora.z);
            }
            if(e instanceof Mob m){m.getNavigation().stop();m.setNoAi(true);}
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void atacar(LivingAttackEvent e){if(e.getSource().getEntity() instanceof LivingEntity dono&&ativo(dono))e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void clique(AttackEntityEvent e){if(ativo(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void interagir(PlayerInteractEvent e){if(!e.getLevel().isClientSide&&e.isCancelable()&&ativo(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void quebrar(BlockEvent.BreakEvent e){if(ativo(e.getPlayer()))e.setCanceled(true);}
    @SubscribeEvent public static void pular(LivingEvent.LivingJumpEvent e){if(ativo(e.getEntity()))e.getEntity().setDeltaMovement(Vec3.ZERO);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void entrar(EntityJoinLevelEvent e){
        if(e.getLevel().isClientSide)return;
        if(e.getEntity() instanceof Mob mob&&mob.getPersistentData().contains(AI)){mob.setNoAi(mob.getPersistentData().getBoolean(AI));mob.getPersistentData().remove(AI);}
        if(e.getEntity() instanceof Projectile p&&p.getOwner() instanceof LivingEntity dono&&ativo(dono))e.setCanceled(true);
    }
    @SubscribeEvent public static void parar(ServerStoppedEvent e){ALVOS.values().forEach(AtordoamentoAtomic::restaurar);ALVOS.clear();}
    private AtordoamentoAtomic(){}
}
