package com.cauansilva.arcanecontrol.core.ichigo;
import com.cauansilva.arcanecontrol.core.rpg.*;
import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.vfx.RoteiroUltimate;
import com.google.gson.*;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class IchigoTest {
    private JsonObject json(String path)throws Exception{try(var in=getClass().getResourceAsStream(path)){assertNotNull(in,path);return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}}
    @Test void idsAntigosEIdReservadoDoGddSaoPreservados(){
        assertEquals(1,PoderAnime.GOJO.id);assertEquals(2,PoderAnime.CID.id);assertEquals(3,PoderAnime.FRIEREN.id);assertEquals(4,PoderAnime.ASTA.id);
        assertEquals(6,PoderAnime.ICHIGO.id);assertNull(PoderAnime.porId(5));
        var e=new EstadoPoderes(4,15,77,new int[15],new int[75]);assertFalse(e.tem(PoderAnime.ICHIGO));
        assertTrue(e.desbloquear(PoderAnime.ICHIGO,true));assertEquals(PoderAnime.ASTA,e.ativo());assertTrue(e.selecionar(PoderAnime.ICHIGO));
        var relido=new EstadoPoderes(e.ativo().id,e.desbloqueados(),e.conhecimento(),e.xps(),e.usos());
        assertEquals(PoderAnime.ICHIGO,relido.ativo());assertTrue(relido.tem(PoderAnime.ASTA));assertEquals(77,relido.conhecimento());
        for(int s=0;s<5;s++)assertEquals(PoderAnime.ICHIGO,PoderAnime.daTecnica(PoderAnime.ICHIGO.tecnica(s)));
    }
    @Test void bankaiNaoTemFimTemporalEOutrasFormasMantemPrazo(){
        var bankai=new EstadoDespertar(PersonagemRpg.ICHIGO,100);assertTrue(bankai.ativa(1000000));assertEquals(1200,bankai.restante(1000000));
        var asta=new EstadoDespertar(PersonagemRpg.ASTA,100);assertTrue(asta.ativa(1299));assertFalse(asta.ativa(1300));
        assertThrows(IllegalArgumentException.class,()->new EstadoDespertar(PersonagemRpg.ICHIGO,-1));
        assertThrows(IllegalArgumentException.class,()->new EstadoDespertar(PersonagemRpg.JINWOO,0));
    }
    @Test void alcanceRecargasEImpactosTemLimitesCoerentes(){
        assertEquals(0,PerfilIchigo.alcance(-1));assertEquals(28,PerfilIchigo.alcance(Integer.MAX_VALUE));
        for(boolean forma:new boolean[]{false,true})for(int s=0;s<4;s++){assertTrue(PerfilIchigo.recarga(s,forma)>0);assertTrue(PerfilIchigo.impacto(s,forma)<40);}
        assertEquals(20,PerfilIchigo.recarga(0,true));assertEquals(8,PerfilIchigo.impacto(0,false));
        assertThrows(IllegalArgumentException.class,()->PerfilIchigo.recarga(4,true));
    }
    @Test void bankaiConsomeMaisQueRegeneracaoMesmoComConjuntoEArvoreCompleta(){
        var m=MestriaRpg.maximaParaTeste();var mana=new RecursoMana(100);
        for(int segundo=0;segundo<10;segundo++){
            assertTrue(mana.consumir(PerfilIchigo.MANA_BANKAI_POR_SEGUNDO,true));
            mana.regenerar(20,5*m.multiplicadorRegeneracao());
        }
        assertTrue(mana.atual()<100);assertTrue(mana.atual()>0);
        var pouca=new RecursoMana(.1);assertFalse(pouca.consumir(PerfilIchigo.MANA_BANKAI_POR_SEGUNDO,false));assertEquals(.1,pouca.atual());
    }
    @Test void gestosNovosCarregamPeloPlayerAnimator()throws Exception{
        try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/player_animation/ichigo.json")){assertNotNull(in);assertEquals(9,AnimationSerializing.deserializeAnimation(in).size());}
        var moves=CatalogoMovesetsRpg.padrao().get(PersonagemRpg.ICHIGO);assertEquals("Getsuga Tenshou",moves.base().get(0).nome());assertEquals("Máscara Hollow",moves.transformada().get(3).nome());
    }
    @Test void modelosPossuemHierarquiaValidaEAtlasCompativel()throws Exception{
        for(String path:new String[]{"ichigo/zangetsu","ichigo/tensa_zangetsu","armaduras/ichigo","nemesis/6"}){
            var geo=json("/assets/arcanecontrol/geo/"+path+".geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();var nomes=new HashSet<String>();
            for(var v:geo.getAsJsonArray("bones")){var bone=v.getAsJsonObject();assertTrue(nomes.add(bone.get("name").getAsString()));}
            for(var v:geo.getAsJsonArray("bones")){var b=v.getAsJsonObject();if(b.has("parent"))assertTrue(nomes.contains(b.get("parent").getAsString()));}
        }
        for(String name:new String[]{"zangetsu","ichigo_cabeca","ichigo_peito","ichigo_pernas","ichigo_botas"}){assertNotNull(json("/assets/arcanecontrol/models/item/"+name+".json"));assertNotNull(json("/data/arcanecontrol/recipes/"+name+".json"));}
    }
    @Test void impactFramesNaoDisparamNaPreparacaoDaChuvaDeEspadas(){
        assertNull(RoteiroUltimate.para(2,34,10));assertEquals(24,RoteiroUltimate.para(2,34,64).tick());
        assertEquals(90,RoteiroUltimate.para(1,32,110).tick());assertEquals(25,RoteiroUltimate.para(3,38,40).tick());
        assertEquals(PerfilIchigo.impacto(0,false),8);
        assertEquals(18,RoteiroUltimate.para(4,306,32).tick());assertNull(RoteiroUltimate.para(1,306,32));
    }
    @Test void espadaAstaCentralizaEmpunhaduraSemMudarGuarda()throws Exception{
        var g=json("/assets/arcanecontrol/geo/asta/matadora.geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        var root=g.getAsJsonArray("bones").get(0).getAsJsonObject();var grip=root.getAsJsonArray("cubes").get(0).getAsJsonObject();
        assertEquals(0,grip.getAsJsonArray("origin").get(1).getAsDouble()+grip.getAsJsonArray("size").get(1).getAsDouble()/2,1e-9);
        var item=json("/assets/arcanecontrol/models/item/matadora_demonios.json").getAsJsonObject("display");
        assertEquals(-90,item.getAsJsonObject("thirdperson_righthand").getAsJsonArray("rotation").get(1).getAsInt());
        assertEquals(90,item.getAsJsonObject("thirdperson_lefthand").getAsJsonArray("rotation").get(1).getAsInt());
    }
}
