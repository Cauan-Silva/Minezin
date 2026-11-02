# Minezin — workflow, decisões e roadmap

Atualizado em **02/11/2026**. Documento de continuidade do projeto, baseado nas decisões desta conversa e nos arquivos locais verificados. Não estabelece datas de entrega nem autoriza mudanças futuras, commits, push, merge ou publicação.

## 1. Objetivo do projeto

Construir um RPG de anime para Minecraft com poderes reconhecíveis, progressão survival, equipamentos próprios, bosses e apresentação audiovisual marcante. A prioridade atual é elevar os personagens existentes ao padrão alcançado pelo Cid, trabalhando **um personagem por onda**.

Qualidade tem prioridade sobre velocidade de entrega. Cada habilidade deve comunicar preparação, liberação, impacto e recuperação. Roupa, animação corporal, câmera, VFX e SFX precisam formar uma sequência coerente com a mecânica real.

“Minezin 2.0” identifica o plano de evolução e organização técnica. Não é a versão atual do arquivo JAR.

## 2. Estado verificável desta entrega

> **Atualização de 02/11/2026 (onda Frieren).** A onda Gojo foi publicada e integrada: PR #26 mergeado na `main` (`1ec0aab`), CI de build aprovado. A onda Frieren (técnicas, forma desperta, traje com tecido, cajado, Qual e SFX) foi aprovada em prévias, entregue como `minezin-3.8.0-frieren-preview.2.jar` e publicada em PR para `main`. Teste completo no Minecraft ainda pendente; a preview.1 teve o Zoltraak corrigido na preview.2. Detalhes, hashes e roteiro: [frieren-onda-aprovacao-visual.md](testing/frieren-onda-aprovacao-visual.md). A tabela abaixo descreve a entrega anterior (Gojo).

| Item | Estado em 02/11/2026 |
| --- | --- |
| Repositório | `Cauan-Silva/Minezin` |
| Modid | `arcanecontrol` |
| Base do checkout | `fc561496e7419c761efd64c7d32515e4dd88b984`, merge do PR #25 |
| Branch local | `feat/gojo-onda-qualidade` |
| Versão de teste | `3.7.0-gojo-preview.1` |
| Onda Gojo | Publicação autorizada; preparação do commit e PR em 02/11/2026 |
| Aprovação visual | VFX, transformação, cutscenes, SFX e tecido Gojo aprovados em prévias |
| Artefato | `minezin-3.7.0-gojo-preview.1.jar` gerado e entregue para teste |
| Verificação local | Build/reobfuscação aprovados; 167 testes Java, zero falhas/erros/ignorados; três testes Python aprovados |
| Teste Minecraft desta preview | Pendente de retorno do usuário; prévias offline não comprovam integração no jogo |
| Rede | Protocolo 15; a onda Gojo preserva o formato existente |

O HEAD acima é a **base** do trabalho, não um commit contendo a onda Gojo. O JAR incorpora alterações ainda não commitadas. Não atribuir a ele um SHA de código final inexistente.

Artefato entregue na pasta `outputs/` do workspace, fora do repositório:

```text
minezin-3.7.0-gojo-preview.1.jar
Tamanho: 4.124.883 bytes
SHA-256: df08b88c9f1a16d7ffe7256b2ab4c5e630ea99556199d8ad71c10294d5998b21
```

O empacotamento foi conferido por integridade ZIP, versão e presença de classes/recursos novos. Não houve instalação automática no Minecraft do usuário.

## 3. Decisões de produto que continuam valendo

- Adotar integralmente os movesets e teclas do GDD v2, conforme decisão explícita do usuário. Pedidos posteriores específicos prevalecem sobre o desenho anterior.
- Permitir troca entre kits desbloqueados, mantendo progressão separada por personagem. P seleciona poderes, H abre a árvore, Z/X/C/V usam técnicas e G ativa ou encerra o despertar conforme o kit.
- O catálogo não representa implementação jogável. Um personagem só deve ser anunciado como disponível quando sua execução, equipamentos e ciclo de progressão estiverem integrados.
- Cid é o medidor de qualidade. Outros personagens devem receber identidade própria com nível equivalente de acabamento, sem simplesmente recolorir seus efeitos.
- Trabalhar em ondas, um personagem por vez. Mostrar imagens e prévias em movimento para aprovação durante o desenvolvimento.
- Manter roupas reconhecíveis no padrão Minecraft: volumes claros, texturas legíveis, sem excesso de ruído por pixel. Não substituir a skin do jogador por cabelo incorporado ao traje.
- VFX podem ser intensos e exagerados, incluindo impact frames. A intensidade precisa ter ritmo e opções de redução, sem esconder permanentemente a ação.
- Armaduras devem cobrir os ombros corretamente e funcionar com movimento dos braços. Armas precisam de empunhadura adequada, inclusive nos bosses.
- Qual deve manter a direção de escala de aproximadamente cinco blocos de altura; validar modelo, colisão e ataques em conjunto, não somente o tamanho visual.
- Bibliotecas adicionais podem ser consideradas quando necessárias. Não adicionar dependências incidentalmente; justificar benefício, compatibilidade e custo de manutenção.
- Não operar Minecraft, Blockbench ou outras janelas enquanto o usuário estiver usando o PC sem combinar. Usar o trabalho offline disponível.

