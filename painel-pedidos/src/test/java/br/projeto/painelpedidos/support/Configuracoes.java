package br.projeto.painelpedidos.support;

import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import java.time.Duration;
import java.util.Map;

public final class Configuracoes {

    private Configuracoes() {
    }

    /** SLA: Novo 5, Em preparo 20, Pronto 10, Em rota 10 minutos; monitor 30 s; notificacao 10 s. */
    public static Map<String, Duration> slaPadrao() {
        return Map.of("Novo", Duration.ofMinutes(5), "Em preparo", Duration.ofMinutes(20),
                "Pronto para entrega", Duration.ofMinutes(10), "Em rota de entrega", Duration.ofMinutes(10));
    }

    public static ConfiguracaoPainelPedido padrao() {
        return padrao(true);
    }

    public static ConfiguracaoPainelPedido padrao(boolean somAtivo) {
        return comIdentidade(somAtivo, "teste-painel");
    }

    public static ConfiguracaoPainelPedido comIdentidade(boolean somAtivo, String identificador) {
        return new ConfiguracaoPainelPedido(slaPadrao(), Duration.ofSeconds(30), 10, somAtivo,
                "Painel de Acompanhamento de Pedidos", identificador);
    }
}
