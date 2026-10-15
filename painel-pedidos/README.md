# painel-pedidos (componente)

Componente Java Swing que exibe pedidos de delivery em seis raias (Novo, Em preparo,
Pronto para entrega, Em rota de entrega, Entregue, Cancelado), com busca, filtro por
período, prioridade por SLA e ingestão por empurrão. Os dados e as telas são do seu
sistema; o componente não gera nem guarda pedidos.

A API pública é a mesma do componente de referência publicado pelo professor
(`com.github.claytonfraga:painel_pedidos_component:v1.0.3`): mesmos 14 tipos, pacotes e
assinaturas. Código escrito para um compila contra o outro. A mais, por exigência do README da
atividade, `PedidoAbstract` mantém as ações sem data/hora (`registrarPagamento(String)`,
`marcarProntoParaEntrega()`, `sairParaEntrega()`, `marcarEntregue()`, `cancelar()`), que usam o
"agora" do relógio do painel.

## Build

```bash
mvn -U clean verify     # compila e roda a suíte (headless)
mvn -U clean install    # instala br.projeto:painel-pedidos:1.0.0 no repositório local
```

Requer Java 21 e Maven 3.9+. O jar gerado já contém todas as dependências.

## Uso

```java
import br.projeto.painelpedidos.aplicacao.PainelPedidoFacade;
import br.projeto.painelpedidos.configuracao.ConfiguracaoPainelPedido;
import br.projeto.painelpedidos.configuracao.LeitorConfiguracaoPainelPedido;
import br.projeto.painelpedidos.som.SomTomStrategy;
import br.projeto.painelpedidos.tempo.RelogioServiceSistema;

ConfiguracaoPainelPedido configuracao = new LeitorConfiguracaoPainelPedido(".", "painel_pedidos.env").ler();
PainelPedidoFacade painel = new PainelPedidoFacade(meuRepositorio, configuracao,
        new SomTomStrategy(880, 200), new RelogioServiceSistema());
painel.setVisualizarPedidoListener(...);      // obrigatório
painel.setConfirmarPagamentoListener(...);    // obrigatório
painel.setCriarNovoPedidoListener(...);       // obrigatório
painel.iniciar();                             // false se já houver um painel aberto
painel.receberNovosPedidos(List.of(pedido));  // ingestão por empurrão
```

| Pacote (`br.projeto.painelpedidos.`) | Tipos |
|---|---|
| `aplicacao` | `PainelPedidoFacade` |
| `model` | `PedidoAbstract` |
| `repositorio` | `IPedidoRepository` |
| `configuracao` | `ConfiguracaoPainelPedido`, `LeitorConfiguracaoPainelPedido` |
| `listener` | `IVisualizarPedidoListener`, `IConfirmarPagamentoListener`, `ICriarNovoPedidoListener` |
| `som` | `IEstrategiaSom`, `SomTomStrategy` |
| `tempo` | `IRelogioService`, `RelogioServiceSistema` |
| `view` | `IPainelPedidoView`, `PainelPedidoView` |

Todo o resto vive em `br.projeto.painelpedidos.internal` e pode mudar entre versões
(`ContratoPublicoTest` fixa a lista e garante que nenhuma assinatura pública expõe tipo interno).

Você implementa: uma subclasse de `PedidoAbstract` (com `criarCopia`), um `IPedidoRepository` e
os três listeners. O `painel_pedidos.env` precisa das quatro chaves de SLA e de
`IDENTIFICADOR_APLICACAO`, no formato `CHAVE=valor` (valores literais; linhas vazias e iniciadas
por `#` são ignoradas).

## Embutir numa janela sua

Nenhum dos caminhos cria janela, maximiza ou fecha a sua janela; "Fechar" para os monitores e
desliga a view.

```java
// 1) Como no componente oficial: a fachada valida os listeners e garante instância única
if (painel.iniciarEmbutido()) {                       // false se já houver um painel aberto
    minhaJanela.add(painel.getPainelPedido(), BorderLayout.CENTER);
}

// 2) Como no README da atividade (seção 4.3): PainelPedidoView com um PainelPedidoPresenter
PainelPedidoView view = new PainelPedidoView();
minhaJanela.add(view, BorderLayout.CENTER);
PainelPedidoPresenter presenter = new PainelPedidoPresenter(view, meuRepositorio, configuracao,
        new SomTomStrategy(880, 200), new RelogioServiceSistema());
presenter.setVisualizarPedidoListener(...);
presenter.setConfirmarPagamentoListener(...);
presenter.setCriarNovoPedidoListener(...);
presenter.iniciar();                                  // na thread do Swing
```

`PainelPedidoPresenter` fica em `br.projeto.painelpedidos.internal.presenter`. No caminho 2, a
validação dos listeners e a instância única ficam com você; um listener ausente só é avisado ao
operador quando a ação é acionada.

## Tela

A tela (`internal/swing/VisaoPainelSwing`) tem o `.form` do NetBeans ao lado do `.java`; as raias
e os cards são montados por código. `PainelPedidoView` dá acesso aos componentes dela pelos getters
de `IPainelPedidoView` (`getBtnExecutarAcao()`, `getCmbPeriodo()`, `getPnlPedidosNovo()` etc.).

Guia completo: [README da raiz](../README.md). Consumidor de referência: [`exemplo`](../exemplo/).
