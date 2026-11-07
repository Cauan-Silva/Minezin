package com.cauansilva.arcanecontrol.core.reliquias;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Tela da Bolsa de Reliquias: fundo do funil vanilla (5 slots) com o nome de cada slot. */
public final class TelaBolsa extends AbstractContainerScreen<MenuBolsa> {
    private static final ResourceLocation FUNDO=new ResourceLocation("minecraft","textures/gui/container/hopper.png");
    private static final String[] ROTULO={"Anel","Colar","Amuleto","Brinco","Mítica"};
    public TelaBolsa(MenuBolsa m,Inventory inv,Component titulo){super(m,inv,titulo);imageHeight=133;inventoryLabelY=imageHeight-94;}
    @Override protected void renderBg(GuiGraphics g,float parcial,int mx,int my){g.blit(FUNDO,leftPos,topPos,0,0,imageWidth,imageHeight);}
    @Override public void render(GuiGraphics g,int mx,int my,float parcial){
        renderBackground(g);super.render(g,mx,my,parcial);
        for(int i=0;i<MenuBolsa.SLOTS;i++)if(!menu.getSlot(i).hasItem()&&mx>=leftPos+44+i*18&&mx<leftPos+60+i*18&&my>=topPos+20&&my<topPos+36)
            g.renderTooltip(font,Component.literal(ROTULO[i]),mx,my);
        renderTooltip(g,mx,my);
    }
}
