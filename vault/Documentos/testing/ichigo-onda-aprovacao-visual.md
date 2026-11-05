---
tags: [documento]
---
# Ichigo — onda de qualidade

Iniciada em 03/11/2026, sobre a `main` local com a onda Asta (`3.9.0-asta-preview.2`). Foco declarado pelo usuário: **animação e VFX no nível máximo** ("próximo nível"). Mecânicas do servidor: **mudam** (C do Bankai vira a ultimate Mugetsu com bloqueio de poderes).

## Referências

Imagens enviadas pelo usuário em 03/11/2026:
- Ichigo base (shihakushō, faixa branca, sandálias). Observação: a imagem veio marcada "Bankai", mas mostra o traje base.
- Ichigo Bankai: casaco longo de barra rasgada com forro/borda branca, Tensa Zangetsu preta.
- Máscara Hollow: branca, listras vermelhas no lado direito, dentes.
- Zangetsu Shikai: cutelo de lâmina preta com fio branco, guarda em meia-lua, cabo branco com fita.

- Tensa Zangetsu (03/11): katana toda preta, guarda quadrada, cabo preto com losangos vermelhos e corrente curta no pomo.

Vídeos do anime: pendentes (liberação do Bankai, Getsuga, Mugetsu).

## Moveset aprovado (03/11/2026)

| Tecla | Forma base (Shikai) — ✅ 100% aprovada | Bankai |
| --- | --- | --- |
| Z | Getsuga Tenshō azul-branco, rasga o chão | **Getsuga Tenshō** preto com borda vermelha |
| X | Shunpo com pós-imagem | Shunpo em sequência (até 3 dashes) |
| C | Golpe pesado do cutelo + onda de choque | **Ultimate: Mugetsu** (ver abaixo) |
| V | Investida com corte ascendente | Máscara Hollow; com a máscara ativa, V dispara Cero |

Com a máscara ativa, o Z do Bankai vira **Getsuga Jūjishō** (X cruzado). Aprovado em 03/11/2026 ("Manter proposta").
| G | Transformação em Bankai com cinemática | encerra o Bankai |

### Mugetsu (C no Bankai)
- Transformação **temporária** para a forma Mugetsu (cabelo longo, faixas, corpo envolto em escuridão), com **troca de modelo/forma** durante a ult.
- Poder avassalador: a lâmina vira escuridão e uma onda negra engole a área.
- Ao terminar: volta à forma anterior e **fica 1 minuto sem poder usar poderes** (custo da Getsuga final).
- Animação é prioridade: entrada, golpe e saída com pose própria (`player_animation/ichigo.json`).

## Ondas internas

| Ordem | Parte | Status |
| --- | --- | --- |
| 1 | Getsuga Tenshō (base + Bankai) | 🔎 v5: v4 "quase perfeito"; rastro do Shikai (pós-imagens e sulco) trocado para azul |
| 2 | Kit base X/C/V | ✅ aprovado em prévia (v3, 03/11/2026) |
| 3 | Transformação em Bankai (G) | ✅ aprovado em prévia (03/11/2026, com giro de pulso da Zangetsu) |
| 4 | Mugetsu: transformação, golpe, saída e bloqueio de 1 min | 🔎 v3 para revisar (corte fino e altíssimo + chamas negras no corpo) |
| 5 | Bankai X/V (+ Jūjishō no Z com máscara) | 🔎 v1 para revisar |
| 6 | Trajes: shihakushō + casaco do Bankai com tecido | 🔎 v1 para revisar |
| 7 | Zangetsu Shikai e Tensa Zangetsu | 🔎 Tensa refeita pela referência; Shikai sem mudança |
| 8 | SFX | 🔎 v1 para ouvir (9 sons próprios) |
| 9 | JAR + roteiro | 🔎 `minezin-3.10.0-ichigo-preview.1.jar` gerado; teste no jogo pendente |

## Getsuga Tenshō — prévia v1 (03/11/2026)

