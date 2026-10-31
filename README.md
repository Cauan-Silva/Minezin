# Minezin — Arcane Control

Mod para Minecraft 1.20.1, Forge 47.2.0 e Java 17.

## Compilar

Instale um JDK 17 e configure `JAVA_HOME` para essa instalação. Não é necessário instalar Gradle: o wrapper baixa o Gradle 8.3 na primeira execução. A primeira compilação precisa de internet para baixar as dependências do Forge e Minecraft.

Windows (PowerShell):

```powershell
.\gradlew.bat build --no-daemon
```

Linux/macOS:

```sh
./gradlew build --no-daemon
```

O JAR é gerado em `build/libs/`. O GitHub Actions executa o build em PRs e em atualizações de `main`, disponibilizando o JAR como artefato.

## Ambiente de desenvolvimento

Use `runClient` para iniciar o cliente de desenvolvimento e `runServer` para iniciar o servidor dedicado. O servidor requer aceitar o EULA do Minecraft em `run/eula.txt` antes de iniciar. Arquivos de execução e mundos ficam em `run/`, fora do controle de versão.

Compilar não substitui os testes dentro do jogo: teclas, HUD, magias, cutscenes e multiplayer precisam de validação manual.

## Progressão do Gojo (1.1)

Abra a árvore com **K** (tecla remapeável). Todos começam com Azul; cada 100 XP aumenta o nível e concede um ponto. O limite é nível 20. Desbloqueios são escolhidos na árvore e validados pelo servidor.

| Técnica | Nível mínimo | Pontos | Pré-requisitos | Recarga inicial |
| --- | --- | --- | --- | --- |
| Azul (Z) | 1 | 0 | — | 10 s |
| Vermelho (X) | 2 | 1 | Azul | 15 s |
| Infinito (C) | 3 | 1 | Azul | 5 s |
| Vazio Roxo (V) | 5 | 2 | Azul e Vermelho | 45 s |
| Vazio Infinito (R) | 8 | 3 | Infinito e Roxo | 90 s |

Cada ativação aceita concede 5 XP e um uso de maestria à técnica. Cada 20 usos concede um grau de maestria, até 5, reduzindo a recarga em 5% por grau (máximo 25%). Derrotar um inimigo hostil com dano atribuído ao jogador concede 25 XP, inclusive com armas normais. Desativar Infinito é livre e não concede XP. A contagem das recargas usa ticks do servidor (20 ticks por segundo em condições normais).

Progresso e recargas são salvos por jogador, preservados na morte e sincronizados ao entrar, renascer e mudar de dimensão. Esta versão inicia progressão também para jogadores de mundos existentes. Cliente e servidor precisam usar a mesma versão (protocolo de rede 3 na versão 1.2).

A árvore permite desativar as cinematics locais, mantendo os efeitos visuais. A preferência fica em `config/arcanecontrol-client.toml`. Esta entrega reaproveita os efeitos e cutscenes existentes: animações corporais com rigs e novas sequências cinematográficas ainda são etapas futuras.

### Validação manual pendente

- Entrar com dois jogadores: confirmar progressos independentes e Azul como única técnica inicial.
- Usar Azul, verificar XP e recarga, derrotar inimigos e desbloquear a árvore respeitando pré-requisitos e pontos.
- Reconectar, morrer, reiniciar o servidor e trocar de dimensão: conferir persistência e sincronização.
- Usar uma Ultimate: confirmar que apenas o conjurador recebe a câmera e que ela retorna à preferência anterior ao terminar, morrer ou desconectar.
- Desativar cinematics pela árvore e confirmar que os efeitos continuam sem mudança de câmera.

Os testes automatizados das regras de progressão rodam com `gradlew test` e no build de CI.

## Animações e efeitos (1.2)

