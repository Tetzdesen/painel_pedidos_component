package br.projeto.painelpedidos.support;

import br.projeto.painelpedidos.repositorio.IPedidoRepository;
import br.projeto.painelpedidos.model.PedidoAbstract;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio em memoria que registra as chamadas recebidas.
 */
public class RepositorioEspiao implements IPedidoRepository {

    private final Map<String, PedidoAbstract> pedidos = new LinkedHashMap<>();
    public final List<String> chamadas = new ArrayList<>();
    public final List<PedidoAbstract> salvos = new ArrayList<>();
    public int buscas;
    /** Executado dentro de salvar, antes de registrar: permite observar a View nesse instante. */
    public java.util.function.Consumer<PedidoAbstract> aoSalvar = pedido -> { };
    private int proximo = 2000;

    public RepositorioEspiao com(PedidoAbstract... iniciais) {
        for (PedidoAbstract pedido : iniciais) {
            pedidos.put(pedido.getIdentificador(), pedido);
        }
        return this;
    }

    /** Insere sem passar pelo painel (simula alteracao externa). */
    public void inserirDiretamente(PedidoAbstract pedido) {
        pedidos.put(pedido.getIdentificador(), pedido);
    }

    @Override
    public void salvar(PedidoAbstract pedido) {
        aoSalvar.accept(pedido);
        chamadas.add("salvar:" + pedido.getIdentificador());
        salvos.add(pedido);
        pedidos.put(pedido.getIdentificador(), pedido);
    }

    @Override
    public List<PedidoAbstract> buscarTodos() {
        chamadas.add("buscarTodos");
        buscas++;
        return new ArrayList<>(pedidos.values());
    }

    @Override
    public String gerarProximoIdentificador() {
        chamadas.add("gerarProximoIdentificador");
        return String.valueOf(++proximo);
    }

    public PedidoAbstract obter(String identificador) {
        return pedidos.get(identificador);
    }
}
