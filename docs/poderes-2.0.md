# Poderes, progressão e trajes — 2.0.0

Forge 1.20.1 / Java 17. Cliente e servidor precisam da mesma versão (protocolo 7). As dependências da versão anterior continuam necessárias: GeckoLib, PlayerAnimator e Photon com LDLib incluída.

## Menu e survival

Pressione **P** para pesquisar e selecionar kits. É possível alternar entre todos os kits desbloqueados; XP e maestria de cada um permanecem separados. A troca aguarda o término das técnicas em andamento e preserva recargas. Gojo começa disponível e mantém sua árvore anterior em **K**.

Cada inimigo hostil derrotado pelo jogador rende 10 de conhecimento. A pesquisa consome conhecimento e materiais do inventário:

| Kit | Conhecimento | Materiais |
|---|---:|---|
| Cid / Shadow | 120 | 8 fragmentos de ametista + 4 obsidianas |
| Frieren | 240 | 8 lápis-lazúli + 4 fragmentos de ametista |

Nos kits Cid e Frieren, conjurações bem-sucedidas rendem 5 XP e inimigos derrotados rendem 25 XP para o kit ativo. Cada 100 XP concede um nível, até 20. As quatro técnicas abrem nos níveis 1, 2, 3 e 5; cada 20 usos de uma técnica concede uma maestria, até 5, reduzindo a recarga em até 25%. Gojo mantém as regras da árvore existente.

No Criativo, pesquise sem custos e receba nível 5 para testar o moveset completo. Depois selecione o kit. Personagens antigos com dados de Cid ou Lâmina Shadow no inventário recebem Cid desbloqueado no nível 5 na primeira migração.

## Técnicas

| Tecla | Cid (Lâmina Shadow na mão) | Frieren (sem item obrigatório) |
|---|---|---|
| Z | Corte de Slime: impacto sincronizado ao arco da espada | Zoltraak: feixe que para no primeiro bloco ou alvo válido |
| X | Passo das Sombras: percurso com colisão e miragens | Barreira Arcana: 4 s, absorve 70% do dano de entidades, reserva de 30 pontos |
| C | Fios de Slime: até 8 alvos, fios vinculados às entidades e atração | Impulso Arcano com queda lenta por 5 s |
| V | I Am Atomic: carga, selos, núcleo e explosão | Buraco Negro: 3,5 s de formação, atração até 11 s, colapso, fim em 14 s |

Gojo conserva Z/X/C/V/R; o Vazio Roxo ganhou raios ramificados pulsantes ao redor do projétil. Os gestos de Cid e Frieren foram escritos no projeto. A ultimate de Frieren possui roteiro de câmera voltado ao centro da singularidade. A representação do buraco negro é uma interpretação visual para este kit.

O colapso usa explosão nativa do Forge para destruição, respeitando seus eventos. Em `<mundo>/serverconfig/arcanecontrol-server.toml`, `buracoNegroDestruirTerreno=false` desliga a destruição da singularidade. A configuração existente de Atomic continua independente. Teste as ultimates em uma área preparada para destruição.

## Armaduras

Novos conjuntos de Gojo e Frieren possuem geometria por peça, textura original compartilhada, detalhes de tecido, sem cabelo adicionado e animações sutis. As mãos e o rosto usam a skin do jogador. As peças aparecem na aba de combate do Criativo.

Na ferraria: **papel + peça de netherita + fragmento de ametista** produz Gojo; **papel + peça de diamante + barra de ouro** produz Frieren. Receitas `smithing_transform` preservam dados e encantamentos da peça base. IDs: `arcanecontrol:gojo_cabeca`, `gojo_peito`, `gojo_pernas`, `gojo_botas`; os de Frieren usam o prefixo `frieren_`.

## Validação e teste em jogo

Os testes automatizados cobrem progressão, limites de dados persistidos, maestria, migração, tempos dos efeitos, carregamento dos gestos e estrutura dos assets. As ferramentas em `tools/preview_*` renderizam os shaders e modelos reais em OpenGL; essas imagens **não são capturas do Minecraft** e não incluem a composição completa, animação em jogo ou bloom do Photon.

Antes de adotar no survival principal, verificar em uma cópia de teste: pesquisa e troca após reconectar/morrer; seleção em multiplayer; dano e recargas; animações com todas as peças equipadas; ícones no inventário; câmera da ultimate; colisões de dash; cancelamento por morte ou dimensão; restauração de projéteis ao desligar Infinito; desempenho com mais de um conjurador. A qualidade final em movimento ainda depende dessa avaliação em jogo.

O atlas original e seu prompt estão documentados em `armor-atlas-generation.md`.
