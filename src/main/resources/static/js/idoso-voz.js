
(() => {
    const botao = document.getElementById('ativarVoz');
    const texto = document.getElementById('textoVoz');
    const dialogo = document.getElementById('dlgVozEmergencia');
    const fechar = document.getElementById('fecharVoz');

    if (!botao || !texto || !dialogo || !fechar) return;
    if (botao.dataset.iniciado) return;

    botao.dataset.iniciado = 'true';

    const SpeechRecognition =
        window.SpeechRecognition || window.webkitSpeechRecognition;

    let reconhecimento;
    let ativo = false;
    let reiniciar = false;
    let temporizador = null;
    let temporizadorInicializacao = null;
    let escutando = false;

    const expressoes = [
        /preciso de ajuda/,
        /preciso de socorro/,
        /me ajuda/,
        /me ajude/,
        /socorro/,
        /estou passando mal/,
        /estou em perigo/,
        /quero ajuda/
    ];

    function atualizarBotao(mensagem) {
        botao.setAttribute('aria-pressed', String(ativo));
        botao.classList.toggle('id-voz-ativo', ativo);

        texto.textContent = mensagem || (
            ativo ? 'Ajuda por voz ativada' : 'Ativar ajuda por voz'
        );
    }

    function limparTemporizadores() {
        clearTimeout(temporizador);
        clearTimeout(temporizadorInicializacao);
    }

    function pararEscuta() {
        ativo = false;
        reiniciar = false;
        escutando = false;

        limparTemporizadores();

        try {
            reconhecimento.stop();
        } catch (erro) {
            console.log('[VitaSense] Escuta já encerrada.');
        }
    }

    if (!SpeechRecognition) {
        botao.disabled = true;
        atualizarBotao('Voz indisponível neste navegador');
        return;
    }

    reconhecimento = new SpeechRecognition();

    reconhecimento.lang = 'pt-BR';
    reconhecimento.continuous = true;
    reconhecimento.interimResults = false;
    reconhecimento.maxAlternatives = 3;

    function iniciarEscuta() {
        if (!ativo || dialogo.open || escutando) return;

        limparTemporizadores();

        try {
            console.log('[VitaSense] Solicitando início do reconhecimento...');

            atualizarBotao('Iniciando microfone...');

            reconhecimento.start();

            temporizadorInicializacao = setTimeout(() => {
                if (ativo && !escutando) {
                    console.error(
                        '[VitaSense] O navegador não confirmou o início do reconhecimento.'
                    );

                    pararEscuta();
                    atualizarBotao('O navegador não iniciou a escuta');
                }
            }, 8000);

        } catch (erro) {
            console.error('[VitaSense] Falha ao iniciar:', erro);

            pararEscuta();
            atualizarBotao('Falha ao iniciar o reconhecimento');
        }
    }

    reconhecimento.onstart = () => {
        clearTimeout(temporizadorInicializacao);

        escutando = true;

        console.log('[VitaSense] Reconhecimento iniciado com sucesso.');

        atualizarBotao('Estou ouvindo...');
    };

    reconhecimento.onaudiostart = () => {
        console.log('[VitaSense] Captura de áudio iniciada.');
    };

    reconhecimento.onspeechstart = () => {
        console.log('[VitaSense] Voz detectada.');
    };

    reconhecimento.onresult = evento => {
        for (let i = evento.resultIndex; i < evento.results.length; i++) {
            const resultado = evento.results[i];

            if (!resultado.isFinal) continue;

            for (let j = 0; j < resultado.length; j++) {
                const frase = resultado[j].transcript
                    .toLocaleLowerCase('pt-BR')
                    .normalize('NFD')
                    .replace(/[\u0300-\u036f]/g, '')
                    .replace(/[.,!?]/g, '')
                    .trim();

                console.log('[VitaSense] Frase reconhecida:', frase);

                const encontrou = expressoes.some(expressao =>
                    expressao.test(frase)
                );

                if (encontrou) {
                    console.log('[VitaSense] Pedido de ajuda identificado.');

                    pararEscuta();
                    atualizarBotao('Pedido de ajuda identificado');

                    if (!dialogo.open) {
                        dialogo.showModal();
                    }

                    return;
                }
            }
        }
    };

    reconhecimento.onerror = evento => {
        console.error(
            '[VitaSense] Erro de reconhecimento:',
            evento.error
        );

        clearTimeout(temporizadorInicializacao);

        if (evento.error === 'no-speech') {
            console.log('[VitaSense] Nenhuma fala detectada.');
            return;
        }

        pararEscuta();

        const mensagens = {
            'not-allowed': 'Permissão de microfone negada',
            'service-not-allowed': 'Serviço de voz não permitido',
            'audio-capture': 'Não foi possível acessar o áudio',
            'network': 'Falha na conexão do serviço de voz',
            'language-not-supported': 'Idioma português indisponível',
            'bad-grammar': 'Erro na configuração do reconhecimento'
        };

        atualizarBotao(
            mensagens[evento.error] || 'Erro: ' + evento.error
        );
    };

    reconhecimento.onend = () => {
        escutando = false;

        console.log('[VitaSense] Reconhecimento encerrado.');

        if (ativo && reiniciar && !dialogo.open) {
            clearTimeout(temporizador);

            temporizador = setTimeout(() => {
                iniciarEscuta();
            }, 1500);
        }
    };

    botao.addEventListener('click', () => {
        if (ativo) {
            pararEscuta();
            atualizarBotao('Ativar ajuda por voz');
            return;
        }

        ativo = true;
        reiniciar = true;

        iniciarEscuta();
    });

    fechar.addEventListener('click', () => {
        dialogo.close();
    });

    dialogo.addEventListener('click', evento => {
        if (evento.target === dialogo) {
            dialogo.close();
        }
    });

    atualizarBotao();

})();
