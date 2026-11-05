/* VitaSense — navegação confortável
   1) Mantém a posição da rolagem ao enviar formulário (POST) ou clicar em link da mesma página.
   2) Mostra as mensagens de sucesso/erro como avisos flutuantes (as do HTML da página e as enviadas pelo servidor). */
(() => {
  'use strict';
  if (window.__vsNav) return; window.__vsNav = true;
  const KEY = 'vs-scroll';

  // ---------- restaurar rolagem ----------
  let salvo = null;
  try { salvo = JSON.parse(sessionStorage.getItem(KEY) || 'null'); sessionStorage.removeItem(KEY); } catch (e) {}
  if (salvo && (salvo.path !== location.pathname || Date.now() - salvo.t > 8000)) salvo = null;
  const restaurar = () => { if (salvo) window.scrollTo(0, salvo.y); };
  if (salvo) {
    document.addEventListener('DOMContentLoaded', restaurar);
    window.addEventListener('load', () => { restaurar(); setTimeout(restaurar, 120); });
    restaurar();
  }

  // ---------- guardar rolagem antes de sair ----------
  const guardar = () => sessionStorage.setItem(KEY, JSON.stringify({ path: location.pathname, y: window.scrollY, t: Date.now() }));
  document.addEventListener('submit', e => {
    if (e.defaultPrevented) return;
    const f = e.target;
    if (f.tagName === 'FORM' && (f.method || '').toLowerCase() === 'post') guardar();
  });
  document.addEventListener('click', e => {
    if (e.defaultPrevented || e.button !== 0 || e.ctrlKey || e.metaKey || e.shiftKey) return;
    const a = e.target.closest && e.target.closest('a[href]');
    if (!a || a.target === '_blank' || a.hasAttribute('download')) return;
    const u = new URL(a.href, location.href);
    if (u.origin === location.origin && u.pathname === location.pathname && !u.hash) guardar();
  });

  // ---------- avisos flutuantes ----------
  const css = document.createElement('style');
  css.textContent = `
    .vs-toasts{position:fixed;top:16px;right:16px;z-index:9999;display:flex;flex-direction:column;gap:10px;max-width:min(420px,calc(100vw - 32px))}
    .vs-toast{display:flex;gap:12px;align-items:flex-start;padding:14px 16px;border-radius:12px;font:500 14px/1.4 system-ui,sans-serif;box-shadow:0 10px 30px rgba(11,25,87,.22);animation:vsIn .25s ease}
    .vs-toast.ok{background:#eaf8ef;color:#1d5c3a;border:1px solid #b7e4c7}.vs-toast.erro{background:#fff0f0;color:#9b2323;border:1px solid #f3b9b9}
    .vs-toast button{margin-left:auto;border:0;background:none;color:inherit;font-size:18px;line-height:1;cursor:pointer;opacity:.7}
    .vs-toast ul{margin:4px 0 0 18px;padding:0}.vs-toast.saindo{opacity:0;transform:translateY(-8px);transition:.25s}
    @keyframes vsIn{from{opacity:0;transform:translateY(-10px)}}
    @media(prefers-reduced-motion:reduce){.vs-toast{animation:none}}`;
  document.head.appendChild(css);

  const caixa = document.createElement('div');
  caixa.className = 'vs-toasts'; caixa.setAttribute('role', 'status'); caixa.setAttribute('aria-live', 'polite');

  function mostrar(erro, preencher) {
    if (!caixa.isConnected) document.body.appendChild(caixa);
    const t = document.createElement('div'); t.className = 'vs-toast ' + (erro ? 'erro' : 'ok');
    const corpo = document.createElement('div'); preencher(corpo);
    const x = document.createElement('button'); x.type = 'button'; x.textContent = '×'; x.setAttribute('aria-label', 'Fechar aviso');
    const fechar = () => { t.classList.add('saindo'); setTimeout(() => t.remove(), 250); };
    x.addEventListener('click', fechar);
    t.append(corpo, x); caixa.appendChild(t);
    setTimeout(fechar, erro ? 12000 : 6000);
  }

  const iniciar = () => {
    const alvos = document.querySelectorAll('main [class*="-msg"], main .alert.ok, main .alert.erro');
    alvos.forEach(el => { mostrar(el.classList.contains('erro'), c => { c.innerHTML = el.innerHTML; }); el.style.display = 'none'; });
    const f = window.__vsFlash;                                   // mensagens do servidor (páginas sem área de mensagem)
    if (!alvos.length && f) {
      if (f.ok) mostrar(false, c => { c.textContent = f.ok; });
      if (f.erro) mostrar(true, c => { c.textContent = f.erro; });
    }
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', iniciar); else iniciar();
})();
