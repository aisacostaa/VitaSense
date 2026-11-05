// mostra os dias da semana só quando a recorrência é "Semanalmente"; valida e confirma exclusão
const rec = document.getElementById('recorrencia'), box = document.getElementById('dias'), txt = document.getElementById('diasTxt');
const atualiza = () => { const sem = rec.value === 'Semanalmente'; box.hidden = !sem; txt.hidden = sem; };
rec?.addEventListener('change', atualiza); if (rec) atualiza();

document.querySelectorAll('[data-confirma]').forEach(f => f.addEventListener('submit', e => { if (!confirm(f.dataset.confirma)) e.preventDefault(); }));

document.getElementById('formLembrete')?.addEventListener('submit', e => {
  const f = e.target;
  if (!f.titulo.value.trim()) { e.preventDefault(); alert('Informe o título do lembrete.'); f.titulo.focus(); }
  else if (!f.data.value || !f.horario.value) { e.preventDefault(); alert('Informe a data e o horário.'); }
});
