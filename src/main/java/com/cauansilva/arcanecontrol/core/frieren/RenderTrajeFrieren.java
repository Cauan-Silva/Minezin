package com.cauansilva.arcanecontrol.core.frieren;

import com.cauansilva.arcanecontrol.core.equipamentos.*;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoFrieren;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/** Frieren-only secondary cloth; restore every bone to avoid leaking poses across players/passes. */
public final class RenderTrajeFrieren extends GeoArmorRenderer<TrajeAnime>{
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
}
