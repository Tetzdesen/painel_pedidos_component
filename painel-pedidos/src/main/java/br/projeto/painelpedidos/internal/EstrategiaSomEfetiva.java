package br.projeto.painelpedidos.internal;

import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.som.IEstrategiaSom;

/**
 * Decide qual estrategia de som o painel usa de fato: com SOM_ATIVO=false,
 * a estrategia informada pelo consumidor e substituida pela silenciosa.
 */
public final class EstrategiaSomEfetiva {

    private EstrategiaSomEfetiva() {
    }

    public static IEstrategiaSom de(ConfiguracaoPainelPedido configuracao, IEstrategiaSom informada) {
        return configuracao.isSomAtivo() ? informada : new SomSilenciosoStrategy();
    }
}
