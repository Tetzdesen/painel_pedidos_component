package br.projeto.exemplo;

import br.projeto.painelpedidos.listener.IVisualizarPedidoListener;
import br.projeto.painelpedidos.model.PedidoAbstract;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JOptionPane;

/**
 * A tela de detalhes e do consumidor: o painel apenas avisa qual pedido abrir.
 */
public class TelaDetalhePedido implements IVisualizarPedidoListener {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale PT_BR = Locale.of("pt", "BR");

    @Override
    public void visualizarPedido(PedidoAbstract pedido) {
        String detalhes = String.format(PT_BR, """
                Pedido: #%s
                Aberto em: %s
                Cliente: %s
                Endereco: %s
                Valor total: R$ %,.2f
                Situacao: %s
                Pagamento: %s
                Credito gerado: R$ %,.2f
                """,
                pedido.getIdentificador(),
                DATA_HORA.format(pedido.getDataHora()),
                pedido.getNomeCliente(),
                pedido.getEnderecoEntrega(),
                pedido.getValorTotal(),
                pedido.getSituacao(),
                pedido.isPagamentoRegistrado() ? pedido.getFormaPagamento() : "Aguardando pagamento",
                pedido.getCreditoGerado());
        JOptionPane.showMessageDialog(null, detalhes, "Pedido #" + pedido.getIdentificador(),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
