package br.projeto.painelpedidos.internal.estado;

/**
 * Estado do pedido (GoF State). Cada estado sobrescreve apenas as acoes validas
 * para ele e devolve o proximo estado; as demais sao rejeitadas por padrao.
 */
public abstract class EstadoPedido {

    public abstract SituacaoPedido situacao();

    public EstadoPedido registrarPagamento() {
        throw acaoInvalida("confirmar o pagamento de");
    }

    public EstadoPedido marcarProntoParaEntrega() {
        throw acaoInvalida("marcar como pronto para entrega");
    }

    public EstadoPedido sairParaEntrega() {
        throw acaoInvalida("enviar para entrega");
    }

    public EstadoPedido marcarEntregue() {
        throw acaoInvalida("marcar como entregue");
    }

    public EstadoPedido cancelar() {
        return EstadoCancelado.INSTANCIA;
    }

    public void validarCopiaComoNovo() {
        throw acaoInvalida("criar novo pedido a partir de");
    }

    protected IllegalStateException acaoInvalida(String acao) {
        return new IllegalStateException("Nao e possivel " + acao + " um pedido na situacao "
                + situacao().getDescricao() + ".");
    }
}
