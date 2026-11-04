package com.cauansilva.arcanecontrol.core.cid;
import com.cauansilva.arcanecontrol.ConfigCliente;
import com.cauansilva.arcanecontrol.core.vfx.RelogioAtomic;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
/** The reference's two-word cadence, shown to the caster and to everyone inside the Atomic's range (user). */
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class LetreiroAtomic {
    /** The dome plus a margin, so whoever stands at its edge also reads it. */
    public static final double ALCANCE=com.cauansilva.arcanecontrol.core.vfx.ExplosaoAtomic.RAIO_DOMO+8;
    @SubscribeEvent public static void desenhar(RenderGuiOverlayEvent.Post event){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.options.hideGui||!ConfigCliente.CINEMATICAS.get()||!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id()))return;
        double tick=EfeitosCid.atomicNoAlcance(mc.getFrameTime(),ALCANCE);if(tick<0)return;
        String texto=RelogioAtomic.letreiro(tick);if(texto.isEmpty())return;
        var g=event.getGuiGraphics();var palavra=Component.literal(texto).withStyle(ChatFormatting.BOLD);
        int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();
        float scale=(float)Math.min(w*.84/mc.font.width(palavra),h*(texto.equals("ATOMIC")?.22:.045)/mc.font.lineHeight);
        g.pose().pushPose();g.pose().translate(w*.5,h*(texto.equals("ATOMIC")?.43:.57),0);g.pose().scale(scale,scale,1);
        try{g.drawCenteredString(mc.font,palavra,0,0,0xFFF8FF);}finally{g.pose().popPose();}
    }
    private LetreiroAtomic(){}
}
