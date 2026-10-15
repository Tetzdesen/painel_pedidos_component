package br.projeto.painelpedidos.internal.estado;

/**
 * As seis situacoes fixas do ciclo de vida, na ordem em que o pedido caminha.
 */
public enum SituacaoPedido {
    NOVO("Novo", "Novo"),
    EM_PREPARO("Em preparo", "Em preparo"),
    PRONTO_PARA_ENTREGA("Pronto para entrega", "Pronto pra entrega"),
    EM_ROTA_DE_ENTREGA("Em rota de entrega", "Em rota de entrega"),
    ENTREGUE("Entregue", "Entregue"),
    CANCELADO("Cancelado", "Cancelado");

    private final String descricao;
    private final String rotuloRaia;

    SituacaoPedido(String descricao, String rotuloRaia) {
        this.descricao = descricao;
        this.rotuloRaia = rotuloRaia;
    }

    /**
     * Nome da situacao no modelo e na barra "Pedido selecionado".
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Rotulo do cabecalho da raia, como na figura do README.
     */
    public String getRotuloRaia() {
        return rotuloRaia;
    }
}
