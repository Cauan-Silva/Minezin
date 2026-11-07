package com.cauansilva.arcanecontrol.core.vazio;

import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;
import com.cauansilva.arcanecontrol.core.rede.*;
import com.cauansilva.arcanecontrol.core.rpg.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;

/**
 * Void System no servidor: cada desafio do altar vira uma instancia propria na dimensao arcanecontrol:boss_realm,
 * numa arena gerada por codigo ({@link PlantaArena}) em coordenadas isoladas. Quem estava a ate {@link #RAIO_GRUPO}
 * blocos do altar entra junto (ate {@link #MAX_GRUPO}). Morreu: vira espectador sem perder nada. Vitoria ou wipe:
 * todos voltam para onde estavam. O retorno fica salvo no NBT do jogador (sobrevive a queda do servidor).
 * Pacotes ao jogador (PacoteSincronizarCutscene3D): 800 entrada (fade + HUD travado), 801 saida (fade),
 * 802 cena (o = centro do chao da arena, a = (kit, altura do chefe, 0)) para a camera de CinematicaEntrada.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class VazioServidor {
    public static final ResourceKey<Level> REINO=ResourceKey.create(Registries.DIMENSION,new ResourceLocation("arcanecontrol","boss_realm"));
    public static final int ENTRADA=800,SAIDA=801,CENA=802,MAX_GRUPO=4;public static final double RAIO_GRUPO=8;

    private static final class Instancia{
        final long n;final BlockPos centro;final int kit;final EstadoInstancia<UUID> estado;UUID chefe;UUID dono;long inicio;
        Instancia(long n,BlockPos centro,int kit,List<UUID> grupo,long agora){this.n=n;this.centro=centro;this.kit=kit;estado=new EstadoInstancia<>(grupo,agora);}
    }
    private static final Map<Long,Instancia> INSTANCIAS=new HashMap<>();

    /** Contador persistente: cada instancia ganha um lugar novo da grade (nunca reaproveita arena suja). */
    public static final class Contador extends SavedData{
        long proxima;
        static Contador ler(CompoundTag t){var c=new Contador();c.proxima=t.getLong("proxima");return c;}
        @Override public CompoundTag save(CompoundTag t){t.putLong("proxima",proxima);return t;}
    }
    private static long proxima(MinecraftServer s){var c=s.overworld().getDataStorage().computeIfAbsent(Contador::ler,Contador::new,"minezin_vazio");long n=c.proxima++;c.setDirty();return n;}
    private static long agora(MinecraftServer s){return s.overworld().getGameTime();}
    private static void mensagem(ServerPlayer p,String s,ChatFormatting cor){p.displayClientMessage(Component.literal(s).withStyle(cor),false);}
    private static void pacote(ServerPlayer p,int tipo,Vec3 o){RegistoRede.ENVIAR.send(PacketDistributor.PLAYER.with(()->p),new PacoteSincronizarCutscene3D(tipo,p.getUUID(),o.x,o.y,o.z,0,0,0));}

    // ---------------- consultas usadas pelo Nemesis ----------------
    private static Instancia doChefe(Entity e){if(e.level().isClientSide||e.level().dimension()!=REINO)return null;for(var i:INSTANCIAS.values())if(e.getUUID().equals(i.chefe))return i;return null;}
    private static Instancia doJogador(UUID id){for(var i:INSTANCIAS.values())if(i.estado.participa(id))return i;return null;}
    public static boolean emInstancia(Entity chefe){return doChefe(chefe)!=null;}
    public static boolean travado(Entity chefe){var i=doChefe(chefe);return i!=null&&i.estado.fase()==EstadoInstancia.Fase.ENTRADA;}
    /** Alvo do chefe: o dono se estiver de pe; senao, outro participante de pe; fora de instancia, o dono. */
    public static ServerPlayer alvoChefe(Entity chefe,UUID dono){
        var s=chefe.getServer();if(s==null)return null;var i=doChefe(chefe);
        var p=dono==null?null:s.getPlayerList().getPlayer(dono);
        if(i==null)return p;
        if(p!=null&&i.estado.vivos().contains(dono))return p;
        for(var id:i.estado.vivos()){var o=s.getPlayerList().getPlayer(id);if(o!=null&&o.level()==chefe.level())return o;}
        return p;
    }
    /** Chefe morreu dentro da instancia: alma do despertar para o dono e vitoria para o grupo. */
    public static void chefeDerrotado(Entity chefe,UUID dono,int kit){
        var i=doChefe(chefe);if(i==null)return;var s=chefe.getServer();i.estado.vencer(agora(s));
        var p=dono==null?null:s.getPlayerList().getPlayer(dono);
        if(p!=null){RpgServidor.dados(p).remove("desafio");var alma=AlmaDespertar.criar(kit);if(!p.getInventory().add(alma))p.drop(alma,false);}
        for(var id:i.estado.todos()){var o=s.getPlayerList().getPlayer(id);if(o!=null)mensagem(o,"Desafio vencido! Voltando em "+EstadoInstancia.SAIDA_VITORIA/20+" s.",ChatFormatting.GOLD);}
    }

    // ---------------- entrada ----------------
    /** Altar: abre a instancia para o dono e quem estiver perto. Devolve false (com mensagem) se nao der. */
    public static boolean iniciar(ServerPlayer dono,BlockPos altar,PoderAnime kit){
        var s=dono.getServer();var reino=s.getLevel(REINO);
        if(reino==null){mensagem(dono,"A dimensão do Vazio não carregou (datapack ausente).",ChatFormatting.RED);return false;}
        if(dono.level().dimension()==REINO){mensagem(dono,"Você já está no Vazio.",ChatFormatting.RED);return false;}
        List<ServerPlayer> grupo=new ArrayList<>();grupo.add(dono);
        for(var o:dono.serverLevel().players())if(grupo.size()<MAX_GRUPO&&o!=dono&&o.isAlive()&&!o.isSpectator()&&doJogador(o.getUUID())==null&&o.position().distanceTo(Vec3.atCenterOf(altar))<=RAIO_GRUPO)grupo.add(o);
        long n=proxima(s);int[] c=PlantaArena.centro(n);BlockPos centro=new BlockPos(c[0],PlantaArena.CHAO,c[1]);
        construir(reino,centro,kit.id);
        var inst=new Instancia(n,centro,kit.id,grupo.stream().map(Entity::getUUID).toList(),agora(s));inst.dono=dono.getUUID();inst.inicio=agora(s);
        INSTANCIAS.put(n,inst);
        for(int k=0;k<grupo.size();k++){var p=grupo.get(k);salvarRetorno(p);double[] e=PlantaArena.entradaJogador(k,grupo.size());
            Vec3 destino=Vec3.atBottomCenterOf(centro).add(e[0],1,e[1]);float yaw=(float)Math.toDegrees(Math.atan2(e[0],-e[1]));
            p.teleportTo(reino,destino.x,destino.y,destino.z,yaw,0);p.fallDistance=0;pacote(p,ENTRADA,destino);
            if(p!=dono)mensagem(p,"Você foi puxado para o Vazio junto com "+dono.getGameProfile().getName()+".",ChatFormatting.DARK_PURPLE);}
        // chefe do lado oposto, depois do teleporte (a troca de dimensao limpa desafios antigos)
        var chefe=RegistroDespertar.NEMESIS.get().create(reino);
        if(chefe==null){encerrar(s,inst);return false;}
        double[] ec=PlantaArena.entradaChefe();chefe.configurarTamanho(kit.id);
        chefe.moveTo(centro.getX()+.5+ec[0],centro.getY()+1,centro.getZ()+.5+ec[1],90,0);
        chefe.preparar(dono,kit.id,Vec3.atBottomCenterOf(centro).add(0,1,0));
        inst.chefe=chefe.getUUID();RpgServidor.dados(dono).putUUID("desafio",chefe.getUUID());
        if(!reino.addFreshEntity(chefe)){encerrar(s,inst);return false;}
        Vec3 chao=Vec3.atBottomCenterOf(centro).add(0,1,0);
        for(var p:grupo)RegistoRede.ENVIAR.send(PacketDistributor.PLAYER.with(()->p),new PacoteSincronizarCutscene3D(CENA,p.getUUID(),chao.x,chao.y,chao.z,kit.id,chefe.getBbHeight(),0));
        mensagem(dono,"Desafio: "+PersonagemRpg.porId(kit.id).nemesis+" (grupo de "+grupo.size()+").",ChatFormatting.DARK_PURPLE);
        return true;
    }
    private static BlockState bloco(int kit,PlantaArena.Papel papel){
        // paleta por chefe: chao, detalhe, borda, pilar, topo do pilar
        Block[] t=switch(kit){
            case 1->new Block[]{Blocks.POLISHED_BLACKSTONE,Blocks.CRYING_OBSIDIAN,Blocks.POLISHED_BLACKSTONE_BRICKS,Blocks.OBSIDIAN,Blocks.SEA_LANTERN};          // Gojo / Toji
            case 2->new Block[]{Blocks.POLISHED_DEEPSLATE,Blocks.AMETHYST_BLOCK,Blocks.DEEPSLATE_TILES,Blocks.DEEPSLATE_BRICKS,Blocks.SHROOMLIGHT};               // Shadow / Aurora
            case 3->new Block[]{Blocks.SMOOTH_QUARTZ,Blocks.GOLD_BLOCK,Blocks.QUARTZ_BRICKS,Blocks.QUARTZ_PILLAR,Blocks.SEA_LANTERN};                           // Frieren / Qual
            case 4->new Block[]{Blocks.BLACKSTONE,Blocks.RED_NETHER_BRICKS,Blocks.POLISHED_BLACKSTONE_BRICKS,Blocks.NETHER_BRICKS,Blocks.SHROOMLIGHT};          // Asta / Liebe
            case 6->new Block[]{Blocks.WHITE_CONCRETE,Blocks.BLACK_CONCRETE,Blocks.SMOOTH_STONE,Blocks.BLACK_CONCRETE,Blocks.SEA_LANTERN};                      // Ichigo / Zangetsu
            case 12->new Block[]{Blocks.STONE_BRICKS,Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,Blocks.CHISELED_STONE_BRICKS,Blocks.STONE_BRICKS,Blocks.SEA_LANTERN}; // Killua
            case 16->new Block[]{Blocks.DARK_OAK_PLANKS,Blocks.PURPLE_TERRACOTTA,Blocks.STRIPPED_DARK_OAK_LOG,Blocks.DARK_OAK_LOG,Blocks.SHROOMLIGHT};         // Subaru
            default->new Block[]{Blocks.POLISHED_BLACKSTONE,Blocks.CRYING_OBSIDIAN,Blocks.POLISHED_BLACKSTONE_BRICKS,Blocks.OBSIDIAN,Blocks.SEA_LANTERN};
        };
        return switch(papel){
            case CHAO->t[0].defaultBlockState();case DETALHE->t[1].defaultBlockState();case BORDA->t[2].defaultBlockState();
            case PILAR->t[3].defaultBlockState();case TOPO_PILAR,LUZ->t[4].defaultBlockState();case BARREIRA->Blocks.BARRIER.defaultBlockState();
            default->Blocks.AIR.defaultBlockState();
        };
    }
    private static void construir(ServerLevel reino,BlockPos centro,int kit){
        int R=PlantaArena.RAIO+2;var m=new BlockPos.MutableBlockPos();
        for(int dx=-R;dx<=R;dx++)for(int dz=-R;dz<=R;dz++)for(int dy=-1;dy<=PlantaArena.TETO;dy++){
            var papel=PlantaArena.papel(dx,dy,dz);if(papel==PlantaArena.Papel.VAZIO)continue;
            reino.setBlock(m.set(centro.getX()+dx,centro.getY()+dy,centro.getZ()+dz),bloco(kit,papel),Block.UPDATE_CLIENTS);
        }
    }

    // ---------------- retorno ----------------
    private static void salvarRetorno(ServerPlayer p){
        var t=RpgServidor.dados(p);t.putString("vz_dim",p.level().dimension().location().toString());
        t.putDouble("vz_x",p.getX());t.putDouble("vz_y",p.getY());t.putDouble("vz_z",p.getZ());t.putFloat("vz_yaw",p.getYRot());
        t.putInt("vz_modo",p.gameMode.getGameModeForPlayer().getId());
    }
    /** Volta para onde estava antes do Vazio, no modo de jogo original. */
    static void retornar(ServerPlayer p){
        var t=RpgServidor.dados(p);var s=p.getServer();
        ServerLevel destino=t.contains("vz_dim")?s.getLevel(ResourceKey.create(Registries.DIMENSION,new ResourceLocation(t.getString("vz_dim")))):null;
        if(destino==null)destino=s.overworld();
        Vec3 pos=t.contains("vz_x")?new Vec3(t.getDouble("vz_x"),t.getDouble("vz_y"),t.getDouble("vz_z")):Vec3.atBottomCenterOf(destino.getSharedSpawnPos());
        var modo=GameType.byId(t.contains("vz_modo")?t.getInt("vz_modo"):GameType.SURVIVAL.getId());
        if(p.gameMode.getGameModeForPlayer()!=modo)p.setGameMode(modo);
        if(!p.isAlive())return;
        p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);p.teleportTo(destino,pos.x,pos.y,pos.z,t.getFloat("vz_yaw"),0);p.fallDistance=0;
        for(var k:new String[]{"vz_dim","vz_x","vz_y","vz_z","vz_yaw","vz_modo"})t.remove(k);
        pacote(p,SAIDA,pos);
    }
    private static void encerrar(MinecraftServer s,Instancia i){
        INSTANCIAS.remove(i.n);var reino=s.getLevel(REINO);
        if(reino!=null&&i.chefe!=null){var c=reino.getEntity(i.chefe);if(c!=null)c.discard();}
        for(var id:i.estado.todos()){var p=s.getPlayerList().getPlayer(id);if(p!=null){RpgServidor.dados(p).remove("desafio");retornar(p);}}
    }

    // ---------------- eventos ----------------
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||INSTANCIAS.isEmpty())return;var s=e.getServer();long agora=agora(s);var reino=s.getLevel(REINO);
        for(var i:List.copyOf(INSTANCIAS.values())){
            var antes=i.estado.fase();var fase=i.estado.tick(agora);
            if(fase==EstadoInstancia.Fase.ENCERRADA){encerrar(s,i);continue;}
            if(fase==EstadoInstancia.Fase.WIPE&&antes!=fase)for(var id:i.estado.todos()){var p=s.getPlayerList().getPlayer(id);if(p!=null)mensagem(p,"Wipe! O grupo caiu. Voltando...",ChatFormatting.RED);}
            if(fase==EstadoInstancia.Fase.ENTRADA)cena(reino,i,agora-i.inicio);
            if(fase==EstadoInstancia.Fase.COMBATE&&antes==EstadoInstancia.Fase.ENTRADA)for(var id:i.estado.vivos()){var p=s.getPlayerList().getPlayer(id);if(p!=null)p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);}
            for(var id:i.estado.todos()){
                var p=s.getPlayerList().getPlayer(id);if(p==null||p.level()!=reino)continue;
                if(fase==EstadoInstancia.Fase.ENTRADA&&i.estado.vivos().contains(id)){p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,5,9,false,false,false));p.setDeltaMovement(0,Math.min(0,p.getDeltaMovement().y),0);p.hurtMarked=true;}
                // espectador nao sai voando da arena
                Vec3 c=Vec3.atBottomCenterOf(i.centro);if(p.isSpectator()&&(p.position().distanceTo(c)>PlantaArena.RAIO+10||p.getY()<c.y-4))p.teleportTo(reino,c.x,c.y+8,c.z,p.getYRot(),30);
                else if(!p.isSpectator())corrigir(reino,i,p);
            }
        }
    }
    /**
     * Teleportes de tecnicas (Passo das Sombras, Teleporte Curto...) podem parar dentro do chao, de um pilar ou fora da
     * barreira: com a camera dentro do bloco o mundo some (tela preta). Aqui o jogador volta para o chao livre mais
     * perto, dentro da arena.
     */
    private static void corrigir(ServerLevel reino,Instancia i,ServerPlayer p){
        Vec3 c=Vec3.atBottomCenterOf(i.centro).add(0,1,0);Vec3 rel=p.position().subtract(c);double h=Math.hypot(rel.x,rel.z);
        boolean fora=h>PlantaArena.RAIO-2.5||p.getY()<c.y-.5||p.getY()>c.y+PlantaArena.TETO-2;
        boolean preso=!reino.noCollision(p,p.getBoundingBox().deflate(.05));
        if(!fora&&!preso)return;
        double k=h>PlantaArena.RAIO-3.5?(PlantaArena.RAIO-3.5)/Math.max(1e-6,h):1;
        double x=c.x+rel.x*k,z=c.z+rel.z*k;
        // sobe ate achar espaco livre (chao da arena ou topo de pilar)
        double y=Math.max(c.y,Math.min(p.getY(),c.y+PlantaArena.ALTURA_PILAR+1));
        for(int n=0;n<PlantaArena.ALTURA_PILAR+3;n++,y=Math.floor(y)+1){
            if(reino.noCollision(p,p.getBoundingBox().move(x-p.getX(),y-p.getY(),z-p.getZ()).deflate(.05)))break;
        }
        p.teleportTo(reino,x,y,z,p.getYRot(),p.getXRot());p.fallDistance=0;
    }
    /** Momentos do chefe na cutscene: rugido (pose + som) e a fala. */
    private static void cena(ServerLevel reino,Instancia i,long t){
        if(reino==null||i.chefe==null||!(reino.getEntity(i.chefe) instanceof NemesisEntity c))return;
        if(t==CinematicaEntrada.RUGIDO){c.rugir(true);reino.playSound(null,c.getX(),c.getY(),c.getZ(),net.minecraft.sounds.SoundEvents.RAVAGER_ROAR,net.minecraft.sounds.SoundSource.HOSTILE,2f,.6f);}
        if(t==CinematicaEntrada.FALA)reino.playSound(null,c.getX(),c.getY(),c.getZ(),net.minecraft.sounds.SoundEvents.EVOKER_PREPARE_SUMMON,net.minecraft.sounds.SoundSource.HOSTILE,1.6f,.55f);
        if(t==CinematicaEntrada.VOLTA)c.rugir(false);
    }
    /** Morte dentro da instancia: nao morre de verdade, vira espectador e nada cai. */
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void morte(LivingDeathEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||p.level().dimension()!=REINO)return;var i=doJogador(p.getUUID());if(i==null)return;
        e.setCanceled(true);p.setHealth(p.getMaxHealth());p.clearFire();p.removeAllEffects();p.fallDistance=0;
        p.setGameMode(GameType.SPECTATOR);i.estado.cair(p.getUUID(),agora(p.getServer()));
        Vec3 c=Vec3.atBottomCenterOf(i.centro);p.teleportTo(p.serverLevel(),c.x,c.y+8,c.z,p.getYRot(),30);
        mensagem(p,"Você caiu. Assista até o fim da luta; nada foi perdido.",ChatFormatting.GRAY);
    }
    /** O chao e a barreira nao quebram pelos jogadores (o chefe ainda pode destruir o terreno). */
    @SubscribeEvent public static void quebrar(BlockEvent.BreakEvent e){
        if(e.getPlayer()!=null&&!e.getPlayer().isCreative()&&e.getLevel() instanceof ServerLevel l&&l.dimension()==REINO)e.setCanceled(true);
    }
    @SubscribeEvent public static void sair(PlayerEvent.PlayerLoggedOutEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var i=doJogador(p.getUUID());if(i==null)return;
        i.estado.remover(p.getUUID(),agora(p.getServer()));
        // volta ja (salvo fora do Vazio); se o grupo acabou, a instancia encerra no proximo tick
        retornar(p);
    }
    /** Entrou no Vazio sem instancia (servidor reiniciou, desconectou no meio): volta para o retorno salvo. */
    @SubscribeEvent public static void entrar(PlayerEvent.PlayerLoggedInEvent e){
        if(e.getEntity() instanceof ServerPlayer p&&p.level().dimension()==REINO&&doJogador(p.getUUID())==null)retornar(p);
    }
    @SubscribeEvent public static void renascer(PlayerEvent.PlayerRespawnEvent e){
        if(e.getEntity() instanceof ServerPlayer p&&p.level().dimension()==REINO&&doJogador(p.getUUID())==null)retornar(p);
    }
    @SubscribeEvent public static void parar(ServerStoppingEvent e){for(var i:List.copyOf(INSTANCIAS.values()))encerrar(e.getServer(),i);INSTANCIAS.clear();}
    private VazioServidor(){}
}
