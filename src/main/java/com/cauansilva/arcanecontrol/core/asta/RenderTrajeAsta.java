package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.client.tecido.TecidoAsta;
import com.cauansilva.arcanecontrol.core.equipamentos.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import software.bernie.geckolib.cache.object.GeoBone;

/** Asta's outfit with the cloth robe (TecidoAsta); keeps the Devil Union hiding of RenderTrajeAnime. Every bone is
 *  restored after drawing so poses never leak across players or passes. */
public final class RenderTrajeAsta extends RenderTrajeAnime{
    @Override public void renderRecursively(PoseStack pose,TrajeAnime item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getRotX(),y=bone.getRotY(),z=bone.getRotZ();var entity=getCurrentEntity();
        if(entity!=null&&TecidoAsta.OSSOS.contains(bone.getName())){
            var p=TecidoAstaCliente.pose(entity.getUUID(),bone.getName(),parcial);bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());
        }
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setRotX(x);bone.setRotY(y);bone.setRotZ(z);}
    }
}
