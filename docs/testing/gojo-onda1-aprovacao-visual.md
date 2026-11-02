# Gojo — onda de qualidade: aprovação visual

Base auditável: `fc561496e7419c761efd64c7d32515e4dd88b984` (`main`, merge do PR 25).
Branch de trabalho: `feat/gojo-onda-qualidade`. Publicação em PR autorizada pelo usuário após entrega do JAR e do workflow/roadmap; preparação do PR em 02/11/2026.

## Vazio Roxo

Direção visual e movimento dos raios aprovados pelo usuário nesta conversa.
Núcleo violeta, coroa de faixas de energia, raios com direções independentes nos três eixos e ramificações móveis.
`CoroaRoxoGojo` e `RaiosRoxo` fornecem geometria determinística compartilhada com as prévias.

## Azul — aprovado

`FluxoAzulGojo` fornece fluxos convergentes em planos inclinados e pulsos que viajam da periferia ao núcleo.
Arcos próximos ao núcleo, núcleo escuro com borda ciano e duas camadas de brilho nos fluxos.
Materiais exclusivos 10 e 11 em `gojo_energy.fsh`; o Azul usado na fusão do Roxo continua usando o material anterior 0.
Qualidade normal: 12 fluxos com 32 segmentos, duas fitas por segmento, mais 3 arcos de 48 segmentos (912 quads de fitas).
Qualidade reduzida: 6 fluxos com 16 segmentos e 2 arcos de 24 segmentos (240 quads de fitas).

## Vermelho — aprovado

`DescargaVermelhoGojo` fornece arcos de preparação, cone de repulsão e três ondas de pressão.
O cone completo surge no tick 13; não há projétil viajando que contradiga o impacto do servidor.
Alcance longitudinal visual preservado em 22 blocos a partir da origem. Duração preservada em 28 ticks.
Materiais exclusivos 12 e 13; o Vermelho usado na fusão do Roxo mantém o material 1.
Núcleo branco incandescente e contorno carmesim, duas camadas de brilho e expansão lateral na descarga.
Qualidade normal: até 960 quads de fitas na descarga; reduzida: 336 quads.
`tools/preview_gojo_red.py` usa geometria Java exportada em `build/gojo-vermelho-samples.json`.
Prévia animada em velocidade normal; quadro inserido amplia o foco, sem ser uma segunda habilidade.

## Vazio Infinito — aprovado

`VazioInfinitoGojo` fornece fluxos convergentes de informação em coordenadas locais, intensificados na abertura e atenuados depois.
Interior sem esfera negra, névoa e seu movimento aprovados pelo usuário. Ajuste seguinte: linhas em ciano, azul, violeta e magenta, com gradientes suaves e pulsos mais claros. As cores são exportadas da mesma função Java usada no runtime; a névoa não foi alterada nesta revisão.
Domo exterior escuro (material 15), céu de estrelas com névoa branca e azul entre áreas de vazio (14), fluxos (17) e piso astral (18).
Revisão solicitada pelo usuário: esfera negra central e seus anéis removidos. Inspiração na composição do vídeo local `nmvkhLz8t7I`, especialmente a sequência interna a partir de aproximadamente 25–32 s. Não foram copiados áudio, quadros, personagens ou texturas do vídeo para o mod.
Estrelas permanecem ancoradas no céu. A névoa é transportada angularmente, com deriva e deformação secundária das bordas; a cintilação das estrelas varia separadamente.
Revisão de movimento: velocidade anterior quase imperceptível aumentada, mantendo movimento contínuo e evitando rotação das coordenadas das estrelas. A ferramenta mede a diferença entre quadros separados por dois segundos para verificar legibilidade do movimento na cena offline. Tamanho mínimo das estrelas acompanha a derivada da projeção para reduzir cintilação por subpixel.
O exterior retorna antes de desenhar objetos internos. A casca opaca escreve profundidade para evitar sobreposição por efeitos distantes.
Câmera externa não recebe o céu interno; a presença e a restauração do ambiente continuam sob `PerfilVfxGojo` e `AmbienteVazioInfinito`.
Raio de 26 blocos, abertura de 35 ticks e duração de 400 ticks preservados.
Superfícies esféricas do Domínio usam 32 subdivisões (12 no modo reduzido); outras técnicas mantêm suas subdivisões anteriores.
Fitas: 64 fluxos × 14 segmentos × 2 camadas = 1792 quads na qualidade normal; 24 × 8 × 2 = 384 no modo reduzido.
Revisão de densidade solicitada: 32 → 64 linhas, paleta mais saturada, núcleo e halo mais largos e ganho emissivo maior. A presença residual dos fluxos passa de 7% a 20% após a abertura, mantendo seu movimento. Os segmentos foram ajustados para conter o aumento de geometria (1536 → 1792 quads). A névoa não foi alterada. Piso: 512 quads (256 no reduzido).
Prévia: `tools/preview_gojo_domain.py`, pontos exportados em `build/gojo-dominio-samples.json`.
A prévia inclui plano opaco e grade para verificar a leitura de domo acima do solo e a oclusão; não representa terreno do Minecraft.

