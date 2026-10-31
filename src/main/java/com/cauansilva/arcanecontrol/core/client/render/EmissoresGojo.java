package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo;
import com.lowdragmc.photon.client.fx.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.curve.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.Sphere;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.*;

/** Bloom de acento: o material principal continua legivel, com poucos sprites. */
public final class EmissoresGojo {
    private record Ativo(GerenciadorCutscene3D.Efeito3DAtivo efeito,FXRuntime runtime,Object mundo) {}
    private static final List<Ativo> ATIVOS=new ArrayList<>();
    public static void iniciar(GerenciadorCutscene3D.Efeito3DAtivo e) {
        var mc=Minecraft.getInstance();
        if(mc.level==null || !ConfigCliente.GOJO_BLOOM.get() || ATIVOS.size()>=8) return;
        var fx=new FX();
        switch(e.tipo) {
            case 1 -> fx.getMainFX().objects().add(emissor("azul",60,12,2,1.1,-.5,.09,0xFF279DFF));
            case 2 -> {
                fx.getMainFX().objects().add(emissor("vermelho",13,6,1,.2,.1,.22,0xFFFF3545));
                fx.getMainFX().objects().add(emissor("repulsao",2,9,16,.2,3.5,.4,0xFFFF5555));
            }
            case 4 -> {
                fx.getMainFX().objects().add(emissor("fusao_azul",30,6,2,.3,.2,.16,0xFF39BFFF));
                fx.getMainFX().objects().add(emissor("fusao_vermelho",30,6,2,.3,.2,.16,0xFFFF3545));
                fx.getMainFX().objects().add(emissor("roxo",55,12,2,3.5,.3,.3,0xFFB36BFF));
            }
            case 5 -> fx.getMainFX().objects().add(emissor("dominio",140,35,2,3.7,.05,.1,0xFFCBEAFF));
            default -> {return;}
        }
        var r=fx.createInternalRuntime();
        atualizar(e,r);
        var mundo=mc.level;r.emmit(()->mundo);
        for(var o:r.getObjects().values()) if(o instanceof ParticleEmitter p) {
            p.setDelay(switch(p.getName()) {case "repulsao"->13;case "roxo"->30;case "dominio"->20;default->0;});
        }
        ATIVOS.add(new Ativo(e,r,mc.level));
    }
    private static ParticleEmitter emissor(String nome,int duracao,int vida,int taxa,double raio,double velocidade,double tamanho,int cor) {
        var p=new ParticleEmitter();p.setName(nome);var c=p.config;
        c.setLooping(false);c.setDuration(duracao);c.setMaxParticles(64);
        c.setStartLifetime(NumberFunction.constant(vida));c.setStartSpeed(NumberFunction.constant(velocidade));
        c.setStartSize(new NumberFunction3(tamanho,tamanho,tamanho));c.setStartColor(NumberFunction.color(cor));
        c.emission.setEmissionRate(NumberFunction.constant(ConfigCliente.VFX_REDUZIDOS.get()?Math.max(1,taxa/2):taxa));
        var s=new Sphere();s.setRadius((float)raio);s.setRadiusThickness(.2f);c.shape.setShape(s);
        c.renderer.setBloomEffect(true);c.renderer.setBloomColor(cor);c.material.setCull(false);
        var curva=new Curve(0,1,0,1,1,"vida","tamanho");curva.setCurves(new ECBCurves(0,1,.2f,.9f,.8f,.1f,1,0));
        c.sizeOverLifetime.setEnable(true);c.sizeOverLifetime.setSize(new NumberFunction3(curva,curva,curva));
        return p;
    }
    private static void atualizar(GerenciadorCutscene3D.Efeito3DAtivo e,FXRuntime r) {
        var mc=Minecraft.getInstance();var p=mc.level.getPlayerByUUID(e.playerId);
        Vec3 olho=p==null?e.origem:p.getEyePosition(),dir=p==null?e.direcao:p.getLookAngle();
        if(e.tipo==2 && e.tickAtual>=13 || e.tipo==4 && e.tickAtual>30) {olho=e.origem;dir=e.direcao;}
        Vec3 centro=switch(e.tipo) {
            case 1->olho.add(dir.scale(7.5));case 2->olho.add(dir.scale(1.8));
            case 4->olho.add(dir.scale(PerfilVfxGojo.distanciaRoxo(e.tickAtual)));
            case 5->e.origem.add(0,8,0);default->olho;
        };
        r.getRoot().updatePos(new Vector3f((float)centro.x,(float)centro.y,(float)centro.z));
        for(var o:r.getObjects().values()) if(o instanceof ParticleEmitter emissor) {
            if((emissor.getName().equals("repulsao") && e.tickAtual>=13
                    || emissor.getName().equals("roxo") && e.tickAtual>=30) && emissor.getDelay()>0) emissor.setDelay(0);
        }
        if(e.tipo==4 && e.tickAtual<=30) {
            Vec3 lado=dir.cross(new Vec3(0,1,0)).normalize().scale(PerfilVfxGojo.separacaoRoxo(e.tickAtual));
            for(var o:r.getObjects().values()) if(o instanceof ParticleEmitter emissor && emissor.getName().startsWith("fusao_")) {
                double sinal=emissor.getName().equals("fusao_azul")?-1:1;
                emissor.config.shape.setPosition(new NumberFunction3(lado.x*sinal,lado.y*sinal,lado.z*sinal));
            }
        }
    }
    public static void tick() {
        var mc=Minecraft.getInstance();var it=ATIVOS.iterator();
        while(it.hasNext()) {
            var a=it.next();var e=a.efeito;
            var p=mc.level==null?null:mc.level.getPlayerByUUID(e.playerId);
            if(mc.level!=a.mundo || p==null || !p.isAlive() || e.tickAtual>=e.duracaoMax
                    || !GerenciadorCutscene3D.efeitos().contains(e) || !ConfigCliente.GOJO_BLOOM.get()) {
                a.runtime.destroy(true);it.remove();
            } else if(!mc.isPaused()) atualizar(e,a.runtime);
        }
    }
    public static void limpar(){ATIVOS.forEach(a->a.runtime.destroy(true));ATIVOS.clear();}
}
