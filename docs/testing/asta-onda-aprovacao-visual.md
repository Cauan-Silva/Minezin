# Asta — onda de qualidade

Iniciada em 02/11/2026, sobre a `main` em `a698e32`. Mecânicas do servidor preservadas. Alterações locais, sem commit.

Uma tentativa anterior de outra IA (não aprovada pelo usuário) foi guardada em `git stash` ("Onda Asta da outra IA (nao aprovada)"), sem ser apagada.

## Referências

Vídeos enviados pelo usuário (quadros em `minezin-outputs/refs-asta/`): Devil Union, Black Meteorite e Demon-Dweller. Linguagem comum: antimagia como **tinta preta chapada com contorno vermelho fino**, bordas rasgadas, respingos; cortes deixam rasgos negros com borda vermelha; Devil Union com estrelas e marcas vermelhas no corpo; Black Meteorite como estrela cadente negra.

## Ondas internas

| Ordem | Parte | Status |
| --- | --- | --- |
| 1 | Black Divider (C desperto, efeito 306) | ✅ aprovado em prévia |
| 2 | Golpes base Z/X/C/V | ✅ aprovado em prévia (relâmpagos negros) |
| 3 | Assimilação / Devil Union + Matadora desperta | ✅ v5 aprovada (100%, depois do JAR de teste) |
| 4 | Black Meteorite, Black Hurricane, ult (V) | Black Meteorite, Black Hurricane e espadas ✅ aprovados em prévia; ult (V) ✅ aprovada em prévia e no código (meia-lua v1 recusada e removida) |
| 5 | Roupa com tecido | ✅ capa do Touro Negro em tecido (padrão do Cid) + detalhes, aprovada em prévia |
| 6 | SFX próprios | ⏳ |
| 7 | Liebe e JAR | ⏳ |

## Black Divider — aprovado em prévia

| Ticks | Fase | Apresentação |
| --- | --- | --- |
| 0–14 | Carga | Fios de tinta da mão à ponta da lâmina, faíscas vermelhas em zigue-zague |
| 14–18 | Condensa | Aura preta irregular nas bordas da lâmina |
| 18 | Corte (tick do dano) | Meia-lua negra descendo; fenda negra de borda vermelha abre os 18 blocos em 4 ticks |
| 18–32 | Rastro | Fenda permanece, gotas de tinta sobem e caem, some |

Implementação: `core/vfx/BlackDividerAsta` (geometria pura), `EfeitosAsta.blackDivider`, material 22 em `anime_energy.fsh` (tinta antimagia, não aditivo; tinta translúcida vira cinza, por isso alpha sólido). Escala da lâmina (`PerfilDivider`) inalterada. `BlackDividerAstaTest`: corte no tick 18 = `PerfilAsta.impacto(2,true)`, fenda e gotas dentro da faixa de dano 18 × 2,2, tudo some até o tick 32; exporta `build/asta-divider-samples.json`. Prévia: `python -B tools/preview_asta_black_divider.py --output <pasta>` (fundo claro).

Verificação local: 189 testes Java. **Teste no jogo pendente.**

## Golpes base Z/X/C/V — aprovados em prévia

Primeira versão em faixas sólidas de tinta foi recusada; o usuário pediu algo "mais parecido com raios". Versão aprovada: **relâmpagos negros** (traço preto fino com contorno vermelho, material 22, mais brilho vermelho aditivo), zigue-zague com ramos curtos que muda a cada 2 ticks, seguindo cada gesto:

| Tecla | Efeito | Visual | Sincronia |
| --- | --- | --- | --- |
| Z | 300 | 5 raios abrindo em leque horizontal de 120° | fecha no tick 6, cone 120°, raio 5 |
| X | 301 | 4 raios descendo de cima ao chão + rasgo de raio no solo (2–6 blocos) | tick 8 |
| C | 302 | raios arrastando atrás na investida; 5 raios do chão no impacto | tick 10, raio 3 |
| V | 303 | 3 raios formando o arco da guarda, seguindo o olhar | 5 s |

Implementação: `core/vfx/GolpesBaseAsta` (formas, tempos, `relampago`/`ramo`), `EfeitosAsta.golpeBase`/`raios`. `GolpesBaseAstaTest`: varredura fecha no tick de dano, formas dentro das áreas do servidor, tudo some no fim. Prévia: `python -B tools/preview_asta_base.py --output <pasta>`. 192 testes Java.

## Devil Union — aprovada (v5)

Primeira versão recusada ("não ficou nada parecido"): a roupa normal aparecia por cima, estrela pequena, vermelho opaco. Referências finais do usuário: duas artes do Devil Union (corpo inteiro preto, estrela vermelha enorme e irregular no peito, veias vermelhas brilhantes em braços e pernas, mãos/garras vermelhas, cabelo preto espetado com coroa vermelha em zigue-zague, chifres, pernas com placas e espinho no joelho, asas grandes e rasgadas, cauda fina com ponta em seta, orbes pretos com borda vermelha flutuando).

