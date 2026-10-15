package br.projeto.painelpedidos.internal;

import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.internal.estado.SituacaoPedido;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Limites de SLA por situacao. Entregue e Cancelado nao tem SLA.
 */
public final class PoliticaSla {

    private final Map<SituacaoPedido, Duration> limites;

    private PoliticaSla(Map<SituacaoPedido, Duration> limites) {
        this.limites = limites;
    }

    public static PoliticaSla de(ConfiguracaoPainelPedido configuracao) {
        Map<SituacaoPedido, Duration> limites = new EnumMap<>(SituacaoPedido.class);
        for (SituacaoPedido situacao : SituacaoPedido.values()) {
            Duration limite = configuracao.getSlaPorSituacao().get(situacao.getDescricao());
            if (limite != null) {
                limites.put(situacao, limite);
            }
        }
        return new PoliticaSla(limites);
    }

    /**
     * Verdadeiro quando o tempo na situacao ultrapassa estritamente o limite.
     */
    public boolean ultrapassou(SituacaoPedido situacao, LocalDateTime desde, LocalDateTime agora) {
        Duration limite = limites.get(situacao);
        if (limite == null) {
            return false;
        }
        return Duration.between(desde, agora).compareTo(limite) > 0;
    }
}
