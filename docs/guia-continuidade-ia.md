# Minezin — modelo de roadmap e guia para IAs

Atualizado em **02/11/2026**. Documento de passagem de bastão para qualquer IA que continue o projeto. A parte 1 é um **modelo de roadmap** para copiar a cada onda; a parte 2 traz o **estado atual**; a parte 3 reúne **dicas práticas** aprendidas nas ondas Gojo e Frieren. Não autoriza commit, push, merge ou publicação: isso continua dependendo de pedido explícito do usuário.

Leia junto: [workflow-roadmap-minezin.md](workflow-roadmap-minezin.md) (decisões e workflow completos), o documento da onda em andamento, [testing/asta-onda-aprovacao-visual.md](testing/asta-onda-aprovacao-visual.md), e a ficha dos ataques da forma desperta do Asta, [asta-ataques-forma-ia.md](asta-ataques-forma-ia.md).

---

## Parte 1 — Modelo de roadmap de uma onda

Copie para `docs/testing/<personagem>-onda-aprovacao-visual.md` e preencha. Registre o estado real, nunca a intenção.

```markdown
# <Personagem> — onda de qualidade

Iniciada em <data>, sobre a `main` em `<sha>` (<último merge>). Mecânicas do servidor preservadas: <sim/não; se não, listar>.

**Publicação:** <branch>, PR #<n>, CI <estado>, merge `<sha>` em <data>. Versão: `<x.y.z-personagem-preview.n>`. Teste no Minecraft: <pendente/feito>.

## Referências
| Técnica | Referência do anime (episódio/cena/fonte) | Canônica? | Observações |
| --- | --- | --- | --- |
| <tecla/técnica> | <ep. N, cena X / vídeo do usuário / wiki> | <sim / nome do GDD sem equivalente> | <cores, forma, ritmo> |

## Inventário antes da onda
| Tecla | Forma base | Forma desperta (G) | Efeito (id) | Estado visual atual |
| --- | --- | --- | --- | --- |

## Ondas internas (uma técnica de cada vez)
| Ordem | Parte | Status | Prévia | Aprovação |
| --- | --- | --- | --- | --- |
| 1 | <técnica de maior identidade> | ⏳/✅ | <arquivos em minezin-outputs> | <data/resposta do usuário> |
| … | Traje + tecido | | | |
| … | Arma/cajado | | | |
| … | Boss | | | |
| … | SFX | | | |
| … | JAR + roteiro | | | |

## <Técnica> — aprovada em prévia
- Fases e ticks: <tabela tick → apresentação, presa aos ticks do servidor>
- Implementação: <classes de geometria pura, render, shader, cues de áudio>
- Testes: <o que garantem>; prévia: `python -B tools/<preview>.py --output <pasta>`
- Limites da prévia: <o que ela não mostra>

## JAR de teste
nome, tamanho, SHA-256, o que foi conferido, roteiro no Minecraft (tabela cenário → aceitação).

## Pendências e riscos
| Prioridade | Situação | Próxima ação |
```

### Fases do roadmap geral (modelo)

| Fase | Entrega | Critério de saída |
| --- | --- | --- |
| R0 | Testar no jogo o JAR da onda anterior e corrigir problemas confirmados | Roteiro concluído, regressões resolvidas e registradas |
| R1 | Publicar a onda (diff final, docs, commit, PR, CI) | PR com CI verde; merge só com autorização e depois do CI |
| R2 | Próxima onda (um personagem) | Técnicas, forma, ultimate, traje, arma, boss e SFX aprovados em prévia e testados no jogo |
| R3 | Completar os kits existentes | Os cinco kits no padrão do Cid |
| R4 | Bosses e integração | Escala, animação, telegraphs, armas e hitboxes coerentes |
| R5 | Base técnica e regressão | Rede, saves e desempenho comparados antes/depois |
| R6 | Expansão do GDD | Novos kits com escopo aprovado, um de cada vez |

---

## Parte 2 — Estado atual (02/11/2026)

