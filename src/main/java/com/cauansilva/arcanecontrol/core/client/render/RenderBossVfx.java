package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar;
import com.cauansilva.arcanecontrol.core.rpg.PerfilBoss;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Boss telegraphs and impacts are independent of player-kit renderers. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class RenderBossVfx {
    private record Efeito(PacoteEfeitoDespertar pacote,Object mundo,long inicio){}
    private static final List<Efeito> EFEITOS=new ArrayList<>();
    private static final Vec3 UP=new Vec3(0,1,0);
    public static void receber(PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();
        if(mc.level==null||!PerfilBoss.efeito(p.tipo())||p.kit()<1||p.kit()>4||p.ticks()<1||p.ticks()>60||!finito(p.a())||!finito(p.b()))return;
        if(EFEITOS.size()>=64)EFEITOS.remove(0);
        EFEITOS.add(new Efeito(p,mc.level,mc.level.getGameTime()));
    }
    private static boolean finito(Vec3 v){return Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.END){var l=Minecraft.getInstance().level;EFEITOS.removeIf(f->f.mundo!=l||l==null||l.getGameTime()-f.inicio>=f.pacote.ticks());}}
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){EFEITOS.clear();}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!com.cauansilva.arcanecontrol.ConfigCliente.CINEMATICAS.get())return;
        double trauma=0;
        for(var f:EFEITOS)if(f.pacote.tipo()>=411){double t=mc.level.getGameTime()+e.getPartialTick()-f.inicio;trauma=Math.max(trauma,PerfilBoss.trauma(t,Math.sqrt(mc.player.distanceToSqr(centro(f.pacote)))));}
        double t=mc.level.getGameTime()+e.getPartialTick();
        e.setPitch(e.getPitch()+(float)(Math.sin(t*2.1)*trauma*trauma*2));e.setRoll(e.getRoll()+(float)(Math.cos(t*1.7)*trauma*trauma));
    }
    private static Vec3 centro(PacoteEfeitoDespertar p){return p.tipo()==411||p.tipo()==413?p.b():p.a();}
    @SubscribeEvent public static void impactFrame(RenderGuiOverlayEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!com.cauansilva.arcanecontrol.ConfigCliente.IMPACT_FRAMES.get()
                ||com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()||!e.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id()))return;
        Efeito escolhido=null;double idade=0;
        for(var f:EFEITOS)if(f.mundo==mc.level&&f.pacote.tipo()>=411){
            double t=mc.level.getGameTime()+e.getPartialTick()-f.inicio;
            Vec3 pos=centro(f.pacote),dir=pos.subtract(mc.gameRenderer.getMainCamera().getPosition());
            if(t<0||t>=2||dir.lengthSqr()>24*24||dir.lengthSqr()>4&&dir.normalize().dot(mc.player.getLookAngle())<.15)continue;
            var hit=mc.level.clip(new net.minecraft.world.level.ClipContext(mc.gameRenderer.getMainCamera().getPosition(),pos.add(0,.5,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,mc.player));
            if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.MISS&&hit.getLocation().distanceToSqr(pos.add(0,.5,0))>2)continue;
            if(escolhido==null||f.inicio>escolhido.inicio){escolhido=f;idade=t;}
        }
        if(escolhido==null)return;
        var g=e.getGuiGraphics();int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();
        if(idade<.65)g.fill(0,0,w,h,0xB8FFFFFF);
        else{
            g.fill(0,0,w,h,0x68000000);var pose=g.pose();pose.pushPose();pose.translate(w/2f,h/2f,0);
            try{for(int i=0;i<18;i++){
                pose.pushPose();pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(i*20f+((i%3)*4)));
                g.fill(w/6+(i%3)*4,-1,w/2+(i%4)*12,1,0xEFFFFFFF);pose.popPose();
            }}finally{pose.popPose();}
        }
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        double agora=mc.level.getGameTime()+e.getPartialTick();
        for(var f:EFEITOS){var p=f.pacote;if(f.mundo!=mc.level||mc.player.distanceToSqr(p.a())>128*128)continue;
            double t=agora-f.inicio;if(t<0||t>=p.ticks())continue;if(!DesenhoMagia.iniciar(agora))continue;
            var s=e.getPoseStack();var camera=e.getCamera().getPosition();s.pushPose();s.translate(p.a().x-camera.x,p.a().y-camera.y,p.a().z-camera.z);
            try{desenhar(s,camera.subtract(p.a()),p,t);}finally{s.popPose();DesenhoMagia.terminar();}
        }
    }
    private static void desenhar(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,PacoteEfeitoDespertar p,double t){
        int cor=p.kit()==1?0xD9C7F2:p.kit()==2?0xC31E50:p.kit()==3?0xBA8BE0:0xB51535;
        float fade=(float)Math.min(1,Math.min((t+1)/2,(p.ticks()-t)/5));Vec3 end=p.b().subtract(p.a());
        int segments=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?16:32;
        if(p.tipo()<411){
            double raio=p.kit()==1?1.2:p.kit()==2?4:p.kit()==3?1.8:5;
            DesenhoMagia.lote(s,6,true,()->{
                DesenhoMagia.anel(s,end.add(0,.06,0),UP,raio-.06,raio,cor,fade,segments);
                double r=raio*(1-t/p.ticks());DesenhoMagia.anel(s,end.add(0,.08,0),UP,r,r+.06,0xFFFFFF,fade,segments);
                if(p.kit()==1||p.kit()==3)DesenhoMagia.fita(s,cam,new Vec3(0,.1,0),end.add(0,.1,0),.04,0,4,cor,fade);
                for(int i=0;i<8;i++){double a=i*Math.PI/4;Vec3 v=end.add(Math.cos(a)*raio,.08,Math.sin(a)*raio);DesenhoMagia.fita(s,cam,v,v.add(0,.4,0),.04,0,1,cor,fade);}
            });return;
        }
        Vec3 impact=p.tipo()==411||p.tipo()==413?end:Vec3.ZERO;
        if(t<9)DesenhoMagia.lote(s,6,true,()->{
            double r=Math.min(6,.5+t*.8);
            DesenhoMagia.anel(s,impact.add(0,.1,0),UP,Math.max(0,r-.16),r,cor,fade,segments);
            int count=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?5:14;
            for(int i=0;i<count;i++){
                double a=i*Math.PI*2/count;Vec3 radial=new Vec3(Math.cos(a),.25+Math.sin(i*2.4)*.4,Math.sin(a));
                Vec3 start=impact.add(0,.5,0),prev=start;
                for(int j=1;j<=4;j++){
                    Vec3 next=start.add(radial.scale(j*r/4)).add(Math.sin(i*7+j*3)*.18,Math.cos(i*5+j*2)*.2,Math.cos(i*3+j)*.18);
                    DesenhoMagia.fita(s,cam,prev,next,.025,0,1,j%2==0?0xFFFFFF:cor,fade);prev=next;
                }
            }
        });
        if(p.tipo()==411){DesenhoMagia.lote(s,1,true,()->{DesenhoMagia.fita(s,cam,Vec3.ZERO,end,.16,0,4,0xECE1FF,fade);for(int i=0;i<4;i++)DesenhoMagia.fita(s,cam,new Vec3(0,i*.14-.2,0),end.add(0,i*.14-.2,0),.025,0,5,cor,fade);});return;}
        if(p.tipo()==413){DesenhoMagia.lote(s,1,true,()->{DesenhoMagia.fita(s,cam,Vec3.ZERO,end,.35,0,8,cor,fade);DesenhoMagia.fita(s,cam,Vec3.ZERO,end,.07,0,8,0xFFFFFF,fade);});DesenhoMagia.lote(s,6,true,()->{DesenhoMagia.selo(s,cam,Vec3.ZERO,end.normalize(),1.25,t,cor,fade);DesenhoMagia.anel(s,end,end.normalize(),.3+t*.11,.42+t*.11,cor,fade,segments);});return;}
        if(p.tipo()==412){DesenhoMagia.lote(s,1,true,()->{for(int i=0;i<9;i++){double a=i*Math.PI/4;Vec3 v=i==8?Vec3.ZERO:new Vec3(Math.cos(a)*3,0,Math.sin(a)*3);double h=4*Math.min(1,(t+1)/2);DesenhoMagia.fita(s,cam,v,v.add(0,h,0),.23,0,4,cor,fade);DesenhoMagia.fita(s,cam,v.add(-.5,h*.65,0),v.add(.5,h*.65,0),.05,0,1,0xFFCEDA,fade);}});}
        else{DesenhoMagia.lote(s,7,true,()->{for(int i=0;i<8;i++){double a=i*Math.PI/4;Vec3 v=new Vec3(Math.cos(a)*5,0,Math.sin(a)*5);DesenhoMagia.fita(s,cam,Vec3.ZERO,v.add(0,2.5,0),.24,0,3,cor,fade);}});}
        DesenhoMagia.lote(s,6,true,()->{double r=Math.min(5,t*.55);DesenhoMagia.anel(s,new Vec3(0,.08,0),UP,Math.max(0,r-.13),r,cor,fade,segments);});
    }
}
