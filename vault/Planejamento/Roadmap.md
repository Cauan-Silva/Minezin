---
tags: [planejamento, roadmap]
---
> Roadmap oficial desde 05/11/2026 (substitui o roadmap anterior e a nota "Próximos Personagens"). Estado do teste no jogo: [[Pendências]]. Detalhes dos kits já pedidos: [[proximos-personagens]].
>
> **Decisões de 05/11/2026:** (1) o G do [[Subaru]] **continua sendo o Contrato com a [[Beatrice]]** (a "Autoridade da Preguiça" abaixo fica só como registro); (2) **as passivas serão implementadas para todos os personagens**, inclusive os já prontos (Gojo, Cid, Frieren, Asta, Ichigo, Subaru).

# DOCUMENTAÇÃO E VAULT ROADMAP: MINEZIN (FORGE 1.20.1)

## 1. Visão Geral e Pilares do Projeto

O mod **Minezin** passa por uma reestruturação arquitetônica para se consolidar como um RPG de Ação de alta fidelidade dentro do Minecraft. O desenvolvimento agora é regido por três pilares inegociáveis:

* **Balanceamento RPG de Classes:** Personagens deixam de ser apenas skins com habilidades soltas e passam a integrar um ecossistema tático (DPS, Tank, Suporte, Controle). O dano, a vida e os *cooldowns* serão escalonados para incentivar o jogo em equipe no multiplayer.

* **Física de Tecidos (Cloth Simulation) e Rework Visual:** O grande diferencial estético do mod. Capas, sobretudos e quimonos terão animações fluídas e independentes do corpo principal, utilizando renderização customizada para reagir a pulos, quedas e ventos.

* **Instanciamento de Boss Battles:** O combate no mundo aberto contra chefes será extinto para evitar quebra de terreno indesejada. Iniciar um confronto aciona um sistema de instanciamento dedicado.

## 2. Sistema de Mecânicas de Boss (Instanciamento e Cutscenes)

### 2.1. Arquitetura de Arenas Instanciadas (Void System)
* **A Dimensão de Combate (`minezin:boss_realm`):** Uma dimensão construída inteiramente no *Void*. Quando ativada, o servidor gera dinamicamente uma cópia da arena específica em coordenadas isoladas (ex: `X: 10000, Z: 10000`), permitindo múltiplas partys enfrentarem o mesmo boss simultaneamente em instâncias separadas.
* **Isolamento e Limites:** Arenas são envoltas por barreiras invisíveis indestrutíveis. O terreno interno pode ser destruído pelo Boss para dinamismo, mas o chão inferior impede quedas no vazio.
* **Morte na Instância:** Jogadores mortos viram espectadores (câmera livre flutuante) até que a Party derrote o Boss ou ocorra um *Wipe* (todos morram).
* **Loot:** Baú instanciado com loot individual e abertura de portal de saída gerados após a animação de morte do Boss.

### 2.2. Flow da Cutscene de Transição
1. **O Gatilho (Trigger):** O jogador interage com um altar no *Overworld* usando um item de invocação (ex: *Dedo de Sukuna*).
2. **Blackout e Teleporte (0.0s - 2.0s):** Tela escurece (*Fade to Black*) e o teleporte da Party ocorre silenciosamente nos bastidores.
3. **Bloqueio de HUD (2.0s - 3.0s):** Tela clareia na arena. Movimento dos jogadores é travado e o HUD é ocultado.
4. **Câmera Cinemática (3.0s - 7.0s):** Câmera se desvincula da 1ª pessoa e segue um *Spline Path* exibindo a escala e iluminação da dimensão.
5. **Entrada do Boss (7.0s - 10.0s):** Foco no ponto de *Spawn* do Boss surgindo (é aqui que o motor de física de tecidos é destacado, reagindo ao movimento de invocação).
6. **Voice Line (10.0s - 12.0s):** Boss solta sua fala icônica (áudio direcional), a câmera volta rapidamente para os jogadores, o HUD e a barra de HP do Boss surgem, e a música inicia.

*Exemplo Prático (Domínio de Sukuna):* Arena de água rasa vermelha com o Santuário Malevolente no centro. A câmera revela o ambiente, sobe focando nas costas de Sukuna. Ele se vira (física do quimono fluindo), sorri e fala. A câmera retorna para os jogadores e a batalha começa com um ataque em área instantâneo.

## 3. Estrutura do Sistema de Classes

Todo personagem possuirá atributos baseados em sua classe principal e uma **Habilidade Passiva Única**:

* **DPS (Dano Contínuo/Explosão):** Foco em eliminar ameaças rapidamente. Baixa defesa, alta mobilidade.

* **Tank (Vanguarda/Defesa):** Alta absorção de dano, mecânicas de aggro e escudos.

* **Suporte (Buffs/Cura):** Foco em manter o time vivo e aplicar buffs de status.

* **Controle (Crowd Control - CC):** Especialistas em atordoar, congelar, cegar ou prender múltiplos inimigos.

## 4. Sistema de Party e Clans (Foco Multiplayer)

* **Sistema de Party (Grupos):** Prevenção de *Friendly Fire*, HUD customizado (vida/mana dos membros), compartilhamento de EXP e drops em instâncias.

* **Sistema de Clans (Guildas):** Tags customizadas, chat global exclusivo, e infraestrutura para futuras guerras de território (GvG).

## 5. Documentação de Movesets e Passivas (Kits de Habilidades)

**REGRA GERAL:** Todos os personagens possuem **1 Habilidade Passiva**. O combate utiliza o mapeamento **(Z, X, C, V)** para as habilidades base e a tecla **(G)** para transformações.

