# Conhecimento do assistente — Projeto Minezin

Este documento é o contexto fixo para qualquer assistente de IA que trabalhe no Minezin. Siga estas instruções em
todas as respostas. Quando algo aqui entrar em conflito com um pedido explícito do usuário na conversa, vale o pedido
do usuário.

---

## 1. O projeto em uma linha

Mod de RPG de anime para **Minecraft 1.20.1 / Forge 47.2.0 / Java 17** (modid `arcanecontrol`, repositório
`Cauan-Silva/Minezin`). Cinco personagens jogáveis: **Gojo, Cid (Shadow), Frieren, Asta e Ichigo**, cada um com quatro
técnicas (Z/X/C/V), despertar (G) com moveset próprio, traje com física de tecido, arma e sons próprios. Bosses:
Toji, Aurora, Qual, Liebe, Zangetsu. Dependências: GeckoLib 4.4.9, Player Animator 1.0.2-rc1+1.20, Photon 1.1.17.

Versão atual: `3.13.0-tecido-preview.1`, protocolo de rede **16**.

---

## 2. Como falar com o usuário

- Responda **sempre em português do Brasil**, direto e claro. O usuário acompanha pelo resultado (prévias, vídeos,
  JARs), não lendo código.
- Use tabelas curtas e listas quando houver várias partes. Explique o que mudou para quem joga, não o nome das funções.
- Diga com honestidade o que **não** foi verificado: o que só passou em teste automático, o que não foi testado no
  jogo, o que a prévia não mostra.
- Quando terminar algo grande, pergunte qual é o próximo passo com opções claras (por exemplo "Aprovado" /
  "Ajustar").

---

## 3. Regras de trabalho (obrigatórias)

1. **Fidelidade ao anime é requisito.** Cores, forma, ritmo e proporção da cena de referência. Peça a referência
   (imagem, vídeo, cena) ou pesquise antes de desenhar. Se a técnica não existir no anime, diga isso e use a canônica
   mais próxima.
2. **VFX em volumes 3D de verdade**, nunca planos ou sprites 2D. Uma versão "2D" foi rejeitada.
3. **Uma técnica por vez:** implementar → gerar prévia (vídeo `.mp4` + painel de fases `.png`) → pedir aprovação →
   registrar. Não reabra o que já foi aprovado sem motivo.
4. **Commit, push, PR e merge só com pedido explícito do usuário.** Cada trabalho em um branch próprio a partir da
   `main`. O usuário já autorizou, em casos específicos, merge direto na `main` sem PR porque o `gh` não tem login;
   isso vale só quando ele pedir de novo.
5. **Antes de enviar para a `main`, rode o build completo e os testes** (seção 7). Nunca envie com teste falhando.
6. **Não gere JAR sem pedido.** Quando pedir, suba a versão em `build.gradle` **e** em
   `src/main/resources/META-INF/mods.toml` juntos e copie o JAR para `../minezin-outputs/`.
7. **Não quebre o que foi aprovado.** Mudanças em sistemas compartilhados (tecido, áudio, shader) precisam manter o
   comportamento aprovado dos outros personagens, e isso deve ser dito ao usuário ("isso também vale para o Ichigo").

---

## 4. Padrões de qualidade aprovados

### Trajes (referência: Ichigo e Cid — `docs/padrao-trajes.md`)
- Silhueta **justa** ao corpo; trajes "gordos" foram reprovados.
- Três camadas de detalhe (base, borda/sombra, detalhe).
- **Todo tecido é uma peça só:** costas em malha de partículas; laterais, quinas e frente seguem as colunas da borda
  das costas; a frente aberta tem exatamente a pose da lateral (limites próprios da frente separavam o pano).
- **Superfície lisa:** casaco, capa e saia são desenhados como malha única costurada (`SuperficieTecido`), nunca
  como blocos soltos. Formas curvas (gola do Cid) são superfícies, não degraus de blocos.
- **Colisão** com as pernas e com os blocos do mundo.
- Peso e rigidez em `assets/arcanecontrol/tecido/<estilo>.json`.

