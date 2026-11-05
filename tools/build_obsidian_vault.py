"""Gera o vault do Obsidian do Minezin em vault/: notas-hub com [[links]] + copia de README e docs/.
Rode da raiz ou de tools/: python -B tools/build_obsidian_vault.py (refaz a pasta inteira)."""
from pathlib import Path
import shutil,re
R=Path(__file__).resolve().parents[1];V=R/'vault'
if V.exists():shutil.rmtree(V)
def nota(caminho,tags,texto):
    p=V/(caminho+'.md');p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(f"---\ntags: [{', '.join(tags)}]\n---\n"+texto.strip()+"\n",encoding='utf-8')

nota('00 Início',['moc'],"""
# Minezin — Arcane Control

Mod de RPG de anime para **Minecraft 1.20.1** (Forge 47.2.0, Java 17). Modid `arcanecontrol`, repositório
`Cauan-Silva/Minezin`. Versão `3.14.0-subaru-preview.9`, protocolo de rede 17.

Cada jogador escolhe um personagem (tecla P), usa as quatro técnicas (Z/X/C/V), evolui a maestria e desperta uma
forma transformada (G) com moveset e ultimate próprios. Efeitos em volumes 3D, trajes com física de tecido e sons
originais sincronizados aos ticks.

## Mapas
- **Personagens:** [[Personagens]]
- **Sistemas:** [[Sistemas]]
- **Desenvolvimento:** [[Desenvolvimento]]
- **Planejamento:** [[Roadmap]] · [[Pendências]] · [[Próximos Personagens]]
- **Registro:** [[Diário]]
- **Documentação original do repositório:** [[Documentos do Repositório]]

## Regras de ouro
1. Fiel ao anime ([[Padrões de Qualidade]]).
2. VFX em volumes 3D, nunca planos.
3. Uma técnica por vez: implementar → prévia (mp4 + fases) → aprovação ([[Workflow]]).
4. Commit, push e merge só com pedido explícito.
5. Modelos e texturas são gerados por `tools/`; sons podem usar gravações CC0 com crédito ([[Áudio]]).
""")

nota('Personagens/Personagens',['moc','personagem'],"""
# Personagens

| Kit | Anime | Despertar (G) | Nota |
| --- | --- | --- | --- |
| Gojo | Jujutsu Kaisen | Vazio Infinito + Roxo Máximo | [[Gojo]] |
| Cid / Shadow | Eminence in Shadow | I Am Atomic | [[Cid]] |
| Frieren | Frieren | Liberar Mana | [[Frieren]] |
| Asta | Black Clover | Assimilação Demoníaca | [[Asta]] |
| Ichigo | Bleach | Bankai: Tensa Zangetsu | [[Ichigo]] |
| Subaru | Re:Zero | Contrato com a [[Beatrice]] | [[Subaru]] |

Inimigos: [[Bosses Nemesis]]. Futuro: [[Próximos Personagens]].
Todos seguem [[Padrão de Trajes]], [[Tecido]] e [[Áudio]].
""")