### 1. Subaru Natsuki (Re:Zero) - *Suporte / Controle*
* **Passiva (Return by Death):** Ao morrer, retorna a um checkpoint salvo há 3 minutos. (Longo Cooldown).
* **Base:** [Z] Shamak (Cegueira), [X] Chamado Espiritual, [C] Instrução Tática, [V] Recuperação.
* **[G] Contrato com a Beatrice** (decidido em 05/11/2026; substitui a Autoridade da Preguiça): [Z] Minya, [X] E.M.M., [C] Murak, [V] Ul Minya.

### 2. Killua Zoldyck (HxH) - *DPS (Velocidade)*
* **Passiva (Imunidade Zoldyck):** Resistência natural a venenos e eletricidade.
* **Base:** [Z] Palma Relâmpago, [X] Passo Borrão, [C] Yo-Yo de Aço, [V] Garras Assassinas.
* **[G] Godspeed (Kanmuru):** [Z] Relâmpago Perfurante, [X] Whirlpool (Contra-ataque), [C] Soco Relâmpago, [V] Pulso Eletromagnético.

### 3. Guts (Berserk) - *Tank / DPS*
* **Passiva (Vontade Indomável):** Dano e resistência aumentam com HP baixo.
* **Base:** [Z] Corte Dragon Slayer, [X] Tiro de Canhão, [C] Investida, [V] Guarda de Ferro.
* **[G] Armadura Berserker:** [Z] Fúria Cega, [X] Salto Esmagador, [C] Drenagem de Sangue, [V] Rugido Aterrorizante.

### 4. Douma (Demon Slayer) - *Controle*
* **Passiva (Névoa de Lótus):** Inimigos próximos recebem dano de gelo lento.
* **Base:** [Z] Lótus de Gelo, [X] Sopro Congelante, [C] Dança Cortante, [V] Muralha Glacial.
* **[G] Bodhisattva de Cristal:** [Z] Sopro do Bodhisattva, [X] Clone Protetor, [C] Jardim Mortífero, [V] Névoa Necrótica.

### 5. Rudeus Greyrat (Mushoku Tensei) - *DPS / Controle*
* **Passiva (Olho do Demônio):** Recebe avisos de ataques mágicos.
* **Base:** [Z] Bala de Pedra, [X] Névoa D'Água, [C] Parede de Terra, [V] Rajada de Vento.
* **[G] Modo Imperador da Magia:** [Z] Lança de Gelo Colossal, [X] Relâmpago Cumulus, [C] Canhão de Rocha, [V] Tornado de Chamas.

### 6. Ichigo Kurosaki (Bleach) - *DPS (Burst)*
* **Passiva (Pressão Espiritual):** Reduz dano mágico sofrido.
* **Base:** [Z] Getsuga Tenshou, [X] Shunpo, [C] Golpe Cruzado, [V] Liberação de Reiatsu.
* **[G] Bankai/Mugetsu:** [Z] Getsuga Negro, [X] Carga Sombria, [C] Getsuga Jujisho, [V] Mugetsu (Hit-Kill condicional).

### 7. Cid Kagenou (Eminence in Shadow) - *DPS / Controle*
* **Passiva (Ocultação):** Agachar fora de combate concede invisibilidade.
* **Base:** [Z] Lâmina de Slime, [X] Agulhas Sombrias, [C] Reflexo Rápido, [V] Escudo de Slime.
* **[G] Shadow:** [Z] Fios de Sangria, [X] Teletransporte, [C] Domínio Púrpura, [V] I Am Atomic.

### 8. Dio Brando (JoJo) - *DPS / Controle*
* **Passiva (Vampirismo):** Ataques físicos curam durante a noite.
* **Base:** [Z] Arremesso de Facas, [X] Congelamento Flash, [C] Salto Vampírico, [V] Dreno de Sangue.
* **[G] The World:** [Z] Muda Muda Rush, [X] Za Warudo (Para o tempo), [C] Rolo Compressor, [V] Arremesso de Facas Parado.

### 9. Homura Akemi (Madoka) - *Controle / DPS*
* **Passiva (Anomalia Temporal):** Imune a efeitos de lentidão.
* **Base:** [Z] Disparo de Pistola, [X] Granada, [C] Escudo Buckler, [V] Salto com Recuo.
* **[G] Arsenal Pesado:** [Z] Lança-Foguetes AT4, [X] Metralhadora, [C] Minas Terrestres, [V] Stop Time Limitado.

### 10. Alucard (Hellsing) - *Tank / DPS*
* **Passiva (Restrição Zero):** Revive 1 vez automaticamente ao morrer na instância.
* **Base:** [Z] Disparo Casull, [X] Disparo Jackal, [C] Evadir em Morcegos, [V] Mordida Sanguinária.
* **[G] Nível de Restrição 0:** [Z] Cães de Baskerville, [X] Mar de Sangue, [C] Empalar (Espinhos do chão), [V] Frenesi Vampírico.

### 11. Rimuru Tempest (Slime) - *Suporte / DPS*
* **Passiva (Glutonaria Passiva):** Eliminar mobs restaura mana e pequena porcentagem de HP.
* **Base:** [Z] Lâmina de Água, [X] Teia de Aço, [C] Cura Majin, [V] Barreira Multicamadas.
* **[G] Lorde Demônio:** [Z] Megiddo (Raios solares focados), [X] Chamas Negras, [C] Ressurreição (Aliados em área), [V] Consumo Absoluto (Buraco negro).

### 12. Reinhard van Astrea (Re:Zero) - *Tank / Suporte*
* **Passiva (Proteção Divina):** Esquiva automaticamente do primeiro ataque sofrido a cada 10 segundos.
* **Base:** [Z] Golpe de Espada Simples, [X] Salto Aprimorado, [C] Aura Protetora, [V] Desarme.
* **[G] Espada do Dragão Reid:** [Z] Corte Quebra-Céus, [X] Avanço Inevitável, [C] Brilho Purificador (Cura debuffs da party), [V] Corte Celestial.

