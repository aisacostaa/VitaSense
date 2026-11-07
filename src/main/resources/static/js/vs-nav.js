
/* VitaSense — navegação confortável
   1) Mantém a posição da rolagem.
   2) Mostra mensagens de sucesso/erro como avisos flutuantes.
*/
(() => {
  'use strict';

  if (window.__vsNav) return;
  window.__vsNav = true;

  const KEY = 'vs-scroll';

  // ---------- restaurar rolagem ----------
  let salvo = null;

  try {
    salvo = JSON.parse(sessionStorage.getItem(KEY) || 'null');
    sessionStorage.removeItem(KEY);
  } catch (e) {}

  if (salvo && (salvo.path !== location.pathname || Date.now() - salvo.t > 8000)) {
    salvo = null;
  }

  const restaurar = () => {
    if (salvo) window.scrollTo(0, salvo.y);
  };

  if (salvo) {
    document.addEventListener('DOMContentLoaded', restaurar);
    window.addEventListener('load', () => {
      restaurar();
      setTimeout(restaurar, 120);
    });
    restaurar();
  }

  // ---------- guardar rolagem antes de sair ----------
  const guardar = () => {
    sessionStorage.setItem(KEY, JSON.stringify({
      path: location.pathname,
      y: window.scrollY,
      t: Date.now()
    }));
  };

  document.addEventListener('submit', e => {
    if (e.defaultPrevented) return;

    const f = e.target;

    if (f.tagName === 'FORM' && (f.method || '').toLowerCase() === 'post') {
      guardar();
    }
  });

  document.addEventListener('click', e => {
    if (e.defaultPrevented || e.button !== 0 || e.ctrlKey || e.metaKey || e.shiftKey) return;

    const a = e.target.closest && e.target.closest('a[href]');

    if (!a || a.target === '_blank' || a.hasAttribute('download')) return;

    const u = new URL(a.href, location.href);

    if (u.origin === location.origin && u.pathname === location.pathname && !u.hash) {
      guardar();
    }
  });

  // ---------- avisos flutuantes ----------
  const css = document.createElement('style');

  css.textContent = `
    .vs-toasts {
      position: fixed;
      top: calc(68px + env(safe-area-inset-top, 0px));
      right: 16px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-width: min(420px, calc(100vw - 32px));
    }

    .vs-toast {
      display: flex;
      gap: 12px;
      align-items: flex-start;
      padding: 16px 18px;
      border-radius: 18px;

      font: 500 14px/1.5 -apple-system, BlinkMacSystemFont,
        "SF Pro Text", Inter, system-ui, sans-serif;

      box-shadow:
        0 14px 36px -12px rgba(60, 45, 40, .18),
        inset 0 1px 0 rgba(255, 255, 255, .9);

      backdrop-filter: blur(18px);
      -webkit-backdrop-filter: blur(18px);

      animation: vsIn .25s ease;
    }

    /* SUCESSO — identidade VitaSense */
    .vs-toast.ok {
      background: rgba(255, 255, 255, .94);
      color: #38343D;

      border: 1px solid rgba(252, 134, 91, .32);
      border-left: 4px solid #FC865B;
    }

    /* ERRO — mantém destaque de atenção */
    .vs-toast.erro {
      background: rgba(255, 255, 255, .97);
      color: #A8441A;
      border: 1px solid #F3D3C0;
      border-left: 4px solid #D96F49;
    }

    .vs-toast button {
      margin-left: auto;
      border: 0;
      background: none;
      color: inherit;
      font-size: 20px;
      line-height: 1;
      cursor: pointer;
      opacity: .65;
      transition: opacity .2s, transform .2s;
    }

    .vs-toast button:hover {
      opacity: 1;
      transform: scale(1.1);
    }

    .vs-toast button:focus-visible {
      outline: 2px solid #FC865B;
      outline-offset: 3px;
      border-radius: 4px;
    }

    .vs-toast ul {
      margin: 4px 0 0 18px;
      padding: 0;
    }

    .vs-toast.saindo {
      opacity: 0;
      transform: translateY(-8px);
      transition: .25s;
    }

    @keyframes vsIn {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
    }

    @media (max-width: 560px) {
      .vs-toasts {
        right: 12px;
        left: 12px;
        max-width: none;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .vs-toast {
        animation: none;
      }
    }
  `;

  document.head.appendChild(css);

  const caixa = document.createElement('div');
  caixa.className = 'vs-toasts';
  caixa.setAttribute('role', 'status');
  caixa.setAttribute('aria-live', 'polite');

  function mostrar(erro, preencher) {
    if (!caixa.isConnected) {
      document.body.appendChild(caixa);
    }

    const t = document.createElement('div');
    t.className = 'vs-toast ' + (erro ? 'erro' : 'ok');

    const corpo = document.createElement('div');
    preencher(corpo);

    const x = document.createElement('button');
    x.type = 'button';
    x.textContent = '×';
    x.setAttribute('aria-label', 'Fechar aviso');

    const fechar = () => {
      t.classList.add('saindo');
      setTimeout(() => t.remove(), 250);
    };

    x.addEventListener('click', fechar);

    t.append(corpo, x);
    caixa.appendChild(t);

    setTimeout(fechar, erro ? 12000 : 6000);
  }

  const iniciar = () => {
    const alvos = document.querySelectorAll(
      'main [class*="-msg"], main .alert.ok, main .alert.erro'
    );

    alvos.forEach(el => {
      mostrar(el.classList.contains('erro'), c => {
        c.innerHTML = el.innerHTML;
      });

      el.style.display = 'none';
    });

    const f = window.__vsFlash;

    // Mensagens do servidor
    if (!alvos.length && f) {
      if (f.ok) mostrar(false, c => {
        c.textContent = f.ok;
      });

      if (f.erro) mostrar(true, c => {
        c.textContent = f.erro;
      });
    }
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', iniciar);
  } else {
    iniciar();
  }
})();
