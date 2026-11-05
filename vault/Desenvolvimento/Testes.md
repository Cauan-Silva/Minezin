---
tags: [dev]
---
# Testes

```powershell
.\gradlew.bat test
python tools/summarize_tests.py
python -m unittest discover -s tools -p "test_blockbench_projects.py"
```

- 252+ testes Java: marcos de tick, limites, sincronia de som, tecido sem esticar nem torcer, costuras, colisão.
- Vários exportam `build/*-samples.json` usados pelas [[Prévias Offline]] (declarados em `build.gradle`).
- O CI (GitHub Actions) compila, testa, valida GLSL e renderiza prévias de materiais e trajes.