| Tick (desde o cast) | Fase | Apresentação |
| --- | --- | --- |
| 0 → impacto (Shikai 8, Bankai 5) | Carga | Reiatsu sobe pelo corpo (azul / preto com veios vermelhos); pose levanta a Zangetsu acima da cabeça e segura a tensão |
| impacto | Libera (tick do dano) | Corte diagonal; meia-lua de frente para o alvo nasce na lâmina e cresce em 3 ticks |
| +0 → +20 | Viagem | Segue exatamente a frente de colisão do servidor (1,4 bloco/tick, até 28); 2 pós-imagens; chamas na borda interna; rasgo no chão |
| parada/parede → +8 | Dissipa | Meia-lua se parte em 14 lascas; rasgo some devagar |

Mudanças no servidor: Getsuga do Bankai ganhou 5 ticks de preparo (antes saía no tick 0); efeitos 520/524 duram 28 ticks.
Implementação: `core/vfx/GetsugaIchigo` (geometria pura), `EfeitosIchigo.getsuga/carga`, poses `desperto_6_500/510` em `tools/build_ichigo_assets.animations()`.
Testes: `GetsugaIchigoTest` (frente = `PerfilIchigo.alcance`, tudo some no fim; exporta `build/ichigo-getsuga-samples.json`).
Prévia: `python -B tools/preview_ichigo_getsuga.py --output <pasta>` → `minezin-outputs/ichigo-getsuga/`.
Limites da prévia: traje provisório, sem mobs, impact frame e SFX.

**v2 (3D):** meia-lua é um volume com seção em cunha (fio afiado por fora, dorso grosso por dentro, some nas pontas), sombreada por face e com depth write; lascas viraram cubos. Shikai em cor chapada (modo 8) + halo aditivo; Bankai em tinta (modo 22) + bordas vermelhas.
**v3:** espessura 1,6 (Shikai) / 1,9 (Bankai) bloco; pontas curvam fundo para trás (foice); iluminação por vértice com normais reais do volume + borda fresnel (azul/vermelha) que contorna a silhueta de qualquer ângulo.
**v4:** arco girado: plano (eixo do corte, frente), meio bojudo apontado para o alvo, pontas para trás; espessura 1,1/1,3 na perpendicular; pós-imagens ficam exatamente atrás no caminho.

## Getsuga ✅ aprovado (03/11/2026, v5): "Aprovado, seguir"

## Kit base X/C/V — prévia v1 (03/11/2026)

| Tecla | Efeito | Visual 3D | Sincronia |
| --- | --- | --- | --- |
| X Shunpo | 501 (16 ticks) | 4 pós-imagens sólidas do corpo ao longo do caminho, riscos de velocidade, poeira em cubos na partida e chegada | teleporte do servidor no cast |
| C Golpe pesado | 525 (novo, 24 ticks) | Parede de onda em volume (anel com seção em cunha) que chega ao raio 8 em 5 ticks, 10 rachaduras radiais com brilho azul, 18 detritos em cubo, clarão | tick 12 (dano em área raio 8) |
| V Investida + corte | 526 (novo, 16 ticks) | Arco vertical em volume subindo na frente do corpo em 3 ticks + 2 pós-imagens | tick 10: avanço de até 3 blocos (novo no servidor) e corte de 5 blocos |

Implementação: `core/vfx/KitBaseIchigo`, `EfeitosIchigo.shunpo/onda/corte/malha` (luz por normal + fresnel), `PoderesIchigo.investida`, poses `desperto_6_501/502/503`. Teste `KitBaseIchigoTest`; prévia `python -B tools/preview_ichigo_kit_base.py --output <pasta>`.
**v2:** C com as duas mãos no cabo (ângulos resolvidos para os punhos se encontrarem); V lança o alvo (impulso 1,3 para cima, aplicado depois do dano) + efeito 527 (3 espirais de reiatsu em volume e 12 estilhaços), corte mais largo e grosso; Shunpo com anéis de ar em toro na partida/chegada, 3 hélices de vento e pós-imagens que se desfazem em flocos.
**v3 (03/11):** X e V aprovados ("Muito BOM"); C ganhou fumaça em volume: 30 nuvens encaroçadas iluminadas (6 azuladas subindo do impacto em coluna, 24 de poeira levantadas quando a onda passa, rolando para fora); efeito 525 passou a durar 36 ticks.

