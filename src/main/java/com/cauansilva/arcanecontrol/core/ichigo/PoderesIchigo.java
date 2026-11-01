package com.cauansilva.arcanecontrol.core.ichigo;

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
    public static boolean armado(ServerPlayer p){
        if(p.getMainHandItem().is(RegistroIchigo.ESPADA.get()))return true;
        p.displayClientMessage(Component.literal("Segure Zangetsu para usar o kit de Ichigo."),true);return false;
    }
    public static void executar(ServerPlayer p,int slot){
        if(slot<0||slot>3||!armado(p)||GOLPES.size()>=128)return;
        boolean bankai=DespertaresServidor.ativo(p);
        if(GOLPES.stream().anyMatch(g->g.p==p&&!g.liberado))return;
        if(slot==1){passo(p,bankai);return;}
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
        if(!RpgServidor.conjurar(p,KIT,1,PerfilIchigo.recarga(1,bankai)))return;
        p.connection.teleport(fim.x,fim.y,fim.z,p.getYRot(),p.getXRot());p.fallDistance=0;
        fx(p,bankai?511:501,origem,fim,16);RpgServidor.uso(p,KIT);
    }
    private static void fx(ServerPlayer p,int tipo,Vec3 a,Vec3 b,int ticks){EfeitosDespertar.enviar(p.serverLevel(),p.getUUID(),tipo,a,b,ticks,6);}
    public static void limpar(ServerPlayer p){RpgServidor.dados(p).remove("ichigo_mascara"); /* Casts retire in tick; death callbacks must not mutate its active iterator. */}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var it=GOLPES.iterator();while(it.hasNext()){
            var g=it.next();var p=g.p;int t=(int)(RpgServidor.agora(p)-g.inicio);
            if(!p.isAlive()||p.hasDisconnected()||p.serverLevel()!=g.mundo||ProgressaoPoderes.ler(p).ativo()!=KIT||g.bankai&&(!DespertaresServidor.ativo(p)||DespertaresServidor.forma(p)!=g.forma)||t>=40){it.remove();continue;}
            int impacto=PerfilIchigo.impacto(g.slot,g.bankai);
            if(t<impacto)continue;
            if(!g.liberado){g.liberado=true;
                if(g.slot==0||g.bankai&&g.slot==2)fx(p,g.bankai?(g.slot==2?523:524):520,g.origem,g.dir,g.bankai&&g.slot==2?21:20);
                if(g.bankai&&g.slot==3){RpgServidor.dados(p).putLong("ichigo_mascara",RpgServidor.agora(p)+PerfilIchigo.MASCARA);fx(p,522,p.position(),Vec3.ZERO,PerfilIchigo.MASCARA);it.remove();continue;}
                if(!g.bankai&&g.slot==2){
                    for(var v:alvos(p,p.position(),8))if(p.hasLineOfSight(v)){
                        var d=v.position().subtract(p.position()).multiply(1,0,1).normalize();v.setDeltaMovement(d.scale(1.2).add(0,.25,0));v.hurtMarked=true;v.hurt(g.mundo.damageSources().indirectMagic(p,p),8);
                    }
                    fx(p,521,p.position(),Vec3.ZERO,18);it.remove();continue;
                }
                if(!g.bankai&&g.slot==3){
                    var from=p.getEyePosition();var to=g.mundo.clip(new ClipContext(from,from.add(g.dir.scale(5)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
                    var alvo=alvos(p,from.add(to).scale(.5),5).stream().filter(v->(v.getBoundingBox().inflate(.45).contains(from)||v.getBoundingBox().inflate(.45).clip(from,to).isPresent())).min(Comparator.comparingDouble(v->v.distanceToSqr(from))).orElse(null);
                    if(alvo!=null){alvo.hurt(p.damageSources().playerAttack(p),16);alvo.setDeltaMovement(alvo.getDeltaMovement().add(0,1,0));alvo.hurtMarked=true;fx(p,521,alvo.position(),Vec3.ZERO,12);}
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
    @SubscribeEvent public static void parar(ServerStoppedEvent e){GOLPES.clear();}
}
