package com.cauansilva.arcanecontrol.core.client.tecido;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** How a garment's cloth behaves, read from assets/arcanecontrol/tecido/<estilo>.json (docs/tecido-arquitetura.md),
 *  so a heavy armour cape and a light silk can differ without touching Java. Every field is optional and multiplies
 *  today's tuned behaviour (1 = unchanged):
 *    peso     gravity on the particles (heavier hangs straighter and swings less)
 *    rigidez  how hard the links keep their length (lower stretches and sags more)
 *    arrasto  air drag (higher settles faster, lower keeps swinging)
 *    inercia  how strongly the wearer's motion throws the cloth
 *    mola     response of the spring pieces (sleeves, straps, bandage ends)
 *  Values are clamped to sane ranges; a missing or broken file gives the defaults. */
public record PerfilTecido(double peso,double rigidez,double arrasto,double inercia,double mola){
    public static final PerfilTecido PADRAO=new PerfilTecido(1,1,1,1,1);
    private static final Map<String,PerfilTecido> CACHE=new ConcurrentHashMap<>();
    public PerfilTecido{
        peso=limitar(peso,.3,3);rigidez=limitar(rigidez,.2,1);arrasto=limitar(arrasto,.2,4);inercia=limitar(inercia,0,3);mola=limitar(mola,.5,2);
    }
    public static PerfilTecido de(String estilo){return CACHE.computeIfAbsent(estilo,PerfilTecido::carregar);}
    static PerfilTecido carregar(String estilo){
        try(var in=PerfilTecido.class.getResourceAsStream("/assets/arcanecontrol/tecido/"+estilo+".json")){
            if(in==null)return PADRAO;
            return ler(JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject());
        }catch(Exception e){return PADRAO;}
    }
    public static PerfilTecido ler(JsonObject j){
        return new PerfilTecido(num(j,"peso"),num(j,"rigidez"),num(j,"arrasto"),num(j,"inercia"),num(j,"mola"));
    }
    private static double num(JsonObject j,String k){try{return j.has(k)?j.get(k).getAsDouble():1;}catch(Exception e){return 1;}}
    private static double limitar(double v,double a,double b){return Double.isFinite(v)?Math.max(a,Math.min(b,v)):1;}
}
