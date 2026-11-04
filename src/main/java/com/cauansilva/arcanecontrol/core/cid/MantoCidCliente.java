package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoIchigo;
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
/** Shadow's coat on the outfit standard (docs/padrao-trajes.md): the same cloth as Ichigo's Bankai coat (TecidoIchigo,
 *  tools/build_cid_traje.py reuses its bones: back 6x6, sides, rounded corners, front flaps, collar straps), at 20 Hz,
 *  reset on teleport (Passo das Sombras); Cid's abilities push it through EfeitosCid.tecido. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class MantoCidCliente {
    private static final class Estado{final TecidoIchigo tecido=new TecidoIchigo(com.cauansilva.arcanecontrol.core.client.tecido.PerfilTecido.de("shadow"));Object mundo;Vec3 pos;float yaw;int entidade;double braco,forca;}
    private static final Map<UUID,Estado> ESTADOS=new HashMap<>();
    private static final MolaMantoCid.Pose ZERO=new MolaMantoCid.Pose(0,0,0);
    public static MolaMantoCid.Pose pose(UUID id,String osso,float parcial){var e=ESTADOS.get(id);return e==null||!ConfigCliente.TECIDO_CID.get()?ZERO:e.tecido.pose(osso,parcial);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.level==null||!ConfigCliente.TECIDO_CID.get()){ESTADOS.clear();return;}if(mc.isPaused())return;
        var vistos=new HashSet<UUID>();
        for(var p:mc.level.players()){
            if(!p.isAlive()||p.isSpectator()||!(p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TrajeAnime t)||!t.estilo.equals("shadow"))continue;
            var id=p.getUUID();if(!ESTADOS.containsKey(id)&&ESTADOS.size()>=64)continue;vistos.add(id);var s=ESTADOS.computeIfAbsent(id,k->new Estado());var pos=p.position();
            if(s.mundo!=mc.level||s.pos==null||s.entidade!=p.getId()||pos.distanceToSqr(s.pos)>16){if(s.pos!=null)s.tecido.reset();s.mundo=mc.level;s.pos=pos;s.yaw=p.yBodyRot;s.entidade=p.getId();continue;}
            var d=pos.subtract(s.pos);double rad=Math.toRadians(p.yBodyRot),giro=Math.toRadians(Mth.wrapDegrees(p.yBodyRot-s.yaw));
            double frente=-Math.sin(rad)*d.x+Math.cos(rad)*d.z,lado=Math.cos(rad)*d.x+Math.sin(rad)*d.z;
            double golpe=Math.sin(Math.PI*p.getAttackAnim(1))*.14,agacha=p.isCrouching()?.07:0;
            double braco=Math.sin(p.walkAnimation.position()*.6662)*1.4*p.walkAnimation.speed(),db=braco-s.braco;s.braco=braco;
            // Ability impulses (EfeitosCid): a strong one throws the coat outward once, like the Bankai reveal.
            var magia=EfeitosCid.tecido(id);double forca=Math.abs(magia.frente())+Math.abs(magia.lado());if(forca>.15&&s.forca<=.15)s.tecido.impulso(Math.min(.9,forca*3));s.forca=forca;
            s.tecido.tick(new MolaMantoCid.Entrada(frente+agacha+magia.frente(),lado+magia.lado(),d.y,giro+golpe+magia.giro(),p.onGround()),-db,db+golpe*2);s.pos=pos;s.yaw=p.yBodyRot;
        }
        ESTADOS.keySet().removeIf(id->!vistos.contains(id));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ESTADOS.clear();}
}
