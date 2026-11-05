// Formulário em 3 passos: mostra só o que o gatilho/ação escolhidos precisam, gera a revisão e libera o botão salvar.
const f = document.getElementById('formAuto');
const val = n => f.querySelector(`[name=${n}]:checked`)?.value;
const TIPO = { LIGAR_LUZ: 'LUZ', DESLIGAR_LUZ: 'LUZ', LIGAR_AQUECEDOR: 'AQUECEDOR', ATIVAR_EXAUSTOR: 'EXAUSTOR' };
const NOMES_GAT = { HORARIO: 'horário específico', PRESENCA: 'presença detectada', AUSENCIA: 'ausência de movimento', MANUAL: 'acionamento manual', SENSOR: 'evento de um sensor' };
const NOMES_ACAO = { LIGAR_LUZ: 'Ligar luz', DESLIGAR_LUZ: 'Desligar luz', LIGAR_AQUECEDOR: 'Ligar aquecedor', ATIVAR_EXAUSTOR: 'Ativar exaustor', ENVIAR_NOTIFICACAO: 'Enviar notificação' };

function gatilho() {
  const g = val('gatilho');
  document.querySelectorAll('[data-gat]').forEach(p => {
    const ativo = p.dataset.gat === g;
    p.hidden = !ativo;
    p.querySelectorAll('input,select').forEach(i => i.disabled = !ativo);   // campos ocultos não são enviados
  });
  document.getElementById('gatTitulo').textContent = NOMES_GAT[g];
  revisao();
}

function acao() {
  const a = val('acao'), tipo = TIPO[a], sel = document.getElementById('selDisp');
  document.getElementById('acaoTitulo').textContent = NOMES_ACAO[a];
  document.getElementById('boxDisp').hidden = !tipo;
  document.getElementById('boxNotif').hidden = !!tipo;
  sel.disabled = !tipo;
  let primeiro = null;
  [...sel.options].forEach(o => {
    const ok = o.dataset.tipo === tipo;
    o.hidden = !ok; o.disabled = !ok;
    if (ok && !primeiro) primeiro = o;
  });
  if (tipo && (sel.selectedOptions[0]?.disabled || !sel.value)) sel.value = primeiro ? primeiro.value : '';
  document.getElementById('dispAjuda').textContent = primeiro ? 'Somente este equipamento será afetado.' : 'Não há equipamento deste tipo cadastrado em Minha casa.';
  revisao();
}

function revisao() {
  const g = val('gatilho'), a = val('acao'), sel = document.getElementById('selDisp');
  const op = sel.selectedOptions[0];
  document.getElementById('campoComodo').value = (!sel.disabled && op) ? op.dataset.comodo : '—';
  const dias = [...f.querySelectorAll('[name=dias]:checked')].map(c => c.value);
  const diasTxt = dias.length === 0 || dias.length === 7 ? 'todos os dias' : dias.join(', ');
  const h = f.querySelector('[data-gat=' + g + '] [name=horario]')?.value;
  const hf = f.querySelector('[data-gat=' + g + '] [name=horarioFim]')?.value;
  const min = f.querySelector('[name=minutos]')?.value;
  const quando = {
    HORARIO: `${diasTxt}, às ${h || '—'}`,
    PRESENCA: `quando houver presença detectada (${diasTxt}${h ? `, das ${h} às ${hf || '—'}` : ''})`,
    AUSENCIA: `após ${min || '—'} minutos sem movimento`,
    MANUAL: 'somente quando você usar “Executar agora”',
    SENSOR: 'quando o sensor escolhido registrar um evento'
  }[g];
  const alvo = !sel.disabled && op ? `${op.dataset.nome}, em ${op.dataset.comodo}` : 'o painel (notificação)';
  document.getElementById('revTexto').textContent =
    `A rotina “${f.nome.value || '(sem nome)'}” será executada ${quando}. Ação: ${NOMES_ACAO[a].toLowerCase()} — ${alvo}. ` +
    'Se o equipamento estiver sem conexão ou com falha, a execução é bloqueada e o aviso fica registrado no histórico.';
  document.getElementById('btnSalvar').disabled = !document.getElementById('revisei').checked;
}

f.querySelectorAll('[name=gatilho]').forEach(r => r.addEventListener('change', gatilho));
f.querySelectorAll('[name=acao]').forEach(r => r.addEventListener('change', acao));
f.addEventListener('input', revisao); f.addEventListener('change', revisao);
document.getElementById('revisei').addEventListener('change', revisao);

f.addEventListener('submit', e => {
  const g = val('gatilho');
  if (!f.nome.value.trim()) { e.preventDefault(); alert('Informe o nome da rotina.'); f.nome.focus(); return; }
  if (g === 'HORARIO' && !f.querySelector('[data-gat=HORARIO] [name=horario]').value) { e.preventDefault(); alert('Informe o horário de execução (etapa 1).'); }
});
document.querySelectorAll('[data-confirma]').forEach(x => x.addEventListener('submit', e => { if (!confirm(x.dataset.confirma)) e.preventDefault(); }));

gatilho(); acao();
