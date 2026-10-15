package br.projeto.painelpedidos.internal.estado;

/**
 * Entregue nao avanca, mas ainda pode ser cancelado (com credito do valor pago).
 */
public final class EstadoEntregue extends EstadoPedido {

    public static final EstadoEntregue INSTANCIA = new EstadoEntregue();

    private EstadoEntregue() {
    }

    @Override
    public SituacaoPedido situacao() {
        return SituacaoPedido.ENTREGUE;
    }
}