### 13. Luffy (One Piece) - *DPS / Controle*
* **Passiva (Corpo de Borracha):** Imunidade a dano de queda e armas de concussão leves.
* **Base:** [Z] Pistol, [X] Gatling Gun, [C] Rocket (Mobilidade), [V] Bazooka.
* **[G] Gear 5 (Nika):** [Z] Gigant (Esmagamento), [X] Raio Reflexivo (Borracha no ambiente), [C] Tambores da Libertação (Buff Party), [V] Pulo em Nuvem de Haki.

### 14. Goku (Dragon Ball) - *DPS (Burst)*
* **Passiva (Zenkai):** Reviver ou ser curado de 10% de HP concede bônus temporário de Dano.
* **Base:** [Z] Rajada de Ki, [X] Combo Corpo a Corpo, [C] Teletransporte Simples, [V] Kamehameha Rápido.
* **[G] Super Saiyajin:** [Z] Super Kamehameha, [X] Frenesi de Meteoros, [C] Transmissão Instantânea (Avançada), [V] Bomba Espiritual (Canalização longa).

### 15. Naruto Uzumaki (Naruto) - *DPS / Suporte*
* **Passiva (Vontade do Fogo):** Regeneração de vida passiva lenta fora de combate.
* **Base:** [Z] Rasengan, [X] Shuriken de Vento, [C] Jutsu Clone das Sombras, [V] Substituição (Esquiva).
* **[G] Kurama Link Mode:** [Z] Rasenshuriken Planetário, [X] Braços de Chakra, [C] Bijuudama, [V] Rugido da Raposa (Knockback).

### 16. Rudo (Gachiakuta) - *DPS (Corpo-a-corpo)*
* **Passiva (Jinka):** Aumenta o dano baseado na quantidade de lixo/itens descartáveis no inventário.
* **Base:** [Z] Soco Energizado, [X] Rasteira Suja, [C] Puxão com Correntes, [V] Esquiva Ágil.
* **[G] Despertar do Lixo:** [Z] Esmagamento de Sucata, [X] Tornado de Entulho, [C] Corrente Perfurante, [V] Golpe Catártico.

### 17. Sanji (One Piece) - *DPS (Velocidade)*
* **Passiva (Sky Walk):** Permite pulo duplo e controle superior no ar.
* **Base:** [Z] Chute Anti-Manner, [X] Flanchet Strike, [C] Party Table, [V] Cozinheiro (Cura passiva da party).
* **[G] Ifrit Jambe:** [Z] Boeuf Burst, [X] Chamas Azuis (Dano contínuo), [C] Combo Supersônico, [V] Hell Memories (Explosão AOE).

### 18. Shinra Kusakabe (Fire Force) - *DPS (Velocidade)*
* **Passiva (Pegadas de Fogo):** Aumenta a velocidade de movimento constantemente.
* **Base:** [Z] Chute Voador de Fogo, [X] Propulsão, [C] Breakdance de Chamas, [V] Recuo Tático.
* **[G] Adolla Burst:** [Z] Chute à Velocidade da Luz, [X] Corna (Impacto Frontal), [C] Rapidez Relativa (Desvia de tudo por 3s), [V] Hellfire.

### 19. Zaraki Kenpachi (Bleach) - *Tank / DPS*
* **Passiva (Sede de Sangue):** Ignora passivamente escudos fracos e ganha bônus de dano ao levar hits.
* **Base:** [Z] Corte Bruto, [X] Pisão Esmagador, [C] Investida Temerária, [V] Grito de Batalha (Taunt).
* **[G] Shikai (Nozarashi):** [Z] Fenda Espacial (Quebra terreno invisível), [X] Terremoto de Pressão, [C] Corte Limpo (Ignora armadura), [V] Fúria do Demônio.

### 20. Kisuke Urahara (Bleach) - *Suporte / Controle*
* **Passiva (Estrategista):** Reduz o cooldown de suas habilidades em 10%.
* **Base:** [Z] Disparo Kido, [X] Escudo de Sangue (Bakudo), [C] Armadilha Espacial, [V] Fio de Energia.
* **[G] Bankai (Kannonbiraki Benihime):** [Z] Reestruturação (Cura um aliado em 100%), [X] Desconstrução (Quebra a armadura do Boss), [C] Campo Cirúrgico, [V] Linha de Sangue.

### 21. Indra Otsutsuki (Naruto) - *DPS / Controle*
* **Passiva (Genialidade Uchiha):** Aumenta o dano de habilidades em sequência (Combo).
* **Base:** [Z] Bola de Fogo, [X] Chidori Simples, [C] Clone Relâmpago, [V] Ilusão Sharigan (Stun).
* **[G] Susanoo Perfeito:** [Z] Flecha de Indra, [X] Espada Descomunal, [C] Tempestade de Raios, [V] Defesa Absoluta.

### 22. Ashura Otsutsuki (Naruto) - *Tank / Suporte*
* **Passiva (Vitalidade Senju):** Bônus natural de Vida Máxima (+5 corações).
* **Base:** [Z] Esfera de Vento, [X] Punho de Madeira, [C] Cura Rápida, [V] Barreira de Raízes.
* **[G] Avatar de Madeira:** [Z] Mil Mãos (Combo), [X] Dragão de Madeira, [C] Área da Vida (Regen constante na party), [V] Esmagamento Divino.

### 23. Ace / Sabo (One Piece) - *DPS (Área)*
* **Passiva (Intangibilidade Flamejante):** Imune a projéteis físicos básicos.
* **Base:** [Z] Hiken (Punho de Fogo), [X] Higan (Tiros de fogo), [C] Pilar de Chamas, [V] Mobilidade Propulsora.
* **[G] Entei (Imperador das Chamas):** [Z] Jujika (Cruz de fogo), [X] Mar de Chamas (AOE contínuo), [C] Parede de Fogo, [V] Dai Enkai: Entei (Sol gigante).