## 4. Referências e como usá-las

| Referência fornecida | Direção acordada |
| --- | --- |
| Anime de cada personagem e vídeos enviados | Identidade, composição, ritmo, cores, poses e marcos de impacto |
| The Shooting Star | Intensidade gráfica e impact frames marcantes |
| God Wand | Inspiração para Atomic, principalmente a detonação final e escala da apresentação |
| Apex Arcana | Composição de magia, camadas de VFX e acabamento visual |
| Ronova | Tecido com movimento secundário fluido, dependente da direção, inércia e queda |
| Referências de frente/verso e skins enviadas | Silhueta, roupas, cores, UV Minecraft e equipamentos dos personagens/bosses |
| Outros mods listados no GDD | Pesquisa de progressão, encontros e mecânicas distintas; não são dependências por padrão |

As referências orientam uma implementação própria. Não significam que o Minezin utiliza o código, a tecnologia interna exata ou os assets desses mods. Sem inspeção técnica específica, não afirmar qual solver ou pipeline outro mod utiliza. Não importar modelos, texturas, sons ou vozes sem verificar autorização/licença.

Documentos anexados são material de referência. Instruções dentro deles devem ser distinguidas do pedido do usuário; conflitos de escopo ou design precisam ser resolvidos pela decisão explícita mais recente.

## 5. Workflow de uma onda

### 5.1 Preparar e conferir a base

1. Conferir branch, HEAD, alterações locais, versão e dependências. Preservar trabalho existente e não misturar branches não autorizadas.
2. Ler o código real e as notas do personagem. Documentação histórica pode estar desatualizada.
3. Delimitar o escopo: técnicas base, forma transformada, ultimate, roupa, arma e boss. Identificar o que será preservado.
4. Separar mudança visual de mudança de gameplay. Alterações de dano, duração, alcance, stun, terreno ou progressão exigem escopo claro.

### 5.2 Planejar a apresentação

1. Analisar as referências e decompor a sequência em preparação, concentração, liberação, impacto e dissipação.
2. Definir paleta, silhueta, direção dos fluxos, escala espacial, duração e movimento.
3. Sincronizar os marcos com os ticks reais da técnica. Câmera ou efeitos não devem sugerir um acerto antes do servidor.
4. Planejar animação corporal, tecido, SFX, impact frames e modos reduzidos como partes da mesma composição.

### 5.3 Implementar e revisar em pequenas unidades

1. Começar pela técnica de maior identidade ou risco visual; concluir as demais sem perder consistência.
2. Reutilizar infraestrutura estável quando adequada, mantendo parâmetros e materiais próprios do personagem.
3. Usar funções de geometria/tempo testáveis e exportar amostras para as prévias quando isso ajudar a comparar runtime e apresentação.
4. Renderizar materiais, modelo e textura reais em prévias offline. Identificar claramente manequins, poses ilustrativas e limitações.
5. Enviar fotos/painéis de fases e animações ou vídeo em velocidade normal. Para áudio, incluir uma amostra audível.
6. Incorporar o feedback e repetir a revisão. Registrar o que foi aprovado; não reabrir partes aprovadas sem necessidade.

### 5.4 Validar e entregar o JAR

1. Executar build, testes pertinentes, verificações de assets e shaders afetados. Não criar testes que apenas repitam a implementação sem verificar comportamento útil.
2. Conferir limpeza de estados e preservação dos personagens fora do escopo.
3. Gerar o JAR de distribuição reobfuscado, conferir metadados, integridade e recursos; registrar nome e hash.
4. Entregar o arquivo e um roteiro de teste no Minecraft. A versão instalada deve ser confirmada antes de atribuir um erro ao código novo.
5. Receber feedback de jogo, corrigir e gerar uma preview identificável quando o conteúdo mudar.

