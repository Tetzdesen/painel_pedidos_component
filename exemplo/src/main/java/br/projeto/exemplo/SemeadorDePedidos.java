package br.projeto.exemplo;

import br.projeto.painelpedidos.repositorio.IPedidoRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

/**
 * Carga inicial deterministica do exemplo: os mesmos pedidos (#1001 a #1020),
 * clientes, valores e situacoes a cada execucao. Os horarios sao relativos ao
 * momento da execucao, para que o periodo "Hoje" sempre tenha dados.
 */
public class SemeadorDePedidos {

    private static final String PIX_CPF = "Pix - chave CPF";

    // As transicoes usam a hora de abertura do pedido: o "tempo no estado" conta desde a
    // abertura, e os pedidos antigos ja aparecem como prioridade (como na figura do README).
    private static final Consumer<MeuPedido> PRONTO = pedido -> pedido.marcarProntoParaEntrega(pedido.getDataHora());
    private static final Consumer<MeuPedido> EM_ROTA = pedido -> pedido.sairParaEntrega(pedido.getDataHora());
    private static final Consumer<MeuPedido> ENTREGUE = pedido -> pedido.marcarEntregue(pedido.getDataHora());
    private static final Consumer<MeuPedido> CANCELADO = pedido -> pedido.cancelar(pedido.getDataHora());

    private final IPedidoRepository repositorio;
    private final LocalDateTime agora;

    public SemeadorDePedidos(IPedidoRepository repositorio, LocalDateTime agora) {
        this.repositorio = repositorio;
        this.agora = agora;
    }

    public void semear() {
        // Novo
        pedido("1003", 361, "Carla Rocha", "Rua Nova, 35 - Vila do Sul", 31.00, p -> { });
        pedido("1002", 13, "Bruno Lima", "Av. da Paz, 480 - Jardim", 88.50, p -> { });
        pedido("1001", 4, "Ana Souza", "Rua das Flores, 120 - Centro", 42.90, p -> { });
        pedido("1017", 1, "Bruno Lima", "Av. da Paz, 480 - Jardim", 88.50, p -> { });
        pedido("1015", 23, "Felipe Costa", "Rua do Porto, 77 - Praia", 64.00, p -> { });
        // Em preparo
        pedido("1005", 181, "Eduardo Mendes", "Rua do Comercio, 210 - Sao Miguel", 120.00, pago(PIX_CPF));
        pedido("1004", 41, "Daniel Almeida", "Rua das Palmeiras, 90 - Centro", 57.30, pago(PIX_CPF));
        pedido("1018", 1, "Carla Rocha", "Rua Nova, 35 - Vila do Sul", 31.00, pago(PIX_CPF));
        // Pronto para entrega
        pedido("1007", 56, "Gabriel Ribeiro", "Rua das Rosas, 12 - Jardim", 74.90,
                pago(PIX_CPF).andThen(PRONTO));
        // Em rota de entrega
        pedido("1009", 71, "Igor Gomes", "Rua Sao Miguel, 8 - Sao Miguel", 52.75, emRota(PIX_CPF));
        pedido("1016", 1, "Ana Souza", "Rua das Flores, 120 - Centro", 42.90, emRota("Pix - chave e-mail"));
        // Entregue
        pedido("1011", 301, "Karina Barros", "Rua das Flores, 400 - Centro", 98.00,
                emRota(PIX_CPF).andThen(ENTREGUE));
        // Cancelado (sem pagamento: sem credito)
        pedido("1019", 1, "Daniel Almeida", "Rua das Palmeiras, 90 - Centro", 57.30, CANCELADO);
        pedido("1020", 1, "Eduardo Mendes", "Rua do Comercio, 210 - Sao Miguel", 120.00, CANCELADO);

        // Dias anteriores (visiveis em "Ontem", "Ultimos 7 dias"...)
        anterior("1006", 1, "Helena Prado", "Rua das Acacias, 15 - Centro", 39.90,
                emRota(PIX_CPF).andThen(ENTREGUE));
        anterior("1008", 1, "Joao Pereira", "Av. Brasil, 1500 - Jardim", 105.40,
                emRota("Cartao de credito").andThen(ENTREGUE));
        anterior("1010", 2, "Larissa Nunes", "Rua XV de Novembro, 300 - Centro", 27.50,
                pago(PIX_CPF).andThen(CANCELADO));
        anterior("1012", 3, "Marcos Vieira", "Rua do Sol, 44 - Praia", 66.10,
                emRota("Dinheiro").andThen(ENTREGUE));
        anterior("1013", 5, "Natalia Freitas", "Av. Central, 900 - Sao Miguel", 48.00, CANCELADO);
        anterior("1014", 12, "Otavio Ramos", "Rua das Hortensias, 3 - Jardim", 81.25,
                emRota(PIX_CPF).andThen(ENTREGUE));
    }

    private void pedido(String id, int minutosAtras, String cliente, String endereco, double valor,
            Consumer<MeuPedido> situacao) {
        LocalDateTime inicioDoDia = agora.toLocalDate().atStartOfDay();
        LocalDateTime abertura = agora.minusMinutes(minutosAtras);
        if (abertura.isBefore(inicioDoDia)) {
            abertura = inicioDoDia;
        }
        salvar(id, abertura, cliente, endereco, valor, situacao);
    }

    private void anterior(String id, int diasAtras, String cliente, String endereco, double valor,
            Consumer<MeuPedido> situacao) {
        LocalDateTime abertura = agora.toLocalDate().minusDays(diasAtras).atTime(LocalTime.of(19, 30));
        salvar(id, abertura, cliente, endereco, valor, situacao);
    }

    private void salvar(String id, LocalDateTime abertura, String cliente, String endereco, double valor,
            Consumer<MeuPedido> situacao) {
        MeuPedido pedido = new MeuPedido(id, abertura, cliente, endereco, valor);
        situacao.accept(pedido);
        repositorio.salvar(pedido);
    }

    private static Consumer<MeuPedido> pago(String forma) {
        return pedido -> pedido.registrarPagamento(forma, pedido.getDataHora());
    }

    private static Consumer<MeuPedido> emRota(String forma) {
        return pago(forma).andThen(PRONTO).andThen(EM_ROTA);
    }
}
