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
