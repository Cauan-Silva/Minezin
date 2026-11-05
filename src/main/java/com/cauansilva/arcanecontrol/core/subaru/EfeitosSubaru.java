package com.cauansilva.arcanecontrol.core.subaru;

import com.cauansilva.arcanecontrol.core.client.*;
import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;
import com.cauansilva.arcanecontrol.core.vfx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Efeitos do kit base do Subaru no cliente: recebe os pacotes 600-605 (PoderesSubaru), toca a animacao do corpo e desenha. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class EfeitosSubaru {
    /** Estado de um efeito. acerto/idAlvo: Shamak (601). quebra: tick (relativo) em que o escudo estoura. */
    public static final class Efeito{
        final int tipo;final UUID jogador;final Vec3 origem,alvo;final long inicio;final Object mundo;
        Vec3 acerto;int idAlvo=-1;double quebra=Double.POSITIVE_INFINITY;Vec3 golpe;final List<UUID> recebem=new ArrayList<>();final List<Double> chegam=new ArrayList<>();
        Efeito(int tipo,UUID jogador,Vec3 origem,Vec3 alvo,long inicio,Object mundo){this.tipo=tipo;this.jogador=jogador;this.origem=origem;this.alvo=alvo;this.inicio=inicio;this.mundo=mundo;}
    }
    private static final List<Efeito> EFEITOS=new ArrayList<>();
    /** Forma de contrato ativa: onde e quando o G foi usado (referencial da entrada) e ate quando dura. */
    public static final class Contrato{
        final long inicio,fim;final Vec3 centro;final double yaw;final Object mundo;Vec3 beatrice;double giro;
        Contrato(long inicio,long fim,Vec3 centro,double yaw,Object mundo){this.inicio=inicio;this.fim=fim;this.centro=centro;this.yaw=yaw;this.mundo=mundo;}
    }
    static final Map<UUID,Contrato> CONTRATOS=new HashMap<>();
    /** PacoteEfeitoDespertar do kit 16: 200 = contrato comecou (ticks = duracao), 0 = acabou. */
    public static void receberDespertar(com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(p.tipo()==0){CONTRATOS.remove(p.dono());return;}
        if(p.tipo()!=200||p.ticks()<1||p.ticks()>2400)return;
        var dono=mc.level.getPlayerByUUID(p.dono());double yaw=dono!=null?dono.getYRot():0;long agora=mc.level.getGameTime();
        CONTRATOS.put(p.dono(),new Contrato(agora,agora+p.ticks(),p.a(),yaw,mc.level));
        AnimacoesGojo.tocarSubaru(p.dono(),606);
    }
    /** Tick (com fracao) desde o G, ou -1 se este jogador nao esta na forma. */
    public static double contrato(UUID id,float parcial){var mc=Minecraft.getInstance();var c=CONTRATOS.get(id);return c==null||mc.level==null||c.mundo!=mc.level?-1:mc.level.getGameTime()-c.inicio+parcial;}

    public static boolean trata(int tipo){return tipo>=PoderesSubaru.SHAMAK&&tipo<=PoderesSubaru.RECUPERACAO||tipo==PoderesSubaru.MINYA;}
    /** Tick (com fracao) da magia `tipo` da Beatrice em andamento para este jogador, ou -1. */
    public static double magia(UUID id,int tipo,float parcial){var mc=Minecraft.getInstance();if(mc.level==null)return -1;
        for(var e:EFEITOS)if(e.tipo==tipo&&e.jogador.equals(id)&&e.mundo==mc.level)return mc.level.getGameTime()-e.inicio+parcial;return -1;}
    public static void iniciar(int tipo,UUID id,Vec3 origem,Vec3 alvo){
        var mc=Minecraft.getInstance();if(mc.level==null)return;long agora=mc.level.getGameTime();
        if(tipo==PoderesSubaru.SHAMAK_ACERTO){
            for(var e:EFEITOS)if(e.tipo==PoderesSubaru.SHAMAK&&e.jogador.equals(id)&&e.acerto==null){e.acerto=origem;e.idAlvo=(int)alvo.x;return;}
            return;
        }
        if(tipo==PoderesSubaru.ESCUDO_QUEBRA){
            for(var e:EFEITOS)if(e.tipo==PoderesSubaru.ESCUDO&&e.jogador.equals(id)&&Double.isInfinite(e.quebra)){
                e.quebra=agora-e.inicio+4;e.golpe=alvo.lengthSqr()<1e-6?null:new Vec3(alvo.x,0,alvo.z).normalize();}
            return;
        }
        EFEITOS.removeIf(e->e.jogador.equals(id)&&e.tipo==tipo);
        if(EFEITOS.size()>=32)EFEITOS.remove(0);
        var e=new Efeito(tipo,id,origem,alvo,agora,mc.level);
        if(tipo==PoderesSubaru.INSTRUCAO){
            var dono=mc.level.getPlayerByUUID(id);
            for(Player o:mc.level.players()){
                double d=o.position().distanceTo(origem);
                if(d<=InstrucaoSubaru.RAIO&&(o.getUUID().equals(id)||dono!=null&&dono.isAlliedTo(o))){e.recebem.add(o.getUUID());e.chegam.add(InstrucaoSubaru.chegada(d));}
            }
        }
        EFEITOS.add(e);
        AnimacoesGojo.tocarSubaru(id,tipo);
    }
    static int duracao(Efeito e){return switch(e.tipo){
        case PoderesSubaru.SHAMAK->SamakSubaru.FIM+2;
        case PoderesSubaru.ESCUDO->(int)Math.min(EscudoSubaru.DURACAO+10,e.quebra+EscudoSubaru.ESTOURO+1);
        case PoderesSubaru.INSTRUCAO->InstrucaoSubaru.ORDEM+InstrucaoSubaru.PULSO+InstrucaoSubaru.DURACAO+2;
        case PoderesSubaru.RECUPERACAO->RecuperacaoSubaru.FIM+2;
        case PoderesSubaru.MINYA->MinyaBeatrice.fim()+2;default->0;};}

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent ev){
        if(ev.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        EFEITOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=duracao(f)
                ||mc.level.getPlayerByUUID(f.jogador)==null);
        CONTRATOS.entrySet().removeIf(c->mc.level==null||c.getValue().mundo!=mc.level||mc.level.getGameTime()>c.getValue().fim+20||mc.level.getPlayerByUUID(c.getKey())==null);
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent ev){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;Vec3 cam=ev.getCamera().getPosition();
        if(ev.getStage()==RenderLevelStageEvent.Stage.AFTER_ENTITIES){
            for(var c:CONTRATOS.entrySet()){var dono=mc.level.getPlayerByUUID(c.getKey());
                if(dono!=null&&dono.position().distanceToSqr(cam)<96*96)RenderBeatrice.desenhar(ev.getPoseStack(),c.getValue(),dono,mc.level.getGameTime()-c.getValue().inicio+ev.getPartialTick(),cam,ev.getPartialTick());}
            return;
        }
        if(ev.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;int n=0;
        for(var c:CONTRATOS.entrySet()){var dono=mc.level.getPlayerByUUID(c.getKey());
            if(dono!=null&&c.getValue().centro.distanceToSqr(cam)<96*96)RenderMagiasSubaru.contrato(ev.getPoseStack(),c.getValue(),dono,mc.level.getGameTime()-c.getValue().inicio+ev.getPartialTick(),cam,ev.getPartialTick());}
        for(var f:EFEITOS){if(f.mundo!=mc.level||f.origem.distanceToSqr(cam)>128*128||n++>=12)continue;
            RenderMagiasSubaru.desenhar(ev.getPoseStack(),f,mc.level.getGameTime()-f.inicio+ev.getPartialTick(),cam,ev.getPartialTick());
        }
    }
    @SubscribeEvent public static void hud(RenderGuiOverlayEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||!e.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id()))return;
        if(RpgCliente.recebido()||PoderesCliente.ativo()!=PoderAnime.SUBARU)return;
        e.getGuiGraphics().fill(8,8,330,47,0xDC101321);
        e.getGuiGraphics().drawString(mc.font,"SUBARU • [P] Poderes",14,14,0xB98CFF);
        e.getGuiGraphics().drawString(mc.font,"[Z] Shamak [X] Escudo [C] Instrução [V] Recuperação",14,30,0xDDE6F2);
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){EFEITOS.clear();CONTRATOS.clear();}
    private EfeitosSubaru(){}
}
