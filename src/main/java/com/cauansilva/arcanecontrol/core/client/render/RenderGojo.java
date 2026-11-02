package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo;
import com.cauansilva.arcanecontrol.core.vfx.AuraDespertarGojo;
import com.cauansilva.arcanecontrol.core.vfx.ChuvaInformacoesGojo;
import com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo;
import com.cauansilva.arcanecontrol.core.vfx.RoxoMaximoGojo;
import com.cauansilva.arcanecontrol.core.vfx.TeleporteGojo;
import com.cauansilva.arcanecontrol.core.vfx.MorteCerebralGojo;
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
        double r=com.cauansilva.arcanecontrol.core.vfx.FluxoAzulGojo.raio(t);
        esfera(pose,cam,centro,r,10,AZUL,alpha,false);
        esfera(pose,cam,centro,r*1.65,6,AZUL,alpha*.3f,true);
        fitas(11,()->{
            int n=reduzido()?6:12,passos=reduzido()?16:32;
            for(int j=0;j<n;j++) {
                Vec3 anterior=null;
                for(int i=0;i<=passos;i++) {
                    double u=i/(double)passos;
                    var p=com.cauansilva.arcanecontrol.core.vfx.FluxoAzulGojo.fluxo(j,u,t);
                    Vec3 q=centro.add(p.x(),p.y(),p.z());
                    float luz=alpha*(float)com.cauansilva.arcanecontrol.core.vfx.FluxoAzulGojo.pulso(j,u,t);
                    if(anterior!=null){
                        fita(pose,cam,anterior,q,.19+.16*u,(i-1d)/passos,u,AZUL,luz*.22f);
                        fita(pose,cam,anterior,q,.08+.1*u,(i-1d)/passos,u,AZUL,luz);
                    }
                    anterior=q;
                }
            }
            for(int j=0;j<(reduzido()?2:3);j++){
                Vec3 anterior=null;int passosArco=reduzido()?24:48;
                for(int i=0;i<=passosArco;i++){
                    double u=i/(double)passosArco;
                    var p=com.cauansilva.arcanecontrol.core.vfx.FluxoAzulGojo.arco(j,u,t);
                    Vec3 q=centro.add(p.x(),p.y(),p.z());
                    if(anterior!=null)fita(pose,cam,anterior,q,.1,(i-1d)/passosArco,u,0x75DFFF,alpha*.85f*(float)Math.sin(u*Math.PI));
                    anterior=q;
                }
            }
        });
    }
    private static void vermelho(PoseStack pose,Vec3 cam,Vec3 origem,Vec3 dir,double t,float alpha) {
        Vec3 foco=origem.add(dir.scale(1.8)),lado=direita(dir),alto=lado.cross(dir).normalize();
        double r=com.cauansilva.arcanecontrol.core.vfx.DescargaVermelhoGojo.raio(t);
        esfera(pose,cam,foco,r,12,VERMELHO,alpha,false);
        esfera(pose,cam,foco,r*2,6,VERMELHO,alpha*.5f,true);
        fitas(13,()->{
            if(t<PerfilVfxGojo.VERMELHO_DISPARO){
                int passos=reduzido()?16:32;
                for(int j=0;j<(reduzido()?3:6);j++){
                    Vec3 anterior=null;
                    for(int i=0;i<=passos;i++){
                        double u=i/(double)passos;var p=com.cauansilva.arcanecontrol.core.vfx.DescargaVermelhoGojo.carga(j,u,t);
                        Vec3 q=foco.add(lado.scale(p.lateral())).add(alto.scale(p.vertical())).add(dir.scale(p.frente()));
                        if(anterior!=null)fita(pose,cam,anterior,q,.035,(i-1d)/passos,u,VERMELHO,alpha*.8f*(float)Math.sin(u*Math.PI));
                        anterior=q;
                    }
                }
            }else{
                // Full cone is visible at impact: the server has no travelling Red projectile.
                int total=reduzido()?6:12,passos=reduzido()?12:24;
                float impacto=(float)com.cauansilva.arcanecontrol.core.vfx.DescargaVermelhoGojo.impacto(t);
                for(int j=0;j<total;j++){
                    Vec3 anterior=foco;
                    for(int i=1;i<=passos;i++){
                        double u=i/(double)passos;var p=com.cauansilva.arcanecontrol.core.vfx.DescargaVermelhoGojo.descarga(j,total,u,t);
                        Vec3 q=foco.add(lado.scale(p.lateral())).add(alto.scale(p.vertical())).add(dir.scale(p.frente()));
                        float luz=alpha*(.3f+.7f*impacto)*(float)(1-u*.5);
                        fita(pose,cam,anterior,q,.28,0,u,VERMELHO,luz*.22f);
                        fita(pose,cam,anterior,q,.085+impacto*.065,0,u,0xFF5161,luz);
                        anterior=q;
                    }
                }
                int passosOnda=reduzido()?32:64;
                for(int j=0;j<3;j++){
                    Vec3 anterior=null;
                    for(int i=0;i<=passosOnda;i++){
                        double u=i/(double)passosOnda;var p=com.cauansilva.arcanecontrol.core.vfx.DescargaVermelhoGojo.onda(j,u,t);
                        Vec3 q=foco.add(lado.scale(p.lateral())).add(alto.scale(p.vertical())).add(dir.scale(p.frente()));
                        if(anterior!=null){
                            fita(pose,cam,anterior,q,1.+impacto*.35,(i-1d)/passosOnda,u,VERMELHO,alpha*.18f);
                            fita(pose,cam,anterior,q,.16+impacto*.2,(i-1d)/passosOnda,u,0xFF7981,alpha*(.4f+.6f*impacto));
                        }
                        anterior=q;
                    }
                }
            }
        });
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
                        var ponto=com.cauansilva.arcanecontrol.core.vfx.CoroaRoxoGojo.ponte(j,s,t);
                        Vec3 q=centro.add(lado.scale(ponto.lateral())).add(0,ponto.vertical(),0);
                        fita(pose,cam,anterior,q,.06,s*3,(s+.05)*3,j%2==0?AZUL:VERMELHO,alpha*.7f);anterior=q;
                    }
                }
            });
            if(t>21){
                double formar=com.cauansilva.arcanecontrol.core.vfx.CoroaRoxoGojo.formacao(t);
                esfera(pose,cam,centro,formar*1.6,2,ROXO,alpha*(float)formar,false);
                coroaRoxo(pose,cam,centro,dir,t,alpha*(float)formar,formar*1.6);
            }
        } else {
            Vec3 pos=origem.add(dir.scale(PerfilVfxGojo.distanciaRoxo(t)));
            esfera(pose,cam,pos,3.6,2,ROXO,alpha,false);
            esfera(pose,cam,pos,4.5,6,ROXO,alpha*.5f,true);
            coroaRoxo(pose,cam,pos,dir,t,alpha,3.6);
            fitas(()->{
                int n=reduzido()?4:8;
                Vec3 alto=lado.cross(dir).normalize();
                for(int j=0;j<n;j++) {
                    Vec3 anterior=null;
                    for(int i=0;i<=24;i++) {
                        double s=i/24.0;var ponto=com.cauansilva.arcanecontrol.core.vfx.CoroaRoxoGojo.rastro(j,n,s,t);
                        Vec3 q=pos.add(dir.scale(ponto.frente())).add(lado.scale(ponto.lateral())).add(alto.scale(ponto.vertical()));
                        if(anterior!=null) fita(pose,cam,anterior,q,.14*(1-s*.7),s*5,(s+1.0/24)*5,ROXO,alpha*(float)(1-s));
                        anterior=q;
                    }
                }
            });
        }
        if(t>12) raiosRoxo(pose,cam,t<=30?centro:origem.add(dir.scale(PerfilVfxGojo.distanciaRoxo(t))),dir,t,alpha);
    }
    private static void coroaRoxo(PoseStack pose,Vec3 cam,Vec3 centro,Vec3 dir,double t,float alpha,double raio){
        if(raio<.05)return;Vec3 lado=direita(dir),alto=lado.cross(dir).normalize();
        fitas(9,()->{
            int faixas=reduzido()?2:3,passos=reduzido()?24:48;
            for(int j=0;j<faixas;j++){
                Vec3 anterior=null;
                for(int i=0;i<=passos;i++){
                    double u=i/(double)passos;var p=com.cauansilva.arcanecontrol.core.vfx.CoroaRoxoGojo.ponto(j,u,t,raio);
                    Vec3 q=centro.add(lado.scale(p.lateral())).add(alto.scale(p.vertical())).add(dir.scale(p.frente()));
                    if(anterior!=null)fita(pose,cam,anterior,q,raio*.055,(i-1d)/passos,u,j==1?0xF1CCFF:ROXO,alpha*.75f);anterior=q;
                }
            }
        });
    }
    public static void roxoMaximo(PoseStack pose,Vec3 cam,Vec3 centro,double raio,double t,float alpha){
        preparar(t);
        try{
            esfera(pose,cam,centro,raio,t>=90?23:2,ROXO,alpha,false);
            esfera(pose,cam,centro,raio*1.18,6,ROXO,alpha*.4f,true);
            coroaRoxo(pose,cam,centro,new Vec3(0,0,1),t,alpha,raio);
            fitas(8,()->geometriaRaios(pose,cam,centro,new Vec3(0,0,1),t,alpha,raio/3.6));
            if(t>=RoxoMaximoGojo.COLAPSO){
                esfera(pose,cam,centro,RoxoMaximoGojo.raioExplosao(t),22,ROXO,alpha*(float)RoxoMaximoGojo.alphaExplosao(t),true);
                fitas(9,()->{
                    int passos=reduzido()?32:64;
                    float intensidade=alpha*(float)RoxoMaximoGojo.alphaChoque(t);
                    for(int plano=0;plano<3;plano++){
                        Vec3[] pontos=new Vec3[passos+1];float[] luz=new float[passos+1];
                        for(int i=0;i<=passos;i++){
                            var p=RoxoMaximoGojo.anel(plano,i/(double)passos,t);pontos[i]=centro.add(p.x(),p.y(),p.z());luz[i]=intensidade;
                        }
                        faixaSuave(pose,cam,pontos,.7,0xA52AFF,luz,.55f);
                        faixaSuave(pose,cam,pontos,.24,0xF4D4FF,luz,1);
                    }
                });
            }
        }finally{restaurar();}
    }
    private static void raiosRoxo(PoseStack pose,Vec3 cam,Vec3 centro,Vec3 dir,double t,float alpha){
        fitas(8,()->geometriaRaios(pose,cam,centro,dir,t,alpha));
    }
    public static void raiosSemMaterial(PoseStack pose,Vec3 cam,Vec3 centro,Vec3 dir,double t,float alpha){
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);
        geometriaRaios(pose,cam,centro,dir,t,alpha);Tesselator.getInstance().end();restaurar();
    }
    private static void geometriaRaios(PoseStack pose,Vec3 cam,Vec3 centro,Vec3 dir,double t,float alpha){geometriaRaios(pose,cam,centro,dir,t,alpha,1);}
    private static void geometriaRaios(PoseStack pose,Vec3 cam,Vec3 centro,Vec3 dir,double t,float alpha,double escala){
        Vec3 lado=direita(dir),alto=lado.cross(dir).normalize();
            for(int j=0;j<(reduzido()?4:8);j++){
                Vec3 anterior=null;float pulso=(float)com.cauansilva.arcanecontrol.core.vfx.RaiosRoxo.intensidade(j,t)*alpha;
                for(int i=0;i<=8;i++){
                    var c=com.cauansilva.arcanecontrol.core.vfx.RaiosRoxo.ponto(j,i,t);
                    Vec3 q=centro.add(lado.scale(c.lateral()*escala)).add(alto.scale(c.vertical()*escala)).add(dir.scale(c.frente()*escala));
                    if(anterior!=null){
                        fita(pose,cam,anterior,q,.24,0,1,ROXO,pulso*.55f);
                        fita(pose,cam,anterior,q,.075,0,1,0xF2DEFF,pulso);
                    }
                    if(i==3||i==5){var r=com.cauansilva.arcanecontrol.core.vfx.RaiosRoxo.ramo(j,i,t);Vec3 ramo=centro.add(lado.scale(r.lateral()*escala)).add(alto.scale(r.vertical()*escala)).add(dir.scale(r.frente()*escala));fita(pose,cam,q,ramo,.065,0,1,0xEAD2FF,pulso);}
                    anterior=q;
                }
            }
    }
    private static void dominio(PoseStack pose,Vec3 cam,Vec3 origem,double t,float alpha) {
        double raio=PerfilVfxGojo.raioDominio(t);
        boolean interior=com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo.interior(cam.distanceTo(origem),raio);
        if(raio>.1)esfera(pose,cam,origem,raio,interior?14:15,BRANCO,alpha,false);
        // Exterior spectators must not see internal objects drawn over the opaque shell.
        if(!interior)return;
        double presenca=PerfilVfxGojo.presencaDominio(t,cam.distanceTo(origem));
        if(ConfigCliente.AMBIENTE_DOMINIO.get()&&presenca>.01)pisoAstral(pose,cam,origem,raio,(float)(presenca*.95));
        if(t<20||presenca<=.01)return;
        float intensidade=alpha*(float)presenca;
        fitas(17,()->{
            int passos=reduzido()?com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo.PASSOS_REDUZIDOS:com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo.PASSOS;
            for(int j=0;j<(reduzido()?com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo.FAIXAS_REDUZIDAS:com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo.FAIXAS);j++){
                Vec3 anterior=null;
                for(int i=0;i<=passos;i++){
                    double u=i/(double)passos;var p=com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo.informacao(j,u,t);
                    Vec3 q=origem.add(p.x(),p.y(),p.z());
                    float luz=intensidade*(float)com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo.pulso(j,u,t);
                    if(anterior!=null){
                        int cor=com.cauansilva.arcanecontrol.core.vfx.VazioInfinitoGojo.cor(j,u,t);
                        fita(pose,cam,anterior,q,.28,(i-1d)/passos,u,cor,luz*.38f);
                        fita(pose,cam,anterior,q,.1,(i-1d)/passos,u,cor,luz);
                    }
                    anterior=q;
                }
            }
        });
    }
    public static void morteCerebral(PoseStack pose,Vec3 cam,Vec3 alvo,double tick){
        if(!ativo())return;
        float alpha=(float)MorteCerebralGojo.alpha(tick);if(alpha<.004f)return;
        preparar(tick);
        try{fitas(25,()->{
            int passos=reduzido()?6:12;
            for(int j=0;j<(reduzido()?MorteCerebralGojo.FAIXAS_REDUZIDAS:MorteCerebralGojo.FAIXAS);j++){
                Vec3[] pontos=new Vec3[passos+1];float[] luz=new float[passos+1];
                for(int i=0;i<=passos;i++){
                    double u=i/(double)passos;var p=MorteCerebralGojo.linha(j,u,tick);
                    pontos[i]=alvo.add(p.x(),p.y(),p.z());luz[i]=alpha*(float)(1-u*.7);
                }
                int cor=VazioInfinitoGojo.cor(j,.5,tick);
                faixaSuave(pose,cam,pontos,.13,cor,luz,.4f);faixaSuave(pose,cam,pontos,.045,cor,luz,1);
            }
        });}finally{restaurar();}
    }
    public static void teleporte(PoseStack pose,Vec3 cam,Vec3 origem,Vec3 destino,double tick){
        if(!ativo())return;
        Vec3 delta=destino.subtract(origem),frente=new Vec3(delta.x,0,delta.z).normalize();
        if(frente.lengthSqr()<1e-8)frente=new Vec3(0,0,1);
        final Vec3 eixo=frente,lado=direita(eixo);preparar(tick);
        try{fitas(24,()->{
            int passos=reduzido()?16:32;
            for(int ponta=0;ponta<2;ponta++){
                boolean chegada=ponta==1;Vec3 centro=chegada?destino:origem;
                float alpha=(float)(chegada?TeleporteGojo.chegada(tick):TeleporteGojo.saida(tick));
                if(alpha<.004f)continue;
                for(int j=0;j<(reduzido()?2:3);j++){
                    Vec3[] pontos=new Vec3[passos+1];float[] luz=new float[passos+1];
                    for(int i=0;i<=passos;i++){
                        double u=i/(double)passos;var p=TeleporteGojo.arco(j,u,tick,chegada);
                        pontos[i]=centro.add(lado.scale(p.x())).add(0,p.y(),0).add(eixo.scale(p.z()));luz[i]=alpha*(float)Math.sin(u*Math.PI);
                    }
                    faixaSuave(pose,cam,pontos,.19,0x279DFF,luz,.45f);faixaSuave(pose,cam,pontos,.055,0xA5EDFF,luz,1);
                }
                for(int i=0;i<(reduzido()?8:16);i++){
                    var p=TeleporteGojo.fragmento(i,tick,chegada);Vec3 q=centro.add(lado.scale(p.x())).add(0,p.y(),0).add(eixo.scale(p.z()));
                    fita(pose,cam,q,q.add(0,.12,0),.04,0,1,0xB7F1FF,alpha*.8f);
                }
            }
            float rastro=(float)TeleporteGojo.rastro(tick);
            if(rastro>.004f)for(int j=0;j<(reduzido()?2:4);j++){
                Vec3[] pontos=new Vec3[passos+1];float[] luz=new float[passos+1];
                for(int i=0;i<=passos;i++){
                    double u=i/(double)passos;var p=TeleporteGojo.trilha(j,u,tick,delta.x,delta.y,delta.z);pontos[i]=origem.add(p.x(),p.y(),p.z());luz[i]=rastro;
                }
                faixaSuave(pose,cam,pontos,.14,0x299BFF,luz,.35f);faixaSuave(pose,cam,pontos,.035,0xB9F5FF,luz,1);
            }
        });}finally{restaurar();}
    }
    public static void chuvaInformacoes(PoseStack pose,Vec3 cam,Vec3 centro,double tick){
        if(!ativo())return;
        float alpha=(float)ChuvaInformacoesGojo.alpha(tick);if(alpha<.004f)return;
        preparar(tick);
        try{fitas(21,()->{
            int passos=reduzido()?ChuvaInformacoesGojo.PASSOS_REDUZIDOS:ChuvaInformacoesGojo.PASSOS;
            int total=reduzido()?ChuvaInformacoesGojo.FAIXAS_REDUZIDAS:ChuvaInformacoesGojo.FAIXAS;
            for(int j=0;j<total;j++){
                float presenca=(float)ChuvaInformacoesGojo.presenca(j,tick);
                Vec3[] pontos=new Vec3[passos+1];float[] luz=new float[passos+1];
                for(int i=0;i<=passos;i++){
                    double u=i/(double)passos;var p=ChuvaInformacoesGojo.coluna(j,u,tick);
                    pontos[i]=centro.add(p.x(),p.y(),p.z());luz[i]=alpha*presenca*(float)ChuvaInformacoesGojo.luz(u);
                }
                int cor=VazioInfinitoGojo.cor(j,.5,tick);
                faixaSuave(pose,cam,pontos,.22,cor,luz,.3f);
                faixaSuave(pose,cam,pontos,.065,cor,luz,1);
                int marcas=reduzido()?2:4;
                for(int m=0;m<marcas;m++){
                    double u=(m+.5)/marcas;var p=ChuvaInformacoesGojo.coluna(j,u,tick);
                    Vec3 q=centro.add(p.x(),p.y(),p.z()),lado=direita(cam.subtract(q));
                    float brilho=alpha*presenca*(float)ChuvaInformacoesGojo.luz(u)*.85f;
                    fita(pose,cam,q.subtract(lado.scale(.16)),q.add(lado.scale(.16)),.025,0,1,cor,brilho);
                    fita(pose,cam,q.add(0,-.1,0),q.add(0,.1,0),.025,0,1,cor,brilho);
                }
            }
        });}finally{restaurar();}
    }
    public static void despertar(PoseStack pose,Vec3 cam,Vec3 corpo,Vec3 direcao,double idade,float fade){
        if(!ativo())return;
        Vec3 frente=new Vec3(direcao.x,0,direcao.z).normalize();
        if(frente.lengthSqr()<1e-8)frente=new Vec3(0,0,1);
        final Vec3 eixo=frente,lado=direita(eixo);
        float intensidade=(float)AuraDespertarGojo.intensidade(idade)*fade;
        if(intensidade<.004f)return;
        preparar(idade);
        try{
            fitas(20,()->{
                int passos=reduzido()?12:24;
                for(int j=0;j<(reduzido()?3:6);j++){
                    Vec3[] pontos=new Vec3[passos+1];float[] luz=new float[passos+1];
                    for(int i=0;i<=passos;i++){
                        double u=i/(double)passos;var p=AuraDespertarGojo.fio(j*2,u,idade);
                        pontos[i]=corpo.add(lado.scale(p.lateral())).add(0,p.altura(),0).add(eixo.scale(p.frente()));
                        luz[i]=intensidade*.33f*(float)Math.sin(u*Math.PI);
                    }
                    faixaSuave(pose,cam,pontos,.36,0x178BFF,luz,1);
                }
            });
            fitas(19,()->{
            int passos=reduzido()?12:24;
            for(int j=0;j<(reduzido()?6:12);j++){
                Vec3[] pontos=new Vec3[passos+1];float[] luz=new float[passos+1];
                for(int i=0;i<=passos;i++){
                    double u=i/(double)passos;var p=AuraDespertarGojo.fio(j,u,idade);
                    pontos[i]=corpo.add(lado.scale(p.lateral())).add(0,p.altura(),0).add(eixo.scale(p.frente()));
                    luz[i]=intensidade*(float)AuraDespertarGojo.pulso(j,u,idade);
                }
                faixaSuave(pose,cam,pontos,.23,0x249BFF,luz,.4f);
                faixaSuave(pose,cam,pontos,.065,j%3==0?0xE1F7FF:0x55DFFF,luz,1);
            }
            for(int j=0;j<(reduzido()?12:24);j++){
                var p=AuraDespertarGojo.particula(j,idade);
                Vec3 q=corpo.add(lado.scale(p.lateral())).add(0,p.altura(),0).add(eixo.scale(p.frente()));
                fita(pose,cam,q,q.add(0,.11,0),.045,0,1,0xC4EFFF,intensidade*(float)AuraDespertarGojo.brilhoParticula(j,idade));
            }
            double r=AuraDespertarGojo.raioOnda(idade);
            if(r>0){
                float luz=fade*(float)AuraDespertarGojo.alphaOnda(idade);
                anel(pose,cam,corpo.add(0,.07,0),CIMA,r,.2,0x279DFF,luz*.4f,0);
                anel(pose,cam,corpo.add(0,.07,0),CIMA,r,.055,0xC2F6FF,luz,0);
            }
        });}finally{restaurar();}
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
        try {esfera(pose,camera,camera,100,14,BRANCO,alpha,false);} finally {restaurar();}
    }
    private static void pisoAstral(PoseStack pose,Vec3 cam,Vec3 centro,double raio,float alpha) {
        selecionar(false);shader.safeGetUniform("EffectMode").set(18f);
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
        int n=reduzido()?12:(modo>=14&&modo<=16?32:22);
        boolean nucleoOpaco=(modo==2||modo==10||modo==12||modo==15||modo==23)&&!aditivo&&alpha>.95f;
        if(nucleoOpaco){RenderSystem.depthMask(true);if(cam.distanceTo(centro)>raio+.01)RenderSystem.enableCull();}
        b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR_TEX);
        Vec3 base=centro.subtract(cam);
        for(int i=0;i<n;i++) for(int j=0;j<n*2;j++) {
            pontoEsfera(b,m,base,raio,i,j,n,cor,alpha);pontoEsfera(b,m,base,raio,i+1,j,n,cor,alpha);
            pontoEsfera(b,m,base,raio,i+1,j+1,n,cor,alpha);pontoEsfera(b,m,base,raio,i,j+1,n,cor,alpha);
        }
        Tesselator.getInstance().end();
        if(nucleoOpaco){RenderSystem.depthMask(false);RenderSystem.disableCull();}
    }
    private static void pontoEsfera(BufferBuilder b,Matrix4f m,Vec3 base,double r,int i,int j,int n,int cor,float alpha) {
        double a=Math.PI*j/n,lat=-Math.PI/2+Math.PI*i/n;
        v(b,m,base.add(Math.cos(a)*Math.cos(lat)*r,Math.sin(lat)*r,Math.sin(a)*Math.cos(lat)*r),j/(double)(n*2),i/(double)n,cor,alpha);
    }
    private static void fitas(Runnable geometria) {fitas(5,geometria);}
    private static void fitas(int modo,Runnable geometria) {
        selecionar(true);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);
        shader.safeGetUniform("EffectMode").set((float)modo);
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
    /** Shared endpoint tangents keep adjacent aura quads connected. */
    private static void faixaSuave(PoseStack pose,Vec3 cam,Vec3[] pontos,double largura,int cor,float[] luz,float ganho){
        var b=Tesselator.getInstance().getBuilder();var m=pose.last().pose();
        Vec3[] lados=new Vec3[pontos.length];
        for(int i=0;i<pontos.length;i++){
            Vec3 tangente=pontos[Math.min(pontos.length-1,i+1)].subtract(pontos[Math.max(0,i-1)]);
            Vec3 lado=tangente.cross(cam.subtract(pontos[i])).normalize();
            if(lado.lengthSqr()<1e-10)lado=i>0?lados[i-1].normalize():new Vec3(1,0,0);
            lados[i]=lado.scale(largura);
        }
        for(int i=1;i<pontos.length;i++){
            double u=(i-1d)/(pontos.length-1),w=i/(double)(pontos.length-1);
            v(b,m,pontos[i-1].subtract(lados[i-1]).subtract(cam),u,0,cor,luz[i-1]*ganho);
            v(b,m,pontos[i].subtract(lados[i]).subtract(cam),w,0,cor,luz[i]*ganho);
            v(b,m,pontos[i].add(lados[i]).subtract(cam),w,1,cor,luz[i]*ganho);
            v(b,m,pontos[i-1].add(lados[i-1]).subtract(cam),u,1,cor,luz[i-1]*ganho);
        }
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
