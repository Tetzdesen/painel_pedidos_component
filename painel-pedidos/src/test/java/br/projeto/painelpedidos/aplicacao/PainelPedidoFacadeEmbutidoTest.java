package br.projeto.painelpedidos.aplicacao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.internal.swing.AcessoView;
import br.projeto.painelpedidos.internal.swing.VisaoPainelSwing;
import br.projeto.painelpedidos.som.SomTomStrategy;
import br.projeto.painelpedidos.support.Configuracoes;
import br.projeto.painelpedidos.support.PedidoTeste;
import br.projeto.painelpedidos.support.RepositorioEspiao;
import br.projeto.painelpedidos.tempo.RelogioServiceSistema;
import br.projeto.painelpedidos.view.PainelPedidoView;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Modo embutido como no componente oficial: iniciarEmbutido() + getPainelPedido().
 * Roda sem ambiente grafico, pois PainelPedidoView e um componente leve.
 */
class PainelPedidoFacadeEmbutidoTest {

    private final RepositorioEspiao repositorio =
            new RepositorioEspiao().com(new PedidoTeste("1001", LocalDateTime.now()));
    private final ConfiguracaoPainelPedido configuracao =
            Configuracoes.comIdentidade(false, "teste-embutido-" + UUID.randomUUID());
    private final List<PainelPedidoFacade> abertas = new ArrayList<>();

    private PainelPedidoFacade fachadaSemListeners() {
        PainelPedidoFacade fachada = new PainelPedidoFacade(repositorio, configuracao,
                new SomTomStrategy(880, 200), new RelogioServiceSistema());
        abertas.add(fachada);
        return fachada;
    }

    private PainelPedidoFacade fachadaCompleta() {
        PainelPedidoFacade fachada = fachadaSemListeners();
        fachada.setVisualizarPedidoListener(pedido -> { });
        fachada.setConfirmarPagamentoListener(pedido -> pedido.registrarPagamento("Pix", LocalDateTime.now()));
        fachada.setCriarNovoPedidoListener(List::of);
        return fachada;
    }

    @AfterEach
    void fechar() {
        abertas.forEach(PainelPedidoFacade::encerrar);
    }

    private static VisaoPainelSwing tela(PainelPedidoView view) {
        return (VisaoPainelSwing) AcessoView.visao(view);
    }

    @Test
    void viewCriadaNoConstrutorEDevolvidaPorGetPainelPedido() {
        PainelPedidoFacade fachada = fachadaSemListeners();

        PainelPedidoView view = fachada.getPainelPedido();

        assertThat(view, sameInstance(fachada.getPainelPedido()));
        assertThat(tela(view).isLigada(), is(false));
    }

    @Test
    void semListenersFalhaENaoLigaAView() {
        PainelPedidoFacade fachada = fachadaSemListeners();
        fachada.setVisualizarPedidoListener(pedido -> { });

        IllegalStateException erro = assertThrows(IllegalStateException.class, fachada::iniciarEmbutido);

        assertThat(erro.getMessage(),
                equalTo("Configure IConfirmarPagamentoListener na aplicacao cliente antes de iniciar o painel"));
        assertThat(tela(fachada.getPainelPedido()).isLigada(), is(false));
    }

    @Test
    void ligaAViewComAMesmaInstanciaUnicaDoModoJanela() throws Exception {
        PainelPedidoFacade fachada = fachadaCompleta();

        assertThat(fachada.iniciarEmbutido(), is(true));
        SwingUtilities.invokeAndWait(() -> { });
        assertThat(tela(fachada.getPainelPedido()).isLigada(), is(true));
        assertThat(texto(fachada.getPainelPedido(), "#1001"), is(true));
        assertThat(fachadaCompleta().iniciarEmbutido(), is(false));
        assertThat(fachadaCompleta().iniciar(), is(false));
    }

    @Test
    void fecharDesligaAViewELiberaATrava() throws Exception {
        PainelPedidoFacade fachada = fachadaCompleta();
        fachada.iniciarEmbutido();

        SwingUtilities.invokeAndWait(() -> fachada.getPainelPedido().getBtnFechar().doClick());

        assertThat(tela(fachada.getPainelPedido()).isLigada(), is(false));
        assertThat(fachadaCompleta().iniciarEmbutido(), is(true));
    }

