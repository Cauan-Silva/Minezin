---
tags: [documento]
---
# Arquitetura de animação e física de tecidos

Visão geral de como as roupas se mexem no Minezin. As regras para criar um traje novo estão em
[[padrao-trajes|padrao-trajes.md]].

O sistema é híbrido: o formato vem do modelo em blocos (Blockbench/GeckoLib, gerado pelos scripts `tools/build_*_traje.py`)
e o movimento vem do código. A física precisa rodar a cada tick e reagir ao jogo, então fica em Java.

## 1. As três camadas

| Camada | Como funciona | Onde é usada | Vantagem / limite |
|---|---|---|---|
| Quadros-chave | Rotações gravadas em `animations/*.animation.json`, interpoladas pelo GeckoLib. | Balanço parado das mangas do Shihakushō. | Fácil e visual; sempre igual, não reage ao jogo. |
| Molas (`MolaMantoCid`) | Cada osso é uma mola amortecida de 4 estágios, empurrada pela inércia do jogador a cada tick; a ponta atrasa em relação à base. | Mangas do Bankai, alças da gola do Cid, faixas do Mugetsu, faixa e corrente das espadas (`TecidoEspadaIchigo`). | Leve e reativa; peças vizinhas podem se separar. |
| Tecido de partículas (`MalhaTecido`) | Grade de partículas presa na cintura, com gravidade, vento e inércia; as distâncias entre vizinhas são restauradas várias vezes por tick (PBD, Position Based Dynamics). | Costas dos casacos do Cid e do Ichigo (6 × 7 partículas). | As colunas se puxam e o pano age como uma peça só; custa mais. |

## 2. Do modelo à tela

1. **Geometria** (`tools/build_cid_traje.py`, `tools/build_ichigo_traje.py`): gera o `.geo.json` com os ossos nomeados para a
   física, por exemplo `bk_capa_costas_3_2` (costas, coluna 3, segmento 2), cada segmento filho do anterior.
2. **Física a 20 Hz** (`TecidoIchigo`, `TecidoIchigoCliente`, `MantoCidCliente`): lê o movimento do jogador, simula as costas
   como partículas; laterais, quinas e frente seguem as colunas da borda das costas; mangas e faixas usam molas.
3. **Animação extra**: onda que desce da cintura à barra, agitação ao correr, girar e cair, subida da barra na queda e impulso
   das habilidades (revelação do Bankai, golpes do Cid via `EfeitosCid.tecido`).
4. **Interpolação**: a física roda a 20 Hz e o jogo desenha a 60 Hz ou mais; a pose é interpolada pelo `partialTick`.
5. **Renderizador** (`RenderTrajeCid`, `RenderBankaiIchigo`): soma a rotação de cada osso antes de desenhar e devolve o valor
   original depois, para a física nunca alterar o modelo base.
6. **Superfície lisa** (`SuperficieTecido`): os ossos de tecido não são desenhados como blocos; os painéis posados viram uma
   malha única costurada, mais fina e suavizada, com barra rasgada e colisão com as pernas. A gola do Cid é uma casca curva.
   Detalhes em [[padrao-trajes|padrao-trajes.md, seção 4]].

A prévia em Python (`tools/preview_ichigo_traje.py` com `tools/superficie_tecido.py`) usa as mesmas amostras de física dos
testes e a mesma geometria da superfície, para aprovar antes de abrir o jogo.

## 3. Estado por personagem

| Personagem | Física | Desenho |
|---|---|---|
| Cid (Shadow) | `TecidoIchigo` (casaco e alças) | Superfície lisa, gola curva, colisão com as pernas |
| Ichigo (Bankai, Mugetsu) | `TecidoIchigo` | Superfície lisa e colisão; debrum e saia da frente do Mugetsu em blocos |
| Asta | `TecidoAsta` (manto), `DevilUnionTecido` (asas e cauda) | Manto liso aberto na frente (`SuperficieTecido.aberto`); asas e cauda em blocos |
| Frieren | `TecidoFrieren` | Saia e capinha lisas, em anel fechado (`SuperficieTecido.anel`) |
| Gojo | `TecidoGojo` | Barra do casaco lisa, em anel fechado; gola em blocos |

Frieren, Gojo e Asta usam o mesmo ajudante, `AneisLisos`: o renderizador só diz quais ossos são pano.

## 4. Colisão e comportamento por traje

- **Pernas** (`SuperficieTecido.caixa`): as duas pernas posadas viram caixas e o pano sai pela lateral mais próxima.
- **Cenário** (`ColisaoCenario`): os blocos do mundo (chão, lajes, paredes, cercas) também empurram o pano, para cima
  ou para os lados, nunca para baixo. Só liga quando o traje é desenhado no mundo: o código confere que as pernas
  posadas estão onde o jogador está, então no inventário ou numa prévia ele desliga sozinho.
- As duas colisões mexem só na superfície desenhada, não na física, então o pano não treme.
- **Peso e rigidez por traje** (`PerfilTecido`): `assets/arcanecontrol/tecido/<estilo>.json` com `peso`, `rigidez`,
  `arrasto`, `inercia` e `mola`. Cada valor multiplica o comportamento atual (1 = igual) e é limitado a uma faixa
  segura; arquivo ausente ou quebrado dá o padrão. Hoje existem `shadow`, `ichigo_bankai`, `frieren`, `gojo` e `asta`,
  todos em 1.

  ```json
  { "peso": 2.0, "rigidez": 1.0, "arrasto": 1.4, "inercia": 0.7, "mola": 1.0 }
  ```
  Exemplo de capa pesada de armadura: cai mais reta, balança menos e assenta mais rápido.

## 5. Melhorias futuras

- **Desempenho com muitos jogadores**: cada jogador com traje roda a própria simulação (até 64 ao mesmo tempo). Medir o FPS
  com vários jogadores de capa no mesmo lugar antes de otimizar; se cair, agrupar os cálculos e partir o espaço.
- **Colisão na física**: hoje as colisões ajustam só o desenho. Levar o cenário para dentro da `MalhaTecido` faria o pano
  realmente se apoiar no chão (deitado, agachado), ao custo de mais cálculo.
- **Asas e cauda do Devil Union** como superfície lisa, se ficar melhor que os blocos.
