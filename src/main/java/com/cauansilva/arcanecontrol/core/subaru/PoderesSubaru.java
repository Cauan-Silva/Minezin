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
    public static final int SHAMAK=600,SHAMAK_ACERTO=601,ESCUDO=602,ESCUDO_QUEBRA=603,INSTRUCAO=604,RECUPERACAO=605,MINYA=610,EMM=611,EMM_GOLPE=612,MURAK=613,UL_MINYA=614;
    /** Recargas (ticks) das magias da Beatrice na forma: Z Minya. */
    static final int RECARGA_MINYA=60,RECARGA_EMM=400,RECARGA_MURAK=500,RECARGA_UL=600;
    static final float DANO_UL_ESTACA=4,DANO_UL_CHUVA=2,DANO_UL_QUEBRA=18;
    /** A cada quantos ticks da chuva a Ul Minya acerta quem esta na area (multi-hit). */
    static final int RITMO_CHUVA=4;
    /** Salto IV (~4,5 blocos com a queda lenta) e Queda Lenta por MurakBeatrice.DURACAO. */
    static final int SALTO_MURAK=3;static final float DANO_MINYA=5;static final double RAIO_ESTACA=1.3;
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
        if(slot==1){
            if(!RpgServidor.conjurar(p,PoderAnime.SUBARU,1,RECARGA_EMM))return;
            var t=RpgServidor.dados(p);t.putLong("sub_emm_inicio",RpgServidor.agora(p));t.putLong("sub_emm_golpe",0);
            enviar(p,EMM,p.position(),horizontal(p.getLookAngle()));RpgServidor.uso(p,PoderAnime.SUBARU);return;
        }
        if(slot==2){
            if(!RpgServidor.conjurar(p,PoderAnime.SUBARU,2,RECARGA_MURAK))return;
            p.addEffect(new MobEffectInstance(MobEffects.JUMP,MurakBeatrice.DURACAO,SALTO_MURAK,false,false,true));
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,MurakBeatrice.DURACAO,0,false,false,true));
            enviar(p,MURAK,p.position(),horizontal(p.getLookAngle()));RpgServidor.uso(p,PoderAnime.SUBARU);return;
        }
        if(slot==3){
            if(!RpgServidor.conjurar(p,PoderAnime.SUBARU,3,RECARGA_UL))return;
            Vec3 frente=horizontal(p.getLookAngle());
            PENDENTES.add(new Pendente(p,p.serverLevel(),13,RpgServidor.agora(p),p.position(),frente));
            enviar(p,UL_MINYA,p.position(),frente);RpgServidor.uso(p,PoderAnime.SUBARU);return;
        }
        if(!RpgServidor.conjurar(p,PoderAnime.SUBARU,0,RECARGA_MINYA))return;
        Vec3 frente=horizontal(p.getLookAngle());
        PENDENTES.add(new Pendente(p,p.serverLevel(),10,RpgServidor.agora(p),p.position(),frente));
        enviar(p,MINYA,p.position(),frente);
        RpgServidor.uso(p,PoderAnime.SUBARU);
    }
    static Vec3 horizontal(Vec3 v){Vec3 h=new Vec3(v.x,0,v.z);return h.lengthSqr()<1e-6?new Vec3(0,0,1):h.normalize();}
    /** Ponto do referencial dos pes no momento do cast (x = esquerda, como nas classes puras) para o mundo. */
    static Vec3 noMundo(Vec3 pes,Vec3 frente,double x,double y,double z){Vec3 l=new Vec3(0,1,0).cross(frente).normalize();return pes.add(l.scale(x)).add(0,y,0).add(frente.scale(z));}
    /** Dano das magias de varios hits: zera a invencibilidade curta e nao empurra (o hurt vanilla empurra a partir do Subaru). */
    private static void ferir(Pendente a,LivingEntity alvo,float dano){
        Vec3 v=alvo.getDeltaMovement();alvo.invulnerableTime=0;alvo.hurt(a.nivel.damageSources().indirectMagic(a.p,a.p),dano);
        alvo.setDeltaMovement(v);alvo.hurtMarked=true;
    }
    /** Inimigos vivos dentro da area da Ul Minya (cilindro de RAIO_AREA em volta do centro, 6 blocos de altura). */
    private static List<LivingEntity> naAreaUl(Pendente a){
        var c=UlMinyaBeatrice.CENTRO_AREA;Vec3 centro=noMundo(a.origem,a.dir,c.x(),c.y(),c.z());double R=UlMinyaBeatrice.RAIO_AREA;
        return a.nivel.getEntitiesOfClass(LivingEntity.class,new AABB(centro,centro).inflate(R,6,R),v->v!=a.p&&v.isAlive()&&!v.isAlliedTo(a.p)
                &&Math.pow(v.getX()-centro.x,2)+Math.pow(v.getZ()-centro.z,2)<=R*R);
    }
    private static void ulMinya(Pendente a,long t){
        for(int k=0;k<UlMinyaBeatrice.ESTACAS;k++)if(t==(long)Math.ceil(UlMinyaBeatrice.acerto(k))){
            var q=UlMinyaBeatrice.pontoAcerto(k);Vec3 ponto=noMundo(a.origem,a.dir,q.x(),q.y(),q.z());
            for(var alvo:a.nivel.getEntitiesOfClass(LivingEntity.class,new AABB(ponto,ponto).inflate(1.5,2.5,1.5),v->v!=a.p&&v.isAlive()&&!v.isAlliedTo(a.p))){
                if(Math.pow(alvo.getX()-ponto.x,2)+Math.pow(alvo.getZ()-ponto.z,2)>2.25)continue;
                if(a.acertos.merge(alvo.getId(),1,Integer::sum)>3)continue;
                ferir(a,alvo,DANO_UL_ESTACA);
            }
        }
        // chuva: enquanto as estacas caem, a area toda leva dano em ritmo (multi-hit)
        if(t>=UlMinyaBeatrice.ABRE&&t<UlMinyaBeatrice.PRISAO&&(t-UlMinyaBeatrice.ABRE)%RITMO_CHUVA==0)for(var alvo:naAreaUl(a))ferir(a,alvo,DANO_UL_CHUVA);
        if(t>=UlMinyaBeatrice.PRISAO&&t<UlMinyaBeatrice.QUEBRA)for(var alvo:naAreaUl(a)){
            // presos no cristal: sem andar nem pular ate a quebra
            if(t==UlMinyaBeatrice.PRISAO)alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,UlMinyaBeatrice.QUEBRA-UlMinyaBeatrice.PRISAO+2,9,false,true));
            if(!(alvo instanceof Player)){alvo.setDeltaMovement(0,Math.min(0,alvo.getDeltaMovement().y),0);alvo.hurtMarked=true;}
        }
        if(t==UlMinyaBeatrice.QUEBRA){
            var c=UlMinyaBeatrice.CENTRO_AREA;Vec3 centro=noMundo(a.origem,a.dir,c.x(),c.y(),c.z());
            for(var alvo:naAreaUl(a)){
                alvo.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);ferir(a,alvo,DANO_UL_QUEBRA);
                Vec3 fora=alvo.position().subtract(centro);fora=new Vec3(fora.x,0,fora.z);if(fora.lengthSqr()>1e-4)fora=fora.normalize();
                alvo.setDeltaMovement(fora.scale(.35).add(0,.3,0));alvo.hurtMarked=true;
            }
        }
    }
    /** Estaca k no ar: acerta o primeiro inimigo a ate 1,1 bloco dela (a estaca para ali). */
    private static void estacaVoo(Pendente a,int k,long t){
        if(a.acertos.containsKey(-1-k))return;
        var q=MinyaBeatrice.centro(k,t);Vec3 c=noMundo(a.origem,a.dir,q.x(),q.y(),q.z());
        for(var alvo:a.nivel.getEntitiesOfClass(LivingEntity.class,new AABB(c,c).inflate(1.1),v->v!=a.p&&v.isAlive()&&!v.isAlliedTo(a.p))){
            if(alvo.getBoundingBox().inflate(.5).contains(c)||alvo.getBoundingBox().getCenter().distanceTo(c)<1.1){
                a.acertos.put(-1-k,1);
                if(a.acertos.merge(alvo.getId(),1,Integer::sum)<=2){ferir(a,alvo,DANO_MINYA);alvo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,MinyaBeatrice.CRISTAL,2,false,true));}
                return;
            }
        }
    }
    private static void estacaCrava(Pendente a,int k){
        if(a.acertos.containsKey(-1-k))return;
        var q=MinyaBeatrice.pontoAcerto(k);Vec3 ponto=noMundo(a.origem,a.dir,q.x(),q.y(),q.z());
        var caixa=new AABB(ponto,ponto).inflate(RAIO_ESTACA,2,RAIO_ESTACA);
        for(var alvo:a.nivel.getEntitiesOfClass(LivingEntity.class,caixa,v->v!=a.p&&v.isAlive()&&!v.isAlliedTo(a.p))){
            double dx=alvo.getX()-ponto.x,dz=alvo.getZ()-ponto.z;if(dx*dx+dz*dz>RAIO_ESTACA*RAIO_ESTACA)continue;
            int n=a.acertos.merge(alvo.getId(),1,Integer::sum);if(n>2)continue;
            ferir(a,alvo,DANO_MINYA);
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
            else if(a.slot==13){ulMinya(a,t);if(t>=UlMinyaBeatrice.FIM)it.remove();}
            else if(a.slot==10){
                for(int k=0;k<MinyaBeatrice.ESTACAS;k++){
                    if(t>=MinyaBeatrice.disparo(k)&&t<MinyaBeatrice.acerto(k))estacaVoo(a,k,t);
                    if(t==MinyaBeatrice.acerto(k))estacaCrava(a,k);
                }
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
    /** E.M.M. fechado: EmmBeatrice.invulneravel no tempo desde o cast. */
    static boolean emm(ServerPlayer p){var t=RpgServidor.dados(p);return t.contains("sub_emm_inicio")&&EmmBeatrice.invulneravel(RpgServidor.agora(p)-t.getLong("sub_emm_inicio"));}
    /** Nada entra: o golpe e cancelado antes do dano (menos o que ignora invulnerabilidade, como /kill e o vazio). */
    @SubscribeEvent public static void casca(net.minecraftforge.event.entity.living.LivingAttackEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!emm(p)||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        e.setCanceled(true);
        var autor=e.getSource().getEntity()!=null?e.getSource().getEntity().position():e.getSource().getSourcePosition();
        var t=RpgServidor.dados(p);long agora=RpgServidor.agora(p);
        if(autor!=null&&agora-t.getLong("sub_emm_golpe")>=3){t.putLong("sub_emm_golpe",agora);
            Vec3 d=autor.subtract(p.position().add(0,1,0));if(d.lengthSqr()>1e-6)enviar(p,EMM_GOLPE,p.position(),d.normalize());}
    }
    /** Efeitos nocivos novos nao pegam enquanto a casca esta fechada. */
    @SubscribeEvent public static void cascaEfeitos(net.minecraftforge.event.entity.living.MobEffectEvent.Applicable e){
        if(e.getEntity() instanceof ServerPlayer p&&emm(p)&&e.getEffectInstance().getEffect().getCategory()==MobEffectCategory.HARMFUL)e.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
    }
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e){PENDENTES.clear();}
    private PoderesSubaru(){}
}
