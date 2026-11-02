package com.cauansilva.arcanecontrol.core.audio;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.*;
public final class RegistroSons {
    public static final DeferredRegister<SoundEvent> SONS=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,"arcanecontrol");
    public static final String[] NOMES=CatalogoSons.NOMES;
    private static final java.util.Map<String,RegistryObject<SoundEvent>> EVENTOS=new java.util.HashMap<>();
    static{for(String nome:NOMES)EVENTOS.put(nome,SONS.register(nome,()->SoundEvent.createVariableRangeEvent(new ResourceLocation("arcanecontrol",nome))));}
    public static SoundEvent obter(String nome){var e=EVENTOS.get(nome);if(e==null)throw new IllegalArgumentException(nome);return e.get();}
}
