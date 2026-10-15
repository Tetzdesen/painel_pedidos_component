package br.projeto.painelpedidos.listener;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.util.List;

/**
 * Obrigatorio. Chamado pelo botao "Novo Pedido": devolve os pedidos a inserir,
 * que o painel salva e exibe. O componente nao cria pedidos.
 */
public interface ICriarNovoPedidoListener {

    List<PedidoAbstract> criarNovoPedido();
}