### 5.5 Publicar somente com autorização

1. Revisar o diff final e excluir arquivos locais, logs e artefatos sem finalidade de versionamento.
2. Atualizar documentação com estado real, verificações, limitações e decisões.
3. Após autorização, criar commit, fazer push e abrir/atualizar o PR para `main`, conforme o pedido.
4. Conferir as verificações do PR; merge é uma etapa distinta e deve seguir a autorização recebida.
5. Registrar a versão e o commit efetivamente publicados. Só então tratar a onda como integrada.

Após a entrega do JAR e deste documento, o usuário autorizou seguir com o PR. Como não havia PR aberto, a publicação do commit/branch e a criação do PR estão sendo preparadas. Não registrar aprovação técnica do GitHub nem merge antes de ocorrerem.

## 6. O que cada aprovação comprova

| Marco | Evidência necessária | O que ainda pode faltar |
| --- | --- | --- |
| Implementado | Código/assets existentes e escopo descrito | Build, integração e aprovação visual |
| Verificado offline | Testes, build e prévias identificados | Execução real do Minecraft |
| Aprovado visualmente | Feedback explícito do usuário sobre a prévia | Encaixe com jogador, terreno, rede e shader packs |
| Validado em jogo | Versão e cenário de teste registrados, resultado observado | Cenários ainda não exercitados |
| Publicado | Commit/PR/versão rastreáveis | Merge ou distribuição, quando separados |
| Integrado | Merge confirmado e verificações registradas | Bugs de uso posteriores continuam possíveis |

Não substituir teste de cliente por renderização OpenGL, teste de rede por função pura ou resultado local por GitHub Actions. Registrar cada evidência com seu limite.

## 7. Padrão de qualidade por área

| Área | Critérios para fechar uma onda |
| --- | --- |
| VFX | Identidade do anime, movimento legível, múltiplas camadas coerentes, dissipação e leitura espacial; efeito acompanha o ponto/momento real |
| Ultimate | Preparação com tensão, clímax marcante, expansão/ondas adequadas e recuperação; sequência completa, sem desaparecer prematuramente |
| Impact frames | Quadros de contraste/exposição bem posicionados; intensidade aprovada e opções de flash respeitadas |
| SFX | Carga, transiente e cauda sincronizados; sem repetição perceptível indesejada; cancelamento correto e sem áudio órfão |
| Câmera | Enquadramento significativo, colisão e restauração de posição/FOV; tratamento de morte, cancelamento e troca de mundo |
| Roupa | Silhueta Minecraft, UV íntegra, ombros fechados, pele preservada, reação direcional, queda e retorno amortecido |
| Arma/boss | Cabo na mão, escala coerente, animação/arma alinhadas, hitbox e ataque compatíveis; não herdar transformações de outro jogador |
| Multiplayer | Autoridade do servidor, observadores sincronizados e isolamento por jogador; sem estados ou bônus duplicados |
| Desempenho | Modo reduzido funcional, limites de geometria/estado e avaliação de múltiplos efeitos simultâneos no jogo |
| Persistência | Reconectar, morrer, trocar dimensão e reabrir mundo sem perder progresso ou renovar recargas indevidamente |

O tecido atual usa partículas com restrições e molas secundárias em ossos, não uma malha de roupa com colisão exata em toda a superfície do corpo. A aceitação é visual e funcional, além da estabilidade numérica.

## 8. Inventário e posição das ondas

Existem **cinco kits implementados** na base de trabalho; o catálogo maior é planejamento. O mapa técnico antigo inventariava quatro porque foi escrito antes da entrega do Ichigo.

| Personagem | Base existente | Posição no rework |
| --- | --- | --- |
| Cid / Shadow | Moveset, Atomic, equipamento, Aurora, despertar, tecido direcional e controle de terreno | Referência de qualidade aprovada pelo usuário; integrada na base até PR #25. Preservar durante as próximas ondas |
| Gojo | Técnicas base, Domínio, Toji, progressão e roupa | Primeira onda: aprovada visualmente e integrada pelo PR #26; teste de jogo da 3.7.0 pendente |
| Frieren | Kit base/transformado, cajado, traje e Qual | Segunda onda: aprovada em prévias com referências do anime; JAR 3.8.0-frieren-preview.2 entregue; publicada em PR; teste de jogo pendente |
| Asta | Antimagia, espadas, traje, Assimilação Demoníaca e Liebe | Próxima candidata: revisar animações, Black Divider, forma e tecido; testar empunhadura no jogo |
| Ichigo | Zangetsu/Tensa Zangetsu, Bankai, máscara, roupa e Zangetsu Interior | Implementado antes da onda Gojo; ainda não recebeu esta onda de acabamento equivalente ao Cid |

