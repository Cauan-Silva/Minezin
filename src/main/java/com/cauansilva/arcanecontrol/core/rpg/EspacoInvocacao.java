package com.cauansilva.arcanecontrol.core.rpg;

import net.minecraft.world.phys.AABB;

/** Folga lateral e acima da cabeca, sem invadir o piso sob os pes. */
public final class EspacoInvocacao {
    private EspacoInvocacao() {}

    public static AABB volumeLivre(AABB corpo) {
        return corpo.inflate(1, 0, 1).expandTowards(0, .1, 0);
    }
}
