package com.cauansilva.arcanecontrol.core.client;
import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;
import com.cauansilva.arcanecontrol.core.client.render.GerenciadorCutscene3D;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class RpgHud {
    @SubscribeEvent public static void desenhar(RenderGuiOverlayEvent.Post e){
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.options.hideGui||mc.player.isSpectator()||!RpgCliente.recebido()||GerenciadorCutscene3D.cutsceneTipo!=0
                ||!e.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.id()))return;
        var kit=PoderAnime.porId(RpgCliente.ativo());if(kit==null)return;
        var g=e.getGuiGraphics();int x=e.getWindow().getGuiScaledWidth()-170,y=e.getWindow().getGuiScaledHeight()-138;
        g.fill(x,y,x+162,y+72,0xDA0D1422);
        g.drawString(mc.font,kit.nome+" · Nv."+RpgCliente.mestria(kit.id).nivel()+" [H]",x+6,y+5,kit.cor);
        g.fill(x+6,y+18,x+156,y+25,0xFF26354A);g.fill(x+6,y+18,x+6+(int)(150*RpgCliente.mana()/100),y+25,0xFF42A8D7);
        g.drawCenteredString(mc.font,"Mana "+(int)RpgCliente.mana()+" / 100",x+81,y+17,0xF3FAFF);
        int quantidade=5;
        if(RpgCliente.forma())g.drawString(mc.font,kit==com.cauansilva.arcanecontrol.core.poderes.PoderAnime.ICHIGO?"Bankai • mantido por mana":"Despertar: "+((RpgCliente.formaTicks()+19)/20)+"s",x+6,y-12,kit.cor);
        for(int i=0;i<quantidade;i++){
            int bx=x+6+i*30,by=y+32;boolean liberada=RpgCliente.tecnica(kit.id,i);int cd=RpgCliente.recarga(i);boolean semMana=RpgCliente.mana()+1e-9<RpgCliente.custo(i);
            g.fill(bx,by,bx+26,by+25,liberada?0xFF26384F:0xFF202631);g.renderOutline(bx,by,26,25,liberada?0xFF000000|kit.cor:0xFF485363);
            if(cd>0)setor(g,bx+13,by+12,12,RpgCliente.fracao(i));
            g.drawCenteredString(mc.font,new String[]{"Z","X","C","V","G"}[i],bx+13,by+8,liberada?(cd>0?0xAAB2BE:semMana?0xB48585:0xFFFFFF):0x657083);
            g.drawCenteredString(mc.font,RpgCliente.forma()&&i==4?"FIM":RpgCliente.forma()&&i==3&&RpgCliente.exclusivoUsado()?"USO":!liberada?"--":cd>0?((cd+19)/20)+"s":semMana?"MP":"OK",bx+13,by+28,liberada?0xC4D3E6:0x657083);
        }
    }
    private static void setor(GuiGraphics g,int x,int y,int r,double fracao){
        if(fracao<=0)return;g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);RenderSystem.setShaderColor(1,1,1,1);
        var b=Tesselator.getInstance().getBuilder();var m=g.pose().last().pose();b.begin(VertexFormat.Mode.TRIANGLE_FAN,DefaultVertexFormat.POSITION_COLOR);
        b.vertex(m,x,y,0).color(4,8,14,210).endVertex();int n=Math.max(1,(int)Math.ceil(fracao*48));
        for(int i=0;i<=n;i++){double a=-Math.PI/2+Math.PI*2*fracao*i/n;b.vertex(m,(float)(x+Math.cos(a)*r),(float)(y+Math.sin(a)*r),0).color(4,8,14,210).endVertex();}
        Tesselator.getInstance().end();RenderSystem.depthMask(true);RenderSystem.disableBlend();
    }
}
