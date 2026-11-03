# Minezin: builds de teste

Este branch guarda só os JARs de teste, para baixar em qualquer PC. O código fica na `main` e nos branches de feature.

## minezin-3.10.0-ichigo-preview.8.jar

**Download direto:** https://github.com/Cauan-Silva/Minezin/raw/builds/minezin-3.10.0-ichigo-preview.8.jar

**Como instalar:** coloque o `.jar` na pasta `mods` do Minecraft e tire de lá a versão anterior (`preview.7`), para não ficar com as duas.

**Situação:** o build foi feito do zero e passou em todos os testes automáticos. **Ainda não foi testado no jogo.**

**O código ainda não tem commit.** Este JAR foi gerado a partir do que está no PC de casa. As mudanças abaixo ainda não estão na `main`.

---

## O que mudou desde o preview 7

### 1. Traje do Cid (Shadow) refeito

Feito a partir das imagens de referência do anime, no mesmo padrão de qualidade dos trajes do Ichigo (`docs/padrao-trajes.md`).

- **Casaco com física de tecido:** usa o mesmo sistema de pano do casaco do Bankai do Ichigo: costas, laterais, quinas e frente.
- **Casaco liso, sem blocos:** a física continua por baixo, mas o desenho é uma única superfície costurada que passa por todas as partes.
  - Cada emenda é compartilhada pelas duas partes vizinhas, então frente, lateral e costas nunca se separam.
  - A barra termina em dentes rasgados de tamanhos diferentes.
- **Gola alta como superfície curva de verdade:**
  - Envolve a cabeça inteira e sai direto do contorno do tronco, sem degrau nem quina quadrada.
  - Primeiro se alarga até um quadrado arredondado um pouco maior que a cabeça. Depois abre até uma borda redonda.
  - É mais alta atrás, quase no topo da cabeça, e mais baixa nas pontas, perto do queixo.
  - É preta por fora e roxa por dentro, com debrum dourado contínuo na borda e nas pontas.
  - As alças pretas saem das pontas.
- **Continuam em blocos**, porque ali o formato reto já fica bom: colete, lapelas, mangas, cinto e botas.
- **Removido:** o sistema antigo do manto direcional (`MantoDirecionalCid`, `DetalhesTrajeCid`) e os testes dele.

### 2. Correções no tecido do Cid em movimento

- **O pano torcia quando balançava muito.** A cada quadro o código adivinhava os cantos de cada painel, e às vezes os trocava.
  - Agora os cantos são marcados uma vez só, na pose parada, e nunca mudam.
  - Um teste novo joga a barra 80° para fora e confere que nenhum pedaço dobra.
- **A frente parecia solta ao correr e parar.** O limite que impedia a frente de entrar nas pernas também a segurava parada.
  - Agora a frente vai para trás junto com a lateral.
  - A parte do movimento que entraria nas pernas vira uma abertura para os lados, como um casaco aberto quando a pessoa corre.

### 3. Tecido colidindo com as pernas (Cid e Ichigo)

- A cada quadro, a posição real das duas pernas é lida como duas caixas.
- Cada ponto do pano que estiver dentro de uma perna é empurrado para fora pela lateral mais próxima, com uma folga pequena.
- O empurrão é só para os lados, então o comprimento do casaco não muda.
- A física não muda, só a superfície desenhada. Por isso o tecido não fica tremendo nem instável.
- **Limitação:** se a perna entrar bem no meio de um pedaço grande do pano, um pedacinho ainda pode atravessar. Vale olhar isso no jogo.

### 4. Casaco do Ichigo no mesmo padrão (Bankai e Mugetsu)

- **Bankai:** costas, laterais, quinas e tiras da frente viraram um tecido liso só, com barra rasgada e colisão com as pernas.
  - O debrum branco e as costuras continuam em blocos, por cima do tecido.
- **Mugetsu:** a saia usa o mesmo tecido liso. A parte fechada da frente (`mg_saia_frente_meio`) continua em blocos.
- As tiras da frente do Bankai seguem a lateral e se abrem para os lados ao correr, em vez de entrar nas pernas.
- A física do tecido do Ichigo não mudou, só o desenho.

### 5. Por dentro do código

- **`SuperficieTecido`** (novo): monta a superfície do casaco, a gola e a colisão com as pernas. Não usa classes do Minecraft, então os testes e a prévia em Python usam a mesma geometria.
- **`CapturaVertices`** (novo): lê onde o GeckoLib colocaria cada bloco. É usado pelos renderizadores do Cid (`RenderTrajeCid`) e do Ichigo (`RenderBankaiIchigo`).
- **Testes:** `SuperficieTecidoTest` (novo), além de `GolaCidTest`, `MantoCidTest` e `AnimeAssetsTest` atualizados.
- **Prévias em Python:**
  - `tools/superficie_tecido.py` e `tools/build_cid_traje.py` são novos.
  - `preview_armor_models.py` agora desenha o tecido liso do Cid e do Ichigo, com as pernas e as botas balançando juntas.

---

## O que conferir no jogo

1. **Gola do Cid:** se aparece na posição e no tamanho certos, com o cabelo da skin dentro.
2. **Casaco do Cid:** se acompanha o corpo e as habilidades sem frestas, correndo, parando e girando.
3. **Bankai e Mugetsu:** se o debrum branco fica colado ao tecido ou parece solto.
4. **Colisão:** se o pano ainda atravessa as pernas em algum momento.
5. **Botas:** se acompanham as pernas normalmente.
