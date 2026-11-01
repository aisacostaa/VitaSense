
/* =========================================
   VITASENSE — CADASTRO
   Wizard de 4 etapas + validação
========================================= */

const f = document.getElementById('f');

if (f) {
    const secs = [...f.querySelectorAll('[data-step]')];

    const nomes = [
        '',
        'Cadastro do idoso',
        'Cadastro do imóvel',
        'Revisão',
        ''
    ];

    let atual = 1;

    /* =========================================
       CAMPOS NUMÉRICOS: SOMENTE DÍGITOS
    ========================================= */

    f.querySelectorAll('[data-digits]').forEach(input => {
        input.addEventListener('input', () => {
            input.value = input.value.replace(/\D/g, '');
        });
    });

    /* =========================================
       MOSTRAR E OCULTAR SENHA
    ========================================= */

    /* MOSTRAR E OCULTAR SENHA */
document.querySelectorAll('.toggle-password').forEach(button => {
    button.addEventListener('click', function (event) {
        event.preventDefault();

        const input = document.getElementById(this.dataset.target);

        if (!input) {
            console.error('Campo de senha não encontrado:', this.dataset.target);
            return;
        }

        const mostrar = input.type === 'password';

        input.type = mostrar ? 'text' : 'password';

        const olhoAberto = this.querySelector('.eye-open');
        const olhoFechado = this.querySelector('.eye-closed');

        if (olhoAberto) {
            olhoAberto.hidden = !mostrar;
        }

        if (olhoFechado) {
            olhoFechado.hidden = mostrar;
        }

        this.setAttribute('aria-pressed', String(mostrar));
        this.setAttribute(
            'aria-label',
            mostrar ? 'Ocultar senha' : 'Mostrar senha'
        );
    });
});
    /* =========================================
       NAVEGAÇÃO ENTRE ETAPAS
    ========================================= */

    function ir(n) {
        atual = n;

        secs.forEach(section => {
            section.hidden = Number(section.dataset.step) !== n;
        });

        document.querySelectorAll('.steps li').forEach(li => {
            const k = Number(li.dataset.s);

            li.className =
                k < n ? 'done' :
                k === n ? 'on' : '';

            const small = li.querySelector('small');

            if (small) {
                small.textContent =
                    k < n ? 'Finalizada' :
                    k === n ? 'Etapa atual' :
                    'Não iniciada';
            }
        });

        document.getElementById('voltar').style.visibility =
            n === 1 ? 'hidden' : 'visible';

        document.getElementById('proximo').textContent =
            n === 4 ? 'Finalizar cadastro' : 'Próximo';

        document.getElementById('prox').textContent =
            n < 4
                ? 'Próxima etapa: ' + nomes[n].toLowerCase()
                : '';

        if (n === 4) {
            revisar();
        }

        window.scrollTo(0, 0);
    }

    /* =========================================
       AVISO DE VALIDAÇÃO
    ========================================= */

    function aviso(el, msg) {
        let b = document.getElementById('aviso');

        if (!b) {
            b = document.createElement('div');
            b.id = 'aviso';
            b.className = 'alert alert-erro';
            f.prepend(b);
        }

        const rotulo =
            el.closest('div')?.querySelector('label')?.textContent ||
            el.labels?.[0]?.textContent ||
            el.placeholder ||
            el.name;

        b.textContent =
            `${rotulo.trim()}: ${msg || el.validationMessage || 'preencha este campo corretamente.'}`;

        el.scrollIntoView({ block: 'center' });

        el.focus({ preventScroll: true });
    }

    /* =========================================
       VALIDAÇÃO DA ETAPA ATUAL
    ========================================= */

    function etapaValida() {
        document.getElementById('aviso')?.remove();

        const sec = secs[atual - 1];

        for (const el of sec.querySelectorAll('input, select, textarea')) {
            if (!el.checkValidity()) {
                aviso(el);
                return false;
            }
        }

        if (atual === 1) {
            const senha = f.elements.namedItem('senha');
            const confirmarSenha = f.elements.namedItem('confirmarSenha');

            if (senha.value !== confirmarSenha.value) {
                aviso(confirmarSenha, 'as senhas não coincidem.');
                return false;
            }
        }

        return true;
    }

    /* =========================================
       DADOS PARA A REVISÃO
    ========================================= */

    const v = n =>
        (f.elements.namedItem(n)?.value || '').trim() || '—';

    const sn = n =>
        f.querySelector(`[name="${n}"]:checked`)?.value === 'true'
            ? 'Sim'
            : 'Não';

    const li = (k, val) => {
        const p = document.createElement('p');
        const b = document.createElement('b');

        b.textContent = `${k}:`;

        p.appendChild(b);
        p.appendChild(document.createTextNode(` ${val}`));

        return p.outerHTML;
    };

    /* =========================================
       MONTAR RESUMO DO CADASTRO
    ========================================= */

    function revisar() {
        const nasc = f.idosoNascimento.value
            ? f.idosoNascimento.value.split('-').reverse().join('/')
            : '—';

        document.getElementById('rv1').innerHTML =
            li('Nome completo', v('nome')) +
            li('E-mail', v('email')) +
            li('Telefone', v('telefone'));

        document.getElementById('rv2').innerHTML =
            li('Nome completo', v('idosoNome')) +
            li('Data de nascimento', nasc) +
            li('Mora sozinho(a)', sn('idosoMoraSozinho')) +
            li('Usa celular', sn('idosoUsaCelular')) +
            li('Condições de saúde', v('idosoCondicoes')) +
            li('Mobilidade', v('idosoMobilidade')) +
            li(
                'Contato de emergência',
                `${v('contatoNome')} (${v('contatoParentesco')}) - ${v('contatoTelefone')}`
            );

        document.getElementById('rv3').innerHTML =
            li('Imóvel', v('imovelNome')) +
            li(
                'Endereço',
                `${v('rua')}, ${v('numero')} - ${v('bairro')}, ${v('cidade')} - CEP ${v('cep')}`
            ) +
            li('Complemento', v('complemento')) +
            li('Tipo', v('tipoResidencia')) +
            li('Cômodos', v('comodos'));
    }

    /* =========================================
       BOTÃO PRÓXIMO / FINALIZAR
    ========================================= */

    document.getElementById('proximo').onclick = () => {
        if (!etapaValida()) return;

        if (atual < 4) {
            return ir(atual + 1);
        }

        /* Validar novamente as três etapas antes de enviar */

        for (let i = 1; i <= 3; i++) {
            ir(i);

            if (!etapaValida()) {
                return;
            }
        }

        HTMLFormElement.prototype.submit.call(f);
    };

    /* =========================================
       BOTÃO VOLTAR
    ========================================= */

    document.getElementById('voltar').onclick = () => {
        if (atual > 1) {
            ir(atual - 1);
        }
    };

    /* =========================================
       BOTÕES EDITAR DA REVISÃO
    ========================================= */

    document.querySelectorAll('[data-edit]').forEach(a => {
        a.onclick = e => {
            e.preventDefault();
            ir(Number(a.dataset.edit));
        };
    });

    /* =========================================
       INICIALIZAÇÃO
    ========================================= */

    ir(1);
}