Segunda versão implementada (build e 196 testes verdes), **não renderizada nem aprovada**:
- `tools/armor_reference_shapes.py::asta_forma` (142 peças) → `python -B -c "import sys;sys.path.insert(0,'tools');import build_asta_assets as b;b.forma()"` gera `models/effects/asta_forma.json` e `geo/asta/forma.geo.json`. Revestimento preto por fora da roupa (medido contra `geo/armaduras/asta.geo.json`), estrela de 8 raios, veias em zigue-zague, coroa, cabelo espetado, placas e espinho nas pernas, asas maiores, cauda.
- Vermelho `FF2A3D` desenhado em passada aditiva (brilha) em `ModeloFormaAsta`; anexos novos `left_arm`, `right_leg`, `left_leg` (`PivoFormaAsta`, `CamadaFormaAsta`, `PerfilCinematico.revelarForma`).
- Asas e cauda com movimento secundário no estilo dos trajes (pedido do usuário, referência Ronova Ruler of Death): `client/tecido/DevilUnionTecido` (molas `MolaMantoCid`, asa raiz/ponta espelhadas, cauda em cadeia, bater lento), `asta/TecidoDevilUnionCliente`, opção `tecidoAsta`; `DevilUnionTecidoTest` exporta `build/asta-devil-union-samples.json`.
- Entrada da forma em relâmpagos negros (mesmo `raios()` dos golpes base) e 6 orbes pretos de borda vermelha orbitando (`EfeitosAsta`, efeito 200).
- `AstaTest` adaptado (dois chifres, asas nos dois lados).

### Prévia da v2 (02/11/2026)

Referência de movimento: vídeo da Ronova enviado pelo usuário (quadros em `minezin-outputs/refs-ronova/`, close das asas em 15,6–18 s). Leitura: as asas não batem. Elas balançam devagar **juntas para o mesmo lado** (ciclo de ~4 s), a ponta acompanha a raiz com atraso (onda raiz → ponta) e há várias lâminas em camadas.

Ajustes feitos para a prévia:
- `DevilUnionTecido`: balanço ocioso compartilhado (`BALANCO=.08`/tick, ~3,9 s; 0,09 rad na raiz e 0,11 rad na ponta, com atraso de fase), respiração espelhada reduzida (0,05 → 0,03) e cauda em onda de chicote crescendo até a ponta. `ModeloFormaAsta`: bater de abertura em Y reduzido (5° → 2°). Novo teste `repousoBalancaComoRonova` e exportação com 80 ticks de repouso (300 no total).
- **Bug corrigido:** `PivoFormaAsta` não conhecia `left_arm`, `right_leg` nem `left_leg` (lançava exceção), então os anexos novos não renderizariam no jogo. Agora usa os pivôs do `HumanoidModel` (braços ±5,2; pernas ±1,9,12). `PivoFormaAstaTest` cobre isso.
- Garras e mãos vermelhas ficavam bege contra o céu (vermelho aditivo sobre fundo claro). Agora as peças vermelhas ganham uma base opaca `5A0A16` na primeira passada, e o brilho aditivo vem por cima.
- A cauda aparecia em pedaços: a cadeia usava constantes fixas (0,75/0,62) que não batiam com os ângulos. Agora cada elo parte do fim do anterior (seno/cosseno do ângulo), com a ponta em losango (`armor_reference_shapes.asta_forma`; `asta_forma.json` e `forma.geo.json` regerados, `art/blockbench/asta_black.bbmodel` regerado, porque estava desatualizado desde a v2).

Prévia: `python -B tools/preview_asta_devil_union.py --output <pasta>`. Gera `devil_union_v2_painel.png` (frente/costas/lado/3/4 costas) e `devil_union_v2_movimento.mp4` (15 s: repouso, corrida, passo lateral, giro, salto, queda, pouso). Usa as mesmas peças e a mesma cadeia de transformações de `ModeloFormaAsta`, cor chapada (material 8) e vermelho aditivo sobre base escura, em céu claro. Limites: sem skin (no jogo o rosto é a pele do jogador), sem entrada em relâmpagos, sem orbes, sem animação real do jogador (só um balanço simples de membros na corrida), sem iluminação nem shader packs.

**Silhueta afinada (pedido do usuário: "ficou tão grosso assim?").** O revestimento envolvia a roupa com folga: tronco com 9,6 × 6,5 px contra 8 × 4 do jogador, braços com 14,6 px de comprimento e um bloco extra no grimório. Decisão do usuário: **esconder a roupa durante a forma**.
- `RenderTrajeAnime.renderToBuffer` pula cada peça da roupa enquanto a parte que ela cobre está 100% preta (`PerfilCinematico.trajeCoberto`: cabeça → head; peito → body + braços; calças → body + pernas; botas → pernas). Na entrada e no fade de saída, a roupa continua visível por baixo, sem buraco.
- O revestimento cola no corpo do jogador, com +0,25 px (a mesma folga da segunda camada da skin). Estrela, veias, coroa, placas e espinho foram reposicionados nas novas faces. Asas e raiz da cauda foram aproximadas das costas (z −1,2 px; pivô da asa `.25 → .175`, `RAIZ_CAUDA` z 3 → 2,4).
- Testes: `AstaTest.devilUnionColaNoCorpoDoJogador` (cada revestimento no máximo 0,3 px além do corpo vanilla) e `PerfilCinematicoTest.trajeSoSomeQuandoAFormaCobreAParteInteira`. O painel da prévia ganhou uma quinta vista: forma ao lado de um jogador normal em cinza.
- Limite: armaduras vanilla ou de outros kits **não** são escondidas e vão atravessar o revestimento colado.

