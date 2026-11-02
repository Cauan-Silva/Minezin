# Ichigo e ultimates transformadas — 3.6.0-preview.1

Base: `a4a663a289c74780bb524dc3c3c10a3a405a7246` (PR #20). Branch local: `feat/ultimates-transformadas-ichigo`. Sem publicação nesta etapa.

## Compatibilidade

Ichigo usa o ID **6** reservado no GDD. Gojo=1, Shadow=2, Frieren=3 e Asta=4 permanecem; o ID 5 continua reservado para Eren. Arrays persistentes mantêm 15 kits e o esquema existente. Nenhum progresso antigo é maximizado automaticamente. O protocolo passa de 12 para **13**, exigindo a mesma versão nos clientes e no servidor devido ao novo kit, itens e efeitos.

Não há nova biblioteca de runtime. Os SFX desta leva reutilizam os sons originais já presentes no projeto; nenhuma voz ou áudio de anime foi copiado.

## Kit jogável

Requer Zangetsu na mão principal. Pesquisar em P: 220 conhecimento, oito lápis-lazúli e quatro ametistas. Técnicas e árvore H seguem os requisitos RPG existentes. Os valores abaixo são recargas-base, antes da redução por maestria.

| Tecla | Base | Bankai |
| --- | --- | --- |
| Z | Getsuga Tenshou azul; preparação de oito ticks; 5 s | Kuroi Getsuga preto/vermelho, liberação imediata; 1 s |
| X | Passo Flash até 10 blocos; 8 s | Shunpo até 15 blocos, silhuetas vermelhas; 2 s |
| C | Pressão Espiritual, empurra inimigos visíveis até oito blocos; 15 s | Getsuga Cruzado, duas meias-luas em X; preparação de dez ticks; 12 s |
| V | Corte Ascendente, alcance cinco blocos e impulso vertical; 20 s | Máscara Hollow 3D por dez segundos: +50% de dano físico/mágico; 40 s |
| G | Ativa Bankai após árvore, desafio e alma | Encerra Bankai; não devolve a recarga de G |

Bankai custa a mana de ativação padrão (50), tem recarga-base de cinco minutos e **não tem duração temporal fixa**: consome oito pontos de mana por segundo enquanto ativo (bônus do conjunto aplicável). A regeneração RPG continua funcionando; sem mana suficiente, o servidor encerra a forma e a máscara. O criativo mantém as exceções existentes de custo de mana. O HUD identifica que a forma é mantida por mana; o relógio visual de 1.200 ticks é renovado para observadores, sem repetir a animação inicial.

Colisões de Getsuga usam varredura entre posições consecutivas, o primeiro bloco sólido e um conjunto de UUIDs atingidos por golpe. Alcance máximo de 28 blocos e uma aplicação de dano por alvo/golpe. Não destrói terreno. Passo Flash verifica espaço, chunks carregados e borda do mundo. Alvos aliados são excluídos.

A máscara acompanha a cabeça real do jogador, e o casaco Bankai acompanha tronco/pernas. Não adiciona cabelo ao jogador. As duas geometrias da espada são selecionadas pelo dono real do ItemStack no contexto de mão; inventário e espada de outro jogador não herdam a transformação.

## Equipamento e chefe

- Zangetsu: receita em grade ` II / SI / S  ` com blocos de ferro e gravetos; arma 3D de lâmina larga. Bankai troca para Tensa Zangetsu fina, negra, com guarda e corrente.
- Traje Shihakusho: ferraria com papel, peça de netherita correspondente e lápis-lazúli. Conjunto completo usa o bônus RPG de mana já existente.
- Zangetsu Interior: modelo próprio, textura original 64×64 com UV Minecraft, arma vinculada à mão, aviso de ataque e linha espiritual bloqueada por terreno. Usa o altar e a alma vinculados ao dono do desafio. Tamanho normal de dois blocos; não reutiliza a escala configurável do Liebe.

## Ultimates transformadas atuais

Animações específicas de braços e tronco para Roxo Máximo (incluindo o marco de colapso no tick 90), atração do Shadow, magia descendente da Frieren (tick 25) e golpes despertos do Asta. Mantidos preparo, pose de impacto breve e recuperação, sem congelar o servidor ou a entrada do jogador. Chuva de espadas acentua o primeiro impacto no tick 24; subpacotes de dez ticks não reiniciam a pose/SFX. Black Divider acentua o tick 18. Impact frames respeitam opções de flash, efeitos reduzidos, distância, campo de visão e oclusão existentes. Não foram alterados os tempos de dano dos quatro kits anteriores.

## Empunhadura do Asta

O modelo anterior tinha o cabo entre Y=-10 e Y=0, com o centro fora da origem da mão. A geometria passa a centralizar o cabo em Y=0 e reposiciona o pivô da lâmina de Y=1,2 para Y=6,2. Ajustes de mão direita/esquerda orientam a espada como uma arma segurada, com inclinação curta em terceira pessoa e face visível em primeira. Apenas o osso da lâmina cresce no Black Divider; cabo e guarda continuam com suas dimensões.

A animação de aparar com botão direito mantém o mecanismo de antimagia existente; não foi convertida ao bloqueio vanilla que concederia imunidade física de escudo. As prévias de geometria não comprovam o encaixe no renderer de mão do jogo.

## Teste rápido

1. Instalar somente o JAR desta versão com as dependências existentes; usar um mundo de testes/cópia. No multiplayer, atualizar os dois lados.
2. `/give @s arcanecontrol:zangetsu` e `/minezin rpg maestria maxima ichigo`; selecionar Ichigo em P. O comando não muda o kit ativo.
3. Testar Z/X/C/V sem Bankai e contra parede/aliado; G ativa a forma. Testar novamente as quatro teclas e a máscara, segurando Zangetsu.
4. `/minezin rpg restaurar_recursos` repõe mana e zera recargas RPG. Esse comando não termina ações em execução.
5. Testar esgotamento de mana em survival, cancelar G, morrer, trocar dimensão e reconectar; máscara/casaco/arma devem retornar ao estado normal. Em criativo, usar G para encerrar.
6. Para o fluxo survival completo, comprar Despertar na árvore sem usar o comando de maximização, vencer Zangetsu Interior no altar e consumir a alma do kit.
7. Asta: primeira/terceira pessoa, mão dominante esquerda/direita, skins normal/slim, parado/andando/atacando/aparando, Black Divider e inventário. Conferir centro do cabo na mão e retorno da lâmina.
8. Gojo/Shadow/Frieren/Asta: testar ultimates despertas com impact frames habilitados e desabilitados, efeitos reduzidos, outro jogador e cancelamento antes de um impacto programado.

## Limites

Cliente Minecraft, servidor dedicado e multiplayer ainda precisam de validação manual; não foram abertos durante o uso do PC. As prévias OpenGL mostram geometria/material, não capturas do jogo. Valores de dano, custo e duração da máscara são uma primeira parametrização para teste de balanceamento (o GDD não fixa todos esses valores). A01/P1 de destruição direta do Roxo normal permanece fora do escopo; testar em cópia do mundo.


## Resultado das verificações locais

- Forge/Java 17: `test build` e reobfuscação aprovados; 106 testes Java, zero falhas/erros/ignorados.
- Três testes Python de projetos Blockbench aprovados.
- OpenGL na RTX 5060 Ti: novos modelos do Ichigo (quatro conjuntos de vistas), geometria do Divider, poses dos trajes existentes, 11 materiais e 12 composições temporais renderizados.
- Geradores do Ichigo e das animações repetidos sem alterar os assets; `git diff --check` sem erros.
- JAR conferido por integridade ZIP, versão, classes novas e recursos comparados byte a byte aos arquivos-fonte.
- Nenhuma execução nova de GitHub Actions, commit, push ou PR. Validação real no Minecraft permanece pendente.
