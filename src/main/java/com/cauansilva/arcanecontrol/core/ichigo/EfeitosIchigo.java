package com.cauansilva.arcanecontrol.core.ichigo;
import com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar;
import com.cauansilva.arcanecontrol.core.client.AnimacoesGojo;
import com.cauansilva.arcanecontrol.core.client.AudioAnime;
import com.cauansilva.arcanecontrol.core.audio.RoteiroAudio;
import com.cauansilva.arcanecontrol.core.client.render.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class EfeitosIchigo {
    private record Efeito(PacoteEfeitoDespertar p,Object mundo,long inicio){}
    private static final List<Efeito> EFEITOS=new ArrayList<>();
    private static final Vec3 UP=new Vec3(0,1,0);
    private static boolean ativo(UUID id,int tipo){var mc=Minecraft.getInstance();return mc.level!=null&&EFEITOS.stream().anyMatch(f->f.mundo==mc.level&&f.p.tipo()==tipo&&f.p.dono().equals(id)&&mc.level.getGameTime()-f.inicio<f.p.ticks());}
    public static boolean bankai(UUID id){return ativo(id,200)||ativo(id,201);}
    public static boolean mascara(UUID id){return bankai(id)&&ativo(id,522);}
    public static void receber(PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(p.tipo()==0){EFEITOS.removeIf(f->f.p.dono().equals(p.dono()));ImpactosAnime.cancelar(p.dono());AudioAnime.cancelar(p.dono(),null);AnimacoesGojo.cancelar(p.dono());return;}
        if(p.kit()!=6||p.ticks()<1||p.ticks()>1200||!Double.isFinite(p.a().lengthSqr())||!Double.isFinite(p.b().lengthSqr()))return;
        boolean inicio=p.tipo()==200&&!bankai(p.dono());
        if(p.tipo()==200||p.tipo()==201)EFEITOS.removeIf(f->(f.p.tipo()==200||f.p.tipo()==201)&&f.p.dono().equals(p.dono()));
        if(p.tipo()==522)EFEITOS.removeIf(f->f.p.tipo()==522&&f.p.dono().equals(p.dono()));
        if(p.tipo()>=500&&p.tipo()<=513||inicio)AnimacoesGojo.tocarDesperto(p.dono(),6,p.tipo());
        if(inicio){ImpactosAnime.programar(p.dono(),p.a().add(0,1,0),12,0xC4355C,.92,20,6);AudioAnime.programar(p.dono(),"bankai",p.a(),RoteiroAudio.imediato("purple_charge",24));}
        if(p.tipo()==520||p.tipo()==523||p.tipo()==524){AudioAnime.programar(p.dono(),"getsuga",p.a(),RoteiroAudio.imediato("divider_release",20));ImpactosAnime.programar(p.dono(),p.a(),0,p.tipo()==523?0xD82C56:0x70CBFF,p.tipo()==523?.92:.55,18,6);}
        if(p.tipo()==521)ImpactosAnime.programar(p.dono(),p.a(),0,0x89D9FF,.8,16,6);
        if(p.tipo()==522)ImpactosAnime.programar(p.dono(),p.a().add(0,1.5,0),0,0xE02F51,.9,18,6);
        if(EFEITOS.size()>=96)EFEITOS.remove(0);EFEITOS.add(new Efeito(p,mc.level,mc.level.getGameTime()));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();EFEITOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=f.p.ticks()||mc.level.getPlayerByUUID(f.p.dono())==null||!mc.level.getPlayerByUUID(f.p.dono()).isAlive());}
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){EFEITOS.clear();}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        double agora=mc.level.getGameTime()+e.getPartialTick();var camera=e.getCamera().getPosition();
        for(var f:EFEITOS){var p=f.p;double t=agora-f.inicio;if(f.mundo!=mc.level||t<0||t>=p.ticks())continue;
            var dono=mc.level.getPlayerByUUID(p.dono());if(dono==null)continue;
            var origem=(p.tipo()==200||p.tipo()==201)?dono.getPosition(e.getPartialTick()):p.a();if(camera.distanceToSqr(origem)>96*96||!DesenhoMagia.iniciar(agora))continue;
            var s=e.getPoseStack();s.pushPose();s.translate(origem.x-camera.x,origem.y-camera.y,origem.z-camera.z);
            var cam=camera.subtract(origem);float fade=(float)Math.min(1,Math.min((t+1)/2,(p.ticks()-t)/5));int cor=(p.tipo()==523||p.tipo()==524||p.tipo()>=510&&p.tipo()<=513||bankai(p.dono()))?0xD32A50:0x6FCFFF;
            try{
                if(p.tipo()==520||p.tipo()==523||p.tipo()==524){
                    var dir=p.b().normalize();double distancia=PerfilIchigo.alcance((int)t);var global=origem.add(dir.scale(distancia));
                    var bloqueio=mc.level.clip(new ClipContext(origem,global,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dono));
                    if(bloqueio.getType()!=HitResult.Type.MISS)continue;
                    var c=dir.scale(distancia);var lado=dir.cross(UP).normalize();if(lado.lengthSqr()<1e-8)lado=new Vec3(1,0,0);final var x=lado;
                    final int n=p.tipo()==523?2:1;
                    DesenhoMagia.lote(s,0,false,()->{for(int j=0;j<n;j++){double a=j==0?-Math.PI/4:Math.PI/4;var y=UP.scale(Math.cos(a)).add(x.scale(Math.sin(a)));var z=x.scale(Math.cos(a)).subtract(UP.scale(Math.sin(a)));GeometriaCinematica.arco(s,cam,c,z,y,p.tipo()==523?2.1:1.25,-Math.PI*.65,Math.PI*1.3,.32,p.tipo()==520?0x226C96:0x160F21,fade);}});
                    DesenhoMagia.lote(s,1,true,()->{for(int j=0;j<n;j++){double a=j==0?-Math.PI/4:Math.PI/4;var y=UP.scale(Math.cos(a)).add(x.scale(Math.sin(a)));var z=x.scale(Math.cos(a)).subtract(UP.scale(Math.sin(a)));GeometriaCinematica.arco(s,cam,c,z,y,p.tipo()==523?2.13:1.28,-Math.PI*.65,Math.PI*1.3,.055,cor,fade);}DesenhoMagia.fita(s,cam,c.subtract(dir.scale(3)),c,.12,0,4,cor,fade*.7f);});
                }else if(p.tipo()==501||p.tipo()==511){
                    var fim=p.b().subtract(p.a());
                    DesenhoMagia.lote(s,0,false,()->{for(int i=0;i<4;i++){var v=fim.scale(i/4d);float a=fade*(1-i/5f)*.35f;
                        DesenhoMagia.caixa(s,v.add(0,1.55,0),.24,.25,.24,0x18131F,a);
                        DesenhoMagia.caixa(s,v.add(0,1.05,0),.25,.35,.14,0x18131F,a);
                        DesenhoMagia.caixa(s,v.add(-.34,1.05,0),.09,.3,.1,0x18131F,a);
                        DesenhoMagia.caixa(s,v.add(.34,1.05,0),.09,.3,.1,0x18131F,a);
                        DesenhoMagia.caixa(s,v.add(-.13,.35,0),.1,.34,.12,0x18131F,a);
                        DesenhoMagia.caixa(s,v.add(.13,.35,0),.1,.34,.12,0x18131F,a);
                    }});
                    DesenhoMagia.lote(s,1,true,()->{for(int i=0;i<4;i++){var v=fim.scale(i/4d);DesenhoMagia.fita(s,cam,v.add(0,.4,0),v.add(0,1.65,0),.05,0,3,cor,fade*(1-i/5f));}});
                }else if(p.tipo()==521){DesenhoMagia.lote(s,1,true,()->GeometriaCinematica.explosao(s,cam,t,5,cor,fade));
                }else if(p.tipo()==200||p.tipo()==201){
                    DesenhoMagia.lote(s,7,false,()->{for(int i=0;i<6;i++){double a=i*Math.PI/3+t*.03;var v=new Vec3(Math.cos(a)*.65,.1,Math.sin(a)*.65);DesenhoMagia.fita(s,cam,v,v.add(.1,2,.1),.06,0,3,0x19131D,fade*.45f);}});
                    if(p.tipo()==200&&t<24)DesenhoMagia.lote(s,1,true,()->GeometriaCinematica.explosao(s,cam,t,3,cor,fade));
                }else if(p.tipo()!=522){
                    DesenhoMagia.lote(s,1,true,()->{var c=dono.getEyePosition().subtract(origem);DesenhoMagia.anel(s,c,UP,.65,.72,cor,fade*.6f,32);});
                }
            }finally{s.popPose();DesenhoMagia.terminar();}
        }
    }
}
