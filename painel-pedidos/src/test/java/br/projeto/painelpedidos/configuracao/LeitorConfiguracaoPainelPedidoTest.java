package br.projeto.painelpedidos.configuracao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LeitorConfiguracaoPainelPedidoTest {

    private static final String OBRIGATORIAS = """
            # SLA fixo por estado
            SLA_NOVO_MINUTOS=5
            SLA_EM_PREPARO_MINUTOS=20
            SLA_PRONTO_PARA_ENTREGA_MINUTOS=10
            SLA_EM_ROTA_DE_ENTREGA_MINUTOS=10
            IDENTIFICADOR_APLICACAO=meu-sistema-de-delivery
            """;

    @TempDir
    Path diretorio;

    private ConfiguracaoPainelPedido ler(String conteudo) throws IOException {
        Files.writeString(diretorio.resolve("painel_pedidos.env"), conteudo, StandardCharsets.UTF_8);
        return new LeitorConfiguracaoPainelPedido(diretorio.toString(), "painel_pedidos.env").ler();
    }

    @Test
    void arquivoAusenteFalhaComOCaminhoEsperado() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> new LeitorConfiguracaoPainelPedido(diretorio.toString(), "painel_pedidos.env").ler());

        assertThat(erro.getMessage(), containsString("painel_pedidos.env"));
        assertThat(erro.getMessage(), containsString(diretorio.getFileName().toString()));
    }

    @Test
    void leDoDiretorioInformadoEAplicaDefaults() throws IOException {
        ConfiguracaoPainelPedido configuracao = ler(OBRIGATORIAS);

        assertThat(configuracao.getSlaPorSituacao(), equalTo(Map.of("Novo", Duration.ofMinutes(5),
                "Em preparo", Duration.ofMinutes(20), "Pronto para entrega", Duration.ofMinutes(10),
                "Em rota de entrega", Duration.ofMinutes(10))));
        assertThat(configuracao.getIdentificadorAplicacao(), equalTo("meu-sistema-de-delivery"));
        assertThat(configuracao.getSlaMonitorIntervalo(), equalTo(Duration.ofSeconds(30)));
        assertThat(configuracao.getDuracaoNotificacaoSegundos(), is(10));
        assertThat(configuracao.isSomAtivo(), is(true));
        assertThat(configuracao.getTituloJanela(), equalTo("Painel de Acompanhamento de Pedidos"));
    }

    @Test
    void leChavesOpcionaisInformadas() throws IOException {
        ConfiguracaoPainelPedido configuracao = ler(OBRIGATORIAS + """
                SLA_MONITOR_INTERVALO_SEGUNDOS=15
                NOTIFICACAO_DURACAO_SEGUNDOS=4
                SOM_ATIVO=false
                TITULO_JANELA=Cozinha Central
                """);

        assertThat(configuracao.getSlaMonitorIntervalo(), equalTo(Duration.ofSeconds(15)));
        assertThat(configuracao.getDuracaoNotificacaoSegundos(), is(4));
        assertThat(configuracao.isSomAtivo(), is(false));
        assertThat(configuracao.getTituloJanela(), equalTo("Cozinha Central"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SLA_NOVO_MINUTOS", "SLA_EM_PREPARO_MINUTOS", "SLA_PRONTO_PARA_ENTREGA_MINUTOS",
        "SLA_EM_ROTA_DE_ENTREGA_MINUTOS", "IDENTIFICADOR_APLICACAO"})
    void chaveObrigatoriaAusenteFalhaCitandoAChave(String chave) {
        String semAChave = OBRIGATORIAS.lines().filter(linha -> !linha.startsWith(chave + "="))
                .reduce("", (a, b) -> a + b + "\n");

        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> ler(semAChave));

        assertThat(erro.getMessage(), containsString(chave));
    }

    @Test
    void chaveObrigatoriaVaziaFalha() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> ler(OBRIGATORIAS.replace("IDENTIFICADOR_APLICACAO=meu-sistema-de-delivery",
                        "IDENTIFICADOR_APLICACAO=")));

        assertThat(erro.getMessage(), containsString("IDENTIFICADOR_APLICACAO"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "-3", "2.5"})
    void slaInvalidoFalhaCitandoAChave(String valor) {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> ler(OBRIGATORIAS.replace("SLA_NOVO_MINUTOS=5", "SLA_NOVO_MINUTOS=" + valor)));

        assertThat(erro.getMessage(), containsString("SLA_NOVO_MINUTOS"));
    }

    @Test
    void valorELiteralSemEscapesNemOutrosSeparadores() throws IOException {
        ConfiguracaoPainelPedido configuracao = ler(OBRIGATORIAS
                + "TITULO_JANELA=Loja: Centro \\ Turno! #1\n"
                + "   # comentario com espacos antes\n"
                + "! linha com exclamacao=nao e comentario\n");

        assertThat(configuracao.getTituloJanela(), equalTo("Loja: Centro \\ Turno! #1"));
    }

    @Test
    void identificadorComBarraInvertidaEPreservado() throws IOException {
        ConfiguracaoPainelPedido configuracao = ler(OBRIGATORIAS.replace(
                "IDENTIFICADOR_APLICACAO=meu-sistema-de-delivery", "IDENTIFICADOR_APLICACAO=loja\\norte"));

        assertThat(configuracao.getIdentificadorAplicacao(), equalTo("loja\\norte"));
    }

    @Test
    void linhaSemIgualFalhaCitandoONumeroDaLinha() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> ler("SLA_NOVO_MINUTOS=5\n\nSOM_ATIVO true\n"));

        assertThat(erro.getMessage(), containsString("linha 3"));
    }

    @Test
    void aceitaBomDoUtf8() throws IOException {
        assertThat(ler("\uFEFF" + OBRIGATORIAS).getSlaPorSituacao().get("Novo"), equalTo(Duration.ofMinutes(5)));
    }

    @Test
    void construcaoDiretaRejeitaValoresInvalidos() {
        Duration trinta = Duration.ofSeconds(30);
        IllegalArgumentException vazio = assertThrows(IllegalArgumentException.class,
                () -> new ConfiguracaoPainelPedido(Map.of(), trinta, 10, true, "Painel", "app"));
        assertThat(vazio.getMessage(), containsString("ao menos uma situacao de SLA"));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracaoPainelPedido(
                Map.of("Entregue", Duration.ofMinutes(5)), trinta, 10, true, "Painel", "app"));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracaoPainelPedido(
                Map.of("Novo", Duration.ZERO), trinta, 10, true, "Painel", "app"));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracaoPainelPedido(
                Map.of("Novo", Duration.ofMinutes(5)), trinta, 0, true, "Painel", "app"));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracaoPainelPedido(
                Map.of("Novo", Duration.ofMinutes(5)), trinta, 10, true, " ", "app"));
    }

    @Test
    void mapaDeSlaEImutavel() throws IOException {
        Map<String, Duration> sla = ler(OBRIGATORIAS).getSlaPorSituacao();
        assertThrows(UnsupportedOperationException.class, () -> sla.put("Novo", Duration.ofMinutes(1)));
    }

    @Test
    void somAtivoInvalidoFalha() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> ler(OBRIGATORIAS + "SOM_ATIVO=talvez\n"));

        assertThat(erro.getMessage(), containsString("SOM_ATIVO"));
    }
}
