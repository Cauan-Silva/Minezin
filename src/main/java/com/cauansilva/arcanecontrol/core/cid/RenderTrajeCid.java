package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.client.tecido.CapturaVertices;
import com.cauansilva.arcanecontrol.core.client.tecido.SuperficieTecido;
import com.cauansilva.arcanecontrol.core.client.tecido.TecidoIchigo;
import com.cauansilva.arcanecontrol.core.equipamentos.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import java.util.*;
/** Cid-only renderer. The coat bones keep their cloth physics (TecidoIchigo) but are not drawn as blocks: their posed
 *  panels are read back and stitched into one smooth sheet (SuperficieTecido.casaco), and the collar is a curved
 *  shell (SuperficieTecido.gola) in the cid_gola bone's space. The palette cubes of cid_gola only lend their texels
 *  (black, purple, gold). Emissive pass draws only the gem. */
public final class RenderTrajeCid extends GeoArmorRenderer<TrajeAnime>{
    private static final ResourceLocation GLOW=new ResourceLocation("arcanecontrol","textures/armor/trajes/shadow_glowmask.png");
    private final Map<String,double[][]> cantos=new HashMap<>(),repouso=new HashMap<>();
    private final float[][] paleta=new float[3][];
    private final List<SuperficieTecido.Obstaculo> pernas=new ArrayList<>();
    private Matrix4f gola;private Matrix3f golaNormal;
    public RenderTrajeCid(){super(new ModeloTrajeAnime(false));addRenderLayer(new Emissao(this));}
    @Override protected void setBoneVisible(GeoBone bone,boolean visivel){super.setBoneVisible(bone,visivel);if(bone!=null)bone.setChildrenHidden(!visivel);}
    @Override public void renderRecursively(PoseStack pose,TrajeAnime item,GeoBone bone,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        float x=bone.getRotX(),y=bone.getRotY(),z=bone.getRotZ();
        var entity=getCurrentEntity();String name=bone.getName();
        if(entity!=null&&TecidoIchigo.OSSOS.contains(name)){
            var p=MantoCidCliente.pose(entity.getUUID(),name,parcial);bone.setRotX(x+(float)p.x());bone.setRotY(y+(float)p.y());bone.setRotZ(z+(float)p.z());
        }
        try{super.renderRecursively(pose,item,bone,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);}
        finally{bone.setRotX(x);bone.setRotY(y);bone.setRotZ(z);}
    }
    @Override public void renderCubesOfBone(PoseStack pose,GeoBone bone,VertexConsumer consumer,int luz,int overlay,float red,float green,float blue,float alpha){
        String name=bone.getName();
        if(name.startsWith("bk_capa_")){
            // Read the main panel (first cube) as posed by GeckoLib; nothing is drawn as a block.
            if(!bone.isHidden()&&!bone.getCubes().isEmpty()){var cubo=bone.getCubes().get(0);var c=new CapturaVertices();renderCube(pose,cubo,c,luz,overlay,1,1,1,1);cantos.put(name,c.cantos());
                // Rest vertices in the same order GeckoLib emits them (null quads are skipped by both).
                repouso.computeIfAbsent(name,k->{var l=new ArrayList<double[]>();for(var q:cubo.quads())if(q!=null)for(var v:q.vertices())l.add(new double[]{v.position().x(),v.position().y(),v.position().z()});return l.toArray(new double[0][]);});}
            return;
        }
        if((name.equals("armorLeftLeg")||name.equals("armorRightLeg"))&&!bone.getCubes().isEmpty()){
            // The legs are drawn in the leggings pass, but their pose is known in every pass: read them as obstacles.
            var cubo=bone.getCubes().get(0);var c=new CapturaVertices();renderCube(pose,cubo,c,luz,overlay,1,1,1,1);
            var rep=new ArrayList<double[]>();for(var q:cubo.quads())if(q!=null)for(var v:q.vertices())rep.add(new double[]{v.position().x(),v.position().y(),v.position().z()});
            pernas.add(SuperficieTecido.caixa(rep.toArray(new double[0][]),c.cantos()));
        }
        if(name.equals("cid_gola")){
            if(bone.isHidden())return;gola=new Matrix4f(pose.last().pose());golaNormal=new Matrix3f(pose.last().normal());
            for(int i=0;i<Math.min(3,bone.getCubes().size());i++){var c=new CapturaVertices();renderCube(pose,bone.getCubes().get(i),c,luz,overlay,1,1,1,1);paleta[i]=c.uv();}
            return;
        }
        super.renderCubesOfBone(pose,bone,consumer,luz,overlay,red,green,blue,alpha);
    }
    @Override public void actuallyRender(PoseStack pose,TrajeAnime item,BakedGeoModel model,RenderType type,MultiBufferSource buffer,VertexConsumer consumer,boolean reRender,float parcial,int luz,int overlay,float red,float green,float blue,float alpha){
        cantos.clear();pernas.clear();gola=null;
        super.actuallyRender(pose,item,model,type,buffer,consumer,reRender,parcial,luz,overlay,red,green,blue,alpha);
        if(reRender||consumer==null||paleta[0]==null)return;
        if(!cantos.isEmpty())for(var t:SuperficieTecido.casaco(cantos,repouso,com.cauansilva.arcanecontrol.core.client.tecido.ColisaoCenario.com(pernas,getCurrentEntity())))emitir(consumer,t,null,null,luz,overlay,red,green,blue,alpha);
        if(gola!=null)for(var t:SuperficieTecido.gola())emitir(consumer,t,gola,golaNormal,luz,overlay,red,green,blue,alpha);
    }
    /** One triangle as a degenerate quad (entity render types take quads); m == null: points already posed. */
    private void emitir(VertexConsumer v,SuperficieTecido.Tri t,Matrix4f m,Matrix3f mn,int luz,int overlay,float r,float g,float b,float a){
        var uv=paleta[Math.min(t.material(),2)]==null?paleta[0]:paleta[Math.min(t.material(),2)];
        double[][] ps={t.a(),t.b(),t.c(),t.c()};double[] n=SuperficieTecido.normal(t);
        var nn=new Vector3f((float)n[0],(float)n[1],(float)n[2]);
        if(m!=null){// model units -> blocks; GeckoLib mirrors x for Bedrock geometry (the collar is symmetric anyway).
            mn.transform(nn);nn.normalize();
        }
        for(var p:ps){
            var q=m==null?new Vector4f((float)p[0],(float)p[1],(float)p[2],1):m.transform(new Vector4f((float)(-p[0]/16),(float)(p[1]/16),(float)(p[2]/16),1));
            v.vertex(q.x(),q.y(),q.z(),r,g,b,a,uv[0],uv[1],overlay,luz,nn.x(),nn.y(),nn.z());
        }
    }
    private static final class Emissao extends GeoRenderLayer<TrajeAnime>{
        Emissao(RenderTrajeCid renderer){super(renderer);}
        @Override public void render(PoseStack pose,TrajeAnime item,BakedGeoModel model,RenderType base,MultiBufferSource buffer,VertexConsumer consumer,float parcial,int luz,int overlay){
            if(!ConfigCliente.EMISSAO_CID.get())return;var type=RenderType.eyes(GLOW);
            getRenderer().reRender(model,pose,buffer,item,type,buffer.getBuffer(type),parcial,LightTexture.FULL_BRIGHT,overlay,1,1,1,1);
        }
    }
}