P={'Gojo':("Jujutsu Kaisen","""| Tecla | Técnica |
| --- | --- |
| Z | Azul |
| X | Vermelho |
| C | Teleporte curto / Infinito |
| V | Vazio Roxo |
| G | Vazio Infinito (domínio) e Roxo Máximo |
| R | Ultimate de domínio |

- Som: cristalino e espacial (`tools/build_gojo_sfx.py`).
- Tecido: barra do casaco em anel liso (`TecidoGojo`, `RenderTrajeGojo`).
- Progressão legada na tecla K.
- Documentos: [[gojo-vfx]], [[gojo-onda1-aprovacao-visual]]."""),
'Cid':("Eminence in Shadow (Shadow)","""| Tecla | Técnica |
| --- | --- |
| Z | Corte de Slime |
| X | Passo das Sombras |
| C | Fios de Slime |
| V | Lâmina oculta |
| G | I Am Atomic |

- **I Am Atomic:** carga de 9 s atordoando quem está perto; pilar de luz de 24 blocos de raio; 20 de dano no impacto e
  +2 a cada 0,5 s (76 no total), sem empurrão (`RelogioAtomic`, `PoderesCid`). Pode destruir terreno (configurável).
- **Traje:** casaco no padrão do Ichigo, sem capuz nem máscara; frente ligada à lateral; gola fechada, arredondada,
  emoldurando a cabeça (`tools/build_cid_traje.py`, `RenderTrajeCid`, `MantoCidCliente`). Perfil de tecido `shadow`.
- Som: elegante (katana, slime, zumbido violeta).
- Documentos: [[cid-traje-terreno-3.6]], [[atomic-cinema-3.6]], [[cid-cloth-vfx-preview7]]."""),
'Frieren':("Frieren","""| Tecla | Técnica |
| --- | --- |
| Z | Zoltraak |
| X | Barreira Arcana |
| C | Impulso Arcano |
| V | Gaiola de Vento |
| G | Liberar Mana (buraco negro, Julgamento, escudo celestial) |

- Tecido: capinha e saia em anéis lisos (`TecidoFrieren`, `LINHAS_CAPA=2`, `LINHAS_SAIA=3`).
- Som: sinos cristalinos e feixes limpos; também gera o boss Qual.
- Documentos: [[frieren-onda-aprovacao-visual]]."""),
'Asta':("Black Clover","""| Tecla | Técnica |
| --- | --- |
| Z | Espada Matadora de Demônios |
| X | Corte Divisor |
| C | Avanço Touro Negro |
| V | Barreira Antimagia |
| G | Assimilação Demoníaca (Black Meteorite, Black Hurricane, Black Divider, ultimate em X) |

- Tecido: manto do Touro Negro em folha aberta (`TecidoAsta`, `asta_capa_`).
- Som: sujo e escuro (antimagia).
- Documentos: [[asta-ataques-forma-ia]], [[asta-onda-aprovacao-visual]], [[asta-3.1]]."""),
'Ichigo':("Bleach","""| Tecla | Técnica |
| --- | --- |
| Z | Getsuga Tenshou (Getsuga Jūjishō com a máscara) |
| X | Shunpo |
| C | Golpe pesado |
| V | Corte ascendente |
| G | Bankai: Tensa Zangetsu (Getsuga negro, Shunpo triplo, máscara Hollow, Mugetsu) |

- **Mugetsu:** forma temporária de 10 s; linha finíssima de 72 blocos de altura que avança 50 blocos; a aura some
  depois do golpe; depois disso o Ichigo fica 1 minuto sem poderes (`MugetsuIchigo`).
- **Traje:** é a **referência** do [[Padrão de Trajes]] (`build_ichigo_traje.py`, `RenderBankaiIchigo`).
- Som: reiatsu rugindo, pesado no Bankai.
- Documentos: [[ichigo-onda-aprovacao-visual]], [[ichigo-3.6]]."""),
}
for n,(anime,corpo) in P.items():
    nota(f'Personagens/{n}',['personagem'],f"# {n}\n\n**Anime:** {anime} · voltar para [[Personagens]]\n\n{corpo}\n\nSistemas: [[Tecido]] · [[Áudio]] · [[Efeitos Visuais]] · [[RPG e Despertar]]")

