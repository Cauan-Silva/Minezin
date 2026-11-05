---
tags: [sistema, sfx]
---
# Áudio

- Sons feitos no motor em camadas `tools/sfx_motor.py` (síntese numpy; a partir do Subaru também gravações **CC0**
  como camada, créditos em `art/audio/cc0/fontes.json` e THIRD_PARTY_NOTICES): transiente, corpo, grave
  saturado, eco; compressão só nos picos; **3 variações** (pitch ±4%).
- Cada habilidade é dividida em momentos tocados no tick exato (`RoteiroAudio`, campos `atraso` e `vida`).
- `AudioAnime.programar` cancela o grupo anterior de mesmo nome. Atenuação máxima 64; pico < 0,99.
- Geradores: `build_gojo_sfx.py`, `build_asta_sfx.py`, `build_cid_sfx.py`, `build_ichigo_sfx.py`,
  `build_frieren_sfx.py`; `build_anime_sfx.py` só mescla o `sounds.json`.
- Caráter: Gojo cristalino, Asta sujo, Cid elegante, Ichigo rugindo, Frieren sinos.
