package com.cauansilva.arcanecontrol.core.frieren;

import com.cauansilva.arcanecontrol.core.equipamentos.*;
import com.cauansilva.arcanecontrol.core.client.tecido.AneisLisos;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoFrieren;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import java.util.Map;

/** Frieren's capelet and skirt on the outfit standard (docs/padrao-trajes.md, section 4): the cloth bones keep their
 *  physics (TecidoFrieren); each garment is drawn as one closed smooth ring pushed out of the legs (AneisLisos), the
 *  trims stay blocks. Every bone is restored after drawing so poses never leak across players/passes. */
public final class RenderTrajeFrieren extends GeoArmorRenderer<TrajeAnime>{
    private final AneisLisos aneis=new AneisLisos(Map.of("frieren_capa_",TecidoFrieren.LINHAS_CAPA,"frieren_saia_",TecidoFrieren.LINHAS_SAIA));
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
        if(!aneis.capturar(pose,bone,consumer,this::renderCube,luz,overlay,red,green,blue,alpha))super.renderCubesOfBone(pose,bone,consumer,luz,overlay,red,green,blue,alpha);
    }
    @Override public void actuallyRender(PoseStack pose,TrajeAnime item,BakedGeoModel model,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        aneis.limpar();
        super.actuallyRender(pose,item,model,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);
        if(!reRender&&consumer!=null)aneis.desenhar(consumer,getCurrentEntity(),luz,overlay,red,green,blue,alpha);
    }
}