## Transformação em Bankai (G) — prévia v1 (03/11/2026)

| Tick | Fase | Apresentação |
| --- | --- | --- |
| 0–14 | Preparo | Zangetsu estendida, mão esquerda no pulso direito; línguas de reiatsu preto/vermelho vazando |
| 14 | Grito | Pilar em volume de 24 blocos sobe em 4 ticks; onda de choque no chão até raio 10; impact frame |
| 14–30 | Poeira | 22 nuvens em volume cobrem o corpo; pilar afina |
| 30 | Revelação | Corte horizontal em volume abre a poeira; **casaco do Bankai e Tensa Zangetsu aparecem aqui** (`EfeitosIchigo.bankaiVisual`) |
| 30–44 | Aura | Chamas pretas de borda vermelha baixando ao repouso |

Servidor: sem fumaça vanilla; golpes bloqueados durante os 44 ticks da liberação (`ichigo_bankai_inicio`).
Implementação: `core/vfx/EntradaBankai`, `EfeitosIchigo.entrada`, pose `desperto_6_200`. Teste `EntradaBankaiTest`; prévia `python -B tools/preview_ichigo_bankai.py --output <pasta>`.

**Aprovado (03/11/2026).** Ajuste final: giro de pulso da Zangetsu no preparo (`EntradaBankai.pulso`, ticks 5–28) em `RenderZangetsu`: lâmina alinhada ao braço (eixo X) e rolada 90° (eixo Y) para ficar em pé, face larga de lado, fio para baixo. Eixo no renderer do Minecraft ainda a conferir no jogo.

## Mugetsu (C no Bankai) — prévia v1 (03/11/2026)

| Tick | Fase | Apresentação / servidor |
| --- | --- | --- |
| 0–20 | Getsuga final | Raios de trevas sugados para o corpo, casulo negro que estoura em cascas |
| 20 | Forma Mugetsu | Cabelo longo, faixas no peito com gola alta, braço direito enfaixado (`CamadaHollow`, `textures/ichigo/mugetsu.png`); casaco do Bankai some |
| 30–50 | Lâmina de trevas | Braço erguido; **Tensa Zangetsu continua na mão, envolta em chamas negras** (referência do usuário), pulso girado para a lâmina seguir o braço |
| 50 | Mugetsu (dano) | Lâmina desce; meia-lua colossal avança 30 blocos; **60 de dano** em cone de 32 blocos (meia-largura 4 + 0,3·z, altura até 14) |
| 50–80 | Trevas | 40 chamas negras em volume no cone e cúpula de escuridão |
| 80 | Fim | Forma desfaz, **Bankai acaba e 60 s sem poderes** (kit e G bloqueados, aviso na barra) |

Trevas em cor chapada (modo 8): o modo 22 tem veios vermelhos da antimagia do Asta. GDD: C do Bankai passa de "Getsuga Cruzado" a "Mugetsu" (o Jūjishō vai para o Z com a máscara, onda 5).
Implementação: `core/vfx/MugetsuIchigo`, `PoderesIchigo` (lista `MUGETSUS`, `bloqueado`), `EfeitosIchigo.mugetsu`, pose `desperto_6_530`. Teste `MugetsuIchigoTest`; prévia `python -B tools/preview_ichigo_mugetsu.py --output <pasta>`.

