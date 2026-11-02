package com.cauansilva.arcanecontrol.core.frieren;
import com.cauansilva.arcanecontrol.core.client.render.*;
import com.cauansilva.arcanecontrol.core.vfx.BuracoNegroFrieren;
import com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren;
import com.cauansilva.arcanecontrol.core.vfx.PerfilFrieren;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class RenderMagiasFrieren {
    private static final int RAJADA=0xEAF6FF;
    /** Rajada de vento no chao, linhas subindo pelo corpo e poucas particulas no rastro; a magia de voo do anime nao brilha. */
    private static void impulso(PoseStack pose,Vec3 cam,Vec3 atual,double t){
        // O efeito nasce na posicao do salto: origem fixa no chao, atual acompanha o voo.
        DesenhoMagia.lote(pose,14,true,()->{
            float rajada=(float)MagiasBaseFrieren.alphaRajada(t);double r=MagiasBaseFrieren.raioRajada(t);
            if(rajada>.01)for(int j=0;j<MagiasBaseFrieren.RAJADAS;j++){
                double a=MagiasBaseFrieren.anguloRajada(j);Vec3 d=new Vec3(Math.cos(a),0,Math.sin(a));
                DesenhoMagia.fita(pose,cam,d.scale(r*.55).add(0,.08,0),d.scale(r).add(0,.15,0),.06,0,1,RAJADA,rajada*.8f);
            }
            float subida=(float)MagiasBaseFrieren.alphaSubida(t);
            if(subida>.01)for(int j=0;j<6;j++){
                double a=j*Math.PI/3+.4;Vec3 base=atual.add(Math.cos(a)*.55,-.3,Math.sin(a)*.55);
                DesenhoMagia.fita(pose,cam,base,base.add(0,1.6+j%2*.6,0),.04,0,1,RAJADA,subida*.7f);
            }
        });
        DesenhoMagia.lote(pose,15,true,()->{
            for(int i=0;i<MagiasBaseFrieren.RASTRO;i++){
                float a=(float)MagiasBaseFrieren.alphaRastro(i,t);if(a<.01)continue;
                Vec3 q=atual.add(Math.sin(i*2.1)*.45,-.4-(t-i*3)*.03,Math.cos(i*2.1)*.45);
                DesenhoMagia.anel(pose,q,cam.subtract(q).normalize(),0,.08,0xE4F0FF,a,8);
            }
        });
    }
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
                // The server sends the hit point in world coordinates; the pose is already at the staff tip.
                zoltraak(pose,cam,e.alvo().subtract(e.origem()),t);
            } else if(e.tipo()==22){
                barreira(pose,cam,atual.add(0,1.25,0),p.getViewVector(parcial),t);
            } else if(e.tipo()==26){
                estilhacos(pose,cam,new Vec3(0,1.25,0),e.alvo(),t);
            } else if(e.tipo()==23){
                impulso(pose,cam,atual,t);
            } else if(e.tipo()==24){
                buracoNegro(pose,e,t,cam,atual,p.getEyePosition(parcial),n);
            }
        }finally{DesenhoMagia.terminar();}
    }

    private static Vec3 v(BuracoNegroFrieren.Ponto p){return new Vec3(p.x(),p.y(),p.z());}
    private static BuracoNegroFrieren.Ponto p(Vec3 v){return new BuracoNegroFrieren.Ponto(v.x,v.y,v.z);}
    static Vec3[] base(Vec3 normal){
        Vec3 u=normal.cross(new Vec3(0,1,0));if(u.lengthSqr()<1e-8)u=new Vec3(1,0,0);u=u.normalize();
        return new Vec3[]{u,normal.cross(u).normalize()};
    }
    /** Anel com uv.x de 0 a 8 ao redor, para materiais que amostram o angulo sem emenda. */
    private static void anelAngular(PoseStack pose,Vec3 centro,Vec3 normal,double interno,double externo,int cor,float alpha,int lados){
        Vec3[] b=base(normal);
        for(int i=0;i<lados;i++){
            double a0=i*Math.PI*2/lados,a1=(i+1)*Math.PI*2/lados,u0=i*8.0/lados,u1=(i+1)*8.0/lados;
            Vec3 p0=b[0].scale(Math.cos(a0)).add(b[1].scale(Math.sin(a0))),p1=b[0].scale(Math.cos(a1)).add(b[1].scale(Math.sin(a1)));
            DesenhoMagia.ponto(pose,centro.add(p0.scale(interno)),u0,0,cor,alpha);DesenhoMagia.ponto(pose,centro.add(p1.scale(interno)),u1,0,cor,alpha);
            DesenhoMagia.ponto(pose,centro.add(p1.scale(externo)),u1,1,cor,alpha);DesenhoMagia.ponto(pose,centro.add(p0.scale(externo)),u0,1,cor,alpha);
        }
    }

    private static final int BRASA=0xFF9A3C,BORDA=0xFF7A2A,RACHA=0xFF5C84,VIOLETA=0xB36BFF,CIANO=0xA8E8FF,VENTO=0xCFEFFF;
    private static void buracoNegro(PoseStack pose,EfeitosFrieren.Efeito e,double t,Vec3 cam,Vec3 atual,Vec3 olho,int n){
        boolean reduzido=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get();
        int A=PerfilFrieren.ABERTURA,C=PerfilFrieren.COLAPSO;
        float fade=(float)PerfilFrieren.opacidade(t);double r=BuracoNegroFrieren.nucleo(t),aperto=BuracoNegroFrieren.tensao(t);
        Vec3 frente=e.origem().subtract(olho);frente=new Vec3(frente.x,0,frente.z);frente=frente.lengthSqr()<1e-8?new Vec3(0,0,1):frente.normalize();
        var mao=p(atual.add(0,1.25,0).add(frente.scale(.75)));
        Vec3 tremor=t<C?new Vec3(Math.sin(t*2.3),Math.sin(t*3.1+1)*.6,Math.cos(t*2.7)).scale(aperto*.1):Vec3.ZERO;
        Vec3 pos=v(BuracoNegroFrieren.posicao(mao,t)).add(tremor);
        Vec3 vista=cam.subtract(pos);vista=vista.lengthSqr()<1e-8?new Vec3(0,0,1):vista.normalize();
        Vec3 lateral=vista.cross(new Vec3(0,1,0));lateral=lateral.lengthSqr()<1e-8?new Vec3(1,0,0):lateral.normalize();
        final Vec3 centro=pos,olhar=vista,horizontal=lateral;
        float casca=(float)BuracoNegroFrieren.presencaCasca(t);
        if(r>.02&&t<C+4){
            DesenhoMagia.esfera(pose,cam,centro,r,3,BORDA,fade,false,n);
            DesenhoMagia.lote(pose,16,true,()->anelAngular(pose,centro,olhar,r*.98,BuracoNegroFrieren.halo(t),0xFFFFFF,fade*(1-casca),reduzido?32:64));
            double meia=BuracoNegroFrieren.estria(t);
            if(meia>.05)DesenhoMagia.lote(pose,17,true,()->DesenhoMagia.fita(pose,cam,centro.subtract(horizontal.scale(meia)),centro.add(horizontal.scale(meia)),BuracoNegroFrieren.larguraEstria(t),0,1,0xFFFFFF,fade*(1-casca)));
            DesenhoMagia.lote(pose,9,true,()->{
                for(int k=0;k<3;k++){
                    float a=fade*(float)BuracoNegroFrieren.alphaDistorcao(k,t);double rd=BuracoNegroFrieren.raioDistorcao(k,t);
                    if(a>.01)anelAngular(pose,centro,olhar,rd,rd+Math.max(.08,r*.12),0xFF7A4A,a,48);
                }
            });
        }
        if(BuracoNegroFrieren.emVoo(t))DesenhoMagia.lote(pose,1,true,()->{
            Vec3 anterior=centro;
            for(int k=1;k<=8;k++){
                Vec3 q=v(BuracoNegroFrieren.posicao(mao,t-k*1.2));float a=fade*(1-k/9f);
                DesenhoMagia.fita(pose,cam,anterior,q,.38*(1-k/9.0),k-1,k,BRASA,a);anterior=q;
            }
        });
        if(t>A-6&&t<C)DesenhoMagia.lote(pose,14,true,()->{
            for(int j=0;j<(reduzido?BuracoNegroFrieren.LINHAS/2:BuracoNegroFrieren.LINHAS);j++){
                Vec3 anterior=null;
                for(int i=0;i<=24;i++){
                    double s=i/24.0;Vec3 q=centro.add(v(BuracoNegroFrieren.linhaVento(j,s,t)));
                    float a=fade*(float)BuracoNegroFrieren.alphaVento(j,s,t);
                    if(anterior!=null&&a>.01)DesenhoMagia.fita(pose,cam,anterior,q,.12,s-1/24.0,s,VENTO,a);
                    anterior=q;
                }
            }
        });
        if(t>=A&&t<C)GeometriaCinematica.detritosSingularidade(pose,t,BuracoNegroFrieren.orbitaDetritos(t),fade);
        if(casca>.01){
            double rc=BuracoNegroFrieren.RAIO_CASCA;
            pose.pushPose();pose.translate(centro.x,centro.y,centro.z);pose.scale(1,1.2f,1);
            DesenhoMagia.esfera(pose,cam.subtract(centro),Vec3.ZERO,rc,3,RACHA,fade*casca,false,n);
            pose.popPose();
            // Brilho rosado na borda: a casca parece iluminada por dentro, como no anime.
            DesenhoMagia.lote(pose,15,true,()->DesenhoMagia.anel(pose,centro,olhar,rc*.9,rc*1.4,RACHA,fade*casca*.55f,48));
            double abertas=BuracoNegroFrieren.aberturaRachaduras(t);
            for(int camada=0;camada<2;camada++){
                final double largura=camada==0?.5:.14;final float ganho=camada==0?.55f:1f;
                DesenhoMagia.lote(pose,15,true,()->{
                    for(int j=0;j<BuracoNegroFrieren.RACHADURAS;j++)for(int i=1;i<BuracoNegroFrieren.PONTOS_RACHADURA&&i<=Math.ceil(abertas);i++){
                        Vec3 a=centro.add(v(BuracoNegroFrieren.rachadura(j,i-1)).scale(rc*1.01));
                        Vec3 b=centro.add(v(BuracoNegroFrieren.rachadura(j,i)).scale(rc*1.01));
                        double parte=Math.min(1,abertas-(i-1));b=a.add(b.subtract(a).scale(parte));
                        DesenhoMagia.fita(pose,cam,a,b,largura,0,1,RACHA,fade*casca*ganho);
                    }
                });
            }
        }
        if(t>=C){
            float espinho=fade*(float)BuracoNegroFrieren.alphaEspinho(t),relampago=fade*(float)BuracoNegroFrieren.alphaRelampago(t);
            if(espinho>.01){
                DesenhoMagia.lote(pose,18,true,()->{
                    for(int j=0;j<BuracoNegroFrieren.ESPINHOS;j++){
                        Vec3 d=v(BuracoNegroFrieren.direcaoEspinho(j)).normalize();
                        DesenhoMagia.fita(pose,cam,centro,centro.add(d.scale(BuracoNegroFrieren.comprimentoEspinho(j,t))),1.8,0,1,VIOLETA,espinho);
                    }
                });
                double clarao=Math.min(10,(t-C)*1.4+1);
                DesenhoMagia.lote(pose,15,true,()->DesenhoMagia.anel(pose,centro,olhar,0,clarao,0xF4E8FF,espinho,32));
            }
            if(relampago>.01){
                double alcance=BuracoNegroFrieren.alcanceRelampago(t);
                for(int camada=0;camada<2;camada++){
                    final double largura=camada==0?.6:.18;final float ganho=camada==0?.4f:1f;
                    DesenhoMagia.lote(pose,15,true,()->{
                        for(int j=0;j<(reduzido?BuracoNegroFrieren.RELAMPAGOS/2:BuracoNegroFrieren.RELAMPAGOS);j++)for(int i=1;i<=Math.ceil(alcance);i++){
                            Vec3 a=centro.add(v(BuracoNegroFrieren.relampago(j,i-1))),b=centro.add(v(BuracoNegroFrieren.relampago(j,i)));
                            b=a.add(b.subtract(a).scale(Math.min(1,alcance-(i-1))));
                            DesenhoMagia.fita(pose,cam,a,b,largura,0,1,CIANO,relampago*ganho);
                        }
                    });
                }
            }
            DesenhoMagia.lote(pose,15,true,()->{
                for(int i=0;i<(reduzido?BuracoNegroFrieren.FAISCAS/2:BuracoNegroFrieren.FAISCAS);i++){
                    float a=fade*(float)BuracoNegroFrieren.alphaFaisca(i,t);if(a<.01)continue;
                    DesenhoMagia.anel(pose,centro.add(v(BuracoNegroFrieren.faisca(i,t))),olhar,0,.26,0xEAF8FF,a,10);
                }
            });
            DesenhoMagia.lote(pose,8,false,()->{
                for(int i=0;i<(reduzido?BuracoNegroFrieren.ESTILHACOS/2:BuracoNegroFrieren.ESTILHACOS);i++){
                    float a=fade*(float)BuracoNegroFrieren.alphaEstilhaco(i,t);if(a<.01)continue;
                    Vec3 q=centro.add(v(BuracoNegroFrieren.estilhaco(i,t)));double tam=.12+(i%4)*.05;
                    pose.pushPose();pose.translate(q.x,q.y,q.z);
                    pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees((float)(t*7+i*41)));pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float)(t*5+i*29)));
                    DesenhoMagia.caixa(pose,Vec3.ZERO,tam,tam*.25,tam*.7,0xE6F2FF,a);pose.popPose();
                }
            });
        }
    }

    private static final int TRACO=0xE8FBFF,HALO_FEIXE=0xC9D4FF,FAVO=0x7FF3FF,VIDRO=0x5FD8FF;
    private static Vec3 plano(Vec3 centro,Vec3[] b,double x,double y){return centro.add(b[0].scale(x)).add(b[1].scale(y));}
    private static Vec3 plano(Vec3 centro,Vec3[] b,MagiasBaseFrieren.P2 q,double escala,double giro){
        double c=Math.cos(giro),s=Math.sin(giro);return plano(centro,b,(q.x()*c-q.y()*s)*escala,(q.x()*s+q.y()*c)*escala);
    }

    /** Feixe branco com circulo de flor de seis petalas na ponta do cajado; o dano ja ocorreu no servidor. */
    private static void zoltraak(PoseStack pose,Vec3 cam,Vec3 alvo,double t){
        Vec3 fim=alvo,dir=fim.lengthSqr()<1e-8?new Vec3(0,0,1):fim.normalize();
        double largura=MagiasBaseFrieren.larguraFeixe(t),comprimento=fim.length();
        Vec3[] b=base(dir);
        if(largura>.01){
            DesenhoMagia.lote(pose,1,true,()->{
                DesenhoMagia.fita(pose,cam,Vec3.ZERO,fim,.95*largura,0,8,HALO_FEIXE,.55f*(float)largura);
                DesenhoMagia.fita(pose,cam,Vec3.ZERO,fim,.32*largura,0,8,0xFFFFFF,(float)Math.min(1,largura*1.6));
            });
            DesenhoMagia.lote(pose,15,true,()->{
                for(int j=0;j<3;j++){
                    double a=j*Math.PI*2/3+t*.25;Vec3 off=b[0].scale(Math.cos(a)*.24*largura).add(b[1].scale(Math.sin(a)*.24*largura));
                    DesenhoMagia.fita(pose,cam,off,fim.add(off),.02,0,1,TRACO,.5f*(float)largura);
                }
            });
        }
        DesenhoMagia.lote(pose,15,true,()->{
            for(int j=0;j<24;j++){
                float a=(float)MagiasBaseFrieren.alphaFaisca(j,t);if(a<.01)continue;
                double ang=MagiasBaseFrieren.anguloFaisca(j),off=MagiasBaseFrieren.afastamentoFaisca(j,t);
                Vec3 q=dir.scale(comprimento*MagiasBaseFrieren.posicaoFaisca(j)).add(b[0].scale(Math.cos(ang)*off)).add(b[1].scale(Math.sin(ang)*off));
                DesenhoMagia.fita(pose,cam,q,q.add(dir.scale(.28)),.035,0,1,0xFFFFFF,a);
            }
            float impacto=(float)MagiasBaseFrieren.impacto(t);
            if(impacto>.01){
                double r=.2+(1-impacto)*1.6;
                DesenhoMagia.anel(pose,fim,cam.subtract(fim).normalize(),r*.75,r,TRACO,impacto,32);
                if(t<8)GeometriaCinematica.raios(pose,cam,fim,1.8,t,0xFFFFFF,impacto*.6f);
            }
        });
        float alpha=(float)MagiasBaseFrieren.alphaCirculo(t);
        if(alpha>.01)DesenhoMagia.lote(pose,15,true,()->circuloZoltraak(pose,cam,dir.scale(.6),dir,.85*MagiasBaseFrieren.escalaCirculo(t),MagiasBaseFrieren.giroCirculo(t),alpha));
    }

    static void circuloZoltraak(PoseStack pose,Vec3 cam,Vec3 centro,Vec3 normal,double raio,double giro,float alpha){
        Vec3[] b=base(normal);double traco=Math.max(.008,raio*.014);
        DesenhoMagia.anel(pose,centro,normal,raio*.97,raio,TRACO,alpha,64);
        DesenhoMagia.anel(pose,centro,normal,raio*.86,raio*.885,TRACO,alpha*.9f,64);
        DesenhoMagia.anel(pose,centro,normal,raio*.8,raio*.815,TRACO,alpha*.8f,64);
        // Faixa de runas entre os aros: tracos e pequenos circulos alternados.
        for(int m=0;m<36;m++){
            double a=giro+m*Math.PI/18;
            if(m%3==2){DesenhoMagia.anel(pose,plano(centro,b,Math.cos(a)*raio*.927,Math.sin(a)*raio*.927),normal,raio*.012,raio*.026,TRACO,alpha*.85f,10);continue;}
            double a0=a-.035,a1=a+.035;
            DesenhoMagia.fita(pose,cam,plano(centro,b,Math.cos(a0)*raio*.927,Math.sin(a0)*raio*.927),plano(centro,b,Math.cos(a1)*raio*.927,Math.sin(a1)*raio*.927),traco,0,1,TRACO,alpha*.85f);
        }
        for(int k=0;k<MagiasBaseFrieren.PETALAS;k++){
            Vec3 anterior=null;
            for(int i=0;i<=24;i++){Vec3 q=plano(centro,b,MagiasBaseFrieren.petala(k,i,24),raio,giro);if(anterior!=null)DesenhoMagia.fita(pose,cam,anterior,q,traco,0,1,TRACO,alpha);anterior=q;}
            Vec3 olho=plano(centro,b,MagiasBaseFrieren.olhoPetala(k),raio,giro);
            DesenhoMagia.anel(pose,olho,normal,raio*.085,raio*.1,TRACO,alpha,24);
            DesenhoMagia.anel(pose,olho,normal,raio*.04,raio*.052,TRACO,alpha*.8f,16);
            for(int v=0;v<3;v++)DesenhoMagia.fita(pose,cam,plano(centro,b,MagiasBaseFrieren.triangulo(k,v),raio,giro),plano(centro,b,MagiasBaseFrieren.triangulo(k,(v+1)%3),raio,giro),traco,0,1,TRACO,alpha*.8f);
        }
        DesenhoMagia.anel(pose,centro,normal,raio*.1,raio*.115,TRACO,alpha,24);
        DesenhoMagia.anel(pose,centro,normal,raio*.04,raio*.05,TRACO,alpha,16);
    }

    /** Favo de sete hexagonos ciano a frente de quem conjura, seguindo o olhar. */
    private static void barreira(PoseStack pose,Vec3 cam,Vec3 peito,Vec3 olhar,double t){
        Vec3[] b=base(olhar);Vec3 centro=peito.add(olhar.scale(1.4));
        float a=(float)MagiasBaseFrieren.alphaBarreira(t);if(a<.01)return;
        DesenhoMagia.lote(pose,8,true,()->{
            for(int c=0;c<MagiasBaseFrieren.CELULAS;c++){
                double esc=MagiasBaseFrieren.escalaCelula(c,t);if(esc<.01)continue;
                Vec3 o=plano(centro,b,MagiasBaseFrieren.celula(c),1,0);
                for(int v=0;v<6;v++){
                    Vec3 p0=plano(centro,b,MagiasBaseFrieren.vertice(c,v,esc),1,0),p1=plano(centro,b,MagiasBaseFrieren.vertice(c,(v+1)%6,esc),1,0);
                    DesenhoMagia.ponto(pose,o,0,0,VIDRO,a*.14f);DesenhoMagia.ponto(pose,p0,1,0,VIDRO,a*.14f);DesenhoMagia.ponto(pose,p1,1,1,VIDRO,a*.14f);DesenhoMagia.ponto(pose,p1,0,1,VIDRO,a*.14f);
                }
            }
        });
        for(int camada=0;camada<2;camada++){
            final double largura=camada==0?.09:.028;final float ganho=camada==0?.3f:1f;
            DesenhoMagia.lote(pose,15,true,()->{
                for(int c=0;c<MagiasBaseFrieren.CELULAS;c++){
                    double esc=MagiasBaseFrieren.escalaCelula(c,t);if(esc<.01)continue;
                    float luz=a*ganho*(float)(MagiasBaseFrieren.pulsoCelula(c,t)*esc);
                    for(int v=0;v<6;v++)DesenhoMagia.fita(pose,cam,plano(centro,b,MagiasBaseFrieren.vertice(c,v,esc),1,0),plano(centro,b,MagiasBaseFrieren.vertice(c,(v+1)%6,esc),1,0),largura,0,1,FAVO,luz);
                }
            });
        }
    }

    /** Ao esgotar a reserva, cada celula se parte em seis cacos que voam para fora e somem. */
    private static void estilhacos(PoseStack pose,Vec3 cam,Vec3 peito,Vec3 olhar,double t){
        Vec3 normal=olhar.lengthSqr()<1e-8?new Vec3(0,0,1):olhar.normalize();Vec3[] b=base(normal);Vec3 centro=peito.add(normal.scale(1.4));
        float a=(float)MagiasBaseFrieren.alphaCaco(t);if(a<.01)return;
        for(int camada=0;camada<2;camada++){
            final boolean borda=camada==1;
            DesenhoMagia.lote(pose,borda?15:8,true,()->{
                for(int c=0;c<MagiasBaseFrieren.CELULAS;c++)for(int v=0;v<6;v++){
                    var o=MagiasBaseFrieren.celula(c);var p0=MagiasBaseFrieren.vertice(c,v,1);var p1=MagiasBaseFrieren.vertice(c,(v+1)%6,1);
                    double cx=(o.x()+p0.x()+p1.x())/3,cy=(o.y()+p0.y()+p1.y())/3,g=MagiasBaseFrieren.giroCaco(c,v,t);
                    var d=MagiasBaseFrieren.deslocamentoCaco(c,v,t);Vec3 frente=normal.scale(MagiasBaseFrieren.frenteCaco(c,v,t));
                    Vec3[] q=new Vec3[3];MagiasBaseFrieren.P2[] pts={o,p0,p1};
                    for(int k=0;k<3;k++){
                        double x=pts[k].x()-cx,y=pts[k].y()-cy,rx=x*Math.cos(g)-y*Math.sin(g),ry=x*Math.sin(g)+y*Math.cos(g);
                        q[k]=plano(centro,b,cx+d.x()+rx*.92,cy+d.y()+ry*.92).add(frente);
                    }
                    if(borda)for(int k=0;k<3;k++)DesenhoMagia.fita(pose,cam,q[k],q[(k+1)%3],.02,0,1,FAVO,a);
                    else{DesenhoMagia.ponto(pose,q[0],0,0,VIDRO,a*.3f);DesenhoMagia.ponto(pose,q[1],1,0,VIDRO,a*.3f);DesenhoMagia.ponto(pose,q[2],1,1,VIDRO,a*.3f);DesenhoMagia.ponto(pose,q[2],0,1,VIDRO,a*.3f);}
                }
            });
        }
    }
}
