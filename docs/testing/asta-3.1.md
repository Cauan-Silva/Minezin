# Asta — versão de teste 3.1.0

Quarto kit executável, mantendo Gojo, Shadow e Frieren e os IDs/progressos anteriores. Asta é ID 4. Ichigo e Tanjiro ficam para as próximas entregas. Protocolo 11: servidor e clientes precisam da mesma versão.

## Survival

Em P, pesquise Asta por 180 de conhecimento, oito lingotes de ferro e quatro obsidianas. Na ferraria, papel + espada de diamante + obsidiana produz a Espada Matadora de Demônios; papel + peça de netherita + obsidiana produz a peça correspondente dos Touros Negros. As transformações de ferraria preservam os dados da peça base. O conjunto completo concede o desconto de 20% de mana.

A faixa preserva a skin: não substitui rosto ou cabelo. O traje inclui manto curto, emblema dos Touros Negros, braçadeiras e estojo de grimório. Asa negra e chifre só aparecem durante o despertar, inclusive sem armadura equipada.

Com Asta ativo, alcance maestria 50 e compre os pré-requisitos e o nó Despertar em H. Use o Altar do Despertar para enfrentar Liebe (480 PV): ataque físico e erupção de antimagia com aviso de 22 ticks, aplicando Silêncio por 3s. Abaixo de metade da vida, os ataques especiais ficam mais frequentes. Use a alma correspondente para liberar G permanentemente.

## Moveset

Todos os ataques requerem a Espada Matadora na mão principal. Recargas abaixo são os valores base; maestria reduz os prazos.

| Tecla | Base | Assimilação Demoníaca |
|---|---|---|
| Z | Cone frontal de 120°, dano físico e reflexão; 4s | Black Meteorite: avanço guiado até inimigo mirado em até 20 blocos, impacto físico em área; 3s |
| X | Corte Divisor vertical; 8s | Furacão Negro: remove efeitos benéficos de inimigos em oito blocos; 10s |
| C | Avanço Touro Negro: deslocamento com colisão, -75% de dano e sem recuo durante o avanço; 12s | Black Divider: golpe linear de 18 blocos e fenda no terreno; 15s |
| V | Barreira frontal de antimagia por 5s; 25s | Meia-lua de antimagia que aplica Silêncio por 4s; 20s |
| G | Assimilação: asa, chifre, tinta negra, câmera com pico de FOV 120; duração 60s, recarga base 5min | Encerra a forma; não devolve a recarga de G |

A abertura de G dura 55 ticks e segura novos golpes até terminar. Câmera/FOV/tremor respeitam a opção de cinematografia. G pode ser encerrado mesmo sob Silêncio. As duas formas compartilham mana e possuem recargas ZXCV independentes.

Segurar o botão direito com a espada ergue uma guarda: bloqueia magia/projéteis frontais ao custo de dois de mana por bloqueio (com desconto de conjunto). Ataques físicos comuns atravessam a guarda. A Barreira de V já paga seu custo ao conjurar. Reflexões são limitadas a duas por projétil e têm intervalo de dez ticks; o jogador passa a ser o responsável pelo projétil refletido. Projéteis já pertencentes a ele são ignorados.

Silêncio bloqueia novas técnicas Minezin, interrompe conjurações das nêmesis e impede novas poções arremessadas, bolas de fogo, projéteis de shulker e presas de evocador dos inimigos afetados. Não é uma integração universal com magias de outros mods. Não cancela ataques físicos nem apaga permanentemente atributos de RPG.

A fenda usa a configuração de destruição do Atomic, proteção por evento de quebra e o orçamento global existente. Não há drops de blocos destruídos pelas habilidades. Desativar a destruição mantém dano e efeitos.

## Teste administrativo

1. Em P, desbloqueie Asta em criativo e selecione o kit. `/give @s arcanecontrol:matadora_demonios`.
2. `/minezin rpg adicionar_xp 4900`, compre a árvore em H e `/give @s arcanecontrol:altar_despertar`. Teste o desafio antes de usar uma alma por comando.
3. Para testar só G: `/give @s arcanecontrol:alma_despertar{kit:4}`; use-a com Asta ativo e o nó comprado.
4. `/minezin rpg mana 100` permite repetir os testes de consumo sem esperar regeneração. Criativo não cobra mana; use survival para conferir custos.

## Roteiro manual de regressão

- Abrir um mundo 3.0 com os três kits antigos: conferir seleção, nós, XP, mana, usos e recargas preservados. Pesquisar Asta não deve mudar os outros perfis.
- Conferir P e H em GUI 320×240 e escalas maiores. Só quatro kits aparecem como jogáveis.
- Testar Z contra inimigos à frente/atrás; flechas e bolas de fogo devem refletir apenas no cone frontal. Dois Astas não devem formar reflexão infinita.
- Testar guarda com e sem mana, ataque físico frontal, magia pelas costas e Barreira V. Conferir custo e desconto de conjunto.
- Testar dash em paredes, teto baixo, bordas de chunk e fronteira do mundo; alvo móvel e alvo morto antes do impacto do Meteorite.
- Aplicar efeitos benéficos a um inimigo e testar Furacão Negro. Conferir que os passivos de maestria não são removidos.
- Testar Silêncio em jogador, Qual, Liebe, bruxa e evocador, incluindo a possibilidade de cancelar G enquanto silenciado.
- Testar Black Divider com destruição ligada/desligada e blocos protegidos, baús, líquidos e blocos indestrutíveis.
- Durante G, morrer, mudar de dimensão, sair, encerrar manualmente e aguardar expiração. Conferir retorno de câmera/FOV e remoção da asa/chifre. Outro jogador deve ver a forma ao começar a acompanhar um Asta já transformado.
- Conferir ícones de armadura, espada nas duas mãos/primeira pessoa/terceira pessoa e preservação da skin sem cabelo imposto.

## Evidências e limites

O gerador `tools/build_asta_assets.py` produz geometria original, gestos, receitas e um glifo nativo de 16 pixels para Silêncio, reutilizando as paletas existentes. A prévia da asa usa o mesmo JSON de origem exportado para inspeção; não é captura do Minecraft. A compilação Forge, serialização de rede/NBT e shaders são verificados no GitHub Actions. As regras locais cobrem migração de IDs/perfis, cone frontal, limite de reflexão, tempos e leitura dos gestos. Combate, movimento, câmera e composição de VFX ainda exigem validação dentro do Minecraft.