### O que compõe a onda Gojo entregue

- Azul: núcleo escuro, borda ciano e fluxos convergentes.
- Vermelho: energia concentrada, núcleo claro, descarga e ondas de repulsão.
- Vazio Roxo: núcleo/coroa e raios ramificados em direções independentes.
- Vazio Infinito: domo exterior escuro, interior cósmico sem esfera negra central, névoa móvel e linhas coloridas intensas.
- Transformação: entrada e aura que acompanham o jogador.
- Chuva de Informações: fluxos coloridos descendentes.
- Morte Cerebral: informação caindo de cima para baixo até a cabeça, no acerto confirmado.
- Roxo Máximo: cutscene própria, maior impacto, pós-processamento e SFX revisado sem a repetição rejeitada.
- Teleporte: ruptura de saída/chegada e rastro residual.
- Traje: barra segmentada e gola com física secundária sutil, preservando mangas e ombros.

Detalhes técnicos e limites: [gojo-onda1-aprovacao-visual.md](testing/gojo-onda1-aprovacao-visual.md). O “Fechou” do usuário aprovou a prévia do tecido; os dois documentos registram essa decisão.

## 9. Roadmap a partir de agora

As fases abaixo são uma ordem proposta de execução, não um cronograma nem autorização para iniciar todas elas. A próxima personagem após Gojo ainda depende da escolha do usuário.

| Fase | Entrega | Dependência | Critério de saída |
| --- | --- | --- | --- |
| R0 — fechar Gojo no jogo | Testar o JAR entregue e corrigir problemas confirmados | Retorno sobre a preview 3.7.0 | Roteiro de cliente concluído, regressões resolvidas e resultado registrado |
| R1 — publicar Gojo | Diff final, documentação, commit/PR e verificações | Aprovação de publicação | PR rastreável, CI conferido e merge conforme autorização |
| R2 — próxima onda | Frieren **ou** Asta, escolhido pelo usuário | Gojo estabilizado; definição da personagem | Moveset, transformação, ultimate, SFX e roupa no padrão Cid, com aprovação e teste no jogo |
| R3 — completar existentes | Personagem restante entre Frieren/Asta; depois Ichigo como proposta | Uma onda concluída de cada vez | Cinco kits com apresentação consistente; ordem confirmada a cada nova onda |
| R4 — bosses e integração | Revisar bosses em cada onda e fechar validação conjunta | Modelos/armas e poderes correspondentes | Escala, animações, telegraphs, armas, hitboxes e limpeza de estado coerentes |
| R5 — base técnica e regressão | Atualizar mapa/baseline, validar rede e saves; refatorações pequenas quando justificadas | Evidências da versão estabilizada | Comparação antes/depois na mesma base, sem regressões de gameplay ou persistência |
| R6 — expansão do GDD | Novos kits e sistemas próprios, com escopo aprovado | Padrão visual e fundação estáveis | Cada kit com formas, progressão, equipamento, boss/desafio e validação completos |

### Direções das próximas ondas

**Frieren:** construir uma linguagem de magia precisa e legível; melhorar concentração, disparos e transições da forma; tornar o Buraco Negro um clímax reconhecível. Confirmar atração, destruição e limites de terreno no código antes de prometer comportamento novo. Preservar o mapeamento adotado: Buraco Negro no C transformado, apesar do pedido anterior de tratá-lo como ultimate. Revisar cajado, mangas/saia e Qual, incluindo a escala de cinco blocos.

**Asta:** dar peso e direção aos golpes, antimagia preta/vermelha e crescimento legível do Black Divider; revisar entrada da Assimilação, asa/forma, roupa e Liebe. Confirmar empunhadura, mão esquerda/direita e primeira/terceira pessoa dentro do jogo. Não confundir prévia geométrica com correção comprovada do renderer de mão.

**Ichigo:** revisar Getsuga e variações, velocidade/afterimages, Bankai e máscara, com peso próprio de espada e pressão espiritual. Preservar as regras existentes de manutenção por mana; não converter a forma em duração fixa por causa da apresentação visual.

### Expansão de longo prazo do GDD

