package br.projeto.painelpedidos.internal.presenter;

import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.internal.AcessoPedido;
import br.projeto.painelpedidos.internal.AvaliadorSla;
import br.projeto.painelpedidos.internal.EstrategiaSomEfetiva;
import br.projeto.painelpedidos.internal.PoliticaSla;
import br.projeto.painelpedidos.internal.estado.SituacaoPedido;
import br.projeto.painelpedidos.internal.swing.AcessoView;
import br.projeto.painelpedidos.internal.swing.AgendadorSwing;
import br.projeto.painelpedidos.listener.IConfirmarPagamentoListener;
import br.projeto.painelpedidos.listener.ICriarNovoPedidoListener;
import br.projeto.painelpedidos.listener.IVisualizarPedidoListener;
import br.projeto.painelpedidos.repositorio.IPedidoRepository;
import br.projeto.painelpedidos.model.PedidoAbstract;
import br.projeto.painelpedidos.som.IEstrategiaSom;
import br.projeto.painelpedidos.tempo.IRelogioService;
import br.projeto.painelpedidos.view.PainelPedidoView;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Presenter (MVP) do painel: concentra filtros, ordenacao, acoes, ingestao,
 * notificacao e monitor de SLA. A View e passiva; o repositorio e os listeners
 * sao do consumidor. Deve ser usado na thread do Swing.
 */
public class PainelPedidoPresenter implements EventosPainelPedido {

    static final String SEM_SELECAO = "Selecione um pedido para visualizar detalhes e executar ações.";
    static final String EXECUTAR_ACAO = "Executar ação";
    static final String TITULO_ERRO_ACAO = "Não foi possível executar a ação";
    static final String TITULO_ERRO_CANCELAR = "Não foi possível cancelar o pedido";

    private final VisaoPainel view;
    private final IPedidoRepository repositorio;
    private final ConfiguracaoPainelPedido configuracao;
    private final IEstrategiaSom som;
    private final IRelogioService relogio;
    private final Agendador agendador;
    private final AvaliadorSla avaliadorSla;

    private IVisualizarPedidoListener visualizarPedidoListener;
    private IConfirmarPagamentoListener confirmarPagamentoListener;
    private ICriarNovoPedidoListener criarNovoPedidoListener;
    private Runnable aoEncerrar;

    private List<PedidoAbstract> pedidos = new ArrayList<>();
    private String textoBusca = "";
    private Periodo periodo = Periodo.HOJE;
    private LocalDate dataEspecifica;
    private String identificadorSelecionado;
    private Set<String> destacados = new HashSet<>();

    private Agendador.Tarefa monitorSla;
    private Agendador.Tarefa fimDestaque;
    private boolean ativo;

    public PainelPedidoPresenter(VisaoPainel view, IPedidoRepository repositorio,
            ConfiguracaoPainelPedido configuracao, IEstrategiaSom som, IRelogioService relogio,
            Agendador agendador) {
        this.view = Objects.requireNonNull(view, "view");
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
        this.configuracao = Objects.requireNonNull(configuracao, "configuracao");
        this.som = Objects.requireNonNull(som, "som");
        this.relogio = Objects.requireNonNull(relogio, "relogio");
        this.agendador = Objects.requireNonNull(agendador, "agendador");
        this.avaliadorSla = new AvaliadorSla(PoliticaSla.de(configuracao));
    }

    /**
     * Liga o painel a uma {@link PainelPedidoView} que o consumidor embutiu na
     * propria janela (README, secao 4.3). Usa os temporizadores do Swing e, com
     * SOM_ATIVO=false, a estrategia silenciosa. Registre os listeners e chame
     * {@link #iniciar()}; validacao de listeners e instancia unica ficam a cargo
     * do consumidor (ou use {@code PainelPedidoFacade.iniciarEmbutido}).
     */
    public PainelPedidoPresenter(PainelPedidoView view, IPedidoRepository repositorio,
            ConfiguracaoPainelPedido configuracao, IEstrategiaSom som, IRelogioService relogio) {
        this(AcessoView.visao(Objects.requireNonNull(view, "view")), repositorio,
                Objects.requireNonNull(configuracao, "configuracao"),
                EstrategiaSomEfetiva.de(configuracao, Objects.requireNonNull(som, "som")),
                relogio, new AgendadorSwing());
    }

    public void setVisualizarPedidoListener(IVisualizarPedidoListener listener) {
        this.visualizarPedidoListener = listener;
    }

    public void setConfirmarPagamentoListener(IConfirmarPagamentoListener listener) {
        this.confirmarPagamentoListener = listener;
    }

