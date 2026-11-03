package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.cid.MolaMantoCid;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoAsta;
import com.cauansilva.arcanecontrol.core.equipamentos.TrajeAnime;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Per-player Black Bulls robe state at fixed 20 Hz, like MantoCidCliente (with the idle breeze inside TecidoAsta);
 *  resets on teleport, world or entity change. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class TecidoAstaCliente {
    private static final class Estado{final TecidoAsta tecido=new TecidoAsta();Object mundo;Vec3 pos;float yaw;int entidade;}
    private static final Map<UUID,Estado> ESTADOS=new HashMap<>();
    private static final MolaMantoCid.Pose ZERO=new MolaMantoCid.Pose(0,0,0);
    public static MolaMantoCid.Pose pose(UUID id,String osso,float parcial){var e=ESTADOS.get(id);return e==null||!ConfigCliente.TECIDO_ASTA.get()?ZERO:e.tecido.pose(osso,parcial);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.level==null||!ConfigCliente.TECIDO_ASTA.get()){ESTADOS.clear();return;}if(mc.isPaused())return;
        var vistos=new HashSet<UUID>();
        for(var p:mc.level.players()){
            if(!p.isAlive()||p.isSpectator()||!(p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TrajeAnime t)||!t.estilo.equals("asta"))continue;
            var id=p.getUUID();if(!ESTADOS.containsKey(id)&&ESTADOS.size()>=64)continue;vistos.add(id);var s=ESTADOS.computeIfAbsent(id,k->new Estado());var pos=p.position();
            if(s.mundo!=mc.level||s.pos==null||s.entidade!=p.getId()||pos.distanceToSqr(s.pos)>16){s.tecido.reset();s.mundo=mc.level;s.pos=pos;s.yaw=p.yBodyRot;s.entidade=p.getId();continue;}
            var d=pos.subtract(s.pos);double rad=Math.toRadians(p.yBodyRot),giro=Math.toRadians(Mth.wrapDegrees(p.yBodyRot-s.yaw));
            double frente=-Math.sin(rad)*d.x+Math.cos(rad)*d.z,lado=Math.cos(rad)*d.x+Math.sin(rad)*d.z;
            // Strikes push the robe; a sword swing flicks it round; crouching lets it settle against the back.
            var magia=EfeitosAsta.tecido(id);double golpe=Math.sin(Math.PI*p.getAttackAnim(1))*.12,agacha=p.isCrouching()?.07:0;
            s.tecido.tick(new MolaMantoCid.Entrada(frente+magia.frente()+agacha,lado+magia.lado(),d.y,giro+magia.giro()+golpe,p.onGround()));s.pos=pos;s.yaw=p.yBodyRot;
        }
        ESTADOS.keySet().removeIf(id->!vistos.contains(id));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ESTADOS.clear();}
}
