# Minezin — Arcane Control

Mod para Minecraft 1.20.1, Forge 47.2.0 e Java 17.

## Compilar

Instale um JDK 17 e configure `JAVA_HOME` para essa instalação. Não é necessário instalar Gradle: o wrapper baixa o Gradle 8.3 na primeira execução. A primeira compilação precisa de internet para baixar as dependências do Forge e Minecraft.

Windows (PowerShell):

```powershell
.\gradlew.bat build --no-daemon
```

Linux/macOS:

```sh
./gradlew build --no-daemon
```

O JAR é gerado em `build/libs/`. O GitHub Actions executa o build em PRs e em atualizações de `main`, disponibilizando o JAR como artefato.

## Ambiente de desenvolvimento

Use `runClient` para iniciar o cliente de desenvolvimento e `runServer` para iniciar o servidor dedicado. O servidor requer aceitar o EULA do Minecraft em `run/eula.txt` antes de iniciar. Arquivos de execução e mundos ficam em `run/`, fora do controle de versão.

Compilar não substitui os testes dentro do jogo: teclas, HUD, magias, cutscenes e multiplayer precisam de validação manual.

## Progressão do Gojo (1.1)

Abra a árvore com **K** (tecla remapeável). Todos começam com Azul; cada 100 XP aumenta o nível e concede um ponto. O limite é nível 20. Desbloqueios são escolhidos na árvore e validados pelo servidor.

| Técnica | Nível mínimo | Pontos | Pré-requisitos | Recarga inicial |
| --- | --- | --- | --- | --- |
| Azul (Z) | 1 | 0 | — | 10 s |
| Vermelho (X) | 2 | 1 | Azul | 15 s |
| Infinito (C) | 3 | 1 | Azul | 5 s |
| Vazio Roxo (V) | 5 | 2 | Azul e Vermelho | 45 s |
| Vazio Infinito (R) | 8 | 3 | Infinito e Roxo | 90 s |

Cada ativação aceita concede 5 XP e um uso de maestria à técnica. Cada 20 usos concede um grau de maestria, até 5, reduzindo a recarga em 5% por grau (máximo 25%). Derrotar um inimigo hostil com dano atribuído ao jogador concede 25 XP, inclusive com armas normais. Desativar Infinito é livre e não concede XP. A contagem das recargas usa ticks do servidor (20 ticks por segundo em condições normais).

Progresso e recargas são salvos por jogador, preservados na morte e sincronizados ao entrar, renascer e mudar de dimensão. Esta versão inicia progressão também para jogadores de mundos existentes. Cliente e servidor precisam usar a mesma versão (protocolo de rede 2).

A árvore permite desativar as cinematics locais, mantendo os efeitos visuais. A preferência fica em `config/arcanecontrol-client.toml`. Esta entrega reaproveita os efeitos e cutscenes existentes: animações corporais com rigs e novas sequências cinematográficas ainda são etapas futuras.

### Validação manual pendente

- Entrar com dois jogadores: confirmar progressos independentes e Azul como única técnica inicial.
- Usar Azul, verificar XP e recarga, derrotar inimigos e desbloquear a árvore respeitando pré-requisitos e pontos.
- Reconectar, morrer, reiniciar o servidor e trocar de dimensão: conferir persistência e sincronização.
- Usar uma Ultimate: confirmar que apenas o conjurador recebe a câmera e que ela retorna à preferência anterior ao terminar, morrer ou desconectar.
- Desativar cinematics pela árvore e confirmar que os efeitos continuam sem mudança de câmera.

Os testes automatizados das regras de progressão rodam com `gradlew test` e no build de CI.
