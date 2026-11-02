package com.cauansilva.arcanecontrol.core.frieren;
import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar;
import com.cauansilva.arcanecontrol.core.vfx.FormaFrieren;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/** Forma desperta da Frieren (kit 3); pose ja transladada para p.a(), cam relativa a p.a(). */
public final class RenderDespertarFrieren {
    private static final int MANA=0xBFD9FF,MANA_CLARO=0xE4F0FF,VIOLETA=0xC56BFF,FAVO=0x7FF3FF,VIDRO=0x5FD8FF,TRACO=0xE8FBFF;
    private static final Vec3 CIMA=new Vec3(0,1,0);
    private static final java.util.List<FormaFrieren.Painel> PAINEIS=FormaFrieren.paineis();
    private static Vec3 v(FormaFrieren.P p){return new Vec3(p.x(),p.y(),p.z());}

    /** Retorna true quando o efeito pertence a Frieren e ja foi desenhado. */
    public static boolean desenhar(PoseStack s,Vec3 cam,PacoteEfeitoDespertar p,double t,float fade){
        boolean reduzido=com.cauansilva.arcanecontrol.ConfigCliente.VFX_REDUZIDOS.get();
        switch(p.tipo()){
            case 200->liberarMana(s,cam,p,t,reduzido);
            case 37->{} // Os vinte raios recebem extremidades reais pelos pacotes 113.
            case 113->zoltraakDesperto(s,cam,p.b().subtract(p.a()),t);
            case 38->julgamentoAviso(s,cam,t);
            case 115->julgamentoRaio(s,cam,p.b().subtract(p.a()),t+FormaFrieren.QUEDA);
            case 39->escudo(s,cam,t);
            case 30->gaiola(s,cam,t,reduzido);
            default->{return false;}
        }
        return true;
    }

    /** Coluna translucida de mana azul-palida subindo da Frieren, com estrias e particulas. */
    private static void liberarMana(PoseStack s,Vec3 cam,PacoteEfeitoDespertar p,double t,boolean reduzido){
        var jogador=Minecraft.getInstance().level.getPlayerByUUID(p.dono());if(jogador==null||!jogador.isAlive())return;
        double idade=900-p.ticks()+t,restante=p.ticks()-t;
        Vec3 corpo=jogador.position().subtract(p.a());
        double r=FormaFrieren.raioColuna(idade),altura=FormaFrieren.alturaColuna(idade);float a=(float)FormaFrieren.alphaColuna(idade,restante);
        if(a<.01)return;
        // Coluna voltada para a camera: corpo macio e bordas claras, como a mana revelada no anime.
        DesenhoMagia.lote(s,19,true,()->{
            DesenhoMagia.fita(s,cam,corpo.add(0,-.2,0),corpo.add(0,altura,0),r,0,1,MANA,a*.8f);
            DesenhoMagia.fita(s,cam,corpo.add(0,-.2,0),corpo.add(0,altura*.8,0),r*.5,0,1,MANA_CLARO,a*.6f);
        });
        DesenhoMagia.lote(s,15,true,()->{
            for(int j=0;j<FormaFrieren.ESTRIAS;j++){
                double ang=FormaFrieren.anguloEstria(j,idade);Vec3 base=corpo.add(Math.cos(ang)*r*.9,0,Math.sin(ang)*r*.9);
                DesenhoMagia.fita(s,cam,base,base.add(0,altura*.85,0),.05,0,1,MANA_CLARO,a*(float)FormaFrieren.alphaEstria(j,idade)*.6f);
            }
            for(int i=0;i<(reduzido?FormaFrieren.PARTICULAS/2:FormaFrieren.PARTICULAS);i++){
                float pa=a*(float)FormaFrieren.alphaParticula(i,idade);if(pa<.01)continue;
                Vec3 q=corpo.add(v(FormaFrieren.particula(i,idade)));
                DesenhoMagia.anel(s,q,cam.subtract(q).normalize(),0,.09,MANA_CLARO,pa,8);
            }
        });
    }

