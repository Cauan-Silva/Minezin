package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import java.io.IOException;

/** Composicoes distintas por tecnica; sem efeitos globais persistentes de GPU. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RenderGojo {
    private static ShaderInstance base,brilho,shader;
    private static float tempoMaterial;
    private static final Vec3 CIMA=new Vec3(0,1,0);
    private static final int AZUL=0x259DFF, VERMELHO=0xFF253D, ROXO=0xB359FF, BRANCO=0xDDEEFF;
    @SubscribeEvent public static void registrar(RegisterShadersEvent e) {
        base=null;brilho=null;shader=null;
        try {
            e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("arcanecontrol","gojo_energy"),
                    DefaultVertexFormat.POSITION_COLOR_TEX),s->base=s);
            e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("arcanecontrol","gojo_energy_additive"),
                    DefaultVertexFormat.POSITION_COLOR_TEX),s->brilho=s);
        } catch(IOException ex) {
            com.mojang.logging.LogUtils.getLogger().warn("Shader Gojo indisponivel; usando efeitos anteriores",ex);
        }
    }
    public static boolean ativo(){return base!=null && brilho!=null && ConfigCliente.GOJO_VFX.get();}
    private static Vec3 direita(Vec3 dir) {
        Vec3 r=dir.cross(CIMA).normalize();return r.lengthSqr()<1e-8?new Vec3(1,0,0):r;
    }
    private static Vec3 olho(GerenciadorCutscene3D.Efeito3DAtivo e,Player p,float parcial) {
        return p==null?e.origem:p.getEyePosition(parcial);
    }
    public static boolean desenhar(PoseStack pose,GerenciadorCutscene3D.Efeito3DAtivo e,Player p,Vec3 camera,float parcial) {
        if(!ativo()) return false;
        double t=e.tickAtual+parcial;
        float alpha=(float)PerfilVfxGojo.opacidade(e.tipo,t);
        if(alpha<=0) return true;
        Vec3 origem=olho(e,p,parcial),dir=p==null?e.direcao:p.getViewVector(parcial);
        if((e.tipo==2 && t>=PerfilVfxGojo.VERMELHO_DISPARO) || (e.tipo==4 && t>PerfilVfxGojo.ROXO_DISPARO)) {
            origem=e.origem;dir=e.direcao;
        }
        preparar(t);
        try {
            if(e.tipo==1) azul(pose,camera,origem.add(dir.scale(7.5)),t,alpha);
            else if(e.tipo==2) vermelho(pose,camera,origem,dir,t,alpha);
            else if(e.tipo==4) roxo(pose,camera,origem,dir,t,alpha);
            else if(e.tipo==5) dominio(pose,camera,e.origem,t,alpha);
        } finally {restaurar();}
        return true;
    }
    private static void azul(PoseStack pose,Vec3 cam,Vec3 centro,double t,float alpha) {
        double r=.72*(.94+.06*Math.sin(t*.3));
        esfera(pose,cam,centro,r,0,AZUL,alpha,false);
        esfera(pose,cam,centro,r*1.65,6,AZUL,alpha*.4f,true);
        fitas(()->{
            int n=reduzido()?5:10;
            for(int j=0;j<n;j++) {
                Vec3 anterior=null;
                for(int i=0;i<=24;i++) {
                    double s=i/24.0,ang=j*2.399963+s*5-t*.09,raio=(1-s)*4.5+.75;
                    Vec3 q=centro.add(Math.cos(ang)*raio,Math.sin(s*Math.PI+j)*.6*(1-s),Math.sin(ang)*raio);
                    if(anterior!=null) fita(pose,cam,anterior,q,.055,s*4,(s+1.0/24)*4,AZUL,alpha*.75f);
                    anterior=q;
                }
            }
        });
    }
    private static void vermelho(PoseStack pose,Vec3 cam,Vec3 origem,Vec3 dir,double t,float alpha) {
        Vec3 foco=origem.add(dir.scale(1.8));
        if(t<PerfilVfxGojo.VERMELHO_DISPARO) {
            double r=.1+.32*PerfilVfxGojo.suave(t/13);
            esfera(pose,cam,foco,r,1,VERMELHO,alpha,false);
            esfera(pose,cam,foco,r*2,6,VERMELHO,alpha*.5f,true);
        } else {
            double idade=t-13,escala=1+idade*.17;
            esfera(pose,cam,foco,.5,1,VERMELHO,alpha,false);
            Vec3 lado=direita(dir),alto=lado.cross(dir).normalize();
            fitas(()->{
                // Onda inteira aparece no impacto: nao sugere um projetil que o servidor nao simula.
                for(int j=0;j<(reduzido()?6:12);j++) {
                    double a=j*Math.PI*2/(reduzido()?6:12);
                    Vec3 radial=lado.scale(Math.cos(a)).add(alto.scale(Math.sin(a)));
                    Vec3 anterior=foco;
                    for(int i=1;i<=12;i++) {
                        double d=i*22.0/12;
                        Vec3 q=origem.add(dir.scale(d)).add(radial.scale((.2+d*.12)*escala));
                        fita(pose,cam,anterior,q,.12,0,d*.2,VERMELHO,alpha*.65f);
                        anterior=q;
                    }
                }
                for(int j=0;j<3;j++) anel(pose,cam,foco.add(dir.scale(1+j*6)),dir,(1+j*1.3)*escala,.16,VERMELHO,alpha*.8f,t+j);
            });
        }
    }
    private static void roxo(PoseStack pose,Vec3 cam,Vec3 origem,Vec3 dir,double t,float alpha) {
        Vec3 centro=origem.add(dir.scale(PerfilVfxGojo.ROXO_ORIGEM)),lado=direita(dir);
        if(t<=PerfilVfxGojo.ROXO_DISPARO) {
            double d=PerfilVfxGojo.separacaoRoxo(t);
            Vec3 blue=centro.subtract(lado.scale(d)),red=centro.add(lado.scale(d));
            esfera(pose,cam,blue,.6,0,AZUL,alpha,false);
            esfera(pose,cam,red,.48,1,VERMELHO,alpha,false);
            esfera(pose,cam,blue,1,6,AZUL,alpha*.4f,true);
            esfera(pose,cam,red,.85,6,VERMELHO,alpha*.4f,true);
            fitas(()->{
                for(int j=0;j<4;j++) {
                    Vec3 anterior=blue;
                    for(int i=1;i<=20;i++) {
                        double s=i/20.0;
                        Vec3 q=blue.lerp(red,s).add(0,Math.sin(s*Math.PI)*Math.sin(t*.12+j)*.6,0);
                        fita(pose,cam,anterior,q,.06,s*3,(s+.05)*3,j%2==0?AZUL:VERMELHO,alpha*.7f);anterior=q;
                    }
                }
            });
            if(t>21) esfera(pose,cam,centro,(t-21)/9*1.6,2,ROXO,alpha*(float)PerfilVfxGojo.suave((t-21)/9),false);
        } else {
            Vec3 pos=origem.add(dir.scale(PerfilVfxGojo.distanciaRoxo(t)));
            esfera(pose,cam,pos,3.6,2,ROXO,alpha,false);
            esfera(pose,cam,pos,4.5,6,ROXO,alpha*.5f,true);
            fitas(()->{
                int n=reduzido()?4:8;
                Vec3 alto=lado.cross(dir).normalize();
                for(int j=0;j<n;j++) {
                    Vec3 anterior=null;
                    for(int i=0;i<=24;i++) {
                        double s=i/24.0,a=j*Math.PI*2/n+s*9-t*.12;
                        Vec3 q=pos.subtract(dir.scale(s*Math.min(12,(t-30)*1.6)))
                                .add(lado.scale(Math.cos(a)*(3.7+s))).add(alto.scale(Math.sin(a)*(3.7+s)));
                        if(anterior!=null) fita(pose,cam,anterior,q,.14*(1-s*.7),s*5,(s+1.0/24)*5,ROXO,alpha*(float)(1-s));
                        anterior=q;
                    }
                }
            });
        }
    }
    private static void dominio(PoseStack pose,Vec3 cam,Vec3 origem,double t,float alpha) {
        double raio=PerfilVfxGojo.raioDominio(t);
        if(raio>.1 && cam.distanceTo(origem)>raio-.5) esfera(pose,cam,origem,raio,7,BRANCO,alpha,false);
        double presenca=PerfilVfxGojo.presencaDominio(t,cam.distanceTo(origem));
        if(ConfigCliente.AMBIENTE_DOMINIO.get() && presenca>.01)
            pisoAstral(pose,cam,origem,raio,(float)(presenca*.95));
        if(t<20) return;
        Vec3 horizonte=origem.add(0,8,0);
        esfera(pose,cam,horizonte,3.3,7,BRANCO,alpha,false);
        esfera(pose,cam,horizonte,3.7,6,BRANCO,alpha*.55f,true);
        fitas(()->{
            for(int j=0;j<4;j++) anel(pose,cam,horizonte,new Vec3(.15,1,.2),4+j*1.1,.09+j*.03,BRANCO,alpha*(.45f-j*.06f),t*.2+j);
            int n=reduzido()?18:40;
            for(int j=0;j<n;j++) {
                double a=j*2.399963+t*.002,r=6+(j%7)*2.1;
                Vec3 ponto=origem.add(Math.cos(a)*r,.1,Math.sin(a)*r);
                Vec3 q=ponto.add(Math.sin(j*1.9)*.8,2+(j%5)*1.8,Math.cos(j)*.8);
                fita(pose,cam,ponto,q,.018,0,4,BRANCO,alpha*.22f);
            }
        });
    }
    public static boolean infinito(PoseStack pose,Player p,Vec3 cam,float parcial) {
        if(!ativo()) return false;
        Vec3 centro=p.getPosition(parcial).add(0,1,0);
        preparar(p.tickCount+parcial);
        try {
            // Sem aneis girando continuamente: a barreira aparece sobretudo no contato.
            esfera(pose,cam,centro,1.4,3,BRANCO,.04f,true);
            var level=p.level();
            var contatos=level.getEntities(p,p.getBoundingBox().inflate(3.5),e->e instanceof net.minecraft.world.entity.projectile.Projectile || e instanceof net.minecraft.world.entity.LivingEntity);
            fitas(()->{
                int n=0;
                for(var entidade:contatos) {
                    if(n++>=8) break;
                    Vec3 normal=entidade.position().add(0,entidade.getBbHeight()*.5,0).subtract(centro).normalize();
                    if(normal.lengthSqr()<1e-8) continue;
                    Vec3 ponto=centro.add(normal.scale(1.5));
                    for(int j=0;j<3;j++) anel(pose,cam,ponto,normal,.2+j*.12,.025,BRANCO,.45f,p.tickCount*.04+j);
                }
            });
        } finally {restaurar();}
        return true;
    }
    public static void ceu(PoseStack pose,Vec3 camera,double tick,float alpha) {
        if(!ativo()) return;
        preparar(tick);
        try {esfera(pose,camera,camera,100,4,BRANCO,alpha,false);} finally {restaurar();}
    }
    private static void pisoAstral(PoseStack pose,Vec3 cam,Vec3 centro,double raio,float alpha) {
        selecionar(false);shader.safeGetUniform("EffectMode").set(4f);
        var b=Tesselator.getInstance().getBuilder();var m=pose.last().pose();
        b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);
        int lados=reduzido()?32:64;
        for(int j=0;j<8;j++) for(int i=0;i<lados;i++) {
            pisoPonto(b,m,cam,centro,raio,j/8.0,i*Math.PI*2/lados,alpha);
            pisoPonto(b,m,cam,centro,raio,(j+1)/8.0,i*Math.PI*2/lados,alpha);
            pisoPonto(b,m,cam,centro,raio,(j+1)/8.0,(i+1)*Math.PI*2/lados,alpha);
            pisoPonto(b,m,cam,centro,raio,j/8.0,(i+1)*Math.PI*2/lados,alpha);
        }
        Tesselator.getInstance().end();
    }
    private static void pisoPonto(BufferBuilder b,Matrix4f m,Vec3 cam,Vec3 centro,double raio,double r,double a,float alpha) {
        double x=Math.cos(a)*r,z=Math.sin(a)*r;
        float fade=(float)(1-PerfilVfxGojo.suave((r-.7)/.3));
        v(b,m,centro.add(x*raio,.035,z*raio).subtract(cam),x*.5+.5,z*.5+.5,BRANCO,alpha*fade);
    }
    private static boolean reduzido(){return ConfigCliente.VFX_REDUZIDOS.get();}
    private static void preparar(double tick) {
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();
        RenderSystem.enableDepthTest();RenderSystem.depthMask(false);
        tempoMaterial=(float)(tick/20);selecionar(false);
    }
    private static void selecionar(boolean aditivo) {
        shader=aditivo?brilho:base;
        RenderSystem.setShader(()->shader);
        shader.safeGetUniform("EffectTime").set(tempoMaterial);
    }
    private static void restaurar() {
        RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }
    private static void esfera(PoseStack pose,Vec3 cam,Vec3 centro,double raio,int modo,int cor,float alpha,boolean aditivo) {
        selecionar(aditivo);
        if(aditivo) RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);
        else RenderSystem.defaultBlendFunc();
        shader.safeGetUniform("EffectMode").set((float)modo);
        Vec3 view=cam.subtract(centro).normalize();if(view.lengthSqr()<1e-8) view=new Vec3(0,0,1);
        shader.safeGetUniform("Viewer").set((float)view.x,(float)view.y,(float)view.z);
        var b=Tesselator.getInstance().getBuilder();var m=pose.last().pose();
        int n=reduzido()?12:22;
        b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);
        Vec3 base=centro.subtract(cam);
        for(int i=0;i<n;i++) for(int j=0;j<n*2;j++) {
            pontoEsfera(b,m,base,raio,i,j,n,cor,alpha);pontoEsfera(b,m,base,raio,i+1,j,n,cor,alpha);
            pontoEsfera(b,m,base,raio,i+1,j+1,n,cor,alpha);pontoEsfera(b,m,base,raio,i,j+1,n,cor,alpha);
        }
        Tesselator.getInstance().end();
    }
    private static void pontoEsfera(BufferBuilder b,Matrix4f m,Vec3 base,double r,int i,int j,int n,int cor,float alpha) {
        double a=Math.PI*j/n,lat=-Math.PI/2+Math.PI*i/n;
        v(b,m,base.add(Math.cos(a)*Math.cos(lat)*r,Math.sin(lat)*r,Math.sin(a)*Math.cos(lat)*r),j/(double)(n*2),i/(double)n,cor,alpha);
    }
    private static void fitas(Runnable geometria) {
        selecionar(true);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);
        shader.safeGetUniform("EffectMode").set(5f);
        var b=Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);
        geometria.run();Tesselator.getInstance().end();
    }
    private static void fita(PoseStack pose,Vec3 cam,Vec3 a,Vec3 c,double largura,double u,double w,int cor,float alpha) {
        Vec3 lado=c.subtract(a).cross(cam.subtract(a)).normalize().scale(largura);
        if(lado.lengthSqr()<1e-10) return;
        var b=Tesselator.getInstance().getBuilder();var m=pose.last().pose();
        v(b,m,a.subtract(lado).subtract(cam),u,0,cor,alpha);v(b,m,c.subtract(lado).subtract(cam),w,0,cor,alpha);
        v(b,m,c.add(lado).subtract(cam),w,1,cor,alpha);v(b,m,a.add(lado).subtract(cam),u,1,cor,alpha);
    }
    private static void anel(PoseStack pose,Vec3 cam,Vec3 centro,Vec3 normal,double raio,double largura,int cor,float alpha,double fase) {
        Vec3 lado=direita(normal),alto=normal.cross(lado).normalize();
        int n=reduzido()?32:56;
        for(int i=0;i<n;i++) {
            double a=i*Math.PI*2/n,b=(i+1)*Math.PI*2/n;
            Vec3 p=centro.add(lado.scale(Math.cos(a)*raio)).add(alto.scale(Math.sin(a)*raio));
            Vec3 q=centro.add(lado.scale(Math.cos(b)*raio)).add(alto.scale(Math.sin(b)*raio));
            fita(pose,cam,p,q,largura,a*2+fase,b*2+fase,cor,alpha);
        }
    }
    private static void v(BufferBuilder b,Matrix4f m,Vec3 p,double u,double w,int cor,float alpha) {
        b.vertex(m,(float)p.x,(float)p.y,(float)p.z).color((cor>>16&255)/255f,(cor>>8&255)/255f,(cor&255)/255f,alpha).uv((float)u,(float)w).endVertex();
    }
}
