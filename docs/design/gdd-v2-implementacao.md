# Implementação do GDD v2

O usuário confirmou a adoção integral dos movesets e teclas de `GDD_Minezin_RPG_Movesets_v2.docx`. Essa decisão substitui as posições anteriores das habilidades: Gojo recebe Teleporte Curto em C e domínio em G; Shadow recebe Lâmina Oculta em V e Atomic em G; Frieren recebe Gaiola de Vento em V e Buraco Negro no C da forma transformada. P continua destinado à seleção entre kits desbloqueados, conforme a preferência anterior. H será a árvore geral e G o despertar.

## Estado desta entrega

Esta é a primeira camada de implementação, voltada às regras e ao catálogo. Não é uma versão jogável com os quinze personagens. A experiência em jogo continua a da 2.2.0: nenhuma habilidade nova é anunciada como disponível, não há item de alma/altar registrado e nenhuma tecla foi remapeada nesta etapa. Não há mudança de protocolo, versão de distribuição ou dependência.

Implementado e testado:

- `PersonagemRpg`: quinze personagens, IDs estáveis para Gojo/Shadow/Frieren, nomes dos desafios e tempos dos despertares. Onde o GDD só fornece a faixa global de recarga, o catálogo usa 300 s; os 360 s do Gojo e 480 s do Eren prevalecem sobre a faixa geral.
- `CatalogoMovesetsRpg` e `data/arcanecontrol/rpg/gdd_v2.json`: 120 técnicas, quatro por forma em cada personagem, e quinze especificações de G. Mantém os nomes e descrições do documento, incluindo as exceções de recarga. O catálogo distingue design de execução: possuir uma entrada não torna um kit jogável.
- `MestriaRpg`: níveis 1–100, pontos, atributos, pré-requisitos da árvore, bloqueio de G e consumo de alma correspondente ao kit. Dados inválidos não concedem nós sem nível/pontos/pré-requisitos.
- `NoRpg`: caminhos de fundamentos, atributos e despertar. O despertar exige nível 50 e a árvore precedente; a alma não substitui esses requisitos.
- `RecursoMana`: consumo validado, regeneração limitada e desconto de 20% com conjunto completo, aplicado uma única vez.
- `RecargasRpg`: Z/X/C/V/G por personagem, prazos independentes, duração para HUD radial e cópias defensivas dos snapshots. Mudar de kit não apaga a recarga de G.
- Migração de domínio testável: preserva técnicas base adquiridas elevando o nível mínimo quando necessário, sem conceder o despertar que passa a exigir desafio/boss.

As classes desta entrega ainda não estão ligadas aos eventos do servidor, pacotes ou GUI. Esse isolamento permite validar as regras antes de alterar os saves e o combate existentes.

## Decisões para parâmetros ausentes

O GDD define a escala de atributos, mas não a curva de EXP, custos de mana ou custos de cada nó. Esta camada centraliza valores iniciais de engenharia: 100 EXP por nível, um ponto por nível conquistado, 100 de capacidade de mana e custos/níveis em `NoRpg`. O nível 1 é a referência de atributos; no nível 100 há +198 pontos de vida, multiplicador de dano 2,485 e multiplicador de recarga 0,505, antes dos nós passivos. Esses números não foram ativados no jogo nesta entrega.

Os passivos iniciais são +10% de dano físico, +10% de dano mágico, +25% de regeneração e +10% de movimento. São parâmetros provisórios de balanceamento, não valores atribuídos ao documento. Duração zero no catálogo de G significa que não há término fixo definido ali; não autoriza uma forma infinita sem mana/superaquecimento/regras próprias.

## Próximas entregas e critérios

| Entrega | Trabalho | Critério de conclusão |
|---|---|---|
| Persistência e rede | Perfis por kit, mana, prazos, nós, migração e sincronização | Reconectar/morrer/trocar dimensão sem perder progresso ou reiniciar recargas; servidor rejeita compras e casts inválidos |
| H e HUD | Árvore em tela cheia, pontos, requisitos, mana e recargas radiais | Todos os estados vêm do servidor; adaptar escala GUI e separar técnicas bloqueadas das em recuperação |
| Despertar | Altar, desafios/bosses, alma vinculada ao personagem, duração e encerramento | Boss derrotado e alma consumida liberam G; limpeza em morte/logout/dimensão, sem bônus duplicados |
| Três kits existentes | Reorganizar Gojo, Shadow e Frieren e implementar suas formas | Todas as técnicas base e transformadas exercitadas com hitbox, VFX, animação e recarga correspondentes ao GDD |
| Asta, Ichigo, Tanjiro | Antimagia, espadas, respirações e formas | Armas, defesa/reflexão, silêncio e rastros próprios, com colisão e sincronização |
| Naruto, Jin-Woo, Eren | Clones, sombras, sapo, dragão, DMT e titã controlável | IA aliada, limite de invocações, movimento/câmera/montaria e descarte corretos |
| Sukuna, Rimuru, Killua, Genos | Domínio, elementos, devorar, yo-yos, eletricidade e calor | Mecânicas distintas, custos, efeitos negativos e limites de alteração de terreno |
| Jotaro e Edward | Tempo parado, dano adiado e transmutação | Restaurar IA/gravidade; liberar dano uma vez; não duplicar blocos/itens nem deixar entidades presas |
| Equipamentos | Armas e quatro peças para todos os personagens | Modelos originais por slot, receita survival, skin preservada e bônus de conjunto de 20% |