    public void setCriarNovoPedidoListener(ICriarNovoPedidoListener listener) {
        this.criarNovoPedidoListener = listener;
    }

    /**
     * Acao executada quando o operador fecha o painel (a fachada fecha a janela).
     */
    public void setAoEncerrar(Runnable aoEncerrar) {
        this.aoEncerrar = aoEncerrar;
    }

    // ---- Ciclo de vida do painel ----

    /**
     * Inicia no modo embutido: nao maximiza nem altera a janela do consumidor.
     */
    public void iniciar() {
        iniciar(false);
    }

    /**
     * Liga a View, carrega os pedidos e inicia o monitor de SLA.
     *
     * @param maximizarJanela {@code true} quando o painel tem janela propria; no
     *        modo embutido a janela e do consumidor e nao e alterada
     */
    public void iniciar(boolean maximizarJanela) {
        ativo = true;
        view.definirEventos(this);
        exibirOpcoesPeriodo();
        recarregar();
        renderizar();
        if (maximizarJanela) {
            view.maximizar();
        }
        monitorSla = agendador.agendarPeriodico(
                configuracao.getSlaMonitorIntervalo(), this::verificarSla);
    }

    /**
     * Para os monitores e desliga a View, que deixa de reagir a eventos.
     */
    public void encerrar() {
        ativo = false;
        view.definirEventos(null);
        cancelar(monitorSla);
        cancelar(fimDestaque);
        monitorSla = null;
        fimDestaque = null;
    }

    public boolean isAtivo() {
        return ativo;
    }

    /**
     * Reavalia o SLA dos pedidos em memoria, sem consultar o repositorio; so
     * redesenha se algum pedido passou a ser prioridade.
     */
    public void verificarSla() {
        if (!ativo) {
            return;
        }
        if (avaliadorSla.avaliar(pedidos, relogio.agora())) {
            renderizar();
        }
    }

    // ---- Ingestao por empurrao ----

    public void receberNovosPedidos(List<PedidoAbstract> novos) {
        if (novos == null) {
            return;
        }
        List<PedidoAbstract> recebidos = novos.stream().filter(Objects::nonNull).toList();
        if (recebidos.isEmpty()) {
            return;
        }
        for (PedidoAbstract pedido : recebidos) {
            AcessoPedido.configurar(pedido, relogio);
            repositorio.salvar(pedido);
        }
        if (!ativo) {
            return;
        }
        PedidoAbstract primeiro = recebidos.get(0);
        recebidos.forEach(this::exibirNaLista);
        identificadorSelecionado = primeiro.getIdentificador();
        destacados = new LinkedHashSet<>();
        recebidos.forEach(pedido -> destacados.add(pedido.getIdentificador()));
        renderizar();
        view.focarCard(primeiro.getIdentificador());
        som.tocar();
        view.exibirMensagem(recebidos.size() == 1
                ? "Novo Pedido #" + primeiro.getIdentificador()
                : recebidos.size() + " novos pedidos");
        cancelar(fimDestaque);
        fimDestaque = agendador.agendarUmaVez(
                Duration.ofSeconds(configuracao.getDuracaoNotificacaoSegundos()), this::encerrarDestaque);
    }

    private void encerrarDestaque() {
        fimDestaque = null;
        destacados = new HashSet<>();
        if (ativo) {
            view.exibirMensagem("");
            renderizar();
        }
    }

    // ---- Eventos da View ----

    @Override
    public void aoBuscar(String texto) {
        textoBusca = texto == null ? "" : texto;
        renderizar();
    }

    @Override
    public void aoSelecionarPeriodo(int indice) {
        Periodo[] periodos = Periodo.values();
        if (indice >= 0 && indice < periodos.length) {
            periodo = periodos[indice];
            view.exibirCampoData(periodo.exigeData(), dataEspecifica());
            renderizar();
        }
    }

    @Override
    public void aoSelecionarData(LocalDate data) {
        if (data != null) {
            dataEspecifica = data;
            if (periodo.exigeData()) {
                renderizar();
            }
        }
    }

    @Override
    public void aoAtualizar() {
        exibirOpcoesPeriodo();
        recarregar();
        renderizar();
    }

    @Override
    public void aoSelecionarCard(String identificador) {
        if (buscar(identificador).isPresent()) {
            identificadorSelecionado = identificador;
            renderizar();
        }
    }

