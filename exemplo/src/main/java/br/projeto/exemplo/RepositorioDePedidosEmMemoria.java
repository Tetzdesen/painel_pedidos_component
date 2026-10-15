package br.projeto.exemplo;

import br.projeto.painelpedidos.repositorio.IPedidoRepository;
import br.projeto.painelpedidos.model.PedidoAbstract;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio em memoria: guarda os pedidos enquanto a aplicacao roda.
 */
public class RepositorioDePedidosEmMemoria implements IPedidoRepository {

    private final Map<String, PedidoAbstract> pedidos = new LinkedHashMap<>();
    private int ultimoGerado = 1000;

    @Override
    public synchronized void salvar(PedidoAbstract pedido) {
        pedidos.put(pedido.getIdentificador(), pedido);
    }

    @Override
    public synchronized List<PedidoAbstract> buscarTodos() {
        return new ArrayList<>(pedidos.values());
    }

    /**
     * Reserva o proximo numero: pedidos criados em threads diferentes antes de
     * serem salvos nunca recebem o mesmo identificador.
     */
    @Override
    public synchronized String gerarProximoIdentificador() {
        int maior = ultimoGerado;
        for (String identificador : pedidos.keySet()) {
            try {
                maior = Math.max(maior, Integer.parseInt(identificador));
            } catch (NumberFormatException naoNumerico) {
                // ignora identificadores nao numericos
            }
        }
        ultimoGerado = maior + 1;
        return String.valueOf(ultimoGerado);
    }
}
