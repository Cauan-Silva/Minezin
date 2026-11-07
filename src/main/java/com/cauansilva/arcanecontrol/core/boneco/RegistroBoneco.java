package com.cauansilva.arcanecontrol.core.boneco;

import com.cauansilva.arcanecontrol.core.cid.EspadaCid;
import com.cauansilva.arcanecontrol.core.rpg.RegistroDespertar;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import java.util.List;

/** Entidade e item do Boneco de Treino. */
@Mod.EventBusSubscriber(modid="arcanecontrol",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RegistroBoneco {
    public static final RegistryObject<EntityType<BonecoEntity>> BONECO=RegistroDespertar.ENTIDADES.register("boneco_treino",
        ()->EntityType.Builder.<BonecoEntity>of(BonecoEntity::new,MobCategory.MISC).sized(.6f,1.9f).clientTrackingRange(10).fireImmune().build("arcanecontrol:boneco_treino"));
    public static final RegistryObject<Item> ITEM_BONECO=EspadaCid.ITEMS.register("boneco_treino",ItemBoneco::new);
    /** Forca o carregamento da classe antes do registro. */
    public static void carregar(){}

    @SubscribeEvent public static void atributos(EntityAttributeCreationEvent e){e.put(BONECO.get(),BonecoEntity.atributos().build());}

    @Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Cliente {
        private static final ResourceLocation TEX=new ResourceLocation("arcanecontrol","textures/entity/boneco_treino.png");
        @SubscribeEvent public static void render(EntityRenderersEvent.RegisterRenderers e){
            e.registerEntityRenderer(BONECO.get(),c->new HumanoidMobRenderer<BonecoEntity,HumanoidModel<BonecoEntity>>(c,new HumanoidModel<>(c.bakeLayer(ModelLayers.ZOMBIE)),.4f){
                @Override public ResourceLocation getTextureLocation(BonecoEntity b){return TEX;}});
        }
    }

    public static final class ItemBoneco extends Item {
        ItemBoneco(){super(new Item.Properties().stacksTo(16));}
        @Override public InteractionResult useOn(UseOnContext c){
            Level l=c.getLevel();if(l.isClientSide)return InteractionResult.SUCCESS;
            var pos=c.getClickedPos().relative(c.getClickedFace());var b=BONECO.get().create((ServerLevel)l);if(b==null)return InteractionResult.FAIL;
            float yaw=c.getPlayer()==null?0:c.getPlayer().getYRot()+180;
            b.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,yaw,0);b.setYHeadRot(yaw);b.setYBodyRot(yaw);
            if(!l.noCollision(b))return InteractionResult.FAIL;
            l.addFreshEntity(b);if(c.getPlayer()==null||!c.getPlayer().getAbilities().instabuild)c.getItemInHand().shrink(1);
            return InteractionResult.CONSUME;
        }
        @Override public void appendHoverText(ItemStack s,Level l,List<Component> t,TooltipFlag f){
            t.add(Component.literal("Mede DPS, maior golpe e dano total. Nunca morre.").withStyle(ChatFormatting.GRAY));
            t.add(Component.literal("Agachado + clique direito no boneco: recolher").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    private RegistroBoneco(){}
}
