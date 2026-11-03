package com.cauansilva.arcanecontrol.core.ichigo;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Head-attached, hair-free mask. Follows the player's real head animation. */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CamadaHollow extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>>{
    private static final ModelLayerLocation LAYER=new ModelLayerLocation(new ResourceLocation("arcanecontrol","hollow"),"main");
    private static final ResourceLocation TEXTURE=new ResourceLocation("arcanecontrol","textures/ichigo/mascara.png");
    private static RenderBankaiIchigo RENDER,MUGETSU_RENDER;private static net.minecraft.world.item.ItemStack PILHA;
    private final ModelPart mascara;
    public CamadaHollow(PlayerRenderer r,ModelPart m){super(r);mascara=m.getChild("mask");}
    @SubscribeEvent public static void definicoes(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,()->{
        // Hollow mask from the user's reference (viewer's right = model -x): domed plate wrapping the cheeks, slanted eye
        // holes, jagged teeth over a dark mouth, a red stripe down the middle and red stripes fanning over one side.
        var mesh=new MeshDefinition();var b=CubeListBuilder.create();
        b.texOffs(0,0).addBox(-4.2f,-8.3f,-4.75f,8.4f,5.9f,.7f).addBox(-3.6f,-2.45f,-4.7f,7.2f,2.7f,.6f)
            .addBox(-4.45f,-8f,-4.6f,.3f,6.8f,3.4f).addBox(4.15f,-8f,-4.6f,.3f,6.8f,3.4f).addBox(-3.4f,-8.6f,-4.6f,6.8f,.35f,2.6f);
        // Sharp slanted eyes (user): thin rows stepping from a low inner corner up to a pointed outer tip.
        for(int lado=-1;lado<=1;lado+=2){float[][] linhas={{2.7f,3.75f,-5.75f},{2.0f,3.7f,-5.45f},{1.35f,3.35f,-5.15f},{1.0f,2.6f,-4.85f},{.9f,1.6f,-4.55f}};
            for(var l:linhas){float x0=lado>0?l[0]:-l[1];b.texOffs(0,16).addBox(x0,l[2],-4.85f,l[1]-l[0],.32f,.2f);}}
        b.texOffs(0,16).addBox(-3.3f,-1.7f,-4.82f,6.6f,.75f,.15f);
        for(int k=0;k<8;k++){float x=-3.2f+k*.82f;b.texOffs(0,0).addBox(x,-2.0f,-4.9f,.6f,.45f,.15f).addBox(x+.15f,-1.55f,-4.9f,.3f,.3f,.15f).addBox(x+.35f,-.95f,-4.9f,.6f,.4f,.15f).addBox(x+.5f,-1.2f,-4.9f,.3f,.25f,.15f);}
        b.texOffs(18,16).addBox(-1f,-8.3f,-4.92f,.6f,6.1f,.2f).addBox(-3.6f,-7.9f,-4.92f,2.6f,.45f,.2f).addBox(-4f,-7f,-4.92f,2.8f,.45f,.2f).addBox(-4.2f,-6.1f,-4.92f,2.8f,.4f,.2f)
            .addBox(-4.2f,-4.1f,-4.92f,3.2f,.45f,.2f).addBox(-4f,-3.3f,-4.92f,2.6f,.4f,.2f).addBox(-3.4f,-.6f,-4.92f,2.4f,.4f,.2f).addBox(-4.5f,-7.4f,-4.4f,.35f,.45f,2.6f).addBox(-4.5f,-4.1f,-4.4f,.35f,.45f,2.6f);
        mesh.getRoot().addOrReplaceChild("mask",b,PartPose.ZERO);
        return LayerDefinition.create(mesh,64,64);
    });}
    @SubscribeEvent public static void camadas(EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins()){PlayerRenderer r=e.getSkin(skin);if(r!=null)r.addLayer(new CamadaHollow(r,e.getEntityModels().bakeLayer(LAYER)));}}
    @Override public void render(PoseStack s,MultiBufferSource buffer,int luz,AbstractClientPlayer p,float a,float b,float parcial,float idade,float yaw,float pitch){
        if(p.isInvisible()||p.isSpectator())return;
        if(EfeitosIchigo.mugetsu(p.getUUID())){
            // Mugetsu form replaces every outfit (user's reference): hair and skirt as cloth, see build_ichigo_traje.mugetsu.
            if(MUGETSU_RENDER==null)MUGETSU_RENDER=new RenderBankaiIchigo("ichigo_mugetsu");
            if(PILHA==null)PILHA=new net.minecraft.world.item.ItemStack(RegistroIchigo.PEITO.get());
            MUGETSU_RENDER.prepForRender(p,PILHA,net.minecraft.world.entity.EquipmentSlot.CHEST,getParentModel());
            MUGETSU_RENDER.renderToBuffer(s,buffer.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("arcanecontrol","textures/armor/trajes/ichigo_mugetsu.png"))),luz,OverlayTexture.NO_OVERLAY,1,1,1,1);
            return;
        }
        if(!EfeitosIchigo.bankaiVisual(p.getUUID()))return;
        // Detailed Bankai coat with cloth tails (same path as an armor layer: follows the player's part transforms).
        if(RENDER==null){RENDER=new RenderBankaiIchigo("ichigo_bankai");PILHA=new net.minecraft.world.item.ItemStack(RegistroIchigo.PEITO.get());}
        RENDER.prepForRender(p,PILHA,net.minecraft.world.entity.EquipmentSlot.CHEST,getParentModel());
        RENDER.renderToBuffer(s,buffer.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("arcanecontrol","textures/armor/trajes/ichigo_bankai.png"))),luz,OverlayTexture.NO_OVERLAY,1,1,1,1);
        if(!EfeitosIchigo.mascara(p.getUUID()))return;
        s.pushPose();try{getParentModel().head.translateAndRotate(s);mascara.render(s,buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),luz,OverlayTexture.NO_OVERLAY);}finally{s.popPose();}
    }
}
