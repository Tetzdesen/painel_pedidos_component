package br.projeto.painelpedidos.internal.estado;

public final class EstadoEmPreparo extends EstadoPedido {

    public static final EstadoEmPreparo INSTANCIA = new EstadoEmPreparo();

    private EstadoEmPreparo() {
    }

    @Override
    public SituacaoPedido situacao() {
        return SituacaoPedido.EM_PREPARO;
    }

    @Override
    public EstadoPedido marcarProntoParaEntrega() {
        return EstadoProntoParaEntrega.INSTANCIA;
    }
}
