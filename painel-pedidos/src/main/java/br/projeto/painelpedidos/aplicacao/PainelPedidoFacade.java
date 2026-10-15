package br.projeto.painelpedidos.aplicacao;

import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.internal.EstrategiaSomEfetiva;
import br.projeto.painelpedidos.internal.TravaInstanciaUnica;
import br.projeto.painelpedidos.internal.presenter.PainelPedidoPresenter;
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
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Ponto unico de uso do componente (GoF Facade): valida as dependencias, garante
 * instancia unica, cria a janela (ou liga o painel embutido) e encaminha a
 * ingestao de pedidos.
 */
public final class PainelPedidoFacade {

    private final IPedidoRepository repositorio;
    private final ConfiguracaoPainelPedido configuracao;
    private final IEstrategiaSom som;
    private final IRelogioService relogio;

    private IVisualizarPedidoListener visualizarPedidoListener;
    private IConfirmarPagamentoListener confirmarPagamentoListener;
    private ICriarNovoPedidoListener criarNovoPedidoListener;

    private final PainelPedidoView view;
    private TravaInstanciaUnica trava;
    private JFrame janela;
    private PainelPedidoPresenter presenter;

    public PainelPedidoFacade(IPedidoRepository repositorio, ConfiguracaoPainelPedido configuracao,
            IEstrategiaSom som, IRelogioService relogio) {
        this.repositorio = Objects.requireNonNull(repositorio, "Informe um repositorio de pedidos valido");
        this.configuracao = Objects.requireNonNull(configuracao, "Informe uma configuracao do painel valida");
        this.som = Objects.requireNonNull(som, "Informe uma estrategia de som valida");
        this.relogio = Objects.requireNonNull(relogio, "Informe um relogio de datas valido");
        PainelPedidoView[] criada = new PainelPedidoView[1];
        naThreadDoSwing(() -> criada[0] = new PainelPedidoView());
        this.view = criada[0];
    }

    public void setVisualizarPedidoListener(IVisualizarPedidoListener listener) {
        this.visualizarPedidoListener = Objects.requireNonNull(listener, "Informe um IVisualizarPedidoListener valido");
    }

    public void setConfirmarPagamentoListener(IConfirmarPagamentoListener listener) {
        this.confirmarPagamentoListener = Objects.requireNonNull(listener,
                "Informe um IConfirmarPagamentoListener valido");
    }

    public void setCriarNovoPedidoListener(ICriarNovoPedidoListener listener) {
        this.criarNovoPedidoListener = Objects.requireNonNull(listener, "Informe um ICriarNovoPedidoListener valido");
    }

