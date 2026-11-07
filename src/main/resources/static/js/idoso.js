/* =========================================================
   VITASENSE — ÁREA DO IDOSO
   Relógio e confirmação de pedido de ajuda
   ========================================================= */

/* Relógio ao vivo */

const relogio = document.getElementById('relogio');

function atualizarRelogio() {
    if (!relogio) return;

    const agora = new Date();

    const horas = String(agora.getHours()).padStart(2, '0');
    const minutos = String(agora.getMinutes()).padStart(2, '0');

    relogio.textContent = `${horas}:${minutos}`;
}

if (relogio) {
    atualizarRelogio();
    setInterval(atualizarRelogio, 10000);
}


/* =========================================================
   JANELA DE CONFIRMAÇÃO SOS
   ========================================================= */

const dlg = document.getElementById('dlgSos');
const abrirSos = document.getElementById('abrirSos');
const fecharSos = document.getElementById('fecharSos');


/* Abrir janela */

abrirSos?.addEventListener('click', () => {

    if (!dlg) return;

    if (!dlg.open) {
        dlg.showModal();
    }

});


/* Fechar pelo botão de cancelar */

fecharSos?.addEventListener('click', () => {

    if (dlg?.open) {
        dlg.close();
    }

});


/* Fechar ao clicar fora do conteúdo */

dlg?.addEventListener('click', (evento) => {

    if (evento.target === dlg) {
        dlg.close();
    }

});