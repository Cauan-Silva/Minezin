---
tags: [documento]
---
# Atomic — sequência de referência — 3.6.0-preview.9

Referência enviada: Short `akVqBtWYTfo`, 1080×1920, 60 FPS, 50,97 s. O usuário aprovou alongar a preparação para seguir o vídeo e pediu que a roupa permanecesse como na preview.8. Base de trabalho: mudanças locais das previews.6–.8 sobre main `09f2727fc796d02171335b86862b7de33d1fca43`. Sem commit/push/PR.

## Leitura da referência e resultado

Foram extraídos frames com decodificação até o timestamp solicitado, evitando confundir keyframes anteriores com o instante desejado. A habilidade começa com o pequeno círculo próximo de 4 s; em 5–12 s surgem ramificações radiais e visão superior; em 14–20 s há paredes e linhas superiores, seguidas por I AM/ATOMIC; 23–24 s ficam escuros e 25 s marca a liberação. A coluna segue até perto de 42 s. Barra do Ender Dragon, enquadramento vertical/blur da edição do Short e partículas de morte do dragão no fim não são elementos transplantados para o mod.

A implementação segue essas fases e seu ritmo. Não se afirma equivalência de cada pixel, do cenário, fonte tipográfica, skin ou áudio da gravação. As amostras do vídeo são referências, não código/modelos/texturas/samples incorporados. O áudio novo é síntese procedural original e não contém a voz ou trilha do Short.

## Relógio e gameplay autorizado

`core/vfx/RelogioAtomic.java` é a fonte de tempo: detonação no tick real 420 (21 s), encerramento no 780 (39 s), ocupação/pose inicial até o 444 (22,2 s). O relógio canônico visual 0–240 foi preservado para reutilizar perfis de materiais e interpolação. Nós reais/visuais: 0/0, 120/30, 200/50, 320/78, 380/88, 420/90, 426/96, 700/170, 780/240. Conversão inversa usada pelo gerador de poses.

`PoderesCid` usa o relógio real para lentidão/ocupação, cancelamento antes da detonação, batimentos, impacto e fim. A explosão e os 45 pontos de dano mágico continuam sendo uma ocorrência, agora no tick 420. Raio, cratera, impulso, custo e recargas não foram alterados. `/minezin atomic terreno false` permanece disponível, persistente e consultado na detonação. O despertar Shadow permanece com 800 ticks; a apresentação termina antes da forma. A limpeza da lentidão e o cancelamento da pose acompanham a ocupação maior.

`EfeitosCid` converte o tempo para render, pós-processamento e impulso visual da roupa, sem editar o solver ou seus perfis. Partículas de liberação usam comparação no tick real para não se repetirem por arredondamento. Photon, áudio e impact frames são programados no tick real 420. Câmera usa duração real 780 e o roteiro canônico mapeado. Não há controle novo de segurar/soltar botão; mantêm-se as teclas e regras existentes, incluindo cancelamento por deslocamento superior a três blocos antes do impacto.

Mudou a semântica temporal cliente/servidor: protocolo de rede 13→14, para rejeitar combinações com versões antigas. Instalar a preview.9 tanto no cliente quanto no servidor. Não houve alteração de campos dos pacotes ou migração dos saves.

## Composição

- Campo inicial de raio 1,6 bloco; até 64 raios ramificados crescem até raio 25,6. Paredes retas sobem até aproximadamente 28 blocos, com ligações diagonais e linhas superiores. Material 15 dá núcleo claro contínuo e halo violeta; modo reduzido usa 24 setores. Substitui o antigo conjunto de arcos arredondados.
- Câmera: conjurador próximo, campo visto de cima, paredes internas, aproximação do conjurador para a frase, pausa e vista da liberação. `RoteiroGojo` muda somente a tabela ATOMIC. Colisão de câmera continua ativa; obstáculos podem mudar o enquadramento no Minecraft.
- Letreiro local `LetreiroAtomic`: I AM pequeno entre os ticks visuais 78 e 86, ATOMIC grande entre 86 e 88. Usa a fonte atual do Minecraft, respeita F1 e a opção de cinematográficas; não desenha a frase na HUD de outro conjurador.
- Pausa escura entre os ticks visuais 88 e 90, seguida por uma única exposição clara curta. Substitui a sequência anterior de inversões gráficas. `impactFrames`/`claraoAtomic` continuam controlando a exposição. A descida antecipada de um feixe foi retirada; o pilar surge na liberação.
- Coluna sustentada e vórtices da preview.8 preservados, com topo agora afilado para reproduzir a silhueta da referência. Densidade reduzida e orçamentos de malha continuam testados.
- Apenas o clipe corporal `cid_4` teve seus keyframes retimados. `tools/cinematic_animations.py` lê os nós do relógio Java para reproduzir o asset; demais clipes são preservados.

