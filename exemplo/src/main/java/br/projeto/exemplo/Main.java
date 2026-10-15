package br.projeto.exemplo;

import br.projeto.painelpedidos.aplicacao.PainelPedidoFacade;
import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.configuracao.LeitorConfiguracaoPainelPedido;
import br.projeto.painelpedidos.tempo.IRelogioService;
import br.projeto.painelpedidos.tempo.RelogioServiceSistema;
import br.projeto.painelpedidos.som.SomTomStrategy;
import javax.swing.UIManager;

/**
 * Consumidor de referencia: liga repositorio, listeners e configuracao ao painel.
 */
public class Main {

    public static void main(String[] argumentos) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        ConfiguracaoPainelPedido configuracao = new LeitorConfiguracaoPainelPedido(
                ".", "painel_pedidos.env").ler();
        IRelogioService relogio = new RelogioServiceSistema();
        RepositorioDePedidosEmMemoria repositorio = new RepositorioDePedidosEmMemoria();
        new SemeadorDePedidos(repositorio, relogio.agora()).semear();

        PainelPedidoFacade painelPedido = new PainelPedidoFacade(
                repositorio, configuracao, new SomTomStrategy(880, 200), relogio);
        painelPedido.setVisualizarPedidoListener(new TelaDetalhePedido());
        painelPedido.setConfirmarPagamentoListener(new FluxoDePagamento(relogio));
        painelPedido.setCriarNovoPedidoListener(new CriadorDePedidos(repositorio, relogio));

        if (!painelPedido.iniciar()) {
            System.out.println("O painel ja esta aberto.");
            return;
        }

        // Pedidos que chegam ao sistema de delivery sao empurrados para o painel.
        new ChegadaDePedidos(repositorio, relogio, painelPedido::receberNovosPedidos).iniciar();
    }
}
