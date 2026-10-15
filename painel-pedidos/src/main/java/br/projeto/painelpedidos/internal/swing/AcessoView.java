package br.projeto.painelpedidos.internal.swing;

import br.projeto.painelpedidos.internal.presenter.VisaoPainel;
import br.projeto.painelpedidos.view.PainelPedidoView;

/**
 * Acesso interno a tela contida em {@link PainelPedidoView} (padrao "friend
 * accessor"), sem expor tipos internos na API publica. A ponte e registrada
 * pela propria PainelPedidoView na sua inicializacao estatica.
 */
public final class AcessoView {

    public interface Ponte {

        VisaoPainel visao(PainelPedidoView view);
    }

    private static volatile Ponte ponte;

    private AcessoView() {
    }

    public static void definirPonte(Ponte novaPonte) {
        if (ponte != null) {
            throw new IllegalStateException("A ponte de acesso a view ja foi definida.");
        }
        ponte = novaPonte;
    }

    public static VisaoPainel visao(PainelPedidoView view) {
        return ponte.visao(view);
    }
}
