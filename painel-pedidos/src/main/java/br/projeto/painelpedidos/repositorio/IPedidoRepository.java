package br.projeto.painelpedidos.repositorio;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.util.List;

/**
 * Porta de dados implementada pelo consumidor. O componente nao guarda pedidos:
 * toda leitura e gravacao passa por aqui.
 */
public interface IPedidoRepository {

    /**
     * Insere o pedido ou atualiza o existente com o mesmo identificador.
     */
    void salvar(PedidoAbstract pedido);

    List<PedidoAbstract> buscarTodos();

    String gerarProximoIdentificador();
}
