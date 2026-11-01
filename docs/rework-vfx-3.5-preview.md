# Rework VFX — 3.5.0-preview.1

Base: `main` no commit `8732ac9aa51adf6cec2b072affd86f34d4a64bad`. Continuação do checkpoint local `4190cf1`, na branch `feat/vfx-ultimates-animacao`. Esta é uma prévia para revisão, não uma versão aprovada dentro do Minecraft.

## Apresentação

- **Shadow:** concentração da Atomic com selos e filamentos convergentes, descarga ramificada, coroa de energia e ondas sucessivas. O material da casca tem menos preenchimento uniforme e destaca veios. O corte base recebe três camadas; cortes despertos e Lâmina Oculta usam lâminas curvas em vez da apresentação genérica de lasers.
- **Frieren:** singularidade com núcleo opaco, halo de luz curvada, disco de acreção, filamentos e fragmentos visuais em espiral. O Zoltraak ganha coroa no ponto final. A pose segura os marcos de abertura e colapso sem alterar sua simulação.
- **Asta:** antecipação concentrada perto da mão, varredura nas janelas dos golpes e corpo/borda do corte no mesmo plano. O despertar revela braço, chifre e asa em etapas; a asa se consolida no tick 47, alinhada à pose, à câmera e ao impacto visual. Observadores tardios usam a idade total da forma, sem repetir sua revelação.
- **Gojo:** materiais, raios do Roxo e ambiente do domínio preservados. O disparo do Roxo e a repulsão recebem feedback adicional a partir dos pacotes de disparo já aceitos pelo servidor.
- **Gestos:** doze animações adicionais para técnicas base/despertas roteadas por `RenderDespertar`; gestos das ultimates de Shadow/Frieren e despertar do Asta revisados. A limpeza da abertura só cancela a pose correspondente, preservando uma técnica posterior.

## Tempo, câmera e configurações

`PerfilCinematico` mantém envelopes, órbitas, limites e revelação visual. `ImpactosAnime` programa apresentação a partir dos efeitos recebidos: Atomic no tick 90; singularidade nos ticks 70/220; consolidação do Asta no tick 47; feedback de disparo do Gojo e impacto recebido do Asta.

Esses agendamentos não são novos eventos autoritativos de dano: usam o relógio e os pacotes existentes do cliente. Latência e sincronização completa continuam exigindo o teste multiplayer descrito abaixo.

Impact frames duram dois ticks, respeitam `impactFrames`, VFX reduzidos e as opções de clarão aplicáveis. Distância, direção da câmera real e oclusão por blocos limitam o clarão. O tremor adicional é amortecido por tempo/distância e reduzido no modo leve; o executor em cutscene usa o tremor do roteiro, evitando somá-lo novamente. Os observadores recebem feedback local, sem assumir a câmera cinematográfica do executor.

As listas têm limite de 48 registros de impacto e oito composições renderizadas por frame. Raios usam dez caminhos, ou quatro no modo reduzido; fragmentos orbitais usam 18, ou seis. A expansão de coroa e os novos gestos não introduzem entidades de gameplay. Fragmentos visuais não substituem os blocos reais atraídos pela singularidade no servidor.

## Limites de escopo

Nenhuma biblioteca foi adicionada. Foram preservados IDs, protocolo 12, NBT, mana, cooldowns, danos, alcances, acertos, duração das formas e regras de destruição. A Atomic continua com impacto no tick 90 e efeito até 240. O tick 47 do Asta é um marco visual; a duração/abertura autoritativa continua em `PerfilAsta`.

O código de gameplay dos controladores dos quatro kits, do roteador e da destruição não foi alterado. O problema conhecido de remoção direta de blocos pelo Roxo normal, identificado na auditoria como A01/P1, **não foi corrigido por este rework visual**. Validar a prévia em mundo de teste ou cópia. O fallback de falha de material dos renderizadores existentes também não está sendo anunciado como resolvido.

Os documentos do Minezin 2.0 preparados em `Minezin-auditoria-docs` permanecem separados. Os resultados abaixo correspondem à preparação local da prévia. O estado do CI remoto deve ser conferido no PR da branch antes do merge.

## Verificações locais

- Build Forge completo por `gradlew.bat test build --no-daemon --console=plain`, incluindo reobfuscação do JAR.
- Suíte expandida para 91 testes Java. A cobertura nova inclui carregamento das doze animações com Player Animator real, hold/retorno das ultimates, janelas visuais, revelação tardia da forma, órbitas, câmera e limites de qualidade.
- Três testes Python do contrato Blockbench, sem regenerar os projetos.
- Geração de animações idempotente por `tools/cinematic_animations.py`; a autoria do Asta preserva essa revisão quando regenerada.
- Compilação/link dos programas GLSL e renderização das prévias em OpenGL 3.2, NVIDIA RTX 5060 Ti. Não foi executado `glslangValidator` separadamente neste ambiente.
- A escrita de profundidade das prévias usa `Framebuffer.depth_mask`, a API efetiva do ModernGL; não um atributo sem efeito no contexto.

O impedimento local do Gradle foi contornado com `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:/Windows/Temp`, aplicado somente ao processo e seus filhos. Isso evita o erro de conexão do socket temporário; não muda arquivos de configuração do projeto nem opções permanentes do Windows.

Comandos de autoria/validação, a partir da raiz:

```powershell
python -B tools/cinematic_animations.py
.\gradlew.bat test build --no-daemon --console=plain
python -B tools/summarize_tests.py
python -B -m unittest discover -s tools -p test_blockbench_projects.py
python -B tools/preview_power_vfx.py --output build/anime-materials.png
python -B tools/preview_gojo_vfx.py --output build/gojo-materials.png
python -B tools/preview_cinematic_vfx.py --output build/cinematic-vfx.png
```

As prévias requerem as ferramentas Python já utilizadas pelo workflow. O novo script temporal foi adicionado à validação futura do CI; o resultado da execução remota deve ser conferido no PR. A folha mostra composições de referência com os materiais do mod: não reproduz Minecraft, Photon bloom, personagens, terreno ou dano.

## Validação manual restante

1. Instalar o JAR de prévia em uma cópia de instância Forge 1.20.1/47.2.0 com as dependências existentes; não manter o JAR antigo junto.
2. Conferir Atomic, singularidade, cortes do Asta e disparo do Gojo em movimento, inclusive de perto, lateralmente e com blocos entre câmera e impacto.
3. Usar o despertar do Asta, observar a ordem braço/chifre/asa e entrar no alcance de um jogador já transformado: a abertura não deve reiniciar para o observador.
4. Testar VFX reduzidos, clarões e cinemáticas desligados; verificar restauração da câmera em cancelamento, morte, desconexão e dimensão.
5. Usar dois jogadores: conferir que dano não depende do frame, poses e efeitos são visíveis, cancelamentos limpam a apresentação e a câmera do observador permanece sob seu controle.
6. Comparar custo de frame/tick em cenário fixo. As verificações automáticas não demonstram ganho de FPS ou equivalência gráfica a um mod de referência.

Minecraft e Blockbench não foram abertos nesta etapa; cliente, servidor dedicado e multiplayer não foram executados. A validação desses itens é necessária antes de considerar a prévia aprovada para uso normal.