**v2 (03/11/2026), refeita pelo vídeo do usuário** (`minezin-outputs/refs-ichigo/`, cena Ichigo × Aizen): v1 recusada ("tá bem feinho"). Linguagem da cena: trevas como tinta fluida de borda macia; forma com cabelo até o chão, faixas cinza na boca e no torso, braço direito com listras vermelhas; no golpe o céu inteiro vira trevas e ficam pingos pendurados.
- 0–50: 10 tentáculos de trevas em volume (tubo + casca translúcida maior para a borda macia) serpenteando e apertando no estouro da forma (tick 20).
- 50–62: cortina de trevas no alto (26 blocos) se abre sobre o cone; 60 gotas descem; as que estão sobre o cone de dano tocam o chão no pico.
- 62–80: gotas recolhem e ficam penduradas (4–14 blocos), como no fim da cena; tudo some no 80.
- Removidos: casulo, meia-lua, cones de chama e cúpula. Forma: `boca` (faixa na boca), cabelo até os joelhos, textura cinza/listras vermelhas.


---

## Montado enquanto o usuário estava fora (03/11/2026) — para revisar juntos

Pedido: "monte tudo e quando eu voltar vou analisar". Nada abaixo foi aprovado ainda.

### Mugetsu v3
- Golpe pela correção do usuário: **um corte fino, reto e altíssimo** (fio negro de 72 blocos, 8 à frente, rasga do céu ao chão em 2 ticks no tick 50), as trevas se abrem dele (cortina no alto) e pingam; os tentáculos saíram.
- **Chamas negras em volta do corpo** (pedido): 14 labaredas em volume (núcleo sólido iluminado + casca translúcida 1,7× para a borda macia) nascendo num anel nos pés, tremulando, e 18 fragmentos de trevas subindo das pontas; acendem até a forma e baixam nas trevas.
- Prévia: `minezin-outputs/ichigo-mugetsu/`.

### Trajes (onda 6) — `tools/build_ichigo_traje.py`
- **Shihakushō (conjunto de armadura `ichigo`)**: kosode preto com o V branco cruzado, obi branco com nó, mangas largas penduradas (balançam no idle), hakama plissada, waraji com tabi branco. 34 cubos, UV 128 do `clothing_uv`.
- **Casaco do Bankai (`ichigo_bankai`, nível novo de detalhe)**: 147 cubos, 40 ossos. Casaco aberto com gola alta forrada de vermelho, debrum branco nas bordas da frente, forro vermelho aparecendo, mangas justas com punho largo debruado, faixa preta, e **caudas em tecido físico** até o tornozelo com a barra rasgada do anime (dois "dentes" de tamanhos diferentes por coluna).
- Tecido: `TecidoIchigo` (painel de trás = malha PBD 4×5 presa na cintura; frentes em molas como o Cid/Asta), `TecidoIchigoCliente` (20 Hz, reset no teleporte/Shunpo), opção de cliente `tecidoIchigo`. Render `RenderBankaiIchigo` pela `CamadaHollow` (substitui o casaco de caixas). Some durante a forma Mugetsu.
- Teste `TecidoIchigoTest` (ossos batem com o modelo, sem esticar, volta ao repouso). Prévia: `minezin-outputs/ichigo-traje/` (giro do shihakushō + casaco parado/correndo/parando/girando).

### Onda 5 — Bankai X/V
| Tecla | Agora |
| --- | --- |
| X | Shunpo em sequência: até **3 dashes em 1,5 s**, só o primeiro paga a recarga; visual 3D do Shunpo base em preto e vermelho (efeito 511) |
| V | 1º toque: **máscara Hollow** (agora se forma: 22 estilhaços brancos convergem no rosto e uma rachadura vermelha corre; modelo da máscara com listras vermelhas e dentes pela referência). Com a máscara: **Cero** (efeito 528): esfera negra com 2 anéis vermelhos e detritos sugados por 14 ticks, depois feixe turbulento de 30 blocos, **30 de dano** em raio 1,6, recarga 3 s |
| Z com máscara | **Getsuga Jūjishō** (efeito 523): dois Getsuga do Bankai aprovados, cruzados em X, raio 2,1 e 28 de dano |
Prévia: `minezin-outputs/ichigo-hollow/` (máscara + Cero). Jūjishō usa a geometria já aprovada do Getsuga, sem prévia própria.

