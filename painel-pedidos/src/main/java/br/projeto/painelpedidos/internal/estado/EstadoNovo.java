package br.projeto.painelpedidos.internal.estado;

public final class EstadoNovo extends EstadoPedido {

    public static final EstadoNovo INSTANCIA = new EstadoNovo();

    private EstadoNovo() {
    }

    @Override
    public SituacaoPedido situacao() {
        return SituacaoPedido.NOVO;
    }

    @Override
    public EstadoPedido registrarPagamento() {
        return EstadoEmPreparo.INSTANCIA;
    }
}
