package com.cauansilva.arcanecontrol.core.magia;
import com.cauansilva.arcanecontrol.core.vfx.PerfilCratera;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Destruicao real, com orÃ§amento global por tick e eventos de proteÃ§Ã£o por bloco. */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class DestruicaoAnime {
    private record Cratera(ServerPlayer dono,ServerLevel nivel,BlockPos centro,Iterator<PerfilCratera.Bloco> plano){}
    private record Fragmento(FallingBlockEntity entidade,UUID dono,Vec3 centro,long fim){}
    private static final List<Cratera> CRATERAS=new ArrayList<>();
    private static final List<Fragmento> FRAGMENTOS=new ArrayList<>();
    public static void cratera(ServerPlayer p,Vec3 centro,int raio,int profundidade){
        if(CRATERAS.size()>=8)return;
        CRATERAS.add(new Cratera(p,p.serverLevel(),BlockPos.containing(centro),PerfilCratera.planejar(raio,profundidade).iterator()));
    }
    public static void fenda(ServerPlayer p,Vec3 origem,Vec3 dir,int alcance){
        if(CRATERAS.size()>=8)return;
        var plano=new ArrayList<PerfilCratera.Bloco>();var vistos=new HashSet<BlockPos>();
        Vec3 lado=new Vec3(-dir.z,0,dir.x);
        for(int i=1;i<=Math.min(24,alcance);i++)for(int largura=-1;largura<=1;largura++)for(int y=-3;y<=0;y++){
            var pos=BlockPos.containing(dir.scale(i).add(lado.scale(largura)).add(0,y,0));
            if(vistos.add(pos))plano.add(new PerfilCratera.Bloco(pos.getX(),pos.getY(),pos.getZ()));
        }
        CRATERAS.add(new Cratera(p,p.serverLevel(),BlockPos.containing(origem),plano.iterator()));
    }
    private static boolean podeRemover(ServerPlayer p,ServerLevel level,BlockPos pos){
        if(!level.hasChunkAt(pos)||!level.getWorldBorder().isWithinBounds(pos)||!level.mayInteract(p,pos))return false;
        var state=level.getBlockState(pos);
        if(state.isAir()||!state.getFluidState().isEmpty()||state.hasBlockEntity()||state.getDestroySpeed(level,pos)<0||state.is(BlockTags.WITHER_IMMUNE))return false;
        return !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,pos,state,p));
    }
    /** Arranca poucos blocos por chamada; as entidades reais convergem e sÃ£o consumidas. */
    public static void atrairBlocos(ServerPlayer p,Vec3 centro,long tick){
        if(FRAGMENTOS.size()>=128)return;
        var level=p.serverLevel();int criados=0;
        for(int i=0;i<32&&criados<3;i++){
            double a=(tick*32+i)*2.399963,raio=4+Math.floorMod(tick*7+i*3,10);
            int x=(int)Math.floor(centro.x+Math.cos(a)*raio),z=(int)Math.floor(centro.z+Math.sin(a)*raio);
            for(int dy=3;dy>=-10;dy--){
                var pos=new BlockPos(x,(int)Math.floor(centro.y)+dy,z);
                if(!level.hasChunkAt(pos))break;
                if(!podeRemover(p,level,pos))continue;
                var state=level.getBlockState(pos);
                var bloco=FallingBlockEntity.fall(level,pos,state);
                bloco.getPersistentData().putBoolean("arcanecontrol_consumido",true);
                bloco.setNoGravity(true);bloco.noPhysics=true;bloco.dropItem=false;bloco.setInvulnerable(true);
                FRAGMENTOS.add(new Fragmento(bloco,p.getUUID(),centro,level.getGameTime()+65));criados++;break;
            }
        }
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        int orcamento=384;var it=CRATERAS.iterator();
        while(it.hasNext()){
            var c=it.next();if(c.dono.hasDisconnected()||c.dono.serverLevel()!=c.nivel){it.remove();continue;}
            int local=0;
            while(c.plano.hasNext()&&orcamento>0&&local++<192){
                var offset=c.plano.next();var pos=c.centro.offset(offset.x(),offset.y(),offset.z());orcamento--;
                if(podeRemover(c.dono,c.nivel,pos)){
                    var state=c.nivel.getBlockState(pos);c.nivel.removeBlock(pos,false);
                    if(local%16==0)c.nivel.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK,state),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,3,.3,.3,.3,.08);
                }
            }
            if(!c.plano.hasNext())it.remove();if(orcamento<=0)break;
        }
        FRAGMENTOS.removeIf(f->{
            var bloco=f.entidade;
            if(!bloco.isAlive())return true;
            var dono=e.getServer().getPlayerList().getPlayer(f.dono);
            if(dono==null||!dono.isAlive()||dono.level()!=bloco.level()){bloco.discard();return true;}
            Vec3 delta=f.centro.subtract(bloco.position());
            if(delta.lengthSqr()<1||bloco.level().getGameTime()>=f.fim){bloco.discard();return true;}
            Vec3 tangente=new Vec3(-delta.z,0,delta.x).normalize().scale(.18);
            bloco.setDeltaMovement(delta.normalize().scale(.75).add(tangente));bloco.hurtMarked=true;return false;
        });
    }
    @SubscribeEvent public static void recarregar(net.minecraftforge.event.entity.EntityJoinLevelEvent e){
        if(!e.getLevel().isClientSide&&e.getEntity().getPersistentData().getBoolean("arcanecontrol_consumido"))e.setCanceled(true);
    }
    @SubscribeEvent public static void parar(net.minecraftforge.event.server.ServerStoppedEvent e){
        FRAGMENTOS.forEach(f->f.entidade.discard());FRAGMENTOS.clear();CRATERAS.clear();
    }
}
