package com.cauansilva.arcanecontrol.core.client.tecido;

import com.cauansilva.arcanecontrol.core.cid.MolaMantoCid;
import java.util.*;

/**
 * Asta's Black Bulls robe, built like Cid's cape (MantoDirecionalCid): the back panel is one connected particle cloth
 * pinned along the shoulders, and each open front panel rides a damped angular spring like Cid's front flaps.
 * Bones: asta_capa_{costas|frente_esq|frente_dir}_{coluna}_{segmento} (armor_reference_shapes.CAPA_PAINEIS).
 */
public final class TecidoAsta {
    public static final int SEGMENTOS=2,COLUNAS_COSTAS=3,COLUNAS_FRENTE=2;
    public static final List<String> OSSOS;
    static{
        var nomes=new ArrayList<String>();
        for(var painel:new String[]{"costas","frente_esq","frente_dir"})
            for(int c=0;c<(painel.equals("costas")?COLUNAS_COSTAS:COLUNAS_FRENTE);c++)for(int s=0;s<SEGMENTOS;s++)nomes.add("asta_capa_"+painel+"_"+c+"_"+s);
        OSSOS=List.copyOf(nomes);
    }
    private static final MolaMantoCid.Pose ZERO=new MolaMantoCid.Pose(0,0,0);
    private final MalhaTecido costas=new MalhaTecido(COLUNAS_COSTAS,SEGMENTOS+1,3.2/16,2.8/16);
    private final MolaMantoCid esquerda=new MolaMantoCid(1.15),direita=new MolaMantoCid(1.0);
    private double tempo;
    public void reset(){costas.reset();esquerda.reset();direita.reset();tempo=0;}
    public void tick(MolaMantoCid.Entrada e){
        if(!Double.isFinite(e.frente()+e.lado()+e.vertical()+e.giro())){reset();return;}
        tempo++;var v=vento(tempo);double frente=e.frente()+v[0],lado=e.lado()+v[1];
        costas.tick(frente,lado,e.vertical(),e.giro());
        // Turning swings the two open front panels apart, as Cid's flaps do.
        esquerda.tick(new MolaMantoCid.Entrada(frente,lado+e.giro()*.15,e.vertical(),e.giro(),e.noChao()));
        direita.tick(new MolaMantoCid.Entrada(frente,lado-e.giro()*.15,e.vertical(),e.giro(),e.noChao()));
    }
    /** Parent-relative rotation delta (radians) for one robe bone; sampling never advances the physics. */
    public MolaMantoCid.Pose pose(String osso,double parcial){
        if(!osso.startsWith("asta_capa_"))return ZERO;
        String[] p=osso.substring(10).split("_");int s=Integer.parseInt(p[p.length-1]),c=Integer.parseInt(p[p.length-2]);
        if(osso.startsWith("asta_capa_costas_")){var r=costas.rotacao(c,s,parcial);return new MolaMantoCid.Pose(r.x(),-r.y(),-r.z());}
        int lado=osso.startsWith("asta_capa_frente_esq_")?1:-1;var mola=lado>0?esquerda:direita;
        // Top segment follows the spring's second stage; the hem adds the difference to the outer stage, so the
        // panel bends like cloth instead of swinging as one board. Inner columns move a little less.
        var a=mola.amostrar(1,parcial);var b=s==0?a:mola.amostrar(3,parcial);
        double k=c==0?1:.85;
        double x=s==0?-a.x()*.9:-(b.x()-a.x())*1.4,y=(s==0?a.y():b.y()-a.y())*.6,z=(s==0?a.z():b.z()-a.z())*.7+(s==0?a.x():b.x()-a.x())*lado*.15;
        return new MolaMantoCid.Pose(clamp(x*k,.32),clamp(y*k,.16),clamp(z*k,.18));
    }
    /** Idle breeze as apparent motion (frente, lado): incommensurate sines so the flutter never visibly loops. */
    public static double[] vento(double t){
        return new double[]{.035*Math.sin(t*.09)+.02*Math.sin(t*.23+1.3)+.012*Math.sin(t*.61+.4),.025*Math.sin(t*.07+.5)+.015*Math.sin(t*.19+2.1)};
    }
    public double erroAlongamento(){return costas.erroAlongamento();}
    private static double clamp(double v,double max){return Math.max(-max,Math.min(max,v));}
}