## Transformação — entrada e aura aprovadas

A apresentação anterior do pacote 200/kit 1 garantia o Domínio, mas retornava antes de desenhar uma aura do Gojo.
`RenderDespertar` agora encaminha a forma ativa para `RenderGojo.despertar`, seguindo posição interpolada e direção horizontal do jogador vivo, sem alterar o estado no servidor.
`AuraDespertarGojo` calcula filamentos azul/branco, partículas ascendentes e uma onda no tick 35, sincronizada com a abertura existente do Domínio.
Camadas largas de energia usam material 20 e filamentos usam 19, exclusivos desta aura.
O corpo da prévia usa a geometria e a textura existentes do traje Gojo sobre manequim neutro estático; não demonstra animação corporal nova nem skin final.
Idade visual = duração existente de 400 ticks menos o prazo recebido mais o tempo decorrido. Observadores tardios não repetem a entrada.
Cancelamento, morte, troca de mundo e desconexão seguem a limpeza existente; o render ignora donos ausentes/mortos.
Não foram adicionados pacotes, bônus, alterações de duração ou novos efeitos globais de câmera.
Entrada e aura aprovadas pelo usuário. Moveset transformado confirmado em `MovesetsServidor`: Morte Cerebral pelo olhar, Teleporte no Domínio, Chuva de Informações e Roxo Máximo. O efeito 30 é usado pela Frieren; não é um Azul Máximo do Gojo.
Ferramenta: `tools/preview_gojo_transformation.py`; amostras: `build/gojo-despertar-samples.json`.

## Chuva de Informações — aprovada

Efeito 40, kit 1: o servidor aplica Cegueira/Wither existentes na ativação e emite apresentação de 70 ticks. A revisão apenas substitui a composição genérica no cliente.
`ChuvaInformacoesGojo` gera 48 rastros descendentes (20 no reduzido) com fragmentos de símbolos e a paleta colorida aprovada do Domínio.
Cada rastro suaviza sua entrada/saída ao reaparecer; todos os pontos permanecem dentro do raio existente de 26 blocos.
Material 21 exclusivo. 48 × 12 × 2 quads de faixas + 48 × 4 × 2 fragmentos = 1536 quads no normal; 320 no reduzido.
Não representam trajetórias de colisão ou projéteis novos; são uma apresentação da sobrecarga de informações já aplicada pelo servidor.
Prévia: `tools/preview_gojo_information_rain.py`, geometria em `build/gojo-chuva-samples.json`, com fundo cósmico existente em composição offline.

## Roxo Máximo — cutscene aprovada

