---
tags: [dev]
---
# Armadilhas conhecidas

1. Coordenadas absolutas × relativas: o render subtrai a origem do efeito.
2. Rotação Z no GeckoLib é horária para ângulos positivos.
3. Não leia `RegistroSons` em teste (inicializa o Forge); use `CatalogoSons`.
4. Vorbis estoura picos: normalize e confira o pico decodificado.
5. `build_armor_models.build()` sem `only` regera outros trajes.
6. Lambdas Java exigem variáveis efetivamente finais; `org.joml.*` deixa `Math` ambíguo.
7. Ao mudar a duração de um efeito, procure prévias/testes com marcos fixos (já quebrou o CI).
8. Tecido liso: cantos marcados pela pose parada (`rotular`); mude Java e Python juntos.
9. PowerShell `Set-Content` grava BOM e quebra o `javac`.
10. Heredoc com aspas no bash falha: escreva o script num arquivo.
11. Imagens grandes podem ser bloqueadas: reduza para JPG ~1000 px.
12. `AudioAnime.programar` cancela o grupo de mesmo nome; `vida` corta o som.
