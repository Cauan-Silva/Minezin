package com.cauansilva.arcanecontrol.core.rank;

import com.cauansilva.arcanecontrol.core.rank.RankCacador.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Escolha de dificuldade do altar: rank do jogador com barra de progresso, rank medio do grupo e 3 botoes. */
public final class TelaDificuldade extends AbstractContainerScreen<MenuDificuldade> {
    private final Button[] botoes=new Button[Dificuldade.values().length];
    public TelaDificuldade(MenuDificuldade m,Inventory inv,Component t){super(m,inv,t);imageWidth=230;imageHeight=176;}
    @Override protected void init(){
        super.init();
        addRenderableWidget(Button.builder(Component.literal("◀"),b->clicarChefe(MenuDificuldade.ANTERIOR)).bounds(leftPos+15,topPos+56,20,20).build());
        addRenderableWidget(Button.builder(Component.literal("▶"),b->clicarChefe(MenuDificuldade.PROXIMO)).bounds(leftPos+195,topPos+56,20,20).build());
        for(var d:Dificuldade.values()){int i=d.ordinal();
            botoes[i]=addRenderableWidget(Button.builder(Component.literal(d.nome),b->clicar(i)).bounds(leftPos+15,topPos+88+i*26,200,20).build());
            botoes[i].setTooltip(Tooltip.create(Component.literal("Rank médio mínimo: "+d.minimo.name()+"\nVida do chefe x"+d.vida+" · Dano x"+d.dano+"\nPontos de Caçador x"+d.pontos)));}
    }
    private void clicarChefe(int b){clicar(b);}
    private void clicar(int b){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,b);}
    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){
        g.fill(leftPos-2,topPos-2,leftPos+imageWidth+2,topPos+imageHeight+2,0xFF4A3A6A);
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xF0100C16);
        int pts=menu.pontos();var r=RankCacador.rank(pts);
        int bx=leftPos+15,by=topPos+44,bw=200;
        g.fill(bx,by,bx+bw,by+4,0xFF2A2236);g.fill(bx,by,bx+(int)(bw*RankCacador.progresso(pts)),by+4,r.cor);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,8,7,0xFFE0D0FF,false);
        int pts=menu.pontos();var r=RankCacador.rank(pts);var n=r.proximo();
        g.drawString(font,"Seu Rank: ",15,24,0xFFBBBBBB,false);g.drawString(font,r.name(),15+font.width("Seu Rank: "),24,r.cor,true);
        g.drawString(font,n==null?"máximo":pts+" / "+n.minimo,150,24,0xFF888888,false);
        var m=menu.media();
        var k=menu.chefe();var pr=com.cauansilva.arcanecontrol.core.rpg.PersonagemRpg.porId(k.id);String nome=pr==null?k.nome:pr.nemesis;
        g.drawCenteredString(font,nome,115,57,0xFF000000|k.cor);g.drawCenteredString(font,"Nêmesis de "+k.nome,115,67,0xFF888888);
        g.drawString(font,"Grupo: "+menu.grupo()+" · média ",15,33,0xFFBBBBBB,false);g.drawString(font,m.name(),15+font.width("Grupo: "+menu.grupo()+" · média "),33,m.cor,true);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float pt){
        var m=menu.media();
        for(var d:Dificuldade.values()){var b=botoes[d.ordinal()];boolean ok=m.ordinal()>=d.minimo.ordinal();b.active=ok;
            b.setMessage(Component.literal(ok?d.nome:d.nome+" (exige Rank "+d.minimo.name()+")"));}
        renderBackground(g);super.render(g,mx,my,pt);renderTooltip(g,mx,my);
    }
}
