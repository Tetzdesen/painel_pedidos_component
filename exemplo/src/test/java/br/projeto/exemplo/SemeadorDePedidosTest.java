package br.projeto.exemplo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SemeadorDePedidosTest {

    private static List<String> retrato(LocalDateTime agora) {
        RepositorioDePedidosEmMemoria repositorio = new RepositorioDePedidosEmMemoria();
        new SemeadorDePedidos(repositorio, agora).semear();
        return repositorio.buscarTodos().stream()
                .map(p -> p.getIdentificador() + "|" + p.getNomeCliente() + "|" + p.getValorTotal() + "|"
                        + p.getSituacao())
                .sorted()
                .toList();
    }

    @Test
    void semeiaOsMesmosPedidosACadaExecucao() {
        assertThat(retrato(LocalDateTime.of(2026, 10, 13, 15, 3)),
                equalTo(retrato(LocalDateTime.of(2026, 10, 14, 9, 0))));
    }

    @Test
    void distribuiOsVintePedidosDeHojeEAnterioresPelasSeisRaias() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 13, 15, 3);
        RepositorioDePedidosEmMemoria repositorio = new RepositorioDePedidosEmMemoria();
        new SemeadorDePedidos(repositorio, agora).semear();

        List<PedidoAbstract> hoje = repositorio.buscarTodos().stream()
                .filter(p -> p.getDataHora().toLocalDate().equals(agora.toLocalDate())).toList();
        Map<String, Long> porSituacao = hoje.stream()
                .collect(Collectors.groupingBy(PedidoAbstract::getSituacao, TreeMap::new, Collectors.counting()));

        assertThat(repositorio.buscarTodos(), hasSize(20));
        assertThat(porSituacao, equalTo(new TreeMap<>(Map.of("Novo", 5L, "Em preparo", 3L,
                "Pronto para entrega", 1L, "Em rota de entrega", 2L, "Entregue", 1L, "Cancelado", 2L))));
        assertThat(repositorio.gerarProximoIdentificador(), equalTo("1021"));
    }
}
