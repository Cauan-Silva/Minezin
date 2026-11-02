package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.client.tecido.DevilUnionTecido;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Per-player wing/tail state while the Devil Union form is visible; resets on teleport, world or entity change. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class TecidoDevilUnionCliente {
    private static final class Estado{final DevilUnionTecido tecido=new DevilUnionTecido();Object mundo;Vec3 pos;float yaw;int entidade;}
    private static final Map<UUID,Estado> ESTADOS=new HashMap<>();
    private static final DevilUnionTecido.Pose ZERO=new DevilUnionTecido.Pose(0,0,0);
    /** With the option off the wings still open and close with the entrance, only the secondary motion stops. */
    public static DevilUnionTecido.Pose pose(UUID id,String osso,float parcial,double abertura){
        var e=ESTADOS.get(id);
        if(e!=null&&ConfigCliente.TECIDO_ASTA.get())return e.tecido.pose(osso,parcial,abertura);
        return osso.startsWith("du_asa_")&&osso.length()==8?REPOUSO.pose(osso,1,abertura):ZERO;
    }
    private static final DevilUnionTecido REPOUSO=new DevilUnionTecido();
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.level==null||!ConfigCliente.TECIDO_ASTA.get()){ESTADOS.clear();return;}if(mc.isPaused())return;
        var vistos=new HashSet<UUID>();
        for(var p:mc.level.players()){
            if(!p.isAlive()||p.isSpectator()||EfeitosAsta.forma(p.getUUID(),1)==null)continue;
            var id=p.getUUID();if(!ESTADOS.containsKey(id)&&ESTADOS.size()>=64)continue;
            vistos.add(id);var s=ESTADOS.computeIfAbsent(id,k->new Estado());var pos=p.position();
            if(s.mundo!=mc.level||s.pos==null||s.entidade!=p.getId()||pos.distanceToSqr(s.pos)>4){s.tecido.reset();s.mundo=mc.level;s.pos=pos;s.yaw=p.yBodyRot;s.entidade=p.getId();continue;}
            var d=pos.subtract(s.pos);double rad=Math.toRadians(p.yBodyRot),giro=Math.toRadians(Mth.wrapDegrees(p.yBodyRot-s.yaw));
            double frente=-Math.sin(rad)*d.x+Math.cos(rad)*d.z,lado=Math.cos(rad)*d.x+Math.sin(rad)*d.z;
            s.tecido.tick(new DevilUnionTecido.Entrada(frente,lado,d.y,giro,p.onGround()));s.pos=pos;s.yaw=p.yBodyRot;
        }
        ESTADOS.keySet().removeIf(id->!vistos.contains(id));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ESTADOS.clear();}
}
