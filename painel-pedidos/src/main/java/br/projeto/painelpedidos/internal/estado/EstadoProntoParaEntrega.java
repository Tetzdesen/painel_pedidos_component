package br.projeto.painelpedidos.internal.estado;

public final class EstadoProntoParaEntrega extends EstadoPedido {

    public static final EstadoProntoParaEntrega INSTANCIA = new EstadoProntoParaEntrega();

    private EstadoProntoParaEntrega() {
    }

    @Override
    public SituacaoPedido situacao() {
        return SituacaoPedido.PRONTO_PARA_ENTREGA;
    }

    @Override
    public EstadoPedido sairParaEntrega() {
        return EstadoEmRotaDeEntrega.INSTANCIA;
    }
}