    /**
     * Abre o painel em janela propria, maximizada.
     *
     * @return {@code false}, sem abrir nada, se ja houver um painel aberto com o
     *         mesmo IDENTIFICADOR_APLICACAO
     * @throws IllegalStateException se algum dos tres listeners obrigatorios faltar
     */
    public synchronized boolean iniciar() {
        return abrir(() -> {
            JFrame novaJanela = new JFrame(configuracao.getTituloJanela());
            novaJanela.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            novaJanela.setContentPane(view);
            novaJanela.setMinimumSize(new Dimension(1024, 700));
            novaJanela.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evento) {
                    encerrar();
                }
            });
            janela = novaJanela;
            novaJanela.pack();
            novaJanela.setLocationRelativeTo(null);
            ligar(true);
            novaJanela.setVisible(true);
        });
    }

    /**
     * Inicia o painel sem criar janela, para ser embutido numa janela do
     * consumidor: adicione {@link #getPainelPedido()} ao seu layout. Tem as
     * mesmas validacoes e a mesma instancia unica de {@link #iniciar()}; nunca
     * maximiza nem fecha a janela do consumidor.
     *
     * @return {@code false}, sem iniciar nada, se ja houver um painel aberto com o
     *         mesmo IDENTIFICADOR_APLICACAO
     * @throws IllegalStateException se algum dos tres listeners obrigatorios faltar
     */
    public synchronized boolean iniciarEmbutido() {
        return abrir(() -> ligar(false));
    }

    /**
     * A View do painel, criada junto com a fachada.
     */
    public PainelPedidoView getPainelPedido() {
        return view;
    }

    /**
     * Empurra novos pedidos para o painel: salva no repositorio, atualiza o
     * painel e notifica o operador. Pode ser chamado de qualquer thread. Nao
     * deduplica pedidos.
     */
    public void receberNovosPedidos(List<PedidoAbstract> pedidos) {
        if (pedidos == null || pedidos.isEmpty()) {
            return;
        }
        List<PedidoAbstract> copia = new ArrayList<>(pedidos);
        SwingUtilities.invokeLater(() -> {
            PainelPedidoPresenter atual = presenter;
            if (atual != null) {
                atual.receberNovosPedidos(copia);
            } else {
                copia.stream().filter(Objects::nonNull).forEach(repositorio::salvar);
            }
        });
    }

    /**
     * Fecha apenas o painel: para os monitores, desliga a View, descarta a janela
     * propria (se houver) e libera a trava. Nunca fecha a janela nem encerra a
     * aplicacao do consumidor.
     */
    public synchronized void encerrar() {
        if (trava == null) {
            return;
        }
        naThreadDoSwing(() -> {
            if (presenter != null) {
                presenter.encerrar();
                presenter = null;
            }
            if (janela != null) {
                janela.dispose();
                janela = null;
            }
        });
        trava.liberar();
        trava = null;
    }

    private void validarListeners() {
        List<String> ausentes = new ArrayList<>();
        if (visualizarPedidoListener == null) {
            ausentes.add("IVisualizarPedidoListener");
        }
        if (confirmarPagamentoListener == null) {
            ausentes.add("IConfirmarPagamentoListener");
        }
        if (criarNovoPedidoListener == null) {
            ausentes.add("ICriarNovoPedidoListener");
        }
        if (!ausentes.isEmpty()) {
            throw new IllegalStateException("Configure " + ausentes.get(0)
                    + " na aplicacao cliente antes de iniciar o painel");
        }
    }

    private boolean abrir(Runnable montarNaThreadDoSwing) {
        validarListeners();
        if (trava != null) {
            return false;
        }
        TravaInstanciaUnica novaTrava = new TravaInstanciaUnica(configuracao.getIdentificadorAplicacao());
        if (!novaTrava.adquirir()) {
            return false;
        }
        trava = novaTrava;
        try {
            naThreadDoSwing(montarNaThreadDoSwing);
        } catch (RuntimeException | Error erro) {
            encerrar();
            throw erro;
        }
        return true;
    }

    private void ligar(boolean janelaPropria) {
        PainelPedidoPresenter novoPresenter = new PainelPedidoPresenter(AcessoView.visao(view), repositorio,
                configuracao, EstrategiaSomEfetiva.de(configuracao, som), relogio, new AgendadorSwing());
        novoPresenter.setVisualizarPedidoListener(visualizarPedidoListener);
        novoPresenter.setConfirmarPagamentoListener(confirmarPagamentoListener);
        novoPresenter.setCriarNovoPedidoListener(criarNovoPedidoListener);
        novoPresenter.setAoEncerrar(this::encerrar);
        presenter = novoPresenter;
        novoPresenter.iniciar(janelaPropria);
    }

    private static void naThreadDoSwing(Runnable acao) {
        if (SwingUtilities.isEventDispatchThread()) {
            acao.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(acao);
        } catch (InterruptedException interrompido) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompido ao acessar a interface do painel.", interrompido);
        } catch (InvocationTargetException erro) {
            Throwable causa = erro.getCause();
            if (causa instanceof RuntimeException excecao) {
                throw excecao;
            }
            if (causa instanceof Error falha) {
                throw falha;
            }
            throw new IllegalStateException(causa);
        }
    }
}
