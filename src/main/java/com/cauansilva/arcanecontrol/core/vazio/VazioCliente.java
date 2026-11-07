package com.cauansilva.arcanecontrol.core.vazio;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Transicao do Void System no cliente (pacotes 800/801 de VazioServidor): tela preta que clareia na arena com o HUD
 * escondido durante a entrada travada; na saida, so o fade. Com o 802 (cena) a camera segue CinematicaEntrada: passeio
 * em spline pela arena, close no chefe com a fala dele e volta aos olhos do jogador, com faixas de cinema.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class VazioCliente {
    /** Entrada: preto cheio por SEGURA ticks, clareia em CLAREIA; HUD volta no fim da entrada travada. */
    static final int SEGURA=10,CLAREIA=30,SAIDA_SEGURA=6,SAIDA_CLAREIA=20;
    private static long inicio=-1000,fimHud=-1000;private static int segura,clareia;
    /** Cena da entrada: centro do chao da arena (mundo), chefe, camera solta. */
    private static net.minecraft.world.phys.Vec3 centro;private static int kit;private static double altura;private static long cena=-1000;
    private static net.minecraft.world.entity.Entity camera;

    public static boolean trata(int tipo){return tipo==VazioServidor.ENTRADA||tipo==VazioServidor.SAIDA||tipo==VazioServidor.CENA;}
    public static void cena(net.minecraft.world.phys.Vec3 o,net.minecraft.world.phys.Vec3 a){
        var mc=Minecraft.getInstance();if(mc.level==null)return;centro=o;kit=(int)Math.round(a.x);altura=Math.max(1,a.y);cena=inicio;
    }
    public static void iniciar(int tipo){
        var mc=Minecraft.getInstance();if(mc.level==null)return;long agora=mc.level.getGameTime();inicio=agora;
        if(tipo==VazioServidor.ENTRADA){segura=SEGURA;clareia=CLAREIA;fimHud=agora+EstadoInstancia.ENTRADA;}
        else{segura=SAIDA_SEGURA;clareia=SAIDA_CLAREIA;fimHud=agora;}
    }
    /** Opacidade do preto agora (0 = nada). */
    static float preto(float parcial){
        var mc=Minecraft.getInstance();if(mc.level==null)return 0;double t=mc.level.getGameTime()-inicio+parcial;
        if(t<0||t>segura+clareia)return 0;if(t<segura)return 1;double u=(t-segura)/clareia;return (float)(1-u*u*(3-2*u));
    }
    private static double tCena(float parcial){var mc=Minecraft.getInstance();return mc.level==null||centro==null?-1:mc.level.getGameTime()-cena+parcial;}
    private static boolean emCena(){double t=tCena(0);return t>=CinematicaEntrada.BLACKOUT&&t<CinematicaEntrada.FIM;}
    private static CinematicaEntrada.V rel(net.minecraft.world.phys.Vec3 v){return new CinematicaEntrada.V(v.x-centro.x,v.y-centro.y,v.z-centro.z);}
    /** Camera solta: um marcador fora do mundo que a camera segue; volta ao jogador no fim. */
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null){camera=null;return;}
        // fora da cena a camera e sempre a do jogador (tambem se um marcador ficou preso por troca de mundo)
        if(!emCena()){if(mc.getCameraEntity() instanceof net.minecraft.world.entity.Marker)mc.setCameraEntity(mc.player);camera=null;return;}
        if(camera==null||camera.level()!=mc.level){camera=net.minecraft.world.entity.EntityType.MARKER.create(mc.level);if(camera==null)return;}
        double t=tCena(0)+1;var q=CinematicaEntrada.quadro(t,rel(mc.player.getEyePosition()),new CinematicaEntrada.V(PlantaArena.RAIO_CHEFE,0,0),altura);
        double x=centro.x+q.pos().x(),y=centro.y+q.pos().y(),z=centro.z+q.pos().z();
        double dx=q.olha().x()-q.pos().x(),dy=q.olha().y()-q.pos().y(),dz=q.olha().z()-q.pos().z();
        float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz)),pitch=(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz)));
        boolean primeiro=mc.getCameraEntity()!=camera;
        camera.xo=primeiro?x:camera.getX();camera.yo=primeiro?y:camera.getY();camera.zo=primeiro?z:camera.getZ();
        camera.yRotO=primeiro?yaw:camera.getYRot();camera.xRotO=primeiro?pitch:camera.getXRot();
        // evita giro de 360 graus na interpolacao do yaw
        while(yaw-camera.yRotO>180)yaw-=360;while(yaw-camera.yRotO<-180)yaw+=360;
        camera.setPos(x,y,z);camera.setYRot(yaw);camera.setXRot(pitch);
        if(primeiro)mc.setCameraEntity(camera);
    }
    /** Sem a mao do jogador na tela enquanto a camera esta solta. */
    @SubscribeEvent public static void mao(RenderHandEvent e){if(emCena())e.setCanceled(true);}
    private static boolean hudEscondido(){var mc=Minecraft.getInstance();return mc.level!=null&&mc.level.getGameTime()<fimHud;}

    @SubscribeEvent public static void esconder(RenderGuiOverlayEvent.Pre e){if(hudEscondido())e.setCanceled(true);}
    @SubscribeEvent public static void fade(RenderGuiEvent.Post e){
        float a=preto(e.getPartialTick());if(a<=.003f)return;var g=e.getGuiGraphics();
        g.fill(0,0,g.guiWidth(),g.guiHeight(),((int)(a*255)<<24));
    }
    /** Faixas de cinema e a fala do chefe durante a cena. */
    @SubscribeEvent public static void legenda(RenderGuiEvent.Pre e){
        double t=tCena(e.getPartialTick());if(t<CinematicaEntrada.BLACKOUT||t>CinematicaEntrada.FIM+10)return;
        var g=e.getGuiGraphics();int w=g.guiWidth(),h=g.guiHeight();
        double fx=Math.min(1,Math.min((t-CinematicaEntrada.BLACKOUT)/10,(CinematicaEntrada.FIM+10-t)/10));int faixa=(int)(h*.11*Math.max(0,fx));
        g.fill(0,0,w,faixa,0xFF000000);g.fill(0,h-faixa,w,h,0xFF000000);
        if(t>=CinematicaEntrada.FALA&&t<CinematicaEntrada.FIM+10){
            var mc=Minecraft.getInstance();float al=(float)Math.min(1,Math.min((t-CinematicaEntrada.FALA)/6,(CinematicaEntrada.FIM+10-t)/8));int a=(int)(al*255)<<24;
            var rpg=com.cauansilva.arcanecontrol.core.rpg.PersonagemRpg.porId(kit);String nome=rpg==null?"":rpg.nemesis;
            String fala="“"+CinematicaEntrada.fala(kit)+"”";
            g.drawCenteredString(mc.font,nome,w/2,h-faixa-30,0xE0B060|a);g.drawCenteredString(mc.font,fala,w/2,h-faixa-16,0xF2F2F2|a);
        }
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){inicio=-1000;fimHud=-1000;cena=-1000;centro=null;camera=null;}
    private VazioCliente(){}
}
