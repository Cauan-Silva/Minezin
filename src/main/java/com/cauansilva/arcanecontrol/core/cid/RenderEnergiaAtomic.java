package com.cauansilva.arcanecontrol.core.cid;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import java.io.IOException;

@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RenderEnergiaAtomic {
    private static ShaderInstance shader;
    @SubscribeEvent public static void registrar(RegisterShadersEvent e) {
        shader=null;
        try {
            e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("arcanecontrol","atomic_energy"),
                    DefaultVertexFormat.POSITION_COLOR_TEX),s->shader=s);
        } catch(IOException ex) {
            com.mojang.logging.LogUtils.getLogger().warn("Shader Atomic indisponivel; usando geometria anterior",ex);
        }
    }

    public static boolean desenhar(PoseStack pose,double tick,Vec3 camera,Vec3 ancora) {
        if(shader==null || !ConfigCliente.SHADER_ATOMIC.get()) return false;
        if(tick<0 || tick>=240) return true;
        // Release now comes from ExplosaoAtomic via RenderMovesetCid, including the sustained column.
        if(tick>=90)return true;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest(); RenderSystem.depthMask(false); RenderSystem.disableCull();
        RenderSystem.setShader(()->shader);
        shader.safeGetUniform("EffectTime").set((float)(tick/20));
        try {
            var b=Tesselator.getInstance().getBuilder(); var m=pose.last().pose();
            boolean reduzido=ConfigCliente.VFX_REDUZIDOS.get();
            if(tick<90) {
                shader.safeGetUniform("EffectMode").set(0f);
                b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);
                int caminhos=reduzido?5:9, passos=reduzido?16:28;
                float alpha=(float)Math.min(1,.15+tick/70);
                for(int j=0;j<caminhos;j++) for(int i=0;i<passos;i++) {
                    double s=i/(double)passos, u=(i+1)/(double)passos;
                    Vec3 a=ponto(FormaEnergiaAtomic.filamento(j,s,tick)).add(ancora);
                    Vec3 c=ponto(FormaEnergiaAtomic.filamento(j,u,tick)).add(ancora);
                    fita(b,m,a,c,camera,.07+tick*.001,s*4,u*4,alpha);
                }
                int linhas=reduzido?14:42;
                for(int j=0;j<linhas;j++)for(int i=0;i<8;i++){
                    Vec3 a=ponto(FormaEnergiaAtomic.campo(j,i/8d,tick)),c=ponto(FormaEnergiaAtomic.campo(j,(i+1)/8d,tick));
                    fita(b,m,a,c,camera,.025,0,1,alpha*.55f);
                }
                if(tick>50)for(int anel=0;anel<2;anel++)for(int i=0;i<64;i++){
                    double a=i*Math.PI/32,c=(i+1)*Math.PI/32,r=1.8+anel*.55;
                    fita(b,m,new Vec3(Math.cos(a)*r,3.6+anel*.35,Math.sin(a)*r),new Vec3(Math.cos(c)*r,3.6+anel*.35,Math.sin(c)*r),camera,.045,0,1,alpha*.8f);
                }
                Tesselator.getInstance().end();
                casca(b,m,ancora,tick,.15+tick*.003,alpha*.65f,reduzido?12:20);
            }

        } finally {
            RenderSystem.depthMask(true); RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc(); RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }
        return true;
    }
    private static Vec3 ponto(FormaEnergiaAtomic.Ponto p){return new Vec3(p.x(),p.y(),p.z());}
    private static void fita(BufferBuilder b,Matrix4f m,Vec3 a,Vec3 c,Vec3 camera,double largura,double u,double v,float alpha) {
        Vec3 lado=c.subtract(a).cross(camera.subtract(a)).normalize().scale(largura);
        if(lado.lengthSqr()<1e-10) lado=new Vec3(largura,0,0);
        vertice(b,m,a.subtract(lado),u,0,alpha);vertice(b,m,c.subtract(lado),v,0,alpha);
        vertice(b,m,c.add(lado),v,1,alpha);vertice(b,m,a.add(lado),u,1,alpha);
    }
    private static void casca(BufferBuilder b,Matrix4f m,Vec3 centro,double tick,double raio,float alpha,int n) {
        shader.safeGetUniform("EffectMode").set(1f);
        b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);
        for(int i=0;i<n;i++) for(int j=0;j<n*2;j++) {
            esfera(b,m,centro,tick,raio,i,j,n,alpha);esfera(b,m,centro,tick,raio,i+1,j,n,alpha);
            esfera(b,m,centro,tick,raio,i+1,j+1,n,alpha);esfera(b,m,centro,tick,raio,i,j+1,n,alpha);
        }
        Tesselator.getInstance().end();
    }
    private static void esfera(BufferBuilder b,Matrix4f m,Vec3 centro,double tick,double r,int i,int j,int n,float alpha) {
        double lat=-Math.PI/2+Math.PI*i/n,ang=Math.PI*j/n;
        double raio=FormaEnergiaAtomic.raio(ang,lat,tick,r);
        vertice(b,m,centro.add(Math.cos(ang)*Math.cos(lat)*raio,Math.sin(lat)*raio,Math.sin(ang)*Math.cos(lat)*raio),j/(double)(n*2),i/(double)n,alpha);
    }
    private static void vertice(BufferBuilder b,Matrix4f m,Vec3 p,double u,double v,float alpha) {
        b.vertex(m,(float)p.x,(float)p.y,(float)p.z).color(1f,1f,1f,alpha).uv((float)u,(float)v).endVertex();
    }
}
