package com.cauansilva.arcanecontrol.core.subaru;

import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rede.*;
import com.cauansilva.arcanecontrol.core.rpg.*;
import com.cauansilva.arcanecontrol.core.vfx.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;

/**
 * Kit base do Subaru (Z/X/C/V) no servidor. O cliente so desenha: alvo, dano e efeitos sao decididos aqui, nos ticks
 * das classes puras (SamakSubaru.IMPACTO, RecuperacaoSubaru.LIMPA, ...).
 * Pacotes de efeito (PacoteSincronizarCutscene3D): 600 Shamak lancado (origem = olhos, alvo = ponto previsto),
 * 601 Shamak acertou (origem = cabeca do alvo, dx = id da entidade ou -1), 602 escudo, 603 escudo estoura
 * (origem = pes), 604 Instrucao Tatica (origem = pes), 605 Recuperacao (origem = pes).
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class PoderesSubaru {
    public static final int SHAMAK=600,SHAMAK_ACERTO=601,ESCUDO=602,ESCUDO_QUEBRA=603,INSTRUCAO=604,RECUPERACAO=605,MINYA=610;
    /** Recargas (ticks) das magias da Beatrice na forma: Z Minya. */
    static final int RECARGA_MINYA=60;static final float DANO_MINYA=5;static final double RAIO_ESTACA=1.3;
    /** Recargas base (ticks) de Z/X/C/V. */
    static final int[] RECARGAS={120,300,400,500};
    static final float DANO_SHAMAK=4;
    private record Pendente(ServerPlayer p,ServerLevel nivel,int slot,long inicio,Vec3 origem,Vec3 dir,Map<Integer,Integer> acertos){
        Pendente(ServerPlayer p,ServerLevel nivel,int slot,long inicio,Vec3 origem,Vec3 dir){this(p,nivel,slot,inicio,origem,dir,new HashMap<>());}
    }
    private static final List<Pendente> PENDENTES=new ArrayList<>();

    private static void enviar(ServerPlayer p,int tipo,Vec3 o,Vec3 a){
        RegistoRede.ENVIAR.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new PacoteSincronizarCutscene3D(tipo,p.getUUID(),o.x,o.y,o.z,a.x,a.y,a.z));
    }
    private static void mensagem(ServerPlayer p,String s){p.displayClientMessage(Component.literal(s),true);}

    public static void executar(ServerPlayer p,int slot){
        if(slot<0||slot>3)return;
        if(DespertaresServidor.ativo(p)){contrato(p,slot);return;}
        if(!RpgServidor.conjurar(p,PoderAnime.SUBARU,slot,RECARGAS[slot]))return;
        long agora=RpgServidor.agora(p);Vec3 olho=p.getEyePosition(),dir=p.getLookAngle();
        switch(slot){
            case 0->{PENDENTES.add(new Pendente(p,p.serverLevel(),0,agora,olho,dir));enviar(p,SHAMAK,olho,fimShamak(p,olho,dir));}
            case 1->{var t=RpgServidor.dados(p);t.putLong("sub_escudo_ate",agora+EscudoSubaru.DURACAO);t.putFloat("sub_escudo_reserva",(float)EscudoSubaru.ABSORVE);
                enviar(p,ESCUDO,p.position(),dir);}
            case 2->{for(var alvo:aliados(p)){
                    alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,InstrucaoSubaru.DURACAO,1,false,true));
                    alvo.addEffect(new MobEffectInstance(MobEffects.JUMP,InstrucaoSubaru.DURACAO,1,false,true));}
                enviar(p,INSTRUCAO,p.position(),dir);}
            default->{PENDENTES.add(new Pendente(p,p.serverLevel(),3,agora,p.position(),dir));enviar(p,RECUPERACAO,p.position(),dir);}
        }
        RpgServidor.uso(p,PoderAnime.SUBARU);
        ProgressaoPoderes.registrarUso(p,PoderAnime.SUBARU,slot,new int[]{SamakSubaru.FIM,EscudoSubaru.DURACAO,InstrucaoSubaru.ORDEM+InstrucaoSubaru.PULSO,RecuperacaoSubaru.FIM}[slot]);
    }

    /** Magias da Beatrice durante o contrato (slots Z/X/C/V da forma). */
    private static void contrato(ServerPlayer p,int slot){
        if(slot!=0){mensagem(p,"E.M.M., Murak e Ul Minya: chegam na próxima atualização.");return;}
        if(!RpgServidor.conjurar(p,PoderAnime.SUBARU,0,RECARGA_MINYA))return;
        Vec3 frente=horizontal(p.getLookAngle());
        PENDENTES.add(new Pendente(p,p.serverLevel(),10,RpgServidor.agora(p),p.position(),frente));
        enviar(p,MINYA,p.position(),frente);
        RpgServidor.uso(p,PoderAnime.SUBARU);
    }
    static Vec3 horizontal(Vec3 v){Vec3 h=new Vec3(v.x,0,v.z);return h.lengthSqr()<1e-6?new Vec3(0,0,1):h.normalize();}
    /** Ponto do referencial dos pes no momento do cast (x = esquerda, como nas classes puras) para o mundo. */
    static Vec3 noMundo(Vec3 pes,Vec3 frente,double x,double y,double z){Vec3 l=new Vec3(0,1,0).cross(frente).normalize();return pes.add(l.scale(x)).add(0,y,0).add(frente.scale(z));}
    private static void estacaCrava(Pendente a,int k){
        var q=MinyaBeatrice.pontoAcerto(k);Vec3 ponto=noMundo(a.origem,a.dir,q.x(),q.y(),q.z());
        var caixa=new AABB(ponto,ponto).inflate(RAIO_ESTACA,2,RAIO_ESTACA);
        for(var alvo:a.nivel.getEntitiesOfClass(LivingEntity.class,caixa,v->v!=a.p&&v.isAlive()&&!v.isAlliedTo(a.p))){
            double dx=alvo.getX()-ponto.x,dz=alvo.getZ()-ponto.z;if(dx*dx+dz*dz>RAIO_ESTACA*RAIO_ESTACA)continue;
            int n=a.acertos.merge(alvo.getId(),1,Integer::sum);if(n>2)continue;
            alvo.hurt(a.nivel.damageSources().indirectMagic(a.p,a.p),DANO_MINYA);
            alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,MinyaBeatrice.CRISTAL,2,false,true));
        }
    }
    /** Ele mesmo e os jogadores aliados (mesmo time) a ate InstrucaoSubaru.RAIO blocos. */
    static List<Player> aliados(ServerPlayer p){
        var r=new ArrayList<Player>();r.add(p);
        for(var o:p.serverLevel().players())if(o!=p&&o.isAlive()&&!o.isSpectator()&&o.distanceTo(p)<=InstrucaoSubaru.RAIO&&o.isAlliedTo(p))r.add(o);
        return r;
    }
    /** Ponto onde o orbe para: bloco no caminho ou o alcance maximo. */
    private static Vec3 fimShamak(ServerPlayer p,Vec3 olho,Vec3 dir){
        Vec3 fim=olho.add(dir.scale(SamakSubaru.ALCANCE));
        var b=p.serverLevel().clip(new ClipContext(olho,fim,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        return b.getType()==HitResult.Type.MISS?fim:b.getLocation();
    }
    private static void acertoShamak(Pendente a){
        var p=a.p;Vec3 fim=fimShamak(p,a.origem,a.dir);
        var hit=ProjectileUtil.getEntityHitResult(p,a.origem,fim,p.getBoundingBox().expandTowards(fim.subtract(a.origem)).inflate(1.2),
                e->e instanceof LivingEntity&&e!=p&&e.isAlive()&&!e.isAlliedTo(p),a.origem.distanceToSqr(fim));
        if(hit!=null&&hit.getEntity() instanceof LivingEntity alvo){
            alvo.hurt(a.nivel.damageSources().indirectMagic(p,p),DANO_SHAMAK);
            alvo.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,SamakSubaru.NEVOA,0,false,true));
            alvo.addEffect(new MobEffectInstance(MobEffects.CONFUSION,SamakSubaru.NEVOA+20,0,false,true));
            enviar(p,SHAMAK_ACERTO,alvo.getEyePosition(),new Vec3(alvo.getId(),0,0));
        } else enviar(p,SHAMAK_ACERTO,fim,new Vec3(-1,0,0));
    }
    private static void limpar(ServerPlayer p){
        for(var e:new MobEffect[]{MobEffects.POISON,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.DIG_SLOWDOWN,MobEffects.BLINDNESS,MobEffects.CONFUSION,MobEffects.HUNGER})p.removeEffect(e);
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,RecuperacaoSubaru.CURA,0,false,true));
    }

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var it=PENDENTES.iterator();
        while(it.hasNext()){
            var a=it.next();var p=a.p;
            if(!p.isAlive()||p.hasDisconnected()||p.serverLevel()!=a.nivel){it.remove();continue;}
            long t=RpgServidor.agora(p)-a.inicio;
            if(a.slot==0&&t>=SamakSubaru.IMPACTO){acertoShamak(a);it.remove();}
            else if(a.slot==3&&t>=RecuperacaoSubaru.LIMPA){limpar(p);it.remove();}
            else if(a.slot==10){
                for(int k=0;k<MinyaBeatrice.ESTACAS;k++)if(t==MinyaBeatrice.acerto(k))estacaCrava(a,k);
                if(t>=MinyaBeatrice.fim())it.remove();
            }
        }
    }
    /** Escudo do X: o primeiro golpe com autor dentro da duracao e absorvido (ate ABSORVE de dano) e a bolha estoura. */
    @SubscribeEvent public static void proteger(LivingHurtEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getSource().getEntity()==null)return;
        var t=RpgServidor.dados(p);if(t.getLong("sub_escudo_ate")<=RpgServidor.agora(p))return;
        float bloqueado=Math.min(t.getFloat("sub_escudo_reserva"),e.getAmount());
        e.setAmount(e.getAmount()-bloqueado);t.putLong("sub_escudo_ate",0);t.putFloat("sub_escudo_reserva",0);
        var autor=e.getSource().getEntity().position();
        enviar(p,ESCUDO_QUEBRA,p.position(),autor.subtract(p.position()));
    }
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e){PENDENTES.clear();}
    private PoderesSubaru(){}
}
