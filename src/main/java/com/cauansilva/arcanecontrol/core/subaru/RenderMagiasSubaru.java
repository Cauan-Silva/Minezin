package com.cauansilva.arcanecontrol.core.subaru;

import com.cauansilva.arcanecontrol.core.client.render.DesenhoMagia;
import com.cauansilva.arcanecontrol.core.vfx.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Desenho no jogo do kit base do Subaru, com as mesmas classes puras e as mesmas cores das previas aprovadas
 * (tools/preview_subaru_shamak|escudo|instrucao|recuperacao.py). Modo 8 = fumaca/casca escura (alfa normal),
 * modo 1 = brilho aditivo, como nas previas.
 */
final class RenderMagiasSubaru {
    // Shamak
    private static final int ESCURO=0x1A0B2E,VIOLETA=0x9B5CFF,CLARO=0xD9C2FF,NEVOA=0x2A1444;
    // Escudo
    private static final int LUZ_ESP=0xE8FFF6,CIANO=0x8FE8FF,BRANCO=0xFFFFFF,MANA=0x5AA8FF;
    // Instrucao
    private static final int OURO=0xFFC24A,BRANCO_OURO=0xFFF6DC,VENTO=0xBFF2FF;
    // Recuperacao
    private static final int VENENO=0x1F3A14,CINZA=0x5A5F66,VERDE_VENENO=0x7FBF2A,CURA=0x7CFFB0,BRANCO_CURA=0xF2FFF6;
    private static final Vec3 CIMA=new Vec3(0,1,0);

    /** Referencial local: L lateral, U cima, F frente (L = cima x frente, como +x das classes puras). */
    record Base(Vec3 l,Vec3 u,Vec3 f){
        Vec3 em(double x,double y,double z){return l.scale(x).add(u.scale(y)).add(f.scale(z));}
        static Base horizontal(Vec3 frente){Vec3 f=new Vec3(frente.x,0,frente.z);f=f.lengthSqr()<1e-6?new Vec3(0,0,1):f.normalize();return new Base(CIMA.cross(f).normalize(),CIMA,f);}
        static Base olhar(Vec3 frente){Vec3 f=frente.normalize();Vec3 l=CIMA.cross(f);l=l.lengthSqr()<1e-6?new Vec3(1,0,0):l.normalize();return new Base(l,f.cross(l).normalize(),f);}
    }
    private static Vec3 frenteCorpo(Player p,float parcial){double y=Math.toRadians(p.getViewYRot(parcial));return new Vec3(-Math.sin(y),0,Math.cos(y));}
    private static void disco(PoseStack pose,Vec3 cam,Vec3 c,double r,int cor,float a){if(r>.004&&a>.004)DesenhoMagia.anel(pose,c,cam.subtract(c).normalize(),0,r,cor,a,10);}
    private static void fita(PoseStack pose,Vec3 cam,Vec3 a,Vec3 b,double w,int cor,float al){if(al>.004)DesenhoMagia.fita(pose,cam,a,b,w,0,1,cor,al);}

    static void desenhar(PoseStack pose,EfeitosSubaru.Efeito e,double t,Vec3 camMundo,float parcial){
        var mc=Minecraft.getInstance();var dono=mc.level.getPlayerByUUID(e.jogador);if(dono==null)return;
        Vec3 ancora=switch(e.tipo){case PoderesSubaru.ESCUDO,PoderesSubaru.RECUPERACAO->dono.getPosition(parcial);default->e.origem;};
        if(!DesenhoMagia.iniciar(t))return;
        pose.pushPose();Vec3 d=ancora.subtract(camMundo);pose.translate(d.x,d.y,d.z);Vec3 cam=camMundo.subtract(ancora);
        try{
            switch(e.tipo){
                case PoderesSubaru.SHAMAK->shamak(pose,cam,e,t,parcial);
                case PoderesSubaru.ESCUDO->escudo(pose,cam,e,t,Base.horizontal(e.golpe!=null?e.golpe:frenteCorpo(dono,parcial)));
                case PoderesSubaru.INSTRUCAO->instrucao(pose,cam,e,t,parcial);
                case PoderesSubaru.RECUPERACAO->recuperacao(pose,cam,t,Base.horizontal(frenteCorpo(dono,parcial)));
                case PoderesSubaru.MINYA->minya(pose,cam,t,Base.horizontal(e.alvo));
                default->{}
            }
        }finally{pose.popPose();DesenhoMagia.terminar();}
    }

