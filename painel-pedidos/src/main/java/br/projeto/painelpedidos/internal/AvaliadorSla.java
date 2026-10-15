package br.projeto.painelpedidos.internal;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Marca como prioridade os pedidos que ultrapassaram o SLA da situacao atual.
 */
public final class AvaliadorSla {

    private final PoliticaSla politica;

    public AvaliadorSla(PoliticaSla politica) {
        this.politica = politica;
    }

    /**
     * @return {@code true} se algum pedido passou a ser prioridade
     */
    public boolean avaliar(Collection<PedidoAbstract> pedidos, LocalDateTime agora) {
        boolean mudou = false;
        for (PedidoAbstract pedido : pedidos) {
            if (!pedido.isPrioridade() && politica.ultrapassou(AcessoPedido.situacao(pedido),
                    pedido.getDataHoraMudancaEstado(), agora)) {
                pedido.marcarPrioridade();
                mudou = true;
            }
        }
        return mudou;
    }
}
