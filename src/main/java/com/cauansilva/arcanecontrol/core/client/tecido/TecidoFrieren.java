package com.cauansilva.arcanecontrol.core.client.tecido;

import java.util.*;

/** Frieren's short capelet and A-line skirt: pinned particle panels per face, same solver as Gojo's hem.
 * Layout must match tools/armor_reference_shapes.py (CAPA/SAIA columns and rows). */
public final class TecidoFrieren {
    public record Entrada(double frente,double lado,double vertical,double giro,boolean noChao){}
    public record Pose(double x,double y,double z){}
    private record Indice(int grupo,int face,int coluna,int segmento){}
    private static final String[] FACES={"back","front","left","right"};
    private static final int[][] COLUNAS={{4,4,2,2},{3,3,2,2}};
    private static final int[] LINHAS={2,3};
    private static final String[] PREFIXO={"capa","saia"};
    private static final Map<String,Indice> INDICES=new LinkedHashMap<>();
    public static final List<String> OSSOS;
    static{
        for(int g=0;g<2;g++)for(int f=0;f<4;f++)for(int c=0;c<COLUNAS[g][f];c++)for(int s=0;s<LINHAS[g];s++)
            INDICES.put("frieren_"+PREFIXO[g]+"_"+FACES[f]+"_"+c+"_"+s,new Indice(g,f,c,s));
        OSSOS=List.copyOf(INDICES.keySet());
    }
    private static final Pose ZERO=new Pose(0,0,0);
    // Panel widths/heights in metres (model units / 16), matching the generated geometry.
    private final MalhaTecido[][] malhas={
        {new MalhaTecido(4,3,2.6/16,1.6/16),new MalhaTecido(4,3,3.2/16,1.6/16),new MalhaTecido(2,3,2.75/16,1.6/16),new MalhaTecido(2,3,2.75/16,1.6/16)},
        {new MalhaTecido(3,4,3.07/16,1.5/16),new MalhaTecido(3,4,3.07/16,1.5/16),new MalhaTecido(2,4,2.5/16,1.5/16),new MalhaTecido(2,4,2.5/16,1.5/16)}};
    public void reset(){for(var g:malhas)for(var m:g)m.reset();}
    public void tick(Entrada e){
        if(!Double.isFinite(e.frente+e.lado+e.vertical+e.giro)){reset();return;}
        // The skirt answers more to stride and falls than the shoulder-pinned capelet.
        double[] ganho={.2,.3};
        for(int g=0;g<2;g++){
            double f=e.frente*ganho[g],l=e.lado*ganho[g],v=e.vertical*.55,gi=e.giro*.55;
            malhas[g][0].tick(f,l,v,gi);malhas[g][1].tick(-f,-l,v,gi);
            malhas[g][2].tick(l,-f,v,gi);malhas[g][3].tick(-l,f,v,gi);
        }
    }
    public Pose pose(String osso,double parcial){
        var i=INDICES.get(osso);if(i==null)return ZERO;
        var r=malhas[i.grupo][i.face].rotacao(i.coluna,i.segmento,parcial);
        double minX=i.grupo==0?-.30:-.42;
        double x=Math.max(minX,Math.min(.05,r.x())),y=clamp(r.y(),.08),z=clamp(r.z(),.12);
        double cx=Math.cos(x),sx=Math.sin(x),cy=Math.cos(y),sy=Math.sin(y),cz=Math.cos(z),sz=Math.sin(z);
        double[][] m={{cz*cy,cz*sy*sx-sz*cx,cz*sy*cx+sz*sx},{sz*cy,sz*sy*sx+cz*cx,sz*sy*cx-cz*sx},{-sy,cy*sx,cy*cx}};
        double a=switch(i.face){case 1->Math.PI;case 2->-Math.PI/2;case 3->Math.PI/2;default->0;},c=Math.cos(a),s=Math.sin(a);
        double[][] q={{c,0,s},{0,1,0},{-s,0,c}},world=new double[3][3];
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)for(int j=0;j<3;j++)for(int k=0;k<3;k++)world[row][col]+=q[row][j]*m[j][k]*q[col][k];
        return new Pose(Math.atan2(world[2][1],world[2][2]),-Math.asin(Math.max(-1,Math.min(1,-world[2][0]))),-Math.atan2(world[1][0],world[0][0]));
    }
    public double erroAlongamento(){double e=0;for(var g:malhas)for(var m:g)e=Math.max(e,m.erroAlongamento());return e;}
    private static double clamp(double n,double max){return Math.max(-max,Math.min(max,n));}
}
