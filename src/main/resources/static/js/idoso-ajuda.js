
// Central de Ajuda: abre e fecha os tutoriais
// e registra quando a pessoa visualiza cada orientação.

const csrf = document.querySelector('meta[name="csrf"]')?.content;
const avisados = new Set();

// Abre o tutorial e registra a visualização
document.querySelectorAll('[data-abrir]').forEach(botao => {
    botao.addEventListener('click', () => {

        const dialogId = botao.dataset.abrir;
        const dialog = document.getElementById(dialogId);

        if (!dialog) return;

        dialog.showModal();

        const id = botao.dataset.tutorial;

        if (id && !avisados.has(id)) {
            avisados.add(id);

            fetch('/idoso/ajuda/visto/' + id, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': csrf
                },
                keepalive: true
            }).catch(() => {});
        }
    });
});

// Fecha os tutoriais
document.querySelectorAll('dialog.ih-dialogo').forEach(dialog => {

    const fechar = dialog.querySelector('[data-fechar]');

    fechar?.addEventListener('click', () => {
        dialog.close();
    });

    // Fecha ao clicar fora do conteúdo
    dialog.addEventListener('click', evento => {
        if (evento.target === dialog) {
            dialog.close();
        }
    });

});
