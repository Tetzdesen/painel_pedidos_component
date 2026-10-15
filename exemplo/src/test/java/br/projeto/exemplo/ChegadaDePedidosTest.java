package br.projeto.exemplo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import br.projeto.painelpedidos.aplicacao.PainelPedidoFacade;
import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.model.PedidoAbstract;
import br.projeto.painelpedidos.tempo.RelogioServiceSistema;
import br.projeto.painelpedidos.som.SomTomStrategy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class ChegadaDePedidosTest {

    private final RepositorioDePedidosEmMemoria repositorio = new RepositorioDePedidosEmMemoria();

    @Test
    void entregaUmPedidoEDepoisUmLoteDeDoisComClientesFixos() {
        new SemeadorDePedidos(repositorio, LocalDateTime.of(2026, 10, 13, 15, 3)).semear();
        List<List<PedidoAbstract>> entregues = new ArrayList<>();
        ChegadaDePedidos chegada = new ChegadaDePedidos(repositorio, new RelogioServiceSistema(), entregues::add);

        chegada.entregar(1);
        chegada.entregar(2);

        assertThat(entregues, hasSize(2));
        assertThat(entregues.get(0).stream().map(PedidoAbstract::getIdentificador).toList(), contains("1021"));
        assertThat(entregues.get(1).stream().map(PedidoAbstract::getIdentificador).toList(),
                contains("1022", "1023"));
        assertThat(entregues.get(1).stream().map(PedidoAbstract::getNomeCliente).toList(),
                contains("Beatriz Lopes", "Caio Fernandes"));
        assertThat(entregues.get(0).get(0).estaNovo(), is(true));
    }

    @Test
    void identificadoresNaoSeRepetemAntesDeSalvar() throws Exception {
        String[] gerados = new String[2];
        Thread outra = new Thread(() -> gerados[0] = repositorio.gerarProximoIdentificador());
        outra.start();
        outra.join();
        gerados[1] = repositorio.gerarProximoIdentificador();

        assertThat(gerados[0], not(equalTo(gerados[1])));
    }

    @Test
    void empurraPelaFachadaAPartirDeUmaThreadForaDoSwing() throws Exception {
        ConfiguracaoPainelPedido configuracao = new ConfiguracaoPainelPedido(
                Map.of("Novo", Duration.ofMinutes(5)), Duration.ofSeconds(30), 10, false, "Painel", "teste-chegada");
        PainelPedidoFacade painel = new PainelPedidoFacade(repositorio, configuracao,
                new SomTomStrategy(880, 200), new RelogioServiceSistema());
        AtomicReference<Boolean> naThreadDoSwing = new AtomicReference<>();
        ChegadaDePedidos chegada = new ChegadaDePedidos(repositorio, new RelogioServiceSistema(), pedidos -> {
            naThreadDoSwing.set(SwingUtilities.isEventDispatchThread());
            painel.receberNovosPedidos(pedidos);
        });

        Thread sistemaDeDelivery = new Thread(() -> chegada.entregar(2));
        sistemaDeDelivery.start();
        sistemaDeDelivery.join();
        SwingUtilities.invokeAndWait(() -> { });

        assertThat(naThreadDoSwing.get(), is(false));
        assertThat(repositorio.buscarTodos(), hasSize(2));
    }
}
