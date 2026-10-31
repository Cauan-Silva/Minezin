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
