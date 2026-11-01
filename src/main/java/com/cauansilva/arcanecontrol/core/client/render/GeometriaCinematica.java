package com.cauansilva.arcanecontrol.core.client.render;

import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.vfx.PerfilCinematico;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

/** Batched, depth-tested geometry; callers own shader state and world translation. */
public final class GeometriaCinematica {
    private static final Vec3 UP=new Vec3(0,1,0);
    private GeometriaCinematica() {}
    public static void raios(PoseStack s,Vec3 cam,Vec3 centro,double r,double t,int cor,float alpha){
        int n=PerfilCinematico.raios(ConfigCliente.VFX_REDUZIDOS.get());double fase=Math.floor(t/2);
        for(int j=0;j<n;j++){
            double a=j*Math.PI*2/n+Math.sin(fase*.37+j)*.12;
            Vec3 radial=new Vec3(Math.cos(a),.3+Math.sin(j*2.1)*.45,Math.sin(a)),prev=centro;
            for(int i=1;i<=7;i++){
                double u=i/7d;Vec3 q=centro.add(radial.scale(r*u)).add(Math.sin(i*7+j*3+fase)*r*.04,Math.cos(i*3+j+fase)*r*.035,Math.sin(i*5+j+fase)*r*.04);
                DesenhoMagia.fita(s,cam,prev,q,.10*(1-u*.65),0,1,cor,alpha*.45f);
                DesenhoMagia.fita(s,cam,prev,q,.024*(1-u*.65),0,1,0xF3E9FF,alpha);
                if(i==4)DesenhoMagia.fita(s,cam,q,q.add(radial.scale(r*.15)).add(Math.sin(j)*r*.2,r*.18,Math.cos(j)*r*.2),.018,0,1,cor,alpha*.7f);
                prev=q;
            }
        }
    }
    public static void explosao(PoseStack s,Vec3 cam,double idade,double raio,int cor,float alpha){
        double expand=1-Math.exp(-Math.max(0,idade)/5);
        DesenhoMagia.anel(s,new Vec3(0,.12,0),UP,Math.max(0,raio*expand-.22),raio*expand,cor,alpha,64);
        int n=ConfigCliente.VFX_REDUZIDOS.get()?8:22;
        for(int i=0;i<n;i++){
            double a=i*2.399963;Vec3 d=new Vec3(Math.cos(a),.06+(i%4)*.08,Math.sin(a));
            double r=raio*expand*(.7+(i%5)*.07);
            DesenhoMagia.fita(s,cam,d.scale(r*.45).add(0,.2,0),d.scale(r).add(0,.2,0),.06*(1-expand*.7),0,2,i%3==0?0xFFFFFF:cor,alpha);
        }
    }
    public static void arco(PoseStack s,Vec3 cam,Vec3 centro,Vec3 eixo,Vec3 alto,double raio,double progresso,double abertura,double largura,int cor,float alpha){
        int n=ConfigCliente.VFX_REDUZIDOS.get()?18:36;
        for(int i=0;i<n;i++){
            double u=i/(double)n,a=-abertura/2+(u+progresso-.5)*abertura,b=a+abertura/n;
            Vec3 p=eixo.scale(Math.cos(a)).add(alto.scale(Math.sin(a))),q=eixo.scale(Math.cos(b)).add(alto.scale(Math.sin(b)));
            double ponta=Math.sin(u*Math.PI);
            DesenhoMagia.fita(s,cam,centro.add(p.scale(raio)),centro.add(q.scale(raio)),largura*Math.sqrt(ponta),u*4,(u+1d/n)*4,cor,alpha*(float)(.3+.7*ponta));
        }
    }
    public static void detritosSingularidade(PoseStack s,double t,double nucleo,float alpha){
        int n=PerfilCinematico.fragmentos(ConfigCliente.VFX_REDUZIDOS.get());
        DesenhoMagia.lote(s,8,false,()->{
            for(int i=0;i<n;i++){
                var p=PerfilCinematico.orbita(i,t,nucleo);double size=.07+(i%4)*.04;
                s.pushPose();s.translate(p.x(),p.y(),p.z());
                s.mulPose(com.mojang.math.Axis.XP.rotationDegrees((float)(t*3+i*43)));
                s.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float)(t*2+i*29)));
                DesenhoMagia.caixa(s,Vec3.ZERO,size,size*.65,size*.85,i%2==0?0x716D77:0x464353,(float)(alpha*PerfilCinematico.opacidadeFragmento(i,t)));s.popPose();
            }
        });
    }
}