nota('Personagens/Subaru',['personagem'],"""
# Subaru Natsuki (Re:Zero)

Estrategista: controle tático e suporte. Voltar para [[Personagens]]. Status: **kit completo no jogo** (kit 16,
protocolo 17, JAR `3.14.0-subaru-preview.9`), com traje (agasalho) e SFX próprios.

## Base
| Tecla | Técnica | Efeito | Classe / clipe |
| --- | --- | --- | --- |
| Z | Shamak | Orbe de sombra com núcleo, casca de fumaça e tentáculos; névoa e venda de sombra na cabeça do alvo (cegueira + confusão 3 s) | `SamakSubaru` / `subaru_shamak` |
| X | Chamado Espiritual de Emergência | Espíritos de luz formam bolha de mana com runas; absorve até 2 corações e estoura | `EscudoSubaru` / `subaru_escudo` |
| C | Instrução Tática | Pulso dourado no chão (6 blocos) dá velocidade e recarga de pulo por 5 s | `InstrucaoSubaru` / `subaru_instrucao` |
| V | Recuperação Psicológica | Fumaça de veneno/lentidão sai do corpo; coração de luz e Regeneração I por 2 s | `RecuperacaoSubaru` / `subaru_recuperacao` |

## G — Contrato com a [[Beatrice]]
| Tecla | Técnica | Status |
| --- | --- | --- |
| G | Contrato: círculo, porta da Biblioteca Proibida, a Beatrice sai e fica ao lado | Aprovado (`ContratoSubaru`, `subaru_contrato`) |
| Z | Minya: leque de 7 estacas de cristal; cada uma acerta o primeiro inimigo no caminho | No jogo (`MinyaBeatrice`) |
| X | E.M.M.: invulnerável por um instante | No jogo |
| C | Murak: peso reduzido, salto alto e queda lenta | No jogo |
| V | Ul Minya: chuva de cristais (2 de dano a cada 4 ticks na área) | No jogo |

A antiga ultimate "Autoridade da Preguiça (Mãos Invisíveis)" foi **substituída** pelo contrato.

## Arquivos
- Animações do corpo: `assets/arcanecontrol/player_animation/subaru.json`.
- Prévias: `tools/preview_subaru_*.py` (as mais novas a 40 fps).
- Servidor: `PoderesSubaru`; cliente: `EfeitosSubaru`, `RenderMagiasSubaru` (pacotes 600-605); forma de 20 s.
- Traje: `tools/build_subaru_traje.py` (jaqueta branca, mangas e barra pretas, detalhes amarelos, "N" no peito).
- Sons: `tools/build_subaru_sfx.py`, 13 sons com gravações CC0 (`art/audio/cc0/`).

Ver também: [[2026-11-04 Subaru e Beatrice]], [[Próximos Personagens]].
""")

nota('Personagens/Beatrice',['personagem','companheira'],"""
# Beatrice (Re:Zero)

Companheira do [[Subaru]] na forma de contrato (G). Ela flutua ao lado do ombro esquerdo dele e é quem lança a magia
Yin (Minya, E.M.M., Murak, Ul Minya/Al Shamak).

## Modelo (aprovado)
- Feito do zero para o Minezin em `tools/build_beatrice.py` → `geo/subaru/beatrice.geo.json` e
  `textures/entity/subaru/beatrice.png` (128×128, rosto pintado).
- Proporção chibi (cabeça 10×10), olhos azuis com pupila em borboleta, sobrancelhas franzidas, boquinha emburrada.
- Brocas grossas e retas até a cintura com fita listrada, um osso por segmento (balançam).
- Vestido carmim: mangas em sino com punho felpudo, laço grande listrado, meias listradas, coroa dourada com rubi.
- **Saia em tecido liso** com bordas suaves (anel de 10 painéis × 3 linhas, como a saia da Frieren) e barra felpuda lisa.
- Prévia: `tools/preview_beatrice.py`.

> O mod "Re:Zero Mod" (CurseForge, celefono_) foi olhado só como referência visual: é *All Rights Reserved*, nada
> dele foi copiado.

No jogo: desenhada com `GeoObjectRenderer` (sem entidade), sai da porta e acompanha o Subaru durante a forma.
""")

nota('Personagens/Bosses Nemesis',['personagem','boss'],"""
# Bosses (Nemesis)

Toji, Aurora, Qual, Liebe e Zangetsu, com telegraphs e golpes próprios. Modelos em `geo/nemesis/`, gerados por
`tools/build_nemesis_models.py`. Código em `core/rpg/*` e `client/render/RenderBossVfx`.

Voltar para [[Personagens]].
""")

