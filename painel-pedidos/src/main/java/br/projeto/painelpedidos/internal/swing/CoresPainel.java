package br.projeto.painelpedidos.internal.swing;

import br.projeto.painelpedidos.internal.estado.SituacaoPedido;
import br.projeto.painelpedidos.internal.presenter.TipoFaixa;
import java.awt.Color;

/**
 * Paleta do painel: cor de cada raia e de cada tipo de faixa do card.
 */
public final class CoresPainel {

    public static final Color FUNDO_CARD = Color.WHITE;
    public static final Color BORDA_CARD = new Color(0xC8, 0xCD, 0xD4);
    public static final Color BORDA_SELECIONADO = new Color(0x1F, 0x5F, 0xD1);
    public static final Color BORDA_DESTACADO = new Color(0xF0, 0x8C, 0x00);
    public static final Color FUNDO_DESTACADO = new Color(0xFF, 0xF7, 0xE0);
    public static final Color TEXTO_PRINCIPAL = new Color(0x1A, 0x1A, 0x1A);
    public static final Color TEXTO_SECUNDARIO = new Color(0x55, 0x5E, 0x6B);
    public static final Color TEXTO_MENSAGEM = new Color(0x1F, 0x5F, 0xD1);

    private CoresPainel() {
    }

    public static Color cabecalho(SituacaoPedido situacao) {
        return switch (situacao) {
            case NOVO -> new Color(0xE6, 0xEC, 0xF5);
            case EM_PREPARO -> new Color(0xD6, 0xE7, 0xF8);
            case PRONTO_PARA_ENTREGA -> new Color(0xFC, 0xF0, 0xC4);
            case EM_ROTA_DE_ENTREGA -> new Color(0xFB, 0xDF, 0xCF);
            case ENTREGUE -> new Color(0xDC, 0xF0, 0xDB);
            case CANCELADO -> new Color(0xF6, 0xD4, 0xD8);
        };
    }

    public static Color fundoRaia(SituacaoPedido situacao) {
        Color base = cabecalho(situacao);
        return new Color((base.getRed() + 255 * 3) / 4, (base.getGreen() + 255 * 3) / 4,
                (base.getBlue() + 255 * 3) / 4);
    }

    public static Color fundoFaixa(TipoFaixa tipo) {
        return switch (tipo) {
            case ALERTA -> new Color(0xFF, 0xF0, 0xB8);
            case SUCESSO -> new Color(0xE0, 0xF4, 0xDC);
            case NEUTRA -> new Color(0xF2, 0xF3, 0xF5);
            case PRIORIDADE -> new Color(0xC8, 0x00, 0x00);
        };
    }

    public static Color textoFaixa(TipoFaixa tipo) {
        return tipo == TipoFaixa.PRIORIDADE ? Color.WHITE : Color.BLACK;
    }
}