    /** Raio dos Vinte Zoltraak: o mesmo feixe branco e circulo de flor, em escala menor. */
    private static void zoltraakDesperto(PoseStack s,Vec3 cam,Vec3 fim,double t){
        double largura=1-com.cauansilva.arcanecontrol.core.vfx.PerfilVfxGojo.suave(t/12);if(largura<.01)return;
        Vec3 dir=fim.lengthSqr()<1e-8?new Vec3(0,0,1):fim.normalize();
        DesenhoMagia.lote(s,1,true,()->{
            DesenhoMagia.fita(s,cam,Vec3.ZERO,fim,.6*largura,0,8,0xC9D4FF,.5f*(float)largura);
            DesenhoMagia.fita(s,cam,Vec3.ZERO,fim,.2*largura,0,8,0xFFFFFF,(float)Math.min(1,largura*1.5));
        });
        DesenhoMagia.lote(s,15,true,()->{
            RenderMagiasFrieren.circuloZoltraak(s,cam,dir.scale(.3),dir,.5,t*.03,(float)largura);
            double r=.2+(1-largura)*1.2;DesenhoMagia.anel(s,fim,cam.subtract(fim).normalize(),r*.75,r,TRACO,(float)largura,24);
        });
    }

    /** Aviso: circulo no chao com anel que fecha no impacto e circulo violeta carregando no ceu. */
    private static void julgamentoAviso(PoseStack s,Vec3 cam,double t){
        float a=(float)FormaFrieren.alphaAviso(t);if(a<.01)return;
        double carga=FormaFrieren.cargaCeu(t),fecha=FormaFrieren.fechamentoAviso(t);
        DesenhoMagia.lote(s,15,true,()->{
            RenderMagiasFrieren.circuloZoltraak(s,cam,new Vec3(0,.08,0),CIMA,4,t*.02,a*.85f);
            DesenhoMagia.anel(s,new Vec3(0,.1,0),CIMA,4*fecha,4*fecha+.12,0xFFFFFF,a,64);
            RenderMagiasFrieren.circuloZoltraak(s,cam,new Vec3(0,30,0),CIMA,3*(.6+.4*carga),-t*.04,a*(float)carga);
        });
        DesenhoMagia.lote(s,15,true,()->{
            for(int k=0;k<6;k++){
                if(((int)t+k)%3!=0)continue;
                double ang=k*Math.PI/3+t*.2;Vec3 c=new Vec3(Math.cos(ang)*2.6*carga,30,Math.sin(ang)*2.6*carga);
                DesenhoMagia.fita(s,cam,c,c.add(Math.cos(ang+1)*.8,-.6,Math.sin(ang+1)*.8),.08,0,1,VIOLETA,a*(float)carga);
            }
        });
    }

    /** Relampagos violeta do Judradjim descendo do ceu e se espalhando pelo chao no impacto. */
    private static void julgamentoRaio(PoseStack s,Vec3 cam,Vec3 chao,double t){
        float a=(float)FormaFrieren.alphaRaio(t);if(a<.01)return;
        double h=-chao.y,var=FormaFrieren.variacaoRaio(t),alcance=FormaFrieren.alcanceRamo(t);
        for(int camada=0;camada<2;camada++){
            final double largura=camada==0?.75:.16;final int cor=camada==0?VIOLETA:0xFFFFFF;final float ganho=camada==0?.45f:1f;
            DesenhoMagia.lote(s,15,true,()->{
                for(int j=0;j<FormaFrieren.RAIOS_JULGAMENTO;j++){
                    Vec3 anterior=null;
                    for(int i=0;i<FormaFrieren.PONTOS_RAIO;i++){Vec3 q=chao.add(v(FormaFrieren.raio(j,i,h,var+j)));if(anterior!=null)DesenhoMagia.fita(s,cam,anterior,q,largura*(j==0?1.4:1),0,1,cor,a*ganho);anterior=q;}
                }
                for(int k=0;k<FormaFrieren.RAMOS;k++)for(int i=1;i<=Math.ceil(alcance);i++){
                    Vec3 p0=chao.add(v(FormaFrieren.ramo(k,i-1,t))),p1=chao.add(v(FormaFrieren.ramo(k,i,t)));
                    p1=p0.add(p1.subtract(p0).scale(Math.min(1,alcance-(i-1))));
                    DesenhoMagia.fita(s,cam,p0,p1,largura*.6,0,1,cor,a*ganho);
                }
            });
        }
        float clarao=(float)FormaFrieren.clarao(t);
        if(clarao>.01)DesenhoMagia.lote(s,15,true,()->DesenhoMagia.anel(s,chao.add(0,.6,0),cam.subtract(chao).normalize(),0,5*(1.2-clarao),0xF2DDFF,clarao,32));
    }

