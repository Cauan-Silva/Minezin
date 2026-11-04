# Padrão de trajes (referência: Ichigo e Cid)

Os trajes do Ichigo (Shihakushō, casaco do Bankai e forma Mugetsu) foram aprovados como **o padrão de qualidade
para todos os personagens**, e o traje do Cid (Shadow) foi o primeiro feito do zero nele, já com a superfície lisa
(seção 4). Todo traje novo segue este documento. A arquitetura geral do tecido está em
[tecido-arquitetura.md](tecido-arquitetura.md).

## 1. Pipeline

| Etapa | Arquivo | O que faz |
|---|---|---|
| Geometria | `tools/build_ichigo_traje.py`, `tools/build_cid_traje.py` | Monta o esqueleto (`a.skeleton()`), cubos (`c(...)`) e ossos de tecido por estilo (`shihakusho()`, `bankai()`, `mugetsu()`). |
| Paleta | `tools/clothing_uv.py` | Uma paleta por estilo (`ichigo`, `ichigo_bankai`, `ichigo_mugetsu`), trocando os tons-base (BLACK, STEEL, INK, SHADOW, WHITE, LEATHER). |
| Textura | `_exportar(style,bones)` | `polish()` + `clothing_uv`: SVG → textura rasterizada 128 px. |
| Registro | `tools/build_armor_models.py` | O estilo entra na lista `document()`. |
| Render | `core/ichigo/RenderBankaiIchigo.java`, `core/cid/RenderTrajeCid.java` | `GeoArmorRenderer`; aplica a pose do tecido aos ossos de `TecidoIchigo.OSSOS`, lê os painéis já posados e desenha a superfície lisa (`SuperficieTecido`). |
| Física | `core/client/tecido/TecidoIchigo.java` + `core/ichigo/TecidoIchigoCliente.java` | Simula e amostra o tecido por tick (com `parcial`). |
| Prévia | `tools/preview_ichigo_traje.py --estilo <estilo>` | Frente, costas e lado, para aprovar antes do jogo. A malha lisa sai de `tools/superficie_tecido.py`, gêmeo em Python de `SuperficieTecido.java`. |
| Testes | `TecidoIchigoTest`, `GolaCidTest`, `SuperficieTecidoTest` | Ossos batem com o geo, tecido não estica, repouso estável, folha sem costura aberta, sem torcer em movimento, colisão com as pernas, exporta amostras para a prévia. |

Gerar tudo: `python -B build_ichigo_assets.py` em `tools/` (chama `build_ichigo_traje`).

## 2. Regras de modelagem

- **Silhueta justa.** O traje é colado ao corpo: espessura mínima sobre a pele (≈0,06–0,12 px de folga). Trajes
  "gordos" foram reprovados. `polish()` já alarga um pouco, então detalhes finos (faixas, listras) ficam
  levemente **por fora** da peça polida para não sumirem.
- **Camadas de detalhe.** Cada peça tem base + borda/sombra + detalhe:
  - Kosode com gola cruzada, obi, correia da espada, mangas com barra (`_mangas(barra)`), hakama com pregas, sandálias.
  - Bankai: mangas largas de tecido (`_mangas_tecido`), cauda com borda (`_caudas(b, borda_branca)`).
  - Mugetsu: faixas em voltas sobrepostas de largura e tom variados (`_faixa(...)`): frente e costas giradas, laterais retas.
- **Sem z-fighting.** Peças que se sobrepõem recebem deslocamentos de 0,03–0,06 px.
- **Troca total de forma.** Em uma transformação completa (Mugetsu), todos os outros trajes anime ficam escondidos
  (`RenderTrajeAnime` checa `EfeitosIchigo.mugetsu`) e só o traje da forma é renderizado.
- **Cabelo fora.** Quando o cabelo não fica bom, remova-o e invista nos detalhes da roupa (decisão do Mugetsu).

## 3. Tecido (cloth): tudo é uma peça só

Este é o ponto que elevou o nível. A regra é: **nenhum pedaço de tecido se move sozinho; tudo está ligado.**

### Estrutura da cauda/saia (`_caudas`)
- **Costas**: malha 6 colunas × 6 segmentos (`BK_PAINEIS`) presa na cintura. Física real de partículas
  (`MalhaTecido(6, 7, 1.46/16, 1.85/16)`, PBD).
- **Laterais** (`BK_LADOS`): seguem a coluna externa das costas, segmento a segmento (`lado_esq` → coluna 5,
  `lado_dir` → coluna 0).
- **Quinas arredondadas**: `quina_*` (costas ↔ lado) e `quinaf_*` (lado ↔ frente), giradas 45°, para a borda não
  formar ângulo reto.
- **Frente aberta** (`frente_*`) ou **frente fechada** (`mg_saia_frente_meio_*`, Mugetsu): a frente fechada segue
  a média das duas colunas externas, então frente, lados e costas balançam juntos.
- **A frente aberta e a quina da frente têm exatamente a pose da lateral** (`TecidoIchigo.lateral`). Qualquer limite
  ou abertura própria da frente separava o pano em abas; quem afasta o pano das pernas é a colisão da superfície
  (seção 4).
- Nomes dos ossos: `bk_capa_{painel}_{coluna}_{segmento}`. Cada segmento é filho do anterior (cadeia), e a
  rotação de cada osso é **relativa ao pai**.

