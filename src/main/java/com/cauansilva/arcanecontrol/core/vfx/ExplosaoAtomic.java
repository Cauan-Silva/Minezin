package com.cauansilva.arcanecontrol.core.vfx;

import java.util.ArrayList;
import java.util.List;
import com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.V;
import com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.Quad;
import com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.Lote;

/** World-space release, rather than a screen flash. Original geometry; visual only. */
public final class ExplosaoAtomic {
    private static final int VIOLETA=0xAC34FF,CLARO=0xF4CBFF;
    /** The pillar covers the Atomic dome and rises far above the build limit. */
    public static final double RAIO_DOMO=24,ALTURA_PILAR=320;
    public record Estado(double raio,double altura,double coluna,double dissipacao){}
    private static double clamp(double v,double a,double b){return Math.max(a,Math.min(b,v));}
    public static Estado estado(double tick){
        if(!Double.isFinite(tick)||tick<90||tick>=240)return new Estado(0,0,0,0);
        double age=tick-90,abrir=1-Math.exp(-age/4.5),fade=1-PerfilVfxGojo.suave((age-75)/75);
        return new Estado((1.5+5.8*abrir)*(.7+.3*fade),12+52*(1-Math.exp(-age/7)),Math.min(1,.4+age/3)*fade,fade);
    }
    private static void fita(List<Quad> out,V cam,V a,V b,double width,double u,double v,int cor,double alpha){
        if(width<=0||alpha<=.002)return;V side=b.sub(a).cross(cam.sub(a)).normal().mul(width);
        out.add(new Quad(a.sub(side),b.sub(side),b.add(side),a.add(side),u,v,cor,(float)clamp(alpha,0,1),0,1,true));
    }
    private static V coluna(double a,double h,double r,double tick){
        double flow=1+.045*Math.sin(a*7+h*.25-tick*.11)+.025*Math.sin(a*13-h*.14+tick*.08);
        return new V(Math.cos(a)*r*flow,h,Math.sin(a)*r*flow);
    }
    public static List<Lote> gerar(double tick,V camera,boolean reduzido){
        var e=estado(tick);if(e.coluna<=0||!Double.isFinite(camera.x()+camera.y()+camera.z()))return List.of();
        double age=tick-90;var casca=new ArrayList<Quad>();var nucleo=new ArrayList<Quad>();var ondas=new ArrayList<Quad>();var espirais=new ArrayList<Quad>();var raios=new ArrayList<Quad>();
        int n=reduzido?24:48,steps=reduzido?8:12;
        // User: a colossal light the size of the dome, absurdly tall (the old solid column read as a bottle).
        // Straight additive shells, faint on the outside and brighter inward, so the pillar reads as light filling
        // the whole dome rather than a solid tube; a white core facing the camera; rays climbing its surface.
        double abrir=1-Math.exp(-age/3.5),R=RAIO_DOMO*abrir,H=ALTURA_PILAR*(1-Math.exp(-age/5));
        double[][] camadas={{1,.16,VIOLETA},{.62,.24,0xCB66FF},{.3,.38,CLARO}};
        int lados=reduzido?20:32,andares=reduzido?5:8;
        for(var c:camadas)for(int y=0;y<andares;y++)for(int i=0;i<lados;i++){
            double a=i*Math.PI*2/lados,b=(i+1d)*Math.PI*2/lados,u=y/(double)andares,v=(y+1d)/andares,r=R*c[0];
            float al=(float)(e.coluna*c[1]*(1-PerfilVfxGojo.suave((u-.55)/.45)));
            nucleo.add(new Quad(coluna(a,u*H,r,tick),coluna(b,u*H,r,tick),coluna(b,v*H,r,tick),coluna(a,v*H,r,tick),i/(double)lados,(i+1d)/lados,(int)c[2],al,u,v));
        }
        int seg=reduzido?10:20;
        for(int k=0;k<seg;k++){
            double u=k/(double)seg,v=(k+1d)/seg;V a=new V(0,u*H,0),b=new V(0,v*H,0);
            double largura=R*.12*(1-.5*u),brilho=.85+.15*Math.sin(tick*.6-u*9);
            fita(nucleo,camera,a,b,largura,u,v,0xFFFFFF,e.coluna*brilho*(1-u*.5));
        }
        int raiosCoroa=reduzido?14:32;
        for(int j=0;j<raiosCoroa;j++){
            double ang=j*2.399963+age*.02*(j%2==0?1:-1),dist=R*(.35+.65*(((j*37)%10)/9.0)),alto=H*(.3+.7*(((j*53+7)%10)/9.0));
            double vida=Math.max(0,Math.min(1,(age-j%5)/6)),largura=.4+.8*(((j*17)%10)/9.0);
            V prev=null;int partes=8;
            for(int k=0;k<=partes;k++){double u=k/(double)partes;V q=new V(Math.cos(ang)*dist,u*alto*vida,Math.sin(ang)*dist);
                if(prev!=null)fita(raios,camera,prev,q,largura*(1-u*.7),(k-1d)/partes,u,j%3==0?VIOLETA:CLARO,e.coluna*.5*(1-u));prev=q;}
        }
        // Fast horizontal shock fronts contrast with the sustained vertical mass.
        for(int j=0;j<3;j++){
            double t=age-j*7;if(t<0||t>58)continue;double fade=Math.pow(1-t/58,1.3),r=1+t*.58,width=.22+Math.min(t,10)*.11;
            for(int i=0;i<n;i++){
                double a=i*Math.PI*2/n,b=(i+1d)*Math.PI*2/n;
                V p=new V(Math.cos(a),0,Math.sin(a)),q=new V(Math.cos(b),0,Math.sin(b));
                double h=.18+j*.28;
                ondas.add(new Quad(p.mul(r-width).add(new V(0,h,0)),q.mul(r-width).add(new V(0,h,0)),q.mul(r+width).add(new V(0,h,0)),p.mul(r+width).add(new V(0,h,0)),i/(double)n,(i+1d)/n,j==0?CLARO:VIOLETA,(float)(fade*.8)));
            }
        }
        // Broad helical collars climb through the base: readable vortex, not thin static rings.
        for(int j=0;j<(reduzido?3:6);j++){
            V prev=null;int count=reduzido?24:48;
            for(int i=0;i<=count;i++){
                double u=i/(double)count,a=u*Math.PI*2+j*1.1-age*.09,h=.3+j*.9+u*2.3;
                double r=e.raio*(1.08+.20*Math.sin(u*Math.PI))+Math.sin(u*8+age*.07)*.2;
                V p=new V(Math.cos(a)*r,h,Math.sin(a)*r);
                if(prev!=null)fita(espirais,camera,prev,p,.35+.2*Math.sin(u*Math.PI),(i-1d)/count,u,j%2==0?CLARO:VIOLETA,e.coluna*.68);prev=p;
            }
        }
        if(age<72)for(int j=0;j<(reduzido?8:20);j++){
            double a=j*2.399963,r=4+(1-Math.exp(-age/10))*(9+j%4),fade=(1-age/72)*e.dissipacao;
            V prev=new V(Math.cos(a)*r,.15,Math.sin(a)*r);
            for(int k=1;k<=5;k++){
                double u=k/5d;V p=new V(Math.cos(a+u*.12)*r*(1+u*.12),.15+u*(3+j%5*1.3),Math.sin(a+u*.12)*r*(1+u*.12));
                fita(raios,camera,prev,p,.09*(1-u*.6),(k-1d)/5,u,CLARO,fade*.8);prev=p;
            }
        }
        // Alpha shell preserves the silhouette; inner core/additive collars provide intensity.
        var out=new ArrayList<Lote>();if(!casca.isEmpty())out.add(new Lote(13,false,List.copyOf(casca)));if(!nucleo.isEmpty())out.add(new Lote(13,true,List.copyOf(nucleo)));
        if(!ondas.isEmpty())out.add(new Lote(14,true,List.copyOf(ondas)));
        if(!espirais.isEmpty())out.add(new Lote(14,true,List.copyOf(espirais)));if(!raios.isEmpty())out.add(new Lote(12,true,List.copyOf(raios)));
        return List.copyOf(out);
    }
    private ExplosaoAtomic(){}
}
