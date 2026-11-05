# Killua Zoldyck — onda de qualidade

Iniciada em 05/11/2026, sobre a `main` na versão `3.14.0-subaru-preview.9` (protocolo 17). Kit novo: nada implementado.
**Publicação:** pendente. Teste no Minecraft: pendente.

## Referências

| Técnica | Referência do anime (HxH 2011) | Canônica? | Observações |
| --- | --- | --- | --- |
| Z Palma Relâmpago | Narukami: vídeo do usuário (YouTube uF7RDvh7PE, 00:20–00:35) | sim | Salto alto, estrela de relâmpago na mão erguida, coluna vertical de raios descendo até o alvo, explosão radial; arcos azul-violeta no alvo |
| X Passo Borrão | Rhythm Echo (ep. 22, vs. Johness / ep. 117) | sim | Pós-imagens; sem eletricidade |
| C Yo-Yo de Aço | Yo-yos (ep. 21-22, Torre Celestial) | sim | Dois yo-yos de 50 kg; giro em arco |
| V Garras de Assassino | Mão em garra (ep. 1-4, vs. Johness) | sim | Unhas alongadas; sangramento é do GDD |
| G Godspeed | Kanmuru / Godspeed (ep. 116-118) | sim | Cabelo eriçado, aura elétrica contínua |
| G-Z Velocidade Relâmpago | Speed of Lightning (ep. 117) | sim | |
| G-X Turbilhão Elétrico | Whirlwind (ep. 117) | sim | Reação automática ao ataque |
| G-C Soco Crítico | sem nome próprio | não | Golpe do GDD; usar soco no modo Godspeed |
| G-V Sobrecarga | sem equivalente direto | não | Inspirar em descarga do Narukami em área |

## Ondas internas (uma técnica de cada vez)

| Ordem | Parte | Status | Prévia | Aprovação |
| --- | --- | --- | --- | --- |
| 1 | G Godspeed (entrada + aura) | aura de tecido aprovada 05/11 (padrão mínimo de auras) | `minezin-outputs/killua-aura-tecido/` | ✅ aura |
| 2 | Kit base Z/X/C/V (pedido do usuário: base inteira primeiro) | prévia 2 enviada 05/11 (prévia 1: "VFX simples demais") | `minezin-outputs/killua-base/killua-base.mp4` + `killua-base-fases.png` | aguardando |
| 6a | G-Z Relâmpago Perfurante | prévia 3 aprovada 05/11 (anim `killua_relampago`, marca queimada) | `minezin-outputs/killua-relampago-perfurante/` | ✅ |
| 6b | G-X Whirlpool | prévia 1 aprovada 05/11 (anim `killua_whirlpool`, usa `Interrupcao`) | `minezin-outputs/killua-whirlpool/` | ✅ |
| 6c | G-C Soco Relâmpago | prévia 2 aprovada 05/11 (anim `killua_soco` fluida; FORTE no `Interrupcao`) | `minezin-outputs/killua-soco-relampago/` | ✅ |
| 6d | G-V Pulso Eletromagnético | prévia 1 aprovada 05/11 (anim `killua_pulso`; FORTE em área) | `minezin-outputs/killua-pulso-eletromagnetico/` | ✅ |
| 6e | Ultimate Tempestade Divina (G no Godspeed) | prévia 1 aprovada 05/11 (anim `killua_tempestade`, ABSOLUTA, encerra o Godspeed) | `minezin-outputs/killua-tempestade-divina/` | ✅ |
| 7 | Traje (camisa, short) + cabelo | ⏳ | | |
| 8 | SFX | ⏳ | | |
| 9 | JAR + roteiro | ⏳ | | |

## Pendências e riscos

| Prioridade | Situação | Próxima ação |
| --- | --- | --- |
| Feito | Sistema de interrupção aprovado e com núcleo implementado (`core/combate/Interrupcao`) | Usar no G-X e no G-C |
| Média | Referências de cena ainda não coletadas | Wiki de HxH ou vídeo do usuário |

## Kit base — prévia 1 (05/11/2026)
- Geometria: `core/vfx/KilluaBase` (Narukami, Echo, Yoyo, Garras); teste `KilluaBaseTest` exporta `build/killua-base-samples.json`.
- Animações: `player_animation/killua.json` (`killua_narukami`, `killua_echo`, `killua_ioio`, `killua_garras`).
- Prévia: `python -B tools/preview_killua_base.py --output ../minezin-outputs/killua-base`.
- Ticks: Z carga 8, avanço 4, golpe 12, paralisia 20 (fim 40) · X teleporte 14, 4 blocos (fim 32) · C lança 6, giro 20, recolhe 8 (fim 38) · V some 8, furtivo 40, golpe 48, sangramento 30 (fim 78).
- Limites: boneco com traje provisório (do Ichigo), sem SFX, terreno ou luz do Minecraft.
- Prévia 3 (05/11/2026): só o Narukami refeito pela referência do usuário (salto 10, estrela até 15, descarga 15, impacto 16, paralisia 20, pouso 32, fim 46). X, C e V seguem da prévia 2.

