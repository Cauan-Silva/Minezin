---
tags: [dev]
---
# Ambiente e build

- JDK 17; Gradle 8.3 pelo wrapper; Python 3 com `numpy`, `pillow`, `moderngl`, `av`, `soundfile`.
- Pasta: `C:\Users\MAIS INTERNET\Documents\My Games\Minezin-main`; saídas em `..\minezin-outputs`.
- O caminho tem espaço → Gradle falha com *loopback*. Rode fora do sandbox:

```powershell
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:\gtmp -Djava.io.tmpdir=C:\gtmp'; $env:TMP='C:\gtmp'; $env:TEMP='C:\gtmp'
.\gradlew.bat clean build --no-daemon
```

- JAR em `build/libs/minezin-<versão>.jar`. Ao gerar JAR, subir versão em `build.gradle` **e** `mods.toml`.
- Dependências do jogo: Forge 47.2.0, GeckoLib 4.4.9, Player Animator 1.0.2-rc1+1.20, Photon 1.1.17 (LDLib 1.0.42).
- `gh` sem login: PR via link `https://github.com/Cauan-Silva/Minezin/compare/main...<branch>`.
