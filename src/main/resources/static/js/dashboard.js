/* VitaSense — Dashboard: gráfico de linha (histórico de BPM) a partir de data-spark */
(function () {
  document.querySelectorAll('[data-spark]').forEach(function (svg) {
    var v = (svg.dataset.spark || '').split(',').map(Number).filter(function (n) { return isFinite(n) && n > 0; });
    if (v.length < 2) return;
    var w = 400, h = 56, min = Math.min.apply(null, v), r = (Math.max.apply(null, v) - min) || 1;
    var pts = v.map(function (n, i) { return [i / (v.length - 1) * w, h - 6 - (n - min) / r * (h - 14)]; });
    var d = pts.map(function (p, i) { return (i ? 'L' : 'M') + p[0].toFixed(1) + ' ' + p[1].toFixed(1); }).join(' ');
    var l = pts[pts.length - 1];
    svg.setAttribute('viewBox', '0 0 ' + w + ' ' + h);
    svg.setAttribute('preserveAspectRatio', 'none');
    svg.innerHTML = '<path d="' + d + '" stroke-width="2.2" vector-effect="non-scaling-stroke"/><circle cx="' + l[0] + '" cy="' + l[1] + '" r="4" fill="currentColor" stroke="none"/>';
  });
})();
