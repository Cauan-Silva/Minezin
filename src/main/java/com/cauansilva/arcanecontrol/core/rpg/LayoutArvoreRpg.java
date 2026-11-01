package com.cauansilva.arcanecontrol.core.rpg;
/** Coordenadas em pixels de GUI, inclusive no minimo de 320x240. */
public record LayoutArvoreRpg(int x,int y,int largura,int altura){
    public static LayoutArvoreRpg paraTela(int w,int h){int largura=Math.min(740,w-24),altura=Math.min(400,h-20);return new LayoutArvoreRpg((w-largura)/2,(h-altura)/2,largura,altura);}
    public int coluna(){return (largura-24)/4;}
    public int nodeX(int indice){int col=indice==7?3:indice<=2?indice:indice-3;return x+12+col*coluna();}
    public int nodeY(int indice){return y+96+(indice>=3&&indice<=6?Math.max(40,(altura-150)/2):0);}
    public int nodeLargura(){return coluna()-4;}
}
