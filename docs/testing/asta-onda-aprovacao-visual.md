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
| 4 | Black Meteorite, Furacão Negro, meia-lua | ⏳ |
| 5 | Roupa com tecido e empunhadura | ⏳ |
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
