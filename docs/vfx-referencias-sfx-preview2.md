# VFX pelas referências e SFX — 3.5.0-preview.2

Base: `c284af96a2efe82a4f89b6d711cd582812b765ef`, último commit do PR #19. Trabalho local na branch `feat/vfx-referencias-sfx`, sem publicar ou alterar aquele PR nesta etapa. A documentação dos Commits 01/02 do Minezin 2.0 continua em checkout separado.

## Diferenças visuais

- Black Divider amplia apenas o osso da lâmina, a partir da guarda, até 3,6× no momento do golpe. Empunhadura e guarda mantêm tamanho e encaixe do item original. Primeira pessoa limita a ampliação a 2,2×. A escala é aplicada no desenho de cada osso, depois da animação GeckoLib, e restaurada após o desenho; não deve vazar para inventário ou outra espada idêntica.
- O renderizador usa o dono do ItemStack efetivamente empunhado. A pose do Asta agora prepara a espada acima do corpo, desce no tick 18 e mantém a posição por dois ticks. A extensão é visual: alcance e hitbox continuam no servidor, sem alteração.
- Atomic usa campo de filamentos verticais e anel elevado na preparação, com gesto de espada mais contido. A descarga usa uma coluna com bordas violetas e núcleo claro; a esfera deixa de ser a forma dominante. Mantém as ondas e os ticks 50/90/170/240.
- Roxo preserva núcleo, cores e domínio aprovados. Os raios ganham distribuição angular irregular, comprimentos diferentes e mais ramificações, mantendo o limite espacial anterior. O braço de apoio aproxima-se da mão de disparo.
- A barreira da Frieren ganha células hexagonais concentradas à frente e mantém uma presença sutil ao redor. Feixes, singularidade e regras de defesa existentes são preservados. As cenas de ataque/defesa do vídeo não foram todas atribuídas à mesma magia.

## Camada de SFX

Dez arquivos originais mono Vorbis, 48 kHz, total aproximado de 165 KiB. Gerador, manifesto de checksums e informações de autoria em `art/audio/` e `tools/build_anime_sfx.py`.

| Técnica | Preparação | Liberação / resposta |
| --- | --- | --- |
| Atomic | Drone crescente no início | Descarga no tick 90 |
| Black Divider | Extensão da lâmina | Corte no tick 18 |
| Roxo | Convergência no início | Disparo a partir do pacote real de liberação |
| Frieren | — | Zoltraak e ressonância da barreira |
| Singularidade | Concentração inicial | Colapso no tick 220 |

`AudioAnime` toca sons posicionais na categoria Jogadores e usa fila por dono/grupo. Cancelamento remove sons ativos e futuros; morte, saída de dimensão e desconexão limpam a apresentação. Sons que seguem o jogador são usados no Divider; Atomic e singularidade permanecem no centro do efeito. Há limites de 64 cues pendentes e 24 sons ativos, sem entidades adicionais de gameplay.

Configurações novas: `sfxAnime` e `volumeSfxAnime` (padrão 0,7; intervalo 0–1). Master e Jogadores do Minecraft continuam aplicáveis. Há legendas em português e inglês. A camada é adicional aos sons vanilla existentes.

Os sons são apresentação no cliente, dirigidos pelos efeitos já recebidos. Não há novo cálculo de dano, pacote de ataque ou alteração de cooldown. O sincronismo audível sob lag e a mixagem no jogo precisam de teste real; duração de áudio é tempo de reprodução, enquanto a fila usa ticks do mundo.

## Validação e limites

Testes novos cobrem extensão/restauração da lâmina, limite em primeira pessoa, geometria do campo/coluna, arquivos mono Vorbis, nomes/legendas e os marcos das cues. Os assets foram decodificados após geração e os picos verificados abaixo de clipping. O build Forge e as prévias GPU são registrados na entrega; não substituem testes dentro do jogo.

Nenhuma biblioteca de runtime foi adicionada. Registro de SoundEvents e arquivos de som fazem parte do novo JAR. Para multiplayer, testar ambos os lados com a mesma versão da prévia e as dependências existentes.

Dano, mana, recargas, NBT, protocolo e destruição não foram alterados. O problema A01/P1 do Roxo normal continua fora do escopo. Usar mundo de teste ou cópia para a validação.

## Conferência manual necessária

1. Ativar Black Divider com Asta em primeira/terceira pessoa e nas duas skins de braço. Conferir guarda imóvel em relação à mão, giro do corte e retorno ao modelo normal, inclusive no inventário e com outro jogador segurando espada idêntica.
2. Ver Atomic de perto e de longe, com efeitos reduzidos; cancelar antes do impacto e confirmar que o SFX de liberação não toca depois.
3. Usar Roxo e verificar braço de apoio, ramificações e SFX no disparo. Usar barreira/Zoltraak e singularidade da Frieren, inclusive cancelamento antes do colapso.
4. Alternar `sfxAnime`, ganho adicional e sliders Master/Jogadores; conferir pausa, legendas, atenuação e posição do som.
5. Testar morte, desconexão, dimensão e dois jogadores, sem câmera invasiva nem áudio duplicado. Não há certificação de cliente, servidor dedicado ou multiplayer nesta etapa.

## Resultado local desta entrega

- `test build`: sucesso com Java 17 / Gradle 8.3; 97 testes Java, zero falhas, erros ou ignorados.
- Três testes Python de projetos Blockbench: sucesso.
- Prévia OpenGL: 11 materiais de poderes e quatro materiais do Gojo compilados e renderizados na RTX 5060 Ti; composição Atomic e modelo Divider também inspecionados em prévias separadas.
- Dez OGG mono/48 kHz decodificados, sem clipping, hashes conferidos contra o manifesto. ZIP e versão do JAR verificados.
- Gerador de animações repetido sem alterar os assets. `git diff --check` sem erros.
- JAR: `minezin-3.5.0-preview.2.jar`, 2.740.907 bytes. SHA-256: `6854eb7a6f13df511cfdfeb82317d7524ac3ef626b4f026ed0dd39e2aed67970`.
- Build local; nenhuma execução nova de GitHub Actions, commit, push ou PR nesta entrega.
