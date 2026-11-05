---
tags: [sistema]
---
# Rede

- `core/rede/*`: `PacoteEfeitoDespertar`, `PacoteSincronizarCutscene3D`.
- `RegistoRede.PROTOCOL_VERSION` = 16; subir quando houver efeito novo. Cliente e servidor precisam da mesma versão.
- Pacotes mandam pontos absolutos; o render subtrai a origem do efeito (ver [[Armadilhas]]).