### 24. Kizaru (One Piece) - *DPS (Projéteis)*
* **Passiva (Velocidade da Luz):** Ignora restrições de lentidão e tem mobilidade base maior.
* **Base:** [Z] Disparo Laser (Dedo), [X] Yata no Kagami (Reflexão), [C] Chute na Velocidade da Luz, [V] Flash Cegante.
* **[G] Espada de Luz (Ama no Murakumo):** [Z] Yasakani no Magatama (Chuva de Lasers), [X] Corte de Luz, [C] Teleporte Luminoso, [V] Supernova (Explosão).

### 25. Mashle (Mash) - *DPS / Tank*
* **Passiva (Física > Magia):** Total imunidade a danos mágicos indiretos ou debuffs.
* **Base:** [Z] Soco Reto, [X] Arremesso de Varão de Ferro, [C] Passo Deslizante, [V] Reflexo de Músculo.
* **[G] Modo Triceps/Músculos Limitless:** [Z] Metralhadora de Socos, [X] Suplex no Boss, [C] Flexão Tática (Regenera vida), [V] Chute Destruidor de Arenas.

### 26. Mob (Shigeo) - *Controle / DPS*
* **Passiva (Barreira Psíquica):** Escudo regenerativo que bloqueia primeiros 10% do dano.
* **Base:** [Z] Empurrão Telecinético, [X] Levitar Bloco, [C] Escudo Bolha, [V] Flutuação.
* **[G] Explosão 100%:** [Z] Esmagamento Telecinético, [X] Tempestade de Detritos, [C] Anulação Mágica (Área), [V] Fúria Emocional.

### 27. Saitama (One Punch Man) - *DPS (One-Shot)*
* **Passiva (Treinamento Inabalável):** Completamente imune a todos os status negativos.
* **Base:** [Z] Soco Normal, [X] Passo Lateral (Esquiva), [C] Pulo Normal, [V] Tédio (Boceja).
* **[G] Série Séria:** [Z] Soco Sério (Hit-kill ou dano máximo do jogo), [X] Pulos Laterais Sérios (Stun em área), [C] Virada de Mesa Séria (Altera terreno), [V] Cabeçada Séria.

### 28. Levi Ackerman (AOT) - *DPS (Mobilidade)*
* **Passiva (Agilidade Ackermann):** Aumenta o dano atacando por trás ou caindo.
* **Base:** [Z] Corte Rápido, [X] Disparo Gancho DMT, [C] Recarga de Lâminas, [V] Impulso a Gás.
* **[G] Fúria Giratória:** [Z] Pião Mortal, [X] Múltiplos Ganchos, [C] Cegar Alvo (Mira nos olhos), [V] Retalhamento Aéreo.

### 29. Eren Yeager (AOT) - *Tank (Transformação)*
* **Passiva (Cura de Titã):** Regenera membros e vida gradualmente.
* **Base:** [Z] Corte de Espada DMT, [X] Disparo Gancho, [C] Mordida na Mão (Causa dano a si).
* **[G] Titã de Ataque:** [Z] Soco Esmagador, [X] Pisão Esmaga-Cidades, [C] Rugido de Coordenada (Stun em Mobs), [V] Endurecimento (Escudo impenetrável).

### 30. To Be Hero X - *DPS / Suporte*
* **Passiva (Imprevisível):** Atributos aleatórios são buffados a cada minuto.
* **Base:** [Z] Golpe Heroico, [X] Pose Dinâmica (Buff), [C] Projétil Estranho, [V] Fuga Comédia.
* **[G] Modo Herói Verdadeiro:** [Z] Soco Justiça, [X] Barreira de Resgate, [C] Inspiração (Cura Party), [V] Raio X de Combate.

### 31. Yosuke Hanamura (Persona 4) - *DPS / Controle*
* **Passiva (Custo de SP):** Habilidades consomem mana, mas tem baixo cooldown.
* **Base:** [Z] Corte de Kunai, [X] Garu (Vento), [C] Sukukaja (Buff Velocidade), [V] Dash Ágil.
* **[G] Persona Jiraiya:** [Z] Garudyne (Tornado), [X] Magarula (Vento AOE), [C] Dekaja (Remove buffs inimigos), [V] Brave Blade.

### 32. Shigaraki Tomura (MHA) - *Controle (Letal)*
* **Passiva (Decay Lento):** Ataques básicos corroem a armadura do alvo gradualmente.
* **Base:** [Z] Toque da Morte, [X] Investida Aterradora, [C] Nuvem de Poeira, [V] Arranhão.
* **[G] Despertar (All For One):** [Z] Onda de Deterioração (Quebra chão), [X] Regeneração Extrema, [C] Roubo de Quirk (Silencia alvo), [V] Fúria Destrutiva.

### 33. Katsuki Bakugou (MHA) - *DPS (Área)*
* **Passiva (Suor Explosivo):** Quanto mais tempo em combate, maior o raio de explosão.
* **Base:** [Z] Explosão Frontal, [X] Propulsão Explosiva, [C] AP Shot (Perfurante), [V] Stun Grenade.
* **[G] Cluster / Howitzer Impact:** [Z] Metralhadora de Explosões, [X] Tiro Concentrado Máximo, [C] Giro de Fogo, [V] Howitzer Impact (Nuke).

### 34. Shoto Todoroki (MHA) - *Controle / DPS*
* **Passiva (Regulação Térmica):** Imune a dano de fogo e efeitos de congelamento.
* **Base:** [Z] Lança de Gelo, [X] Onda de Fogo, [C] Parede de Gelo, [V] Deslize no Gelo.
* **[G] Flashfire Fist:** [Z] Heaven-Piercing Ice Wall, [X] Prominence Burn, [C] Expansão Congelante, [V] Explosão Térmica Híbrida.

