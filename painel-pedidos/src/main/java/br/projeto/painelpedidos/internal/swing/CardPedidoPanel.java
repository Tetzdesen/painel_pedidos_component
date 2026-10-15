package br.projeto.painelpedidos.internal.swing;

import br.projeto.painelpedidos.internal.presenter.CardViewModel;
import br.projeto.painelpedidos.internal.presenter.EventosPainelPedido;
import br.projeto.painelpedidos.internal.presenter.FaixaViewModel;
import br.projeto.painelpedidos.internal.presenter.TipoFaixa;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Card de um pedido, desenhado a partir do {@link CardViewModel}.
 */
public class CardPedidoPanel extends JPanel {

    private final CardViewModel card;

    public CardPedidoPanel(CardViewModel card, Color fundoSelecionado, Supplier<EventosPainelPedido> eventos) {
        this.card = card;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(card.destacado() ? CoresPainel.FUNDO_DESTACADO
                : card.selecionado() ? fundoSelecionado : CoresPainel.FUNDO_CARD);
        Color corBorda = card.destacado() ? CoresPainel.BORDA_DESTACADO
                : card.selecionado() ? CoresPainel.BORDA_SELECIONADO : CoresPainel.BORDA_CARD;
        int espessura = card.destacado() || card.selecionado() ? 2 : 1;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corBorda, espessura),
                BorderFactory.createEmptyBorder(10 - espessura, 12 - espessura, 10, 12 - espessura)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(linha(rotulo(card.numero(), Font.BOLD, 18f, Color.BLACK),
                rotulo(card.horario(), Font.PLAIN, 13f, Color.BLACK)));
        add(Box.createVerticalStrut(4));
        add(linha(rotulo(card.cliente(), Font.PLAIN, 13f, Color.BLACK), null));
        add(linha(rotulo(card.endereco(), Font.PLAIN, 13f, CoresPainel.TEXTO_SECUNDARIO), null));
        add(linha(rotulo(card.valor(), Font.BOLD, 14f, Color.BLACK),
                rotulo(card.formaPagamento(), Font.PLAIN, 13f, Color.BLACK)));
        add(Box.createVerticalStrut(4));
        for (FaixaViewModel faixa : card.faixas()) {
            add(faixa(faixa));
            add(Box.createVerticalStrut(3));
        }
        add(Box.createVerticalStrut(6));

        JPanel botoes = new JPanel(new GridLayout(0, 1, 0, 6));
        botoes.setOpaque(false);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton acao = new JButton(card.textoAcao());
        acao.setEnabled(card.acaoHabilitada());
        acao.addActionListener(evento -> eventos.get().aoAcionarAcaoDoCard(card.identificador()));
        botoes.add(acao);
        if (card.exibeCancelar()) {
            JButton cancelar = new JButton("Cancelar");
            cancelar.addActionListener(evento -> eventos.get().aoCancelarCard(card.identificador()));
            botoes.add(cancelar);
        }
        add(botoes);

        MouseAdapter selecionar = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                eventos.get().aoSelecionarCard(card.identificador());
            }
        };
        registrarClique(this, selecionar);
    }

    public String getIdentificador() {
        return card.identificador();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    private static JPanel linha(JComponent esquerda, JComponent direita) {
        JPanel linha = new JPanel(new BorderLayout(8, 0));
        linha.setOpaque(false);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (direita == null) {
            linha.add(esquerda, BorderLayout.CENTER);
        } else {
            // o texto da esquerda (numero, valor) nunca e truncado; o da direita cede espaco
            linha.add(esquerda, BorderLayout.WEST);
            if (direita instanceof JLabel rotulo) {
                rotulo.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            linha.add(direita, BorderLayout.CENTER);
        }
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, linha.getPreferredSize().height));
        return linha;
    }

    private static JLabel rotulo(String texto, int estilo, float tamanho, Color cor) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(rotulo.getFont().deriveFont(estilo, tamanho));
        rotulo.setForeground(cor);
        return rotulo;
    }

    private static JLabel faixa(FaixaViewModel faixa) {
        boolean prioridade = faixa.tipo() == TipoFaixa.PRIORIDADE;
        JLabel rotulo = rotulo(faixa.texto(), prioridade ? Font.BOLD : Font.PLAIN, 13f,
                CoresPainel.textoFaixa(faixa.tipo()));
        rotulo.setOpaque(true);
        rotulo.setBackground(CoresPainel.fundoFaixa(faixa.tipo()));
        rotulo.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        rotulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        rotulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, rotulo.getPreferredSize().height));
        return rotulo;
    }

    private static void registrarClique(Component componente, MouseAdapter ouvinte) {
        if (componente instanceof AbstractButton) {
            return;
        }
        componente.addMouseListener(ouvinte);
        if (componente instanceof Container container) {
            for (Component filho : container.getComponents()) {
                registrarClique(filho, ouvinte);
            }
        }
    }
}
