package br.projeto.painelpedidos.listener;

import br.projeto.painelpedidos.model.PedidoAbstract;

/**
 * Obrigatorio. Chamado pela acao "Confirmar pagamento" de um pedido Novo. O fluxo
 * de pagamento e do consumidor; ao confirmar, chame
 * {@link PedidoAbstract#registrarPagamento(String)} no proprio pedido.
 */
public interface IConfirmarPagamentoListener {

    void confirmarPagamento(PedidoAbstract pedido);
}