### 35. Rukia Kuchiki (Bleach) - *Controle*
* **Passiva (Reiatsu Fria):** Inimigos próximos ficam gradualmente mais lentos.
* **Base:** [Z] Tsukishiro (Pilar Glacial), [X] Hakuren (Avalanche), [C] Shunpo, [V] Kido de Restrição.
* **[G] Bankai (Hakka no Togame):** [Z] Congelamento Absoluto (Zero absoluto em área gigante), [X] Lâmina de Gelo, [C] Escudo de Frio, [V] Nevasca Mortífera.

### 36. Esdeath (Akame ga Kill) - *Controle / DPS*
* **Passiva (Marca do Demônio):** Aumenta o dano de acerto crítico.
* **Base:** [Z] Estacas de Gelo, [X] Espada de Rapieira, [C] Cavalaria Glacial, [V] Chute de Submissão.
* **[G] Mahapadma:** [Z] Chuva de Meteoros de Gelo, [X] Parede Inquebrável, [C] Zero Absoluto, [V] Mahapadma (Para o tempo e movimentos globais por 6s).

### 37. Shikamaru Nara (Naruto) - *Controle / Suporte*
* **Passiva (Estratégia 200 QI):** Destaca o ponto fraco de chefes na interface do grupo.
* **Base:** [Z] Kunai Bomba, [X] Costura das Sombras, [C] Fuga Estratégica, [V] Armadilha de Arame.
* **[G] Imitação das Sombras:** [Z] Prisão das Sombras (Trava o boss completamente), [X] Estrangulamento, [C] Enforcamento das Sombras, [V] Formação Ino-Shika-Cho (Buff massivo no grupo).

### 38. Escanor (7 Deadly Sins) - *Tank / DPS*
* **Passiva (Sol da Manhã):** Status multiplicados durante o dia (noite no jogo reduz poder drasticamente).
* **Base:** [Z] Golpe de Machado (Rhitta), [X] Onda de Calor, [C] Salto Majestoso, [V] Postura do Leão.
* **[G] The One:** [Z] Cruel Sun (Mini sol), [X] Super Slash, [C] Ignora Magia, [V] Divine Sword Escanor (Corte de fogo puro).

### 39. King (7 Deadly Sins) - *Controle / DPS*
* **Passiva (Rei das Fadas):** Levita ao invés de andar, imune a armadilhas de chão.
* **Base:** [Z] Chastiefol Forma 1 (Lança), [X] Forma 2 (Guardião - Tank), [C] Forma 5 (Aumento), [V] Cura de Fada.
* **[G] Forma Verdadeira Chastiefol:** [Z] Sunflower (Raio Solar), [X] Luminosity, [C] Pólen de Cura (Party), [V] Chuva de Lanças Cortantes.

### 40. Meliodas (7 Deadly Sins) - *DPS / Tank*
* **Passiva (Sangue Demoníaco):** Regenera vida mais rápido ao perder corações.
* **Base:** [Z] Corte Rápido, [X] Full Counter (Reflete habilidades), [C] Chamas Negras, [V] Clone (Lostvayne).
* **[G] Assault Mode:** [Z] Hellblaze (Fogo inextinguível), [X] Trillion Dark, [C] Full Counter Aprimorado, [V] Revenge Counter (Absorve dano por 3s e devolve em dobro).

### 41. Aoi Todo (Jujutsu Kaisen) - *Suporte / Tank*
* **Passiva (Físico Excepcional):** Dano base alto em ataques desarmados.
* **Base:** [Z] Soco Direto, [X] Chute Baixo, [C] Defesa Cruzada, [V] Provocação.
* **[G] Boogie Woogie:** [Z] Palma Trocadora (Troca de lugar com alvo/aliado), [X] Black Flash (Condicional), [C] Troca de Aggro, [V] Combo de Best Friend.

### 42. Megumi Fushiguro (Jujutsu) - *Suporte / DPS*
* **Passiva (Energia Amaldiçoada):** Recarga de mana contínua aumentada nas sombras.
* **Base:** [Z] Cães Divinos (Atacam), [X] Nue (Raio aéreo), [C] Sapo (Puxa aliados/inimigos), [V] Fuga Sombria.
* **[G] Expansão de Domínio (Chimera Shadow Garden):** [Z] Elefante Max (Esmagamento de água), [X] Clone de Sombra, [C] Ocultação Perfeita, [V] Invocação: Mahoraga (Invocação de boss aliado temporário).

### 43. Sukuna (Jujutsu) - *DPS (Área)*
* **Passiva (Rei das Maldições):** Imunidade a veneno e medo.
* **Base:** [Z] Dismantle (Corte rápido), [X] Cleave (Ajusta dano à defesa), [C] Agarre Veloz, [V] Regeneração Reversa.
* **[G] Malevolent Shrine:** [Z] Chuva de Cortes (Cleave em área infinita), [X] Flecha de Fogo (Fuga), [C] Pisar no Inimigo, [V] Desmembramento Completo.

### 44. Yuta Okkotsu (Jujutsu) - *DPS / Suporte*
* **Passiva (Cópia Ilimitada):** Copia a última habilidade usada por aliado/inimigo e atribui à tecla V.
* **Base:** [Z] Corte com Katana, [X] Energia Reversa (Cura Aliados), [C] Investida, [V] [Habilidade Copiada].
* **[G] Manifestação Completa (Rika):** [Z] Raio de Amor Puro, [X] Esmagamento de Rika, [C] Barreira Protetora, [V] Fúria da Maldição.

### 45. Mahito (Jujutsu) - *Controle / DPS*
* **Passiva (Mutação da Alma):** Ignora 50% do valor da armadura do inimigo.
* **Base:** [Z] Lâmina Mutante, [X] Cuspir Humanos Transfigurados, [C] Escudo de Carne, [V] Deslize de Lama.
* **[G] Idle Transfiguration:** [Z] Toque da Transfiguração (Hit Kill em mobs normais), [X] Espinho de Alma, [C] Isomer (Invocação Gigante), [V] Expansão de Domínio (Stun absoluto).

