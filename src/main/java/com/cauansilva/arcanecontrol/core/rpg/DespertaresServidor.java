package com.cauansilva.arcanecontrol.core.rpg;

import com.cauansilva.arcanecontrol.core.poderes.*;
import net.minecraft.server.level.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class DespertaresServidor {
    public record Forma(ServerPlayer p,ServerLevel nivel,Vec3 centro,EstadoDespertar estado,boolean gravidadeOriginal){}
    private static final Map<UUID,Forma> FORMAS=new HashMap<>();
    public static Forma forma(ServerPlayer p){return FORMAS.get(p.getUUID());}
    public static boolean ativo(ServerPlayer p){var f=forma(p);return f!=null&&f.estado.ativa(RpgServidor.agora(p));}
    public static int restante(ServerPlayer p){var f=forma(p);return f==null?0:f.estado.restante(RpgServidor.agora(p));}
    public static boolean exclusivoUsado(ServerPlayer p){var f=forma(p);return f!=null&&f.estado.exclusivoUsado();}
    public static void ativar(ServerPlayer p,PoderAnime kit){
        if(ativo(p)){cancelar(p);return;}
        if(ProgressaoPoderes.ocupado(p))return;
        if(kit==PoderAnime.CID&&!p.getMainHandItem().is(com.cauansilva.arcanecontrol.core.cid.EspadaCid.ESPADA.get())){p.displayClientMessage(net.minecraft.network.chat.Component.literal("Segure a Lâmina Shadow."),true);return;}
        if(kit==PoderAnime.ASTA&&!com.cauansilva.arcanecontrol.core.asta.PoderesAsta.armado(p))return;
        var personagem=PersonagemRpg.porId(kit.id);
        if(!RpgServidor.conjurar(p,kit,4,personagem.recargaSegundos*20))return;
        var f=new Forma(p,p.serverLevel(),p.position(),new EstadoDespertar(personagem,RpgServidor.agora(p)),p.isNoGravity());FORMAS.put(p.getUUID(),f);
        if(kit==PoderAnime.ASTA)p.stopUsingItem();
        if(kit==PoderAnime.FRIEREN){var t=RpgServidor.dados(p);t.putBoolean("restaurarGravidade",true);t.putBoolean("gravidadeOriginal",f.gravidadeOriginal);p.setNoGravity(true);}
        if(kit==PoderAnime.GOJO)com.cauansilva.arcanecontrol.core.magia.GerenciadorMagiasGojo.executarEfeito(p,5);
        if(kit==PoderAnime.CID)com.cauansilva.arcanecontrol.core.cid.PoderesCid.iniciarDespertar(p);
        com.cauansilva.arcanecontrol.core.rede.RegistoRede.ENVIAR.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar(p.getUUID(),200,f.centro,Vec3.ZERO,personagem.duracaoSegundos*20,kit.id));
        RpgServidor.uso(p,kit);RpgServidor.sincronizar(p);
    }
    public static void cancelar(ServerPlayer p){
        var f=FORMAS.remove(p.getUUID());if(f==null)return;
        if(f.estado.kit==PersonagemRpg.FRIEREN){p.setNoGravity(f.gravidadeOriginal);RpgServidor.dados(p).remove("restaurarGravidade");}
        if(f.estado.kit==PersonagemRpg.SHADOW){
            com.cauansilva.arcanecontrol.core.cid.PoderesCid.dados(p).putLong("cid_ocupado",0);
            var lento=p.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
            if(lento!=null&&lento.getAmplifier()==5&&lento.getDuration()<=100)p.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        }
        EfeitosDespertar.enviar(f.nivel,p.getUUID(),0,f.centro,Vec3.ZERO,0,f.estado.kit.id);
        com.cauansilva.arcanecontrol.core.rede.RegistoRede.ENVIAR.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar(p.getUUID(),0,f.centro,Vec3.ZERO,0,f.estado.kit.id));
        if(!p.hasDisconnected())RpgServidor.sincronizar(p);
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        for(var f:List.copyOf(FORMAS.values())){
            var p=f.p;
            if(!p.isAlive()||p.hasDisconnected()||p.serverLevel()!=f.nivel||!f.estado.ativa(RpgServidor.agora(p))||ProgressaoPoderes.ler(p).ativo().id!=f.estado.kit.id){cancelar(p);continue;}
            if(f.estado.kit==PersonagemRpg.FRIEREN){p.setNoGravity(true);var alvo=f.centro.add(0,1.6,0);p.setDeltaMovement(alvo.subtract(p.position()).scale(.18));p.fallDistance=0;p.hurtMarked=true;}
            if(f.estado.kit==PersonagemRpg.GOJO&&p.tickCount%10==0)MovesetsServidor.morteCerebral(f);
        }
    }
    private static void limparDesafio(ServerPlayer p){var t=RpgServidor.dados(p);if(t.hasUUID("desafio")){var id=t.getUUID("desafio");for(var l:p.server.getAllLevels()){var chefe=l.getEntity(id);if(chefe instanceof NemesisEntity)chefe.discard();}t.remove("desafio");}}
    @SubscribeEvent public static void sair(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){cancelar(p);limparDesafio(p);}}
    @SubscribeEvent public static void morrer(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){cancelar(p);limparDesafio(p);}}
    @SubscribeEvent public static void dimensao(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){cancelar(p);limparDesafio(p);}}
    @SubscribeEvent public static void entrar(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){var t=RpgServidor.dados(p);if(t.getBoolean("restaurarGravidade")){p.setNoGravity(t.getBoolean("gravidadeOriginal"));t.remove("restaurarGravidade");}limparDesafio(p);}}
    @SubscribeEvent public static void observar(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer observador&&e.getTarget() instanceof ServerPlayer p){var f=forma(p);if(f!=null)com.cauansilva.arcanecontrol.core.rede.RegistoRede.ENVIAR.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->observador),new com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar(p.getUUID(),200,f.centro,Vec3.ZERO,restante(p),f.estado.kit.id));}}
    @SubscribeEvent public static void parar(ServerStoppedEvent e){for(var f:List.copyOf(FORMAS.values()))cancelar(f.p);FORMAS.clear();}
}