## Áudio

Carga original sintetizada de 21 s com crescimento, pulsos graves e silêncio final; liberação original de 18 s com transiente e rescaldo. Cues e durações alinham-se ao tick 420 e ao fim 780. Vorbis mono 48 kHz, picos decodificados abaixo de 0,99, manifesto com hashes atualizado. O gerador escreve Vorbis em blocos de 4.096 samples para evitar falha do encoder local com buffers longos. Sons dos demais kits são preservados.

## Verificações

- Build Forge/reobfuscação e 136 testes Java aprovados; três testes Python Blockbench aprovados. Relógio monotônico/reversível, impacto, cancelamento pré-impacto, títulos/áudio, duração da pose e câmera finita verificados.
- 26 arquivos de roupa/solver comparados byte a byte com o snapshot da preview.8, incluindo modelos e texturas do Shadow. JAR comparado com preview.8 para garantir que alterações se limitem à Atomic, seu relógio e sincronização. Demais clipes Cid e áudios/recursos dos outros kits preservados.
- Geração do `cid_4` reproduz o asset atual. Vorbis decodificado integralmente, 21/18 s, mono 48 kHz, amostras finitas e picos limitados.
- GLSL real compilado/renderizado em OpenGL standalone. Cinco verificações do pós-processamento: identidade em intensidade zero, exposição distinta da liberação estabilizada, respeito a frames desligados, dissipação e preservação do depth de entrada.
- Prévia de 39 s usa as malhas do Java, câmera/tempos/peso do roteiro real e pós-processamento do mod. Billboards são reorientados para a câmera da prévia. Geometria amostrada por tick visual; avatar parado e cenário de grade são de teste. A tipografia da prévia é ilustrativa; no jogo é a fonte configurada no Minecraft. SFX autorais sincronizados no vídeo final. Não contém Photon, terreno real, pose corporal animada nem integração de shader packs.

## Aceitação manual pendente

1. Substituir o JAR anterior pela preview.9, selecionar/equipar Cid e maximizar maestria para teste. Usar `/minezin atomic terreno false` em um mundo de teste. Aguardar aproximadamente 21 s até o impacto; confirmar que dano/cratera não ocorrem no antigo marco de 4,5 s nem se repetem.
2. Comparar círculo, campo radial, visão superior, paredes, letreiros, pausa e liberação com o Short. Testar terreno irregular e obstáculos para avaliar altura das linhas, oclusão e colisão da câmera. Conferir a pose segurando a espada até a detonação.
3. Desligar cinematográficas, SFX, shader Atomic e impact frames separadamente; conferir fallback/ausência de letreiro ou exposição conforme a opção. Testar modo reduzido e shader packs.
4. Cancelar por movimento antes da detonação, alternar G, morrer, trocar dimensão ou desconectar. Câmera, pose, fila de áudio, Photon e VFX não podem permanecer presos. Verificar retorno ao controle após a ocupação maior.
5. Cliente e dedicado ambos na preview.9: dois jogadores, timing de dano/efeitos e comportamento de observadores. Entrada na área no meio da execução continua um caso pendente, pois o pacote inicial existente não reenvia a idade da Atomic. Não há certificação de multiplayer/FPS por testes offline.

A comparação visual dentro do Minecraft permanece necessária antes de declarar fidelidade final ao modelo enviado. Nenhum cliente ou servidor foi aberto durante esta implementação.
