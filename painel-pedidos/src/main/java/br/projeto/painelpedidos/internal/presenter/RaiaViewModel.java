package br.projeto.painelpedidos.internal.presenter;

import br.projeto.painelpedidos.internal.estado.SituacaoPedido;
import java.util.List;

public record RaiaViewModel(SituacaoPedido situacao, String titulo, List<CardViewModel> cards) {

    public RaiaViewModel {
        cards = List.copyOf(cards);
    }
}
