package br.projeto.exemplo;

import br.projeto.painelpedidos.listener.IConfirmarPagamentoListener;
import br.projeto.painelpedidos.model.PedidoAbstract;
import br.projeto.painelpedidos.tempo.IRelogioService;
import javax.swing.JOptionPane;

/**
 * O fluxo de pagamento e do consumidor: aqui o operador escolhe a forma de
 * pagamento e, ao confirmar, o pagamento e registrado no proprio pedido.
 */
public class FluxoDePagamento implements IConfirmarPagamentoListener {

    private final IRelogioService relogio;

    public FluxoDePagamento(IRelogioService relogio) {
        this.relogio = relogio;
    }

    static final String[] FORMAS = {
        "Pix - chave CPF", "Pix - chave e-mail", "Cartao de credito", "Cartao de debito", "Dinheiro"
    };

    @Override
    public void confirmarPagamento(PedidoAbstract pedido) {
        Object escolha = JOptionPane.showInputDialog(null,
                "Forma de pagamento do pedido #" + pedido.getIdentificador() + ":",
                "Confirmar pagamento", JOptionPane.QUESTION_MESSAGE, null, FORMAS, FORMAS[0]);
        if (escolha != null) {
            pedido.registrarPagamento(escolha.toString(), relogio.agora());
        }
    }
}
