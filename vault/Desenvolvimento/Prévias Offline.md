---
tags: [dev, vfx]
---
# Prévias offline

Cada técnica, traje e som é aprovado por prévia gerada com os dados reais dos testes (moderngl + PyAV), rodando
`python -B <script>` de dentro de `tools/`. Saída: vídeo `.mp4` + painel de fases `.png` em `minezin-outputs/`.

- Trajes: `preview_ichigo_traje.py --estilo shadow|ichigo_bankai|ichigo_mugetsu`.
- Tecido: `preview_frieren_cloth.py`, `preview_gojo_cloth.py`, `preview_asta_capa.py`.
- Subaru: `preview_subaru_shamak.py`, `_escudo`, `_instrucao`, `_recuperacao`, `_contrato`, `_minya`;
  `preview_beatrice.py`.
- Sons: `build_<kit>_sfx.py --preview <wav>`.

**Limites:** não mostram terreno, skin real, primeira pessoa, shader packs, atenuação espacial nem colisão com o
cenário.
