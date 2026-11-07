package com.cauansilva.arcanecontrol.core.vazio;

import org.junit.jupiter.api.Test;
import java.util.*;
import static com.cauansilva.arcanecontrol.core.vazio.PlantaArena.*;
import static org.junit.jupiter.api.Assertions.*;

class VazioTest {
    @Test void instanciasNaoSeSobrepoem(){
        Set<String> vistos=new HashSet<>();
        for(int n=0;n<300;n++){int[] c=centro(n);assertTrue(vistos.add(c[0]+","+c[1]));
            for(int m=0;m<n;m++){int[] o=centro(m);assertTrue(Math.max(Math.abs(c[0]-o[0]),Math.abs(c[1]-o[1]))>=ESPACO);}}
        assertEquals(ORIGEM,centro(0)[0]);assertEquals(ORIGEM+ESPACO,centro(POR_LINHA)[1]);
        assertTrue(ESPACO>2*(RAIO+2)+64,"arenas vizinhas fora da distancia de visao uma da outra");
    }
    @Test void arenaFechadaComChaoEBarreira(){
        // base embaixo de toda a area andavel; barreira em volta e no teto
        for(int dx=-RAIO;dx<=RAIO;dx++)for(int dz=-RAIO;dz<=RAIO;dz++){
            double d=Math.hypot(dx,dz);
            if(d<=RAIO){assertEquals(Papel.CHAO,papel(dx,-1,dz));assertNotEquals(Papel.VAZIO,papel(dx,0,dz));assertEquals(Papel.BARREIRA,papel(dx,TETO,dz));}
        }
        // sem fresta: nenhum bloco livre de dentro encosta (nem na diagonal) num bloco livre de fora da barreira
        int L=RAIO+4;
        for(int dx=-L;dx<=L;dx++)for(int dz=-L;dz<=L;dz++){if(Math.hypot(dx,dz)>RAIO+.5)continue;
            for(int ox=-1;ox<=1;ox++)for(int oz=-1;oz<=1;oz++){int x=dx+ox,z=dz+oz;
                if(Math.hypot(x,z)>RAIO+.5)for(int dy=1;dy<TETO;dy++)assertEquals(Papel.BARREIRA,papel(x,dy,z),x+","+dy+","+z);}}
        assertEquals(Papel.VAZIO,papel(RAIO+5,0,0));
    }
    @Test void pilaresEEntradasLivres(){
        for(int k=0;k<PILARES;k++){int[] p=pilar(k);assertEquals(Papel.PILAR,papel(p[0],1,p[1]));assertEquals(Papel.TOPO_PILAR,papel(p[0],ALTURA_PILAR,p[1]));}
        for(int n=1;n<=4;n++)for(int i=0;i<n;i++){double[] e=entradaJogador(i,n);for(int dy=1;dy<=2;dy++)assertEquals(Papel.VAZIO,papel((int)Math.round(e[0]),dy,(int)Math.round(e[1])));}
        double[] c=entradaChefe();for(int dy=1;dy<=6;dy++)assertEquals(Papel.VAZIO,papel((int)c[0],dy,(int)c[1]));
    }
    @Test void fluxoVitoria(){
        var e=new EstadoInstancia<>(List.of("a","b"),0);
        assertTrue(e.travado(10));assertEquals(EstadoInstancia.Fase.COMBATE,e.tick(EstadoInstancia.ENTRADA));
        e.cair("a",100);assertEquals(EstadoInstancia.Fase.COMBATE,e.fase());assertEquals(Set.of("a"),e.caidos());
        e.vencer(200);assertEquals(EstadoInstancia.Fase.VITORIA,e.tick(200));
        assertEquals(EstadoInstancia.Fase.ENCERRADA,e.tick(200+EstadoInstancia.SAIDA_VITORIA));assertEquals(Set.of("a","b"),e.todos());
    }
    @Test void wipeQuandoTodosCaem(){
        var e=new EstadoInstancia<>(List.of("a","b"),0);e.tick(EstadoInstancia.ENTRADA);
        e.cair("a",70);e.remover("b",80);assertEquals(EstadoInstancia.Fase.WIPE,e.fase());
        e.vencer(90);assertEquals(EstadoInstancia.Fase.WIPE,e.fase(),"vitoria depois do wipe nao conta");
        assertEquals(EstadoInstancia.Fase.ENCERRADA,e.tick(80+EstadoInstancia.SAIDA_WIPE));
    }
    @Test void tempoLimiteViraWipe(){
        var e=new EstadoInstancia<>(List.of("a"),0);assertEquals(EstadoInstancia.Fase.WIPE,e.tick(EstadoInstancia.LIMITE));
    }
    @Test void cutsceneSemSaltosVisiveis(){
        var J=new CinematicaEntrada.V(-14,65.6,0);var C=new CinematicaEntrada.V(14,65,0);
        CinematicaEntrada.Quadro antes=null;
        for(double t=0;t<=CinematicaEntrada.FIM+2;t+=.5){var q=CinematicaEntrada.quadro(t,J,C,3.2);
            if(antes!=null&&t!=CinematicaEntrada.BLACKOUT)assertTrue(q.pos().menos(antes.pos()).tamanho()<3.5,"salto em t="+t);
            antes=q;}
        assertEquals(J,CinematicaEntrada.quadro(CinematicaEntrada.FIM,J,C,3.2).pos());
        assertEquals(J,CinematicaEntrada.quadro(CinematicaEntrada.BLACKOUT-1,J,C,3.2).pos());
        // no close da fala a camera fica do lado dos jogadores, olhando a cabeca do chefe
        var f=CinematicaEntrada.quadro(CinematicaEntrada.FALA+5,J,C,3.2);assertTrue(f.pos().x()<C.x());assertTrue(f.olha().y()>C.y()+2);
        assertEquals(CinematicaEntrada.FIM,EstadoInstancia.ENTRADA);
        for(int k:new int[]{1,2,3,4,6,12,16})assertFalse(CinematicaEntrada.fala(k).isBlank());
    }
}
