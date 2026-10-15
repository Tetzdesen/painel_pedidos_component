package br.projeto.exemplo;

import br.projeto.painelpedidos.repositorio.IPedidoRepository;
import br.projeto.painelpedidos.model.PedidoAbstract;
import br.projeto.painelpedidos.tempo.IRelogioService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Entrada de pedidos do sistema de delivery de exemplo. Quando pedidos chegam,
 * o sistema os empurra para o painel (README, secao 2.6) a partir de uma thread
 * propria, fora da thread do Swing: primeiro um pedido e depois um lote de dois.
 */
public class ChegadaDePedidos {

    public static final Duration PRIMEIRA_CHEGADA = Duration.ofSeconds(15);
    public static final Duration SEGUNDA_CHEGADA = Duration.ofSeconds(40);

    private record Cliente(String nome, String endereco, double valor) {
    }

    private static final List<Cliente> CLIENTES = List.of(
            new Cliente("Vitor Almeida", "Rua das Mangueiras, 21 - Jardim", 54.90),
            new Cliente("Beatriz Lopes", "Av. das Nacoes, 77 - Centro", 36.50),
            new Cliente("Caio Fernandes", "Rua do Pescador, 5 - Praia", 92.00));

    private final IPedidoRepository repositorio;
    private final IRelogioService relogio;
    private final Consumer<List<PedidoAbstract>> destino;
    private int proximoCliente;

    /**
     * @param destino para onde os pedidos sao empurrados, normalmente
     *        {@code painelPedido::receberNovosPedidos}
     */
    public ChegadaDePedidos(IPedidoRepository repositorio, IRelogioService relogio,
            Consumer<List<PedidoAbstract>> destino) {
        this.repositorio = repositorio;
        this.relogio = relogio;
        this.destino = destino;
    }

    /**
     * Agenda as duas chegadas numa thread daemon (nao impede a aplicacao de terminar).
     */
    public void iniciar() {
        ScheduledExecutorService agenda = Executors.newSingleThreadScheduledExecutor(tarefa -> {
            Thread thread = new Thread(tarefa, "chegada-de-pedidos");
            thread.setDaemon(true);
            return thread;
        });
        agenda.schedule(() -> entregar(1), PRIMEIRA_CHEGADA.toMillis(), TimeUnit.MILLISECONDS);
        agenda.schedule(() -> entregar(2), SEGUNDA_CHEGADA.toMillis(), TimeUnit.MILLISECONDS);
        agenda.shutdown();
    }

    /**
     * Cria {@code quantidade} pedidos novos e os empurra para o destino.
     */
    public synchronized List<PedidoAbstract> entregar(int quantidade) {
        List<PedidoAbstract> chegaram = new ArrayList<>();
        for (int indice = 0; indice < quantidade; indice++) {
            Cliente cliente = CLIENTES.get(proximoCliente++ % CLIENTES.size());
            chegaram.add(new MeuPedido(repositorio.gerarProximoIdentificador(), relogio.agora(),
                    cliente.nome(), cliente.endereco(), cliente.valor()));
        }
        destino.accept(chegaram);
        return chegaram;
    }
}
