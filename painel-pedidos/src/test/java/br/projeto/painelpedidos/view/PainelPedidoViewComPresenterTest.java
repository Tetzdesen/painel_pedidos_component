package br.projeto.painelpedidos.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.projeto.painelpedidos.internal.presenter.PainelPedidoPresenter;
import br.projeto.painelpedidos.internal.swing.AcessoView;
import br.projeto.painelpedidos.internal.swing.VisaoPainelSwing;
import br.projeto.painelpedidos.tempo.RelogioServiceSistema;
import br.projeto.painelpedidos.som.SomTomStrategy;
import br.projeto.painelpedidos.support.Configuracoes;
import br.projeto.painelpedidos.support.PedidoTeste;
import br.projeto.painelpedidos.support.RepositorioEspiao;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/**
 * Caminho do README (secao 4.3): PainelPedidoView com um PainelPedidoPresenter.
 * Roda sem ambiente grafico, pois PainelPedidoView e um componente leve.
 */
class PainelPedidoViewComPresenterTest {

    private final RepositorioEspiao repositorio =
            new RepositorioEspiao().com(new PedidoTeste("1001", LocalDateTime.now()));

    private PainelPedidoPresenter presenter(PainelPedidoView view) {
        PainelPedidoPresenter presenter = new PainelPedidoPresenter(view, repositorio,
                Configuracoes.padrao(false), new SomTomStrategy(880, 200), new RelogioServiceSistema());
        presenter.setVisualizarPedidoListener(pedido -> { });
        presenter.setConfirmarPagamentoListener(pedido -> pedido.registrarPagamento("Pix"));
        presenter.setCriarNovoPedidoListener(List::of);
        return presenter;
    }

    @Test
    void exibeOsPedidosDoRepositorioNaViewPublica() throws Exception {
        PainelPedidoView view = new PainelPedidoView();
        PainelPedidoPresenter presenter = presenter(view);

        SwingUtilities.invokeAndWait(presenter::iniciar);

        assertThat(texto(view, "#1001"), is(true));
        assertThat(texto(view, "Novo (1)"), is(true));
        SwingUtilities.invokeAndWait(presenter::encerrar);
    }

    @Test
    void acoesFuncionamPelaViewPublica() throws Exception {
        PainelPedidoView view = new PainelPedidoView();
        PainelPedidoPresenter presenter = presenter(view);
        SwingUtilities.invokeAndWait(presenter::iniciar);

        SwingUtilities.invokeAndWait(() -> botao(view, "Confirmar pagamento").doClick());

        assertThat(repositorio.obter("1001").estaEmPreparo(), is(true));
        assertThat(texto(view, "Em preparo (1)"), is(true));
        SwingUtilities.invokeAndWait(presenter::encerrar);
    }

    @Test
    void fecharParaOsMonitoresEDesligaAView() throws Exception {
        PainelPedidoView view = new PainelPedidoView();
        PainelPedidoPresenter presenter = presenter(view);
        SwingUtilities.invokeAndWait(presenter::iniciar);

        SwingUtilities.invokeAndWait(() -> botao(view, "Fechar").doClick());

        assertThat(presenter.isAtivo(), is(false));
        assertThat(((VisaoPainelSwing) AcessoView.visao(view)).isLigada(), is(false));
    }

    @Test
    void viewNulaERejeitada() {
        assertThrows(NullPointerException.class, () -> new PainelPedidoPresenter((PainelPedidoView) null,
                repositorio, Configuracoes.padrao(), new SomTomStrategy(880, 200), new RelogioServiceSistema()));
    }

    private static boolean texto(Container raiz, String procurado) {
        for (Component filho : raiz.getComponents()) {
            if (filho instanceof JLabel rotulo && procurado.equals(rotulo.getText())) {
                return true;
            }
            if (filho instanceof Container container && texto(container, procurado)) {
                return true;
            }
        }
        return false;
    }

    private static AbstractButton botao(Container raiz, String texto) {
        for (Component filho : raiz.getComponents()) {
            if (filho instanceof AbstractButton candidato && texto.equals(candidato.getText())) {
                return candidato;
            }
            if (filho instanceof Container container) {
                AbstractButton encontrado = botao(container, texto);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }
}