    // ---------------- Z: Shamak ----------------
    private static void shamak(PoseStack pose,Vec3 cam,EfeitosSubaru.Efeito e,double t,float parcial){
        Vec3 ate=e.alvo.subtract(e.origem);double dist=Math.max(.6,ate.length());Base b=Base.olhar(ate);double k=dist/SamakSubaru.ALCANCE;
        java.util.function.Function<SamakSubaru.P,Vec3> W=p->b.em(p.x(),p.y(),p.z()*k);
        float af=(float)SamakSubaru.alphaFios(t),ar=(float)SamakSubaru.alphaRastro(t),ao=(float)SamakSubaru.alphaOrbe(t),at=(float)SamakSubaru.alphaTentaculos(t)*ao;
        DesenhoMagia.lote(pose,8,false,()->{
            if(af>.01)for(int f=0;f<SamakSubaru.FIOS;f++)for(int i=0;i+1<SamakSubaru.PONTOS_FIO;i++){
                double u0=i/(SamakSubaru.PONTOS_FIO-1.),u1=(i+1)/(SamakSubaru.PONTOS_FIO-1.);
                fita(pose,cam,W.apply(SamakSubaru.fio(f,u0,t)),W.apply(SamakSubaru.fio(f,u1,t)),.07*u1+.015,ESCURO,af*.85f);}
            if(ar>.01)for(int r=0;r<SamakSubaru.RASTROS;r++)for(int i=0;i+1<SamakSubaru.PONTOS_RASTRO;i++){
                double u0=i/(SamakSubaru.PONTOS_RASTRO-1.),u1=(i+1)/(SamakSubaru.PONTOS_RASTRO-1.);
                fita(pose,cam,W.apply(SamakSubaru.rastro(r,u0,t)),W.apply(SamakSubaru.rastro(r,u1,t)),.11*(1-u1)+.01,ESCURO,ar*.8f);}
            for(int p=0;p<SamakSubaru.PARTICULAS;p++)disco(pose,cam,W.apply(SamakSubaru.particula(p,t)),SamakSubaru.tamanhoParticula(p,t),ESCURO,.9f);
            if(at>.01)for(int c=0;c<SamakSubaru.TENTACULOS;c++)for(int i=0;i+1<SamakSubaru.PONTOS_TENTACULO;i++){
                double u0=i/(SamakSubaru.PONTOS_TENTACULO-1.),u1=(i+1)/(SamakSubaru.PONTOS_TENTACULO-1.);
                fita(pose,cam,W.apply(SamakSubaru.tentaculo(c,u0,t)),W.apply(SamakSubaru.tentaculo(c,u1,t)),.07*(1-u0)+.008,ESCURO,at*.85f);}
        });
        DesenhoMagia.lote(pose,1,true,()->{
            if(af>.01)for(int f=0;f<SamakSubaru.FIOS;f++)for(int i=0;i+1<SamakSubaru.PONTOS_FIO;i++){
                double u0=i/(SamakSubaru.PONTOS_FIO-1.),u1=(i+1)/(SamakSubaru.PONTOS_FIO-1.);
                fita(pose,cam,W.apply(SamakSubaru.fio(f,u0,t)),W.apply(SamakSubaru.fio(f,u1,t)),.025,VIOLETA,af*(float)u1);}
            if(ar>.01)for(int r=0;r<SamakSubaru.RASTROS;r++)for(int i=0;i+1<SamakSubaru.PONTOS_RASTRO;i++){
                double u0=i/(SamakSubaru.PONTOS_RASTRO-1.),u1=(i+1)/(SamakSubaru.PONTOS_RASTRO-1.);
                fita(pose,cam,W.apply(SamakSubaru.rastro(r,u0,t)),W.apply(SamakSubaru.rastro(r,u1,t)),.03*(1-u1),VIOLETA,ar*.7f);}
            for(int p=0;p<SamakSubaru.PARTICULAS;p++)disco(pose,cam,W.apply(SamakSubaru.particula(p,t)),SamakSubaru.tamanhoParticula(p,t)*1.6,VIOLETA,.25f);
        });
        if(ao>.01){
            var c=SamakSubaru.centro(t);Vec3 centro=W.apply(c);double r=SamakSubaru.RAIO_ORBE*SamakSubaru.escalaOrbe(t);
            DesenhoMagia.esfera(pose,cam,centro,r,8,ESCURO,ao,false,8);
            DesenhoMagia.esfera(pose,cam,centro,r*1.5,8,NEVOA,ao*.32f,false,8);
            DesenhoMagia.esfera(pose,cam,centro,r*1.6,1,VIOLETA,ao*.3f,true,8);
        }
        if(e.acerto==null||t<SamakSubaru.IMPACTO)return;
        // nevoa em volta da cabeca do alvo (segue a entidade se ela ainda existe)
        Vec3 cabeca=e.acerto;
        if(e.idAlvo>=0){Entity alvo=Minecraft.getInstance().level.getEntity(e.idAlvo);if(alvo!=null&&alvo.isAlive())cabeca=alvo.getEyePosition(parcial);}
        Vec3 base=cabeca.subtract(e.origem);Base h=Base.horizontal(ate);var A=SamakSubaru.ALVO;
        java.util.function.Function<SamakSubaru.P,Vec3> M=p->base.add(h.em(p.x()-A.x(),p.y()-A.y(),p.z()-A.z()));
        float an=(float)SamakSubaru.alphaNevoa(t),ae=(float)SamakSubaru.alphaEnlaces(t),aa=(float)SamakSubaru.alphaAneis(t),ac=(float)SamakSubaru.alphaChao(t),aw=(float)SamakSubaru.alphaOnda(t);
        Vec3 pes=base.add(0,-1.55,0);
        DesenhoMagia.lote(pose,8,false,()->{
            if(ac>.01){double r=SamakSubaru.raioChao(t);for(int q=0;q<18;q++){double ang=q*2.399+t*.02,rr=r*Math.sqrt((q+.5)/18);
                disco(pose,cam,pes.add(h.em(Math.cos(ang)*rr,0,Math.sin(ang)*rr)),.28+.1*(q%3),NEVOA,ac*.5f);}}
            if(ae>.01)for(int q=0;q<SamakSubaru.ENLACES;q++)for(int i=0;i+1<SamakSubaru.PONTOS_ENLACE;i++){
                double kk=Math.sin(Math.PI*(i+.5)/SamakSubaru.PONTOS_ENLACE);fita(pose,cam,M.apply(SamakSubaru.enlace(q,i,t)),M.apply(SamakSubaru.enlace(q,i+1,t)),.09*kk+.01,ESCURO,ae*.8f);}
            if(an>.01)for(int q=0;q<SamakSubaru.BOLHAS;q++)disco(pose,cam,M.apply(SamakSubaru.bolha(q,t)),SamakSubaru.tamanhoBolha(q,t),NEVOA,an*.55f);
        });
        DesenhoMagia.lote(pose,1,true,()->{
            if(ae>.01)for(int q=0;q<SamakSubaru.ENLACES;q++)for(int i=0;i+1<SamakSubaru.PONTOS_ENLACE;i++){
                double kk=Math.sin(Math.PI*(i+.5)/SamakSubaru.PONTOS_ENLACE);fita(pose,cam,M.apply(SamakSubaru.enlace(q,i,t)),M.apply(SamakSubaru.enlace(q,i+1,t)),.025*kk,VIOLETA,ae*.5f);}
            if(aa>.01)for(int a=0;a<SamakSubaru.ANEIS;a++)for(int i=0;i+1<SamakSubaru.PONTOS_ANEL;i++)if(i%4!=3)
                fita(pose,cam,M.apply(SamakSubaru.anel(a,i,t)),M.apply(SamakSubaru.anel(a,i+1,t)),.035,VIOLETA,aa);
            if(aw>.01){double r=SamakSubaru.raioOnda(t);for(int i=0;i<40;i++){double a0=2*Math.PI*i/40,a1=2*Math.PI*(i+1)/40;
                fita(pose,cam,base.add(h.l().scale(Math.cos(a0)*r)).add(0,Math.sin(a0)*r,0),base.add(h.l().scale(Math.cos(a1)*r)).add(0,Math.sin(a1)*r,0),.07*(1-aw*.4),CLARO,aw);}}
            if(an>.01)for(int q=0;q<SamakSubaru.BOLHAS;q++)disco(pose,cam,M.apply(SamakSubaru.bolha(q,t)),SamakSubaru.tamanhoBolha(q,t)*1.25,VIOLETA,an*.07f);
        });
    }

