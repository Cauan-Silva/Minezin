# Comandos administrativos de teste — preview.3

Exigem cheats habilitados ou operador com nível de permissão 2. Executam no próprio jogador; o console deve executar via `execute as <jogador> run ...`.

| Comando | Resultado |
| --- | --- |
| `/minezin rpg maestria maxima` | Kit ativo no nível 100, árvore completa e despertar liberado. |
| `/minezin rpg maestria maxima gojo` | Mesmo resultado para Gojo; aceita também `cid`, `frieren` e `asta`. |
| `/minezin rpg maestria maxima todos` | Desbloqueia e maximiza os quatro kits, mantendo a seleção atual. |
| `/minezin rpg restaurar_recursos` | Mana 100 e recargas RPG zeradas em todos os kits, nas formas normal e transformada. |

A maestria máxima fica salva no perfil RPG e é sincronizada com a interface. Os kits indicados são desbloqueados sem consumir materiais ou almas. O comando não ativa a transformação, não fornece armas/armaduras e não preenche vida. Selecione em P e utilize G para despertar; requisitos de arma continuam valendo. Os contadores de uso da progressão legada não são alterados.

Restaurar recursos não cancela técnicas em execução, não remove silêncio/antimagia nem encerra o tempo de ocupação ou de transformação. Aguarde a ação terminar para repetir. A progressão survival normal mantém seus requisitos; somente a execução explícita dos comandos concede os benefícios.

Validação: build Forge e teste de perfil máximo/releitura; comandos ainda precisam de execução dentro do Minecraft, incluindo permissões, reconexão e seleção entre kits. Sem commit, push ou PR nesta entrega.
