package com.cauansilva.arcanecontrol.core.killua;

import com.cauansilva.arcanecontrol.core.client.*;
import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;
import com.cauansilva.arcanecontrol.core.vfx.*;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/**
 * Efeitos do kit do Killua no cliente: recebe os pacotes 700-716 (PoderesKillua) e o PacoteEfeitoDespertar do Godspeed
 * (kit 12), toca a animacao do corpo (player_animation/killua.json) e desenha (RenderMagiasKillua).
 */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class EfeitosKillua {
    /** Copias dos tipos de PoderesKillua (mesmos valores; aqui para nao depender da ordem de compilacao). */
    static final int NARUKAMI=700,ECHO=701,YOYO=702,GARRAS=703,FAISCA=704,GODSPEED=706,
            RELAMPAGO=710,WHIRLPOOL=711,CONTRA=712,SOCO=713,PULSO=714,TEMPESTADE=715,CANCELADO=716,GARRAS_GOLPE=717;

    /** Estado de um efeito. golpe: tick (relativo) do contra-ataque do Whirlpool; fim: corte antecipado (716). */
    public static final class Efeito{
        final int tipo;final UUID jogador;final Vec3 origem,alvo;final long inicio;final Object mundo;
        double golpe=Double.NaN,fim=Double.POSITIVE_INFINITY;Vec3 golpeO,golpeD;
        /** Pulso: entidades alcancadas (fotografadas no cast) e o tick em que a esfera chega em cada uma. */
        final List<Integer> alvos=new ArrayList<>();final List<Double> toques=new ArrayList<>();
        Efeito(int tipo,UUID jogador,Vec3 origem,Vec3 alvo,long inicio,Object mundo){this.tipo=tipo;this.jogador=jogador;this.origem=origem;this.alvo=alvo;this.inicio=inicio;this.mundo=mundo;}
    }
    private static final List<Efeito> EFEITOS=new ArrayList<>();
    /** Forma Godspeed ativa: onde e quando o G foi usado, ate quando dura e a aura de tecido (simulada a 20 Hz). */
    public static final class Godspeed{
        final long inicio;long fim;final Vec3 centro;final double yaw;final Object mundo;
        final AuraTecidoKillua aura=new AuraTecidoKillua();boolean ticada,noChao=true;Vec3 pes;
        Godspeed(long inicio,long fim,Vec3 centro,double yaw,Object mundo){this.inicio=inicio;this.fim=fim;this.centro=centro;this.yaw=yaw;this.mundo=mundo;}
    }
    static final Map<UUID,Godspeed> GODSPEED_ATIVO=new HashMap<>();

    /** PacoteEfeitoDespertar do kit 12: 200 = Godspeed comecou (ticks = duracao, a = pes), 0 = acabou. */
    public static void receberDespertar(com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;long agora=mc.level.getGameTime();
        if(p.tipo()==0){var g=GODSPEED_ATIVO.get(p.dono());if(g!=null)g.fim=Math.min(g.fim,agora);AnimacoesGojo.cancelarSeTipo(p.dono(),GODSPEED);return;}
        if(p.tipo()!=200||p.ticks()<1||p.ticks()>12000)return;
        var dono=mc.level.getPlayerByUUID(p.dono());double yaw=dono!=null?dono.getYRot():0;
        GODSPEED_ATIVO.put(p.dono(),new Godspeed(agora,agora+p.ticks(),p.a(),yaw,mc.level));
        AnimacoesGojo.tocarKillua(p.dono(),GODSPEED);
        com.cauansilva.arcanecontrol.core.client.AudioAnime.programar(p.dono(),"killua_godspeed",p.a(),com.cauansilva.arcanecontrol.core.audio.RoteiroAudio.killua(GODSPEED));
    }
    /** Tick (com fracao) desde o G, ou -1 se este jogador nao esta no Godspeed. */
    public static double godspeed(UUID id,float parcial){var mc=Minecraft.getInstance();var g=GODSPEED_ATIVO.get(id);
        return g==null||mc.level==null||g.mundo!=mc.level||mc.level.getGameTime()>g.fim?-1:mc.level.getGameTime()-g.inicio+parcial;}

    public static boolean trata(int tipo){return tipo>=NARUKAMI&&tipo<=GARRAS_GOLPE;}
    public static void iniciar(int tipo,UUID id,Vec3 origem,Vec3 alvo){
        {var sons=com.cauansilva.arcanecontrol.core.audio.RoteiroAudio.killua(tipo);if(!sons.isEmpty())com.cauansilva.arcanecontrol.core.client.AudioAnime.programar(id,"killua_"+tipo,origem,sons);}
        var mc=Minecraft.getInstance();if(mc.level==null||!trata(tipo))return;long agora=mc.level.getGameTime();
        if(tipo==CANCELADO){int c=(int)Math.round(alvo.x);com.cauansilva.arcanecontrol.core.client.AudioAnime.cancelar(id,"killua_"+c);
            for(var e:EFEITOS)if(e.jogador.equals(id)&&e.tipo==c)e.fim=Math.min(e.fim,agora-e.inicio+3);
            if(c!=FAISCA)AnimacoesGojo.cancelarSeTipo(id,c);return;}
        if(tipo==CONTRA){
            for(var e:EFEITOS)if(e.tipo==WHIRLPOOL&&e.jogador.equals(id)&&Double.isNaN(e.golpe)){e.golpe=agora-e.inicio;e.golpeO=origem;e.golpeD=alvo;e.fim=Double.POSITIVE_INFINITY;AnimacoesGojo.tocarKillua(id,CONTRA);return;}
            // guarda ja sumiu no cliente: contra sozinho, ancorado no Killua (atacante - dir * ATACANTE)
            Vec3 d=new Vec3(alvo.x,0,alvo.z);d=d.lengthSqr()<1e-6?new Vec3(0,0,1):d.normalize();
            var e=new Efeito(WHIRLPOOL,id,origem.subtract(d.scale(KilluaWhirlpool.ATACANTE)),d,agora,mc.level);e.golpe=0;e.golpeO=origem;e.golpeD=d;
            EFEITOS.removeIf(f->f.jogador.equals(id)&&f.tipo==WHIRLPOOL);adicionar(e);AnimacoesGojo.tocarKillua(id,CONTRA);return;
        }
        if(tipo==GARRAS_GOLPE){
            // corte critico das Garras: quadro ancorado no alvo real (alvo em KilluaBase.ALVO), comecando logo antes do golpe
            Vec3 d=new Vec3(alvo.x-origem.x,0,alvo.z-origem.z);d=d.lengthSqr()<1e-6?new Vec3(0,0,1):d.normalize();
            EFEITOS.removeIf(f->f.jogador.equals(id)&&f.tipo==GARRAS);
            adicionar(new Efeito(GARRAS,id,alvo.subtract(d.scale(KilluaBase.ALVO)),alvo,agora-(KilluaBase.Garras.GOLPE-3),mc.level));return;
        }
        if(tipo!=FAISCA)EFEITOS.removeIf(e->e.jogador.equals(id)&&e.tipo==tipo);
        var e=new Efeito(tipo,id,origem,alvo,agora,mc.level);
        if(tipo==PULSO){
            for(LivingEntity x:mc.level.getEntitiesOfClass(LivingEntity.class,new AABB(origem,origem).inflate(KilluaPulsoEletromagnetico.RAIO),q->q.isAlive()&&!q.getUUID().equals(id))){
                double dist=Math.hypot(x.getX()-origem.x,x.getZ()-origem.z);if(dist>KilluaPulsoEletromagnetico.RAIO||e.alvos.size()>=8)continue;
                double tq=KilluaPulsoEletromagnetico.PULSO+KilluaPulsoEletromagnetico.EXPANSAO;
                for(double t=KilluaPulsoEletromagnetico.PULSO;t<KilluaPulsoEletromagnetico.PULSO+KilluaPulsoEletromagnetico.EXPANSAO+1;t+=.25)if(KilluaPulsoEletromagnetico.raioPulso(t)>=dist){tq=t;break;}
                e.alvos.add(x.getId());e.toques.add(tq);
            }
        }
        if(tipo==GARRAS)e.fim=KilluaBase.Garras.SOME+10;   // so a fumaca do sumico; o corte vem no 717, onde ele estiver
        adicionar(e);
        if(tipo!=FAISCA&&tipo!=GARRAS)AnimacoesGojo.tocarKillua(id,tipo);
    }
    private static void adicionar(Efeito e){if(EFEITOS.size()>=32)EFEITOS.remove(0);EFEITOS.add(e);}
    static double duracao(Efeito e){double d=switch(e.tipo){
        case NARUKAMI->KilluaBase.Narukami.FIM+2;
        case ECHO->KilluaBase.Echo.FIM+2;
        case YOYO->KilluaBase.Yoyo.FIM+2;
        case GARRAS->KilluaBase.Garras.FIM+2;
        case FAISCA->8;
        case RELAMPAGO->KilluaRelampagoPerfurante.FIM+2;
        case WHIRLPOOL->Double.isNaN(e.golpe)?KilluaWhirlpool.ENTRADA+KilluaWhirlpool.JANELA+2:e.golpe-KilluaWhirlpool.ACERTO+KilluaWhirlpool.FIM+2;
        case SOCO->KilluaSocoRelampago.FIM+2;
        case PULSO->KilluaPulsoEletromagnetico.FIM+2;
        case TEMPESTADE->KilluaTempestadeDivina.FIM+2;
        default->0;};return Math.min(d,e.fim);}

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent ev){
        if(ev.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        EFEITOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=duracao(f)
                ||mc.level.getPlayerByUUID(f.jogador)==null);
        GODSPEED_ATIVO.entrySet().removeIf(c->mc.level==null||c.getValue().mundo!=mc.level||mc.level.getGameTime()>c.getValue().fim+10||mc.level.getPlayerByUUID(c.getKey())==null);
        if(mc.level==null||mc.isPaused())return;long agora=mc.level.getGameTime();
        for(var c:GODSPEED_ATIVO.entrySet()){var g=c.getValue();var p=mc.level.getPlayerByUUID(c.getKey());if(p==null)continue;
            double t=agora-g.inicio;if(t<KilluaGodspeed.ATIVA)continue;
            Vec3 pos=p.position();g.aura.tick(new P(pos.x,pos.y,pos.z),-Math.toRadians(p.getYRot()),PerfilVfxGojo.suave((t-KilluaGodspeed.ATIVA)/8.0)*saida(g,agora,0));
            g.pes=pos;g.ticada=true;g.noChao=p.onGround();
        }
    }
    private static double saida(Godspeed g,long agora,float parcial){double a=agora+parcial-g.fim;return a<=0?1:Math.max(0,1-a/10);}
    @SubscribeEvent public static void render(RenderLevelStageEvent ev){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||ev.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        Vec3 cam=ev.getCamera().getPosition();float parcial=ev.getPartialTick();long agora=mc.level.getGameTime();int n=0;
        for(var c:GODSPEED_ATIVO.entrySet()){var g=c.getValue();var dono=mc.level.getPlayerByUUID(c.getKey());
            if(dono!=null&&g.mundo==mc.level&&dono.position().distanceToSqr(cam)<96*96)RenderMagiasKillua.godspeed(ev.getPoseStack(),g,dono,agora-g.inicio+parcial,cam,parcial,saida(g,agora,parcial));}
        for(var f:EFEITOS){if(f.mundo!=mc.level||f.origem.distanceToSqr(cam)>128*128||n++>=12)continue;
            RenderMagiasKillua.desenhar(ev.getPoseStack(),f,agora-f.inicio+parcial,cam,parcial);
        }
    }
    @SubscribeEvent public static void hud(RenderGuiOverlayEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||!e.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id()))return;
        if(RpgCliente.recebido()||PoderesCliente.ativo()!=PoderAnime.KILLUA)return;
        boolean g=godspeed(mc.player.getUUID(),0)>=0;
        e.getGuiGraphics().fill(8,8,370,47,0xDC101321);
        e.getGuiGraphics().drawString(mc.font,g?"KILLUA • GODSPEED • [P] Poderes":"KILLUA • [P] Poderes",14,14,0x7FC8FF);
        e.getGuiGraphics().drawString(mc.font,g?"[Z] Relâmpago [X] Whirlpool [C] Soco [V] Pulso [G] Tempestade":"[Z] Narukami [X] Echo [C] Yo-yo [V] Garras [G] Godspeed",14,30,0xDDE6F2);
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){EFEITOS.clear();GODSPEED_ATIVO.clear();}
    private EfeitosKillua(){}
}
