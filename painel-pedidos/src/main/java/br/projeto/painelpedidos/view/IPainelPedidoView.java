package br.projeto.painelpedidos.view;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 * View do painel (API igual a do componente oficial): da acesso aos componentes
 * Swing da tela. O comportamento fica no Presenter; a View e passiva.
 */
public interface IPainelPedidoView {

    JPanel getPainelPrincipal();

    /**
     * Maximiza a janela que contem o painel (usado quando o painel tem janela propria).
     */
    void maximizar();

    JButton getBtnAtualizarPainel();

    /**
     * Botao da barra inferior que executa a acao do pedido selecionado.
     */
    JButton getBtnExecutarAcao();

    JButton getBtnFechar();

    JButton getBtnVisualizarPedido();

    JButton getBtnNovoPedido();

    JTextField getTxtBuscaPedidoCliente();

    JLabel getLblNotificacao();

    JLabel getLblData();

    JSpinner getSpnData();

    JComboBox<String> getCmbPeriodo();

    JPanel getPnlPedidosNovo();

    JPanel getPnlPedidosEmPreparo();

    JPanel getPnlPedidosProntoParaEntrega();

    JPanel getPnlPedidosEmRotaDeEntrega();

    JPanel getPnlPedidosEntregue();

    JPanel getPnlPedidosCancelado();

    JLabel getLblPedidoSelecionado();

    JLabel getLblTituloNovo();

    JLabel getLblTituloEmPreparo();

    JLabel getLblTituloProntoParaEntrega();

    JLabel getLblTituloEmRotaDeEntrega();

    JLabel getLblTituloEntregue();

    JLabel getLblTituloCancelado();
}
