package br.projeto.painelpedidos.view;

import br.projeto.painelpedidos.internal.estado.SituacaoPedido;
import br.projeto.painelpedidos.internal.swing.AcessoView;
import br.projeto.painelpedidos.internal.swing.VisaoPainelSwing;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 * Painel Swing do componente (barra de filtros, seis raias e barra do pedido
 * selecionado). Para embuti-lo numa janela sua, chame
 * {@code PainelPedidoFacade.iniciarEmbutido()} e adicione
 * {@code getPainelPedido()} ao seu layout.
 */
public class PainelPedidoView extends JPanel implements IPainelPedidoView {

    static {
        AcessoView.definirPonte(view -> view.tela);
    }

    private final VisaoPainelSwing tela = new VisaoPainelSwing();

    public PainelPedidoView() {
        super(new BorderLayout());
        add(tela, BorderLayout.CENTER);
    }

    @Override
    public JPanel getPainelPrincipal() {
        return tela;
    }

    @Override
    public void maximizar() {
        tela.maximizar();
    }

    @Override
    public JButton getBtnAtualizarPainel() {
        return tela.botaoAtualizar();
    }

    @Override
    public JButton getBtnExecutarAcao() {
        return tela.botaoExecutarAcao();
    }

    @Override
    public JButton getBtnFechar() {
        return tela.botaoFechar();
    }

    @Override
    public JButton getBtnVisualizarPedido() {
        return tela.botaoVisualizar();
    }

    @Override
    public JButton getBtnNovoPedido() {
        return tela.botaoNovoPedido();
    }

    @Override
    public JTextField getTxtBuscaPedidoCliente() {
        return tela.campoBusca();
    }

    @Override
    public JLabel getLblNotificacao() {
        return tela.rotuloMensagem();
    }

    @Override
    public JLabel getLblData() {
        return tela.rotuloData();
    }

    @Override
    public JSpinner getSpnData() {
        return tela.seletorData();
    }

    @Override
    public JComboBox<String> getCmbPeriodo() {
        return tela.comboPeriodo();
    }

    @Override
    public JPanel getPnlPedidosNovo() {
        return tela.conteudoDaRaia(SituacaoPedido.NOVO);
    }

    @Override
    public JPanel getPnlPedidosEmPreparo() {
        return tela.conteudoDaRaia(SituacaoPedido.EM_PREPARO);
    }

    @Override
    public JPanel getPnlPedidosProntoParaEntrega() {
        return tela.conteudoDaRaia(SituacaoPedido.PRONTO_PARA_ENTREGA);
    }

    @Override
    public JPanel getPnlPedidosEmRotaDeEntrega() {
        return tela.conteudoDaRaia(SituacaoPedido.EM_ROTA_DE_ENTREGA);
    }

    @Override
    public JPanel getPnlPedidosEntregue() {
        return tela.conteudoDaRaia(SituacaoPedido.ENTREGUE);
    }

    @Override
    public JPanel getPnlPedidosCancelado() {
        return tela.conteudoDaRaia(SituacaoPedido.CANCELADO);
    }

    @Override
    public JLabel getLblPedidoSelecionado() {
        return tela.rotuloSelecionado();
    }

    @Override
    public JLabel getLblTituloNovo() {
        return tela.cabecalhoDaRaia(SituacaoPedido.NOVO);
    }

    @Override
    public JLabel getLblTituloEmPreparo() {
        return tela.cabecalhoDaRaia(SituacaoPedido.EM_PREPARO);
    }

    @Override
    public JLabel getLblTituloProntoParaEntrega() {
        return tela.cabecalhoDaRaia(SituacaoPedido.PRONTO_PARA_ENTREGA);
    }

    @Override
    public JLabel getLblTituloEmRotaDeEntrega() {
        return tela.cabecalhoDaRaia(SituacaoPedido.EM_ROTA_DE_ENTREGA);
    }

    @Override
    public JLabel getLblTituloEntregue() {
        return tela.cabecalhoDaRaia(SituacaoPedido.ENTREGUE);
    }

    @Override
    public JLabel getLblTituloCancelado() {
        return tela.cabecalhoDaRaia(SituacaoPedido.CANCELADO);
    }
}