### Tensa Zangetsu (onda 7)
Pela referência: guarda quadrada com cantos, cabo preto com losangos vermelhos, lâmina toda preta levemente curva (8 segmentos) e corrente de 7 elos. Imagem: `minezin-outputs/ichigo-traje/ichigo-espadas.png`. A Zangetsu do Shikai não mudou.

### SFX (onda 8) — `tools/build_ichigo_sfx.py`
9 sons próprios sintetizados (Vorbis mono 48 kHz, legendas pt/en): `ichigo_getsuga`, `ichigo_getsuga_bankai`, `ichigo_shunpo`, `ichigo_golpe_pesado`, `ichigo_corte_ascendente`, `ichigo_bankai` (pressão → grito no tick 14 → corte no tick 30), `ichigo_mascara`, `ichigo_cero` (disparo no tick 14), `ichigo_mugetsu` (zumbido → "shing" fino no tick 50 → silêncio → estrondo). Substituem os sons emprestados (`divider_release`, `purple_charge`) do Ichigo. Todos juntos para ouvir: `minezin-outputs/ichigo-sfx/ichigo-sfx-todos.wav`.

### JAR de teste — 3.10.0-ichigo-preview.1
`minezin-outputs/minezin-3.10.0-ichigo-preview.1.jar`: 3.880.898 bytes, SHA-256 `cf7ece9654dccec2faef528d0b5006616ca476cc7a3461262473297de1adf9ff`. Build limpo com 245 testes Java passando. `version=` do `mods.toml` conferido. **Protocolo de rede 15 → 16** (efeitos novos 525–530: cliente e servidor precisam da mesma versão).

| Cenário no Minecraft | Aceitação |
| --- | --- |
| Z/X/C/V sem Bankai | Getsuga azul em volume, Shunpo com anéis e flocos, golpe pesado com fumaça, investida que lança o alvo ~4 blocos |
| G (Bankai) | Preparo com a Zangetsu **apontada para frente e em pé** (giro de pulso — eixo a conferir), pilar, poeira, casaco e Tensa aparecem no corte |
| Andar/correr/girar no Bankai | Caudas do casaco balançam e abrem para trás; nada atravessa as pernas de forma grosseira |
| X no Bankai 3× rápido | Três dashes seguidos; o 4º espera a recarga |
| V, V no Bankai | Máscara se forma; segundo V dispara o Cero na direção da mira |
| Z com máscara | Dois Getsuga em X |
| C no Bankai | Forma Mugetsu (cabelo, faixas, chamas negras), Tensa em chamas, corte fino altíssimo, trevas; no fim o Bankai acaba e "Sem reiatsu…" por 60 s (G e kit bloqueados) |
| Opção `tecidoIchigo` desligada | Casaco parado, sem erro |

### Pendências e riscos
| Prioridade | Situação | Próxima ação |
| --- | --- | --- |
| Alta | Nada desta onda foi testado no Minecraft | Rodar o roteiro acima com o JAR |
| Alta | Giro de pulso da Zangetsu (Bankai e Mugetsu) validado só na prévia | Conferir eixo/sinal no jogo, 1ª e 3ª pessoa |
| Média | Casaco do Bankai é desenhado por cima do shihakushō (armadura) | Ver se há sobreposição feia; se houver, esconder peito/pernas da armadura no Bankai |
| Média | Forma Mugetsu nas prévias é aproximada por caixas | Avaliar o modelo real no jogo |
| Baixa | `build_ichigo_assets.main()` não regera mais as receitas das peças `ichigo_*` (os JSONs já existem) | Mover as receitas para `build_ichigo_traje.py` se for regerar do zero |
| Baixa | Prévias com câmeras às vezes mal enquadradas (vista lateral do Cero) | Ajustar se a prévia for usada para aprovação |


## Revisão do usuário (03/11/2026) e ajustes — JAR 3.10.0-ichigo-preview.2

