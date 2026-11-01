# SFX originais de anime

Dez efeitos sintetizados para o projeto por `tools/build_anime_sfx.py`: osciladores com varredura de frequência, ruído filtrado, transientes, ressonâncias e envelopes. Os vídeos enviados orientam a composição visual; o áudio destes arquivos foi gerado matematicamente.

Formato de runtime: Ogg Vorbis, mono, 48 kHz. Mono permite atenuação espacial. As legendas estão em `pt_br.json` e `en_us.json`; os eventos e distâncias estão em `sounds.json`. `anime-sfx-manifest.json` registra duração decodificada, pico, RMS e SHA-256 de cada arquivo.

Ferramentas de autoria usadas: Python 3.12, numpy 2.5.4, soundfile 0.14.0. São ferramentas externas de autoria, não novas dependências Java ou mods necessários para jogar. Não regenerar assets durante uma simples execução de testes.

```text
python tools/build_anime_sfx.py --preview build/anime-sfx-preview.wav
```

O gerador decodifica e verifica os arquivos após escrevê-los. O PCM de origem é reproduzível pela semente de cada nome; o contêiner Ogg pode receber metadados/identificadores diferentes entre gerações. O manifesto identifica os bytes efetivamente entregues.

O ganho adicional padrão é 0,7, ajustável em `volumeSfxAnime`; `sfxAnime` desliga a camada nova. O mixer do jogo ainda aplica Master/Jogadores. Ouvir o arquivo de prévia fora do Minecraft não confirma a mixagem com os sons vanilla, a posição no mundo ou o comportamento em multiplayer.
