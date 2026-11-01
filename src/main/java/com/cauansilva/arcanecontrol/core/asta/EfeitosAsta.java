package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar;
import com.cauansilva.arcanecontrol.core.client.AnimacoesGojo;
import com.cauansilva.arcanecontrol.core.client.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class EfeitosAsta {
    private record Efeito(PacoteEfeitoDespertar p,Object mundo,long inicio){}
    private static final List<Efeito> EFEITOS=new ArrayList<>();
    private static final Vec3 CIMA=new Vec3(0,1,0);
    public static void receber(PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(p.tipo()==0){EFEITOS.removeIf(e->e.p.dono().equals(p.dono()));AnimacoesGojo.cancelar(p.dono());if(mc.player!=null&&p.dono().equals(mc.player.getUUID())&&GerenciadorCutscene3D.cutsceneTipo==35)GerenciadorCutscene3D.finalizarCutscene();return;}
        if(p.ticks()<1||p.ticks()>1200)return;
        if(p.tipo()==310){EFEITOS.removeIf(e->e.p.dono().equals(p.dono())&&e.p.tipo()==307);receber(new PacoteEfeitoDespertar(p.dono(),309,p.a(),p.b(),p.ticks(),4));return;}
        if(p.tipo()==200){
            EFEITOS.removeIf(e->e.p.dono().equals(p.dono())&&e.p.tipo()==200);
            if(p.ticks()==1200){AnimacoesGojo.tocarAsta(p.dono(),200);if(mc.player!=null&&p.dono().equals(mc.player.getUUID()))GerenciadorCutscene3D.iniciarCutsceneLocal(35,PerfilAsta.ABERTURA);}
        }else if(p.tipo()>=300&&p.tipo()<=307)AnimacoesGojo.tocarAsta(p.dono(),p.tipo());
        if(EFEITOS.size()>=64)EFEITOS.remove(0);
        EFEITOS.add(new Efeito(p,mc.level,mc.level.getGameTime()));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        EFEITOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=f.p.ticks()||f.p.tipo()==200&&(mc.level.getPlayerByUUID(f.p.dono())==null||!mc.level.getPlayerByUUID(f.p.dono()).isAlive()));
        if(mc.level==null)return;
        for(var f:EFEITOS)if(f.p.tipo()==200){int idade=1200-f.p.ticks()+(int)(mc.level.getGameTime()-f.inicio);var p=mc.level.getPlayerByUUID(f.p.dono());
            if(idade==55)AnimacoesGojo.cancelar(f.p.dono());
            if(p!=null&&idade<55&&idade%3==0&&!mc.isPaused())for(int i=0;i<3;i++){double a=idade*.3+i*2.094;mc.level.addParticle(ParticleTypes.SQUID_INK,p.getX()+Math.cos(a),p.getY()+1+i*.3,p.getZ()+Math.sin(a),0,.04,0);}
        }
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){EFEITOS.clear();}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        var camera=e.getCamera().getPosition();double agora=mc.level.getGameTime()+e.getPartialTick();
        for(var f:EFEITOS){
            var p=f.p;double t=agora-f.inicio;if(t<0||t>=p.ticks()||f.mundo!=mc.level)continue;
            var dono=mc.level.getPlayerByUUID(p.dono());Vec3 origem=p.tipo()==200||p.tipo()==303||p.tipo()==302||p.tipo()==304?dono==null?p.a():dono.getPosition(e.getPartialTick()):p.a();
            if(camera.distanceToSqr(origem)>128*128||!DesenhoMagia.iniciar(agora))continue;
            var s=e.getPoseStack();s.pushPose();s.translate(origem.x-camera.x,origem.y-camera.y,origem.z-camera.z);
            try{
                float fade=(float)Math.min(1,Math.min(t/3,(p.ticks()-t)/6));var cam=camera.subtract(origem);
                if(p.tipo()==200&&dono!=null){
                    s.mulPose(Axis.YP.rotationDegrees(180-net.minecraft.util.Mth.rotLerp(e.getPartialTick(),dono.yBodyRotO,dono.yBodyRot)));
                    ModeloFormaAsta.desenhar(s,t,fade,1200-p.ticks()+t);
                    continue;
                }
                if(p.tipo()==104){DesenhoMagia.lote(s,6,true,()->{var c=p.b().subtract(origem);DesenhoMagia.selo(s,cam,c,CIMA,5,t,0xDD294A,fade);DesenhoMagia.anel(s,c,CIMA,5*(1-t/p.ticks()),5*(1-t/p.ticks())+.07,0xF1BAC3,fade,64);});continue;}
                if(p.tipo()==114||p.tipo()==309){DesenhoMagia.lote(s,7,true,()->{for(int i=0;i<12;i++){double a=i*Math.PI/6;var d=new Vec3(Math.cos(a),.2,Math.sin(a));DesenhoMagia.fita(s,cam,d.scale(t*.13),d.scale(t*.13+2),.12,0,2,0xCE2747,fade);}DesenhoMagia.anel(s,Vec3.ZERO,CIMA,t*.2,t*.2+.25,0xBC2043,fade,64);});continue;}
                if(p.tipo()==303){
                    Vec3 dir=dono==null?p.b():dono.getLookAngle();Vec3 c=dir.scale(1.5).add(0,1,0);
                    DesenhoMagia.lote(s,7,false,()->DesenhoMagia.selo(s,cam,c,dir,2.3,t,0xAD1536,fade));
                    DesenhoMagia.lote(s,6,true,()->DesenhoMagia.anel(s,c,dir,2.2,2.35,0xEC4A68,fade,64));continue;
                }
                if(p.tipo()==308){DesenhoMagia.lote(s,7,true,()->{DesenhoMagia.anel(s,Vec3.ZERO,p.b(),.15+t*.06,.25+t*.06,0xFF5A70,fade,40);for(int i=0;i<6;i++){double a=i*Math.PI/3;var v=new Vec3(Math.cos(a),Math.sin(a),0);DesenhoMagia.fita(s,cam,v.scale(.3),v.scale(1.2),.04,0,2,0xF5B2BB,fade);}});continue;}
                Vec3 dir=p.b().normalize();Vec3 lado=dir.cross(CIMA).normalize();if(lado.lengthSqr()<.1)lado=new Vec3(1,0,0);Vec3 alto=lado.cross(dir).normalize();final Vec3 lateral=lado,vertical=alto;
                boolean transformado=p.tipo()>=304;int slot=(p.tipo()-300)%4;
                if(transformado&&slot==1){
                    DesenhoMagia.lote(s,7,true,()->{for(int j=0;j<5;j++)for(int i=0;i<32;i++){double a=i*.23+t*.17+j*Math.PI*.4,b=a+.23;double r=2+Math.min(6,t*.25);DesenhoMagia.fita(s,cam,new Vec3(Math.cos(a)*r,i*.15,Math.sin(a)*r),new Vec3(Math.cos(b)*r,(i+1)*.15,Math.sin(b)*r),.08,0,1,0xC32348,fade);}});continue;
                }
                if(transformado&&slot==0){DesenhoMagia.lote(s,7,true,()->{for(int i=0;i<7;i++){double a=i*Math.PI*2/7+t*.1;Vec3 v=lateral.scale(Math.cos(a)).add(vertical.scale(Math.sin(a)));DesenhoMagia.fita(s,cam,v.add(0,1,0),v.subtract(dir.scale(Math.min(18,t*.9))).add(0,1,0),.15,0,4,0xA31538,fade);}});continue;}
                double distancia=transformado&&slot==3?PerfilAsta.alcanceOnda((int)t):3;Vec3 centro=dir.scale(distancia).add(0,1.2,0);
                DesenhoMagia.lote(s,7,false,()->{
                    int n=32;double plano=slot==1||slot==2?Math.PI/2:0;
                    for(int i=0;i<n;i++){
                        double arco=slot==0&&!transformado?Math.PI*2/3:Math.PI*1.3;double giro=slot==1?t*.08:0;double a=-arco/2+i*arco/n+giro,b=a+arco/n;
                        Vec3 eixo=lateral.scale(Math.cos(plano)).add(vertical.scale(Math.sin(plano)));
                        Vec3 v=eixo.scale(Math.cos(a)).add(vertical.scale(Math.sin(a))),w=eixo.scale(Math.cos(b)).add(vertical.scale(Math.sin(b)));
                        if(slot==1||slot==2){v=vertical.scale(Math.cos(a)).add(dir.scale(Math.sin(a)));w=vertical.scale(Math.cos(b)).add(dir.scale(Math.sin(b)));}
                        double r=transformado?4:2.7;
                        DesenhoMagia.fita(s,cam,centro.add(v.scale(r)),centro.add(w.scale(r)),.25,0,1,0x110912,fade);
                    }
                });
                DesenhoMagia.lote(s,7,true,()->{
                    for(int i=0;i<28;i++){double arco=slot==0&&!transformado?Math.PI*2/3:Math.PI*1.3;double a=-arco/2+i*arco/28+(slot==1?t*.08:0),b=a+arco/28;Vec3 v=lateral.scale(Math.cos(a)).add(vertical.scale(Math.sin(a))),w=lateral.scale(Math.cos(b)).add(vertical.scale(Math.sin(b)));double r=transformado?4.1:2.8;DesenhoMagia.fita(s,cam,centro.add(v.scale(r)),centro.add(w.scale(r)),.045,0,1,0xDF2449,fade);}
                    if(transformado&&slot==2)for(int i=0;i<18;i++){var c=dir.multiply(1,0,1).normalize().scale(i).add(0,.08,0);DesenhoMagia.fita(s,cam,c,c.add(lateral.scale(Math.sin(i*3)*.5)).add(dir),.08,0,1,0xE52848,fade);}
                });
            }finally{s.popPose();DesenhoMagia.terminar();}
        }
    }
}
