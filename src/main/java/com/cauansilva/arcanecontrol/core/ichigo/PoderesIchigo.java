package com.cauansilva.arcanecontrol.core.ichigo;
import com.cauansilva.arcanecontrol.core.vfx.GetsugaIchigo;
import com.cauansilva.arcanecontrol.core.vfx.KitBaseIchigo;
import com.cauansilva.arcanecontrol.core.vfx.EntradaBankai;
import com.cauansilva.arcanecontrol.core.vfx.MugetsuIchigo;
import com.cauansilva.arcanecontrol.core.vfx.CeroIchigo;

import com.cauansilva.arcanecontrol.core.rpg.*;
import com.cauansilva.arcanecontrol.core.poderes.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Server-authoritative casts: bounded swept collision, terrain occlusion and per-action hit sets. */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class PoderesIchigo {
    private static final PoderAnime KIT=PoderAnime.ICHIGO;
    private static final class Golpe {
        final ServerPlayer p;final ServerLevel mundo;final Vec3 origem,dir;final long inicio;final int slot;final boolean bankai;
        final DespertaresServidor.Forma forma;
        final Set<UUID> atingidos=new HashSet<>();boolean liberado;Vec3 anterior;
        Golpe(ServerPlayer p,int slot,boolean bankai){this.p=p;mundo=p.serverLevel();origem=p.getEyePosition();dir=p.getLookAngle().normalize();inicio=RpgServidor.agora(p);this.slot=slot;this.bankai=bankai;forma=bankai?DespertaresServidor.forma(p):null;anterior=origem;}
    }
    private static final List<Golpe> GOLPES=new ArrayList<>();
    /** Mugetsu em andamento: origem nos pes e direcao horizontal fixadas no cast. */
    private static final class Mugetsu{final ServerPlayer p;final Object mundo;final long inicio;final Vec3 pes,dir;
        Mugetsu(ServerPlayer p){this.p=p;mundo=p.serverLevel();inicio=RpgServidor.agora(p);pes=p.position();var d=new Vec3(p.getLookAngle().x,0,p.getLookAngle().z);dir=d.lengthSqr()<1e-6?new Vec3(0,0,1):d.normalize();}}
    private static final List<Mugetsu> MUGETSUS=new ArrayList<>();
    /** Depois do Mugetsu o Ichigo fica sem reiatsu: nenhum poder do kit nem o Bankai ate o fim do bloqueio. */
    public static boolean bloqueado(ServerPlayer p){
        long resto=RpgServidor.dados(p).getLong("ichigo_bloqueio")-RpgServidor.agora(p);if(resto<=0)return false;
        p.displayClientMessage(Component.literal("Sem reiatsu depois do Mugetsu: "+(resto+19)/20+" s"),true);return true;
    }
    public static boolean armado(ServerPlayer p){
        if(p.getMainHandItem().is(RegistroIchigo.ESPADA.get()))return true;
        p.displayClientMessage(Component.literal("Segure Zangetsu para usar o kit de Ichigo."),true);return false;
    }
    public static void executar(ServerPlayer p,int slot){
        if(slot<0||slot>3||!armado(p)||GOLPES.size()>=128||bloqueado(p)||MUGETSUS.stream().anyMatch(m->m.p==p))return;
        boolean bankai=DespertaresServidor.ativo(p);
        if(bankai&&RpgServidor.agora(p)-RpgServidor.dados(p).getLong("ichigo_bankai_inicio")<EntradaBankai.FIM)return; // liberacao em andamento
        if(GOLPES.stream().anyMatch(g->g.p==p&&!g.liberado))return;
        if(slot==1){passo(p,bankai);return;}
        boolean mascara=bankai&&RpgServidor.dados(p).getLong("ichigo_mascara")>RpgServidor.agora(p);
        // With the Hollow mask on: it is a damage buff only (no Cero, user's call); Z becomes the Getsuga Juujishou.
        if(mascara&&slot==3){p.displayClientMessage(Component.literal("Máscara Hollow já ativa."),true);return;}
        if(mascara&&slot==0){
            if(!RpgServidor.conjurar(p,KIT,0,PerfilIchigo.recarga(0,true)))return;
            GOLPES.add(new Golpe(p,2,true));fx(p,512,p.getEyePosition(),p.getLookAngle(),40);RpgServidor.uso(p,KIT);return;
        }
        if(bankai&&slot==2){
            if(!RpgServidor.conjurar(p,KIT,slot,PerfilIchigo.recarga(slot,true)))return;
            var m=new Mugetsu(p);MUGETSUS.add(m);fx(p,530,m.pes,m.dir,MugetsuIchigo.FIM);RpgServidor.uso(p,KIT);return;
        }
        if(!RpgServidor.conjurar(p,KIT,slot,PerfilIchigo.recarga(slot,bankai)))return;
        GOLPES.add(new Golpe(p,slot,bankai));
        fx(p,bankai?510+slot:500+slot,p.getEyePosition(),p.getLookAngle(),bankai&&slot==3?24:40);
        RpgServidor.uso(p,KIT);
    }
    private static void passo(ServerPlayer p,boolean bankai){
        Vec3 origem=p.position(),fim=origem,dir=p.getLookAngle();int limite=bankai?60:40;
        for(int i=1;i<=limite;i++){
            var q=origem.add(dir.scale(i*.25));var bp=BlockPos.containing(q);
            if(!p.serverLevel().hasChunkAt(bp)||!p.serverLevel().getWorldBorder().isWithinBounds(bp)||!p.serverLevel().noCollision(p,p.getBoundingBox().move(q.subtract(origem))))break;
            fim=q;
        }
        if(fim.distanceToSqr(origem)<1){p.displayClientMessage(Component.literal("Sem espaço livre para o Passo Flash."),true);return;}
        // Bankai: up to CeroIchigo.SHUNPOS dashes inside a short window; only the first one pays the cooldown.
        var d=RpgServidor.dados(p);long agora=RpgServidor.agora(p);
        boolean encadeado=bankai&&agora<=d.getLong("ichigo_shunpo_ate")&&d.getInt("ichigo_shunpo_n")<CeroIchigo.SHUNPOS;
        if(!encadeado&&!RpgServidor.conjurar(p,KIT,1,PerfilIchigo.recarga(1,bankai)))return;
        if(bankai){if(encadeado)d.putInt("ichigo_shunpo_n",d.getInt("ichigo_shunpo_n")+1);else{d.putInt("ichigo_shunpo_n",1);d.putLong("ichigo_shunpo_ate",agora+CeroIchigo.JANELA_SHUNPO);}}
        p.connection.teleport(fim.x,fim.y,fim.z,p.getYRot(),p.getXRot());p.fallDistance=0;
        fx(p,bankai?511:501,origem,fim,16);RpgServidor.uso(p,KIT);
    }
    /** V da forma base: avanca ate KitBaseIchigo.INVESTIDA blocos no plano, parando antes de colidir. */
    private static void investida(ServerPlayer p,Vec3 olhar){
        var dir=new Vec3(olhar.x,0,olhar.z);if(dir.lengthSqr()<1e-6)return;dir=dir.normalize();var origem=p.position();var fim=origem;
        for(int i=1;i<=KitBaseIchigo.INVESTIDA*4;i++){
            var q=origem.add(dir.scale(i*.25));var bp=BlockPos.containing(q);
            if(!p.serverLevel().hasChunkAt(bp)||!p.serverLevel().getWorldBorder().isWithinBounds(bp)||!p.serverLevel().noCollision(p,p.getBoundingBox().move(q.subtract(origem))))break;
            fim=q;
        }
        if(fim.distanceToSqr(origem)>.01){p.connection.teleport(fim.x,fim.y,fim.z,p.getYRot(),p.getXRot());p.fallDistance=0;}
    }
    private static void fx(ServerPlayer p,int tipo,Vec3 a,Vec3 b,int ticks){EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),tipo,a,b,ticks,6);}
    public static void limpar(ServerPlayer p){RpgServidor.dados(p).remove("ichigo_mascara"); /* Casts retire in tick; death callbacks must not mutate its active iterator. */}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var mi=MUGETSUS.iterator();while(mi.hasNext()){
            var m=mi.next();var p=m.p;long t=RpgServidor.agora(p)-m.inicio;
            if(!p.isAlive()||p.hasDisconnected()||p.serverLevel()!=m.mundo){mi.remove();continue;}
            if(t==MugetsuIchigo.GOLPE){
                var lado=m.dir.cross(new Vec3(0,1,0)).normalize();
                for(var v:p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(m.pes,m.pes).inflate(MugetsuIchigo.ALCANCE+14),v->v!=p&&v.isAlive()&&!v.isAlliedTo(p)).stream().limit(128).toList()){
                    var d=v.position().subtract(m.pes);
                    if(!MugetsuIchigo.atinge(d.dot(lado),d.y,d.dot(m.dir)))continue;
                    v.hurt(p.damageSources().indirectMagic(p,p),(float)MugetsuIchigo.DANO);v.setDeltaMovement(v.getDeltaMovement().add(m.dir.scale(1.2)).add(0,.4,0));v.hurtMarked=true;
                }
            }
            if(t>=MugetsuIchigo.FIM){
                // The final Getsuga burns all reiatsu: the form ends with the Bankai and every power stays locked.
                if(DespertaresServidor.ativo(p))DespertaresServidor.cancelar(p);
                RpgServidor.dados(p).putLong("ichigo_bloqueio",RpgServidor.agora(p)+MugetsuIchigo.BLOQUEIO);RpgServidor.sincronizar(p);mi.remove();
            }
        }
        var it=GOLPES.iterator();while(it.hasNext()){
            var g=it.next();var p=g.p;int t=(int)(RpgServidor.agora(p)-g.inicio);
            if(!p.isAlive()||p.hasDisconnected()||p.serverLevel()!=g.mundo||ProgressaoPoderes.ler(p).ativo()!=KIT||g.bankai&&(!DespertaresServidor.ativo(p)||DespertaresServidor.forma(p)!=g.forma)||t>=40){it.remove();continue;}
            int impacto=PerfilIchigo.impacto(g.slot,g.bankai);
            if(t<impacto)continue;
            if(!g.liberado){g.liberado=true;
                if(g.slot==0||g.bankai&&g.slot==2)fx(p,g.bankai?(g.slot==2?523:524):520,g.origem,g.dir,GetsugaIchigo.FIM);
                if(g.bankai&&g.slot==3){RpgServidor.dados(p).putLong("ichigo_mascara",RpgServidor.agora(p)+PerfilIchigo.MASCARA);fx(p,522,p.position(),Vec3.ZERO,PerfilIchigo.MASCARA);it.remove();continue;}
                if(!g.bankai&&g.slot==2){
                    for(var v:alvos(p,p.position(),8))if(p.hasLineOfSight(v)){
                        var d=v.position().subtract(p.position()).multiply(1,0,1).normalize();v.setDeltaMovement(d.scale(1.2).add(0,.25,0));v.hurtMarked=true;v.hurt(g.mundo.damageSources().indirectMagic(p,p),8);
                    }
                    fx(p,525,p.position(),p.getLookAngle(),KitBaseIchigo.ONDA_FIM);it.remove();continue;
                }
                if(!g.bankai&&g.slot==3){
                    investida(p,g.dir);var from=p.getEyePosition();fx(p,526,from,g.dir,KitBaseIchigo.CORTE_FIM);var to=g.mundo.clip(new ClipContext(from,from.add(g.dir.scale(5)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
                    var alvo=alvos(p,from.add(to).scale(.5),5).stream().filter(v->(v.getBoundingBox().inflate(.45).contains(from)||v.getBoundingBox().inflate(.45).clip(from,to).isPresent())).min(Comparator.comparingDouble(v->v.distanceToSqr(from))).orElse(null);
                    if(alvo!=null){alvo.hurt(p.damageSources().playerAttack(p),16);/* Launch after hurt so vanilla knockback cannot cancel it. */alvo.setDeltaMovement(alvo.getDeltaMovement().multiply(.2,0,.2).add(0,KitBaseIchigo.IMPULSO,0));alvo.hurtMarked=true;fx(p,527,alvo.position(),Vec3.ZERO,KitBaseIchigo.LANCA_FIM);}
                    it.remove();continue;
                }
            }
            double alcance=PerfilIchigo.alcance(t-impacto);var previsto=g.origem.add(g.dir.scale(alcance));
            if(!g.mundo.hasChunkAt(BlockPos.containing(previsto))||!g.mundo.getWorldBorder().isWithinBounds(BlockPos.containing(previsto))){it.remove();continue;}
            var bloco=g.mundo.clip(new ClipContext(g.anterior,previsto,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));var fim=bloco.getLocation();
            double raio=PerfilIchigo.raio(g.bankai&&g.slot==2);
            for(var v:g.mundo.getEntitiesOfClass(LivingEntity.class,new AABB(g.anterior,fim).inflate(raio),v->v!=p&&v.isAlive()&&!v.isAlliedTo(p))){
                if(g.atingidos.size()>=64)break;
                if(!g.atingidos.contains(v.getUUID())&&(v.getBoundingBox().inflate(raio).contains(g.anterior)||v.getBoundingBox().inflate(raio).clip(g.anterior,fim).isPresent())&&p.hasLineOfSight(v)){
                    g.atingidos.add(v.getUUID());v.hurt(g.mundo.damageSources().indirectMagic(p,p),g.bankai?(g.slot==2?28:18):14);fx(p,521,v.getEyePosition(),Vec3.ZERO,10);
                }
            }
            g.anterior=fim;
            if(bloco.getType()!=HitResult.Type.MISS||alcance>=28)it.remove();
        }
    }
    private static List<LivingEntity> alvos(ServerPlayer p,Vec3 c,double r){return p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(c,c).inflate(r),v->v!=p&&v.isAlive()&&!v.isAlliedTo(p)&&v.distanceToSqr(c)<=r*r).stream().limit(64).toList();}
    @SubscribeEvent public static void dano(LivingHurtEvent e){
        if(e.getSource().getEntity() instanceof ServerPlayer p&&e.getEntity()!=p&&ProgressaoPoderes.ler(p).ativo()==KIT&&DespertaresServidor.ativo(p)&&RpgServidor.dados(p).getLong("ichigo_mascara")>RpgServidor.agora(p))e.setAmount(e.getAmount()*1.5f);
    }
    @SubscribeEvent public static void observar(PlayerEvent.StartTracking e){
        if(e.getEntity() instanceof ServerPlayer observador&&e.getTarget() instanceof ServerPlayer p&&DespertaresServidor.ativo(p)&&ProgressaoPoderes.ler(p).ativo()==KIT){
            var rede=com.cauansilva.arcanecontrol.core.rede.RegistoRede.ENVIAR;
            rede.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->observador),new com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar(p.getUUID(),201,p.position(),Vec3.ZERO,1200,6));
            int resto=(int)Math.max(0,RpgServidor.dados(p).getLong("ichigo_mascara")-RpgServidor.agora(p));
            if(resto>0)rede.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->observador),new com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar(p.getUUID(),522,p.position(),Vec3.ZERO,Math.min(200,resto),6));
        }
    }
    @SubscribeEvent public static void parar(ServerStoppedEvent e){GOLPES.clear();MUGETSUS.clear();}
}