`UltimatesTransformadas` inicia a câmera tipo 32 somente para o lançador local, ao receber o efeito aceito pelo servidor. Observadores recebem os VFX, sem assumir a câmera.
`RoxoMaximoGojo` define abertura no personagem, transição para o foco fixo, órbita e enquadramento amplo do impacto. A trajetória ideal inclui uma contenção antes do tick 90 e um recuo/FOV mais brusco no clímax; evita o núcleo e permanece no interior do Domínio (margem de 1,2 bloco no núcleo e raio de câmera de 24,8 blocos no Domínio).
O tratamento de colisão da câmera existente continua ativo. Testes puros cobrem focos centrais, próximos da borda e verticais; colisões com terreno real permanecem pendentes.
Raio crescente existente `1 + tick × 0,1`, duração de 110 ticks e colapso no tick 90 preservados. A esfera continua representando a área ativa após o marco do colapso; uma casca de plasma e três ondas de pressão mais amplas acentuam esse marco. A expansão externa é cosmética; o raio de dano existente permanece igual.
A animação corporal existente `desperto_1_32` dura 5,5 segundos e já é iniciada para o lançador e observadores. A prévia amostra seus canais sobre o manequim com o traje atual; não valida a integração PlayerAnimator/GeckoLib no cliente.
Câmera, FOV e tremor locais encerram/restauram pelo gerenciador existente. Cancelar a forma agora encerra também a câmera 32. Morte, saída, troca de mundo e desativação de cinemáticas usam a limpeza existente.
Impact frames usam o pós-processamento nativo descrito abaixo quando disponível; a rotina GUI anterior é suprimida nesse caso e permanece como fallback. O enquadramento do clímax permanece dentro do raio de visibilidade do cue existente.
Uma carga contínua procedural `purple_maximum_charge` no tick 0, sem pulsação periódica, e SFX procedural `purple_maximum_release` no tick 90, no grupo cancelável da ultimate. A carga dura 4,5 s e deixa 0,25 s de silêncio antes do impacto. A liberação tem 2,4 s, 48 kHz, mono Vorbis, com subgrave restrito ao transiente inicial e cauda de ruído aperiódico; gerador reprodutível `tools/build_roxo_maximo_sfx.py`. Legenda reutiliza a tradução existente de liberação roxa. Não foi copiado áudio do anime.
Prévia `tools/preview_gojo_maximum_purple.py`: câmera/geometria Java exportadas em `build/gojo-roxo-maximo-samples.json`, shaders do mod, poses amostradas e SFX existentes. A revisão de impacto executa o shader de pós-processamento real sobre cor e profundidade da cena offline; o mixer não reproduz a atenuação espacial do Minecraft. FOV base da prévia: 70.
Saídas: GIF, MP4 com SFX e painel de fases. Teleporte no Domínio e indicação visual da Morte Cerebral aprovados. Terreno, destruição real, oclusão, configurações do usuário e funcionamento de cliente/multiplayer exigem teste no Minecraft.

## Revisão de impacto do Roxo Máximo — aprovada

`CinemaRoxoMaximo` usa a mesma abordagem nativa de pós-processamento empregada pela Atomic: `PostChain` próprio, alvo temporário e cópia de cor preservando o depth buffer principal. Arquivos e efeitos da Atomic permanecem inalterados.
Recursos exclusivos: `shaders/program/roxo_maximo_cinema.*`, `shaders/post/roxo_maximo_cinema.json` e `shaders/core/roxo_maximo_copy.*`.
Apenas o lançador local recebe esse passe. Respeita `gojoVfx`, `cinematicas`, `vfxReduzidos`, `impactFrames` e `claraoGojo`; os dois últimos controlam os quadros de contraste/exposição. Uma Atomic visível tem prioridade para evitar empilhar os dois passes.
Antes do tick 90: compressão de cor, distorção convergente e contenção escura. No impacto: exposição gráfica, negativo/silhuetas, descarga branca/violeta, separação cromática e bloom radial limitado a oito amostras. O peso volta a zero no tick 110.
Não substitui o slot de shader do GameRenderer; evita atuar enquanto um framebuffer externo está ativo. Recursos são liberados na saída, troca de mundo, recarga, desativação ou fim; falhas desligam o passe até recarga, preservando o fallback.
Verificações GPU na prévia: identidade no início/fim, contenção escura perceptível, impacto com flashes desligados distinto da exposição, quadros claro/escuro distintos e profundidade fonte intacta. Métricas em `outputs/gojo-roxo-maximo-impacto-gpu.json` fora do repositório.
Compatibilidade com shader packs, ordem de composição com mods externos, desempenho e legibilidade dentro do Minecraft continuam pendentes.

