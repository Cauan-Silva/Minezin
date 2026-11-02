package com.cauansilva.arcanecontrol.core.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Gestos de tronco/braços: as pernas continuam com a locomoção vanilla. */
@Mod.EventBusSubscriber(modid = "arcanecontrol", value = Dist.CLIENT)
public final class AnimacoesGojo {
    private static final ResourceLocation CAMADA = new ResourceLocation("arcanecontrol", "gojo_gestos");
    private static final String[] NOMES = {"azul", "vermelho", "infinito", "roxo", "dominio"};
    private static final Map<UUID, Integer> TIPOS = new HashMap<>();
    private static final Map<UUID, Integer> PRIORIDADES = new HashMap<>();

    public static void registrar() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((jogador, pilha) -> {
            ModifierLayer<IAnimation> camada = new ModifierLayer<>();
            pilha.addAnimLayer(1000, camada);
            PlayerAnimationAccess.getPlayerAssociatedData(jogador).set(CAMADA, camada);
        });
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<IAnimation> camada(AbstractClientPlayer jogador) {
        Object dado = PlayerAnimationAccess.getPlayerAssociatedData(jogador).get(CAMADA);
        return dado instanceof ModifierLayer<?> c ? (ModifierLayer<IAnimation>) c : null;
    }

    public static void tocar(UUID id, int tipo) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || tipo < 1 || tipo > 5) return;
        if (!(mc.level.getPlayerByUUID(id) instanceof AbstractClientPlayer jogador)) return;
        ModifierLayer<IAnimation> camada = camada(jogador);
        if (camada == null) return;
        if (camada.isActive() && PRIORIDADES.getOrDefault(id, 0) >= 4 && tipo < 4) return;
        reproduzir(jogador, camada, id, tipo, "gojo_" + NOMES[tipo - 1]);
    }
    public static void cancelar(UUID id) {
        var mc = Minecraft.getInstance();
        if(mc.level != null && mc.level.getPlayerByUUID(id) instanceof AbstractClientPlayer jogador) {
            var camada = camada(jogador);
            if(camada != null) camada.setAnimation(null);
        }
        PRIORIDADES.remove(id);TIPOS.remove(id);
    }
    public static void cancelarSeTipo(UUID id,int tipo){if(TIPOS.getOrDefault(id,-1)==tipo)cancelar(id);}
    public static void tocarAsta(UUID id,int tipo){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(!(mc.level.getPlayerByUUID(id) instanceof AbstractClientPlayer jogador))return;
        var c=camada(jogador);if(c!=null)reproduzir(jogador,c,id,tipo,"asta_"+tipo);
    }
    public static void tocarDesperto(UUID id,int kit,int tipo){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(!(mc.level.getPlayerByUUID(id) instanceof AbstractClientPlayer jogador))return;
        var c=camada(jogador);if(c==null)return;
        if(c.isActive()&&PRIORIDADES.getOrDefault(id,0)>=4&&prioridade(tipo)<4)return;
        reproduzir(jogador,c,id,tipo,"desperto_"+kit+"_"+tipo);
    }
    public static void tocarCid(UUID id, int tipo) {
        var mc = Minecraft.getInstance();
        if(mc.level == null || tipo < 11 || tipo > 14) return;
        if(!(mc.level.getPlayerByUUID(id) instanceof AbstractClientPlayer jogador)) return;
        var camada = camada(jogador);
        if(camada == null) return;
        reproduzir(jogador,camada,id,tipo,"cid_" + (tipo - 10));
    }
    public static void tocarFrieren(UUID id,int tipo){
        var mc=Minecraft.getInstance();if(mc.level==null||tipo<21||tipo>24)return;
        if(!(mc.level.getPlayerByUUID(id) instanceof AbstractClientPlayer jogador))return;
        var c=camada(jogador);if(c!=null)reproduzir(jogador,c,id,tipo,"frieren_"+(tipo-20));
    }
    private static void reproduzir(AbstractClientPlayer jogador, ModifierLayer<IAnimation> camada, UUID id, int tipo, String nome) {
        KeyframeAnimation animacao = PlayerAnimationRegistry.getAnimation(new ResourceLocation("arcanecontrol",nome));
        if (animacao == null) return;
        camada.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(tipo==33?1:3, Ease.OUTQUAD),
                new KeyframeAnimationPlayer(animacao));
        TIPOS.put(id,tipo);
        PRIORIDADES.put(id,prioridade(tipo));
    }

    private static int prioridade(int tipo){return switch(tipo){case 4,5,14,24,32,35,38,306,512,513->4;default->1;};}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.level == null) return;
        TIPOS.keySet().removeIf(id -> mc.level.getPlayerByUUID(id) == null);
        PRIORIDADES.keySet().removeIf(id -> mc.level.getPlayerByUUID(id) == null);
        for (AbstractClientPlayer jogador : mc.level.players()) {
            if (!jogador.isAlive()) {
                ModifierLayer<IAnimation> camada = camada(jogador);
                if (camada != null) camada.setAnimation(null);
                PRIORIDADES.remove(jogador.getUUID());TIPOS.remove(jogador.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void sair(ClientPlayerNetworkEvent.LoggingOut event) { PRIORIDADES.clear();TIPOS.clear(); }
}
