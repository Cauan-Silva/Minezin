# Gojo: direção visual e validação da versão 1.5

A direção atual foi aprovada pelo usuário: Azul com atração e núcleo escuro, Vermelho concentrado com repulsão, fusão Azul/Vermelho legível no Roxo e ambientação cósmica no Vazio Infinito. Evoluções para outras apresentações do anime ficam para uma etapa posterior. Os materiais e a geometria desta versão são autorais; não extraem recursos de outros mods ou do anime.

## Composição por técnica

| Técnica | Material e movimento | Correspondência com a simulação |
| --- | --- | --- |
| Azul | Núcleo escuro, borda ciano, filamentos espirais que terminam junto ao núcleo e bloom discreto | Centro acompanha o olhar a 7,5 blocos, como a atração no servidor; duração de 60 ticks |
| Vermelho | Fonte pequena branca/carmesim e frente de repulsão, sem projétil viajando | Impacto no tick 13; posição e direção de disparo são confirmadas pelo servidor; fim no tick 28 |
| Infinito | Barreira quase invisível; ondulações localizadas perto de entidades que se aproximam | Estado ligado/desligado sincronizado com observadores, inclusive ao começar a rastrear o jogador; desligamento na morte |
| Roxo | Azul e Vermelho se aproximam; núcleo violeta surge durante a fusão; massa lançada com esteira helicoidal | Fusão termina no tick 30; origem a 3,2 blocos do olho e avanço de 1,6 bloco/tick, compartilhados com o servidor |
| Vazio Infinito | Barreira escura por fora, céu estrelado/nebulosa e piso astral por dentro, horizonte escuro com halo e linhas de informação | Centro nos pés do conjurador, raio de 26 blocos e fim no tick 160; ambiente depende da câmera estar dentro da área |

A consulta do Domínio no servidor agora filtra a esfera de raio 26, corrigindo os cantos do antigo AABB cúbico que afetavam alvos fora do domo. Dano, recargas, desbloqueios e maestria não são alterados.

## Materiais, ambiente e configurações

O shader `gojo_energy` diferencia as técnicas por composição e não apenas por cor. Corpos usam mistura alfa; halos e fitas usam uma definição aditiva separada, compartilhando os programas GLSL. A geometria é desenhada em lotes; bloom Photon usa emissores pequenos, limitados a 64 partículas cada. Até oito técnicas e oito barreiras de Infinito são desenhadas por frame, com até oito runtimes Photon. A lista de efeitos mantém limite de 64.

O céu do Domínio é desenhado depois do céu normal e antes do terreno. Cor e distância da neblina são alteradas somente nos eventos do frame atual, com entrada/saída suaves e sem interferir na neblina de água/lava. O terreno e as entidades continuam visíveis. Um disco astral com borda suave recobre o piso plano junto ao centro, sem trocar blocos; relevos mais altos continuam sujeitos ao teste de profundidade. Não há refração real da imagem do cenário ou iluminação dinâmica de blocos nesta versão.

Em `config/arcanecontrol-client.toml`:

- `gojoVfx`: material/composição novos; desligar usa a geometria anterior.
- `bloomGojo`: emissores Photon de acento, independentes da câmera.
- `ambienteDominio`: céu e neblina locais do Domínio.
- `claraoGojo`: clarão breve na fusão e na abertura do Domínio.
- `vfxReduzidos`: menos segmentos, filamentos e emissão; não encurta a técnica.
- `cinematicas`: controla câmera, zoom e tremor.

Falhas ao carregar o shader registram aviso e mantêm o renderizador anterior. Efeitos e runtimes são removidos ao expirar, desconectar, perder o conjurador, trocar de mundo ou detectar sua morte. As legendas não cobrem mais os gestos; a câmera do Domínio revela o horizonte acima do conjurador.

## Verificações automatizadas e prévia

Os testes JUnit cobrem a distância do Roxo, continuidade da fusão/trajetória, limites de opacidade, isolamento espacial/temporal do ambiente e continuidade do alvo vertical da câmera. O CI compila Forge, verifica GLSL e compila/vincula/renderiza os shaders reais em OpenGL/Mesa, gerando um artefato de prévia. A ferramenta também verifica que o Azul preserva núcleo escuro e que Vermelho/Roxo mantêm sua identidade cromática.

Para reproduzir a prévia, instale `moderngl`, `numpy` e `pillow` em um ambiente Python de desenvolvimento e execute:

```shell
python tools/preview_gojo_vfx.py --output work/gojo-materials.png
```

Em Linux sem tela, use `--backend egl`. A prévia contém os materiais reais, sem cenário Minecraft, bloom, partículas, gestos ou composição completa. Ela não substitui um teste no jogo.

## Teste manual ainda necessário

Use uma cópia de um mundo de testes, pois o Roxo já possui destruição de blocos. Cliente e servidor precisam da mesma versão 1.5.0/protocolo 5 e das dependências existentes. Use as técnicas desbloqueadas na árvore com `K`.

| Cenário | Resultado a conferir |
| --- | --- |
| Azul: mirar em direções diferentes, em primeira e terceira pessoa | O centro visual acompanha a atração; o núcleo permanece legível, sem nuvem de poeira cobrindo-o |
| Vermelho: virar a câmera após o disparo | A frente de repulsão fica ancorada no disparo confirmado; não nasce um projétil tardio seguindo o novo olhar |
| Roxo: mirar e disparar | As duas cores se fundem antes da massa violeta; trajeto visual coincide com dano e corredor de blocos destruídos |
| Infinito: ativar, aproximar entidade/projétil e desligar | Barreira discreta em repouso e ondulação no contato; outro cliente vê o mesmo estado |
| Infinito: observador entrar na área depois da ativação; dono morrer/renascer | Estado inicial correto; não resta barreira fantasma após a morte |
| Domínio: câmera dentro, na borda e fora | Céu/neblina entram e saem suavemente; fora da esfera o ambiente normal é preservado |
| Domínio: fim, morte e mudança de dimensão | Sem céu, neblina, câmera ou emissor preso no mundo seguinte |
| Opções desligadas, perfil reduzido e recarga `F3+T` | Recuperação previsível, shaders recarregados e ausência de erros gráficos |
| Multiplayer, várias técnicas e Oculus/Embeddium | Conferir sincronização, sobreposição, FPS e compatibilidade; ainda não verificados no jogo |

Esta entrega não declara equivalência gráfica com Shooting Star. Esse nível depende da composição completa em jogo e de avaliação visual, não apenas de compilação ou quantidade de partículas.
