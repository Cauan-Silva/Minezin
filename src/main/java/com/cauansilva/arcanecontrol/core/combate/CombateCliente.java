package com.cauansilva.arcanecontrol.core.combate;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.client.TeclasControlo;
import com.cauansilva.arcanecontrol.core.client.render.GerenciadorCutscene3D;
import com.cauansilva.arcanecontrol.core.rede.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Polimento de combate no cliente (so apresentacao + o dash local):
 * hitstop (relogio do cliente quase parado por 35-110 ms), tremor direcional, Perfect Dodge com camera lenta e
 * Lock-on com FOV dinamico. O servidor decide i-frames, perfeita e contra-ataque.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class CombateCliente {
    private static final float TICK_MS=50f;
    private static long hitstopAte,lentoAte,perfeitoAte,tremorInicio,contraAte;
    private static double tremor,dirX,dirZ;
    private static long ultimaEsquiva;private static int esquivaFov;
    private static LivingEntity alvo;private static int semVisao;

    static void receber(PacoteCombate p){
        long agora=System.currentTimeMillis();
        if(p.tipo()==PacoteCombate.IMPACTO){
            if(!ConfigCliente.CINEMATICAS.get())return;
            int ms=RegrasCombate.hitstopMs(p.forca());if(p.apanhou())ms=(int)(ms*.7);
            if(ConfigCliente.VFX_REDUZIDOS.get())ms/=2;
            hitstopAte=Math.max(hitstopAte,agora+ms);
            double t=RegrasCombate.tremor(p.forca(),p.apanhou());
            if(t>=tremor*decaimento(agora)){tremor=t;tremorInicio=agora;dirX=p.dx();dirZ=p.dz();}
        }else if(p.tipo()==PacoteCombate.PERFEITA){lentoAte=agora+420;perfeitoAte=agora+900;ultimaEsquiva-=RegrasCombate.RECARGA_ESQUIVA/2;}
        else if(p.tipo()==PacoteCombate.CONTRA)contraAte=agora+500;
    }
    private static double decaimento(long agora){double t=(agora-tremorInicio)/260.0;return t>=1?0:(1-t)*(1-t);}

    // ---------------- relogio (hitstop / camera lenta) ----------------
    @SubscribeEvent public static void quadro(TickEvent.RenderTickEvent e){
        if(e.phase!=TickEvent.Phase.START)return;var mc=Minecraft.getInstance();long agora=System.currentTimeMillis();
        float escala=mc.isPaused()||mc.level==null?1:agora<hitstopAte?.06f:agora<lentoAte?.35f:1;
        float alvoMs=TICK_MS/escala;if(mc.timer.msPerTick!=alvoMs)mc.timer.msPerTick=alvoMs;
        mira(mc,e.renderTickTime);
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){Minecraft.getInstance().timer.msPerTick=TICK_MS;alvo=null;hitstopAte=lentoAte=0;}

    // ---------------- teclas: esquiva e lock-on ----------------
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||mc.level==null){alvo=null;return;}
        if(esquivaFov>0)esquivaFov--;
        boolean livre=mc.screen==null&&GerenciadorCutscene3D.cutsceneTipo==0&&p.isAlive()&&!p.isSpectator();
        while(TeclasControlo.ESQUIVA.consumeClick())if(livre)esquivar(mc);
        while(TeclasControlo.LOCK_ON.consumeClick())if(livre)alvo=alvo!=null?null:escolher(mc);
        if(alvo!=null){
            double d=alvo.distanceTo(p);
            if(!livre||!alvo.isAlive()||alvo.isRemoved()||alvo.level()!=p.level()||d>RegrasCombate.SOLTA_LOCK)alvo=null;
            else{semVisao=p.hasLineOfSight(alvo)?0:semVisao+1;if(semVisao>40)alvo=null;}
        }
    }
    private static void esquivar(Minecraft mc){
        var p=mc.player;long agora=p.level().getGameTime();
        if(!RegrasCombate.podeEsquivar(agora,ultimaEsquiva)||p.isPassenger()||p.getAbilities().flying)return;
        ultimaEsquiva=agora;
        float frente=p.input.forwardImpulse,lado=p.input.leftImpulse;
        if(Math.abs(frente)<.1f&&Math.abs(lado)<.1f)frente=-1;           // parado: esquiva para tras
        float yaw=p.getYRot()*Mth.DEG_TO_RAD;
        Vec3 dir=new Vec3(lado*Mth.cos(yaw)-frente*Mth.sin(yaw),0,frente*Mth.cos(yaw)+lado*Mth.sin(yaw)).normalize();
        double impulso=RegrasCombate.IMPULSO*(p.onGround()?1:.6);
        p.setDeltaMovement(dir.x*impulso,p.onGround()?.18:Math.max(p.getDeltaMovement().y,.05),dir.z*impulso);
        esquivaFov=6;
        RegistoRede.ENVIAR.sendToServer(new PacoteAcaoRpg(11,0,0));
    }
    private static LivingEntity escolher(Minecraft mc){
        var p=mc.player;Vec3 olho=p.getEyePosition(),olhar=p.getViewVector(1);LivingEntity melhor=null;double nota=Double.POSITIVE_INFINITY;
        for(var e:mc.level.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(RegrasCombate.ALCANCE_LOCK),x->x!=p&&x.isAlive()&&!(x instanceof ArmorStand)&&!x.isInvisible())){
            Vec3 para=e.getBoundingBox().getCenter().subtract(olho);double dist=para.length();if(dist<1e-3)continue;
            double ang=Math.toDegrees(Math.acos(Mth.clamp(para.normalize().dot(olhar),-1,1)));
            double n=RegrasCombate.pontuar(ang,dist);if(n<nota&&p.hasLineOfSight(e)){nota=n;melhor=e;}
        }
        return melhor;
    }
    private static void mira(Minecraft mc,float parcial){
        var p=mc.player;if(alvo==null||p==null||mc.isPaused())return;
        Vec3 para=alvo.getPosition(parcial).add(0,alvo.getBbHeight()*.6,0).subtract(p.getEyePosition(parcial));
        float yaw=(float)(Mth.atan2(para.z,para.x)*Mth.RAD_TO_DEG)-90f,pitch=(float)-(Mth.atan2(para.y,Math.hypot(para.x,para.z))*Mth.RAD_TO_DEG);
        float y=RegrasCombate.aproximar(p.getYRot(),yaw,.25f),x=Mth.clamp(RegrasCombate.aproximar(p.getXRot(),pitch,.2f),-70,70);
        p.setYRot(y);p.setXRot(x);p.yRotO=y;p.xRotO=x;p.setYHeadRot(y);
    }

    // ---------------- camera ----------------
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
        var mc=Minecraft.getInstance();if(mc.player==null||GerenciadorCutscene3D.cutsceneTipo!=0)return;
        long agora=System.currentTimeMillis();double k=tremor*decaimento(agora);if(k<=.01)return;
        if(ConfigCliente.VFX_REDUZIDOS.get())k*=.4;
        // tremor direcional: empurra a camera para o lado do golpe (yaw/roll) com oscilacao curta
        float yawCam=mc.player.getYRot()*Mth.DEG_TO_RAD;double lado=dirX*Mth.cos(yawCam)+dirZ*Mth.sin(yawCam),frente=-dirX*Mth.sin(yawCam)+dirZ*Mth.cos(yawCam);
        double osc=Math.sin(agora*.09);
        e.setRoll((float)(e.getRoll()+lado*k*2.2*osc));e.setYaw((float)(e.getYaw()+lado*k*.9*osc));e.setPitch((float)(e.getPitch()+(Math.abs(frente)*.6+.4)*k*Math.cos(agora*.11)));
    }
    @SubscribeEvent public static void fov(ComputeFovModifierEvent e){
        double f=1;
        if(alvo!=null&&e.getPlayer()==Minecraft.getInstance().player)f*=RegrasCombate.fov(alvo.distanceTo(e.getPlayer()));
        if(esquivaFov>0)f*=1+.05*esquivaFov/6.0;
        if(System.currentTimeMillis()<lentoAte)f*=.94;
        if(f!=1)e.setNewFovModifier((float)(e.getNewFovModifier()*f));
    }

    // ---------------- HUD ----------------
    @SubscribeEvent public static void hud(RenderGuiOverlayEvent.Post e){
        if(e.getOverlay()!=VanillaGuiOverlay.CROSSHAIR.type())return;var mc=Minecraft.getInstance();if(mc.options.hideGui)return;
        var g=e.getGuiGraphics();int w=e.getWindow().getGuiScaledWidth(),h=e.getWindow().getGuiScaledHeight(),cx=w/2,cy=h/2;long agora=System.currentTimeMillis();
        if(alvo!=null){
            int c=0xFFE8433C;g.fill(cx-7,cy-7,cx-4,cy-6,c);g.fill(cx-7,cy-7,cx-6,cy-4,c);g.fill(cx+4,cy-7,cx+7,cy-6,c);g.fill(cx+6,cy-7,cx+7,cy-4,c);
            g.fill(cx-7,cy+6,cx-4,cy+7,c);g.fill(cx-7,cy+4,cx-6,cy+7,c);g.fill(cx+4,cy+6,cx+7,cy+7,c);g.fill(cx+6,cy+4,cx+7,cy+7,c);
            var nome=alvo.getDisplayName();int bw=60;float fv=alvo.getHealth()/Math.max(1,alvo.getMaxHealth());
            g.drawCenteredString(mc.font,nome,cx,cy-24,0xFFFFFFFF);
            g.fill(cx-bw/2,cy-14,cx+bw/2,cy-12,0xC0200808);g.fill(cx-bw/2,cy-14,cx-bw/2+(int)(bw*fv),cy-12,0xFFC0322A);
        }
        if(agora<lentoAte){int a=(int)(90*(lentoAte-agora)/420.0);g.fill(0,0,w,h,(a<<24)|0x1A3A6A);}
        if(agora<perfeitoAte){float t=1-(perfeitoAte-agora)/900f;int a=(int)(255*Math.max(.1,1-t));
            g.pose().pushPose();g.pose().translate(cx,cy+22,0);g.pose().scale(1.6f,1.6f,1);
            g.drawCenteredString(mc.font,"PERFEITO",0,0,(a<<24)|0x9FE4FF);g.pose().popPose();}
        if(agora<contraAte)g.drawCenteredString(mc.font,"CONTRA-ATAQUE",cx,cy+38,(int)(255*(contraAte-agora)/500.0)<<24|0xF2C14E);
        // recarga da esquiva: arco simples sob a mira
        var p=mc.player;if(p!=null){long passou=p.level().getGameTime()-ultimaEsquiva;
            if(ultimaEsquiva>0&&passou<RegrasCombate.RECARGA_ESQUIVA){int lw=(int)(16*(1-passou/(double)RegrasCombate.RECARGA_ESQUIVA));g.fill(cx-8,cy+10,cx-8+lw,cy+11,0xA0FFFFFF);}}
    }
    private CombateCliente(){}
}
