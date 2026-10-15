package br.projeto.painelpedidos.internal.presenter;

import br.projeto.painelpedidos.internal.AcessoPedido;
import br.projeto.painelpedidos.model.PedidoAbstract;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Converte um pedido no {@link CardViewModel} correspondente ao seu estado.
 */
public final class MapeadorCard {

    private static final DateTimeFormatter HORARIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DIA_E_HORARIO = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final Locale PT_BR = Locale.of("pt", "BR");

    private MapeadorCard() {
    }

    public static String formatarValor(double valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    /**
     * Horario de abertura: "HH:mm" se for de hoje, "Ontem HH:mm" se for de ontem e
     * "dd/MM HH:mm" nos demais casos.
     */
    public static String formatarHorario(LocalDateTime dataHora, LocalDate hoje) {
        LocalDate dia = dataHora.toLocalDate();
        if (dia.equals(hoje)) {
            return HORARIO.format(dataHora);
        }
        if (dia.equals(hoje.minusDays(1))) {
            return "Ontem " + HORARIO.format(dataHora);
        }
        return DIA_E_HORARIO.format(dataHora);
    }

    /**
     * Texto da acao principal do pedido (botao do card e "Executar ação").
     */
    public static String textoDaAcao(PedidoAbstract pedido) {
        return switch (AcessoPedido.situacao(pedido)) {
            case NOVO -> "Confirmar pagamento";
            case EM_PREPARO -> "Pronto pra entrega";
            case PRONTO_PARA_ENTREGA -> "Sair pra entrega";
            case EM_ROTA_DE_ENTREGA -> "Entregue";
            case ENTREGUE -> "Pedido finalizado";
            case CANCELADO -> "Criar novo pedido";
        };
    }

    /**
     * Entregue nao tem acao executavel.
     */
    public static boolean possuiAcaoExecutavel(PedidoAbstract pedido) {
        return !pedido.estaEntregue();
    }

    public static CardViewModel mapear(PedidoAbstract pedido, LocalDate hoje, boolean selecionado,
            boolean destacado) {
        List<FaixaViewModel> faixas = new ArrayList<>();

        if (pedido.estaCancelado()) {
            faixas.add(new FaixaViewModel("Pedido cancelado", TipoFaixa.NEUTRA));
            faixas.add(pedido.getCreditoGerado() > 0
                    ? new FaixaViewModel("Crédito gerado ao cliente: " + formatarValor(pedido.getCreditoGerado()), TipoFaixa.NEUTRA)
                    : new FaixaViewModel("Sem crédito gerado", TipoFaixa.NEUTRA));
        } else if (pedido.isPagamentoRegistrado()) {
            faixas.add(new FaixaViewModel("Pagamento confirmado", TipoFaixa.SUCESSO));
        } else {
            faixas.add(new FaixaViewModel("Aguardando pagamento", TipoFaixa.ALERTA));
        }
        boolean prioridade = pedido.isPrioridade();
        if (prioridade) {
            faixas.add(new FaixaViewModel("Prioridade", TipoFaixa.PRIORIDADE));
        }

        return new CardViewModel(
                pedido.getIdentificador(),
                "#" + pedido.getIdentificador(),
                formatarHorario(pedido.getDataHora(), hoje),
                pedido.getNomeCliente(),
                pedido.getEnderecoEntrega(),
                formatarValor(pedido.getValorTotal()),
                pedido.isPagamentoRegistrado() ? pedido.getFormaPagamento() : "",
                faixas,
                prioridade,
                textoDaAcao(pedido),
                possuiAcaoExecutavel(pedido),
                !pedido.estaCancelado(),
                selecionado,
                destacado);
    }
}
