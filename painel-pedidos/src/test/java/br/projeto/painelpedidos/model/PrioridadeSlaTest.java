package br.projeto.painelpedidos.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import br.projeto.painelpedidos.internal.AcessoPedido;
import br.projeto.painelpedidos.internal.AvaliadorSla;
import br.projeto.painelpedidos.internal.PoliticaSla;
import br.projeto.painelpedidos.internal.presenter.OrdenacaoRaia;
import br.projeto.painelpedidos.support.Configuracoes;
import br.projeto.painelpedidos.support.PedidoTeste;
import br.projeto.painelpedidos.support.RelogioAjustavel;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PrioridadeSlaTest {

    private static final LocalDateTime ABERTURA = LocalDateTime.of(2026, 10, 13, 14, 0);
    private final AvaliadorSla avaliador = new AvaliadorSla(PoliticaSla.de(Configuracoes.padrao()));
    private final RelogioAjustavel relogio = new RelogioAjustavel(ABERTURA);

    private PedidoTeste pedido(String id, LocalDateTime abertura) {
        PedidoTeste pedido = new PedidoTeste(id, abertura);
        AcessoPedido.configurar(pedido, relogio);
        return pedido;
    }

    @Test
    void ultrapassarOSlaTornaPrioridade() {
        PedidoTeste pedido = pedido("1", ABERTURA);

        relogio.avancar(Duration.ofMinutes(6));
        avaliador.avaliar(List.of(pedido), relogio.agora());

        assertThat(pedido.isPrioridade(), is(true));
    }

    @Test
    void atingirExatamenteOLimiteAindaNaoEPrioridade() {
        PedidoTeste pedido = pedido("1", ABERTURA);

        relogio.avancar(Duration.ofMinutes(5));
        avaliador.avaliar(List.of(pedido), relogio.agora());

        assertThat(pedido.isPrioridade(), is(false));
    }

    @Test
    void mudancaDeEstadoRemoveAPrioridadeEReiniciaAContagem() {
        PedidoTeste pedido = pedido("1", ABERTURA);
        relogio.avancar(Duration.ofMinutes(6));
        avaliador.avaliar(List.of(pedido), relogio.agora());
        assertThat(pedido.isPrioridade(), is(true));

        pedido.registrarPagamento("Pix");
        assertThat(pedido.isPrioridade(), is(false));

        relogio.avancar(Duration.ofMinutes(20));
        avaliador.avaliar(List.of(pedido), relogio.agora());
        assertThat(pedido.isPrioridade(), is(false));
        relogio.avancar(Duration.ofSeconds(1));
        avaliador.avaliar(List.of(pedido), relogio.agora());
        assertThat(pedido.isPrioridade(), is(true));
    }

    @Test
    void entregueECanceladoNuncaSaoPrioridade() {
        PedidoTeste entregue = pedido("1", ABERTURA);
        entregue.registrarPagamento("Pix");
        entregue.marcarProntoParaEntrega();
        entregue.sairParaEntrega();
        entregue.marcarEntregue();
        PedidoTeste cancelado = pedido("2", ABERTURA);
        cancelado.cancelar();

        relogio.avancar(Duration.ofDays(3));
        avaliador.avaliar(List.of(entregue, cancelado), relogio.agora());

        assertThat(entregue.isPrioridade(), is(false));
        assertThat(cancelado.isPrioridade(), is(false));
    }

    @Test
    void marcacaoManualValeAteAProximaMudancaDeEstado() {
        PedidoTeste pedido = pedido("1", ABERTURA);

        pedido.marcarPrioridade();
        assertThat(pedido.isPrioridade(), is(true));
        pedido.registrarPagamento("Pix");

        assertThat(pedido.isPrioridade(), is(false));
    }

    @Test
    void avaliadorSoInformaMudancaQuandoMarcaAlgumPedido() {
        PedidoTeste pedido = pedido("1", ABERTURA);
        relogio.avancar(Duration.ofMinutes(6));

        assertThat(avaliador.avaliar(List.of(pedido), relogio.agora()), is(true));
        assertThat(avaliador.avaliar(List.of(pedido), relogio.agora()), is(false));
    }

    @Test
    void pedidoForaDoPainelNaoEPrioridade() {
        PedidoTeste solto = new PedidoTeste("1", ABERTURA.minusDays(1));

        assertThat(solto.isPrioridade(), is(false));
    }

    @Test
    void raiaOrdenaPrioridadePrimeiroEDepoisFifo() {
        LocalDateTime dia = LocalDateTime.of(2026, 10, 13, 0, 0);
        relogio.definir(dia.withHour(14).withMinute(55));
        PedidoTeste p1003 = pedido("1003", dia.withHour(9).withMinute(2));
        PedidoTeste p1002 = pedido("1002", dia.withHour(14).withMinute(50));
        PedidoTeste p1001 = pedido("1001", dia.withHour(14).withMinute(59));
        PedidoTeste p1017 = pedido("1017", dia.withHour(15).withMinute(2));
        relogio.definir(dia.withHour(15).withMinute(3));
        // 1003 e 1002 passaram de 5 min; 1001 (4 min) e 1017 (1 min) nao.
        avaliador.avaliar(List.of(p1003, p1002, p1001, p1017), relogio.agora());

        List<PedidoAbstract> raia = new ArrayList<>(List.of(p1017, p1001, p1002, p1003));
        raia.sort(OrdenacaoRaia.COMPARADOR);

        assertThat(raia.stream().map(PedidoAbstract::getIdentificador).toList(),
                contains("1003", "1002", "1001", "1017"));
    }

    @Test
    void filaPeloTempoNoEstadoEnaoPelaAbertura() {
        LocalDateTime dia = LocalDateTime.of(2026, 10, 13, 0, 0);
        PedidoTeste p1004 = pedido("1004", dia.withHour(10));
        p1004.registrarPagamento("Pix", dia.withHour(14).withMinute(30));
        PedidoTeste p1018 = pedido("1018", dia.withHour(11));
        p1018.registrarPagamento("Pix", dia.withHour(14));

        List<PedidoAbstract> raia = new ArrayList<>(List.of(p1004, p1018));
        raia.sort(OrdenacaoRaia.COMPARADOR);

        assertThat(raia.stream().map(PedidoAbstract::getIdentificador).toList(), contains("1018", "1004"));
    }
}
