package com.cauansilva.arcanecontrol.core.equipamentos;
import net.minecraft.world.item.*;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
public class TrajeAnime extends ArmorItem implements GeoItem {
    public final String estilo;
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public TrajeAnime(String estilo,Type tipo){super(estilo.equals("frieren")?ArmorMaterials.DIAMOND:ArmorMaterials.NETHERITE,tipo,
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());this.estilo=estilo;}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void appendHoverText(ItemStack s,net.minecraft.world.level.Level l,java.util.List<net.minecraft.network.chat.Component> texto,TooltipFlag f){
        super.appendHoverText(s,l,texto,f);
        texto.add(net.minecraft.network.chat.Component.literal("Conjunto completo: -20% de mana no kit correspondente.").withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
        texto.add(net.minecraft.network.chat.Component.literal(estilo.equals("shadow")?"Conjunto Shadow: capuz e capa de slime.":estilo.equals("gojo")?"Ferraria: papel + peça de netherita + ametista":"Ferraria: papel + peça de diamante + ouro").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar r){r.add(new AnimationController<>(this,"tecido",5,s->s.setAndContinue(RawAnimation.begin().thenLoop("animation.traje.idle"))));}
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer){
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions(){
            private software.bernie.geckolib.renderer.GeoArmorRenderer<TrajeAnime> armadura;
            private software.bernie.geckolib.renderer.GeoItemRenderer<TrajeAnime> icone;
            @Override public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(net.minecraft.world.entity.LivingEntity e,ItemStack s,net.minecraft.world.entity.EquipmentSlot slot,net.minecraft.client.model.HumanoidModel<?> original){
                if(armadura==null)armadura=new RenderTrajeAnime();
                armadura.prepForRender(e,s,slot,original);return armadura;
            }
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){
                if(icone==null)icone=new software.bernie.geckolib.renderer.GeoItemRenderer<>(new ModeloTrajeAnime(true));return icone;
            }
        });
    }
}
