package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.cauansilva.arcanecontrol.core.vfx.PerfilCid;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class RenderMovesetCid {
    private static final int VIOLETA=0xB76BFF,CLARO=0xE2CFFF;
    public static void selosAtomic(PoseStack pose,double t,Vec3 cam){
        if(t>=105||!DesenhoMagia.iniciar(t))return;
        float a=(float)(Math.min(1,t/20)*Math.max(0,(105-t)/15));
        try{DesenhoMagia.lote(pose,6,true,()->{
            DesenhoMagia.anel(pose,new Vec3(0,.04,0),new Vec3(0,1,0),1.2,1.26,VIOLETA,a*.6f,64);
            DesenhoMagia.anel(pose,new Vec3(0,.05,0),new Vec3(0,1,0),1.8,1.84,CLARO,a*.5f,64);
            for(int j=0;j<8;j++){
                double ang=j*Math.PI/4+t*.008;
                Vec3 u=new Vec3(Math.cos(ang),0,Math.sin(ang));
                Vec3 v=new Vec3(Math.cos(ang+.15),0,Math.sin(ang+.15));
                DesenhoMagia.fita(pose,cam,u.scale(1.3).add(0,.055,0),v.scale(1.68).add(0,.055,0),.028,0,2,CLARO,a*.65f);
            }
        });}finally{DesenhoMagia.terminar();}
    }
    public static boolean desenhar(PoseStack pose,int tipo,UUID dono,Vec3 origem,Vec3 dir,Vec3 destino,double t,Vec3 cam,List<UUID> alvos){
        if(tipo<11||tipo>13||!DesenhoMagia.iniciar(t))return false;
        Vec3 frente=dir.multiply(1,0,1).normalize();if(frente.lengthSqr()<1e-8)frente=new Vec3(0,0,1);
        final Vec3 f=frente,lado=frente.cross(new Vec3(0,1,0));
        try{
            if(tipo==11){
                float alpha=(float)PerfilCid.opacidadeCorte(t);double r=PerfilCid.raioCorte(t);
                for(int camada=0;camada<2;camada++){
                    final int layer=camada;
                    DesenhoMagia.lote(pose,camada==0?0:1,camada!=0,()->{
                        Vec3 anterior=null;
                        for(int i=0;i<=36;i++){
                            double a=-PerfilCid.CORTE_ANGULO+i*(PerfilCid.CORTE_ANGULO*2/36);
                            Vec3 q=f.scale(Math.cos(a)*r).add(lado.scale(Math.sin(a)*r)).add(0,1.15+Math.sin(a)*.35,0);
                            if(anterior!=null)DesenhoMagia.fita(pose,cam,anterior,q,layer==0?.34:.07,i*.15,(i+1)*.15,layer==0?VIOLETA:CLARO,alpha*(layer==0?.95f:.7f));
                            anterior=q;
                        }
                    });
                }
            } else if(tipo==12){
                Vec3 fim=destino==null?f.scale(6):destino.subtract(origem);float fade=(float)Math.max(0,1-t/22);
                DesenhoMagia.lote(pose,5,false,()->{
                    int n=Math.min(4,(int)(fim.length()/1.4));
                    for(int i=1;i<=n;i++){
                        Vec3 centro=fim.scale(i/(double)(n+1));
                        DesenhoMagia.caixa(pose,centro.add(0,1.65,0),.26,.25,.26,VIOLETA,fade*.6f);
                        DesenhoMagia.caixa(pose,centro.add(0,1.05,0),.32,.42,.2,VIOLETA,fade*.6f);
                        DesenhoMagia.caixa(pose,centro.add(0,.55,.16),.4,.4,.09,VIOLETA,fade*.5f);
                        DesenhoMagia.caixa(pose,centro.add(-.18,.35,0),.13,.35,.15,VIOLETA,fade*.5f);
                        DesenhoMagia.caixa(pose,centro.add(.18,.35,0),.13,.35,.15,VIOLETA,fade*.5f);
                    }
                });
                DesenhoMagia.lote(pose,1,true,()->{
                    for(int j=0;j<5;j++)DesenhoMagia.fita(pose,cam,new Vec3(0,.3+j*.3,0),fim.add(0,.3+j*.3,0),.045,0,4,CLARO,fade*.5f);
                });
            } else {
                var mc=Minecraft.getInstance();var p=mc.level.getPlayerByUUID(dono);if(p==null)return true;
                Vec3 mao=p.getEyePosition().subtract(origem).add(f.scale(.4));
                float fade=(float)Math.min(1,Math.max(0,(48-t)/8));
                DesenhoMagia.lote(pose,1,true,()->{
                    int j=0;
                    for(UUID id:alvos){
                        var encontrado=mc.level.entitiesForRendering();net.minecraft.world.entity.Entity entidade=null;
                        for(var e:encontrado)if(e.getUUID().equals(id)){entidade=e;break;}
                        if(entidade==null||!entidade.isAlive())continue;
                        Vec3 fim=entidade.position().add(0,entidade.getBbHeight()*.5,0).subtract(origem);
                        Vec3 anterior=mao;
                        for(int i=1;i<=18;i++){
                            double s=i/18.0;
                            Vec3 q=mao.lerp(fim,s).add(0,-Math.sin(s*Math.PI)*.35+Math.sin(s*18-t*.2+j)*.05,0);
                            DesenhoMagia.fita(pose,cam,anterior,q,.035,s*4,(s+1.0/18)*4,VIOLETA,fade*.85f);anterior=q;
                        }j++;
                    }
                    if(alvos.isEmpty()&&t<8)for(int i=0;i<5;i++){
                        double a=i*Math.PI*2/5+t*.07;
                        DesenhoMagia.fita(pose,cam,mao,mao.add(Math.cos(a)*1.2,-.3,Math.sin(a)*1.2),.035,0,2,VIOLETA,fade*.6f);
                    }
                });
            }
        }finally{DesenhoMagia.terminar();}
        return true;
    }
}