nota('Sistemas/Sistemas',['moc','sistema'],"""
# Sistemas

- [[RPG e Despertar]]: mana, maestria, despertar.
- [[Efeitos Visuais]]: VFX em volumes 3D.
- [[Áudio]]: SFX procedural e sincronia.
- [[Tecido]]: física e superfícies lisas.
- [[Rede]]: pacotes e protocolo.
- [[Configuração]]: arquivos TOML.
""")
nota('Sistemas/RPG e Despertar',['sistema'],"""
# RPG, maestria e despertar

- Mana, recargas e maestria salvas por jogador (sobrevivem à morte, reconexão e troca de dimensão).
- Maestria: +0,75% de dano e −0,3% de recarga por nível.
- Despertar (G) libera a forma transformada e o moveset desperto. Validado no servidor; o cliente só desenha.
- Código: `core/rpg/*`, `core/progressao/*`, `core/rpg/MovesetsServidor.java`.
- Balanceamento: [[balanceamento-3.11]]. Comandos: [[comandos-testes-rpg]]. Integração: [[rpg-integracao-2.3]],
  [[despertar-3.0]].
""")
nota('Sistemas/Efeitos Visuais',['sistema','vfx'],"""
# Efeitos visuais (VFX)

- Volumes 3D (malhas com luz e borda), nunca sprites planos.
- Geometria de cada efeito é uma **classe pura** em `core/vfx/` (sem Minecraft), presa aos ticks do servidor; testes
  exportam `build/*-samples.json` para as [[Prévias Offline]].
- Desenho com o shader `anime_energy.fsh` (vários modos de material) e Photon para bloom de acento.
- Ultimates com câmera cinematográfica (desligável na árvore).
- Render: `core/<kit>/Efeitos*.java`, `client/render/*` (`RenderDespertar`, `UltimatesTransformadas`).
- Documentos: [[rework-vfx-3.5-preview]], [[vfx-referencias-sfx-preview2]].
""")
nota('Sistemas/Áudio',['sistema','sfx'],"""
# Áudio

- Sons feitos no motor em camadas `tools/sfx_motor.py` (síntese numpy; a partir do Subaru também gravações **CC0**
  como camada, créditos em `art/audio/cc0/fontes.json` e THIRD_PARTY_NOTICES): transiente, corpo, grave
  saturado, eco; compressão só nos picos; **3 variações** (pitch ±4%).
- Cada habilidade é dividida em momentos tocados no tick exato (`RoteiroAudio`, campos `atraso` e `vida`).
- `AudioAnime.programar` cancela o grupo anterior de mesmo nome. Atenuação máxima 64; pico < 0,99.
- Geradores: `build_gojo_sfx.py`, `build_asta_sfx.py`, `build_cid_sfx.py`, `build_ichigo_sfx.py`,
  `build_frieren_sfx.py`; `build_anime_sfx.py` só mescla o `sounds.json`.
- Caráter: Gojo cristalino, Asta sujo, Cid elegante, Ichigo rugindo, Frieren sinos.
""")
nota('Sistemas/Tecido',['sistema','tecido'],"""
# Tecido (cloth)

- **Física:** costas em malha PBD (`MalhaTecido`); laterais e frente ligadas às costas; mangas e faixas em molas
  (`MolaMantoCid`); 20 Hz com interpolação; onda, vento, subida na queda, impulso das habilidades.
- **Desenho:** painéis posados viram malha única costurada e suavizada (`SuperficieTecido`): casaco aberto (Cid,
  Ichigo), anel fechado (saia/capinha da Frieren, barra do Gojo, saia da [[Beatrice]]) ou folha aberta (Asta).
- **Colisão:** pernas (`Caixa`/`Obstaculo`) e blocos do mundo (`ColisaoCenario`, só no render, com checagem de
  segurança).
- **Por traje:** `assets/arcanecontrol/tecido/<estilo>.json` (`PerfilTecido`: peso, rigidez, arrasto, inércia, mola).
- Gêmeo em Python: `tools/superficie_tecido.py` (mude os dois juntos).
- Documentos: [[tecido-arquitetura]], [[Padrão de Trajes]].
""")
nota('Sistemas/Rede',['sistema'],"""
# Rede

- `core/rede/*`: `PacoteEfeitoDespertar`, `PacoteSincronizarCutscene3D`.
- `RegistoRede.PROTOCOL_VERSION` = 16; subir quando houver efeito novo. Cliente e servidor precisam da mesma versão.
- Pacotes mandam pontos absolutos; o render subtrai a origem do efeito (ver [[Armadilhas]]).
""")
nota('Sistemas/Configuração',['sistema'],"""
# Configuração

- Cliente `config/arcanecontrol-client.toml`: cinematics, tecido por personagem, emissão, SFX, VFX reduzidos, shader e
  Photon da Atomic.
- Servidor `serverconfig/arcanecontrol-server.toml`: destruição de terreno e raio da cratera da Atomic.
""")

