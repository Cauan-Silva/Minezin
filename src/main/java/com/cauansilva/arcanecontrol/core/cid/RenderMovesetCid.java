package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.cauansilva.arcanecontrol.core.client.render.GeometriaCinematica;
import com.cauansilva.arcanecontrol.core.vfx.PerfilCid;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class RenderMovesetCid {
    private static final int VIOLETA=0xB76BFF,CLARO=0xE2CFFF;
    public static void selosAtomic(PoseStack pose,double t,Vec3 cam){
        if(t<0||t>=130||!DesenhoMagia.iniciar(t))return;
        try{
            if(t<90){
                float alpha=(float)(Math.min(1,t/20)*Math.min(1,(100-t)/10));
                DesenhoMagia.lote(pose,6,true,()->{
                    for(int j=0;j<3;j++){
                        double r=1.2+j*.65;
                        DesenhoMagia.anel(pose,new Vec3(0,.04+j*.01,0),new Vec3(0,1,0),r,r+.045,j==1?CLARO:VIOLETA,alpha*.6f,64);
                        DesenhoMagia.selo(pose,cam,new Vec3(0,.06+j*.01,0),new Vec3(0,1,0),r,t*(j%2==0?1:-1),VIOLETA,alpha*.35f);
                    }
                    // Negative pressure: disconnected streaks collapse toward the blade before detonation.
                    for(int i=0;i<16;i++){
                        double a=i*2.399963+t*.012,r=2.4+((i*7+t*.15)%5);
                        Vec3 v=new Vec3(Math.cos(a),0,Math.sin(a));
                        DesenhoMagia.fita(pose,cam,v.scale(r).add(0,.12,0),v.scale(r-.7).add(0,.15,0),.025,0,1,CLARO,alpha*.45f);
                    }
                });
                if(t>60)DesenhoMagia.lote(pose,1,true,()->GeometriaCinematica.raios(pose,cam,new Vec3(0,1.2,0),.7+(t-60)*.045,t,VIOLETA,alpha*.5f));
            }else{
                double idade=t-90;float alpha=(float)Math.pow(Math.max(0,1-idade/40),1.5);
                DesenhoMagia.lote(pose,1,true,()->{
                    for(int j=0;j<3;j++){
                        double r=Math.max(0,idade-j*4)*.7;
                        if(r>0)DesenhoMagia.anel(pose,new Vec3(0,.15+j*.45,0),new Vec3(0,1,0),Math.max(0,r-.18),r,j==1?CLARO:VIOLETA,alpha*.6f,72);
                    }
                    // A crowned blast column, not a single uniformly bright cylinder.
                    for(int j=0;j<8;j++){
                        double a=j*Math.PI/4;Vec3 base=new Vec3(Math.cos(a)*2,.3,Math.sin(a)*2);
                        Vec3 topo=new Vec3(Math.cos(a)*5,14+Math.sin(j*2)*3,Math.sin(a)*5);
                        DesenhoMagia.fita(pose,cam,base,topo,.10,0,5,CLARO,alpha*.5f);
                    }
                });
            }
        }finally{DesenhoMagia.terminar();}
    }

    public static boolean desenhar(PoseStack pose,int tipo,UUID dono,Vec3 origem,Vec3 dir,Vec3 destino,double t,Vec3 cam,List<UUID> alvos){
        if(tipo<11||tipo>13||!DesenhoMagia.iniciar(t))return false;
        Vec3 frente=dir.multiply(1,0,1).normalize();if(frente.lengthSqr()<1e-8)frente=new Vec3(0,0,1);
        final Vec3 f=frente,lado=frente.cross(new Vec3(0,1,0));
        try{
            if(tipo==11){
                float alpha=(float)PerfilCid.opacidadeCorte(t);double r=PerfilCid.raioCorte(t);
                for(int camada=0;camada<3;camada++){
                    final int layer=camada;
                    DesenhoMagia.lote(pose,camada==0?0:1,camada!=0,()->{
                        Vec3 anterior=null;
                        for(int i=0;i<=36;i++){
                            double a=-PerfilCid.CORTE_ANGULO+i*(PerfilCid.CORTE_ANGULO*2/36);
                            Vec3 q=f.scale(Math.cos(a)*r).add(lado.scale(Math.sin(a)*r)).add(0,1.15+Math.sin(a)*.35,0);
                            if(anterior!=null)DesenhoMagia.fita(pose,cam,anterior,q,layer==0?.34:layer==1?.075:.022,i*.15,(i+1)*.15,layer==0?VIOLETA:layer==1?CLARO:0xFFFFFF,alpha*(layer==0?.95f:.7f));
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
