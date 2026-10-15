package br.projeto.painelpedidos.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.projeto.painelpedidos.internal.AcessoPedido;
import br.projeto.painelpedidos.internal.PoliticaSla;
import br.projeto.painelpedidos.support.Configuracoes;
import br.projeto.painelpedidos.support.PedidoTeste;
import br.projeto.painelpedidos.support.RelogioAjustavel;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CicloVidaPedidoTest {

    private static final LocalDateTime ABERTURA = LocalDateTime.of(2026, 10, 13, 14, 0);

    private RelogioAjustavel relogio;
    private PedidoTeste pedido;

    @BeforeEach
    void preparar() {
        relogio = new RelogioAjustavel(ABERTURA);
        pedido = new PedidoTeste("1001", ABERTURA, "Ana Souza", "Rua das Flores, 120 - Centro", 42.90);
        AcessoPedido.configurar(pedido, relogio);
    }

    @Test
    void pedidoRecemCriadoEstaNovoSemPagamentoESemCredito() {
        assertThat(pedido.estaNovo(), is(true));
        assertThat(pedido.getSituacao(), equalTo("Novo"));
        assertThat(pedido.isPagamentoRegistrado(), is(false));
        assertThat(pedido.getFormaPagamento(), nullValue());
        assertThat(pedido.getCreditoGerado(), closeTo(0, 0.001));
        assertThat(pedido.getDataHoraMudancaEstado(), equalTo(ABERTURA));
        assertThat(pedido.isPrioridade(), is(false));
    }

    @Test
    void consultasDeSituacaoSaoExclusivas() {
        pedido.registrarPagamento("Pix");
        pedido.marcarProntoParaEntrega();
        pedido.sairParaEntrega();

        assertThat(pedido.estaEmRotaDeEntrega(), is(true));
        assertThat(List.of(pedido.estaNovo(), pedido.estaEmPreparo(), pedido.estaProntoParaEntrega(),
                pedido.estaEntregue(), pedido.estaCancelado()), not(org.hamcrest.Matchers.hasItem(true)));
    }

    @Test
    void pagamentoLevaAoPreparoERegistraAFormaEOInstante() {
        relogio.avancar(Duration.ofMinutes(3));

        pedido.registrarPagamento("Pix - chave CPF");

        assertThat(pedido.estaEmPreparo(), is(true));
        assertThat(pedido.getFormaPagamento(), equalTo("Pix - chave CPF"));
        assertThat(pedido.getDataHoraMudancaEstado(), equalTo(ABERTURA.plusMinutes(3)));
    }

    @Test
    void cicloCompletoAteEntregue() {
        pedido.registrarPagamento("Pix");
        pedido.marcarProntoParaEntrega();
        assertThat(pedido.estaProntoParaEntrega(), is(true));
        pedido.sairParaEntrega();
        assertThat(pedido.estaEmRotaDeEntrega(), is(true));
        pedido.marcarEntregue();
        assertThat(pedido.estaEntregue(), is(true));
        assertThat(pedido.getSituacao(), equalTo("Entregue"));
    }

    @Test
    void naoAvancaSemPagamento() {
        assertRejeitada(PedidoAbstract::marcarProntoParaEntrega);
        assertRejeitada(PedidoAbstract::sairParaEntrega);
        assertRejeitada(PedidoAbstract::marcarEntregue);
        assertThat(pedido.estaNovo(), is(true));
    }

    @Test
    void acaoForaDeOrdemERejeitadaSemAlterarOPedido() {
        pedido.registrarPagamento("Pix");
        LocalDateTime mudanca = pedido.getDataHoraMudancaEstado();
        relogio.avancar(Duration.ofMinutes(1));

        assertRejeitada(PedidoAbstract::marcarEntregue);
        assertRejeitada(p -> p.registrarPagamento("Dinheiro"));

        assertThat(pedido.estaEmPreparo(), is(true));
        assertThat(pedido.getFormaPagamento(), equalTo("Pix"));
        assertThat(pedido.getDataHoraMudancaEstado(), equalTo(mudanca));
    }

    @Test
    void formaDePagamentoEObrigatoria() {
        assertThrows(IllegalArgumentException.class, () -> pedido.registrarPagamento(" "));
        assertThat(pedido.estaNovo(), is(true));
    }

    @Test
    void cancelamentoSemPagamentoNaoGeraCredito() {
        pedido.cancelar();

        assertThat(pedido.estaCancelado(), is(true));
        assertThat(pedido.getCreditoGerado(), closeTo(0, 0.001));
    }

    @Test
    void cancelamentoDeEntreguePagoGeraCreditoDoValorPago() {
        PedidoTeste entregue = new PedidoTeste("1011", ABERTURA, "Karina Barros", "Rua das Flores, 400", 98.00);
        entregue.registrarPagamento("Pix - chave CPF");
        entregue.marcarProntoParaEntrega();
        entregue.sairParaEntrega();
        entregue.marcarEntregue();

        entregue.cancelar();

        assertThat(entregue.estaCancelado(), is(true));
        assertThat(entregue.getCreditoGerado(), closeTo(98.00, 0.001));
    }

    @Test
    void canceladoNaoRetorna() {
        pedido.registrarPagamento("Pix");
        pedido.cancelar();
        double credito = pedido.getCreditoGerado();

        assertRejeitada(PedidoAbstract::cancelar);
        assertRejeitada(PedidoAbstract::marcarProntoParaEntrega);
        assertRejeitada(PedidoAbstract::sairParaEntrega);
        assertRejeitada(PedidoAbstract::marcarEntregue);
        assertRejeitada(p -> p.registrarPagamento("Pix"));

        assertThat(pedido.estaCancelado(), is(true));
        assertThat(pedido.getCreditoGerado(), closeTo(credito, 0.001));
    }

    @Test
    void copiaDeCanceladoNasceNovaSemPagamento() {
        pedido.registrarPagamento("Pix");
        pedido.cancelar();
        LocalDateTime agora = ABERTURA.plusHours(1);

        PedidoAbstract copia = pedido.copiarComoNovo("1021", agora);

        assertThat(copia, not(sameInstance(pedido)));
        assertThat(copia.getIdentificador(), equalTo("1021"));
        assertThat(copia.getDataHora(), equalTo(agora));
        assertThat(copia.getNomeCliente(), equalTo("Ana Souza"));
        assertThat(copia.getEnderecoEntrega(), equalTo("Rua das Flores, 120 - Centro"));
        assertThat(copia.getValorTotal(), closeTo(42.90, 0.001));
        assertThat(copia.estaNovo(), is(true));
        assertThat(copia.isPagamentoRegistrado(), is(false));
        assertThat(pedido.estaCancelado(), is(true));
    }

    @Test
    void copiaDePedidoAtivoERejeitada() {
        pedido.registrarPagamento("Pix");

        assertThrows(IllegalStateException.class, () -> pedido.copiarComoNovo("1021", ABERTURA));
    }

    @Test
    void acoesComDataHoraDoComponenteOficial() {
        LocalDateTime agora = ABERTURA.plusMinutes(7);

        pedido.registrarPagamento("Pix", agora);
        assertThat(pedido.estaEmPreparo(), is(true));
        assertThat(pedido.getDataHoraMudancaEstado(), equalTo(agora));
        pedido.marcarProntoParaEntrega(agora.plusMinutes(1));
        pedido.sairParaEntrega(agora.plusMinutes(2));
        pedido.marcarEntregue(agora.plusMinutes(3));
        assertThat(pedido.estaEntregue(), is(true));
        pedido.cancelar(agora.plusMinutes(4));

        assertThat(pedido.estaCancelado(), is(true));
        assertThat(pedido.getDataHoraMudancaEstado(), equalTo(agora.plusMinutes(4)));
        assertThat(pedido.getCreditoGerado(), closeTo(42.90, 0.001));
    }

    @Test
    void aplicarPagamentoRegistraSemMudarDeSituacao() {
        pedido.aplicarPagamento("Dinheiro", ABERTURA.plusMinutes(1));

        assertThat(pedido.isPagamentoRegistrado(), is(true));
        assertThat(pedido.getFormaPagamento(), equalTo("Dinheiro"));
        assertThat(pedido.estaNovo(), is(true));
    }

    @Test
    void confirmarCancelamentoCalculaOCredito() {
        assertThat(pedido.getCreditoGerado(), closeTo(0, 0.001));
        pedido.aplicarPagamento("Pix", ABERTURA);

        pedido.confirmarCancelamento();

        assertThat(pedido.getCreditoGerado(), closeTo(42.90, 0.001));
    }

    @Test
    void dataHoraNulaERejeitadaSemAlterarOPedido() {
        assertThrows(NullPointerException.class, () -> pedido.registrarPagamento("Pix", null));
        assertThrows(NullPointerException.class, () -> pedido.cancelar(null));
        assertThat(pedido.estaNovo(), is(true));
        assertThat(pedido.isPagamentoRegistrado(), is(false));
    }

    @Test
    void dadosObrigatoriosSaoValidados() {
        assertThrows(IllegalArgumentException.class, () -> new PedidoTeste(" ", ABERTURA));
        assertThrows(NullPointerException.class, () -> new PedidoTeste("1", null));
        assertThrows(IllegalArgumentException.class,
                () -> new PedidoTeste("1", ABERTURA, "A", "B", -1));
    }

    private void assertRejeitada(Consumer<PedidoAbstract> acao) {
        assertThrows(IllegalStateException.class, () -> acao.accept(pedido));
    }
}