| Pedido | Feito |
| --- | --- |
| Mugetsu: "uma linha que vai de baixo para cima, sem o poço esquisito no céu" | A linha sobe do chão ao alto em 3 ticks (`topoLinha`); cortina e gotas removidas; halo escuro mais fino |
| "Trajes muito feios", mangas do shihakushō e do Bankai | Shihakushō refeito pela referência: lapelas brancas em V até o obi, alça vermelho-terra na diagonal com fivela, obi branco com nó e pontas, mangas largas retangulares com barra branca (base) ou barra rasgada (Bankai), dobras, hakama larga. Casaco do Bankai sem forro vermelho nem gola alta: aberto com borda branca, caudas em tecido rasgadas |
| Máscara: "somente ela com buff de dano, sem Cero" | Cero removido (servidor, efeito 528, som `ichigo_cero`, prévia). V com a máscara ativa só avisa "Máscara Hollow já ativa". Modelo da máscara refeito: placa abaulada até as bochechas, olhos inclinados, dentes serrilhados, listra vermelha no meio e listras em leque do lado direito de quem olha |
| Tensa Zangetsu "perfeita" | Mantida |
| Zangetsu do Shikai "mais fiel, afinar a ponta" | Lâmina em 22 fatias: dorso reto, fio barrigudo varrendo até uma ponta afiada, faixa clara ao longo do fio, gancho do dorso perto da guarda |

JAR `minezin-outputs/minezin-3.10.0-ichigo-preview.2.jar` (o preview.1 foi substituído). 245 testes Java passando. Máscara nova não tem prévia offline (é ModelPart Java): conferir no jogo.

## Rodada de ajustes 3 (03/11/2026) — sem JAR (usuário pediu para gerar só depois de analisar)

| Pedido | Situação |
| --- | --- |
| Trajes "gordos demais" | Mangas 5,0 (antes 6,3), hakama, corpo, obi, alça e casaco mais justos. Usuário: "trajes estão absurdamente insanos" |
| Casaco do Bankai "faltando na lateral" | Painéis laterais em tecido (`bk_capa_lado_{esq,dir}_0_0..3`, `BK_LADOS`), barra rasgada; `TecidoIchigo` passa a ter 40 ossos |
| Zangetsu "mais afiada", "falta o molho", "lâmina bugada" | Lâmina longa e esguia, fio liso (cubos girados ao longo da curva, sem passar do dorso), degradê preto→aço→branco, cabo escuro com trançado branco, faixa de pano em cadeia de 4 ossos. Usuário: "espadas perfeitas" |
| Acessórios das espadas com cloth | `TecidoEspadaIchigo`: faixa da Zangetsu e corrente da Tensa (4 ossos cada) numa mola de 4 estágios movida por andar, girar e golpear; `RenderZangetsu` aplica e restaura; animação idle fixa removida |
| Máscara: mostrar e olhos mais afiados | `tools/preview_ichigo_mascara.py` lê a geometria do `CamadaHollow`; olhos em lâmina inclinada |
| Mugetsu "corte em linha reta se expandindo para frente na mesma finura e largura", 50 blocos | Parede finíssima (0,14) e altíssima (72) que avança de 2 a **50 blocos** em 6 ticks; espessura e altura constantes; cone de dano também vai a 50 |

## JAR de teste — 3.10.0-ichigo-preview.3 (03/11/2026)

`minezin-outputs/minezin-3.10.0-ichigo-preview.3.jar`: 3.871.584 bytes, SHA-256 `867b967f7875f21c6e4f161343b235f84d5c6079bd67462cae4147bb477cbcdc`. Build limpo, 245 testes Java. Protocolo de rede 16. Conferido: zip íntegro, `version=` no `mods.toml`, `TecidoIchigo`/`TecidoEspadaIchigo`/`RenderBankaiIchigo`, geos e texturas novas, sem o som do Cero.

Inclui desde o preview.2: trajes mais justos; casaco do Bankai aberto na frente, laterais e quinas arredondadas ligadas à malha das costas (6×6), mangas em tecido, onda que desce pelo pano e agitação pela velocidade/quedas, abertura na revelação; Zangetsu do Shikai nova; faixa da Zangetsu e corrente da Tensa em tecido; máscara com olhos afiados; Mugetsu como parede fina que avança reto até 50 blocos.