Personagens restantes no planejamento: **Tanjiro, Naruto, Jin-Woo, Eren, Sukuna, Rimuru, Killua, Genos, Jotaro e Edward**. Não estão anunciados como kits jogáveis por este documento. A ordem precisa ser acordada; Tanjiro pertence à leva histórica Asta/Ichigo/Tanjiro, mas não substitui automaticamente o foco atual em qualidade dos kits existentes.

As entregas futuras trazem riscos próprios: invocações/IA e descarte; movimento e câmera de titãs/DMT; domínios simultâneos; calor/recursos; tempo parado e dano adiado; transmutação e conservação de blocos/itens. Avaliar essas mecânicas individualmente antes da implementação.

## 10. Próximo roteiro de teste — Gojo 3.7.0

Usar um mundo de testes ou cópia; substituir o JAR anterior do Minezin, evitando duplicatas. Confirmar a versão carregada e manter as dependências necessárias. No multiplayer, usar a versão compatível nos dois lados.

| Cenário | Aceitação |
| --- | --- |
| Inicialização | Cliente carrega o mod, dependências e recursos sem falha |
| Roupa | Gola/barra respondem em corrida, passo lateral, giro, salto, queda e pouso; ombros fechados, sem deformação persistente |
| Azul/Vermelho/Roxo | Efeitos legíveis, direção correta, raios multidirecionais; colisão e acerto coerentes |
| Transformação/Domínio | Aura acompanha jogador; domo exterior e interior corretos; névoa move e linhas coloridas permanecem visíveis |
| Informação | Chuva e acerto passivo descem para a cabeça; repetição não reinicia indevidamente som/animação |
| Roxo Máximo | Cutscene, liberação e SFX sincronizados; câmera/FOV e pós-processamento restaurados ao terminar/cancelar |
| Teleporte | Saída/chegada corretas inclusive na vertical; sem roupa ou câmera carregando inércia de grande deslocamento |
| Opções | Cinemáticas, tecido, efeitos reduzidos e flashes podem ser desativados sem quebrar a técnica |
| Ciclo de vida | Morte, logout, dimensão, troca de kit e equipamento não deixam aura, forma, som ou roupa presos |
| Multiplayer/dedicado | Inicialização sem classes client-only indevidas; observadores coerentes e sem contaminação entre jogadores |
| Compatibilidade | Conferir com e sem os shader packs usados, primeira/terceira pessoa e múltiplas técnicas; registrar FPS/frametimes e hardware |
| Mundo antigo | Progresso, itens e recargas persistem após salvar/reabrir; migração não concede benefícios indevidos |

Para relatar falhas: informar nome do JAR, habilidade/tecla, forma ativa, perspectiva, shaders/mods relevantes, passos de reprodução e resultado esperado/observado. Screenshot/vídeo e `latest.log` ajudam quando disponíveis.

### Comandos já existentes para facilitar os testes

Exigem cheats/operador com permissão 2. Não fazem parte do fluxo survival normal.

```mcfunction
/minezin rpg maestria maxima gojo
/minezin rpg maestria maxima todos
/minezin rpg restaurar_recursos
/minezin atomic terreno false
```

Selecionar o kit em P; maximizar não seleciona automaticamente nem fornece equipamentos. Restaurar recursos repõe mana e recargas RPG, sem encerrar ações em execução. O comando de terreno é **exclusivo da Atomic**, global e persistente; não protege automaticamente contra outras habilidades nem restaura blocos já destruídos. Ver [comandos-testes-rpg.md](comandos-testes-rpg.md).

## 11. Ferramentas e base técnica preservada

| Componente | Versão/função confirmada |
| --- | --- |
| Minecraft / Forge | 1.20.1 / 47.2.0 |
| Java / Gradle / ForgeGradle | Java 17 / wrapper 8.3 / 6.0.54 |
| GeckoLib | 4.4.9 no build; modelos, itens, armaduras e entidades |
| Player Animator | 1.0.2-rc1+1.20; animação do jogador |
| Photon / LDLib | 1.1.17 / 1.0.42; infraestrutura visual existente |
| JUnit Jupiter | 5.10.0; testes Java |
| Blockbench | Projetos editáveis em `art/blockbench/`; geometria e UV verificadas contra os assets |
| Prévias offline | Python, ModernGL, NumPy, Pillow e PyAV; shaders/modelos/amostras reais quando indicado |
| Áudio | SFX procedurais próprios, empacotados em Vorbis; prévias não reproduzem exatamente a atenuação espacial do jogo |

