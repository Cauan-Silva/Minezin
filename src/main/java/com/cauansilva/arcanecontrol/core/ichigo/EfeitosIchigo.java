package com.cauansilva.arcanecontrol.core.ichigo;
import com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar;
import com.cauansilva.arcanecontrol.core.client.AnimacoesGojo;
import com.cauansilva.arcanecontrol.core.client.AudioAnime;
import com.cauansilva.arcanecontrol.core.audio.RoteiroAudio;
import com.cauansilva.arcanecontrol.core.client.render.*;
import com.cauansilva.arcanecontrol.core.vfx.GetsugaIchigo;
import com.cauansilva.arcanecontrol.core.vfx.KitBaseIchigo;
import com.cauansilva.arcanecontrol.core.vfx.EntradaBankai;
import com.cauansilva.arcanecontrol.core.vfx.MugetsuIchigo;
import com.cauansilva.arcanecontrol.core.vfx.CeroIchigo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class EfeitosIchigo {
    private record Efeito(PacoteEfeitoDespertar p,Object mundo,long inicio){}
    private static final List<Efeito> EFEITOS=new ArrayList<>();
    private static final Vec3 UP=new Vec3(0,1,0);
    private static boolean ativo(UUID id,int tipo){var mc=Minecraft.getInstance();return mc.level!=null&&EFEITOS.stream().anyMatch(f->f.mundo==mc.level&&f.p.tipo()==tipo&&f.p.dono().equals(id)&&mc.level.getGameTime()-f.inicio<f.p.ticks());}
    public static boolean bankai(UUID id){return ativo(id,200)||ativo(id,201);}
    private static final Map<UUID,Long> ENTRADAS=new HashMap<>();
    /** Casaco do Bankai e Tensa Zangetsu: so aparecem quando o corte abre a poeira (EntradaBankai.REVELA). */
    /** Giro do pulso da Zangetsu durante a liberacao (0 fora dela). */
    public static double pulso(UUID id,float parcial){
        double tm=idade(id,530);if(tm>=0)return MugetsuIchigo.pulso(tm+parcial);
        var mc=Minecraft.getInstance();Long t0=ENTRADAS.get(id);if(t0==null||mc.level==null)return 0;
        double t=mc.level.getGameTime()-t0+parcial;return t>EntradaBankai.FIM?0:EntradaBankai.pulso(t);
    }
    private static double idade(UUID id,int tipo){var mc=Minecraft.getInstance();if(mc.level==null)return -1;
        for(var f:EFEITOS)if(f.mundo==mc.level&&f.p.tipo()==tipo&&f.p.dono().equals(id)&&mc.level.getGameTime()-f.inicio<f.p.ticks())return mc.level.getGameTime()-f.inicio;return -1;}
    /** Forma Mugetsu (cabelo longo, faixas) durante a ultimate. */
    public static boolean mugetsu(UUID id){double t=idade(id,530);return t>=0&&MugetsuIchigo.forma(t);}
    public static boolean bankaiVisual(UUID id){
        if(!bankai(id))return false;var mc=Minecraft.getInstance();Long t0=ENTRADAS.get(id);
        return t0==null||mc.level==null||EntradaBankai.trajeVisivel(mc.level.getGameTime()-t0);
    }
    public static boolean mascara(UUID id){return bankai(id)&&ativo(id,522);}
    public static void receber(PacoteEfeitoDespertar p){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(p.tipo()==0){ENTRADAS.remove(p.dono());EFEITOS.removeIf(f->f.p.dono().equals(p.dono()));ImpactosAnime.cancelar(p.dono());AudioAnime.cancelar(p.dono(),null);AnimacoesGojo.cancelar(p.dono());return;}
        if(p.kit()!=6||p.ticks()<1||p.ticks()>1200||!Double.isFinite(p.a().lengthSqr())||!Double.isFinite(p.b().lengthSqr()))return;
        boolean inicio=p.tipo()==200&&!bankai(p.dono());
        if(p.tipo()==200||p.tipo()==201)EFEITOS.removeIf(f->(f.p.tipo()==200||f.p.tipo()==201)&&f.p.dono().equals(p.dono()));
        if(p.tipo()==522)EFEITOS.removeIf(f->f.p.tipo()==522&&f.p.dono().equals(p.dono()));
        if(p.tipo()==530){ImpactosAnime.programar(p.dono(),p.a().add(0,1.2,0),MugetsuIchigo.FORMA,0x101010,.85,18,6);ImpactosAnime.programar(p.dono(),p.a().add(0,2,0),MugetsuIchigo.GOLPE,0x0A0610,1,26,6);}
        if(p.tipo()>=500&&p.tipo()<=513||p.tipo()==530||inicio)AnimacoesGojo.tocarDesperto(p.dono(),6,p.tipo());
        if(inicio){ENTRADAS.put(p.dono(),mc.level.getGameTime());ImpactosAnime.programar(p.dono(),p.a().add(0,1,0),EntradaBankai.GRITO,0xC4355C,.92,20,6);ImpactosAnime.programar(p.dono(),p.a().add(0,1.3,0),EntradaBankai.REVELA,0xD32A50,.8,14,6);AudioAnime.programar(p.dono(),"bankai",p.a(),RoteiroAudio.imediato("ichigo_bankai",48));}
        if(p.tipo()==520||p.tipo()==523||p.tipo()==524){AudioAnime.programar(p.dono(),"getsuga",p.a(),RoteiroAudio.imediato(p.tipo()==520?"ichigo_getsuga":"ichigo_getsuga_bankai",24));ImpactosAnime.programar(p.dono(),p.a(),0,p.tipo()!=520?0xD82C56:0x70CBFF,p.tipo()!=520?.92:.55,18,6);}
        if(p.tipo()==521)ImpactosAnime.programar(p.dono(),p.a(),0,0x89D9FF,.8,16,6);
        if(p.tipo()==527)ImpactosAnime.programar(p.dono(),p.a().add(0,1,0),0,0x89D9FF,.8,16,6);
        // Ichigo's own SFX (tools/build_ichigo_sfx.py); cues start with the effect, impacts are timed inside the sound.
        if(p.tipo()==501||p.tipo()==511)AudioAnime.programar(p.dono(),"shunpo",p.a(),RoteiroAudio.imediato("ichigo_shunpo",8));
        if(p.tipo()==526)AudioAnime.programar(p.dono(),"corte",p.a(),RoteiroAudio.imediato("ichigo_corte_ascendente",16));
        if(p.tipo()==522)AudioAnime.programar(p.dono(),"mascara",p.a(),RoteiroAudio.imediato("ichigo_mascara",18));
        if(p.tipo()==530)AudioAnime.programar(p.dono(),"mugetsu",p.a(),RoteiroAudio.imediato("ichigo_mugetsu",MugetsuIchigo.FIM+4));
        if(p.tipo()==525){ImpactosAnime.programar(p.dono(),p.a().add(0,.5,0),0,0x89D9FF,.85,18,6);AudioAnime.programar(p.dono(),"onda",p.a(),RoteiroAudio.imediato("ichigo_golpe_pesado",32));}
        if(p.tipo()==522)ImpactosAnime.programar(p.dono(),p.a().add(0,1.5,0),0,0xE02F51,.9,18,6);
        if(EFEITOS.size()>=96)EFEITOS.remove(0);EFEITOS.add(new Efeito(p,mc.level,mc.level.getGameTime()));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();EFEITOS.removeIf(f->mc.level==null||f.mundo!=mc.level||mc.level.getGameTime()-f.inicio>=f.p.ticks()||mc.level.getPlayerByUUID(f.p.dono())==null||!mc.level.getPlayerByUUID(f.p.dono()).isAlive());}
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){EFEITOS.clear();ENTRADAS.clear();}
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        double agora=mc.level.getGameTime()+e.getPartialTick();var camera=e.getCamera().getPosition();
        for(var f:EFEITOS){var p=f.p;double t=agora-f.inicio;if(f.mundo!=mc.level||t<0||t>=p.ticks())continue;
            var dono=mc.level.getPlayerByUUID(p.dono());if(dono==null)continue;
            var origem=(p.tipo()==200||p.tipo()==201)?dono.getPosition(e.getPartialTick()):p.a();if(camera.distanceToSqr(origem)>96*96||!DesenhoMagia.iniciar(agora))continue;
            var s=e.getPoseStack();s.pushPose();s.translate(origem.x-camera.x,origem.y-camera.y,origem.z-camera.z);
            var cam=camera.subtract(origem);float fade=(float)Math.min(1,Math.min((t+1)/2,(p.ticks()-t)/5));int cor=(p.tipo()==523||p.tipo()==524||p.tipo()>=510&&p.tipo()<=513||bankai(p.dono()))?0xD32A50:0x6FCFFF;
            try{
                if(p.tipo()==520||p.tipo()==524)getsuga(s,cam,mc,dono,p,t,origem,0);
                else if(p.tipo()==500||p.tipo()==510)carga(s,cam,dono.getPosition(e.getPartialTick()).subtract(origem),t,p.tipo()==510);
                else if(p.tipo()==523){getsuga(s,cam,mc,dono,p,t,origem,Math.toRadians(10));getsuga(s,cam,mc,dono,p,t,origem,Math.toRadians(-80));
                }else if(p.tipo()==501)shunpo(s,cam,p.b().subtract(p.a()),t,false);
                else if(p.tipo()==511)shunpo(s,cam,p.b().subtract(p.a()),t,true);
                else if(p.tipo()==522&&t<CeroIchigo.FORMA+12)formarMascara(s,cam,dono.getEyePosition(e.getPartialTick()).subtract(origem),dono.getLookAngle(),t);
                else if(p.tipo()==525)onda(s,cam,p.b(),t);
                else if(p.tipo()==526)corte(s,cam,p.b(),t);
                else if(p.tipo()==527)lancamento(s,cam,t);
                else if(p.tipo()==530)mugetsu(s,cam,p.b(),t,dono.getPosition(e.getPartialTick()).subtract(origem));
                else if(p.tipo()==521){DesenhoMagia.lote(s,1,true,()->GeometriaCinematica.explosao(s,cam,t,5,cor,fade));
                }else if(p.tipo()==200||p.tipo()==201){
                    DesenhoMagia.lote(s,7,false,()->{for(int i=0;i<6;i++){double a=i*Math.PI/3+t*.03;var v=new Vec3(Math.cos(a)*.65,.1,Math.sin(a)*.65);DesenhoMagia.fita(s,cam,v,v.add(.1,2,.1),.06,0,3,0x19131D,fade*.45f);}});
                    if(p.tipo()==200&&t<EntradaBankai.FIM&&ENTRADAS.containsKey(p.dono()))entrada(s,cam,dono.getLookAngle(),t);
                }else if(p.tipo()!=522){
                    DesenhoMagia.lote(s,1,true,()->{var c=dono.getEyePosition().subtract(origem);DesenhoMagia.anel(s,c,UP,.65,.72,cor,fade*.6f,32);});
                }
            }finally{s.popPose();DesenhoMagia.terminar();}
        }
    }
    // Getsuga Tensho: Shikai azul-branco aditivo; Bankai em tinta preta (material 22) com borda vermelha.
    private static final int TINTA=0x0E0A12,RUBRO=0xD32A50,AZUL=0x5FC4FF,BRANCO=0xEAF8FF;
    private static Vec3 w(Vec3 lado,Vec3 cima,Vec3 dir,GetsugaIchigo.P q,double e){return lado.scale(q.x()*e).add(cima.scale(q.y()*e)).add(dir.scale(q.z()*e));}
    private static void getsuga(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,Minecraft mc,net.minecraft.world.entity.player.Player dono,PacoteEfeitoDespertar p,double t,Vec3 origem,double rolar){
        boolean bk=p.tipo()!=520;var dir=p.b().normalize();var lado0=dir.cross(UP);lado0=lado0.lengthSqr()<1e-8?new Vec3(1,0,0):lado0.normalize();var cima0=lado0.cross(dir).normalize();
        // Roll the frame around the flight direction (Juujishou draws two crescents rolled apart).
        var lado=lado0.scale(Math.cos(rolar)).add(cima0.scale(Math.sin(rolar)));var cima=cima0.scale(Math.cos(rolar)).subtract(lado0.scale(Math.sin(rolar)));
        var hit=mc.level.clip(new ClipContext(origem,origem.add(dir.scale(28)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dono));
        double bloqueio=hit.getType()==HitResult.Type.MISS?28:hit.getLocation().distanceTo(origem),fim=GetsugaIchigo.parada(bloqueio);
        float a=(float)GetsugaIchigo.alpha(t,fim);double e=GetsugaIchigo.escala(t);var c=dir.scale(Math.min(GetsugaIchigo.distancia(t),bloqueio));
        final var L=lado;final var C=cima;
        // Rasgo no chao: so onde o caminho passa rente ao terreno.
        var sulco=new ArrayList<Vec3[]>();var alphas=new ArrayList<Float>();
        for(double z=0;z<=Math.min(bloqueio,GetsugaIchigo.distancia(t));z+=1){
            float as=(float)GetsugaIchigo.alphaSulco(z,t);if(as<.02)continue;var q=origem.add(dir.scale(z));
            var chao=mc.level.clip(new ClipContext(q,q.add(0,-3.2,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dono));if(chao.getType()==HitResult.Type.MISS)continue;
            var h=new Vec3(dir.x,0,dir.z);if(h.lengthSqr()<1e-6)continue;h=h.normalize();var lh=h.cross(UP).normalize().scale(GetsugaIchigo.larguraSulco(z,bk));
            var b=chao.getLocation().add(0,.03,0).subtract(origem);sulco.add(new Vec3[]{b.subtract(lh),b.add(lh),b.add(h.scale(1.02)).add(lh),b.add(h.scale(1.02)).subtract(lh)});alphas.add(as);
        }
        if(!sulco.isEmpty()){
            DesenhoMagia.lote(s,bk?22:1,!bk,()->{for(int i=0;i<sulco.size();i++){var v=sulco.get(i);float as=alphas.get(i);int cs=bk?TINTA:AZUL;DesenhoMagia.ponto(s,v[0],0,0,cs,as);DesenhoMagia.ponto(s,v[1],0,1,cs,as);DesenhoMagia.ponto(s,v[2],1,1,cs,as);DesenhoMagia.ponto(s,v[3],1,0,cs,as);}});
            DesenhoMagia.lote(s,1,true,()->{for(int i=0;i<sulco.size();i++){var v=sulco.get(i);float as=alphas.get(i)*.55f;DesenhoMagia.fita(s,cam,v[0],v[3],.05,0,1,bk?RUBRO:AZUL,as);DesenhoMagia.fita(s,cam,v[1],v[2],.05,0,1,bk?RUBRO:AZUL,as);}});
        }
        if(a<.01)return;
        int n=GetsugaIchigo.PONTOS;var fr=new Vec3[n];var tr=new Vec3[n];
        for(int i=0;i<n;i++){fr[i]=c.add(w(L,C,dir,GetsugaIchigo.frente(i,bk),e));tr[i]=c.add(w(L,C,dir,GetsugaIchigo.tras(i,t,bk),e));}
        double dis=GetsugaIchigo.dissolve(t,fim);
        if(dis<.6){
            float ac=a*(float)(1-dis/.6);
            // Corpo da meia-lua e pos-imagens.
            // Volume 3D da meia-lua (secao em cunha) e pos-imagens no mesmo volume.
            int m=GetsugaIchigo.SECAO;var vol=new Vec3[n][m+1];var lum=new double[n][m+1];var rim=new double[n][m+1];
            for(int i=0;i<n;i++)for(int j=0;j<=m;j++){
                vol[i][j]=c.add(w(L,C,dir,GetsugaIchigo.volume(i,j%m,t,bk),e));var no=GetsugaIchigo.normal(i,j%m,t,bk);var v=cam.subtract(vol[i][j]);
                double vx=v.dot(L),vy=v.dot(C),vz=v.dot(dir);lum[i][j]=GetsugaIchigo.luz(no,vx,vy,vz);rim[i][j]=GetsugaIchigo.borda(no,vx,vy,vz);
            }
            final int base=bk?0x4A3446:BRANCO,eco=bk?TINTA:AZUL;
            DesenhoMagia.lote(s,bk?22:1,!bk,()->{
                for(int k=1;k<=GetsugaIchigo.ECOS;k++){double de=GetsugaIchigo.distanciaEco(k,t);float ae=(float)GetsugaIchigo.alphaEco(k,t,fim)*ac;if(ae<.02)continue;var off=dir.scale(de-Math.min(GetsugaIchigo.distancia(t),bloqueio));
                    for(int i=0;i<n-1;i++)for(int j=0;j<m;j++)quadLuz(s,vol,lum,off,i,j,eco,ae);}
            });
            // Solid core writes depth so the far side of the wedge never shows through the near side.
            if(ac>.95f)com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
            DesenhoMagia.lote(s,bk?22:8,false,()->{for(int i=0;i<n-1;i++)for(int j=0;j<m;j++)quadLuz(s,vol,lum,Vec3.ZERO,i,j,base,ac);});
            com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
            // Borda de raspao (fresnel): contorno que acompanha a silhueta 3D de qualquer angulo.
            DesenhoMagia.lote(s,1,true,()->{for(int i=0;i<n-1;i++)for(int j=0;j<m;j++){int[][] q={{i,j},{i+1,j},{i+1,j+1},{i,j+1}};int u=0;
                for(var p2:q){DesenhoMagia.ponto(s,vol[p2[0]][p2[1]],(u==1||u==2)?(p2[0])*.4:p2[0]*.4,(u>=2)?1:0,bk?RUBRO:AZUL,ac*(float)rim[p2[0]][p2[1]]);u++;}}});
            com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
            DesenhoMagia.lote(s,bk?22:1,!bk,()->{for(int j=0;j<GetsugaIchigo.CHAMAS;j++){Vec3 ant=null;for(int u=0;u<=6;u++){var pt=c.add(w(L,C,dir,GetsugaIchigo.chama(j,u/6d,t,bk),e));if(ant!=null)DesenhoMagia.fita(s,cam,ant,pt,(bk?.2:.16)*(1-u/7d),u/6d,(u+1)/6d,bk?TINTA:AZUL,ac*(float)(1-u/7d));ant=pt;}}});
            // Fio cortante da frente e brilho da borda.
            DesenhoMagia.lote(s,1,true,()->{for(int i=0;i<n-1;i++){DesenhoMagia.fita(s,cam,fr[i],fr[i+1],bk?.07:.1,i/(n-1d),(i+1)/(n-1d),bk?RUBRO:BRANCO,ac);if(bk)DesenhoMagia.fita(s,cam,tr[i],tr[i+1],.05,i/(n-1d),(i+1)/(n-1d),RUBRO,ac*.8f);}
                for(int i=0;i<n-1;i++)DesenhoMagia.fita(s,cam,fr[i],fr[i+1],bk?.35:.45,0,1,bk?RUBRO:AZUL,ac*.25f);});
        }
        if(dis>0){
            // Lascas solidas (cubos) que encolhem enquanto se espalham.
            double tam=.22*(1-dis*.6);
            DesenhoMagia.lote(s,bk?22:8,false,()->{for(int k=0;k<GetsugaIchigo.LASCAS;k++){var q=c.add(w(L,C,dir,GetsugaIchigo.lasca(k,t,fim,bk),e));DesenhoMagia.caixa(s,q,tam,tam*.6,tam,bk?0x2A1A24:BRANCO,a);}});
            if(bk)DesenhoMagia.lote(s,1,true,()->{for(int k=0;k<GetsugaIchigo.LASCAS;k++){var q=c.add(w(L,C,dir,GetsugaIchigo.lasca(k,t,fim,true),e));DesenhoMagia.caixa(s,q,tam*1.3,tam*.8,tam*1.3,RUBRO,a*.35f);}});
        }
    }
    // ---------- Kit base: volumes 3D com luz por normal e borda fresnel ----------
    private static final Vec3 LUZ=new Vec3(.45,.8,-.4).normalize();
    /** Malha (i ao longo, j em volta e fechado) iluminada; 'rim' desenha a borda aditiva nessa cor (0 = sem borda). */
    private static void malha(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,Vec3[][] g,int modo,int cor,int rim,float al,boolean solido){
        int n=g.length,m=g[0].length;var lum=new double[n][m];var fr=new double[n][m];
        for(int i=0;i<n;i++)for(int j=0;j<m;j++){
            var du=g[Math.min(n-1,i+1)][j].subtract(g[Math.max(0,i-1)][j]);var dv=g[i][(j+1)%m].subtract(g[i][(j+m-1)%m]);var no=du.cross(dv);
            no=no.lengthSqr()<1e-12?UP:no.normalize();var vista=cam.subtract(g[i][j]).normalize();double f=Math.pow(1-Math.abs(no.dot(vista)),3);
            lum[i][j]=Math.min(1,.28+.72*Math.abs(no.dot(LUZ))+.4*f);fr[i][j]=f;
        }
        if(solido&&al>.95f)com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
        DesenhoMagia.lote(s,modo,false,()->{for(int i=0;i<n-1;i++)for(int j=0;j<m;j++){int[][] q={{i,j},{i+1,j},{i+1,(j+1)%m},{i,(j+1)%m}};
            for(int k=0;k<4;k++)DesenhoMagia.ponto(s,g[q[k][0]][q[k][1]],q[k][0]*.4,k>=2?1:0,sombra(cor,lum[q[k][0]][q[k][1]]),al);}});
        com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
        if(rim!=0)DesenhoMagia.lote(s,1,true,()->{for(int i=0;i<n-1;i++)for(int j=0;j<m;j++){int[][] q={{i,j},{i+1,j},{i+1,(j+1)%m},{i,(j+1)%m}};
            for(int k=0;k<4;k++)DesenhoMagia.ponto(s,g[q[k][0]][q[k][1]],q[k][0]*.4,k>=2?1:0,rim,al*(float)fr[q[k][0]][q[k][1]]);}});
    }
    private static void cubos(com.mojang.blaze3d.vertex.PoseStack s,List<Vec3> c,List<Double> t,int cor){
        DesenhoMagia.lote(s,8,false,()->{for(int k=0;k<c.size();k++){double d=t.get(k);if(d>.005)DesenhoMagia.caixa(s,c.get(k),d,d,d,cor,1);}});
    }
    private static Vec3[] base(Vec3 olhar){var d=new Vec3(olhar.x,0,olhar.z);d=d.lengthSqr()<1e-6?new Vec3(0,0,1):d.normalize();return new Vec3[]{d.cross(UP).normalize(),UP,d};}
    private static Vec3 local(Vec3[] b,KitBaseIchigo.P q){return b[0].scale(q.x()).add(b[1].scale(q.y())).add(b[2].scale(q.z()));}
    /** X Shunpo: pos-imagens solidas do corpo, riscos de velocidade e poeira em cubos na partida e na chegada. */
    private static void shunpo(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,Vec3 fim,double t,boolean bk){
        var b=base(fim);
        for(int k=0;k<KitBaseIchigo.ECOS;k++){var o=fim.scale(KitBaseIchigo.ecoU(k));float a=(float)KitBaseIchigo.alphaEco(k,t);if(a<.02)continue;
            DesenhoMagia.lote(s,8,false,()->{int cor=bk?0x1A1020:0x2B5C86;
                DesenhoMagia.caixa(s,o.add(0,1.55,0),.25,.25,.25,cor,a);DesenhoMagia.caixa(s,o.add(0,1.05,0),.26,.36,.15,cor,a);
                DesenhoMagia.caixa(s,o.add(b[0].scale(.36)).add(0,1.05,0),.1,.32,.1,cor,a);DesenhoMagia.caixa(s,o.add(b[0].scale(-.36)).add(0,1.05,0),.1,.32,.1,cor,a);
                DesenhoMagia.caixa(s,o.add(b[0].scale(.13)).add(0,.36,0),.11,.36,.12,cor,a);DesenhoMagia.caixa(s,o.add(b[0].scale(-.13)).add(0,.36,0),.11,.36,.12,cor,a);});
        }
        float ar=(float)KitBaseIchigo.alphaRisco(t);
        if(ar>.02)DesenhoMagia.lote(s,1,true,()->{for(int j=0;j<KitBaseIchigo.RISCOS;j++){var o=local(b,KitBaseIchigo.riscoOffset(j));double u0=KitBaseIchigo.riscoU0(j,t),u1=KitBaseIchigo.riscoU1(j,t);
            if(u1>u0)DesenhoMagia.fita(s,cam,o.add(fim.scale(u0)),o.add(fim.scale(u1)),.03,0,1,bk?RUBRO:0x9FDCFF,ar);}});
        var c=new ArrayList<Vec3>();var d=new ArrayList<Double>();
        for(int k=0;k<KitBaseIchigo.POEIRA;k++){c.add(local(b,KitBaseIchigo.poeira(k,t,false)));d.add(KitBaseIchigo.tamanhoPoeira(k,t));c.add(fim.add(local(b,KitBaseIchigo.poeira(k,t,true))));d.add(KitBaseIchigo.tamanhoPoeira(k,t));}
        cubos(s,c,d,0x8A7F70);
        // Aneis de ar em volume na partida e na chegada.
        for(boolean ch:new boolean[]{false,true}){float aa=(float)KitBaseIchigo.alphaAnelAr(t,ch);if(aa<.02)continue;int n=KitBaseIchigo.ANEL_SHUNPO+1,m=KitBaseIchigo.TUBO;var g=new Vec3[n][m];var o=ch?fim:Vec3.ZERO;
            for(int i=0;i<n;i++)for(int j=0;j<m;j++)g[i][j]=o.add(local(b,KitBaseIchigo.anelAr(i%KitBaseIchigo.ANEL_SHUNPO,j,t,ch)));
            malha(s,cam,g,8,bk?0x2A1A24:0xDDF3FF,bk?RUBRO:AZUL,aa,false);}
        // Helices de vento enrolando o caminho, correndo como os riscos.
        if(ar>.02)DesenhoMagia.lote(s,1,true,()->{for(int h=0;h<KitBaseIchigo.HELICES;h++){double u0=KitBaseIchigo.riscoU0(h,t),u1=KitBaseIchigo.riscoU1(h,t);int n=KitBaseIchigo.PONTOS_HELICE;
            for(int i=0;i<n;i++){double a0=u0+(u1-u0)*i/n,a1=u0+(u1-u0)*(i+1)/n;if(a1<=a0)continue;
                DesenhoMagia.fita(s,cam,fim.scale(a0).add(local(b,KitBaseIchigo.helice(h,a0))),fim.scale(a1).add(local(b,KitBaseIchigo.helice(h,a1))),.06*Math.sin(Math.PI*i/n),0,1,bk?RUBRO:0xBFE8FF,ar*.8f);}}});
        // Pos-imagens se desfazendo em flocos.
        var fl=new ArrayList<Vec3>();var ft=new ArrayList<Double>();
        for(int k=0;k<KitBaseIchigo.ECOS;k++)for(int f=0;f<KitBaseIchigo.FLOCOS;f++){fl.add(fim.scale(KitBaseIchigo.ecoU(k)).add(local(b,KitBaseIchigo.floco(k,f,t))));ft.add(KitBaseIchigo.tamanhoFloco(k,f,t));}
        cubos(s,fl,ft,bk?0x2A1A24:0x4F8FC4);
    }
    /** Lancamento do V: espirais de reiatsu em volume subindo com o alvo e estilhacos solidos. */
    private static void lancamento(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,double t){
        float a=(float)KitBaseIchigo.alphaEspiral(t);
        if(a>.02)for(int e=0;e<KitBaseIchigo.ESPIRAIS;e++){int n=KitBaseIchigo.PONTOS_ESPIRAL,m=6;var g=new Vec3[n][m];
            for(int i=0;i<n;i++){var c=KitBaseIchigo.espiral(e,i/(n-1d),t);double r=.09*Math.sin(Math.PI*i/(n-1d))+.01;var d=KitBaseIchigo.espiral(e,Math.min(1,(i+1)/(n-1d)),t);
                var cc=v(c);var tg=v(d).subtract(cc);if(tg.lengthSqr()<1e-9)tg=UP;var x=tg.cross(UP);x=x.lengthSqr()<1e-9?new Vec3(1,0,0):x.normalize();var y=x.cross(tg).normalize();
                for(int j=0;j<m;j++){double q=j*Math.PI*2/m;g[i][j]=cc.add(x.scale(Math.cos(q)*r)).add(y.scale(Math.sin(q)*r));}}
            malha(s,cam,g,8,0xDDF3FF,AZUL,a,false);}
        var c=new ArrayList<Vec3>();var d=new ArrayList<Double>();
        for(int k=0;k<KitBaseIchigo.ESTILHACOS;k++){c.add(v(KitBaseIchigo.estilhaco(k,t)));d.add(KitBaseIchigo.tamanhoEstilhaco(k,t));}
        cubos(s,c,d,BRANCO);
    }
    private static Vec3 e(EntradaBankai.P q,Vec3[] b){return b[0].scale(q.x()).add(b[1].scale(q.y())).add(b[2].scale(q.z()));}
    private static void chamas(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,java.util.function.BiFunction<Integer,Double,Vec3> f,float a){
        DesenhoMagia.lote(s,22,false,()->{for(int j=0;j<EntradaBankai.LINGUAS;j++){Vec3 ant=null;for(int u=0;u<=6;u++){var pt=f.apply(j,u/6d);if(ant!=null)DesenhoMagia.fita(s,cam,ant,pt,.14*(1-u/7d),u/6d,(u+1)/6d,TINTA,a*(float)(1-u/7d));ant=pt;}}});
        DesenhoMagia.lote(s,1,true,()->{for(int j=0;j<EntradaBankai.LINGUAS;j+=2){Vec3 ant=null;for(int u=0;u<=6;u++){var pt=f.apply(j,u/6d);if(ant!=null)DesenhoMagia.fita(s,cam,ant,pt,.04,0,1,RUBRO,a*.7f);ant=pt;}}});
    }
    /** G: liberacao do Bankai (preparo, pilar, poeira, corte da revelacao, aura). Origem nos pes do dono. */
    private static void entrada(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,Vec3 olhar,double t){
        var b=base(olhar);
        float vz=(float)EntradaBankai.vazamento(t);if(vz>.02)chamas(s,cam,(j,u)->e(EntradaBankai.lingua(j,u,t),b),vz*.85f);
        float ap=(float)EntradaBankai.alphaPilar(t);
        if(ap>.02&&EntradaBankai.alturaPilar(t)>.1){var g=new Vec3[EntradaBankai.COLUNA][EntradaBankai.VOLTAS];
            for(int i=0;i<EntradaBankai.COLUNA;i++)for(int j=0;j<EntradaBankai.VOLTAS;j++)g[i][j]=e(EntradaBankai.pilar(i,j,t),b);malha(s,cam,g,22,0x2A1A24,RUBRO,ap,true);}
        float ac=(float)EntradaBankai.alphaChoque(t);
        if(ac>.02&&EntradaBankai.raioChoque(t)>.1){int n=EntradaBankai.ANEL+1;var g=new Vec3[n][EntradaBankai.SECAO];
            for(int i=0;i<n;i++)for(int j=0;j<EntradaBankai.SECAO;j++)g[i][j]=e(EntradaBankai.choque(i%EntradaBankai.ANEL,j,t),b);malha(s,cam,g,22,0x2A1A24,RUBRO,ac,true);}
        for(int k=0;k<EntradaBankai.NUVENS;k++){float an=(float)EntradaBankai.alphaNuvem(k,t)*.9f;if(an<.02||EntradaBankai.raioNuvem(k,t)<.05)continue;
            var g=new Vec3[EntradaBankai.LAT][EntradaBankai.LON];for(int i=0;i<EntradaBankai.LAT;i++)for(int j=0;j<EntradaBankai.LON;j++)g[i][j]=e(EntradaBankai.bolha(k,i,j,t),b);
            malha(s,cam,g,8,0x7D7670,0,an,false);}
        float ak=(float)EntradaBankai.alphaCorte(t);
        if(ak>.02){var g=new Vec3[EntradaBankai.ARCO][EntradaBankai.SECAO_CORTE];
            for(int i=0;i<EntradaBankai.ARCO;i++)for(int j=0;j<EntradaBankai.SECAO_CORTE;j++)g[i][j]=e(EntradaBankai.corte(i,j,t),b);malha(s,cam,g,22,0x2A1A24,RUBRO,ak,true);}
        float au=(float)EntradaBankai.aura(t);if(au>.02)chamas(s,cam,(j,u)->e(EntradaBankai.chama(j,u,t),b),au*.8f);
    }
    private static Vec3 m(MugetsuIchigo.P q,Vec3[] b){return b[0].scale(q.x()).add(b[1].scale(q.y())).add(b[2].scale(q.z()));}
    private static final int TREVA=0x141018;
    /** C no Bankai: Mugetsu (trevas em cor chapada, modo 8: o modo 22 tem veios vermelhos da antimagia do Asta). Implosao e casulo, lamina de trevas, meia-lua colossal, chamas negras e cupula. */
    private static void mugetsu(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam0,Vec3 olhar,double t,Vec3 corpo){
        var b=base(olhar);
        // Chamas negras em volta do corpo (seguem o dono; o corte fica onde foi lancado): nucleo solido + casca translucida + fragmentos.
        s.pushPose();s.translate(corpo.x,corpo.y,corpo.z);var camC=cam0.subtract(corpo);
        float aa=(float)MugetsuIchigo.aura(t);
        if(aa>.02){for(int k=0;k<MugetsuIchigo.LABAREDAS;k++){if(MugetsuIchigo.alturaLabareda(k,t)<.1)continue;int n=MugetsuIchigo.ANEL_CHAMA,mm=MugetsuIchigo.TUBO_CHAMA;var g=new Vec3[n][mm];var h=new Vec3[n][mm];
                for(int i=0;i<n;i++){var cx=m(MugetsuIchigo.eixoLabareda(k,i/(n-1d),t),b);for(int j=0;j<mm;j++){g[i][j]=m(MugetsuIchigo.labareda(k,i,j,t),b);h[i][j]=cx.add(g[i][j].subtract(cx).scale(1.7));}}
                malha(s,camC,g,8,TREVA,0,aa,true);malha(s,camC,h,8,TREVA,0,aa*.28f,false);}
            var fc=new ArrayList<Vec3>();var fd=new ArrayList<Double>();for(int k=0;k<MugetsuIchigo.FRAGMENTOS;k++){fc.add(m(MugetsuIchigo.fragmento(k,t),b));fd.add(MugetsuIchigo.tamanhoFragmento(k,t));}
            cubos(s,fc,fd,TREVA);}
        s.popPose();var cam=cam0;
        // O corte: fio negro finissimo e altissimo, do ceu ao chao, com halo escuro enquanto as trevas vazam dele.
        float af=(float)MugetsuIchigo.alphaLinha(t);double e=MugetsuIchigo.espessuraLinha(t);
        if(af>.02&&e>.001){
            // Thin wall in the cut plane: lateral thickness e, height topoLinha, from LINHA_Z to frenteLinha straight ahead.
            double topoY=MugetsuIchigo.topoLinha(t),z0=MugetsuIchigo.LINHA_Z,z1=MugetsuIchigo.frenteLinha(t);var g=new Vec3[2][4];
            double[][] sec={{-e,0},{e,0},{e,topoY},{-e,topoY}};
            for(int j=0;j<4;j++){g[0][j]=m(new MugetsuIchigo.P(sec[j][0],sec[j][1],z0),b);g[1][j]=m(new MugetsuIchigo.P(sec[j][0],sec[j][1],z1),b);}
            malha(s,cam,g,8,0x000000,0,af,true);
            double h=MugetsuIchigo.halo(t);if(h>.05){var g2=new Vec3[2][4];double[][] s2={{-e*h,0},{e*h,0},{e*h,topoY},{-e*h,topoY}};
                for(int j=0;j<4;j++){g2[0][j]=m(new MugetsuIchigo.P(s2[j][0],s2[j][1],z0),b);g2[1][j]=m(new MugetsuIchigo.P(s2[j][0],s2[j][1],z1),b);}
                malha(s,cam,g2,8,0x000000,0,af*.3f,false);}}
        float al=(float)MugetsuIchigo.alphaLamina(t);
        if(al>.02&&MugetsuIchigo.comprimento(t)>.1){var g=new Vec3[MugetsuIchigo.COLUNA][MugetsuIchigo.VOLTAS];
            for(int i=0;i<MugetsuIchigo.COLUNA;i++)for(int j=0;j<MugetsuIchigo.VOLTAS;j++)g[i][j]=m(MugetsuIchigo.lamina(i,j,t),b);malha(s,cam,g,8,TREVA,0,al*.7f,false);}
        if(al>.02)DesenhoMagia.lote(s,8,false,()->{for(int k=0;k<MugetsuIchigo.LINGUAS;k++)for(int u=0;u<5;u++)
            DesenhoMagia.fita(s,cam,m(MugetsuIchigo.linguaLamina(k,u/5d,t),b),m(MugetsuIchigo.linguaLamina(k,(u+1)/5d,t),b),.09*(1-u/6d),0,1,TREVA,al);});
    }
    private static Vec3 cv(CeroIchigo.P q,Vec3[] b){return b[0].scale(q.x()).add(b[1].scale(q.y())).add(b[2].scale(q.z()));}
    /** Frame for the Cero: like base() but the forward axis keeps the pitch, so the beam goes where the player aims. */
    private static Vec3[] mira(Vec3 olhar){var d=olhar.normalize();var x=d.cross(UP);x=x.lengthSqr()<1e-8?new Vec3(1,0,0):x.normalize();return new Vec3[]{x,x.cross(d).normalize(),d};}
    /** Mask formation (first FORMA ticks of effect 522): white shards converge on the face, a red crack runs across. */
    private static void formarMascara(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,Vec3 olhos,Vec3 olhar,double t){
        var b=mira(new Vec3(olhar.x,0,olhar.z).lengthSqr()<1e-6?new Vec3(0,0,1):new Vec3(olhar.x,0,olhar.z));
        var fc=new ArrayList<Vec3>();var fd=new ArrayList<Double>();
        for(int k=0;k<CeroIchigo.ESTILHACOS;k++){fc.add(olhos.add(cv(CeroIchigo.estilhaco(k,t),b)));fd.add(CeroIchigo.tamanhoEstilhaco(k,t));}
        cubos(s,fc,fd,0xEEEAE2);
        float ra=(float)CeroIchigo.rachadura(t);
        if(ra>.02)DesenhoMagia.lote(s,1,true,()->{var c=olhos.add(b[2].scale(.3));
            for(int k=0;k<3;k++){var a=c.add(b[0].scale(.05+k*.08)).add(b[1].scale(.22-k*.12));DesenhoMagia.fita(s,cam,a,a.add(b[0].scale(.22)).add(b[1].scale(-.1)),.025,0,1,RUBRO,ra);}});
    }
    private static Vec3 v(KitBaseIchigo.P q){return new Vec3(q.x(),q.y(),q.z());}
    /** C: parede de onda em volume, rachaduras radiais, detritos e clarao azul. */
    private static void onda(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,Vec3 olhar,double t){
        var b=base(olhar);float ao=(float)KitBaseIchigo.alphaOnda(t);
        if(ao>.02&&KitBaseIchigo.alturaOnda(t)>.05){int n=KitBaseIchigo.ANEL+1,m=KitBaseIchigo.SECAO_ANEL;var g=new Vec3[n][m];
            for(int i=0;i<n;i++)for(int j=0;j<m;j++)g[i][j]=local(b,KitBaseIchigo.onda(i%KitBaseIchigo.ANEL,j,t));
            malha(s,cam,g,8,0xCFEFFF,AZUL,ao,true);}
        float ar=(float)KitBaseIchigo.alphaRachadura(t);
        if(ar>.02){
            DesenhoMagia.lote(s,22,false,()->{for(int j=0;j<KitBaseIchigo.RACHADURAS;j++)for(int i=0;i<KitBaseIchigo.PONTOS_RACHADURA-1;i++){
                var a=local(b,KitBaseIchigo.rachadura(j,i,t));var c=local(b,KitBaseIchigo.rachadura(j,i+1,t));var l=c.subtract(a).cross(UP);if(l.lengthSqr()<1e-8)continue;l=l.normalize();
                double w0=KitBaseIchigo.larguraRachadura(i),w1=KitBaseIchigo.larguraRachadura(i+1);
                DesenhoMagia.ponto(s,a.add(l.scale(w0)),0,0,TINTA,ar);DesenhoMagia.ponto(s,c.add(l.scale(w1)),1,0,TINTA,ar);DesenhoMagia.ponto(s,c.subtract(l.scale(w1)),1,1,TINTA,ar);DesenhoMagia.ponto(s,a.subtract(l.scale(w0)),0,1,TINTA,ar);}});
            DesenhoMagia.lote(s,1,true,()->{for(int j=0;j<KitBaseIchigo.RACHADURAS;j++)for(int i=0;i<KitBaseIchigo.PONTOS_RACHADURA-1;i++)
                DesenhoMagia.fita(s,cam,local(b,KitBaseIchigo.rachadura(j,i,t)).add(0,.02,0),local(b,KitBaseIchigo.rachadura(j,i+1,t)).add(0,.02,0),KitBaseIchigo.larguraRachadura(i)*.45,0,1,AZUL,ar*.8f);});
        }
        var c=new ArrayList<Vec3>();var d=new ArrayList<Double>();
        for(int k=0;k<KitBaseIchigo.DETRITOS;k++){c.add(local(b,KitBaseIchigo.detrito(k,t)));d.add(KitBaseIchigo.tamanhoDetrito(k,t));}
        cubos(s,c,d,0x6E6458);
        // Fumaca em volume: nuvens iluminadas (sem escrever profundidade, para se misturarem).
        for(int k=0;k<KitBaseIchigo.FUMACAS;k++){float af=(float)KitBaseIchigo.alphaFumaca(k,t)*.85f;if(af<.02||KitBaseIchigo.raioFumaca(k,t)<.05)continue;
            var g=new Vec3[KitBaseIchigo.LAT][KitBaseIchigo.LON];for(int i=0;i<KitBaseIchigo.LAT;i++)for(int j=0;j<KitBaseIchigo.LON;j++)g[i][j]=local(b,KitBaseIchigo.bolha(k,i,j,t));
            malha(s,cam,g,8,k<6?0xB8C7D6:0xA39A8C,k<6?AZUL:0,af,false);}
        float ac=(float)KitBaseIchigo.alphaClarao(t);if(ac>.02)DesenhoMagia.esfera(s,cam,new Vec3(0,.3,0),KitBaseIchigo.clarao(t),15,AZUL,ac*.6f,true,10);
    }
    /** V: arco vertical em volume que sobe na frente do corpo, com duas pos-imagens atras da ponta. */
    private static void corte(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,Vec3 olhar,double t){
        var b=base(olhar);float a=(float)KitBaseIchigo.alphaCorte(t);if(a<.02)return;int n=KitBaseIchigo.ARCO,m=KitBaseIchigo.SECAO;
        for(int k=KitBaseIchigo.ECOS_CORTE;k>=0;k--){double te=KitBaseIchigo.ecoCorte(k,t);if(te<0)continue;var g=new Vec3[n][m];
            for(int i=0;i<n;i++)for(int j=0;j<m;j++)g[i][j]=local(b,KitBaseIchigo.corte(i,j,te));
            if(k==0)malha(s,cam,g,8,BRANCO,AZUL,a,true);else malha(s,cam,g,1,AZUL,0,a*(float)KitBaseIchigo.alphaEcoCorte(k),false);}
    }
    private static int sombra(int cor,double l){return (int)((cor>>16&255)*l)<<16|(int)((cor>>8&255)*l)<<8|(int)((cor&255)*l);}
    private static void quadLuz(com.mojang.blaze3d.vertex.PoseStack s,Vec3[][] v,double[][] l,Vec3 off,int i,int j,int cor,float al){
        int[][] q={{i,j,0,0},{i+1,j,1,0},{i+1,j+1,1,1},{i,j+1,0,1}};
        for(var p:q)DesenhoMagia.ponto(s,v[p[0]][p[1]].add(off),(i+p[2])*.4,p[3],sombra(cor,Math.min(1,l[p[0]][p[1]])),al);
    }
    private static void quad(com.mojang.blaze3d.vertex.PoseStack s,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int i,int cor,float al){
        DesenhoMagia.ponto(s,a,i*.4,0,cor,al);DesenhoMagia.ponto(s,b,(i+1)*.4,0,cor,al);DesenhoMagia.ponto(s,c,(i+1)*.4,1,cor,al);DesenhoMagia.ponto(s,d,i*.4,1,cor,al);
    }
    /** Reiatsu subindo pelo corpo antes do Getsuga (pes em 'pes', relativo a origem do efeito). */
    private static void carga(com.mojang.blaze3d.vertex.PoseStack s,Vec3 cam,Vec3 pes,double t,boolean bk){
        int imp=PerfilIchigo.impacto(0,bk);float k=(float)GetsugaIchigo.carga(t,imp);if(k<.01)return;
        DesenhoMagia.lote(s,bk?22:1,!bk,()->{for(int j=0;j<GetsugaIchigo.LINGUAS;j++){Vec3 ant=null;for(int u=0;u<=6;u++){var q=GetsugaIchigo.lingua(j,u/6d,t,bk);var pt=pes.add(q.x(),q.y(),q.z());if(ant!=null)DesenhoMagia.fita(s,cam,ant,pt,.13*(1-u/7d),u/6d,(u+1)/6d,bk?TINTA:AZUL,k*(float)(1-u/7d)*(bk?.9f:.55f));ant=pt;}}});
        if(bk)DesenhoMagia.lote(s,1,true,()->{for(int j=0;j<GetsugaIchigo.LINGUAS;j+=2){Vec3 ant=null;for(int u=0;u<=6;u++){var q=GetsugaIchigo.lingua(j,u/6d,t,true);var pt=pes.add(q.x(),q.y(),q.z());if(ant!=null)DesenhoMagia.fita(s,cam,ant,pt,.04,0,1,RUBRO,k*.7f);ant=pt;}}});
    }
}
