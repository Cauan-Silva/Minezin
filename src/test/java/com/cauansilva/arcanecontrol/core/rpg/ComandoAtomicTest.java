package com.cauansilva.arcanecontrol.core.rpg;
import org.junit.jupiter.api.Test;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import static org.junit.jupiter.api.Assertions.*;
class ComandoAtomicTest {
    @Test void comandoTemConsultaEBooleanoEstrito()throws Exception{
        var root=ComandosRpg.comandoAtomic().build();assertEquals("atomic",root.getName());
        var terreno=root.getChild("terreno");assertNotNull(terreno.getCommand());
        var arg=(ArgumentCommandNode<?,?>)terreno.getChild("ativo");assertNotNull(arg.getCommand());
        assertEquals(false,arg.getType().parse(new StringReader("false")));assertEquals(true,arg.getType().parse(new StringReader("true")));
        assertThrows(com.mojang.brigadier.exceptions.CommandSyntaxException.class,()->arg.getType().parse(new StringReader("talvez")));
    }
}