Não adicionar um mod de escala de bosses antes de avaliar os recursos já presentes. Tamanho visual, dimensão da entidade, colisão, alcance, navegação e animação são aspectos distintos.

Comandos gerais na raiz do repositório, com JDK 17 configurado:

```powershell
./gradlew.bat test build --no-daemon --console=plain
python -B tools/summarize_tests.py
python -B -m unittest discover -s tools -p test_blockbench_projects.py
git diff --check
```

Para execução de desenvolvimento, existem `./gradlew.bat runClient` e `./gradlew.bat runServer`; são testes de integração separados, não foram executados para validar a preview entregue. Instalação/distribuição com mods reais também deve ser testada. O ambiente local utiliza cache Gradle e ajustes de JDK específicos; esses caminhos não devem virar requisito universal do projeto.

## 12. Riscos e prioridades atuais

Esta tabela usa prioridades operacionais próprias; não substitui os identificadores A01–A05 da auditoria histórica.

| Prioridade | Situação | Próxima ação |
| --- | --- | --- |
| P0 se reproduzido | Crash de cliente/servidor, corrupção de mundo ou estado permanente | Interromper publicação e corrigir antes de novos efeitos; não há confirmação nova desses defeitos nesta entrega |
| P1 | Preview Gojo ainda sem validação real de cliente/dedicado/multiplayer | Executar o roteiro e registrar resultados |
| P1 | Interação de câmera/pós-processamento com shader packs e cancelamentos | Validar restauração, ordem de composição e opções desligadas |
| P1 | Alteração de terreno e persistência de mundos antigos | Testar em cópia, confirmar limites por habilidade e revisar pendências históricas |
| P2 | Encaixe de roupa/arma com animação real, skin slim e primeira pessoa | Exercitar movimentos e perspectivas; corrigir problemas observados |
| P2 | Desempenho de efeitos intensos e sobrepostos | Medir em jogo, comparar modo reduzido e ajustar orçamento |
| P2 | Documentação histórica com estados/versões diferentes | Manter referências datadas; atualizar o mapa ao fechar uma base estável |

Nenhum FPS mínimo foi acordado. Não inventar um critério numérico de desempenho; medir primeiro no ambiente usado pelo usuário e combinar uma meta.

## 13. Documentos e rastreabilidade

- [GDD v2 — implementação e catálogo](design/gdd-v2-implementacao.md): fonte histórica do plano, com seções explicitamente antigas. Não usar seu cabeçalho 3.1.0 como inventário atual.
- [Gojo — onda de aprovação](testing/gojo-onda1-aprovacao-visual.md): materiais, tempos, prévias, testes e limitações da onda atual.
- [Cid — contagem e stun](testing/cid-contagem-stun-preview10.md): regra de contagem após Atomic e stun durante a carga, incorporados à referência.
- [Cid — tecido e terreno](testing/cid-traje-terreno-3.6.md) e [queda/explosão](testing/cid-queda-explosao-preview8.md): evolução de roupa/VFX.
- [Ichigo e ultimates transformadas](testing/ichigo-3.6.md): kit, Bankai por mana, equipamento e roteiro.
- [Asta](testing/asta-3.1.md): integração histórica do kit e desafio.
- [Comandos de teste](comandos-testes-rpg.md): maximização, recursos e controle da Atomic.

Os documentos `mapa-sistemas-dependencias.md` e `baseline-build-validacao.md` estão no checkout separado `work/Minezin-auditoria-docs/docs/`. Referem-se a **8732ac9aa51adf6cec2b072affd86f34d4a64bad**, versão **3.4.0**, protocolo **12**. Registram quatro kits e 81 testes Java no CI histórico; não representam o inventário nem os resultados da preview Gojo. Não foram copiados ou modificados nesta entrega.

## 14. Como retomar o trabalho

1. Ler este documento e a nota técnica da onda ativa.
2. Conferir branch/HEAD/status e a versão que o usuário realmente está usando.
3. Recuperar o último feedback e a última autorização explícita. Hoje: visual Gojo aprovado, JAR entregue, teste em jogo pendente e publicação em PR autorizada.
4. Corrigir primeiro os problemas reproduzidos; preservar o Cid e as partes já aprovadas.
5. Atualizar a evidência e a documentação antes de concluir ou publicar.

Ao fechar cada onda, registrar personagem, escopo final, aprovações, JAR/hash, testes executados e pendentes, commit/PR/merge quando existirem, além da próxima decisão necessária. O roadmap deve acompanhar o estado real, não apenas a intenção.