### 46. Denji (Chainsaw Man) - *DPS / Tank*
* **Passiva (Coração do Demônio):** Curar batendo e coletando sangue (esferas vermelhas) de mobs.
* **Base:** [Z] Corte de Motosserra Simples, [X] Puxão na Corda (Revive), [C] Chute Sujo, [V] Mordida.
* **[G] Modo Demônio da Motosserra:** [Z] Rotação Sangrenta, [X] Arrastão no Chão, [C] Correntes da Motosserra (Puxa boss), [V] Estripação.

### 47. Aki Hayakawa (Chainsaw) - *Controle / DPS*
* **Passiva (Tempo de Vida Reduzido):** O uso de habilidades especiais consome o HP máximo temporariamente.
* **Base:** [Z] Corte de Katana, [X] Tiro Simples, [C] Esquiva Focada, [V] Fumar (Recupera Mana).
* **[G] Contratos Demoníacos:** [Z] Kon (Demônio Raposa Esmaga), [X] Espada de Prego (Curse Demon - 3 hits = morte), [C] Previsão (Demônio do Futuro - Evita 3 ataques), [V] Golpe Preciso.

### 48. Makima (Chainsaw) - *Controle / Suporte*
* **Passiva (Controle Absoluto):** Repassa qualquer dano recebido para um mob menor aleatório na área (se houver).
* **Base:** [Z] Disparo Bang, [X] Empurrão Invisível, [C] Olhar Intimidador (Medo), [V] Marcha Graciosa.
* **[G] Dominação:** [Z] Esmagamento Aéreo (Shrine), [X] Invocação de Escravos, [C] Controle de Mente (Vira mobs contra o boss), [V] Disparo Bang Concentrado.

### 49. Edward Elric (FMA) - *Suporte / Controle*
* **Passiva (Sem Círculos):** Conjuração instantânea.
* **Base:** [Z] Lança de Pedra, [X] Espada de Automail, [C] Parede de Terra, [V] Prender Pés (Gaiola de Pedra).
* **[G] Alquimia Mestre:** [Z] Canhão de Pedra Terrestre, [X] Chuva de Lanças, [C] Mão Gigante do Chão, [V] Transmutação de Área (Muda tipo do bloco para atrasar inimigos).

### 50. Kokushibo (Demon Slayer) - *DPS (Alcance)*
* **Passiva (Visão Transparente):** Ignora chance de esquiva do alvo.
* **Base:** [Z] Corte Meia-Lua (Projétil), [X] Avanço Lunar, [C] Lâminas Rodopiantes, [V] Reflexos Afiados.
* **[G] Respiração da Lua (Formas Superiores):** [Z] Turbilhão Lunar, [X] Corte Catastrófico de Várias Lâminas, [C] Lâmina Mutante (Crescimento), [V] Tornado da Lua Cheia.

### 51. Muzan Kibutsuji (Demon Slayer) - *Tank / Controle*
* **Passiva (Regeneração de Rei Demônio):** A vida se regenera quase instantaneamente (exceto dano solar).
* **Base:** [Z] Chicotes de Carne, [X] Sangue Tóxico (Envenenamento), [C] Teleporte nas Sombras, [V] Absorção.
* **[G] Forma de Combate Monstruosa:** [Z] Turbilhão de Tentáculos (Em área), [X] Onda de Choque de Sangue, [C] Veneno de Sangue Demoníaco (CC), [V] Sugador de Energia.

### 52. Madara Uchiha (Naruto) - *DPS / Tank*
* **Passiva (Edo Tensei/Chakra Ilimitado):** Regeneração rápida de mana/chakra.
* **Base:** [Z] Katon (Aniquilação de Fogo), [X] Susanoo Costelas (Defesa), [C] Taijutsu Violento, [V] Gumbai (Reflete ataque).
* **[G] Susanoo Perfeito & Rinnegan:** [Z] Corte das Espadas de Susanoo, [X] Tengai Shinsei (Invoca Meteoro - Boss Nuke), [C] Limbo (Clone invisível ataca), [V] Chibaku Tensei.

### 53. Obito Uchiha (Naruto) - *Controle*
* **Passiva (Células de Hashirama):** Alta resistência física.
* **Base:** [Z] Katon: Tempestade Dançante, [X] Lançamento de Shuriken Gigante, [C] Kamui (Fica intangível por 3s), [V] Estacas de Madeira.
* **[G] Kamui Avançado / Máscara:** [Z] Sugador Kamui (Teleporta mobs pro Void), [X] Deslize Intangível (Passa por paredes), [C] Correntes da Estátua Gedo, [V] Ilusão em Área.

### 54. Kakashi Hatake (Naruto) - *DPS / Suporte*
* **Passiva (Sharingan Copiador):** Aumenta chance de evasão após ver o chefe atacar 3 vezes.
* **Base:** [Z] Chidori/Raikiri, [X] Cão Invocado, [C] Dragão de Água, [V] Parede de Terra.
* **[G] Mangekyou Sharingan:** [Z] Raiden (Cabo de raio), [X] Kamui Shuriken, [C] Kamui Sniper (Deleta pedaço da vida do boss), [V] Susanoo Perfeito (Armadura).

### 55. Kirito (SAO) - *DPS (Burst)*
* **Passiva (Reação Acelerada):** Tempo de ataque base menor (bate muito rápido).
* **Base:** [Z] Vorpal Strike, [X] Sonic Leap, [C] Defesa de Espada, [V] Esquiva Rápida.
* **[G] Dual Wield (Starburst Stream):** [Z] Double Circular, [X] Eclipse, [C] Parier (Parry Perfeito = Stun), [V] Starburst Stream (Combo de 16 hits massivos no alvo).

