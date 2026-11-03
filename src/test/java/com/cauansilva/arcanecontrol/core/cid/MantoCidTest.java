package com.cauansilva.arcanecontrol.core.cid;
import org.junit.jupiter.api.Test;
import com.google.gson.*;
import java.nio.file.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class MantoCidTest {
    private static final MolaMantoCid.Entrada PARADO=new MolaMantoCid.Entrada(0,0,0,0,true);
    @Test void interpolarNaoAvancaAFisicaEPassosIguaisIndependemDoNumeroDeFrames(){
        var a=new MolaMantoCid();var b=new MolaMantoCid();var entrada=new MolaMantoCid.Entrada(.27,.02,-.1,.15,false);
        for(int tick=0;tick<120;tick++){a.tick(entrada);b.tick(entrada);for(int frame=0;frame<12;frame++)a.amostrar(3,frame/12d);}
        for(int i=0;i<4;i++)assertEquals(a.amostrar(i,1),b.amostrar(i,1));
        var antes=a.amostrar(3,.5);for(int i=0;i<100;i++)a.amostrar(3,.5);assertEquals(antes,a.amostrar(3,.5));
    }
    @Test void movimentoTemLimitesAmortecimentoEReinicio(){
        var m=new MolaMantoCid();for(int t=0;t<1000;t++){
            m.tick(new MolaMantoCid.Entrada(Math.sin(t)*10,Math.cos(t)*10,t%2==0?30:-30,Math.sin(t)*30,false));
            for(int i=0;i<4;i++){var p=m.amostrar(i,.5);assertTrue(Double.isFinite(p.x()+p.y()+p.z()));assertTrue(p.x()>=-.35&&p.x()<=.08);assertTrue(Math.abs(p.y())<=.16&&Math.abs(p.z())<=.16);}
        }
        for(int i=0;i<300;i++)m.tick(PARADO);assertEquals(0,m.amostrar(3,1).x(),1e-8);
        m.tick(new MolaMantoCid.Entrada(Double.NaN,0,0,0,true));assertEquals(new MolaMantoCid.Pose(0,0,0),m.amostrar(3,1));
        assertThrows(IllegalArgumentException.class,()->m.amostrar(4,.5));
    }
    @Test void mascaraEmiteApenasDetalhesEModeloVestidoNaoCobreACabeca()throws Exception{
        JsonObject geo;try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/geo/armaduras/shadow.geo.json")){geo=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();}
        java.awt.image.BufferedImage image;try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/textures/armor/trajes/shadow_glowmask.png")){image=javax.imageio.ImageIO.read(in);}
        int ativos=0;for(int y=0;y<128;y++)for(int x=0;x<128;x++)if((image.getRGB(x,y)>>>24)>0)ativos++;else assertEquals(0,image.getRGB(x,y)&0xFFFFFF);
        assertTrue(ativos>0&&ativos<64);
        for(var v:geo.getAsJsonArray("bones")){var b=v.getAsJsonObject();String name=b.get("name").getAsString();
            if(name.equals("armorHead"))assertEquals(0,b.getAsJsonArray("cubes").size());
            for(var c:b.getAsJsonArray("cubes"))for(var f:c.getAsJsonObject().getAsJsonObject("uv").entrySet()){
                var uv=f.getValue().getAsJsonObject();int x=uv.getAsJsonArray("uv").get(0).getAsInt(),y=uv.getAsJsonArray("uv").get(1).getAsInt();
                int w=uv.getAsJsonArray("uv_size").get(0).getAsInt(),h=uv.getAsJsonArray("uv_size").get(1).getAsInt();
                assertEquals(name.startsWith("cid_glow_")?255:0,image.getRGB(x+w/2,y+h/2)>>>24,name);
            }
        }
    }
}
