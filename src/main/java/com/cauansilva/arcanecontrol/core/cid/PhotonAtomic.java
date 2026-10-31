package com.cauansilva.arcanecontrol.core.cid;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.lowdragmc.photon.client.fx.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.curve.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.Sphere;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.*;

/** Emissores autorais. Cada execucao possui runtime e cancelamento independentes. */
public final class PhotonAtomic {
    private record Ativo(FXRuntime runtime, Object mundo, long fim) {}
    private static final Map<UUID, Ativo> ATIVOS = new HashMap<>();

    public static void iniciar(UUID jogador, Vec3 centro) {
        cancelar(jogador);
        var mc = Minecraft.getInstance();
        if(mc.level == null || !ConfigCliente.PHOTON.get() || ATIVOS.size() >= 4) return;
        int escala = ConfigCliente.VFX_REDUZIDOS.get() ? 2 : 1;
        FX fx = new FX();
        fx.getMainFX().objects().add(emissor("convergencia",80,42,6/escala,7,-3,.16,0xFFAC55FF,true));
        fx.getMainFX().objects().add(emissor("centelhas",68,25,3/escala,4,-3,.09,0xFFF1DFFF,true));
        fx.getMainFX().objects().add(emissor("explosao",3,38,60/escala,.3,12,.45,0xFFDDAAFF,true));
        fx.getMainFX().objects().add(emissor("corona",5,25,12/escala,.5,7,2.5,0xAABB66FF,true));
        fx.getMainFX().objects().add(emissor("poeira",70,75,3/escala,8,.3,2.2,0x66554865,false));
        // FX e emissores sao novos por conjuracao; mantem nomes/tempos sem shallowCopy.
        var runtime = fx.createInternalRuntime();
        runtime.getRoot().updatePos(new Vector3f((float)centro.x,(float)centro.y+1,(float)centro.z));
        var mundo = mc.level;
        runtime.emmit(() -> mundo);
        // startDelay de ParticleConfig atrasa cada particula, nao o emissor.
        for(var objeto:runtime.getObjects().values()) {
            if(objeto instanceof ParticleEmitter emissor) emissor.setDelay(switch(emissor.getName()) {
                case "atomic_centelhas" -> 20;
                case "atomic_explosao", "atomic_corona" -> 90;
                case "atomic_poeira" -> 94;
                default -> 0;
            });
        }
        ATIVOS.put(jogador,new Ativo(runtime,mundo,mundo.getGameTime()+245));
    }

    private static ParticleEmitter emissor(String nome,int duracao,int vida,int taxa,
            double raio,double velocidade,double tamanho,int cor,boolean bloom) {
        var e = new ParticleEmitter(); e.setName("atomic_"+nome);
        var c = e.config;
        c.setLooping(false); c.setDuration(duracao);
        c.setStartDelay(NumberFunction.constant(0));
        c.setStartLifetime(NumberFunction.constant(vida));
        c.setStartSpeed(NumberFunction.constant(velocidade));
        c.setStartSize(new NumberFunction3(tamanho,tamanho,tamanho));
        c.setStartColor(NumberFunction.color(cor));
        c.setMaxParticles(256);
        c.emission.setEmissionRate(NumberFunction.constant(taxa));
        var sphere = new Sphere(); sphere.setRadius((float)raio); sphere.setRadiusThickness(.15f);
        c.shape.setShape(sphere);
        c.renderer.setBloomEffect(bloom); c.renderer.setBloomColor(cor);
        c.material.setCull(false);
        var curva = new Curve(0,1,0,1,1,"vida","tamanho");
        curva.setCurves(new ECBCurves(0,1,.2f,1,.8f,0,1,0));
        c.sizeOverLifetime.setEnable(true);
        c.sizeOverLifetime.setSize(new NumberFunction3(curva,curva,curva));
        return e;
    }

    public static void tick() {
        var mc = Minecraft.getInstance();
        var it = ATIVOS.entrySet().iterator();
        while(it.hasNext()) {
            var entry=it.next(); var a=entry.getValue();
            var p=mc.level==null?null:mc.level.getPlayerByUUID(entry.getKey());
            if(mc.level!=a.mundo || p==null || !p.isAlive() || mc.level.getGameTime()>=a.fim || !ConfigCliente.PHOTON.get()) {
                a.runtime.destroy(true); it.remove();
            }
        }
    }
    public static void cancelar(UUID jogador) {
        var a=ATIVOS.remove(jogador); if(a!=null) a.runtime.destroy(true);
    }
    public static void limpar() {
        ATIVOS.values().forEach(a->a.runtime.destroy(true)); ATIVOS.clear();
    }
}
