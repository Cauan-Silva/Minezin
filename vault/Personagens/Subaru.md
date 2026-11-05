---
tags: [personagem]
---
# Subaru Natsuki (Re:Zero)

Estrategista: controle tático e suporte. Voltar para [[Personagens]]. Status: **kit completo no jogo** (kit 16,
protocolo 17, JAR `3.14.0-subaru-preview.9`), com traje (agasalho) e SFX próprios.

## Base
| Tecla | Técnica | Efeito | Classe / clipe |
| --- | --- | --- | --- |
| Z | Shamak | Orbe de sombra com núcleo, casca de fumaça e tentáculos; névoa e venda de sombra na cabeça do alvo (cegueira + confusão 3 s) | `SamakSubaru` / `subaru_shamak` |
| X | Chamado Espiritual de Emergência | Espíritos de luz formam bolha de mana com runas; absorve até 2 corações e estoura | `EscudoSubaru` / `subaru_escudo` |
| C | Instrução Tática | Pulso dourado no chão (6 blocos) dá velocidade e recarga de pulo por 5 s | `InstrucaoSubaru` / `subaru_instrucao` |
| V | Recuperação Psicológica | Fumaça de veneno/lentidão sai do corpo; coração de luz e Regeneração I por 2 s | `RecuperacaoSubaru` / `subaru_recuperacao` |

## G — Contrato com a [[Beatrice]]
| Tecla | Técnica | Status |
| --- | --- | --- |
| G | Contrato: círculo, porta da Biblioteca Proibida, a Beatrice sai e fica ao lado | Aprovado (`ContratoSubaru`, `subaru_contrato`) |
| Z | Minya: leque de 7 estacas de cristal; cada uma acerta o primeiro inimigo no caminho | No jogo (`MinyaBeatrice`) |
| X | E.M.M.: invulnerável por um instante | No jogo |
| C | Murak: peso reduzido, salto alto e queda lenta | No jogo |
| V | Ul Minya: chuva de cristais (2 de dano a cada 4 ticks na área) | No jogo |

A antiga ultimate "Autoridade da Preguiça (Mãos Invisíveis)" foi **substituída** pelo contrato.

## Arquivos
- Animações do corpo: `assets/arcanecontrol/player_animation/subaru.json`.
- Prévias: `tools/preview_subaru_*.py` (as mais novas a 40 fps).
- Servidor: `PoderesSubaru`; cliente: `EfeitosSubaru`, `RenderMagiasSubaru` (pacotes 600-605); forma de 20 s.
- Traje: `tools/build_subaru_traje.py` (jaqueta branca, mangas e barra pretas, detalhes amarelos, "N" no peito).
- Sons: `tools/build_subaru_sfx.py`, 13 sons com gravações CC0 (`art/audio/cc0/`).

Ver também: [[2026-11-04 Subaru e Beatrice]], [[Próximos Personagens]].