| Item | Estado |
| --- | --- |
| Repositório | `Cauan-Silva/Minezin`, modid `arcanecontrol`, Minecraft 1.20.1 / Forge 47.2.0, Java 17 |
| Clone local | `C:\Users\Usuário\Documents\Projetos\Minezin` |
| `main` | `a698e32` (merge do PR #29, guia) sobre `fad7cb6` (PR #28) e `7b5f88e` (PR #27, onda Frieren) |
| Versão | `3.9.0-asta-preview.2` (onda Asta, parte 1); JAR em `C:\Users\Usuário\Documents\Projetos\minezin-outputs` |
| Testes | 227 testes Java + 3 testes Python de projetos Blockbench, zero falhas |
| Protocolo de rede | 15 (efeito novo 115 exige cliente e servidor na mesma versão) |

| Personagem | Situação |
| --- | --- |
| Cid / Shadow | Referência de qualidade, integrado |
| Gojo | Onda integrada (PR #26); teste de jogo da 3.7.0 pendente |
| Frieren | Onda integrada (PR #27); teste completo no jogo pendente (a preview.1 tinha o Zoltraak apontando sempre para a mesma direção, corrigido na preview.2) |
| Asta | **Onda em andamento** ([testing/asta-onda-aprovacao-visual.md](testing/asta-onda-aprovacao-visual.md)): Black Divider, golpes base, Devil Union (modelo GeckoLib com asas e cauda em tecido) e Matadora desperta aprovados; faltam Black Meteorite e golpes X/V da forma, roupa com tecido, SFX e Liebe |
| Ichigo | Depois do Asta: Getsuga, Bankai, máscara, Zangetsu |

Melhorias anotadas fora das ondas:
- **SFX do Gojo:** Azul, Vermelho, Infinito, Vazio Roxo e Domínio usam sons vanilla; só o Roxo Máximo tem SFX próprio.
- **SFX do Cid:** golpes base compartilham o sweep vanilla; a Atomic sobrepõe batimento e sonic boom do warden ao SFX próprio.
- **Sons emprestados entre kits:** `divider_release` (Asta) no Cid desperto e no Ichigo; `purple_charge` (Gojo) no Bankai.
- **Cinema da Atomic** sem opção de redução própria.

---

## Parte 3 — Dicas para a próxima IA

### 3.1 Como trabalhar com o usuário

- Responda em **português**, direto, com tabelas curtas. O usuário acompanha por prévias, não por código.
- **Fidelidade ao anime é requisito.** A primeira versão do Buraco Negro foi rejeitada por parecer "ficção científica" (disco de acreção dourado). Antes de desenhar: peça vídeo/cena ou pesquise; se a técnica não existir no anime (nomes do GDD como "Julgamento"), diga isso e use a magia canônica mais próxima.
- **Uma técnica por vez:** implementar → prévia (mp4 + painel de fases) → pedir aprovação → registrar no documento da onda. Não reabra partes aprovadas sem motivo.
- **Commit, push, PR e merge só com pedido explícito.** Branch própria para cada PR (o usuário pediu para trabalhar na `main` localmente; o PR sai de uma branch criada na hora).
- **Merge só depois do CI verde.** O repositório não exige checks, então `gh pr merge` passa com o build rodando. Confira `pending=0` e `passing>0` antes.
- Ao concluir algo grande, ofereça o próximo passo com opções claras (ex.: "Gerar JAR" / "Fazer SFX").

### 3.2 Coletar referências

- **Vídeo do usuário:** extraia quadros com PyAV para ver composição e ritmo (folha de contato a cada 0,5 s e quadros-chave em 640×360). Use `python -E -P` (não `-I`, que esconde os pacotes do usuário).
- **Wiki:** `https://frieren.fandom.com/wiki/<Página>` no navegador embutido; liste imagens com `document.querySelectorAll('img')` e abra `.../revision/latest` para tamanho cheio. GIFs: capture algumas telas com espera de ~1 s.
- Não é possível assistir YouTube/streaming; vídeo enviado pelo usuário é a melhor fonte de timing.
- Não copie assets de outros mods (Ronova, Apex Arcana etc.): são referência de comportamento.

### 3.3 Mapa da arquitetura

| Área | Onde | Padrão |
| --- | --- | --- |
| Geometria/tempos de VFX | `core/vfx/*` (ex.: `BuracoNegroFrieren`, `MagiasBaseFrieren`, `FormaFrieren`) | Classes **puras**, sem Minecraft; fases presas a constantes do servidor (`PerfilFrieren.ABERTURA/COLAPSO/FIM`) |
| Testes de VFX | `src/test/.../core/vfx/*Test` | Verificam marcos de tick e limites e **exportam `build/*-samples.json`** para as prévias (declarar em `build.gradle` → `outputs.file`) |
| Render base da Frieren | `core/frieren/RenderMagiasFrieren` + `EfeitosFrieren` | Pose já transladada para a origem do efeito |
| Render desperto | `client/render/RenderDespertar` roteia por kit → `core/frieren/RenderDespertarFrieren`, `asta/EfeitosAsta`, `ichigo/EfeitosIchigo` | |
| Bosses | `client/render/RenderBossVfx`, `rpg/NemesisEntity`, `rpg/PerfilBoss` | Telegraph `400+kit`, golpe `411–416` |
| Primitivas | `client/render/DesenhoMagia` (`lote`, `fita`, `anel`, `esfera`, `caixa`, `ponto`) | `lote(modo, aditivo, ...)` escolhe o material |
| Shader | `assets/arcanecontrol/shaders/core/anime_energy.fsh` | Ver tabela de modos abaixo; novo material = novo modo |
| Áudio | `audio/RoteiroAudio` (listas de `Cue`), `client/AudioAnime` (agenda), `audio/CatalogoSons` (nomes), `RegistroSons` (Forge) | Cues seguem ticks do servidor |
| Ultimates/cues | `vfx/RoteiroUltimate`, `client/render/UltimatesTransformadas` | Impact frame + som por efeito desperto |
| Tecido | `client/tecido/MalhaTecido` (solver PBD), `TecidoGojo`, `TecidoFrieren`; cliente `gojo/TecidoGojoCliente`, `frieren/TecidoFrierenCliente`; render `RenderTrajeGojo`, `RenderTrajeFrieren` | Ossos encadeados lidos da malha; 20 Hz fixos; reset em teleporte; cache 64; opção de cliente |
| Trajes (geometria) | `tools/armor_reference_shapes.py` → `build_armor_models.build(only={...})` → `clothing_uv.py` (SVG) | `tools/build_frieren_cloth.py` regera só a Frieren e rasteriza o SVG |
| Bosses (geometria) | `tools/boss_reference_shapes.py`, `skin_boss_models.py`, `build_boss_skin_svg.py`, `build_nemesis_models.py` | Corpo no layout de skin 64×64; acessórios em ossos próprios com cor da linha de amostras |
| SFX | `tools/build_anime_sfx.py`, `build_roxo_maximo_sfx.py`, `build_frieren_sfx.py` | Síntese numpy, Vorbis mono 48 kHz via `soundfile`, legendas pt_br/en_us, manifesto em `art/audio/` |
| Blockbench | `art/blockbench/*.bbmodel` via `tools/export_blockbench_projects.make_project(nome)` | Regerar ao mudar geometria; `test_blockbench_projects.py` confere |

**IDs de efeito usados (kit Frieren = 3):** 21 Zoltraak, 22 Barreira, 23 Impulso, 24 Buraco Negro, 25 cancelamento, 26 quebra da barreira, 30 Gaiola de Vento, 37 Vinte Zoltraak (raios via 113), 38 Julgamento (aviso), 115 raio do Julgamento, 39 Escudo Celestial, 200 forma desperta. Qual: 403 carga, 413 disparo. Asta usa 114 e 304–306; não reutilize sem conferir.

**Modos do `anime_energy.fsh`:** 0 borda violeta · 1 fita com núcleo branco · 2 esfera hexagonal · 3 núcleo opaco com borda · 4 faixas douradas · 5 dissolução · 6 traço de selo · 7 borda vermelha · 8 cor chapada · 9 filamentos · 10 ponta afilada · 11 veias rasgadas · 12 núcleo viajante · 13 coluna turbulenta · 14 crista com fluxo · 15 núcleo + halo · 16 brilho da singularidade · 17 traço horizontal (flare) · 18 espinho · 19 coluna de mana · 20 faixa de vento cel-shading (não aditivo) · 21 Zoltraak preto da Qual (não aditivo).

### 3.4 Armadilhas já encontradas

1. **Coordenadas absolutas × relativas.** `PacoteSincronizarCutscene3D` e `PacoteEfeitoDespertar` mandam pontos **do mundo**; o render desenha com a pose na origem. Sempre subtraia a origem (`alvo - origem`). Esse erro deixou o Zoltraak apontando para uma direção fixa e a prévia offline não pegou, porque montava o alvo já relativo. Ao escrever prévia, alimente-a com coordenadas absolutas como o jogo faz.
2. **Rotação Z no GeckoLib** é horária para ângulos positivos: para segmentos tangentes em arcos use `-(ângulo+90)`.
3. **Ler `RegistroSons` em teste** inicializa o `DeferredRegister` e quebra fora do Forge; use `CatalogoSons`.
4. **Cues de áudio de bosses** precisam ter atraso 0: `AudioAnime` descarta cues atrasados cujo dono não é um jogador.
5. **Vorbis estoura picos** em transientes secos: normalize para ~0,72 antes de codificar e verifique o pico decodificado (< 0,99).
6. **`build_nemesis_models.py` reescreve os quatro bosses** (só fim de linha muda nos outros); restaure com `git checkout` os que você não alterou.
7. **`build_armor_models.build()` sem `only`** regera Gojo e Shadow e apaga etapas de tecido posteriores; use `build(only={'<estilo>'})`.
8. **Testes antigos** às vezes travam o desenho anterior (ex.: saias nas pernas, antebraço extra da Qual). Adapte preservando a garantia original e explique na mensagem.
9. **Lambdas Java** exigem variáveis efetivamente finais (erro comum ao reatribuir `dir`).
10. A prévia offline **não** mostra terreno, impact frames, skin, animações reais, primeira pessoa, shader packs nem atenuação espacial do som. Diga isso ao entregar.

### 3.5 Ambiente deste PC (Windows)

- O caminho do usuário tem acento (`Usuário`): o Gradle falha com *Unable to establish loopback connection*. Rode com JDK 17 e pasta temporária ASCII, **fora do sandbox**:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"; $env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=<pasta ASCII> -Djava.io.tmpdir=<pasta ASCII>"
cmd /c ".\gradlew.bat test build --no-daemon --console=plain > <log> 2>&1"
```

- O build cria a pasta `logs/` na raiz: apague antes de commitar.
- `git push`, `gh` e `pip` também precisam de rede (fora do sandbox).
- PowerShell 5 grava UTF-8 **com BOM** (`Out-File`); para mensagem de commit/PR use `[IO.File]::WriteAllText(..., New-Object Text.UTF8Encoding $false)`.
- Um `index.lock` passageiro pode aparecer (consulta do app). Confira se há processo git antes de agir; geralmente some sozinho.
- Python 3.15 com `moderngl`, `numpy`, `pillow`, `av` e `soundfile` instalados (GPU RTX 5060 Ti funciona para prévias OpenGL standalone).

### 3.6 Verificação antes de entregar

```powershell
./gradlew.bat test build --no-daemon --console=plain
python -B tools/summarize_tests.py
python -B -m unittest discover -s tools -p test_blockbench_projects.py
git diff --check
```

JAR: `build/libs/minezin-<versão>.jar`. Conferir integridade do zip, `version=` no `META-INF/mods.toml`, presença das classes/recursos novos, nomes SRG (`m_\d+_`) no bytecode (reobfuscado) e SHA-256. Ajustar a versão em `build.gradle` e `mods.toml` juntos. Entregar com roteiro de teste no jogo e registrar nome/tamanho/hash no documento da onda.

### 3.7 Sugestão de início para a onda Asta

1. Conferir `main`, status e versão; criar o documento da onda pelo modelo da parte 1.
2. Inventário: `core/asta/*` (`PoderesAsta`, `EfeitosAsta`, `RenderEspadaAsta`, `RenderFormaAsta`, `TecidoDevilUnionCliente`), boss Liebe (kit 4), efeitos 114 e 304–310.
3. Pedir ao usuário vídeos das cenas (Black Divider, Assimilação Demoníaca, Liebe) ou pesquisar na wiki de Black Clover; registrar o que é canônico.
4. Começar pela técnica de maior identidade (Black Divider), com geometria pura + teste exportando amostras + prévia.
5. Revisar empunhadura (mão esquerda/direita, primeira/terceira pessoa) e trocar os sons emprestados por SFX próprios do Asta.
