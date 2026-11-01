package com.cauansilva.arcanecontrol;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ConfigCliente {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue CINEMATICAS;
    public static final ForgeConfigSpec.BooleanValue PHOTON;
    public static final ForgeConfigSpec.BooleanValue VFX_REDUZIDOS;
    public static final ForgeConfigSpec.BooleanValue CLARAO;
    public static final ForgeConfigSpec.BooleanValue SHADER_ATOMIC;
    public static final ForgeConfigSpec.BooleanValue GOJO_VFX;
    public static final ForgeConfigSpec.BooleanValue AMBIENTE_DOMINIO;
    public static final ForgeConfigSpec.BooleanValue CLARAO_GOJO;
    public static final ForgeConfigSpec.BooleanValue GOJO_BLOOM;
    public static final ForgeConfigSpec.BooleanValue IMPACT_FRAMES;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        GOJO_BLOOM = builder.comment("Bloom de acento Photon nas tecnicas do Gojo.").define("bloomGojo", true);
        CLARAO_GOJO = builder.comment("Clarao breve na fusao do Roxo e abertura do Dominio.")
                .define("claraoGojo", true);
        GOJO_VFX = builder.comment("Materiais e composicoes novas de Azul, Vermelho, Roxo e Infinito.")
                .define("gojoVfx", true);
        AMBIENTE_DOMINIO = builder.comment("Ceu e neblina cosmicos somente dentro do Vazio Infinito.")
                .define("ambienteDominio", true);
        SHADER_ATOMIC = builder.comment("Material procedural e filamentos da Atomic. Desligar restaura a geometria anterior.")
                .define("shaderAtomic", true);
        PHOTON = builder.comment("Ativar emissores e bloom Photon da Atomic.").define("photonAtomic", true);
        VFX_REDUZIDOS = builder.comment("Reduzir a densidade dos efeitos do Gojo e da Atomic.").define("vfxReduzidos", false);
        CLARAO = builder.comment("Ativar o clarao breve da Atomic.").define("claraoAtomic", true);
        CINEMATICAS = builder.comment("Ativar câmera, zoom e tremor nas Ultimates locais.")
                .define("cinematicas", true);
        IMPACT_FRAMES=builder.comment("Impact frames breves nos ataques dos bosses; desligado tambem por vfxReduzidos.").define("impactFrames",true);
        SPEC = builder.build();
    }
}
