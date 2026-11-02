package com.cauansilva.arcanecontrol.core.vfx;

import java.util.ArrayList;
import java.util.List;
import com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.V;
import com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.Quad;
import com.cauansilva.arcanecontrol.core.vfx.ComposicaoCid.Lote;

/** World-space release, rather than a screen flash. Original geometry; visual only. */
public final class ExplosaoAtomic {
    private static final int VIOLETA=0xAC34FF,CLARO=0xF4CBFF;
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
        for(int layer=0;layer<2;layer++)for(int y=0;y<steps;y++)for(int i=0;i<n;i++){
            double a=i*Math.PI*2/n,b=(i+1d)*Math.PI*2/n,u=y/(double)steps,v=(y+1d)/steps;
            double r=e.raio*(layer==0?1:.65),r0=r*(.9+.45*Math.exp(-u*12)+.12*u)*Math.max(.025,1-PerfilVfxGojo.suave((u-.72)/.28)),r1=r*(.9+.45*Math.exp(-v*12)+.12*v)*Math.max(.025,1-PerfilVfxGojo.suave((v-.72)/.28));
            var q=new Quad(coluna(a,u*e.altura,r0,tick),coluna(b,u*e.altura,r0,tick),coluna(b,v*e.altura,r1,tick),coluna(a,v*e.altura,r1,tick),i/(double)n,(i+1d)/n,layer==0?VIOLETA:0xCB66FF,(float)(e.coluna*(layer==0?.65:.48)),u,v);
            (layer==0?casca:nucleo).add(q);
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
        var out=new ArrayList<Lote>();out.add(new Lote(13,false,List.copyOf(casca)));out.add(new Lote(13,true,List.copyOf(nucleo)));
        if(!ondas.isEmpty())out.add(new Lote(14,true,List.copyOf(ondas)));
        if(!espirais.isEmpty())out.add(new Lote(14,true,List.copyOf(espirais)));if(!raios.isEmpty())out.add(new Lote(12,true,List.copyOf(raios)));
        return List.copyOf(out);
    }
    private ExplosaoAtomic(){}
}
