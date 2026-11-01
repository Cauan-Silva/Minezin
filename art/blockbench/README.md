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

Em `saves/<mundo>/serverconfig/arcanecontrol-server.toml`, a seção `[bosses]` aceita `tojiEscala`, `auroraEscala`, `qualEscala`, `liebeEscala` entre 0.5 e 3.0, exceto Qual (mínimo 2.5). Padrões: 1.0, 1.0, 2.5, 0.85. Qual tem corpo/colisão de 5 blocos de altura; chifres elevam a silhueta a aproximadamente 6 blocos. Valores antigos menores são corrigidos ao invocar/carregar. Aplicação ao invocar/carregar. A escala é sincronizada pelo servidor e afeta renderização, sombra, altura dos olhos e colisão; asas e capas não ganham colisões individuais. O altar verifica o volume já escalado. Não requer outro mod.

Regenere os bosses com `python tools/build_nemesis_models.py` antes de exportar os projetos. As prévias mostram as malhas em escala de autoria; use F3+B no Minecraft para avaliar a colisão com a escala configurada.

Revisão no Blockbench desktop: Shadow abriu com atlas, grupos e reprodução do idle; Qual abriu com atlas, 75 cubos e quatro animações. Isso não substitui a validação da luta no Minecraft.

### Skins enviadas pelo usuário
Aurora e Liebe usam layout Minecraft 64×64 e segunda camada com inflação. Aurora foi copiada de `Aurora-Witch-of-Calamity-Eminence-in-Shadow-on-planetminecraft-com.png`. Liebe foi transcodificado de `liebe-version-blue-17238469.webp` para PNG, preservando todos os pixels RGBA decodificados. Não são skins criadas neste rework. Qual usa pixel art original em SVG, no mesmo layout 64×64; o PNG é exportado desse SVG, sem gradientes. Toji usa agora a skin de Gabbahh, disponível em https://www.minecraftskins.com/skin/23948308/toji-fushiguro---jujutsu-kaisen/ — arquivo original preservado e autoria creditada. A fonte editável é gerada por `tools/build_boss_skin_svg.py` e deve ser exportada em 64×64 sem interpolação.

Os impact frames dos bosses podem ser desligados em `impactFrames` na configuração cliente; `vfxReduzidos` também os desliga. Duram dois ticks, exigem proximidade, direção do olhar e linha de visão do impacto.

### Correção 3.3.1
Qual: corpo de 5 blocos no mínimo (escala 2.5) com colisão correspondente; o valor antigo 1.4 é elevado ao mínimo no carregamento e na invocação. A régua da prévia usa a mesma escala e o corpo base de 32 unidades = 2 blocos. O altar solicita seis blocos livres de altura, incluindo a folga de invocação.

Toji: skin por **Gabbahh**, https://www.minecraftskins.com/skin/23948308/toji-fushiguro---jujutsu-kaisen/ ; PNG original https://www.minecraftskins.com/uploads/skins/2026/03/25/toji-fushiguro---jujutsu-kaisen-23948308.png . Modelo Classic (braços de 4 pixels), sem alterações nos pixels. Os acessórios usam amostras dessa textura. A antiga skin SVG simplificada do Toji foi removida para não sobrescrever o arquivo externo na regeneração.

## Trajes 3.4.0

Gojo, Frieren, Shadow e Asta usam agora texturas independentes de 128×128 em `textures/armor/trajes/`, com ilhas UV dimensionadas em aproximadamente um pixel por unidade Blockbench. As margens de um pixel evitam amostrar ilhas vizinhas. Cada PNG tem fonte SVG editável, sem blur, ruído aleatório ou gradientes. Os geradores escrevem os SVGs; exporte-os para PNG no tamanho original 128×128, sem redimensionar, antes de empacotar o mod.

Fontes: `tools/armor_reference_shapes.py`, refinamentos em `tools/polish_clothing_shapes.py` e UV/pixel art em `tools/clothing_uv.py`. Execute `build_armor_models.py` e `build_asta_assets.py`, exporte os quatro SVGs para PNG e depois execute `export_blockbench_projects.py`. Alterações manuais no Blockbench devem ser incorporadas às fontes antes da próxima regeneração.

Gojo conserva a gola alta e botão único, com costuras na textura. A venda e a faixa do Asta têm folga para a camada externa da skin. Frieren tem barra mais longa e brincos afastados da cabeça. Shadow tem cinco painéis largos, com barra irregular e forro roxo. Ombreiras do Asta são filhas dos braços, com aba própria na bolsa.

Os ícones dos quatro slots são exportados dos mesmos modelos e UVs. Rosto/cabelo continuam pertencendo à skin do jogador. Cajado, armas e forma demoníaca mantêm seus materiais existentes. A prévia `--poses` mostra poses estáticas sintéticas de repouso, caminhada e braços erguidos; não é captura do Minecraft nem teste da animação em movimento. A prévia usa dois samplers para não aplicar a textura da roupa no cajado ou nos acessórios da forma.

Revisão no Minecraft pendente: skins Classic/Slim com hat layer, combinação de slots, ataques, agachar, primeira/terceira pessoa e recarga de recursos. O controle de tela foi interrompido pelo usuário durante esta etapa; a validação seguiu sem operar aplicativos.

### Correção do ombro e manto do Asta
As mangas cobrem o topo do braço até Y=24.4 e o contorno da camada externa padrão da skin. Antes, terminavam abaixo de Y=24, expondo toda a tampa do braço ao girar. A correção é na manga presa ao osso do braço, acompanhada de regressão de cobertura para os quatro trajes.

O manto do Asta usa superfícies finas no peito, costas e ombros, barra frontal inclinada e borda dourada mais estreita. Substitui os blocos grossos e inclinados das ombreiras. As peças laterais continuam seguindo os braços. As prévias estáticas foram refeitas sem operar a tela do usuário; validar encaixe em movimento no Minecraft.
