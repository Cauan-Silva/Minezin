package com.cauansilva.arcanecontrol.core.cid;
import java.util.*;
/** Per-target ownership: one Atomic ending must not release a target held by another. */
public final class FontesStunAtomic {
    private final Map<UUID,Long> fontes=new HashMap<>();
    public void renovar(UUID fonte,long agora){fontes.put(fonte,agora+2);}
    public void remover(UUID fonte){fontes.remove(fonte);}
    public boolean ativa(long agora){fontes.values().removeIf(fim->fim<=agora);return !fontes.isEmpty();}
}
