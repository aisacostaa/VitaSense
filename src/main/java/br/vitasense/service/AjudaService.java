package br.vitasense.service;

import br.vitasense.model.Atividade;
import br.vitasense.model.PessoaAcompanhada;
import br.vitasense.repository.AtividadeRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/** Tutoriais da área do idoso. Cada um tem passo a passo escrito e, se o arquivo existir, um vídeo em static/video/ajuda/. */
@Service
public class AjudaService {
    private final AtividadeRepository ativ;
    public AjudaService(AtividadeRepository a) { ativ = a; }

    /** {id, título, descrição, arquivo do vídeo, passos} */
    private static final List<Object[]> TUTORIAIS = List.of(
        new Object[]{"luzes", "Como acender e apagar as luzes.", "Veja como ligar e desligar as luzes de cada cômodo.", "ajuda-luzes.mp4",
            List.of("Toque em “Minha casa” no menu.", "Encontre o cômodo, por exemplo a Sala.", "Ao lado da luz, toque em “Ligar” para acender ou em “Desligar” para apagar.", "O botão muda e aparece uma mensagem confirmando.")},
        new Object[]{"aparelhos", "Como controlar os aparelhos da casa.", "Aprenda a ligar e desligar seus aparelhos.", "ajuda-aparelhos.mp4",
            List.of("Toque em “Minha casa” no menu.", "Veja os aparelhos de cada cômodo: ventilador, aquecedor, exaustor e outros.", "Toque em “Ligar” ou em “Desligar”.", "Se aparecer “Sem conexão” ou “Com problema”, o aparelho não pode ser ligado agora. Confira a tomada ou avise o seu responsável.")},
        new Object[]{"vi", "Como conversar com a Vi.", "Veja como falar e pedir ajuda à sua assistente.", "ajuda-vi.mp4",
            List.of("No Início, toque em “Falar com a Vi”.", "Toque no microfone e diga o que precisa, ou escolha “Prefiro digitar”.", "A Vi responde e ajuda com as luzes, os lembretes e os avisos.")},
        new Object[]{"lembretes", "Como consultar meus lembretes.", "Saiba onde ver seus horários e compromissos.", "ajuda-lembretes.mp4",
            List.of("Toque em “Lembretes” no menu.", "Veja o que você precisa lembrar hoje.", "Quando chegar a hora, toque em “Confirmar lembrete”.", "Para criar um lembrete seu, toque em “Adicionar novo lembrete”.")},
        new Object[]{"avisos", "O que fazer quando receber um aviso.", "Entenda o aviso e saiba qual é o próximo passo.", "ajuda-avisos.mp4",
            List.of("Toque em “Avisos” no menu.", "Leia primeiro os avisos importantes.", "Depois de ler, toque em “Já li”. O seu responsável vai saber.", "Se não souber o que fazer, toque em “Preciso de ajuda”.")}
    );

    public List<Map<String, Object>> tutoriais() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] t : TUTORIAIS) {
            String arquivo = (String) t[3];
            out.add(Map.of("id", t[0], "titulo", t[1], "descricao", t[2], "passos", t[4],
                    "video", "/video/ajuda/" + arquivo, "temVideo", new ClassPathResource("static/video/ajuda/" + arquivo).exists()));
        }
        return out;
    }

    public static boolean idValido(String id) { return TUTORIAIS.stream().anyMatch(t -> t[0].equals(id)); }

    /** A pessoa abriu um tutorial: o responsável vê nas atividades (para saber o que ela já aprendeu). */
    @Transactional
    public void registrarVisto(PessoaAcompanhada p, String id) {
        Object[] t = TUTORIAIS.stream().filter(x -> x[0].equals(id)).findFirst().orElseThrow(() -> new IllegalStateException("Tutorial não encontrado."));
        Atividade a = new Atividade(LocalDateTime.now(), IdosoService.primeiroNome(p.getNome()) + " abriu um tutorial", (String) t[1], "ROTINA", p.getImovel());
        a.setAutor(p.getNome());
        ativ.save(a);
    }
}