Verificação da v2: 199 testes Java e 3 testes Blockbench. **A v2 foi recusada.**

### v3 — refeita no padrão dos trajes (02/11/2026)

O usuário recusou a v2 pelos motivos abaixo, e cada um teve uma correção:

| Recusa | Correção |
| --- | --- |
| "Calda toda desconectada" | Na v2, cada elo girava sozinho em volta da raiz. Agora a cauda é uma cadeia de ossos ligados (`du_cauda_0..7`, cada um filho do anterior), então não abre vão |
| Asas tinham que bater | Bater de verdade: descida rápida (40% do ciclo) e recuperação lenta. Ciclo de 44 ticks parado e 16 ticks no ar, com força conforme o esforço |
| Tecido fluido como a Ronova | Asas feitas de 5 lâminas por lado (largas no meio, afiladas, curvas como chama), cada uma com 6 ossos encadeados. Cada elo persegue o anterior com mola amortecida, então a onda corre da raiz à ponta e a ponta passa do ponto nos extremos da batida |
| Sem cabelo, só chifres | Cabelo e coroa removidos; dois chifres maiores (7 peças) |
| Garras desnecessárias | Garras removidas; ficam as mãos vermelhas |
| Modelos muito diferentes dos da Frieren e do Cid | A forma agora é um modelo GeckoLib como os trajes: `geo/armaduras/asta_forma.geo.json`, textura de 128 px gerada pelo mesmo `clothing_uv` (`asta_forma.png`) e glowmask só do vermelho (`asta_forma_glowmask.png`, com `RenderType.eyes`, o mesmo padrão do Cid) |

Implementação:
- Geometria: `armor_reference_shapes.asta_forma` (constantes `ASA_*`/`CAUDA_*`) → `build_asta_assets.forma()`. Pose de repouso embutida no modelo; a simulação só soma deltas.
- Simulação: `client/tecido/DevilUnionTecido` (pura). Cadeias de ângulo com amortecimento ζ 0,55–0,65: o acoplamento é de mão única, então amortecimento baixo multiplicava a ressonância elo a elo. Corrida dobra as asas para trás e estende a cauda; passo lateral joga a cauda para o lado oposto; a entrada abre as asas (`PerfilCinematico.abrirAsa`).
- Render: `asta/RenderFormaAsta` (`GeoArmorRenderer`, todos os ossos visíveis, translúcido, fade por parte com `revelarForma`), chamado por `CamadaFormaAsta` com o item `asta_peito` como animatable. `TecidoDevilUnionCliente` guarda o estado por jogador.
- Removidos: `ModeloFormaAsta`, `PivoFormaAsta` (e teste), `models/effects/asta_forma.json` e `geo/asta/forma.geo.json`. O Blockbench `asta_black` agora aponta para o geo novo.
- Testes: `DevilUnionTecidoTest` (layout igual ao do modelo, cauda encadeada, sem cabelo, bater mais forte e mais rápido no ar, follow-through, corrida, passo lateral, entrada, limites) e `AstaTest` (revestimento colado ao corpo, 14 peças de chifre, asas espelhadas).

Prévia: `python -B tools/preview_asta_devil_union.py --output <pasta>` → `devil_union_v3_painel.png` e `devil_union_v3_movimento.mp4` (16 s: abertura, repouso, corrida, passo lateral, giro, salto, voo, pouso). Usa o modelo GeckoLib real com textura, as poses exportadas pelo teste e o vermelho em brilho pleno. Limites: manequim neutro em vez da skin, sem relâmpagos nem orbes, sem iluminação do Minecraft; **não foi testado no jogo**, e o render GeckoLib na camada do jogador só se confirma lá.

Verificação da v3: 201 testes Java e 3 testes Blockbench. **A v3 foi substituída** depois que o usuário mandou uma arte conceitual do Devil Union no Minecraft (frente e costas).

### v4 — a partir da arte conceitual do usuário (02/11/2026)

O que a arte mostra e o que foi feito:

| Arte | Implementação |
| --- | --- |
| Asas de morcego: braço na borda de cima, garra no pulso, 3 dedos, membrana com borda recortada em arcos | `ASA_RAIZ/PULSO/PONTA/DEDOS`: borda de ataque rígida no osso raiz; membrana em `ASA_FAIXAS=8` faixas penduradas com `ASA_SEGMENTOS=3` ossos ligados (tecido, como a capinha da Frieren); dedos cortados em pedaços presos à faixa embaixo, para dobrarem junto |
| Preto/grafite com rachaduras de lava vermelhas brilhantes no corpo e nas asas | `build_asta_assets._rachaduras`: pintor procedural com semente fixa por ilha UV. Textura com 4× a densidade dos trajes (512 px), para os traços terem 1/64 de bloco. Núcleo vermelho e pontos quentes vão para a glowmask; a borda vermelho-escura fica fosca. A estrela e as veias em peça foram removidas |
| Ombreiras com espinho, manoplas, placas em V no peito, cinto, joelheiras | Peças grafite (`SHADOW`) e ossos/espinhos (`INK` `3a333f`) |
| Chifres grandes em gancho | 8 segmentos que saem para fora e para cima; só a ponta volta para dentro |
| Cauda fina curvando para o lado, ponta de flecha | `CAUDA_DIRECOES`: desce, curva para a direita do jogador e sobe na ponta; flecha triangular em degraus |