    @Test
    void ingestaoFuncionaNoModoEmbutido() throws Exception {
        PainelPedidoFacade fachada = fachadaCompleta();
        fachada.iniciarEmbutido();

        fachada.receberNovosPedidos(List.of(new PedidoTeste("1021", LocalDateTime.now())));
        SwingUtilities.invokeAndWait(() -> { });

        assertThat(repositorio.chamadas.contains("salvar:1021"), is(true));
        assertThat(fachada.getPainelPedido().getLblNotificacao().getText(), equalTo("Novo Pedido #1021"));
    }

    @Test
    void recebidoComOPainelFechadoESalvoEApareceNaProximaAbertura() throws Exception {
        PainelPedidoFacade fachada = fachadaCompleta();
        fachada.iniciarEmbutido();
        fachada.encerrar();

        fachada.receberNovosPedidos(List.of(new PedidoTeste("1022", LocalDateTime.now())));
        SwingUtilities.invokeAndWait(() -> { });

        assertThat(repositorio.chamadas.contains("salvar:1022"), is(true));
        assertThat(texto(fachada.getPainelPedido(), "Novo Pedido #1022"), is(false));

        assertThat(fachada.iniciarEmbutido(), is(true));
        SwingUtilities.invokeAndWait(() -> { });
        assertThat(texto(fachada.getPainelPedido(), "#1022"), is(true));
        assertThat(texto(fachada.getPainelPedido(), "Novo Pedido #1022"), is(false));
    }

    @Test
    void componentesDaViewSaoOsDaTela() throws Exception {
        PainelPedidoFacade fachada = fachadaCompleta();
        fachada.iniciarEmbutido();
        SwingUtilities.invokeAndWait(() -> { });
        PainelPedidoView view = fachada.getPainelPedido();

        assertThat(view.getLblTituloNovo().getText(), equalTo("Novo (1)"));
        assertThat(view.getLblTituloProntoParaEntrega().getText(), equalTo("Pronto pra entrega (0)"));
        assertThat(view.getPnlPedidosNovo().getComponentCount() > 0, is(true));
        assertThat(view.getBtnExecutarAcao().getText(), equalTo("Executar ação"));
        assertThat(view.getBtnExecutarAcao().isVisible(), is(false));
        assertThat(view.getLblPedidoSelecionado().getText(),
                equalTo("Selecione um pedido para visualizar detalhes e executar ações."));
        assertThat(view.getCmbPeriodo().getItemAt(4), equalTo("Data específica"));
        assertThat(view.getSpnData().isVisible(), is(false));
        assertThat(view.getLblData().getText(), equalTo("Data:"));

        SwingUtilities.invokeAndWait(() -> view.getPnlPedidosNovo().getComponent(0)
                .dispatchEvent(new java.awt.event.MouseEvent(view.getPnlPedidosNovo().getComponent(0),
                        java.awt.event.MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 1, false)));
        assertThat(view.getBtnExecutarAcao().isVisible(), is(true));
        assertThat(view.getBtnExecutarAcao().getText(), equalTo("Confirmar pagamento"));

        SwingUtilities.invokeAndWait(() -> view.getCmbPeriodo().setSelectedIndex(4));
        assertThat(view.getSpnData().isVisible(), is(true));
        assertThat(view.getPainelPrincipal().isAncestorOf(view.getBtnFechar()), is(true));
        assertThat(view.getTxtBuscaPedidoCliente().isEditable(), is(true));
    }

    private static boolean texto(Container raiz, String procurado) {
        for (Component filho : raiz.getComponents()) {
            if (filho instanceof JLabel rotulo && procurado.equals(rotulo.getText())) {
                return true;
            }
            if (filho instanceof Container container && texto(container, procurado)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void mensagensDeValidacaoDoComponenteOficial() {
        PainelPedidoFacade fachada = fachadaSemListeners();

        NullPointerException listenerNulo = assertThrows(NullPointerException.class,
                () -> fachada.setVisualizarPedidoListener(null));

        assertThat(listenerNulo.getMessage(), containsString("Informe um IVisualizarPedidoListener valido"));
    }
}
