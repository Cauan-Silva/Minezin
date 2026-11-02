# Frieren — onda de qualidade

Iniciada em **02/11/2026**, sobre a `main` em `1ec0aab` (merge do PR #26, onda Gojo). Alterações locais, sem commit. Mecânicas do servidor (dano, alcance, atração, cratera, tempos) preservadas.

## Buraco Negro — aprovado em prévia

Referência fornecida pelo usuário: cena do clone da Frieren usando o buraco negro (vídeo do anime). Uma primeira versão cinematográfica (selos dourados em giroscópio, disco de acreção) foi **rejeitada** por fugir do visual do anime; a versão abaixo segue a cena e foi aprovada pelo usuário.

| Ticks | Fase | Apresentação |
| --- | --- | --- |
| 0–50 | Formação na mão | Núcleo negro pequeno, borda amarela, brilho laranja, traço vermelho horizontal e anéis de ar distorcido |
| 50–70 | Lançamento | Voo acelerado com arco curto e rastro laranja até o centro; câmera da cutscene acompanha o voo |
| 70 | Abertura | Chegada coincide com o início da atração do servidor; impact frame laranja |
| 70–196 | Atração | Traço horizontal longo, linhas de vento azul-claras, detritos visuais em órbita |
| 196–220 | Tensão | Detritos compactam numa casca oval com borda rosa e rachaduras que se abrem |
| 220 | Colapso | Espinhos violeta, clarão, impact frame violeta |
| 220–280 | Dissipação | Rachaduras ciano, faíscas brancas e estilhaços |

Implementação:

- `core/vfx/BuracoNegroFrieren`: geometria e tempos puros, presos a `PerfilFrieren` (`ABERTURA`, `COLAPSO`, `FIM`).
- `RenderMagiasFrieren.buracoNegro`: render do efeito 24 (também usado pelo C da forma desperta).
- `anime_energy.fsh`: modos 16 (brilho da singularidade), 17 (traço horizontal) e 18 (espinho).
- `EfeitosFrieren`: cores de impact frames e emissores; HUD antigo corrigido para "[V] Gaiola de Vento".
- `BuracoNegroFrierenTest`: marcos sincronizados com o servidor, casca/rachaduras, limites e exportação para `build/frieren-buraco-samples.json`.

Prévia: `python -B tools/preview_frieren_black_hole.py --output <pasta>` (após `gradlew test`). Usa shaders e geometria do mod e os SFX atuais; não mostra terreno, impact frames, detritos orbitando nem a física real dos blocos. Não é captura do Minecraft.

Verificação local: build Forge e 171 testes Java, zero falhas. **Teste no jogo pendente.**

Pendências conhecidas do Buraco Negro: SFX ainda são os anteriores (`singularity_charge`/`singularity_collapse`), com sons do Minecraft tocados pelo servidor por cima.

## Zoltraak e Barreira Arcana — aprovados em prévia

Referências pesquisadas na wiki da Frieren (imagens do anime): círculo do Zoltraak com flor de seis pétalas (ep. 2), Zoltraak branco (ep. 8) e preto da Qual (ep. 3), barreira em favo e quebra de barreira (ep. 21).

- **Zoltraak (efeito 21):** círculo com aro duplo, faixa de runas, flor de seis pétalas com círculo concêntrico em cada pétala, triângulos e centro, em traço branco/ciano; abre em 3 ticks e some em 30. Feixe branco grosso com halo lilás-azulado, estrias e faíscas, já inteiro no tick do dano do servidor. Dourado removido.
- **Barreira (efeito 22):** favo de 7 hexágonos de topo reto à frente da Frieren, seguindo o olhar; acende do centro para fora; contorno ciano e vidro translúcido. Esfera e selo dourado no chão removidos.
- **Quebra (efeito 26):** ao esgotar a reserva, cada hexágono se parte em 6 cacos que voam e somem em 20 ticks (antes apenas um pulso de partículas).

Implementação: `core/vfx/MagiasBaseFrieren` (geometria e tempos), `RenderMagiasFrieren.zoltraak/barreira/estilhacos`, `EfeitosFrieren` (efeito 26 visual), `MagiasBaseFrierenTest` (flor fechada e simétrica, favo encaixado, cacos, sincronia do feixe; exporta `build/frieren-base-samples.json`). Prévia: `python -B tools/preview_frieren_base.py --output <pasta>`.

Guardado para o boss: Zoltraak original da Qual em negro-violeta com borda granulada e anéis de pontos nos disparos.

Verificação local: build Forge e 176 testes Java, zero falhas. **Teste no jogo pendente.**

## Forma desperta — aprovada em prévia

Referências na wiki da Frieren: mana revelada (ep. 10, e coluna de luz da Fern), Judradjim (ep. 25) e painéis da magia defensiva (ep. 3). "Julgamento" e "Escudo Celestial" são nomes do GDD sem magia equivalente no anime; usam as magias canônicas mais próximas.

- **Liberar Mana (efeito 200, kit 3):** coluna translúcida azul-pálida voltada para a câmera (material 19), larga no surto e estável durante os 45 s, com estrias e partículas. Selo e fitas douradas removidos.
- **Vinte Zoltraak (37/113):** feixe branco-lilás e círculo de flor menor em cada raio.
- **Julgamento (38 e 115):** aviso com círculo de flor e anel que fecha no tick 25 do servidor, círculo violeta no céu; no impacto, relâmpagos violeta animados e ramificações no chão. O servidor passou a enviar o raio como efeito **115** (antes 113, compartilhado com os Vinte Zoltraak); dano, tempo e área inalterados.
- **Escudo Celestial (39):** cúpula de ~170 painéis hexagonais ciano no raio protegido de 8 blocos, acendendo de cima para baixo e dissolvendo no fim.
- Impact frames de 38/39 em violeta/ciano (`RoteiroUltimate`).

Implementação: `core/vfx/FormaFrieren`, `core/frieren/RenderDespertarFrieren` (roteado por `RenderDespertar` para o kit 3), `FormaFrierenTest` (exporta `build/frieren-forma-samples.json`). Prévia: `python -B tools/preview_frieren_forma.py --output <pasta>`.

Verificação local: build Forge e 180 testes Java, zero falhas. **Teste no jogo pendente.**

## Impulso Arcano e Gaiola de Vento — aprovados em prévia

- **Impulso Arcano (efeito 23):** a magia de voo do anime (ep. 2) não tem brilho; ficou só rajada radial no chão, linhas de vento subindo pelo corpo e poucas partículas no rastro. Anéis dourados removidos.
- **Gaiola de Vento (efeito 30, kit 3):** tornado de poeira bege em faixas diagonais com cel-shading (material 20), estreito embaixo e largo em cima, poeira na base e detritos; referência no Waldgose (ep. 21, magia do Denken, única magia de vento do anime). Raio máximo 4,8, dentro da atração de 5 blocos do servidor. Selo dourado e espirais verdes removidos.

Implementação: `MagiasBaseFrieren` (impulso/gaiola), `RenderMagiasFrieren.impulso`, `RenderDespertarFrieren.gaiola`; testes em `MagiasBaseFrierenTest`. Prévia incluída em `tools/preview_frieren_base.py`.

Verificação local: build Forge e 181 testes Java, zero falhas. **Teste no jogo pendente.**

## Traje e cajado — aprovados em prévia

Referência: perfis oficiais do anime na wiki da Frieren.

- **Cajado:** haste vinho, cabeça em lua crescente dourada envolvendo gema vermelha redonda engastada, cordão vermelho com duas fitas e ponteira dourada. Corrigido o sinal da rotação Z dos segmentos (GeckoLib gira no sentido horário para ângulos positivos), que deixava o anel antigo espinhento.
- **Traje:** capinha contínua até um terço do tronco com barra dourada e aberta na frente (camisa listrada só na gola); vestido com saia evasê acima do joelho e barra dourada, deixando a meia escura visível; mangas largas com punho dourado; gola alta, cinto escuro, botas marrons.
- **Tecido:** `TecidoFrieren` (mesmo `MalhaTecido` do Gojo/Cid) com 4 painéis de capinha e 4 de saia, 54 ossos encadeados; 20 Hz fixos, reinício em teleporte/troca de mundo/entidade, cache de 64 jogadores; `TecidoFrierenCliente`, `RenderTrajeFrieren` e opção de cliente `tecidoFrieren`. A saia saiu das pernas (item de calça) para o peitoral, como a barra do Gojo.
- **Geração:** `python -B tools/build_frieren_cloth.py` regenera só a Frieren (geo, ícones, textura rasterizada do SVG e projeto Blockbench); `build_armor_models.build(only=...)` evita sobrescrever as etapas de tecido do Gojo/Shadow.
- **Testes:** `TecidoFrierenTest` (ossos encadeados, amostragem sem avançar física, limites, repouso, resposta ao passo; exporta `build/frieren-cloth-samples.json`); `AnimeAssetsTest` atualizado para a saia no corpo. Prévia: `python -B tools/preview_frieren_cloth.py --output <mp4> --poster <png>`.

Verificação local: build Forge e 186 testes Java, 3 testes Blockbench, zero falhas. **Pendente no jogo:** encaixe com animação corporal, primeira pessoa, pernas andando dentro da saia, skin slim e observadores.

## Qual — aprovada em prévia

Referência: perfil oficial do anime na wiki (aparência e cena do ep. 3).

- **Modelo:** rosto de máscara com marcas marrom-claras, olhos fechados e dentes serrilhados; chifres amarelos irregulares; cabelo loiro longo nas costas e barba até a cintura; pele cinza-amarronzada com faixas tatuadas e grampos; capa de pele creme nos ombros; punhos de pele; quatro garras vermelho-escuras por mão; saia cinza-escura até o chão com corrente e colar de presas. Removidos o antebraço extra (`lower_arm_*`) e as tiras de manto. Escala de 5 blocos e hitbox 2,1 × 5 inalteradas; passo das pernas reduzido para os pés não saírem da saia.
- **Zoltraak original (efeitos 403/413, kit 3):** carga com dois anéis de pontos de luz na origem do disparo (altura dos olhos, de onde o servidor dispara) e anel no chão fechando no tick de liberação; feixe com núcleo quase preto, halo violeta e borda granulada cintilante (material 21). Dano, alcance e tempo inalterados.
- **Geração:** `tools/boss_reference_shapes.py` (`qual`), `tools/skin_boss_models.py`, `tools/build_boss_skin_svg.py` (SVG → PNG), `tools/build_nemesis_models.py`; projeto `art/blockbench/qual.bbmodel` atualizado. Toji, Aurora e Liebe sem alterações.
- **Testes:** `BossAttachmentsTest` adaptado (chifres conectados após o conversor real do GeckoLib; garras dentro da rotação do braço; sem antebraço extra). Prévia: `python -B tools/preview_qual_zoltraak.py --output <pasta>`.

Verificação local: build Forge, 186 testes Java e 3 testes Blockbench, zero falhas. **Pendente no jogo:** animações reais, leitura do ataque, desempenho.

## SFX — aprovados em prévia

Quatorze sons procedurais originais (sem samples), Vorbis mono 48 kHz, legendas pt_br/en_us, gerados por `python -B tools/build_frieren_sfx.py` (atualiza só essas entradas em `sounds.json`, idiomas e `art/audio/anime-sfx-manifest.json`).

| Som | Uso |
| --- | --- |
| `frieren_cast` | Zoltraak (21) e cada um dos Vinte Zoltraak (113, volume menor) |
| `frieren_barrier` / `frieren_barrier_break` | Barreira (22) / quebra (26) |
| `frieren_impulso` | Impulso (23) |
| `frieren_buraco_carga` / `_atracao` / `_colapso` | Buraco Negro nos ticks 0, 70 e 220 (`RoteiroAudio.singularidade`) |
| `frieren_mana` | Entrada de Liberar Mana (200, kit 3) |
| `frieren_julgamento_carga` / `_trovao` | Julgamento: ticks 0–25 e trovão no tick do dano (`RoteiroAudio.julgamento`) |
| `frieren_escudo` | Escudo Celestial (39) |
| `frieren_gaiola` | Gaiola de Vento (30, kit 3) |
| `qual_zoltraak_carga` / `qual_zoltraak` | Qual: carga (403) e disparo (413) |

Removidos os sons vanilla que o servidor sobrepunha nas técnicas da Frieren (ametista, beacon, élitro, sonic boom, explosão genérica); mantido o toque de ametista ao absorver golpe. Com `sfxAnime` desligado as técnicas ficam sem som. Nomes de som em `CatalogoSons` (sem Forge), usados por `RegistroSons` e por `SfxAnimeTest`, que confere registro, arquivos e sincronia.

Verificação local: build Forge e 186 testes Java, zero falhas. A prévia não reproduz a atenuação espacial do Minecraft.

## JAR de teste — 3.8.0-frieren-preview.1

Gerado em 02/11/2026 a partir das alterações locais sobre `1ec0aab` (sem commit; não há SHA de código final).

```text
minezin-3.8.0-frieren-preview.1.jar
Tamanho: 4.467.681 bytes
SHA-256: cc9a55d2e46f31fc99862cdd33f13a90a62c734258e0cfff2848e8a43e6b69ae
```

Conferido: build limpo com 186 testes Java e 3 Blockbench; zip íntegro (561 entradas); `mods.toml` 3.8.0-frieren-preview.1; classes, modelos, texturas, shaders (modos 16–21) e os 14 SFX presentes; nomes SRG no bytecode (reobfuscado). Protocolo de rede 15 mantido, mas há efeito novo (115): cliente e servidor precisam da mesma versão. Não houve instalação automática no Minecraft.

### 3.8.0-frieren-preview.2 — correção do Zoltraak

Teste no jogo da preview.1: o Zoltraak apontava sempre para a mesma direção. Causa: na reescrita do efeito 21 o alvo do pacote (coordenadas absolutas do mundo) passou a ser usado como vetor relativo à ponta do cajado. Correção em `RenderMagiasFrieren`: `e.alvo().subtract(e.origem())`. Os demais efeitos já convertiam para coordenadas relativas. A prévia offline não detectava o erro porque montava o alvo já relativo.

```text
minezin-3.8.0-frieren-preview.2.jar
Tamanho: 4.467.678 bytes
SHA-256: f38a09c169126df9c1ba4288225f34ebe49dcee3282fe53f3712c3414f9ad109
```

Build limpo, 186 testes, zip íntegro, versão e reobfuscação conferidas. Substitui a preview.1.

### Roteiro no Minecraft

Usar mundo de testes ou cópia; substituir o JAR anterior do Minezin sem deixar duplicatas e conferir a versão carregada. Preparação (cheats, permissão 2): `/minezin rpg maestria maxima frieren`, selecionar o kit em P, `/give @s arcanecontrol:cajado_frieren` e as quatro peças `arcanecontrol:frieren_cabeca/peito/pernas/botas`; `/minezin rpg restaurar_recursos` entre os testes.

| Cenário | Aceitação |
| --- | --- |
| Inicialização | Cliente e servidor carregam sem erro; sons, shaders e modelos aparecem |
| Z Zoltraak | Círculo de flor na ponta do cajado (mão direita e esquerda), feixe branco grosso, faíscas, impacto; som de disparo |
| X Barreira | Favo de 7 hexágonos à frente, seguindo o olhar; ao absorver golpes até esgotar, estilhaça com som de vidro |
| C Impulso | Salto com rajada discreta e partículas; queda lenta |
| V Gaiola de Vento | Tornado de poeira no ponto mirado, puxa inimigos; uivo de vento por 6 s |
| G Liberar Mana | Coluna azul-pálida, levitação, tremor de câmera; som de liberação |
| Forma: Z Vinte Zoltraak | Raios brancos com círculo menor saindo ao redor da Frieren até o alvo; rajada sonora |
| Forma: X Julgamento | Círculo no chão com anel que fecha, círculo violeta no céu; relâmpagos e trovão no impacto |
| Forma: C Buraco Negro | Forma na mão, voa até o alvo, câmera acompanha; atração, casca rachada, explosão violeta e faíscas; três sons sincronizados; testar com destruição de terreno ligada e desligada e cancelando antes do colapso |
| Forma: V Escudo Celestial | Cúpula de painéis ciano no raio de 8 blocos; aliados protegidos por 6 s |
| Traje | Capinha e saia reagem a correr, passo lateral, girar, saltar, cair e pousar; pernas andam dentro da saia; ombros e mangas sem buracos; skin Steve e Alex; primeira pessoa |
| Qual | Altar com alma da Frieren; Qual de 5 blocos reconhecível; carga com anéis de pontos e Zoltraak preto alinhados; dano coerente com o feixe; passo curto dentro da saia; sons de carga e disparo |
| Opções | `tecidoFrieren`, efeitos reduzidos, impact frames e `sfxAnime` desligados sem quebrar as técnicas |
| Ciclo de vida | Morte, logout, troca de dimensão e de kit não deixam coluna de mana, levitação, tornado, som ou tecido presos |
| Multiplayer | Observador vê efeitos, traje e tecido coerentes; sem contaminação entre jogadores |
| Desempenho | Buraco Negro + Gaiola + forma ativa ao mesmo tempo; anotar FPS e hardware; repetir com shader pack se usado |

Para relatar falhas: nome do JAR, tecla e forma, perspectiva, shaders/mods relevantes, passos, esperado e observado; vídeo/screenshot e `latest.log` ajudam.

## Build neste PC

O caminho do usuário contém acento ("Usuário"); o Gradle falha com "Unable to establish loopback connection" a menos que o Java use uma pasta temporária ASCII, por exemplo `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=<pasta ASCII> -Djava.io.tmpdir=<pasta ASCII>`. Ajuste local, não requisito do projeto.