    // ---------------- X: Chamado Espiritual de Emergencia ----------------
    private static Vec3 v(Base b,EscudoSubaru.P p){return b.em(p.x(),p.y(),p.z());}
    private static void escudo(PoseStack pose,Vec3 cam,EfeitosSubaru.Efeito e,double t,Base b){
        double q=e.quebra;float ar=(float)EscudoSubaru.alphaRunas(t,q),ae=(float)EscudoSubaru.alphaEspiritos(t),ab=(float)EscudoSubaru.alphaBolha(t,q),cl=(float)EscudoSubaru.clarao(t,q),ao=(float)EscudoSubaru.alphaOnda(t,q);
        if(ab>.01)DesenhoMagia.lote(pose,8,false,()->malhaBolha(pose,cam,b,t,q,MANA,ab*.18f,false,0));
        DesenhoMagia.lote(pose,1,true,()->{
            if(ar>.01)for(int a=0;a<EscudoSubaru.RUNAS;a++)for(int i=0;i<EscudoSubaru.PONTOS_RUNA;i++){
                if(a==1&&i%6>=4)continue;
                fita(pose,cam,v(b,EscudoSubaru.runa(a,i,t)),v(b,EscudoSubaru.runa(a,(i+1)%EscudoSubaru.PONTOS_RUNA,t)),.03+.01*a,a!=1?CIANO:LUZ_ESP,ar*.8f);
                if(a==2&&i%8==0)fita(pose,cam,v(b,EscudoSubaru.runa(a,i,t)),v(b,EscudoSubaru.runa(a,(i+16)%EscudoSubaru.PONTOS_RUNA,t)),.015,CIANO,ar*.6f);
            }
            if(ae>.01)for(int k=0;k<EscudoSubaru.ESPIRITOS;k++){
                for(int i=0;i+1<EscudoSubaru.PONTOS_ESPIRITO;i++){double u0=i/(EscudoSubaru.PONTOS_ESPIRITO-1.),u1=(i+1)/(EscudoSubaru.PONTOS_ESPIRITO-1.);
                    fita(pose,cam,v(b,EscudoSubaru.espirito(k,u0,t)),v(b,EscudoSubaru.espirito(k,u1,t)),.05*u1,CIANO,ae*(float)u1);}
                disco(pose,cam,v(b,EscudoSubaru.espirito(k,1,t)),.07,LUZ_ESP,ae);
            }
            if(ab>.01)malhaBolha(pose,cam,b,t,q,cl<.3f?CIANO:BRANCO,ab,true,cl);
            if(ao>.01){
                Vec3 c=v(b,EscudoSubaru.CENTRO);Vec3 dg=v(b,EscudoSubaru.GOLPE).subtract(c).normalize();Vec3 u=dg.cross(CIMA).normalize(),w=dg.cross(u);double ang=EscudoSubaru.anguloOnda(t,q),R=EscudoSubaru.RAIO;
                Vec3 ant=null;for(int i=0;i<=40;i++){double bb=2*Math.PI*i/40;Vec3 p=c.add(dg.scale(R*Math.cos(ang))).add(u.scale(R*Math.sin(ang)*Math.cos(bb))).add(w.scale(R*Math.sin(ang)*Math.sin(bb)));
                    if(ant!=null)fita(pose,cam,ant,p,.06,BRANCO,ao);ant=p;}
                disco(pose,cam,c.add(dg.scale(R)),.25*ao,BRANCO,ao);
            }
            for(int k=0;k<EscudoSubaru.FRAGMENTOS;k++){double tam=EscudoSubaru.tamanhoFragmento(k,t,q);if(tam>.005)disco(pose,cam,v(b,EscudoSubaru.fragmento(k,t,q)),tam,CIANO,.7f);}
        });
    }
    /** Casca da bolha: no passe escuro, cor e alfa fixos; no aditivo, brilho na borda (rim) + clarao do golpe. */
    private static void malhaBolha(PoseStack pose,Vec3 cam,Base b,double t,double q,int cor,float alfa,boolean rim,float clarao){
        int L=EscudoSubaru.LAT,N=EscudoSubaru.LON;
        for(int i=0;i<L;i++)for(int j=0;j<N;j++){
            int[][] id={{i,j},{i+1,j},{i+1,(j+1)%N},{i,(j+1)%N}};
            for(int[] k:id){
                Vec3 p=v(b,EscudoSubaru.bolha(k[0],k[1],t,q));float a=alfa;
                if(rim){Vec3 n=v(b,EscudoSubaru.normal(k[0],k[1]));Vec3 vv=cam.subtract(p).normalize();double r=Math.pow(1-Math.abs(n.dot(vv)),2.5);
                    double hexa=.5+.5*Math.cos(k[0]*1.7)*Math.cos(k[1]*1.3);a=(float)(alfa*(.2+.8*r+.25*hexa*r)+clarao*.35);}
                DesenhoMagia.ponto(pose,p,k[1]/(double)N,k[0]/(double)L,cor,Math.min(1,a));
            }
        }
    }

