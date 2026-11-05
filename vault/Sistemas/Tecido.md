---
tags: [sistema, tecido]
---
# Tecido (cloth)

- **Física:** costas em malha PBD (`MalhaTecido`); laterais e frente ligadas às costas; mangas e faixas em molas
  (`MolaMantoCid`); 20 Hz com interpolação; onda, vento, subida na queda, impulso das habilidades.
- **Desenho:** painéis posados viram malha única costurada e suavizada (`SuperficieTecido`): casaco aberto (Cid,
  Ichigo), anel fechado (saia/capinha da Frieren, barra do Gojo, saia da [[Beatrice]]) ou folha aberta (Asta).
- **Colisão:** pernas (`Caixa`/`Obstaculo`) e blocos do mundo (`ColisaoCenario`, só no render, com checagem de
  segurança).
- **Por traje:** `assets/arcanecontrol/tecido/<estilo>.json` (`PerfilTecido`: peso, rigidez, arrasto, inércia, mola).
- Gêmeo em Python: `tools/superficie_tecido.py` (mude os dois juntos).
- Documentos: [[tecido-arquitetura]], [[Padrão de Trajes]].
