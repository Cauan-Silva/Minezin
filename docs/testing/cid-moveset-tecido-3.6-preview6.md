# Cid — tecido e moveset — 3.6.0-preview.6

Base integrada: `09f2727fc796d02171335b86862b7de33d1fca43` (main, PR #24). Branch local: `feat/cid-moveset-tecido-vfx`. Revisão visual exclusiva do Cid, sem bibliotecas adicionais e sem alteração de dano, alcance, cooldown, destruição, progressão ou protocolo de rede.

## Implementação

- `RoteiroTecidoCid`: envelopes visuais em ticks para corte (11), dash (12), fios (13), Atomic (14) e transformação (17, identificador interno cliente). Impulsos somados ao deslocamento local antes da simulação amortecida; valores agregados limitados. Não move o jogador.
- `MantoDirecionalCid`: mantém os cinco perfis da capa e o perfil firme da gola; acrescenta dois perfis independentes para as abas frontais. `DetalhesTrajeCid` aplica flexão e abertura assimétrica limitada a 0,1 radiano nas abas; gola mantém limite de 0,045. Dimensões/modelos/texturas da preview.5 preservados.
- `EfeitosCid`: consulta os eventos visuais existentes, amostra os envelopes no tick e limita a soma. Evento interno 17 usa o mesmo ciclo de expiração/mundo/jogador; cancelamento limpa Atomic e transformação. Interpolação e restauração de ossos continuam no renderer existente.
- `RenderDespertar`: somente Shadow, início de forma com tipo 200 e duração completa de 800 ticks, inicia o efeito 17. Pacotes de observação com duração restante não reiniciam a sequência (exceto se a observação coincidir exatamente com o tick inicial). Nenhum pacote novo.
- `RenderMovesetCid`: corte com silhueta escura, bordas estreitas, ecos atrasados e fragmentos radiais; dash com filamentos helicoidais e anéis nas extremidades; fios com selo de origem e anel por alvo; transformação com seis filamentos ascendentes, selo e onda de expansão. As geometrias reutilizam os materiais procedurais existentes e respeitam profundidade. Configuração reduzida diminui arcos/filamentos/fragmentos adicionais. A transformação dura 25 ticks; não substitui a aura sustentada existente.

A Atomic conserva os impact frames, áudio e pós-processamento aprovados; nesta leva recebe reação do tecido na concentração e detonação, sem reescrever sua composição. As animações corporais existentes são preservadas: o acréscimo é a resposta secundária da roupa e os VFX. Não há novos keyframes corporais nesta entrega. Apex Arcana é referência de composição por camadas; não há cópia de recursos ou alegação de reproduzir seu algoritmo.

## Verificações

Build Forge/reobfuscação e 124 testes Java aprovados, incluindo envelopes finitos, limites, fases e dissipação. Três testes Python dos projetos Blockbench aprovados. Prévias OpenGL do tecido usam poses exportadas da implementação Java para corte, dash, fios, transformação e Atomic. Materiais procedurais existentes compilados/renderizados offline. Diff sem erros de whitespace.

As prévias do tecido mostram geometria e máscara reais, sem poses corporais de combate. A prévia de materiais mostra shaders isolados e não comprova a composição completa dos novos efeitos Java. A ordem de integração de eventos e os efeitos completos ainda exigem Minecraft real. Nenhum cliente/servidor foi aberto durante o uso do PC.

## Roteiro no Minecraft

1. Substituir o JAR anterior pela preview.6, sem manter versões duplicadas. Selecionar Cid em P, equipar o conjunto e a espada.
2. Com OP/cheats: `/minezin rpg maestria maxima cid`, `/minezin rpg restaurar_recursos` e `/minezin atomic terreno false` para preservar o terreno. Recursos não cancelam ações em execução.
3. Executar corte, dash e fios parado e em movimento. Confirmar timing do corte no tick 6, direção do rastro, destino do dash, marcação dos alvos e dissipação sem efeitos presos.
4. Ativar a transformação com G: conferir sequência ascendente e abertura do tecido, mantendo o traje pequeno da preview.5. Na Atomic, conferir reação na detonação, sem mudar os impact frames aprovados.
5. Conferir abas/gola em primeira e terceira pessoa, agachamento, nado, giro de cabeça e movimentação lateral. Não há colisão física de tecido; clipping continua sendo um critério de revisão.
6. Dois jogadores próximos: poses/eventos independentes. Cancelar, morrer, trocar dimensão, remover o traje, desconectar e recarregar recursos; nenhuma pose ou efeito deve ficar preso.
7. Comparar VFX normal/reduzido, emissão ligada/desligada e com/sem shader pack. Conferir oclusão por paredes e custo em combate com vários jogadores.

Pendentes: validação visual completa no Minecraft, multiplayer, servidor dedicado e medição de FPS. A validação offline não estabelece qualidade equivalente a outro mod. Sem commit, push ou PR nesta entrega.
