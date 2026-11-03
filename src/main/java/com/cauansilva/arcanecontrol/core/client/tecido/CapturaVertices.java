package com.cauansilva.arcanecontrol.core.client.tecido;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.*;
import software.bernie.geckolib.cache.object.GeoCube;

/** VertexConsumer that only records where GeckoLib would put a cube's vertices (already posed) and the UV of its
 *  first face. Shared by the smooth-cloth renderers (RenderTrajeCid, RenderBankaiIchigo). */
public final class CapturaVertices implements VertexConsumer{
    private final List<double[]> pos=new ArrayList<>();private double su,sv;private int n;
    public double[][] cantos(){return pos.toArray(new double[0][]);}
    public float[] uv(){return n==0?new float[]{0,0}:new float[]{(float)(su/n),(float)(sv/n)};}
    /** Rest vertices in the same order GeckoLib emits them (null quads are skipped by both). */
    public static double[][] repouso(GeoCube cubo){
        var l=new ArrayList<double[]>();for(var q:cubo.quads())if(q!=null)for(var v:q.vertices())l.add(new double[]{v.position().x(),v.position().y(),v.position().z()});
        return l.toArray(new double[0][]);
    }
    @Override public void vertex(float x,float y,float z,float r,float g,float b,float a,float u,float v,int o,int l,float nx,float ny,float nz){pos.add(new double[]{x,y,z});if(n<4){su+=u;sv+=v;n++;}}
    @Override public VertexConsumer vertex(double x,double y,double z){pos.add(new double[]{x,y,z});return this;}
    @Override public VertexConsumer color(int r,int g,int b,int a){return this;}
    @Override public VertexConsumer uv(float u,float v){if(n<4){su+=u;sv+=v;n++;}return this;}
    @Override public VertexConsumer overlayCoords(int u,int v){return this;}
    @Override public VertexConsumer uv2(int u,int v){return this;}
    @Override public VertexConsumer normal(float x,float y,float z){return this;}
    @Override public void endVertex(){}
    @Override public void defaultColor(int r,int g,int b,int a){}
    @Override public void unsetDefaultColor(){}
}
