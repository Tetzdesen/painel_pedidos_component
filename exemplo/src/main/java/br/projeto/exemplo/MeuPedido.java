package br.projeto.exemplo;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.time.LocalDateTime;

/**
 * Pedido concreto do sistema de delivery de exemplo.
 */
public class MeuPedido extends PedidoAbstract {

    public MeuPedido(String identificador, LocalDateTime dataHora, String nomeCliente,
            String enderecoEntrega, double valorTotal) {
        super(identificador, dataHora, nomeCliente, enderecoEntrega, valorTotal);
    }

    @Override
    public PedidoAbstract criarCopia(String novoIdentificador, LocalDateTime novaDataHora) {
        return new MeuPedido(novoIdentificador, novaDataHora,
                getNomeCliente(), getEnderecoEntrega(), getValorTotal());
    }
}