O tempo parado de Jotaro será simulado por estado das entidades na área afetada, sem suspender o loop inteiro do servidor. É uma escolha de implementação para manter rede, outros jogadores e limpeza de estados funcionando durante os seis segundos. Toda alteração de terreno precisa respeitar proteção de blocos, configuração e orçamento por tick já usados pelo Minezin.

Cada personagem só deve aparecer como jogável após possuir as duas formas, o despertar, equipamento exigido e testes de encerramento. A qualidade visual precisa ser avaliada em movimento no Minecraft; compilar shaders ou renderizar material isolado não comprova o resultado de uma técnica.

## Referências consultadas

As notas abaixo se baseiam nas páginas dos projetos e suas descrições. Não foram executados os mods nem foram avaliados todos os seus vídeos; os pontos de aplicação são decisões para estudar no Minezin, não afirmações de equivalência visual.

| Projeto e fonte | Informação confirmada | Aplicação a estudar |
|---|---|---|
| [True Prime Piece](https://www.curseforge.com/minecraft/mc-mods/true-prime-piece) | Progressão por combate, distribuição de atributos e obtenção de poderes | Ciclo de progressão legível no survival |
| [Prime Slayer](https://www.curseforge.com/minecraft/mc-mods/prime-slayer-demon-slayer-mod) | Treinamento por quests, estilos de respiração e estruturas | Desafios de desbloqueio e identidade das respirações |
| [Quirks Unchained](https://quirks-unchained.github.io/wiki/) | Wiki oficial descreve 26 individualidades e versão Forge 1.19.2 | Diversidade de kits e explicação das habilidades; referência, não dependência 1.20.1 |
| [Cursed Fate](https://www.curseforge.com/minecraft/mc-mods/cursed-fate) | Técnicas, NPCs, quests, traits, votos e evolução | Domínios, apresentação de habilidades e interação dos sistemas |
| [Prime Punch](https://www.curseforge.com/minecraft/mc-mods/prime-punch-one-punch-mod) | Habilidades de One Punch Man; usa Photon, GeckoLib e Player Animator | Sensação de impacto e composição de animação/VFX |
| [Solo Leveling Reawakening](https://efkrdnz.github.io/mods/solo-leveling-reawakening/) | Página do autor descreve portais, dungeons e progressão de classe | Exército de sombras, progressão e encontros |
| [Kira Death Note](https://www.curseforge.com/minecraft/mc-mods/kira-death-note) | Nome/URL encontrados na busca; acesso direto retornou 404 | Pendente de confirmação de página acessível; não inferir mecânicas |
| [Danny's AOT](https://modrinth.com/mod/dannys-aot) | DMT com ganchos/órbita/impulso e transformações; página consultada lista Fabric 1.21.1 | Controle do DMT e câmera do titã; não instalar como dependência Forge 1.20.1 |
| [JCraft Eyes of Ender](https://modrinth.com/mod/jojo-eyes-of-ender) e [wiki](https://wiki.jcraft-eoe.com/index.php?title=Main_Page) | Combate, stands e habilidades em Forge/Fabric 1.20.1 | Stand, legibilidade e desenho do combate de Jotaro |
| [Alien Evolution](https://www.curseforge.com/minecraft/mc-mods/alienevo) | Projeto baseado em Palladium para Ben 10 e progressão de transformações | Separação de estado, forma visual e habilidades disponíveis |
| [Elementals](https://modrinth.com/mod/elemental) | Habilidades de dobra elemental; versões/loaders variam | Linguagem visual por elemento |
| [Cobblemon](https://modrinth.com/mod/cobblemon) | Criaturas com progressão, evolução, combate e montaria | Silhuetas e apresentação de invocações/companheiros |
| [Poke Clothing](https://modrinth.com/mod/poke-clothing) | Roupas craftáveis e estética próxima do Minecraft; a página descreve Fabric 1.21.1 e Forge 1.16.5 | Roupas limpas, reconhecíveis e obtidas no survival |
| [Touhou Little Maid](https://modrinth.com/mod/touhou-little-maid) | Companheiras com tarefas/combate e modelos personalizados | Modelos e animações de NPCs aliados |
| [Block Pops](https://modrinth.com/mod/block-pops) | Figuras colecionáveis e coleções de personagens/jogadores | Reconhecimento de personagem em formas simples; colecionáveis não entram no escopo do GDD por essa referência |

As referências não foram adicionadas como dependências. O Minezin continua em Forge 1.20.1 com suas bibliotecas atuais. Não há importação de código, modelos, texturas, sons ou vídeos desses mods nesta entrega.