## Godspeed (G) — prévia 1 (05/11/2026)
- Geometria: `core/vfx/KilluaGodspeed`; teste `KilluaGodspeedTest` exporta `build/killua-godspeed-samples.json`; animação `killua_godspeed`.
- Ticks: carga 0-14 (arcos nos braços e no chão), ativação 14 (coluna, estouro radial, ondas), aura contínua até 600 (30 s).
- Prévia: `python -B tools/preview_killua_godspeed.py --output ../minezin-outputs/killua-godspeed`.
- Decisões do usuário em 05/11: G do Subaru continua o Contrato; passivas para todos; kits passam por aprovação antes de seguir.
- Godspeed prévia 1 reprovada (aura feia; não mexer no cabelo em transformações). Versão volumétrica (cascas) também reprovada.
- **Protótipo de aura de tecido (05/11/2026):** `core/client/tecido/MalhaAura` (base presa ao corpo, flutuação, turbulência, pulso de chama; perfil `PerfilAura`) + `core/vfx/AuraTecidoKillua` (14 línguas + raios nas bordas). Teste `AuraTecidoKilluaTest` simula parado, corrida, salto e parada e exporta `build/killua-aura-tecido-samples.json`. Prévia: `python -B tools/preview_killua_aura_tecido.py --output ../minezin-outputs/killua-aura-tecido`. Se aprovado, vira o padrão de auras de todos os personagens.

## G-Z Relâmpago Perfurante — aprovado (05/11/2026)
- Geometria: `core/vfx/KilluaRelampagoPerfurante` (preparo 7 t, disparo 4 t por 9,5 blocos, golpe no tick `GOLPE`, paralisia de 1 s, rastro, marca queimada com brasas e fumaça).
- Teste `KilluaRelampagoPerfuranteTest` exporta `build/killua-relampago-perfurante-samples.json`; animação `killua_relampago` (corpo inteiro, inclinação no osso `body`).
- Prévia: `python -B tools/preview_killua_relampago_perfurante.py --output ../minezin-outputs/killua-relampago-perfurante`.
- Feedback: prévia 1 "faltou animação no golpe" → animação de corpo inteiro e pós-imagens; faixa cinza → marca queimada irregular.

## G-X Whirlpool — aprovado (05/11/2026)
- Geometria: `core/vfx/KilluaWhirlpool` (guarda de 10 t com cúpula de arcos; se atingido some num raio, reaparece atrás do atacante e dá um chute giratório com espiral de raios; atacante interrompido FORTE, arremessado e paralisado).
- Teste `KilluaWhirlpoolTest` (inclui o fluxo do `Interrupcao`) exporta `build/killua-whirlpool-samples.json`; animação `killua_whirlpool`.
- Prévia: `python -B tools/preview_killua_whirlpool.py --output ../minezin-outputs/killua-whirlpool`.

## G-C Soco Relâmpago — aprovado (05/11/2026)
- Geometria: `core/vfx/KilluaSocoRelampago` (carga no punho 8 t, avanço 2,1 blocos, impacto FORTE: cone de raios, anéis, rachaduras, arremesso). Teste `KilluaSocoRelampagoTest`; animação `killua_soco` com canal `position` do `body` e easing `easeOutBack`.
- Feedback: prévia 1 "personagem muito duro" → antecipação, carga com tremor, overshoot, follow-through, volta à guarda, quique e inclinação lateral.

## G-V Pulso Eletromagnético — aprovado (05/11/2026)
- Geometria: `core/vfx/KilluaPulsoEletromagnetico` (carga 16 t com linhas de campo e núcleo no peito; esfera de raios até 7 blocos em 9 t, onda no chão; cada inimigo tocado leva um raio, é interrompido FORTE e atordoado). Teste `KilluaPulsoEletromagneticoTest`; animação `killua_pulso`.
- Prévia: `python -B tools/preview_killua_pulso_eletromagnetico.py --output ../minezin-outputs/killua-pulso-eletromagnetico`.

## Ultimate Tempestade Divina — aprovada (05/11/2026)
- Ativação: G de novo durante o Godspeed; encerra a forma. Força ABSOLUTA no `Interrupcao`.
- Geometria: `core/vfx/KilluaTempestadeDivina` (prende 8 t; 12 teleportes a cada 3 t com gaiola de raios e pós-imagens; sobe 15 blocos; mergulho como coluna de raios; impacto com explosão, anéis, cratera e paralisia). Teste `KilluaTempestadeDivinaTest`; animação `killua_tempestade` (48 chaves por osso).
- Prévia: `python -B tools/preview_killua_tempestade_divina.py --output ../minezin-outputs/killua-tempestade-divina`.
