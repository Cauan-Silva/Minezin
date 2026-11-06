package com.cauansilva.arcanecontrol.core.killua;

import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.cauansilva.arcanecontrol.core.vfx.*;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.P;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.Narukami;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.Echo;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.Yoyo;
import com.cauansilva.arcanecontrol.core.vfx.KilluaBase.Garras;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/**
 * Desenho no jogo do kit do Killua (base + Godspeed), com as mesmas classes puras e as mesmas cores das previas aprovadas
 * (tools/preview_killua_*.py). Modo 8 = fumaca/sombra/aco (alfa normal) ou camadas de tecido (aditivo), modo 1 = brilho
 * aditivo. A aura continua do Godspeed e a de tecido (AuraTecidoKillua/MalhaAura), nao as cascas volumetricas.
 */
final class RenderMagiasKillua {
    private static final int AZUL=0x4FA8FF,BRANCO=0xF2FAFF,CIANO=0x9FE6FF,ACO=0x6E7680,ACO_CLARO=0xC9D2DC,FIO=0xE8E8E8,
            FUMACA=0x1C1E26,SANGUE=0xB0101A,OURO=0xFFD24A,LILAS=0xB9C8FF,VIOLETA=0x6F6CFF,LILAS2=0xA9B8FF,POEIRA=0xB7A98E;
    private static final Vec3 CIMA=new Vec3(0,1,0);

