package com.cauansilva.arcanecontrol;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ConfigServidor {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue DESTRUIR;
    public static final ForgeConfigSpec.IntValue RAIO;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.push("atomic");
        DESTRUIR = b.comment("Permite que I Am Atomic destrua terreno. Respeita eventos de explosao do Forge.").define("destruirTerreno", true);
        RAIO = b.comment("Escala da cratera em blocos. A resistencia do terreno altera o alcance real.").defineInRange("raioCratera", 14, 1, 24);
        b.pop();
        SPEC = b.build();
    }
}
