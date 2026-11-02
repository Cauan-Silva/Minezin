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
    @Test void exportaAmostrasDaFisicaRealParaAPrevia()throws Exception{
        var root=new JsonObject();var detalhes=new JsonObject();var paineis=new JsonObject();root.add("detalhes",detalhes);root.add("paineis",paineis);
        var m=new MantoDirecionalCid();capture(root,detalhes,paineis,"parado",m);
        for(int i=0;i<60;i++)m.tick(new MolaMantoCid.Entrada(.27,0,0,0,true));capture(root,detalhes,paineis,"correndo",m);
        for(int i=0;i<10;i++)m.tick(new MolaMantoCid.Entrada(.2,.12,0,.4,true));capture(root,detalhes,paineis,"virando",m);
        for(int i=0;i<20;i++)m.tick(new MolaMantoCid.Entrada(.02,0,-.5,.01,false));capture(root,detalhes,paineis,"caindo",m);
        m.reset();for(int i=0;i<100;i++)m.tick(new MolaMantoCid.Entrada(-.22,0,0,0,true));capture(root,detalhes,paineis,"re",m);
        m.reset();for(int i=0;i<100;i++)m.tick(new MolaMantoCid.Entrada(0,.22,0,0,true));capture(root,detalhes,paineis,"direita",m);
        m.reset();for(int i=0;i<100;i++)m.tick(new MolaMantoCid.Entrada(0,-.22,0,0,true));capture(root,detalhes,paineis,"esquerda",m);
        m.reset();for(int i=0;i<60;i++)m.tick(new MolaMantoCid.Entrada(.27,0,0,0,true));for(int i=0;i<5;i++)m.tick(PARADO);capture(root,detalhes,paineis,"freando",m);
        for(int tipo:new int[]{11,12,13,14,17}){
            m.reset();int fim=tipo==14?96:tipo==11?8:tipo==12?4:tipo==13?12:10;
            for(int i=0;i<=fim;i++){var impulso=RoteiroTecidoCid.amostrar(tipo,i);m.tick(new MolaMantoCid.Entrada(impulso.frente(),impulso.lado(),0,impulso.giro(),true));}
            capture(root,detalhes,paineis,"acao"+tipo,m);
        }
        assertTrue(root.getAsJsonArray("correndo").get(0).getAsJsonArray().get(0).getAsDouble()<-.1);
        m.reset();
        for(int frame=0;frame<220;frame++){
            double frente=frame<35?.25:frame<70?.12:0,lado=frame>=35&&frame<70?.2:frame>=70&&frame<100?-.2:0,giro=frame>=100&&frame<120?.12:0;
            var impulso=RoteiroTecidoCid.amostrar(17,frame-120);
            double vertical=frame>=140&&frame<180?-.65:0;
            m.tick(new MolaMantoCid.Entrada(frente+impulso.frente(),lado+impulso.lado(),vertical,giro+impulso.giro(),true));
            capture(root,detalhes,paineis,"frame"+frame,m);
        }
        Files.createDirectories(Path.of("build"));Files.writeString(Path.of("build/cid-cape-samples.json"),root.toString());
    }
    private static void capture(JsonObject root,JsonObject detalhes,JsonObject paineis,String nome,MantoDirecionalCid m){
        var panelArray=new JsonArray();for(int i=0;i<5;i++)panelArray.add(sample(m,i));paineis.add(nome,panelArray);root.add(nome,sample(m,2));var d=new JsonObject();
        for(String n:DetalhesTrajeCid.OSSOS){var p=m.detalhe(n,1);var a=new JsonArray();a.add(p.x());a.add(p.y());a.add(p.z());d.add(n,a);}
        detalhes.add(nome,d);
    }
    private static JsonArray sample(MantoDirecionalCid m,int painel){var a=new JsonArray();for(int i=0;i<4;i++){var p=m.amostrar(painel,i,1);var v=new JsonArray();v.add(p.x());v.add(p.y());v.add(p.z());a.add(v);}return a;}
}
