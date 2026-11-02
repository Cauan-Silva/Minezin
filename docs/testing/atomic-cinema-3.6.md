# Atomic: composição intensa — 3.6.0-preview.2

Base: `47dd0fede76a0afd8516c6dee681789404a48e35` (PR #21). Branch local: `feat/cid-atomic-impact-cinema`. Este trabalho foca somente a Atomic do Cid. Sem commit, push ou PR nesta entrega.

## Composição

| Ticks | Apresentação |
| --- | --- |
| 0–50 | Cena gradualmente escurecida, energia concentrada no personagem |
| 50–88 | Compressão radial, dessaturação e halo violeta; o feixe mantém a descida original |
| 88–90 | Negativo preparatório e linhas convergentes |
| 90–91,25 | Cena em branco com massas escuras e contornos de profundidade |
| 91,25–92,5 | Inversão: massas claras sobre fundo escuro |
| 92,5–94 | Duotomia violeta e branco com acento de bordas |
| 94–96 | Traço de alto contraste, corte diagonal e rasgos horizontais breves |
| 96–130 | Dispersão cromática, bloom radial e refração na onda |
| 130–240 | Rescaldo, cinza baixa e retorno suave à imagem original |

Os quadros de silhueta derivam da luminância da cena e das descontinuidades de profundidade; não há uma máscara semântica exclusiva do personagem. Afetam a imagem real do mundo, incluindo a geometria do Cid. Não são apenas retângulos na HUD. A implementação é autoral; não usa código, imagens, biblioteca ou áudio do Shooting Star.

A coluna mantém sua forma principal, com veios brancos mais fortes e uma coroa fragmentada no primeiro impulso. Há até 48 fragmentos cosméticos (12 em efeitos reduzidos), trajetória determinística e campo baixo de cinza. Não há novas entidades de bloco caindo, colisões ou modificações de terreno por esses fragmentos.

A câmera local termina no tick 180, permitindo acompanhar a descarga após o impacto; a opção `cinematicas` continua desativando-a. O mundo e o controle do jogador não são pausados. As poses de impacto existentes continuam brevemente sustentadas na animação.

## Implementação e isolamento

`CinemaAtomic` aplica uma `PostChain` própria em `AFTER_LEVEL`, lendo cor e profundidade do alvo principal e escrevendo em `atomic_swap`. A cópia final usa um shader próprio com escrita de profundidade desativada, sem limpar o framebuffer principal. O passe não ocupa nem substitui o slot de pós-processamento do `GameRenderer`. A fonte e o destino da transformação são texturas diferentes.

Se outro pipeline ainda possui o framebuffer nessa etapa, o passe é ignorado para não redirecionar sua saída. Isso é uma proteção, **não certificação de compatibilidade** com Oculus, OptiFine ou shader packs. O processamento acontece antes da mão de primeira pessoa e da HUD; esses elementos não integram a silhueta. Terceira pessoa é a referência da composição.

Resize recria as dimensões do alvo privado; recarga de shaders, desconexão, fim do efeito e troca de mundo liberam a cadeia. Erro de criação/processamento desativa o passe até a próxima recarga de recursos e registra o motivo. A apresentação anterior permanece disponível. A seleção prefere a Atomic local; para observadores, há limite de distância, campo de visão e oclusão. No máximo uma cadeia é processada por quadro, mesmo com múltiplas Atomics.

## Controles

No arquivo cliente `config/arcanecontrol-client.toml`:

- `cinemaAtomic = true`: novo passe e acentos geométricos; desativar preserva a apresentação anterior.
- `intensidadeCinemaAtomic = 1.0`: intensidade do pós-processamento, de 0 a 1. Zero não aplica o passe.
- `impactFrames = false` ou `claraoAtomic = false`: remove negativo, branco/preto, inversão e cortes gráficos. Compressão, tintura e bloom natural da energia permanecem.
- `vfxReduzidos = true`: desativa o passe intenso inteiro e reduz os fragmentos.
- `cinematicas`, `sfxAnime`, `volumeSfxAnime` e os volumes Master/Jogadores mantêm seus controles existentes.

O overlay genérico de impacto do Shadow é omitido somente nos quadros em que este passe foi aplicado, evitando dupla exposição. O código genérico dos demais kits não foi reformulado.

## SFX

Dois sons originais foram refinados: `atomic_charge` e `atomic_release`. A carga agora termina com uma pausa curta antes do tick 90; a liberação possui ataque grave, ruído de impacto e ressonância retardada. Os outros oito OGG permanecem idênticos. São mono Vorbis/48 kHz, com picos limitados e manifesto atualizado em `art/audio/`. Nenhum áudio de referência foi extraído.

Regenerar somente esse par: `python tools/build_anime_sfx.py --atomic-only --preview build/atomic-sfx.wav`. O WAV concatena carga e liberação nos tempos reais, sem o intervalo adicional usado na prévia geral de sons.

## Gameplay e compatibilidade

Permanecem os marcos do servidor: feixe no tick 50, impacto no 90, fim do feixe no 170 e fim visual no 240. Dano, alcance, mana, cooldown, progressão, protocolo 13, proteção/destruição de terreno e demais kits não foram alterados. Os comandos administrativos existentes podem facilitar o teste. Nenhuma dependência de runtime nova.

Para testar: selecionar Cid em P, segurar `arcanecontrol:lamina_shadow`, usar `/minezin rpg maestria maxima cid`, ativar com G e observar a Atomic. `/minezin rpg restaurar_recursos` repõe recursos para repetir depois que a ação terminar. Usar cópia do mundo: a destruição existente da Atomic continua ativa conforme a configuração do servidor.

## Validação manual pendente

1. Cliente com recursos padrão: verificar ordem do passe, orientação da imagem e centro da composição; comparar com a prévia offline.
2. Repetir em terceira/primeira pessoa, com F1, interface aberta, pausa, mudança de resolução, fullscreen e recarga F3+T.
3. Desativar separadamente cinema, intensidade, impact frames, clarão, efeitos reduzidos, câmera e sons; conferir restauração imediata.
4. Cancelar antes do tick 90; morrer, desconectar e mudar dimensão. Nenhum quadro, tremor ou som futuro deve continuar.
5. Dois jogadores: observar perto, longe, de costas e atrás de parede; dois lançamentos simultâneos não devem empilhar cadeias.
6. Shader packs/Oculus/OptiFine: conferir se o framebuffer pertence ao pipeline estrangeiro; o passe pode ser ignorado. Verificar HUD, mão, água/transparências e ausência de conflitos. Não há teste real de compatibilidade nesta entrega.
7. Medir FPS, tempo de GPU e uso de memória em 1080p/1440p/4K; a prévia não certifica desempenho do jogo.

A prévia PNG/MP4 usa o GLSL real, o modelo Shadow e um buffer de profundidade real numa cena offline. Não é captura do Minecraft; não reproduz Photon, a explosão completa, a animação do jogador ou o pipeline de um shader pack. O MP4 é silencioso; o WAV é uma prévia separada. O áudio não foi validado por escuta dentro do jogo.


## Resultado local desta entrega

- Java 17 / Forge / Gradle: `test build` e reobfuscação aprovados. **111 testes Java**, zero falhas, erros ou ignorados.
- Três testes Python de projetos Blockbench aprovados.
- OpenGL na RTX 5060 Ti: shader de composição e shader de cópia compilados; 12 quadros temporais renderizados. Cinco checks GPU passaram: identidade em intensidade zero, etapas de impacto distintas, frames desativados, dissipação e preservação da profundidade da cena-fonte offline.
- Materiais existentes (11) e composições temporais (12) renderizados com o shader de coluna atualizado.
- Dez OGG decodificados, mono/48 kHz, sem clipping e hashes conferidos. Pausa final da carga confirmada numericamente; outros oito OGG idênticos ao HEAD-base.
- Comparação Git confirmou ausência de alterações em combate do servidor, terreno e rede. `git diff --check` sem erros.
- Integridade ZIP, versão, classes novas e recursos do JAR conferidos byte a byte. Artefato: `minezin-3.6.0-preview.2.jar`, 3.706.240 bytes; SHA-256 `361f185d333d84c06a90ffa96e21011992c5444a9bf96bcf0b4435deb9f4a778`.
- Sem nova execução de GitHub Actions, commit, push ou PR. Cliente Minecraft, servidor dedicado, multiplayer e shader packs continuam pendentes de validação real.
