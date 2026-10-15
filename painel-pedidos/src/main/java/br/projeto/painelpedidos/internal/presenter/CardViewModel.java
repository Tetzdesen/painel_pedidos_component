package br.projeto.painelpedidos.internal.presenter;

import java.util.List;

/**
 * Tudo o que a View precisa para desenhar um card, ja formatado.
 */
public record CardViewModel(
        String identificador,
        String numero,
        String horario,
        String cliente,
        String endereco,
        String valor,
        String formaPagamento,
        List<FaixaViewModel> faixas,
        boolean prioridade,
        String textoAcao,
        boolean acaoHabilitada,
        boolean exibeCancelar,
        boolean selecionado,
        boolean destacado) {

    public CardViewModel {
        faixas = List.copyOf(faixas);
    }
}
