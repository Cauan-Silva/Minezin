package com.cauansilva.arcanecontrol.core.frieren;

import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rede.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraft.sounds.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;
import com.cauansilva.arcanecontrol.core.vfx.PerfilFrieren;

@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class PoderesFrieren {
    private record Singularidade(ServerPlayer jogador,ServerLevel nivel,Vec3 centro,long inicio,boolean silencioso){}
    private static final List<Singularidade> ATIVOS=new ArrayList<>();
    private static final int[] RECARGAS={80,240,200,500};
    private static void enviar(ServerPlayer p,int tipo,Vec3 origem,Vec3 alvo){
        RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),
                new PacoteSincronizarCutscene3D(tipo,p.getUUID(),origem.x,origem.y,origem.z,alvo.x,alvo.y,alvo.z));
    }
    public static void executar(ServerPlayer p,int id){executar(p,id,false);}
    public static void buracoDespertado(ServerPlayer p){executar(p,24,true);}
    private static void executar(ServerPlayer p,int id,boolean despertado){
        if(!ProgressaoPoderes.podeUsar(p,id))return;
        int slot=id-21;if(slot<0||slot>3)return;
        var tag=ProgressaoPoderes.tag(p);long agora=ProgressaoPoderes.agora(p);
        Vec3 origem=p.getEyePosition(),dir=p.getLookAngle(),alvo=origem.add(dir.scale(slot==0?32:12));
        var level=p.serverLevel();
        if(slot==0||slot==3){
            if(!level.hasChunkAt(BlockPos.containing(alvo))||!level.getWorldBorder().isWithinBounds(BlockPos.containing(alvo)))return;
            var bloco=level.clip(new ClipContext(origem,alvo,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            if(bloco.getType()!=HitResult.Type.MISS)alvo=bloco.getLocation();
        }
        if(slot==3){
            if(ATIVOS.stream().anyMatch(a->a.jogador==p))return;
            if(alvo.distanceToSqr(p.position())<9){p.displayClientMessage(net.minecraft.network.chat.Component.literal("Mire em uma área mais distante para formar a singularidade."),true);return;}
        }
        if(!com.cauansilva.arcanecontrol.core.rpg.RpgServidor.conjurar(p,PoderAnime.FRIEREN,despertado?2:slot,RECARGAS[slot]))return;
        if(slot==0){
            Vec3 fim=alvo;
            var hit=ProjectileUtil.getEntityHitResult(p,origem,fim,p.getBoundingBox().expandTowards(fim.subtract(origem)).inflate(1),
                    e->e instanceof LivingEntity&&e!=p&&e.isAlive()&&!e.isAlliedTo(p),origem.distanceToSqr(fim));
            if(hit!=null){alvo=hit.getLocation();hit.getEntity().hurt(level.damageSources().indirectMagic(p,p),10);}
            Vec3 origemVisual=origem;
            var cajado=com.cauansilva.arcanecontrol.core.equipamentos.RegistroTrajes.CAJADO_FRIEREN.get();
            boolean direita=p.getMainHandItem().is(cajado),esquerda=p.getOffhandItem().is(cajado);
            if(direita||esquerda){
                Vec3 lado=dir.cross(new Vec3(0,1,0)).normalize();
                boolean maoDireita=direita==(p.getMainArm()==net.minecraft.world.entity.HumanoidArm.RIGHT);
                Vec3 foco=origem.add(dir.scale(.95)).add(lado.scale(maoDireita?.35:-.35)).add(0,-.2,0);
                var limite=level.clip(new ClipContext(origem,foco,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
                origemVisual=limite.getType()==HitResult.Type.MISS?foco:limite.getLocation().subtract(dir.scale(.03));
            }
            enviar(p,21,origemVisual,alvo);
            level.playSound(null,p.blockPosition(),SoundEvents.AMETHYST_BLOCK_RESONATE,SoundSource.PLAYERS,1.5f,1.6f);
        } else if(slot==1){
            tag.putLong("fri_barreira_ate",agora+80);tag.putFloat("fri_barreira_reserva",30);
            enviar(p,22,p.position(),dir);
            level.playSound(null,p.blockPosition(),SoundEvents.BEACON_ACTIVATE,SoundSource.PLAYERS,1.3f,1.4f);
        } else if(slot==2){
            p.setDeltaMovement(dir.x*.9,Math.max(.7,dir.y*.9+.4),dir.z*.9);p.hurtMarked=true;
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,100,0,false,false));
            enviar(p,23,p.position(),dir);
            level.playSound(null,p.blockPosition(),SoundEvents.ELYTRA_FLYING,SoundSource.PLAYERS,1,.8f);
        } else {
            ATIVOS.add(new Singularidade(p,level,alvo,agora,despertado));
            if(!com.cauansilva.arcanecontrol.ConfigServidor.BURACO_DESTRUIR.get())p.displayClientMessage(net.minecraft.network.chat.Component.literal("Destruição de terreno desativada neste mundo."),true);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,PerfilFrieren.ABERTURA,2,false,false));
            enviar(p,24,alvo,dir);
            level.playSound(null,BlockPos.containing(alvo),SoundEvents.BEACON_POWER_SELECT,SoundSource.PLAYERS,2,.5f);
        }
        ProgressaoPoderes.registrarUso(p,PoderAnime.FRIEREN,slot,new int[]{30,80,100,PerfilFrieren.FIM}[slot]);
    }
    @SubscribeEvent public static void proteger(LivingHurtEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getSource().getEntity()==null)return;
        var t=ProgressaoPoderes.tag(p);
        if(t.getLong("fri_barreira_ate")<=ProgressaoPoderes.agora(p))return;
        float reserva=t.getFloat("fri_barreira_reserva"),bloqueado=Math.min(reserva,e.getAmount()*.7f);
        if(bloqueado<=0)return;
        e.setAmount(e.getAmount()-bloqueado);reserva-=bloqueado;t.putFloat("fri_barreira_reserva",reserva);
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.AMETHYST_BLOCK_HIT,SoundSource.PLAYERS,1,1.2f);
        if(reserva<=0){t.putLong("fri_barreira_ate",0);enviar(p,26,p.position(),Vec3.ZERO);}
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var it=ATIVOS.iterator();
        while(it.hasNext()){
            var a=it.next();var p=a.jogador;
            if(!p.isAlive()||p.hasDisconnected()||p.serverLevel()!=a.nivel||p.distanceToSqr(a.centro)>48*48||(a.silencioso&&!com.cauansilva.arcanecontrol.core.rpg.DespertaresServidor.ativo(p))){
                if(!p.hasDisconnected())enviar(p,25,a.centro,Vec3.ZERO);it.remove();continue;
            }
            long t=ProgressaoPoderes.agora(p)-a.inicio;
            if(t==PerfilFrieren.ABERTURA)a.nivel.playSound(null,BlockPos.containing(a.centro),SoundEvents.WARDEN_SONIC_BOOM,SoundSource.PLAYERS,2,.55f);
            if(t>=PerfilFrieren.ABERTURA&&t<PerfilFrieren.COLAPSO){
                if(t%2==0&&com.cauansilva.arcanecontrol.ConfigServidor.BURACO_DESTRUIR.get())com.cauansilva.arcanecontrol.core.magia.DestruicaoAnime.atrairBlocos(p,a.centro,t);
                for(var alvo:a.nivel.getEntities(p,new AABB(a.centro,a.centro).inflate(16),
                        v->(v instanceof LivingEntity||v instanceof Projectile)&&!v.isAlliedTo(p))){
                    Vec3 delta=a.centro.subtract(alvo.position().add(0,alvo.getBbHeight()*.5,0));double dist=delta.length();
                    if(dist>16)continue;
                    alvo.setDeltaMovement(alvo.getDeltaMovement().scale(.25).add(delta.normalize().scale(Math.min(1.3,.25+4/Math.max(1,dist)))));
                    alvo.hurtMarked=true;
                    if(alvo instanceof LivingEntity vivo&&dist<4&&t%10==0)vivo.hurt(a.nivel.damageSources().indirectMagic(p,p),6);
                }
            }
            if(t==PerfilFrieren.COLAPSO){
                if(com.cauansilva.arcanecontrol.ConfigServidor.BURACO_DESTRUIR.get()){
                    com.cauansilva.arcanecontrol.core.magia.DestruicaoAnime.cratera(p,a.centro,14,9);
                    if(!a.silencioso)a.nivel.explode(p,a.centro.x,a.centro.y,a.centro.z,5f,false,net.minecraft.world.level.Level.ExplosionInteraction.TNT);
                }
                for(var alvo:a.nivel.getEntitiesOfClass(LivingEntity.class,new AABB(a.centro,a.centro).inflate(8),v->v!=p&&!v.isAlliedTo(p))) {
                    if(alvo.distanceToSqr(a.centro)>64)continue;
                    alvo.hurt(a.nivel.damageSources().indirectMagic(p,p),25);
                    alvo.setDeltaMovement(alvo.position().subtract(a.centro).normalize().scale(1.5).add(0,.6,0));alvo.hurtMarked=true;
                }
                if(!a.silencioso)a.nivel.playSound(null,BlockPos.containing(a.centro),SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,3,.6f);
            }
            if(t>=PerfilFrieren.FIM)it.remove();
        }
    }
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e){ATIVOS.clear();}
}
