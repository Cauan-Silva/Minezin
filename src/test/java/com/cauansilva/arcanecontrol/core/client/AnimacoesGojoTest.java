package com.cauansilva.arcanecontrol.core.client;

import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class AnimacoesGojoTest {
    private static final String ARQUIVO = "/assets/arcanecontrol/player_animation/gojo.json";

    @Test void playerAnimatorCarregaTodosOsGestos() throws Exception {
        try (var arquivo = getClass().getResourceAsStream(ARQUIVO)) {
            assertNotNull(arquivo);
            var animacoes = AnimationSerializing.deserializeAnimation(arquivo);
            assertEquals(Set.of("gojo_azul", "gojo_vermelho", "gojo_infinito", "gojo_roxo", "gojo_dominio"),
                    animacoes.stream().map(a -> a.extraData.get("name").toString()).collect(Collectors.toSet()));
            for (var animacao : animacoes) {
                assertFalse(animacao.isInfinite);
                assertTrue(animacao.endTick > 0);
            }
        }
    }

    @Test void gestosPreservamPernasETemDuracaoDoServidor() throws Exception {
        try (var arquivo = getClass().getResourceAsStream(ARQUIVO)) {
            assertNotNull(arquivo);
            var animacoes = JsonParser.parseReader(new InputStreamReader(arquivo, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("animations");
            assertEquals(85, animacoes.getAsJsonObject("gojo_roxo").get("animation_length").getAsDouble() * 20);
            assertEquals(160, animacoes.getAsJsonObject("gojo_dominio").get("animation_length").getAsDouble() * 20);
            for (var entrada : animacoes.entrySet()) {
                assertEquals(Set.of("right_arm", "left_arm", "torso"),
                        entrada.getValue().getAsJsonObject().getAsJsonObject("bones").keySet());
            }
        }
    }
}
