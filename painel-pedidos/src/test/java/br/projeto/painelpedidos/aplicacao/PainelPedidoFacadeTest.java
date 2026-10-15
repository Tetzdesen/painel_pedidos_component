package br.projeto.painelpedidos.aplicacao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.tempo.RelogioServiceSistema;
import br.projeto.painelpedidos.som.SomTomStrategy;
import br.projeto.painelpedidos.support.Configuracoes;
import br.projeto.painelpedidos.support.PedidoTeste;
import br.projeto.painelpedidos.support.RepositorioEspiao;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class PainelPedidoFacadeTest {

    private final RepositorioEspiao repositorio = new RepositorioEspiao();
    private final ConfiguracaoPainelPedido configuracao = Configuracoes.padrao();

    private PainelPedidoFacade fachada() {
        return new PainelPedidoFacade(repositorio, configuracao, new SomTomStrategy(880, 200),
                new RelogioServiceSistema());
    }

    @Test
    void construtorExigeTodasAsDependencias() {
        NullPointerException semRepositorio = assertThrows(NullPointerException.class,
                () -> new PainelPedidoFacade(null, configuracao, new SomTomStrategy(880, 200),
                        new RelogioServiceSistema()));
        assertThat(semRepositorio.getMessage(), equalTo("Informe um repositorio de pedidos valido"));
        assertThrows(NullPointerException.class, () -> new PainelPedidoFacade(repositorio, null,
                new SomTomStrategy(880, 200), new RelogioServiceSistema()));
        assertThrows(NullPointerException.class, () -> new PainelPedidoFacade(repositorio, configuracao,
                null, new RelogioServiceSistema()));
        assertThrows(NullPointerException.class, () -> new PainelPedidoFacade(repositorio, configuracao,
                new SomTomStrategy(880, 200), null));
    }

    @Test
    void iniciarSemListenersFalhaNomeandoOsAusentes() {
        PainelPedidoFacade fachada = fachada();
        fachada.setVisualizarPedidoListener(pedido -> { });
        fachada.setCriarNovoPedidoListener(List::of);

        IllegalStateException erro = assertThrows(IllegalStateException.class, fachada::iniciar);

        assertThat(erro.getMessage(),
                equalTo("Configure IConfirmarPagamentoListener na aplicacao cliente antes de iniciar o painel"));
    }

    @Test
    void iniciarSemNenhumListenerFalha() {
        IllegalStateException erro = assertThrows(IllegalStateException.class, fachada()::iniciar);

        assertThat(erro.getMessage(),
                equalTo("Configure IVisualizarPedidoListener na aplicacao cliente antes de iniciar o painel"));
    }

    @Test
    void receberDeOutraThreadComOPainelFechadoAindaSalvaNoRepositorio() throws Exception {
        PainelPedidoFacade fachada = fachada();
        PedidoTeste pedido = new PedidoTeste("1021", LocalDateTime.now());

        Thread produtor = new Thread(() -> fachada.receberNovosPedidos(List.of(pedido)));
        produtor.start();
        produtor.join();
        SwingUtilities.invokeAndWait(() -> { });

        assertThat(repositorio.chamadas, contains("salvar:1021"));
    }

    @Test
    void encerrarSemIniciarNaoFalha() {
        fachada().encerrar();
    }

    @Test
    void componenteNuncaEncerraAAplicacaoDoConsumidor() throws IOException {
        try (Stream<Path> fontes = Files.walk(Path.of("src/main/java"))) {
            for (Path fonte : fontes.filter(caminho -> caminho.toString().endsWith(".java")).toList()) {
                String codigo = Files.readString(fonte);
                assertThat(fonte.toString(), codigo, not(containsString("System.exit")));
                assertThat(fonte.toString(), codigo, not(containsString("EXIT_ON_CLOSE")));
                assertThat(fonte.toString(), codigo, not(containsString("setLookAndFeel")));
            }
        }
    }
}
