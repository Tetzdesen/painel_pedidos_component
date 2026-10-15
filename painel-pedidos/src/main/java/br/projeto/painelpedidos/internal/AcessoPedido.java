package br.projeto.painelpedidos.internal;

import br.projeto.painelpedidos.internal.estado.SituacaoPedido;
import br.projeto.painelpedidos.model.PedidoAbstract;
import br.projeto.painelpedidos.tempo.IRelogioService;

/**
 * Acesso interno ao pedido (padrao "friend accessor"): permite ao painel
 * atribuir o relogio do painel e ler a situacao tipada sem expor
 * setters publicos em {@link PedidoAbstract}. A ponte e registrada pela
 * propria PedidoAbstract na sua inicializacao estatica.
 */
public final class AcessoPedido {

    public interface Ponte {

        void configurar(PedidoAbstract pedido, IRelogioService relogio);

        SituacaoPedido situacao(PedidoAbstract pedido);
    }

    private static volatile Ponte ponte;

    private AcessoPedido() {
    }

    public static void definirPonte(Ponte novaPonte) {
        if (ponte != null) {
            throw new IllegalStateException("A ponte de acesso ao pedido ja foi definida.");
        }
        ponte = novaPonte;
    }

    public static void configurar(PedidoAbstract pedido, IRelogioService relogio) {
        ponte.configurar(pedido, relogio);
    }

    public static SituacaoPedido situacao(PedidoAbstract pedido) {
        return ponte.situacao(pedido);
    }
}
