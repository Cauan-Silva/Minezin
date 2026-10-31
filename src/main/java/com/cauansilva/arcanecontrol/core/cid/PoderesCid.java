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
        int slot = id - 11;
        if (slot < 0 || slot > 3 || !p.isAlive() || p.isSpectator()
                || !p.getMainHandItem().is(EspadaCid.ESPADA.get())) return;
        long agora = p.server.overworld().getGameTime();
        var dados = dados(p);
        if (dados.getLong("cid_ocupado") > agora || dados.getLong("cid_cd_" + slot) > agora) {
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Shadow: técnica em recuperação."), true);
            return;
        }
        dados.putLong("cid_cd_" + slot, agora + RECARGAS[slot]);
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
        } else if (slot == 3) {
            dados.putLong("cid_ocupado", agora + 140);
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,140,5,false,false));
            ATIVOS.add(new Atomic(p, p.serverLevel(), origem, agora));
        } else {
            double raio = slot == 0 ? 5 : 9;
            for (LivingEntity alvo : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(raio), e -> e != p && e.isAlive())) {
                Vec3 delta = alvo.getEyePosition().subtract(p.getEyePosition());
                if (delta.length() > raio || !p.hasLineOfSight(alvo) || (slot == 0 && delta.normalize().dot(dir) < .25)) continue;
                alvo.hurt(p.damageSources().playerAttack(p), slot == 0 ? 10 : 5);
                if (slot == 2) {
                    alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                    alvo.setDeltaMovement(origem.subtract(alvo.position()).normalize().scale(.4));
                    alvo.hurtMarked = true;
                }
            }
        }
        RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p),
                new PacoteSincronizarCutscene3D(id, p.getUUID(), origem.x, origem.y, origem.z, dir.x, dir.y, dir.z));
        p.serverLevel().playSound(null, p.blockPosition(), slot == 3 ? SoundEvents.WARDEN_HEARTBEAT : SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.2f, .65f);
    }

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
            if (t == 90) {
                for (LivingEntity alvo : a.nivel.getEntitiesOfClass(LivingEntity.class, new AABB(a.centro,a.centro).inflate(14),
                        e -> e != a.jogador && e.isAlive())) {
                    if (alvo.distanceToSqr(a.centro) <= 196) alvo.hurt(a.nivel.damageSources().indirectMagic(a.jogador,a.jogador), 45);
                }
                a.nivel.playSound(null, net.minecraft.core.BlockPos.containing(a.centro), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3, .5f);
            }
            if (t >= 140) it.remove();
        }
    }
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e) { ATIVOS.clear(); }
}