A simulação continua a mesma da v3 (batida da raiz + cadeias com follow-through), agora aplicada às faixas da membrana. Testes: os mesmos 201 (layout, faixas encadeadas, membrana passando do ponto e voltando, 16 peças de chifre).

Prévia: `python -B tools/preview_asta_devil_union.py --output <pasta>` → `minezin-outputs/asta-devil-union-v4/devil_union_v4_painel.png` e `devil_union_v4_movimento.mp4`. Mesmos limites da v3; **não foi testado no jogo**.

Verificação: 201 testes Java, 3 testes Blockbench, build ok, `git diff --check` limpo.

**Aprovada provisoriamente em 02/11/2026.** Nas palavras do usuário: "ainda está meio esquisito a física das asas, mas podemos seguir com isso por enquanto". Pendência: refinar o movimento das asas (batida e membrana) depois do teste no jogo.

### v5 — referência em vídeo do usuário (02/11/2026)

O usuário mandou um short de Devil Union num mod de Minecraft (quadros em `minezin-outputs/refs-asta/devil-union-short/`, com os principais em `chave.jpg`). Ajustes sobre a v4, sem mudar a simulação:

| Referência | v5 |
| --- | --- |
| Corpo preto justo, sem armadura | Saíram manoplas, placas em V, cinto, joelheiras e espinhos; ficam ombreiras quadradas simples |
| Marcas vermelhas desenhadas e brilhantes (bloom) | `build_asta_assets._sigilos`: sigilo em estrela no peito com linha descendo até a cruz num quadrado na barriga, coluna nas costas, X em todos os lados das ombreiras, traços nos braços, nervuras em V nas pernas. Três camadas: halo fosco, vermelho e núcleo claro, com vermelho e núcleo na glowmask. As rachaduras de lava saíram |
| Coroa na testa | Faixa vermelha brilhante, 13 dentes claros e losango na frente; os olhos continuam da skin |
| Chifres menores no alto da cabeça | 6 segmentos atrás da coroa |
| Dedos e borda da asa em brilho vermelho | Dedos em `RUBY`; bainha pintada na faixa de baixo (material próprio, para a ilha UV não ser compartilhada) |

As pernas usam o material `PLUM`, pintado do mesmo preto: isso só separa as ilhas UV delas das dos braços, que têm o mesmo tamanho arredondado.

### Matadora desperta (02/11/2026)

Pedido: "a espada Matadora, quando entrar na forma, deve mudar junto com o poder". Na referência em vídeo a espada continua cinza; seguimos o pedido, no estilo dos sigilos.
- `build_asta_assets.espada_desperta()` → `geo/asta/matadora_desperta.geo.json`, mais textura e glowmask em `textures/armor/trajes/matadora_desperta*`. Mesmo cabo, mesmos ossos e pivôs (`root`, `blade`, `energia_divider`), então a empunhadura e a escala do Black Divider não pulam. Lâmina mais larga e preta, borda serrilhada, linha central e linhas de borda vermelhas brilhantes, guarda com espinhos e ponta afilada.
- `RenderEspadaAsta`: modelo próprio que troca geo e textura conforme o dono, mais uma camada de brilho (`RenderType.eyes`) só quando desperta. Troca em `PerfilCinematico.espadaDesperta`: quando o braço da espada fica preto na entrada, e de volta no fade de saída. Vale em primeira e terceira pessoa, só para o dono que segura a espada.
- Testes: `PerfilCinematicoTest.espadaMudaJuntoComOBracoDaForma` e `AstaTest.matadoraDespertaMantemCaboEOssosDaNormal`.

Prévia: `python -B tools/preview_asta_devil_union.py --output <pasta>` → `minezin-outputs/asta-devil-union-v5/devil_union_v5_painel.png`, `devil_union_v5_movimento.mp4` e `matadora_desperta.png` (normal × desperta, parada e no Divider). **Não foi testado no jogo.**

Verificação: 203 testes Java, 3 testes Blockbench, build ok, `git diff --check` limpo.

**v5 e Matadora desperta aprovadas em prévia em 02/11/2026** ("Aprovar as duas"). Pendências: refinar a física das asas depois do teste no jogo e confirmar no jogo o brilho, a troca da espada e a roupa escondida. Depois: Black Meteorite/Furacão/meia-lua, roupa base com tecido, SFX próprios, Liebe, JAR.

## Parte 4 — inventário (retomar aqui depois do teste)

Levantado antes de o usuário pedir o JAR. Os efeitos da forma são `300+4+slot`:

| Tecla (forma) | Efeito | Servidor (`PoderesAsta`) | Visual atual (`EfeitosAsta`) |
| --- | --- | --- | --- |
| Z | 304 Black Meteorite | Exige alvo a até 20 blocos; investida até o alvo nos ticks 0–12; dano 22 em raio 4 no tick 12; manda 309 (impacto) | 5 fitas pretas e vermelhas se arrastando atrás; 309 = anel e raios vermelhos |
| X | 305 | Tick 10: dano 8 em raio 8 e remove efeitos benéficos (antimagia em área), candidato a "Furacão Negro" | 3 arcos girando, abrindo até raio 8 |
| C | 306 Black Divider | ✅ aprovado | ✅ |
| V | 307 | Onda que avança nos ticks 8–28 (`alcanceOnda`), dano 18 e silêncio, colide com bloco (310), candidato a "meia-lua" | Arco que avança |

