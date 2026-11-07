package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.Reforja;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Tela do Ferreiro: fundo de bau recortado, 2 slots e 3 botoes com o custo do tier da reliquia no slot. */
public final class TelaFerreiro extends AbstractContainerScreen<MenuFerreiro> {
    private static final ResourceLocation FUNDO=new ResourceLocation("minecraft","textures/gui/container/generic_54.png");
    private Button desmanchar,valores,tudo;
    private int tierDica=-1;
    public TelaFerreiro(MenuFerreiro m,Inventory inv,Component t){super(m,inv,t);imageHeight=166;inventoryLabelY=imageHeight-94;}
    @Override protected void init(){
        super.init();
        desmanchar=addRenderableWidget(Button.builder(Component.literal("Desmanchar"),b->clicar(MenuFerreiro.DESMANCHAR)).bounds(leftPos+7,topPos+50,52,16).build());
        valores=addRenderableWidget(Button.builder(Component.literal("Valores"),b->clicar(MenuFerreiro.VALORES)).bounds(leftPos+62,topPos+50,52,16).build());
        tudo=addRenderableWidget(Button.builder(Component.literal("Tudo"),b->clicar(MenuFerreiro.TUDO)).bounds(leftPos+117,topPos+50,52,16).build());
        tierDica=-1;
    }
    private void clicar(int b){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,b);}
    @Override protected void renderBg(GuiGraphics g,float parcial,int mx,int my){
        g.blit(FUNDO,leftPos,topPos,0,0,imageWidth,71);g.blit(FUNDO,leftPos,topPos+71,0,126,imageWidth,96);
        // cobre as linhas de slots do bau e desenha so os 2 slots do Ferreiro
        g.fill(leftPos+7,topPos+17,leftPos+169,topPos+70,0xFFC6C6C6);
        for(int x:new int[]{43,115}){g.fill(leftPos+x,topPos+23,leftPos+x+18,topPos+41,0xFF8B8B8B);g.fill(leftPos+x+1,topPos+24,leftPos+x+17,topPos+40,0xFF373737);}
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        super.renderLabels(g,mx,my);
        g.drawString(font,"Relíquia",30,14,0x404040,false);g.drawString(font,"Essência",102,14,0x404040,false);
        var r=ItemReliquia.ler(menu.reliquia());
        if(r!=null)g.drawString(font,"T"+r.tier(),80,28,0x7A3FB0,false);
    }
    private static Tooltip dica(String t){return Tooltip.create(Component.literal(t));}
    @Override public void render(GuiGraphics g,int mx,int my,float parcial){
        var r=ItemReliquia.ler(menu.reliquia());int e=menu.essencia();
        desmanchar.active=r!=null;
        valores.active=r!=null&&e>=Reliquias.custo(r.tier(),Reforja.VALORES);
        tudo.active=r!=null&&e>=Reliquias.custo(r.tier(),Reforja.TUDO);
        int t=r==null?0:r.tier();
        if(t!=tierDica){tierDica=t;
            if(r==null){desmanchar.setTooltip(dica("Coloque uma relíquia"));valores.setTooltip(dica("Coloque uma relíquia"));tudo.setTooltip(dica("Coloque uma relíquia"));}
            else{desmanchar.setTooltip(dica("Destrói a relíquia: +"+Reliquias.DESMANCHE[t]+" Essência"));
                valores.setTooltip(dica("Re-sorteia os valores dos mesmos afixos: "+Reliquias.custo(t,Reforja.VALORES)+" Essência"));
                tudo.setTooltip(dica("Sorteia afixos novos (mesmo tier e set): "+Reliquias.custo(t,Reforja.TUDO)+" Essência"));}}
        renderBackground(g);super.render(g,mx,my,parcial);renderTooltip(g,mx,my);
    }
}
