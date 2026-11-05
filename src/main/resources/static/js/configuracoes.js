// Configurações: envio automático da foto, campos só-números e conferência da nova senha.
document.querySelectorAll('input[data-auto]').forEach(i => i.addEventListener('change', () => {
  const f = i.files && i.files[0];
  if (!f) return;
  if (f.size > 2 * 1024 * 1024) { alert('A imagem deve ter no máximo 2 MB.'); i.value = ''; return; }
  i.form.submit();
}));
document.querySelectorAll('[data-digits]').forEach(i => i.addEventListener('input', () => i.value = i.value.replace(/\D/g, '')));
document.getElementById('formSenha')?.addEventListener('submit', e => {
  const f = e.target;
  if (f.nova.value.length < 8 || !/[A-Za-z]/.test(f.nova.value) || !/\d/.test(f.nova.value)) { e.preventDefault(); alert('A nova senha deve ter pelo menos 8 caracteres, com letras e números.'); f.nova.focus(); }
  else if (f.nova.value !== f.confirmar.value) { e.preventDefault(); alert('A confirmação não coincide com a nova senha.'); f.confirmar.focus(); }
});
