# Cid — queda e explosão final — 3.6.0-preview.8

Continuação local da preview.7, sobre `09f2727fc796d02171335b86862b7de33d1fca43`. Sem commit/push/PR. Demais mudanças das previews.6/.7 permanecem nesta branch.

## Queda

Causa confirmada: `MalhaTecido.tick` usava `-9.81 + vertical*3` no eixo Y. Uma velocidade negativa de queda aumentava a força para baixo; como a capa pendia verticalmente, o movimento apenas tensionava o tecido. Não havia pressão normal da corrente de ar gerada pela queda.

Agora a velocidade vertical e sua variação produzem sustentação e pressão normal variável entre linhas/colunas. A sustentação positiva é limitada a 7,5 m/s², abaixo da gravidade, para que a capa eleve e ondule sem permanecer achatada na barreira dos ombros. O limite de entrada foi ampliado até -2,5 blocos/tick para diferenciar quedas rápidas. Amostras t=0/1 retornam diretamente os estados exatos, evitando arredondamento que aparentava cruzar a barreira na validação de colisões.

Teste novo: queda vertical pura durante 35 ticks levanta a bainha e a afasta das costas sem entrada horizontal; após 300 ticks parada retorna à pose pendente. O roteiro animado foi ampliado para 220 ticks, incluindo queda e pouso. A barreira continua sendo uma aproximação das costas/ombros, sem autocolisão ou colisão completa com corpo e mundo.

## Explosão final da Atomic

Amostras do God Wand fornecido pelo usuário, em 108–132 s, mostram preparação clara, intervalo escuro e coluna de grande massa com fluxo espiralado, base incandescente e frentes radiais. Esta revisão usa a linguagem visual; nenhum recurso ou código desse mod foi extraído.

`core/vfx/ExplosaoAtomic.java` gera a liberação inteira, compartilhada entre o jogo e a ferramenta de revisão. A geometria antiga de liberação em `RenderEnergiaAtomic` foi removida do caminho padrão, evitando sobrepor a coluna nova com a anterior. O shader Atomic existente continua responsável pela preparação. A opção `shaderAtomic=false` e falha de carga do shader Atomic preservam o fallback anterior.

- Crescimento rápido entre os ticks 90 e 115; altura visual até aproximadamente 64 blocos e raio nominal chegando a aproximadamente 7,3 blocos. A base alargada ultrapassa o raio nominal.
- Casca violeta com mistura alfa e núcleo aditivo separado; material novo 13 com turbulência, estratos em movimento e veios. UV vertical contínua em toda a coluna, em vez de repetir o material em cada segmento.
- Até seis colares helicoidais largos em torno da base, usando o material novo 14; três frentes de choque horizontais com atrasos de sete ticks. Raios/pilares radiais complementam o impacto durante os primeiros 72 ticks da liberação.
- Coluna sustentada, início de dissipação no tick 165 e término no 240. Novos materiais não mudam os ramos anteriores do shader compartilhado.
- Modo reduzido diminui setores, segmentos, espirais e raios. Orçamento automatizado de até 1.800 quads na composição completa. Sem lotes vazios.

A altura/escala é exclusivamente visual. Dano, cratera, alcance de combate, timing de servidor, recargas e comando de terreno permanecem iguais à preview.7. Os impact frames e SFX existentes permanecem. A animação corporal e o cenário não foram refeitos nesta correção.

## Verificações e limites

132 testes Java aprovados, incluindo a queda sem movimento horizontal, retorno ao repouso, fases da explosão, limites de geometria, lotes e dissipação. Build Forge/reobfuscação e três testes Python aprovados. Materiais novos compilados e renderizados em OpenGL com as malhas exportadas pelo Java.

Vídeos locais: tecido com queda/pouso (220 quadros, 20 FPS, 11 s) e liberação da Atomic dos ticks 80 a 239 (160 quadros, 20 FPS, 8 s). São cenas offline: não incluem Photon, mundo Minecraft, câmera cinematográfica e o pós-processamento completo. A câmera da prévia amplia o enquadramento no tick 90 para caber a coluna; não é uma nova câmera no jogo. Grade é apenas referência espacial.

JAR conferido por integridade ZIP/versão e comparação com preview.7: combate, comandos, rede, dados e assets originais preservados, exceto os novos modos no shader `anime_energy.fsh`. Classes alteradas se limitam aos renderizadores/solver visuais previstos. Não foram adicionadas dependências.

## Conferir no Minecraft

1. Substituir a preview.7 pelo JAR preview.8. Cair de uma altura segura sem avançar e comparar com deslocamento horizontal; bainha deve levantar/ondular, mantendo ombros fixos, e assentar depois do pouso. Testar queda longa, escada, água, agachamento e giro durante a queda.
2. Com OP e mundo de teste: `/minezin rpg maestria maxima cid`, selecionar Cid em P e `/minezin atomic terreno false`. Confirmar a nova coluna e espirais após o tick 90; o comando continua preservando blocos sem suprimir dano/VFX.
3. Avaliar núcleo/contorno em dia/noite, vistas próxima/distante, oclusão por paredes, modo reduzido, shader packs e desempenho com Atomics concorrentes. Verificar cancelamento/morte/troca de dimensão, fim da coluna e ausência de efeitos presos.

Minecraft, servidor dedicado, multiplayer e medição de FPS real continuam pendentes. Validação offline comprova a implementação e a prévia, não garante encaixe ou desempenho em todas as poses/packs.
