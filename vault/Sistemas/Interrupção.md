---
tags: [sistema, combate]
---
# Sistema de interrupção (aprovado 05/11/2026, núcleo implementado)

É um sistema comum a todos os kits (jogadores e [[Bosses Nemesis]]). Ele existe primeiro para o G-X Whirlpool e o G-C Soco Relâmpago do [[Killua]].

## Peças
- **Canalização:** toda técnica com preparo, carga ou canal se registra ao começar, com dono, técnica, início, fim, resistência e uma função para cancelar. Hoje cada kit faz isso sozinho, como o `cancelar` do Atomic do [[Cid]].
- **Força do golpe que interrompe:**
  - LEVE: golpes comuns e técnicas sem a marca de interrupção;
  - FORTE: técnicas marcadas, como o Soco Relâmpago, stuns e o Narukami;
  - ABSOLUTA: paralisia e ultimates.
- **Resistência da canalização:**
  - NENHUMA: cai com LEVE;
  - FIRME: cai só com FORTE (é o padrão das técnicas);
  - SUPERARMADURA: cai só com ABSOLUTA (ultimates, G de chefes);
  - IMUNE: cutscenes.
- **Janela de contra-ataque:** o dono abre uma guarda por N ticks (Whirlpool: 10). Se receber um golpe dentro dela, o dano é anulado, o atacante tem a canalização dele interrompida e o contra-ataque dispara.
- **Ao interromper:**
  - a técnica para tudo de uma vez: VFX, som, animação e dano;
  - ela entra em 50% da recarga;
  - o alvo fica 1 s imune a uma nova interrupção (sem prender em loop);
  - aparece um efeito comum de "quebra" (estilhaço azul e branco), com som próprio.

## Código
- `core/combate/Interrupcao` em Java puro (regras, forças, janelas, imunidade e `recargaPenalizada`); testes em `InterrupcaoTest`.
- `core/combate/InterrupcaoServidor` é a ponte Forge. Os kits chamam `iniciar`, `concluir`, `guardar` e `interromper`. Qualquer dano vindo de uma entidade viva conta como golpe LEVE. A guarda anula o golpe no `LivingAttackEvent`. O efeito de quebra ainda é provisório (faíscas e o som de escudo quebrando), até ter a prévia própria.
- Ainda falta: ligar os kits (os antigos registram as próprias canalizações aos poucos; o Killua vem primeiro, com G-X e G-C) e fazer a prévia do efeito de quebra.
