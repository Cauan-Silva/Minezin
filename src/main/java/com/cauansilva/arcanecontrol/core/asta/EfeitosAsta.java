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
import com.cauansilva.arcanecontrol.core.vfx.BlackHurricaneAsta;
import com.cauansilva.arcanecontrol.core.vfx.BlackMeteoriteAsta;
import com.cauansilva.arcanecontrol.core.vfx.UltAsta;
import java.util.*;

@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class EfeitosAsta {
    /** impacto[0] guarda onde o dono estava no tick do dano do Black Meteorite (a cratera fica ali mesmo se ele andar);
     *  na ult, impacto[j] e o centro do corte j e impacto[CORTES] o do X, anunciados pelo servidor (311). */
    private record Efeito(PacoteEfeitoDespertar p,Object mundo,long inicio,Vec3[] impacto){}
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
    /** Sum of the robe pushes from the owner's running base strikes (RoteiroTecidoAsta), clamped like Cid's. */
    public static com.cauansilva.arcanecontrol.core.cid.RoteiroTecidoCid.Impulso tecido(java.util.UUID id){
        var mc=Minecraft.getInstance();double frente=0,lado=0,giro=0;
        if(mc.level!=null)for(var f:EFEITOS)if(f.mundo==mc.level&&f.p.dono().equals(id)&&f.p.tipo()>=300&&f.p.tipo()<=303){
            var i=RoteiroTecidoAsta.amostrar(f.p.tipo(),mc.level.getGameTime()-f.inicio);frente+=i.frente();lado+=i.lado();giro+=i.giro();
        }
        return new com.cauansilva.arcanecontrol.core.cid.RoteiroTecidoCid.Impulso(Math.max(-.1,Math.min(.32,frente)),Math.max(-.18,Math.min(.18,lado)),Math.max(-.22,Math.min(.22,giro)));
    }
    /** Black Meteorite: the player travels as an anti-magic beam and is not drawn until the overhead cut. */
    public static boolean oculto(java.util.UUID id,float parcial){
        var mc=Minecraft.getInstance();if(mc.level==null)return false;
        for(var f:EFEITOS)if(f.p.tipo()==304&&f.mundo==mc.level&&f.p.dono().equals(id)){
            double t=mc.level.getGameTime()+parcial-f.inicio;if(t>=0&&!BlackMeteoriteAsta.corpoVisivel(t))return true;
        }
        return false;
    }
    @SubscribeEvent public static void esconder(RenderPlayerEvent.Pre e){if(oculto(e.getEntity().getUUID(),e.getPartialTick()))e.setCanceled(true);}
    public static void receber(PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(p.tipo()==0){com.cauansilva.arcanecontrol.core.client.AudioAnime.cancelar(p.dono(),null);ImpactosAnime.cancelar(p.dono());EFEITOS.removeIf(e->e.p.dono().equals(p.dono()));AnimacoesGojo.cancelar(p.dono());if(mc.player!=null&&p.dono().equals(mc.player.getUUID())&&(GerenciadorCutscene3D.cutsceneTipo==35||GerenciadorCutscene3D.cutsceneTipo==36))GerenciadorCutscene3D.finalizarCutscene();return;}
        if(p.ticks()<1||p.ticks()>1200)return;
        if(p.tipo()==311){
            // Where a cut of the ultimate lands (on the locked target): kept in the owner's running 307, never drawn alone.
            int j=(int)Math.round(p.b().x);
            for(int i=EFEITOS.size()-1;i>=0;i--){var f=EFEITOS.get(i);if(f.p.tipo()==307&&f.mundo==mc.level&&f.p.dono().equals(p.dono())){if(j>=0&&j<=UltAsta.CORTES)f.impacto[j]=p.a();break;}}
            return;
        }
        com.cauansilva.arcanecontrol.core.client.render.UltimatesTransformadas.receber(p);
        if(p.tipo()==306)com.cauansilva.arcanecontrol.core.client.AudioAnime.programar(p.dono(),"divider",p.a(),com.cauansilva.arcanecontrol.core.audio.RoteiroAudio.divider());
        if(p.tipo()==309)ImpactosAnime.programar(p.dono(),p.a().add(0,.7,0),0,0xD5264C,.75,18,4);
        if(p.tipo()==200){
            EFEITOS.removeIf(e->e.p.dono().equals(p.dono())&&e.p.tipo()==200);
            if(p.ticks()==1200){ImpactosAnime.programar(p.dono(),p.a().add(0,1,0),com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico.FORMA_IMPACTO,0xD5264C,.85,24,4);AnimacoesGojo.tocarAsta(p.dono(),200);if(mc.player!=null&&p.dono().equals(mc.player.getUUID()))GerenciadorCutscene3D.iniciarCutsceneLocal(35,PerfilAsta.ABERTURA);}
        }else if(p.tipo()>=300&&p.tipo()<=307)AnimacoesGojo.tocarAsta(p.dono(),p.tipo());
        if(EFEITOS.size()>=64)EFEITOS.remove(0);
        EFEITOS.add(new Efeito(p,mc.level,mc.level.getGameTime(),new Vec3[UltAsta.CORTES+1]));
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
            var dono=mc.level.getPlayerByUUID(p.dono());Vec3 origem=p.tipo()==200||p.tipo()==303||p.tipo()==302||p.tipo()==304||p.tipo()==305||p.tipo()==307?dono==null?p.a():dono.getPosition(e.getPartialTick()):p.a();
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
                if(p.tipo()==307){ultAsta(s,cam,p.b(),t,f.impacto,origem);continue;}
                if(p.tipo()==305){blackHurricane(s,cam,t);continue;}
                if(p.tipo()==304){
                    if(t>=PerfilAsta.impacto(0,true)&&f.impacto[0]==null)f.impacto[0]=origem;
                    blackMeteorite(s,cam,p.b(),p.a().subtract(origem),f.impacto[0]==null?null:f.impacto[0].subtract(origem),t);continue;
                }
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
                float alfa=(float)com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico.corte(t,acerto);
                double distancia=2.1;
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
    private static final int VERMELHO_SOLIDO=0xE01E3A,BRANCO=0xFFF2EE;
    private static final int PAGINA=0xC8283C;
    /** Age of the owner's ultimate (307) or -1: drives the held Demon-Dweller and the book. */
    public static double ult(java.util.UUID id,float parcial){
        var mc=Minecraft.getInstance();if(mc.level==null)return -1;
        for(int i=EFEITOS.size()-1;i>=0;i--){var f=EFEITOS.get(i);if(f.p.tipo()==307&&f.mundo==mc.level&&f.p.dono().equals(id)){
            double t=mc.level.getGameTime()+parcial-f.inicio;return t>=0&&t<UltAsta.DURACAO?t:-1;
        }}return -1;
    }
    /** Start of the owner's ultimate and its horizontal facing, for the book and the sword rising out of it. */
    public static Vec3[] ultOrigem(java.util.UUID id){
        var mc=Minecraft.getInstance();if(mc.level==null)return null;
        for(int i=EFEITOS.size()-1;i>=0;i--){var f=EFEITOS.get(i);if(f.p.tipo()==307&&f.mundo==mc.level&&f.p.dono().equals(id))return new Vec3[]{f.p.a(),f.p.b()};}
        return null;
    }
    static Vec3 frenteHorizontal(Vec3 olhar){Vec3 f=new Vec3(olhar.x,0,olhar.z);return f.lengthSqr()<1e-8?new Vec3(0,0,1):f.normalize();}
    /** Grimoire center at tick t (caster frame LIVRO, carried forward by the dash), relative to the cast origin. */
    public static Vec3 livro(Vec3 frente,double t){Vec3 lado=frente.cross(CIMA);var q=UltAsta.LIVRO;return lado.scale(q.x()).add(0,q.y(),0).add(frente.scale(q.z()+UltAsta.avanco(t)));}
    /**
     * Ultimate (307), origin = the owner now (he closes in on the target): the open grimoire with glowing pages and the
     * black flame burst stay on his body; each slash and the final X are drawn where the server announced them (311),
     * on the locked target. Solid lens-shaped blades with a red seam on the outer edge, as UltAsta defines.
     */
    private static void ultAsta(PoseStack s,Vec3 cam,Vec3 olhar,double t,Vec3[] centros,Vec3 origem){
        final Vec3 frente=frenteHorizontal(olhar),lado=frente.cross(CIMA);
        // UltAsta's caster frame assumes the scripted dash; the body is here, so pull its offsets back by it.
        final Vec3 recuo=frente.scale(-UltAsta.avanco(t));
        java.util.function.Function<UltAsta.P,Vec3> w=q->lado.scale(q.x()).add(0,q.y(),0).add(frente.scale(q.z()));
        java.util.function.Function<Vec3,UltAsta.P> local=v->{Vec3 r=v.subtract(origem);return new UltAsta.P(r.dot(lado),r.y,r.dot(frente));};
        final double livro=UltAsta.abrirLivro(t),explosao=UltAsta.explosao(t);
        final Vec3 c=livro(frente,t).add(recuo),eixo=CIMA,n=w.apply(new UltAsta.P(-.6,0,1)).normalize(),tg=eixo.cross(n);
        DesenhoMagia.lote(s,22,false,()->{
            if(livro>.01){double ab=Math.toRadians(10+160*livro);
                for(int sgn=-1;sgn<=1;sgn+=2){Vec3 d=tg.scale(sgn*Math.sin(ab/2)).subtract(n.scale(Math.cos(ab/2)));
                    Vec3 p0=c.subtract(eixo.scale(.36)),p1=c.add(eixo.scale(.36));
                    DesenhoMagia.ponto(s,p0,0,0,TINTA,1);DesenhoMagia.ponto(s,p1,1,0,TINTA,1);DesenhoMagia.ponto(s,p1.add(d.scale(.5)),1,1,TINTA,1);DesenhoMagia.ponto(s,p0.add(d.scale(.5)),0,1,TINTA,1);}}
            if(explosao>.01)for(int k=0;k<UltAsta.LINGUAS;k++)for(int i=0;i<8;i++)afilada(s,cam,w.apply(UltAsta.lingua(k,i/8d,t)).add(recuo),w.apply(UltAsta.lingua(k,(i+1)/8d,t)).add(recuo),.12*(1-i/9d),.12*(1-(i+1)/9d),0,1,TINTA,1);
            for(int j=0;j<UltAsta.CORTES;j++){if(centros[j]==null)continue;final int jj=j;final UltAsta.P cc=local.apply(centros[j]);float a=(float)UltAsta.alphaCorte(j,t);if(a>.01&&UltAsta.progresso(j,t)>.01)lamina(s,(f,v,l)->w.apply(UltAsta.corte(jj,cc,f,v,l,t)),a);}
            float ax=(float)UltAsta.alphaX(t);if(centros[UltAsta.CORTES]!=null&&ax>.01&&UltAsta.progressoX(t)>.01){final UltAsta.P cx=local.apply(centros[UltAsta.CORTES]);for(int k=0;k<2;k++){final int kk=k;lamina(s,(f,v,l)->w.apply(UltAsta.cruz(kk,cx,f,v,l,t)),ax);}}
        });
        DesenhoMagia.lote(s,8,false,()->{
            if(livro>.01){double ab=Math.toRadians(10+160*livro);
                for(int sgn=-1;sgn<=1;sgn+=2){Vec3 d=tg.scale(sgn*Math.sin(ab/2)).subtract(n.scale(Math.cos(ab/2))),f=n.scale(.03);
                    Vec3 p0=c.subtract(eixo.scale(.33)).add(f),p1=c.add(eixo.scale(.33)).add(f);
                    DesenhoMagia.ponto(s,p0,0,0,PAGINA,1);DesenhoMagia.ponto(s,p1,1,0,PAGINA,1);DesenhoMagia.ponto(s,p1.add(d.scale(.46)),1,1,PAGINA,1);DesenhoMagia.ponto(s,p0.add(d.scale(.46)),0,1,PAGINA,1);}}
            // Glowing seam on the outer edge of each blade, visible from every side.
            for(int j=0;j<UltAsta.CORTES;j++)if(centros[j]!=null&&UltAsta.alphaCorte(j,t)>.01&&UltAsta.progresso(j,t)>.01){var cc=local.apply(centros[j]);for(int i=0;i<16;i++)afilada(s,cam,w.apply(UltAsta.corte(j,cc,i/16d,1,1,t)),w.apply(UltAsta.corte(j,cc,(i+1)/16d,1,1,t)),.035,.035,0,1,VERMELHO_SOLIDO,1);}
            if(centros[UltAsta.CORTES]!=null&&UltAsta.alphaX(t)>.01&&UltAsta.progressoX(t)>.01){var cx=local.apply(centros[UltAsta.CORTES]);for(int k=0;k<2;k++)for(int i=0;i<16;i++)afilada(s,cam,w.apply(UltAsta.cruz(k,cx,i/16d,1,1,t)),w.apply(UltAsta.cruz(k,cx,(i+1)/16d,1,1,t)),.05,.05,0,1,VERMELHO_SOLIDO,1);}
        });
    }
    private interface Lamina{Vec3 em(double f,double v,int lado);}
    /** Both faces of a lens-shaped blade, 16 x 4 quads each; uv across the blade so the ink shader rims both edges. */
    private static void lamina(PoseStack s,Lamina l,float alpha){
        for(int lado=-1;lado<=1;lado+=2)for(int i=0;i<16;i++)for(int v=0;v<4;v++){
            double f0=i/16d,f1=(i+1)/16d,v0=v/4d,v1=(v+1)/4d;
            DesenhoMagia.ponto(s,l.em(f0,v0,lado),i*.4,v0,TINTA,alpha);DesenhoMagia.ponto(s,l.em(f1,v0,lado),(i+1)*.4,v0,TINTA,alpha);
            DesenhoMagia.ponto(s,l.em(f1,v1,lado),(i+1)*.4,v1,TINTA,alpha);DesenhoMagia.ponto(s,l.em(f0,v1,lado),i*.4,v1,TINTA,alpha);
        }
    }

    /** Ribbon whose half-width goes from wa at a to wb at b, facing the camera. */
    private static void afilada(PoseStack s,Vec3 cam,Vec3 a,Vec3 b,double wa,double wb,double u0,double u1,int cor,float alpha){
        Vec3 d=b.subtract(a);if(d.lengthSqr()<1e-10)return;
        Vec3 l=d.cross(cam.subtract(a));if(l.lengthSqr()<1e-10)l=d.cross(CIMA);if(l.lengthSqr()<1e-10)l=new Vec3(1,0,0);l=l.normalize();
        DesenhoMagia.ponto(s,a.subtract(l.scale(wa)),u0,0,cor,alpha);DesenhoMagia.ponto(s,b.subtract(l.scale(wb)),u1,0,cor,alpha);
        DesenhoMagia.ponto(s,b.add(l.scale(wb)),u1,1,cor,alpha);DesenhoMagia.ponto(s,a.add(l.scale(wa)),u0,1,cor,alpha);
    }
    /**
     * Black Meteorite (304), origin = owner: launch splash at the start point, the owner as an anti-magic beam (body hidden
     * by {@link #oculto}) with its trail back to the start, ink shards where he reappears, the overhead cut, then the star
     * flash and the ink crater pinned where he stood on the server's hit tick.
     */
    private static void blackMeteorite(PoseStack s,Vec3 cam,Vec3 olhar,Vec3 partida,Vec3 impacto,double t){
        Vec3 f0=new Vec3(olhar.x,0,olhar.z);final Vec3 olharH=f0.lengthSqr()<1e-8?new Vec3(0,0,1):f0.normalize();
        Vec3 viagem=partida.scale(-1);final double d=viagem.length();Vec3 m0=new Vec3(viagem.x,0,viagem.z);
        final Vec3 frente=d>.3&&m0.lengthSqr()>1e-6?m0.normalize():olharH,lado=frente.cross(CIMA).normalize();
        final Vec3 rumo=d>1e-6?viagem.scale(1/d):frente,chao=frente;
        final Vec3 ladoRumo=rumo.cross(CIMA).lengthSqr()<1e-6?lado:rumo.cross(CIMA).normalize(),cimaRumo=ladoRumo.cross(rumo).normalize();
        java.util.function.Function<BlackMeteoriteAsta.P,Vec3> w=q->lado.scale(q.x()).add(0,q.y(),0).add(frente.scale(q.z()));
        final Vec3 ladoO=olharH.cross(CIMA).normalize();
        java.util.function.Function<BlackMeteoriteAsta.P,Vec3> wo=q->ladoO.scale(q.x()).add(0,q.y(),0).add(olharH.scale(q.z()));
        // Points measured on the ground from the start point, along the travel direction.
        java.util.function.Function<BlackMeteoriteAsta.P,Vec3> wp=q->partida.add(chao.scale(q.z())).add(lado.scale(q.x())).add(0,q.y(),0);
        final double arr=BlackMeteoriteAsta.arranque(t),feixe=BlackMeteoriteAsta.feixe(t),rastro=BlackMeteoriteAsta.alphaRastro(t),rp=BlackMeteoriteAsta.reaparece(t),varre=BlackMeteoriteAsta.varredura(t),golpe=BlackMeteoriteAsta.alphaGolpe(t);
        final double raio=BlackMeteoriteAsta.cratera(t),estrela=BlackMeteoriteAsta.estrela(t);final float ac=(float)BlackMeteoriteAsta.alphaCratera(t);
        DesenhoMagia.lote(s,22,false,()->{
            if(arr>.01)for(int k=0;k<BlackMeteoriteAsta.ESPINHOS;k++)afilada(s,cam,partida.add(0,.05,0),wp.apply(BlackMeteoriteAsta.espinhoArranque(k,t)),.2*arr,.02,0,1,TINTA,1);
            if(rastro>.01&&d>.05){
                Vec3 ant=null;double wAnt=0,cauda=BlackMeteoriteAsta.caudaRastro(t);
                for(int i=0;i<=40;i++){
                    double u=i/40d;if(u<cauda){ant=null;continue;}
                    var dv=BlackMeteoriteAsta.desvioRastro(u);double wd=BlackMeteoriteAsta.larguraRastro(u)*rastro;
                    Vec3 q=partida.add(rumo.scale(d*u)).add(0,1,0).add(ladoRumo.scale(dv.x())).add(cimaRumo.scale(dv.y()));
                    if(ant!=null)afilada(s,cam,ant,q,wAnt,wd,u*6,u*6+.15,TINTA,1);ant=q;wAnt=wd;
                }
                for(int i=0;i<BlackMeteoriteAsta.GOTAS_RASTRO;i++){
                    double a=BlackMeteoriteAsta.alphaGotaRastro(i,t);if(a*rastro<.05)continue;Vec3 q=wp.apply(BlackMeteoriteAsta.gotaRastro(i,t,d));
                    afilada(s,cam,q,q.add(0,.3,0),.11*Math.min(1,a*1.6),0,0,1,TINTA,1);
                }
            }
            if(feixe>.01){
                int n=BlackMeteoriteAsta.PONTOS_FEIXE;
                for(int i=0;i<n;i++){double u0=i/(double)n,u1=(i+1)/(double)n;afilada(s,cam,w.apply(BlackMeteoriteAsta.eixoFeixe(u0)),w.apply(BlackMeteoriteAsta.eixoFeixe(u1)),BlackMeteoriteAsta.larguraFeixe(u0)*feixe,BlackMeteoriteAsta.larguraFeixe(u1)*feixe,i*.5,i*.5+.5,TINTA,1);}
            }
            if(rp>.01)for(int k=0;k<BlackMeteoriteAsta.ESTILHACOS;k++){Vec3 q=w.apply(BlackMeteoriteAsta.estilhaco(k,t)),r=q.subtract(0,1,0);Vec3 dir=r.lengthSqr()<1e-8?CIMA:r.normalize();afilada(s,cam,q,q.add(dir.scale(.45*rp)),.12*rp,0,0,1,TINTA,1);}
            if(golpe>.01&&varre>.01){
                int n=18;double fim=BlackMeteoriteAsta.ARCO_GOLPE*varre,ini=fim-BlackMeteoriteAsta.JANELA_GOLPE;float a=(float)golpe;
                for(int i=0;i<n;i++){
                    double g0=i/(double)n,g1=(i+1)/(double)n,a0=ini+(fim-ini)*g0,a1=ini+(fim-ini)*g1;if(a1<=0)continue;a0=Math.max(0,a0);
                    DesenhoMagia.ponto(s,w.apply(BlackMeteoriteAsta.lamina(BlackMeteoriteAsta.internoGolpe(g0),a0)),i*.4,0,TINTA,a);DesenhoMagia.ponto(s,w.apply(BlackMeteoriteAsta.lamina(BlackMeteoriteAsta.internoGolpe(g1),a1)),(i+1)*.4,0,TINTA,a);
                    DesenhoMagia.ponto(s,w.apply(BlackMeteoriteAsta.lamina(1,a1)),(i+1)*.4,1,TINTA,a);DesenhoMagia.ponto(s,w.apply(BlackMeteoriteAsta.lamina(1,a0)),i*.4,1,TINTA,a);
                }
            }
            if(impacto!=null&&raio>.05&&ac>.01){
                // Ink disc as a fan: center v=.5, torn border v=0, so the shader draws the red rim on the edge only.
                Vec3 c=impacto.add(0,.03,0);int n=BlackMeteoriteAsta.PONTOS_CRATERA;
                for(int i=0;i<n;i++){Vec3 b0=impacto.add(wo.apply(BlackMeteoriteAsta.bordaCratera(i,raio))),b1=impacto.add(wo.apply(BlackMeteoriteAsta.bordaCratera((i+1)%n,raio)));
                    DesenhoMagia.ponto(s,c,i*.3,.5,TINTA,ac);DesenhoMagia.ponto(s,b0,i*.3,0,TINTA,ac);DesenhoMagia.ponto(s,b1,(i+1)*.3,0,TINTA,ac);DesenhoMagia.ponto(s,c,(i+1)*.3,.5,TINTA,ac);}
                for(int k=0;k<BlackMeteoriteAsta.ESPINHOS;k++){double h=BlackMeteoriteAsta.alturaEspinho(k,t);if(h<.02)continue;Vec3 b=impacto.add(wo.apply(BlackMeteoriteAsta.baseEspinho(k)));afilada(s,cam,b,b.add(b.subtract(impacto).scale(.18)).add(0,h,0),.22,.02,0,1,TINTA,ac);}
                for(int i=0;i<BlackMeteoriteAsta.GOTAS;i++){double a=BlackMeteoriteAsta.alphaGota(i,t);if(a<.05)continue;Vec3 q=impacto.add(wo.apply(BlackMeteoriteAsta.gota(i,t)));afilada(s,cam,q,q.add(0,.32,0),.13*Math.min(1,a*1.6),0,0,1,TINTA,ac);}
            }
        });
        DesenhoMagia.lote(s,8,false,()->{
            if(feixe>.01){
                int n=BlackMeteoriteAsta.PONTOS_FEIXE;
                for(int i=0;i<n*.6;i++){Vec3 a=w.apply(BlackMeteoriteAsta.eixoFeixe(i/(double)n)),b=w.apply(BlackMeteoriteAsta.eixoFeixe((i+1)/(double)n));afilada(s,cam,a.add(cam.subtract(a).normalize().scale(.06)),b.add(cam.subtract(b).normalize().scale(.06)),.05*feixe*(1-i/(double)n),.05*feixe*(1-(i+1)/(double)n),0,1,VERMELHO_SOLIDO,1);}
                for(int k=0;k<BlackMeteoriteAsta.ESPIRAIS;k++)for(int i=0;i<24;i++)afilada(s,cam,w.apply(BlackMeteoriteAsta.espiral(k,i/24d,t)),w.apply(BlackMeteoriteAsta.espiral(k,(i+1)/24d,t)),.035*feixe*(1-i/25d),.035*feixe*(1-(i+1)/25d),0,1,VERMELHO_SOLIDO,1);
            }
            if(impacto!=null&&raio>.05&&ac>.01)for(int k=0;k<BlackMeteoriteAsta.RACHADURAS;k++)for(int i=0;i<5;i++)afilada(s,cam,impacto.add(wo.apply(BlackMeteoriteAsta.rachadura(k,i,raio))).add(0,.03,0),impacto.add(wo.apply(BlackMeteoriteAsta.rachadura(k,i+1,raio))).add(0,.03,0),.045,.045,0,1,VERMELHO_SOLIDO,ac);
            if(impacto!=null&&estrela>.01){
                // Four-point star: the anime's white star on a black frame, red outside and a white core.
                Vec3 c=impacto.add(0,1.2,0),dc=cam.subtract(c).normalize(),hz=CIMA.cross(dc).normalize();
                for(int camada=0;camada<2;camada++)for(Vec3 v:new Vec3[]{CIMA.scale(3.5),CIMA.scale(-3.5),hz.scale(5),hz.scale(-5)}){
                    Vec3 o=camada==0?c:c.add(dc.scale(.05));double k=camada==0?1:.85,lw=camada==0?.2:.08;
                    afilada(s,cam,o,o.add(v.scale(k*estrela)),lw*estrela,0,0,1,camada==0?VERMELHO_SOLIDO:BRANCO,1);
                }
            }
        });
    }
    private static Vec3 v(BlackHurricaneAsta.P q){return new Vec3(q.x(),q.y(),q.z());}
    /**
     * Black Hurricane (305), origin = owner (the server hits around where he stands on tick 10): ink pulled in, a funnel
     * of rising wind streaks, blades sweeping out to radius 8 exactly on the hit tick, a ring on the ground at radius 8
     * and the stripped magic sucked into the funnel.
     */
    private static void blackHurricane(PoseStack s,Vec3 cam,double t){
        final double succao=BlackHurricaneAsta.succao(t),funil=BlackHurricaneAsta.alphaFunil(t)*Math.min(1,BlackHurricaneAsta.funil(t)*2),laminas=BlackHurricaneAsta.alphaLaminas(t),fronteira=BlackHurricaneAsta.fronteira(t),anel=BlackHurricaneAsta.anel(t);
        DesenhoMagia.lote(s,22,false,()->{
            if(succao>.01)for(int k=0;k<BlackHurricaneAsta.FIOS;k++){
                Vec3 ant=null;double aAnt=0;
                for(int i=0;i<=20;i++){double u=i/20d,a=BlackHurricaneAsta.alphaFio(k,u,t);Vec3 q=v(BlackHurricaneAsta.fio(k,u,t));if(ant!=null&&Math.max(a,aAnt)>.03)afilada(s,cam,ant,q,.09*Math.min(1,aAnt*1.8),.09*Math.min(1,a*1.8),0,1,TINTA,1);ant=q;aAnt=a;}
            }
            if(funil>.01)for(int b=0;b<BlackHurricaneAsta.BANDAS;b++){
                double u0=BlackHurricaneAsta.inicioRisco(b,t);Vec3 ant=null;double wAnt=0;
                for(int i=0;i<=12;i++){
                    double f=i/12d,u=u0+f*BlackHurricaneAsta.COMPRIMENTO_RISCO;if(u<0||u>1){ant=null;continue;}
                    Vec3 q=v(BlackHurricaneAsta.banda(b,u,t));double wd=BlackHurricaneAsta.larguraRisco(u,f)*funil;
                    if(ant!=null)afilada(s,cam,ant,q,wAnt,wd,i*.4-.4,i*.4,TINTA,1);ant=q;wAnt=wd;
                }
            }
            if(laminas>.01&&fronteira>.1)for(int j=0;j<BlackHurricaneAsta.LAMINAS;j++)for(int i=0;i<12;i++){
                double f0=i/12d,f1=(i+1)/12d;float a=(float)laminas;
                DesenhoMagia.ponto(s,v(BlackHurricaneAsta.lamina(j,f0,0,t)),i*.5,0,TINTA,a);DesenhoMagia.ponto(s,v(BlackHurricaneAsta.lamina(j,f1,0,t)),(i+1)*.5,0,TINTA,a);
                DesenhoMagia.ponto(s,v(BlackHurricaneAsta.lamina(j,f1,1,t)),(i+1)*.5,1,TINTA,a);DesenhoMagia.ponto(s,v(BlackHurricaneAsta.lamina(j,f0,1,t)),i*.5,1,TINTA,a);
            }
            if(anel>.01){
                int n=64;double r0=BlackHurricaneAsta.RAIO-.35*anel,r1=BlackHurricaneAsta.RAIO;
                for(int i=0;i<n;i++){
                    double a0=i*Math.PI*2/n,a1=(i+1)*Math.PI*2/n;
                    DesenhoMagia.ponto(s,new Vec3(Math.cos(a0)*r0,.04,Math.sin(a0)*r0),i*.4,0,TINTA,1);DesenhoMagia.ponto(s,new Vec3(Math.cos(a1)*r0,.04,Math.sin(a1)*r0),(i+1)*.4,0,TINTA,1);
                    DesenhoMagia.ponto(s,new Vec3(Math.cos(a1)*r1,.04,Math.sin(a1)*r1),(i+1)*.4,1,TINTA,1);DesenhoMagia.ponto(s,new Vec3(Math.cos(a0)*r1,.04,Math.sin(a0)*r1),i*.4,1,TINTA,1);
                }
            }
        });
        if(t>=BlackHurricaneAsta.ESTOURO)DesenhoMagia.lote(s,8,false,()->{
            // Stripped buffs: red and white shards spiralling into the funnel.
            for(int i=0;i<BlackHurricaneAsta.FRAGMENTOS;i++){
                double a=BlackHurricaneAsta.alphaFragmento(i,t);if(a<.05)continue;Vec3 c=v(BlackHurricaneAsta.fragmento(i,t)),d=new Vec3(-c.x,0,-c.z);
                d=d.lengthSqr()<1e-8?new Vec3(1,0,0):d.normalize();afilada(s,cam,c,c.add(d.scale(.5)).add(0,.15,0),.12*a,0,0,1,i%3==0?BRANCO:VERMELHO_SOLIDO,1);
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