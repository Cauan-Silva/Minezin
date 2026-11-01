package com.cauansilva.arcanecontrol;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ConfigServidor {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue DESTRUIR;
    public static final ForgeConfigSpec.IntValue RAIO;
    public static final ForgeConfigSpec.BooleanValue BURACO_DESTRUIR;
    public static final ForgeConfigSpec.DoubleValue TOJI_ESCALA,AURORA_ESCALA,QUAL_ESCALA,LIEBE_ESCALA;
    public static double escalaChefe(int kit){return (kit==1?TOJI_ESCALA:kit==2?AURORA_ESCALA:kit==3?QUAL_ESCALA:LIEBE_ESCALA).get();}
    static {
        var b = new ForgeConfigSpec.Builder();
        b.push("atomic");
        DESTRUIR = b.comment("Permite que I Am Atomic destrua terreno. Respeita eventos de explosao do Forge.").define("destruirTerreno", true);
        RAIO = b.comment("Escala da cratera em blocos. A resistencia do terreno altera o alcance real.").defineInRange("raioCratera", 20, 1, 28);
        b.pop();
        BURACO_DESTRUIR=b.comment("Permitir cratera no colapso da Ultimate da Frieren; usa eventos de explosao do Forge.")
                .define("buracoNegroDestruirTerreno",true);
        b.push("bosses");
        TOJI_ESCALA=b.comment("Escala visual e da colisao; aplicada ao invocar ou carregar o boss.").defineInRange("tojiEscala",1.0,.5,3.0);
        AURORA_ESCALA=b.defineInRange("auroraEscala",1.0,.5,3.0);
        QUAL_ESCALA=b.defineInRange("qualEscala",1.4,.5,3.0);
        LIEBE_ESCALA=b.defineInRange("liebeEscala",.85,.5,3.0);
        b.pop();
        SPEC = b.build();
    }
}