### Sons (`tools/sfx_motor.py`)
- Som próprio para cada habilidade; nenhum som do Minecraft.
- Camadas: ataque curto (onde a sincronia é sentida), corpo, grave saturado, eco. Compressão só nos picos, para o
  golpe ficar mais alto que a cauda.
- **3 variações** de cada som.
- Cada habilidade dividida em momentos, tocados no tick exato do efeito visual (`RoteiroAudio`).
- Distância de atenuação máxima **64** (um teste exige isso). Pico decodificado < 0,99.
- Caráter por personagem: Gojo cristalino e espacial; Asta sujo e escuro (antimagia); Cid elegante (katana, slime,
  zumbido violeta); Ichigo reiatsu rugindo (escuro e pesado no Bankai); Frieren sinos cristalinos e feixes limpos.

### VFX e sincronia
- Geometria de efeito em classes **puras** em `core/vfx/` (sem Minecraft), com fases presas às constantes do
  servidor. Som, efeito e dano saem das mesmas constantes.
- Testes exportam `build/*-samples.json` para as prévias.

---

## 5. Mapa do código

Raiz Java: `src/main/java/com/cauansilva/arcanecontrol/`.

| Área | Onde |
| --- | --- |
| Poderes (servidor) | `core/<kit>/Poderes*.java`, `core/magia/GerenciadorMagiasGojo.java`, `core/rpg/MovesetsServidor.java` |
| Tempos compartilhados | `core/vfx/Perfil*`, `RelogioAtomic`, `UltAsta`, `MugetsuIchigo`, `EntradaBankai`, `PerfilAsta`, `PerfilIchigo` |
| Geometria de VFX | `core/vfx/*` |
| Render de efeitos | `core/<kit>/Efeitos*.java`, `client/render/*` (`RenderDespertar`, `UltimatesTransformadas`, `RenderBossVfx`) |
| Rede | `core/rede/*` (`RegistoRede.PROTOCOL_VERSION`; subir quando houver efeito novo) |
| RPG | `core/rpg/*`, `core/progressao/*` |
| Áudio | `core/audio/RoteiroAudio` (cues), `CatalogoSons` (nomes), `RegistroSons` (Forge), `client/AudioAnime` (agenda) |
| Tecido | `core/client/tecido/`: `MalhaTecido`, `TecidoIchigo` (Cid e Ichigo), `TecidoFrieren`, `TecidoGojo`, `TecidoAsta`, `SuperficieTecido`, `AneisLisos`, `ColisaoCenario`, `PerfilTecido`, `CapturaVertices` |
| Render de trajes | `core/cid/RenderTrajeCid`, `core/ichigo/RenderBankaiIchigo`, `core/frieren/RenderTrajeFrieren`, `core/gojo/RenderTrajeGojo`, `core/asta/RenderTrajeAsta` |
| Geradores | `tools/build_<kit>_traje.py`, `build_<kit>_sfx.py`, `build_<kit>_assets.py`, `clothing_uv.py`, `build_armor_models.py` |
| Prévias | `tools/preview_*.py` (moderngl + PyAV); `tools/superficie_tecido.py` é o gêmeo em Python de `SuperficieTecido.java` |
| Blockbench | `art/blockbench/*.bbmodel` |

Teclas: P escolhe o kit, Z/X/C/V técnicas, G despertar, R domínio do Gojo, H árvore RPG, K árvore legada.

---

## 6. Ambiente do PC do usuário (Windows)

- Pasta do projeto: `C:\Users\MAIS INTERNET\Documents\My Games\Minezin-main`; saídas em
  `C:\Users\MAIS INTERNET\Documents\My Games\minezin-outputs`.
- O caminho tem espaço: o Gradle falha com *Unable to establish loopback connection*. Rode sempre assim, **fora do
  sandbox**:

```powershell
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:\gtmp -Djava.io.tmpdir=C:\gtmp'; $env:TMP='C:\gtmp'; $env:TEMP='C:\gtmp'
.\gradlew.bat clean build -q
```

- Python: rode as ferramentas com `python -B` de dentro de `tools/` (não use `-P`, que esconde os módulos vizinhos).
- PowerShell `Set-Content`/`Out-File` gravam com BOM e quebram o `javac`: edite Java com Python ou com a ferramenta
  de edição, nunca com `Set-Content`.
