// filtros aplicam ao mudar; contador de selecionados; confirmação ao remover
document.querySelectorAll('#filtros select').forEach(s => s.addEventListener('change', () => {
  const f = document.getElementById('filtros'); f.action = f.getAttribute('action') + '#equipamentos'; f.submit();
}));
const conta = () => document.getElementById('selCount').textContent = document.querySelectorAll('.chk:checked').length;
document.querySelectorAll('.chk').forEach(c => c.addEventListener('change', conta));
document.querySelectorAll('[data-confirma]').forEach(f => f.addEventListener('submit', e => {
  if (!confirm(f.dataset.confirma)) e.preventDefault();
}));
// validação do formulário de dispositivo
document.getElementById('formDisp')?.addEventListener('submit', e => {
  const n = e.target.nome, id = e.target.identificacao;
  if (!n.value.trim()) { e.preventDefault(); alert('Informe o nome do dispositivo.'); n.focus(); }
  else if (id.value && !/^[A-Za-z0-9-]+$/.test(id.value)) { e.preventDefault(); alert('Identificação aceita só letras, números e hífen.'); id.focus(); }
});
