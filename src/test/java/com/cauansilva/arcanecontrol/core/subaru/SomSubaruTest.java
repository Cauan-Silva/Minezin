package com.cauansilva.arcanecontrol.core.subaru;
import com.cauansilva.arcanecontrol.core.audio.RoteiroAudio;
import com.cauansilva.arcanecontrol.core.vfx.*;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import static org.junit.jupiter.api.Assertions.*;
/** Sons do Subaru e da Beatrice: registrados, no tick certo, e o gerador usa os mesmos ticks das classes puras. */
class SomSubaruTest {
    @Test void roteiroUsaSonsRegistrados()throws Exception{
        String sj;try(var in=getClass().getResourceAsStream("/assets/arcanecontrol/sounds.json")){sj=new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}
        var defs=JsonParser.parseString(sj).getAsJsonObject();var cat=Set.of(com.cauansilva.arcanecontrol.core.audio.CatalogoSons.NOMES);
        var todos=new ArrayList<RoteiroAudio.Cue>(RoteiroAudio.subaruContrato());
        for(int t:new int[]{600,601,602,603,604,605,610,611,612,613,614}){var l=RoteiroAudio.subaru(t);assertFalse(l.isEmpty(),"tipo "+t);todos.addAll(l);}
        for(var c:todos){assertTrue(defs.has(c.nome()),c.nome());assertTrue(cat.contains(c.nome()),c.nome());}
        assertEquals(SamakSubaru.CARGA,RoteiroAudio.subaru(600).get(1).atraso(),"disparo do Shamak no tick CARGA");
        assertTrue(RoteiroAudio.subaru(606).isEmpty());
    }
    @Test void geradorUsaOsTicksDasClassesPuras()throws Exception{
        var py=Files.readString(Path.of("tools/build_subaru_sfx.py"));
        Map<String,Integer> esperado=new LinkedHashMap<>();
        esperado.put("SH_CARGA",SamakSubaru.CARGA);esperado.put("SH_NEVOA",SamakSubaru.NEVOA);esperado.put("ESC_INVOCA",EscudoSubaru.INVOCA);
        esperado.put("INS_ORDEM",InstrucaoSubaru.ORDEM);esperado.put("INS_PULSO",InstrucaoSubaru.PULSO);
        esperado.put("REC_INSPIRA",RecuperacaoSubaru.INSPIRA);esperado.put("REC_LIMPA",RecuperacaoSubaru.LIMPA);esperado.put("REC_CURA",RecuperacaoSubaru.CURA);
        esperado.put("CON_PORTA",ContratoSubaru.PORTA);esperado.put("CON_ABRE",ContratoSubaru.ABRE);esperado.put("CON_SAI",ContratoSubaru.SAI);esperado.put("CON_FECHA",ContratoSubaru.FECHA);esperado.put("CON_FIM",ContratoSubaru.FIM);
        esperado.put("MI_FORMA",MinyaBeatrice.FORMA);esperado.put("MI_DISPARO",MinyaBeatrice.DISPARO);esperado.put("MI_VOO",MinyaBeatrice.VOO);esperado.put("MI_CRISTAL",MinyaBeatrice.CRISTAL);
        esperado.put("MI_QUEBRA",MinyaBeatrice.QUEBRA);esperado.put("MI_ESTACAS",MinyaBeatrice.ESTACAS);
        esperado.put("EMM_FECHA",EmmBeatrice.FECHA);esperado.put("EMM_ABRE",EmmBeatrice.ABRE);esperado.put("EMM_SOLTA",EmmBeatrice.SOLTA);
        esperado.put("UL_QUEDA",UlMinyaBeatrice.QUEDA);esperado.put("UL_ABRE",UlMinyaBeatrice.ABRE);esperado.put("UL_CHUVA",UlMinyaBeatrice.CHUVA);
        // "A,B,C=1,2,3" em uma linha: monta nome -> valor
        var lido=new HashMap<String,Integer>();var m=Pattern.compile("^([A-Z_,]+)=([0-9,]+)\\s*$",Pattern.MULTILINE).matcher(py);
        while(m.find()){String[] n=m.group(1).split(","),v=m.group(2).split(",");for(int i=0;i<n.length&&i<v.length;i++)lido.put(n[i],Integer.parseInt(v[i]));}
        for(var e:esperado.entrySet())assertEquals(e.getValue(),lido.get(e.getKey()),e.getKey());
        assertTrue(Files.exists(Path.of("art/audio/cc0/fontes.json")),"manifesto das gravacoes CC0");
    }
}
