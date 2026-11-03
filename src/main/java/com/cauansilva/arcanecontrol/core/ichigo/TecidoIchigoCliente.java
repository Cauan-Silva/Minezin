package com.cauansilva.arcanecontrol.core.ichigo;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.cid.MolaMantoCid;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoIchigo;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Per-player Bankai coat tails at fixed 20 Hz, like TecidoAstaCliente; runs only while the coat is visible and resets
 *  on teleport, world or entity change (Shunpo is a teleport, so the tails restart behind the player instead of
 *  stretching across the dash). */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class TecidoIchigoCliente {
    private static final class Estado{final TecidoIchigo tecido=new TecidoIchigo();Object mundo;Vec3 pos;float yaw;int entidade;double braco;boolean revelado;}
    private static final Map<UUID,Estado> ESTADOS=new HashMap<>();
    private static final MolaMantoCid.Pose ZERO=new MolaMantoCid.Pose(0,0,0);
    public static MolaMantoCid.Pose pose(UUID id,String osso,float parcial){var e=ESTADOS.get(id);return e==null||!ConfigCliente.TECIDO_ICHIGO.get()?ZERO:e.tecido.pose(osso,parcial);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.level==null||!ConfigCliente.TECIDO_ICHIGO.get()){ESTADOS.clear();return;}if(mc.isPaused())return;
        var vistos=new HashSet<UUID>();
        for(var p:mc.level.players()){
            if(!p.isAlive()||p.isSpectator()||!EfeitosIchigo.bankaiVisual(p.getUUID())&&!EfeitosIchigo.mugetsu(p.getUUID()))continue;
            var id=p.getUUID();if(!ESTADOS.containsKey(id)&&ESTADOS.size()>=64)continue;vistos.add(id);boolean novo=!ESTADOS.containsKey(id);var s=ESTADOS.computeIfAbsent(id,k->new Estado());var pos=p.position();
            // First tick the coat is visible = the reveal cut: throw it outward once.
            if(novo&&!s.revelado){s.revelado=true;s.tecido.impulso(.9);}
            if(s.mundo!=mc.level||s.pos==null||s.entidade!=p.getId()||pos.distanceToSqr(s.pos)>16){if(s.pos!=null)s.tecido.reset();s.mundo=mc.level;s.pos=pos;s.yaw=p.yBodyRot;s.entidade=p.getId();continue;}
            var d=pos.subtract(s.pos);double rad=Math.toRadians(p.yBodyRot),giro=Math.toRadians(Mth.wrapDegrees(p.yBodyRot-s.yaw));
            double frente=-Math.sin(rad)*d.x+Math.cos(rad)*d.z,lado=Math.cos(rad)*d.x+Math.sin(rad)*d.z;
            // A sword swing flicks the tails round; crouching lets them settle.
            double golpe=Math.sin(Math.PI*p.getAttackAnim(1))*.14,agacha=p.isCrouching()?.07:0;
            // Vanilla arm swing (sin of the walk cycle); the sleeves trail its change, the sword arm also takes the swing.
            double braco=Math.sin(p.walkAnimation.position()*.6662)*1.4*p.walkAnimation.speed(),db=braco-s.braco;s.braco=braco;
            s.tecido.tick(new MolaMantoCid.Entrada(frente+agacha,lado,d.y,giro+golpe,p.onGround()),-db,db+golpe*2);s.pos=pos;s.yaw=p.yBodyRot;
        }
        ESTADOS.keySet().removeIf(id->!vistos.contains(id));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ESTADOS.clear();}
}
