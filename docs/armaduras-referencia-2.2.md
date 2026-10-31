# Armaduras de referência e cajado — 2.2.0

Modelos escritos no projeto seguindo a imagem enviada pelo usuário: uniforme índigo com cinco botões dourados para Gojo; túnica branca curta, faixas douradas, roupa interna escura, cinto e botas marrons para Frieren; capuz aberto, placas em chevrons, dourado e capa violeta para Shadow. Mantém a orientação anterior de não adicionar cabelo ou rosto: essas partes são da skin. Os adornos da cabeça de Frieren são brincos. A capa e as abas possuem movimento discreto.

Os IDs e estatísticas das armaduras existentes foram preservados: peças já adquiridas recebem a nova aparência. Cada peça tem modelo próprio no inventário. O gerador `tools/build_armor_models.py` reproduz os modelos e animações dos três conjuntos e do cajado.

## Cajado da Frieren

Item: `arcanecontrol:cajado_frieren`. Disponível na aba Combate do Criativo e pelo comando `/give @s arcanecontrol:cajado_frieren`.

Na bancada de trabalho, usando G = barra de ouro, A = fragmento de ametista, S = graveto:

```text
· G A
· S G
S · ·
```

Custo: 2 barras de ouro, 1 fragmento de ametista e 2 gravetos. A receita aparece no livro ao obter ametista.

Selecione Frieren em **P**. O clique direito com o cajado usa **Zoltraak**, passando pela mesma validação de kit/nível e pela mesma recarga da tecla **Z**, no servidor. Com o cajado equipado, o feixe nasce de um foco deslocado à frente da mão; a colisão e o dano continuam usando o raycast do servidor. As teclas X/C/V continuam disponíveis e o cajado não impõe nova obrigação para usar o kit. Modelo 3D com haste rubra, aro dourado, gema vermelha e fitas animadas; transformações próprias para inventário, mãos, chão e moldura.

## Validação

As prévias OpenGL mostram os modelos e materiais reais em frente, costas e perspectiva, incluindo o cajado ao lado do manequim de Frieren. O manequim é neutro, sem cabelo/rosto; não é uma captura do Minecraft nem uma skin entregue com o mod.

Testar em jogo: peças isoladas e conjunto completo; caminhada/agachamento e capa; ícones de todas as peças; cajado nas duas mãos, primeira/terceira pessoa e chão; clique direito e Z compartilhando recarga; tentativa com kit bloqueado ou outro kit; disparo junto a paredes; receita no survival. Não foi executado cliente Minecraft neste ambiente.

Versão 2.2.0 / protocolo 8: atualizar cliente e servidor juntos, pois há um novo item registrado.

## Atlas original

Arquivo final: `src/main/resources/assets/arcanecontrol/textures/armor/anime_reference_atlas.png` (1254 × 1254), gerado com a ferramenta integrada `image_gen` e preservado sem edição raster. A referência fornecida orientou as formas; o atlas contém apenas cores limpas. Prompt exato:

```text
undefined
```
