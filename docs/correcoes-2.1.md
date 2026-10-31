# Correções visuais e de impacto — 2.1.0

- Armaduras Gojo/Frieren com cores lisas e geometria reduzida. Sem cabelo, orelhas ou tranças; a skin continua visível. A textura anterior foi substituída por um atlas original gerado com a ferramenta integrada image_gen, preservado sem edição raster.
- Vazio Roxo: oito raios ramificados de maior largura e intensidade, desde a formação, com material próprio e alternativa sem shader de energia.
- Vazio Infinito: domo cósmico renderizado por dentro; halo e disco claros reduzidos também no renderizador alternativo.
- Frieren: selos geométricos sobrepostos, barreira com selo e aro, disco de acreção contínuo e lente orientada à câmera. Ultimate com formação em 3,5 s, colapso em 11 s e fim em 14 s; arranca até 3 blocos por chamada, com até 128 fragmentos simultâneos no servidor, e abre cratera de raio 14/profundidade 9.
- Atomic: feixe desce entre 2,5 e 4,5 s, permanece até 8,5 s, com encerramento em 12 s e cratera progressiva. Depois do impacto, a câmera fica ancorada na explosão e a queda na própria cratera não cancela a técnica; o conjurador recebe queda lenta. Novos mundos usam raio 20 (diâmetro 40); mundos existentes preservam seu raio configurado.

A destruição requer, em `<mundo>/serverconfig/arcanecontrol-server.toml`, `buracoNegroDestruirTerreno=true` e `[atomic] destruirTerreno=true`. O valor `[atomic] raioCratera` controla o raio real da cratera, até 28. Blocos com inventário, fluidos, blocos indestrutíveis e os protegidos por cancelamento de `BlockEvent.BreakEvent` são preservados. As explosões nativas continuam sujeitas aos eventos do Forge. Os blocos consumidos não geram drops. O trabalho é limitado globalmente a 384 posições por tick, até 192 por cratera.

Cliente e servidor devem usar 2.1.0 (protocolo 7). Testar em mundo separado, com as mesmas configurações do servidor de destino: solo comum, região protegida, destruição desligada e ultimates simultâneas. O terreno deve mudar no servidor; partículas sozinhas não validam essa mecânica. Conferir feixe/raios em primeira e terceira pessoa e o domínio por dentro e por fora.

As prévias OpenGL usam materiais e modelos reais, mas não substituem a avaliação em movimento dentro do Minecraft. Não foi executado um cliente Minecraft neste ambiente.

## Textura

Arquivo final: `src/main/resources/assets/arcanecontrol/textures/armor/anime_atlas.png` (1254 × 1254). Ferramenta integrada image_gen. Prompt exato:

```text
Generate a clean flat-color game material atlas for Minecraft-adapted anime clothing. Exactly 4 by 4 equal square cells, filling the square image edge to edge, no gaps, no labels, no outlines. Each cell is almost uniform flat color with only a very broad subtle cel-shaded gradient (at most 3 shades). This must look calm and smooth at small scale. Absolutely no pixel noise, woven fabric, grain, fibers, hair strands, scratches, small patterns, jewels texture or detailed embroidery. Row1: near-black charcoal cloth; dark navy cloth; warm ivory cloth; dark plum cloth. Row2: flat pale silver; muted warm gold; cool light steel gray; warm peach. Row3: flat cyan; flat violet; flat ruby red; warm ivory with a single broad restrained gold stripe. Row4: near-black leather; pale gray leather; dark navy with a single broad muted blue stripe; ivory with a single broad gold stripe. Orthographic 2D diffuse material sheet, opaque, crisp simple anime cel shading, no objects, no garments, no character, no render, no photographic textures. User explicitly wants to replace excessively noisy detailed clothing textures with smooth restrained colors.
```