### Peças com mola (`MolaMantoCid`)
- Mangas largas (`bk_manga_{esq|dir}_{seg}`, 3 segmentos): mola amortecida de 4 estágios, **amostrada de forma
  contínua** ao longo dos segmentos (curva suave, sem dobra). Recebe a variação do balanço do braço, para a
  manga "arrastar" atrás do braço.
- Pontas soltas leves (`mg_faixa_{col}_{seg}`, 2 × 6): mola mais rápida, ganho maior, levantam ao cair.

### Movimento ("animar ao máximo")
- **Agitação** cresce com velocidade, giro de corpo, queda e impulso, e decai suavemente.
- **Onda viajante** (`onda()`): vai da cintura à barra, defasada por coluna, mais forte nos segmentos de baixo;
  soma de uma ondulação lenta e uma vibração rápida, para nunca parecer um seno único.
- **Vento** (`TecidoAsta.vento`) sempre ativo, mesmo parado.
- **Subida**: ao cair, a barra sobe.
- **Impulso**: eventos (revelação do Bankai, `impulso(0.9)`) jogam o tecido para fora e ele assenta sozinho.
- **Limites**: todas as rotações são limitadas (`clamp`) e o teste garante repouso < 0,4 rad e alongamento < 0,08.
- Liga e desliga por config (`ConfigCliente.TECIDO_ICHIGO`).

### Acessórios das armas
Faixa e corrente seguem o mesmo princípio (`TecidoEspadaIchigo`): cadeia de 4 ossos (`cloth`, `cloth_1..3` /
`chain`, `chain_1..3`), movidos pela câmera (yaw/pitch), pelo balanço da caminhada, pelo ataque e pelo movimento.

## 4. Superfície lisa e colisão (`SuperficieTecido`)

O GeckoLib só desenha blocos. Para o pano não parecer feito de pedaços soltos, os ossos de tecido continuam com a
física, mas **não são desenhados como blocos**: o renderizador lê onde cada painel ficou e desenha uma malha única.

- **Leitura dos painéis** (`CapturaVertices`): o primeiro cubo de cada osso `bk_capa_*` é passado ao GeckoLib com um
  consumidor que só registra onde os vértices cairiam. Nada é desenhado.
- **Cantos fixos**: os quatro cantos de cada painel (topo/base × início/fim) são marcados uma vez, pela pose parada
  (`rotular`), e nunca trocam com o movimento. Adivinhar pela distância a cada quadro torcia o pano.
- **Costura**: as peças são unidas numa ordem fixa em volta do corpo (`ORDEM`: frente direita, quina, lateral,
  quina, costas, ..., frente esquerda). Cada emenda é a média dos dois vizinhos, então não existe fresta.
- **Malha mais fina** (`subdividir`): cada painel vira 3 × 2 células, com curva Catmull-Rom em volta do corpo
  (redonda e passando por todas as emendas) e reta na descida, para a colisão poder dobrar o pano em volta da perna.
- **Linhas suavizadas** (`suavizar`): cada linha em volta do corpo é alisada, só na dobra que não existe na pose
  parada, então o anel nunca encolhe para dentro das pernas. A linha da cintura fica presa e as bordas da frente
  aberta ficam como estão.
- **Barra rasgada**: cada coluna termina em dois dentes de tamanhos diferentes com um recorte no meio.
- **Colisão com as pernas** (`caixa`, `colidir`): as pernas, já posadas, viram duas caixas a cada quadro, e todo
  ponto do pano dentro de uma delas sai pela lateral mais próxima, com folga. Nunca para cima ou para baixo.
  Limite: só os pontos da malha colidem (agora mais densa); um pedaço do pano entre dois pontos ainda pode
  atravessar, e o pano não colide com o cenário nem com o resto do corpo.
- **Gola curva** (`gola`, Cid): uma casca lisa que sai do contorno do tronco, vira um quadrado arredondado em volta
  da cabeça e abre até a borda redonda; preta por fora, roxa por dentro, debrum dourado. As cores vêm de três cubos
  minúsculos de paleta no osso `cid_gola`, que não são desenhados.
- **Prévia igual ao jogo**: `tools/superficie_tecido.py` repete a mesma conta. Ao mudar um, mude o outro.
- Detalhes que ficam bons retos (colete, lapelas, mangas, cinto, botas, debrum do Bankai) continuam em blocos.

## 5. Checklist para um traje novo

1. Função de estilo no `build_<personagem>_traje.py`, silhueta justa, três camadas de detalhe.
2. Paleta própria em `clothing_uv.py`; estilo adicionado em `build_armor_models.py`.
3. Toda peça solta é osso em cadeia; painéis laterais e frente **seguem a malha das costas**; quinas arredondadas.
4. Peças leves (mangas, faixas, cabelo) em `MolaMantoCid`, amostradas continuamente.
5. Onda viajante + agitação + vento + subida + impulso nos eventos do personagem.
6. Casaco, capa ou saia desenhados pela superfície lisa (seção 4), com colisão com as pernas; formas curvas como
   golas também viram superfície, não degraus de blocos.
7. Teste: ossos batem com o geo, sem esticar, repouso estável, folha sem costura aberta, sem torcer em movimento.
8. Prévia (frente/costas/lado + vídeo do tecido) aprovada antes de gerar o JAR.
