# Painel de Acompanhamento de Pedidos

[![Release](https://jitpack.io/v/Tetzdesen/painel_pedidos_component.svg)](https://jitpack.io/#Tetzdesen/painel_pedidos_component)

Este projeto é uma **atividade da disciplina Projeto de Sistemas de Software**
(UFES) e é, ele mesmo, um exercício do que a ementa cobra: *"padrões de projeto,
princípio de reuso, construção e uso de componentes de software e frameworks"*.

Nesta atividade ainda se usa NetBeans pela facilidade de construir telas Swing.
Swing, por sua vez, porque a disciplina não tem disciplina de web como
pré-requisito: montar um software do zero, sem frameworks e com o mínimo de
dependências, é a forma de fixar os fundamentos que se quer ensinar. É uma escolha
didática, e só por isso.

O que se entrega é um componente **Java Swing** reutilizável para sistemas de
delivery. Apresenta os pedidos em seis colunas de situação — **Novo**, **Em
preparo**, **Pronto para entrega**, **Em rota de entrega**, **Entregue** e
**Cancelado** — com busca, filtro por período, ações de transição, cancelamento
com crédito, prioridade por SLA e ordenação em fila (FIFO).

O componente é consumido como **dependência Maven**: o seu sistema de delivery
implementa um pequeno conjunto de **portas** (interfaces) e o painel chama de
volta as **suas** telas. Não há simulação, geração de pedidos, seed nem dados
mockados no componente — os dados e as telas são sempre seus.

## Visão geral

A figura abaixo é a tela real do componente em execução, ligada a um repositório
de demonstração. Ela reúne, numa única imagem, tudo o que as seções seguintes
detalham: as seis raias, o topo de filtros e a barra inferior de ações.

![Painel de Acompanhamento de Pedidos em execução](assets/painel-pedidos.png)

**Figura 1 — O painel em execução.** As faixas coloridas no topo da área de
trabalho são as **seis situações** do ciclo de vida, na ordem em que o pedido
caminha: *Novo*, *Em preparo*, *Pronto para entrega*, *Em rota de entrega*,
*Entregue* e *Cancelado*. O número entre parênteses ao lado de cada título é a
contagem de pedidos naquela raia no momento da renderização, e é o que dá ao
operador a leitura imediata da fila.

Cada **card** mostra os dados que o componente recebe do seu modelo — número do
pedido (`#1003`), horário de abertura (`09:02`), cliente, endereço, valor total,
forma de pagamento e crédito gerado — sem que o componente conheça a origem
desses dados. Logo abaixo do valor, uma faixa dispensa o estado do pagamento
("Aguardando pagamento", "Pagamento confirmado", "Pedido cancelado", "Sem crédito
gerado"); quando o pedido **ultrapassa o SLA do estado**, essa faixa dá lugar ao
rótulo vermelho **Prioridade** (visível, por exemplo, em `#1003` e `#1005`), que
empurra o pedido para cima na raia. Os botões inferiores do card são a ação
válida daquele estado ("Confirmar pagamento" em *Novo*, "Sair pra entrega" em
*Pronto para entrega*, "Entregue" em *Em rota de entrega*) acompanhada de
"Cancelar" — a única ação disponível em *Cancelado* é "Criar novo pedido".

No topo, a barra de **Filtros** concentra a busca por pedido ou cliente, a
seleção de **Período** e o botão **Atualizar painel**; é por ali que o operador
restringe o que cada raia exibe. Por fim, a barra **Pedido selecionado**, no pé
da janela, mostra o card em foco (`#1020 - Eduardo Mendes - Cancelado`) e reúne
os botões **Novo Pedido**, **Visualizar pedido**, **Criar novo pedido** e
**Fechar**. A distribuição de pedidos pelas raias, os nomes, valores e horários
desta captura são do repositório de demonstração; no seu sistema, serão os seus
dados — o componente apenas reflete o que o seu repositório entrega.

## O que você precisa fazer e saber

**Fazer** — quatro passos, detalhados na seção 2:

1. adicionar o componente como dependência (seção 1);
2. estender `PedidoAbstract` e implementar `IPedidoRepository` — o modelo e a
   origem de dados são seus;
3. implementar os **três listeners obrigatórios**: visualizar pedido, confirmar
   pagamento e criar novo pedido;
4. criar o `painel_pedidos.env` com as quatro chaves de SLA e o
   `IDENTIFICADOR_APLICACAO` (seção 3).

**Saber** — o que costuma surpreender quem integra:

- **Os dados e as telas são do seu sistema.** O componente não gera, não semeia e
  não guarda pedidos: ele exibe o que você entregar.
- **A ingestão é por empurrão.** Você chama `receberNovosPedidos(...)`; não há
  leitura automática, e o componente **não deduplica** (seção 2.6).
- **O ciclo de vida é fixo**: seis situações, com as transições descritas na seção
  5 — não há como criar uma situação nova sem mexer no componente.
- **O SLA é por estado, em minutos**, vem do `.env`, e marca "Prioridade" quando o
  pedido *ultrapassa* o limite (seção 5).
- **O contrato público tem 14 tipos**; o resto vive sob `internal` e pode mudar
  entre versões (seção 3, "Contrato público").
- **Há um consumidor completo para copiar**: o projeto [`exemplo`](exemplo/) faz
  tudo isso e roda (seção 6). Ele é um projeto independente que depende do
  componente pelo JitPack — o mesmo caminho que o seu sistema vai usar.

## Requisitos

- Java 21 (alvo do bytecode `--release 21`; o build pode rodar em JDK 21 ou
  superior, ex.: 24).
- Maven 3.9+.

## 1. Adicione a dependência

O componente é publicado no [JitPack](https://jitpack.io) a partir das tags do
repositório. As coordenadas abaixo usam o autor de referência; **troque o
usuário pelas suas** (veja a seção 1.1) e a versão pela que quiser:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.SEU-USUARIO</groupId>
    <artifactId>painel_pedidos_component</artifactId>
    <version>v1.0.3</version>
</dependency>
```

O jar do componente **já contém todas as dependências** (é empacotado com
`maven-shade-plugin`), então esta é a única dependência necessária. O JitPack
constrói **no primeiro pedido**, então a primeira resolução pode demorar um
pouco mais.

**Uso local** (antes de publicar): dentro de `painel-pedidos/`, rode
`mvn -U clean install` e dependa de `br.projeto:painel-pedidos:1.0.0` - o
`groupId` de desenvolvimento. No JitPack a coordenada publicada é
`com.github.<usuario>:<repositorio>` (sem pom na raiz, o JitPack publica o
repositório como um artefato único, com `artifactId` igual ao nome do repositório).

### 1.1 Publique o seu próprio componente (template)

Este repositório é um **template**: cada pessoa publica o seu componente, na
própria conta do GitHub, e aponta o exemplo para ele. O ponto único de autoria é
um par de propriedades no [`exemplo/pom.xml`](exemplo/pom.xml):

```xml
<painel-pedidos.groupId>com.github.SEU-USUARIO</painel-pedidos.groupId>
<painel-pedidos.artifactId>painel_pedidos_component</painel-pedidos.artifactId>
<painel-pedidos.version>v1.0.3</painel-pedidos.version>
```

1. Faça um fork (ou clone) do repositório no **seu** GitHub, mantendo-o público.
2. Crie a sua tag e uma Release: `git tag v1.0.3 && git push origin v1.0.3`. O
   workflow `Release` alinha a versão, verifica e publica a Release; o JitPack
   constrói o artefato **no primeiro pedido**.
3. Troque as três propriedades acima para `com.github.<seu-usuario>`, o nome do
   seu repositório e a sua versão. Pronto: o `exemplo` passa a resolver o **seu** artefato.

> **A versão no JitPack é a tag** (`v1.0.3`), e não o número sem o `v`. A imagem
> de build do JitPack traz um Maven antigo; o `jitpack.yml` instala um Maven
> moderno antes do build por causa disso. O workflow [`Verificar
> JitPack`](.github/workflows/verificar-jitpack.yml) aguarda o build e valida o
> `exemplo` contra o artefato publicado — se ele passar, a sua publicação está
> íntegra. Para não depender do JitPack, instale o jar localmente:
> `mvn -U clean install` dentro de `painel-pedidos/` e dependa de
> `br.projeto:painel-pedidos:1.0.0`.

## 2. O que o seu projeto implementa

### 2.1 Estenda `PedidoAbstract` (o modelo)

O componente **não possui pedido concreto**. Estenda
`br.projeto.painelpedidos.model.PedidoAbstract` — ela já traz os dados essenciais
do painel e a **máquina de estados** (GoF State) — e implemente `criarCopia`
(usada pelo botão "Criar novo pedido" a partir de um cancelado):

```java
public class MeuPedido extends PedidoAbstract {

    public MeuPedido(String identificador, LocalDateTime dataHora, String nomeCliente,
            String enderecoEntrega, double valorTotal) {
        super(identificador, dataHora, nomeCliente, enderecoEntrega, valorTotal);
    }

    @Override
    public PedidoAbstract criarCopia(String novoIdentificador, LocalDateTime novaDataHora) {
        return new MeuPedido(novoIdentificador, novaDataHora,
                getNomeCliente(), getEnderecoEntrega(), getValorTotal());
    }
}
```

A `PedidoAbstract` oferece (e o painel usa):

| Grupo | Métodos |
|---|---|
| Identidade/dados | `getIdentificador`, `getDataHora`, `getNomeCliente`, `getEnderecoEntrega`, `getValorTotal`, `getFormaPagamento`, `getCreditoGerado`, `getSituacao`, `getDataHoraMudancaEstado` |
| Estado | `estaNovo`, `estaEmPreparo`, `estaProntoParaEntrega`, `estaEmRotaDeEntrega`, `estaEntregue`, `estaCancelado`, `isPagamentoRegistrado`, `isPrioridade` |
| Ações | `registrarPagamento`, `marcarProntoParaEntrega`, `sairParaEntrega`, `marcarEntregue`, `cancelar`, `copiarComoNovo` |
| Copia | `criarCopia` (**abstrata** — você implementa) |

Regras validadas pela própria máquina de estados: não avança **Novo → Em
preparo** sem pagamento; **Cancelado** não retorna; o crédito é calculado no
cancelamento.

### 2.2 Implemente `IPedidoRepository` (a origem de dados)

```java
public class MeuRepositorioDePedidos implements IPedidoRepository {
    @Override public void salvar(PedidoAbstract pedido) { /* inserir/atualizar */ }
    @Override public List<PedidoAbstract> buscarTodos() { /* listar por período */ }
    @Override public String gerarProximoIdentificador() { /* próximo número */ }
}
```

### 2.3 Implemente `IVisualizarPedidoListener` (**obrigatório**)

O componente **não** abre a tela de detalhes: o botão "Visualizar pedido" chama o
seu listener, e a tela é sua, lendo os seus dados.

```java
public class MinhaTelaDePedido implements IVisualizarPedidoListener {
    @Override
    public void visualizarPedido(PedidoAbstract pedido) {
        // abrir a SUA tela de pedido
    }
}
```

### 2.4 Implemente `IConfirmarPagamentoListener` (**obrigatório**)

O componente **não** escolhe a forma de pagamento: a ação de um pedido **Novo**
chama o seu listener, e o fluxo é seu. Ao confirmar, registre o pagamento no
próprio pedido.

```java
public class MeuPagamento implements IConfirmarPagamentoListener {
    @Override
    public void confirmarPagamento(PedidoAbstract pedido) {
        // abrir o SEU fluxo de pagamento e, ao confirmar:
        pedido.registrarPagamento("Pix - chave CPF");
    }
}
```

> **Sem os três listeners (visualizar, pagamento e novo pedido), `iniciar()`
> lança `IllegalStateException`.** Eles são a forma de o componente devolver ao
> cliente as telas e ações que são do cliente.

### 2.5 Implemente `ICriarNovoPedidoListener` (**obrigatório**)

O botão **"Novo Pedido"** (à esquerda de "Visualizar pedido") exige este
listener. Ele **devolve** os pedidos a inserir; o componente os salva e atualiza
o painel — o componente **não** cria pedidos.

```java
public class MeuCriadorDePedido implements ICriarNovoPedidoListener {
    @Override
    public List<PedidoAbstract> criarNovoPedido() {
        // você decide quais pedidos e quantos; devolva a lista
        return listaDeNovosPedidos;
    }
}
```

### 2.6 Coloque novos pedidos no painel (empurrão)

Não há simulação nem leitura automática. Quando um novo pedido chega no **seu**
sistema, **você empurra** para o painel — e ele se atualiza sozinho. Aceita
`List<PedidoAbstract>`, inclusive um só pedido:

```java
// um pedido
painelPedido.receberNovosPedidos(List.of(meuPedido));

// ou vários de uma vez
painelPedido.receberNovosPedidos(listaDeNovosPedidos);
```

Ao receber, o componente: salva no seu `IPedidoRepository`, **atualiza o
painel**, seleciona/foca o primeiro card, emite o alerta sonoro, mostra a
mensagem ("Novo Pedido #N" para um, "N novos pedidos" para vários) e destaca os
cards por `NOTIFICACAO_DURACAO_SEGUNDOS`.

### 2.7 (Opcional) Som e relógio

- `IEstrategiaSom` — como tocar o alerta. Default: `SomTomStrategy(frequencia,
  duracaoMs)`. Desligue com `SOM_ATIVO=false` (usa `SomSilenciosoStrategy`).
- `IRelogioService` — de onde vem "agora". Default: `RelogioServiceSistema`.

Ambos têm implementação default; só informe se quiser trocar.

## 3. Configuração `painel_pedidos.env` (**obrigatória**)

No diretório de trabalho da sua aplicação, crie `painel_pedidos.env` com as
chaves abaixo:

```properties
# SLA fixo por estado, em minutos (OBRIGATÓRIO)
SLA_NOVO_MINUTOS=5
SLA_EM_PREPARO_MINUTOS=20
SLA_PRONTO_PARA_ENTREGA_MINUTOS=10
SLA_EM_ROTA_DE_ENTREGA_MINUTOS=10

# Monitor de SLA e notificação
SLA_MONITOR_INTERVALO_SEGUNDOS=30
NOTIFICACAO_DURACAO_SEGUNDOS=10

# Som e título
SOM_ATIVO=true
TITULO_JANELA=Painel de Acompanhamento de Pedidos

# Identidade da aplicação (OBRIGATÓRIO): distingue este painel dos demais
# na mesma máquina; o arquivo de controle da instância única deriva dela
IDENTIFICADOR_APLICACAO=meu-sistema-de-delivery
```

- O **arquivo** é obrigatório: ausente, `LeitorConfiguracaoPainelPedido` lança
  exceção.
- As **quatro chaves de SLA por estado** também são obrigatórias, e a
  **identidade da aplicação** (`IDENTIFICADOR_APLICACAO`) também.
- As demais chaves usam defaults seguros.

Não quer o arquivo no diretório de trabalho? Aponte o diretório no construtor:
`new LeitorConfiguracaoPainelPedido(diretorio, "painel_pedidos.env").ler()`.

### Contrato público

São API apenas estes tipos: `PainelPedidoFacade`, `PedidoAbstract`,
`IPedidoRepository`, `ConfiguracaoPainelPedido`,
`LeitorConfiguracaoPainelPedido`, os três listeners, `IEstrategiaSom`,
`SomTomStrategy`, `IRelogioService`, `RelogioServiceSistema`,
`IPainelPedidoView` e `PainelPedidoView`. Todo o resto vive sob
`br.projeto.painelpedidos.internal.*` e pode mudar entre versões — a lista é
fixada por teste (`ContratoPublicoTest`), que falha nomeando o tipo que entrar
ou sair.

## 4. Executando

### 4.1 Fachada `PainelPedidoFacade`

O componente **não** define Look and Feel (é decisão da sua aplicação). As duas
ações do cliente são obrigatórias.

```java
UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

ConfiguracaoPainelPedido configuracao = new LeitorConfiguracaoPainelPedido(
        ".", "painel_pedidos.env").ler();

PainelPedidoFacade painelPedido = new PainelPedidoFacade(
        meuRepositorio, configuracao, new SomTomStrategy(880, 200), new RelogioServiceSistema());
painelPedido.setVisualizarPedidoListener(minhaTelaDePedido);   // obrigatório
painelPedido.setConfirmarPagamentoListener(meuPagamento);      // obrigatório
painelPedido.setCriarNovoPedidoListener(meuCriadorDePedido);   // obrigatório

if (!painelPedido.iniciar()) {
    System.out.println("O painel ja esta aberto.");
}
```

O construtor da fachada é **único** e exige repositório, configuração, estratégia
de som e relógio (use os defaults `SomTomStrategy` e `RelogioServiceSistema`).

Métodos da fachada: `iniciar()` (devolve `boolean`),
`receberNovosPedidos(List<PedidoAbstract>)`, `encerrar()`,
`setVisualizarPedidoListener(...)`, `setConfirmarPagamentoListener(...)`,
`setCriarNovoPedidoListener(...)`.

### 4.2 Instância única (do componente)

O componente cuida disso sozinho: `iniciar()` adquire uma trava de arquivo antes
de criar a janela e devolve `false` quando já há um painel aberto, **sem abrir
nada**. `encerrar()` libera a trava, então fechar e reabrir volta a funcionar. O
consumidor não configura trava, caminho nem singleton; a mensagem ao usuário é
sua (o componente apenas sinaliza).

### 4.3 Embutir em uma aplicação Swing existente

Use `PainelPedidoView` (`JPanel`, implementa `IPainelPedidoView`) com um
`PainelPedidoPresenter`. A janela é criada pela fachada (`JFrame`).

## 5. Comportamento do painel

### Ciclo de vida do pedido

`Novo → Em preparo → Pronto para entrega → Em rota de entrega → Entregue`;
qualquer estado não cancelado pode ir para `Cancelado`; `Cancelado` não retorna.
Cada card tem a ação do estado e um botão "Cancelar" (exceto em `Cancelado`).

### Cancelamento e crédito

Cancelamento em qualquer pedido não cancelado (inclusive `Entregue`). Sem
pagamento, nenhum crédito; com pagamento, crédito igual ao valor pago. Um pedido
`Cancelado` permite "Criar novo pedido" (novo número, nova data/hora, situação
`Novo`, sem pagamento).

### Prioridade por SLA

Cada estado com SLA tem um limite fixo em minutos (tabela acima). Pedidos que
ultrapassam ganham o rótulo vermelho **"Prioridade"** (removido ao mudar de
estado) e sobem na raia. Entregue e Cancelado não têm SLA.

### Ingestão e notificação

Novos pedidos entram por **empurrão**: o seu sistema chama
`receberNovosPedidos(List<PedidoAbstract>)`. Ao receber, o componente salva no
seu repositório, atualiza o painel, seleciona/foca o primeiro, emite alerta
sonoro, exibe a mensagem por cardinalidade ("Novo Pedido #N" ou "N novos
pedidos") e destaca os cards por `NOTIFICACAO_DURACAO_SEGUNDOS`.

### Janela

O painel **inicia sempre maximizado** (comando da Presenter para a View passiva).
O botão **Fechar** encerra **apenas o painel** e para os monitores internos; o
componente **nunca** encerra a sua aplicação.

## 6. Exemplo completo

```java
public class MeuMain {

    public static void main(String[] argumentos) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        ConfiguracaoPainelPedido configuracao = new LeitorConfiguracaoPainelPedido(
                ".", "painel_pedidos.env").ler();
        IPedidoRepository repositorio = new MeuRepositorioDePedidos();

        PainelPedidoFacade painelPedido = new PainelPedidoFacade(
                repositorio, configuracao, new SomTomStrategy(880, 200), new RelogioServiceSistema());
        painelPedido.setVisualizarPedidoListener(new MinhaTelaDePedido());
        painelPedido.setConfirmarPagamentoListener(new MeuPagamento());
        painelPedido.setCriarNovoPedidoListener(new MeuCriadorDePedido());

        if (!painelPedido.iniciar()) {
            System.out.println("O painel ja esta aberto.");
        }
    }
}
```

Há um consumidor de referência no projeto [`exemplo`](exemplo/) (repositório em
memória, seeder determinístico e `painel_pedidos.env`). Ele é um projeto Maven
independente que depende do componente **pelo JitPack**, na versão declarada na
propriedade `painel-pedidos.version` do `exemplo/pom.xml`:

```bash
cd exemplo
mvn -U clean verify
java -jar target/exemplo-painel-pedidos-1.0.0.jar
```

## 7. Projetos, build e testes

São **dois projetos Maven independentes**, cada um com o próprio `pom.xml`:

| Projeto | Papel | Como construir |
|---|---|---|
| `painel-pedidos/` | O componente (biblioteca, jar com todas as dependências) | `cd painel-pedidos && mvn -U clean verify` |
| `exemplo/` | Consumidor de demonstração, que depende do componente publicado no JitPack | `cd exemplo && mvn -U clean verify` |

Não há `pom.xml` agregador na raiz: cada projeto é construído dentro do seu
diretório. O `exemplo` é, por isso, um **teste vivo da publicação** — ele resolve
o artefato do JitPack como qualquer consumidor faria, então uma quebra na
publicação aparece como falha no build do exemplo.

O exemplo consome a versão publicada; para trocá-la, edite a propriedade
`painel-pedidos.version` em `exemplo/pom.xml` (o workflow de release faz isso
sozinho a cada nova tag).

A suíte (JUnit Jupiter + Hamcrest) cobre o ciclo de vida, a configuração, a
ingestão (empurrão), a fachada, os listeners e a maximização, além de garantir a
ausência de simulação/dados mockados e da leitura automática de novos pedidos.

### Publicação (CI/CD)

| Workflow | Quando | O que faz |
|---|---|---|
| [`qualidade.yml`](.github/workflows/qualidade.yml) | push/PR em `main` | Gate do componente (`mvn -U clean verify`) e, quando o artefato publicado resolve, o do exemplo |
| [`release.yml`](.github/workflows/release.yml) | tag `v*` | Alinha a versão, verifica e publica a Release com o jar do componente |
| [`verificar-jitpack.yml`](.github/workflows/verificar-jitpack.yml) | após o `Release` | Aguarda o JitPack construir o artefato, verifica o exemplo contra ele e registra a versão |

São **duas etapas** de propósito: o `release.yml` publica a Release e termina; o
`verificar-jitpack.yml` é disparado pela conclusão do primeiro. Sem essa separação
o ciclo seria circular — o release esperaria o JitPack, que só constrói depois da
Release. Localmente, `scripts/ci-local.sh` roda o mesmo gate do componente.

## 8. Solução de problemas (Windows)

- **`clean` falha com "Failed to delete ...\target\..."**: algum processo segura
  a pasta. Feche o NetBeans e instâncias do painel em execução, pause o OneDrive
  (se o projeto estiver em `Downloads`) e rode o build dentro do projeto. Também
  pode apagar manualmente: `rmdir /s /q target` (dentro do projeto).
- **`ClassNotFoundException` ao rodar pelo NetBeans**: a configuração de "Run"
  guardou um nome de classe antigo. Rode o arquivo com a classe `main` correta ou
  use `java -jar` (o `Main-Class` do jar está correto).
- **A janela não abre / erro de arena headless em testes**: o painel exige um
  ambiente gráfico para abrir a janela; os testes do componente são headless.

## 9. Documentação

Este README cobre o uso do componente. O projeto
[`painel-pedidos/README.md`](painel-pedidos/README.md) traz o mesmo guia de forma
mais enxuta, e o projeto [`exemplo`](exemplo/) é um consumidor completo - a melhor
referência para copiar.
