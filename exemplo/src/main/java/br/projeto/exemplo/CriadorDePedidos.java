package br.projeto.exemplo;

import br.projeto.painelpedidos.listener.ICriarNovoPedidoListener;
import br.projeto.painelpedidos.repositorio.IPedidoRepository;
import br.projeto.painelpedidos.model.PedidoAbstract;
import br.projeto.painelpedidos.tempo.IRelogioService;
import java.util.List;

/**
 * Cria um novo pedido a cada clique em "Novo Pedido", percorrendo uma lista
 * fixa de clientes. Quem decide quais e quantos pedidos criar e o consumidor.
 */
public class CriadorDePedidos implements ICriarNovoPedidoListener {

    private record Cliente(String nome, String endereco, double valor) {
    }

    private static final List<Cliente> CLIENTES = List.of(
            new Cliente("Paula Martins", "Rua das Laranjeiras, 58 - Centro", 45.90),
            new Cliente("Rafael Teixeira", "Av. Beira Mar, 1200 - Praia", 73.40),
            new Cliente("Sofia Cardoso", "Rua Bela Vista, 9 - Jardim", 29.90),
            new Cliente("Tiago Moreira", "Rua do Mercado, 310 - Sao Miguel", 112.00));

    private final IPedidoRepository repositorio;
    private final IRelogioService relogio;
    private int proximoCliente;

    public CriadorDePedidos(IPedidoRepository repositorio, IRelogioService relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    @Override
    public List<PedidoAbstract> criarNovoPedido() {
        Cliente cliente = CLIENTES.get(proximoCliente++ % CLIENTES.size());
        return List.of(new MeuPedido(repositorio.gerarProximoIdentificador(), relogio.agora(),
                cliente.nome(), cliente.endereco(), cliente.valor()));
    }
}
