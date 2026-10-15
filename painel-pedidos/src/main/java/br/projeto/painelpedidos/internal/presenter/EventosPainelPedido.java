package br.projeto.painelpedidos.internal.presenter;

import java.time.LocalDate;

/**
 * Eventos que a View passiva repassa ao Presenter.
 */
public interface EventosPainelPedido {

    void aoBuscar(String texto);

    void aoSelecionarPeriodo(int indice);

    void aoSelecionarData(LocalDate data);

    void aoAtualizar();

    void aoSelecionarCard(String identificador);

    void aoAcionarAcaoDoCard(String identificador);

    void aoCancelarCard(String identificador);

    void aoNovoPedido();

    void aoVisualizarPedido();

    void aoExecutarAcaoDoSelecionado();

    void aoFechar();
}
