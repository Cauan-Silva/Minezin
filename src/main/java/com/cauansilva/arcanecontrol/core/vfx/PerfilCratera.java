package com.cauansilva.arcanecontrol.core.vfx;
import java.util.*;
/** Plano limitado, ordenado do centro para fora, compartilhado pelos efeitos de terreno. */
public final class PerfilCratera {
    public record Bloco(int x,int y,int z){}
    public static List<Bloco> planejar(int raio,int profundidade){
        raio=Math.max(1,Math.min(28,raio));profundidade=Math.max(1,Math.min(12,profundidade));
        List<Bloco> blocos=new ArrayList<>();
        for(int x=-raio;x<=raio;x++)for(int z=-raio;z<=raio;z++){
            double r2=(x*x+z*z)/(double)(raio*raio);if(r2>1)continue;
            int fundo=-(int)Math.ceil(profundidade*(1-r2));
            for(int y=fundo;y<=2;y++)blocos.add(new Bloco(x,y,z));
        }
        blocos.sort(Comparator.comparingInt(b->b.x*b.x+b.z*b.z));return List.copyOf(blocos);
    }
}