Referências: `minezin-outputs/refs-asta/black-meteorite/` (vídeo do usuário). Ainda falta confirmar com ele os nomes canônicos de X e V.

## Black Meteorite — aprovado em prévia (02/11/2026)

Referência: quadros-chave 107–127 s de `minezin-outputs/refs-asta/black-meteorite/key/` (risco preto de borda vermelha cortando o céu, mergulho com asas e espada, estrela branca no quadro preto do impacto). A v1 tinha o corpo envolto em chamas; o usuário pediu: "o player vire um feixe de luz da anti-magia e apareça somente para realizar o corte". A v2 foi aprovada ("Aprovar").

| Ticks | Fase | Apresentação |
| --- | --- | --- |
| 0–3 | Arranque | Respingo de tinta no ponto de partida; o corpo some |
| 0–9 | Feixe | Fuso preto de borda vermelha (3,4 blocos) com fio vermelho no centro e três fios vermelhos enrolados; rastro preto ondulado até a partida, que recolhe depois da chegada; gotas pretas caindo |
| 7,5–9 | Reaparição | Estilhaços de tinta convergem; o corpo volta no tick 9 e eles estouram |
| 9–12 | Golpe | Meia-lua preta de cima para baixo, afilada nas pontas, completa no tick 12 |
| 12 | Impacto (tick do dano) | Estrela de quatro pontas (vermelha, núcleo branco); cratera de tinta abre o raio 4 em 3 ticks |
| 12–32 | Cratera | Rachaduras vermelhas, coroa de espinhos de tinta que sobe e afunda, gotas atiradas dentro do raio 4; some até 32 |

- Geometria pura: `core/vfx/BlackMeteoriteAsta`; teste `BlackMeteoriteAstaTest` (impacto só no tick 12, corpo escondido no feixe e de volta antes do golpe, cratera e gotas dentro do raio 4, tudo some em 32) exporta `build/asta-meteorite-samples.json`.
- Jogo: `EfeitosAsta.blackMeteorite` (tinta modo 22, vermelho sólido modo 8; sem aditivo, que fica amarelo sobre a grama). O rastro fica preso à partida (`p.a()`), o feixe e o golpe ao dono, e a cratera ao ponto onde o dono estava no tick 12 (guardado no efeito). `EfeitosAsta.oculto` + `RenderPlayerEvent.Pre` cancelado escondem o jogador (forma, asas, espada) do tick 0 ao 9 para todos; a mão em primeira pessoa continua. O 309 genérico (anel, raios, impact frame) continua no impacto. Mecânica do servidor sem mudança.
- Prévia: `python -B tools/preview_asta_black_meteorite.py --output <pasta>` → `minezin-outputs/refs-asta/asta-black-meteorite-fases.png` e `asta-black-meteorite.mp4` (modelo real da Devil Union; sem espada na mão, alvo, 309 nem luz do Minecraft). **Não foi testado no jogo.**

Verificação: 207 testes Java, 3 testes Blockbench, build ok, `git diff --check` limpo.

## Black Hurricane (X) — aprovado em prévia (02/11/2026)

Nome canônico do anime (Asta gira a espada e solta um redemoinho de antimagia) no lugar de "Furacão Negro". Sem vídeo de referência; coreografia proposta e aprovada na v1 ("Aprovar").

| Ticks | Fase | Apresentação |
| --- | --- | --- |
| 0–6 | Sucção | 8 fios de tinta em espiral do raio 5 até o corpo |
| 3–10 | Funil | 14 riscos de vento pretos de borda vermelha subindo e girando (até ~4,2 blocos), acelerando |
| 6–10 | Varredura | 5 lâminas em meia-lua girando rente ao chão, abrindo até o raio 8 |
| 10 | Estouro (tick do dano) | Lâminas exatamente no raio 8, nunca além; anel preto no chão marca a área |
| 10–22 | Magia arrancada | 24 fragmentos vermelhos e brancos sugados em espiral para o funil (remoção dos buffs) |
| até 32 | Dissipa | Funil gira enquanto some; anel some |

- Geometria pura: `core/vfx/BlackHurricaneAsta`; teste `BlackHurricaneAstaTest` (raio 8 no tick 10, nada além do raio, giro sempre crescente, tudo some em 32) exporta `build/asta-hurricane-samples.json`.
- Jogo: `EfeitosAsta.blackHurricane` (tinta modo 22, fragmentos vermelho sólido modo 8). Origem segue o dono, porque o servidor acerta em volta da posição dele no tick 10. Mecânica do servidor sem mudança.
- Prévia: `python -B tools/preview_asta_black_hurricane.py --output <pasta>` → `minezin-outputs/refs-asta/asta-black-hurricane-fases.png` e `asta-black-hurricane.mp4` (sem giro do corpo, espada, mobs nem luz do Minecraft). **Não foi testado no jogo.** Giro do corpo durante o golpe fica como possível etapa futura.

