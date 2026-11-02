package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.equipamentos.TrajeAnime;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class MantoCidCliente {
    private static final class Estado{final MantoDirecionalCid mola=new MantoDirecionalCid();Object mundo;Vec3 pos;float yaw;int entidade;}
    private static final Map<UUID,Estado> ESTADOS=new HashMap<>();
    private static final MolaMantoCid.Pose ZERO=new MolaMantoCid.Pose(0,0,0);
    public static MolaMantoCid.Pose pose(UUID id,int segmento,float parcial){var e=ESTADOS.get(id);return e==null||!ConfigCliente.TECIDO_CID.get()?ZERO:e.mola.amostrar(2,segmento,parcial);}
    public static MolaMantoCid.Pose painel(UUID id,int painel,int segmento,float parcial){var e=ESTADOS.get(id);return e==null||!ConfigCliente.TECIDO_CID.get()?ZERO:e.mola.amostrar(painel,segmento,parcial);}
    public static MolaMantoCid.Pose detalhe(UUID id,String osso,float parcial){var e=ESTADOS.get(id);return e==null||!ConfigCliente.TECIDO_CID.get()?ZERO:e.mola.detalhe(osso,parcial);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.level==null||!ConfigCliente.TECIDO_CID.get()){ESTADOS.clear();return;}if(mc.isPaused())return;
        var vistos=new HashSet<UUID>();
        for(var p:mc.level.players()){
            if(!p.isAlive()||p.isSpectator()||!(p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TrajeAnime t)||!t.estilo.equals("shadow"))continue;
            var id=p.getUUID();if(!ESTADOS.containsKey(id)&&ESTADOS.size()>=64)continue;vistos.add(id);var s=ESTADOS.computeIfAbsent(id,k->new Estado());var pos=p.position();
            if(s.mundo!=mc.level||s.pos==null||s.entidade!=p.getId()||pos.distanceToSqr(s.pos)>16){s.mola.reset();s.mundo=mc.level;s.pos=pos;s.yaw=p.yBodyRot;s.entidade=p.getId();continue;}
            var delta=pos.subtract(s.pos);double rad=Math.toRadians(p.yBodyRot),giro=Math.toRadians(Mth.wrapDegrees(p.yBodyRot-s.yaw));
            double frente=-Math.sin(rad)*delta.x+Math.cos(rad)*delta.z,lado=Math.cos(rad)*delta.x+Math.sin(rad)*delta.z;
            s.mola.tick(new MolaMantoCid.Entrada(frente,lado,delta.y,giro,p.onGround()));s.pos=pos;s.yaw=p.yBodyRot;
        }
        ESTADOS.keySet().removeIf(id->!vistos.contains(id));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ESTADOS.clear();}
}
