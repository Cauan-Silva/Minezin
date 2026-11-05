---
tags: [documento]
---
# Cid: tecido conectado e composição visual — 3.6.0-preview.7

Base: `09f2727fc796d02171335b86862b7de33d1fca43` (main, PR #24). Branch local `feat/cid-moveset-tecido-vfx`. Inclui a revisão local preview.6, ainda sem commit. Esta entrega é um piloto para avaliação visual do Cid, não uma declaração de equivalência gráfica às referências ou uma migração dos demais personagens.

## Referências examinadas

Foram extraídas amostras visuais dos arquivos locais fornecidos pelo usuário; não houve extração de código, modelos ou texturas dos mods.

- Ronova (`qu0GozxADPo`, 131,1 s): silhueta ampla, áreas escuras contrastando com bordas luminosas e fases de preparação/liberação. Amostras de 23–46 s para ataques, 54–62 s para composição superior e 77–115 s para ambientação. O vídeo não revela o algoritmo interno do tecido; molas angulares e cadeias de partículas são ambas formas possíveis de física secundária.
- God Wand (`Ex2b0LpqscE`, 344,97 s): amostras próximas de 90–134 s e 306–335 s mostram campo radial, estrutura vertical, concentração e coluna de liberação. Esses elementos orientaram a leitura espacial da Atomic, sem copiar o desenho exato.
- Apex Arcana (`DnmDDFOtYdw`, 197,23 s): amostras em 35–52 s mostram estruturas grandes, em 60–68 s linhas ramificadas, em 93–125 s selos/coluna e em 134–150 s uma forma orgânica ramificada. Referência para escala, hierarquia e formas reconhecíveis à distância. Não foi alegada análise contínua de cada frame dos vídeos.

## Simulação de tecido

`core/client/tecido/MalhaTecido.java` é independente de Minecraft/renderização. Integração Verlet e resolução PBD: partículas conectadas, gravidade, amortecimento, arrasto por deslocamento/variação da velocidade, transporte para o referencial do corpo durante giro e pressão normal variável em movimento lateral. Tick fixo de 20 Hz com três subpassos a 60 Hz e oito iterações de restrições. Topo fixado; restrições verticais/horizontais, diagonais de cisalhamento e flexão a duas linhas de distância.

No Cid são 5×5 partículas, passo lateral de 0,15 bloco e vertical de 5,5/16 bloco. `MantoDirecionalCid` converte as tangentes de altura e largura da malha em orientação tridimensional e depois em rotações relativas dos 20 ossos existentes. Conversão respeita os sinais Y/Z do renderer GeckoLib. A simulação não é avançada pelo render; a interpolação usa os estados do tick anterior e atual.

A barreira local `z>=0` representa aproximadamente as costas, e o limite de altura evita que partículas atravessem a linha dos ombros. Isso não constitui colisão completa com tronco, cabeça, braços, pernas ou mundo, nem autocolisão de tecido. A malha física é projetada no rig segmentado existente, não renderizada como uma superfície contínua com skinning por vértice; dobras fortes ainda podem revelar junções. Agachamento e nado exigem conferência no jogo. Gola e abas conservam molas angulares próprias: não é desejável que tenham o mesmo peso da capa.

Reinício e cache continuam sob `MantoCidCliente`: troca de mundo/entidade, teleporte, morte, remoção do manto e desconexão; máximo de 64 estados. Opção `tecidoCid=false` mantém pose estática. Nenhuma biblioteca ou pacote de rede novo.

## VFX

`core/vfx/ComposicaoCid.java` gera lotes imutáveis de quads em coordenadas locais. Runtime e ferramenta de revisão consomem as mesmas malhas; não há uma reconstrução aproximada em Python dos efeitos novos. `RenderMovesetCid` faz orientação/submissão e restaura o PoseStack. O shader `anime_energy.fsh` reserva os modos 11 e 12 para sombra turbulenta e veios luminosos pulsantes; ramos dos modos anteriores ficam intactos.

- Corte: faixa larga escura, chama violeta, borda clara estreita, ecos em atraso e fragmentos radiais. Surgimento, expansão e dissipação definidos em ticks.
- Dash: rastro volumétrico de faixas torcidas e dispersão na saída. Substitui as silhuetas antigas feitas de caixas. O destino existente define direção e comprimento; não é uma simulação do trajeto real do jogador, e segmentos verticais de teleportes ainda precisam de conferência.
- Fios: selo de origem, marcador por alvo e até três filamentos por ligação; configuração reduzida usa um. Essa parte depende das entidades no cliente e não está representada no vídeo de malhas isoladas.
- Transformação: composição de faixas escuras e veios ascendentes durante 32 ticks, sobre a aura já existente. A ativação usa o evento Shadow 200 com duração inicial de 800 ticks; eventos de observação com tempo restante não reiniciam a sequência, exceto se coincidirem exatamente com o início.
- Atomic: arcos de energia até aproximadamente 7 blocos de altura, diagrama radial de raio inicial 6,8 blocos e marcas angulares no chão. Preparação, convergência de 42 a 88 ticks e liberação radial de 90 a 125 ticks. A geometria nova soma-se à coluna, áudio e pós-processamento aprovados; não altera dano, alcance ou instante de detonação. A prévia isolada não inclui todos esses sistemas anteriores nem a câmera cinematográfica.

Orçamento testado abaixo de 1.200 quads adicionais por composição, com menos subdivisões/setores em modo reduzido. Renderer mantém limite de quatro Atomics e limita eventos secundários a oito (quatro em modo reduzido). Sem medição de FPS no Minecraft; limites e benchmark isolado não certificam desempenho multiplayer. Sem novas animações corporais nesta leva: trata-se de tecido e composição dos efeitos.

## Verificação concluída

- Build Forge/reobfuscação, Java 17: aprovado. 130 testes Java, zero falhas/erros/ignorados. Quatro testes do solver cobrem fixação, alongamento, barreira, direções, reinício, invariância à taxa de render e reconstrução das tangentes pelas rotações relativas.
- Geometrias: coordenadas finitas, orçamento, densidade reduzida e encerramento; exportação determinística das malhas e de 140 poses de tecido para revisão animada.
- Três testes Python dos projetos Blockbench aprovados. Materiais GLSL reais compilados/renderizados em OpenGL standalone. Prévias de composição e tecido em vídeo, com geometria/poses reais; sem Minecraft ou shader packs.
- Sondagem local isolada do solver: aproximadamente 0,035 ms por atualização após aquecimento (10.000 atualizações, Java 17, PC local). Exclui renderização, amostragem dos ossos, rede, demais mods e custo do Minecraft; não é meta ou garantia de FPS.
- JAR conferido: versão e integridade ZIP; classes de combate, rede, progressão/comando de terreno preservadas em relação à preview.5; assets/dados originais preservados exceto a adição dos dois modos ao shader comum. Sem commit/push/PR nesta entrega.

## Revisão necessária no Minecraft

Instalar somente o JAR preview.7 e selecionar Cid em P. Equipar traje e espada; para testes com OP usar `/minezin rpg maestria maxima cid`, `/minezin rpg restaurar_recursos` e `/minezin atomic terreno false`. O último preserva blocos e mantém dano a entidades.

1. Andar, parar, trocar lateral, girar rápido, saltar/cair, agachar e nadar: tecido deve permanecer preso nos ombros, reagir às direções e amortecer sem vibração crescente. Avaliar junções e clipping; a aproximação de colisão não cobre todas as poses.
2. Corte/dash/fios: orientação, alvos, bordas, oclusão por paredes, desaparecimento e leitura em cenário claro. Dash com mudança de altura é um caso específico pendente.
3. Transformação/Atomic: separação entre preparação, convergência e impacto; conservar câmera, SFX e impact frames anteriores. Verificar diagrama no terreno irregular e evitar perda da silhueta por excesso de brilho.
4. Testar dois jogadores, morte, teleporte, mudança de dimensão, desconexão e reload de recursos. Comparar modos reduzido/normal e packs de shader. Medir FPS/frame time em combate concorrente antes de expandir aos outros kits.

A próxima revisão visual deve priorizar a sequência completa da Atomic com God Wand/Apex Arcana como referência; a nova base já permite observar malhas completas e tecido animado. Nenhum resultado offline substitui a comparação dentro do jogo.
