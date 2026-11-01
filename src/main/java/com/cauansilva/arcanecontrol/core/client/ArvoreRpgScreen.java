package com.cauansilva.arcanecontrol.core.client;
import com.cauansilva.arcanecontrol.core.rpg.*;
import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;
import com.cauansilva.arcanecontrol.core.rede.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ArvoreRpgScreen extends Screen {
    private PoderAnime foco=PoderAnime.porId(RpgCliente.ativo());
    private LayoutArvoreRpg layout;
    private final Button[] nos=new Button[8];
    private PostChain blur;
    private static final String[] ROTULOS={"X","C","V","+Físico","+Magia","+Mana","+Mov.","Despertar"};
    private static final String[] DETALHES={"Desbloqueia a técnica X.","Desbloqueia a técnica C.","Desbloqueia a técnica V.","+10% de dano físico.","+10% de dano mágico.","+25% de regeneração de mana.","+10% de velocidade de movimento.","Nível 50: compre este nó, vença seu chefe no Altar e use a alma. G ativa a forma."};
    public ArvoreRpgScreen(){this(PoderAnime.porId(RpgCliente.ativo()));}
    public ArvoreRpgScreen(PoderAnime foco){super(Component.literal("Árvore de habilidades"));this.foco=foco==null?PoderAnime.GOJO:foco;}
    @Override protected void init(){
        layout=LayoutArvoreRpg.paraTela(width,height);int x=layout.x(),y=layout.y(),w=layout.largura();
        for(var kit:PoderAnime.values())addRenderableWidget(Button.builder(Component.literal(kit.nome),b->foco=kit)
                .bounds(x+12+(kit.id-1)*(w-24)/3,y+42,(w-24)/3-4,18).build());
        for(var no:NoRpg.values())nos[no.indice]=addRenderableWidget(Button.builder(Component.literal(ROTULOS[no.indice]),b->{
            b.active=false;RegistoRede.ENVIAR.sendToServer(new PacoteAcaoRpg(1,foco.id,no.indice));
        }).bounds(layout.nodeX(no.indice),layout.nodeY(no.indice),layout.nodeLargura(),18).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"),b->onClose()).bounds(x+w-80,y+layout.altura()-26,68,18).build());
        RegistoRede.ENVIAR.sendToServer(new PacoteAcaoRpg(0,0,0));
        if(blur==null&&minecraft.gameRenderer.currentEffect()==null){
            minecraft.gameRenderer.loadEffect(new ResourceLocation("minecraft","shaders/post/blur.json"));blur=minecraft.gameRenderer.currentEffect();
        }
    }
    @Override public void removed(){
        if(blur!=null&&minecraft.gameRenderer.currentEffect()==blur)minecraft.gameRenderer.shutdownEffect();blur=null;super.removed();
    }
    @Override public void render(GuiGraphics g,int mx,int my,float parcial){
        renderBackground(g);int x=layout.x(),y=layout.y(),w=layout.largura(),h=layout.altura();var m=RpgCliente.mestria(foco.id);
        g.fill(x,y,x+w,y+h,0xEC0C1322);g.renderOutline(x,y,w,h,0xFF000000|foco.cor);
        g.drawCenteredString(font,title,x+w/2,y+9,0xFFFFFF);
        g.drawCenteredString(font,foco.nome+" · "+(RpgCliente.tem(foco.id)?"Maestria "+m.nivel():"Pesquise este kit em P"),x+w/2,y+25,foco.cor);
        g.drawString(font,"Nível "+m.nivel()+" / 100 · "+m.xp()+" XP",x+12,y+69,0xDFE7F4);
        g.drawString(font,"Pontos: "+m.pontos(),x+w-86,y+69,0xE8CE83);
        int barra=w-24;g.fill(x+12,y+83,x+w-12,y+87,0xFF26344A);
        g.fill(x+12,y+83,x+12+barra*(m.nivel()==100?100:m.xp()%100)/100,y+87,0xFF000000|foco.cor);
        for(int i=0;i<3;i++)g.hLine(layout.nodeX(i)+layout.nodeLargura(),layout.nodeX(i+1),layout.nodeY(i)+9,0xFF435675);
        for(int i=3;i<=6;i++){
            int nx=layout.nodeX(i)+layout.nodeLargura()/2,ny=layout.nodeY(i);
            g.vLine(nx,layout.nodeY(7)+27,ny,0xFF293A54);g.hLine(nx,layout.nodeX(7)+layout.nodeLargura()/2,layout.nodeY(7)+27,0xFF293A54);
        }
        NoRpg hover=null;
        for(var no:NoRpg.values()){
            var b=nos[no.indice];boolean tem=m.tem(no);
            b.setMessage(Component.literal((tem?"OK ":"")+ROTULOS[no.indice]));
            b.active=RpgCliente.recebido()&&RpgCliente.tem(foco.id)&&!tem&&m.nivel()>=no.nivel&&m.pontos()>=no.custo&&(m.nos()&no.requisitos)==no.requisitos;
            g.drawCenteredString(font,"Nv"+no.nivel+" "+no.custo+"pt",b.getX()+b.getWidth()/2,b.getY()+22,tem?0x86CBA8:0x9EACC0);
            if(mx>=b.getX()&&mx<b.getX()+b.getWidth()&&my>=b.getY()&&my<b.getY()+b.getHeight())hover=no;
        }
        String texto=hover==null?"Vida +"+(int)m.vidaAdicional()+" · Físico +"+(int)Math.round((m.multiplicadorFisico()-1)*100)+"% · Magia +"+(int)Math.round((m.multiplicadorMagico()-1)*100)+"%":DETALHES[hover.indice];
        g.drawWordWrap(font,Component.literal(texto),x+12,y+h-48,w-24,0xB9C8DE);
        if(!RpgCliente.recebido())g.drawString(font,"Sincronizando...",x+12,y+h-22,0xD9BD80);
        else g.drawString(font,"[P] Escolher kit",x+12,y+h-22,0x7C91B0);
        super.render(g,mx,my,parcial);
    }
    @Override public boolean isPauseScreen(){return false;}
}
