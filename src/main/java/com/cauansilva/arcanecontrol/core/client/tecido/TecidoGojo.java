package com.cauansilva.arcanecontrol.core.client.tecido;

import com.cauansilva.arcanecontrol.core.cid.MolaMantoCid;
import java.util.*;

/** Short structured jacket: pinned particle panels, outward support planes and a firm collar spring. */
public final class TecidoGojo {
    public record Entrada(double frente,double lado,double vertical,double giro,boolean noChao){}
    public record Pose(double x,double y,double z){}
    private record Indice(int face,int coluna,int segmento){}
    private static final Map<String,Indice> INDICES=new LinkedHashMap<>();
    public static final List<String> OSSOS;
    static{
        String[] lados={"back","front","left","right"};
        for(int f=0;f<4;f++)for(int c=0;c<(f<2?3:2);c++)for(int s=0;s<2;s++)INDICES.put("gojo_hem_"+lados[f]+"_"+c+"_"+s,new Indice(f,c,s));
        var nomes=new ArrayList<String>();nomes.add("gojo_collar");nomes.addAll(INDICES.keySet());OSSOS=List.copyOf(nomes);
    }
    /** Segments of the hem (RenderTrajeGojo stitches it into one smooth ring). */
    public static final int LINHAS_BARRA=2;
    private static final Pose ZERO=new Pose(0,0,0);
    private final MalhaTecido[] malhas;
    private final MolaMantoCid gola;
    public TecidoGojo(){this(PerfilTecido.PADRAO);}
    public TecidoGojo(PerfilTecido perfil){
        malhas=new MalhaTecido[]{new MalhaTecido(3,3,2.8/16,1.3/16,perfil),new MalhaTecido(3,3,2.8/16,1.3/16,perfil),new MalhaTecido(2,3,2.1/16,1.3/16,perfil),new MalhaTecido(2,3,2.1/16,1.3/16,perfil)};
        gola=new MolaMantoCid(2*perfil.mola());
    }
    public void reset(){for(var m:malhas)m.reset();gola.reset();}
    public void tick(Entrada e){
        if(!Double.isFinite(e.frente+e.lado+e.vertical+e.giro)){reset();return;}
        double f=e.frente*.22,l=e.lado*.22,v=e.vertical*.55,g=e.giro*.55;
        malhas[0].tick(f,l,v,g);malhas[1].tick(-f,-l,v,g);
        malhas[2].tick(l,-f,v,g);malhas[3].tick(-l,f,v,g);
        gola.tick(new MolaMantoCid.Entrada(e.frente,e.lado,e.vertical,e.giro,e.noChao));
    }
    public Pose pose(String osso,double parcial){
        if(osso.equals("gojo_collar")){var p=gola.amostrar(0,parcial);return new Pose(clamp(p.x()*.26,.035),clamp(p.y()*.15,.035),clamp(p.z()*.22,.035));}
        var i=INDICES.get(osso);if(i==null)return ZERO;
        var r=malhas[i.face].rotacao(i.coluna,i.segmento,parcial);
        double x=Math.max(-.24,Math.min(.03,r.x())),y=clamp(r.y(),.06),z=clamp(r.z(),.08);
        double cx=Math.cos(x),sx=Math.sin(x),cy=Math.cos(y),sy=Math.sin(y),cz=Math.cos(z),sz=Math.sin(z);
        double[][] m={{cz*cy,cz*sy*sx-sz*cx,cz*sy*cx+sz*sx},{sz*cy,sz*sy*sx+cz*cx,sz*sy*cx-cz*sx},{-sy,cy*sx,cy*cx}};
        double a=switch(i.face){case 1->Math.PI;case 2->-Math.PI/2;case 3->Math.PI/2;default->0;},c=Math.cos(a),s=Math.sin(a);
        double[][] q={{c,0,s},{0,1,0},{-s,0,c}},world=new double[3][3];
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)for(int j=0;j<3;j++)for(int k=0;k<3;k++)world[row][col]+=q[row][j]*m[j][k]*q[col][k];
        return new Pose(Math.atan2(world[2][1],world[2][2]),-Math.asin(Math.max(-1,Math.min(1,-world[2][0]))),-Math.atan2(world[1][0],world[0][0]));
    }
    public double erroAlongamento(){double e=0;for(var m:malhas)e=Math.max(e,m.erroAlongamento());return e;}
    private static double clamp(double n,double max){return Math.max(-max,Math.min(max,n));}
}
