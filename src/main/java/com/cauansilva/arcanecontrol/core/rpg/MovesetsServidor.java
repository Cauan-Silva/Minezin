package com.cauansilva.arcanecontrol.core.rpg;

import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.cid.*;
import com.cauansilva.arcanecontrol.core.frieren.PoderesFrieren;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.world.effect.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Unico roteador de teclado/cajado. O cliente nunca escolhe alvos, dano ou forma. */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class MovesetsServidor {
    private record Acao(ServerPlayer p,ServerLevel nivel,int tipo,Vec3 centro,Vec3 direcao,long inicio,int duracao,boolean forma){}
    private static final List<Acao> ACOES=new ArrayList<>();
    private static boolean valido(Acao a){return a.p.isAlive()&&!a.p.hasDisconnected()&&a.p.serverLevel()==a.nivel&&(!a.forma||DespertaresServidor.ativo(a.p));}
    public static void executar(ServerPlayer p,int id){
        var kit=PoderAnime.daTecnica(id);if(kit==null)return;int slot=id-(kit.id-1)*10-1;
        if(slot==4&&DespertaresServidor.ativo(p)&&ProgressaoPoderes.ler(p).ativo()==kit){DespertaresServidor.cancelar(p);return;}
        if(!ProgressaoPoderes.podeUsar(p,id))return;
        if(slot==4){DespertaresServidor.ativar(p,kit);return;}
        if(ACOES.size()>=256){mensagem(p,"Aguarde as técnicas ativas terminarem.");return;}
        if(PoderesCid.dados(p).getLong("cid_ocupado")>RpgServidor.agora(p))return;
        if(kit==PoderAnime.CID&&!p.getMainHandItem().is(EspadaCid.ESPADA.get())){p.displayClientMessage(Component.literal("Segure a Lâmina Shadow."),true);return;}
        if(kit==PoderAnime.ICHIGO){com.cauansilva.arcanecontrol.core.ichigo.PoderesIchigo.executar(p,slot);return;}
        if(kit==PoderAnime.ASTA){com.cauansilva.arcanecontrol.core.asta.PoderesAsta.executar(p,slot);return;}
        if(DespertaresServidor.ativo(p)){transformada(p,kit,slot);return;}
        if(kit==PoderAnime.GOJO){
            if(slot==2){teleporte(p,kit,slot,300,null);return;}
            if(RpgServidor.conjurar(p,kit,slot,new int[]{100,200,300,1000}[slot])){
                com.cauansilva.arcanecontrol.core.magia.GerenciadorMagiasGojo.executarEfeito(p,slot+1);RpgServidor.uso(p,kit);
            }
        }else if(kit==PoderAnime.CID){
            if(slot<3){PoderesCid.executar(p,id);return;}
            var alvo=mirar(p,5);if(alvo==null){mensagem(p,"Mire em um inimigo a até cinco blocos.");return;}
            if(RpgServidor.conjurar(p,kit,slot,600)){alvo.hurt(p.damageSources().playerAttack(p),24);fx(p,31,p.getEyePosition(),alvo.getEyePosition(),20);RpgServidor.uso(p,kit);}
        }else{
            if(slot<3){PoderesFrieren.executar(p,id);return;}
            if(RpgServidor.conjurar(p,kit,slot,600))agendar(p,30,pontoMira(p,18),Vec3.ZERO,120,false);
        }
    }
    private static void transformada(ServerPlayer p,PoderAnime kit,int slot){
        var f=DespertaresServidor.forma(p);var centro=f.centro();
        if(kit==PoderAnime.GOJO){
            if(p.distanceToSqr(centro)>26*26){mensagem(p,"Retorne ao domo para usar o moveset desperto.");return;}
            if(slot==0){mensagem(p,"Morte Cerebral ativa: mantenha o olhar sobre um inimigo no domo.");return;}
            if(slot==1){teleporte(p,kit,slot,20,centro);return;}
            if(slot==3&&f.estado().exclusivoUsado()){mensagem(p,"Roxo Máximo já usado neste domínio.");return;}
            if(!RpgServidor.conjurar(p,kit,slot,slot==2?100:0))return;
            if(slot==2){
                for(var alvo:inimigos(p,centro,26)){alvo.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100,0));alvo.addEffect(new MobEffectInstance(MobEffects.WITHER,100,1));}
                fx(p,40,centro,Vec3.ZERO,70);
            }else{f.estado().consumirExclusivo(RpgServidor.agora(p));var ponto=pontoMira(p,10);if(ponto.distanceToSqr(centro)>24*24)ponto=centro.add(ponto.subtract(centro).normalize().scale(24));agendar(p,32,ponto,Vec3.ZERO,110,true);}
        }else if(kit==PoderAnime.CID){
            if(p.distanceToSqr(centro)>20*20){mensagem(p,"Retorne à área Atomic para usar o moveset desperto.");return;}
            if(slot==3&&f.estado().exclusivoUsado()){mensagem(p,"Cura das Sombras já usada nesta forma.");return;}
            if(!RpgServidor.conjurar(p,kit,slot,new int[]{60,240,400,0}[slot]))return;
            if(slot==0)agendar(p,33,p.position(),p.getLookAngle(),12,true);
            else if(slot==1)agendar(p,34,pontoMira(p,15),p.getLookAngle(),64,true);
            else if(slot==2)agendar(p,35,centro.add(0,2,0),Vec3.ZERO,100,true);
            else{f.estado().consumirExclusivo(RpgServidor.agora(p));p.setHealth(p.getMaxHealth());fx(p,36,p.position(),Vec3.ZERO,45);}
        }else{
            if(slot==2){PoderesFrieren.buracoDespertado(p);return;}
            var alvo=slot==0?mirar(p,40):null;
            if(slot==0&&alvo==null){mensagem(p,"Mire em um inimigo a até quarenta blocos.");return;}
            if(!RpgServidor.conjurar(p,kit,slot,new int[]{40,300,500,800}[slot]))return;
            if(slot==0)agendar(p,37,alvo.position(),new Vec3(alvo.getId(),0,0),40,true);
            else if(slot==1)agendar(p,38,pontoMira(p,32),Vec3.ZERO,40,true);
            else agendar(p,39,p.position(),Vec3.ZERO,120,true);
        }
        RpgServidor.uso(p,kit);RpgServidor.sincronizar(p);
    }
    private static void mensagem(ServerPlayer p,String s){p.displayClientMessage(Component.literal(s),true);}
    private static void agendar(ServerPlayer p,int tipo,Vec3 centro,Vec3 dir,int duracao,boolean forma){
        // A recarga minima ainda e maior que o ciclo de cada acao; limite protege servidores grandes.
        if(ACOES.size()>=256)return;
        ACOES.add(new Acao(p,p.serverLevel(),tipo,centro,dir,RpgServidor.agora(p),duracao,forma));fx(p,tipo,centro,dir,duracao);
        if(!forma)RpgServidor.uso(p,ProgressaoPoderes.ler(p).ativo());
    }
    private static void fx(ServerPlayer p,int tipo,Vec3 a,Vec3 b,int ticks){EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),tipo,a,b,ticks,ProgressaoPoderes.ler(p).ativo().id);}
    private static Vec3 pontoMira(ServerPlayer p,double distancia){var a=p.getEyePosition();return p.serverLevel().clip(new ClipContext(a,a.add(p.getLookAngle().scale(distancia)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();}
    private static LivingEntity mirar(ServerPlayer p,double raio){
        var a=p.getEyePosition();var b=pontoMira(p,raio);
        var hit=ProjectileUtil.getEntityHitResult(p,a,b,p.getBoundingBox().expandTowards(b.subtract(a)).inflate(1),e->e instanceof LivingEntity&&e.isAlive()&&e!=p&&!e.isAlliedTo(p),a.distanceToSqr(b));
        return hit!=null&&hit.getEntity() instanceof LivingEntity v?v:null;
    }
    private static List<LivingEntity> inimigos(ServerPlayer p,Vec3 centro,double r){return p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(centro,centro).inflate(r),v->v!=p&&v.isAlive()&&!v.isAlliedTo(p)&&v.distanceToSqr(centro)<=r*r).stream().limit(64).toList();}
    public static void morteCerebral(DespertaresServidor.Forma f){var alvo=mirar(f.p(),26);if(alvo!=null&&alvo.distanceToSqr(f.centro())<=26*26)alvo.hurt(f.nivel().damageSources().indirectMagic(f.p(),f.p()),4);}
    private static void teleporte(ServerPlayer p,PoderAnime kit,int slot,int cd,Vec3 limite){
        Vec3 inicio=p.position(),dir=p.getLookAngle();Vec3 fim=inicio;
        for(int i=1;i<=(limite==null?64:208);i++){
            var q=inicio.add(dir.scale(i*.25));var bp=BlockPos.containing(q);
            if(limite!=null&&q.distanceToSqr(limite)>26*26||!p.serverLevel().hasChunkAt(bp)||!p.serverLevel().getWorldBorder().isWithinBounds(bp)||!p.serverLevel().noCollision(p,p.getBoundingBox().move(q.subtract(inicio))))break;
            fim=q;
        }
        if(fim.distanceToSqr(inicio)<1){mensagem(p,"Não há espaço livre para o teleporte.");return;}
        if(!RpgServidor.conjurar(p,kit,slot,cd))return;
        p.connection.teleport(fim.x,fim.y,fim.z,p.getYRot(),p.getXRot());p.fallDistance=0;fx(p,41,inicio,fim,18);RpgServidor.uso(p,kit);
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var it=ACOES.iterator();while(it.hasNext()){
            var a=it.next();long t=RpgServidor.agora(a.p)-a.inicio;
            if(!valido(a)||t>=a.duracao){it.remove();continue;}
            var p=a.p;var c=a.centro;
            switch(a.tipo){
                case 30->{if(t%5==0)for(var v:inimigos(p,c,5)){var d=c.subtract(v.position());v.setDeltaMovement(d.scale(.15).add(0,.12,0));v.hurtMarked=true;v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,15,4));if(t%20==0)v.hurt(a.nivel.damageSources().indirectMagic(p,p),3);}}
                case 32->{double r=1+t*.10;for(var v:inimigos(p,c,r+4)){v.setDeltaMovement(c.subtract(v.position()).normalize().scale(.8));v.hurtMarked=true;if(v.distanceToSqr(c)<r*r&&t%10==0)v.hurt(a.nivel.damageSources().indirectMagic(p,p),18);}if(t==90&&com.cauansilva.arcanecontrol.ConfigServidor.BURACO_DESTRUIR.get())com.cauansilva.arcanecontrol.core.magia.DestruicaoAnime.cratera(p,c,12,6);}
                case 33->{if(t%3==0)for(var v:inimigos(p,c,15))if(p.hasLineOfSight(v)){v.invulnerableTime=0;v.hurt(p.damageSources().playerAttack(p),5);fx(p,31,p.getEyePosition(),v.getEyePosition(),8);}}
                case 34->{if(t>=24&&t%4==0){int n=(int)((t-24)/4);double ang=n*2.39996;var impacto=c.add(Math.cos(ang)*(n%4),0,Math.sin(ang)*(n%4));var alto=impacto.add(0,12,0);var hit=a.nivel.clip(new ClipContext(alto,impacto.add(0,-4,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();fx(p,34,alto,hit,10);for(var v:inimigos(p,hit,2))v.hurt(p.damageSources().playerAttack(p),8);}}
                case 35->{for(var v:inimigos(p,c,20)){v.setDeltaMovement(c.subtract(v.getEyePosition()).normalize().scale(.6));v.hurtMarked=true;if(t%20==0&&v.distanceToSqr(c)<16)v.hurt(a.nivel.damageSources().indirectMagic(p,p),6);}}
                case 37->{if(t%2==0&&a.nivel.getEntity((int)a.direcao.x) instanceof LivingEntity alvo&&alvo.isAlive()&&!alvo.isAlliedTo(p)&&alvo.distanceToSqr(p)<=1600){
                    Vec3 origem=p.getEyePosition().add(Math.cos(t*1.3)*1.4,Math.sin(t*.8)*.7+.5,Math.sin(t*1.3)*1.4);var bloqueio=a.nivel.clip(new ClipContext(p.getEyePosition(),origem,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(bloqueio.getType()!=HitResult.Type.MISS)origem=p.getEyePosition();
                    var fim=alvo.getEyePosition();var bloco=a.nivel.clip(new ClipContext(origem,fim,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
                    if(bloco.getType()==HitResult.Type.MISS){alvo.invulnerableTime=0;alvo.hurt(a.nivel.damageSources().indirectMagic(p,p),1.5f);}else fim=bloco.getLocation();fx(p,113,origem,fim,12);
                }}
                case 38->{if(t==25){var alto=c.add(0,30,0);var fim=a.nivel.clip(new ClipContext(alto,c.add(0,-4,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();fx(p,113,alto,fim,30);for(var v:inimigos(p,fim,4))v.hurt(a.nivel.damageSources().indirectMagic(p,p),28);}}
                default->{}
            }
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void escudo(LivingHurtEvent e){
        if(e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        for(var a:ACOES)if(a.tipo==39&&valido(a)&&a.nivel==e.getEntity().level()&&RpgServidor.agora(a.p)-a.inicio<a.duracao
                &&(e.getEntity()==a.p||e.getEntity().isAlliedTo(a.p))&&e.getEntity().distanceToSqr(a.centro)<=64){e.setCanceled(true);return;}
    }
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e){ACOES.clear();}
}
