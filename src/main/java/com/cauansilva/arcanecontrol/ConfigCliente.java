package com.cauansilva.arcanecontrol;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ConfigCliente {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue CINEMATICAS;
    public static final ForgeConfigSpec.BooleanValue PHOTON;
    public static final ForgeConfigSpec.BooleanValue VFX_REDUZIDOS;
    public static final ForgeConfigSpec.BooleanValue CLARAO;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        PHOTON = builder.comment("Ativar emissores e bloom Photon da Atomic.").define("photonAtomic", true);
        VFX_REDUZIDOS = builder.comment("Reduzir a emissao de particulas da Atomic pela metade.").define("vfxReduzidos", false);
        CLARAO = builder.comment("Ativar o clarao breve da Atomic.").define("claraoAtomic", true);
        CINEMATICAS = builder.comment("Ativar câmera, zoom e tremor nas Ultimates locais.")
                .define("cinematicas", true);
        SPEC = builder.build();
    }
}
