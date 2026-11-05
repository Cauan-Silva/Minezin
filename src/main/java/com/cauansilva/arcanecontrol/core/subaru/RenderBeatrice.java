package com.cauansilva.arcanecontrol.core.subaru;

import com.cauansilva.arcanecontrol.core.vfx.ContratoSubaru;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.*;

/**
 * Beatrice no jogo durante o Contrato (G do Subaru): o modelo aprovado (geo/subaru/beatrice.geo.json, textura 128x128)
 * desenhado no mundo sem entidade, com GeckoLib. Na entrada segue ContratoSubaru.beatrice(t) a partir da porta; depois
 * flutua em ContratoSubaru.LUGAR ao lado do ombro e acompanha o Subaru com suavizacao. O balanco das brocas, da saia,
 * dos bracos e da cabeca e o mesmo da previa (tools/preview_beatrice.py). A fisica de tecido da saia fica para depois.
 */
final class RenderBeatrice {
    private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
    private static final ResourceLocation GEO=id("geo/subaru/beatrice.geo.json"),TEXTURA=id("textures/entity/subaru/beatrice.png"),ANIM=id("animations/subaru/beatrice.animation.json");

    /** Animavel unico: a pose (graus, convencao do geo) e trocada antes de cada desenho. */
    static final class Beatrice implements GeoAnimatable{
        private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
        final Map<String,float[]> pose=new HashMap<>();double tick;
        @Override public void registerControllers(AnimatableManager.ControllerRegistrar r){}
        @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
        @Override public double getTick(Object o){return tick;}
    }
    private static final class Modelo extends GeoModel<Beatrice>{
        @Override public ResourceLocation getModelResource(Beatrice a){return GEO;}
        @Override public ResourceLocation getTextureResource(Beatrice a){return TEXTURA;}
        @Override public ResourceLocation getAnimationResource(Beatrice a){return ANIM;}
        @Override public void setCustomAnimations(Beatrice a,long instancia,AnimationState<Beatrice> estado){
            for(var e:a.pose.entrySet())getBone(e.getKey()).ifPresent(b->{var i=b.getInitialSnapshot();float[] r=e.getValue();
                b.setRotX(i.getRotX()-(float)Math.toRadians(r[0]));b.setRotY(i.getRotY()-(float)Math.toRadians(r[1]));b.setRotZ(i.getRotZ()+(float)Math.toRadians(r[2]));});
        }
    }
    private static GeoObjectRenderer<Beatrice> RENDER;private static Beatrice BEA;

    /** Balanco ocioso da previa (graus por osso). */
    static void poseOciosa(Map<String,float[]> r,double t){
        r.clear();
        for(int lado=0;lado<2;lado++){double f=lado==0?0:1.3;String n=lado==0?"e":"d";
            for(int k=0;k<8;k++)r.put("broca_"+n+"_"+k,new float[]{(float)(3*Math.sin(t*.18+f-k*.35)*(1+k*.1)),0,(float)((lado==0?2:-2)*Math.sin(t*.13+f-k*.3))});}
        for(int i=0;i<10;i++){double a=3+2*Math.sin(t*.16+i*.63);
            r.put("beatrice_saia_"+i+"_0",new float[]{(float)(-a*(i<3?1:5<=i&&i<8?-1:0)),0,(float)(a*(3<=i&&i<5?1:i>=8?-1:0))});}
        r.put("saia",new float[]{(float)(2.5*Math.sin(t*.16)),0,(float)(2*Math.sin(t*.11))});
        r.put("braco_e",new float[]{(float)(-8+4*Math.sin(t*.15)),0,-6});r.put("braco_d",new float[]{(float)(-8+4*Math.sin(t*.15+.5)),0,6});
        r.put("perna_e",new float[]{(float)(8+5*Math.sin(t*.14)),0,0});r.put("perna_d",new float[]{(float)(14+5*Math.sin(t*.14+1)),0,0});
        r.put("cabeca",new float[]{(float)(3*Math.sin(t*.09)),(float)(8*Math.sin(t*.05)),0});
    }

    static void desenhar(PoseStack pose,EfeitosSubaru.Contrato c,Player dono,double t,Vec3 cam,float parcial){
        var mc=Minecraft.getInstance();if(RENDER==null){RENDER=new GeoObjectRenderer<>(new Modelo());BEA=new Beatrice();}
        double escala,giro,inclina,yaw;Vec3 alvo;
        if(t<ContratoSubaru.SAI+6){
            // entrada: caminho da porta ao ombro no referencial do momento do G
            var b=RenderMagiasSubaru.baseContrato(c);var q=ContratoSubaru.beatrice(t);
            alvo=c.centro.add(b.em(q.x(),q.y(),q.z()));escala=ContratoSubaru.escalaBeatrice(t);giro=ContratoSubaru.giroBeatrice(t);inclina=ContratoSubaru.inclinaBeatrice(t);yaw=c.yaw;
            c.beatrice=alvo;
        }else{
            yaw=Mth.rotLerp(parcial,dono.yBodyRotO,dono.yBodyRot);double y=Math.toRadians(yaw);
            var b=RenderMagiasSubaru.Base.horizontal(new Vec3(-Math.sin(y),0,Math.cos(y)));var L=ContratoSubaru.LUGAR;
            alvo=dono.getPosition(parcial).add(b.em(L.x(),L.y()+.08*Math.sin(t*.2),L.z()));
            c.beatrice=c.beatrice==null?alvo:c.beatrice.lerp(alvo,.18);
            escala=1;giro=-12;inclina=Mth.clamp(c.beatrice.distanceTo(alvo)*8,0,14);
        }
        if(escala<.01)return;
        BEA.tick=t;poseOciosa(BEA.pose,t+60);
        Vec3 p=c.beatrice.subtract(cam);int luz=LevelRenderer.getLightColor(mc.level,BlockPos.containing(c.beatrice.add(0,.5,0)));
        pose.pushPose();
        pose.translate(p.x,p.y,p.z);
        pose.mulPose(Axis.YP.rotationDegrees((float)(180-yaw-giro)));pose.mulPose(Axis.XP.rotationDegrees((float)inclina));
        pose.scale((float)escala,(float)escala,(float)escala);
        pose.translate(-.5,-.51,-.5); // GeoObjectRenderer.preRender desloca (0,5; 0,51; 0,5)
        var buf=mc.renderBuffers().bufferSource();
        RENDER.render(pose,BEA,buf,null,null,luz);buf.endBatch();
        pose.popPose();
    }
    private RenderBeatrice(){}
}
