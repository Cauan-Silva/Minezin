# Projetos editáveis — rework dos trajes

Abra os arquivos `.bbmodel` no Blockbench com o plugin **GeckoLib Models & Animations** instalado. Gojo, Frieren, Shadow e Asta usam tipo **Armor**; `asta_black` é uma malha de acessórios da forma, tipo **Entity**, com grupos `body`, `head` e `right_arm`.

Os projetos guardados no Git usam o atlas existente por caminho relativo. Mantenha a estrutura do checkout. O pacote ZIP gerado pelo CI incorpora a textura em cada projeto e pode ser aberto separadamente, inclusive no editor web. Não há cabelo ou rosto embutido nas roupas.

Para editar um traje, preserve os nomes e pivôs `armor*` e mantenha peças filhas dentro da raiz correspondente. Exporte a geometria GeckoLib e as animações para os caminhos já existentes em `src/main/resources/assets/arcanecontrol`. Os modelos de inventário separados por peça também precisam ser atualizados após uma mudança de geometria.

Os acessórios da forma Asta têm uma etapa adicional: a versão de jogo usa `models/effects/asta_forma.json`, com cada cubo associado a `body`, `head` ou `right_arm`. A malha `geo/asta/forma.geo.json` é o espelho para edição/inspeção. Exportar apenas esse `.geo.json` não atualiza automaticamente os acessórios de jogo.

Fonte paramétrica: `tools/armor_reference_shapes.py`. Regeneração:

```text
python tools/build_armor_models.py
python tools/build_asta_assets.py
python tools/export_blockbench_projects.py --package build/blockbench-models.zip
python -m unittest discover -s tools -p "test_blockbench_projects.py"
```

Executar os geradores substitui os arquivos derivados; mudanças manuais precisam ser preservadas também na fonte paramétrica antes de regenerar.

O conversor segue as convenções oficiais do [codec Bedrock do Blockbench](https://github.com/JannisX11/blockbench/blob/master/js/formats/bedrock/bedrock.js) e do [plugin GeckoLib](https://github.com/JannisX11/blockbench-plugins/blob/master/plugins/geckolib/geckolib.js). A validação automatizada compara hierarquia, UVs, coordenadas, pivôs e animações com os arquivos de jogo. A revisão visual em movimento deve ser concluída no Minecraft.

## Bosses e escala (3.3)

Toji, Aurora, Qual e Liebe usam projetos `Entity`, com quatro animações próprias: `idle`, `walk`, `cast`, `release`. Cabelos e rostos pertencem aos NPCs; a regra de preservar a skin continua nos trajes dos jogadores.

Em `saves/<mundo>/serverconfig/arcanecontrol-server.toml`, a seção `[bosses]` aceita `tojiEscala`, `auroraEscala`, `qualEscala`, `liebeEscala` entre 0.5 e 3.0. Padrões: 1.0, 1.0, 1.4, 0.85. Aplicação ao invocar/carregar. A escala é sincronizada pelo servidor e afeta renderização, sombra, altura dos olhos e colisão; asas e capas não ganham colisões individuais. O altar verifica o volume já escalado. Não requer outro mod.

Regenere os bosses com `python tools/build_nemesis_models.py` antes de exportar os projetos. As prévias mostram as malhas em escala de autoria; use F3+B no Minecraft para avaliar a colisão com a escala configurada.

Revisão no Blockbench desktop: Shadow abriu com atlas, grupos e reprodução do idle; Qual abriu com atlas, 75 cubos e quatro animações. Isso não substitui a validação da luta no Minecraft.

### Skins enviadas pelo usuário
Aurora e Liebe usam layout Minecraft 64×64 e segunda camada com inflação. Aurora foi copiada de `Aurora-Witch-of-Calamity-Eminence-in-Shadow-on-planetminecraft-com.png`. Liebe foi transcodificado de `liebe-version-blue-17238469.webp` para PNG, preservando todos os pixels RGBA decodificados. Não são skins criadas neste rework. Toji e Qual usam pixel art original em SVG, no mesmo layout 64×64; os PNGs são exportações raster desses SVGs, sem gradientes. A fonte editável é gerada por `tools/build_boss_skin_svg.py` e deve ser exportada em 64×64 sem interpolação.

Os impact frames dos bosses podem ser desligados em `impactFrames` na configuração cliente; `vfxReduzidos` também os desliga. Duram dois ticks, exigem proximidade, direção do olhar e linha de visão do impacto.
