package br.projeto.painelpedidos.internal.presenter;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.util.Comparator;

/**
 * Ordem dentro da raia: prioridade primeiro; em cada grupo, FIFO pelo tempo na
 * situacao atual (data/hora da ultima mudanca de estado).
 */
public final class OrdenacaoRaia {

    public static final Comparator<PedidoAbstract> COMPARADOR = Comparator
            .comparing((PedidoAbstract pedido) -> !pedido.isPrioridade())
            .thenComparing(PedidoAbstract::getDataHoraMudancaEstado)
            .thenComparing(PedidoAbstract::getIdentificador);

    private OrdenacaoRaia() {
    }
}