- Heredocs longos de Python no bash falham com aspas: escreva o script num arquivo e rode o arquivo.
- O GitHub CLI (`gh`) pode estar **sem login**. Nesse caso, não dá para abrir PR: envie o branch e passe o link
  `https://github.com/Cauan-Silva/Minezin/compare/main...<branch>` com título e descrição prontos.
- Leitura de imagens grandes pode ser bloqueada: reduza (por exemplo, para JPG de ~1000 px) antes de abrir.

---

## 7. Verificação antes de entregar

```powershell
.\gradlew.bat clean build -q
python tools/summarize_tests.py
python -m unittest discover -s tools -p "test_blockbench_projects.py"
```

Antes de enviar para a `main`, rode também as prévias que o CI executa (`.github/workflows/build.yml`), porque o CI
renderiza materiais e trajes: por exemplo `python tools/preview_armor_models.py --poses --output build/b2.png`,
`--ichigo`, sem argumento, e `python tools/preview_atomic_reference.py --output build/a3.png`.

Ao entregar um JAR: informe nome, tamanho, o que entrou e **o roteiro do que testar no jogo**.

---

## 8. Armadilhas conhecidas

1. **Coordenadas absolutas × relativas:** os pacotes de efeito mandam pontos do mundo; o render desenha com a pose na
   origem do efeito. Sempre subtraia a origem. As prévias devem receber coordenadas absolutas como o jogo.
2. **Rotação Z no GeckoLib** é horária para ângulos positivos.
3. **Não leia `RegistroSons` em teste** (inicializa o Forge); use `CatalogoSons`.
4. **Vorbis estoura picos** em transientes secos: normalize e confira o pico decodificado.
5. **`build_armor_models.build()` sem `only`** regera outros trajes e apaga etapas posteriores: use `only={'<estilo>'}`.
   O `build_anime_sfx.py` antigo agora só mescla o `sounds.json` (antes reescrevia tudo).
6. **Lambdas Java** exigem variáveis efetivamente finais.
7. **Duração de efeitos:** ao mudar um tempo (ex.: Atomic de 39 s para 27 s), procure prévias e testes que usam marcos
   fixos; uma prévia com marcos antigos quebrou o CI.
8. **Tecido em superfície lisa:** os cantos de cada painel são marcados pela pose parada (`rotular`), nunca
   adivinhados por distância a cada quadro (isso torcia o pano). Ao mudar `SuperficieTecido.java`, mude também
   `tools/superficie_tecido.py`.
9. **A prévia offline não mostra** terreno, skin real, primeira pessoa, shader packs, atenuação espacial do som nem a
   colisão com o cenário. Diga isso ao entregar.
10. **`AudioAnime.programar`** cancela o grupo anterior com o mesmo nome; use um grupo por efeito
    (`"<kit>_"+tipo`). O campo `vida` do cue corta o som: ele precisa ser pelo menos a duração do momento.

---

## 9. Estado e próximos passos (04/11/2026)

- Na `main`: tecido liso nos cinco kits com colisão nas pernas e no cenário, peso por traje em JSON, rework completo
  dos sons, balanceamento 3.11.
- **Nada disso foi testado no jogo ainda.** O JAR de teste é `minezin-3.13.0-tecido-preview.1.jar`.
- Próximos: corrigir o que aparecer no teste; depois os personagens novos de `docs/design/proximos-personagens.md`,
  começando pelo **Subaru (Re:Zero)**, uma técnica por vez com prévia.
- Futuro do tecido: desempenho com muitos jogadores, colisão do cenário dentro da física, asas e cauda do Devil Union
  como superfície lisa.

Documentos de apoio no repositório: `README.md`, `docs/guia-continuidade-ia.md` (modelo de roadmap de onda),
`docs/workflow-roadmap-minezin.md`, `docs/padrao-trajes.md`, `docs/tecido-arquitetura.md`,
`docs/balanceamento-3.11.md`, `docs/testing/*-onda-aprovacao-visual.md`.
