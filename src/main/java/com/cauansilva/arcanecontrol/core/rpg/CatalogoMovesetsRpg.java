package com.cauansilva.arcanecontrol.core.rpg;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Fonte unica de nomes e especificacoes do GDD; nao equivale a um registro de habilidades prontas. */
public final class CatalogoMovesetsRpg {
    public record Tecnica(String tecla,String nome,String descricao){}
    public record Moveset(String nome,String universo,String descricao,String despertar,List<Tecnica> base,List<Tecnica> transformada){
        public Moveset{base=List.copyOf(base);transformada=List.copyOf(transformada);}
    }
    private final Map<PersonagemRpg,Moveset> personagens;
    private CatalogoMovesetsRpg(Map<PersonagemRpg,Moveset> personagens){this.personagens=Map.copyOf(personagens);}
    public Moveset get(PersonagemRpg kit){return personagens.get(kit);}
    public int tamanho(){return personagens.size();}
    public static CatalogoMovesetsRpg padrao()throws IOException{
        try(var in=CatalogoMovesetsRpg.class.getResourceAsStream("/data/arcanecontrol/rpg/gdd_v2.json")){
            if(in==null)throw new IOException("Catalogo GDD ausente");return ler(in);
        }
    }
    public static CatalogoMovesetsRpg ler(InputStream in)throws IOException{
        byte[] bytes=in.readNBytes(131073);if(bytes.length>131072)throw new IOException("Catalogo excede 128 KiB");
        try{
            var root=JsonParser.parseString(new String(bytes,StandardCharsets.UTF_8)).getAsJsonObject();
            if(root.get("versao").getAsInt()!=2)throw new IllegalArgumentException("Versao invalida");
            var mapa=new EnumMap<PersonagemRpg,Moveset>(PersonagemRpg.class);
            for(var element:root.getAsJsonArray("personagens")){
                var p=element.getAsJsonObject();var kit=PersonagemRpg.porId(p.get("id").getAsInt());
                if(kit==null||mapa.containsKey(kit))throw new IllegalArgumentException("Kit invalido ou duplicado");
                mapa.put(kit,new Moveset(texto(p,"nome"),texto(p,"universo"),texto(p,"descricao"),texto(p,"despertar"),tecnicas(p.getAsJsonArray("base")),tecnicas(p.getAsJsonArray("transformada"))));
            }
            if(mapa.size()!=PersonagemRpg.values().length)throw new IllegalArgumentException("Catalogo incompleto");
            return new CatalogoMovesetsRpg(mapa);
        }catch(RuntimeException e){throw new IOException("Catalogo GDD invalido",e);}
    }
    private static String texto(JsonObject o,String key){String t=o.get(key).getAsString();if(t.isBlank()||t.length()>2000)throw new IllegalArgumentException("Texto invalido: "+key);return t;}
    private static List<Tecnica> tecnicas(JsonArray array){
        if(array.size()!=4)throw new IllegalArgumentException("Sao necessarias quatro tecnicas por forma");
        var lista=new ArrayList<Tecnica>();
        for(int i=0;i<4;i++){
            var o=array.get(i).getAsJsonObject();String tecla=texto(o,"tecla");
            if(!tecla.equals("ZXCV".substring(i,i+1)))throw new IllegalArgumentException("Teclas fora de ordem");
            lista.add(new Tecnica(tecla,texto(o,"nome"),texto(o,"descricao")));
        }
        return lista;
    }
}
