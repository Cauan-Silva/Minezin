---
tags: [dev, processo]
---
# Padrões de qualidade

- **Fiel ao anime:** cores, formas e ritmo da cena de referência.
- **VFX em volumes 3D**, nunca planos 2D (uma versão 2D foi reprovada).
- **Uma técnica por vez** com prévia e aprovação.
- **Trajes:** ver [[Padrão de Trajes]].
- **Auras:** aura de tecido com raios em volta é o mínimo; ver [[Padrão de Auras]].
- **Sons:** próprios, em camadas, 3 variações, presos aos ticks.
- **Sincronia:** som, efeito e dano saem das mesmas constantes.
- **Animação fluida:** chaves encadeadas, sem poses paradas; prévias a 40 fps. Cada golpe tem antecipação, overshoot (`easeOutBack`), follow-through, volta à guarda, sobe e desce do corpo (`position`) e inclinação; 10 ou mais chaves por osso (reprovado como "duro" com 5 ou 6).
