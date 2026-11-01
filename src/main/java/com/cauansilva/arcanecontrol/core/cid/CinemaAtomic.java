package com.cauansilva.arcanecontrol.core.cid;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.vfx.RoteiroCinemaAtomic;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Own native post chain, never GameRenderer's shader slot. World depth is not cleared on copy-back. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class CinemaAtomic {
    private static ShaderInstance copia;
    private static PostChain cadeia;
    private static PostPass passe;
    private static RenderTarget alvo;
    private static int largura,altura;
    private static boolean bloqueado,aplicado;
    private static Object mundo;
    public static boolean aplicado(){return aplicado;}
    private static boolean habilitado(){return ConfigCliente.CINEMA_ATOMIC.get()&&!ConfigCliente.VFX_REDUZIDOS.get()&&ConfigCliente.INTENSIDADE_ATOMIC.get()>0;}
    private static void fechar(){
        if(cadeia!=null){
            int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            try{cadeia.close();}finally{GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw==0||GL30.glIsFramebuffer(draw)?draw:0);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read==0||GL30.glIsFramebuffer(read)?read:0);}
        }
        cadeia=null;passe=null;alvo=null;largura=altura=0;aplicado=false;
    }
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){fechar();mundo=null;bloqueado=false;}
    @Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Recursos {
        @SubscribeEvent public static void shader(RegisterShadersEvent e){
            fechar();copia=null;bloqueado=false;
            try{e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("arcanecontrol","atomic_copy"),DefaultVertexFormat.POSITION_TEX),s->copia=s);}
            catch(java.io.IOException ex){com.mojang.logging.LogUtils.getLogger().warn("Cópia do pós-processamento Atomic indisponível; apresentação anterior preservada",ex);}
        }
    }
    private static void preparar(Minecraft mc)throws java.io.IOException{
        var principal=mc.getMainRenderTarget();
        if(cadeia==null||alvo!=principal){
            fechar();alvo=principal;
            cadeia=new PostChain(mc.getTextureManager(),mc.getResourceManager(),alvo,new ResourceLocation("arcanecontrol","shaders/post/atomic_cinema.json"));
            passe=cadeia.addPass("arcanecontrol:atomic_cinema",alvo,cadeia.getTempTarget("atomic_swap"));
            passe.addAuxAsset("DepthSampler",alvo::getDepthTextureId,alvo.width,alvo.height);
        }
        if(largura!=principal.width||altura!=principal.height){largura=principal.width;altura=principal.height;cadeia.resize(largura,altura);}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL)return;
        aplicado=false;var mc=Minecraft.getInstance();
        if(mundo!=mc.level){fechar();mundo=mc.level;}
        if(mc.level==null||mc.player==null||!habilitado()||copia==null||bloqueado){if(cadeia!=null)fechar();return;}
        var visual=EfeitosCid.atomicVisual(e.getPartialTick());
        if(visual==null){if(cadeia!=null)fechar();return;}
        // A foreign pipeline still owning the framebuffer must finish its own world pass.
        // Do not redirect that framebuffer or silently replace GameRenderer's post effect.
        if(GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)!=mc.getMainRenderTarget().frameBufferId)return;
        var camera=e.getCamera().getPosition();
        float peso=(float)(RoteiroCinemaAtomic.peso(visual.tick(),camera.distanceTo(visual.centro()),visual.local())*ConfigCliente.INTENSIDADE_ATOMIC.get());
        if(peso<=.001||mc.getMainRenderTarget().getDepthTextureId()<0)return;
        var shaderAnterior=RenderSystem.getShader();int texturaAnterior=RenderSystem.getShaderTexture(0);
        try{
            preparar(mc);
            var rel=visual.centro().subtract(camera);
            var clip=new Vector4f((float)rel.x,(float)rel.y,(float)rel.z,1).mul(e.getPoseStack().last().pose()).mul(e.getProjectionMatrix());
            float x=.5f,y=.5f;
            if(clip.w>.01f){x=Math.max(.04f,Math.min(.96f,.5f+clip.x/clip.w*.5f));y=Math.max(.04f,Math.min(.96f,.5f+clip.y/clip.w*.5f));}
            var fx=passe.getEffect();fx.safeGetUniform("AtomicTick").set((float)visual.tick());
            fx.safeGetUniform("Strength").set(peso);fx.safeGetUniform("Frames").set(ConfigCliente.IMPACT_FRAMES.get()&&ConfigCliente.CLARAO.get()?1f:0f);
            fx.safeGetUniform("ScreenCenter").set(x,y);
            RenderSystem.disableBlend();RenderSystem.disableDepthTest();RenderSystem.depthMask(false);
            cadeia.process(e.getPartialTick());
            // Native PostPass clears its output. Never make the main target its output:
            // retain the original depth attachment for the hand, other renderers and shader packs.
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableBlend();RenderSystem.disableCull();
            RenderSystem.setShader(()->copia);RenderSystem.setShaderTexture(0,cadeia.getTempTarget("atomic_swap").getColorTextureId());
            var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
            b.vertex(-1,-1,0).uv(0,0).endVertex();b.vertex(1,-1,0).uv(1,0).endVertex();b.vertex(1,1,0).uv(1,1).endVertex();b.vertex(-1,1,0).uv(0,1).endVertex();
            BufferUploader.drawWithShader(b.end());aplicado=true;
        }catch(java.io.IOException|RuntimeException ex){
            fechar();bloqueado=true;com.mojang.logging.LogUtils.getLogger().warn("Pós-processamento Atomic desativado até recarga dos recursos",ex);
        }finally{
            mc.getMainRenderTarget().bindWrite(true);RenderSystem.depthMask(true);RenderSystem.enableDepthTest();RenderSystem.enableCull();RenderSystem.defaultBlendFunc();RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0,texturaAnterior);if(shaderAnterior!=null)RenderSystem.setShader(()->shaderAnterior);
        }
    }
}
