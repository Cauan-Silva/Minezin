package com.cauansilva.arcanecontrol.core.client;
import com.cauansilva.arcanecontrol.core.client.tecido.SuperficieTecido;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
/** The smooth coat is one sheet (no gaps between front, sides and back) and the collar a closed rounded shell. */
class SuperficieTecidoTest {
    @Test void casacoEhUmaFolhaSoComBarraRasgada()throws Exception{
        var geo=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/arcanecontrol/geo/armaduras/shadow.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var cantos=new HashMap<String,double[][]>();
        for(var e:geo.getAsJsonArray("bones")){var b=e.getAsJsonObject();var n=b.get("name").getAsString();if(!n.startsWith("bk_capa_"))continue;
            var c=b.getAsJsonArray("cubes").get(0).getAsJsonObject();var o=c.getAsJsonArray("origin");var s=c.getAsJsonArray("size");var v=new double[8][];
            for(int i=0;i<8;i++)v[i]=new double[]{o.get(0).getAsDouble()+s.get(0).getAsDouble()*(i&1),o.get(1).getAsDouble()+s.get(1).getAsDouble()*((i>>1)&1),o.get(2).getAsDouble()+s.get(2).getAsDouble()*((i>>2)&1)};
            cantos.put(n,v);}
        var t=SuperficieTecido.casaco(cantos,cantos);int n=SuperficieTecido.ORDEM.size();
        assertEquals(n*6*2+n*4,t.size(),"grade 6 linhas x "+n+" pecas + dentes");
        // Every vertex is shared by its neighbours: the number of distinct points equals the stitched grid.
        var pts=new HashSet<String>();for(var x:t)for(var p:new double[][]{x.a(),x.b(),x.c()})pts.add(Math.round(p[0]*1e4)+","+Math.round(p[1]*1e4)+","+Math.round(p[2]*1e4));
        assertEquals(7*(n+1)+n*4,pts.size(),"sem costuras abertas");
        double minY=1e9;for(var x:t)minY=Math.min(minY,Math.min(x.a()[1],Math.min(x.b()[1],x.c()[1])));assertTrue(minY<1,"barra chega perto dos pes");
    }
    /** Corners are labelled from the rest pose: a hem flung 80 degrees out (running) must not twist any quad. */
    @Test void emMovimentoNaoTorceOsPaineis()throws Exception{
        var geo=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/arcanecontrol/geo/armaduras/shadow.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var rep=new HashMap<String,double[][]>();var pos=new HashMap<String,double[][]>();double ang=Math.toRadians(80),piv=12.2-1.85*3;
        for(var e:geo.getAsJsonArray("bones")){var b=e.getAsJsonObject();var n=b.get("name").getAsString();if(!n.startsWith("bk_capa_"))continue;
            var c=b.getAsJsonArray("cubes").get(0).getAsJsonObject();var o=c.getAsJsonArray("origin");var s=c.getAsJsonArray("size");var v=new double[8][];var w=new double[8][];
            // The hem flares outward like a skirt opening (radially from the body axis), 80 degrees below the pivot row.
            double cx=o.get(0).getAsDouble()+s.get(0).getAsDouble()/2,cz=o.get(2).getAsDouble()+s.get(2).getAsDouble()/2,l=Math.hypot(cx,cz),nx=cx/l,nz=cz/l;
            for(int i=0;i<8;i++){v[i]=new double[]{o.get(0).getAsDouble()+s.get(0).getAsDouble()*(i&1),o.get(1).getAsDouble()+s.get(1).getAsDouble()*((i>>1)&1),o.get(2).getAsDouble()+s.get(2).getAsDouble()*((i>>2)&1)};
                double y=v[i][1]-piv;if(y>=0){w[i]=v[i].clone();continue;}
                double rad=v[i][0]*nx+v[i][2]*nz,tx=v[i][0]-rad*nx,tz=v[i][2]-rad*nz;
                double r2=rad*Math.cos(ang)-y*Math.sin(ang),y2=rad*Math.sin(ang)+y*Math.cos(ang);
                w[i]=new double[]{tx+r2*nx,piv+y2,tz+r2*nz};}
            rep.put(n,v);pos.put(n,w);}
        var t=SuperficieTecido.casaco(pos,rep);int n=SuperficieTecido.ORDEM.size();
        // Each grid quad's two triangles face the same way (a twisted quad would make them oppose).
        for(int k=0;k<n*6*2;k+=2){var a=SuperficieTecido.normal(t.get(k));var b=SuperficieTecido.normal(t.get(k+1));assertTrue(a[0]*b[0]+a[1]*b[1]+a[2]*b[2]>0,"quad "+k/2+" torcido");}
    }
    /** User: the front cloth collides with the legs. A sheet hanging through a leg comes out of it sideways. */
    @Test void tecidoNaoAtravessaAsPernas(){
        double[][] rep=new double[8][];for(int i=0;i<8;i++)rep[i]=new double[]{4*(i&1),12*((i>>1)&1),-2+4*((i>>2)&1)};
        var perna=SuperficieTecido.caixa(rep,rep);
        var dentro=new double[]{2,6,-1.5};var p=SuperficieTecido.colidir(dentro,List.of(perna));
        assertTrue(p[2]<-2||p[0]<0||p[0]>4,"empurrado para fora");assertEquals(6,p[1],1e-9,"nunca para cima ou para baixo");
        var fora=new double[]{2,6,-3};assertArrayEquals(fora,SuperficieTecido.colidir(fora,List.of(perna)),1e-12);
    }
    @Test void golaFechadaArredondadaEMaisAltaAtras(){
        var g=SuperficieTecido.gola();assertFalse(g.isEmpty());
        for(var x:g)for(var p:new double[][]{x.a(),x.b(),x.c()})assertTrue(Double.isFinite(p[0]+p[1]+p[2]));
        double lim=180-SuperficieTecido.GOLA_ABERTURA;
        assertTrue(SuperficieTecido.alturaGola(0)>SuperficieTecido.alturaGola(120));
        assertTrue(SuperficieTecido.alturaGola(lim)<SuperficieTecido.alturaGola(lim-40)*.4,"pontas arredondadas");
        assertTrue(SuperficieTecido.pontoGola(0,1,0)[2]>SuperficieTecido.pontoGola(0,0,0)[2],"abre para fora");
        assertTrue(g.stream().anyMatch(x->x.material()==SuperficieTecido.ROXO)&&g.stream().anyMatch(x->x.material()==SuperficieTecido.OURO));
    }
}