nota('Desenvolvimento/Desenvolvimento',['moc','dev'],"""
# Desenvolvimento

- [[Ambiente e Build]]
- [[Testes]]
- [[Prévias Offline]]
- [[Arquitetura do Código]]
- [[Workflow]]
- [[Padrões de Qualidade]] · [[Padrão de Trajes]]
- [[Armadilhas]]
- Para IAs: [[conhecimento-assistente]], [[guia-continuidade-ia]]
""")
nota('Desenvolvimento/Ambiente e Build',['dev'],"""
# Ambiente e build

- JDK 17; Gradle 8.3 pelo wrapper; Python 3 com `numpy`, `pillow`, `moderngl`, `av`, `soundfile`.
- Pasta: `C:\\Users\\MAIS INTERNET\\Documents\\My Games\\Minezin-main`; saídas em `..\\minezin-outputs`.
- O caminho tem espaço → Gradle falha com *loopback*. Rode fora do sandbox:

```powershell
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:\\gtmp -Djava.io.tmpdir=C:\\gtmp'; $env:TMP='C:\\gtmp'; $env:TEMP='C:\\gtmp'
.\\gradlew.bat clean build --no-daemon
```

- JAR em `build/libs/minezin-<versão>.jar`. Ao gerar JAR, subir versão em `build.gradle` **e** `mods.toml`.
- Dependências do jogo: Forge 47.2.0, GeckoLib 4.4.9, Player Animator 1.0.2-rc1+1.20, Photon 1.1.17 (LDLib 1.0.42).
- `gh` sem login: PR via link `https://github.com/Cauan-Silva/Minezin/compare/main...<branch>`.
""")
nota('Desenvolvimento/Testes',['dev'],"""
# Testes

```powershell
.\\gradlew.bat test
python tools/summarize_tests.py
python -m unittest discover -s tools -p "test_blockbench_projects.py"
```

- 252+ testes Java: marcos de tick, limites, sincronia de som, tecido sem esticar nem torcer, costuras, colisão.
- Vários exportam `build/*-samples.json` usados pelas [[Prévias Offline]] (declarados em `build.gradle`).
- O CI (GitHub Actions) compila, testa, valida GLSL e renderiza prévias de materiais e trajes.
""")
nota('Desenvolvimento/Prévias Offline',['dev','vfx'],"""
# Prévias offline

Cada técnica, traje e som é aprovado por prévia gerada com os dados reais dos testes (moderngl + PyAV), rodando
`python -B <script>` de dentro de `tools/`. Saída: vídeo `.mp4` + painel de fases `.png` em `minezin-outputs/`.

- Trajes: `preview_ichigo_traje.py --estilo shadow|ichigo_bankai|ichigo_mugetsu`.
- Tecido: `preview_frieren_cloth.py`, `preview_gojo_cloth.py`, `preview_asta_capa.py`.
- Subaru: `preview_subaru_shamak.py`, `_escudo`, `_instrucao`, `_recuperacao`, `_contrato`, `_minya`;
  `preview_beatrice.py`.
- Sons: `build_<kit>_sfx.py --preview <wav>`.

**Limites:** não mostram terreno, skin real, primeira pessoa, shader packs, atenuação espacial nem colisão com o
cenário.
""")
nota('Desenvolvimento/Arquitetura do Código',['dev'],"""
# Arquitetura do código

Raiz: `src/main/java/com/cauansilva/arcanecontrol/`.

| Área | Onde |
| --- | --- |
| Poderes (servidor) | `core/<kit>/Poderes*.java`, `core/magia/GerenciadorMagiasGojo.java` |
| Tempos compartilhados | `core/vfx/Perfil*`, `RelogioAtomic`, `UltAsta`, `MugetsuIchigo` |
| Geometria de VFX | `core/vfx/*` (classes puras) |
| Render de efeitos | `core/<kit>/Efeitos*.java`, `client/render/*` |
| Rede | `core/rede/*` → [[Rede]] |
| RPG | `core/rpg/*`, `core/progressao/*` → [[RPG e Despertar]] |
| Áudio | `core/audio/*`, `client/AudioAnime` → [[Áudio]] |
| Tecido | `core/client/tecido/*` → [[Tecido]] |
| Trajes | `core/<kit>/RenderTraje*` |
| Assets | `assets/arcanecontrol/` (`geo`, `textures`, `sounds`, `shaders`, `tecido`, `player_animation`) |
| Ferramentas | `tools/` |
| Blockbench | `art/blockbench/*.bbmodel` |

Fluxo de um traje: `build_<kit>_traje.py` gera geo + textura → física a 20 Hz → renderizador aplica a pose e desenha
a superfície lisa → prévia em Python usa a mesma conta.
""")
nota('Desenvolvimento/Workflow',['dev','processo'],"""
# Workflow

1. Pedir/pesquisar a referência do anime.
2. Implementar a geometria pura em `core/vfx/` + teste que exporta amostras.
3. Gerar prévia (mp4 + fases) e enviar.
4. Aprovação do usuário ("Aprovado" / "Ajustar"). Uma técnica por vez.
5. Integrar no jogo (servidor, pacotes, render, som).
6. Build e testes completos antes de ir para a `main`.
7. Commit/push/merge só com pedido explícito; cada trabalho em branch própria.

Detalhes: [[workflow-roadmap-minezin]], [[guia-continuidade-ia]].
""")
nota('Desenvolvimento/Padrões de Qualidade',['dev','processo'],"""
# Padrões de qualidade

- **Fiel ao anime:** cores, formas e ritmo da cena de referência.
- **VFX em volumes 3D**, nunca planos 2D (uma versão 2D foi reprovada).
- **Uma técnica por vez** com prévia e aprovação.
- **Trajes:** ver [[Padrão de Trajes]].
- **Sons:** próprios, em camadas, 3 variações, presos aos ticks.
- **Sincronia:** som, efeito e dano saem das mesmas constantes.
- **Animação fluida:** chaves encadeadas, sem poses paradas; prévias a 40 fps.
""")
nota('Desenvolvimento/Padrão de Trajes',['dev','tecido'],"""
# Padrão de trajes

Referência: [[Ichigo]] e [[Cid]].
- Silhueta justa (trajes "gordos" foram reprovados); três camadas de detalhe.
- Todo tecido é uma peça só; a frente aberta segue a pose da lateral.
- Superfície lisa costurada (`SuperficieTecido`), nunca blocos soltos; formas curvas como superfícies.
- Colisão com pernas e cenário; peso/rigidez em JSON por traje.

Documento completo: [[padrao-trajes]]. Sistema: [[Tecido]].
""")
nota('Desenvolvimento/Armadilhas',['dev'],"""
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
""")

