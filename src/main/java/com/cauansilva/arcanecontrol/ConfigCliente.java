package com.cauansilva.arcanecontrol;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ConfigCliente {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue CINEMATICAS;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CINEMATICAS = builder.comment("Ativar câmera, zoom e tremor nas Ultimates locais.")
                .define("cinematicas", true);
        SPEC = builder.build();
    }
}
