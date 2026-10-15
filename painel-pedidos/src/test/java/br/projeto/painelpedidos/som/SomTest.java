package br.projeto.painelpedidos.som;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.projeto.painelpedidos.internal.EstrategiaSomEfetiva;
import br.projeto.painelpedidos.internal.SomSilenciosoStrategy;
import br.projeto.painelpedidos.support.Configuracoes;
import org.junit.jupiter.api.Test;

class SomTest {

    @Test
    void tomNaoLancaExcecaoMesmoSemAudio() throws InterruptedException {
        SomTomStrategy som = new SomTomStrategy(880, 50);

        assertDoesNotThrow(som::tocar);
        Thread.sleep(200);
    }

    @Test
    void tomRejeitaParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new SomTomStrategy(0, 200));
        assertThrows(IllegalArgumentException.class, () -> new SomTomStrategy(880, 0));
    }

    @Test
    void somAtivoUsaAEstrategiaInformada() {
        IEstrategiaSom informada = new SomTomStrategy(880, 200);

        assertThat(EstrategiaSomEfetiva.de(Configuracoes.padrao(true), informada), sameInstance(informada));
    }

    @Test
    void somDesligadoTrocaPelaEstrategiaSilenciosa() {
        int[] toques = {0};
        IEstrategiaSom espia = () -> toques[0]++;

        IEstrategiaSom efetiva = EstrategiaSomEfetiva.de(Configuracoes.padrao(false), espia);
        efetiva.tocar();

        assertThat(efetiva, instanceOf(SomSilenciosoStrategy.class));
        assertThat(toques[0], org.hamcrest.Matchers.is(0));
    }
}
