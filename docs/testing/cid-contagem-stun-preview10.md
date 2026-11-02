# Shadow — contagem após Atomic e stun — 3.6.0-preview.10

Ajustes solicitados sobre a preview.9. O usuário autorizou commit, push e abertura de PR. A publicação reúne as revisões locais do Cid desde a main `09f2727fc796d02171335b86862b7de33d1fca43`.

## Contagem da forma

A forma Shadow tinha 800 ticks (40 s) desde a ativação, consumidos quase inteiramente pelos 780 ticks (39 s) da Atomic. `EstadoDespertar` agora calcula fim em início + introdução + duração: Shadow dispõe dos 800 ticks completos depois da sequência. As demais formas mantêm seu prazo anterior, incluindo Ichigo por mana. Recarga de G não foi adiada nem reiniciada.

`restante()` fica limitado a 800 durante a introdução; `prazoRestante()` inclui o tempo da introdução para o snapshot de rede. O cliente interpola esse prazo e limita apenas o número exibido do Shadow: o HUD permanece em 40 s durante a Atomic, sem cair e voltar a 40 a cada sincronização. Em seguida mostra a contagem normal.

Cancelar a Atomic antes do fim antecipa o início da contagem para aquele instante, uma única vez. Repetir o callback não renova a forma. Cancelar G, morrer ou trocar de dimensão continua encerrando a forma normalmente. A aura/pose e observadores usam a duração inicial total de 1.580 ticks. Protocolo passa a 15 para impedir cliente antigo interpretando essa duração como uma contagem imediata. Usar preview.10 no cliente e no servidor.

## Stun na carga

`AtordoamentoAtomic` atua no servidor durante a carga, antes do tick real 420. Usa a mesma esfera de raio 14 blocos do dano: monstros, mobs que tenham o conjurador como alvo e jogadores adversários em servidor com PvP habilitado. Exclui o próprio conjurador, aliados/equipes, espectadores e jogadores criativos. Animais neutros sem alvo hostil não são classificados como inimigos.

O stun fixa o alvo na posição capturada, zera velocidade e uso de item, desabilita IA/navegação de mobs e aplica modificadores transitórios de movimento. Bloqueia ataques, interações/quebra de blocos de jogadores, disparo de novos projéteis e novas conjurações RPG. Os bosses Minezin verificam o stun antes de seu moveset próprio. Uma Atomic em carga também é interrompida caso seu conjurador seja atordoado por outra.

A liberação ocorre antes do dano/impulso na detonação e também no cancelamento, morte, saída do conjurador, troca de dimensão ou encerramento do servidor. Fontes sobrepostas são controladas separadamente: terminar uma Atomic não remove o stun mantido por outra. Uma fonte sem renovação expira em dois ticks. A IA original é restaurada; um marcador salvo permite restaurá-la no carregamento de um mob se o mundo tiver sido salvo durante o stun. Modificadores de movimento são transitórios. Entidades recarregadas com o mesmo UUID substituem referências antigas.

VFX, áudio, roupa/solver, dano, raio de dano e comando de terreno da preview.9 não foram alterados. O stun é uma alteração de gameplay explicitamente pedida.

## Verificação

Build Forge e reobfuscação com Java 17 aprovados: 141 testes Java e três testes Python, sem falhas. Testes Java cobrem início/fim da contagem, cancelamento idempotente, interpolação do HUD e fontes de stun simultâneas/expiração. Testes Python de projetos Blockbench permanecem no conjunto de verificações. Assets e roupa comparados com preview.9.

Ainda é necessário testar no Minecraft e dedicado: stun real de monstros/bosses/jogadores, restauração após chunk unload/reinício, latência, sobreposição de campos e ataques de outros mods. Os testes offline não executam IA ou rede multiplayer. O raio de stun refere-se ao dano (14), não ao campo visual de 25,6 blocos ou ao raio configurável da cratera.

Roteiro: ativar Shadow; HUD deve manter 40 s durante os 39 s da Atomic e começar em 40 após o fim. Inimigos a até 14 blocos devem ficar presos durante a carga e voltar a agir na detonação/cancelamento. Conferir alvos fora do raio, aliados/PvP desligado, dois Cids, morte, mudança de dimensão e reconexão. Usar `/minezin atomic terreno false` para conferir os efeitos sem destruir o terreno.
