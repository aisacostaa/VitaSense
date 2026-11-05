package br.vitasense.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.RequestContextUtils;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Em toda página HTML: injeta o script de navegação (rolagem preservada) e entrega as mensagens "ok"/"erro" do servidor como avisos flutuantes. */
@Component
public class VsNavInjectionFilter extends OncePerRequestFilter {
    private static final String TAG = "<script src=\"/js/vs-nav.js\" defer></script>";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest r) {
        String p = r.getRequestURI();
        return p.startsWith("/css/") || p.startsWith("/js/") || p.startsWith("/img/") || p.startsWith("/video/") || p.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {
        Buffer w = new Buffer(resp);
        chain.doFilter(req, w);
        w.flushWriter();
        byte[] body = w.baos.toByteArray();
        if (body.length == 0) return;                                   // redirecionamentos etc.
        String ct = resp.getContentType();
        if (ct != null && ct.startsWith("text/html")) {
            Charset cs = resp.getCharacterEncoding() != null ? Charset.forName(resp.getCharacterEncoding()) : StandardCharsets.UTF_8;
            String html = new String(body, cs);
            int i = html.lastIndexOf("</body>");
            if (i >= 0 && !html.contains("vs-nav.js")) body = (html.substring(0, i) + flash(req) + TAG + html.substring(i)).getBytes(cs);
        }
        resp.setContentLength(body.length);
        resp.getOutputStream().write(body);
    }

    /** Mensagens que o controller mandou com RedirectAttributes.addFlashAttribute("ok"|"erro", texto). */
    private String flash(HttpServletRequest req) {
        Map<String, ?> f = RequestContextUtils.getInputFlashMap(req);
        if (f == null) return "";
        Object ok = f.get("ok"), erro = f.get("erro");
        if (!(ok instanceof String) && !(erro instanceof String)) return "";
        return "<script>window.__vsFlash={ok:" + js(ok instanceof String s ? s : null) + ",erro:" + js(erro instanceof String s ? s : null) + "};</script>";
    }

    private static String js(String s) {
        if (s == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\""); case '\\' -> b.append("\\\\"); case '\n' -> b.append("\\n"); case '\r' -> b.append("\\r");
                case '<' -> b.append("\\u003c"); case '>' -> b.append("\\u003e"); default -> b.append(c);
            }
        }
        return b.append('"').toString();
    }

    private static class Buffer extends HttpServletResponseWrapper {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private PrintWriter writer; private ServletOutputStream out;
        Buffer(HttpServletResponse r) { super(r); }
        @Override public ServletOutputStream getOutputStream() {
            if (out == null) out = new ServletOutputStream() {
                @Override public void write(int b) { baos.write(b); }
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener l) {}
            };
            return out;
        }
        @Override public PrintWriter getWriter() throws IOException {
            if (writer == null) writer = new PrintWriter(new OutputStreamWriter(baos, getCharacterEncoding()));
            return writer;
        }
        void flushWriter() { if (writer != null) writer.flush(); }
        @Override public void flushBuffer() {}
        @Override public void setContentLength(int len) {}
        @Override public void setContentLengthLong(long len) {}
    }
}
