// relógio da visualização (horário fictício) e confirmação das ações de privacidade
const p2 = n => String(n).padStart(2, '0');
const tick = () => {
  const d = new Date();
  document.querySelectorAll('[data-clock]').forEach(e =>
    e.textContent = `${p2(d.getDate())}/${p2(d.getMonth() + 1)}/${d.getFullYear()} · ${p2(d.getHours())}:${p2(d.getMinutes())}:${p2(d.getSeconds())}`);
};
tick(); setInterval(tick, 1000);
document.querySelectorAll('[data-confirma]').forEach(f => f.addEventListener('submit', e => {
  if (!confirm(f.dataset.confirma)) e.preventDefault();
}));

// "Ativar áudio" liga o som do vídeo. Se o navegador bloquear, mantém mudo (regra de autoplay).
document.querySelectorAll('.vs-viewer video').forEach(v => {
  if (v.dataset.audio === 'true') {
    v.muted = false;
    v.play().catch(() => { v.muted = true; v.play(); });
  }
});
