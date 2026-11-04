package com.cauansilva.arcanecontrol.core.client.tecido;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import java.util.*;

/** Closed smooth rings for a GeoArmorRenderer (docs/padrao-trajes.md, section 4): Frieren's skirt and capelet,
 *  Gojo's jacket hem; or open sheets cut at the front (Asta's robe). Per frame the renderer hands every bone to capturar(); cloth bones (one of the prefixes) give
 *  their posed main panel instead of drawing it, the legs give their boxes. desenhar() then emits one ring per prefix,
 *  coloured with the texel of its first panel. The trim cubes of the cloth bones are drawn as usual. */
public final class AneisLisos {
    /** The renderer's own GeckoLib cube drawing (GeoRenderer.renderCube), so posed vertices can be captured. */
    public interface Cubo{void desenhar(PoseStack pose,GeoCube cubo,VertexConsumer saida,int luz,int overlay,float r,float g,float b,float a);}
    private final Map<String,Integer> prefixos;private final Set<String> abertos;
    private final Map<String,double[][]> cantos=new HashMap<>(),repouso=new HashMap<>();
    private final Map<String,float[]> cor=new HashMap<>();
    private final List<SuperficieTecido.Caixa> pernas=new ArrayList<>();
    /** prefixos: bone prefix -> segments of that garment, e.g. {"frieren_saia_": 3}. */
    public AneisLisos(Map<String,Integer> prefixos){this(prefixos,Set.of());}
    /** abertos: prefixes drawn as an open sheet (cut at the front opening, SuperficieTecido.aberto) instead of a ring. */
    public AneisLisos(Map<String,Integer> prefixos,Set<String> abertos){this.prefixos=Map.copyOf(prefixos);this.abertos=Set.copyOf(abertos);}
    public void limpar(){cantos.clear();pernas.clear();}
    private String prefixo(String nome){for(var p:prefixos.keySet())if(nome.startsWith(p))return p;return null;}
    /** true when the bone was fully handled (cloth panel captured, trims drawn); false: draw it normally. */
    public boolean capturar(PoseStack pose,GeoBone bone,VertexConsumer consumer,Cubo cubo,int luz,int overlay,float r,float g,float b,float a){
        String nome=bone.getName(),p=prefixo(nome);
        if(p!=null&&!bone.isHidden()&&!bone.getCubes().isEmpty()){
            var principal=bone.getCubes().get(0);var c=new CapturaVertices();cubo.desenhar(pose,principal,c,luz,overlay,1,1,1,1);cantos.put(nome,c.cantos());
            repouso.computeIfAbsent(nome,k->CapturaVertices.repouso(principal));cor.putIfAbsent(p,c.uv());
            for(int i=1;i<bone.getCubes().size();i++)cubo.desenhar(pose,bone.getCubes().get(i),consumer,luz,overlay,r,g,b,a);
            return true;
        }
        if((nome.equals("armorLeftLeg")||nome.equals("armorRightLeg"))&&!bone.getCubes().isEmpty()){
            // The legs are drawn in the leggings pass, but their pose is known in every pass: read them as obstacles.
            var perna=bone.getCubes().get(0);var c=new CapturaVertices();cubo.desenhar(pose,perna,c,luz,overlay,1,1,1,1);
            pernas.add(SuperficieTecido.caixa(CapturaVertices.repouso(perna),c.cantos()));
        }
        return false;
    }
    public void desenhar(VertexConsumer v,int luz,int overlay,float r,float g,float b,float a){
        if(cantos.isEmpty())return;
        for(var e:prefixos.entrySet()){var uv=cor.get(e.getKey());if(uv==null)continue;
            var tris=abertos.contains(e.getKey())?SuperficieTecido.aberto(e.getKey(),e.getValue(),cantos,repouso,pernas):SuperficieTecido.anel(e.getKey(),e.getValue(),cantos,repouso,pernas);
            for(var t:tris){
                double[] n=SuperficieTecido.normal(t);
                // One triangle as a degenerate quad (entity render types take quads); points are already posed.
                for(var p:new double[][]{t.a(),t.b(),t.c(),t.c()})v.vertex((float)p[0],(float)p[1],(float)p[2],r,g,b,a,uv[0],uv[1],overlay,luz,(float)n[0],(float)n[1],(float)n[2]);
            }}
    }
}
