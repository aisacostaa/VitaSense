/* VitaSense — Dashboard interactions */
(() => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // Mini-gráficos SVG: linhas suaves, gradiente discreto e animação de entrada.
  document.querySelectorAll('[data-spark]').forEach((svg) => {
    const values = (svg.dataset.spark || '')
      .split(',')
      .map(Number)
      .filter((value) => Number.isFinite(value) && value > 0);

    if (values.length < 2) return;

    const width = 200;
    const height = 44;
    const min = Math.min(...values);
    const range = (Math.max(...values) - min) || 1;
    const points = values.map((value, index) => {
      const x = (index / (values.length - 1)) * width;
      const y = height - 5 - ((value - min) / range) * (height - 12);
      return [x, y];
    });

    const line = points.map(([x, y]) => `${x.toFixed(1)},${y.toFixed(1)}`).join(' ');
    const area = `0,${height} ${line} ${width},${height}`;
    const gradientId = `spark-gradient-${Math.random().toString(36).slice(2, 9)}`;

    svg.setAttribute('viewBox', `0 0 ${width} ${height}`);
    svg.setAttribute('preserveAspectRatio', 'none');
    svg.innerHTML = `
      <defs>
        <linearGradient id="${gradientId}" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="currentColor" stop-opacity=".20"/>
          <stop offset="100%" stop-color="currentColor" stop-opacity="0"/>
        </linearGradient>
      </defs>
      <polygon class="spark-area" points="${area}" fill="url(#${gradientId})"/>
      <polyline class="spark-line" points="${line}" fill="none"
        stroke="currentColor" stroke-width="2.4" stroke-linecap="round"
        stroke-linejoin="round" vector-effect="non-scaling-stroke"/>
      <circle class="spark-end" cx="${points[points.length - 1][0]}"
        cy="${points[points.length - 1][1]}" r="3" fill="currentColor"/>
    `;

    if (!reduceMotion) {
      const path = svg.querySelector('.spark-line');
      const length = path.getTotalLength();
      path.style.strokeDasharray = `${length}`;
      path.style.strokeDashoffset = `${length}`;
      requestAnimationFrame(() => {
        path.style.transition = 'stroke-dashoffset 1100ms cubic-bezier(.22,1,.36,1)';
        path.style.strokeDashoffset = '0';
      });
    }
  });

  // Entrada escalonada dos cartões e seções.
  const motionRoot = document.querySelector('[data-dashboard-motion]');
  if (!motionRoot || reduceMotion) return;

  const revealItems = motionRoot.querySelectorAll(
    '.welcome-row, .summary-card, .monitor-grid > .dashboard-section, .device-card, .bottom-grid > .dashboard-section'
  );

  revealItems.forEach((item, index) => {
    item.classList.add('motion-ready');
    item.style.setProperty('--motion-delay', `${Math.min(index * 55, 440)}ms`);
  });

  if (!('IntersectionObserver' in window)) {
    revealItems.forEach((item) => item.classList.add('is-visible'));
    return;
  }

  const observer = new IntersectionObserver((entries, currentObserver) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add('is-visible');
      currentObserver.unobserve(entry.target);
    });
  }, { threshold: 0.08, rootMargin: '0px 0px -24px 0px' });

  revealItems.forEach((item) => observer.observe(item));
})();