    /** Referencial local: o origem, L lateral (= cima x frente, o +x das classes puras), U cima, F frente; k escala o z. */
    record Base(Vec3 o,Vec3 l,Vec3 u,Vec3 f,double k){
        Vec3 em(double x,double y,double z){return o.add(l.scale(x)).add(u.scale(y)).add(f.scale(z*k));}
        Vec3 p(P q){return em(q.x(),q.y(),q.z());}
        Vec3 dir(double x,double y,double z){return l.scale(x).add(u.scale(y)).add(f.scale(z));}
        List<Vec3> pts(List<P> q){List<Vec3> r=new ArrayList<>(q.size());for(P p:q)r.add(p(p));return r;}
        /** Coordenadas locais (sem escala) de um ponto relativo ao referencial. */
        P local(Vec3 v){Vec3 d=v.subtract(o);return new P(d.dot(l),d.dot(u),d.dot(f)/k);}
        static Base horizontal(Vec3 o,Vec3 frente,double k){Vec3 f=new Vec3(frente.x,0,frente.z);f=f.lengthSqr()<1e-6?new Vec3(0,0,1):f.normalize();return new Base(o,CIMA.cross(f).normalize(),CIMA,f,k);}
        static Base yaw(double graus){double y=Math.toRadians(graus);return horizontal(Vec3.ZERO,new Vec3(-Math.sin(y),0,Math.cos(y)),1);}
    }
    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}
    private static Vec3 frenteCorpo(Player p,float parcial){double y=Math.toRadians(p.getViewYRot(parcial));return new Vec3(-Math.sin(y),0,Math.cos(y));}

    // ---------------- primitivas (mesmas da previa: rib/linha/raio/forte/parts/nuvem/esfera) ----------------
    private static void fita(PoseStack pose,Vec3 cam,Vec3 a,Vec3 b,double w,int cor,double al){if(al>.004&&w>.0005)DesenhoMagia.fita(pose,cam,a,b,w,0,1,cor,(float)Math.min(1,al));}
    private static void disco(PoseStack pose,Vec3 cam,Vec3 c,double r,int cor,double a){if(r>.004&&a>.004)DesenhoMagia.anel(pose,c,cam.subtract(c).normalize(),0,r,cor,(float)Math.min(1,a),10);}
    private static void quad(PoseStack pose,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int cor,double al){if(al<.004)return;float f=(float)Math.min(1,al);
        DesenhoMagia.ponto(pose,a,0,0,cor,f);DesenhoMagia.ponto(pose,b,1,0,cor,f);DesenhoMagia.ponto(pose,c,1,1,cor,f);DesenhoMagia.ponto(pose,d,0,1,cor,f);}
    /** Faixa deitada no chao (largura horizontal), como plano() da previa da Tempestade. */
    private static void plano(PoseStack pose,Vec3 a,Vec3 b,double larg,double al,int cor){
        Vec3 d=new Vec3(b.x-a.x,0,b.z-a.z);double n=d.length();if(n<1e-5)return;Vec3 p=new Vec3(-d.z,0,d.x).scale(larg/2/n);
        quad(pose,a.add(p),a.subtract(p),b.subtract(p),b.add(p),cor,al);}
    private static void linha(PoseStack pose,Vec3 cam,List<Vec3> l,double larg,double al,int cor){if(al<.01)return;for(int i=0;i+1<l.size();i++)fita(pose,cam,l.get(i),l.get(i+1),larg,cor,al);}
    /** Raio eletrico: halo azul, corpo ciano, nucleo branco. */
    private static void raio(PoseStack pose,Vec3 cam,List<Vec3> l,double al,double larg){
        if(al<.01)return;for(int i=0;i+1<l.size();i++){Vec3 a=l.get(i),b=l.get(i+1);
            fita(pose,cam,a,b,.13*larg,AZUL,al*.45);fita(pose,cam,a,b,.05*larg,CIANO,al*.8);fita(pose,cam,a,b,.018*larg,BRANCO,al);}}
    /** Raio forte do Godspeed: halo violeta, corpo lilas, nucleo branco. */
    private static void forte(PoseStack pose,Vec3 cam,List<Vec3> l,double a,double w){
        if(a<.01)return;for(int i=0;i+1<l.size();i++){Vec3 A=l.get(i),B=l.get(i+1);
            fita(pose,cam,A,B,.3*w*a,VIOLETA,a*.33);fita(pose,cam,A,B,.11*w,LILAS2,a*.8);fita(pose,cam,A,B,.04*w,BRANCO,a);}}
    private static void tres(PoseStack pose,Vec3 cam,List<Vec3> l,double w1,double a1,double w2,double a2,double w3,double a3){
        for(int i=0;i+1<l.size();i++){Vec3 A=l.get(i),B=l.get(i+1);fita(pose,cam,A,B,w1,VIOLETA,a1);fita(pose,cam,A,B,w2,LILAS2,a2);fita(pose,cam,A,B,w3,BRANCO,a3);}}
    private static void parts(PoseStack pose,Vec3 cam,Base b,List<double[]> l,int cor,double larg,double al,boolean cabeca,int corCabeca){
        for(double[] q:l){Vec3 a=b.em(q[0],q[1],q[2]),c=b.em(q[3],q[4],q[5]);if(a.distanceToSqr(c)<1e-8)c=a.add(0,.02,0);double k=q[6]*al;
            fita(pose,cam,c,a,larg,cor,k);if(cabeca)fita(pose,cam,c.add(a.subtract(c).scale(.6)),a,larg*.45,corCabeca,k);}}
    private static void nuvem(PoseStack pose,Vec3 cam,Base b,List<double[]> l,int cor,double tam,double al){
        for(double[] q:l)disco(pose,cam,b.em(q[0],q[1],q[2]),tam*(1.6-q[6]*.6),cor,al*q[6]*.5);}
    /** Esfera de brilho: abre o proprio lote, entao so pode ser chamada fora de DesenhoMagia.lote. */
    private static void esfera(PoseStack pose,Vec3 cam,Vec3 c,double r,int cor,double a){if(a>.01&&r>.01)DesenhoMagia.esfera(pose,cam,c,r,1,cor,(float)Math.min(1,a),true,8);}

    static void desenhar(PoseStack pose,EfeitosKillua.Efeito e,double t,Vec3 camMundo,float parcial){
        if(!DesenhoMagia.iniciar(t))return;
        pose.pushPose();Vec3 d=e.origem.subtract(camMundo);pose.translate(d.x,d.y,d.z);Vec3 cam=camMundo.subtract(e.origem);
        try{
            switch(e.tipo){
                case EfeitosKillua.NARUKAMI->narukami(pose,cam,paraAlvo(e,KilluaBase.ALVO),t);
                case EfeitosKillua.ECHO->echo(pose,cam,paraAlvo(e,Echo.DISTANCIA),t);
                case EfeitosKillua.YOYO->yoyo(pose,cam,Base.horizontal(Vec3.ZERO,e.alvo,1),t);
                case EfeitosKillua.GARRAS->garras(pose,cam,paraAlvo(e,KilluaBase.ALVO),t);
                case EfeitosKillua.FAISCA->faisca(pose,cam,t,e.inicio);
                case EfeitosKillua.RELAMPAGO->relampago(pose,cam,Base.horizontal(Vec3.ZERO,e.alvo,1),t);
                case EfeitosKillua.WHIRLPOOL->whirlpool(pose,cam,e,t);
                case EfeitosKillua.SOCO->soco(pose,cam,Base.horizontal(Vec3.ZERO,e.alvo,1),t);
                case EfeitosKillua.PULSO->pulso(pose,cam,e,Base.horizontal(Vec3.ZERO,e.alvo,1),t,parcial);
                case EfeitosKillua.TEMPESTADE->tempestade(pose,cam,paraAlvo(e,KilluaTempestadeDivina.ALVO),t);
                default->{}
            }
        }finally{pose.popPose();DesenhoMagia.terminar();}
    }
    /** Frente do caster ao alvo; o z das classes puras e esticado para o alvo real cair no lugar do alvo de teste. */
    private static Base paraAlvo(EfeitosKillua.Efeito e,double ref){Vec3 ate=e.alvo.subtract(e.origem);double dist=Math.hypot(ate.x,ate.z);
        return Base.horizontal(Vec3.ZERO,ate,dist<.5?1:Math.max(.3,Math.min(2.5,dist/ref)));}

    // ---------------- Z: Narukami ----------------
    private static void narukami(PoseStack pose,Vec3 cam,Base b,double t){
        double aa=Narukami.alphaAura(t),ae=Narukami.alphaEstrela(t),ac=Narukami.alphaColuna(t),ar=Narukami.alphaRaio(t),ax=Narukami.alphaExplosao(t),
                cl=Narukami.clarao(t),ao=Narukami.alphaOnda(t),ap=Narukami.alphaParalisia(t);
        DesenhoMagia.lote(pose,1,true,()->{
            if(aa>.01)for(int k=0;k<Narukami.ARCOS_AURA;k++)raio(pose,cam,b.pts(Narukami.arcoAura(k,t)),aa*.85,.55);
            if(ae>.01){
                for(int k=0;k<Narukami.FLARES;k++){P[] f=Narukami.flare(k,t);Vec3 A=b.p(f[0]),B=b.p(f[1]);fita(pose,cam,A,B,.14*ae,CIANO,ae*.5);fita(pose,cam,A,B,.05*ae,BRANCO,ae);}
                for(int k=0;k<Narukami.RAIOS_ESTRELA;k++)raio(pose,cam,b.pts(Narukami.raioEstrela(k,t)),ae*.9,.8);
                for(int k=0;k<Narukami.FAISCAS;k++)raio(pose,cam,b.pts(Narukami.faisca(k,t)),ae*.8,.45);
                linha(pose,cam,b.pts(Narukami.anelEstrela(t)),.04,ae*.8,BRANCO);
            }
            if(ac>.01){
                for(int k=0;k<Narukami.RISCOS_COLUNA;k++){P[] r=Narukami.riscoColuna(k,t);double wd=Narukami.larguraRisco(k);Vec3 A=b.p(r[0]),B=b.p(r[1]);
                    if(A.distanceTo(B)>.05){fita(pose,cam,A,B,wd*2.2,AZUL,ac*.35);fita(pose,cam,A,B,wd,wd<.08?CIANO:BRANCO,ac*.75);}}
                for(int k=0;k<Narukami.RAIOS_CEU;k++)raio(pose,cam,b.pts(Narukami.raioCeu(k,t)),ac*.9,1.1);
            }
            if(ar>.01){
                for(int k=0;k<Narukami.RAIOS_TRANCADOS;k++)raio(pose,cam,b.pts(Narukami.raioTrancado(k,t)),ar*(k==0?1:.75),k==0?2.4:1.0);
                raio(pose,cam,b.pts(Narukami.raio(t)),ar,1.6);
                for(int k=0;k<Narukami.GALHOS;k++)raio(pose,cam,b.pts(Narukami.galho(k,t)),ar*.8,.6);
            }
            if(ax>.01)for(int k=0;k<Narukami.RAIOS_EXPLOSAO;k++){P[] r=Narukami.raioExplosao(k,t);Vec3 A=b.p(r[0]),B=b.p(r[1]);fita(pose,cam,A,B,.2*ax,AZUL,ax*.35);fita(pose,cam,A,B,.07*ax,BRANCO,ax*.9);}
            if(ao>.01){List<Vec3> v=b.pts(Narukami.ondaVertical(t)),c=b.pts(Narukami.ondaChao(t));
                linha(pose,cam,v,.12*ao+.02,ao*.6,CIANO);linha(pose,cam,v,.035,ao,BRANCO);linha(pose,cam,c,.16*ao+.02,ao*.5,AZUL);linha(pose,cam,c,.04,ao*.9,BRANCO);}
            parts(pose,cam,b,Narukami.faiscasVoo(t),CIANO,.04,1,true,BRANCO);
            if(ap>.01){
                for(int k=0;k<8;k++)raio(pose,cam,b.pts(Narukami.arcoParalisia(k,t)),ap,.8);
                for(int k=0;k<Narukami.ARCOS_GAIOLA;k++)raio(pose,cam,b.pts(Narukami.arcoGaiola(k,t)),ap*.7,.6);
                raio(pose,cam,b.pts(Narukami.anelParalisia(t)),ap*.8,.6);
            }
        });
        DesenhoMagia.lote(pose,8,false,()->nuvem(pose,cam,b,Narukami.pouso(t),POEIRA,.14,1));
        if(ae>.01){Vec3 c=b.p(Narukami.mao(t));double re=Narukami.raioEstrela(t);esfera(pose,cam,c,re*.8,BRANCO,ae);esfera(pose,cam,c,re*1.6,CIANO,ae*.3);esfera(pose,cam,c,re*3,AZUL,ae*.1);}
        Vec3 alvo=b.em(0,0,KilluaBase.ALVO);
        if(cl>.01){Vec3 c=alvo.add(0,1.6,0);esfera(pose,cam,c,.5+.5*(1-cl),BRANCO,cl);esfera(pose,cam,c,1.4,CIANO,cl*.35);esfera(pose,cam,c,2.6,AZUL,cl*.15);}
        if(ap>.01)esfera(pose,cam,alvo.add(0,1,0),1.0,0x7F6BFF,ap*.08);
    }

    // ---------------- X: Rhythm Echo ----------------
    private static void echo(PoseStack pose,Vec3 cam,Base b,double t){
        double ab=Echo.alphaBorrao(t),ae=Echo.alphaEstouro(t),ar=Echo.alphaRiscos(t);
        DesenhoMagia.lote(pose,8,false,()->{
            for(int n=0;n<Echo.IMAGENS;n++)nuvem(pose,cam,b,Echo.poeira(n,t),POEIRA,.12,1);
            nuvem(pose,cam,b,Echo.poeiraChegada(t),POEIRA,.12,1);
        });
        DesenhoMagia.lote(pose,1,true,()->{
            for(int n=0;n<Echo.IMAGENS;n++){double a=Echo.alphaPasso(n,t);if(a>.01){List<Vec3> l=b.pts(Echo.passo(n,t));linha(pose,cam,l,.05*a+.01,a*.7,LILAS);linha(pose,cam,l,.015,a,BRANCO);}}
            if(ab>.01)for(int h=0;h<3;h++){List<Vec3> l=b.pts(Echo.borrao(h,t));int n=l.size();
                for(int i=0;i+1<n;i++){double k=(i+1.)/Math.max(1,n);fita(pose,cam,l.get(i),l.get(i+1),.28*k,LILAS,ab*.35*k);fita(pose,cam,l.get(i),l.get(i+1),.06*k,BRANCO,ab*.5*k);}}
            if(ae>.01)for(int c=0;c<3;c++)for(boolean ch:new boolean[]{false,true}){List<Vec3> l=b.pts(Echo.estouro(ch,c,t));linha(pose,cam,l,.09*ae+.015,ae*.7,LILAS);linha(pose,cam,l,.02,ae,BRANCO);}
            if(ar>.01){
                for(int k=0;k<8;k++){P[] r=Echo.risco(k,t);Vec3 A=b.p(r[0]),B=b.p(r[1]);fita(pose,cam,A,B,.05,LILAS,ar*.7);fita(pose,cam,A,B,.015,BRANCO,ar);}
                for(int k=0;k<10;k++){P[] r=Echo.vento(k,t);fita(pose,cam,b.p(r[0]),b.p(r[1]),.025,BRANCO,ar*.8);}
            }
        });
    }

    // ---------------- C: Yo-yo de aco ----------------
    private static int sombra(int cor,double k){int r=(int)Math.min(255,(cor>>16&255)*k),g=(int)Math.min(255,(cor>>8&255)*k),bb=(int)Math.min(255,(cor&255)*k);return r<<16|g<<8|bb;}
    private static void discoYoyo(PoseStack pose,Vec3 cam,Base b,int lado,double t,boolean brilho){
        Vec3 c=b.p(Yoyo.yoyo(lado,t));P ei=Yoyo.eixo(lado,t);Vec3 e=b.dir(ei.x(),ei.y(),ei.z()).normalize(),u=CIMA,v=e.cross(u).normalize();double giro=Yoyo.giroProprio(t);
        if(brilho){for(int k=0;k<3;k++){double a=giro*1.3+k*2.09;Vec3 d=u.scale(Math.cos(a)).add(e.scale(Math.sin(a)));fita(pose,cam,c.add(d.scale(.24)),c.add(d.scale(.42)),.035,ACO_CLARO,.5);}return;}
        for(int ld=-1;ld<=1;ld+=2){Vec3 cc=c.add(v.scale(ld*.07));Vec3[] p=new Vec3[15];
            for(int i=0;i<15;i++){double a=2*Math.PI*i/14+giro;p[i]=cc.add(u.scale(Math.cos(a)).add(e.scale(Math.sin(a))).scale(.22));}
            for(int i=0;i<14;i++){quad(pose,cc,p[i],p[i+1],cc,sombra(i%3!=0?ACO:ACO_CLARO,.8+.3*Math.cos(i)),1);
                quad(pose,p[i],p[i+1],p[i+1].subtract(v.scale(ld*.06)),p[i].subtract(v.scale(ld*.06)),0x4A5058,1);}
            disco(pose,cam,cc.add(v.scale(ld*.01)),.06,0xD8DEE6,1);}
    }
    private static void yoyo(PoseStack pose,Vec3 cam,Base b,double t){
        double ar=Yoyo.alphaRastro(t),ao=Yoyo.alphaOnda(t),ap=Yoyo.alphaPuxao(t),ac=Yoyo.acerto(t);
        DesenhoMagia.lote(pose,8,false,()->{for(int lado=-1;lado<=1;lado+=2){
            linha(pose,cam,b.pts(Yoyo.fio(lado,t)),.014,.95,FIO);discoYoyo(pose,cam,b,lado,t,false);nuvem(pose,cam,b,Yoyo.poeira(lado,t),POEIRA,.16,1);}});
        DesenhoMagia.lote(pose,1,true,()->{
            for(int lado=-1;lado<=1;lado+=2){
                linha(pose,cam,b.pts(Yoyo.fio(lado,t)),.03,.15,BRANCO);discoYoyo(pose,cam,b,lado,t,true);
                if(ar>.01){int n=17;Vec3[] B=new Vec3[n],C=new Vec3[n],R=new Vec3[n];
                    for(int i=0;i<n;i++){B[i]=b.p(Yoyo.faixa(lado,i/16.,t,0));C[i]=b.p(Yoyo.faixa(lado,i/16.,t,1));R[i]=b.p(Yoyo.rastro(lado,i/16.,t));}
                    for(int i=0;i+1<n;i++){double kk=1-i/(double)n;quad(pose,B[i],B[i+1],C[i+1],C[i],ACO_CLARO,ar*.35*kk);fita(pose,cam,R[i],R[i+1],.05*kk+.01,BRANCO,ar*.9*kk);}}
            }
            if(ao>.01)for(int i=0;i<3;i++)linha(pose,cam,b.pts(Yoyo.onda(i,t)),.06*ao+.01,ao*(i==1?.8:.4),i==1?BRANCO:ACO_CLARO);
            if(ap>.01)for(int k=0;k<5;k++){P[] r=Yoyo.puxaoLinha(k,t);fita(pose,cam,b.p(r[0]),b.p(r[1]),.03,BRANCO,ap*.6);}
            parts(pose,cam,b,Yoyo.faiscasAcerto(t),0xFFB040,.03,1,true,0xFFF2C0);
            if(ac>.05){Vec3 c=b.em(0,.7,Math.min(Yoyo.RAIO,Yoyo.puxao(t)));
                for(int k=0;k<10;k++){double a=k*Math.PI/5;Vec3 d=b.dir(Math.cos(a),Math.sin(a),0);fita(pose,cam,c.add(d.scale(.1)),c.add(d.scale(.4+.3*(k%2))),.04*ac,BRANCO,ac);}}
        });
    }

    // ---------------- V: Garras de assassino ----------------
    private static void garras(PoseStack pose,Vec3 cam,Base b,double t){
        double af=Garras.alphaFumaca(t),aa=Garras.alphaAura(t),un=Garras.unha(t),ab=Garras.alphaBraco(t),ac=Garras.alphaCorte(t),am=Garras.alphaMeiaLua(t),
                ai=Garras.alphaImpacto(t),as=Garras.alphaSangue(t),amc=Garras.alphaMancha(t),cr=Garras.critico(t);
        Vec3 m=b.p(Garras.mao(t));
        DesenhoMagia.lote(pose,8,false,()->{
            if(ac>.01)for(int k=0;k<Garras.CORTES;k++){List<Vec3> l=corte(b,k,t);for(int i=0;i+1<l.size();i++){double kk=Math.sin(Math.PI*(i+.5)/l.size());fita(pose,cam,l.get(i),l.get(i+1),.16*kk+.02,0x200006,ac*.75);}}
            if(af>.01)for(int k=0;k<12;k++)disco(pose,cam,b.p(Garras.fumaca(k,t)),.3+.12*(k%3),FUMACA,af*.55);
            if(aa>.01)for(int k=0;k<6;k++){List<Vec3> l=b.pts(Garras.fioAura(k,t));for(int i=0;i+1<l.size();i++){double kk=Math.sin(Math.PI*(i+.5)/l.size());fita(pose,cam,l.get(i),l.get(i+1),.06*kk+.01,0x120A1E,aa*.35*kk);}}
            if(un>.01)for(int k=0;k<5;k++){Vec3 d=b.dir((k-2)*.18,.3,.9).normalize(),a=m.add(b.dir((k-2)*.06,0,0));fita(pose,cam,a,a.add(d.scale(un*1.8)),.035,0xE8E4DA,1);}
            parts(pose,cam,b,Garras.jorro(t),SANGUE,.045,1,false,0);
            if(as>.01)for(int k=0;k<Garras.GOTAS;k++){P g=Garras.gota(k,t);if(g!=null)disco(pose,cam,b.p(g),.07,SANGUE,as);}
            if(amc>.01)for(double[] q:Garras.manchas()){Vec3 c=b.em(q[0],q[1],q[2]);double r=q[3];Vec3[] p=new Vec3[11];
                for(int i=0;i<11;i++){double a=2*Math.PI*i/10;p[i]=c.add(b.dir(Math.cos(a),0,Math.sin(a)).scale(r*(1+.25*Math.sin(i*2.3))));}
                for(int i=0;i<10;i++)quad(pose,c,p[i],p[i+1],c,0x6A0810,amc*.9);}
        });
        DesenhoMagia.lote(pose,1,true,()->{
            parts(pose,cam,b,Garras.fragmentos(t),0x7A84A0,.04,1,false,0);
            if(aa>.01)for(int k=0;k<6;k++){List<Vec3> l=b.pts(Garras.fioAura(k,t));for(int i=0;i+1<l.size();i++){double kk=Math.sin(Math.PI*(i+.5)/l.size());fita(pose,cam,l.get(i),l.get(i+1),.015*kk,0x7A4FB0,aa*.4*kk);}}
            for(int n=0;n<Garras.PASSOS;n++){double a=Garras.alphaPassoFurtivo(n,t);if(a>.01)linha(pose,cam,b.pts(Garras.passoFurtivo(n,t)),.025,a*.5,0x7A4FB0);}
            if(un>.01)for(int k=0;k<5;k++){Vec3 d=b.dir((k-2)*.18,.3,.9).normalize(),a=m.add(b.dir((k-2)*.06,0,0));fita(pose,cam,a,a.add(d.scale(un*1.8)),.06,0xFF5060,.35);}
            if(ab>.01){List<Vec3> l=b.pts(Garras.rastroBraco(t));int n=l.size();
                for(int i=0;i+1<n;i++){double k=(i+1.)/Math.max(1,n);fita(pose,cam,l.get(i),l.get(i+1),.3*k,0xFF8090,ab*.3*k);fita(pose,cam,l.get(i),l.get(i+1),.08*k,BRANCO,ab*.7*k);}}
            if(ac>.01)for(int k=0;k<Garras.CORTES;k++){List<Vec3> l=corte(b,k,t);for(int i=0;i+1<l.size();i++){double kk=Math.sin(Math.PI*(i+.5)/l.size());
                fita(pose,cam,l.get(i),l.get(i+1),.12*kk+.02,0xFF2035,ac*.8);fita(pose,cam,l.get(i),l.get(i+1),.04*kk+.005,BRANCO,ac);}}
            if(am>.01){List<Vec3> l=b.pts(Garras.meiaLua(t));for(int i=0;i+1<l.size();i++){double kk=Math.sin(Math.PI*(i+.5)/l.size());
                fita(pose,cam,l.get(i),l.get(i+1),.18*kk+.01,0xFF3040,am*.45);fita(pose,cam,l.get(i),l.get(i+1),.05*kk,BRANCO,am*.9);}}
            if(ai>.01){Vec3 c=b.em(0,1.3,KilluaBase.ALVO).add(b.f().scale(-.5));
                for(int k=0;k<14;k++){double a=k*Math.PI/7,r=.8+.6*(k%2);Vec3 d=b.dir(Math.cos(a),Math.sin(a),0);fita(pose,cam,c.add(d.scale(.2)),c.add(d.scale(r)),.05*ai,k%2==1?BRANCO:0xFF3040,ai);}}
            if(cr>.01){Vec3 c=b.em(0,2.4,KilluaBase.ALVO);double r=.5*cr;
                for(Vec3 d:new Vec3[]{b.dir(1,0,0),b.dir(0,1,0),b.dir(.7,.7,0),b.dir(.7,-.7,0)})fita(pose,cam,c.subtract(d.scale(r)),c.add(d.scale(r)),.05,OURO,cr);
                List<Vec3> l=new ArrayList<>();for(int i=0;i<25;i++){double a=2*Math.PI*i/24;l.add(c.add(b.dir(Math.cos(a)*r*1.2,0,Math.sin(a)*r*1.2)));}
                linha(pose,cam,l,.03,cr,OURO);}
        });
    }
    private static List<Vec3> corte(Base b,int k,double t){List<Vec3> l=new ArrayList<>();for(int i=0;i<=12;i++)l.add(b.p(Garras.corte(k,i/12.,t)));return l;}

    // ---------------- faisca de acerto (704) ----------------
    private static void faisca(PoseStack pose,Vec3 cam,double t,long semente){
        Base b=new Base(new Vec3(0,-1.6,0),new Vec3(1,0,0),CIMA,new Vec3(0,0,1),1);int sm=9000+(int)(semente%97);double a=Math.max(0,1-t/5);
        DesenhoMagia.lote(pose,1,true,()->{
            parts(pose,cam,b,KilluaBase.particulas(sm,12,new P(0,1.6,0),0,t,.3,.25,new P(0,.2,0),.012,8),CIANO,.03,1,true,BRANCO);
            if(a>.01)for(int k=0;k<6;k++){double g=k*2.39996+semente,el=(k%3-1)*.6;
                raio(pose,cam,b.pts(KilluaBase.raioQuebrado(new P(0,1.6,0),new P(Math.cos(g)*Math.cos(el)*.7,1.6+Math.sin(el)*.7,Math.sin(g)*Math.cos(el)*.7),4,.1,sm+k,t*2)),a,.5);}
        });
        esfera(pose,cam,Vec3.ZERO,.2+.25*(1-a),BRANCO,a*.8);
    }

    // ---------------- G-Z: Relampago Perfurante ----------------
    private static void relampago(PoseStack pose,Vec3 cam,Base b,double t){
        double ap=KilluaRelampagoPerfurante.alphaPreparo(t),aq=KilluaRelampagoPerfurante.alphaQueimado(t),ca=KilluaRelampagoPerfurante.calorMarca(t),ar=KilluaRelampagoPerfurante.alphaRastro(t),
                ai=KilluaRelampagoPerfurante.alphaImpacto(t),apa=KilluaRelampagoPerfurante.alphaParalisia(t),fr=KilluaRelampagoPerfurante.frenteRaio(t);
        List<double[]> marca=KilluaRelampagoPerfurante.marca(t);
        DesenhoMagia.lote(pose,8,false,()->{
            if(aq>.01)for(int i=0;i+1<marca.size();i++){double[] a=marca.get(i),c=marca.get(i+1);Vec3 A=b.em(a[0],.02,a[1]),B=b.em(c[0],.02,c[1]);
                fita(pose,cam,A,B,a[2]*2.2,0x101216,aq*.5);fita(pose,cam,A,B,a[2],0x050608,aq*.8);}
            nuvem(pose,cam,b,KilluaRelampagoPerfurante.fumacaMarca(t),0x3A3E48,.07,.3);
        });
        DesenhoMagia.lote(pose,1,true,()->{
            if(ap>.01){for(int k=0;k<KilluaRelampagoPerfurante.ARCOS_PES;k++)raio(pose,cam,b.pts(KilluaRelampagoPerfurante.arcoPes(k,t)),ap,.6);
                List<Vec3> l=b.pts(KilluaRelampagoPerfurante.ponta(t));for(int i=0;i+1<l.size();i++){fita(pose,cam,l.get(i),l.get(i+1),.12*ap,AZUL,ap*.5);fita(pose,cam,l.get(i),l.get(i+1),.04,BRANCO,ap);}}
            if(aq>.01&&ca>.01)for(int i=0;i+1<marca.size();i++){double[] a=marca.get(i),c=marca.get(i+1);Vec3 A=b.em(a[0],.02,a[1]),B=b.em(c[0],.02,c[1]);
                fita(pose,cam,A,B,a[2]*1.6,AZUL,aq*ca*.45);fita(pose,cam,A,B,a[2]*.45,ca>.6?BRANCO:CIANO,aq*ca);}
            parts(pose,cam,b,KilluaRelampagoPerfurante.brasasMarca(t),CIANO,.025,1,true,BRANCO);
            if(ar>.01){
                for(int k=0;k<KilluaRelampagoPerfurante.FIOS;k++){double w=k==0?1:.45;tres(pose,cam,b.pts(KilluaRelampagoPerfurante.fio(k,t)),.7*w*ar,ar*.35,.28*w,ar*.8,.1*w,ar);}
                for(int k=0;k<KilluaRelampagoPerfurante.GALHOS;k++)if(KilluaRelampagoPerfurante.galhoVisivel(k,t))raio(pose,cam,b.pts(KilluaRelampagoPerfurante.galho(k,t)),ar*.85,.6);
            }
            if(ai>.01)for(int k=0;k<KilluaRelampagoPerfurante.ESPINHOS;k++)tres(pose,cam,b.pts(KilluaRelampagoPerfurante.espinho(k,t)),.22*ai,ai*.35,.08,ai*.8,.03,ai);
            for(int c=0;c<2;c++){double a=KilluaRelampagoPerfurante.alphaOnda(c,t);if(a>.01){List<Vec3> l=b.pts(KilluaRelampagoPerfurante.onda(c,t));linha(pose,cam,l,.12*a+.02,a*.8,BRANCO);linha(pose,cam,l,.3*a,a*.25,CIANO);}}
            if(apa>.01)for(int k=0;k<KilluaRelampagoPerfurante.ARCOS_ALVO;k++)raio(pose,cam,b.pts(KilluaRelampagoPerfurante.arcoAlvo(k,t)),apa,.7);
            parts(pose,cam,b,KilluaRelampagoPerfurante.faiscasImpacto(t),CIANO,.035,1,true,BRANCO);
            parts(pose,cam,b,KilluaRelampagoPerfurante.faiscasFreio(t),BRANCO,.03,1,true,BRANCO);
        });
        if(ar>.9&&fr<KilluaRelampagoPerfurante.ALCANCE-.05){Vec3 fz=b.em(0,1.0,fr);esfera(pose,cam,fz,.45,BRANCO,.9);esfera(pose,cam,fz,1.0,CIANO,.35);}
        if(ai>.01)esfera(pose,cam,b.em(0,1.1,KilluaBase.ALVO),.5+.6*(1-ai),BRANCO,ai*.8);
    }

    // ---------------- G-X: Whirlpool (guarda 711 + contra-ataque 712) ----------------
    private static void whirlpool(PoseStack pose,Vec3 cam,EfeitosKillua.Efeito e,double t){
        Base b=Base.horizontal(Vec3.ZERO,e.alvo,1);boolean contra=!Double.isNaN(e.golpe);
        double ag=!contra||t<e.golpe?s(t/KilluaWhirlpool.ENTRADA)*(1-lim((t-KilluaWhirlpool.JANELA)/3)):Math.max(0,1-(t-e.golpe))*s(e.golpe/KilluaWhirlpool.ENTRADA);
        DesenhoMagia.lote(pose,1,true,()->{
            if(ag>.01){for(int k=0;k<KilluaWhirlpool.ARCOS_CUPULA;k++)raio(pose,cam,b.pts(KilluaWhirlpool.arcoCupula(k,t)),ag,.55);
                for(int i=0;i<2;i++)linha(pose,cam,b.pts(KilluaWhirlpool.anelGuarda(i,t)),.05*ag+.01,ag*(.8-.3*i),i==0?BRANCO:CIANO);}
        });
        if(!contra||t<e.golpe||e.golpeO==null)return;
        // referencial do contra: Killua (guarda) em z = 0, atacante em z = ATACANTE na direcao recebida
        Vec3 dir=e.golpeD;if(dir.x*dir.x+dir.z*dir.z<1e-6)return;Base c=Base.horizontal(e.golpeO.subtract(e.origem).subtract(new Vec3(dir.x,0,dir.z).normalize().scale(KilluaWhirlpool.ATACANTE)),dir,1);
        double tc=t-e.golpe+KilluaWhirlpool.ACERTO;
        double asu=KilluaWhirlpool.alphaSumico(tc),ar=KilluaWhirlpool.alphaRedemoinho(tc),ai=KilluaWhirlpool.alphaImpacto(tc),apa=KilluaWhirlpool.alphaParalisia(tc);
        DesenhoMagia.lote(pose,1,true,()->{
            if(asu>.01)for(int k=0;k<2;k++)tres(pose,cam,c.pts(KilluaWhirlpool.raioSumico(k,tc)),.35*asu,asu*.35,.12,asu*.8,.045,asu);
            parts(pose,cam,c,KilluaWhirlpool.faiscasSumico(tc),CIANO,.03,1,true,BRANCO);
            if(ar>.01){
                for(int k=0;k<KilluaWhirlpool.ESPIRAIS;k++){List<Vec3> l=c.pts(KilluaWhirlpool.espiral(k,tc));int n=l.size();
                    for(int i=0;i+1<n;i++){double kk=.5+.5*i/n;Vec3 A=l.get(i),B=l.get(i+1);fita(pose,cam,A,B,.26*ar*kk,VIOLETA,ar*.3*kk);fita(pose,cam,A,B,.09*kk,LILAS2,ar*.75*kk);fita(pose,cam,A,B,.03,BRANCO,ar*kk);}}
                if(tc>=KilluaWhirlpool.REAPARECE&&tc<=KilluaWhirlpool.FIM_CHUTE+2){List<Vec3> l=c.pts(KilluaWhirlpool.arcoChute(tc));int n=l.size();
                    for(int i=0;i+1<n;i++){double kk=1-i/(double)n;fita(pose,cam,l.get(i),l.get(i+1),.3*kk,CIANO,ar*.5*kk);fita(pose,cam,l.get(i),l.get(i+1),.08*kk,BRANCO,ar*kk);}}
            }
            if(ai>.01)for(int k=0;k<10;k++)tres(pose,cam,c.pts(KilluaWhirlpool.raioImpacto(k,tc)),.2*ai,ai*.35,.07,ai*.85,.025,ai);
            parts(pose,cam,c,KilluaWhirlpool.faiscasImpacto(tc),CIANO,.035,1,true,BRANCO);
            if(apa>.01)for(int k=0;k<KilluaWhirlpool.ARCOS_ALVO;k++)raio(pose,cam,c.pts(KilluaWhirlpool.arcoAlvo(k,tc)),apa,.7);
        });
    }

    // ---------------- G-C: Soco Relampago ----------------
    private static void soco(PoseStack pose,Vec3 cam,Base b,double t){
        double acg=KilluaSocoRelampago.alphaCarga(t),ai=KilluaSocoRelampago.alphaImpacto(t),arr=KilluaSocoRelampago.alphaRacha(t),apa=KilluaSocoRelampago.alphaParalisia(t);
        DesenhoMagia.lote(pose,8,false,()->{
            if(arr>.01)for(int k=0;k<KilluaSocoRelampago.RACHAS;k++)linha(pose,cam,b.pts(KilluaSocoRelampago.racha(k,t)),.16,arr*.7,0x0A0B0E);
            nuvem(pose,cam,b,KilluaSocoRelampago.poeira(t),POEIRA,.12,.6);
        });
        DesenhoMagia.lote(pose,1,true,()->{
            if(acg>.01){for(int k=0;k<KilluaSocoRelampago.ARCOS_PUNHO;k++)raio(pose,cam,b.pts(KilluaSocoRelampago.arcoPunho(k,t)),acg,.55);
                raio(pose,cam,b.pts(KilluaSocoRelampago.espiralBraco(t)),acg,.5);}
            if(ai>.01)for(int k=0;k<KilluaSocoRelampago.CONE;k++)tres(pose,cam,b.pts(KilluaSocoRelampago.cone(k,t)),.3*ai,ai*.35,.11,ai*.8,.04,ai);
            for(int c=0;c<2;c++){double a=KilluaSocoRelampago.alphaAnel(c,t);if(a>.01){List<Vec3> l=b.pts(KilluaSocoRelampago.anelChoque(c,t));linha(pose,cam,l,.12*a+.02,a*.85,BRANCO);linha(pose,cam,l,.32*a,a*.25,CIANO);}}
            if(arr>.01)for(int k=0;k<KilluaSocoRelampago.RACHAS;k++){List<Vec3> l=b.pts(KilluaSocoRelampago.racha(k,t));linha(pose,cam,l,.07,arr*.6,CIANO);linha(pose,cam,l,.025,arr,BRANCO);}
            if(apa>.01)for(int k=0;k<KilluaSocoRelampago.ARCOS_ALVO;k++)raio(pose,cam,b.pts(KilluaSocoRelampago.arcoAlvo(k,t)),apa,.7);
            parts(pose,cam,b,KilluaSocoRelampago.faiscasImpacto(t),CIANO,.035,1,true,BRANCO);
        });
        if(acg>.01){Vec3 p=b.p(KilluaSocoRelampago.punho(t));double r=KilluaSocoRelampago.raioEsfera(t);esfera(pose,cam,p,r,BRANCO,acg*.9);esfera(pose,cam,p,r*2.2,CIANO,acg*.3);}
        if(ai>.01)esfera(pose,cam,b.p(KilluaSocoRelampago.pontoImpacto()),.35+.7*(1-ai),BRANCO,ai*.85);
    }

    // ---------------- G-V: Pulso Eletromagnetico ----------------
    private static void pulso(PoseStack pose,Vec3 cam,EfeitosKillua.Efeito e,Base b,double t,float parcial){
        double ac=KilluaPulsoEletromagnetico.alphaCarga(t),rn=KilluaPulsoEletromagnetico.raioNucleo(t),ap=KilluaPulsoEletromagnetico.alphaPulso(t),R=KilluaPulsoEletromagnetico.raioPulso(t);
        Vec3 C=b.p(KilluaPulsoEletromagnetico.centro());
        // alvos reais (fotografados no cast): posicao local atual de cada um
        var mc=Minecraft.getInstance();List<P> pos=new ArrayList<>();List<Double> toque=new ArrayList<>();
        for(int i=0;i<e.alvos.size();i++){Entity x=mc.level.getEntity(e.alvos.get(i));if(x==null)continue;pos.add(b.local(x.getPosition(parcial).subtract(e.origem)));toque.add(e.toques.get(i));}
        DesenhoMagia.lote(pose,1,true,()->{
            if(ac>.01){for(int k=0;k<KilluaPulsoEletromagnetico.LINHAS;k++){List<Vec3> l=b.pts(KilluaPulsoEletromagnetico.linhaCampo(k,t));
                    for(int i=0;i+1<l.size();i++){fita(pose,cam,l.get(i),l.get(i+1),.09*ac,VIOLETA,ac*.35);fita(pose,cam,l.get(i),l.get(i+1),.03,LILAS2,ac*.85);}}
                for(int k=0;k<6;k++)raio(pose,cam,b.pts(KilluaPulsoEletromagnetico.arcoNucleo(k,t)),ac,.45);}
            if(ap>.01){for(int k=0;k<KilluaPulsoEletromagnetico.ARCOS_CASCA;k++)tres(pose,cam,b.pts(KilluaPulsoEletromagnetico.arcoCasca(k,t)),.25*ap,ap*.3,.09,ap*.8,.03,ap);
                for(int i=0;i<2;i++)linha(pose,cam,b.pts(KilluaPulsoEletromagnetico.onda(i,t)),(.16-.06*i)*ap+.02,ap*(.85-.3*i),i==0?BRANCO:CIANO);}
            for(int i=0;i<pos.size();i++){P a=pos.get(i);double tq=toque.get(i),u=t-tq,al=u<0||u>6?0:1-u/6;
                if(al>.01)tres(pose,cam,b.pts(KilluaBase.raioQuebrado(new P(a.x()*.7,KilluaPulsoEletromagnetico.PEITO+1.6,a.z()*.7),new P(a.x(),a.y()+1.1,a.z()),6,.25,4200+i*5,t*2)),.3*al,al*.35,.1,al*.85,.035,al);
                parts(pose,cam,b,KilluaBase.particulas(4410+i,14,new P(a.x(),a.y()+1.1,a.z()),tq,t,.25,.22,new P(0,.25,0),.012,12),CIANO,.035,1,true,BRANCO);}
            parts(pose,cam,b,KilluaBase.particulas(4400,50,KilluaPulsoEletromagnetico.centro(),KilluaPulsoEletromagnetico.PULSO,t,.55,.55,new P(0,.05,0),.01,16),CIANO,.035,1,true,BRANCO);
        });
        if(rn>.02){esfera(pose,cam,C,rn,BRANCO,.95);esfera(pose,cam,C,rn*2.4,CIANO,.35);}
        if(ap>.01){esfera(pose,cam,C,R,CIANO,ap*.06);esfera(pose,cam,C,R*.97,LILAS2,ap*.04);}
    }

    // ---------------- G durante o Godspeed: Tempestade Divina ----------------
    private static void tempestade(PoseStack pose,Vec3 cam,Base b,double t){
        double arp=KilluaTempestadeDivina.alphaRaioPrender(t),ap=KilluaTempestadeDivina.alphaPrisao(t),ag=KilluaTempestadeDivina.alphaGaiola(t),as=KilluaTempestadeDivina.alphaSubida(t),
                ac=KilluaTempestadeDivina.alphaCeu(t),aco=KilluaTempestadeDivina.alphaColuna(t),ai=KilluaTempestadeDivina.alphaImpacto(t),acr=KilluaTempestadeDivina.alphaCratera(t),
                ca=KilluaTempestadeDivina.calorCratera(t),apa=KilluaTempestadeDivina.alphaParalisia(t);
        Vec3 C=b.p(KilluaTempestadeDivina.chao());
        List<Vec3> borda=acr>.01?b.pts(KilluaTempestadeDivina.bordaCratera(t)):List.of();
        List<List<Vec3>> rachas=new ArrayList<>();if(acr>.01)for(int k=0;k<KilluaTempestadeDivina.RACHAS;k++)rachas.add(b.pts(KilluaTempestadeDivina.racha(k,t)));
        DesenhoMagia.lote(pose,8,false,()->{
            if(acr>.01){
                for(int i=0;i+1<borda.size();i++){Vec3 A=borda.get(i),B=borda.get(i+1);double nb=A.distanceTo(B);
                    for(int k=0;k<5;k++){double u0=k/5.,u1=(k+1)/5.;plano(pose,C.add(A.subtract(C).scale(u0)),C.add(A.subtract(C).scale(u1)),nb*u1+.05,.55-.08*k,0x0C0D10);}
                    plano(pose,A,B,.35,.7,0x1A1B20);}
                for(List<Vec3> l:rachas)for(int i=0;i+1<l.size();i++)plano(pose,l.get(i),l.get(i+1),.22,.75,0x08090B);
            }
            nuvem(pose,cam,b,KilluaTempestadeDivina.poeira(t),0x8A8476,.1,.25);
        });
        DesenhoMagia.lote(pose,1,true,()->{
            if(arp>.01)forte(pose,cam,b.pts(KilluaTempestadeDivina.raioPrender(t)),arp,.8);
            if(ap>.01)for(int k=0;k<3;k++){List<Vec3> l=b.pts(KilluaTempestadeDivina.anelPrisao(k,t));linha(pose,cam,l,.05,ap*.8,CIANO);linha(pose,cam,l,.015,ap,BRANCO);}
            for(int i=0;i<KilluaTempestadeDivina.GOLPES;i++){
                if(ag>.01&&KilluaTempestadeDivina.segmentoVisivel(i,t))forte(pose,cam,b.pts(KilluaTempestadeDivina.segmentoGaiola(i,t)),ag,.8);
                double a=KilluaTempestadeDivina.alphaAcerto(i,t);if(a>.01)for(int k=0;k<6;k++)forte(pose,cam,b.pts(KilluaTempestadeDivina.raioAcerto(i,k,t)),a,.5);
            }
            if(as>.01)forte(pose,cam,b.pts(KilluaTempestadeDivina.rastroSubida(t)),as,1.2);
            if(aco>.01)for(int k=0;k<KilluaTempestadeDivina.COLUNA;k++)forte(pose,cam,b.pts(KilluaTempestadeDivina.fioColuna(k,t)),aco,k==0?3:1.2);
            if(ai>.01)for(int k=0;k<KilluaTempestadeDivina.ESTOURO;k++)forte(pose,cam,b.pts(KilluaTempestadeDivina.raioEstouro(k,t)),ai,1);
            for(int c=0;c<3;c++){double a=KilluaTempestadeDivina.alphaAnel(c,t);if(a>.01){List<Vec3> l=b.pts(KilluaTempestadeDivina.anelChoque(c,t));linha(pose,cam,l,.18*a+.03,a*.85,BRANCO);linha(pose,cam,l,.45*a,a*.25,CIANO);}}
            if(acr>.01&&ca>.01){for(int i=0;i+1<borda.size();i++)plano(pose,borda.get(i),borda.get(i+1),.08,ca*.6,CIANO);
                for(List<Vec3> l:rachas)for(int i=0;i+1<l.size();i++){plano(pose,l.get(i),l.get(i+1),.09,ca*.7,CIANO);plano(pose,l.get(i),l.get(i+1),.03,ca,BRANCO);}}
            if(apa>.01)for(int k=0;k<6;k++)raio(pose,cam,b.pts(KilluaTempestadeDivina.arcoAlvo(k,t)),apa,.7);
            parts(pose,cam,b,KilluaTempestadeDivina.faiscas(t),CIANO,.035,1,true,BRANCO);
        });
        if(ac>.01){Vec3 c=b.em(0,KilluaTempestadeDivina.CEU+2,KilluaTempestadeDivina.ALVO);esfera(pose,cam,c,2.2,0x8FA0FF,ac*.25);esfera(pose,cam,c,.9,BRANCO,ac*.7);}
        if(ai>.01&&t>=KilluaTempestadeDivina.IMPACTO){double r=KilluaTempestadeDivina.raioExplosao(t);esfera(pose,cam,C,r,LILAS2,ai*.12);esfera(pose,cam,C,r*.5,BRANCO,ai*.4);}
    }

    // ---------------- G: Godspeed (entrada + aura de tecido continua) ----------------
    /** saida: 1 enquanto a forma dura, cai a 0 nos 10 ticks depois do fim. */
    static void godspeed(PoseStack pose,EfeitosKillua.Godspeed g,Player dono,double t,Vec3 camMundo,float parcial,double saida){
        if(!DesenhoMagia.iniciar(t))return;
        // primeira pessoa do proprio Killua: a aura e a entrada quase somem para nao cegar a tela
        var mc=net.minecraft.client.Minecraft.getInstance();PRIMEIRA=dono==mc.player&&mc.options.getCameraType().isFirstPerson()?.12:1;
        try{
            if(t<KilluaGodspeed.ATIVA+KilluaGodspeed.ESTOURO+8&&PRIMEIRA>.5){
                pose.pushPose();Vec3 d=g.centro.subtract(camMundo);pose.translate(d.x,d.y,d.z);
                try{entrada(pose,camMundo.subtract(g.centro),Base.yaw(g.yaw),t);}finally{pose.popPose();}
            }
            if(g.ticada&&g.pes!=null){
                Vec3 ref=dono.getPosition(parcial);pose.pushPose();Vec3 d=ref.subtract(camMundo);pose.translate(d.x,d.y,d.z);
                try{aura(pose,camMundo.subtract(ref),g,dono,t,parcial,saida);}finally{pose.popPose();}
            }
        }finally{DesenhoMagia.terminar();}
    }
    private static void entrada(PoseStack pose,Vec3 cam,Base b,double t){
        double ac=KilluaGodspeed.alphaCarga(t),ae=KilluaGodspeed.alphaEstouro(t),cl=KilluaGodspeed.clarao(t);
        DesenhoMagia.lote(pose,1,true,()->{
            if(ac>.01){
                for(int k=0;k<KilluaGodspeed.ARCOS_BRACO;k++)for(int lado=-1;lado<=1;lado+=2)raio(pose,cam,b.pts(KilluaGodspeed.arcoBraco(k,lado,t)),ac,.55);
                for(int k=0;k<KilluaGodspeed.ARCOS_CHAO;k++)raio(pose,cam,b.pts(KilluaGodspeed.arcoChao(k,t)),ac*.85,.6);
                parts(pose,cam,b,KilluaGodspeed.faiscasCarga(t),CIANO,.03,ac,true,BRANCO);
            }
            if(ae>.01){
                for(int k=0;k<KilluaGodspeed.RAIOS_COLUNA;k++)raio(pose,cam,b.pts(KilluaGodspeed.raioColuna(k,t)),ae,1.5);
                for(int k=0;k<KilluaGodspeed.RAIOS_ESTOURO;k++){P[] r=KilluaGodspeed.raioEstouro(k,t);Vec3 A=b.p(r[0]),B=b.p(r[1]);fita(pose,cam,A,B,.2*ae,AZUL,ae*.35);fita(pose,cam,A,B,.07*ae,BRANCO,ae*.9);}
                for(int i=0;i<3;i++)linha(pose,cam,b.pts(KilluaGodspeed.ondaChao(i,t)),(.14-.04*i)*ae+.02,ae*(.8-.2*i),i==0?BRANCO:CIANO);
            }
            parts(pose,cam,b,KilluaGodspeed.faiscasEstouro(t),CIANO,.04,1,true,BRANCO);
        });
        if(cl>.01){Vec3 c=b.em(0,1.1,0);esfera(pose,cam,c,.6+.6*(1-cl),BRANCO,cl);esfera(pose,cam,c,1.8,CIANO,cl*.3);}
    }
    private static double PRIMEIRA=1;
    private static final double[][] CAMADAS={{2.2,.1},{1.4,.17},{.8,.3}};
    private static final int[][] CORES={{AZUL,AZUL,AZUL},{CIANO,CIANO,CIANO},{CIANO,BRANCO,CIANO}};
    /** Aura de tecido: pontos do mundo da simulacao (tick) relativos a g.pes, desenhados em volta da posicao interpolada. */
    private static void aura(PoseStack pose,Vec3 cam,EfeitosKillua.Godspeed g,Player dono,double t,float parcial,double saida){
        AuraTecidoKillua a=g.aura;Vec3 o=g.pes;
        double cr=(t<KilluaGodspeed.ATIVA?0:s((t-KilluaGodspeed.ATIVA)/8))*saida*PRIMEIRA;if(cr<.01)return;
        List<List<Vec3>> malhas=new ArrayList<>();int[] linhas=new int[AuraTecidoKillua.LINGUAS];
        for(int n=0;n<AuraTecidoKillua.LINGUAS;n++){var gr=a.lingua(n);linhas[n]=gr.size();List<Vec3> l=new ArrayList<>();for(var row:gr)for(P p:row)l.add(new Vec3(p.x()-o.x,p.y()-o.y,p.z()-o.z));malhas.add(l);}
        int C=AuraTecidoKillua.COLUNAS;Base corpo=Base.horizontal(Vec3.ZERO,frenteCorpo(dono,parcial),1);Vec3 lado=corpo.l();
        // linguas: halo + corpo + nucleo branco, somem para as pontas
        DesenhoMagia.lote(pose,8,true,()->{
            for(int n=0;n<malhas.size();n++){List<Vec3> G=malhas.get(n);int L=linhas[n];
                for(int c=0;c<3;c++){double esc=CAMADAS[c][0],amax=CAMADAS[c][1];int[] cores=CORES[c];
                    for(int i=0;i+1<L;i++)for(int j=0;j<2;j++){
                        int[][] q={{i,j},{i+1,j},{i+1,j+1},{i,j+1}};
                        for(int[] v:q){Vec3 m=G.get(v[0]*C+1),p=G.get(v[0]*C+v[1]);Vec3 P_=m.add(p.subtract(m).scale(esc));double u=v[0]/(L-1.),fl=.8+.2*Math.sin(t*2.7+v[0]*1.3+v[1]);
                            double al=cr*amax*fl*(1-Math.pow(u,1.4))*(v[1]==1?1:.55);DesenhoMagia.ponto(pose,P_,v[1]*.5,u,cores[v[1]],(float)Math.max(0,Math.min(1,al)));}
                    }}
            }
            // brilho interno: envelope eliptico com lobos
            double[] ev=a.envelope();double RH=ev[3],RV=ev[4];Vec3 C0=new Vec3(ev[0]-o.x,ev[1]-o.y,ev[2]-o.z);int lat=10,lon=20;double tm=(t-14)/20,fr=Math.max(0,1-(RH-1.3)*8);
            if(fr>.001)for(int i=0;i<lat;i++)for(int j=0;j<lon;j++){int[][] q={{i,j},{i+1,j},{i+1,j+1},{i,j+1}};
                for(int[] v:q){double th=Math.PI*v[0]/lat,ph=2*Math.PI*v[1]/lon,dx=Math.sin(th)*Math.cos(ph),dy=-Math.cos(th),dz=Math.sin(th)*Math.sin(ph),az=Math.atan2(dz,dx);
                    double lb=.92*(1+.32*(.55*Math.sin(az*3+tm*1.3)+.3*Math.sin(az*5-tm*2.1+dy*3)+.35*Math.sin(dy*4.5-tm*1.7+az*2)));
                    Vec3 P_=C0.add(dx*RH*lb,dy*RV*lb,dz*RH*lb);Vec3 nn=P_.subtract(C0).normalize(),vv=cam.subtract(P_).normalize();double rim=Math.pow(1-Math.abs(nn.dot(vv)),2.5);
                    double baixo=lim(P_.y/.5),al=cr*baixo*(.035+.025*rim)*fr*(.85+.15*Math.sin(t*1.9+i+j));
                    DesenhoMagia.ponto(pose,P_,v[1]/(double)lon,v[0]/(double)lat,CIANO,(float)Math.min(1,al));}}
        });
        DesenhoMagia.lote(pose,1,true,()->{
            // pulsos subindo pelo centro de cada lingua e brasas soltando das pontas
            for(int n=0;n<malhas.size();n++){List<Vec3> G=malhas.get(n);int L=linhas[n];double fase=((t*.09+n*.37)%1);
                for(int i=0;i+1<L;i++){double u=i/(L-1.),k=Math.exp(-Math.pow((u-fase)/.09,2));if(k>.05)fita(pose,cam,G.get(i*C+1),G.get((i+1)*C+1),.09*k,BRANCO,cr*k*.9*(1-u*.5));}
                if(L>3){Vec3 d=G.get((L-1)*C+1).subtract(G.get((L-3)*C+1));if(d.lengthSqr()>1e-8){d=d.normalize();
                    for(int bb=0;bb<2;bb++){double ph=((t*.21+n*.53+bb*.5)%1);Vec3 p0=G.get((L-1)*C+1).add(d.scale(.1+ph*.6)).add(lado.scale(Math.sin(n*3.1+bb)*.08*ph));
                        fita(pose,cam,p0,p0.add(d.scale(.12)),.025,bb==0?BRANCO:CIANO,cr*(1-ph)*.9);}}}
            }
            // chao: anel que pulsa e arcos curtos em volta dos pes
            if(g.noChao){double rr=1.05+.05*Math.sin(t*1.3);List<Vec3> an=new ArrayList<>();for(int i=0;i<=48;i++){double q=2*Math.PI*i/48;an.add(new Vec3(Math.cos(q)*rr,.03,Math.sin(q)*rr));}
                linha(pose,cam,an,.06,cr*.35,AZUL);linha(pose,cam,an,.018,cr*.6,BRANCO);int q_=(int)t;
                for(int kk=0;kk<3;kk++){double a0=2*Math.PI*((kk*.618+q_*.137)%1),r0=.35,r1=.9+.3*((kk*7+q_*3)%5)/5;List<Vec3> pts=new ArrayList<>();
                    for(int i=0;i<6;i++){double u=i/5.,r=r0+(r1-r0)*u,jit=Math.sin(kk*13.1+i*7.7+q_*3.3)*.12*Math.sin(Math.PI*u);pts.add(new Vec3(Math.cos(a0+jit)*r,.03,Math.sin(a0+jit)*r));}
                    raio(pose,cam,pts,cr*.7,.45);}}
            // raios explosivos em volta do envelope, bordas e pontes das linguas
            for(int k=0;k<AuraTecidoKillua.EXPLOSIVOS;k+=2)if(AuraTecidoKillua.alphaExplosivo(k,t)>0)for(List<P> l:List.of(a.raioExplosivo(k,t),a.galhoExplosivo(k,t))){
                List<Vec3> v=mundo(l,o);for(int i=0;i+1<v.size();i++){Vec3 A=v.get(i),B=v.get(i+1);fita(pose,cam,A,B,.2*cr,VIOLETA,cr*.35);fita(pose,cam,A,B,.09*cr,LILAS2,cr*.8);fita(pose,cam,A,B,.035*cr,BRANCO,cr);}}
            for(int n=0;n<AuraTecidoKillua.LINGUAS;n+=2){raio(pose,cam,mundo(a.raioBorda(n,(int)(t/4)%2,t),o),cr*.8,.5);if(n%4==0)raio(pose,cam,mundo(a.raioPonte(n,t),o),cr*.8,.6);}
            // arcos no corpo e descarga ao chao (referencial do corpo)
            for(int k=0;k<KilluaGodspeed.ARCOS_CORPO;k+=2)raio(pose,cam,corpo.pts(KilluaGodspeed.arcoCorpo(k,t)),cr*.9,.7);
            double ad=KilluaGodspeed.alphaDescarga(t);if(ad>.01&&g.noChao)raio(pose,cam,corpo.pts(KilluaGodspeed.descarga(t)),cr*ad,1.0);
        });
    }
    private static List<Vec3> mundo(List<P> l,Vec3 o){List<Vec3> r=new ArrayList<>(l.size());for(P p:l)r.add(new Vec3(p.x()-o.x,p.y()-o.y,p.z()-o.z));return r;}

    private RenderMagiasKillua(){}
}
