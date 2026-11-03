package com.cauansilva.arcanecontrol.core.ichigo;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.cid.MolaMantoCid;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Sword accessories as cloth: Zangetsu's ribbon (cloth, cloth_1..3) and Tensa Zangetsu's chain (chain, chain_1..3).
 *  A held item moves mostly with the arm and the view, not with walking (first test in game: the ribbon looked static),
 *  so the damped 4-stage spring (MolaMantoCid) is driven by view yaw/pitch changes, the arm swing of walking, sword
 *  swings and the body motion, with a strong gain; a continuous flutter travels down the strip. 20 Hz. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class TecidoEspadaIchigo {
    private static final class Estado{final MolaMantoCid mola=new MolaMantoCid(1.8);double tempo,agitacao,braco;Object mundo;Vec3 pos;float yaw,pitch;int entidade;}
    private static final Map<UUID,Estado> ESTADOS=new HashMap<>();
    private static final MolaMantoCid.Pose ZERO=new MolaMantoCid.Pose(0,0,0);
    /** Segment of an accessory bone (0..3), or -1 if the bone is not an accessory. */
    public static int segmento(String osso){
        if(osso.equals("cloth")||osso.equals("chain"))return 0;
        if(osso.startsWith("cloth_")||osso.startsWith("chain_"))try{return Math.min(3,Integer.parseInt(osso.substring(6)));}catch(NumberFormatException e){return -1;}
        return -1;
    }
    public static MolaMantoCid.Pose pose(UUID id,String osso,float parcial){
        int k=segmento(osso);var e=ESTADOS.get(id);if(k<0||e==null||!ConfigCliente.TECIDO_ICHIGO.get())return ZERO;
        var a=e.mola.amostrar(k,parcial);var b=k==0?ZERO:e.mola.amostrar(k-1,parcial);
        // Chain links are heavier than the ribbon: less whip and flutter on the far end.
        boolean corrente=osso.startsWith("chain");double g=(corrente?3.2:4.6)*(k==0?.8:1+.35*k),t=e.tempo+parcial,u=(k+1)/4.0;
        // Never still (user: "muito duro"): a pendulum sway that travels down the strip even when idle, plus a fast
        // flutter that grows with agitation; the chain swings slower and wider, the ribbon flutters more.
        double ag=e.agitacao,bal=(corrente?.14:.12)*(.6+ag)*u*Math.sin(t*(corrente?.32:.45)-k*.8),
               tremor=ag*(corrente?.1:.22)*u*(Math.sin(t*1.1-k*1.2)+.6*Math.sin(t*2.7-k*2.1)),
               lateral=(corrente?.1:.08)*(.5+ag)*u*Math.sin(t*(corrente?.27:.38)+1.3-k*.9);
        return new MolaMantoCid.Pose(lim((a.x()-b.x())*g+tremor+bal,1.3),lim((a.y()-b.y())*g*.6+tremor*.3,.6),lim((a.z()-b.z())*g+tremor*.7+lateral,1.3));
    }
    private static double lim(double v,double m){return Math.max(-m,Math.min(m,v));}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.level==null||!ConfigCliente.TECIDO_ICHIGO.get()){ESTADOS.clear();return;}if(mc.isPaused())return;
        var vistos=new HashSet<UUID>();
        for(var p:mc.level.players()){
            if(!p.isAlive()||p.isSpectator()||!p.getMainHandItem().is(RegistroIchigo.ESPADA.get()))continue;
            var id=p.getUUID();if(!ESTADOS.containsKey(id)&&ESTADOS.size()>=64)continue;vistos.add(id);var s=ESTADOS.computeIfAbsent(id,k->new Estado());var pos=p.position();
            if(s.mundo!=mc.level||s.pos==null||s.entidade!=p.getId()||pos.distanceToSqr(s.pos)>16){s.mola.reset();s.mundo=mc.level;s.pos=pos;s.yaw=p.getYHeadRot();s.pitch=p.getXRot();s.entidade=p.getId();continue;}
            var d=pos.subtract(s.pos);double rad=Math.toRadians(p.yBodyRot);s.tempo++;
            double frente=-Math.sin(rad)*d.x+Math.cos(rad)*d.z,lado=Math.cos(rad)*d.x+Math.sin(rad)*d.z;
            double giro=Math.toRadians(Mth.wrapDegrees(p.getYHeadRot()-s.yaw)),arfagem=Math.toRadians(p.getXRot()-s.pitch);
            double braco=Math.sin(p.walkAnimation.position()*.6662)*1.4*p.walkAnimation.speed(),db=braco-s.braco;s.braco=braco;
            double golpe=Math.sin(Math.PI*p.getAttackAnim(1))*.6;
            s.agitacao+=(Math.min(2,.45+Math.hypot(frente,lado)*4+Math.abs(giro)*3+Math.abs(arfagem)*3+golpe)-s.agitacao)*.3;
            s.mola.tick(new MolaMantoCid.Entrada((frente+db*.5+arfagem*1.1)*3.2,(lado+giro*.9)*3.2,d.y*3+arfagem*.8,giro*2.4+golpe*1.5,p.onGround()));
            s.pos=pos;s.yaw=p.getYHeadRot();s.pitch=p.getXRot();
        }
        ESTADOS.keySet().removeIf(id->!vistos.contains(id));
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){ESTADOS.clear();}
}
