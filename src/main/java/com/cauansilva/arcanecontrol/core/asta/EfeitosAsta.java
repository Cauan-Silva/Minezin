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
                        int n=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?3:5;
                        // Entry: black anti-magic lightning spiralling up the body (same style as the approved base strikes).
                        raios(s,cam,new Vec3(0,0,1),new Vec3(-1,0,0),(u,v)->{double ang=v*Math.PI*2+idade*.065+u*2.5,r=.7+Math.sin(u*Math.PI)*.6;return new com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.P(Math.cos(ang)*r,u*3,Math.sin(ang)*r);},idade,.35,.2,aura,n);
                    }                    // Floating black orbs with a red rim around the body, as in the Devil Union references.
                    int orbes=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?3:6;float presenca=(float)com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo.suave((idade-20)/20);
                    if(presenca>.01)for(int k=0;k<orbes;k++){double ang=k*Math.PI*2/orbes+idade*.02,r=2.3+.4*Math.sin(k*1.7);Vec3 c=new Vec3(Math.cos(ang)*r,1.0+k%3*.8+Math.sin(idade*.05+k)*.25,Math.sin(ang)*r);DesenhoMagia.esfera(s,cam,c,.18+.08*(k%2),3,0xFF2A3D,presenca,false,10);}
                    continue; // Attached wing/horn/arm geometry is drawn by CamadaFormaAsta.
                }
                if(p.tipo()==104){DesenhoMagia.lote(s,6,true,()->{var c=p.b().subtract(origem);DesenhoMagia.selo(s,cam,c,CIMA,5,t,0xDD294A,fade);DesenhoMagia.anel(s,c,CIMA,5*(1-t/p.ticks()),5*(1-t/p.ticks())+.07,0xF1BAC3,fade,64);});continue;}
                if(p.tipo()==114||p.tipo()==309){DesenhoMagia.lote(s,7,true,()->{for(int i=0;i<12;i++){double a=i*Math.PI/6;var d=new Vec3(Math.cos(a),.2,Math.sin(a));DesenhoMagia.fita(s,cam,d.scale(t*.13),d.scale(t*.13+2),.12,0,2,0xCE2747,fade);}DesenhoMagia.anel(s,Vec3.ZERO,CIMA,t*.2,t*.2+.25,0xBC2043,fade,64);});continue;}
                if(p.tipo()==303){
                    Vec3 dir=dono==null?p.b():dono.getLookAngle();Vec3 c=dir.scale(1.5).add(0,1,0);
                    DesenhoMagia.lote(s,7,false,()->DesenhoMagia.selo(s,cam,c,dir,2.3,t,0xAD1536,fade));
                    DesenhoMagia.lote(s,6,true,()->DesenhoMagia.anel(s,c,dir,2.2,2.35,0xEC4A68,fade,64));continue;
                }
                if(p.tipo()==306){blackDivider(s,cam,p.b(),t);continue;}
                if(p.tipo()>=300&&p.tipo()<=303){golpeBase(s,cam,p.tipo()-300,p.tipo()==303&&dono!=null?dono.getLookAngle():p.b(),t);continue;}
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
    private static final int TINTA=0x140A12,VERMELHO=0xE01E3A;
    /** Black Divider (306): ink gathers on the blade, then the black crescent and the red-rimmed rift on the server's hit tick. */
    private static void blackDivider(PoseStack s,Vec3 cam,Vec3 olhar,double t){
        Vec3 f0=new Vec3(olhar.x,0,olhar.z);final Vec3 frente=f0.lengthSqr()<1e-8?new Vec3(0,0,1):f0.normalize();
        final Vec3 lado=frente.cross(CIMA).normalize();
        java.util.function.Function<com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.P,Vec3> w=q->lado.scale(q.x()).add(0,q.y(),0).add(frente.scale(q.z()));
        Vec3 mao=lado.scale(.55).add(0,1.1,0).add(frente.scale(.6));
        double carga=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.carga(t),condensa=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.condensa(t);
        if(carga>.01){
            DesenhoMagia.lote(s,22,false,()->{
                for(int j=0;j<com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.FIOS;j++){
                    Vec3 ant=null;
                    for(int i=0;i<=16;i++){double u=i/16d;Vec3 q=mao.add(w.apply(com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.fio(j,u,t)));float a=(float)Math.min(1,com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.alphaFio(j,u,t)*1.8);if(ant!=null&&a>.02)DesenhoMagia.fita(s,cam,ant,q,.09,u*3,u*3+.2,TINTA,a);ant=q;}
                }
                if(condensa>.01)for(int k=0;k<4;k++){double off=(k-1.5)*.12;Vec3 a0=mao.add(lado.scale(off)),b0=mao.add(frente.scale(3.6)).add(lado.scale(off*1.6)).add(0,Math.sin(t*1.7+k)*.15,0);DesenhoMagia.fita(s,cam,a0,b0,.3*condensa,0,4,TINTA,(float)condensa);}
            });
            DesenhoMagia.lote(s,15,true,()->{
                for(int k=0;k<com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.FAISCAS;k++){Vec3 ant=mao;for(int i=0;i<5;i++){Vec3 q=mao.add(w.apply(com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.faisca(k,i,t)));DesenhoMagia.fita(s,cam,ant,q,.035,0,1,VERMELHO,(float)carga*.8f);ant=q;}}
            });
        }
        double varre=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.varredura(t);float meia=(float)com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.alphaMeiaLua(t);
        if(meia>.01&&varre>.01)DesenhoMagia.lote(s,22,false,()->{
            int n=18;double fim=1.95*varre,ini=Math.max(0,fim-1.2);
            for(int i=0;i<n;i++){
                double a0=ini+(fim-ini)*i/n,a1=ini+(fim-ini)*(i+1)/n;
                Vec3 p0=w.apply(com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.lamina(.55,a0)),p1=w.apply(com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.lamina(.55,a1));
                Vec3 q0=w.apply(com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.lamina(1,a0)),q1=w.apply(com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.lamina(1,a1));
                float a=meia; // Solid ink: translucent black reads grey.
                DesenhoMagia.ponto(s,p0,i*.4,0,TINTA,a);DesenhoMagia.ponto(s,p1,(i+1)*.4,0,TINTA,a);DesenhoMagia.ponto(s,q1,(i+1)*.4,1,TINTA,a);DesenhoMagia.ponto(s,q0,i*.4,1,TINTA,a);
            }
        });
        double aberta=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.fenda(t);float fenda=(float)com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.alphaFenda(t);
        if(fenda>.01&&aberta>.05)DesenhoMagia.lote(s,22,false,()->{
            int n=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.PONTOS_FENDA;
            for(int i=0;i<n-1;i++){
                var b0=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.borda(i);var b1=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.borda(i+1);
                if(b0.z()>aberta)break;double z1=Math.min(b1.z(),aberta),k=(z1-b0.z())/Math.max(1e-6,b1.z()-b0.z());
                double x1=b0.x()+(b1.x()-b0.x())*k,w0=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.larguraFenda(i),w1=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.larguraFenda(i+1);
                Vec3 c0=w.apply(new com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.P(b0.x(),.04,b0.z())),c1=w.apply(new com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.P(x1,.04,z1));
                DesenhoMagia.ponto(s,c0.subtract(lado.scale(w0)),b0.z()*.5,0,TINTA,fenda);DesenhoMagia.ponto(s,c1.subtract(lado.scale(w1)),z1*.5,0,TINTA,fenda);
                DesenhoMagia.ponto(s,c1.add(lado.scale(w1)),z1*.5,1,TINTA,fenda);DesenhoMagia.ponto(s,c0.add(lado.scale(w0)),b0.z()*.5,1,TINTA,fenda);
            }
        });
        if(t>=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.CORTE)DesenhoMagia.lote(s,22,false,()->{
            for(int i=0;i<com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.GOTAS;i++){
                float a=fenda*(float)Math.min(1,com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.alphaGota(i,t)*1.6);if(a<.05)continue;
                var g=com.cauansilva.arcanecontrol.core.vfx.BlackDividerAsta.gota(i,t);if(g.z()>aberta)continue;Vec3 q=w.apply(g);
                DesenhoMagia.fita(s,cam,q,q.add(0,.28,0),.13,0,1,TINTA,a);
            }
        });
    }
    private interface Faixa{com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.P em(double u,double v);}
    /** Ink band sampled along u (n segments) between v=0 and v=1, in the strike frame. */
    private static void faixa(PoseStack s,Vec3 frente,Vec3 lado,Faixa fx,double u0,double u1,int n,float alpha){
        for(int i=0;i<n;i++){
            double a=u0+(u1-u0)*i/n,b=u0+(u1-u0)*(i+1)/n;
            Vec3[] q={w(frente,lado,fx.em(a,0)),w(frente,lado,fx.em(b,0)),w(frente,lado,fx.em(b,1)),w(frente,lado,fx.em(a,1))};
            DesenhoMagia.ponto(s,q[0],i*.4,0,TINTA,alpha);DesenhoMagia.ponto(s,q[1],(i+1)*.4,0,TINTA,alpha);DesenhoMagia.ponto(s,q[2],(i+1)*.4,1,TINTA,alpha);DesenhoMagia.ponto(s,q[3],i*.4,1,TINTA,alpha);
        }
    }
    private static Vec3 w(Vec3 frente,Vec3 lado,com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.P q){return lado.scale(q.x()).add(0,q.y(),0).add(frente.scale(q.z()));}
    /** Black lightning: jagged ink strokes with a red rim, plus a thin red crackle, along one stroke path per bolt. */
    private static void raios(PoseStack s,Vec3 cam,Vec3 frente,Vec3 lado,java.util.function.BiFunction<Double,Double,com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.P> caminho,double t,double amp,double largura,float alpha,int bolts){
        int n=com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.PONTOS;
        for(int camada=0;camada<2;camada++){
            final boolean brilho=camada==1;
            DesenhoMagia.lote(s,brilho?15:22,brilho,()->{
                for(int j=0;j<bolts;j++){
                    Vec3 ant=null;
                    for(int i=0;i<=n;i++){
                        var q=com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.relampago(caminho,j,i,t,amp);Vec3 p=w(frente,lado,q);
                        double af=Math.sin(Math.PI*i/(double)n)*.6+.4;
                        if(ant!=null)DesenhoMagia.fita(s,cam,ant,p,brilho?largura*.4:largura*.5*af,0,1,brilho?VERMELHO:TINTA,brilho?alpha*.8f:alpha);
                        if(i%3==1){Vec3 r=w(frente,lado,com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.ramo(q,j,i,t,amp*2.2));DesenhoMagia.fita(s,cam,p,r,brilho?largura*.25:largura*.28,0,1,brilho?VERMELHO:TINTA,brilho?alpha*.6f:alpha);}
                        ant=p;
                    }
                }
            });
        }
    }
    /** Base Z/X/C/V as black anti-magic lightning following each gesture; each strike completes on the server's damage tick. */
    private static void golpeBase(PoseStack s,Vec3 cam,int slot,Vec3 olhar,double t){
        Vec3 f0=new Vec3(olhar.x,0,olhar.z);final Vec3 frente=f0.lengthSqr()<1e-8?new Vec3(0,0,1):f0.normalize();final Vec3 lado=frente.cross(CIMA).normalize();
        double varre=com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.varredura(slot,t);float a=(float)com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.alpha(slot,t);
        if(slot==0&&a>.01&&varre>.01)raios(s,cam,frente,lado,(u,v)->com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.leque(-1+2*varre*u,v),t,.45,.2,a,5);
        if(slot==1){
            double lo=Math.max(0,varre-.8);
            if(a>.01&&varre>.01)raios(s,cam,frente,lado,(u,v)->com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.corteVertical(lo+(varre-lo)*u,v),t,.5,.22,a,4);
            double r=com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.rasgo(t);
            if(r>.01)raios(s,cam,frente,lado,(u,v)->{var b=com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.bordaRasgo((int)Math.round(u*11*r));return new com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.P(b.x()+(v-.5)*.6,.06,b.z());},t,.3,.16,a,2);
        }
        if(slot==2){
            double len=com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.rastro(t);
            if(len>.05)raios(s,cam,frente,lado,(u,v)->new com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.P((v-.5)*.9,.5+v*.9,-len*(1-u)),t,.3,.16,1,3);
            float e=(float)com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.alphaEspinho(t);
            if(e>.01)for(int k=0;k<com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.ESPINHOS;k+=2){
                var tip=com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.espinho(k,t);
                raios(s,cam,frente,lado,(u,v)->new com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.P(tip.x()*u,.1+(tip.y()-.1)*u,tip.z()*u),t+k,.35,.18,e,1);
            }
        }
        if(slot==3){
            float g=(float)com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.alphaGuarda(t);
            if(g>.01)raios(s,cam,frente,lado,(u,v)->com.cauansilva.arcanecontrol.core.vfx.GolpesBaseAsta.guarda(-1+2*u,v,t),t,.28,.14,g,3);
        }
    }
}