    @Override
    public void aoAcionarAcaoDoCard(String identificador) {
        Optional<PedidoAbstract> encontrado = buscar(identificador);
        if (encontrado.isEmpty()) {
            return;
        }
        PedidoAbstract pedido = encontrado.get();
        identificadorSelecionado = identificador;
        switch (AcessoPedido.situacao(pedido)) {
            case NOVO -> confirmarPagamento(pedido);
            case EM_PREPARO -> executar(pedido, () -> pedido.marcarProntoParaEntrega(relogio.agora()));
            case PRONTO_PARA_ENTREGA -> executar(pedido, () -> pedido.sairParaEntrega(relogio.agora()));
            case EM_ROTA_DE_ENTREGA -> executar(pedido, () -> pedido.marcarEntregue(relogio.agora()));
            case CANCELADO -> criarNovoAPartirDe(pedido);
            case ENTREGUE -> renderizar();
            default -> throw new IllegalStateException("Situacao desconhecida");
        }
    }

    @Override
    public void aoCancelarCard(String identificador) {
        Optional<PedidoAbstract> encontrado = buscar(identificador);
        if (encontrado.isEmpty()) {
            return;
        }
        PedidoAbstract pedido = encontrado.get();
        identificadorSelecionado = identificador;
        renderizar();
        if (!view.confirmar("Cancelar pedido",
                "Confirma o cancelamento do pedido #" + pedido.getIdentificador() + "?")) {
            return;
        }
        try {
            pedido.cancelar(relogio.agora());
        } catch (RuntimeException erro) {
            view.exibirErro(TITULO_ERRO_CANCELAR, erro.getMessage());
            return;
        }
        repositorio.salvar(pedido);
        renderizar();
    }

    @Override
    public void aoNovoPedido() {
        if (criarNovoPedidoListener == null) {
            view.exibirErro(TITULO_ERRO_ACAO,
                    "Configure ICriarNovoPedidoListener na aplicacao cliente para criar novo pedido");
            return;
        }
        receberNovosPedidos(criarNovoPedidoListener.criarNovoPedido());
    }

    @Override
    public void aoVisualizarPedido() {
        Optional<PedidoAbstract> selecionado = pedidoSelecionado();
        if (selecionado.isEmpty()) {
            return;
        }
        if (visualizarPedidoListener == null) {
            view.exibirErro(TITULO_ERRO_ACAO,
                    "Configure IVisualizarPedidoListener na aplicacao cliente para visualizar o pedido");
            return;
        }
        visualizarPedidoListener.visualizarPedido(selecionado.get());
    }

    /**
     * Botao "Executar ação" da barra inferior: a mesma acao do botao do card selecionado.
     */
    @Override
    public void aoExecutarAcaoDoSelecionado() {
        pedidoSelecionado().filter(MapeadorCard::possuiAcaoExecutavel)
                .ifPresent(pedido -> aoAcionarAcaoDoCard(pedido.getIdentificador()));
    }

    @Override
    public void aoFechar() {
        encerrar();
        if (aoEncerrar != null) {
            aoEncerrar.run();
        }
    }

    // ---- Acoes ----

    private void confirmarPagamento(PedidoAbstract pedido) {
        if (confirmarPagamentoListener == null) {
            view.exibirErro(TITULO_ERRO_ACAO,
                    "Configure IConfirmarPagamentoListener na aplicacao cliente para registrar o pagamento");
            return;
        }
        try {
            confirmarPagamentoListener.confirmarPagamento(pedido);
        } catch (RuntimeException erro) {
            view.exibirErro(TITULO_ERRO_ACAO, erro.getMessage());
        }
        if (pedido.isPagamentoRegistrado()) {
            repositorio.salvar(pedido);
        }
        renderizar();
    }

