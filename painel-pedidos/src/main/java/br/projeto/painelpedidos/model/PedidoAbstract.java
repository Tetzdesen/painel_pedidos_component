package br.projeto.painelpedidos.model;

import br.projeto.painelpedidos.internal.AcessoPedido;
import br.projeto.painelpedidos.internal.estado.EstadoNovo;
import br.projeto.painelpedidos.internal.estado.EstadoPedido;
import br.projeto.painelpedidos.internal.estado.SituacaoPedido;
import br.projeto.painelpedidos.tempo.IRelogioService;
import br.projeto.painelpedidos.tempo.RelogioServiceSistema;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de pedido exibido pelo painel. Traz os dados essenciais e a maquina de
 * estados (GoF State); o consumidor estende esta classe e implementa
 * {@link #criarCopia(String, LocalDateTime)}.
 */
public abstract class PedidoAbstract {

    static {
        AcessoPedido.definirPonte(new AcessoPedido.Ponte() {
            @Override
            public void configurar(PedidoAbstract pedido, IRelogioService relogio) {
                pedido.relogio = Objects.requireNonNull(relogio, "relogio");
            }

            @Override
            public SituacaoPedido situacao(PedidoAbstract pedido) {
                return pedido.estado.situacao();
            }
        });
    }

    private final String identificador;
    private final LocalDateTime dataHora;
    private final String nomeCliente;
    private final String enderecoEntrega;
    private final double valorTotal;

    private EstadoPedido estado = EstadoNovo.INSTANCIA;
    private LocalDateTime dataHoraMudancaEstado;
    private String formaPagamento;
    private LocalDateTime dataHoraPagamento;
    private double creditoGerado;
    private boolean prioridade;

    private IRelogioService relogio = new RelogioServiceSistema();

    protected PedidoAbstract(String identificador, LocalDateTime dataHora, String nomeCliente,
            String enderecoEntrega, double valorTotal) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("O identificador do pedido e obrigatorio.");
        }
        if (valorTotal < 0) {
            throw new IllegalArgumentException("O valor total nao pode ser negativo.");
        }
        this.identificador = identificador;
        this.dataHora = Objects.requireNonNull(dataHora, "dataHora");
        this.nomeCliente = Objects.requireNonNull(nomeCliente, "nomeCliente");
        this.enderecoEntrega = Objects.requireNonNull(enderecoEntrega, "enderecoEntrega");
        this.valorTotal = valorTotal;
        this.dataHoraMudancaEstado = dataHora;
    }

    /**
     * Cria um novo pedido com os mesmos dados do cliente, na situacao Novo.
     * Usado pelo botao "Criar novo pedido" a partir de um pedido cancelado.
     */
    public abstract PedidoAbstract criarCopia(String novoIdentificador, LocalDateTime novaDataHora);

    // ---- Identidade/dados ----

    public String getIdentificador() {
        return identificador;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    /**
     * Forma de pagamento registrada, ou {@code null} se ainda nao houve pagamento.
     */
    public String getFormaPagamento() {
        return formaPagamento;
    }

    public double getCreditoGerado() {
        return creditoGerado;
    }

    /**
     * Descricao da situacao atual (ex.: "Em preparo").
     */
    public String getSituacao() {
        return estado.situacao().getDescricao();
    }

    public LocalDateTime getDataHoraMudancaEstado() {
        return dataHoraMudancaEstado;
    }

    // ---- Estado ----

    public boolean estaNovo() {
        return estado.situacao() == SituacaoPedido.NOVO;
    }

    public boolean estaEmPreparo() {
        return estado.situacao() == SituacaoPedido.EM_PREPARO;
    }

    public boolean estaProntoParaEntrega() {
        return estado.situacao() == SituacaoPedido.PRONTO_PARA_ENTREGA;
    }

    public boolean estaEmRotaDeEntrega() {
        return estado.situacao() == SituacaoPedido.EM_ROTA_DE_ENTREGA;
    }

    public boolean estaEntregue() {
        return estado.situacao() == SituacaoPedido.ENTREGUE;
    }

    public boolean estaCancelado() {
        return estado.situacao() == SituacaoPedido.CANCELADO;
    }

    public boolean isPagamentoRegistrado() {
        return formaPagamento != null && dataHoraPagamento != null;
    }

    public boolean isPrioridade() {
        return prioridade;
    }

    // ---- Acoes ----
    // As formas com data/hora sao as do componente oficial; as formas sem data/hora sao as do
    // README e usam o "agora" do relogio do painel.

    /**
     * Registra o pagamento de um pedido Novo e o leva para Em preparo.
     */
    public void registrarPagamento(String formaPagamento, LocalDateTime dataHora) {
        validarFormaPagamento(formaPagamento);
        exigirDataHora(dataHora);
        EstadoPedido proximo = estado.registrarPagamento();
        aplicarPagamento(formaPagamento, dataHora);
        mudarPara(proximo, dataHora);
    }

    public void registrarPagamento(String formaPagamento) {
        registrarPagamento(formaPagamento, relogio.agora());
    }

    public void marcarProntoParaEntrega(LocalDateTime dataHora) {
        exigirDataHora(dataHora);
        mudarPara(estado.marcarProntoParaEntrega(), dataHora);
    }

    public void marcarProntoParaEntrega() {
        marcarProntoParaEntrega(relogio.agora());
    }

    public void sairParaEntrega(LocalDateTime dataHora) {
        exigirDataHora(dataHora);
        mudarPara(estado.sairParaEntrega(), dataHora);
    }

    public void sairParaEntrega() {
        sairParaEntrega(relogio.agora());
    }

    public void marcarEntregue(LocalDateTime dataHora) {
        exigirDataHora(dataHora);
        mudarPara(estado.marcarEntregue(), dataHora);
    }

    public void marcarEntregue() {
        marcarEntregue(relogio.agora());
    }

    /**
     * Cancela o pedido. Sem pagamento nao ha credito; com pagamento, o credito e o valor pago.
     */
    public void cancelar(LocalDateTime dataHora) {
        exigirDataHora(dataHora);
        EstadoPedido proximo = estado.cancelar();
        confirmarCancelamento();
        mudarPara(proximo, dataHora);
    }

    public void cancelar() {
        cancelar(relogio.agora());
    }

    /**
     * Apenas registra a forma e a data/hora do pagamento, sem mudar a situacao.
     */
    public void aplicarPagamento(String formaPagamento, LocalDateTime dataHora) {
        validarFormaPagamento(formaPagamento);
        this.dataHoraPagamento = Objects.requireNonNull(dataHora, "Informe a data e hora do pagamento valida");
        this.formaPagamento = formaPagamento;
    }

    /**
     * Calcula o credito do cancelamento: o valor pago, ou zero sem pagamento.
     */
    public void confirmarCancelamento() {
        this.creditoGerado = isPagamentoRegistrado() ? valorTotal : 0.0;
    }

    /**
     * Cria, a partir de um pedido cancelado, um novo pedido em Novo e sem pagamento.
     */
    public PedidoAbstract copiarComoNovo(String novoIdentificador, LocalDateTime novaDataHora) {
        estado.validarCopiaComoNovo();
        PedidoAbstract copia = criarCopia(novoIdentificador, novaDataHora);
        if (copia == null || !copia.estaNovo() || copia.isPagamentoRegistrado()) {
            throw new IllegalStateException("criarCopia deve devolver um pedido Novo e sem pagamento.");
        }
        copia.relogio = relogio;
        return copia;
    }

    /**
     * Marca o pedido como prioridade ate a proxima mudanca de estado. O painel marca os pedidos
     * que ultrapassam o SLA da situacao atual.
     */
    public void marcarPrioridade() {
        this.prioridade = true;
    }

    private void mudarPara(EstadoPedido proximo, LocalDateTime dataHora) {
        this.estado = proximo;
        this.dataHoraMudancaEstado = dataHora;
        this.prioridade = false;
    }

    private static void validarFormaPagamento(String formaPagamento) {
        if (formaPagamento == null || formaPagamento.isBlank()) {
            throw new IllegalArgumentException("Informe a forma de pagamento (o texto nao pode ser vazio)");
        }
    }

    private static void exigirDataHora(LocalDateTime dataHora) {
        Objects.requireNonNull(dataHora, "Informe a data e hora da mudanca de estado valida");
    }
}
