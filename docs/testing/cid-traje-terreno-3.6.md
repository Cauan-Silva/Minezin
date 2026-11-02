# Teste restrito ao Cid: manto, emissão e terreno — 3.6.0-preview.5

Base: `35958ec842ebe717a196c02844df2435e051210c` (PR #22). Branch local: `feat/cid-traje-inercial-terreno`. Somente o Cid recebeu o teste de traje dos estudos. Não há implementação de restauração temporária de terreno nem alteração dos movesets dos demais personagens.

## Comando de terreno

```mcfunction
/minezin atomic terreno false
/minezin atomic terreno true
/minezin atomic terreno
```

`false` desliga a quebra de blocos da Atomic; `true` reativa; sem argumento consulta o estado. Exige operador/cheats, nível 2, e funciona também no console. A configuração é global para todos os jogadores e dimensões do mundo/servidor e é salva na configuração SERVER existente (`atomic.destruirTerreno`), sem exigir reinício.

O impacto consulta a preferência ao detonar. A cratera agora possui identificação Atomic na fila e verifica a opção a cada tick: desligar remove o restante da fila sem interromper crateras de outras habilidades. Partes já destruídas não são restauradas. Reativar não retoma uma fila descartada.

A explosão usa `ExplosionInteraction.NONE` quando desligada, mantendo dano a entidades, som e efeito visual sem destruir blocos. O dano mágico explícito e o impulso da Atomic permanecem. **Correção de comportamento:** antes, o mesmo `if` desligava a chamada da explosão inteira, incluindo seu dano adicional. Agora o controle trata apenas terreno, conforme seu nome. Com destruição ativada o comportamento anterior permanece; com ela desligada a explosão também continua causando dano. Esse comando não oferece invulnerabilidade e não bloqueia destruição de outras técnicas/mods.

## Traje do Cid

- Cinco painéis traseiros, cada um com quatro ossos encadeados. Segmentos sobrepostos evitam aberturas nas articulações.
- Cinco perfis independentes de molas angulares para a capa e um perfil próprio para a gola, avançadas em ticks fixos (20 Hz, dois subpassos), com amortecimento, limites e interpolação no render. FPS não determina a velocidade da simulação.
- Arrasto por deslocamento horizontal, giro do corpo e movimento vertical. Reinício após teleporte superior a quatro blocos, troca de mundo, morte, remoção do manto ou troca da entidade. Cache limitado a 64 jogadores; quem exceder o limite recebe a pose estática.
- A simulação é visual e cliente; não há colisão física de tecido, entidades adicionais ou pacotes de sincronização.
- Máscara RGBA esparsa e separada para filamentos roxos e dois pequenos acentos dourados. Tecido conserva a iluminação ambiente; só detalhes escolhidos usam o passe emissivo fullbright.
- O renderer específico do Cid mantém a visibilidade por slot e aplica/restaura as mesmas rotações nos passes difuso/emissivo. Modelos compartilhados não devem herdar a pose de outro jogador ou do inventário.
- Cabeça/cabelo da skin ficam visíveis: o capuz do modelo vestido fica invisível nesta prévia, mantendo os atributos/ID do item. O ícone continua mostrando o capuz dobrado, evitando item invisível no inventário.

A camada emissiva usa `RenderType.eyes` e `LightTexture.FULL_BRIGHT`, sem biblioteca nova. Isso não cria luz dinâmica no ambiente nem garante bloom/compatibilidade em todos os shader packs. Não foi necessário substituir os renderers dos outros trajes.

Opções no cliente: `tecidoCid` e `emissaoCid` (ambas padrão true). Desligar tecido usa a pose estática; desligar emissão conserva as costuras difusas sem fullbright. O novo pós-processamento da Atomic mantém seus controles anteriores.

## Selos da Atomic

Os três selos passam a convergir de raios maiores até pequenos anéis próximos da âncora visual da lâmina antes do tick 90. A elevação só ocorre no final da carga. A âncora é a mesma aproximação visual já usada pelo efeito, não uma leitura exata da ponta do modelo da espada. A escala visual não altera raio de dano ou da cratera. Os impact frames aprovados no PR #22 permanecem.

## Fontes editáveis

`tools/build_shadow_trial.py` gera somente os modelos/ícones/texturas do Shadow e a máscara. A geração geral de armaduras chama essa etapa ao terminar, para não sobrescrever o teste com o manto anterior. O projeto `art/blockbench/shadow.bbmodel` está atualizado. Molas são executadas em Java; não são keyframes exportados ao Blockbench. As amostras de poses da prévia são exportadas pelos testes da simulação real.

Os estudos enviados foram tratados como referências de design. Não há alegação de que esses exemplos reproduzem a implementação interna de Apex Arcana ou Ronova. Nenhum recurso desses mods foi copiado. Não foram implementados os exemplos de `BlockSnapshot`, silenciamento global do áudio ou mudanças de moveset sugeridas nos documentos.

## Conferência no jogo, ainda pendente

1. Instalar a preview.5 e equipar o conjunto Shadow; confirmar cabeça/cabelo próprios, ombros completos e ícones. Testar Steve/Alex e primeira/terceira pessoa.
2. Andar, correr, girar, saltar, cair, agachar, nadar e usar Passo das Sombras. Verificar direção do arrasto, limites, junções e clipping; a simulação não possui colisões com corpo/mundo.
3. Ver no claro e no escuro, com `emissaoCid` on/off. Conferir a mesma pose nas duas camadas; testar shader packs separadamente.
4. Dois jogadores com o traje: poses independentes. Remover o manto, morrer, mudar dimensão e reconectar; não pode restar movimento do jogador anterior.
5. Com cheats/OP, executar `terreno false` antes de G: nenhum bloco pode quebrar, mas dano, som, debris cosméticos e VFX continuam. Consultar a opção e reiniciar o mundo/servidor para confirmar persistência.
6. Reativar com `true` numa cópia do mundo. Desligar durante a carga e durante o processamento da cratera; conferir que o trabalho pendente para e que outros kits mantêm suas regras. O comando não desfaz destruição instantânea já ocorrida.
7. Sem OP, os comandos devem ser recusados. Conferir execução no console dedicado e o retorno de erro se o arquivo de configuração não puder ser salvo.

Build/testes e prévias offline não substituem esse roteiro. Minecraft, servidor dedicado e multiplayer não foram abertos durante o uso do PC.


## Resultado das verificações locais

- Build Forge/reobfuscação com Java 17 aprovado. 117 testes Java, zero falhas/erros/ignorados; três testes Python de projetos Blockbench aprovados.
- Testes cobrem estabilidade/amortecimento/interpolação da mola, reinício, hierarquia de 20 ossos, preservação de cabeça, máscara emissiva esparsa, sintaxe booleana do comando e convergência dos selos.
- Nove vistas OpenGL com geometria e máscara reais; poses exportadas pela simulação Java. Emissão noturna comparada com a mesma cena sem emissão, afetando apenas detalhes esparsos.
- Gerador do Cid repetido sem alterar assets; recursos dos outros personagens conferidos sem alterações.
- JAR: versão, ZIP, classes novas, máscara e recursos comparados byte a byte. Projeto Blockbench atualizado e pacote separado com textura embutida.
- Minecraft real, persistência efetiva após reinício, execução do comando e comportamento dos blocos ainda precisam do roteiro manual. Nenhum commit, push ou novo PR nesta entrega.


## Ajuste da gola — preview.4

O teste no jogo enviado pelo usuário confirmou a fluidez da capa e apontou uma região ausente na gola. A geometria anterior tinha pontas frontais e acabamento em V nas costas, sem uma ligação traseira contínua. Esta revisão acrescenta uma base fixa, pontes laterais e painel traseiro sobrepostos, com folga na região da cabeça.

Três partes da gola e as abas frontais recebem offsets pequenos derivados da mesma simulação em ticks: no máximo 0,045 radianos (aprox. 2,5°) na gola e 0,10 radianos (aprox. 5,7°) nas abas. As molas e os cinco painéis da capa permanecem iguais à preview.3. O renderer aplica/restaura os mesmos offsets em ambas as camadas, incluindo os novos acentos dourados. A cabeça da skin permanece preservada.

Validação local atual: 119 testes Java, três testes Python e prévias OpenGL do traje e da gola. Os testes novos verificam hierarquia/conexão estática, limites dos detalhes e emissão seletiva. O encaixe com giro da cabeça, agachamento, nado e animações reais ainda precisa de conferência no Minecraft; não há simulação de colisão com a cabeça/corpo.

Referências registradas para uma próxima revisão da Atomic, sem alteração adicional de gameplay nesta correção:

- [God Wand](https://www.curseforge.com/minecraft/mc-mods/god-wand): a descrição oficial inclui linhas no chão, fortaleza, energia concentrada na espada, compressão, escuridão, coluna e marcas residuais. Foi lida a descrição, sem alegação de ter reproduzido o mod em execução. Não foi adotado seu bloqueio de movimento, imunidade ou liberação por segurar botão.
- [Apex Arcana](https://www.curseforge.com/minecraft/mc-mods/apex-arcana-irons-spells-n-spellbooks-addon): escala dos feitiços, céu e selos como referências de composição. Nenhuma dependência de Iron’s Spells ou outro mod foi adicionada.

Os mods são referências; nenhum código ou recurso deles foi copiado. O comando de terreno da preview.3 permanece disponível. Sem commit, push ou PR nesta entrega.

Conferência do JAR preview.4: simulação/cache da capa, comandos de terreno e combate comparados byte a byte com o JAR preview.3; idênticos. Recursos dos demais personagens preservados. Gerador do Shadow repetido sem mudar os assets.


## Movimento direcional e gola reduzida — preview.5

A gola anterior ficou volumosa no teste do usuário. A ponte traseira passou de 13,8 para 10,2 unidades de largura e de 4,45 para 2,5 de profundidade; o painel traseiro tem 1,8 de altura. Pontas frontais menores e ligações laterais mantêm o fechamento. A cabeça da skin continua visível.

A física anterior usava a magnitude horizontal, fazendo frente e ré produzirem o mesmo arrasto. Agora usa velocidades locais com sinal, variação de velocidade, giro e queda. Frente abre a capa para trás; ré pressiona levemente em direção ao corpo, com limite para reduzir atravessamentos; esquerda/direita produzem inclinações opostas. Frenagem conserva resposta amortecida. Os cinco painéis têm respostas independentes, e a gola usa a mesma tecnologia de molas, com perfil próprio mais firme e amplitude pequena. Não se trata de uma reprodução do algoritmo interno da Ronova.

Verificação desta revisão: 122 testes Java e três testes Python aprovados; build Forge e reobfuscação aprovados. As prévias OpenGL usam poses exportadas da simulação Java e incluem frente, ré, laterais, giro, frenagem e queda. Gerador do Shadow repetido com saída idêntica. A máscara emissiva mantém emissão seletiva. Essas imagens não são capturas do Minecraft.

A conferência no jogo continua necessária, principalmente giro da cabeça, agachamento, nado, clipping e dois jogadores. Não há colisão física de tecido. O comando de terreno permanece disponível e não foi alterado nesta revisão. Nenhum commit, push ou PR realizado.