    // ---------------- C: Instrucao Tatica ----------------
    private static Vec3 v(InstrucaoSubaru.P p){return new Vec3(p.x(),p.y(),p.z());}
    private static void instrucao(PoseStack pose,Vec3 cam,EfeitosSubaru.Efeito e,double t,float parcial){
        var mc=Minecraft.getInstance();float ap=(float)InstrucaoSubaru.alphaPulso(t);
        DesenhoMagia.lote(pose,1,true,()->{
            if(ap>.01){
                for(int i=0;i<InstrucaoSubaru.PONTOS_ANEL;i++){Vec3 a=v(InstrucaoSubaru.anelPulso(i,t)),c=v(InstrucaoSubaru.anelPulso((i+1)%InstrucaoSubaru.PONTOS_ANEL,t));
                    fita(pose,cam,a,c,.09,OURO,ap);fita(pose,cam,a,c,.03,BRANCO_OURO,ap);}
                for(int k=0;k<InstrucaoSubaru.RAIOS;k++){Vec3 a=v(InstrucaoSubaru.raio(k,0,t)),c=v(InstrucaoSubaru.raio(k,1,t));fita(pose,cam,a.add(c.subtract(a).scale(.6)),c,.03,OURO,ap*.8f);}
            }
            for(int r=0;r<e.recebem.size();r++){
                Player alvo=mc.level.getPlayerByUUID(e.recebem.get(r));if(alvo==null)continue;double chega=e.chegam.get(r);
                float al=(float)InstrucaoSubaru.alphaBuff(t,chega);if(al<.01)continue;
                Vec3 o=alvo.getPosition(parcial).subtract(e.origem);
                for(int i=0;i<InstrucaoSubaru.PONTOS_PE;i++)if(i%4!=3)fita(pose,cam,o.add(v(InstrucaoSubaru.pe(i,t,chega))),o.add(v(InstrucaoSubaru.pe((i+1)%InstrucaoSubaru.PONTOS_PE,t,chega))),.04,OURO,al*.9f);
                for(int k=0;k<InstrucaoSubaru.ESPIRAIS;k++){double frente=InstrucaoSubaru.frenteEspiral(k,t);
                    for(int i=0;i+1<InstrucaoSubaru.PONTOS_ESPIRAL;i++){double u0=i/(InstrucaoSubaru.PONTOS_ESPIRAL-1.),u1=(i+1)/(InstrucaoSubaru.PONTOS_ESPIRAL-1.);
                        double kk=Math.max(0,1-Math.abs(u0-frente)*4);fita(pose,cam,o.add(v(InstrucaoSubaru.espiral(k,u0,t))),o.add(v(InstrucaoSubaru.espiral(k,u1,t))),.015+.03*kk,VENTO,al*(float)(.15+.75*kk));}}
                Vec3 lado=cam.subtract(o).cross(CIMA);lado=lado.lengthSqr()<1e-6?new Vec3(.18,0,0):lado.normalize().scale(.18);
                for(int n=0;n<InstrucaoSubaru.DIVISAS;n++){float ad=(float)InstrucaoSubaru.alphaDivisa(n,t);Vec3 c=o.add(0,InstrucaoSubaru.alturaDivisa(n,t),0);
                    fita(pose,cam,c.subtract(lado),c.add(0,.14,0),.035,OURO,al*ad);fita(pose,cam,c.add(0,.14,0),c.add(lado),.035,OURO,al*ad);}
                float acd=(float)InstrucaoSubaru.acende(t,chega);if(acd>.01)disco(pose,cam,o.add(0,1,0),.9,OURO,acd*.12f);
            }
        });
    }

