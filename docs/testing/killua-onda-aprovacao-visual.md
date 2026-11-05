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
| 1 | G Godspeed (entrada + aura) | ⏳ | | |
| 2 | Kit base Z/X/C/V (pedido do usuário: base inteira primeiro) | prévia 2 enviada 05/11 (prévia 1: "VFX simples demais") | `minezin-outputs/killua-base/killua-base.mp4` + `killua-base-fases.png` | aguardando |
| 6 | G-Z / G-X / G-C / G-V | ⏳ | | |
| 7 | Traje (camisa, short) + cabelo | ⏳ | | |
| 8 | SFX | ⏳ | | |
| 9 | JAR + roteiro | ⏳ | | |

## Pendências e riscos

| Prioridade | Situação | Próxima ação |
| --- | --- | --- |
| Alta | G-X e G-C dependem de um sistema de interrupção comum a todos os kits | Desenhar antes da onda 6 |
| Média | Referências de cena ainda não coletadas | Wiki de HxH ou vídeo do usuário |

## Kit base — prévia 1 (05/11/2026)
- Geometria: `core/vfx/KilluaBase` (Narukami, Echo, Yoyo, Garras); teste `KilluaBaseTest` exporta `build/killua-base-samples.json`.
- Animações: `player_animation/killua.json` (`killua_narukami`, `killua_echo`, `killua_ioio`, `killua_garras`).
- Prévia: `python -B tools/preview_killua_base.py --output ../minezin-outputs/killua-base`.
- Ticks: Z carga 8, avanço 4, golpe 12, paralisia 20 (fim 40) · X teleporte 14, 4 blocos (fim 32) · C lança 6, giro 20, recolhe 8 (fim 38) · V some 8, furtivo 40, golpe 48, sangramento 30 (fim 78).
- Limites: boneco com traje provisório (do Ichigo), sem SFX, terreno ou luz do Minecraft.
- Prévia 3 (05/11/2026): só o Narukami refeito pela referência do usuário (salto 10, estrela até 15, descarga 15, impacto 16, paralisia 20, pouso 32, fim 46). X, C e V seguem da prévia 2.
