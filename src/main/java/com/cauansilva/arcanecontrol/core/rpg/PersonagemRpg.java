package com.cauansilva.arcanecontrol.core.rpg;

/** Catalogo de design. Preserva os IDs dos tres kits existentes; nao habilita kits sem moveset. */
public enum PersonagemRpg {
    GOJO(1,"Gojo Satoru","Toji Fushiguro",20,360),
    SHADOW(2,"Cid / Shadow","Aurora",40,300),
    FRIEREN(3,"Frieren","Qual",45,300),
    ASTA(4,"Asta","Liebe",60,300),
    EREN(5,"Eren Yeager","Titã Encouraçado",120,480),
    ICHIGO(6,"Ichigo Kurosaki","Zangetsu Interior",0,300),
    NARUTO(7,"Naruto Uzumaki","Kurama",180,300),
    JINWOO(8,"Sung Jin-Woo","Igris",0,300),
    SUKUNA(9,"Sukuna","Mahoraga",30,300),
    RIMURU(10,"Rimuru Tempest","Orc Disaster",120,300),
    TANJIRO(11,"Tanjiro Kamado","Akaza",0,300),
    KILLUA(12,"Killua Zoldyck","Raio de Eletricidade Absoluta",45,300),
    GENOS(13,"Genos","Carnage Kabuto",0,300),
    JOTARO(14,"Jotaro Kujo","DIO Vampire",6,180),
    EDWARD(15,"Edward Elric","Homunculus Mestre",120,300);

    public final int id,duracaoSegundos,recargaSegundos;
    public final String nome,nemesis;
    PersonagemRpg(int id,String nome,String nemesis,int duracao,int recarga){
        this.id=id;this.nome=nome;this.nemesis=nemesis;duracaoSegundos=duracao;recargaSegundos=recarga;
    }
    public static PersonagemRpg porId(int id){for(var p:values())if(p.id==id)return p;return null;}
}