O cliente agora exige **Player Animator Forge 1.0.2-rc1+1.20**, instalado separadamente na pasta `mods/`. A dependência vem do [projeto oficial](https://github.com/KosmX/minecraftPlayerAnimator); no ambiente de desenvolvimento o Gradle a baixa automaticamente. O servidor dedicado não precisa da biblioteca, mas todos os clientes precisam dela e da mesma versão do Minezin.

Cinco gestos de braços/tronco acompanham as técnicas, inclusive para jogadores próximos que recebem o evento do servidor. As pernas mantêm a locomoção normal. Os keyframes têm preparação, liberação e recuperação, alinhados aos tempos do servidor. Desligar cinematics na árvore desliga a câmera; os gestos e efeitos continuam.

Roxo e Domínio usam roteiros de câmera relativos ao conjurador, com interpolação suave, aproximação, afastamento, FOV gradual e teste de colisão com blocos. O tremor fica restrito ao impacto. Flashes foram reduzidos e as barras de cinema entram e saem suavemente. Camadas translúcidas de energia usam mistura aditiva e os efeitos simultâneos têm limite de 64 por cliente.

O servidor envia uma atualização no instante do disparo do Roxo: os clientes passam a desenhar a trajetória a partir da posição e direção reais de lançamento, mesmo que o jogador se mova durante a preparação.

O HobbyMine anterior serviu como referência para a integração de animação e câmera; veja `THIRD_PARTY_NOTICES.md`. Não foram importados todos os elementos, modelos e sistemas antigos. Os cinco testes adicionais verificam carregamento pelo parser real do Player Animator, durações, ausência de animação das pernas, continuidade dos roteiros, FOV e janela do tremor. Aparência, primeira/terceira pessoa, colisão da câmera e sincronização visual com dois jogadores ainda exigem teste no jogo. Não há promessa de qualidade visual validada somente pelo build.


## Cid / Shadow (1.3)

Requer GeckoLib Forge 4.4.9 (cliente e servidor) e Player Animator Forge 1.0.2-rc1+1.20 no cliente. A biblioteca GeckoLib fornece o modelo da espada e do traje, ossos animados e camada emissiva. Cliente e servidor devem atualizar juntos (protocolo 4).

Combine uma espada de netherita, uma ametista e uma obsidiana para criar a Lâmina de Shadow. Equipá-la na mão principal troca Z/X/C/V para o kit Cid: corte (2 s), passo sombrio (8 s), contenção (12 s), Atomic (90 s). R não ativa uma técnica enquanto a espada estiver equipada. K continua abrindo a árvore do Gojo; o kit Cid é liberado pela criação da espada e ainda não tem árvore de maestria própria.

O traje usa peças de netherita + ametista + lã preta em receitas sem formato. Itens: `capuz_shadow`, `manto_shadow`, `calcas_shadow`, `botas_shadow`. Para testes em criativo, use `/give @s arcanecontrol:lamina_shadow` e os IDs das peças. A espada tem lâmina afilada, núcleo emissivo e fragmentos orbitais. O manto reaproveita o traje detalhado do HobbyMine com 117 peças distribuídas em três ossos móveis adicionais. Texturas originais preservadas; referência e licença em THIRD_PARTY_NOTICES.md.

Atomic prepara por 90 ticks, aplica um impacto em raio de 14 blocos e encerra aos 140 ticks. Não destrói blocos. O jogador fica lento durante a preparação; afastar-se mais de 3 blocos do centro cancela a sequência. Morte, desconexão e mudança de dimensão cancelam a execução. Cooldowns ficam salvos por jogador; danos e permissões de uso são validados pelo servidor. Esta versão inicial usa sons do Minecraft, sem fala gravada.

Validação manual necessária: aparência do traje/espada em primeira e terceira pessoa, recorte do manto ao andar e agachar, colisão da câmera, efeito Atomic e sincronia com dois jogadores. A aprovação do build não comprova a qualidade visual. O aprimoramento do modelo do Gojo e a árvore própria do Cid são próximos trabalhos.

### 1.3.1 — Atomic e itens
Ícones do traje agora usam o atlas de itens. Atomic inclui carga orbital, partículas, núcleo branco/violeta, ondas de choque e clarão breve. No impacto, causa explosão real e arremesso dos alvos. A explosão usa os eventos nativos do Forge; compatibilidade com cada mod de proteção precisa ser testada.
Configuração por mundo em serverconfig/arcanecontrol-server.toml: atomic.destruirTerreno (padrão true) e atomic.raioCratera (padrão 14, intervalo 1–24). O alcance efetivo varia com a resistência dos blocos. Teste em uma cópia do mundo: a explosão destrói blocos de verdade.

### 1.4.0 — Protótipo Photon da Atomic
Requer Photon 1.1.17 (Forge 1.20.1), que inclui LDLib 1.0.42. Em desenvolvimento, as duas bibliotecas são declaradas explicitamente. Instale o Photon nos clientes; no servidor dedicado ele não é exigido pelo Minezin.
Cinco emissores autorais: convergência violeta, centelhas claras, explosão radial, corona luminosa e poeira residual. Photon faz o bloom das camadas luminosas; a geometria existente continua compondo o núcleo e as ondas. Não há distorção de tela implementada nesta etapa.
Limites: 4 sequências Photon simultâneas, 256 partículas por emissor, encerramento obrigatório aos 185 ticks. Interrupção, morte, troca de mundo e desconexão removem os runtimes. O dano e a destruição continuam controlados pelo servidor.
Configuração do cliente: photonAtomic, vfxReduzidos e claraoAtomic. Desligar photonAtomic mantém os efeitos geométricos anteriores; vfxReduzidos corta a taxa de emissão pela metade. Não altera a configuração global do bloom Photon.
Referência de direção gráfica: The Shooting Star. Este protótipo ainda precisa de comparação visual no Minecraft, medição de FPS e testes com Oculus/Embeddium. O build não comprova equivalência gráfica à referência.

### 1.4.1 — Material procedural da Atomic
A Atomic substitui a malha lisa por material GLSL próprio: ruído em movimento, veios claros, bordas suaves e opacidade variável. Filamentos convergem para uma âncora próxima da espada; no impacto, a casca deformada, frentes de choque fragmentadas e descargas ascendentes quebram a silhueta uniforme. A âncora é aproximada a partir da direção do jogador, não é vinculada ao osso animado da mão.
Duas chamadas de desenho por Atomic, até quatro Atomics visíveis. vfxReduzidos reduz segmentos e filamentos. shaderAtomic=false restaura a geometria anterior; erros de carregamento do shader também usam essa geometria e registram aviso. A geometria nova dura até 170 ticks; os emissores Photon terminam até 185.
CI valida a sintaxe GLSL e testes verificam convergência, continuidade, expansão e limites da malha. Aparência em GPU, FPS, recarga F3+T e compatibilidade com Oculus ainda precisam de teste dentro do Minecraft. O material não implementa refração/distorção do cenário nem iluminação dinâmica real.

### 1.5.0 — Rework visual do Gojo
Azul com núcleo escuro e fluxo de atração; Vermelho concentrado e frente de repulsão; Infinito discreto com reação ao contato e estado remoto sincronizado; Roxo com fusão e trajeto alinhados ao servidor; Vazio Infinito com céu, neblina e horizonte cósmico locais. Bloom Photon de acento e perfil reduzido acompanham o conjunto.
O Vermelho recebe confirmação da posição/direção de disparo. O Domínio usa o centro nos pés e área esférica de raio 26, corrigindo os cantos do AABB antigo. Protocolo de rede 5: atualize cliente e servidor juntos.
Configurações, testes e limitações estão em [docs/gojo-vfx.md](docs/gojo-vfx.md). O CI gera uma prévia OpenGL dos materiais reais; isso não substitui validação dentro do Minecraft.
