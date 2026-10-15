package br.projeto.painelpedidos.internal.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import br.projeto.painelpedidos.model.PedidoAbstract;
import br.projeto.painelpedidos.support.AgendadorManual;
import br.projeto.painelpedidos.support.Configuracoes;
import br.projeto.painelpedidos.support.PedidoTeste;
import br.projeto.painelpedidos.support.RelogioAjustavel;
import br.projeto.painelpedidos.support.RepositorioEspiao;
import br.projeto.painelpedidos.support.ViewFalsa;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PainelPedidoPresenterTest {

    private static final LocalDateTime HOJE = LocalDateTime.of(2026, 10, 13, 15, 3);

    private RelogioAjustavel relogio;
    private RepositorioEspiao repositorio;
    private ViewFalsa view;
    private AgendadorManual agendador;
    private PainelPedidoPresenter presenter;
    private int toques;
    private final List<PedidoAbstract> visualizados = new ArrayList<>();
    private List<PedidoAbstract> retornoCriarNovo = List.of();
    private String formaPagamentoDoConsumidor = "Pix - chave CPF";

    private PedidoTeste pedido(String id, int hora, int minuto, String cliente) {
        return new PedidoTeste(id, HOJE.withHour(hora).withMinute(minuto), cliente, "Rua A, 1", 50.0);
    }

    @BeforeEach
    void preparar() {
        relogio = new RelogioAjustavel(HOJE);
        repositorio = new RepositorioEspiao();
        view = new ViewFalsa();
        agendador = new AgendadorManual();
        presenter = new PainelPedidoPresenter(view, repositorio, Configuracoes.padrao(), () -> toques++,
                relogio, agendador);
        presenter.setVisualizarPedidoListener(visualizados::add);
        presenter.setConfirmarPagamentoListener(pedido -> {
            if (formaPagamentoDoConsumidor != null) {
                pedido.registrarPagamento(formaPagamentoDoConsumidor);
            }
        });
        presenter.setCriarNovoPedidoListener(() -> retornoCriarNovo);
    }

    private void iniciarCom(PedidoAbstract... pedidos) {
        repositorio.com(pedidos);
        presenter.iniciar(true);
        repositorio.chamadas.clear();
    }

    @Nested
    class Raias {

        @Test
        void exibeSeisRaiasNaOrdemComContagem() {
            PedidoTeste emPreparo = pedido("1005", 12, 2, "Eduardo Mendes");
            emPreparo.registrarPagamento("Pix");
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"), pedido("1002", 14, 50, "Bruno Lima"), emPreparo);

            assertThat(view.titulos(), contains("Novo (2)", "Em preparo (1)", "Pronto pra entrega (0)",
                    "Em rota de entrega (0)", "Entregue (0)", "Cancelado (0)"));
            assertThat(view.maximizada, is(true));
        }

        @Test
        void modoEmbutidoNaoMaximiza() {
            repositorio.com(pedido("1001", 14, 59, "Ana Souza"));
            presenter.iniciar(false);

            assertThat(view.maximizada, is(false));
            assertThat(view.titulos().get(0), equalTo("Novo (1)"));
        }

        @Test
        void rotuloDaRaiaSegueAFiguraEABarraUsaONomeDaSituacao() {
            PedidoTeste pronto = pedido("1007", 14, 7, "Gabriel Ribeiro");
            pronto.registrarPagamento("Pix");
            pronto.marcarProntoParaEntrega();
            iniciarCom(pronto);

            view.eventos.aoSelecionarCard("1007");

            assertThat(view.titulos().get(2), equalTo("Pronto pra entrega (1)"));
            assertThat(view.selecionado, equalTo("#1007 - Gabriel Ribeiro - Pronto para entrega"));
        }

        @Test
        void ordenaPrioridadePrimeiroEDepoisFifo() {
            iniciarCom(pedido("1017", 15, 2, "Bruno Lima"), pedido("1001", 14, 59, "Ana Souza"),
                    pedido("1002", 14, 50, "Bruno Lima"), pedido("1003", 9, 2, "Carla Rocha"));

            assertThat(view.idsDaRaia(0), contains("1003", "1002", "1001", "1017"));
            assertThat(view.card("1003").orElseThrow().prioridade(), is(true));
            assertThat(view.card("1001").orElseThrow().prioridade(), is(false));
        }
    }

    @Nested
    class Filtros {

        @BeforeEach
        void dados() {
            PedidoTeste ontem = new PedidoTeste("0990", HOJE.minusDays(1), "Carla Rocha", "Rua", 10);
            PedidoTeste semanaPassada = new PedidoTeste("0900", HOJE.minusDays(5), "Igor Gomes", "Rua", 10);
            iniciarCom(pedido("1003", 9, 2, "Carla Rocha"), pedido("1005", 12, 2, "Eduardo Mendes"),
                    pedido("1009", 13, 52, "Igor Gómes"), ontem, semanaPassada);
        }

        @Test
        void periodoPadraoEHoje() {
            assertThat(view.opcoesPeriodo.get(view.periodoSelecionado), equalTo("Hoje (13/10/2026)"));
            assertThat(view.opcoesPeriodo, contains("Hoje (13/10/2026)", "Últimos 7 dias", "Últimos 30 dias",
                    "Todos", "Data específica"));
            assertThat(view.campoDataVisivel, is(false));
            assertThat(view.idsDaRaia(0), contains("1003", "1005", "1009"));
        }

        @Test
        void buscaPorClienteSemCaixa() {
            view.eventos.aoBuscar("rocha");

            assertThat(view.idsDaRaia(0), contains("1003"));
            assertThat(view.titulos().get(0), equalTo("Novo (1)"));
        }

        @Test
        void buscaIgnoraAcentos() {
            view.eventos.aoBuscar("GOMES");
            assertThat(view.idsDaRaia(0), contains("1009"));
        }

        @Test
        void buscaPorNumero() {
            view.eventos.aoBuscar("#1005");
            assertThat(view.idsDaRaia(0), contains("1005"));
        }

        @Test
        void periodos() {
            view.eventos.aoSelecionarPeriodo(1);
            assertThat(view.idsDaRaia(0), contains("0900", "0990", "1003", "1005", "1009"));

            view.eventos.aoSelecionarPeriodo(2);
            assertThat(view.titulos().get(0), equalTo("Novo (5)"));

            view.eventos.aoSelecionarPeriodo(3);
            assertThat(view.titulos().get(0), equalTo("Novo (5)"));
        }

        @Test
        void dataEspecificaMostraOCampoEFiltraPelaData() {
            view.eventos.aoSelecionarPeriodo(4);
            assertThat(view.campoDataVisivel, is(true));
            assertThat(view.dataDoCampo, equalTo(HOJE.toLocalDate()));
            assertThat(view.idsDaRaia(0), contains("1003", "1005", "1009"));

            view.eventos.aoSelecionarData(HOJE.toLocalDate().minusDays(5));
            assertThat(view.idsDaRaia(0), contains("0900"));

            view.eventos.aoSelecionarPeriodo(0);
            assertThat(view.campoDataVisivel, is(false));
        }

        @Test
        void atualizarRecarregaDoRepositorio() {
            repositorio.inserirDiretamente(pedido("1030", 15, 0, "Externo"));
            assertThat(view.card("1030").isPresent(), is(false));

            view.eventos.aoAtualizar();

            assertThat(view.card("1030").isPresent(), is(true));
        }
    }

    @Nested
    class Acoes {

        @Test
        void confirmarPagamentoDelegaAoConsumidorESalva() {
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"));

            view.eventos.aoAcionarAcaoDoCard("1001");

            assertThat(repositorio.obter("1001").estaEmPreparo(), is(true));
            assertThat(repositorio.chamadas.get(0), equalTo("salvar:1001"));
            assertThat(view.indiceDaRaiaDo("1001"), is(1));
        }

        @Test
        void pagamentoNaoConfirmadoMantemNovoESemSalvar() {
            formaPagamentoDoConsumidor = null;
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"));

            view.eventos.aoAcionarAcaoDoCard("1001");

            assertThat(repositorio.obter("1001").estaNovo(), is(true));
            assertThat(repositorio.chamadas, not(hasItem("salvar:1001")));
        }

        @Test
        void avancosSalvamAntesDeAtualizar() {
            PedidoTeste pedido = pedido("1007", 14, 7, "Gabriel Ribeiro");
            pedido.registrarPagamento("Pix");
            iniciarCom(pedido);
            List<Integer> raiaNoMomentoDoSalvar = new ArrayList<>();
            repositorio.aoSalvar = salvo -> raiaNoMomentoDoSalvar.add(view.indiceDaRaiaDo(salvo.getIdentificador()));

            view.eventos.aoAcionarAcaoDoCard("1007");
            assertThat(view.indiceDaRaiaDo("1007"), is(2));
            view.eventos.aoAcionarAcaoDoCard("1007");
            assertThat(view.indiceDaRaiaDo("1007"), is(3));
            view.eventos.aoAcionarAcaoDoCard("1007");
            assertThat(view.indiceDaRaiaDo("1007"), is(4));

            assertThat(raiaNoMomentoDoSalvar, contains(1, 2, 3));
            assertThat(repositorio.chamadas, contains("salvar:1007", "salvar:1007", "salvar:1007"));
        }

        @Test
        void cancelarEntreguePagoGeraCredito() {
            PedidoTeste pedido = pedido("1011", 10, 2, "Karina Barros");
            pedido.registrarPagamento("Pix");
            pedido.marcarProntoParaEntrega();
            pedido.sairParaEntrega();
            pedido.marcarEntregue();
            iniciarCom(pedido);

            view.eventos.aoCancelarCard("1011");

            assertThat(view.confirmacoes, contains("Cancelar pedido|Confirma o cancelamento do pedido #1011?"));
            assertThat(view.indiceDaRaiaDo("1011"), is(5));
            assertThat(repositorio.obter("1011").getCreditoGerado(), is(50.0));
            assertThat(repositorio.chamadas.get(0), equalTo("salvar:1011"));
        }

        @Test
        void criarNovoPedidoNoCardCancelado() {
            PedidoTeste cancelado = pedido("1020", 15, 2, "Eduardo Mendes");
            cancelado.cancelar();
            iniciarCom(cancelado);

            view.eventos.aoAcionarAcaoDoCard("1020");

            assertThat(repositorio.chamadas, contains("gerarProximoIdentificador", "salvar:2001"));
            assertThat(view.indiceDaRaiaDo("2001"), is(0));
            assertThat(repositorio.obter("2001").getDataHora(), equalTo(HOJE));
            assertThat(repositorio.obter("1020").estaCancelado(), is(true));
            assertThat(view.selecionado, equalTo("#2001 - Eduardo Mendes - Novo"));
        }

        @Test
        void cancelamentoNaoConfirmadoNaoMudaNada() {
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"));
            view.respostaConfirmacao = false;

            view.eventos.aoCancelarCard("1001");

            assertThat(repositorio.obter("1001").estaNovo(), is(true));
            assertThat(repositorio.chamadas, empty());
        }

        @Test
        void falhaNaAcaoUsaOTituloDoComponenteOficial() {
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"));
            presenter.setConfirmarPagamentoListener(p -> {
                throw new IllegalStateException("Falha no pagamento");
            });

            view.eventos.aoAcionarAcaoDoCard("1001");

            assertThat(view.titulosDeErro, contains("Não foi possível executar a ação"));
        }

        @Test
        void entregueNaoTemAcao() {
            PedidoTeste pedido = pedido("1011", 10, 2, "Karina Barros");
            pedido.registrarPagamento("Pix");
            pedido.marcarProntoParaEntrega();
            pedido.sairParaEntrega();
            pedido.marcarEntregue();
            iniciarCom(pedido);

            view.eventos.aoAcionarAcaoDoCard("1011");

            assertThat(repositorio.chamadas, not(hasItem("salvar:1011")));
            assertThat(view.erros, empty());
        }
    }

    @Nested
    class SelecaoEBarra {

        @Test
        void selecionarCardMostraTextoEHabilitaBotoes() {
            PedidoTeste cancelado = pedido("1020", 15, 2, "Eduardo Mendes");
            cancelado.cancelar();
            iniciarCom(cancelado, pedido("1001", 14, 59, "Ana Souza"));
            assertThat(view.visualizarHabilitado, is(false));
            assertThat(view.selecionado, equalTo("Selecione um pedido para visualizar detalhes e executar ações."));
            assertThat(view.textoAcao, equalTo("Executar ação"));
            assertThat(view.acaoHabilitada, is(false));

            view.eventos.aoSelecionarCard("1020");

            assertThat(view.selecionado, equalTo("#1020 - Eduardo Mendes - Cancelado"));
            assertThat(view.card("1020").orElseThrow().selecionado(), is(true));
            assertThat(view.visualizarHabilitado, is(true));
            assertThat(view.textoAcao, equalTo("Criar novo pedido"));
            assertThat(view.acaoHabilitada, is(true));

            view.eventos.aoSelecionarCard("1001");
            assertThat(view.textoAcao, equalTo("Confirmar pagamento"));
            assertThat(view.acaoHabilitada, is(true));
        }

        @Test
        void listenerAusenteEAvisadoAoOperador() {
            presenter.setVisualizarPedidoListener(null);
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"));
            view.eventos.aoSelecionarCard("1001");

            view.eventos.aoVisualizarPedido();

            assertThat(view.erros.get(0),
                    equalTo("Configure IVisualizarPedidoListener na aplicacao cliente para visualizar o pedido"));
            assertThat(presenter.isAtivo(), is(true));
        }

        @Test
        void visualizarChamaOListenerComOSelecionado() {
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"));

            view.eventos.aoVisualizarPedido();
            assertThat(visualizados, empty());

            view.eventos.aoSelecionarCard("1001");
            view.eventos.aoVisualizarPedido();

            assertThat(visualizados.get(0).getIdentificador(), equalTo("1001"));
        }

        @Test
        void executarAcaoPelaBarraCriaNovoPedidoDoCancelado() {
            PedidoTeste cancelado = pedido("1020", 15, 2, "Eduardo Mendes");
            cancelado.cancelar();
            iniciarCom(cancelado);
            view.eventos.aoSelecionarCard("1020");

            view.eventos.aoExecutarAcaoDoSelecionado();

            assertThat(view.indiceDaRaiaDo("2001"), is(0));
        }

        @Test
        void executarAcaoPelaBarraAvancaOSelecionado() {
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"));
            view.eventos.aoSelecionarCard("1001");

            view.eventos.aoExecutarAcaoDoSelecionado();

            assertThat(view.indiceDaRaiaDo("1001"), is(1));
            assertThat(view.textoAcao, equalTo("Pronto pra entrega"));
        }

        @Test
        void entregueSelecionadoDesabilitaAAcao() {
            PedidoTeste entregue = pedido("1011", 10, 2, "Karina Barros");
            entregue.registrarPagamento("Pix");
            entregue.marcarProntoParaEntrega();
            entregue.sairParaEntrega();
            entregue.marcarEntregue();
            iniciarCom(entregue);
            view.eventos.aoSelecionarCard("1011");

            view.eventos.aoExecutarAcaoDoSelecionado();

            assertThat(view.textoAcao, equalTo("Pedido finalizado"));
            assertThat(view.acaoHabilitada, is(false));
            assertThat(repositorio.chamadas, empty());
        }

        @Test
        void novoPedidoIngereOsPedidosDevolvidos() {
            iniciarCom();
            retornoCriarNovo = List.of(pedido("3001", 15, 3, "X"), pedido("3002", 15, 3, "Y"));

            view.eventos.aoNovoPedido();

            assertThat(repositorio.chamadas.subList(0, 2), contains("salvar:3001", "salvar:3002"));
            assertThat(view.mensagem, equalTo("2 novos pedidos"));
        }

        @Test
        void novoPedidoSemRetornoNaoMudaNada() {
            iniciarCom();
            int renderizacoes = view.renderizacoes;
            retornoCriarNovo = null;
            view.eventos.aoNovoPedido();
            retornoCriarNovo = List.of();
            view.eventos.aoNovoPedido();

            assertThat(repositorio.chamadas, empty());
            assertThat(view.renderizacoes, is(renderizacoes));
        }

        @Test
        void fecharParaMonitoresEAvisaAFachada() {
            boolean[] encerrado = {false};
            presenter.setAoEncerrar(() -> encerrado[0] = true);
            iniciarCom();

            view.eventos.aoFechar();

            assertThat(encerrado[0], is(true));
            assertThat(agendador.periodica().cancelada, is(true));
            assertThat(presenter.isAtivo(), is(false));
            assertThat(view.eventos, org.hamcrest.Matchers.nullValue());
        }
    }

    @Nested
    class Ingestao {

        @Test
        void umPedidoESalvoSelecionadoDestacadoENotificado() {
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"));

            presenter.receberNovosPedidos(List.of(pedido("1021", 15, 3, "Nova Cliente")));

            assertThat(repositorio.chamadas.get(0), equalTo("salvar:1021"));
            assertThat(view.card("1021").orElseThrow().selecionado(), is(true));
            assertThat(view.card("1021").orElseThrow().destacado(), is(true));
            assertThat(view.focos, contains("1021"));
            assertThat(toques, is(1));
            assertThat(view.mensagem, equalTo("Novo Pedido #1021"));
        }

        @Test
        void variosPedidosMostramAQuantidade() {
            iniciarCom();

            presenter.receberNovosPedidos(List.of(pedido("1021", 15, 0, "A"), pedido("1022", 15, 1, "B"),
                    pedido("1023", 15, 2, "C")));

            assertThat(presenter.getIdentificadorSelecionado(), equalTo("1021"));
            assertThat(presenter.getDestacados(), contains("1021", "1022", "1023"));
            assertThat(view.mensagem, equalTo("3 novos pedidos"));
            assertThat(toques, is(1));
        }

        @Test
        void destaqueTerminaAposADuracaoConfigurada() {
            iniciarCom();
            presenter.receberNovosPedidos(List.of(pedido("1021", 15, 0, "A")));
            assertThat(agendador.unicasAtivas().get(0).tempo, equalTo(Duration.ofSeconds(10)));

            agendador.dispararUnicas();

            assertThat(view.card("1021").orElseThrow().destacado(), is(false));
            assertThat(view.mensagem, equalTo(""));
        }

        @Test
        void listaVaziaOuNulaEIgnorada() {
            iniciarCom();
            presenter.receberNovosPedidos(null);
            presenter.receberNovosPedidos(List.of());

            assertThat(repositorio.chamadas, empty());
            assertThat(toques, is(0));
        }

        @Test
        void naoDeduplica() {
            iniciarCom();
            PedidoTeste pedido = pedido("1021", 15, 0, "A");
            presenter.receberNovosPedidos(List.of(pedido));
            presenter.receberNovosPedidos(List.of(pedido));

            assertThat(repositorio.chamadas.stream().filter("salvar:1021"::equals).count(), is(2L));
            assertThat(toques, is(2));
        }

        @Test
        void acoesEmOutrosCardsNaoLeemORepositorio() {
            PedidoTeste emPreparo = pedido("1005", 12, 2, "Eduardo Mendes");
            emPreparo.registrarPagamento("Pix");
            PedidoTeste cancelado = pedido("1020", 15, 2, "Eduardo Mendes");
            cancelado.cancelar();
            iniciarCom(pedido("1001", 14, 59, "Ana Souza"), emPreparo, cancelado);
            repositorio.inserirDiretamente(pedido("1030", 15, 0, "Externo"));

            view.eventos.aoAcionarAcaoDoCard("1001");
            view.eventos.aoAcionarAcaoDoCard("1005");
            view.eventos.aoCancelarCard("1005");
            view.eventos.aoAcionarAcaoDoCard("1020");
            view.eventos.aoSelecionarCard("1001");
            view.eventos.aoBuscar("");
            view.eventos.aoSelecionarPeriodo(4);
            view.eventos.aoVisualizarPedido();
            presenter.receberNovosPedidos(List.of(pedido("1040", 15, 3, "Empurrado")));

            assertThat(view.card("1030").isPresent(), is(false));
            assertThat(view.card("1040").isPresent(), is(true));
            assertThat(repositorio.chamadas, not(hasItem("buscarTodos")));
        }

        @Test
        void naoHaLeituraAutomatica() {
            iniciarCom();
            repositorio.inserirDiretamente(pedido("1030", 15, 0, "Externo"));

            relogio.avancar(Duration.ofMinutes(10));
            agendador.periodica().acao.run();

            assertThat(view.card("1030").isPresent(), is(false));
            assertThat(repositorio.chamadas, not(hasItem("buscarTodos")));
        }
    }

    @Nested
    class MonitorSla {

        @Test
        void monitorUsaOIntervaloConfiguradoERedesenhaQuandoPrioridadeMuda() {
            iniciarCom(pedido("1001", 15, 0, "Ana Souza"));
            assertThat(agendador.periodica().tempo, equalTo(Duration.ofSeconds(30)));
            assertThat(view.card("1001").orElseThrow().prioridade(), is(false));
            int renderizacoes = view.renderizacoes;

            agendador.periodica().acao.run();
            assertThat(view.renderizacoes, is(renderizacoes));

            relogio.avancar(Duration.ofMinutes(3));
            agendador.periodica().acao.run();

            assertThat(view.card("1001").orElseThrow().prioridade(), is(true));
            assertThat(view.renderizacoes, is(renderizacoes + 1));
        }

        @Test
        void encerrarParaOMonitor() {
            iniciarCom();
            presenter.encerrar();
            assertThat(agendador.periodica().cancelada, is(true));
        }
    }

    @Test
    void erroDeTransicaoEExibidoAoOperador() {
        PedidoTeste pedido = pedido("1001", 14, 59, "Ana Souza");
        iniciarCom(pedido);
        presenter.setConfirmarPagamentoListener(p -> {
            throw new IllegalStateException("Falha no pagamento");
        });

        view.eventos.aoAcionarAcaoDoCard("1001");

        assertThat(view.erros.get(0), containsString("Falha no pagamento"));
    }
}