| Cenário extra no Minecraft | Aceitação |
| --- | --- |
| Andar, correr, pular e cair no Bankai | Casaco ondula da cintura à barra; laterais e quinas acompanham as costas sem abrir vão; ao cair o casaco sobe |
| Golpes com a Zangetsu/Tensa | Faixa e corrente chicoteiam no golpe e balançam ao andar |
| G (revelação) | Casaco abre para fora no corte e assenta |
| C no Bankai | Parede finíssima e altíssima avança reto até 50 blocos; dano no mesmo alcance |

## Forma Mugetsu como traje completo (03/11/2026)

Pedido: "quando usar o Mugetsu deve perder toda a roupa e substituir por essa forma" (imagem de referência) e "cabelo deve ter o cloth do casaco".
- `build_ichigo_traje.mugetsu()` → `geo/armaduras/ichigo_mugetsu` (217 cubos, 110 ossos): cabelo preto até os joelhos em 4 mechas × 6 segmentos (`mg_cabelo_*`, presas à cabeça), faixa cinza no nariz e boca, tronco cinza nu enrolado em faixas escuras, braço do lado direito de quem olha em pele com listras pretas, hakama preta longa e larga com barra rasgada (as mesmas caudas em tecido do casaco + frente fechada `mg_saia_frente_meio_*`), pés enfaixados com os dedos de fora.
- Durante a Mugetsu: `RenderTrajeAnime` esconde todos os trajes de anime, a `CamadaHollow` desenha a forma (`RenderBankaiIchigo("ichigo_mugetsu")`) e o `TecidoIchigoCliente` continua rodando. Cabelo: mola própria, mais leve (sobe na queda, abre em leque com a agitação); frente da saia é empurrada pelas pernas.
- No fim: forma some, volta o traje base, Bankai acaba e 60 s sem poderes (sem mudança).
- Removidos: partes de caixas antigas da `CamadaHollow` (casaco e forma Mugetsu) e as texturas `textures/ichigo/mugetsu.png` e `bankai_coat.png`.
- Prévia: `python -B tools/preview_ichigo_traje.py --output <pasta> --estilo ichigo_mugetsu`.

## Retorno do teste no jogo (preview.3) e correções

| Retorno | Correção |
| --- | --- |
| Faixa da Zangetsu e corrente da Tensa sem cloth | O movimento existia mas era fraco: o item segue o braço/câmera, não o deslocamento. `TecidoEspadaIchigo` agora é movido por giro/arfagem da câmera, balanço do braço, golpe e deslocamento, ganho 2,2–3,4× e tremor contínuo descendo a tira. **Conferir de novo no jogo** (a prévia offline não mostra a espada na mão) |
| Casaco: frente não ligada à lateral | Tiras da frente e quinas novas frente↔lateral (`quinaf_*`, 45°) seguem a mesma coluna externa da malha das costas: costas, quinas, laterais e frente são um pano só |
| Mugetsu: mesmo problema na frente | Mesma correção (usa as mesmas caudas) |
| "Ignora o cabelo, foca nos detalhes das faixas" | Cabelo removido. Faixas: rosto em três voltas sobrepostas com nó na nuca e duas pontas soltas em tecido (`mg_faixa_*`); tronco com 10 voltas de larguras e tons alternados; braços com 8 voltas (por fora da manga ajustada pelo `polish`); ponta solta no quadril; faixa na cintura; pés em X |

## JAR de teste — 3.10.0-ichigo-preview.4 (03/11/2026)

`minezin-outputs/minezin-3.10.0-ichigo-preview.4.jar`. Build limpo, testes passando, protocolo 16. Inclui: cloth da faixa/corrente movido por câmera, braço e golpe; frente do casaco e da saia da Mugetsu ligadas às laterais (quinas frente↔lado); forma Mugetsu sem cabelo e com faixas detalhadas. **Conferir no jogo:** faixa da Zangetsu e corrente da Tensa balançando ao virar a câmera, andar e golpear.
