package br.projeto.painelpedidos.internal.presenter;

import java.time.LocalDate;
import java.util.List;

/**
 * Tela vista pelo Presenter (View passiva). Interna: a API publica da View e
 * {@code IPainelPedidoView}, igual a do componente oficial.
 */
public interface VisaoPainel {

    /**
     * Liga os eventos do operador ao Presenter; {@code null} desliga a tela.
     */
    void definirEventos(EventosPainelPedido eventos);

    void exibirOpcoesPeriodo(List<String> opcoes, int indiceSelecionado);

    /**
     * Mostra ou oculta o campo "Data:" (periodo "Data específica").
     */
    void exibirCampoData(boolean visivel, LocalDate data);

    void exibirRaias(List<RaiaViewModel> raias);

    /**
     * Texto da barra "Pedido selecionado".
     */
    void exibirPedidoSelecionado(String texto);

    void habilitarVisualizarPedido(boolean habilitado);

    /**
     * Botao de acao da barra inferior: texto e se pode ser acionado.
     */
    void exibirAcaoDoSelecionado(String texto, boolean habilitado);

    /**
     * Mensagem de notificacao nao modal; vazio limpa a mensagem.
     */
    void exibirMensagem(String mensagem);

    void exibirErro(String titulo, String mensagem);

    /**
     * Pergunta sim/nao ao operador.
     */
    boolean confirmar(String titulo, String mensagem);

    void focarCard(String identificador);

    void maximizar();
}