    // ---------------- V: Recuperacao Psicologica ----------------
    private static Vec3 v(Base b,RecuperacaoSubaru.P p){return b.em(p.x(),p.y(),p.z());}
    private static void recuperacao(PoseStack pose,Vec3 cam,double t,Base b){
        float ax=(float)RecuperacaoSubaru.alphaToxinas(t),apu=(float)RecuperacaoSubaru.alphaPulso(t),acu=(float)RecuperacaoSubaru.alphaCura(t);
        Vec3 peito=v(b,RecuperacaoSubaru.PEITO);
        if(ax>.01)DesenhoMagia.lote(pose,8,false,()->{
            for(int k=0;k<RecuperacaoSubaru.TOXINAS;k++)for(int n=0;n<RecuperacaoSubaru.FUMACAS;n++)
                disco(pose,cam,v(b,RecuperacaoSubaru.fumaca(k,n,t)),RecuperacaoSubaru.tamanhoFumaca(k,n,t),k%3==0?CINZA:VENENO,ax*(float)RecuperacaoSubaru.densidadeFumaca(k,n,t)*.7f);
        });
        DesenhoMagia.lote(pose,1,true,()->{
            if(ax>.01)for(int k=0;k<RecuperacaoSubaru.TOXINAS;k++)for(int n=0;n<RecuperacaoSubaru.FUMACAS;n+=2)
                disco(pose,cam,v(b,RecuperacaoSubaru.fumaca(k,n,t)),RecuperacaoSubaru.tamanhoFumaca(k,n,t)*.6,VERDE_VENENO,ax*.12f);
            if(apu>.01){double r=RecuperacaoSubaru.raioPulso(t);for(int i=0;i<40;i++){double a0=2*Math.PI*i/40,a1=2*Math.PI*(i+1)/40;
                fita(pose,cam,peito.add(Math.cos(a0)*r,0,Math.sin(a0)*r),peito.add(Math.cos(a1)*r,0,Math.sin(a1)*r),.05,BRANCO_CURA,apu);}}
            if(acu>.01){double c=RecuperacaoSubaru.coracao(t);disco(pose,cam,peito,.22*c,CURA,acu*.6f);disco(pose,cam,peito,.1*c,BRANCO_CURA,acu*.8f);
                for(int n=0;n<2;n++){float aa=(float)RecuperacaoSubaru.alphaAnel(n,t);if(aa<.01)continue;
                    for(int i=0;i<RecuperacaoSubaru.PONTOS_ANEL;i++)fita(pose,cam,v(b,RecuperacaoSubaru.anel(n,i,t)),v(b,RecuperacaoSubaru.anel(n,(i+1)%RecuperacaoSubaru.PONTOS_ANEL,t)),.03,CURA,aa*.8f);}
                for(int k=0;k<RecuperacaoSubaru.PARTICULAS;k++)disco(pose,cam,v(b,RecuperacaoSubaru.particula(k,t)),RecuperacaoSubaru.tamanhoParticula(k,t),CURA,.9f);}
        });
    }
    // ---------------- G-Z: Minya (Beatrice) ----------------
    private static final int CRISTAL=0xB98CFF,CRISTAL_ESC=0x5A2FA8,BRILHO=0xF2E8FF,M_VIOLETA=0x9B5CFF;
    private static final Vec3 LUZ=new Vec3(.4,1,.3).normalize();
    private static Vec3 v(Base b,MinyaBeatrice.P p){return b.em(p.x(),p.y(),p.z());}
    private static void tri(PoseStack pose,Vec3 a,Vec3 b,Vec3 c,int cor,float al){DesenhoMagia.ponto(pose,a,0,0,cor,al);DesenhoMagia.ponto(pose,b,1,0,cor,al);DesenhoMagia.ponto(pose,c,1,1,cor,al);DesenhoMagia.ponto(pose,c,0,1,cor,al);}
    private static int sombra(int cor,double k){k=Math.max(0,Math.min(1.4,k));int r=(int)Math.min(255,(cor>>16&255)*k),g=(int)Math.min(255,(cor>>8&255)*k),b=(int)Math.min(255,(cor&255)*k);return r<<16|g<<8|b;}
    /** Bipiramide hexagonal (ponta em base+eixo*comp*.75, cauda em base-eixo*comp*.25), como na previa. brilho=false: faces; true: borda aditiva. */
    static void cristal(PoseStack pose,Vec3 cam,Vec3 base,Vec3 eixo,double comp,double raio,float al,double forca,boolean brilho){
        if(comp<.01)return;eixo=eixo.normalize();Vec3 ref=Math.abs(eixo.y)<.9?CIMA:new Vec3(1,0,0);Vec3 u=eixo.cross(ref).normalize(),w=eixo.cross(u);
        Vec3 ponta=base.add(eixo.scale(comp*.75)),cauda=base.subtract(eixo.scale(comp*.25)),meio=base.add(eixo.scale(comp*.18));Vec3[] an=new Vec3[6];
        for(int i=0;i<6;i++){double a=Math.PI*2*i/6;an[i]=meio.add(u.scale(raio*Math.cos(a))).add(w.scale(raio*Math.sin(a)));}
        for(int i=0;i<6;i++){Vec3 a=an[i],b=an[(i+1)%6];
            for(Vec3[] t:new Vec3[][]{{a,b,ponta},{b,a,cauda}}){
                Vec3 n=t[1].subtract(t[0]).cross(t[2].subtract(t[0]));n=n.lengthSqr()<1e-12?eixo:n.normalize();
                if(!brilho)tri(pose,t[0],t[1],t[2],sombra(i%2==1?CRISTAL:CRISTAL_ESC,.45+.75*Math.max(0,n.dot(LUZ))),al*.85f);
                else{Vec3 vv=cam.subtract(t[0]).normalize();double rim=Math.pow(1-Math.abs(n.dot(vv)),2);tri(pose,t[0],t[1],t[2],BRILHO,(float)(al*(.15+.55*rim)*forca));}
            }
        }
    }
    private static void minya(PoseStack pose,Vec3 cam,double t,Base b){
        float ac=(float)MinyaBeatrice.alphaCirculo(t);double C=MinyaBeatrice.COMPRIMENTO,R=MinyaBeatrice.RAIO;
        for(int passe=0;passe<2;passe++){boolean g=passe==1;
            DesenhoMagia.lote(pose,g?1:8,g,()->{
                for(int k=0;k<MinyaBeatrice.ESTACAS;k++){
                    double es=MinyaBeatrice.escala(k,t);
                    if(es>.01)cristal(pose,cam,v(b,MinyaBeatrice.centro(k,t)),v(b,MinyaBeatrice.direcao(k,t)),C*es,R*es,1,1,g);
                    Vec3 pa=v(b,MinyaBeatrice.pontoAcerto(k));
                    for(int j=0;j<MinyaBeatrice.ESPINHOS;j++){double tam=MinyaBeatrice.tamanhoEspinho(k,j,t);if(tam>.01)cristal(pose,cam,pa,v(b,MinyaBeatrice.espinho(k,j)),tam*1.4,tam*.22,1,.8,g);}
                    for(int c=0;c<MinyaBeatrice.CACOS;c++){double tam=MinyaBeatrice.tamanhoCaco(k,c,t);if(tam<=.005)continue;Vec3 q=v(b,MinyaBeatrice.caco(k,c,t));
                        cristal(pose,cam,q,new Vec3(Math.sin(t+q.x),1,Math.cos(t*1.3+q.z)),tam*2.2,tam*.7,1,.6,g);}
                }
            });
        }
        DesenhoMagia.lote(pose,1,true,()->{
            if(ac>.01)for(int a=0;a<2;a++){int N=MinyaBeatrice.PONTOS_CIRCULO;
                for(int i=0;i<N;i++)fita(pose,cam,v(b,MinyaBeatrice.circulo(a,i,t)),v(b,MinyaBeatrice.circulo(a,(i+1)%N,t)),.025,a==1?M_VIOLETA:BRILHO,ac);
                if(a==1)for(int i=0;i<N;i+=8)fita(pose,cam,v(b,MinyaBeatrice.circulo(a,i,t)),v(b,MinyaBeatrice.circulo(a,(i+16)%N,t)),.015,M_VIOLETA,ac*.7f);}
            for(int k=0;k<MinyaBeatrice.ESTACAS;k++){
                float ra=(float)MinyaBeatrice.alphaRastro(k,t);
                if(ra>.01&&MinyaBeatrice.escala(k,t)>.01){Vec3 c=v(b,MinyaBeatrice.centro(k,t)),d=v(b,MinyaBeatrice.direcao(k,t));
                    for(int q=0;q<6;q++)fita(pose,cam,c.subtract(d.scale(.3+.5*q)),c.subtract(d.scale(.8+.5*q)),.05*(1-q/6.),M_VIOLETA,ra*(1-q/6f));}
                float im=(float)MinyaBeatrice.impacto(k,t);if(im>.01)disco(pose,cam,v(b,MinyaBeatrice.pontoAcerto(k)).add(0,.2,0),.5*(1.4-im),BRILHO,im*.4f);
            }
        });
    }

