package com.cauansilva.arcanecontrol.core.rpg;

import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.progressao.*;
import com.cauansilva.arcanecontrol.core.rede.*;
import com.cauansilva.arcanecontrol.core.equipamentos.RegistroTrajes;
import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;

@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class RpgServidor {
    private static final String CHAVE="minezin_rpg_v3";
    private static final UUID VIDA=UUID.fromString("0688bac9-4859-461e-afd5-6addcc8fc23e"),MOVIMENTO=UUID.fromString("bb5d7fb3-bf3f-4020-a12c-f90319ca22cb");
    public static long agora(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static CompoundTag dados(ServerPlayer p){
        var raiz=p.getPersistentData();
        if(!raiz.contains(Player.PERSISTED_NBT_TAG))raiz.put(Player.PERSISTED_NBT_TAG,new CompoundTag());
        var persistido=raiz.getCompound(Player.PERSISTED_NBT_TAG);
        if(!persistido.contains(CHAVE)){
            var tag=new CompoundTag();persistido.put(CHAVE,tag);tag.putInt("versao",1);tag.putDouble("mana",100);
            var legado=ProgressaoPoderes.ler(p);var gojo=ProgressaoGojo.ler(p);long[] prazos=new long[75];int[] duracoes=new int[75];
            for(var kit:PoderAnime.values())if(legado.tem(kit)){
                int xp=kit==PoderAnime.GOJO?gojo.xp():legado.xp(kit);boolean x,c,v;
                if(kit==PoderAnime.GOJO){x=gojo.tem(TecnicaGojo.VERMELHO);c=gojo.tem(TecnicaGojo.INFINITO);v=gojo.tem(TecnicaGojo.ROXO);}
                else{x=legado.nivel(kit)>=2;c=legado.nivel(kit)>=3;v=legado.nivel(kit)>=5;}
                salvarMestria(tag,kit.id,MestriaRpg.migrar(xp,x,c,v));
            }
            tag.putBoolean("dominioLegado",gojo.tem(TecnicaGojo.DOMINIO));
            long[] gojoCd=persistido.getCompound("arcanecontrol_gojo").getLongArray("cooldowns");
            for(int i=0;i<5;i++)if(i<gojoCd.length)prazos[i]=gojoCd[i];
            var cid=persistido.getCompound("cid");for(int i=0;i<4;i++)prazos[5+i]=cid.getLong("cid_cd_"+i);
            var fri=ProgressaoPoderes.tag(p).getLongArray("fri_cds");for(int i=0;i<4&&i<fri.length;i++)prazos[10+i]=fri[i];
            long now=agora(p);for(int i=0;i<75;i++){prazos[i]=Math.max(0,Math.min(now+12000,prazos[i]));duracoes[i]=(int)Math.max(0,prazos[i]-now);}
            tag.putLongArray("prazos",prazos);tag.putIntArray("duracoes",duracoes);
        }
        var atual=persistido.getCompound(CHAVE);
        if(atual.getInt("versao")<2){PerfilRpgNbt.salvarRecargas(atual,PerfilRpgNbt.recargas(atual));atual.putInt("versao",2);}
        return atual;
    }
    public static void salvarMestria(CompoundTag raiz,int kit,MestriaRpg m){
        PerfilRpgNbt.salvar(raiz,kit,m);
    }
    public static MestriaRpg mestria(ServerPlayer p,int kit){
        var raiz=dados(p);
        if(!raiz.contains("kit_"+kit))salvarMestria(raiz,kit,new MestriaRpg());
        return PerfilRpgNbt.ler(raiz,kit);
    }
    public static boolean podeUsar(ServerPlayer p,PoderAnime kit,int slot){
        if(kit==null||slot<0||slot>=kit.tecnicas.length||!p.isAlive()||p.isSpectator())return false;
        if(p.hasEffect(com.cauansilva.arcanecontrol.core.asta.RegistroAsta.SILENCIO.get())){p.displayClientMessage(Component.literal("Silenciado: aguarde a antimagia terminar."),true);return false;}
        var e=ProgressaoPoderes.ler(p);if(!e.tem(kit)||e.ativo()!=kit)return false;
        if(slot==4)return mestria(p,kit.id).despertarLiberado();
        return p.isCreative()||mestria(p,kit.id).podeUsarSlot(slot);
    }
    private static RecargasRpg recargas(ServerPlayer p){return PerfilRpgNbt.recargas(dados(p));}
    public static boolean conjurar(ServerPlayer p,PoderAnime kit,int slot,int baseTicks){
        if(!podeUsar(p,kit,slot)){mensagem(p,"Técnica bloqueada. Confira o kit em P e a árvore em H.");return false;}
        var mana=new RecursoMana(dados(p).getDouble("mana"));var cds=recargas(p);
        var resultado=ConjuracaoRpg.tentar(mestria(p,kit.id),mana,cds,PersonagemRpg.porId(kit.id),slot,agora(p),baseTicks,
                ConjuracaoRpg.custoBase(slot),conjuntoCompleto(p,kit),p.isCreative(),false,DespertaresServidor.ativo(p));
        if(resultado!=ConjuracaoRpg.Resultado.OK){
            mensagem(p,resultado==ConjuracaoRpg.Resultado.MANA?"Mana insuficiente.":resultado==ConjuracaoRpg.Resultado.RECARGA?"Técnica em recuperação.":"Técnica bloqueada. Abra H.");return false;
        }
        var tag=dados(p);tag.putDouble("mana",mana.atual());PerfilRpgNbt.salvarRecargas(tag,cds);sincronizar(p);return true;
    }
    public static boolean consumirMana(ServerPlayer p,PoderAnime kit,double custo){
        if(p.isCreative())return true;var mana=new RecursoMana(dados(p).getDouble("mana"));
        if(!mana.consumir(custo,conjuntoCompleto(p,kit)))return false;
        dados(p).putDouble("mana",mana.atual());sincronizar(p);return true;
    }
    public static void uso(ServerPlayer p,PoderAnime kit){ganharXp(p,kit,5);}
    public static void ganharXp(ServerPlayer p,PoderAnime kit,int xp){
        if(!ProgressaoPoderes.ler(p).tem(kit)||xp<=0)return;
        var m=mestria(p,kit.id);int niveis=m.ganharXp(xp);salvarMestria(dados(p),kit.id,m);
        if(niveis>0){
            aplicarAtributos(p);p.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,p.getX(),p.getY()+1,p.getZ(),24,.5,.7,.5,.15);
            p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.PLAYER_LEVELUP,SoundSource.PLAYERS,1,1);
        }
        sincronizar(p);
    }
    public static void comprar(ServerPlayer p,int kitId,int indice){
        if(!p.isAlive()||p.isSpectator()||indice<0||indice>=NoRpg.values().length)return;
        var kit=PoderAnime.porId(kitId);if(kit==null||!ProgressaoPoderes.ler(p).tem(kit))return;
        long now=agora(p),antes=p.getPersistentData().getLong("rpg_compra_tick");if(antes>0&&now-antes<4)return;
        p.getPersistentData().putLong("rpg_compra_tick",now);var no=NoRpg.values()[indice];
        var m=mestria(p,kit.id);
        if(m.comprar(no)){salvarMestria(dados(p),kit.id,m);aplicarAtributos(p);sincronizar(p);}
        else mensagem(p,"Confira nível, pontos e pré-requisitos na árvore.");
    }
    private static void mensagem(ServerPlayer p,String texto){p.displayClientMessage(Component.literal(texto),true);sincronizar(p);}
    public static void sincronizar(ServerPlayer p){
        var legado=ProgressaoPoderes.ler(p);int[] xp=new int[15],nos=new int[15];boolean[] despertar=new boolean[15];
        for(var kit:PoderAnime.values())if(legado.tem(kit)){var m=mestria(p,kit.id);xp[kit.id-1]=m.xp();nos[kit.id-1]=m.nos();despertar[kit.id-1]=m.despertarLiberado();}
        int[] restante=new int[5],duracao=new int[5];var cds=recargas(p);var atual=PersonagemRpg.porId(legado.ativo().id);
        for(int i=0;i<5;i++){restante[i]=cds.restante(atual,i,agora(p),DespertaresServidor.ativo(p));duracao[i]=cds.duracao(atual,i,DespertaresServidor.ativo(p));}
        RegistoRede.ENVIAR.send(PacketDistributor.PLAYER.with(()->p),new PacoteEstadoRpg(legado.ativo().id,legado.desbloqueados(),xp,nos,despertar,
                (float)new RecursoMana(dados(p).getDouble("mana")).atual(),restante,duracao,p.isCreative(),dados(p).getBoolean("dominioLegado"),conjuntoCompleto(p,legado.ativo()),DespertaresServidor.restante(p),DespertaresServidor.exclusivoUsado(p)));
    }
    public static void aplicarAtributos(ServerPlayer p){
        var m=mestria(p,ProgressaoPoderes.ler(p).ativo().id);
        atributo(p.getAttribute(Attributes.MAX_HEALTH),VIDA,"minezin_mestria_vida",m.vidaAdicional(),AttributeModifier.Operation.ADDITION);
        atributo(p.getAttribute(Attributes.MOVEMENT_SPEED),MOVIMENTO,"minezin_mestria_movimento",m.velocidadeAdicional(),AttributeModifier.Operation.MULTIPLY_TOTAL);
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
    }
    private static void atributo(AttributeInstance a,UUID id,String nome,double valor,AttributeModifier.Operation operacao){
        if(a==null)return;var atual=a.getModifier(id);if(atual!=null&&Math.abs(atual.getAmount()-valor)<1e-9)return;
        if(atual!=null)a.removeModifier(id);if(valor!=0)a.addPermanentModifier(new AttributeModifier(id,nome,valor,operacao));
    }
    private static boolean conjuntoCompleto(ServerPlayer p,PoderAnime kit){
        Item[] itens=switch(kit){
            case GOJO->new Item[]{RegistroTrajes.GOJO_CABECA.get(),RegistroTrajes.GOJO_PEITO.get(),RegistroTrajes.GOJO_PERNAS.get(),RegistroTrajes.GOJO_BOTAS.get()};
            case CID->new Item[]{EspadaCid.CAPUZ.get(),EspadaCid.MANTO.get(),EspadaCid.CALCAS.get(),EspadaCid.BOTAS.get()};
            case ASTA->com.cauansilva.arcanecontrol.core.asta.RegistroAsta.conjunto();
            case FRIEREN->new Item[]{RegistroTrajes.FRI_CABECA.get(),RegistroTrajes.FRI_PEITO.get(),RegistroTrajes.FRI_PERNAS.get(),RegistroTrajes.FRI_BOTAS.get()};
        };
        EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
        for(int i=0;i<4;i++)if(!p.getItemBySlot(slots[i]).is(itens[i]))return false;return true;
    }
    @SubscribeEvent public static void entrar(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){dados(p);aplicarAtributos(p);sincronizar(p);}}
    @SubscribeEvent public static void renascer(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p){aplicarAtributos(p);p.setHealth(p.getMaxHealth());sincronizar(p);}}
    @SubscribeEvent public static void dimensao(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){aplicarAtributos(p);sincronizar(p);}}
    @SubscribeEvent public static void clonar(PlayerEvent.Clone e){if(e.getEntity() instanceof ServerPlayer p&&e.getOriginal() instanceof ServerPlayer antigo){
        var raiz=p.getPersistentData();if(!raiz.contains(Player.PERSISTED_NBT_TAG))raiz.put(Player.PERSISTED_NBT_TAG,new CompoundTag());
        raiz.getCompound(Player.PERSISTED_NBT_TAG).put(CHAVE,dados(antigo).copy());
    }}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator())return;
        if(p.tickCount%5==0){var mana=new RecursoMana(dados(p).getDouble("mana"));mana.regenerar(5,5*mestria(p,ProgressaoPoderes.ler(p).ativo().id).multiplicadorRegeneracao());dados(p).putDouble("mana",mana.atual());}
        if(p.tickCount%10==0)sincronizar(p);
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void dano(LivingHurtEvent e){
        if(e.getAmount()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getEntity()==p)return;
        var m=mestria(p,ProgressaoPoderes.ler(p).ativo().id);
        boolean magico=e.getSource().is(DamageTypes.MAGIC)||e.getSource().is(DamageTypes.INDIRECT_MAGIC);
        e.setAmount((float)(e.getAmount()*(magico?m.multiplicadorMagico():m.multiplicadorFisico())));
    }
    @SubscribeEvent public static void danoCausado(LivingDamageEvent e){
        if(e.getAmount()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||!p.isAlive()||e.getEntity()==p||e.getEntity().isAlliedTo(p))return;
        if(!(e.getEntity() instanceof Enemy)&&!(e.getEntity() instanceof ServerPlayer))return;
        long now=agora(p),ultimo=p.getPersistentData().getLong("rpg_dano_xp_tick");if(ultimo>0&&now-ultimo<10)return;
        p.getPersistentData().putLong("rpg_dano_xp_tick",now);ganharXp(p,ProgressaoPoderes.ler(p).ativo(),Math.max(1,Math.min(10,(int)e.getAmount())));
    }
    @SubscribeEvent public static void derrotar(LivingDeathEvent e){if(e.getEntity() instanceof Enemy&&e.getSource().getEntity() instanceof ServerPlayer p)ganharXp(p,ProgressaoPoderes.ler(p).ativo(),25);}
}
