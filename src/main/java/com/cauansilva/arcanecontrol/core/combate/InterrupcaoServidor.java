package com.cauansilva.arcanecontrol.core.combate;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Ponte Forge de {@link Interrupcao}: um registro do servidor por UUID. Kits registram canalizacoes e guardas por aqui;
 * qualquer golpe de uma entidade viva conta como LEVE, tecnicas marcadas chamam {@link #interromper} com FORTE/ABSOLUTA.
 * A guarda anula o golpe no LivingAttackEvent (antes do dano) e dispara o contra-ataque.
 */
@Mod.EventBusSubscriber(modid = "arcanecontrol")
public final class InterrupcaoServidor {
    private static final Interrupcao<UUID> SISTEMA = new Interrupcao<>();

    public static long agora(LivingEntity e) { return e.level().getServer() == null ? e.level().getGameTime() : e.level().getServer().overworld().getGameTime(); }

    public static void iniciar(LivingEntity dono, String tecnica, int duracao, Resistencia r, Consumer<Quebra<UUID>> cancelar) {
        if (!dono.level().isClientSide) SISTEMA.iniciar(dono.getUUID(), tecnica, agora(dono), duracao, r, cancelar);
    }
    public static void concluir(LivingEntity dono, String tecnica) { SISTEMA.concluir(dono.getUUID(), tecnica); }
    public static boolean canalizando(LivingEntity e) { return SISTEMA.canalizando(e.getUUID(), agora(e)); }
    public static void guardar(LivingEntity dono, String tecnica, int janela, Consumer<UUID> contra) {
        if (!dono.level().isClientSide) SISTEMA.guardar(dono.getUUID(), tecnica, agora(dono), janela, contra);
    }
    public static boolean guardando(LivingEntity e) { return SISTEMA.guardando(e.getUUID(), agora(e)); }

    /** Interrompe a canalizacao do alvo; mostra o efeito comum de quebra quando cai. */
    public static Resultado interromper(LivingEntity alvo, Object fonte, Forca f) {
        Resultado r = SISTEMA.interromper(alvo.getUUID(), fonte, f, agora(alvo));
        if (r == Resultado.INTERROMPIDO) quebra(alvo);
        return r;
    }

    /** Efeito comum de quebra (provisorio ate a previa propria do estilhaco azul e branco). */
    private static void quebra(LivingEntity e) {
        if (!(e.level() instanceof ServerLevel s)) return;
        s.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + e.getBbHeight() * .6, e.getZ(), 24, .35, .4, .35, .25);
        s.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY() + e.getBbHeight() * .6, e.getZ(), 12, .3, .3, .3, .4);
        s.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, .9f, 1.4f);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void atacado(LivingAttackEvent e) {
        LivingEntity def = e.getEntity();
        if (def.level().isClientSide || !(e.getSource().getEntity() instanceof LivingEntity atq) || atq == def) return;
        if (SISTEMA.receberGolpe(def.getUUID(), atq.getUUID(), agora(def)) == Defesa.CONTRA_ATAQUE) {
            e.setCanceled(true);
            quebra(atq);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void ferido(LivingHurtEvent e) {
        if (e.getEntity().level().isClientSide || e.getAmount() <= 0 || !(e.getSource().getEntity() instanceof LivingEntity atq)) return;
        if (atq != e.getEntity()) interromper(e.getEntity(), atq, Forca.LEVE);
    }
    @SubscribeEvent public static void morreu(LivingDeathEvent e) { SISTEMA.limpar(e.getEntity().getUUID()); }
    @SubscribeEvent public static void saiu(PlayerEvent.PlayerLoggedOutEvent e) { SISTEMA.limpar(e.getEntity().getUUID()); }
    @SubscribeEvent public static void parar(ServerStoppedEvent e) { SISTEMA.podar(Long.MAX_VALUE); }

    private InterrupcaoServidor() {}
}
