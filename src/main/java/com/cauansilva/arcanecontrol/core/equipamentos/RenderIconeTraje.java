package com.cauansilva.arcanecontrol.core.equipamentos;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import java.util.*;
/**
 * Icone das pecas de traje: centraliza e encaixa o modelo (geo/armaduras/<estilo>_<peca>) num bloco, medindo os cubos
 * de verdade. Corrige icones gigantes ou deslocados no slot (cada peca tinha centro e tamanho proprios); o tamanho
 * final no slot vem do display.gui de models/item (mesmo valor em todas as pecas).
 */
public final class RenderIconeTraje extends GeoItemRenderer<TrajeAnime>{
    private static final Map<BakedGeoModel,float[]> CAIXAS=new WeakHashMap<>();
    public RenderIconeTraje(){super(new ModeloTrajeAnime(true));}
    @Override public void preRender(PoseStack pose,TrajeAnime a,BakedGeoModel modelo,MultiBufferSource fontes,VertexConsumer buffer,boolean reRender,float parcial,int luz,int overlay,float r,float g,float b,float alpha){
        super.preRender(pose,a,modelo,fontes,buffer,reRender,parcial,luz,overlay,r,g,b,alpha);
        if(reRender)return;
        float[] c=CAIXAS.computeIfAbsent(modelo,RenderIconeTraje::medir);if(c==null)return;
        // super ja moveu para (0.5, 0.51, 0.5): encaixa a maior dimensao em 1 bloco e poe o centro da caixa no centro do bloco
        float k=1f/Math.max(.05f,Math.max(c[3]-c[0],Math.max(c[4]-c[1],c[5]-c[2])));
        pose.translate(0,-.01f,0);pose.scale(k,k,k);pose.translate(-(c[0]+c[3])/2,-(c[1]+c[4])/2,-(c[2]+c[5])/2);
    }
    private static float[] medir(BakedGeoModel m){
        float[] c={Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,-Float.MAX_VALUE,-Float.MAX_VALUE,-Float.MAX_VALUE};
        Deque<GeoBone> pilha=new ArrayDeque<>(m.topLevelBones());boolean achou=false;
        while(!pilha.isEmpty()){var osso=pilha.pop();pilha.addAll(osso.getChildBones());
            for(var cubo:osso.getCubes())for(var q:cubo.quads())for(var v:q.vertices()){var p=v.position();achou=true;
                c[0]=Math.min(c[0],p.x());c[1]=Math.min(c[1],p.y());c[2]=Math.min(c[2],p.z());c[3]=Math.max(c[3],p.x());c[4]=Math.max(c[4],p.y());c[5]=Math.max(c[5],p.z());}}
        return achou?c:null;
    }
}
