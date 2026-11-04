package com.cauansilva.arcanecontrol.core.client.tecido;

import java.util.*;

/**
 * Smooth cloth surfaces instead of blocks (user: the coat looked like loose pieces, the collar "terrivelmente feio").
 * Pure geometry, no Minecraft classes, so tests and tools/superficie_tecido.py share it.
 *
 * casaco(): the cloth bones keep the physics (TecidoIchigo); each segment's main panel gives four mid-plane corners
 * (the renderer reads them from the cube already posed by GeckoLib). Pieces are stitched in a fixed order around the
 * body (front right, its corner, side, back corner, back columns, ..., front left) and each shared edge is the
 * average of both neighbours, so front, sides and back are literally one sheet; the hem is cut into uneven teeth.
 *
 * anel(): the same sheet closed all the way round with a straight hem (Frieren's skirt and capelet).
 *
 * gola(): Shadow's high collar as one rounded shell around the neck (model units, 1 = 1/16 block): it rises
 * tallest at the back, flares outward as it climbs, rounds off at both tips by the chin, black outside, purple
 * inside, gold rim along the top edge and down the tips.
 */
public final class SuperficieTecido {
    public static final int PRETO=0,ROXO=1,OURO=2;
    /** One triangle: three points and a material (PRETO, ROXO, OURO). */
    public record Tri(double[] a,double[] b,double[] c,int material){}
    /** Stitch order around the body, from the right front flap to the left one (TecidoIchigo bone names). */
    public static final List<String> ORDEM;
    static{
        var o=new ArrayList<String>(List.of("frente_dir_0","quinaf_dir_0","lado_dir_0","quina_dir_0"));
        for(int c=0;c<TecidoIchigo.COLUNAS_COSTAS;c++)o.add("costas_"+c);
        o.addAll(List.of("quina_esq_0","lado_esq_0","quinaf_esq_0","frente_esq_0"));ORDEM=List.copyOf(o);
    }
    public static String osso(String peca,int segmento){return "bk_capa_"+peca+"_"+segmento;}

