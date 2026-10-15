package br.projeto.painelpedidos.internal.estado;

/**
 * Estado terminal: nenhuma transicao sai daqui; so permite copiar como novo.
 */
public final class EstadoCancelado extends EstadoPedido {

    public static final EstadoCancelado INSTANCIA = new EstadoCancelado();

    private EstadoCancelado() {
    }

    @Override
    public SituacaoPedido situacao() {
        return SituacaoPedido.CANCELADO;
    }

    @Override
    public EstadoPedido cancelar() {
        throw acaoInvalida("cancelar");
    }

    @Override
    public void validarCopiaComoNovo() {
        // permitido apenas a partir de Cancelado
    }
}
