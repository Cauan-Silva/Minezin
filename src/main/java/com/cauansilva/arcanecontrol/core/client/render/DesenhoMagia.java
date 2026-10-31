package com.cauansilva.arcanecontrol.core.client.render;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import java.io.IOException;

/** Coordenadas locais: o chamador posiciona o PoseStack em origem-camera. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class DesenhoMagia {
    private static ShaderInstance base,brilho,atual;private static float tempo;
    @SubscribeEvent public static void registrar(RegisterShadersEvent e){
        base=null;brilho=null;
        try{
            e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("arcanecontrol","anime_energy"),DefaultVertexFormat.POSITION_COLOR_TEX),s->base=s);
            e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("arcanecontrol","anime_energy_additive"),DefaultVertexFormat.POSITION_COLOR_TEX),s->brilho=s);
        }catch(IOException ex){com.mojang.logging.LogUtils.getLogger().warn("Materiais de magia indisponiveis",ex);}
    }
    public static boolean iniciar(double tick){
        if(base==null||brilho==null)return false;
        tempo=(float)(tick/20);RenderSystem.enableBlend();RenderSystem.enableDepthTest();RenderSystem.disableCull();RenderSystem.depthMask(false);return true;
    }
    public static void terminar(){RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.defaultBlendFunc();RenderSystem.disableBlend();RenderSystem.setShader(GameRenderer::getPositionColorShader);}
    private static void material(int modo,boolean aditivo,Vec3 view){
        atual=aditivo?brilho:base;RenderSystem.setShader(()->atual);
        atual.safeGetUniform("EffectTime").set(tempo);atual.safeGetUniform("EffectMode").set((float)modo);
        Vec3 v=view.lengthSqr()<1e-8?new Vec3(0,0,1):view.normalize();atual.safeGetUniform("Viewer").set((float)v.x,(float)v.y,(float)v.z);
    }
    public static void lote(PoseStack pose,int modo,boolean aditivo,Runnable desenho){
        material(modo,aditivo,new Vec3(0,0,1));var b=Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);desenho.run();Tesselator.getInstance().end();
    }
    public static void fita(PoseStack pose,Vec3 cam,Vec3 a,Vec3 b,double largura,double u,double v,int cor,float alpha){
        Vec3 d=b.subtract(a);if(d.lengthSqr()<1e-10)return;
        Vec3 lado=d.cross(cam.subtract(a)).normalize();
        if(lado.lengthSqr()<1e-10)lado=d.cross(new Vec3(0,1,0)).normalize();
        if(lado.lengthSqr()<1e-10)lado=new Vec3(1,0,0);
        lado=lado.scale(largura);
        ponto(pose,a.subtract(lado),u,0,cor,alpha);ponto(pose,b.subtract(lado),v,0,cor,alpha);
        ponto(pose,b.add(lado),v,1,cor,alpha);ponto(pose,a.add(lado),u,1,cor,alpha);
    }
    public static void anel(PoseStack pose,Vec3 centro,Vec3 normal,double interno,double externo,int cor,float alpha,int lados){
        Vec3 u=normal.cross(new Vec3(0,1,0)).normalize();if(u.lengthSqr()<1e-8)u=new Vec3(1,0,0);
        Vec3 v=normal.cross(u).normalize();
        for(int i=0;i<lados;i++){
            double a=i*Math.PI*2/lados,b=(i+1)*Math.PI*2/lados;
            Vec3 p=u.scale(Math.cos(a)).add(v.scale(Math.sin(a))),q=u.scale(Math.cos(b)).add(v.scale(Math.sin(b)));
            ponto(pose,centro.add(p.scale(interno)),i*.3,0,cor,alpha);ponto(pose,centro.add(q.scale(interno)),(i+1)*.3,0,cor,alpha);
            ponto(pose,centro.add(q.scale(externo)),(i+1)*.3,1,cor,alpha);ponto(pose,centro.add(p.scale(externo)),i*.3,1,cor,alpha);
        }
    }
    public static void selo(PoseStack pose,Vec3 cam,Vec3 centro,Vec3 normal,double raio,double t,int cor,float alpha){
        anel(pose,centro,normal,raio*.94,raio,cor,alpha,64);
        anel(pose,centro,normal,raio*.69,raio*.72,cor,alpha*.7f,48);
        Vec3 u=normal.cross(new Vec3(0,1,0)).normalize();if(u.lengthSqr()<1e-8)u=new Vec3(1,0,0);
        Vec3 v=normal.cross(u).normalize();
        for(int j=0;j<8;j++){
            double a=j*Math.PI/4+t*.012,b=a+.18;
            Vec3 radial=u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));
            Vec3 borda=u.scale(Math.cos(b)).add(v.scale(Math.sin(b)));
            fita(pose,cam,centro.add(radial.scale(raio*.76)),centro.add(borda.scale(raio*.9)),.024,0,1,cor,alpha);
            fita(pose,cam,centro.add(borda.scale(raio*.9)),centro.add(radial.scale(raio*.9)),.024,0,1,cor,alpha);
        }
        for(int j=0;j<6;j++){
            double a=j*Math.PI/3-t*.01,b=a+Math.PI*2/3;
            Vec3 p=u.scale(Math.cos(a)).add(v.scale(Math.sin(a))).scale(raio*.6);
            Vec3 q=u.scale(Math.cos(b)).add(v.scale(Math.sin(b))).scale(raio*.6);
            fita(pose,cam,centro.add(p),centro.add(q),.018,0,2,cor,alpha*.6f);
        }
    }
    public static void esfera(PoseStack pose,Vec3 cam,Vec3 centro,double raio,int modo,int cor,float alpha,boolean aditivo,int segmentos){
        if(modo==3&&alpha>.95f)RenderSystem.depthMask(true);
        material(modo,aditivo,cam.subtract(centro));var b=Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);
        for(int i=0;i<segmentos;i++)for(int j=0;j<segmentos*2;j++){
            esferaPonto(pose,centro,raio,i,j,segmentos,cor,alpha);esferaPonto(pose,centro,raio,i+1,j,segmentos,cor,alpha);
            esferaPonto(pose,centro,raio,i+1,j+1,segmentos,cor,alpha);esferaPonto(pose,centro,raio,i,j+1,segmentos,cor,alpha);
        }Tesselator.getInstance().end();RenderSystem.depthMask(false);
    }
    public static void caixa(PoseStack p,Vec3 centro,double x,double y,double z,int cor,float a){
        Vec3[] v=new Vec3[8];
        for(int i=0;i<8;i++)v[i]=centro.add((i&1)==0?-x:x,(i&2)==0?-y:y,(i&4)==0?-z:z);
        int[][] faces={{0,1,3,2},{4,6,7,5},{0,4,5,1},{2,3,7,6},{0,2,6,4},{1,5,7,3}};
        for(int[] f:faces){ponto(p,v[f[0]],0,0,cor,a);ponto(p,v[f[1]],1,0,cor,a);ponto(p,v[f[2]],1,1,cor,a);ponto(p,v[f[3]],0,1,cor,a);}
    }
    private static void esferaPonto(PoseStack p,Vec3 centro,double r,int i,int j,int n,int c,float a){
        double ang=Math.PI*j/n,l=-Math.PI/2+Math.PI*i/n;
        ponto(p,centro.add(Math.cos(ang)*Math.cos(l)*r,Math.sin(l)*r,Math.sin(ang)*Math.cos(l)*r),j/(double)(n*2),i/(double)n,c,a);
    }
    public static void ponto(PoseStack p,Vec3 pos,double u,double v,int cor,float alpha){
        Tesselator.getInstance().getBuilder().vertex(p.last().pose(),(float)pos.x,(float)pos.y,(float)pos.z)
                .color((cor>>16&255)/255f,(cor>>8&255)/255f,(cor&255)/255f,alpha).uv((float)u,(float)v).endVertex();
    }
}