nota('Planejamento/Roadmap',['planejamento'],"""
# Roadmap

1. Testar o `3.14.0-subaru-preview.9` no jogo e corrigir o que aparecer ([[Pendências]]).
2. [[Subaru]] completo; seguir para o próximo personagem (Killua) ([[Próximos Personagens]]).
4. Tecido: desempenho com muitos jogadores, colisão do cenário dentro da física, asas e cauda do Devil Union lisas.

Completo: [[workflow-roadmap-minezin]].
""")
nota('Planejamento/Pendências',['planejamento'],"""
# Pendências (testar no jogo)

| Área | O que conferir |
| --- | --- |
| Tecido liso (5 kits) | Costuras sem frestas correndo, parando e girando; colisão com as pernas |
| Colisão com o cenário | Parede, cerca, laje; nada estranho no inventário |
| Sons | Volume, sincronia, variações |
| Balanceamento 3.11 | Danos e recargas, Atomic de 9 s com ticks sem empurrão, Bankai ~45 s |
| Ichigo | Empunhadura da Zangetsu, Mugetsu de 10 s |
| [[Subaru]] e [[Beatrice]] | Kit base, contrato, magias da forma, agasalho e sons CC0 |
""")
nota('Planejamento/Próximos Personagens',['planejamento','personagem'],"""
# Próximos personagens

Ordem informada em 03/11/2026: **[[Subaru]] (Re:Zero)** (feito), **Killua** (próximo) (Hunter x Hunter) e os demais do
documento [[proximos-personagens]].
""")