Verificação: 211 testes Java, 3 testes Blockbench, build ok, `git diff --check` limpo.

## Espadas pelas referências do usuário — aprovadas em prévia (02/11/2026)

O usuário mandou imagens das duas espadas e um print da Matadora na forma ("não tem essas linhas vermelhas, mas sim uma aura negra e avermelhada"). Isso **substitui o visual da Matadora desperta aprovado antes** (lâmina preta serrilhada com linhas vermelhas).

| Espada | Modelo |
| --- | --- |
| Matadora (Demon-Slayer) | Lâmina larga cinza com nuvens de tinta preta nas bordas (`_manchas`), guarda em cruz com pontas abertas, disco central, pomo redondo, cabo longo com anéis. Textura própria `textures/armor/trajes/matadora.png` (antes usava o atlas genérico) |
| Matadora desperta | Mesma silhueta; lâmina carmesim escura com as mesmas manchas, sem linhas nem serrilhado. Aura animada `core/vfx/AuraMatadora` (22 chamas pretas de borda vermelha nas bordas e na ponta, ciclo de 26 ticks, halo carmesim), desenhada em `RenderEspadaAsta.aura` junto com a espada na mão; some enquanto o Black Divider estica a lâmina |
| Demon-Dweller (nova, para a ult) | Lâmina reta fina com sulco cinza, guarda arredondada em lóbulos, cabo em gomos, estrelas de quatro pontas em vermelho brilhante perto da guarda e da ponta (`_estrelas`). `geo/asta/demon_dweller.geo.json`; ainda não aparece no jogo (entra com a ult) |

Mesmo cabo e mesmos ossos (`root`/`blade`/`energia_divider`) nas duas Matadoras. Testes: `AuraMatadoraTest` (chamas nascem na borda, ficam perto, ciclo sem salto), `AstaTest.matadoraDespertaMantemCaboEOssosDaNormal`; `SfxAnimeTest.laminaTemOssoIndependenteDaGuarda` adaptado (a guarda nova tem 12 peças em vez de 7; a garantia de nada da guarda no osso da lâmina continua). Prévia: `python -B tools/preview_asta_espadas.py --output <pasta>/asta-espadas.png` (+ `asta-matadora-aura.mp4`). **Não foi testado no jogo.** 218 testes Java, build ok.

## Ult (V na forma) — aprovada em prévia (02/11/2026)

Pedido do usuário: "o V deveria ser a ult… não vi nenhuma cutscene… o Asta puxando duas espadas e fazendo diversos cortes de antimagia… a meia-lua parece 2D". Referência: vídeo `demon-dweller` ("My second sword!"). Decisões do usuário: **combo de cortes** no servidor (mecânica nova) e a Demon-Dweller feita junto. A meia-lua v1 (onda de 24 blocos, 310 ao bater em bloco) foi removida. Coreografia v1 aprovada depois da v2 com animação dos braços.

| Ticks | Fase | Servidor | Visual |
| --- | --- | --- | --- |
| 0–18 | Grimório | Protegido (dano ×0,25, sem recuo) até o fim | Grimório abre à esquerda (páginas vermelhas); a Demon-Dweller sobe do livro, cabo primeiro; braço esquerdo puxa |
| 18–30 | Cruza | — | Lâminas cruzadas em X diante do rosto; 16 chamas pretas explodem em volta |
| 30–58 | Avanço | Anda 6 blocos na direção do olhar (`mover`, respeita colisão) | — |
| 32–52 | Rajada | Um corte a cada 4 ticks: 4 de dano em raio 3 do centro do corte, com linha de visão (i-frames zerados) | 6 cortes com volume (seção em lente, costura vermelha na borda), braços alternando |
| 60 | X | 20 de dano + Silêncio (4 s) em raio 7 do centro do X | Duas lâminas longas cruzadas; impact frame vermelho (`RoteiroUltimate` 307) e tremor |
| 60–80 | Final | Golpe termina no tick 80; bloqueio de habilidades por 80 ticks | X some; a espada volta ao livro |

- Geometria pura e chaves de câmera: `core/vfx/UltAsta` (teste `UltAstaTest`: cada corte fecha no seu tick e cabe no raio 3, X cabe no raio 7, cortes têm volume, espada sai do livro antes dos cortes e volta, câmera nunca entra no corpo; exporta `build/asta-ult-samples.json`).
- Servidor: `PoderesAsta.ult`; `PerfilAsta.impacto(3,true)` = 60 (`UltAsta.FINAL`); `alcanceOnda` e o 310 saíram. Recarga 400 t e custo 35 iguais.
- Cliente: `EfeitosAsta.ultAsta` (livro, explosão, cortes, X); câmera tipo 36 só para quem usa (`GerenciadorCutscene3D.iniciarUltAsta`, via `UltimatesTransformadas`), encerrada no cancelamento; `CamadaDemonDweller` (espada na mão esquerda e subindo do livro, com estrelas brilhantes).
- Animação: `asta_307` gerada de `build_asta_assets.ULT_BRACOS` (80 ticks, braços e tronco); `refine_player_timing` não mexe mais no 307. Testes adaptados: só um gesto pode passar de 55 ticks; a pose de impacto do V é conferida no tick 60.
- Prévia: `python -B tools/preview_asta_ult.py --output <pasta>` → `asta-ult-fases.png`, `asta-ult.mp4` (cutscene + vista externa). **Não foi testado no jogo.** Sem SFX próprio (parte 6).

