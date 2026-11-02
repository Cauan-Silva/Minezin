package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.cauansilva.arcanecontrol.core.client.render.GeometriaCinematica;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class RenderMovesetCid {
    private static final int VIOLETA=0xB76BFF,CLARO=0xE2CFFF;
    private static void composicao(PoseStack pose,int tipo,double t,double distancia,Vec3 direcao,Vec3 cam){
        double yaw=Math.atan2(direcao.x,direcao.z),co=Math.cos(yaw),si=Math.sin(yaw);
        var camera=new com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.V(cam.x*co-cam.z*si,cam.y,cam.x*si+cam.z*co);
        var lotes=com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.gerar(tipo,t,distancia,camera,com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get());
        pose.pushPose();pose.mulPose(com.mojang.math.Axis.YP.rotation((float)yaw));
        try{for(var lote:lotes)DesenhoMagia.lote(pose,lote.material(),lote.aditivo(),()->{for(var q:lote.faces()){
            var pontos=new com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.V[]{q.a(),q.b(),q.c(),q.d()};
            for(int i=0;i<4;i++){var p=pontos[i];DesenhoMagia.ponto(pose,new Vec3(p.x(),p.y(),p.z()),i==0||i==3?q.u0():q.u1(),i<2?q.v0():q.v1(),q.cor(),q.alpha());}
        }});}finally{pose.popPose();}
    }
    public static void transformacao(PoseStack pose,double t,Vec3 cam){
        if(t<0||t>=32||!DesenhoMagia.iniciar(t))return;
        try{composicao(pose,17,t,0,new Vec3(0,0,1),cam);}finally{DesenhoMagia.terminar();}
    }
    public static void selosAtomic(PoseStack pose,double t,Vec3 cam,Vec3 ancora){
        if(t<0||t>=240||!DesenhoMagia.iniciar(t))return;
        try{
            composicao(pose,14,t,0,new Vec3(0,0,1),cam);

        }finally{DesenhoMagia.terminar();}
    }

    public static boolean desenhar(PoseStack pose,int tipo,UUID dono,Vec3 origem,Vec3 dir,Vec3 destino,double t,Vec3 cam,List<UUID> alvos){
        if(tipo<11||tipo>13||!DesenhoMagia.iniciar(t))return false;
        Vec3 frente=dir.multiply(1,0,1).normalize();if(frente.lengthSqr()<1e-8)frente=new Vec3(0,0,1);
        try{
            if(tipo==11||tipo==12){
                Vec3 delta=destino==null?frente.scale(6):destino.subtract(origem);
                composicao(pose,tipo,t,delta.length(),tipo==12?delta:frente,cam);return true;
            }
            var mc=Minecraft.getInstance();if(mc.level==null)return true;
            var p=mc.level.getPlayerByUUID(dono);if(p==null)return true;
            Vec3 mao=p.getEyePosition().subtract(origem).add(frente.scale(.4));
            float fade=(float)(Math.min(1,t/5)*Math.max(0,Math.min(1,(48-t)/8)));
            DesenhoMagia.lote(pose,6,true,()->{
                DesenhoMagia.selo(pose,cam,new Vec3(0,.04,0),new Vec3(0,1,0),.65,t*.9,VIOLETA,fade*.4f);
                DesenhoMagia.anel(pose,new Vec3(0,.06,0),new Vec3(0,1,0),.76,.79,CLARO,fade*.5f,40);
            });
            DesenhoMagia.lote(pose,12,true,()->{
                int j=0;
                for(UUID id:alvos){
                    net.minecraft.world.entity.Entity entidade=null;
                    for(var e:mc.level.entitiesForRendering())if(e.getUUID().equals(id)){entidade=e;break;}
                    if(entidade==null||!entidade.isAlive())continue;
                    Vec3 fim=entidade.position().add(0,entidade.getBbHeight()*.5,0).subtract(origem);
                    double pulso=.28+.05*Math.sin(t*.5+j);
                    DesenhoMagia.anel(pose,fim,new Vec3(0,1,0),pulso,pulso+.025,CLARO,fade*.6f,24);
                    int fios=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?1:3;
                    for(int fio=0;fio<fios;fio++){
                        Vec3 anterior=mao;
                        for(int i=1;i<=24;i++){
                            double u=i/24d,a=u*15-t*.3+fio*Math.PI*2/3;
                            Vec3 q=mao.lerp(fim,u).add(Math.cos(a)*.065*Math.sin(u*Math.PI),-Math.sin(u*Math.PI)*.25+Math.sin(a)*.065,0);
                            DesenhoMagia.fita(pose,cam,anterior,q,fio==0?.026:.012,(i-1d)/24,u,fio==0?CLARO:VIOLETA,fade*.8f);anterior=q;
                        }
                    }j++;
                }
                if(alvos.isEmpty()&&t<8)for(int i=0;i<5;i++){
                    double a=i*Math.PI*2/5+t*.07;
                    DesenhoMagia.fita(pose,cam,mao,mao.add(Math.cos(a)*1.2,-.3,Math.sin(a)*1.2),.025,0,1,VIOLETA,fade*.6f);
                }
            });
        }finally{DesenhoMagia.terminar();}
        return true;
    }
}
