package br.projeto.painelpedidos.support;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.time.LocalDateTime;

public class PedidoTeste extends PedidoAbstract {

    public PedidoTeste(String identificador, LocalDateTime dataHora, String nomeCliente,
            String enderecoEntrega, double valorTotal) {
        super(identificador, dataHora, nomeCliente, enderecoEntrega, valorTotal);
    }

    public PedidoTeste(String identificador, LocalDateTime dataHora) {
        this(identificador, dataHora, "Cliente " + identificador, "Rua A, 1 - Centro", 50.0);
    }

    @Override
    public PedidoAbstract criarCopia(String novoIdentificador, LocalDateTime novaDataHora) {
        return new PedidoTeste(novoIdentificador, novaDataHora, getNomeCliente(), getEnderecoEntrega(),
                getValorTotal());
    }
}
