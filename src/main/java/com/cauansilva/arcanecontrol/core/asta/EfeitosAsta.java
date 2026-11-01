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
    public record EstadoVisual(double tempo,double idade,float alpha){}
    public static EstadoVisual forma(java.util.UUID id,float parcial){
        var mc=Minecraft.getInstance();if(mc.level==null)return null;
        for(var f:EFEITOS)if(f.p.tipo()==200&&f.mundo==mc.level&&f.p.dono().equals(id)){
            double t=mc.level.getGameTime()+parcial-f.inicio;
            if(t>=0&&t<f.p.ticks())return new EstadoVisual(t,1200-f.p.ticks()+t,(float)Math.min(1,Math.min(t/3,(f.p.ticks()-t)/6)));
        }
        return null;
    }

    public static double divider(java.util.UUID id,float parcial){
        var mc=Minecraft.getInstance();if(mc.level==null)return -1;
        var jogador=mc.level.getPlayerByUUID(id);if(jogador==null||!jogador.isAlive())return -1;
        for(int i=EFEITOS.size()-1;i>=0;i--){var f=EFEITOS.get(i);if(f.p.tipo()==306&&f.mundo==mc.level&&f.p.dono().equals(id)){
            double t=mc.level.getGameTime()+parcial-f.inicio;return t>=0&&t<32?t:-1;
        }}return -1;
    }
    public static void receber(PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(p.tipo()==0){com.cauansilva.arcanecontrol.core.client.AudioAnime.cancelar(p.dono(),null);ImpactosAnime.cancelar(p.dono());EFEITOS.removeIf(e->e.p.dono().equals(p.dono()));AnimacoesGojo.cancelar(p.dono());if(mc.player!=null&&p.dono().equals(mc.player.getUUID())&&GerenciadorCutscene3D.cutsceneTipo==35)GerenciadorCutscene3D.finalizarCutscene();return;}
        if(p.ticks()<1||p.ticks()>1200)return;
        if(p.tipo()==310){EFEITOS.removeIf(e->e.p.dono().equals(p.dono())&&e.p.tipo()==307);receber(new PacoteEfeitoDespertar(p.dono(),309,p.a(),p.b(),p.ticks(),4));return;}
        com.cauansilva.arcanecontrol.core.client.render.UltimatesTransformadas.receber(p);
        if(p.tipo()==306)com.cauansilva.arcanecontrol.core.client.AudioAnime.programar(p.dono(),"divider",p.a(),com.cauansilva.arcanecontrol.core.audio.RoteiroAudio.divider());
        if(p.tipo()==309)ImpactosAnime.programar(p.dono(),p.a().add(0,.7,0),0,0xD5264C,.75,18,4);
        if(p.tipo()==200){
            EFEITOS.removeIf(e->e.p.dono().equals(p.dono())&&e.p.tipo()==200);
            if(p.ticks()==1200){ImpactosAnime.programar(p.dono(),p.a().add(0,1,0),com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico.FORMA_IMPACTO,0xD5264C,.85,24,4);AnimacoesGojo.tocarAsta(p.dono(),200);if(mc.player!=null&&p.dono().equals(mc.player.getUUID()))GerenciadorCutscene3D.iniciarCutsceneLocal(35,PerfilAsta.ABERTURA);}
        }else if(p.tipo()>=300&&p.tipo()<=307)AnimacoesGojo.tocarAsta(p.dono(),p.tipo());
        if(EFEITOS.size()>=64)EFEITOS.remove(0);
        EFEITOS.add(new Efeito(p,mc.level,mc.level.getGameTime()));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        EFEITOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=f.p.ticks()||f.p.tipo()==200&&(mc.level.getPlayerByUUID(f.p.dono())==null||!mc.level.getPlayerByUUID(f.p.dono()).isAlive()));
        if(mc.level==null)return;
        for(var f:EFEITOS)if(f.p.tipo()==200){int idade=1200-f.p.ticks()+(int)(mc.level.getGameTime()-f.inicio);var p=mc.level.getPlayerByUUID(f.p.dono());
            if(idade==55)AnimacoesGojo.cancelarSeTipo(f.p.dono(),200);
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
                if(p.tipo()==200){
                    double idade=1200-p.ticks()+t;
                    if(idade<55){
                        float aura=(float)(Math.min(1,idade/8)*Math.min(1,(55-idade)/8));
                        int n=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?3:6;
                        DesenhoMagia.lote(s,7,false,()->{
                            for(int j=0;j<n;j++)for(int i=0;i<14;i++){
                                double u=i/14d,v=(i+1)/14d,a=j*Math.PI*2/n+idade*.065+u*2.5,b=a+(v-u)*2.5;
                                double r=.7+Math.sin(u*Math.PI)*.6;
                                DesenhoMagia.fita(s,cam,new Vec3(Math.cos(a)*r,u*3,Math.sin(a)*r),new Vec3(Math.cos(b)*r,v*3,Math.sin(b)*r),.18*Math.sin(u*Math.PI),u*4,v*4,0x140B19,aura*.7f);
                            }
                        });
                        DesenhoMagia.lote(s,10,true,()->{
                            for(int j=0;j<n;j++)for(int i=0;i<14;i++){
                                double u=i/14d,v=(i+1)/14d,a=j*Math.PI*2/n+idade*.065+u*2.5,b=a+(v-u)*2.5;
                                double r=.72+Math.sin(u*Math.PI)*.6;
                                DesenhoMagia.fita(s,cam,new Vec3(Math.cos(a)*r,u*3,Math.sin(a)*r),new Vec3(Math.cos(b)*r,v*3,Math.sin(b)*r),.03,u*4,v*4,0xE82951,aura*.7f);
                            }
                        });
                    }
                    continue; // Attached wing/horn/arm geometry is drawn by CamadaFormaAsta.
                }
                if(p.tipo()==104){DesenhoMagia.lote(s,6,true,()->{var c=p.b().subtract(origem);DesenhoMagia.selo(s,cam,c,CIMA,5,t,0xDD294A,fade);DesenhoMagia.anel(s,c,CIMA,5*(1-t/p.ticks()),5*(1-t/p.ticks())+.07,0xF1BAC3,fade,64);});continue;}
                if(p.tipo()==114||p.tipo()==309){DesenhoMagia.lote(s,7,true,()->{for(int i=0;i<12;i++){double a=i*Math.PI/6;var d=new Vec3(Math.cos(a),.2,Math.sin(a));DesenhoMagia.fita(s,cam,d.scale(t*.13),d.scale(t*.13+2),.12,0,2,0xCE2747,fade);}DesenhoMagia.anel(s,Vec3.ZERO,CIMA,t*.2,t*.2+.25,0xBC2043,fade,64);});continue;}
                if(p.tipo()==303){
                    Vec3 dir=dono==null?p.b():dono.getLookAngle();Vec3 c=dir.scale(1.5).add(0,1,0);
                    DesenhoMagia.lote(s,7,false,()->DesenhoMagia.selo(s,cam,c,dir,2.3,t,0xAD1536,fade));
                    DesenhoMagia.lote(s,6,true,()->DesenhoMagia.anel(s,c,dir,2.2,2.35,0xEC4A68,fade,64));continue;
                }
                if(p.tipo()==308){DesenhoMagia.lote(s,7,true,()->{DesenhoMagia.anel(s,Vec3.ZERO,p.b(),.15+t*.06,.25+t*.06,0xFF5A70,fade,40);for(int i=0;i<6;i++){double a=i*Math.PI/3;var v=new Vec3(Math.cos(a),Math.sin(a),0);DesenhoMagia.fita(s,cam,v.scale(.3),v.scale(1.2),.04,0,2,0xF5B2BB,fade);}});continue;}
                Vec3 dir=p.b().normalize();if(dir.lengthSqr()<1e-8)dir=new Vec3(0,0,1);
                Vec3 lado=dir.cross(CIMA).normalize();if(lado.lengthSqr()<.1)lado=new Vec3(1,0,0);
                final Vec3 frente=dir,lateral=lado,vertical=lado.cross(dir).normalize();
                boolean transformado=p.tipo()>=304;int slot=(p.tipo()-300)%4;
                int acerto=PerfilAsta.impacto(slot,transformado);
                // No completed slash before the server's hit. Charge stays near the weapon/hand.
                if(t<acerto-2){
                    float carga=(float)Math.min(1,(t+1)/Math.max(1,acerto));
                    DesenhoMagia.lote(s,1,true,()->{
                        Vec3 mao=lateral.scale(.55).add(0,1.1,0).add(frente.scale(.7));
                        for(int i=0;i<4;i++){
                            double a=i*Math.PI/2+t*.2;Vec3 v=lateral.scale(Math.cos(a)).add(vertical.scale(Math.sin(a)));
                            DesenhoMagia.fita(s,cam,mao.add(v.scale(.7*(1-carga))),mao,.025,0,1,0xD62348,carga*.7f);
                        }
                    });continue;
                }
                float alfa=transformado&&slot==3?fade:(float)com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico.corte(t,acerto);
                if(transformado&&slot==1){
                    double abertura=com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo.suave((t-acerto+2)/7);
                    DesenhoMagia.lote(s,7,false,()->{for(int j=0;j<3;j++){
                        double a=t*.15+j*Math.PI*2/3;Vec3 eixo=lateral.scale(Math.cos(a)).add(frente.scale(Math.sin(a)));
                        GeometriaCinematica.arco(s,cam,new Vec3(0,1,0),eixo,CIMA,1+7*abertura,.5,Math.PI*1.8,.24,0x130A14,alfa);
                    }});
                    DesenhoMagia.lote(s,10,true,()->{for(int j=0;j<3;j++){
                        double a=t*.15+j*Math.PI*2/3;Vec3 eixo=lateral.scale(Math.cos(a)).add(frente.scale(Math.sin(a)));
                        GeometriaCinematica.arco(s,cam,new Vec3(0,1,0),eixo,CIMA,1+7*abertura,.5,Math.PI*1.8,.07,0xD92C50,alfa);
                    }});continue;
                }
                if(transformado&&slot==0){
                    DesenhoMagia.lote(s,7,false,()->{for(int i=0;i<5;i++){
                        double a=i*Math.PI*2/5+t*.13;Vec3 v=lateral.scale(Math.cos(a)).add(vertical.scale(Math.sin(a)));
                        DesenhoMagia.fita(s,cam,v.add(0,1,0),v.subtract(frente.scale(Math.min(12,t*.8))).add(0,1,0),.24,0,4,0x160B18,alfa);
                    }});
                    DesenhoMagia.lote(s,10,true,()->{for(int i=0;i<5;i++){
                        double a=i*Math.PI*2/5+t*.13;Vec3 v=lateral.scale(Math.cos(a)).add(vertical.scale(Math.sin(a)));
                        DesenhoMagia.fita(s,cam,v.add(0,1,0),v.subtract(frente.scale(Math.min(12,t*.8))).add(0,1,0),.045,0,4,0xE8274D,alfa);
                    }});continue;
                }
                double distancia=transformado&&slot==3?PerfilAsta.alcanceOnda((int)t):2.1;
                Vec3 centro=frente.scale(distancia).add(0,1.2,0);
                final Vec3 eixo=slot==1||slot==2?vertical:lateral;
                final Vec3 alto=slot==1||slot==2?frente:vertical;
                double progresso=com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico.varredura(t,acerto);
                double raio=transformado?4:2.7,arco=slot==0?Math.PI*1.25:Math.PI*1.5;
                // Identical plane, center and radius for black body, red rim and white edge.
                DesenhoMagia.lote(s,7,false,()->GeometriaCinematica.arco(s,cam,centro,eixo,alto,raio,progresso,arco,.32,0x130A15,alfa));
                DesenhoMagia.lote(s,10,true,()->{
                    GeometriaCinematica.arco(s,cam,centro,eixo,alto,raio+.05,progresso,arco,.065,0xED264E,alfa);
                    if(transformado&&slot==2&&t>=acerto)for(int i=0;i<18;i++){
                        var c=frente.multiply(1,0,1).normalize().scale(i).add(0,.08,0);
                        DesenhoMagia.fita(s,cam,c,c.add(lateral.scale(Math.sin(i*3)*.5)).add(frente),.09,0,1,0xE52848,alfa);
                    }
                });
            }finally{s.popPose();DesenhoMagia.terminar();}
        }
    }
}
