package com.cauansilva.arcanecontrol.core.client.render;
import com.lowdragmc.photon.client.fx.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.curve.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.Sphere;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import java.util.*;
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class EmissoresAnime {
    private record Ativo(FXRuntime runtime,Object mundo,UUID dono,long fim){}
    private static final List<Ativo> ATIVOS=new ArrayList<>();
    public static void pulso(UUID dono,Vec3 pos,int cor,int quantidade,double velocidade,double tamanho,int vida){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(ATIVOS.size()>=24){ATIVOS.remove(0).runtime.destroy(true);}
        int n=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?Math.max(2,quantidade/2):quantidade;
        var fx=new FX();var p=new ParticleEmitter();p.setName("pulso_anime");var c=p.config;
        c.setLooping(false);c.setDuration(1);c.setMaxParticles(128);c.setStartLifetime(NumberFunction.constant(vida));
        c.setStartSpeed(NumberFunction.constant(velocidade));c.setStartSize(new NumberFunction3(tamanho,tamanho,tamanho));
        c.setStartColor(NumberFunction.color(0xFF000000|cor));c.emission.setEmissionRate(NumberFunction.constant(Math.min(128,n)));
        var s=new Sphere();s.setRadius(.2f);c.shape.setShape(s);c.renderer.setBloomEffect(true);c.renderer.setBloomColor(0xFF000000|cor);
        var curva=new Curve(0,1,0,1,1,"vida","tamanho");curva.setCurves(new ECBCurves(0,1,.2f,.9f,.8f,.1f,1,0));
        c.sizeOverLifetime.setEnable(true);c.sizeOverLifetime.setSize(new NumberFunction3(curva,curva,curva));
        fx.getMainFX().objects().add(p);var r=fx.createInternalRuntime();
        r.getRoot().updatePos(new Vector3f((float)pos.x,(float)pos.y,(float)pos.z));var mundo=mc.level;r.emmit(()->mundo);
        ATIVOS.add(new Ativo(r,mundo,dono,mundo.getGameTime()+vida+3));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();var it=ATIVOS.iterator();
        while(it.hasNext()){
            var a=it.next();var dono=mc.level==null?null:mc.level.getPlayerByUUID(a.dono);
            if(mc.level!=a.mundo||dono==null||!dono.isAlive()||mc.level.getGameTime()>=a.fim){a.runtime.destroy(true);it.remove();}
        }
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ATIVOS.forEach(a->a.runtime.destroy(true));ATIVOS.clear();}
}
