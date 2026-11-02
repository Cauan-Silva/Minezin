package com.cauansilva.arcanecontrol.core.equipamentos;
import com.mojang.blaze3d.vertex.*;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.cache.object.GeoBone;
public final class RenderTrajeAnime extends GeoArmorRenderer<TrajeAnime>{
    public RenderTrajeAnime(){super(new ModeloTrajeAnime(false));}
    @Override protected void setBoneVisible(GeoBone bone,boolean visivel){
        super.setBoneVisible(bone,visivel);if(bone!=null)bone.setChildrenHidden(!visivel);
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int luz,int overlay,float red,float green,float blue,float alpha){
        if(cobertoPelaDevilUnion())return;
        super.renderToBuffer(pose,buffer,luz,overlay,red,green,blue,alpha);
    }
    /** Asta's Devil Union coats the bare body: each outfit slot disappears once its body parts are fully black. */
    private boolean cobertoPelaDevilUnion(){
        var entidade=getCurrentEntity();var slot=getCurrentSlot();if(entidade==null||slot==null)return false;
        var estado=com.cauansilva.arcanecontrol.core.asta.EfeitosAsta.forma(entidade.getUUID(),net.minecraft.client.Minecraft.getInstance().getFrameTime());
        if(estado==null)return false;
        String[] partes=switch(slot){
            case HEAD->new String[]{"head"};
            case CHEST->new String[]{"body","right_arm","left_arm"};
            case LEGS->new String[]{"body","right_leg","left_leg"};
            case FEET->new String[]{"right_leg","left_leg"};
            default->new String[0];
        };
        return com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico.trajeCoberto(estado.idade(),estado.alpha(),partes);
    }
}
