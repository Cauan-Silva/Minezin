package com.cauansilva.arcanecontrol.core.vfx;

import java.util.ArrayList;
import java.util.List;

/** Deterministic local-space geometry shared by runtime and offline animated review.
 * Local +Z is the cast direction. All values are visual, never hit boxes.
 */
public final class ComposicaoCid {
    public record V(double x,double y,double z){
        public V add(V b){return new V(x+b.x,y+b.y,z+b.z);} public V mul(double s){return new V(x*s,y*s,z*s);}
        public V sub(V b){return add(b.mul(-1));} public double len(){return Math.sqrt(x*x+y*y+z*z);}
        public V cross(V b){return new V(y*b.z-z*b.y,z*b.x-x*b.z,x*b.y-y*b.x);}
        public V normal(){double n=len();return n<1e-8?new V(1,0,0):mul(1/n);}
    }
    public record Quad(V a,V b,V c,V d,double u0,double u1,int cor,float alpha,double v0,double v1,boolean billboard){
        public Quad(V a,V b,V c,V d,double u0,double u1,int cor,float alpha,double v0,double v1){this(a,b,c,d,u0,u1,cor,alpha,v0,v1,false);}
        public Quad(V a,V b,V c,V d,double u0,double u1,int cor,float alpha){this(a,b,c,d,u0,u1,cor,alpha,0,1,false);}
    }
    public record Lote(int material,boolean aditivo,List<Quad> faces){}
    private static final int ESCURO=0x200934,ROXO=0x8D30F2,LILAS=0xDAB1FF;
    private final List<Lote> lotes=new ArrayList<>();
    private final V cam;
    private ComposicaoCid(V cam){this.cam=cam;}
    private static double clamp(double v,double a,double b){return Math.max(a,Math.min(b,v));}
    private static double envelope(double t,double ataque,double fim){return clamp(t/ataque,0,1)*clamp((fim-t)/(fim*.35),0,1);}
    private void faixa(List<Quad> out,V a,V b,double largura,double u0,double u1,int cor,double alpha){
        if(alpha<=.002||largura<=0||a.sub(b).len()<1e-8)return;
        V side=b.sub(a).cross(cam.sub(a)).normal().mul(largura);
        out.add(new Quad(a.sub(side),b.sub(side),b.add(side),a.add(side),u0,u1,cor,(float)clamp(alpha,0,1),0,1,true));
    }
    private void arco(List<Quad> out,double raio,double largura,double abertura,double giro,double altura,double inclina,int n,int cor,double alpha){
        for(int i=0;i<n;i++){
            double u=i/(double)n,v=(i+1d)/n,a=(u-.5)*abertura+giro,b=(v-.5)*abertura+giro;
            double wa=largura*Math.pow(Math.max(0,Math.sin(u*Math.PI)),.6),wb=largura*Math.pow(Math.max(0,Math.sin(v*Math.PI)),.6);
            V p=new V(Math.sin(a),0,Math.cos(a)),q=new V(Math.sin(b),0,Math.cos(b));
            V h=new V(0,altura+Math.sin(a)*inclina,0),hh=new V(0,altura+Math.sin(b)*inclina,0);
            out.add(new Quad(p.mul(raio-wa).add(h),q.mul(raio-wb).add(hh),q.mul(raio+wb).add(hh),p.mul(raio+wa).add(h),u,v,cor,(float)alpha));
        }
    }
    private void adicionar(int modo,boolean aditivo,List<Quad> faces){if(!faces.isEmpty())lotes.add(new Lote(modo,aditivo,List.copyOf(faces)));}
    public static List<Lote> gerar(int tipo,double t,double distancia,V camera,boolean reduzido){
        if(!Double.isFinite(t+distancia+camera.x+camera.y+camera.z)||t<0)return List.of();
        if(tipo==14&&t>=90)return ExplosaoAtomic.gerar(t,camera,reduzido);
        var c=new ComposicaoCid(camera);int n=reduzido?24:48;double alpha;
        if(tipo==11){
            alpha=envelope(t-2,3,22);if(alpha<=0)return List.of();
            double p=clamp((t-3)/6,0,1),raio=2+3*p,giro=-.25+(1-p)*.7;
            var sombra=new ArrayList<Quad>();var chama=new ArrayList<Quad>();var borda=new ArrayList<Quad>();
            c.arco(sombra,raio,.52,2.7,giro,1.15,.52,n,ESCURO,alpha*.95);
            c.arco(chama,raio-.06,.34,2.7,giro,1.17,.52,n,ROXO,alpha*.85);
            c.arco(borda,raio+.14,.065,2.65,giro,1.2,.52,n,LILAS,alpha*.9);
            for(int j=1;j<=(reduzido?1:3);j++)c.arco(chama,raio-.25*j,.11,2.7-j*.16,giro-j*.10,1.16+j*.06,.52,n,ROXO,alpha*(.45-j*.07));
            if(t>=6)for(int j=0;j<(reduzido?6:18);j++){
                double a=-1.25+j*2.5/(reduzido?5:17),r=raio+.2+(t-6)*.12,tail=.35+.12*(j%4);
                V inicio=new V(Math.sin(a)*r,1.15+Math.sin(a)*.52,Math.cos(a)*r);
                V fim=new V(Math.sin(a)*(r+tail),inicio.y+(j%3-1)*.12,Math.cos(a)*(r+tail));
                c.faixa(borda,inicio,fim,.018,0,1,j%3==0?LILAS:ROXO,alpha*.8);
            }
            c.adicionar(11,false,sombra);c.adicionar(11,true,chama);c.adicionar(12,true,borda);
        }else if(tipo==12){
            alpha=envelope(t+.5,1.5,21);if(alpha<=0)return List.of();double length=clamp(distancia,.5,20);
            var corpo=new ArrayList<Quad>();var fios=new ArrayList<Quad>();
            for(int j=0;j<(reduzido?3:7);j++){
                V anterior=null;int passos=reduzido?16:32;
                for(int i=0;i<=passos;i++){
                    double u=i/(double)passos,a=j*2.39996+u*5-t*.12,r=(.45+.12*Math.sin(u*6+j))*Math.sin(u*Math.PI);
                    V p=new V(Math.cos(a)*r,1+Math.sin(a)*r,u*length);
                    if(anterior!=null){c.faixa(corpo,anterior,p,.12, (i-1d)/passos,u,ESCURO,alpha*.55);c.faixa(fios,anterior,p,.028,(i-1d)/passos,u,j%3==0?LILAS:ROXO,alpha*.75);}anterior=p;
                }
            }
            for(int j=0;j<(reduzido?4:10);j++){
                double a=j*2.39996,r=.3+(t/20)*.8;V inicio=new V(Math.cos(a)*r,1+Math.sin(a)*r,length);
                c.faixa(fios,inicio,inicio.add(new V(Math.cos(a)*.4,Math.sin(a)*.4,.18)),.025,0,1,LILAS,alpha*.7);
            }
            c.adicionar(11,false,corpo);c.adicionar(12,true,fios);
        }else if(tipo==17){
            double duracao=tipo==17?32:90;alpha=envelope(t,5,duracao);if(alpha<=0)return List.of();
            var preto=new ArrayList<Quad>();var roxo=new ArrayList<Quad>();var veios=new ArrayList<Quad>();
            int petalas=reduzido?4:8;double altura=tipo==17?2.6:1.9,raio=tipo==17?.8:1.05*(1-clamp(t/90,0,1)*.65);
            for(int j=0;j<petalas;j++){
                V anterior=null;int passos=reduzido?14:28;
                for(int i=0;i<=passos;i++){
                    double u=i/(double)passos,a=j*Math.PI*2/petalas+u*1.8-t*.045,r=raio*(.55+.45*Math.sin(u*Math.PI));
                    V pos=new V(Math.cos(a)*r,.1+u*altura,Math.sin(a)*r);
                    if(anterior!=null){double w=Math.pow(Math.sin(u*Math.PI),.65);
                        c.faixa(preto,anterior,pos,.16*w,(i-1d)/passos,u,ESCURO,alpha*.7);
                        c.faixa(roxo,anterior,pos,.09*w,(i-1d)/passos,u,ROXO,alpha*.65);
                        c.faixa(veios,anterior.add(new V(0,.015,0)),pos.add(new V(0,.015,0)),.015*w,(i-1d)/passos,u,LILAS,alpha*.6);
                    }anterior=pos;
                }
            }
            c.adicionar(11,false,preto);c.adicionar(11,true,roxo);c.adicionar(12,true,veios);
        }
        if(tipo==14)c.catedral(t,reduzido);
        return List.copyOf(c.lotes);
    }
    /** Radial branching circuit and a straight-sided energy enclosure, matching the supplied visual reference. */
    private void catedral(double t,boolean reduzido){
        if(t<0||t>=88)return;
        var linhas=new ArrayList<Quad>();var detalhes=new ArrayList<Quad>();
        int setores=reduzido?24:64;double alpha=Math.min(1,t/4)*(1-PerfilVfxGojo.suave((t-80)/8));
        double raio=1.6+24*PerfilVfxGojo.suave(t/26),parede=28*PerfilVfxGojo.suave((t-25)/25);
        for(int j=0;j<setores;j++){
            double a=j*Math.PI*2/setores,b=(j+1d)*Math.PI*2/setores;
            V base=new V(Math.cos(a)*raio,.06,Math.sin(a)*raio),proxima=new V(Math.cos(b)*raio,.06,Math.sin(b)*raio);
            faixa(linhas,base,proxima,.11,0,1,LILAS,alpha*.85);
            V ant=new V(Math.cos(a)*1.6,.12,Math.sin(a)*1.6);
            int n=reduzido?5:9;
            for(int k=1;k<=n;k++){
                double u=k/(double)n,r=1.6+(raio-1.6)*u,ang=a+Math.sin(k*2.1+j*1.7)*.018;
                V p=new V(Math.cos(ang)*r,.12,Math.sin(ang)*r);
                faixa(linhas,ant,p,.09,0,1,LILAS,alpha*.8);
                if(k%3==0){double branch=ang+.055;faixa(detalhes,p,new V(Math.cos(branch)*(r+1.2),.06,Math.sin(branch)*(r+1.2)),.06,0,1,ROXO,alpha*.7);}ant=p;
            }
            if(parede>.05){
                V top=base.add(new V(0,parede,0)),top2=proxima.add(new V(0,parede,0));
                faixa(linhas,base,top,.11,0,1,ROXO,alpha*.55);faixa(detalhes,top,top2,.09,0,1,LILAS,alpha*.65);
                if(j%3==0){V vizinha=new V(Math.cos(b)*raio,parede*.55,Math.sin(b)*raio);faixa(detalhes,base.add(new V(0,parede*.35,0)),vizinha,.08,0,1,LILAS,alpha*.6);}
                if(j%2==0)faixa(linhas,top,new V(0,parede+3,0),.09,0,1,ROXO,alpha*.45);
            }
        }
        // The initial small circle remains readable around the caster throughout preparation.
        for(int j=0;j<64;j++){
            double a=j*Math.PI/32,b=(j+1)*Math.PI/32;
            faixa(detalhes,new V(Math.cos(a)*1.6,.08,Math.sin(a)*1.6),new V(Math.cos(b)*1.6,.08,Math.sin(b)*1.6),.12,0,1,LILAS,alpha);
        }
        adicionar(15,true,linhas);adicionar(15,true,detalhes);
    }
    private ComposicaoCid(){this(new V(0,2,-8));}
}
