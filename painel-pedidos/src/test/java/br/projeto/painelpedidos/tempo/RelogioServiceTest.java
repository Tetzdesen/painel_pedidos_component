package br.projeto.painelpedidos.tempo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import br.projeto.painelpedidos.support.RelogioAjustavel;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RelogioServiceTest {

    @Test
    void relogioDoSistemaDevolveOAgoraReal() {
        LocalDateTime antes = LocalDateTime.now();
        LocalDateTime agora = new RelogioServiceSistema().agora();
        LocalDateTime depois = LocalDateTime.now();

        assertThat(agora, greaterThanOrEqualTo(antes));
        assertThat(agora, lessThanOrEqualTo(depois));
    }

    @Test
    void relogioAjustavelFicaParadoAteSerAvancado() {
        RelogioAjustavel relogio = new RelogioAjustavel(LocalDateTime.of(2026, 10, 13, 9, 0));

        assertThat(relogio.agora(), equalTo(LocalDateTime.of(2026, 10, 13, 9, 0)));
        relogio.avancar(Duration.ofMinutes(6));
        assertThat(relogio.agora(), equalTo(LocalDateTime.of(2026, 10, 13, 9, 6)));
    }
}
