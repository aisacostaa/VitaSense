package br.vitasense.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.NoSuchElementException;

/** Mostra uma tela de erro amigável (com mensagem clara) para as falhas de regra de negócio e de permissão. */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalStateException.class, NoSuchElementException.class, AccessDeniedException.class})
    public String tratar(Exception e, Model m, HttpServletResponse resp) {
        boolean negado = e instanceof AccessDeniedException;
        resp.setStatus(negado ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_BAD_REQUEST);
        if (!negado) log.warn("Falha tratada: {}", e.getMessage());
        m.addAttribute("titulo", negado ? "Acesso não permitido" : "Não foi possível concluir");
        m.addAttribute("mensagem", negado ? "Seu perfil não tem permissão para esta ação ou página." : e.getMessage());
        return "erro";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String arquivoGrande(Model m, HttpServletResponse resp) {
        resp.setStatus(413);
        m.addAttribute("titulo", "Arquivo muito grande");
        m.addAttribute("mensagem", "A imagem deve ter no máximo 2 MB.");
        return "erro";
    }
}
