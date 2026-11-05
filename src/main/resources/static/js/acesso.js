// Código de acesso: máscara 0000-0000, copiar, imprimir e confirmações.
const campo = document.getElementById('codigo');
if (campo) {
  campo.addEventListener('input', () => {
    const d = campo.value.replace(/\D/g, '').slice(0, 8);
    campo.value = d.length > 4 ? d.slice(0, 4) + '-' + d.slice(4) : d;
  });
  campo.form.addEventListener('submit', e => {
    if (campo.value.replace(/\D/g, '').length !== 8) { e.preventDefault(); alert('Digite os 8 números do código.'); campo.focus(); }
  });
}
document.querySelectorAll('[data-copiar]').forEach(b => b.addEventListener('click', async () => {
  const original = b.textContent;
  try { await navigator.clipboard.writeText(b.dataset.copiar); b.textContent = 'Copiado!'; setTimeout(() => b.textContent = original, 2000); }
  catch (e) { prompt('Copie o código:', b.dataset.copiar); }
}));
document.querySelectorAll('[data-imprimir]').forEach(b => b.addEventListener('click', () => window.print()));
document.querySelectorAll('[data-confirma]').forEach(f => f.addEventListener('submit', e => { if (!confirm(f.dataset.confirma)) e.preventDefault(); }));
