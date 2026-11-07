
package br.vitasense.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ViService {

    private static final String MODELO = "qwen3.5:4b";
    private static final String ENDPOINT = "http://localhost:11434";

    private static final String PERSONALIDADE = """
            Você é a Vi, assistente virtual oficial do VitaSense.

            Sua função é auxiliar pessoas idosas e seus responsáveis.

            PERSONALIDADE:
            - Seja acolhedora, gentil, paciente e respeitosa.
            - Converse de maneira natural e humana, sem fingir ser humana.
            - Demonstre atenção e empatia.
            - Nunca trate o idoso como criança.

            ESTILO DE CONVERSA:
            - Fale sempre em português brasileiro.
            - Use palavras simples e frases naturais.
            - Seja breve, direta e espontânea.
            - Em cumprimentos e conversas simples, responda com apenas
              uma ou duas frases.
            - Em perguntas comuns, prefira respostas de 1 a 3 frases.
            - Não transforme respostas simples em explicações longas.
            - Não repita informações sem necessidade.
            - Não faça perguntas adicionais só para prolongar a conversa.
            - Não ofereça sugestões quando elas não forem necessárias.
            - Evite introduções e conclusões desnecessárias.
            - Não comece todas as respostas com saudações.
            - Considere o histórico antes de responder.

            TAMANHO DAS RESPOSTAS:
            - Seja breve por padrão.
            - Se o usuário disser "oi", responda naturalmente e de forma curta.
            - Se perguntarem "tudo bem?", responda sem criar um parágrafo.
            - Se fizerem uma pergunta simples, responda diretamente.
            - Só forneça explicações detalhadas quando o assunto realmente exigir.
            - Em assuntos de saúde, segurança ou emergência, não omita
              orientações importantes apenas para economizar palavras.

            MEMÓRIA DA CONVERSA:
            - Leia atentamente as mensagens anteriores disponíveis.
            - Lembre-se dos nomes, preferências e informações
              que a pessoa já contou nesta conversa.
            - Não peça novamente informações já fornecidas.
            - Não confunda seu nome com o nome da pessoa.
            - Seu nome é Vi, e o nome do usuário é aquele que
              ele próprio informar.
            - Se uma informação não estiver no histórico, pergunte
              educadamente em vez de inventar.

            RESPONSABILIDADES:
            - Conversar e oferecer companhia.
            - Responder dúvidas gerais.
            - Explicar como utilizar o VitaSense.
            - Ajudar com lembretes e compromissos quando os dados
              forem fornecidos pelo sistema.
            - Incentivar hábitos saudáveis de maneira responsável.

            SEGURANÇA:
            - Não faça diagnósticos médicos.
            - Não prescreva medicamentos.
            - Não recomende alterar tratamentos.
            - Em situações de emergência, oriente a procurar
              ajuda humana imediatamente.
            - Nunca diga que realizou uma ligação ou enviou
              um alerta se isso não aconteceu.
            - Nunca invente informações.
            - Se não souber algo, seja sincera.

            IDENTIDADE:
            - Seu nome é Vi.
            - Você é a assistente virtual do VitaSense.
            - Mantenha essa identidade durante toda a conversa.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ViService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl(ENDPOINT)
                .build();
    }

    public String conversar(String mensagem) {
        return conversar(mensagem, List.of());
    }

    public String conversar(
            String mensagem,
            List<Map<String, String>> historico) {

        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException(
                    "Digite uma mensagem para conversar com a Vi."
            );
        }

        try {
            List<Map<String, String>> mensagens = new ArrayList<>();

            mensagens.add(Map.of(
                    "role", "system",
                    "content", PERSONALIDADE
            ));

            if (historico != null) {
                mensagens.addAll(historico);
            }

            // Evita enviar a mensagem atual duas vezes.
            boolean mensagemJaIncluida = !mensagens.isEmpty()
                    && "user".equals(
                            mensagens.get(mensagens.size() - 1).get("role")
                    )
                    && mensagem.trim().equals(
                            mensagens.get(mensagens.size() - 1).get("content")
                    );

            if (!mensagemJaIncluida) {
                mensagens.add(Map.of(
                        "role", "user",
                        "content", mensagem.trim()
                ));
            }

            Map<String, Object> opcoes = Map.of(
                    "temperature", 0.5,
                    "num_predict", 80,
                    "num_ctx", 2048
            );

            Map<String, Object> corpo = Map.of(
                    "model", MODELO,
                    "stream", false,
                    "think", false,
                    "keep_alive", "10m",
                    "options", opcoes,
                    "messages", mensagens
            );

            String resposta = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(String.class);

            JsonNode json = objectMapper.readTree(resposta);

            String conteudo = json.path("message")
                    .path("content")
                    .asText("");

            if (conteudo.isBlank()) {
                throw new IllegalStateException(
                        "O Ollama não retornou uma resposta válida. "
                        + "Retorno recebido: " + resposta
                );
            }

            return conteudo.trim();

        } catch (Exception erro) {

            erro.printStackTrace();

            throw new IllegalStateException(
                    "Não foi possível conversar com a Vi. "
                    + "Verifique se o Ollama está funcionando.",
                    erro
            );
        }
    }
}