    /** Labels a panel's corners once, from its rest (unposed) vertices, so they can never swap while the cloth swings
     *  (the old nearest-point guess flipped and twisted pieces in motion). Returns, for top-start, top-end,
     *  bottom-start, bottom-end, the vertex indices averaged into that corner (front and back face of the thin panel).
     *  dir: +1 if "start" is the lower angle around the body (atan2(x,z)), -1 otherwise. */
    public static int[][] rotular(double[][] rep,int dir){
        double[] lo={1e9,1e9,1e9},hi={-1e9,-1e9,-1e9};for(var p:rep)for(int k=0;k<3;k++){lo[k]=Math.min(lo[k],p[k]);hi[k]=Math.max(hi[k],p[k]);}
        double cx=(lo[0]+hi[0])/2,cy=(lo[1]+hi[1])/2,cz=(lo[2]+hi[2])/2,base=Math.atan2(cx,cz);
        var grupos=new ArrayList<List<Integer>>();for(int q=0;q<4;q++)grupos.add(new ArrayList<>());
        for(int i=0;i<rep.length;i++){var p=rep[i];boolean topo=p[1]>cy;
            // Angle of the corner relative to the panel centre, unwrapped around the panel's own angle.
            double ang=Math.atan2(p[0],p[2])-base;while(ang>Math.PI)ang-=2*Math.PI;while(ang<-Math.PI)ang+=2*Math.PI;
            boolean inicio=ang*dir<0;
            grupos.get((topo?0:2)+(inicio?0:1)).add(i);}
        var out=new int[4][];for(int q=0;q<4;q++)out[q]=grupos.get(q).stream().mapToInt(Integer::intValue).toArray();return out;
    }
    /** Traversal direction of ORDEM around the body, from the rest centres: +1 if the angle grows along it. */
    public static int direcao(Map<String,double[][]> repouso){var l=new ArrayList<String>();for(var p:ORDEM)l.add("bk_capa_"+p);return direcao(repouso,l);}
    static int direcao(Map<String,double[][]> repouso,List<String> ordem){
        double soma=0;Double ant=null;
        for(var p:ordem){var r=repouso.get(p+"_0");if(r==null)continue;double[] c=media(Arrays.asList(r));double a=Math.atan2(c[0],c[2]);
            if(ant!=null){double d=a-ant;while(d>Math.PI)d-=2*Math.PI;while(d<-Math.PI)d+=2*Math.PI;soma+=d;}ant=a;}
        return soma>=0?1:-1;
    }
    /** Anything the cloth is pushed out of: the legs (Caixa) or the world's blocks (ColisaoCenario). */
    public interface Obstaculo{double[] fora(double[] p);}
    /** A posed box (a leg): origin corner and its three edge vectors. */
    public record Caixa(double[] o,double[] ax,double[] ay,double[] az) implements Obstaculo{
        @Override public double[] fora(double[] p){return SuperficieTecido.fora(p,this);}
    }
    /** Box from a cube's rest and posed vertices (same order): the rest min corner and its neighbours along x, y, z. */
    public static Caixa caixa(double[][] rep,double[][] pos){
        double[] lo={1e9,1e9,1e9},hi={-1e9,-1e9,-1e9};for(var p:rep)for(int k=0;k<3;k++){lo[k]=Math.min(lo[k],p[k]);hi[k]=Math.max(hi[k],p[k]);}
        int[] id=new int[4];double[][] alvo={{lo[0],lo[1],lo[2]},{hi[0],lo[1],lo[2]},{lo[0],hi[1],lo[2]},{lo[0],lo[1],hi[2]}};
        for(int q=0;q<4;q++){double bd=1e18;for(int i=0;i<rep.length;i++){double d=dist(rep[i],alvo[q]);if(d<bd){bd=d;id[q]=i;}}}
        var o=pos[id[0]];return new Caixa(o,desce(o,pos[id[1]]),desce(o,pos[id[2]]),desce(o,pos[id[3]]));
    }
    /** Pushes p out of the box sideways (never up or down) through the nearest side face, with a small gap. */
    static double[] fora(double[] p,Caixa c){
        double[] d=desce(c.o(),p);double[][] e={c.ax(),c.ay(),c.az()};double[] t=new double[3];
        for(int k=0;k<3;k++){double l2=e[k][0]*e[k][0]+e[k][1]*e[k][1]+e[k][2]*e[k][2];if(l2<1e-12)return p;t[k]=(d[0]*e[k][0]+d[1]*e[k][1]+d[2]*e[k][2])/l2;}
        double m=.06;if(t[1]<0||t[1]>1)return p;
        // Never out through the inner face (between the legs, user: the cloth went into the leg): only outer, front, back.
        double cx=c.o()[0]+(e[0][0]+e[1][0]+e[2][0])/2;
        int eixo=-1;double melhor=1e18,alvo=0;
        for(int k:new int[]{0,2}){if(t[k]<-m||t[k]>1+m)return p;
            boolean internoA=k==0&&e[0][0]*cx>0,internoB=k==0&&e[0][0]*cx<0;
            double a=t[k]+m,b=1+m-t[k];if(a<melhor&&!internoA){melhor=a;eixo=k;alvo=-m;}if(b<melhor&&!internoB){melhor=b;eixo=k;alvo=1+m;}}
        return soma(p,escala(e[eixo],alvo-t[eixo]));
    }
    public static double[] colidir(double[] p,List<? extends Obstaculo> obst){for(var c:obst)p=c.fora(p);return p;}
    public static List<Tri> casaco(Map<String,double[][]> posados,Map<String,double[][]> repouso){return casaco(posados,repouso,List.of());}
    /** posados: bone -> posed vertices (same order as repouso); repouso: bone -> rest vertices. Missing pieces skipped.
     *  pernas: the posed legs; the sheet is pushed out of them (user: the front cloth collides with the legs). */
    public static List<Tri> casaco(Map<String,double[][]> posados,Map<String,double[][]> repouso,List<? extends Obstaculo> pernas){
        var l=new ArrayList<String>();for(var p:ORDEM)l.add("bk_capa_"+p);
        return faixa(l,TecidoIchigo.SEGMENTOS,posados,repouso,pernas,false,true);
    }
    /** A closed skirt/capelet ring (Frieren): the pieces whose bones start with prefixo (named prefixo+face_col_seg),
     *  ordered by their rest angle around the body, stitched all the way round, straight hem. */
    public static List<Tri> anel(String prefixo,int segmentos,Map<String,double[][]> posados,Map<String,double[][]> repouso,List<? extends Obstaculo> pernas){
        var pecas=new TreeMap<Double,String>();
        for(var e:repouso.entrySet())if(e.getKey().startsWith(prefixo)&&e.getKey().endsWith("_0")){
            var c=media(Arrays.asList(e.getValue()));pecas.put(Math.atan2(c[0],c[2]),e.getKey().substring(0,e.getKey().length()-2));}
        return faixa(new ArrayList<>(pecas.values()),segmentos,posados,repouso,pernas,true,false);
    }
    /** An open cape/robe (Asta): the pieces whose bones start with prefixo, ordered by their rest angle around the
     *  body and cut open at the widest gap between neighbours (the front opening), straight hem. */
    public static List<Tri> aberto(String prefixo,int segmentos,Map<String,double[][]> posados,Map<String,double[][]> repouso,List<? extends Obstaculo> pernas){
        var pecas=new TreeMap<Double,String>();
        for(var e:repouso.entrySet())if(e.getKey().startsWith(prefixo)&&e.getKey().endsWith("_0")){
            var c=media(Arrays.asList(e.getValue()));pecas.put(Math.atan2(c[0],c[2]),e.getKey().substring(0,e.getKey().length()-2));}
        var ang=new ArrayList<>(pecas.keySet());var nomes=new ArrayList<>(pecas.values());int n=nomes.size();if(n<2)return new ArrayList<>();
        int corte=0;double maior=-1;
        for(int i=0;i<n;i++){double d=(i+1<n?ang.get(i+1):ang.get(0)+2*Math.PI)-ang.get(i);if(d>maior){maior=d;corte=(i+1)%n;}}
        var ordem=new ArrayList<String>();for(int i=0;i<n;i++)ordem.add(nomes.get((corte+i)%n));
        return faixa(ordem,segmentos,posados,repouso,pernas,false,false);
    }
    /** One sheet through the given pieces (bone = piece+"_"+segment), in order around the body.
     *  fechado: the last piece is stitched back to the first (a ring); rasgado: torn hem teeth, else a straight hem. */
    static List<Tri> faixa(List<String> ordem,int S,Map<String,double[][]> posados,Map<String,double[][]> repouso,List<? extends Obstaculo> pernas,boolean fechado,boolean rasgado){
        var pecas=new ArrayList<String>();
        for(var p:ordem){boolean ok=true;for(int s=0;s<S;s++)ok&=posados.containsKey(p+"_"+s)&&repouso.containsKey(p+"_"+s);if(ok)pecas.add(p);}
        var out=new ArrayList<Tri>();int n=pecas.size();if(n<2)return out;int dir=direcao(repouso,pecas);
        var meio=new double[n][S][][];var meioR=new double[n][S][][];
        for(int i=0;i<n;i++)for(int s=0;s<S;s++){String o=pecas.get(i)+"_"+s;var idx=rotular(repouso.get(o),dir);var v=posados.get(o);
            var vr=repouso.get(o);meio[i][s]=new double[4][];meioR[i][s]=new double[4][];
            for(int q=0;q<4;q++){var acc=new ArrayList<double[]>();var accR=new ArrayList<double[]>();for(int k:idx[q])if(k<v.length){acc.add(v[k]);accR.add(vr[k]);}
                meio[i][s][q]=acc.isEmpty()?v[0]:media(acc);meioR[i][s][q]=accR.isEmpty()?vr[0]:media(accR);}}
        var g=grade(meio,S,n,fechado);var gr=grade(meioR,S,n,fechado);
        suavizar(g,gr,fechado);
        g=subdividir(g,fechado);int R=g.length-1,N=g[0].length-1;
        for(var linha:g)for(int c=0;c<=N;c++)linha[c]=colidir(linha[c],pernas);
        if(fechado)for(var linha:g)linha[N]=linha[0];
        for(int r=0;r<R;r++)for(int c=0;c<N;c++)quad(out,g[r][c],g[r][c+1],g[r+1][c+1],g[r+1][c],PRETO);
        if(!rasgado)return out;
        S=R;n=N;
        for(int c=0;c<n;c++){
            double[] a=g[S][c],b=g[S][c+1],da=desce(g[S-1][c],a),db=desce(g[S-1][c+1],b);
            double l1=dente(c,0)*SUB_LINHAS,l2=dente(c,1)*SUB_LINHAS,l3=dente(c,2)*.35*SUB_LINHAS;
            double[] m=lerp(a,b,.5),dm=lerp(da,db,.5);
            double[] t1=colidir(soma(lerp(a,m,.45),escala(lerp(da,dm,.45),l1)),pernas),t2=colidir(soma(lerp(m,b,.55),escala(lerp(dm,db,.55),l2)),pernas),nt=colidir(soma(m,escala(dm,l3)),pernas);
            tri(out,a,m,nt,PRETO);tri(out,a,nt,t1,PRETO);tri(out,m,b,nt,PRETO);tri(out,nt,b,t2,PRETO);
        }
        return out;
    }
    /** Grid of seam points: each shared edge is the average of both neighbouring panels. */
    static double[][][] grade(double[][][][] meio,int S,int n){return grade(meio,S,n,false);}
    /** fechado: column 0 is also the end edge of the last piece, and column n repeats column 0. */
    static double[][][] grade(double[][][][] meio,int S,int n,boolean fechado){
        var g=new double[S+1][n+1][];
        for(int r=0;r<=S;r++)for(int c=0;c<=n;c++){
            if(fechado&&c==n){g[r][c]=g[r][0];continue;}
            var acc=new ArrayList<double[]>();
            for(int i:new int[]{c-1,c}){int lado=i==c?0:1;if(fechado)i=(i+n)%n;
                if(i>=0&&i<n){if(r<S)acc.add(meio[i][r][lado]);if(r>0)acc.add(meio[i][r-1][2+lado]);}}
            g[r][c]=media(acc);
        }
        return g;
    }
    public static final int SUB_COLUNAS=3,SUB_LINHAS=2;
    /** Finer sheet between the bones (user: more bones so the side folds around the leg): every panel is split into
     *  SUB_COLUNAS x SUB_LINHAS cells, Catmull-Rom across the body (round, passes through every seam) and linear down,
     *  so the leg collision can wrap the cloth around a leg instead of cutting a whole panel at once. */
    static double[][][] subdividir(double[][][] g){return subdividir(g,false);}
    static double[][][] subdividir(double[][][] g,boolean fechado){
        int S=g.length-1,n=g[0].length-1,R=S*SUB_LINHAS,N=n*SUB_COLUNAS;var f=new double[R+1][N+1][];
        for(int r=0;r<=R;r++){int r0=Math.min(S-1,r/SUB_LINHAS);double v=r/(double)SUB_LINHAS-r0;
            for(int c=0;c<=N;c++){int c0=Math.min(n-1,c/SUB_COLUNAS);double u=c/(double)SUB_COLUNAS-c0;
                f[r][c]=lerp(curva(g[r0],c0,u,fechado),curva(g[r0+1],c0,u,fechado),v);}}
        return f;
    }
    static double[] curva(double[][] l,int c,double u){return curva(l,c,u,false);}
    /** Catmull-Rom point between l[c] and l[c+1]; open ends repeat their edge point, a closed ring wraps around
     *  (l[n] is l[0]). */
    static double[] curva(double[][] l,int c,double u,boolean fechado){
        int n=l.length-1;double[] p0,p3;
        if(fechado){p0=l[(c-1+n)%n];p3=l[(c+2)%n];}else{p0=l[Math.max(0,c-1)];p3=l[Math.min(n,c+2)];}
        double[] p1=l[c],p2=l[c+1];double u2=u*u,u3=u2*u;
        double[] o=new double[3];for(int k=0;k<3;k++)o[k]=.5*(2*p1[k]+(-p0[k]+p2[k])*u+(2*p0[k]-5*p1[k]+4*p2[k]-p3[k])*u2+(-p0[k]+3*p1[k]-3*p2[k]+p3[k])*u3);return o;
    }
    public static final int PASSES_SUAVE=3;public static final double SUAVE=.45;
    /** Smooths each row around the body (user: front joined to the side, without the sharp fold where the front
     *  swings open). Only the bend that is not in the rest shape is smoothed (rest: gr), so the ring never shrinks
     *  into the legs: each point moves by k times (neighbour mean - point) minus the same at rest; more toward the hem,
     *  the waist row stays pinned, the open front edges are left as they are. */
    static void suavizar(double[][][] g,double[][][] gr){suavizar(g,gr,false);}
    static void suavizar(double[][][] g,double[][][] gr,boolean fechado){
        int S=g.length-1,n=g[0].length-1;if(n<2)return;
        for(int r=1;r<=S;r++){double k=SUAVE*r/S;var lr=gr[r];
            for(int it=0;it<PASSES_SUAVE;it++){var l=g[r];var novo=l.clone();
                for(int c=fechado?0:1;c<n;c++){int a=fechado?(c-1+n)%n:c-1,b=c+1;
                    double[] lap=desce(l[c],lerp(l[a],l[b],.5)),lapR=desce(lr[c],lerp(lr[a],lr[b],.5));
                    novo[c]=soma(l[c],escala(desce(lapR,lap),k));}
                if(fechado)novo[n]=novo[0];
                g[r]=novo;}}
    }
    /** Tooth length (in the posed units of the hanging direction, i.e. segment lengths) per column, deterministic. */
    static double dente(int c,int k){return .35+.75*(((c*37+k*53+11)%10)/9.0);}

