# Integração RPG 2.3

Esta entrega liga a fundação do GDD ao servidor e aos três kits jogáveis existentes. Adiciona progressão de maestria até 100, mana, pontos, atributos, árvore em H e HUD de recarga. Os novos personagens, bosses, altar e formas despertas continuam na próxima etapa do plano; a reorganização dos movesets em G será feita junto desses componentes. O catálogo do GDD continua sendo a especificação aprovada.

## Uso em jogo

- **P** continua pesquisando e selecionando kits desbloqueados.
- **H** abre a árvore do kit. **K** funciona como atalho de compatibilidade para a mesma tela.
- X, C e V são compradas com pontos; os passivos aumentam dano físico, dano mágico, regeneração de mana e movimento.
- Os nós mostram nível/custo; o servidor valida propriedade do kit, pré-requisitos e pontos antes de descontar.
- O nó Despertar informa que está indisponível e não aceita pontos nesta versão. Não há objetivo de boss ainda acessível.
- O HUD no lado direito mostra kit, nível, mana e recargas com setores radiais; MP indica mana insuficiente, considerando o desconto de conjunto informado pelo servidor. Durante cutscenes o HUD dá lugar às legendas existentes.
- O menu P também mostra os níveis novos e permite abrir a árvore do kit selecionado para consulta.

Os ataques desta etapa mantêm seus efeitos e teclas atuais, inclusive Infinito em C e Atomic/Buraco Negro em V. O domínio em R é preservado para personagens que já o haviam adquirido. Não há nova compra do domínio legado; a futura obtenção em G seguirá o fluxo do GDD. Essa compatibilidade evita retirar uma técnica adquirida antes de existir seu novo desafio de desbloqueio.

## Progressão e mana

Cada uso bem-sucedido concede 5 XP; inimigos derrotados concedem 25 XP. Dano efetivamente causado após defesa/armadura concede de 1 a 10 XP por evento, no máximo uma vez a cada dez ticks, contra inimigos hostis ou jogadores não aliados. XP continua separado por kit, com 100 por nível e limite 100. Subir de nível produz partículas de totem e som.

O nível 1 é a referência: cada nível adicional concede +2 de vida máxima, +1,5% de dano e -0,5% de recarga. Os nós passivos acrescentam os valores documentados na fundação. Bônus de vida e velocidade usam UUIDs fixos de modificador; aplicar novamente não deve empilhar o efeito. Trocar de kit ajusta os atributos e limita a vida ao novo máximo, sem curar o jogador.

Mana tem capacidade 100 e regeneração base de 5/s. Os custos provisórios dos slots são Z=8, X=12, C=20, V=35 e domínio legado R=50. O conjunto completo do personagem ativo reduz esses custos em 20%; peças misturadas não concedem o desconto. A reserva é compartilhada entre kits e não é recarregada por troca, morte, dimensão ou reconexão. Desligar Infinito é gratuito, inclusive sem mana, e não concede XP. No Criativo, as técnicas base dispensam nós e mana, mantendo a recarga para testar o combate.

Custos e curva de XP são decisões de balanceamento provisórias, pois o GDD não especifica esses números. As recargas-base atuais foram preservadas nesta integração; os valores específicos e novas posições do GDD serão aplicados na migração de cada moveset.

Cajado e tecla Z usam a mesma autorização de Zoltraak. Cast bloqueado por nível, kit, recarga ou mana não inicia uma nova recarga nem cobra mana. A ultimate da Frieren valida alvo/distância antes da cobrança. O cliente envia IDs de ação/kit/nó, nunca XP, mana ou pontos.

## Persistência e migração

Os dados ficam em `PlayerPersisted/minezin_rpg_v3`, separados do legado: versão do schema, maestria/nós por kit, mana e prazos/durações de recarga. Os três kits mantêm seus IDs 1/2/3; os demais permanecem sem seleção jogável.

Na primeira entrada, a migração lê o progresso antigo. Técnicas já adquiridas são convertidas em nós correspondentes. Se os novos requisitos forem maiores, o nível mínimo é elevado para preservar a técnica (por exemplo, um V adquirido é preservado no nível 10). A migração não concede despertar. O domínio legado recebe uma marca própria. Recargas em andamento são copiadas e limitadas a dez minutos de saldo para sanitizar valores inválidos.

Depois da migração, o perfil novo é a autoridade; relogar não deve migrar novamente ou repor mana. A clonagem por morte copia o perfil inteiro, incluindo recargas. O respawn recupera a vida máxima como no respawn normal, sem repor a mana. A dimensão conserva recursos e progresso.

Cliente e servidor exigem **2.3.0 / protocolo 9**. O snapshot é sincronizado a cada dez ticks, além de compras, casts, XP, pesquisa e troca de kit. O cliente calcula apenas o tempo visual restante; toda autorização continua no servidor.

## Interface e compatibilidade gráfica

A árvore ocupa a tela e usa o blur nativo quando não há outro pós-processamento ativo. Ela guarda a referência do efeito criado e só o encerra se ainda for o mesmo, para não apagar um efeito assumido por outro sistema. O layout é calculado em pixels de GUI e tem testes desde 320×240 até 1920×1080. A aparência e convivência com shaders externos ainda exigem avaliação no cliente Minecraft.

## Comandos administrativos de teste

Com cheats ou permissão de operador nível 2, `/minezin rpg adicionar_xp 4900` concede XP ao kit ativo e permite testar os nós sem grind. `/minezin rpg mana 0` e `/minezin rpg mana 100` permitem conferir bloqueio e cobrança. Os comandos não concedem despertar, não fazem parte da progressão survival normal e não estão disponíveis para jogadores sem essa permissão.

## Verificação manual pendente

Testar uma cópia de mundo antigo com técnicas e recargas adquiridas; entrar duas vezes e comparar XP/nós/mana. Testar morte, respawn, mudança de dimensão, troca de kit com menos vida máxima e alocação repetida sem empilhar atributos. No survival novo: comprar X/C/V, tentar compra sem pontos, equipamento completo versus incompleto e uso sem mana. No multiplayer: usar cajado e Z rapidamente, trocar kit antes do pacote de cast e conferir que os recursos vêm do servidor. Conferir H/K/P, escalas de GUI, blur ao fechar/trocar de tela, setores radiais e legendas de ultimate.

Os testes automatizados cobrem regras transacionais, limites de dados, migração pura, geometria do layout e round-trip real de NBT/pacotes. Não foi executado cliente Minecraft neste ambiente; compilar e testar serialização não comprova a aparência nem todo o ciclo de eventos em um mundo real.
