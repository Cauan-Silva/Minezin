package com.cauansilva.arcanecontrol.core.client;

import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rede.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PoderesScreen extends Screen {
    private PoderAnime foco=PoderesCliente.ativo();
    private Button acao,voltar;
    private int x,y,w,h;
    public PoderesScreen(){super(Component.literal("Poderes de Anime"));}
    @Override protected void init(){
        w=Math.min(480,width-20);h=Math.min(260,height-16);x=(width-w)/2;y=(height-h)/2;
        int tab=(w-24)/3;
        for(PoderAnime p:PoderAnime.values())addRenderableWidget(Button.builder(Component.literal(p.nome),b->foco=p)
                .bounds(x+12+(p.id-1)*tab,y+42,tab-3,18).build());
        acao=addRenderableWidget(Button.builder(Component.literal("Selecionar"),b->{
            b.active=false;RegistoRede.ENVIAR.sendToServer(new PacoteAcaoPoder(PoderesCliente.estado.tem(foco)?1:2,foco.id));
        }).bounds(x+12,y+h-30,w-112,20).build());
        voltar=addRenderableWidget(Button.builder(Component.literal("Fechar"),b->{
            if(foco==PoderAnime.GOJO&&PoderesCliente.estado.tem(foco))minecraft.setScreen(new ArvoreGojoScreen());else onClose();
        }).bounds(x+w-92,y+h-30,80,20).build());
        RegistoRede.ENVIAR.sendToServer(new PacoteAcaoPoder(0,0));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float parcial){
        renderBackground(g);var p=PoderesCliente.estado;
        g.fill(x,y,x+w,y+h,0xF00B101C);g.renderOutline(x,y,w,h,0xFF000000|foco.cor);
        g.drawCenteredString(font,title,width/2,y+10,0xFFFFFF);
        g.drawCenteredString(font,"Conhecimento: "+p.conhecimento()+" • +10 por inimigo",width/2,y+25,0xC6CEDC);
        String estado=p.tem(foco)?p.ativo()==foco?"ATIVO":"DESBLOQUEADO":"BLOQUEADO";
        g.drawString(font,foco.nome+" • "+estado,x+12,y+70,foco.cor);
        g.renderItem(new net.minecraft.world.item.ItemStack(foco==PoderAnime.GOJO?com.cauansilva.arcanecontrol.core.equipamentos.RegistroTrajes.GOJO_CABECA.get()
                :foco==PoderAnime.CID?com.cauansilva.arcanecontrol.core.cid.EspadaCid.ESPADA.get():com.cauansilva.arcanecontrol.core.equipamentos.RegistroTrajes.FRI_CABECA.get()),x+w-32,y+68);
        g.drawString(font,"Nível "+p.nivel(foco)+" • "+p.xp(foco)+" XP",x+12,y+84,0xE5E9F2);
        int barra=w-24;g.fill(x+12,y+97,x+w-12,y+100,0xFF253247);
        int valor=p.nivel(foco)>=20?100:p.xp(foco)%100;
        g.fill(x+12,y+97,x+12+barra*valor/100,y+100,0xFF000000|foco.cor);
        String[] teclas={"Z","X","C","V","R"};int linha=h<240?10:13;
        for(int i=0;i<foco.tecnicas.length;i++){
            boolean aprendida=foco==PoderAnime.GOJO?ProgressoGojoCliente.progresso.tem(com.cauansilva.arcanecontrol.core.progressao.TecnicaGojo.values()[i])
                    :p.tem(foco)&&p.nivel(foco)>=foco.nivelTecnica(i);
            String detalhe=aprendida?"M"+p.maestria(foco,i):"Nv. "+foco.nivelTecnica(i);
            if(foco==PoderAnime.GOJO&&aprendida)detalhe="M"+ProgressoGojoCliente.progresso.maestria(com.cauansilva.arcanecontrol.core.progressao.TecnicaGojo.values()[i]);
            g.drawString(font,"["+teclas[i]+"] "+foco.tecnicas[i]+" • "+detalhe,x+12,y+109+i*linha,aprendida?0xDCE8FA:0x7E899B);
        }
        String custo=foco==PoderAnime.GOJO?"Árvore de técnicas em K. Uso: +5 XP; inimigo: +25 XP."
                :p.tem(foco)?"Uso: +5 XP; inimigo: +25 XP. "+(foco==PoderAnime.CID?"Requer a Lâmina Shadow.":"Magia pode ser usada sem cajado.")
                :foco.custo+" conhecimento + "+(foco==PoderAnime.CID?"8 ametistas + 4 obsidianas":"8 lápis-lazúli + 4 ametistas");
        g.drawWordWrap(font,Component.literal(custo),x+12,y+h-64,w-24,0xABB8CC);
        acao.setMessage(Component.literal(p.tem(foco)?p.ativo()==foco?"Kit selecionado":"Selecionar kit":PoderesCliente.criativo?"Desbloquear (Criativo)":"Pesquisar kit"));
        acao.active=PoderesCliente.recebido&&(p.tem(foco)?p.ativo()!=foco:PoderesCliente.criativo||p.podeDesbloquear(foco));
        voltar.setMessage(Component.literal(foco==PoderAnime.GOJO?"Árvore K":"Fechar"));
        super.render(g,mx,my,parcial);
    }
    @Override public boolean isPauseScreen(){return false;}
}
