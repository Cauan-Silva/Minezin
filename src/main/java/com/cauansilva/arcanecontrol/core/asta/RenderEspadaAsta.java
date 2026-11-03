package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.vfx.AuraMatadora;
import com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico;
import com.cauansilva.arcanecontrol.core.vfx.PerfilDivider;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import java.util.UUID;

/** The blade inherits the actual held-item transform in first and third person. While its owner is in the Devil
 * Union the sword turns with the power: the dark crimson model (same grip and bones) wrapped in the animated
 * black-and-red flame aura ({@link AuraMatadora}). */
public final class RenderEspadaAsta extends GeoItemRenderer<EspadaAsta> {
    private static final ResourceLocation GLOW=new ResourceLocation("arcanecontrol","textures/armor/trajes/matadora_desperta_glowmask.png");
    /** Picks the normal or the awakened geometry/texture for the sword being drawn right now. */
    private static final class Modelo extends GeoModel<EspadaAsta>{
        boolean desperta;
        private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
        @Override public ResourceLocation getModelResource(EspadaAsta a){return id(desperta?"geo/asta/matadora_desperta.geo.json":"geo/asta/matadora.geo.json");}
        @Override public ResourceLocation getTextureResource(EspadaAsta a){return id(desperta?"textures/armor/trajes/matadora_desperta.png":"textures/armor/trajes/matadora.png");}
        @Override public ResourceLocation getAnimationResource(EspadaAsta a){return id("animations/asta/matadora.animation.json");}
    }
    private final Modelo modelo;
    private UUID dono;
    private boolean primeiraPessoa;
    public RenderEspadaAsta(){this(new Modelo());}
    private RenderEspadaAsta(Modelo modelo){super(modelo);this.modelo=modelo;addRenderLayer(new Brilho(this));}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext contexto,PoseStack pose,MultiBufferSource buffer,int luz,int overlay){
        UUID anterior=dono;boolean primeiraAnterior=primeiraPessoa,despertaAnterior=modelo.desperta;
        dono=null;primeiraPessoa=contexto.firstPerson();var mc=Minecraft.getInstance();
        if(primeiraPessoa&&mc.player!=null&&mc.player.getMainHandItem()==stack)dono=mc.player.getUUID();
        else if(mc.level!=null&&(contexto==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||contexto==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)){
            // Identity, not item equality: identical swords on other players must not inherit the cast.
            for(var p:mc.level.players())if(p.isAlive()&&p.getMainHandItem()==stack){dono=p.getUUID();break;}
        }
        var forma=dono==null?null:EfeitosAsta.forma(dono,mc.getFrameTime());
        modelo.desperta=forma!=null&&PerfilCinematico.espadaDesperta(forma.idade(),forma.alpha());
        try{
            super.renderByItem(stack,contexto,pose,buffer,luz,overlay);
            // No aura while the Black Divider stretches the blade: the flames follow the resting outline.
            if(modelo.desperta&&mc.level!=null&&EfeitosAsta.divider(dono,mc.getFrameTime())<0)aura(pose,mc.level.getGameTime()+mc.getFrameTime());
        }finally{dono=anterior;primeiraPessoa=primeiraAnterior;modelo.desperta=despertaAnterior;}
    }
    @Override public void renderRecursively(PoseStack pose,EspadaAsta item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getScaleX(),y=bone.getScaleY(),z=bone.getScaleZ();boolean hidden=bone.isHidden();
        double t=dono==null?-1:EfeitosAsta.divider(dono,parcial);
        if(bone.getName().equals("blade")){bone.setScaleX(PerfilDivider.largura(t));bone.setScaleY(PerfilDivider.comprimento(t,primeiraPessoa));bone.setScaleZ(1);}
        if(bone.getName().equals("energia_divider"))bone.setHidden(PerfilDivider.intensidade(t)<.03);
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setScaleX(x);bone.setScaleY(y);bone.setScaleZ(z);bone.setHidden(hidden);}
    }
    private static final int TINTA=0x140A12,HALO=0x8C1238;
    /** Flame aura in the sword model's pixels (GeckoLib's item origin is 0.5, 0.51, 0.5). Halo first, then the ink. */
    private static void aura(PoseStack pose,double tick){
        if(!com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia.iniciar(tick))return;
        pose.pushPose();pose.translate(.5,.51,.5);pose.scale(1/16f,1/16f,1/16f);
        try{
            var c=new org.joml.Matrix4f(pose.last().pose()).invert().transformPosition(new org.joml.Vector3f());
            var cam=new net.minecraft.world.phys.Vec3(c.x,c.y,c.z);
            for(int camada=0;camada<2;camada++){
                final boolean halo=camada==0;
                com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia.lote(pose,halo?8:22,false,()->{
                    for(int k=0;k<AuraMatadora.LINGUAS;k++){
                        int n=AuraMatadora.PONTOS;
                        if(halo){
                            // One strip per flame: overlapping translucent segments would band.
                            var a=v(AuraMatadora.lingua(k,0,tick));var m=v(AuraMatadora.lingua(k,.5,tick));var b=v(AuraMatadora.lingua(k,1,tick));double w=AuraMatadora.largura(k,0,tick);
                            fita(pose,cam,a,m,w*2.4+.8,w*1.6+.6,HALO,.25f);fita(pose,cam,m,b,w*1.6+.6,.3,HALO,.25f);continue;
                        }
                        for(int i=0;i<n;i++){double s0=i/(double)n,s1=(i+1)/(double)n;fita(pose,cam,v(AuraMatadora.lingua(k,s0,tick)),v(AuraMatadora.lingua(k,s1,tick)),AuraMatadora.largura(k,s0,tick),AuraMatadora.largura(k,s1,tick),TINTA,1);}
                    }
                });
            }
        }finally{pose.popPose();com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia.terminar();}
    }
    private static net.minecraft.world.phys.Vec3 v(AuraMatadora.P q){return new net.minecraft.world.phys.Vec3(q.x(),q.y(),q.z());}
    private static void fita(PoseStack pose,net.minecraft.world.phys.Vec3 cam,net.minecraft.world.phys.Vec3 a,net.minecraft.world.phys.Vec3 b,double wa,double wb,int cor,float alpha){
        var d=b.subtract(a);if(d.lengthSqr()<1e-10)return;var l=d.cross(cam.subtract(a));if(l.lengthSqr()<1e-10)return;l=l.normalize();
        com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia.ponto(pose,a.subtract(l.scale(wa)),0,0,cor,alpha);com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia.ponto(pose,b.subtract(l.scale(wb)),1,0,cor,alpha);
        com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia.ponto(pose,b.add(l.scale(wb)),1,1,cor,alpha);com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia.ponto(pose,a.add(l.scale(wa)),0,1,cor,alpha);
    }
    private static final class Brilho extends GeoRenderLayer<EspadaAsta>{
        private final RenderEspadaAsta espada;
        Brilho(RenderEspadaAsta renderer){super(renderer);espada=renderer;}
        @Override public void render(PoseStack pose,EspadaAsta item,BakedGeoModel model,RenderType base,MultiBufferSource buffer,VertexConsumer consumer,float parcial,int luz,int overlay){
            if(!espada.modelo.desperta)return;var type=RenderType.eyes(GLOW);
            getRenderer().reRender(model,pose,buffer,item,type,buffer.getBuffer(type),parcial,LightTexture.FULL_BRIGHT,overlay,1,1,1,1);
        }
    }
}
