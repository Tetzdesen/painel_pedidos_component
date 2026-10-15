package br.projeto.painelpedidos.configuracao;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Le o arquivo painel_pedidos.env: linhas CHAVE=valor em UTF-8, divididas no
 * primeiro '=' e sem interpretacao de escapes; linhas vazias e iniciadas por '#'
 * sao ignoradas.
 * O arquivo, as quatro chaves de SLA e IDENTIFICADOR_APLICACAO sao obrigatorios;
 * as demais chaves usam defaults.
 */
public final class LeitorConfiguracaoPainelPedido {

    private final Path arquivo;

    private static final int PADRAO_SLA_MONITOR_INTERVALO_SEGUNDOS = 30;
    private static final int PADRAO_NOTIFICACAO_DURACAO_SEGUNDOS = 10;
    private static final boolean PADRAO_SOM_ATIVO = true;
    private static final String PADRAO_TITULO_JANELA = "Painel de Acompanhamento de Pedidos";

    public LeitorConfiguracaoPainelPedido(String diretorio, String nomeArquivo) {
        if (diretorio == null || diretorio.isBlank()) {
            throw new IllegalArgumentException("Informe um diretorio do arquivo de configuracao valido");
        }
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            throw new IllegalArgumentException("Informe um nome de arquivo de configuracao valido");
        }
        this.arquivo = Path.of(diretorio).resolve(nomeArquivo);
    }

    public ConfiguracaoPainelPedido ler() {
        if (!Files.isRegularFile(arquivo)) {
            throw new IllegalStateException("Arquivo de configuracao nao encontrado: "
                    + arquivo.toAbsolutePath().normalize());
        }
        Map<String, String> propriedades = lerPares();
        Map<String, Duration> slaPorSituacao = new LinkedHashMap<>();
        slaPorSituacao.put("Novo", minutos(propriedades, "SLA_NOVO_MINUTOS"));
        slaPorSituacao.put("Em preparo", minutos(propriedades, "SLA_EM_PREPARO_MINUTOS"));
        slaPorSituacao.put("Pronto para entrega", minutos(propriedades, "SLA_PRONTO_PARA_ENTREGA_MINUTOS"));
        slaPorSituacao.put("Em rota de entrega", minutos(propriedades, "SLA_EM_ROTA_DE_ENTREGA_MINUTOS"));
        return new ConfiguracaoPainelPedido(
                slaPorSituacao,
                Duration.ofSeconds(inteiroOpcional(propriedades, "SLA_MONITOR_INTERVALO_SEGUNDOS",
                        PADRAO_SLA_MONITOR_INTERVALO_SEGUNDOS)),
                inteiroOpcional(propriedades, "NOTIFICACAO_DURACAO_SEGUNDOS", PADRAO_NOTIFICACAO_DURACAO_SEGUNDOS),
                booleanoOpcional(propriedades, "SOM_ATIVO", PADRAO_SOM_ATIVO),
                textoOpcional(propriedades, "TITULO_JANELA", PADRAO_TITULO_JANELA),
                textoObrigatorio(propriedades, "IDENTIFICADOR_APLICACAO"));
    }

    private static Duration minutos(Map<String, String> propriedades, String chave) {
        return Duration.ofMinutes(inteiroObrigatorio(propriedades, chave));
    }

    private Map<String, String> lerPares() {
        List<String> linhas;
        try {
            linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        } catch (IOException erro) {
            throw new UncheckedIOException("Falha ao ler " + arquivo, erro);
        }
        Map<String, String> pares = new HashMap<>();
        for (int indice = 0; indice < linhas.size(); indice++) {
            String linha = linhas.get(indice);
            if (indice == 0 && linha.startsWith("\uFEFF")) {
                linha = linha.substring(1);
            }
            String conteudo = linha.strip();
            if (conteudo.isEmpty() || conteudo.startsWith("#")) {
                continue;
            }
            int separador = conteudo.indexOf('=');
            if (separador <= 0) {
                throw new IllegalStateException("Configuracao invalida em " + arquivo.getFileName()
                        + ", linha " + (indice + 1) + ": esperado CHAVE=valor (linha: '" + conteudo + "').");
            }
            pares.put(conteudo.substring(0, separador).strip(), conteudo.substring(separador + 1).strip());
        }
        return pares;
    }

    private static String valor(Map<String, String> propriedades, String chave) {
        String valor = propriedades.get(chave);
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String textoObrigatorio(Map<String, String> propriedades, String chave) {
        String valor = valor(propriedades, chave);
        if (valor == null) {
            throw new IllegalStateException("Configuracao obrigatoria ausente: " + chave);
        }
        return valor;
    }

    private static String textoOpcional(Map<String, String> propriedades, String chave, String padrao) {
        String valor = valor(propriedades, chave);
        return valor == null ? padrao : valor;
    }

    private static int inteiroObrigatorio(Map<String, String> propriedades, String chave) {
        return inteiroPositivo(textoObrigatorio(propriedades, chave), chave);
    }

    private static int inteiroOpcional(Map<String, String> propriedades, String chave, int padrao) {
        String valor = valor(propriedades, chave);
        return valor == null ? padrao : inteiroPositivo(valor, chave);
    }

    private static int inteiroPositivo(String valor, String chave) {
        try {
            int numero = Integer.parseInt(valor);
            if (numero > 0) {
                return numero;
            }
        } catch (NumberFormatException formatoInvalido) {
            // tratado abaixo
        }
        throw new IllegalStateException("Configuracao invalida: " + chave
                + " deve ser um inteiro positivo (valor: '" + valor + "').");
    }

    private static boolean booleanoOpcional(Map<String, String> propriedades, String chave, boolean padrao) {
        String valor = valor(propriedades, chave);
        if (valor == null) {
            return padrao;
        }
        return switch (valor.toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalStateException("Configuracao invalida: " + chave
                    + " deve ser true ou false (valor: '" + valor + "').");
        };
    }
}