    public static final int GOLA_COLUNAS=32,GOLA_LINHAS=16;public static final double GOLA_ABERTURA=28,GOLA_BASE=21.6,GOLA_CABECA=24.2,GOLA_CABECA_MEIA=4.75,GOLA_OMBRO_X=4.1,GOLA_OMBRO_Z=2.15,GOLA_RAIO=6.8,GOLA_ALTURA=10.4,GOLA_ESPESSURA=.32;
    /** Collar height at angle u (degrees, 0 = back): tallest at the back, lower to the front, rounded tips. */
    public static double alturaGola(double u){
        double lim=180-GOLA_ABERTURA,t=Math.max(0,(Math.abs(u)-(lim-34))/34);
        return GOLA_ALTURA*(.6+.4*Math.pow(Math.cos(Math.toRadians(u)/2),2))*(.22+.78*Math.sqrt(Math.max(0,1-t*t)));
    }
    /** Distance from the axis to a rounded square (superellipse) of half-size m along angle a. */
    static double quadrado(double a,double m,double p){return m/Math.pow(Math.pow(Math.abs(Math.sin(a)),p)+Math.pow(Math.abs(Math.cos(a)),p),1/p);}
    /** Point on the collar: angle u (degrees), height fraction v, depth (0 outer, 1 inner); model units.
     *  It frames the whole head (user): from the torso's top outline it widens to a rounded square just larger than
     *  the head before the head starts (GOLA_CABECA), then flares from that square to the round open rim. */
    public static double[] pontoGola(double u,double v,double prof){
        double a=Math.toRadians(u),sx=Math.sin(a),cz=Math.cos(a),y=GOLA_BASE+alturaGola(u)*v;
        double tronco=Math.min(GOLA_OMBRO_X/Math.max(1e-6,Math.abs(sx)),GOLA_OMBRO_Z/Math.max(1e-6,Math.abs(cz)));
        double cabeca=quadrado(a,GOLA_CABECA_MEIA,6),r;
        if(y<GOLA_CABECA){double t=(y-GOLA_BASE)/(GOLA_CABECA-GOLA_BASE);t=t*t*(3-2*t);r=tronco+(cabeca-tronco)*t;}
        else{double t=Math.min(1,(y-GOLA_CABECA)/(GOLA_BASE+GOLA_ALTURA-GOLA_CABECA));r=cabeca+(GOLA_RAIO-cabeca)*Math.pow(t,1.5);}
        r-=prof*GOLA_ESPESSURA;
        return new double[]{r*sx,y,r*cz};
    }
    public static List<Tri> gola(){
        var out=new ArrayList<Tri>();double lim=180-GOLA_ABERTURA;
        for(int i=0;i<GOLA_COLUNAS;i++){double u0=-lim+2*lim*i/GOLA_COLUNAS,u1=-lim+2*lim*(i+1)/GOLA_COLUNAS;
            for(int j=0;j<GOLA_LINHAS;j++){double v0=j/(double)GOLA_LINHAS,v1=(j+1)/(double)GOLA_LINHAS;
                int borda=j==GOLA_LINHAS-1?OURO:PRETO;
                quad(out,pontoGola(u0,v0,0),pontoGola(u1,v0,0),pontoGola(u1,v1,0),pontoGola(u0,v1,0),borda);
                quad(out,pontoGola(u1,v0,1),pontoGola(u0,v0,1),pontoGola(u0,v1,1),pontoGola(u1,v1,1),j==GOLA_LINHAS-1?OURO:ROXO);
            }
            // Top edge between outer and inner shells: gold rim.
            quad(out,pontoGola(u0,1,0),pontoGola(u1,1,0),pontoGola(u1,1,1),pontoGola(u0,1,1),OURO);
        }
        // Both tips closed by a gold strip from bottom to top.
        for(double u:new double[]{-lim,lim})for(int j=0;j<GOLA_LINHAS;j++){double v0=j/(double)GOLA_LINHAS,v1=(j+1)/(double)GOLA_LINHAS;
            quad(out,pontoGola(u,v0,0),pontoGola(u,v0,1),pontoGola(u,v1,1),pontoGola(u,v1,0),OURO);}
        return out;
    }

