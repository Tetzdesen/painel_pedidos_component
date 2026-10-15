package br.projeto.painelpedidos.configuracao;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Configuracao imutavel do painel, normalmente lida de painel_pedidos.env por
 * {@link LeitorConfiguracaoPainelPedido}.
 */
public final class ConfiguracaoPainelPedido {

    private static final Set<String> SITUACOES_COM_SLA =
            Set.of("Novo", "Em preparo", "Pronto para entrega", "Em rota de entrega");

    private final Map<String, Duration> slaPorSituacao;
    private final Duration slaMonitorIntervalo;
    private final int duracaoNotificacaoSegundos;
    private final boolean somAtivo;
    private final String tituloJanela;
    private final String identificadorAplicacao;

    /**
     * @param slaPorSituacao SLA de cada situacao ("Novo", "Em preparo", "Pronto para entrega",
     *        "Em rota de entrega"); Entregue e Cancelado nao tem SLA
     */
    public ConfiguracaoPainelPedido(Map<String, Duration> slaPorSituacao, Duration slaMonitorIntervalo,
            int duracaoNotificacaoSegundos, boolean somAtivo, String tituloJanela,
            String identificadorAplicacao) {
        this.slaPorSituacao = validarSla(slaPorSituacao);
        this.slaMonitorIntervalo = positiva(slaMonitorIntervalo, "a duracao do intervalo do monitor de SLA");
        if (duracaoNotificacaoSegundos < 1) {
            throw new IllegalArgumentException("Informe a duracao da notificacao em segundos com pelo menos 1");
        }
        this.duracaoNotificacaoSegundos = duracaoNotificacaoSegundos;
        this.somAtivo = somAtivo;
        this.tituloJanela = texto(tituloJanela, "um titulo de janela valido");
        this.identificadorAplicacao = texto(identificadorAplicacao, "um identificador da aplicacao valido");
    }

    private static Map<String, Duration> validarSla(Map<String, Duration> sla) {
        Objects.requireNonNull(sla, "Informe um mapa de SLA por situacao valido");
        if (sla.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma situacao de SLA com a sua duracao");
        }
        Map<String, Duration> copia = new LinkedHashMap<>();
        for (Map.Entry<String, Duration> item : sla.entrySet()) {
            String situacao = item.getKey();
            if (situacao == null || situacao.isBlank()) {
                throw new IllegalArgumentException("Informe uma situacao de SLA nao vazia");
            }
            if (!SITUACOES_COM_SLA.contains(situacao)) {
                throw new IllegalArgumentException("Informe uma situacao de SLA valida (" + situacao + ")");
            }
            copia.put(situacao, positiva(item.getValue(), "a duracao do SLA de " + situacao));
        }
        return Collections.unmodifiableMap(copia);
    }

    private static Duration positiva(Duration duracao, String descricao) {
        if (duracao == null) {
            throw new IllegalArgumentException("Informe " + descricao);
        }
        if (duracao.isZero() || duracao.isNegative()) {
            throw new IllegalArgumentException("Informe " + descricao + " com valor maior que zero");
        }
        return duracao;
    }

    private static String texto(String valor, String descricao) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Informe " + descricao + " (o texto nao pode ser vazio)");
        }
        return valor.trim();
    }

    public Map<String, Duration> getSlaPorSituacao() {
        return slaPorSituacao;
    }

    public Duration getSlaMonitorIntervalo() {
        return slaMonitorIntervalo;
    }

    public int getDuracaoNotificacaoSegundos() {
        return duracaoNotificacaoSegundos;
    }

    public boolean isSomAtivo() {
        return somAtivo;
    }

    public String getTituloJanela() {
        return tituloJanela;
    }

    public String getIdentificadorAplicacao() {
        return identificadorAplicacao;
    }
}