Verificação: 219 testes Java (os 4 da meia-lua saíram), 3 Blockbench, build ok.

## Roupa base — capa do Touro Negro em tecido, aprovada em prévia (02/11/2026)

Pedido: "o modelo está muito bom, foque mais na animação" (+ imagem de referência da roupa); "pode usar o Cid também como base, a capa dele está em outro nível"; depois "mais animações nessa capa" e "a logo dos Touros Negros tem que ser no lado direito, e na bandana a mesma coisa". v2 aprovada ("Aprovar").

- Capa como tecido no padrão do Cid (`MantoDirecionalCid`): **costas** = `MalhaTecido` 3×3 presa nos ombros (3 colunas × 2 segmentos); **abas da frente** abertas sobre a camisa, cada uma numa `MolaMantoCid` (como as abas do Cid), barra dobrando em relação ao topo. Mantos dos ombros continuam rígidos nos braços. Simulação pura `client/tecido/TecidoAsta`, estado por jogador `asta/TecidoAstaCliente` (opção `tecidoAsta`, a mesma das asas da Devil Union), render `asta/RenderTrajeAsta` (estende `RenderTrajeAnime`, que deixou de ser `final`, mantendo o esconder da roupa na forma).
- Animações extras: vento parado (`TecidoAsta.vento`, senos incomensuráveis), golpes base Z/X/C/V (`asta/RoteiroTecidoAsta` + `EfeitosAsta.tecido`, no tempo de cada golpe), chicote no ataque com a espada (`getAttackAnim`) e assentar ao agachar.
- Detalhes da referência: gola alta com borda dourada e botão; emblema do touro na aba **direita** da capa (osso `asta_capa_frente_dir_0_0`, balança junto) e na faixa, deslocado para a direita; barra dourada em cada painel.
- Geometria: `armor_reference_shapes.asta` (`CAPA_PAINEIS`, ossos `asta_capa_{costas|frente_esq|frente_dir}_{coluna}_{segmento}`); a capa rígida antiga saiu de `polish_clothing_shapes`. `build_asta_assets.uniforme()` gera só a roupa (geo, ícones, textura rasterizada do vetor — o PNG anterior era exatamente o vetor antigo — e o projeto Blockbench).
- Testes `TecidoAstaTest` (ossos em cadeia no modelo, amostrar sem avançar, estabilidade, corrida/parada, barra dobra, vento, golpes; exporta `build/asta-capa-samples.json`). Prévia: `python -B tools/preview_asta_capa.py --output <pasta>` → `asta-capa-fases.png`, `asta-capa.mp4`. **Não foi testado no jogo.** 227 testes Java, 3 Blockbench, build ok.

## Ult travada no alvo + JAR 3.9.0-asta-preview.4 (02/11/2026)

Teste do preview.3: "ficou muito bom, porém a ult deveria ficar focando no inimigo ou nunca irá acertar todos os hits". Mudança:
- Servidor: ao lançar, pega o inimigo mais próximo na mira (até 24 blocos, mesmo critério do Black Meteorite); o quadro do golpe vira para ele. O avanço (30–58) aproxima até 2,2 blocos do alvo (no máximo 0,45 bloco/tick). Cada corte e o X são centrados na posição **atual** do alvo (1,1 / 2,8 blocos acima dos pés). Sem alvo, comportamento anterior (6 blocos para a frente, cortes fixos).
- Efeito novo **311**: o servidor anuncia o centro de cada corte (b.x = índice; 6 = X) 3 ticks antes do acerto, para todos os clientes desenharem o golpe inteiro ali. O cliente guarda em `Efeito.impacto[j]` do 307 e não desenha o 311 sozinho.
- Cliente: a origem do 307 segue o dono; livro e chamas presos ao corpo real; câmera tipo 36 segue o corpo real e olha na direção do alvo.
- `UltAsta`: `corte/cruz` com centro explícito, `anuncio(j)`, constantes de trava. Teste novo `cortesTravadosNoAlvoSaoAnunciadosAntesECabemNaArea`. 220 testes Java, 3 Blockbench, build ok.

- Cortes em 3D (pedido no mesmo teste: "ainda ficou em 2D… mudar a direção do golpe"): cada corte agora é um rastro de espada **envolvendo o alvo** (centro de curvatura no alvo, arco de ±1,3 rad) num plano próprio, girado e inclinado (`ROLAGEM` + `INCLINACAO`: horizontal, diagonais, quase vertical), com a mesma espessura em lente e costura vermelha; em perspectiva lê como elipse. A câmera da rajada recuou para os cortes não a atravessarem. O X não mudou.

`minezin-outputs/minezin-3.9.0-asta-preview.4.jar`: 4.568.570 bytes, SHA-256 `047179603f29733b33e3d2397c72667b254119511288a8bcc39bb8de9f15dd64`. Zip íntegro, versão no `mods.toml`, nomes SRG. Roteiro: o do preview.3, mais — ult mirando num mob que anda/foge: os 6 cortes e o X caem nele; ult mirando no vazio: sai para a frente como antes.

