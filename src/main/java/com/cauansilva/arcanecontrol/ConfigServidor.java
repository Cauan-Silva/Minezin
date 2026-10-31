package com.cauansilva.arcanecontrol;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ConfigServidor {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue DESTRUIR;
    public static final ForgeConfigSpec.IntValue RAIO;
    public static final ForgeConfigSpec.BooleanValue BURACO_DESTRUIR;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.push("atomic");
        DESTRUIR = b.comment("Permite que I Am Atomic destrua terreno. Respeita eventos de explosao do Forge.").define("destruirTerreno", true);
        RAIO = b.comment("Escala da cratera em blocos. A resistencia do terreno altera o alcance real.").defineInRange("raioCratera", 20, 1, 28);
        b.pop();
        BURACO_DESTRUIR=b.comment("Permitir cratera no colapso da Ultimate da Frieren; usa eventos de explosao do Forge.")
                .define("buracoNegroDestruirTerreno",true);
        SPEC = b.build();
    }
}
