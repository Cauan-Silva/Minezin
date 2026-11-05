---
tags: [sistema, vfx]
---
# Efeitos visuais (VFX)

- Volumes 3D (malhas com luz e borda), nunca sprites planos.
- Geometria de cada efeito é uma **classe pura** em `core/vfx/` (sem Minecraft), presa aos ticks do servidor; testes
  exportam `build/*-samples.json` para as [[Prévias Offline]].
- Desenho com o shader `anime_energy.fsh` (vários modos de material) e Photon para bloom de acento.
- Ultimates com câmera cinematográfica (desligável na árvore).
- Render: `core/<kit>/Efeitos*.java`, `client/render/*` (`RenderDespertar`, `UltimatesTransformadas`).
- Documentos: [[rework-vfx-3.5-preview]], [[vfx-referencias-sfx-preview2]].
