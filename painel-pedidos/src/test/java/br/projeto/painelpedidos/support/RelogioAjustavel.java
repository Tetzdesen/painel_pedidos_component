package br.projeto.painelpedidos.support;

import br.projeto.painelpedidos.tempo.IRelogioService;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Relogio de teste: parado em um instante e avancado manualmente.
 */
public class RelogioAjustavel implements IRelogioService {

    private LocalDateTime agora;

    public RelogioAjustavel(LocalDateTime agora) {
        this.agora = agora;
    }

    @Override
    public LocalDateTime agora() {
        return agora;
    }

    public void avancar(Duration duracao) {
        agora = agora.plus(duracao);
    }

    public void definir(LocalDateTime instante) {
        agora = instante;
    }
}
