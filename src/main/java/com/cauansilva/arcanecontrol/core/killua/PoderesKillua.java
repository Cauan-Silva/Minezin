package com.cauansilva.arcanecontrol.core.killua;

import com.cauansilva.arcanecontrol.core.combate.Interrupcao;
import com.cauansilva.arcanecontrol.core.combate.InterrupcaoServidor;
import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rede.*;
import com.cauansilva.arcanecontrol.core.rpg.*;
import com.cauansilva.arcanecontrol.core.vfx.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;

/**
 * Kit do Killua no servidor. O cliente (EfeitosKillua) so desenha; alvo, dano, paralisia e teleporte sao decididos
 * aqui, nos ticks das classes puras aprovadas (KilluaBase.*, KilluaRelampagoPerfurante, ...).
 * Pacotes (PacoteSincronizarCutscene3D): 700 Narukami (o = pes, a = ponto do alvo), 701 Echo (o = pes, a = destino),
 * 702 Yo-yo (a = frente), 703 Garras (a = ponto do alvo), 704 faisca de acerto (o = olhos do alvo, a.x = id),
 * 710 Relampago Perfurante, 711 Whirlpool (guarda), 712 contra-ataque (o = pes do atacante, a = direcao),
 * 713 Soco Relampago, 714 Pulso Eletromagnetico, 715 Tempestade Divina (a = pes do alvo), 716 cancelado (a.x = tipo).
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class PoderesKillua {
    public static final int NARUKAMI=700,ECHO=701,YOYO=702,GARRAS=703,ACERTO=704,
            RELAMPAGO=710,WHIRLPOOL=711,CONTRA=712,SOCO=713,PULSO=714,TEMPESTADE=715,CANCELADO=716,GARRAS_GOLPE=717,SANGRA=718;
    /** Recargas base (ticks) de Z/X/C/V, sem e com Godspeed. */
    static final int[] RECARGAS={100,160,300,500},RECARGAS_GODSPEED={100,200,240,400};
    static final float DANO_NARUKAMI=6,DANO_YOYO=3,DANO_GARRAS=10,DANO_SANGRA=1,DANO_RELAMPAGO=8,DANO_CONTRA=10,DANO_SOCO=12,
            DANO_PULSO=6,DANO_TEMPESTADE_GOLPE=2,DANO_TEMPESTADE_FINAL=20;
    static final double ALCANCE_MIRA=8,LARGURA_RELAMPAGO=1.3;
    private static final class Pendente{
        final ServerPlayer p;final ServerLevel nivel;final int tipo;final long inicio;final Vec3 origem,dir;LivingEntity alvo;
        final Set<Integer> acertados=new HashSet<>();
        Pendente(ServerPlayer p,int tipo,long inicio,Vec3 origem,Vec3 dir,LivingEntity alvo){this.p=p;nivel=p.serverLevel();this.tipo=tipo;this.inicio=inicio;this.origem=origem;this.dir=dir;this.alvo=alvo;}
    }
    private static final List<Pendente> PENDENTES=new ArrayList<>();

    private static void enviar(ServerPlayer p,int tipo,Vec3 o,Vec3 a){
        RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new PacoteSincronizarCutscene3D(tipo,p.getUUID(),o.x,o.y,o.z,a.x,a.y,a.z));
    }
    private static void mensagem(ServerPlayer p,String s){p.displayClientMessage(Component.literal(s),true);}
    static Vec3 horizontal(Vec3 v){Vec3 h=new Vec3(v.x,0,v.z);return h.lengthSqr()<1e-6?new Vec3(0,0,1):h.normalize();}

    public static void executar(ServerPlayer p,int slot){
        if(slot<0||slot>3)return;
        if(DespertaresServidor.ativo(p)){godspeed(p,slot);return;}
        if(!RpgServidor.conjurar(p,PoderAnime.KILLUA,slot,RECARGAS[slot]))return;
        long agora=RpgServidor.agora(p);Vec3 pes=p.position(),frente=horizontal(p.getLookAngle());
        switch(slot){
            case 0->{var alvo=mirar(p,KilluaBase.ALVO+1);Vec3 ponto=alvo!=null?alvo.position():chao(p,pes.add(frente.scale(KilluaBase.ALVO)));
                var a=new Pendente(p,NARUKAMI,agora,pes,horizontal(ponto.subtract(pes)),alvo);PENDENTES.add(a);
                canal(a,KilluaBase.Narukami.FIM,Interrupcao.Resistencia.FIRME);
                p.setDeltaMovement(a.dir.scale(.75).add(0,.6,0));p.hurtMarked=true;enviar(p,NARUKAMI,pes,ponto);}
            case 1->{Vec3 destino=livre(p,pes,frente,KilluaBase.Echo.DISTANCIA);PENDENTES.add(new Pendente(p,ECHO,agora,pes,destino,null));enviar(p,ECHO,pes,destino);}
            case 2->{PENDENTES.add(new Pendente(p,YOYO,agora,pes,frente,null));enviar(p,YOYO,pes,frente);}
            default->{// furtividade: o proximo golpe dele (ou o fim da janela, se houver alvo perto) vira o corte critico
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,KilluaBase.Garras.GOLPE,0,false,false));esquecer(p);
                RpgServidor.dados(p).putLong("kil_garras_ate",agora+KilluaBase.Garras.GOLPE);
                PENDENTES.add(new Pendente(p,GARRAS,agora,pes,frente,null));enviar(p,GARRAS,pes,pes.add(frente.scale(KilluaBase.ALVO)));}
        }
        RpgServidor.uso(p,PoderAnime.KILLUA);
        ProgressaoPoderes.registrarUso(p,PoderAnime.KILLUA,slot,new int[]{KilluaBase.Narukami.FIM,KilluaBase.Echo.FIM,KilluaBase.Yoyo.FIM,KilluaBase.Garras.FIM}[slot]);
    }

    /** Tecnicas do Godspeed (Z/X/C/V da forma). */
    private static void godspeed(ServerPlayer p,int slot){
        if(!RpgServidor.conjurar(p,PoderAnime.KILLUA,slot,RECARGAS_GODSPEED[slot]))return;
        long agora=RpgServidor.agora(p);Vec3 pes=p.position(),frente=horizontal(p.getLookAngle());
        switch(slot){
            case 0->{PENDENTES.add(new Pendente(p,RELAMPAGO,agora,pes,frente,null));enviar(p,RELAMPAGO,pes,frente);}
            case 1->{InterrupcaoServidor.guardar(p,"killua_whirlpool",KilluaWhirlpool.ENTRADA+KilluaWhirlpool.JANELA,atq->contra(p,atq));enviar(p,WHIRLPOOL,pes,frente);}
            case 2->{PENDENTES.add(new Pendente(p,SOCO,agora,pes,frente,null));enviar(p,SOCO,pes,frente);}
            default->{PENDENTES.add(new Pendente(p,PULSO,agora,pes,frente,null));enviar(p,PULSO,pes,frente);}
        }
        RpgServidor.uso(p,PoderAnime.KILLUA);
        ProgressaoPoderes.registrarUso(p,PoderAnime.KILLUA,slot,new int[]{KilluaRelampagoPerfurante.FIM,KilluaWhirlpool.FIM,KilluaSocoRelampago.FIM,KilluaPulsoEletromagnetico.FIM}[slot]);
    }

    /** G durante o Godspeed: Tempestade Divina no alvo mirado; encerra a forma no fim. Devolve true se tratou o G. */
    public static boolean ultimate(ServerPlayer p){
        if(!DespertaresServidor.ativo(p))return false;
        for(var a:PENDENTES)if(a.p==p&&a.tipo==TEMPESTADE)return true;
        var alvo=mirar(p,ALCANCE_MIRA);
        if(alvo==null){mensagem(p,"Mire em um inimigo a até oito blocos para a Tempestade Divina.");return true;}
        var a=new Pendente(p,TEMPESTADE,RpgServidor.agora(p),p.position(),horizontal(alvo.position().subtract(p.position())),alvo);PENDENTES.add(a);
        canal(a,KilluaTempestadeDivina.FIM,Interrupcao.Resistencia.IMUNE);
        InterrupcaoServidor.interromper(alvo,p,KilluaTempestadeDivina.FORCA);
        enviar(p,TEMPESTADE,p.position(),alvo.position());RpgServidor.uso(p,PoderAnime.KILLUA);
        ProgressaoPoderes.registrarUso(p,PoderAnime.KILLUA,4,KilluaTempestadeDivina.FIM);
        return true;
    }

    private static void canal(Pendente a,int duracao,Interrupcao.Resistencia r){
        InterrupcaoServidor.iniciar(a.p,"killua_"+a.tipo,duracao,r,q->{PENDENTES.remove(a);enviar(a.p,CANCELADO,a.p.position(),new Vec3(a.tipo,0,0));});
    }

    // ---------------- alvos ----------------
    private static boolean inimigo(ServerPlayer p,LivingEntity v){return v!=p&&v.isAlive()&&!v.isAlliedTo(p)&&!(v instanceof Player o&&(o.isSpectator()||o.isCreative()));}
    /** Inimigo mais alinhado com a mira, ate `alcance` blocos, com linha de visao. */
    static LivingEntity mirar(ServerPlayer p,double alcance){
        Vec3 olho=p.getEyePosition(),olhar=p.getLookAngle();LivingEntity melhor=null;double nota=-1;
        for(var v:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(alcance),v->inimigo(p,v))){
            Vec3 d=v.getBoundingBox().getCenter().subtract(olho);double dist=d.length();if(dist>alcance||dist<1e-3)continue;
            double c=d.scale(1/dist).dot(olhar);if(c<.9||!p.hasLineOfSight(v))continue;
            if(c-dist*.01>nota){nota=c-dist*.01;melhor=v;}
        }
        return melhor;
    }
    private static Vec3 chao(ServerPlayer p,Vec3 ponto){
        var b=p.serverLevel().clip(new ClipContext(ponto.add(0,1.5,0),ponto.add(0,-4,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        return b.getType()==HitResult.Type.MISS?ponto:b.getLocation();
    }
    /** Ponto ate `dist` blocos a frente sem atravessar parede (para no ultimo trecho livre). */
    private static Vec3 livre(ServerPlayer p,Vec3 pes,Vec3 frente,double dist){
        Vec3 meio=pes.add(0,.9,0),fim=meio.add(frente.scale(dist));
        var b=p.serverLevel().clip(new ClipContext(meio,fim,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        double ate=b.getType()==HitResult.Type.MISS?dist:Math.max(0,b.getLocation().distanceTo(meio)-.6);
        return pes.add(frente.scale(ate));
    }
    private static void teleportar(ServerPlayer p,Vec3 destino,Vec3 frente){
        float yaw=(float)Math.toDegrees(Math.atan2(-frente.x,frente.z));
        p.teleportTo(p.serverLevel(),destino.x,destino.y,destino.z,yaw,p.getXRot());p.fallDistance=0;
    }
    /** Mobs que o miravam perdem o alvo (pos-imagens / furtividade confundem a IA). */
    private static void esquecer(ServerPlayer p){
        for(var m:p.serverLevel().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(16),m->m.getTarget()==p))m.setTarget(null);
    }
    private static void ferir(Pendente a,LivingEntity alvo,float dano,boolean empurra){
        Vec3 v=alvo.getDeltaMovement();alvo.invulnerableTime=0;alvo.hurt(a.nivel.damageSources().indirectMagic(a.p,a.p),dano);
        if(!empurra){alvo.setDeltaMovement(v);alvo.hurtMarked=true;}
        enviar(a.p,ACERTO,alvo.getEyePosition(),new Vec3(alvo.getId(),0,0));
    }
    /** Paralisia eletrica: parado e sem pular (amplificador alto de lentidao, como o GDD pede). */
    private static void paralisar(LivingEntity alvo,int ticks){
        alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,ticks,9,false,true));
        alvo.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,ticks,1,false,true));
        if(!(alvo instanceof Player)){alvo.setDeltaMovement(0,Math.min(0,alvo.getDeltaMovement().y),0);alvo.hurtMarked=true;}
    }
    private static List<LivingEntity> raio(Pendente a,Vec3 c,double r){
        return a.nivel.getEntitiesOfClass(LivingEntity.class,new AABB(c,c).inflate(r,2,r),v->inimigo(a.p,v)&&v.position().distanceToSqr(c)<=r*r);
    }

    // ---------------- contra-ataque do Whirlpool ----------------
    private static void contra(ServerPlayer p,UUID atacante){
        if(atacante==null||!(p.serverLevel().getEntity(atacante) instanceof LivingEntity atq)||!atq.isAlive())return;
        Vec3 d=horizontal(atq.position().subtract(p.position()));
        var a=new Pendente(p,CONTRA,RpgServidor.agora(p),atq.position(),d,atq);PENDENTES.add(a);
        enviar(p,CONTRA,atq.position(),d);
    }

    // ---------------- tick ----------------
    private static boolean passo(Pendente a,long t){
        var p=a.p;
        switch(a.tipo){
            case NARUKAMI->{
                if(t==KilluaBase.Narukami.IMPACTO){Vec3 c=a.alvo!=null&&a.alvo.isAlive()?a.alvo.position():a.origem.add(a.dir.scale(KilluaBase.ALVO));
                    for(var v:raio(a,c,2.1)){ferir(a,v,DANO_NARUKAMI,false);paralisar(v,KilluaBase.Narukami.PARALISIA);}}
                if(t>=KilluaBase.Narukami.FIM){InterrupcaoServidor.concluir(p,"killua_"+a.tipo);return true;}
            }
            case ECHO->{
                if(t==KilluaBase.Echo.PREPARO)esquecer(p);
                if(t==KilluaBase.Echo.TELEPORTE)teleportar(p,a.dir,horizontal(a.dir.subtract(a.origem)));
                return t>=KilluaBase.Echo.FIM;
            }
            case YOYO->{
                int ini=KilluaBase.Yoyo.LANCA,fim=ini+KilluaBase.Yoyo.GIRO;
                if(t>=ini&&t<fim&&(t-ini)%5==0)for(var v:raio(a,p.position(),KilluaBase.Yoyo.RAIO)){
                    ferir(a,v,DANO_YOYO,false);
                    // puxa os menores para perto (nao chefes nem jogadores)
                    if(!(v instanceof Player)&&v.getBbWidth()<=1&&v.getMaxHealth()<=40){Vec3 d=p.position().subtract(v.position());double dist=d.length();
                        if(dist>KilluaBase.Yoyo.PUXA_ATE){v.setDeltaMovement(d.scale(.25/dist).add(0,.1,0));v.hurtMarked=true;}}
                }
                return t>=KilluaBase.Yoyo.FIM;
            }
            case GARRAS->{
                if(!furtivo(p))return true;                                   // ja golpeou (critico)
                if(t>=KilluaBase.Garras.GOLPE){var v=mirar(p,3.5);if(v!=null)golpeGarras(p,v,DANO_GARRAS);else sairFurtivo(p);return true;}
                return false;
            }
            case SANGRA->{
                // sangramento: 1 de dano a cada 10 ticks durante Garras.SANGRA
                if(a.alvo==null||!a.alvo.isAlive())return true;
                if(t>0&&t%10==0)ferir(a,a.alvo,DANO_SANGRA,false);
                return t>=KilluaBase.Garras.SANGRA;
            }
            case RELAMPAGO->{
                // o raio passa pelo caminho entre o preparo e o fim do disparo: acerta cada um uma vez
                if(t>=KilluaRelampagoPerfurante.PREPARO&&t<=KilluaRelampagoPerfurante.FIM_DISPARO){
                    double u=Math.min(1,(t-KilluaRelampagoPerfurante.PREPARO+1)/(double)KilluaRelampagoPerfurante.DISPARO);
                    Vec3 fim=livre(p,a.origem,a.dir,KilluaRelampagoPerfurante.ALCANCE);double ate=fim.distanceTo(a.origem)*u;
                    for(var v:a.nivel.getEntitiesOfClass(LivingEntity.class,new AABB(a.origem,a.origem.add(a.dir.scale(ate))).inflate(LARGURA_RELAMPAGO,1.5,LARGURA_RELAMPAGO),v->inimigo(p,v))){
                        Vec3 rel=v.position().subtract(a.origem);double z=rel.dot(a.dir);if(z<0||z>ate)continue;
                        if(rel.subtract(a.dir.scale(z)).horizontalDistance()>LARGURA_RELAMPAGO||!a.acertados.add(v.getId()))continue;
                        ferir(a,v,DANO_RELAMPAGO,false);paralisar(v,20);
                    }
                    if(t==KilluaRelampagoPerfurante.FIM_DISPARO)teleportar(p,fim,a.dir);
                }
                return t>=KilluaRelampagoPerfurante.FIM;
            }
            case CONTRA->{
                if(t==KilluaWhirlpool.SUMICO&&a.alvo!=null&&a.alvo.isAlive())teleportar(p,a.alvo.position().add(a.dir.scale(KilluaWhirlpool.ATRAS)),a.dir.scale(-1));
                if(t==KilluaWhirlpool.GOLPE-KilluaWhirlpool.ACERTO&&a.alvo!=null&&a.alvo.isAlive()){
                    ferir(a,a.alvo,DANO_CONTRA,true);paralisar(a.alvo,40);
                    a.alvo.setDeltaMovement(a.dir.scale(.6).add(0,.45,0));a.alvo.hurtMarked=true;}
                return t>=KilluaWhirlpool.FIM-KilluaWhirlpool.ACERTO;
            }
            case SOCO->{
                if(t==KilluaSocoRelampago.CARGA){p.setDeltaMovement(a.dir.scale(.7));p.hurtMarked=true;}
                if(t==KilluaSocoRelampago.IMPACTO){Vec3 c=p.position().add(a.dir.scale(1.6));
                    for(var v:raio(a,c,1.9)){
                        if(v instanceof Player o&&o.isBlocking())o.disableShield(true);
                        ferir(a,v,DANO_SOCO,true);InterrupcaoServidor.interromper(v,p,KilluaSocoRelampago.FORCA);
                        v.setDeltaMovement(a.dir.scale(.9).add(0,.25,0));v.hurtMarked=true;}}
                return t>=KilluaSocoRelampago.FIM;
            }
            case PULSO->{
                if(t>=KilluaPulsoEletromagnetico.PULSO&&t<=KilluaPulsoEletromagnetico.PULSO+KilluaPulsoEletromagnetico.EXPANSAO){
                    double r=KilluaPulsoEletromagnetico.RAIO*Math.min(1,(t-KilluaPulsoEletromagnetico.PULSO+1)/(double)KilluaPulsoEletromagnetico.EXPANSAO);
                    for(var v:raio(a,a.origem,r))if(a.acertados.add(v.getId())){
                        ferir(a,v,DANO_PULSO,false);paralisar(v,60);InterrupcaoServidor.interromper(v,p,KilluaPulsoEletromagnetico.FORCA);}
                }
                return t>=KilluaPulsoEletromagnetico.FIM;
            }
            case TEMPESTADE->{
                var v=a.alvo;
                if(v==null||!v.isAlive()){if(t<KilluaTempestadeDivina.IMPACTO){fimTempestade(a);return true;}}
                else{
                    if(t<KilluaTempestadeDivina.IMPACTO){paralisar(v,5);v.fallDistance=0;}
                    if(t>=KilluaTempestadeDivina.PRENDER&&t<KilluaTempestadeDivina.FIM_GOLPES&&(t-KilluaTempestadeDivina.PRENDER)%KilluaTempestadeDivina.INTERVALO==0)ferir(a,v,DANO_TEMPESTADE_GOLPE,false);
                    if(t==KilluaTempestadeDivina.IMPACTO){for(var o:raio(a,v.position(),KilluaTempestadeDivina.CRATERA))ferir(a,o,o==v?DANO_TEMPESTADE_FINAL:DANO_TEMPESTADE_FINAL/2,true);
                        v.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);}
                }
                p.fallDistance=0;
                if(t>=KilluaTempestadeDivina.FIM){fimTempestade(a);return true;}
            }
            default->{return true;}
        }
        return false;
    }
    private static void fimTempestade(Pendente a){InterrupcaoServidor.concluir(a.p,"killua_"+a.tipo);if(DespertaresServidor.ativo(a.p))DespertaresServidor.cancelar(a.p);}

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var it=PENDENTES.iterator();
        while(it.hasNext()){
            var a=it.next();var p=a.p;
            if(!p.isAlive()||p.hasDisconnected()||p.serverLevel()!=a.nivel){it.remove();continue;}
            if(passo(a,RpgServidor.agora(p)-a.inicio))it.remove();
        }
    }
    /** Godspeed: velocidade extrema enquanto a forma dura (renovada a cada segundo). */
    @SubscribeEvent public static void forma(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||p.tickCount%20!=0)return;
        if(ProgressaoPoderes.ler(p).ativo()!=PoderAnime.KILLUA||!DespertaresServidor.ativo(p))return;
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,30,2,false,false,true));
        p.addEffect(new MobEffectInstance(MobEffects.JUMP,30,1,false,false,true));
    }
    static boolean furtivo(ServerPlayer p){return RpgServidor.dados(p).getLong("kil_garras_ate")>RpgServidor.agora(p);}
    private static void sairFurtivo(ServerPlayer p){RpgServidor.dados(p).putLong("kil_garras_ate",0);p.removeEffect(MobEffects.INVISIBILITY);}
    /** Corte critico das Garras no alvo real: VFX ancorado nele (o cliente poe o alvo em KilluaBase.ALVO), depois sangra. */
    private static void golpeGarras(ServerPlayer p,LivingEntity alvo,float dano){
        sairFurtivo(p);Vec3 d=horizontal(alvo.position().subtract(p.position()));
        float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));p.setYRot(yaw);p.setYHeadRot(yaw);p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
        var a=new Pendente(p,SANGRA,RpgServidor.agora(p),p.position(),d,alvo);
        if(dano>0){ferir(a,alvo,dano,true);}
        PENDENTES.add(a);enviar(p,GARRAS_GOLPE,p.position(),alvo.position());
    }
    /** Garras: o primeiro golpe corpo a corpo dele enquanto furtivo vira o corte critico (minimo DANO_GARRAS, +50%). */
    @SubscribeEvent public static void critico(LivingHurtEvent e){
        if(!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getSource().getDirectEntity()!=p||!furtivo(p)||ProgressaoPoderes.ler(p).ativo()!=PoderAnime.KILLUA)return;
        e.setAmount(Math.max(DANO_GARRAS,e.getAmount()*1.5f));golpeGarras(p,e.getEntity(),0);
    }
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e){PENDENTES.clear();}
    private PoderesKillua(){}
}
