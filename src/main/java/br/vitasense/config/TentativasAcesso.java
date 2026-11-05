package br.vitasense.config;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Limite de tentativas erradas por origem (IP): 5 erros bloqueiam por 10 minutos. Protege o código de acesso contra adivinhação. */
@Component
public class TentativasAcesso {
    static final int MAXIMO = 5;
    static final long JANELA_MS = 10 * 60_000L;
    private final Map<String, long[]> mapa = new ConcurrentHashMap<>();   // chave -> {erros, inícioDaJanela}

    public boolean bloqueado(String chave) {
        long[] e = mapa.get(chave);
        if (e == null) return false;
        if (agora() - e[1] > JANELA_MS) { mapa.remove(chave); return false; }
        return e[0] >= MAXIMO;
    }
    public void falha(String chave) {
        mapa.compute(chave, (k, e) -> e == null || agora() - e[1] > JANELA_MS ? new long[]{1, agora()} : new long[]{e[0] + 1, e[1]});
    }
    public void sucesso(String chave) { mapa.remove(chave); }
    long agora() { return System.currentTimeMillis(); }
}
