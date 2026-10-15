package br.projeto.painelpedidos.internal.presenter;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Busca por numero do pedido ou nome do cliente, sem diferenciar caixa e acentos.
 */
public final class FiltroBusca {

    private FiltroBusca() {
    }

    public static boolean atende(PedidoAbstract pedido, String texto) {
        String termo = normalizar(texto);
        if (termo.startsWith("#")) {
            termo = termo.substring(1);
        }
        if (termo.isEmpty()) {
            return true;
        }
        return normalizar(pedido.getIdentificador()).contains(termo)
                || normalizar(pedido.getNomeCliente()).contains(termo);
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT).trim();
    }
}
