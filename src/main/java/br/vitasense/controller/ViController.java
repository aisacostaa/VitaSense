package br.vitasense.controller;

import br.vitasense.model.Usuario;
import br.vitasense.model.ViMemoria;
import br.vitasense.repository.ViMemoriaRepository;
import br.vitasense.service.ViService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/idoso/vi")
public class ViController extends BaseController {

    private static final int LIMITE_MENSAGENS = 12;

    private final ViService viService;
    private final ViMemoriaRepository memoriaRepository;
    private final ObjectMapper objectMapper;

    public ViController(
            ViService viService,
            ViMemoriaRepository memoriaRepository,
            ObjectMapper objectMapper) {

        this.viService = viService;
        this.memoriaRepository = memoriaRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/conversar")
    public ResponseEntity<Map<String, String>> conversar(
            @RequestBody Map<String, String> dados,
            Authentication auth) {

        String mensagem = dados.get("mensagem");

        if (mensagem == null || mensagem.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Digite uma mensagem."));
        }

        try {
            Usuario usuario = ctx.usuario(auth);

            ViMemoria memoria = memoriaRepository
                    .findByUsuario_Id(usuario.getId())
                    .orElseGet(() -> new ViMemoria(usuario));

            List<Map<String, String>> historico =
                    objectMapper.readValue(
                            memoria.getHistorico(),
                            new TypeReference<List<Map<String, String>>>() {}
                    );

            if (historico == null) {
                historico = new ArrayList<>();
            }

            historico.add(Map.of(
                    "role", "user",
                    "content", mensagem.trim()
            ));

            String resposta = viService.conversar(
                    mensagem,
                    historico
            );

            historico.add(Map.of(
                    "role", "assistant",
                    "content", resposta
            ));

            while (historico.size() > LIMITE_MENSAGENS) {
                historico.remove(0);
            }

            memoria.setHistorico(
                    objectMapper.writeValueAsString(historico)
            );

            memoriaRepository.save(memoria);

            return ResponseEntity.ok(
                    Map.of("resposta", resposta)
            );

        } catch (Exception erro) {

            erro.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "erro",
                            "A Vi não conseguiu responder agora. Tente novamente."
                    ));
        }
    }

    // Gera o áudio neural da Vi usando edge-tts
    @PostMapping(
            value = "/voz",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "audio/mpeg"
    )
    public ResponseEntity<?> gerarVoz(
            @RequestBody Map<String, String> dados) {

        String texto = dados.get("texto");

        if (texto == null || texto.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Não há texto para falar."));
        }

        if (texto.length() > 3000) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "A resposta é muito longa para gerar o áudio."));
        }

        Path arquivoAudio = null;
        Path arquivoLog = null;

        try {
            arquivoAudio = Files.createTempFile(
                    "vitasense-vi-", ".mp3"
            );

            arquivoLog = Files.createTempFile(
                    "vitasense-vi-log-", ".txt"
            );

            ProcessBuilder construtor = new ProcessBuilder(
                    "python",
                    "-m",
                    "edge_tts",
                    "--voice",
                    "pt-BR-FranciscaNeural",
                    "--text",
                    texto,
                    "--write-media",
                    arquivoAudio.toAbsolutePath().toString()
            );

            construtor.redirectErrorStream(true);
            construtor.redirectOutput(arquivoLog.toFile());

            Process processo = construtor.start();

            boolean terminou = processo.waitFor(
                    60,
                    TimeUnit.SECONDS
            );

            if (!terminou) {
                processo.destroyForcibly();

                return ResponseEntity.internalServerError()
                        .body(Map.of(
                                "erro",
                                "A geração da voz demorou demais. Tente novamente."
                        ));
            }

            if (processo.exitValue() != 0 ||
                    !Files.exists(arquivoAudio) ||
                    Files.size(arquivoAudio) == 0) {

                String detalhe = Files.exists(arquivoLog)
                        ? Files.readString(arquivoLog)
                        : "";

                System.err.println("[VitaSense] Erro edge-tts: " + detalhe);

                return ResponseEntity.internalServerError()
                        .body(Map.of(
                                "erro",
                                "Não foi possível gerar a voz. Verifique a conexão com a internet."
                        ));
            }

            byte[] audio = Files.readAllBytes(arquivoAudio);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(audio);

        } catch (InterruptedException erro) {

            Thread.currentThread().interrupt();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "erro",
                            "A geração da voz foi interrompida."
                    ));

        } catch (Exception erro) {

            erro.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "erro",
                            "Não foi possível gerar o áudio da Vi. Verifique se o Python e o edge-tts estão instalados."
                    ));

        } finally {

            try {
                if (arquivoAudio != null) {
                    Files.deleteIfExists(arquivoAudio);
                }

                if (arquivoLog != null) {
                    Files.deleteIfExists(arquivoLog);
                }

            } catch (Exception erro) {
                erro.printStackTrace();
            }
        }
    }
}