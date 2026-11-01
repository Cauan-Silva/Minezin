# Despertar dos três kits — 3.0.0

O fluxo survival agora é: pesquisar o kit em P, subir a maestria até 50, comprar todos os pré-requisitos e o nó Despertar em H, construir o altar, vencer a nêmesis e usar a alma com o kit correspondente ativo. A alma libera G permanentemente para aquele kit; não é consumida a cada transformação.

## Altar e chefes

Receita (bancada): `GAG / DDD / OEO`, sendo G lingote de ouro, A bloco de ametista, D ardósia polida, O obsidiana e E diamante. Use o altar mirando uma área com chão firme cinco blocos à frente. Agachar + usar cancela o desafio sem recompensa. O altar pode ser recolhido com picareta de ferro ou superior.

Gojo enfrenta Toji (360 PV, avanço com lança); Shadow enfrenta Aurora (420 PV, erupções de sangue); Frieren enfrenta Qual (460 PV, raio e redução de dano durante a conjuração). Os ataques têm avisos geométricos antes do impacto e ficam mais frequentes abaixo de metade da vida. A alma é entregue ao inventário do desafiante; se estiver cheio, cai aos seus pés. Chefes abandonados não concedem alma.

Há um desafio por jogador, identificado por UUID persistente. Morte, saída, mudança de dimensão ou afastamento de 64 blocos do altar cancela o desafio. Chefes descarregados ficam inválidos após cancelamento e são descartados quando carregarem. Desafios não são retomados após reconexão.

## Teclas e formas

Z/X/C/V usam o moveset da forma atual. G ativa ou encerra a transformação; R permanece como atalho compatível para G. A mana é compartilhada; as recargas de base e transformação são separadas. G mantém uma única recarga, inclusive depois de encerrar a forma ou trocar de kit. A maestria reduz as recargas conforme o sistema RPG existente. Criativo dispensa mana, mas continua exigindo árvore e alma para G.

| Kit | Base Z / X / C / V | G | Transformado Z / X / C / V |
|---|---|---|---|
| Gojo | Azul / Vermelho / Teleporte Curto / Vazio Roxo | Vazio Infinito, 20s, recarga base 360s | Morte Cerebral pelo olhar / Teleporte no domo / Chuva de Informações / Roxo Máximo, uma vez por domínio |
| Shadow | Corte de Slime / Passo das Sombras / Fios de Slime / Lâmina Oculta | I Am Atomic, 40s, recarga base 300s | Omni Slash / Chuva de Lâminas / Buraco Negro de Sombras / Cura total, uma vez por forma |
| Frieren | Zoltraak / Barreira Arcana / Impulso Arcano / Gaiola de Vento | Liberar Mana, 45s, recarga base 300s | Vinte raios Zoltraak / Julgamento / Buraco Negro Mágico / Escudo Celestial |

O cajado usa o mesmo Z do teclado e compartilha a recarga. Frieren levita em posição fixa durante G; a gravidade anterior é restaurada ao encerrar, morrer, sair ou mudar de dimensão. O Escudo Celestial protege por 6s a dona e aliados de equipe dentro de oito blocos, exceto fontes que ignoram invulnerabilidade. Chuva de Lâminas e Julgamento colidem com o terreno. O buraco negro das sombras atrai entidades; o da Frieren usa a destruição e os blocos físicos já existentes, respeitando a configuração do mundo. Atomic usa inversão de cor de um frame no impacto (opção de clarão respeitada), mantém a carga e destruição existentes, mas libera o controle após 5s. A cinematografia de abertura dura 5s para Atomic e 3s para o domínio; os efeitos continuam durante a forma.

As novas nêmesis usam modelos GeckoLib originais e a paleta suave existente. A prévia OpenGL mostra a geometria e o atlas reais; não representa iluminação, animações ou uma captura do Minecraft.

## Roteiro de teste manual

1. Em cópia de um mundo 2.3, verificar maestria, mana e todas as recargas antigas preservadas. Confirmar que G legado não dispensa a alma.
2. Em survival, tentar altar sem nível/nó; confirmar rejeição sem chefe. Após obter nível 50 e comprar a árvore, invocar cada nêmesis. Testar chão bloqueado, dois cliques e segundo altar durante um desafio.
3. Vencer cada chefe e conferir uma única alma. Tentar usá-la em outro kit, sem nó e depois de já ter despertado. Não deve consumir nesses casos.
4. Ativar G nos três kits, conferir HUD, mana, duração e recarga. Encerrar e reativar: deve continuar em recuperação. Tentar trocar o kit durante a forma.
5. Testar ZXCV base e transformado, uso único de Roxo Máximo/Cura, obstáculos em teleporte e raios, alvo móvel em Zoltraak e aliados no Escudo Celestial.
6. Durante as formas, morrer, desconectar e mudar de dimensão. Confirmar câmera, gravidade e efeitos limpos. Outro jogador deve ver a aura ao começar a acompanhar um jogador já transformado.
7. Durante o desafio, sair/morrer/afastar-se e descarregar o chunk. Recarregar depois: nenhum chefe antigo deve conceder alma ou bloquear um novo desafio.
8. Conferir Atomic e Buraco Negro com destruição ativada e desativada, proteção de blocos e custo de frame em multiplayer.

Para testes administrativos: `/minezin rpg adicionar_xp 4900`, comprar os nós em H, `/give @s arcanecontrol:altar_despertar`. Para testar somente a ativação de G, `/give @s arcanecontrol:alma_despertar{kit:1}` (1 Gojo, 2 Shadow, 3 Frieren), ainda exigindo o nó.

## Validação e limites

Testes automatizados cobrem duração/expiração, exclusividade, autorização da alma, migração de 75 para 150 posições de recarga, isolamento de formas, recarga única de G, NBT/rede e hierarquia dos modelos. A compilação Forge e os relatórios completos são produzidos no GitHub Actions. Teste visual e de combate dentro do Minecraft permanece necessário; não foi executado nesta máquina. Esta etapa implementa os três kits existentes; os outros doze continuam como catálogo de design.