    // ---------------- G: Contrato com a Beatrice (entrada + aura da forma) ----------------
    private static final int C_VIOLETA=0x9B5CFF,C_CLARO=0xE6D6FF,C_OURO=0xFFD27A,MADEIRA=0x6B3E26,MADEIRA_ESC=0x3E2316,LUZ_PORTA=0xFFF1C9;
    private static Vec3 v(Base b,ContratoSubaru.P p){return b.em(p.x(),p.y(),p.z());}
    private static Vec3 giraY(Vec3 v,double graus){double a=Math.toRadians(graus);return v.scale(Math.cos(a)).add(CIMA.cross(v).scale(Math.sin(a)));}
    /** Frente do referencial da entrada: o yaw do Subaru quando usou o G. */
    static Base baseContrato(EfeitosSubaru.Contrato c){double y=Math.toRadians(c.yaw);return Base.horizontal(new Vec3(-Math.sin(y),0,Math.cos(y)));}
    /** Caixa orientada: centro e os tres semi-eixos. */
    private static void caixa(PoseStack pose,Vec3 c,Vec3 x,Vec3 y,Vec3 z,int cor,float a){
        Vec3[] q=new Vec3[8];for(int i=0;i<8;i++)q[i]=c.add((i&1)==0?x.scale(-1):x).add((i&2)==0?y.scale(-1):y).add((i&4)==0?z.scale(-1):z);
        int[][] f={{0,1,3,2},{4,6,7,5},{0,4,5,1},{2,3,7,6},{0,2,6,4},{1,5,7,3}};
        for(int[] k:f){DesenhoMagia.ponto(pose,q[k[0]],0,0,cor,a);DesenhoMagia.ponto(pose,q[k[1]],1,0,cor,a);DesenhoMagia.ponto(pose,q[k[2]],1,1,cor,a);DesenhoMagia.ponto(pose,q[k[3]],0,1,cor,a);}
    }
    static void contrato(PoseStack pose,EfeitosSubaru.Contrato c,Player dono,double t,Vec3 camMundo,float parcial){
        if(!DesenhoMagia.iniciar(t))return;
        pose.pushPose();Vec3 d=c.centro.subtract(camMundo);pose.translate(d.x,d.y,d.z);Vec3 cam=camMundo.subtract(c.centro);Base b=baseContrato(c);
        try{
            float ac=(float)ContratoSubaru.alphaCirculo(t),cl=(float)ContratoSubaru.clarao(t),ap=(float)ContratoSubaru.alphaPorta(t),af=(float)ContratoSubaru.alphaFio(t),au=(float)ContratoSubaru.aura(t);
            if(ap>.01&&t<ContratoSubaru.FIM)DesenhoMagia.lote(pose,8,false,()->{
                double er=ContratoSubaru.ergue(t),ab=ContratoSubaru.abertura(t),hgt=2.6*er;Vec3 base=v(b,ContratoSubaru.PORTA_POS);
                Vec3 L=giraY(b.l(),-35),F=giraY(b.f(),-35),topo=base.add(0,hgt,0);
                for(int sx=-1;sx<=1;sx+=2)caixa(pose,base.add(L.scale(sx*.72)).add(0,hgt/2,0),L.scale(.09),CIMA.scale(hgt/2),F.scale(.12),MADEIRA_ESC,ap);
                caixa(pose,topo.add(0,.1,0),L.scale(.85),CIMA.scale(.12),F.scale(.14),MADEIRA_ESC,ap);caixa(pose,topo.add(0,.3,0),L.scale(.35),CIMA.scale(.1),F.scale(.1),C_OURO,ap);
                if(hgt>.1)for(int sx=-1;sx<=1;sx+=2){
                    Vec3 Lf=giraY(L,sx*100*ab),Ff=giraY(F,sx*100*ab),dob=base.add(L.scale(sx*.63));
                    caixa(pose,dob.add(Lf.scale(-sx*.31)).add(0,hgt/2,0),Lf.scale(.31),CIMA.scale(hgt/2-.04),Ff.scale(.05),MADEIRA,ap);
                    caixa(pose,dob.add(Lf.scale(-sx*.5)).add(Ff.scale(-.06)).add(0,hgt*.5,0),Lf.scale(.03),CIMA.scale(.03),Ff.scale(.03),C_OURO,ap);
                }
            });
            DesenhoMagia.lote(pose,1,true,()->{
                if(ac>.01){
                    for(int a=0;a<ContratoSubaru.ANEIS;a++)for(int i=0;i<ContratoSubaru.PONTOS_CIRCULO;i++)
                        fita(pose,cam,v(b,ContratoSubaru.circulo(a,i,t)),v(b,ContratoSubaru.circulo(a,(i+1)%ContratoSubaru.PONTOS_CIRCULO,t)),.035+.01*a+.04*cl,a!=1?C_VIOLETA:C_CLARO,ac);
                    for(int k=0;k<ContratoSubaru.ESTRELAS;k++)fita(pose,cam,v(b,ContratoSubaru.estrela(k,t)),v(b,ContratoSubaru.estrela((k+2)%ContratoSubaru.ESTRELAS,t)),.025+.03*cl,C_VIOLETA,ac*.8f);
                    for(int k=0;k<ContratoSubaru.RUNAS;k++){float r=(float)ContratoSubaru.acendeRuna(k,t);if(r<.01)continue;Vec3 q=v(b,ContratoSubaru.runa(k,t));
                        fita(pose,cam,q.subtract(b.l().scale(.1)),q.add(b.l().scale(.1)),.05,C_CLARO,ac*r);fita(pose,cam,q.subtract(b.f().scale(.1)),q.add(b.f().scale(.1)),.05,C_CLARO,ac*r);}
                    if(cl>.01)disco(pose,cam,new Vec3(0,.1,0),1.9*cl+.2,C_VIOLETA,cl*.12f);
                }
                if(ap>.01&&t<ContratoSubaru.FIM){
                    float lz=(float)ContratoSubaru.luzPorta(t);double hgt=2.6*ContratoSubaru.ergue(t);
                    if(lz>.01&&hgt>.1){Vec3 base=v(b,ContratoSubaru.PORTA_POS),L=giraY(b.l(),-35),n=giraY(b.f(),-35).scale(-1),cc=base.add(0,hgt/2,0).add(n.scale(-.02));
                        DesenhoMagia.ponto(pose,cc.subtract(L.scale(.6)).add(0,-hgt/2,0),0,0,LUZ_PORTA,lz*ap*.9f);DesenhoMagia.ponto(pose,cc.add(L.scale(.6)).add(0,-hgt/2,0),1,0,LUZ_PORTA,lz*ap*.9f);
                        DesenhoMagia.ponto(pose,cc.add(L.scale(.6)).add(0,hgt/2,0),1,1,LUZ_PORTA,lz*ap*.9f);DesenhoMagia.ponto(pose,cc.subtract(L.scale(.6)).add(0,hgt/2,0),0,1,LUZ_PORTA,lz*ap*.9f);
                        for(int k=0;k<6;k++)disco(pose,cam,cc.add(n.scale(.3)),(.25+.2*k)*lz,LUZ_PORTA,lz*ap*.05f);}
                }
                Vec3 pe=dono.getPosition(parcial).subtract(c.centro);
                if(af>.01&&c.beatrice!=null){
                    Vec3 A=pe.add(b.l().scale(0)).add(0,1.2,0),B=c.beatrice.subtract(c.centro).add(0,.75,0).subtract(Base.horizontal(new Vec3(-Math.sin(Math.toRadians(dono.getViewYRot(parcial))),0,Math.cos(Math.toRadians(dono.getViewYRot(parcial))))).l().scale(.25));
                    double alc=ContratoSubaru.alcanceFio(t);Vec3 ant=A;
                    for(int i=1;i<=ContratoSubaru.PONTOS_FIO;i++){double u=alc*i/ContratoSubaru.PONTOS_FIO;Vec3 q=A.add(B.subtract(A).scale(u)).add(0,Math.sin(Math.PI*u)*.25+.03*Math.sin(u*12-t*.6),0);
                        fita(pose,cam,ant,q,.03,C_VIOLETA,af);fita(pose,cam,ant,q,.01,C_CLARO,af);ant=q;}
                }
                if(au>.01)for(int k=0;k<14;k++){double a=2*Math.PI*k/14+t*.08,h=((t*.04+k*.37)%1);Vec3 q=pe.add(Math.cos(a)*.55,.1+1.8*h,Math.sin(a)*.55);
                    fita(pose,cam,q,q.add(0,.35,0),.04,C_VIOLETA,au*(float)Math.sin(Math.PI*h)*.7f);}
            });
        }finally{pose.popPose();DesenhoMagia.terminar();}
    }
    private RenderMagiasSubaru(){}
}
