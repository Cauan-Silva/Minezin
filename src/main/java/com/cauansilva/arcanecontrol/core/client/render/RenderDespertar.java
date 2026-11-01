package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar;
import com.cauansilva.arcanecontrol.core.client.AnimacoesGojo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Geometria 3D em camadas; avisos usam a mesma posicao/tempo do impacto no servidor. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class RenderDespertar {
    private record Efeito(PacoteEfeitoDespertar p,Object mundo,long inicio){}
    private static final List<Efeito> EFEITOS=new ArrayList<>();
    private static final Vec3 CIMA=new Vec3(0,1,0);
    public static void receber(PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(com.cauansilva.arcanecontrol.core.rpg.PerfilBoss.efeito(p.tipo())){RenderBossVfx.receber(p);return;}
        if(p.kit()==4){com.cauansilva.arcanecontrol.core.asta.EfeitosAsta.receber(p);return;}
        if(p.tipo()==0){ImpactosAnime.cancelar(p.dono());EFEITOS.removeIf(e->e.p.dono().equals(p.dono()));GerenciadorCutscene3D.cancelarDominioCliente(p.dono());if(p.kit()==2)com.cauansilva.arcanecontrol.core.cid.EfeitosCid.iniciar(15,p.dono(),Vec3.ZERO,Vec3.ZERO);AnimacoesGojo.cancelar(p.dono());return;}
        if(p.ticks()<1||p.ticks()>1200||p.kit()<1||p.kit()>3||!finito(p.a())||!finito(p.b()))return;
        if(p.tipo()==200){EFEITOS.removeIf(e->e.p.tipo()==200&&e.p.dono().equals(p.dono()));if(p.kit()==1)GerenciadorCutscene3D.garantirDominio(p.dono(),p.a(),p.ticks());}
        if((p.tipo()>=30&&p.tipo()<=41&&p.tipo()!=31&&(p.tipo()!=34||p.ticks()>10))||p.tipo()==200&&p.kit()==3&&p.ticks()==900)AnimacoesGojo.tocarDesperto(p.dono(),p.kit(),p.tipo());
        if(EFEITOS.size()>=128)EFEITOS.remove(0);
        EFEITOS.add(new Efeito(p,mc.level,mc.level.getGameTime()));
    }
    private static boolean finito(Vec3 v){return Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        EFEITOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=f.p.ticks());
        if(mc.level==null)return;
        for(var f:EFEITOS)if(f.p.tipo()==200){long t=mc.level.getGameTime()-f.inicio;int idade=(f.p.kit()==1?400:f.p.kit()==2?800:900)-f.p.ticks()+(int)t;if(idade==(f.p.kit()==2?100:60))AnimacoesGojo.cancelarSeTipo(f.p.dono(),f.p.kit()==2?14:f.p.kit()==1?5:200);}
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){EFEITOS.clear();}
    @SubscribeEvent public static void tremor(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!com.cauansilva.arcanecontrol.ConfigCliente.CINEMATICAS.get())return;
        for(var f:EFEITOS)if(f.p.tipo()==200&&f.p.kit()==3&&mc.player.distanceToSqr(f.p.a())<24*24){
            double t=mc.level.getGameTime()+e.getPartialTick();float intensidade=(float)((1-Math.sqrt(mc.player.distanceToSqr(f.p.a()))/24)*.08);
            e.setYaw(e.getYaw()+(float)Math.sin(t*1.7)*intensidade);e.setPitch(e.getPitch()+(float)Math.cos(t*1.3)*intensidade);break;
        }
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        double agora=mc.level.getGameTime()+e.getPartialTick();if(!DesenhoMagia.iniciar(agora))return;
        try{for(var f:EFEITOS){
            var p=f.p;if(f.mundo!=mc.level||mc.player.distanceToSqr(p.a())>128*128)continue;
            double t=agora-f.inicio;if(t<0||t>=p.ticks())continue;
            var pose=e.getPoseStack();var cam=e.getCamera().getPosition();
            if(p.tipo()==32&&RenderGojo.ativo()){RenderGojo.roxoMaximo(pose,cam,p.a(),1+t*.1,t,(float)Math.min(1,Math.min(t/3,(p.ticks()-t)/6)));continue;}
            pose.pushPose();pose.translate(p.a().x-cam.x,p.a().y-cam.y,p.a().z-cam.z);
            DesenhoMagia.iniciar(agora);
            try{desenhar(pose,cam.subtract(p.a()),p,t,(float)Math.min(1,Math.min(t/3,(p.ticks()-t)/6)));}finally{pose.popPose();}
        }}finally{DesenhoMagia.terminar();}
    }
    private static void desenhar(PoseStack s,Vec3 cam,PacoteEfeitoDespertar p,double t,float fade){
        int cor=p.kit()==1?0x91DFFF:p.kit()==2?0xA159FF:0xFFE5A0;
        Vec3 fim=p.b().subtract(p.a());
        if(p.tipo()==200){
            if(p.kit()==1)return; // Domo cosmico usa o material exclusivo do Gojo.
            var jogador=Minecraft.getInstance().level.getPlayerByUUID(p.dono());if(jogador==null||!jogador.isAlive())return;
            Vec3 corpo=jogador.position().subtract(p.a());double r=p.kit()==2?20:2.6;
            if(p.kit()==2){DesenhoMagia.esfera(s,cam,Vec3.ZERO,20,2,0x6A22C4,.10f*fade,false,com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?12:18);}
            DesenhoMagia.lote(s,6,true,()->{
                DesenhoMagia.selo(s,cam,p.kit()==2?Vec3.ZERO:corpo.add(0,.1,0),CIMA,r,t,cor,.36f*fade);
                for(int i=0;i<9;i++){double a=t*.035+i*Math.PI*2/9;Vec3 a0=corpo.add(Math.cos(a)*1.2,.2,Math.sin(a)*1.2);Vec3 b=corpo.add(Math.cos(a+.6)*.8,2.6+Math.sin(a)*.3,Math.sin(a+.6)*.8);DesenhoMagia.fita(s,cam,a0,b,.035,0,3,cor,.45f*fade);}
            });return;
        }
        if(p.tipo()>=101&&p.tipo()<=103||p.tipo()==38){
            Vec3 c=p.tipo()==101?fim: p.tipo()<=103?fim:Vec3.ZERO;double r=p.tipo()==101?1.2:p.tipo()==102?4: p.tipo()==103?1.8:4;
            DesenhoMagia.lote(s,6,true,()->{
                DesenhoMagia.selo(s,cam,c.add(0,.06,0),CIMA,r,t,p.tipo()==102?0xFF3C80:cor,fade*.8f);
                DesenhoMagia.anel(s,c.add(0,.08,0),CIMA,r*(1-t/p.ticks()),r*(1-t/p.ticks())+.07,0xFFFFFF,fade,64);
                if(p.tipo()==101||p.tipo()==103)DesenhoMagia.fita(s,cam,new Vec3(0,.08,0),fim.add(0,.08,0),.06,0,6,cor,fade*.7f);
                if(p.tipo()==38)for(int i=0;i<5;i++)DesenhoMagia.anel(s,new Vec3(0,3+i*4,0),CIMA,r*.5,r*.5+.06,cor,fade*.5f,48);
            });return;
        }
        if(p.tipo()==31&&p.kit()==2){
            Vec3 eixo=cam.cross(CIMA).normalize();if(eixo.lengthSqr()<1e-8)eixo=new Vec3(1,0,0);
            final Vec3 x=eixo;
            float a=(float)Math.max(0,1-t/p.ticks());
            DesenhoMagia.lote(s,0,false,()->{
                for(int j=0;j<2;j++)GeometriaCinematica.arco(s,cam,fim,x,CIMA,1.5,.5+j*.22,Math.PI*1.2,.16,cor,a);
            });
            DesenhoMagia.lote(s,1,true,()->{
                for(int j=0;j<2;j++)GeometriaCinematica.arco(s,cam,fim,x,CIMA,1.5,.5+j*.22,Math.PI*1.2,.035,0xE5D3FF,a);
            });return;
        }
        if(p.tipo()==30){
            DesenhoMagia.lote(s,6,true,()->{DesenhoMagia.selo(s,cam,Vec3.ZERO,CIMA,5,t,cor,fade*.7f);
                for(int j=0;j<4;j++)for(int i=0;i<48;i++){double a=i*.22+t*.13+j*Math.PI/2,b=a+.22;double y=i*.1;DesenhoMagia.fita(s,cam,new Vec3(Math.cos(a)*4.5,y,Math.sin(a)*4.5),new Vec3(Math.cos(b)*4.5,y+.1,Math.sin(b)*4.5),.045,0,1,0xC8F4DE,fade*.65f);}
            });return;
        }
        if(p.tipo()==32||p.tipo()==35){double r=p.tipo()==32?1+t*.1:2.4+Math.sin(t*.12)*.2;
            DesenhoMagia.esfera(s,cam,Vec3.ZERO,r,3,p.tipo()==32?0x7145C9:0x250849,fade,false,com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?12:20);
            DesenhoMagia.lote(s,6,true,()->{for(int i=0;i<3;i++){Vec3 n=new Vec3(Math.sin(t*.02+i),Math.cos(i+1),Math.cos(t*.02+i)).normalize();DesenhoMagia.anel(s,Vec3.ZERO,n,r*1.05,r*1.12,cor,fade*.9f,72);}
                for(int i=0;i<12;i++){double a=i*Math.PI/6+t*.04;Vec3 v=new Vec3(Math.cos(a),Math.sin(a*3)*.3,Math.sin(a));DesenhoMagia.fita(s,cam,v.scale(r*1.05),v.scale(r*1.7),.03,0,2,0xE5C7FF,fade);}
            });return;
        }
        if(p.tipo()==39){DesenhoMagia.esfera(s,cam,Vec3.ZERO,8,2,cor,fade*.38f,false,com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?12:20);DesenhoMagia.lote(s,6,true,()->DesenhoMagia.selo(s,cam,Vec3.ZERO,CIMA,8,t,cor,fade));return;}
        if(p.tipo()==34&&p.ticks()>10){DesenhoMagia.lote(s,6,true,()->DesenhoMagia.selo(s,cam,Vec3.ZERO,CIMA,4,t,cor,fade*.65f));return;}
        if(p.tipo()==34){ // Lâmina tridimensional, ponta orientada para o impacto.
            Vec3 c=fim.scale(Math.min(1,t/7));DesenhoMagia.lote(s,0,false,()->{DesenhoMagia.caixa(s,c,.10,1.6,.16,0x20142F,fade);DesenhoMagia.caixa(s,c.add(0,1.1,0),.5,.07,.16,0xC4A15F,fade);});
            DesenhoMagia.lote(s,1,true,()->DesenhoMagia.fita(s,cam,Vec3.ZERO,c,.07,0,5,cor,fade));return;
        }
        if(p.tipo()==112){DesenhoMagia.lote(s,1,true,()->{for(int i=0;i<9;i++){double a=i*Math.PI/4;var v=i==8?Vec3.ZERO:new Vec3(Math.cos(a)*3,0,Math.sin(a)*3);DesenhoMagia.fita(s,cam,v,v.add(0,4,0),.25,0,4,0xF02C64,fade);}});return;}
        if(p.tipo()==36||p.tipo()==40){DesenhoMagia.lote(s,6,true,()->{for(int i=0;i<8;i++){double a=t*.05+i*Math.PI/4;var v=new Vec3(Math.cos(a)*3,0,Math.sin(a)*3);DesenhoMagia.fita(s,cam,v,v.add(0,6,0),.06,0,5,cor,fade*.7f);}DesenhoMagia.selo(s,cam,Vec3.ZERO,CIMA,4,t,cor,fade);});return;}
        if(p.tipo()==37)return; // Os vinte raios recebem extremidades reais por pacotes 113.
        if(p.tipo()==33){
            float a=(float)Math.max(0,1-t/p.ticks());
            DesenhoMagia.lote(s,0,false,()->{
                for(int i=0;i<6;i++){
                    double ang=i*Math.PI/3+Math.floor(t/3)*.7;Vec3 radial=new Vec3(Math.cos(ang),0,Math.sin(ang));
                    GeometriaCinematica.arco(s,cam,radial.scale(4).add(0,1.2,0),radial,CIMA,2.4,.5,Math.PI*1.4,.22,cor,a);
                }
            });
            DesenhoMagia.lote(s,1,true,()->{
                for(int i=0;i<6;i++){
                    double ang=i*Math.PI/3+Math.floor(t/3)*.7;Vec3 radial=new Vec3(Math.cos(ang),0,Math.sin(ang));
                    GeometriaCinematica.arco(s,cam,radial.scale(4).add(0,1.2,0),radial,CIMA,2.4,.5,Math.PI*1.4,.035,0xEAD6FF,a);
                }
            });return;
        }
        DesenhoMagia.lote(s,1,true,()->{
            DesenhoMagia.fita(s,cam,Vec3.ZERO,fim,p.tipo()==113?.32:.15,0,Math.max(1,fim.length()/2),cor,fade*.55f);
            DesenhoMagia.fita(s,cam,Vec3.ZERO,fim,p.tipo()==113?.07:.035,0,4,0xFFFFFF,fade);
            if(p.tipo()==113){var n=fim.normalize();DesenhoMagia.selo(s,cam,Vec3.ZERO,n,1.1,t,cor,fade*.85f);DesenhoMagia.anel(s,fim,n,.2+t*.08,.3+t*.08,cor,fade,48);}
        });
    }
}
