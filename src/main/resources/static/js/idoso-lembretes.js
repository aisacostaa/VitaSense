// Lembretes do idoso: janela de "novo lembrete".
const dlgNovo = document.getElementById('dlgNovo');
document.getElementById('abrirNovo')?.addEventListener('click', () => { dlgNovo.showModal(); document.getElementById('titulo').focus(); });
document.getElementById('fecharNovo')?.addEventListener('click', () => dlgNovo.close());
dlgNovo?.addEventListener('click', e => { if (e.target === dlgNovo) dlgNovo.close(); });
dlgNovo?.querySelector('form')?.addEventListener('submit', e => {
  const f = e.target;
  if (!f.titulo.value.trim()) { e.preventDefault(); alert('Escreva do que você quer se lembrar.'); f.titulo.focus(); }
  else if (!f.horario.value) { e.preventDefault(); alert('Escolha o horário.'); f.horario.focus(); }
});