### 56. Roy Mustang (FMA) - *DPS (Área)*
* **Passiva (Chamas Direcionadas):** Seus ataques de fogo não causam fogo amigo (Friendly Fire desativado passivamente na magia).
* **Base:** [Z] Estalo de Fogo Pequeno, [X] Parede de Fogo, [C] Ignitar, [V] Passo Tático.
* **[G] Alquimia de Fogo Contínua:** [Z] Incineração Constante, [X] Fogo nos Olhos (Cegueira instantânea), [C] Tempestade de Chamas (AOE gigante), [V] Estalo Mortal (Foco único massivo).

### 57. Sunraku (Shangri-La Frontier) - *DPS (Agilidade)*
* **Passiva (Gamer Hardcore):** Esquivas executadas no "frame perfeito" dão 100% de chance de crítico no próximo hit.
* **Base:** [Z] Corte Duplo Rápido, [X] Rolo de Esquiva, [C] Salto na Parede, [V] Agarre com Empurrão.
* **[G] Modo Foco Extremo:** [Z] Lâminas Gêmeas Velozes, [X] Parry Absoluto, [C] Passos Fantasmas (Atrás do inimigo), [V] Corte Crítico da Queda.

### 58. Tsuna Sawada (Hitman Reborn) - *DPS (Mobilidade)*
* **Passiva (Hiper Intuição):** Avisos na tela (partículas) antes de ataques pesados de boss.
* **Base:** [Z] X-Burner Mini, [X] Impulso de Chamas (Voo), [C] Soco Certeiro, [V] Absorção de Chamas.
* **[G] Hyper Shinuki Mode:** [Z] X-Burner (Massivo Nuke), [X] Zero Point Breakthrough (Congela mobs em gelo eterno), [C] Dança Aérea, [V] Vongola Primo's Mantle.

### 59. Itachi Uchiha (Naruto) - *Controle / DPS*
* **Passiva (Mestre de Ilusões):** Quando atacado há 20% de chance de o inimigo ter atacado corvos (nega o dano).
* **Base:** [Z] Clones de Corvo, [X] Bola de Fogo, [C] Shuriken Jutsu, [V] Tsukuyomi Simples (Paralisa).
* **[G] Susanoo e Amaterasu:** [Z] Amaterasu (Fogo negro, dano eterno até limpar o status), [X] Espada de Totsuka (Sela habilidades do Boss por 10s), [C] Espelho de Yata (Reflete 100% dano), [V] Yasaka Magatama.

### 60. Megumin (Konosuba) - *DPS (Nuke)*
* **Passiva (Amor às Explosões):** Buff global no dano da Explosion.
* **Base:** [Z] Golpe com Cajado (Fraco), [X] Conjuração Lenta, [C] Preparar Magia, [V] Pose Dramática.
* **[G] Magia Suprema:** [Z] EXPLOSION! (Causa o maior dano em área do mod inteiro. Porém, drena toda mana e o jogador fica caido (Stun de 10 segundos) imóvel no chão). [X, C, V] Bloqueados.

### 61. Isagi Yoichi (Blue Lock) - *Suporte (Visão)*
* **Passiva (Egoísta):** Dano aumenta se ele for o único atacando o Boss no momento.
* **Base:** [Z] Chute Direto, [X] Roubo de Bola (Desarme), [C] Passo Deslizante, [V] Foco.
* **[G] Metavision:** [Z] Chute Direto Absoluto, [X] Antecipação (Esquiva 100%), [C] Passe Tático (Buffa DPS do aliado), [V] Ponto Cego (Ataca pelas costas).

### 62. Kaiju No. 8 - *Tank / DPS*
* **Passiva (Corpo de Daikaiju):** Mitiga dano físico passivo em 30%.
* **Base:** [Z] Soco Pesado, [X] Grito Sonoro, [C] Salto Enorme, [V] Bloqueio Biológico.
* **[G] Liberação de Energia Máxima:** [Z] Soco Nuclear, [X] Uppercut Destruidor, [C] Esmagamento de Área, [V] Regeneração Extrema de Tecidos.

### 63. Mina Ashiro (Kaiju No.8) - *DPS (Sniper)*
* **Passiva (Sentido Anti-Kaiju):** Mobs hostis e Bosses ficam marcados na interface com contorno.
* **Base:** [Z] Disparo de Rifle Leve, [X] Mini-Foguete Guiado, [C] Recuo Tático, [V] Granada.
* **[G] Postura de Artilharia Pesada (Type-94):** [Z] Disparo Canhão de Íons (Perfura tudo em linha), [X] Fogo de Supressão, [C] Reforço de Posicionamento (Mais dano parada), [V] Fixar Mira (Crítico garantido).

### 64. Naofumi Iwatani (Shield Hero) - *Tank (Puro)*
* **Passiva (Herói do Escudo):** Impossibilitado de usar qualquer arma, mas o dano recebido é bloqueado em grande escala.
* **Base:** [Z] Escudo Aéreo (Bloqueia), [X] Escudo de Ataque (Mini empurrão), [C] Escudo de Fogo Refletor, [V] Cura Erval.
* **[G] Escudo da Ira:** [Z] Iron Maiden (Prende o boss e causa dano de sangramento), [X] Escudo de Duas Cabeças (Dano de maldição), [C] Cobertura de Aliado, [V] Dreno de Fúria (Provoca todos na área).

### 65. Lelouch vi Britannia (Code Geass) - *Suporte / Controle*
* **Passiva (Gênio Tático):** Aliados próximos regeneram mana 20% mais rápido.
* **Base:** [Z] Tiro de Pistola, [X] Ordem de Retirada (Move aliados rápido), [C] Analisar Ponto Fraco, [V] Escudo Knightmare.
* **[G] Geass:** [Z] "Morra!" (Hit kill em mobs comuns), [X] "Congele!" (Paralisa chefe por 5s), [C] "Vire-se!" (Força o chefe a atacar os próprios capangas), [V] Rei do Caos (Buff de 100% de ataque na party inteira por 8s).

