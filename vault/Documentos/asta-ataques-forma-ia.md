---
tags: [documento]
---
# Asta — ataques na forma desperta (Devil Union): passagem para outra IA

Atualizado em **02/11/2026**, sobre a `main` em `cbce84e` (merge do PR #30). Este documento descreve os quatro ataques do Asta **durante a forma desperta (G, Devil Union)**: o que o servidor faz, o que o cliente desenha hoje, o que já foi aprovado e o que falta. Leia junto: [[guia-continuidade-ia|guia-continuidade-ia.md]] (regras, ambiente, armadilhas) e [[asta-onda-aprovacao-visual|testing/asta-onda-aprovacao-visual.md]] (histórico da onda Asta). Nada aqui autoriza commit, push, PR ou merge: isso só com pedido explícito do usuário.

## 1. Como trabalhar com o usuário

- Responda em **português**, direto, com tabelas curtas. O usuário avalia por **prévias** (painel PNG de fases + vídeo MP4), não por código.
- **Uma técnica por vez:** geometria pura → teste que exporta amostras → prévia → aprovação → registrar no documento da onda. Não reabra o que já foi aprovado.
- **Fidelidade ao anime é requisito.** Peça vídeo/cena antes de desenhar; se não houver, pesquise e diga o que é canônico e o que é invenção do GDD.
- **Mecânica do servidor não muda** sem pedido: ticks de dano, áreas, alvos e recargas abaixo são o contrato. O visual se prende a eles.
- Antes de mostrar uma prévia, confira você mesmo o quadro renderizado: nesta onda, várias versões tinham defeitos visíveis (ilhas UV compartilhadas, câmera fora do plano distante, peças desconectadas).

## 2. A forma desperta (contexto comum aos quatro ataques)

| Item | Valor | Onde |
| --- | --- | --- |
| Ativação | G com a Espada Matadora na mão principal, nó Despertar comprado; custo base de mana 50 | `DespertaresServidor.ativar`, `ConjuracaoRpg.custoBase(4)` |
| Duração / recarga de G | 60 s / 300 s base | `PersonagemRpg.ASTA(4,"Asta","Liebe",60,300)`, `PerfilAsta.DURACAO_FORMA=1200` |
| Abertura | 55 ticks: nenhum golpe sai até ela terminar | `PerfilAsta.ABERTURA`, checagem em `PoderesAsta.executar` |
| Revelação do corpo (cliente) | braços 4→18, pernas 10→24, cabeça 18→34, tronco 30→47 ticks; asas abrem 26→47 | `PerfilCinematico.revelarForma`, `abrirAsa` |
| Efeito de entrada | 200: relâmpagos negros em espiral + 6 orbes pretos de borda vermelha | `EfeitosAsta` (tipo 200) |
| Modelo | GeckoLib `geo/armaduras/asta_forma` + textura com sigilos + glowmask; asas e cauda em tecido | `RenderFormaAsta`, `DevilUnionTecido`, `TecidoDevilUnionCliente` |
| Roupa | some peça por peça quando a parte fica 100% preta | `RenderTrajeAnime`, `PerfilCinematico.trajeCoberto` |
| Espada | troca para a Matadora desperta quando o braço escurece | `RenderEspadaAsta`, `PerfilCinematico.espadaDesperta` |

Regras do servidor que valem para os quatro ataques (`PoderesAsta`):
- Exigem a Espada Matadora na mão principal; um golpe ativo por jogador; no máximo 128 golpes no mundo.
- Cada golpe bloqueia novas habilidades por 32 ticks (`ProgressaoPoderes.bloquear`).
- O golpe é cancelado se o jogador morrer, sair, trocar de dimensão, trocar de kit ou se a forma acabar (`valido`).
- Ao conjurar, o servidor manda `PacoteEfeitoDespertar` com tipo `304+slot`, origem = posição do jogador e direção = olhar. Duração 32 ticks (V manda 30). Toca o sweep vanilla (pitch 0,55).
- Recarga e mana passam por `RpgServidor.conjurar` (maestria, desconto de 20% do conjunto, estado desperto). Custos base por slot: 8, 12, 20, 35.
- Dano é registrado uma vez por alvo por golpe (`atingidos`).

## 3. Visão geral

| Tecla | Ataque | Efeito | Recarga (forma) | Tick do dano | Estado visual |
| --- | --- | --- | --- | --- | --- |
| Z | **Black Meteorite** | 304 (+309 no impacto) | 60 t (3 s) | 12 | ✅ **aprovado em prévia, não mexer** (feixe de antimagia; ver documento da onda) |
| X | **Black Hurricane** (antes "Furacão Negro") | 305 | 200 t (10 s) | 10 | ✅ **aprovado em prévia, não mexer** |
| C | **Black Divider** | 306 | 300 t (15 s) | 18 | ✅ **aprovado, não mexer** |
| V | **Ult** (antes "Meia-lua de antimagia") | 307 (+310 ao bater em bloco) | 400 t (20 s) | cortes 32–52, X no 60 | ✅ **aprovada em prévia, não mexer**: cutscene, Demon-Dweller do grimório, 6 cortes de 4 + X de 20 com Silêncio (ver documento da onda) |

Os nomes vêm do GDD e do roteiro antigo (`docs/testing/asta-3.1.md`). **Confirme com o usuário** se X e V correspondem a técnicas do anime ou se ficam com esses nomes.

Efeitos auxiliares: 308 = clarão da reflexão/bloqueio (fora da forma também), 309 = impacto vermelho (anel + 12 raios + impact frame `0xD5264C`), 310 = onda da meia-lua que bateu em bloco (o cliente converte em 309 e encerra a 307).

## 4. Fichas dos ataques

### Z — Black Meteorite (304)

**Servidor (contrato):**

| Tick | Acontece |
| --- | --- |
| Conjuração | Exige alvo vivo, em linha de visão, a até **20 blocos** e dentro de ~41° do olhar (`dot > .75`); sem alvo, mostra "Mire em um inimigo a até vinte blocos para o Black Meteorite." e não gasta nada |
| 0–11 | Investida: o jogador avança até o alvo, no máximo 0,9 bloco por tick (`mover`, respeita colisão, chunk e borda do mundo); fica protegido (dano ×0,25 e sem recuo) |
| **12** | Dano **22** em todos os inimigos em raio **4** com linha de visão; manda **309** na posição do jogador (20 ticks) |

**Visual atual** (`EfeitosAsta.receber`, ramo `transformado&&slot==0`): até o tick 10, a carga genérica (fios vermelhos até a mão); depois, 5 fitas pretas com núcleo vermelho se arrastando atrás do jogador. O impacto usa o 309 genérico.

**Direção:** no documento da onda, a leitura da referência foi "Black Meteorite como **estrela cadente negra**": o Asta vira um projétil de antimagia preta com contorno vermelho e rastro, e explode em tinta/rasgo no chão ao chegar. Linguagem já aprovada nesta onda: **tinta preta chapada com contorno vermelho fino**, bordas rasgadas, respingos (material 22 do `anime_energy.fsh`, não aditivo, alpha sólido) + brilho vermelho aditivo; relâmpagos negros (`GolpesBaseAsta.relampago`).

**Atenção:** a pasta `minezin-outputs/refs-asta/black-meteorite/` tem quadros de um vídeo enviado pelo usuário, mas eles mostram mais a cena de diálogo do que o golpe. **Peça ao usuário uma referência melhor do golpe** antes de desenhar.

**Aceitação sugerida:** o rastro acompanha a posição real do jogador (a origem é a do dono, igual ao 302/303); nada de "impacto" antes do tick 12; a explosão cobre visualmente o raio 4; some até o tick 32.

### X — Furacão Negro (305)

**Servidor (contrato):** no **tick 10**, todos os inimigos em raio **8** (sem exigir linha de visão) perdem **todos os efeitos benéficos** e levam **8** de dano. Não há deslocamento nem projétil.

**Visual atual** (ramo `transformado&&slot==1`): 3 arcos pretos (material 7) + contorno vermelho (material 10) girando em volta do jogador e abrindo do raio 1 ao 8 a partir de `acerto-2`.

**Direção:** um redemoinho/rajada de antimagia preta girando em volta do Asta, com a fronteira chegando ao raio 8 exatamente no tick 10 e "rasgando" a magia dos alvos (dá para mostrar os buffs sendo arrancados como fagulhas). Confirme com o usuário se há cena do anime para usar.

**Aceitação sugerida:** o raio visual bate com o raio 8 do servidor no tick 10; sem mira (é em área, 360°).

### C — Black Divider (306) — ✅ aprovado

Não reabrir. Resumo para referência: carga em fios de tinta da mão à ponta (0–14), aura irregular (14–18), **corte no tick 18** com meia-lua negra e fenda de borda vermelha de **18 blocos** (largura 2,2, dano **32**, `DestruicaoAnime.fenda` se a destruição estiver ligada), rastro até o tick 32. Áudio próprio: `RoteiroAudio.divider()` (`divider_charge` 0–18, `divider_release` 18–24). Implementação: `core/vfx/BlackDividerAsta`, `EfeitosAsta.blackDivider`, teste `BlackDividerAstaTest` (exporta `build/asta-divider-samples.json`), prévia `tools/preview_asta_black_divider.py`. A lâmina cresce pelo `PerfilDivider` (`RenderEspadaAsta`), inclusive na Matadora desperta.

### V — Meia-lua de antimagia (307)

**Servidor (contrato):**

| Tick | Acontece |
| --- | --- |
| 8–28 | A onda avança da altura do peito (origem +1 em y) na direção do olhar no momento da conjuração: `PerfilAsta.alcanceOnda(t) = clamp((t−8)·1,2; 0; 24)` blocos |
| A cada tick | Raycast do trecho percorrido (`ClipContext.Block.COLLIDER`): se bater em bloco, manda **310** no ponto e a onda acaba |
| A cada tick | Inimigos em raio **3** do centro da onda, com linha de visão: dano **18** + **Silêncio** por 80 ticks (4 s; impede conjurar magia e projéteis mágicos) |

**Visual atual** (ramo genérico com `transformado&&slot==3`): o mesmo arco preto e vermelho dos golpes base, com raio 4, levado à distância `alcanceOnda(t)` e com `fade` em vez do corte.

**Direção:** uma meia-lua de tinta antimagia viajando (o mesmo material da meia-lua aprovada no Black Divider, para manter a coerência), larga o bastante para ler o raio 3, deixando respingos; ao bater em parede, estoura (310 → 309). O Silêncio pode ganhar uma marca nos alvos atingidos (opcional; confirme).

**Aceitação sugerida:** a posição do arco é exatamente `origem + dir·alcanceOnda(t)` (com coordenadas **do mundo** subtraindo a origem; veja a armadilha 1 do guia); some no tick do 310 quando bate em bloco.

## 5. Como implementar (padrão desta onda)

1. **Geometria pura** em `core/vfx/<Nome>Asta.java` (sem Minecraft): formas e tempos em função de `t`, presos a `PerfilAsta.impacto(slot,true)` / `alcanceOnda`.
2. **Teste** em `src/test/.../core/vfx/<Nome>AstaTest.java`: o marco visual principal coincide com o tick do servidor, as formas ficam dentro da área de dano, tudo some até o fim; exporte `build/asta-<nome>-samples.json` e declare em `build.gradle` (`outputs.file`).
3. **Render** em `EfeitosAsta` (substitua o ramo antigo do slot), usando `DesenhoMagia.lote(modo, aditivo, ...)`: material 22 = tinta antimagia sólida, 10 = ponta afilada aditiva, 7 = borda vermelha.
4. **Prévia** em `tools/preview_asta_<nome>.py`, copiando o padrão de `preview_asta_black_divider.py` / `preview_asta_base.py`: alimente-a com **coordenadas absolutas** como o jogo faz; fundo claro, porque o preto some no escuro; painel de fases + MP4.
5. Mostre ao usuário, espere a aprovação e registre no documento da onda (tabela "Ondas internas" e uma seção por técnica).

Verificação antes de entregar (detalhes do ambiente Windows no guia, seção 3.5):

```powershell
./gradlew.bat test build --no-daemon --console=plain
python -B tools/summarize_tests.py
python -B -m unittest discover -s tools -p test_blockbench_projects.py
git diff --check
```

## 6. Armadilhas específicas

- **Origem do efeito:** `EfeitosAsta` usa a posição atual do dono para os tipos 200, 302, 303 e 304; os outros usam o ponto enviado (`p.a()`). Se o novo visual precisar seguir o jogador (Meteorite), mantenha o 304 nessa lista.
- **Corte antes do servidor:** o ramo genérico só desenha "golpe completo" a partir de `acerto−2` (`PerfilCinematico.corte/varredura`). Não antecipe o impacto.
- **Tinta translúcida vira cinza:** no material 22, use alpha sólido.
- **Sons:** só o Black Divider tem SFX próprio; os outros tocam o sweep vanilla na conjuração. SFX próprios são a parte 6 da onda: não misture com o visual sem pedido.
- **Testes que leem `RegistroSons`** quebram fora do Forge; use `CatalogoSons`.
- Não reutilize IDs de efeito sem conferir: o Asta usa 114, 200, 300–309 e 311 (310 não é mais usado).

## 7. Arquivos

| Assunto | Arquivo |
| --- | --- |
| Servidor dos golpes | `core/asta/PoderesAsta.java` |
| Tempos e recargas | `core/asta/PerfilAsta.java` |
| Visual de todos os efeitos do Asta | `core/asta/EfeitosAsta.java` |
| Golpes base (relâmpagos) | `core/vfx/GolpesBaseAsta.java` |
| Black Divider | `core/vfx/BlackDividerAsta.java`, `core/vfx/PerfilDivider.java` |
| Forma, roupa e espada | `core/asta/RenderFormaAsta.java`, `CamadaFormaAsta.java`, `RenderEspadaAsta.java`, `core/vfx/PerfilCinematico.java` |
| Shader | `assets/arcanecontrol/shaders/core/anime_energy.fsh` |
| Áudio | `audio/RoteiroAudio.java`, `audio/CatalogoSons.java` |
| Referências do usuário | `minezin-outputs/refs-asta/` (`black-meteorite`, `demon-dweller`, `devil-union`, `devil-union-short`) |
