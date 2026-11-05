---
tags: [dev]
---
# Arquitetura do código

Raiz: `src/main/java/com/cauansilva/arcanecontrol/`.

| Área | Onde |
| --- | --- |
| Poderes (servidor) | `core/<kit>/Poderes*.java`, `core/magia/GerenciadorMagiasGojo.java` |
| Tempos compartilhados | `core/vfx/Perfil*`, `RelogioAtomic`, `UltAsta`, `MugetsuIchigo` |
| Geometria de VFX | `core/vfx/*` (classes puras) |
| Render de efeitos | `core/<kit>/Efeitos*.java`, `client/render/*` |
| Rede | `core/rede/*` → [[Rede]] |
| RPG | `core/rpg/*`, `core/progressao/*` → [[RPG e Despertar]] |
| Áudio | `core/audio/*`, `client/AudioAnime` → [[Áudio]] |
| Tecido | `core/client/tecido/*` → [[Tecido]] |
| Trajes | `core/<kit>/RenderTraje*` |
| Assets | `assets/arcanecontrol/` (`geo`, `textures`, `sounds`, `shaders`, `tecido`, `player_animation`) |
| Ferramentas | `tools/` |
| Blockbench | `art/blockbench/*.bbmodel` |

Fluxo de um traje: `build_<kit>_traje.py` gera geo + textura → física a 20 Hz → renderizador aplica a pose e desenha
a superfície lisa → prévia em Python usa a mesma conta.