### 66. Johnny Joestar (JoJo) - *DPS (Projéteis)*
* **Passiva (Giro Infinito):** Projéteis atravessam escudos menores.
* **Base:** [Z] Act 1 - Disparo de Unha Simples, [X] Act 2 - Buraco Rastreador, [C] Act 3 - Movimento pelo Buraco (Fuga), [V] Slow Horse Dash.
* **[G] Tusk Act 4:** [Z] Rotação Infinita (Dano verdadeiro contínuo), [X] Quebra-Barreiras Absoluta, [C] Chuva de Unhas Pesadas, [V] Esmagamento de Tusk.

### 67. Minato Namikaze (Naruto) - *DPS (Agilidade)*
* **Passiva (O Relâmpago Amarelo):** Sua velocidade base de caminhada/corrida é 30% maior que a média.
* **Base:** [Z] Arremesso de Kunai, [X] Hiraishin (Teleporta para a Kunai), [C] Rasengan, [V] Esquiva Instantânea.
* **[G] Modo de Chakra da Kyuubi:** [Z] Deus Voador do Trovão (Combo), [X] Super Rasengan, [C] Barreira Espaço-Tempo (Sugação de ataques de boss), [V] Garras de Chakra Velozes.

### 68. Enrico Pucci (JoJo) - *Controle / Suporte*
* **Passiva (Gravidade Sagrada):** Mobs a curta distância ficam mais pesados (lentidão).
* **Base:** [Z] Arremesso de CD, [X] Roubo de Disco (Silencia skill 1 alvo), [C] White Snake Ilusão, [V] Cura através de Disco.
* **[G] Made In Heaven:** [Z] Aceleração Extrema (Movimenta na velocidade da luz enquanto o ambiente pausa), [X] Corte Rápido em Massa, [C] Envelhecimento Rápido (Dano por segundo ao boss), [V] Reset do Universo (Traz todos da party de volta de morte certa, resetando a boss fight).

### 69. Magna Swing (Black Clover) - *DPS*
* **Passiva (Resistência Plebéia):** Dano recebido diminui 1% a cada hit até o fim do combate.
* **Base:** [Z] Bola de Fogo Rebatida, [X] Bastão de Fogo, [C] Home Run de Fogo, [V] Dash Fervente.
* **[G] Soul Chain Death Match (Magia de Fogo Secreta):** [Z] Corrente de Fogo (Liga a vida e mana de Magna à do Boss, equalizando o poder e quebrando super-armaduras), [X] Combo Insano de Bastão, [C] Rebatida Perfeita, [V] Nocaute Incendiário.

### 70. Luck Voltia (Black Clover) - *DPS (Velocidade)*
* **Passiva (Amor pela Batalha):** Ganha buff cumulativo de velocidade ao acertar hits consecutivos.
* **Base:** [Z] Esfera de Relâmpago, [X] Chute Elétrico, [C] Sentir Mana (Revela Mobs), [V] Esquiva de Raio.
* **[G] Armadura de Relâmpago Verdadeiro:** [Z] Flecha de Raio, [X] Investida Tempestuosa, [C] Velocidade de Deus dos Raios, [V] Garra do Leopardo Elétrico (Execução condicional).

### 71. Mereoleona Vermillion (Black Clover) - *DPS / Tank*
* **Passiva (Mana Selvagem):** Imunidade total a Knockback (Repulsão).
* **Base:** [Z] Soco de Fogo, [X] Pulo Esmagador, [C] Garra Flamejante, [V] Grito de Batalha (Stun menor).
* **[G] Mana Zone:** [Z] Calidoscópio de Chamas (Chuva de socos de fogo teleguiados), [X] Incineração Dupla, [C] Purgatório, [V] Fúria da Leoa.

### 72. Yuno Grinberryall (Black Clover) - *DPS / Suporte*
* **Passiva (Espírito do Vento):** Pulos maiores e flutuação natural ao cair.
* **Base:** [Z] Lâmina de Vento, [X] Ciclone Afastador, [C] Voo Controlado, [V] Escudo de Brisa (Aliados).
* **[G] Spirit Dive (Modo Coroa):** [Z] Spirit of Zephyr (Espada que não desfaz), [X] Spirit of Boreas (Dano em área maciço), [C] Flecha de Vento Sagrado, [V] Zona de Estrelas (Teleporte Tático Absoluto).

### 73. Yami Sukehiro (Black Clover) - *Tank / DPS*
* **Passiva (Ki Sensing):** Pode bloquear ou esquivar de ataques por trás automaticamente (Cooldown de 5s).
* **Base:** [Z] Corte Sombrio, [X] Buraco Negro (Puxa mobs), [C] Parede de Escuridão, [V] Intimidação (Olhar assustador).
* **[G] Magia de Escuridão Avançada / Katana:** [Z] Corte Dimensional (Divide o cenário e ignora qualquer tipo de defesa/escudo do jogo), [X] Zona de Mana (Acertos automáticos sem mirar), [C] Iai Sombrio, [V] Black Moon (Anula magias na área).

### 74. Julius Novachrono (Black Clover) - *Suporte / Controle*
* **Passiva (Tempo Acumulado):** Ganha "pontos de tempo" conforme caminha, usados para reviver instantaneamente (Cooldown longo).
* **Base:** [Z] Acelerar (Atira projétil de envelhecimento), [X] Paralisia Temporal Rápida, [C] Prever o Futuro (Aviso de hit no grupo), [V] Cura Temporal (Reverte feridas recentes).
* **[G] Rei Mago do Tempo:** [Z] Chrono Stasis (Prende o boss numa bolha de tempo por 8s), [X] Esfera Temporal Maciça, [C] Roubo de Tempo (Ganha vida curando dano da party), [V] Grande Reversão (Anula o último grande ataque sofrido pela Party).