nota('Diário/Diário',['moc','diario'],"""
# Diário

- [[2026-11-04 Subaru e Beatrice]]
- [[2026-11-05 Subaru no jogo]]
""")
nota('Diário/2026-11-04 Subaru e Beatrice',['diario'],"""
# 04/11/2026 — Subaru e Beatrice

- Shamak (Z) refeito mais detalhado: núcleo + casca de fumaça, tentáculos, partículas, venda de sombra, névoa no chão.
- Escudo Espiritual (X), Instrução Tática (C) e Recuperação Psicológica (V) aprovados; V com animação de duas
  respirações e toxina em fumaça (os fios ficaram feios).
- Ultimate trocada: G passa a ser o **Contrato com a [[Beatrice]]** (Minya, E.M.M., Murak, Ul Minya/Al Shamak).
- Modelo da Beatrice refeito no estilo chibi com rosto pintado; saia em tecido liso com bordas suaves.
- Entrada do contrato aprovada e deixada mais fluida (40 fps nas prévias).
- Minya (G-Z) em prévia.
- Merge na `main`: `cb9e4eb` + `37b5cc4` (sem PR, `gh` sem login).
""")

nota('Diário/2026-11-05 Subaru no jogo',['diario'],"""
# 05/11/2026 — Subaru no jogo

- Kit base integrado (kit 16, protocolo 17); contrato com a [[Beatrice]] no jogo, forma de 20 s.
- Magias da forma no jogo: Minya, E.M.M., Murak, Ul Minya.
- Correções do primeiro teste: G aparecia cinza no HUD, Minya sem dano em quem estava perto, multi-hit do Ul Minya.
- Agasalho do [[Subaru]] aprovado; SFX de todos os ataques com gravações CC0.
- Documentos atualizados e vault do Obsidian versionado em `vault/`.
""")

# documentacao original
D=V/'Documentos';n=0
for f in [R/'README.md',R/'THIRD_PARTY_NOTICES.md',*sorted((R/'docs').rglob('*.md'))]:
    if not f.exists():continue
    rel=f.relative_to(R/'docs') if 'docs' in f.parts else Path(f.name)
    alvo=D/rel;alvo.parent.mkdir(parents=True,exist_ok=True)
    txt=f.read_text(encoding='utf-8')
    # links relativos .md viram wikilinks pelo nome do arquivo
    txt=re.sub(r'\[([^\]]+)\]\((?!http)([^)#]*/)?([^/)#]+)\.md(#[^)]*)?\)',lambda m:f'[[{m.group(3)}|{m.group(1)}]]',txt)
    alvo.write_text('---\ntags: [documento]\n---\n'+txt,encoding='utf-8');n+=1
lista='\n'.join(f'- [[{p.stem}]]' for p in sorted(D.rglob('*.md')))
nota('Documentos do Repositório',['moc','documento'],"# Documentos do repositório\n\nCópia de `README.md` e de `docs/` (links convertidos para o Obsidian).\n\n"+lista)
(V/'.obsidian').mkdir(exist_ok=True)
(V/'.obsidian/app.json').write_text('{"alwaysUpdateLinks": true, "newLinkFormat": "shortest"}',encoding='utf-8')
print('ok',n,'documentos',sum(1 for _ in V.rglob('*.md')),'notas')
