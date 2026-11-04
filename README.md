# Minezin — Arcane Control

Mod de RPG de anime para **Minecraft 1.20.1** (Forge 47.2.0, Java 17). Cada jogador escolhe um personagem, evolui a
maestria do kit e desperta uma forma transformada com ultimate cinematográfica. Os efeitos são volumes 3D de verdade,
os trajes têm física de tecido e cada habilidade tem som próprio sincronizado com o efeito.

- **modid:** `arcanecontrol` · **repositório:** `Cauan-Silva/Minezin`
- **Versão atual:** `3.13.0-tecido-preview.1` · **protocolo de rede:** 16 (cliente e servidor precisam da mesma versão)
- **Testes:** 252 testes Java + 3 testes Python dos projetos Blockbench, todos passando
- **Estado:** tudo abaixo passou nos testes automáticos; o que ainda não foi conferido dentro do jogo está em
  [Pendências](#pendências)

> Para IAs e novos colaboradores: leia primeiro o [Conhecimento do assistente](docs/conhecimento-assistente.md). Ele
> resume o jeito de trabalhar, o ambiente e as armadilhas conhecidas.

---

## Sumário

1. [Instalação para jogar](#instalação-para-jogar)
2. [Controles](#controles)
3. [Personagens](#personagens)
4. [Sistemas](#sistemas)
5. [Desenvolvimento](#desenvolvimento)
6. [Arquitetura](#arquitetura)
7. [Padrões de qualidade](#padrões-de-qualidade)
8. [Documentação](#documentação)
9. [Pendências](#pendências)
10. [Roadmap](#roadmap)
11. [Créditos e licenças](#créditos-e-licenças)

---

## Instalação para jogar

1. Instale o **Forge 47.2.0** para Minecraft 1.20.1.
2. Coloque na pasta `mods/`:

| Mod | Versão | Onde |
| --- | --- | --- |
| Minezin (`minezin-<versão>.jar`) | a mesma em todos | cliente e servidor |
| [GeckoLib](https://github.com/bernie-g/geckolib) Forge | 4.4.9 | cliente e servidor |
| [Player Animator](https://github.com/KosmX/minecraftPlayerAnimator) Forge | 1.0.2-rc1+1.20 | cliente |
| Photon (inclui LDLib 1.0.42) | 1.1.17 | cliente |

3. Tire da pasta `mods/` qualquer versão anterior do Minezin, para não ficar com duas.

Os JARs de teste ficam no branch `builds` do repositório
(`https://github.com/Cauan-Silva/Minezin/tree/builds`) ou são gerados localmente (veja [Compilar](#compilar)).

---

## Controles

Todas as teclas podem ser trocadas em *Opções → Controles*.

| Tecla | Função |
| --- | --- |
| **P** | Escolher o personagem (kit) ativo |
| **Z / X / C / V** | As quatro técnicas do kit |
| **G** | Despertar: forma transformada, que troca Z/X/C/V pelo moveset desperto |
| **R** | Ultimate de domínio (Gojo) |
| **H** | Árvore de habilidades RPG |
| **K** | Árvore de técnicas (progressão legada do Gojo) |

### Comandos de teste

Precisam de cheats ou operador nível 2. Detalhes em [docs/comandos-testes-rpg.md](docs/comandos-testes-rpg.md).

| Comando | Efeito |
| --- | --- |
| `/minezin rpg maestria maxima [gojo\|cid\|frieren\|asta\|ichigo\|todos]` | Maestria 100, árvore completa e despertar liberado |
| `/minezin rpg restaurar_recursos` | Mana 100 e recargas zeradas |
| `/minezin atomic terreno <true\|false>` | Liga ou desliga a destruição de blocos da Atomic |

---

## Personagens

Cada kit tem quatro técnicas na forma base, um despertar (G) com moveset próprio, traje com tecido, arma e sons
próprios. Os tempos de efeito e de som saem das mesmas constantes que o servidor usa para o dano.

| Kit | Z | X | C | V | G (despertar) |
| --- | --- | --- | --- | --- | --- |
| **Gojo** (Jujutsu Kaisen) | Azul | Vermelho | Teleporte curto / Infinito | Vazio Roxo | Vazio Infinito (domínio) e Roxo Máximo |
| **Cid / Shadow** (Eminence in Shadow) | Corte de Slime | Passo das Sombras | Fios de Slime | Lâmina oculta | I Am Atomic |
| **Frieren** (Frieren) | Zoltraak | Barreira Arcana | Impulso Arcano | Gaiola de Vento | Liberar Mana (buraco negro, Julgamento, escudo celestial) |
| **Asta** (Black Clover) | Espada Matadora de Demônios | Corte Divisor | Avanço Touro Negro | Barreira Antimagia | Assimilação Demoníaca (Black Meteorite, Black Hurricane, Black Divider, ultimate em X) |
| **Ichigo** (Bleach) | Getsuga Tenshou | Shunpo | Golpe pesado | Corte ascendente | Bankai: Tensa Zangetsu (Getsuga negro, Shunpo triplo, máscara Hollow, Mugetsu) |

Destaques:

- **I Am Atomic (Cid):** carga de 9 s com o Cid atordoando quem está perto, pilar de luz do tamanho do domo
  (24 blocos de raio), 20 de dano no impacto e mais 2 a cada 0,5 s enquanto o pilar existe (76 no total), sem
  empurrão. Pode destruir terreno (configurável).
- **Mugetsu (Ichigo):** forma temporária; o corte é uma linha finíssima de 72 blocos de altura que avança 50 blocos.
  Depois o Bankai acaba e o Ichigo fica 1 minuto sem poderes.
- **Máscara Hollow (Ichigo):** buff de dano; com ela, o Z vira Getsuga Jūjishō.

Bosses (Nemesis): Toji, Aurora, Qual, Liebe e Zangetsu, com telegraphs e golpes próprios.

O balanceamento de danos e recargas está em [docs/balanceamento-3.11.md](docs/balanceamento-3.11.md). Os próximos
personagens pedidos (começando pelo Subaru) estão em [docs/design/proximos-personagens.md](docs/design/proximos-personagens.md).

---

## Sistemas

### RPG, maestria e despertar
Cada kit tem mana, recargas e maestria salvas por jogador (sobrevivem à morte, à reconexão e à troca de dimensão).
A maestria dá +0,75% de dano e -0,3% de recarga por nível. O despertar (G) libera a forma transformada e o moveset
desperto. Tudo é validado no servidor; o cliente só desenha. Detalhes: [docs/rpg-integracao-2.3.md](docs/rpg-integracao-2.3.md),
[docs/testing/despertar-3.0.md](docs/testing/despertar-3.0.md).

### Efeitos visuais (VFX)
Volumes 3D (malhas com luz e borda), não sprites planos. A geometria de cada efeito é uma classe pura em
`core/vfx/` presa aos ticks do servidor; o desenho usa o shader `anime_energy.fsh` com vários modos de material
e Photon para o bloom de acento. Ultimates têm câmera cinematográfica, que pode ser desligada na árvore.

### Áudio
Todo som é **original e procedural** (síntese em numpy, sem amostras de terceiros), feito no motor em camadas
`tools/sfx_motor.py`: ataque curto, corpo, grave saturado e eco, com compressão só nos picos e **3 variações** de
cada som (o jogo sorteia uma por uso). Cada habilidade é dividida em momentos tocados no tick exato do efeito
(`RoteiroAudio`). Nenhuma habilidade usa som do Minecraft.

| Gerador | Sons |
| --- | --- |
| `tools/build_gojo_sfx.py` | Gojo |
| `tools/build_asta_sfx.py` | Asta |
| `tools/build_cid_sfx.py` | Cid e a Atomic |
| `tools/build_ichigo_sfx.py` | Ichigo |
| `tools/build_frieren_sfx.py` | Frieren e o boss Qual |
| `tools/build_anime_sfx.py`, `build_roxo_maximo_sfx.py` | Black Divider e sons roxos do Gojo (motor antigo) |

### Tecido (cloth)
Os trajes balançam com física de verdade e são desenhados como superfícies lisas, sem frestas:

- **Física:** costas em malha de partículas (PBD, `MalhaTecido`), laterais e frente ligadas às costas, mangas, alças e
  faixas em molas amortecidas (`MolaMantoCid`), a 20 Hz com interpolação. Onda, agitação, vento, subida na queda e
  impulso das habilidades.
- **Desenho:** os painéis posados viram uma malha única costurada, mais fina e suavizada (`SuperficieTecido`):
  casaco aberto (Cid, Ichigo), anel fechado (saia e capinha da Frieren, barra do Gojo) ou folha aberta (manto do
  Asta). Gola curva no Cid.
- **Colisão:** o pano é empurrado para fora das pernas e dos blocos do mundo (chão, paredes, cercas).
- **Por traje:** `assets/arcanecontrol/tecido/<estilo>.json` define peso, rigidez, arrasto, inércia e mola.

Arquitetura completa: [docs/tecido-arquitetura.md](docs/tecido-arquitetura.md). Regras para trajes novos:
[docs/padrao-trajes.md](docs/padrao-trajes.md).

### Configuração
- Cliente (`config/arcanecontrol-client.toml`): cinematics, tecido por personagem, emissão, SFX, VFX reduzidos,
  shader e Photon da Atomic.
- Servidor (`serverconfig/arcanecontrol-server.toml`): destruição de terreno e raio da cratera da Atomic.

---

## Desenvolvimento

### Requisitos
- **JDK 17** (`JAVA_HOME` apontando para ele). O wrapper baixa o Gradle 8.3; a primeira compilação baixa Forge e
  Minecraft (precisa de internet).
- **Python 3** com `numpy`, `pillow`, `moderngl`, `av` (PyAV) e `soundfile`, para os geradores e as prévias em `tools/`.

### Compilar

```powershell
.\gradlew.bat build --no-daemon
```

```sh
./gradlew build --no-daemon
```

O JAR sai em `build/libs/minezin-<versão>.jar`. `runClient` e `runServer` abrem o ambiente de desenvolvimento
(o servidor pede aceitar o EULA em `run/eula.txt`).

**Windows:** se o Gradle falhar com *Unable to establish loopback connection* (pasta de usuário com espaço ou
acento), use uma pasta temporária ASCII:

```powershell
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:\gtmp -Djava.io.tmpdir=C:\gtmp'; $env:TMP='C:\gtmp'; $env:TEMP='C:\gtmp'
.\gradlew.bat clean build --no-daemon
```

### Testes

```powershell
.\gradlew.bat test
python tools/summarize_tests.py
python -m unittest discover -s tools -p "test_blockbench_projects.py"
```

Os testes conferem marcos de tick, limites, sincronia de som, tecido sem esticar nem torcer, costuras fechadas e
colisão. Vários exportam `build/*-samples.json`, que as prévias usam.

### Prévias offline
Antes de abrir o jogo, cada técnica, traje e som é aprovado por prévia gerada com os dados reais dos testes:

| Prévia | Comando (em `tools/`) |
| --- | --- |
| Trajes do Ichigo e do Cid | `python -B preview_ichigo_traje.py --estilo shadow --output <pasta>` (`ichigo_bankai`, `ichigo_mugetsu`) |
| Tecido da Frieren | `python -B preview_frieren_cloth.py --output <mp4> --poster <png>` |
| Tecido do Gojo | `python -B preview_gojo_cloth.py --output <mp4> --poster <png>` |
| Manto do Asta | `python -B preview_asta_capa.py --output <pasta>` |
| Sons | `python -B build_<personagem>_sfx.py --preview <wav>` |

As prévias **não** mostram terreno, skin real, primeira pessoa, shader packs nem atenuação do som no espaço.

### Integração contínua
O GitHub Actions roda em PRs e na `main`: valida os projetos Blockbench, compila, roda os testes, valida o GLSL e
renderiza prévias OpenGL dos materiais e trajes. O JAR fica como artefato do workflow.

---

## Arquitetura

Código em `src/main/java/com/cauansilva/arcanecontrol/`.

| Área | Onde | Observação |
| --- | --- | --- |
| Poderes por kit | `core/<kit>/Poderes*.java`, `core/magia/GerenciadorMagiasGojo.java` | Servidor: dano, recargas, tempos |
| Perfis e tempos | `core/vfx/Perfil*`, `RelogioAtomic`, `UltAsta`, `MugetsuIchigo`… | Constantes compartilhadas por servidor, VFX e som |
| Geometria de VFX | `core/vfx/*` | Classes puras (sem Minecraft), testáveis |
| Render de efeitos | `core/<kit>/Efeitos*.java`, `client/render/*` | Recebem pacotes e desenham |
| Rede | `core/rede/*` | `PacoteEfeitoDespertar`, `PacoteSincronizarCutscene3D`; `PROTOCOL_VERSION` em `RegistoRede` |
| RPG | `core/rpg/*`, `core/progressao/*` | Mana, maestria, despertar, bosses (Nemesis) |
| Áudio | `core/audio/RoteiroAudio`, `CatalogoSons`, `RegistroSons`, `client/AudioAnime` | Cues em ticks; nomes no catálogo |
| Tecido | `core/client/tecido/*` | `MalhaTecido`, `Tecido<Kit>`, `SuperficieTecido`, `AneisLisos`, `ColisaoCenario`, `PerfilTecido` |
| Trajes (render) | `core/<kit>/RenderTraje*`, `core/ichigo/RenderBankaiIchigo` | Aplicam a pose do tecido e desenham a superfície lisa |
| Assets | `src/main/resources/assets/arcanecontrol/` | `geo/`, `animations/`, `textures/`, `sounds/`, `shaders/`, `tecido/`, `player_animation/` |
| Ferramentas | `tools/` | Geradores de modelos, texturas, sons e prévias |
| Blockbench | `art/blockbench/*.bbmodel` | Projetos editáveis exportados pelos geradores |

Fluxo de um traje: `tools/build_<kit>_traje.py` gera o `.geo.json` e a textura → a física roda a 20 Hz no
cliente → o renderizador soma a pose em cada osso e desenha a superfície lisa → a prévia em Python usa a mesma conta
(`tools/superficie_tecido.py` é o gêmeo de `SuperficieTecido.java`).

---

## Padrões de qualidade

- **Fiel ao anime:** cores, formas e ritmo da cena de referência. Se a técnica não existe no anime, dizer isso e
  usar a canônica mais próxima.
- **VFX em volumes 3D**, nunca planos 2D.
- **Uma técnica por vez:** implementar, gerar prévia (vídeo + painel de fases), aprovar, registrar.
- **Trajes:** silhueta justa, três camadas de detalhe, tecido ligado e liso, colisão com pernas e cenário
  ([docs/padrao-trajes.md](docs/padrao-trajes.md)). O Ichigo e o Cid são a referência.
- **Sons:** próprios de cada personagem, em camadas, com 3 variações e presos aos ticks do efeito.
- **Sincronia:** som, efeito e dano saem das mesmas constantes de tick.

---

## Documentação

| Documento | Conteúdo |
| --- | --- |
| [docs/conhecimento-assistente.md](docs/conhecimento-assistente.md) | Instruções para IAs: como trabalhar, ambiente, armadilhas |
| [docs/guia-continuidade-ia.md](docs/guia-continuidade-ia.md) | Modelo de roadmap de uma onda e dicas detalhadas |
| [docs/workflow-roadmap-minezin.md](docs/workflow-roadmap-minezin.md) | Decisões e workflow completos |
| [docs/padrao-trajes.md](docs/padrao-trajes.md) | Padrão e checklist de trajes |
| [docs/tecido-arquitetura.md](docs/tecido-arquitetura.md) | Arquitetura do tecido |
| [docs/balanceamento-3.11.md](docs/balanceamento-3.11.md) | Danos, recargas e maestria |
| [docs/design/proximos-personagens.md](docs/design/proximos-personagens.md) | Kits pedidos para o futuro |
| [docs/design/gdd-v2-implementacao.md](docs/design/gdd-v2-implementacao.md) | GDD e implementação |
| [docs/comandos-testes-rpg.md](docs/comandos-testes-rpg.md) | Comandos de teste |
| `docs/testing/*-onda-aprovacao-visual.md` | Registro de cada onda (Gojo, Frieren, Asta, Ichigo) |
| [docs/gojo-vfx.md](docs/gojo-vfx.md), [docs/asta-ataques-forma-ia.md](docs/asta-ataques-forma-ia.md) | Detalhes de kits |
| [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) | Créditos de terceiros |

O histórico das versões antigas (1.1 a 3.10) está nos documentos acima e no histórico do Git.

---

## Pendências

Nada destas entregas recentes foi testado dentro do Minecraft ainda:

| Área | O que conferir no jogo |
| --- | --- |
| Tecido liso (5 kits) | Costuras sem frestas correndo, parando e girando; colisão com as pernas |
| Colisão com o cenário | Encostar em parede, agachar perto de cerca, andar em laje; nada estranho no inventário |
| Sons (todos os kits) | Volume, sincronia com o efeito, variações |
| Balanceamento 3.11 | Danos e recargas, Atomic de 9 s com dano em ticks e sem empurrão, campo antimagia do Asta, Bankai ~45 s |
| Ichigo | Empunhadura da Zangetsu, forma Mugetsu de 10 s, tecido das espadas |

---

## Roadmap

1. Testar o `3.13.0-tecido-preview.1` no jogo e corrigir o que aparecer.
2. Personagens novos, uma técnica por vez, começando pelo **Subaru** ([próximos personagens](docs/design/proximos-personagens.md)).
3. Desempenho com muitos jogadores de capa; colisão do cenário dentro da física; asas e cauda do Devil Union lisas.

---

## Créditos e licenças

Modelos, texturas, efeitos e sons são originais deste projeto (gerados pelos scripts em `tools/`), inspirados nos
animes citados. O HobbyMine anterior serviu de referência para animação e câmera. Bibliotecas: Forge, GeckoLib,
Player Animator, Photon e LDLib, cada uma com sua licença. Veja [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