## JAR de teste — 3.9.0-asta-preview.3 (02/11/2026)

`minezin-outputs/minezin-3.9.0-asta-preview.3.jar`: 4.566.752 bytes, SHA-256 `56b358a5fb19e65cab16bfcc44025e74243b3ce8444da128c0ebf6ee42dca0a9`. Build limpo, 219 testes Java + 3 Blockbench. Conferido: zip íntegro, `version=` no `mods.toml`, classes e recursos novos (`UltAsta`, `BlackMeteoriteAsta`, `BlackHurricaneAsta`, `AuraMatadora`, `CamadaDemonDweller`, `demon_dweller*`, `matadora.png`, `asta_307` com 4 s), `MeiaLuaAsta` ausente, nomes SRG no bytecode. Protocolo de rede sem mudança de pacotes (o 310 só deixou de ser enviado).

| Cenário | Aceitação |
| --- | --- |
| Matadora fora da forma | Lâmina cinza com manchas pretas nas bordas, guarda em cruz, disco, pomo redondo |
| Ativar a forma (G) com a Matadora | Espada troca para a carmesim escura com aura de chamas pretas em loop, em 1ª e 3ª pessoa; aura some durante o Black Divider (C) |
| Z — Black Meteorite (mire num inimigo a até 20 blocos) | Corpo some e vira feixe preto de borda vermelha com rastro até a partida; reaparece no tick 9 para o corte; cratera de tinta raio 4 e estrela no impacto (tick 12) |
| X — Black Hurricane | Fios puxados, funil de riscos girando, lâminas chegam ao raio 8 no estouro; anel preto no chão; fragmentos sugados |
| V — Ult (na forma) | Câmera da cutscene só para quem usa; grimório abre, Demon-Dweller sobe do livro e vai para a mão esquerda; espadas cruzam com explosão de chamas; avança ~6 blocos com 6 cortes com volume; X gigante no tick 60 com impact frame e tremor; espada volta ao livro |
| Ult com mobs | Cada corte acerta (4) quem estiver perto; o X dá 20 + Silêncio em raio 7; Asta leva pouco dano e não é empurrado durante a ult |
| Ult perto de parede | O avanço para na parede, sem atravessar |
| Outro jogador olhando | Vê cortes, livro e espadas, sem a câmera |
| Cinemáticas desligadas (config) | Ult funciona sem a câmera |
| Morrer ou sair da forma no meio da ult | Câmera e efeitos encerram |

Pontos mais prováveis de ajuste: posição/ângulo da Demon-Dweller na mão, encaixe da câmera, sombra do jogador visível durante o feixe do Meteorite.

## JAR de teste — 3.9.0-asta-preview.2 (02/11/2026)

`minezin-outputs/minezin-3.9.0-asta-preview.2.jar`: 4.520.797 bytes, SHA-256 `d3be5599aeb54d7da3601e096dc7d760068de40ffc38c5f3b1b2290dcd559e29`. Build limpo, 203 testes Java + 3 Blockbench. Conferido: zip íntegro, `version=` no `mods.toml`, classes e texturas novas presentes (`RenderFormaAsta`, `DevilUnionTecido`, `RenderEspadaAsta`, `asta_forma*`, `matadora_desperta*`), modelo antigo ausente, nomes SRG no bytecode. Protocolo de rede 15 (sem mudança de pacotes). Inclui também a Frieren voando livre na forma desperta (preview.3). O `minezin-3.9.0-asta-preview.1.jar` que já estava na pasta é anterior a esta sessão (tentativa não aprovada) e não deve ser usado.

| Cenário | Aceitação |
| --- | --- |
| Asta com a roupa completa ativa a forma (G) | Cabeça, braços, pernas e tronco ficam pretos em sequência, sem buraco; cada peça da roupa some só quando a parte dela está coberta; marcas e coroa brilham no escuro |
| Parado, andando, correndo, pulando, caindo | Asas batem devagar parado e forte no ar, dobram para trás correndo; membrana ondula; cauda inteira, curvando para o lado |
| Primeira pessoa e terceira pessoa (F5), com a Matadora | Espada troca para a desperta quando o braço escurece e volta no fim; empunhadura não pula; Black Divider (C) com a espada desperta |
| Fim da forma ou morte | Roupa volta, espada volta ao normal, sem resto de asa ou cauda |
| Golpes base Z/X/C/V e Black Divider fora da forma | Relâmpagos negros e fenda como aprovados em prévia |
| Dois jogadores (servidor) | Forma, asas e espada só no dono; a espada de outro jogador não muda |
| Opção `tecidoAsta` desligada | Asas abrem e fecham na entrada, mas sem movimento secundário |
| Armadura vanilla durante a forma | Limite conhecido: atravessa o revestimento preto |

**Aprovação total em 02/11/2026**, depois do JAR de teste: "esse novo modelo 100% aprovado, pode subir o PR". A física das asas deixa de ser provisória.

## Estado do repositório (02/11/2026)

`main` local em `a698e32` com alterações **sem commit**: Frieren voando livre na forma desperta (`DespertaresServidor`) + toda a onda Asta acima, versão `3.9.0-asta-preview.2`. Trabalho anterior de outra IA guardado em `git stash@{0}` (não aprovado).
