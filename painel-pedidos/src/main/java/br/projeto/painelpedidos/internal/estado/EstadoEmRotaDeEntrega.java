package br.projeto.painelpedidos.internal.estado;

public final class EstadoEmRotaDeEntrega extends EstadoPedido {

    public static final EstadoEmRotaDeEntrega INSTANCIA = new EstadoEmRotaDeEntrega();

    private EstadoEmRotaDeEntrega() {
    }

    @Override
    public SituacaoPedido situacao() {
        return SituacaoPedido.EM_ROTA_DE_ENTREGA;
    }

    @Override
    public EstadoPedido marcarEntregue() {
        return EstadoEntregue.INSTANCIA;
    }
}
