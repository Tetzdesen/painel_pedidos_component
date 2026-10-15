package br.projeto.painelpedidos.listener;

import br.projeto.painelpedidos.model.PedidoAbstract;

/**
 * Obrigatorio. Chamado pelo botao "Visualizar pedido": a tela de detalhes e do consumidor.
 */
public interface IVisualizarPedidoListener {

    void visualizarPedido(PedidoAbstract pedido);
}
