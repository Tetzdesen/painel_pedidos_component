package br.projeto.painelpedidos.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import br.projeto.painelpedidos.internal.presenter.PainelPedidoPresenter;
import br.projeto.painelpedidos.support.AgendadorManual;
import br.projeto.painelpedidos.support.Configuracoes;
import br.projeto.painelpedidos.support.PedidoTeste;
import br.projeto.painelpedidos.support.RelogioAjustavel;
import br.projeto.painelpedidos.support.RepositorioEspiao;
import java.awt.GraphicsEnvironment;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/**
 * Teste de fumaca da View Swing real; so roda com ambiente grafico
 * (ex.: mvn test -Djava.awt.headless=false).
 */
class PainelPedidoViewSmokeTest {

    @Test
    void desenhaRaiasERecebePedidoNaThreadDoSwing() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "sem ambiente grafico");
        LocalDateTime agora = LocalDateTime.of(2026, 10, 13, 15, 0);
        RepositorioEspiao repositorio = new RepositorioEspiao().com(new PedidoTeste("1001", agora));
        boolean[] ok = {false};

        SwingUtilities.invokeAndWait(() -> {
            PainelPedidoView view = new PainelPedidoView();
            PainelPedidoPresenter presenter = new PainelPedidoPresenter(
                    br.projeto.painelpedidos.internal.swing.AcessoView.visao(view), repositorio,
                    Configuracoes.padrao(false), () -> { }, new RelogioAjustavel(agora), new AgendadorManual());
            presenter.iniciar(true);
            presenter.receberNovosPedidos(List.of(new PedidoTeste("1002", agora)));
            ok[0] = SwingUtilities.isEventDispatchThread();
        });

        assertThat(ok[0], is(true));
    }
}