    private static final int POEIRA=0xD8CDB8,DETRITO=0x6E665A;
    /** Tornado de poeira em faixas com cel-shading, estreito embaixo e largo em cima, como o Waldgose. */
    private static void gaiola(PoseStack s,Vec3 cam,double t,boolean reduzido){
        float a=(float)com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.alphaTornado(t);if(a<.01)return;
        double espalha=com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.espalhamento(t);int passos=reduzido?14:24;
        DesenhoMagia.lote(s,20,false,()->{
            for(int k=0;k<com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.FAIXAS;k++){
                Vec3 anterior=null;
                for(int i=0;i<=passos;i++){
                    var q=com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.faixaAngAltura(k,i/(double)passos,t);
                    double r=com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.raioTornado(q.y())*espalha;
                    Vec3 p=new Vec3(Math.cos(q.x())*r,q.y(),Math.sin(q.x())*r);
                    if(anterior!=null)DesenhoMagia.fita(s,cam,anterior,p,.6+q.y()*.06,i*.25,(i+1)*.25,POEIRA,a*.85f);
                    anterior=p;
                }
            }
            DesenhoMagia.anel(s,new Vec3(0,.1,0),CIMA,1.6*espalha,5.5*espalha,POEIRA,a*.5f,48);
        });
        DesenhoMagia.lote(s,8,false,()->{
            for(int i=0;i<com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.DETRITOS;i++){
                double y=com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.alturaDetrito(i,t),ang=com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.anguloDetrito(i,t);
                double r=com.cauansilva.arcanecontrol.core.vfx.MagiasBaseFrieren.raioTornado(y)*espalha*1.08;
                DesenhoMagia.caixa(s,new Vec3(Math.cos(ang)*r,y,Math.sin(ang)*r),.08+(i%3)*.04,.06,.07,DETRITO,a);
            }
        });
    }

    /** Cupula de paineis hexagonais ciano, a mesma magia defensiva da Barreira em escala de grupo. */
    private static void escudo(PoseStack s,Vec3 cam,double t){
        for(int camada=0;camada<2;camada++){
            final boolean borda=camada==1;
            DesenhoMagia.lote(s,borda?15:8,true,()->{
                for(var painel:PAINEIS){
                    double esc=FormaFrieren.escalaPainel(painel,t);if(esc<.01)continue;
                    Vec3 c=v(painel.centro()),n=v(painel.normal());Vec3[] b=RenderMagiasFrieren.base(n);
                    float luz=(float)(FormaFrieren.brilhoPainel(painel,t)*esc);double r=FormaFrieren.PAINEL*esc;
                    for(int k=0;k<6;k++){
                        double a0=k*Math.PI/3,a1=(k+1)*Math.PI/3;
                        Vec3 p0=c.add(b[0].scale(Math.cos(a0)*r)).add(b[1].scale(Math.sin(a0)*r)),p1=c.add(b[0].scale(Math.cos(a1)*r)).add(b[1].scale(Math.sin(a1)*r));
                        if(borda)DesenhoMagia.fita(s,cam,p0,p1,.045,0,1,FAVO,luz);
                        else{DesenhoMagia.ponto(s,c,0,0,VIDRO,.1f*luz);DesenhoMagia.ponto(s,p0,1,0,VIDRO,.1f*luz);DesenhoMagia.ponto(s,p1,1,1,VIDRO,.1f*luz);DesenhoMagia.ponto(s,p1,0,1,VIDRO,.1f*luz);}
                    }
                }
            });
        }
    }
}
