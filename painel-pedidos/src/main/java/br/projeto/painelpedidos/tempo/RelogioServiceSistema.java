package br.projeto.painelpedidos.tempo;

import java.time.LocalDateTime;

/**
 * Relogio padrao: usa o relogio do sistema operacional.
 */
public final class RelogioServiceSistema implements IRelogioService {

    @Override
    public LocalDateTime agora() {
        return LocalDateTime.now();
    }
}
