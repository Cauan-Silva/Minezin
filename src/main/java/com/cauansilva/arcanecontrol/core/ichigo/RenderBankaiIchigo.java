package com.cauansilva.arcanecontrol.core.ichigo;

import com.cauansilva.arcanecontrol.core.client.tecido.CapturaVertices;
import com.cauansilva.arcanecontrol.core.client.tecido.SuperficieTecido;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoIchigo;
import com.cauansilva.arcanecontrol.core.equipamentos.TrajeAnime;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import java.util.*;

/** Bankai coat / Mugetsu form drawn like RenderFormaAsta: one GeckoLib model (geo/armaduras/ichigo_bankai) following the player's
 *  parts; the tail bones take TecidoIchigo deltas and are restored after drawing.
 *  Like RenderTrajeCid (padrao-trajes), the coat skirt is not drawn as blocks: each bk_capa_ bone's main panel is read
 *  back posed and stitched into one smooth sheet (SuperficieTecido.casaco) pushed out of the legs. The small detail
 *  cubes on the panels (white trim, seams) and the Mugetsu closed front (mg_saia_*) stay blocks. */
public final class RenderBankaiIchigo extends GeoArmorRenderer<TrajeAnime>{
    private static ResourceLocation id(String p){return new ResourceLocation("arcanecontrol",p);}
    private static final class Modelo extends GeoModel<TrajeAnime>{
        private final String estilo;Modelo(String estilo){this.estilo=estilo;}
        @Override public ResourceLocation getModelResource(TrajeAnime a){return id("geo/armaduras/"+estilo+".geo.json");}
        @Override public ResourceLocation getTextureResource(TrajeAnime a){return id("textures/armor/trajes/"+estilo+".png");}
        @Override public ResourceLocation getAnimationResource(TrajeAnime a){return id("animations/armaduras/"+estilo+".animation.json");}
    }
    private final Map<String,double[][]> cantos=new HashMap<>(),repouso=new HashMap<>();
    private final List<SuperficieTecido.Obstaculo> pernas=new ArrayList<>();
    private float[] tecido;
    /** estilo: ichigo_bankai (coat) or ichigo_mugetsu (Mugetsu form); both use TecidoIchigo bones. */
    public RenderBankaiIchigo(String estilo){super(new Modelo(estilo));}
    @Override protected void applyBoneVisibilityBySlot(EquipmentSlot slot){setAllVisible(true);}
    @Override public void renderRecursively(PoseStack pose,TrajeAnime item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getRotX(),y=bone.getRotY(),z=bone.getRotZ();var entidade=getCurrentEntity();
        if(entidade!=null&&TecidoIchigo.OSSOS.contains(bone.getName())){
            var p=TecidoIchigoCliente.pose(entidade.getUUID(),bone.getName(),parcial);bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());
        }
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setRotX(x);bone.setRotY(y);bone.setRotZ(z);}
    }
    @Override public void renderCubesOfBone(PoseStack pose,GeoBone bone,VertexConsumer consumer,int luz,int overlay,float red,float green,float blue,float alpha){
        String name=bone.getName();
        if(name.startsWith("bk_capa_")){
            if(bone.isHidden()||bone.getCubes().isEmpty())return;var cubos=bone.getCubes();
            // Main panel: read as posed by GeckoLib, drawn later as part of the sheet.
            var c=new CapturaVertices();renderCube(pose,cubos.get(0),c,luz,overlay,1,1,1,1);cantos.put(name,c.cantos());
            repouso.computeIfAbsent(name,k->CapturaVertices.repouso(cubos.get(0)));
            if(tecido==null)tecido=c.uv();
            // Detail cubes stay blocks on top of the sheet.
            for(int i=1;i<cubos.size();i++){pose.pushPose();renderCube(pose,cubos.get(i),consumer,luz,overlay,red,green,blue,alpha);pose.popPose();}
            return;
        }
        if((name.equals("armorLeftLeg")||name.equals("armorRightLeg"))&&!bone.isHidden()&&!bone.getCubes().isEmpty()){
            var cubo=bone.getCubes().get(0);var c=new CapturaVertices();renderCube(pose,cubo,c,luz,overlay,1,1,1,1);
            pernas.add(SuperficieTecido.caixa(CapturaVertices.repouso(cubo),c.cantos()));
        }
        super.renderCubesOfBone(pose,bone,consumer,luz,overlay,red,green,blue,alpha);
    }
    @Override public void actuallyRender(PoseStack pose,TrajeAnime item,BakedGeoModel model,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        cantos.clear();pernas.clear();
        super.actuallyRender(pose,item,model,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);
        if(consumer==null||tecido==null||cantos.isEmpty())return;
        for(var t:SuperficieTecido.casaco(cantos,repouso,com.cauansilva.arcanecontrol.core.client.tecido.ColisaoCenario.com(pernas,getCurrentEntity()))){
            // One triangle as a degenerate quad (entity render types take quads); points are already posed.
            double[] n=SuperficieTecido.normal(t);
            for(var p:new double[][]{t.a(),t.b(),t.c(),t.c()})
                consumer.vertex((float)p[0],(float)p[1],(float)p[2],red,green,blue,alpha,tecido[0],tecido[1],overlay,luz,(float)n[0],(float)n[1],(float)n[2]);
        }
    }
}
