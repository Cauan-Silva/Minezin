package com.cauansilva.arcanecontrol.core.frieren;
import com.cauansilva.arcanecontrol.core.client.render.*;
import com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo;
import com.cauansilva.arcanecontrol.core.vfx.PerfilFrieren;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class RenderMagiasFrieren {
    private static final int OURO=0xEACF95,CLARO=0xD6EBFF;
    public static void desenhar(PoseStack pose,EfeitosFrieren.Efeito e,double t,Vec3 cam,float parcial){
        if(!DesenhoMagia.iniciar(t)){
            if(e.tipo()==24)RenderizadorModelos3D.desenharEsfera3D(pose,2.5f,0,0,0,255,20);
            return;
        }
        var mc=Minecraft.getInstance();var p=mc.level.getPlayerByUUID(e.jogador());if(p==null){DesenhoMagia.terminar();return;}
        Vec3 atual=p.getPosition(parcial).subtract(e.origem());
        int n=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?14:24;
        try{
            if(e.tipo()==21){
                Vec3 fim=e.alvo().subtract(e.origem()),dir=fim.normalize(),lado=dir.cross(new Vec3(0,1,0)).normalize();
                Vec3 mao=lado.scale(.18).add(0,-.15,0);float a=(float)Math.max(0,1-t/30);
                DesenhoMagia.lote(pose,1,true,()->{
                    DesenhoMagia.fita(pose,cam,mao,fim,.24,0,8,CLARO,a*.4f);
                    if(t<8){DesenhoMagia.anel(pose,fim,dir,.15+t*.14,.23+t*.14,OURO,a,48);GeometriaCinematica.raios(pose,cam,fim,1.8,t,CLARO,a*.55f);}
                    DesenhoMagia.fita(pose,cam,mao,fim,.045,0,8,0xFFFFFF,a);
                });
                DesenhoMagia.lote(pose,6,true,()->{
                    DesenhoMagia.anel(pose,dir.scale(.9),dir,.7,.74,OURO,a,48);
                    DesenhoMagia.selo(pose,cam,dir.scale(.9),dir,.7,t,OURO,a);
                    DesenhoMagia.selo(pose,cam,dir.scale(1.35),dir,.42,-t,CLARO,a*.6f);
                });
            } else if(e.tipo()==22){
                float a=(float)Math.min(1,Math.min(t/6,(80-t)/10));
                DesenhoMagia.esfera(pose,cam,atual.add(0,1,0),1.9,2,CLARO,a*.75f,true,n);
                DesenhoMagia.lote(pose,6,true,()->{
                    DesenhoMagia.selo(pose,cam,atual.add(0,.05,0),new Vec3(0,1,0),1.65,t,OURO,a*.7f);
                    DesenhoMagia.anel(pose,atual.add(0,1,0),cam.subtract(atual).normalize(),1.86,1.9,CLARO,a*.4f,64);
                });
            } else if(e.tipo()==23){
                float a=(float)Math.min(1,Math.max(0,(100-t)/20));
                DesenhoMagia.lote(pose,6,true,()->{
                    DesenhoMagia.anel(pose,atual.add(0,.03,0),new Vec3(0,1,0),.7,.82,CLARO,a*.6f,48);
                    DesenhoMagia.anel(pose,atual.add(0,.03,0),new Vec3(0,1,0),1.05,1.1,OURO,a*.5f,48);
                    for(int j=0;j<8;j++){
                        double ang=j*Math.PI/4+t*.025;
                        Vec3 u=new Vec3(Math.cos(ang),0,Math.sin(ang));
                        DesenhoMagia.fita(pose,cam,atual.add(u.scale(.35)),atual.add(u.scale(.65)),.035,0,2,OURO,a*.65f);
                    }
                });
            } else if(e.tipo()==24){
                double carga=PerfilVfxGojo.suave(t/PerfilFrieren.ABERTURA);
                double r=PerfilFrieren.nucleo(t);float fade=(float)PerfilFrieren.opacidade(t);
                if(r>.02)DesenhoMagia.esfera(pose,cam,Vec3.ZERO,r,3,0x8F9CCF,fade,false,n);
                Vec3 vista=cam.lengthSqr()<1e-8?new Vec3(0,0,1):cam.normalize();
                if(r>.02){
                    // Lensed light bends around the opaque core; a different material from the hot disk.
                    DesenhoMagia.lote(pose,9,true,()->{
                        DesenhoMagia.anel(pose,Vec3.ZERO,vista,r*1.12,r*2.45,0xA8BBF2,fade*.65f,96);
                    });
                    Vec3 lateral=vista.cross(new Vec3(0,1,0)).normalize();if(lateral.lengthSqr()<1e-8)lateral=new Vec3(1,0,0);
                    final Vec3 x=lateral,y=vista.cross(lateral).normalize();
                    DesenhoMagia.lote(pose,1,true,()->{
                        for(int j=0;j<3;j++)for(int i=0;i<48;i++){
                            double a=i*Math.PI/24,b=(i+1)*Math.PI/24,raio=r*(1.06+j*.035);
                            Vec3 u=x.scale(Math.cos(a)*raio*1.07).add(y.scale(Math.sin(a)*raio));
                            Vec3 v=x.scale(Math.cos(b)*raio*1.07).add(y.scale(Math.sin(b)*raio));
                            DesenhoMagia.fita(pose,cam,u,v,.026,0,1,j==0?CLARO:OURO,fade*(j==0?.85f:.3f));
                        }
                    });
                    if(t<PerfilFrieren.COLAPSO)GeometriaCinematica.detritosSingularidade(pose,t,r,fade);
                }
                Vec3 normal=new Vec3(Math.sin(t*.008)*.3,1,Math.cos(t*.008)*.2).normalize();
                DesenhoMagia.lote(pose,4,true,()->{
                    if(r>.02){
                        DesenhoMagia.anel(pose,Vec3.ZERO,normal,r*1.3,r*2.8,OURO,fade*.85f,96);
                        DesenhoMagia.anel(pose,Vec3.ZERO,cam.normalize(),r*1.03,r*1.11,CLARO,fade*.7f,80);
                    }
                    if(t>=PerfilFrieren.COLAPSO){double onda=(t-PerfilFrieren.COLAPSO)*.5;DesenhoMagia.anel(pose,Vec3.ZERO,new Vec3(0,1,0),onda,onda+.7,OURO,fade*.7f,80);}
                });
                if(t<PerfilFrieren.ABERTURA){
                    Vec3 mao=atual.add(0,1.2,0),normalMao=e.origem().subtract(p.getEyePosition(parcial)).normalize();
                    DesenhoMagia.lote(pose,6,true,()->{
                        DesenhoMagia.selo(pose,cam,mao,normalMao,1.4,t,OURO,fade*.8f);
                        DesenhoMagia.selo(pose,cam,Vec3.ZERO,new Vec3(0,1,0),2+3*carga,-t,CLARO,fade*.5f);
                    });
                }
                if(t>=PerfilFrieren.COLAPSO&&t<PerfilFrieren.COLAPSO+30){
                    double onda=(t-PerfilFrieren.COLAPSO)*.5;
                    DesenhoMagia.esfera(pose,cam,Vec3.ZERO,onda,2,CLARO,fade*.3f,true,n);
                }
                if(t<PerfilFrieren.COLAPSO)DesenhoMagia.lote(pose,1,true,()->{
                    int caminhos=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get()?4:8;
                    for(int j=0;j<caminhos;j++){
                        Vec3 anterior=null;
                        for(int i=0;i<=24;i++){
                            double s=i/24.0,a=j*Math.PI*2/caminhos+s*6-t*.04,raio=(1-s)*(6+8*carga)+r;
                            Vec3 q=new Vec3(Math.cos(a)*raio,Math.sin(s*Math.PI+j)*1.5*(1-s),Math.sin(a)*raio);
                            if(anterior!=null)DesenhoMagia.fita(pose,cam,anterior,q,.035,s*5,(s+1.0/24)*5,CLARO,fade*.55f);
                            anterior=q;
                        }
                    }
                });
            }
        }finally{DesenhoMagia.terminar();}
    }
}
