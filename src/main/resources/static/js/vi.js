
(() => {
    const formulario = document.getElementById('viFormulario');
    const campo = document.getElementById('viMensagem');
    const conversa = document.getElementById('viConversa');
    const enviar = document.getElementById('viEnviar');
    const digitacao = document.getElementById('viDigitacao');

    const botaoMicrofone = document.getElementById('viMicrofone');
    const botaoRepetir = document.getElementById('viRepetir');
    const botaoParar = document.getElementById('viParar');
    const statusVoz = document.getElementById('viStatusVoz');

    if (!formulario || !campo || !conversa || !enviar) return;

    const csrf = document.querySelector('meta[name="csrf"]')?.content;

    // Reconhecimento de voz
    const Reconhecimento =
        window.SpeechRecognition || window.webkitSpeechRecognition;

    let reconhecimento = null;
    let ouvindo = false;
    let ultimaResposta = '';

    // Áudio neural da Vi
    let audioAtual = null;
    let urlAudioAtual = null;
    let requisicaoVoz = null;

    function informarStatus(texto) {
        if (statusVoz) statusVoz.textContent = texto;
    }

    function horarioAtual() {
        return new Intl.DateTimeFormat('pt-BR', {
            hour: '2-digit',
            minute: '2-digit'
        }).format(new Date());
    }

    function adicionarMensagem(texto, tipo, erro = false) {
        const mensagem = document.createElement('div');
        mensagem.className = `vi-mensagem vi-mensagem-${tipo}`;

        const bolha = document.createElement('div');
        bolha.className = 'vi-bolha';

        if (erro) bolha.classList.add('vi-erro');

        bolha.textContent = texto;

        const horario = document.createElement('span');
        horario.className = 'vi-horario';
        horario.textContent = horarioAtual();

        mensagem.append(bolha, horario);
        conversa.appendChild(mensagem);

        conversa.scrollTop = conversa.scrollHeight;
    }

    // Interrompe qualquer áudio ou geração anterior
    function pararFala() {
        if (requisicaoVoz) {
            requisicaoVoz.abort();
            requisicaoVoz = null;
        }

        if (audioAtual) {
            audioAtual.pause();
            audioAtual.currentTime = 0;
            audioAtual = null;
        }

        if (urlAudioAtual) {
            URL.revokeObjectURL(urlAudioAtual);
            urlAudioAtual = null;
        }

        informarStatus('Fala interrompida.');
    }

    // Prepara o áudio sem iniciar a reprodução
    async function prepararAudio(texto) {
        if (!texto || !texto.trim()) return null;

        pararFala();

        const controle = new AbortController();
        requisicaoVoz = controle;

        informarStatus('Preparando a voz da Vi...');

        try {
            const resposta = await fetch('/idoso/vi/voz', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(csrf ? { 'X-CSRF-TOKEN': csrf } : {})
                },
                body: JSON.stringify({ texto }),
                signal: controle.signal
            });

            if (!resposta.ok) {
                let mensagem = 'Não foi possível gerar a voz da Vi.';

                try {
                    const dados = await resposta.json();
                    mensagem = dados.erro || mensagem;
                } catch (_) {}

                throw new Error(mensagem);
            }

            const arquivo = await resposta.blob();

            if (controle.signal.aborted) return null;

            requisicaoVoz = null;

            urlAudioAtual = URL.createObjectURL(arquivo);
            audioAtual = new Audio(urlAudioAtual);

            const audio = audioAtual;

            audio.onplay = () => {
                informarStatus('A Vi está falando...');
            };

            audio.onended = () => {
                if (audioAtual !== audio) return;

                audioAtual = null;

                if (urlAudioAtual) {
                    URL.revokeObjectURL(urlAudioAtual);
                    urlAudioAtual = null;
                }

                informarStatus('A Vi terminou de falar.');
            };

            audio.onerror = () => {
                if (audioAtual !== audio) return;

                audioAtual = null;

                informarStatus('Não foi possível reproduzir o áudio.');
            };

            return audio;

        } catch (erro) {
            if (erro.name === 'AbortError') return null;

            console.error('[VitaSense] Erro na voz:', erro);

            informarStatus(
                erro.message || 'Não foi possível reproduzir a voz.'
            );

            requisicaoVoz = null;
            return null;
        }
    }

    // Inicia a reprodução do áudio já preparado
    function reproduzirAudio(audio) {
        if (!audio) return;

        audio.play().catch(erro => {
            console.error('[VitaSense] Erro ao iniciar áudio:', erro);
            informarStatus('Não foi possível iniciar a reprodução da voz.');
        });
    }

    // Repetir uma resposta
    async function falar(texto) {
        const audio = await prepararAudio(texto);

        if (audio) {
            reproduzirAudio(audio);
        }
    }

    // Configura o reconhecimento de voz
    if (Reconhecimento) {
        reconhecimento = new Reconhecimento();

        reconhecimento.lang = 'pt-BR';
        reconhecimento.continuous = false;
        reconhecimento.interimResults = false;
        reconhecimento.maxAlternatives = 1;

        reconhecimento.onstart = () => {
            ouvindo = true;
            botaoMicrofone?.classList.add('vi-ouvindo');
            botaoMicrofone?.setAttribute('aria-label', 'Parar de ouvir');

            informarStatus('Estou ouvindo. Pode falar!');
        };

        reconhecimento.onresult = evento => {
            const texto = evento.results[0][0].transcript;

            if (texto && texto.trim()) {
                campo.value = texto.trim();
                informarStatus('Enviando sua mensagem...');
                formulario.requestSubmit();
            }
        };

        reconhecimento.onerror = evento => {
            const mensagens = {
                'not-allowed': 'Permita o acesso ao microfone no navegador.',
                'no-speech': 'Não ouvi sua voz. Tente novamente.',
                'audio-capture': 'Não encontrei um microfone disponível.',
                'network': 'Falha na conexão durante o reconhecimento de voz.'
            };

            informarStatus(
                mensagens[evento.error] ||
                'Não consegui reconhecer sua voz.'
            );
        };

        reconhecimento.onend = () => {
            ouvindo = false;

            botaoMicrofone?.classList.remove('vi-ouvindo');
            botaoMicrofone?.setAttribute('aria-label', 'Falar com a Vi');

            if (statusVoz?.textContent === 'Estou ouvindo. Pode falar!') {
                informarStatus('Microfone desligado.');
            }
        };

    } else if (botaoMicrofone) {
        botaoMicrofone.disabled = true;

        informarStatus(
            'Seu navegador não oferece reconhecimento de voz.'
        );
    }

    // Botão do microfone
    botaoMicrofone?.addEventListener('click', () => {
        if (!reconhecimento) {
            informarStatus(
                'Reconhecimento de voz não disponível neste navegador.'
            );
            return;
        }

        if (ouvindo) {
            reconhecimento.stop();
            return;
        }

        pararFala();

        try {
            reconhecimento.start();
        } catch (erro) {
            informarStatus(
                'Não foi possível iniciar o microfone. Tente novamente.'
            );
        }
    });

    // Repetir a última resposta
    botaoRepetir?.addEventListener('click', () => {
        if (ultimaResposta) {
            falar(ultimaResposta);
        } else {
            informarStatus('Ainda não há uma resposta para repetir.');
        }
    });

    // Interromper a fala
    botaoParar?.addEventListener('click', pararFala);

    // Envio de mensagens
    formulario.addEventListener('submit', async evento => {
        evento.preventDefault();

        const mensagem = campo.value.trim();

        if (!mensagem || enviar.disabled) return;

        pararFala();

        adicionarMensagem(mensagem, 'usuario');

        campo.value = '';
        enviar.disabled = true;
        campo.disabled = true;

        if (botaoMicrofone) botaoMicrofone.disabled = true;
        if (digitacao) digitacao.hidden = false;

        conversa.scrollTop = conversa.scrollHeight;

        try {
            const resposta = await fetch('/idoso/vi/conversar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(csrf ? { 'X-CSRF-TOKEN': csrf } : {})
                },
                body: JSON.stringify({ mensagem })
            });

            const dados = await resposta.json();

            if (!resposta.ok) {
                throw new Error(
                    dados.erro || 'Não foi possível enviar a mensagem.'
                );
            }

            const textoResposta =
                dados.resposta ||
                'Não consegui preparar uma resposta agora.';

            ultimaResposta = textoResposta;

            // Primeiro prepara a voz; depois mostra o texto e reproduz.
            const audio = await prepararAudio(textoResposta);

            adicionarMensagem(textoResposta, 'assistente');

            if (audio) {
                reproduzirAudio(audio);
            }

        } catch (erro) {
            adicionarMensagem(
                erro.message ||
                'Não consegui conversar agora. Tente novamente.',
                'assistente',
                true
            );

            informarStatus('Não foi possível obter uma resposta.');

        } finally {
            if (digitacao) digitacao.hidden = true;

            enviar.disabled = false;
            campo.disabled = false;

            if (botaoMicrofone && reconhecimento) {
                botaoMicrofone.disabled = false;
            }

            campo.focus();
        }
    });
})();
