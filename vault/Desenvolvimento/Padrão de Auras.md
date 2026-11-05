---
tags: [dev, vfx, tecido]
---
# Padrão de auras

Aprovado em 2026-11-05 com o Godspeed do [[Killua]]. **Este é o mínimo para qualquer aura de personagem.**

- **Aura de tecido (cloth):** línguas de energia em `MalhaAura` (PBD), com a base presa ao corpo nas coordenadas do mundo. Elas sobem por flutuação, ondulam com turbulência e pulsam como chama.
- **Grande e volumosa:** nunca colada ao corpo. Uma força de abertura empurra as línguas para fora, e um envelope elíptico deixa o topo arredondado.
- **Nunca um ovo:** o envelope tem lobos deformados que mudam com o tempo, e cada língua tem uma escala própria.
- **Em movimento:** o envelope estica para trás e as línguas arrastam. A flutuação aumenta (`impulso`), então a aura cobre a cabeça, e o brilho interno some com a velocidade.
- **Transparente, em camadas:** 3 camadas de alpha nas línguas, pulsos subindo, brasas nas pontas e um anel no chão.
- **Raios em volta da aura, nunca saindo dela:** os raios correm pela superfície deformada, com quinas e galhos. Também há raios nas bordas das línguas e pontes entre línguas vizinhas.
- **Sem cabelo:** a transformação não adiciona nem modifica o cabelo.
- O perfil (`PerfilAura`: flutuação, arrasto, turbulência, rigidez, pulso, frequência) e as cores mudam por personagem.

Código de referência: `core/client/tecido/MalhaAura.java`, `core/vfx/AuraTecidoKillua.java`, `tools/preview_killua_aura_tecido.py`. Sistema: [[Tecido]] · [[Efeitos Visuais]].
