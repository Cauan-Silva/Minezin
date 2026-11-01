package com.cauansilva.arcanecontrol.core.poderes;

public enum PoderAnime {
    GOJO(1,"Gojo",0,0x49CFFF,new String[]{"Azul","Vermelho","Teleporte Curto","Vazio Roxo","Vazio Infinito"}),
    CID(2,"Cid / Shadow",120,0xBC79FF,new String[]{"Corte de Slime","Passo das Sombras","Fios de Slime","Lâmina Oculta","I Am Atomic"}),
    FRIEREN(3,"Frieren",240,0xE8CF86,new String[]{"Zoltraak","Barreira Arcana","Impulso Arcano","Gaiola de Vento","Liberar Mana"}),
    ASTA(4,"Asta",180,0xD84559,new String[]{"Espada Matadora de Demônios","Corte Divisor","Avanço Touro Negro","Barreira Antimagia","Assimilação Demoníaca"});
    public final int id,custo,cor;
    public final String nome;
    public final String[] tecnicas;
    PoderAnime(int id,String nome,int custo,int cor,String[] tecnicas){this.id=id;this.nome=nome;this.custo=custo;this.cor=cor;this.tecnicas=tecnicas;}
    public int bit(){return 1<<(id-1);}
    public int tecnica(int slot){return slot<0||slot>=tecnicas.length?0:(id-1)*10+slot+1;}
    public int nivelTecnica(int slot){return slot<0||slot>=tecnicas.length?99:new int[]{1,2,3,5,8}[slot];}
    public static PoderAnime porId(int id){for(var p:values())if(p.id==id)return p;return null;}
    public static PoderAnime daTecnica(int id){for(var p:values())if(id>=(p.id-1)*10+1&&id<=(p.id-1)*10+5)return p;return null;}
}
