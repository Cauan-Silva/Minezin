package com.cauansilva.arcanecontrol.core.gojo;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoGojo;
import com.cauansilva.arcanecontrol.core.equipamentos.TrajeAnime;
import com.cauansilva.arcanecontrol.core.client.render.RenderDespertar;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class TecidoGojoCliente {
    private static final class Estado{final TecidoGojo tecido=new TecidoGojo();Object mundo;Vec3 pos;float yaw;int entidade;}
    private static final Map<UUID,Estado> ESTADOS=new HashMap<>();
    private static final TecidoGojo.Pose ZERO=new TecidoGojo.Pose(0,0,0);
    public static TecidoGojo.Pose pose(UUID id,String osso,float parcial){var e=ESTADOS.get(id);return e==null||!ConfigCliente.TECIDO_GOJO.get()?ZERO:e.tecido.pose(osso,parcial);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.level==null||!ConfigCliente.TECIDO_GOJO.get()){ESTADOS.clear();return;}if(mc.isPaused())return;
        var vistos=new HashSet<UUID>();
        for(var p:mc.level.players()){
            if(!p.isAlive()||p.isSpectator()||!(p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TrajeAnime t)||!t.estilo.equals("gojo"))continue;
            var id=p.getUUID();if(!ESTADOS.containsKey(id)&&ESTADOS.size()>=64)continue;
            vistos.add(id);var s=ESTADOS.computeIfAbsent(id,k->new Estado());var pos=p.position();
            if(s.mundo!=mc.level||s.pos==null||s.entidade!=p.getId()||pos.distanceToSqr(s.pos)>4){s.tecido.reset();s.mundo=mc.level;s.pos=pos;s.yaw=p.yBodyRot;s.entidade=p.getId();continue;}
            var delta=pos.subtract(s.pos);double rad=Math.toRadians(p.yBodyRot),giro=Math.toRadians(Mth.wrapDegrees(p.yBodyRot-s.yaw));
            double frente=-Math.sin(rad)*delta.x+Math.cos(rad)*delta.z,lado=Math.cos(rad)*delta.x+Math.sin(rad)*delta.z;
            var magia=RenderDespertar.impulsoTecidoGojo(id);
            s.tecido.tick(new TecidoGojo.Entrada(frente+magia.frente(),lado+magia.lado(),delta.y,giro+magia.giro(),p.onGround()));
            s.pos=pos;s.yaw=p.yBodyRot;
        }
        ESTADOS.keySet().removeIf(id->!vistos.contains(id));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ESTADOS.clear();}
}
