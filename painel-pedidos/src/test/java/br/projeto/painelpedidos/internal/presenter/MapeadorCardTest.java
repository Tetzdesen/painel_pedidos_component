package br.projeto.painelpedidos.internal.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import br.projeto.painelpedidos.internal.AcessoPedido;
import br.projeto.painelpedidos.internal.PoliticaSla;
import br.projeto.painelpedidos.support.Configuracoes;
import br.projeto.painelpedidos.support.PedidoTeste;
import br.projeto.painelpedidos.support.RelogioAjustavel;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapeadorCardTest {

    private static final LocalDateTime ABERTURA = LocalDateTime.of(2026, 10, 13, 9, 2);
    private static final LocalDate HOJE = ABERTURA.toLocalDate();
    private RelogioAjustavel relogio;
    private PedidoTeste pedido;

    @BeforeEach
    void preparar() {
        relogio = new RelogioAjustavel(ABERTURA);
        pedido = new PedidoTeste("1003", ABERTURA, "Carla Rocha", "Rua Nova, 35 - Vila do Sul", 31.00);
        AcessoPedido.configurar(pedido, relogio);
    }

    private static List<String> faixas(CardViewModel card) {
        return card.faixas().stream().map(FaixaViewModel::texto).toList();
    }

    @Test
    void novoAguardandoPagamentoEPrioritario() {
        relogio.avancar(Duration.ofMinutes(10));
        pedido.marcarPrioridade();

        CardViewModel card = MapeadorCard.mapear(pedido, HOJE, false, false);

        assertThat(card.numero(), equalTo("#1003"));
        assertThat(card.horario(), equalTo("09:02"));
        assertThat(card.cliente(), equalTo("Carla Rocha"));
        assertThat(card.endereco(), equalTo("Rua Nova, 35 - Vila do Sul"));
        assertThat(card.valor(), equalTo("R$ 31,00"));
        assertThat(card.formaPagamento(), equalTo(""));
        assertThat(faixas(card), contains("Aguardando pagamento", "Prioridade"));
        assertThat(card.faixas().get(0).tipo(), is(TipoFaixa.ALERTA));
        assertThat(card.faixas().get(1).tipo(), is(TipoFaixa.PRIORIDADE));
        assertThat(card.prioridade(), is(true));
        assertThat(card.textoAcao(), equalTo("Confirmar pagamento"));
        assertThat(card.acaoHabilitada(), is(true));
        assertThat(card.exibeCancelar(), is(true));
    }

    @Test
    void emPreparoMostraPagamentoConfirmadoEFormaDePagamento() {
        pedido.registrarPagamento("Pix - chave CPF");

        CardViewModel card = MapeadorCard.mapear(pedido, HOJE, true, false);

        assertThat(faixas(card), contains("Pagamento confirmado"));
        assertThat(card.faixas().get(0).tipo(), is(TipoFaixa.SUCESSO));
        assertThat(card.formaPagamento(), equalTo("Pix - chave CPF"));
        assertThat(card.textoAcao(), equalTo("Pronto pra entrega"));
        assertThat(card.selecionado(), is(true));
    }

    @Test
    void acaoDeCadaEstado() {
        pedido.registrarPagamento("Pix");
        pedido.marcarProntoParaEntrega();
        assertThat(MapeadorCard.mapear(pedido, HOJE, false, false).textoAcao(), equalTo("Sair pra entrega"));
        pedido.sairParaEntrega();
        assertThat(MapeadorCard.mapear(pedido, HOJE, false, false).textoAcao(), equalTo("Entregue"));
        pedido.marcarEntregue();

        CardViewModel entregue = MapeadorCard.mapear(pedido, HOJE, false, false);
        assertThat(entregue.textoAcao(), equalTo("Pedido finalizado"));
        assertThat(entregue.acaoHabilitada(), is(false));
        assertThat(entregue.exibeCancelar(), is(true));
    }

    @Test
    void horarioDeHojeOntemEDiasAnteriores() {
        assertThat(MapeadorCard.formatarHorario(LocalDateTime.of(2026, 10, 13, 9, 2), HOJE), equalTo("09:02"));
        assertThat(MapeadorCard.formatarHorario(LocalDateTime.of(2026, 10, 12, 19, 30), HOJE),
                equalTo("Ontem 19:30"));
        assertThat(MapeadorCard.formatarHorario(LocalDateTime.of(2026, 10, 10, 8, 15), HOJE),
                equalTo("10/10 08:15"));
    }

    @Test
    void canceladoSemCredito() {
        pedido.cancelar();

        CardViewModel card = MapeadorCard.mapear(pedido, HOJE, false, false);

        assertThat(faixas(card), contains("Pedido cancelado", "Sem crédito gerado"));
        assertThat(card.textoAcao(), equalTo("Criar novo pedido"));
        assertThat(card.exibeCancelar(), is(false));
    }

    @Test
    void canceladoComCredito() {
        PedidoTeste caro = new PedidoTeste("1011", ABERTURA, "Karina Barros", "Rua das Flores, 400", 1234.5);
        caro.registrarPagamento("Pix");
        caro.cancelar();

        CardViewModel card = MapeadorCard.mapear(caro, HOJE, false, false);

        assertThat(faixas(card), contains("Pedido cancelado", "Crédito gerado ao cliente: R$ 1.234,50"));
        assertThat(card.valor(), equalTo("R$ 1.234,50"));
    }
}
