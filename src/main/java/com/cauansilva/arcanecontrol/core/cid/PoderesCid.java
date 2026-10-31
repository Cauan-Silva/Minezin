package com.cauansilva.arcanecontrol.core.cid;

import com.cauansilva.arcanecontrol.core.rede.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraft.sounds.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;

@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class PoderesCid {
    private record Atomic(ServerPlayer jogador, net.minecraft.server.level.ServerLevel nivel, Vec3 centro, long inicio) {}
    private static final List<Atomic> ATIVOS = new ArrayList<>();
    private record Corte(ServerPlayer p,net.minecraft.server.level.ServerLevel nivel,Vec3 olho,Vec3 dir,long inicio){}
    private record Fios(ServerPlayer p,net.minecraft.server.level.ServerLevel nivel,List<LivingEntity> alvos,long inicio){}
    private static final List<Corte> CORTES=new ArrayList<>();
    private static final List<Fios> FIOS=new ArrayList<>();
    private static final int[] RECARGAS = {40, 160, 240, 1800};

    public static net.minecraft.nbt.CompoundTag dados(ServerPlayer p) {
        var raiz=p.getPersistentData();
        String chave=net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG;
        if(!raiz.contains(chave)) raiz.put(chave,new net.minecraft.nbt.CompoundTag());
        var persistido=raiz.getCompound(chave);
        if(!persistido.contains("cid")) persistido.put("cid",new net.minecraft.nbt.CompoundTag());
        return persistido.getCompound("cid");
    }
    @SubscribeEvent public static void clonar(net.minecraftforge.event.entity.player.PlayerEvent.Clone event){
        if(event.getEntity() instanceof ServerPlayer destino && event.getOriginal() instanceof ServerPlayer origem){
            var copia=dados(origem).copy();copia.putLong("cid_ocupado",0);
            dados(destino).merge(copia);
        }
    }
    public static void executar(ServerPlayer p, int id) {
        if(!com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes.podeUsar(p,id))return;
        int slot = id - 11;
        if (slot < 0 || slot > 3 || !p.isAlive() || p.isSpectator()) return;
        if(!p.getMainHandItem().is(EspadaCid.ESPADA.get())){
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Segure a Lâmina Shadow para usar esse kit."),true);return;
        }
        long agora = p.server.overworld().getGameTime();
        var dados = dados(p);
        if (dados.getLong("cid_ocupado") > agora || dados.getLong("cid_cd_" + slot) > agora) {
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Shadow: técnica em recuperação."), true);
            return;
        }
        dados.putLong("cid_cd_" + slot, agora + com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes.recarga(p,
                com.cauansilva.arcanecontrol.core.poderes.PoderAnime.CID,slot,RECARGAS[slot]));
        Vec3 origem = p.position(), dir = p.getLookAngle();
        if (slot == 1) {
            Vec3 passo = new Vec3(dir.x, 0, dir.z).normalize().scale(.25);
            Vec3 destino = origem;
            for (int i=0; i<32; i++) {
                Vec3 proximo = destino.add(passo);
                if (!p.serverLevel().hasChunkAt(net.minecraft.core.BlockPos.containing(proximo))
                        || !p.serverLevel().getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(proximo))
                        || !p.serverLevel().noCollision(p, p.getBoundingBox().move(proximo.subtract(origem)))) break;
                destino = proximo;
            }
            p.connection.teleport(destino.x, destino.y, destino.z, p.getYRot(), p.getXRot());
            RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),
                    new PacoteSincronizarCutscene3D(16,p.getUUID(),destino.x,destino.y,destino.z,0,0,0));
        } else if (slot == 3) {
            dados.putLong("cid_ocupado", agora + 140);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,140,5,false,false));
            ATIVOS.add(new Atomic(p, p.serverLevel(), origem, agora));
        } else if(slot==0) CORTES.add(new Corte(p,p.serverLevel(),p.getEyePosition(),dir,agora));
        else FIOS.add(new Fios(p,p.serverLevel(),new ArrayList<>(),agora));
        RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p),
                new PacoteSincronizarCutscene3D(id, p.getUUID(), origem.x, origem.y, origem.z, dir.x, dir.y, dir.z));
        p.serverLevel().playSound(null, p.blockPosition(), slot == 3 ? SoundEvents.WARDEN_HEARTBEAT : SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.2f, .65f);
        com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes.registrarUso(p,
                com.cauansilva.arcanecontrol.core.poderes.PoderAnime.CID,slot,slot==3?140:slot==1?12:slot==2?48:25);
    }

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long agora=event.getServer().overworld().getGameTime();
        CORTES.removeIf(a->{
            if(!a.p.isAlive()||a.p.hasDisconnected()||a.p.serverLevel()!=a.nivel)return true;
            if(agora-a.inicio<com.cauansilva.arcanecontrol.core.vfx.PerfilCid.CORTE_IMPACTO)return false;
            for(var alvo:a.nivel.getEntitiesOfClass(LivingEntity.class,new AABB(a.olho,a.olho).inflate(5),v->v!=a.p&&v.isAlive())){
                Vec3 d=alvo.getEyePosition().subtract(a.olho);
                if(d.length()<=5&&d.normalize().dot(a.dir)>=Math.cos(com.cauansilva.arcanecontrol.core.vfx.PerfilCid.CORTE_ANGULO)&&a.p.hasLineOfSight(alvo))
                    alvo.hurt(a.p.damageSources().playerAttack(a.p),10);
            }
            return true;
        });
        var fios=FIOS.iterator();
        while(fios.hasNext()){
            var a=fios.next();long t=agora-a.inicio;
            if(!a.p.isAlive()||a.p.hasDisconnected()||a.p.serverLevel()!=a.nivel||t>=48){
                if(!a.p.hasDisconnected())RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->a.p),new PacoteAlvosCid(a.p.getUUID(),List.of()));
                fios.remove();continue;
            }
            if(t==com.cauansilva.arcanecontrol.core.vfx.PerfilCid.FIOS_IMPACTO){
                a.alvos.addAll(a.nivel.getEntitiesOfClass(LivingEntity.class,a.p.getBoundingBox().inflate(9),v->v!=a.p&&v.isAlive()
                        &&!v.isAlliedTo(a.p)&&v.distanceToSqr(a.p)<=81&&a.p.hasLineOfSight(v)).stream().sorted(Comparator.comparingDouble((LivingEntity v)->v.distanceToSqr(a.p))).limit(8).toList());
                for(var alvo:a.alvos){alvo.hurt(a.p.damageSources().playerAttack(a.p),5);alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,2));}
                RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->a.p),new PacoteAlvosCid(a.p.getUUID(),a.alvos.stream().map(v->v.getUUID()).toList()));
            }
            if(t>=8&&t<40)for(var alvo:a.alvos)if(alvo.level()==a.nivel&&alvo.isAlive()&&alvo.distanceToSqr(a.p)<=100&&a.p.hasLineOfSight(alvo)){
                Vec3 d=a.p.position().subtract(alvo.position());if(d.lengthSqr()>2.25){alvo.setDeltaMovement(d.normalize().scale(.3));alvo.hurtMarked=true;}
            }
        }
        Iterator<Atomic> it = ATIVOS.iterator();
        while (it.hasNext()) {
            Atomic a = it.next();
            if (!a.jogador.isAlive() || a.jogador.hasDisconnected() || a.jogador.serverLevel() != a.nivel) { it.remove(); continue; }
            if (a.jogador.distanceToSqr(a.centro)>9) {
                dados(a.jogador).putLong("cid_ocupado",0);
                RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> a.jogador),
                    new PacoteSincronizarCutscene3D(15,a.jogador.getUUID(),0,0,0,0,0,0));
                it.remove();continue;
            }
            long t = event.getServer().overworld().getGameTime() - a.inicio;
            if (t < 90 && t % 12 == 0) {
                a.nivel.playSound(null, a.jogador.blockPosition(), SoundEvents.WARDEN_HEARTBEAT,
                        SoundSource.PLAYERS, 1.8f, (float)(.55 + t / 130.0));
            }
            if (t == 90) {
                if (com.cauansilva.arcanecontrol.ConfigServidor.DESTRUIR.get()) {
                    a.nivel.explode(a.jogador, a.centro.x, a.centro.y, a.centro.z,
                            com.cauansilva.arcanecontrol.ConfigServidor.RAIO.get() / 2f, false,
                            net.minecraft.world.level.Level.ExplosionInteraction.TNT);
                }
                for (LivingEntity alvo : a.nivel.getEntitiesOfClass(LivingEntity.class, new AABB(a.centro,a.centro).inflate(14),
                        e -> e != a.jogador && e.isAlive())) {
                    if (alvo.distanceToSqr(a.centro) <= 196) {
                        alvo.hurt(a.nivel.damageSources().indirectMagic(a.jogador,a.jogador), 45);
                        alvo.setDeltaMovement(alvo.position().subtract(a.centro).normalize().scale(1.6).add(0,.6,0));
                        alvo.hurtMarked = true;
                    }
                }
                a.nivel.playSound(null, net.minecraft.core.BlockPos.containing(a.centro), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3, .5f);
            }
            if (t >= 140) it.remove();
        }
    }
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e) { ATIVOS.clear();CORTES.clear();FIOS.clear(); }
}
