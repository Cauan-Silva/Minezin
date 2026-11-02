package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.audio.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Client presentation driven by accepted effects; spatial PLAYERS sounds respect the game's mixers. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class AudioAnime {
    private record Pendente(UUID dono,String grupo,Object mundo,Vec3 centro,long inicio,RoteiroAudio.Cue cue){}
    private static final List<Pendente> FILA=new ArrayList<>();
    private static final List<Som> ATIVOS=new ArrayList<>();
    public static void programar(UUID dono,String grupo,Vec3 centro,List<RoteiroAudio.Cue> cues){
        cancelar(dono,grupo);var mc=Minecraft.getInstance();
        if(mc.level==null||!ConfigCliente.SFX_ANIME.get()||!Double.isFinite(centro.lengthSqr()))return;
        for(var c:cues){
            if(FILA.size()>=64)break;
            var p=new Pendente(dono,grupo,mc.level,centro,mc.level.getGameTime()+c.atraso(),c);
            if(c.atraso()==0)tocar(p);else FILA.add(p);
        }
    }
    public static void cancelar(UUID dono,String grupo){
        FILA.removeIf(p->p.dono.equals(dono)&&(grupo==null||p.grupo.equals(grupo)));
        var it=ATIVOS.iterator();while(it.hasNext()){var s=it.next();if(s.dono.equals(dono)&&(grupo==null||s.grupo.equals(grupo))){s.encerrar();Minecraft.getInstance().getSoundManager().stop(s);it.remove();}}
    }
    private static void tocar(Pendente p){
        var mc=Minecraft.getInstance();if(mc.level!=p.mundo||mc.player==null||mc.player.distanceToSqr(p.centro)>96*96)return;
        if(ATIVOS.size()>=24){var antigo=ATIVOS.remove(0);antigo.encerrar();mc.getSoundManager().stop(antigo);}
        var s=new Som(p);ATIVOS.add(s);mc.getSoundManager().play(s);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.isPaused())return;
        if(mc.level==null||!ConfigCliente.SFX_ANIME.get()){limpar();return;}
        ATIVOS.removeIf(s->{boolean fim=s.isStopped()||s.mundo!=mc.level||mc.level.getGameTime()>=s.fim;if(fim){s.encerrar();mc.getSoundManager().stop(s);}return fim;});
        var it=FILA.iterator();while(it.hasNext()){
            var p=it.next();var dono=mc.level.getPlayerByUUID(p.dono);
            if(p.mundo!=mc.level||dono==null||!dono.isAlive()){it.remove();continue;}
            if(mc.level.getGameTime()>=p.inicio){tocar(p);it.remove();}
        }
    }
    private static void limpar(){for(var s:ATIVOS){s.encerrar();Minecraft.getInstance().getSoundManager().stop(s);}ATIVOS.clear();FILA.clear();}
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){limpar();}
    private static final class Som extends AbstractTickableSoundInstance {
        final UUID dono;final String grupo;final Object mundo;final long fim;final boolean seguir;final float ganho;
        Som(Pendente p){
            super(RegistroSons.obter(p.cue.nome()),SoundSource.PLAYERS,RandomSource.create());
            dono=p.dono;grupo=p.grupo;mundo=p.mundo;seguir=p.cue.seguir();ganho=p.cue.volume();fim=p.inicio+p.cue.vida();
            x=p.centro.x;y=p.centro.y;z=p.centro.z;volume=(float)(ganho*ConfigCliente.VOLUME_SFX.get());pitch=1;looping=false;relative=false;
        }
        void encerrar(){stop();}
        @Override public void tick(){
            var mc=Minecraft.getInstance();if(mc.level==null||mc.level!=mundo||!ConfigCliente.SFX_ANIME.get()){stop();return;}
            var p=mc.level.getPlayerByUUID(dono);
            if(p==null||!p.isAlive()||mc.level.getGameTime()>=fim){stop();return;}
            if(seguir){x=p.getX();y=p.getY()+1;z=p.getZ();}
            volume=(float)(ganho*ConfigCliente.VOLUME_SFX.get());
        }
    }
}