    private void executar(PedidoAbstract pedido, Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException erro) {
            view.exibirErro(TITULO_ERRO_ACAO, erro.getMessage());
            renderizar();
            return;
        }
        repositorio.salvar(pedido);
        renderizar();
    }

    private void criarNovoAPartirDe(PedidoAbstract cancelado) {
        PedidoAbstract copia;
        try {
            copia = cancelado.copiarComoNovo(repositorio.gerarProximoIdentificador(), relogio.agora());
        } catch (RuntimeException erro) {
            view.exibirErro(TITULO_ERRO_ACAO, erro.getMessage());
            return;
        }
        repositorio.salvar(copia);
        exibirNaLista(copia);
        identificadorSelecionado = copia.getIdentificador();
        renderizar();
        view.focarCard(copia.getIdentificador());
    }

    // ---- Dados e renderizacao ----

    /**
     * Inclui (ou substitui, pelo identificador) um pedido na lista exibida, sem
     * consultar o repositorio: so iniciar, "Atualizar painel" e a ingestao mudam
     * o conjunto de pedidos do painel.
     */
    private void exibirNaLista(PedidoAbstract pedido) {
        AcessoPedido.configurar(pedido, relogio);
        for (int indice = 0; indice < pedidos.size(); indice++) {
            if (pedidos.get(indice).getIdentificador().equals(pedido.getIdentificador())) {
                pedidos.set(indice, pedido);
                return;
            }
        }
        pedidos.add(pedido);
    }

    private void recarregar() {
        List<PedidoAbstract> todos = repositorio.buscarTodos();
        pedidos = todos == null ? new ArrayList<>() : new ArrayList<>(todos);
        pedidos.removeIf(Objects::isNull);
        pedidos.forEach(pedido -> AcessoPedido.configurar(pedido, relogio));
        if (identificadorSelecionado != null && buscar(identificadorSelecionado).isEmpty()) {
            identificadorSelecionado = null;
        }
    }

    private void exibirOpcoesPeriodo() {
        LocalDate hoje = relogio.agora().toLocalDate();
        List<String> opcoes = Arrays.stream(Periodo.values()).map(opcao -> opcao.rotulo(hoje)).toList();
        view.exibirOpcoesPeriodo(opcoes, periodo.ordinal());
        view.exibirCampoData(periodo.exigeData(), dataEspecifica());
    }

    private List<PedidoAbstract> visiveis() {
        LocalDate hoje = relogio.agora().toLocalDate();
        return pedidos.stream()
                .filter(pedido -> periodo.contem(pedido.getDataHora().toLocalDate(), hoje, dataEspecifica()))
                .filter(pedido -> FiltroBusca.atende(pedido, textoBusca))
                .toList();
    }

    private void renderizar() {
        avaliadorSla.avaliar(pedidos, relogio.agora());
        LocalDate hoje = relogio.agora().toLocalDate();
        Map<SituacaoPedido, List<PedidoAbstract>> porSituacao = new EnumMap<>(SituacaoPedido.class);
        for (SituacaoPedido situacao : SituacaoPedido.values()) {
            porSituacao.put(situacao, new ArrayList<>());
        }
        for (PedidoAbstract pedido : visiveis()) {
            porSituacao.get(AcessoPedido.situacao(pedido)).add(pedido);
        }

        List<RaiaViewModel> raias = new ArrayList<>();
        for (Map.Entry<SituacaoPedido, List<PedidoAbstract>> raia : porSituacao.entrySet()) {
            List<PedidoAbstract> ordenados = new ArrayList<>(raia.getValue());
            ordenados.sort(OrdenacaoRaia.COMPARADOR);
            List<CardViewModel> cards = new ArrayList<>();
            for (PedidoAbstract pedido : ordenados) {
                CardViewModel card = MapeadorCard.mapear(pedido, hoje,
                        pedido.getIdentificador().equals(identificadorSelecionado),
                        destacados.contains(pedido.getIdentificador()));
                cards.add(card);
            }
            raias.add(new RaiaViewModel(raia.getKey(),
                    raia.getKey().getRotuloRaia() + " (" + cards.size() + ")", cards));
        }
        view.exibirRaias(raias);

        Optional<PedidoAbstract> selecionado = pedidoSelecionado();
        view.exibirPedidoSelecionado(selecionado.map(PainelPedidoPresenter::textoDaSelecao).orElse(SEM_SELECAO));
        view.habilitarVisualizarPedido(selecionado.isPresent());
        view.exibirAcaoDoSelecionado(selecionado.map(MapeadorCard::textoDaAcao).orElse(EXECUTAR_ACAO),
                selecionado.map(MapeadorCard::possuiAcaoExecutavel).orElse(false));
    }

    private static String textoDaSelecao(PedidoAbstract pedido) {
        return "#" + pedido.getIdentificador() + " - " + pedido.getNomeCliente() + " - " + pedido.getSituacao();
    }

    private LocalDate dataEspecifica() {
        if (dataEspecifica == null) {
            dataEspecifica = relogio.agora().toLocalDate();
        }
        return dataEspecifica;
    }

    private Optional<PedidoAbstract> buscar(String identificador) {
        if (identificador == null) {
            return Optional.empty();
        }
        return pedidos.stream().filter(pedido -> pedido.getIdentificador().equals(identificador)).findFirst();
    }

    private Optional<PedidoAbstract> pedidoSelecionado() {
        return buscar(identificadorSelecionado);
    }

    private static void cancelar(Agendador.Tarefa tarefa) {
        if (tarefa != null) {
            tarefa.cancelar();
        }
    }

    // ---- Leitura de estado para testes e diagnostico ----

    public Set<String> getDestacados() {
        return Collections.unmodifiableSet(destacados);
    }

    public String getIdentificadorSelecionado() {
        return identificadorSelecionado;
    }
}