## Teleporte — aprovado

Efeito 41 / kit 1, compartilhado pelo Teleporte Curto e pelo Teleporte no Domínio: VFX de saída e chegada nos pontos enviados pelo servidor.
`TeleporteGojo` preserva ambas as extremidades inclusive em deslocamentos verticais. O rastro residual aparece inteiro e some em 6 ticks; não apresenta deslocamento progressivo do jogador.
Ruptura azul/branca na saída, chegada expandindo e fragmentos locais. Apresentação total de 18 ticks, sem câmera adicional, alterações de recarga, distância ou colisão.
A prévia usa a geometria Java e um manequim em pose estática, que muda de ponto instantaneamente no início do efeito; não valida o teleporte no Minecraft.
Ferramenta `tools/preview_gojo_teleport.py`, amostras `build/gojo-teleporte-samples.json`. Material exclusivo 24.
Morte Cerebral aprovada abaixo. Tecido Gojo implementado para revisão visual na seção correspondente.

## Morte Cerebral — aprovada

Novo tipo visual 42/kit 1 no `PacoteEfeitoDespertar` existente, com apresentação de seis ticks no ponto de acerto confirmado. Não há novo pacote ou mudança do formato de rede.
`MovesetsServidor.morteCerebral` preserva seleção, alcance e chamada de dano existentes; emite o VFX apenas quando `hurt(...)` retorna true. A cadência do servidor permanece a existente: verificação a cada dez ticks da forma ativa.
`MorteCerebralGojo` agora usa 12 linhas na paleta colorida do Domínio, descendo na vertical do mundo até a cabeça do alvo. Revisão solicitada: substituídos os fragmentos radiais e os traços vindos do lançador por informação caindo de cima para baixo. A textura também flui para baixo. Não há som repetido ou reinício da animação corporal.
Testes verificam queda monotônica, convergência próxima à cabeça, valores finitos e encerramento em seis ticks.
É uma indicação curta no ponto enviado, não um acompanhamento de entidade: não foi adicionado identificador do alvo ao pacote. Alvos em movimento e confirmação de dano bloqueado exigem validação dentro do Minecraft.
Material 25, ferramenta `tools/preview_gojo_passive_hit.py`, amostras `build/gojo-olhar-samples.json`.
Prévia com o modelo/skin existente do Toji em pose estática; a repetição a cada dez ticks é ilustrativa, não uma captura de combate.

## Tecido Gojo — aprovado visualmente

Barra externa dividida em quatro faces, dez colunas e dois segmentos por coluna; gola inteira em um osso independente para preservar as junções dos cantos. Corpo central da jaqueta permanece sob os painéis. Cabeça, braços, mangas, ombros, pernas, botas e botão preservados em relação à geometria base arquivada em `art/gojo/gojo-base.geo.json`. Textura original preservada.

`TecidoGojo` adapta a malha de partículas existente (`MalhaTecido`, inalterada) para painéis curtos com borda superior fixada e plano de suporte local. Cada face recebe a velocidade projetada na sua própria direção. Limite principal de flexão da barra: 0,24 rad por segmento; gola usa a mola amortecida existente com limite de 0,035 rad por eixo, aproximadamente dois graus. Não é uma simulação contínua da malha visual nem colisão exata com o corpo inteiro.

`TecidoGojoCliente` avança a física em ticks de cliente e interpola durante a renderização, por UUID, com limite de 64 estados. Reinicia em teleporte superior a dois blocos, troca de mundo/entidade ou equipamento; limpa na saída e ao desativar `tecidoGojo`. Pausa respeitada. `RenderTrajeGojo` aplica as poses e restaura rotações em `finally` para evitar contaminação entre jogadores e passes.