    // ---------- small vector helpers ----------
    static double[] centro(double[][] p){var a=new ArrayList<double[]>(Arrays.asList(p));return media(a);}
    static double[] media(List<double[]> p){double[] m=new double[3];for(var q:p)for(int i=0;i<3;i++)m[i]+=q[i]/p.size();return m;}
    static double dist(double[] a,double[] b){return Math.sqrt((a[0]-b[0])*(a[0]-b[0])+(a[1]-b[1])*(a[1]-b[1])+(a[2]-b[2])*(a[2]-b[2]));}
    static double[] lerp(double[] a,double[] b,double t){return new double[]{a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t,a[2]+(b[2]-a[2])*t};}
    static double[] soma(double[] a,double[] b){return new double[]{a[0]+b[0],a[1]+b[1],a[2]+b[2]};}
    static double[] escala(double[] a,double k){return new double[]{a[0]*k,a[1]*k,a[2]*k};}
    static double[] desce(double[] de,double[] para){return new double[]{para[0]-de[0],para[1]-de[1],para[2]-de[2]};}
    static void quad(List<Tri> o,double[] a,double[] b,double[] c,double[] d,int m){o.add(new Tri(a,b,c,m));o.add(new Tri(a,c,d,m));}
    static void tri(List<Tri> o,double[] a,double[] b,double[] c,int m){o.add(new Tri(a,b,c,m));}
    public static double[] normal(Tri t){
        double[] u=desce(t.a(),t.b()),v=desce(t.a(),t.c());double[] n={u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]};
        double l=Math.sqrt(n[0]*n[0]+n[1]*n[1]+n[2]*n[2]);return l<1e-12?new double[]{0,1,0}:new double[]{n[0]/l,n[1]/l,n[2]/l};
    }
    private SuperficieTecido(){}
}
