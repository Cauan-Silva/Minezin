package com.cauansilva.arcanecontrol.core.frieren;

import com.cauansilva.arcanecontrol.core.equipamentos.*;
import com.cauansilva.arcanecontrol.core.client.tecido.CapturaVertices;
import com.cauansilva.arcanecontrol.core.client.tecido.SuperficieTecido;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoFrieren;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import java.util.*;

/** Frieren's capelet and skirt on the outfit standard (docs/padrao-trajes.md, section 4): the cloth bones keep their
 *  physics (TecidoFrieren) but their main panel is not drawn as a block; each garment is one closed smooth ring
 *  (SuperficieTecido.anel) pushed out of the legs. The trim cubes of the bones are still drawn on top. Every bone is
 *  restored after drawing so poses never leak across players/passes. */
public final class RenderTrajeFrieren extends GeoArmorRenderer<TrajeAnime>{
    private static final String CAPA="frieren_capa_",SAIA="frieren_saia_";
    private final Map<String,double[][]> cantos=new HashMap<>(),repouso=new HashMap<>();
    private final Map<String,float[]> cor=new HashMap<>();
    private final List<SuperficieTecido.Caixa> pernas=new ArrayList<>();
    public RenderTrajeFrieren(){super(new ModeloTrajeAnime(false));}
    @Override protected void setBoneVisible(GeoBone bone,boolean visivel){super.setBoneVisible(bone,visivel);if(bone!=null)bone.setChildrenHidden(!visivel);}
    @Override public void renderRecursively(PoseStack pose,TrajeAnime item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getRotX(),y=bone.getRotY(),z=bone.getRotZ();var entity=getCurrentEntity();
        if(entity!=null&&TecidoFrieren.OSSOS.contains(bone.getName())){
            var p=TecidoFrierenCliente.pose(entity.getUUID(),bone.getName(),parcial);bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());
        }
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setRotX(x);bone.setRotY(y);bone.setRotZ(z);}
    }
    @Override public void renderCubesOfBone(PoseStack pose,GeoBone bone,VertexConsumer consumer,int luz,int overlay,float red,float green,float blue,float alpha){
        String name=bone.getName();
        if((name.startsWith(CAPA)||name.startsWith(SAIA))&&!bone.isHidden()&&!bone.getCubes().isEmpty()){
            // Main panel: read where GeckoLib would put it (the smooth ring replaces it); trim cubes drawn as usual.
            var cubo=bone.getCubes().get(0);var c=new CapturaVertices();renderCube(pose,cubo,c,luz,overlay,1,1,1,1);cantos.put(name,c.cantos());
            repouso.computeIfAbsent(name,k->CapturaVertices.repouso(cubo));cor.putIfAbsent(name.startsWith(CAPA)?CAPA:SAIA,c.uv());
            for(int i=1;i<bone.getCubes().size();i++)renderCube(pose,bone.getCubes().get(i),consumer,luz,overlay,red,green,blue,alpha);
            return;
        }
        if((name.equals("armorLeftLeg")||name.equals("armorRightLeg"))&&!bone.getCubes().isEmpty()){
            // The legs are drawn in the leggings pass, but their pose is known in every pass: read them as obstacles.
            var cubo=bone.getCubes().get(0);var c=new CapturaVertices();renderCube(pose,cubo,c,luz,overlay,1,1,1,1);
            pernas.add(SuperficieTecido.caixa(CapturaVertices.repouso(cubo),c.cantos()));
        }
        super.renderCubesOfBone(pose,bone,consumer,luz,overlay,red,green,blue,alpha);
    }
    @Override public void actuallyRender(PoseStack pose,TrajeAnime item,BakedGeoModel model,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        cantos.clear();pernas.clear();
        super.actuallyRender(pose,item,model,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);
        if(reRender||consumer==null||cantos.isEmpty())return;
        anel(consumer,CAPA,TecidoFrieren.LINHAS_CAPA,luz,overlay,red,green,blue,alpha);
        anel(consumer,SAIA,TecidoFrieren.LINHAS_SAIA,luz,overlay,red,green,blue,alpha);
    }
    private void anel(VertexConsumer v,String prefixo,int linhas,int luz,int overlay,float r,float g,float b,float a){
        var uv=cor.get(prefixo);if(uv==null)return;
        for(var t:SuperficieTecido.anel(prefixo,linhas,cantos,repouso,pernas)){
            double[] n=SuperficieTecido.normal(t);
            // One triangle as a degenerate quad (entity render types take quads); points are already posed.
            for(var p:new double[][]{t.a(),t.b(),t.c(),t.c()})v.vertex((float)p[0],(float)p[1],(float)p[2],r,g,b,a,uv[0],uv[1],overlay,luz,(float)n[0],(float)n[1],(float)n[2]);
        }
    }
}