Impulsos leves do despertar, Roxo Máximo e Teleporte aproveitam efeitos já aceitos pelo cliente. Nenhum pacote adicional, mudança de gameplay ou alteração do tecido do Cid. O renderer de inventário permanece estático.

Geração reproduzível: `python -B tools/build_gojo_cloth.py`; altera apenas geometrias Gojo vestido/peitoral e o projeto Blockbench correspondente. UVs recortadas das ilhas existentes, com mínimo de um pixel por face. `TecidoGojoTest` verifica independência entre amostragem e simulação, resposta direcional, limites em queda/giro, amortecimento, reinício e preservação dos membros. Exporta 480 poses interpoladas em `build/gojo-cloth-samples.json`.

`tools/preview_gojo_cloth.py` renderiza modelo/textura reais sobre manequim fixo em OpenGL, usando essas poses Java. Sequência: repouso, corrida, passo lateral, giro, salto, queda, pouso e retorno amortecido. Saídas externas ao repositório: `outputs/gojo-traje-tecido.mp4` e `outputs/gojo-traje-tecido-fases.png`. Não é captura do Minecraft. Encaixe com animações corporais, agachamento, primeira pessoa, observadores remotos e colisões visuais continuam pendentes no jogo.

## Verificação e limites

Build Java/Forge e 167 testes Java passaram; três testes Python passaram.
Shaders base e aditivo compilados e renderizados em OpenGL na RTX 5060 Ti.
Prévias usam os pontos exportados dos testes Java e materiais GLSL do mod.
Ferramentas: `tools/preview_gojo_wave.py` e `tools/preview_gojo_blue.py`.
Saídas Java declaradas no Gradle: `build/gojo-roxo-samples.json` e `build/gojo-azul-samples.json`.

São cenas offline com grade espacial: não executam Minecraft, Photon ou shader packs. Quando presentes, manequins e poses amostradas dos arquivos servem para revisão da composição; não validam a animação do jogador no cliente.
Não implementam refração do cenário: a sensação de distorção usa volumes e fluxos locais.
Integração no cliente, perspectiva de primeira pessoa, multiplayer, shader packs e desempenho no jogo permanecem pendentes.
Não houve alteração das regras de dano, alcance, duração ou progressão. O traje do Gojo agora possui física secundária na barra e gola. A Morte Cerebral emite uma indicação visual pelo pacote de efeitos já existente; formato e protocolo de rede permanecem iguais.
Tecido aprovado pelo usuário. JAR 3.7.0-gojo-preview.1 entregue para teste; publicação em PR autorizada posteriormente. Testes dentro do Minecraft permanecem pendentes.

A exportação de prévias de `ComposicaoCidTest` excedeu o heap na primeira validação desta etapa. A serialização foi alterada para gravar diretamente em Writer, evitando a cópia integral do JSON em memória; a geometria e os efeitos do Cid não foram alterados.

Revisão de áudio após aprovação do impacto visual: removida a segunda chamada de carga no tick 45. Novos arquivos de carga/liberação são one-shots com evolução contínua e cauda difusa, sem LFO de volume ou grave tonal sustentado. As imagens e os tempos da cutscene permanecem inalterados.

## Publicação — PR #26

Commit inicial da onda: `2a85f00e27f8f13c305872f5a4bdc68b8ab6b261`, PR https://github.com/Cauan-Silva/Minezin/pull/26. A tentativa de aprovação formal foi recusada pelo GitHub porque a conta conectada é autora do PR; isso não equivale a uma revisão aprovada ou a merge.

O primeiro CI passou build/testes, mas encontrou conflito entre a função local `noise3(vec3)` e a função reservada do GLSL. Renomeada para `gojoNoise3`, sem alterar cálculos, parâmetros ou aparência; compilação/link/render local novamente aprovados. O JAR entregue antes dessa correção permanece identificado pelo hash original no workflow/roadmap. O artefato do CI atualizado contém a correção de compatibilidade.
